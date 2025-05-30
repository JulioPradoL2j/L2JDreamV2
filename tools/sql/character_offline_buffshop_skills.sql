/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:17
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_offline_buffshop_skills
-- ----------------------------
DROP TABLE IF EXISTS `character_offline_buffshop_skills`;
CREATE TABLE `character_offline_buffshop_skills` (
  `charId` int(10) unsigned NOT NULL,
  `item` int(10) unsigned NOT NULL DEFAULT 0,
  `price` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`item`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
