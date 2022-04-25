package com.db2es.core.sync.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.db2es.util.DataCleanUtils;
import com.db2es.core.sync.mapper.BigDataMapper;
import com.db2es.core.sync.mapper.assist.TriggerDeletedMapper;
import com.db2es.core.sync.service.Collector;
import lombok.AllArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des 法规
 */
@Service
@AllArgsConstructor
public class BigDataCollector implements Collector {
    private BigDataMapper bigDataMapper;
    private TriggerDeletedMapper triggerDeletedMapper;

    @Override
    public List<Map<String, Object>> getAllDataByPage(int size, int pageNum) {
        List<Map<String, Object>> dataByPage = bigDataMapper.getAllData(pageNum * size, pageNum * size + size);
        return cleanData(dataByPage);
    }

    private List<Map<String, Object>> cleanData(List<Map<String, Object>> dataByPage) {
        for (Map<String, Object> map : dataByPage) {
            DataCleanUtils.addLangFlag(map, "中文", "lang", "defaultRangeDoc");
        }
        return dataByPage;
    }


    @Override
    public List<Map<String, Object>> getAllData() {
        return null;
    }


    @Override
    public Long getMaxAuto() {
        return bigDataMapper.getMaxAuto();
    }

    @Override
    public Long getAllDataCount() {
        return bigDataMapper.getAllDataCount();
    }

    @Override
    public String getMaxLastUpdatedTime() {
        return bigDataMapper.getLastUpdatedTime();
    }


    @Override
    public List<Map<String, Object>> getAddByTime(String lastUpdatedTime) {
        List<Map<String, Object>> addByTime = bigDataMapper.getAddByTime(lastUpdatedTime);
        return cleanData(addByTime);
    }


    @Override
    public List<Map<String, Object>> getAddByIds(List<String> ids) {
        List<Map<String, Object>> addByIds = bigDataMapper.getAddByIds(ids);
        return cleanData(addByIds);
    }

    @Override
    public List<String> getIds() {
        return bigDataMapper.getIds();
    }

    @Override
    public RestHighLevelClient getEsClient() {
        return SpringUtil.getBean("cluster1Client");
    }

    @Override
    public String getMaxDelTime() {
        return triggerDeletedMapper.getMaxDelTime("big_1");
    }

    @Override
    public List<String> getDelIdsByTime(String delCycleTime) {
        return bigDataMapper.getDelIdsByTime(delCycleTime);
    }

    @Override
    public void delDirtyTriggerDataByTime(String delCycleTime) {
        triggerDeletedMapper.delDirtyTriggerDataByTime(delCycleTime, "big_1");
    }

}
