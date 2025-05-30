/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:41:27
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for siege_clans
-- ----------------------------
DROP TABLE IF EXISTS `siege_clans`;
CREATE TABLE `siege_clans` (
  `castle_id` int(1) NOT NULL DEFAULT 0,
  `clan_id` int(11) NOT NULL DEFAULT 0,
  `type` int(1) DEFAULT NULL,
  `castle_owner` int(1) DEFAULT NULL,
  PRIMARY KEY (`clan_id`,`castle_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
