package com.db2es.core.sync.mapper.assist;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Fan.Wang
 * @date 2022/1/28 14:38
 * @des 通过触发器删除
 */
@Mapper
public interface TriggerDeletedMapper {

    /**
     * 获取数据删除表最大更新时间
     *
     * @param type 文档类型
     * @return java.lang.String
     * @author Fan.Wang
     * @dateTime 2021/1/28 16:30
     */
    @Select("SELECT max(delTime) FROM trigger_deleted WHERE type = #{type}")
    String getMaxDelTime(String type);

    /**
     * 删除上次已经用过的id 无用id,以及其余暂时不使用trigger_deleted表索引的脏数据
     *
     * @param delCycleTime 上次记录的最大删除时间
     * @param type      该索引的type
     * @author Fan.Wang
     * @dateTime 2022/1/28 10:36
     */
    @Delete("DELETE FROM trigger_deleted where type = #{type} and delTime < #{delCycleTime}")
    void delDirtyTriggerDataByTime(String delCycleTime, String type);
}  