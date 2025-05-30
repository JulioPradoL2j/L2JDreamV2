/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:33:27
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for castle_functions
-- ----------------------------
DROP TABLE IF EXISTS `castle_functions`;
CREATE TABLE `castle_functions` (
  `castle_id` int(2) NOT NULL DEFAULT 0,
  `type` int(1) NOT NULL DEFAULT 0,
  `lvl` int(3) NOT NULL DEFAULT 0,
  `lease` int(10) NOT NULL DEFAULT 0,
  `rate` decimal(20,0) NOT NULL DEFAULT 0,
  `endTime` decimal(20,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`castle_id`,`type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
