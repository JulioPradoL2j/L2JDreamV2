/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:13
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_offline_buffshop
-- ----------------------------
DROP TABLE IF EXISTS `character_offline_buffshop`;
CREATE TABLE `character_offline_buffshop` (
  `charId` int(10) unsigned NOT NULL,
  `time` bigint(13) unsigned NOT NULL DEFAULT 0,
  `title` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
