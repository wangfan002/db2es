package com.db2es.core.sync.enums;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;

import java.util.List;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des 存储索引信息
 */
public enum IndexEnum {

    // 数据量较大 全量需要多线程 分批同步 (最佳实践是每批10M)
    BIG_DATA("big_data", 200, true, false, "id", 5, 1),
    // 数据量较小 直接全量同步
    SMALL_DATA("small_data", null, false, false, "id", 5, 0);

    /**
     * 索引名
     */
    @Getter
    final String indexName;

    /**
     * 每个线程处理的数据量
     */
    @Getter
    final Integer pageSize;

    /**
     * 是否按时间删除索引(否则按ES和数据库对比删除)
     */
    @Getter
    final Boolean isDelByTime;

    /**
     * 是否只有全量同步
     */
    @Getter
    final Boolean isOnlyRebuild;

    /**
     * ES文档的id
     */
    @Getter
    final String esIdField;

    /**
     * 将index buffer刷新到os cache的时间间隔
     */
    @Getter
    final Integer refreshInterval;

    /**
     * 索引副本数
     */
    @Getter
    final Integer numberOfReplicas;

    IndexEnum(String indexName, Integer pageSize, Boolean isDelByTime, Boolean isOnlyRebuild, String esIdField, Integer refreshInterval, Integer numberOfReplicas) {
        this.indexName = indexName;
        this.pageSize = pageSize;
        this.isDelByTime = isDelByTime;
        this.isOnlyRebuild = isOnlyRebuild;
        this.esIdField = esIdField;
        this.refreshInterval = refreshInterval;
        this.numberOfReplicas = numberOfReplicas;
    }

    public static IndexEnum getByName(String indexName) {
        for (IndexEnum indexEnum : values()) {
            if (indexName.equals(indexEnum.indexName)) {
                return indexEnum;
            }
        }
        return null;
    }

    public static List<String> getAllIndexName() {
        List<String> list = CollUtil.newArrayList();
        for (IndexEnum indexEnum : values()) {
            list.add(indexEnum.indexName);
        }
        return list;
    }


}