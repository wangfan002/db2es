package com.db2es.core.sync.service;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.extra.spring.SpringUtil;
import com.db2es.core.sync.enums.IndexEnum;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.db2es.util.ListUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des 数据同步具体业务代码
 */
@Service
@Slf4j
public class DataSyncHandler {
    private static final String PROD_ACTIVE = "prod";
    ThreadPoolExecutor threadPoolExecutor;

    @Value("${elasticsearch.thread}")
    public void setThreads(Integer thread) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("db2es-%d").build();
        threadPoolExecutor = new ThreadPoolExecutor(thread, thread, 60,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(), threadFactory);
        log.info("当前环境的线程数为[{}]", thread);
    }

    private static final int ES_BULK_SIZE = 400;
    private final EsService esService;
    private final CollectorFactory collectorFactory;


    public DataSyncHandler(EsService esService, CollectorFactory collectorFactory) {
        this.esService = esService;
        this.collectorFactory = collectorFactory;
    }

    public Long syncAllDataByPage(String tempIndexName, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        Long allDbSize = collector.getAllDataCount();
        Long maxAuto = collector.getMaxAuto();
        if (maxAuto == null) {
            log.error("[{}]获取MaxAuto失败", indexEnum.getIndexName());
            return null;
        }
        long totalPage = (maxAuto - 1) / indexEnum.getPageSize() + 1;
        log.info("[{}] ============= DbSize: {}, maxAuto: {}, totalPage: {}", indexEnum.getIndexName(), allDbSize, maxAuto, totalPage);
        List<Integer> errorPages = new CopyOnWriteArrayList<>();
        List<CompletableFuture<Void>> cfList = new ArrayList<>();
        for (int i = 0; i < totalPage; i++) {
            final int page = i;
            CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
                try {
                    bulkInsertByPage(tempIndexName, indexEnum, collector, page);
                } catch (Exception e) {
                    log.error("{} 数据全量同步,部分失败 page = {}, error : {}", indexEnum.getIndexName(), page, e.getMessage(), e);
                    errorPages.add(page);
                }
            }, threadPoolExecutor);
            cfList.add(cf);
        }
        // 等待所有任务执行完毕
        CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0])).join();
        try {
            // 失败的page重试
            for (Integer errorPage : errorPages) {
                log.info("[{}] 重试写入errorPage :=== page: {}", indexEnum.getIndexName(), errorPage);
                bulkInsertByPage(tempIndexName, indexEnum, collector, errorPage);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return allDbSize;
    }

    private void bulkInsertByPage(String tempIndexName, IndexEnum indexEnum, Collector collector, int page) throws Exception {
        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = collector.getAllDataByPage(indexEnum.getPageSize(), page);
        long end1 = System.currentTimeMillis();
        if (!list.isEmpty()) {
            esService.bulkInsert(tempIndexName, indexEnum, list, collector.getEsClient());
            long end2 = System.currentTimeMillis();
            log.info("[{}] 写入ES :=== page: {}, size: {}, sqlserver cost : {}, es cost : {}", indexEnum.getIndexName(), page, list.size(), end1 - start, end2 - end1);
            list.clear();
        }
    }

    public Long syncAllData(String tempIndexName, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        String indexName = indexEnum.getIndexName();
        try {
            long start = System.currentTimeMillis();
            List<Map<String, Object>> list = collector.getAllData();
            int size = syncList(tempIndexName, indexEnum, collector, indexName, start, list);
            return (long) size;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private int syncList(String indexTrueName, IndexEnum indexEnum, Collector collector, String indexName, long start, List<Map<String, Object>> list) {
        int size = list.size();
        long end1 = System.currentTimeMillis();
        if (!list.isEmpty()) {
            List<List<Map<String, Object>>> split = ListUtil.split(list, ES_BULK_SIZE);
            log.info("[{}] 查询数据成功,数据量为 [{}],分[{}]page插入ES", indexName, size, split.size());
            List<CompletableFuture<Void>> cfList = new ArrayList<>();
            for (int i = 0; i < split.size(); i++) {
                int finalI = i;
                CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
                    try {
                        long es1 = System.currentTimeMillis();
                        esService.bulkInsert(indexTrueName, indexEnum, split.get(finalI), collector.getEsClient());
                        long es2 = System.currentTimeMillis();
                        log.info("[{}] 写入ES :=== page: {}, size: {}, es cost : {}", indexEnum.getIndexName(), finalI, split.get(finalI).size(), es2 - es1);
                    } catch (Exception e) {
                        log.error("{} 数据同步,部分失败 page = {}, error : {}", indexEnum.getIndexName(), finalI, e.getMessage(), e);
                    }
                });
                cfList.add(cf);
            }
            // 等待所有任务执行完毕
            CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0])).join();
            long end2 = System.currentTimeMillis();
            log.info("[{}] 写入ES sqlserver cost : {}, es cost : {}", indexName, end1 - start, end2 - end1);
        }
        return size;
    }

    public float checkDataCount(Long dataSize, String indexName, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        long tempSize = esService.getDocCount(indexName, collector.getEsClient());
        log.info("[{}]数据库数据量为[{}],[{}]索引数据量为[{}]", indexEnum.getIndexName(), dataSize, indexName, tempSize);
        return (float) Math.abs(dataSize - tempSize) / dataSize;
    }

    public long checkDataCount(String indexName, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        long esSize = esService.getDocCount(indexName, collector.getEsClient());
        Long dataSize = collector.getAllDataCount();
        log.info("[{}]数据库数据量为[{}],[{}]索引数据量为[{}]", indexEnum.getIndexName(), dataSize, indexName, esSize);
        return dataSize - esSize;
    }

    public String getLastUpdatedTime(IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        return collector.getMaxLastUpdatedTime();
    }

    public String getDelTime(IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        return collector.getMaxDelTime();
    }

    public boolean syncByTime(IndexEnum indexEnum, String lastUpdatedTime, String indexTrueName) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        try {
            long start = System.currentTimeMillis();
            List<Map<String, Object>> addIds = collector.getAddByTime(lastUpdatedTime);
            if (!addIds.isEmpty()) {
                syncList(indexTrueName, indexEnum, collector, indexEnum.getIndexName(), start, addIds);
                log.info("[{}] 数据增量成功,新增和更新共[{}]条数据", indexEnum.getIndexName(), addIds.size());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean delByTime(IndexEnum indexEnum, String delCycleTime, String indexTrueName) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        List<String> cnDelIds = collector.getDelIdsByTime(delCycleTime);
        return delByIds(indexTrueName, collector, cnDelIds);
    }

    public boolean delByDiffer(IndexEnum indexEnum, String indexTrueName) {
        TimeInterval timer = DateUtil.timer();
        String indexName = indexEnum.getIndexName();
        log.info("[{}] 对比索引id与数据库的差异开始...", indexName);
        Collector collector = collectorFactory.getCollector(indexEnum);
        List<String> esIds = esService.getIdsByIndexName(indexEnum, collector.getEsClient());
        long o1 = timer.intervalSecond();
        log.info("[{}] 查询ES的索引id所用时间为[{}]秒", indexName, o1);
        List<String> dbIds = collector.getIds();
        log.info("[{}] 查询DB的索引id所用时间为[{}]秒", indexName, timer.intervalSecond() - o1);
        List<String> esOverIds = ListUtils.getDifferListByMap(esIds, dbIds);
        return delByIds(indexTrueName, collector, esOverIds);
    }

    private boolean delByIds(String indexTrueName, Collector collector, List<String> delIds) {
        try {
            esService.bulkDelete(indexTrueName, delIds, collector.getEsClient());
            log.info("[{}] 删除了[{}]条数据", indexTrueName.substring(0, indexTrueName.lastIndexOf("_")), delIds.size());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }


    public boolean syncIndexById(IndexEnum indexEnum, List<String> ids, String indexTrueName) {
        Long timeStart = System.currentTimeMillis();
        Collector collector = collectorFactory.getCollector(indexEnum);
        String indexName = indexEnum.getIndexName();
        try {
            List<Map<String, Object>> list = collector.getAddByIds(ids);
            if (!list.isEmpty()) {
                esService.bulkInsert(indexTrueName, indexEnum, list, collector.getEsClient());
            }
            Long timeEnd = System.currentTimeMillis();
            log.info("[{}] 数据指定id同步结束,同步数据量为[{}],同步所用时间为[{}]秒", indexName, list.size(), (timeEnd - timeStart) / 1000);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean delIndexById(IndexEnum indexEnum, List<String> ids, String indexTrueName) {
        Long timeStart = System.currentTimeMillis();
        String indexName = indexEnum.getIndexName();
        try {
            if (!ids.isEmpty()) {
                Collector collector = collectorFactory.getCollector(indexEnum);
                esService.bulkDelete(indexTrueName, ids, collector.getEsClient());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        Long timeEnd = System.currentTimeMillis();
        log.info("[{}] 数据指定id删除结束,所用时间为[{}]秒", indexName, (timeEnd - timeStart) / 1000);
        return true;
    }


    public boolean createIndex(String tempIndexName, String mappingSource, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        return esService.createIndex(tempIndexName, mappingSource, collector.getEsClient());
    }

    public boolean updateIndexSetting(String tempIndexName, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        Integer numberOfReplicas = 0;
        if (PROD_ACTIVE.equals(SpringUtil.getActiveProfile())) {
            numberOfReplicas = indexEnum.getNumberOfReplicas();
        }
        Integer refreshInterval = indexEnum.getRefreshInterval();
        log.info("[{}] 更新索引的副本数为[{}],刷新间隔为[{}]", indexEnum.getIndexName(), numberOfReplicas, refreshInterval);
        return esService.updateIndexSetting(tempIndexName, numberOfReplicas, refreshInterval, collector.getEsClient());
    }

    public String getIndexNameByAlias(IndexEnum indexEnum) throws IOException {
        Collector collector = collectorFactory.getCollector(indexEnum);
        return esService.getCurrentIndexNameByAlias(indexEnum.getIndexName(), collector.getEsClient());
    }

    public boolean aliasIndex(String tempIndexName, String preIndexName, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        return esService.aliasIndex(tempIndexName, indexEnum.getIndexName(), preIndexName, collector.getEsClient());
    }

    public boolean deleteIndex(String indexTrueName, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        return esService.deleteIndex(indexTrueName, collector.getEsClient());
    }

    public boolean changeIndexVersion(IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        return esService.changeIndexVersion(indexEnum, collector.getEsClient());
    }

    public Map<String, List<String>> checkDifferIdsByIndexName(IndexEnum indexEnum) {
        TimeInterval timer = DateUtil.timer();
        String indexName = indexEnum.getIndexName();
        log.info("[{}] 对比索引id与数据库的差异开始...", indexName);
        Collector collector = collectorFactory.getCollector(indexEnum);
        CompletableFuture<List<String>> futureEsIds = CompletableFuture.supplyAsync(() -> esService.getIdsByIndexName(indexEnum, collector.getEsClient()))
                .whenComplete((r, e) -> log.info("[{}] 查询ES的索引id所用时间为[{}]秒", indexName, timer.intervalSecond()));
        CompletableFuture<List<String>> futureDbIds = CompletableFuture.supplyAsync(collector::getIds)
                .whenComplete((r, e) -> log.info("[{}] 查询DB的索引id所用时间为[{}]秒", indexName, timer.intervalSecond()));
        List<String> esIdList = futureEsIds.join();
        List<String> dbIdList = futureDbIds.join();
        Map<String, List<String>> map = new HashMap<>(2);
        List<String> dbOverIds = ListUtils.getDifferListByMap(dbIdList, esIdList);
        map.put("db-es", dbOverIds);
        log.info("[{}] 数据库比ES多的id为[{}]", indexName, dbOverIds);
        List<String> esOverIds = ListUtils.getDifferListByMap(esIdList, dbIdList);
        map.put("es-db", esOverIds);
        log.info("[{}] ES比数据库多的id为[{}]", indexName, esOverIds);
        log.info("[{}] 对比id所用总时间为[{}]秒", indexName, timer.intervalSecond());
        return map;
    }


    public void delDirtyTriggerDataByTime(IndexEnum indexEnum, String delCycleTime) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        collector.delDirtyTriggerDataByTime(delCycleTime);
    }

    public long getCurrentDocCount(String indexName, IndexEnum indexEnum) {
        Collector collector = collectorFactory.getCollector(indexEnum);
        return esService.getDocCount(indexName, collector.getEsClient());
    }

    public Set<String> getIndexesByPrefix(String prefix) throws IOException {
        return esService.getIndexesByPrefix(prefix);
    }
}
