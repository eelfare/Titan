
DROP TABLE IF EXISTS `batch`;
CREATE TABLE `batch` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `target` int(11) DEFAULT NULL COMMENT '批次执行目标的任务ID',
  `dataTotal` int(11) NOT NULL DEFAULT '0' COMMENT '目标数据总数',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '数据构造状态（0：未开始,1：构造中，2：构造完成，3：构造失败，4：已导出）',
  `tasks` text NOT NULL COMMENT '任务清单',
  `doTime` datetime DEFAULT NULL COMMENT '批次构造时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='任务批次信息表';


DROP TABLE IF EXISTS `dubbo_report`;
CREATE TABLE `dubbo_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sceneId` int(11) NOT NULL COMMENT '场景id',
  `snap` text NOT NULL COMMENT '快照内容',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='dubbo压测报告快照信息表';


DROP TABLE IF EXISTS `dubbo_scene`;
CREATE TABLE `dubbo_scene` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '链路名',
  `concurrent` int(11) NOT NULL COMMENT '并发',
  `total` bigint(20) DEFAULT NULL,
  `timeout` int(11) NOT NULL COMMENT '停止时间',
  `throughPut` int(11) NOT NULL COMMENT '期待吞吐量(每秒)',
  `strategy` varchar(100) NOT NULL COMMENT '策略',
  `flow` varchar(100) NOT NULL COMMENT '流量方式',
  `idsWeight` text NOT NULL COMMENT '服务id权重',
  `sequence` varchar(100) NOT NULL COMMENT '服务顺序',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '场景状态 （0：未开始,1：压测进行中，2：停止压测中）',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='dubbo压测场景信息表';


DROP TABLE IF EXISTS `dubbo_service`;
CREATE TABLE `dubbo_service` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) NOT NULL COMMENT '服务别名',
  `applicationName` varchar(200) NOT NULL COMMENT '应用名称',
  `service_Name` varchar(500) NOT NULL COMMENT '服务名称',
  `methodName` varchar(200) NOT NULL COMMENT '方法名称',
  `paramsType` varchar(500) DEFAULT NULL COMMENT '参数类型',
  `rpcContent` varchar(500) DEFAULT NULL COMMENT 'rpc上下文信息',
  `address` text COMMENT '服务地址',
  `clusterAddress` mediumtext,
  `paramMode` varchar(30) NOT NULL DEFAULT 'RANDOM',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `paramStatus` tinyint(4) DEFAULT '0' COMMENT '链路参数状态,0乱序，1顺序',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='dubbo服务信息表';

DROP TABLE IF EXISTS `filter`;
CREATE TABLE `filter` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) NOT NULL DEFAULT '1',
  `domain` varchar(100) NOT NULL DEFAULT '*',
  `path` text NOT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `group_filter` (`group_id`),
  CONSTRAINT `group_filter` FOREIGN KEY (`group_id`) REFERENCES `group_blacklist` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=274 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `group_blacklist`;
CREATE TABLE `group_blacklist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL DEFAULT '-',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `sl_name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `http_base_line`;
CREATE TABLE `http_base_line` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sceneId` int(11) NOT NULL COMMENT '场景id',
  `linkId` int(11) NOT NULL COMMENT '链路Id',
  `baseLine` text NOT NULL COMMENT '动态的基线指标数据（json）',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='http压测报告中的个链路性能基线表';


DROP TABLE IF EXISTS `http_link`;
CREATE TABLE `http_link` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(500) NOT NULL COMMENT '链路名',
  `protocol` varchar(30) NOT NULL COMMENT '协议',
  `url` varchar(500) NOT NULL COMMENT '路径',
  `method` varchar(30) NOT NULL COMMENT '请求方法',
  `contentType` varchar(100) NOT NULL COMMENT '文本类型',
  `charset` varchar(30) NOT NULL COMMENT '字符类型',
  `paramMode` varchar(30) NOT NULL DEFAULT 'RANDOM',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `paramStatus` tinyint(4) DEFAULT '0' COMMENT '链路参数状态,0乱序，1顺序',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `url` (`url`),
  KEY `index_updateTime_deleted` (`updateTime`,`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=12531 DEFAULT CHARSET=utf8 COMMENT='http链路信息表';


DROP TABLE IF EXISTS `http_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `http_report` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sceneId` int(11) NOT NULL COMMENT '场景id',
  `snap` longtext,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2693 DEFAULT CHARSET=utf8 COMMENT='http压测报告快照信息表';


