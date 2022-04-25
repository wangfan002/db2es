package com.db2es.core.sync.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Fan.Wang
 * @date 2021/11/19 14:58
 * @des big_data数据查询的接口
 */
public interface BigDataMapper {

    /**
     * 查询自增列的最大值
     * @dateTime 2021/11/23 12:12
     * @return java.lang.Long
     */
    @Select("SELECT max(id) FROM big_data")
    Long getMaxAuto();

    /**
     * 根据自增列的范围查询数据
     * @dateTime 2021/11/23 12:13
     * @param offsetNum 起始值
     * @param endNum 终止值
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getAllData(int offsetNum, int endNum);


    /**
     * 获取全量数据的数据量大小
     * @dateTime 2021/11/23 12:15
     * @return int
     */
    @Select("SELECT count(1) FROM big_data")
    Long getAllDataCount();

    /**
     * 获取更新或新增的最大数据更新时间
     * @dateTime 2021/11/23 13:46
     * @return String
     */
    String getLastUpdatedTime();

    /**
     * 根据时间获取数据
     * @dateTime 2021/11/23 16:01
     * @param lastUpdatedTime 最后数据更新时间
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getAddByTime(String lastUpdatedTime);

    /**
     * 根据id获取数据
     * @dateTime 2021/11/24 13:39
     * @param ids docIDs
     * @return String
     */
    List<Map<String, Object>> getAddByIds(List<String> ids);

    /**
     * 获取所有id
     *
     * @author Fan.Wang
     * @dateTime 2022/1/7 11:21
     * @return java.util.List<java.lang.String>
     */
    @Select("SELECT t1.id as id FROM big_data t1 WITH (NOLOCK)")
    List<String> getIds();

    /**
     * 在原表里查一下确保真的删除了
     * @dateTime 2021/11/24 11:43
     * @param delCycleTime 最后数据更新时间
     * @return java.util.List<java.lang.String>
     */
    @Select("SELECT distinct docID FROM trigger_deleted WHERE delTime > #{delCycleTime} and type = 'big_1' " +
            "and id not in (select id FROM big_data WITH (NOLOCK))")
    List<String> getDelIdsByTime(String delCycleTime);
}