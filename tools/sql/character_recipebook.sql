/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:36
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_recipebook
-- ----------------------------
DROP TABLE IF EXISTS `character_recipebook`;
CREATE TABLE `character_recipebook` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `id` decimal(11,0) NOT NULL DEFAULT 0,
  `type` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`,`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
