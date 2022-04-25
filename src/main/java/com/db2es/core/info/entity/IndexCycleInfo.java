package com.db2es.core.info.entity;


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
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 记录索引上次更新时间(IndexCycleInfo)表实体类
 *
 * @author Fan.Wang
 * @since 2021-12-09 09:58:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("记录索引上次更新时间")
public class IndexCycleInfo extends Model<IndexCycleInfo> {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键id",hidden = true)
    private Integer id;

	@ApiModelProperty(value = "索引名字",example = "big_data")
    private String indexName;

	@ApiModelProperty("索引上次更新的时间点")
    private String cycleTime;

    @ApiModelProperty("索引上次更新的时间点")
    private String delCycleTime;

	@ApiModelProperty("该条数据的更新时间")
    private Date updateTime;

	@ApiModelProperty("该条数据的创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty("索引别名")
    private String indexTrueName;



}

