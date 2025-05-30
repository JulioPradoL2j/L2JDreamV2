/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:38:38
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for fishing_championship_date
-- ----------------------------
DROP TABLE IF EXISTS `fishing_championship_date`;
CREATE TABLE `fishing_championship_date` (
  `finish_date` decimal(20,0) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `fishing_championship_date` VALUES ('1748988000322');
