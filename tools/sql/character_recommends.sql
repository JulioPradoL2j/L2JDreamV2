/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:41
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_recommends
-- ----------------------------
DROP TABLE IF EXISTS `character_recommends`;
CREATE TABLE `character_recommends` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `target_id` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`target_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
