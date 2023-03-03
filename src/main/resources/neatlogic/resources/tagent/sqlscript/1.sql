/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
 SET FOREIGN_KEY_CHECKS=0;
 
 CREATE TABLE `tagent`  (
   `id` bigint UNSIGNED NOT NULL COMMENT 'tagent Id',
   `ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'tagentIP',
   `port` int NOT NULL COMMENT 'tagent注册端口',
   `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'tagent名称',
   `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'tagent版本',
   `os_id` bigint NULL DEFAULT NULL COMMENT 'osId',
   `os_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '系统类型',
   `os_version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '系统版本',
   `osbit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作系统位数',
   `account_id` bigint NULL DEFAULT NULL COMMENT '账号id',
   `runner_id` bigint NULL DEFAULT NULL COMMENT 'proxy_id',
   `runner_group_id` bigint NULL DEFAULT NULL COMMENT 'proxy组id',
   `runner_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'proxy ip',
   `runner_port` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'proxy netty端口',
   `user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'tagent用户',
   `pcpu` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'cpu占用',
   `mem` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内存占用',
   `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'tagent状态',
   `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
   `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
   `disconnect_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '连接失败原因',
   PRIMARY KEY (`id`) USING BTREE,
   UNIQUE INDEX `idx_ip_port`(`ip`, `port`) USING BTREE
 ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'tagent信息表' ROW_FORMAT = Dynamic;
 
 CREATE TABLE `tagent_ip`  (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
   `tagent_id` bigint NOT NULL COMMENT 'tagentId',
   `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标主机的网卡IP',
   PRIMARY KEY (`id`) USING BTREE,
   UNIQUE INDEX `idx_id_ip`(`tagent_id`, `ip`) USING BTREE
 ) ENGINE = InnoDB AUTO_INCREMENT = 2222478 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'tagent ip表（多网卡）' ROW_FORMAT = Dynamic;
 
 CREATE TABLE `tagent_os`  (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
   `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作系统名称',
   `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
   `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
   `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
   `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
   `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '修改人',
   PRIMARY KEY (`id`) USING BTREE,
   UNIQUE INDEX `uk_name`(`name`) USING BTREE
 ) ENGINE = InnoDB AUTO_INCREMENT = 787 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'tagent os' ROW_FORMAT = Dynamic;
 
 CREATE TABLE `tagent_upgrade_audit`  (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
   `count` bigint NULL DEFAULT NULL COMMENT '升级个数',
   `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '时间',
   `network` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '网段信息',
   `fcu` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '升级操作用户',
   PRIMARY KEY (`id`) USING BTREE
 ) ENGINE = InnoDB AUTO_INCREMENT = 735473471119360 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'tagent升级' ROW_FORMAT = Dynamic;
 
 CREATE TABLE `tagent_upgrade_detail`  (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
   `audit_id` bigint NULL DEFAULT NULL COMMENT '记录id,关联 flow_tagent_upgrade_audit表',
   `ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'tagent ip',
   `port` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'tagent port',
   `source_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '原版本',
   `target_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标版本',
   `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '结果',
   `error` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '异常',
   `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
   PRIMARY KEY (`id`) USING BTREE
 ) ENGINE = InnoDB AUTO_INCREMENT = 735473471119364 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'tagent升级' ROW_FORMAT = Dynamic;
 
 CREATE TABLE `tagent_version`  (
   `id` bigint NOT NULL COMMENT '主键',
   `os_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'os类型',
   `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '版本',
   `osbit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'CPU架构\r\n\r\nCPU框架',
   `ignore_file` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '忽略目录或文件',
   `file_id` bigint NOT NULL COMMENT '关联file表，用于下载安装包',
   PRIMARY KEY (`id`) USING BTREE
 ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'tagent版本' ROW_FORMAT = Dynamic;
 
 SET FOREIGN_KEY_CHECKS=1;