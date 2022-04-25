SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for index_cycle_info
-- ----------------------------
DROP TABLE IF EXISTS `index_cycle_info`;
CREATE TABLE `index_cycle_info`  (
                                     `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                     `index_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '索引名字',
                                     `index_true_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '索引别名',
                                     `cycle_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '索引上次更新的时间点',
                                     `del_cycle_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '删除索引上次的时间点',
                                     `update_time` datetime(0) NULL DEFAULT NULL COMMENT '该条数据的更新时间',
                                     `create_time` datetime(0) NULL DEFAULT NULL COMMENT '该条数据的创建时间',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '记录索引上次更新时间' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for task_job
-- ----------------------------
DROP TABLE IF EXISTS `task_job`;
CREATE TABLE `task_job`  (
                             `job_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
                             `bean_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'bean名称',
                             `method_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '方法名称',
                             `method_params` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '方法参数',
                             `cron_expression` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'cron表达式',
                             `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
                             `job_status` tinyint(0) NOT NULL COMMENT '状态(1正常 0暂停)',
                             `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                             `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
                             PRIMARY KEY (`job_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '定时任务表' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for task_record
-- ----------------------------
DROP TABLE IF EXISTS `task_record`;
CREATE TABLE `task_record`  (
                                `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                `index_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '索引名',
                                `event_type` tinyint(0) NOT NULL COMMENT '事件类型 1:重建索引 2:增量索引 3:根据id增量 4:根据id删除',
                                `status` tinyint(0) NOT NULL COMMENT '同步状态 0:同步中 1:同步成功 2:同步失败',
                                `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '记录失败原因',
                                `start_time` datetime(0) NULL DEFAULT NULL COMMENT '同步开始时间',
                                `end_time` datetime(0) NULL DEFAULT NULL COMMENT '同步结束时间',
                                PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '索引同步任务执行记录' ROW_FORMAT = Dynamic;



