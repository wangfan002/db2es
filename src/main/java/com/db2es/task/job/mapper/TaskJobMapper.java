package com.db2es.task.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.db2es.task.job.entity.TaskJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务表(TaskJob)表数据库访问层
 *
 * @author Fan.Wang
 * @since 2021-12-08 11:51:18
 */
@Mapper
public interface TaskJobMapper extends BaseMapper<TaskJob> {

}

