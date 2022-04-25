package com.db2es.task.job.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.db2es.task.job.entity.TaskJob;
import com.db2es.task.job.mapper.TaskJobMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Fan.Wang
 * @date 2021/12/8 14:56
 * @des 实现了CommandLineRunner接口的SysJobRunner类，当spring boot项目启动完成后， 加载数据库里状态为正常的定时任务
 */
@Slf4j
@Component
@AllArgsConstructor
@DS("mysql")
public class SysJobRunner implements CommandLineRunner {

    private CronTaskRegistrar cronTaskRegistrar;
    private TaskJobMapper taskJobMapper;

    @Override
    public void run(String... args) {
        // 初始加载数据库里状态为正常的定时任务
        List<TaskJob> jobList = taskJobMapper.selectList(Wrappers.<TaskJob>query().eq("job_status", 1));
        if (CollectionUtils.isNotEmpty(jobList)) {
            for (TaskJob job : jobList) {
                log.info("方法名:[{}],参数:[{}],状态:[{}],cron表达式:[{}]", job.getMethodName(),
                        job.getMethodParams(), job.getJobStatus(), job.getCronExpression());
                SchedulingRunnable task = new SchedulingRunnable(job.getBeanName(), job.getMethodName(), job.getMethodParams());
                cronTaskRegistrar.addCronTask(task, job.getCronExpression());
            }
            log.info("定时任务已加载完毕...");
        }
    }
}