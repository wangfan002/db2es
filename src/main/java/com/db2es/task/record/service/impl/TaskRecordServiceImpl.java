package com.db2es.task.record.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.db2es.task.record.service.TaskRecordService;
import com.db2es.task.record.entity.TaskRecord;
import com.db2es.task.record.mapper.TaskRecordMapper;
import org.springframework.stereotype.Service;

/**
 * 索引同步任务执行记录(TaskRecord)表服务实现类
 *
 * @author Fan.Wang
 * @since 2021-12-09 14:03:21
 */
@Service("taskRecordService")
@DS("mysql")
public class TaskRecordServiceImpl extends ServiceImpl<TaskRecordMapper, TaskRecord> implements TaskRecordService {

}

