package com.db2es.task.record.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 索引同步任务执行记录(TaskRecord)表实体类
 *
 * @author Fan.Wang
 * @since 2021-12-09 14:49:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("索引同步记录")
public class TaskRecord extends Model<TaskRecord> {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键id",hidden = true)
    private Integer id;

	@ApiModelProperty(value = "索引名",example = "big_data")
    private String indexName;

	@ApiModelProperty(value = "同步状态 0:同步中 1:同步成功 2:同步失败",example = "0")
    private Integer status;

	@ApiModelProperty("记录失败原因")
    private String remark;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(value = "同步开始时间",hidden = true)
    private Date startTime;


    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "同步结束时间",hidden = true)
    private Date endTime;

	@ApiModelProperty(value = "事件类型 1:重建索引 2:增量索引 3:根据id增量 4:根据id删除",example = "1")
    private Integer eventType;

    @TableField(exist = false)
    @JsonIgnore
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "同步开始日期查询起点",example = "2021-12-09 00:00:00")
    private Date startTimeBegin;

    @TableField(exist = false)
    @JsonIgnore
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "同步开始日期查询终点",example = "2021-12-16 00:00:00")
    private Date startTimeEnd;


}

