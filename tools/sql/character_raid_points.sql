/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:30
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_raid_points
-- ----------------------------
DROP TABLE IF EXISTS `character_raid_points`;
CREATE TABLE `character_raid_points` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `boss_id` int(10) unsigned NOT NULL DEFAULT 0,
  `points` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`boss_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
