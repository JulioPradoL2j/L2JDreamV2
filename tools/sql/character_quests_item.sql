/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:27
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_quests_item
-- ----------------------------
DROP TABLE IF EXISTS `character_quests_item`;
CREATE TABLE `character_quests_item` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `questId` int(10) NOT NULL,
  `itemId` decimal(11,0) NOT NULL,
  PRIMARY KEY (`charId`,`questId`,`itemId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
