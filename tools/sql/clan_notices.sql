/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:36:23
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for clan_notices
-- ----------------------------
DROP TABLE IF EXISTS `clan_notices`;
CREATE TABLE `clan_notices` (
  `clanID` int(32) NOT NULL DEFAULT 0,
  `notice` text NOT NULL,
  `enabled` varchar(5) NOT NULL DEFAULT '',
  PRIMARY KEY (`clanID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
