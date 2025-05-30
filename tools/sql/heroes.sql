/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:39:44
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for heroes
-- ----------------------------
DROP TABLE IF EXISTS `heroes`;
CREATE TABLE `heroes` (
  `charId` decimal(11,0) NOT NULL DEFAULT 0,
  `char_name` varchar(45) NOT NULL DEFAULT '',
  `class_id` decimal(3,0) NOT NULL DEFAULT 0,
  `count` decimal(3,0) NOT NULL DEFAULT 0,
  `played` decimal(1,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
