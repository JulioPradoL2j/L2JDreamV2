/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:45
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_shortcuts
-- ----------------------------
DROP TABLE IF EXISTS `character_shortcuts`;
CREATE TABLE `character_shortcuts` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `slot` decimal(3,0) NOT NULL DEFAULT 0,
  `page` decimal(3,0) NOT NULL DEFAULT 0,
  `type` decimal(3,0) DEFAULT NULL,
  `shortcut_id` decimal(16,0) DEFAULT NULL,
  `level` varchar(4) DEFAULT NULL,
  `class_index` int(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`slot`,`page`,`class_index`),
  KEY `shortcut_id` (`shortcut_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
