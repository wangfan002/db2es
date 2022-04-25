package com.db2es.task.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.db2es.task.job.entity.TaskJob;
import com.db2es.task.job.mapper.TaskJobMapper;
import com.db2es.task.job.service.CronTaskRegistrar;
import com.db2es.task.job.service.SchedulingRunnable;
import com.db2es.task.job.service.TaskJobService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务表(TaskJob)表服务实现类
 *
 * @author Fan.Wang
 * @since 2021-12-08 11:51:18
 */
@Service("taskJobService")
@DS("mysql")
@AllArgsConstructor
public class TaskJobServiceImpl extends ServiceImpl<TaskJobMapper, TaskJob> implements TaskJobService {

    private CronTaskRegistrar cronTaskRegistrar;

    @Override
    public boolean saveTaskJob(TaskJob taskJob)  {
        boolean save = this.save(taskJob);
        if (save) {
            SchedulingRunnable task = new SchedulingRunnable(taskJob.getBeanName(), taskJob.getMethodName(), taskJob.getMethodParams());
            cronTaskRegistrar.addCronTask(task, taskJob.getCronExpression());
        }
        return save;
    }

    @Override
    public boolean updateTaskJobById(TaskJob taskJob) {
        SchedulingRunnable removeTask = new SchedulingRunnable(taskJob.getBeanName(), taskJob.getMethodName(), taskJob.getMethodParams());
        cronTaskRegistrar.removeCronTask(removeTask);
        SchedulingRunnable addTask = new SchedulingRunnable(taskJob.getBeanName(), taskJob.getMethodName(), taskJob.getMethodParams());
        cronTaskRegistrar.addCronTask(addTask, taskJob.getCronExpression());
        return this.updateById(taskJob);
    }

    @Override
    public boolean removeTaskJobByIds(List<Long> idList) {
        for (Long id : idList) {
            TaskJob oldJob = this.getById(id);
            SchedulingRunnable removeTask = new SchedulingRunnable(oldJob.getBeanName(), oldJob.getMethodName(), oldJob.getMethodParams());
            cronTaskRegistrar.removeCronTask(removeTask);
        }
        return this.removeByIds(idList);
    }
}

