/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:38:35
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for fishing_championship
-- ----------------------------
DROP TABLE IF EXISTS `fishing_championship`;
CREATE TABLE `fishing_championship` (
  `PlayerName` varchar(35) NOT NULL,
  `fishLength` float(10,2) NOT NULL,
  `rewarded` int(1) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
