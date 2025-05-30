/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:59
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_subclasses
-- ----------------------------
DROP TABLE IF EXISTS `character_subclasses`;
CREATE TABLE `character_subclasses` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `class_id` int(2) NOT NULL DEFAULT 0,
  `exp` decimal(20,0) NOT NULL DEFAULT 0,
  `sp` decimal(11,0) NOT NULL DEFAULT 0,
  `level` int(2) NOT NULL DEFAULT 40,
  `class_index` int(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`class_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
