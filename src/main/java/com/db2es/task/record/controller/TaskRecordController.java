package com.db2es.task.record.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.db2es.apiresult.ApiResult;
import com.db2es.task.record.entity.TaskRecord;
import com.db2es.task.record.service.TaskRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Fan.Wang
 * @date 2021-12-09 14:03:21
 * @des model层
 */
@Api("索引操作记录")
@RestController
@RequestMapping("/record")
@Slf4j
public class TaskRecordController {
    /**
     * 服务对象
     */
    @Resource
    private TaskRecordService taskRecordService;


    /**
     * 分页查询所有数据
     *
     * @param page       分页对象
     * @param taskRecord 任务执行记录
     * @return 所有数据
     */
    @GetMapping("/task-record/list")
    @ApiOperation("分页查询列表")
    public ApiResult selectAll(Page<TaskRecord> page, TaskRecord taskRecord) {
        return ApiResult.success(taskRecordService.page(page, Wrappers.<TaskRecord>query()
                .ge(taskRecord.getStartTimeBegin() != null,"start_time",taskRecord.getStartTimeBegin())
                .le(taskRecord.getStartTimeEnd() != null,"start_time",taskRecord.getStartTimeEnd()).orderByDesc("start_time")));
    }

    /**
     * 测试定时任务
     *
     * @author Fan.Wang
     * @dateTime 2021/12/21 15:32
     * @param param 定时任务参数
     */
    @SuppressWarnings("unused")
    public void test(String param) {
        log.info("测试:"+param);
    }

}

