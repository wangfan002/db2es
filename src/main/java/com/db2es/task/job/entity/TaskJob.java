package com.db2es.task.job.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 定时任务表(TaskJob)表实体类
 *
 * @author Fan.Wang
 * @since 2021-12-08 11:51:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("定时任务实体")
public class TaskJob extends Model<TaskJob> {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键id",example = "1")
    private Integer jobId;

    @ApiModelProperty("备注")
    private String remark;

    @NotNull
    @ApiModelProperty(value = "bean名称 首字母小写",example = "syncController",required = true)
    private String beanName;

    @NotNull
    @ApiModelProperty(value = "方法名称",example = "syncIndexByTime",required = true)
    private String methodName;


    @ApiModelProperty(value = "方法参数",example = "big_data")
    private String methodParams;

    @NotNull
    @ApiModelProperty(value = "cron表达式",example = "0 0 */1 * * ?",required = true)
    private String cronExpression;

    @NotNull
    @ApiModelProperty(value = "状态(1正常 0暂停)",example = "1",required = true)
    private Integer jobStatus;


    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;


    @ApiModelProperty("更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

}

