/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:36:12
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_variables
-- ----------------------------
DROP TABLE IF EXISTS `character_variables`;
CREATE TABLE `character_variables` (
  `obj_id` int(11) NOT NULL DEFAULT 0,
  `type` varchar(86) NOT NULL DEFAULT '0',
  `name` varchar(100) CHARACTER SET utf8 NOT NULL DEFAULT '0',
  `value` varchar(333) CHARACTER SET utf8 NOT NULL DEFAULT '0',
  `expire_time` bigint(20) NOT NULL DEFAULT 0,
  UNIQUE KEY `prim` (`obj_id`,`type`,`name`) USING BTREE,
  KEY `obj_id` (`obj_id`) USING BTREE,
  KEY `type` (`type`) USING BTREE,
  KEY `name` (`name`) USING BTREE,
  KEY `value` (`value`) USING BTREE,
  KEY `expire_time` (`expire_time`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records 
-- ----------------------------
