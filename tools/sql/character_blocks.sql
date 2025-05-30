/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:34:03
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_blocks
-- ----------------------------
DROP TABLE IF EXISTS `character_blocks`;
CREATE TABLE `character_blocks` (
  `charId` int(10) unsigned NOT NULL,
  `name` varchar(35) NOT NULL,
  PRIMARY KEY (`charId`,`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