DROP TABLE IF EXISTS `http_scene`;
CREATE TABLE `http_scene` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '链路名',
  `strategy` varchar(100) NOT NULL COMMENT '策略',
  `concurrent` int(11) NOT NULL COMMENT '并发',
  `total` bigint(20) DEFAULT NULL,
  `timeout` int(11) NOT NULL COMMENT '停止时间',
  `throughPut` int(11) NOT NULL COMMENT '期待吞吐量(每秒)',
  `flow` varchar(100) NOT NULL COMMENT '流量方式',
  `sequence` varchar(100) NOT NULL COMMENT '链路顺序',
  `allot` varchar(100) NOT NULL DEFAULT 'WEIGHT',
  `idsScale` text NOT NULL,
  `idsWeight` text NOT NULL COMMENT '链路id和权重，以逗号分隔',
  `idsQps` text NOT NULL,
  `status` int(11) NOT NULL COMMENT '场景状态 （0：未开始 ，1：压测进行中，2：停止压测中）',
  `webhook` varchar(200) DEFAULT '' COMMENT '压测报告通知连接地址',
  `alertThreshold` varchar(20) DEFAULT NULL COMMENT '链路告警阈值(失败比率)',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=588 DEFAULT CHARSET=utf8 COMMENT='http压测场景信息表';


DROP TABLE IF EXISTS `incrementID`;
CREATE TABLE `incrementID` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8 COMMENT='titanrtx自增id';

DROP TABLE IF EXISTS `link_params`;
CREATE TABLE `link_params` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `linkId` int(11) NOT NULL COMMENT '链路id',
  `param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `orders` int(4) DEFAULT '0' COMMENT '相同链路的参数参数顺序',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `linkid_updatetime` (`linkId`,`updateTime`),
  KEY `idx_linkId` (`linkId`),
  KEY `idx_orders` (`orders`)
) ENGINE=InnoDB AUTO_INCREMENT=277160323 DEFAULT CHARSET=utf8 COMMENT='http链路参数信息信息表';


DROP TABLE IF EXISTS `my_task`;
CREATE TABLE `my_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '任务名称',
  `tableName` varchar(50) NOT NULL COMMENT '任务执行结果保存的表名',
  `doMode` varchar(20) DEFAULT 'REST' COMMENT '执行方式（REST、RPC）',
  `url` text COMMENT '路径',
  `method` varchar(30) DEFAULT NULL COMMENT '请求方法',
  `contentType` varchar(100) DEFAULT NULL COMMENT '文本类型',
  `isTarget` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否是目标任务(0:普通任务；1：目标任务)',
  `paramMode` varchar(30) DEFAULT 'RANDOM' COMMENT '只有目标任务时才会有参数获取方式(RANDOM,ORDER)',
  `template` text COMMENT '数据模板，只有是目标任务的时候，才有内容',
  `status` int(11) DEFAULT '0' COMMENT '是否已执行，0未执行，1，已执行',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `url` (`url`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务信息表';


DROP TABLE IF EXISTS `ops_log`;
CREATE TABLE `ops_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` int(11) NOT NULL COMMENT '用户id',
  `userName` varchar(30) NOT NULL COMMENT '用户名',
  `pathName` varchar(30) NOT NULL COMMENT '路径名',
  `params` text NOT NULL COMMENT '参数内容',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=93166 DEFAULT CHARSET=utf8 COMMENT='操作信息记录表';



DROP TABLE IF EXISTS `path`;
CREATE TABLE `path` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '路径名称',
  `uri` varchar(150) NOT NULL COMMENT '路径地址',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `uri` (`uri`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='路径信息记录表';


DROP TABLE IF EXISTS `service_params`;
CREATE TABLE `service_params` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `serviceId` int(11) NOT NULL COMMENT '服务id',
  `param` text NOT NULL COMMENT '参数内容',
  `orders` int(4) DEFAULT '0' COMMENT '相同链路的参数参数顺序',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `serviceId` (`serviceId`),
  KEY `idx_orders` (`orders`)
) ENGINE=InnoDB AUTO_INCREMENT=3992703 DEFAULT CHARSET=utf8 COMMENT='dubbo服务参数信息信息表';

DROP TABLE IF EXISTS `auto_deploy_his`;
CREATE TABLE `auto_deploy_his` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `content` varchar(200) NOT NULL COMMENT '自动话配置信息',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8 COMMENT='自动化调度任务历史表';


DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '任务名称',
  `tableName` varchar(50) NOT NULL COMMENT '任务执行结果保存的表名',
  `doMode` varchar(20) DEFAULT 'REST' COMMENT '执行方式（REST、RPC）',
  `url` text COMMENT '路径',
  `method` varchar(30) DEFAULT NULL COMMENT '请求方法',
  `contentType` varchar(100) DEFAULT NULL COMMENT '文本类型',
  `isTarget` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否是目标任务(0:普通任务；1：目标任务)',
  `paramMode` varchar(30) DEFAULT 'RANDOM' COMMENT '只有目标任务时才会有参数获取方式(RANDOM,ORDER)',
  `template` text COMMENT '数据模板，只有是目标任务的时候，才有内容',
  `status` int(11) DEFAULT '0' COMMENT '是否已执行，0未执行，1，已执行',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `url` (`url`(255))
) ENGINE=InnoDB AUTO_INCREMENT=397 DEFAULT CHARSET=utf8 COMMENT='任务信息表';


