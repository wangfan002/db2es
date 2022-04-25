package com.db2es.core.sync.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.db2es.core.info.entity.IndexCycleInfo;
import com.db2es.core.info.service.IndexCycleInfoService;
import com.db2es.core.sync.enums.IndexEnum;
import com.db2es.apiresult.ApiResult;
import com.db2es.core.sync.exception.IndexSyncException;
import com.db2es.task.record.constant.Constant;
import com.db2es.task.record.entity.TaskRecord;
import com.db2es.task.record.service.TaskRecordService;
import com.db2es.util.DingMessageUtils;
import com.db2es.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:49
 * @des 索引同步业务接口
 */
@Service
@Slf4j
public class SyncService implements Constant {
    private static final String PROD_ACTIVE = "prod";

    /**
     * 防止对同一个index重复重建
     */
    private static final Set<IndexEnum> INDEX_DOING_SET = new CopyOnWriteArraySet<>();

    public static Set<IndexEnum> getIndexDoingSet() {
        return INDEX_DOING_SET;
    }

    private final DataSyncHandler dataSyncHandler;
    private final IndexCycleInfoService indexCycleInfoService;
    private final TaskRecordService taskRecordService;
    // 检测同步是否成功前需要等待 数据刷进os cache
    private final static int TIN_WAIT_MILLIS = 2 * 1000;
    private final static int LONG_WAIT_MILLIS = 10 * 1000;

    public SyncService(DataSyncHandler dataSyncHandler, IndexCycleInfoService indexCycleInfoService, TaskRecordService taskRecordService) {
        this.dataSyncHandler = dataSyncHandler;
        this.indexCycleInfoService = indexCycleInfoService;
        this.taskRecordService = taskRecordService;
    }

    @Value("${mail.to}")
    private String mailSendTo;

    @Value("${spring.profiles.active}")
    private String activeEnv;

