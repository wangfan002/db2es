package com.db2es.core.sync.service;

import com.db2es.core.sync.enums.IndexEnum;
import com.db2es.core.sync.service.impl.BigDataCollector;
import com.db2es.core.sync.service.impl.SmallDataCollector;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des 获取Collector
 */
@Service
@AllArgsConstructor
public class CollectorFactory {
    private final BigDataCollector bigDataCollector;
    private final SmallDataCollector smallDataCollector;

    public Collector getCollector(IndexEnum indexEnum) {
        switch (indexEnum) {
            case BIG_DATA:
                return bigDataCollector;
            case SMALL_DATA:
                return smallDataCollector;
            default:
                return null;
        }
    }
}
