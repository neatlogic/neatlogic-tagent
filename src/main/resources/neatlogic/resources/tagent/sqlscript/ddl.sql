-- ----------------------------
-- Table structure for tagent
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tagent` (
  `id` bigint unsigned NOT NULL COMMENT 'tagent Id',
  `ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'tagentIP',
  `port` int NOT NULL COMMENT 'tagent注册端口',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'tagent名称',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'tagent版本',
  `os_id` bigint DEFAULT NULL COMMENT 'osId',
  `os_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '系统类型',
  `os_version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '系统版本',
  `osbit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作系统位数',
  `account_id` bigint DEFAULT NULL COMMENT '账号id',
  `runner_id` bigint DEFAULT NULL COMMENT 'proxy_id',
  `runner_group_id` bigint DEFAULT NULL COMMENT 'proxy组id',
  `runner_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'proxy ip',
  `runner_port` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'proxy netty端口',
  `user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'tagent用户',
  `pcpu` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'cpu占用',
  `mem` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '内存占用',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'tagent状态',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `disconnect_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '连接失败原因',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_ip_port` (`ip`,`port`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent信息表';

-- ----------------------------
-- Table structure for tagent_ip
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tagent_ip` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `tagent_id` bigint NOT NULL COMMENT 'tagentId',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标主机的网卡IP',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_id_ip` (`tagent_id`,`ip`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2222745 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent ip表（多网卡）';

-- ----------------------------
-- Table structure for tagent_os
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tagent_os` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作系统名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '修改时间',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=788 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent os';

-- ----------------------------
-- Table structure for tagent_osbit
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tagent_osbit` (
  `osbit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'tagent cpu架构',
  PRIMARY KEY (`osbit`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent cpu架构表';

-- ----------------------------
-- Table structure for tagent_upgrade_audit
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tagent_upgrade_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `count` bigint DEFAULT NULL COMMENT '升级个数',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '时间',
  `network` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '网段信息',
  `fcu` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '升级操作用户',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=735473471119361 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent升级';

-- ----------------------------
-- Table structure for tagent_upgrade_detail
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tagent_upgrade_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `audit_id` bigint DEFAULT NULL COMMENT '记录id,关联 flow_tagent_upgrade_audit表',
  `ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'tagent ip',
  `port` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'tagent port',
  `source_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原版本',
  `target_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '目标版本',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '结果',
  `error` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '异常',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=735473471119365 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent升级';

-- ----------------------------
-- Table structure for tagent_version
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tagent_version` (
  `id` bigint NOT NULL COMMENT '主键',
  `os_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'os类型',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '版本',
  `osbit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'CPU架构\r\n\r\nCPU框架',
  `ignore_file` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '忽略目录或文件',
  `file_id` bigint NOT NULL COMMENT '关联file表，用于下载安装包',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent版本';

-- ----------------------------
-- Table structure for tagent_account
-- ----------------------------

CREATE TABLE IF NOT EXISTS `tagent_account` (
    `id` bigint NOT NULL COMMENT '主键id',
    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
    `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '密码',
    `protocol_id` bigint NOT NULL COMMENT 'tgent协议id',
    `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
    `fcd` timestamp NULL DEFAULT NULL COMMENT '创建时间',
    `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改人',
    `lcd` timestamp NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent账号表';

-- ----------------------------
-- Table structure for tagent_account_ip
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tagent_account_ip` (
   `account_id` bigint NOT NULL COMMENT '账号id',
    `ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号对应的ip',
    PRIMARY KEY (`account_id`,`ip`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='tagent账号ip表';