    public void rebuildIndex(IndexEnum indexEnum) {
        Long timeStart = System.currentTimeMillis();
        String indexName = indexEnum.getIndexName();
        TaskRecord taskRecord = new TaskRecord().setIndexName(indexName).setEventType(REBUILD).setStatus(DOING);
        taskRecordService.save(taskRecord);
        if (INDEX_DOING_SET.contains(indexEnum)) {
            recordError(indexEnum, indexName, taskRecord, "正在进行同步,请确认", true, false);
        }
        INDEX_DOING_SET.add(indexEnum);
        log.info("========= 开始同步全量 [{}] 数据 ===============", indexName);
        String tempIndexName = indexName + "_" + DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN);
        log.info("[{}]: 临时索引名为: [{}]", indexName, tempIndexName);
        // 1.get mappingSource  2.create index
        prepareWork(indexEnum, indexName, taskRecord, tempIndexName);
        // 提前生成第7步需要的时间 避免同步期间有数据库增加新数据
        String lastUpdatedTime = dataSyncHandler.getLastUpdatedTime(indexEnum);
        // 3.sync data
        boolean isPage = indexEnum.getPageSize() != null;
        Long dbSize;
        dbSize = doSync(indexEnum, indexName, taskRecord, tempIndexName, isPage);
        //  4.update setting
        boolean isUpdateSettingSuccess = dataSyncHandler.updateIndexSetting(tempIndexName, indexEnum);
        if (!isUpdateSettingSuccess) {
            recordError(indexEnum, indexName, taskRecord, "更新setting失败", true, true);
        }
        // 5.check indexSize  6.change indexAliasMapping
        shiftIndex(indexEnum, indexName, taskRecord, tempIndexName, isPage, dbSize);
        // 7.写入同步数据的最后时间,为了以后的增量同步
        if (!indexEnum.getIsOnlyRebuild()) {
            saveCycleInfo(indexName, tempIndexName, lastUpdatedTime, isPage);
        }
        // 8.修改索引version
        boolean isVersionChangeSuccess = dataSyncHandler.changeIndexVersion(indexEnum);
        if (!isVersionChangeSuccess) {
            recordError(indexEnum, indexName, taskRecord, "索引version修改失败", false, true);
        }
        Long timeEnd = System.currentTimeMillis();
        recordSuccess(indexEnum, indexName, taskRecord, StrUtil.format("数据全量同步成功,用时[{}]s", (timeEnd - timeStart) / 1000), true);
    }

    private void prepareWork(IndexEnum indexEnum, String indexName, TaskRecord taskRecord, String tempIndexName) {
        // 1.get mappingSource
        String mappingSource = JsonUtils.getJsonStringSource(indexName);
        if (StringUtils.isBlank(mappingSource)) {
            recordError(indexEnum, indexName, taskRecord, "mapping不存在", true, true);
        }
        // 2.create index
        boolean isCreateSuccess = dataSyncHandler.createIndex(tempIndexName, mappingSource, indexEnum);
        if (!isCreateSuccess) {
            recordError(indexEnum, indexName, taskRecord, "索引创建失败", true, true);
        }
    }

    private Long doSync(IndexEnum indexEnum, String indexName, TaskRecord taskRecord, String tempIndexName, boolean isPage) {
        Long dbSize;
        if (isPage) {
            // 分页方式同步
            dbSize = dataSyncHandler.syncAllDataByPage(tempIndexName, indexEnum);
            if (dbSize == null) {
                recordError(indexEnum, indexName, taskRecord, "数据全量同步失败-分页方式", true, true);
            }
        } else {
            // 直接查询同步
            dbSize = dataSyncHandler.syncAllData(tempIndexName, indexEnum);
            if (dbSize == null) {
                recordError(indexEnum, indexName, taskRecord, "数据全量同步失败-不分页方式", true, true);
            }
        }
        return dbSize;
    }

    private void shiftIndex(IndexEnum indexEnum, String indexName, TaskRecord taskRecord, String tempIndexName, boolean isPage, Long dbSize) {
        // 数据量大的话需要多等待时间 然后才能验证数量
        if (isPage) {
            ThreadUtil.sleep(LONG_WAIT_MILLIS);
        } else {
            ThreadUtil.sleep(TIN_WAIT_MILLIS);
        }
        // 5.check indexSize
        float differ = dataSyncHandler.checkDataCount(dbSize, tempIndexName, indexEnum);
        // 数据库数据量比ES少没有问题 可能是同步过程中有增量被索引了
        // ES比数据库数据量缺少量数据可能是 同步过程中有减量被索引了
        // 但是如果ES比数据库少很多就有问题
        float countDiffFlag = 0.001f;
        if (differ > countDiffFlag) {
            log.info("[{}]: 等待[{}]ms,再次检测", indexName, TIN_WAIT_MILLIS);
            ThreadUtil.sleep(TIN_WAIT_MILLIS);
            if (dataSyncHandler.checkDataCount(dbSize, tempIndexName, indexEnum) > countDiffFlag) {
                recordError(indexEnum, indexName, taskRecord, "数据量差异较大,不切换索引", true, true);
            }
        }
        // 6.change indexAliasMapping
        String preIndexName = null;
        try {
            preIndexName = dataSyncHandler.getIndexNameByAlias(indexEnum);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            recordError(indexEnum, indexName, taskRecord, "获取索引trueName失败", true, true);
        }
        boolean isAliasSuccess = dataSyncHandler.aliasIndex(tempIndexName, preIndexName, indexEnum);
        if (!isAliasSuccess) {
            recordError(indexEnum, indexName, taskRecord, "索引alias失败", true, true);
        }
        if (StrUtil.isNotBlank(preIndexName)) {
            log.info("[{}]: 索引alias成功 | {} --> {}, remove alias : {} ", indexName, tempIndexName, indexName, preIndexName);
        } else {
            log.info("[{}]: 索引alias成功 | {} --> {} ", indexName, tempIndexName, indexName);
        }

    }

    private void saveCycleInfo(String indexName, String tempIndexName, String lastUpdatedTime, boolean isPage) {
        IndexCycleInfo indexCycleInfo = new IndexCycleInfo();
        indexCycleInfo.setIndexName(indexName).setCycleTime(lastUpdatedTime).setDelCycleTime(isPage ? lastUpdatedTime : null).setIndexTrueName(tempIndexName);
        indexCycleInfoService.saveOrUpdate(indexCycleInfo, Wrappers.<IndexCycleInfo>query().eq("index_name", indexName));
        log.info("[{}]: 数据最大更新日期已写入[{}]", indexName, lastUpdatedTime);
    }

    public void syncIndexByTime(IndexEnum indexEnum) {
        Long timeStart = System.currentTimeMillis();
        String indexName = indexEnum.getIndexName();
        TaskRecord taskRecord = new TaskRecord().setIndexName(indexName).setEventType(SYNC_BY_TIME).setStatus(DOING);
        taskRecordService.save(taskRecord);
        if (indexEnum.getIsOnlyRebuild()) {
            recordError(indexEnum, indexName, taskRecord, "该索引仅支持重建同步", true, true);
        }
        if (INDEX_DOING_SET.contains(indexEnum)) {
            recordError(indexEnum, indexName, taskRecord, "正在进行同步,请确认", true, false);
        }
        INDEX_DOING_SET.add(indexEnum);
        log.info("========= 开始同步增量 [{}] 数据 ===============", indexName);
        // 1.get mappingSource
        String mappingSource = JsonUtils.getJsonStringSource(indexName);
        if (StringUtils.isBlank(mappingSource)) {
            recordError(indexEnum, indexName, taskRecord, "mapping不存在", true, true);
        }
        // 2.sync data
        IndexCycleInfo cycleInfo = indexCycleInfoService.getOne(Wrappers.<IndexCycleInfo>query().select("index_true_name", "cycle_time", "del_cycle_time").eq("index_name", indexName));
        String indexTrueName = null;
        try {
            indexTrueName = dataSyncHandler.getIndexNameByAlias(indexEnum);
            Validator.validateNotNull(indexTrueName, "获取{}索引trueName为Null", indexName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            recordError(indexEnum, indexName, taskRecord, "获取索引trueName失败", true, true);
        }
        String lastUpdatedTime = cycleInfo.getCycleTime();
        long currentDocCount = dataSyncHandler.getCurrentDocCount(indexName, indexEnum);
        log.info("[{}]:索引当前数据量为[{}],数据从[{}]时间继续更新或新增", indexName, currentDocCount, lastUpdatedTime);
        // 提前生成时间 避免同步期间有数据库增加新数据
        String newLastUpdatedTime = dataSyncHandler.getLastUpdatedTime(indexEnum);

        boolean isSyncSuccess = dataSyncHandler.syncByTime(indexEnum, lastUpdatedTime, indexTrueName);
        if (!isSyncSuccess) {
            recordError(indexEnum, indexName, taskRecord, "数据增量同步失败", true, true);
        }
        log.info("[{}]: 增量数据同步成功,最大更新日期更新为[{}]", indexName, newLastUpdatedTime);
        // 3.del data
        UpdateWrapper<IndexCycleInfo> updateWrapper = delData(indexEnum, indexName, taskRecord, cycleInfo, newLastUpdatedTime, indexTrueName);
        // 4.check indexSize
        ThreadUtil.sleep(TIN_WAIT_MILLIS);
        checkIndexSize(indexEnum, indexName, taskRecord);
        indexCycleInfoService.update(updateWrapper);
        // 5.修改索引version
        boolean isVersionChangeSuccess = dataSyncHandler.changeIndexVersion(indexEnum);
        if (!isVersionChangeSuccess) {
            recordError(indexEnum, indexName, taskRecord, "索引version修改失败", false, true);
        }
        Long timeEnd = System.currentTimeMillis();
        recordSuccess(indexEnum, indexName, taskRecord, StrUtil.format("数据增量同步完成,用时[{}]s", (timeEnd - timeStart) / 1000), false);
    }

    private void checkIndexSize(IndexEnum indexEnum, String indexName, TaskRecord taskRecord) {
        long differ = dataSyncHandler.checkDataCount(indexName, indexEnum);
        int countDiffFlag = 5;
        if (Math.abs(differ) > countDiffFlag) {
            if (indexEnum.getIsDelByTime()) {
                log.info("[{}]: 等待[{}]ms,再次检测", indexName, 2 * LONG_WAIT_MILLIS);
                // 像案例一次增量上万的短睡眠时间不够
                ThreadUtil.sleep(2 * LONG_WAIT_MILLIS);
            }
            differ = dataSyncHandler.checkDataCount(indexName, indexEnum);
            if (Math.abs(differ) > countDiffFlag) {
                recordError(indexEnum, indexName, taskRecord, StrUtil.format("增量同步后与数据库数量相差{}条以上,db-es=[{}]", countDiffFlag, differ), false, true);
            }
        }
    }

    private UpdateWrapper<IndexCycleInfo> delData(IndexEnum indexEnum, String indexName, TaskRecord taskRecord, IndexCycleInfo cycleInfo, String newLastUpdatedTime, String indexTrueName) {
        UpdateWrapper<IndexCycleInfo> updateWrapper = new UpdateWrapper<IndexCycleInfo>().eq("index_name", indexName)
                .set("cycle_time", newLastUpdatedTime);
        if (indexEnum.getIsDelByTime()) {
            String delCycleTime = cycleInfo.getDelCycleTime();
            // 清理之前记录的已经删除的id
            dataSyncHandler.delDirtyTriggerDataByTime(indexEnum, delCycleTime);
            log.info("[{}]数据从[{}]开始删除", indexName, delCycleTime);
            boolean isDelSuccess = dataSyncHandler.delByTime(indexEnum, delCycleTime, indexTrueName);
            if (!isDelSuccess) {
                recordError(indexEnum, indexName, taskRecord, "数据删除失败", true, true);
            }
            String newDelTime = dataSyncHandler.getDelTime(indexEnum);
            String trueDelTime = StrUtil.compare(newDelTime, delCycleTime, true) > 0 ? newDelTime : delCycleTime;
            updateWrapper.set("del_cycle_time", trueDelTime);
            log.info("[{}]: 数据删除成功,删除数据最大更新日期为[{}]", indexName, trueDelTime);
        } else {
            log.info("[{}]数据通过ES和DB对比方式删除", indexName);
            boolean isDelSuccess = dataSyncHandler.delByDiffer(indexEnum, indexTrueName);
            if (!isDelSuccess) {
                recordError(indexEnum, indexName, taskRecord, "数据删除失败", true, true);
            }
            log.info("[{}]: 数据删除成功", indexName);
        }
        return updateWrapper;
    }


    public ApiResult syncIndexById(IndexEnum indexEnum, List<String> ids) {
        if (indexEnum.getIsOnlyRebuild()) {
            return ApiResult.failure("该索引仅支持重建同步");
        }
        String indexName = indexEnum.getIndexName();
        if (INDEX_DOING_SET.contains(indexEnum)) {
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("正在进行同步,请确认!");
        }
        INDEX_DOING_SET.add(indexEnum);
        log.info("========= 开始指定docID同步 [{}] 数据 ===============", indexName);
        // 1.get mappingSource
        String mappingSource = JsonUtils.getJsonStringSource(indexName);
        if (StringUtils.isBlank(mappingSource)) {
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("mapping不存在!");
        }
        // 2.sync data
        String indexTrueName;
        try {
            indexTrueName = dataSyncHandler.getIndexNameByAlias(indexEnum);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("获取索引trueName失败!");
        }
        boolean isSyncSuccess = dataSyncHandler.syncIndexById(indexEnum, ids, indexTrueName);
        if (!isSyncSuccess) {
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("指定id同步失败!");
        }
        // 3.修改索引version
        boolean isVersionChangeSuccess = dataSyncHandler.changeIndexVersion(indexEnum);
        if (!isVersionChangeSuccess) {
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("索引version修改失败!");
        }
        INDEX_DOING_SET.remove(indexEnum);
        return ApiResult.success("指定id同步成功");
    }

    public ApiResult delIndexById(IndexEnum indexEnum, List<String> ids) {
        if (indexEnum.getIsOnlyRebuild()) {
            return ApiResult.failure("该索引仅支持重建同步");
        }
        String indexName = indexEnum.getIndexName();
        if (INDEX_DOING_SET.contains(indexEnum)) {
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("正在进行同步,请确认");
        }
        INDEX_DOING_SET.add(indexEnum);
        log.info("========= 开始指定docID删除 [{}] 数据 ===============", indexName);
        // delete data
        String indexTrueName;
        try {
            indexTrueName = dataSyncHandler.getIndexNameByAlias(indexEnum);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("获取索引trueName失败");
        }
        boolean isDelSuccess = dataSyncHandler.delIndexById(indexEnum, ids, indexTrueName);
        if (!isDelSuccess) {
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("指定id删除失败");
        }
        // 修改索引version
        boolean isVersionChangeSuccess = dataSyncHandler.changeIndexVersion(indexEnum);
        if (!isVersionChangeSuccess) {
            INDEX_DOING_SET.remove(indexEnum);
            return ApiResult.failure("索引version修改失败!");
        }
        INDEX_DOING_SET.remove(indexEnum);
        return ApiResult.success("指定id删除成功");
    }

    public void rebuildMultiIndex(List<String> indexNames) {
        for (String indexName : indexNames) {
            IndexEnum indexEnum = IndexEnum.getByName(indexName);
            if (indexEnum != null) {
                rebuildIndex(indexEnum);
            }
        }
    }

    private void recordError(IndexEnum indexEnum, String indexName, TaskRecord taskRecord, String errorMsg, boolean isStop, boolean isReleaseExclusive) {
        String errorInfo = StrUtil.format("ES 索引同步异常--[{}] [{}]", indexName, errorMsg);
        log.error(errorInfo);
        if (isReleaseExclusive) {
            INDEX_DOING_SET.remove(indexEnum);
        }
        taskRecordService.saveOrUpdate(taskRecord.setStatus(FAIL).setRemark(errorMsg));
        MailAccount mailAccount = SpringUtil.getBean("getMailAccount");
        MailUtil.send(mailAccount, mailSendTo, "[" + activeEnv + "环境]" + indexName + "索引同步异常", errorInfo, false);
        if (PROD_ACTIVE.equals(SpringUtil.getActiveProfile())) {
            DingMessageUtils.sendMessage(indexName + ":索引同步异常," + errorInfo);
        }
        if (isStop) {
            throw new IndexSyncException(errorInfo);
        }
    }


    private void recordSuccess(IndexEnum indexEnum, String indexName, TaskRecord taskRecord, String successMsg, boolean isSendMail) {
        log.info("索引[{}]: [{}]", indexName, successMsg);
        INDEX_DOING_SET.remove(indexEnum);
        taskRecordService.saveOrUpdate(taskRecord.setStatus(SUCCESS).setRemark(successMsg));
        if (isSendMail) {
            MailAccount mailAccount = SpringUtil.getBean("getMailAccount");
            MailUtil.send(mailAccount, mailSendTo, "[" + activeEnv + "环境]" + indexName + ":索引同步成功", successMsg, false);
            if (PROD_ACTIVE.equals(SpringUtil.getActiveProfile())) {
                DingMessageUtils.sendMessage(indexName + ":索引同步成功," + successMsg);
            }
        }
    }
}