DROP TABLE IF EXISTS `task_output_deploy`;
CREATE TABLE `task_output_deploy` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `taskId` int(11) NOT NULL COMMENT '任务ID',
  `name` varchar(100) NOT NULL COMMENT '列名称',
  `expr` varchar(255) NOT NULL COMMENT '结果表达式 如果source为PARAM，则表示为param配置对应的ID，否则就是JSONPath表达式',
  `source` varchar(20) NOT NULL DEFAULT 'PARAM' COMMENT '数据来源（PARAM、RESPONSE）',
  `type` varchar(20) DEFAULT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `taskId` (`taskId`)
) ENGINE=InnoDB AUTO_INCREMENT=254 DEFAULT CHARSET=utf8 COMMENT='任务保存结果配置表';


DROP TABLE IF EXISTS `task_param_deploy`;
CREATE TABLE `task_param_deploy` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `taskId` int(11) NOT NULL COMMENT '任务ID',
  `name` varchar(100) NOT NULL COMMENT '参数名',
  `extra` varchar(500) DEFAULT NULL COMMENT '参数备注',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `taskId` (`taskId`),
  KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=211 DEFAULT CHARSET=utf8 COMMENT='任务参数配置表';


DROP TABLE IF EXISTS `task_param_source`;
CREATE TABLE `task_param_source` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `batchId` int(11) NOT NULL COMMENT '批次ID',
  `taskId` int(11) NOT NULL COMMENT '任务ID',
  `paramsSource` text COMMENT '参数名_使用方式_数据来源类型_source',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `name` (`batchId`)
) ENGINE=InnoDB AUTO_INCREMENT=873 DEFAULT CHARSET=utf8 COMMENT='任务参数数据来源配置表';

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userName` varchar(30) NOT NULL COMMENT '用户工号',
  `nickName` varchar(30) NOT NULL COMMENT '用户姓名',
  `phone` varchar(30) NOT NULL COMMENT '用户手机号 登录名',
  `passWord` varchar(30) NOT NULL COMMENT '用户密码',
  `pathIds` text COMMENT '用户权限id',
  `loginTimes` bigint(20) NOT NULL DEFAULT '0' COMMENT '登陆次数',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(4) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='用户表信息表';

DROP TABLE IF EXISTS `white_list`;
CREATE TABLE `white_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(100) NOT NULL DEFAULT '*',
  `path` text NOT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=385 DEFAULT CHARSET=utf8 COMMENT='链路白名单信息表';


/*init data-------------------------------------------------*/
INSERT INTO `path` VALUES (1,'首页','overview','2019-04-10 10:01:00','2019-04-10 10:01:00',0),
(2,'链路管理','link','2019-04-10 10:01:00','2019-04-10 10:01:00',0),
(3,'HTTP场景','httpScene','2019-04-10 10:01:00','2019-04-10 10:01:00',0),
(4,'HTTP报告','httpReport','2019-04-10 10:01:00','2019-04-10 10:01:00',0),
(5,'服务管理','service','2019-04-10 10:01:00','2019-04-10 10:01:00',0),
(6,'DUBBO场景','dubboScene','2019-04-10 10:01:00','2019-04-10 10:01:00',0),
(7,'DUBBO报告','dubboReport','2019-04-10 10:01:00','2019-04-10 10:01:00',0),
(8,'TOP链路','topLink','2019-04-10 10:01:00','2019-04-10 10:01:00',0),
(9,'规则管理','rulesLink','2019-04-10 10:01:01','2019-04-10 10:01:01',0),
(10,'机器管理','machine','2019-04-10 10:01:01','2019-04-10 10:01:01',0),
(11,'用户管理','user','2019-04-10 10:01:01','2019-04-10 10:01:01',0),
(12,'通用测试','common','2019-04-10 10:01:01','2019-04-10 10:01:01',0),
(13,'压测链路黑名单','blackList','2019-12-06 23:08:46','2019-12-06 23:08:46',0),
(14,'压测链路白名单','whiteList','2019-12-06 11:33:43','2019-12-06 11:33:43',0),
(15,'压测链路黑名单分组','blackGroup','2019-12-06 23:07:24','2019-12-06 23:07:24',0),
(16,'流量构造任务管理','task','2020-03-09 01:35:09','2020-03-09 01:35:09',0),
(17,'流量构造批次管理','batch','2020-03-09 01:35:09','2020-03-09 01:35:09',0),
(18,'自动压测','autoTestDeploy','2020-05-11 22:24:02','2020-05-11 22:24:02',0),
(19,'自动部署','autoDeploy','2020-07-24 17:24:02','2020-05-11 22:24:02',0),
(20,'导出','export','2020-05-12 22:17:35','2020-05-12 22:17:35',0);

insert into user(userName,phone,passWord) values("root","root","yunji2019");
-- edit

-- ALTER TABLE dubbo_service ADD COLUMN clusterAddress MEDIUMTEXT AFTER address;


