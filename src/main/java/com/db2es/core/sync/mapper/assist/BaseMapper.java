package com.db2es.core.sync.mapper.assist;

import java.util.List;
import java.util.Map;

/**
 * @author Fan.Wang
 * @date 2022/4/12 15:42
 * @des 全量同步不用分page的索引, mapper是一样的
 */
public interface BaseMapper {

    /**
     * 获取全量数据
     *
     * @author Fan.Wang
     * @dateTime 2022/1/6 11:09
     * @return List
     */
    List<Map<String, Object>> getAllData();


    /**
     * 获取全量数据的数据量大小
     *
     * @dateTime 2022/1/6 12:15
     * @return Long
     */
    Long getAllDataCount();

    /**
     * 获取更新或新增的最大数据更新时间
     *
     * @author Fan.Wang
     * @dateTime 2022/1/6 11:30
     * @return java.lang.String
     */
    String getMaxUpdatedTime();

    /**
     * 根据时间获取数据
     *
     * @author Fan.Wang
     * @dateTime 2022/1/6 11:32
     * @param lastUpdatedTime 最后数据更新时间
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getAddByTime(String lastUpdatedTime);

    /**
     * 根据id获取数据
     *
     * @author Fan.Wang
     * @dateTime 2022/1/6 11:35
     * @param ids docIDs
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getAddByIds(List<String> ids);

    /**
     * 获取所有id
     *
     * @author Fan.Wang
     * @dateTime 2022/1/11 16:19
     * @return java.util.List<java.lang.String>
     */
    List<String> getIds();
}  