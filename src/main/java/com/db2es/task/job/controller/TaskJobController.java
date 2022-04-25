package com.db2es.task.job.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.db2es.apiresult.ApiResult;
import com.db2es.task.job.entity.TaskJob;
import com.db2es.task.job.service.TaskJobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Fan.Wang
 * @date 2021-12-08 11:56:56
 * @des model层
 */

@RestController
@Api("定时任务操作")
@RequestMapping("/task")
public class TaskJobController {
    /**
     * 服务对象
     */
    @Resource
    private TaskJobService taskJobService;

    /**
     * 分页查询列表
     *
     * @param page 分页实体
     * @param taskJob 定时任务实体
     * @return PageUtils
     */
    @GetMapping("/task-job/list")
    @ApiOperation("分页查询列表")
    public ApiResult list(Page<TaskJob> page, TaskJob taskJob) {
        return ApiResult.success(taskJobService.page(page, new QueryWrapper<>(taskJob)));
    }

    /**
     * 新增数据
     *
     * @param taskJob 定时任务实体
     * @return ApiResult
     */
    @PostMapping("/task-job/save")
    @ApiOperation("新增数据")
    public ApiResult saveTask(TaskJob taskJob) {
        if (taskJobService.saveTaskJob(taskJob)) {
            return ApiResult.success();
        }
        return ApiResult.failure();
    }

    /**
     * 修改数据
     *
     * @param taskJob 定时任务实体
     * @return ApiResult
     */
    @PostMapping("/task-job/update")
    @ApiOperation("修改数据")
    public ApiResult update(TaskJob taskJob) {
        if (taskJobService.updateTaskJobById(taskJob)) {
            return ApiResult.success();
        }
        return ApiResult.failure();
    }

    /**
     * 删除数据
     *
     * @param idList 主键集合
     * @return ApiResult
     */
    @PostMapping("/task-job/delete")
    @ApiOperation("删除数据")
    @ApiImplicitParam(name = "idList", value = "待删除的id集合", required = true, dataType = "List", dataTypeClass = List.class, paramType = "query")
    public ApiResult deleteById(@RequestParam("idList") List<Long> idList) {
        if (taskJobService.removeTaskJobByIds(idList)) {
            return ApiResult.success();
        }
        return ApiResult.failure();
    }
}

