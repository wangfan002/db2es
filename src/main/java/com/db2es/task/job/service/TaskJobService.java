package com.db2es.task.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.db2es.task.job.entity.TaskJob;

import java.util.List;

/**
 * 定时任务表(TaskJob)表服务接口
 *
 * @author Fan.Wang
 * @since 2021-12-08 11:51:18
 */
public interface TaskJobService extends IService<TaskJob> {
    /**
     * 新增定时任务
     *
     * @param taskJob 任务实体
     * @return boolean
     * @author Fan.Wang
     * @dateTime 2021/12/8 14:31
     */
    boolean saveTaskJob(TaskJob taskJob);

    /**
     * 新增定时任务
     *
     * @author Fan.Wang
     * @dateTime 2021/12/8 14:48
     * @param taskJob 任务实体
     * @return boolean
     */
    boolean updateTaskJobById(TaskJob taskJob);

    /**
     * 根据id删除任务
     *
     * @author Fan.Wang
     * @dateTime 2021/12/8 14:53
     * @param idList 待删除id集合
     * @return boolean
     */
    boolean removeTaskJobByIds(List<Long> idList);

}

