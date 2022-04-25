package com.db2es.task.job.service;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Fan.Wang
 * @date 2021/12/7 16:15
 * @des ScheduledFuture的包装类。ScheduledFuture是ScheduledExecutorService定时任务线程池的执行结果。
 */
public class ScheduledTask {
    ScheduledFuture<?> future;

    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(true);
        }
    }

}  