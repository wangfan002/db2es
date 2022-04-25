package com.db2es.core.sync.service;

import cn.hutool.extra.spring.SpringUtil;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Fan.Wang
 * @date 2021/11/18 17:24
 * @des 工厂接口
 */
public interface Collector {

    /**
     * 分页获取全量待同步数据
     * @dateTime 2021/11/22 17:13
     * @param size 每个线程处理的数据量
     * @param pageNum 线程处理的page下标
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    default List<Map<String, Object>> getAllDataByPage(int size, int pageNum){
        return new ArrayList<>();
    }

    /**
     * 获取全量待同步数据
     * @dateTime 2021/11/22 17:13
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getAllData();

    /**
     * 获取自增列的最大值
     * @dateTime 2021/11/22 17:18
     * @return java.lang.Long
     */
    default Long getMaxAuto(){
        return null;
    }

    /**
     * 获取全量数据个数
     * @dateTime 2021/11/22 17:18
     * @return long
     */
    Long getAllDataCount();

    /**
     * 获取数据最后的更新时间
     * @dateTime 2021/11/23 12:42
     * @return java.lang.String
     */
    String getMaxLastUpdatedTime();

    /**
     * 增量数据获取
     * @dateTime 2021/11/23 15:57
     * @param lastUpdatedTime 数据最后的更新时间
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getAddByTime(String lastUpdatedTime);

    /**
     * 获取待同步的数据
     * @dateTime 2021/11/24 13:36
     * @param ids 待同步的数据id
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getAddByIds(List<String> ids);

    /**
     * 根据索引名称获取索引id
     *
     * @author Fan.Wang
     * @dateTime 2022/1/7 11:12
     * @return java.util.List<java.lang.String>
     */
    List<String> getIds();

    /**
     * 获取ESClient
     *
     * @author Fan.Wang
     * @dateTime 2021/12/21 12:15
     * @return org.elasticsearch.client.RestHighLevelClient
     */
    default RestHighLevelClient getEsClient(){
        return SpringUtil.getBean("cluster2Client");
    }

    /**
     * 获取数据最后的删除时间
     * @dateTime 2021/11/23 12:42
     * @return java.lang.String
     */
    default String getMaxDelTime(){
        return null;
    }

    /**
     * 获取待删除的docIds
     * @dateTime 2021/11/24 11:35
     * @param delCycleTime 数据最后的更新时间
     * @return java.util.List<java.lang.String>
     */
    default List<String> getDelIdsByTime(String delCycleTime){
        return new ArrayList<>();
    }

    /**
     * 删除上次已经用过的id
     *
     * @author Fan.Wang
     * @dateTime 2022/1/26 10:36
     * @param delCycleTime 上次记录的最大删除时间
     */
    default void delDirtyTriggerDataByTime(String delCycleTime){
    }
}
