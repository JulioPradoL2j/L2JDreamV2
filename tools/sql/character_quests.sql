/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:23
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_quests
-- ----------------------------
DROP TABLE IF EXISTS `character_quests`;
CREATE TABLE `character_quests` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `name` varchar(40) NOT NULL DEFAULT '',
  `var` varchar(20) NOT NULL DEFAULT '',
  `value` varchar(255) DEFAULT NULL,
  `class_index` int(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`name`,`var`,`class_index`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
