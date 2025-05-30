/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:55
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for quest_global_data
-- ----------------------------
DROP TABLE IF EXISTS `quest_global_data`;
CREATE TABLE `quest_global_data` (
  `quest_name` varchar(40) NOT NULL DEFAULT '',
  `var` varchar(20) NOT NULL DEFAULT '',
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`quest_name`,`var`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
