/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:34:35
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_hennas
-- ----------------------------
DROP TABLE IF EXISTS `character_hennas`;
CREATE TABLE `character_hennas` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `symbol_id` int(11) DEFAULT NULL,
  `slot` int(11) NOT NULL DEFAULT 0,
  `class_index` int(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`slot`,`class_index`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
