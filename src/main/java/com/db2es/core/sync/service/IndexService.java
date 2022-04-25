package com.db2es.core.sync.service;

import cn.hutool.core.util.StrUtil;
import com.db2es.core.sync.enums.IndexEnum;
import com.db2es.apiresult.ApiResult;
import com.db2es.task.record.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:49
 * @des 索引操作业务接口
 */
@Service
@AllArgsConstructor
@Slf4j
public class IndexService implements Constant {

    private final DataSyncHandler dataSyncHandler;

    public ApiResult deleteIndexByName(IndexEnum indexEnum, String indexTrueName) {
        try {
            String indexNameByAlias = dataSyncHandler.getIndexNameByAlias(indexEnum);
            if (StrUtil.isNotEmpty(indexNameByAlias) && indexNameByAlias.equals(indexTrueName)) {
                return ApiResult.failure(StrUtil.format("索引[{}]正在被引用无法删除!", indexTrueName));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ApiResult.failure(StrUtil.format("获取[{}]索引trueName失败!", indexEnum.getIndexName()));
        }
        boolean isDelete = dataSyncHandler.deleteIndex(indexTrueName, indexEnum);
        if (isDelete) {
            return ApiResult.success(StrUtil.format("[{}]索引删除成功!", indexTrueName));
        } else {
            return ApiResult.failure(StrUtil.format("[{}]索引删除失败!", indexTrueName));
        }
    }


    public Map<String, List<String>> checkDifferIdsByIndexName(IndexEnum indexEnum) {
        return dataSyncHandler.checkDifferIdsByIndexName(indexEnum);
    }

    public ApiResult aliasIndex(IndexEnum indexEnum, String indexTrueName) {
        String preIndexName;
        try {
            preIndexName = dataSyncHandler.getIndexNameByAlias(indexEnum);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ApiResult.failure("获取当前索引trueName失败!");
        }
        boolean isAliasSuccess = dataSyncHandler.aliasIndex(indexTrueName, preIndexName, indexEnum);
        if (!isAliasSuccess) {
            return ApiResult.failure("索引alias失败!");
        }
        log.info("[{}]: 索引alias成功 | {} --> {}, remove alias : {} ", indexEnum.getIndexName(), indexTrueName, indexEnum.getIndexName(), preIndexName);
        return ApiResult.success("索引alias成功!");
    }

    public ApiResult deleteIndexByPrefix(String prefix) {
        List<String> list = new ArrayList<>();
        String systemPrefix = ".";
        if (prefix.startsWith(systemPrefix)) {
            return ApiResult.failure(StrUtil.format("请勿删除系统元索引!"));
        }
        Set<IndexEnum> indexDoingSet = SyncService.getIndexDoingSet();
        if (!indexDoingSet.isEmpty()) {
            return ApiResult.failure(StrUtil.format("系统当前有索引正在进行同步,请稍候再试!"));
        }
        Set<String> prefixIndexes;
        try {
            prefixIndexes = dataSyncHandler.getIndexesByPrefix(prefix);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ApiResult.failure(StrUtil.format("根据前缀获取索引失败"));
        }
        for (String indexTrueName : prefixIndexes) {
            String indexName = indexTrueName.substring(0, indexTrueName.lastIndexOf("_"));
            ApiResult apiResult = deleteIndexByName(IndexEnum.getByName(indexName), indexTrueName);
            if (apiResult.isSuccess()) {
                list.add(indexTrueName);
            }
        }
        return ApiResult.success(list.isEmpty() ? "无可删除索引" : list);
    }

    public ApiResult checkIndexMiss() {
        List<String> list = new ArrayList<>();
        for (IndexEnum indexEnum : IndexEnum.values()) {
            try {
                if (StrUtil.isEmpty(dataSyncHandler.getIndexNameByAlias(indexEnum))) {
                    list.add(indexEnum.getIndexName());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return ApiResult.failure(StrUtil.format("获取[{}]索引trueName失败!", indexEnum.getIndexName()));
            }
        }
        return ApiResult.success(list.isEmpty() ? "索引均已创建" : list);
    }
}