/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:37:01
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for cursed_weapons
-- ----------------------------
DROP TABLE IF EXISTS `cursed_weapons`;
CREATE TABLE `cursed_weapons` (
  `itemId` int(11) NOT NULL DEFAULT 0,
  `charId` int(11) DEFAULT 0,
  `playerKarma` int(11) DEFAULT 0,
  `playerPkKills` int(11) DEFAULT 0,
  `nbKills` int(11) DEFAULT 0,
  `endTime` decimal(20,0) DEFAULT 0,
  PRIMARY KEY (`itemId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
