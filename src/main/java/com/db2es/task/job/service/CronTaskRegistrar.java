package com.db2es.task.job.service;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Fan.Wang
 * @date 2021/12/7 16:32
 * @des 定时任务注册类，用来增加、删除定时任务
 */
@Component
public class CronTaskRegistrar implements DisposableBean {

    private final Map<Runnable, ScheduledTask> scheduledTasksMap = new ConcurrentHashMap<>();

    @Resource
    private TaskScheduler taskScheduler;

    public void addCronTask(Runnable task, String cronExpression) {
        CronTask cronTask = new CronTask(task, cronExpression);
        if (this.scheduledTasksMap.containsKey(task)) {
            removeCronTask(task);
        }
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.future = this.taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
        this.scheduledTasksMap.put(task, scheduledTask);
    }

    public void removeCronTask(Runnable task) {
        ScheduledTask scheduledTask = this.scheduledTasksMap.remove(task);
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
    }

    @Override
    public void destroy() {
        for (ScheduledTask task : this.scheduledTasksMap.values()) {
            task.cancel();
        }
        this.scheduledTasksMap.clear();
    }
}