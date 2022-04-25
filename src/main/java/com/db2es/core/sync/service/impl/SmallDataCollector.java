package com.db2es.core.sync.service.impl;

import com.db2es.util.DataCleanUtils;
import com.db2es.core.sync.mapper.SmallDataMapper;
import com.db2es.core.sync.service.Collector;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Fan.Wang
 * @date 2022/1/14 15:03
 * @des Article
 */
@Service
@AllArgsConstructor
public class SmallDataCollector implements Collector {
    private SmallDataMapper smallDataMapper;

    @Override
    public List<Map<String, Object>> getAllData() {
        List<Map<String, Object>> allData = smallDataMapper.getAllData();
        return cleanData(allData);
    }

    private List<Map<String, Object>> cleanData(List<Map<String, Object>> allData) {
        for (Map<String, Object> map : allData) {
            DataCleanUtils.addYearCol(map,"releaseDate","releaseYear");
        }
        return allData;
    }


    @Override
    public Long getAllDataCount() {
        return smallDataMapper.getAllDataCount();
    }

    @Override
    public String getMaxLastUpdatedTime() {
        return smallDataMapper.getMaxUpdatedTime();
    }

    @Override
    public List<Map<String, Object>> getAddByTime(String lastUpdatedTime) {
        List<Map<String, Object>> addByTime = smallDataMapper.getAddByTime(lastUpdatedTime);
        return cleanData(addByTime);
    }

    @Override
    public List<Map<String, Object>> getAddByIds(List<String> ids) {
        List<Map<String, Object>> addByIds = smallDataMapper.getAddByIds(ids);
        return cleanData(addByIds);
    }

    @Override
    public List<String> getIds() {
        return smallDataMapper.getIds();
    }
}