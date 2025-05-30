/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:29
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for olympiad_nobles
-- ----------------------------
DROP TABLE IF EXISTS `olympiad_nobles`;
CREATE TABLE `olympiad_nobles` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `class_id` decimal(3,0) NOT NULL DEFAULT 0,
  `olympiad_points` decimal(10,0) NOT NULL DEFAULT 0,
  `competitions_done` decimal(3,0) NOT NULL DEFAULT 0,
  `competitions_won` decimal(3,0) NOT NULL DEFAULT 0,
  `competitions_lost` decimal(3,0) NOT NULL DEFAULT 0,
  `competitions_drawn` decimal(3,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
