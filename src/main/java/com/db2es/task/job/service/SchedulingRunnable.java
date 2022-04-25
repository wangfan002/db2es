package com.db2es.task.job.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Fan.Wang
 * @date 2021/12/7 16:17
 * @des Runnable接口实现类，被定时任务线程池调用，用来执行指定bean里面的方法。
 */
@Slf4j
@AllArgsConstructor
public class SchedulingRunnable implements Runnable {

    private String beanName;
    private String methodName;
    private String params;

    @Override
    public void run() {
        log.info("定时任务开始执行 - bean：{}，方法：{}，参数：{}", beanName, methodName, params);
        try {
            Object target = SpringUtil.getBean(beanName);
            Method method;
            if (StrUtil.isNotEmpty(params)) {
                method = target.getClass().getDeclaredMethod(methodName, String.class);
            } else {
                method = target.getClass().getDeclaredMethod(methodName);
            }
            ReflectionUtils.makeAccessible(method);
            if (StrUtil.isNotEmpty(params)) {
                method.invoke(target, params);
            } else {
                method.invoke(target);

            }
        } catch (Exception e) {
            log.error("定时任务执行异常 - bean：{}，方法：{}，参数：{}，error： {} ", beanName, methodName, params,
                    e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SchedulingRunnable that = (SchedulingRunnable) o;
        if (StrUtil.isBlank(params)) {
            return beanName.equals(that.beanName) && methodName.equals(that.methodName);
        }
        return beanName.equals(that.beanName) &&
                methodName.equals(that.methodName) && params.equals(that.params);
    }

    @Override
    public int hashCode() {
        if (StrUtil.isBlank(params)) {
            return Objects.hash(beanName, methodName);
        }
        return Objects.hash(beanName, methodName, params);
    }
}