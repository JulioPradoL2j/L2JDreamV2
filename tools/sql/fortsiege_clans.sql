/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:39:04
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for fortsiege_clans
-- ----------------------------
DROP TABLE IF EXISTS `fortsiege_clans`;
CREATE TABLE `fortsiege_clans` (
  `fort_id` int(1) NOT NULL DEFAULT 0,
  `clan_id` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`clan_id`,`fort_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
