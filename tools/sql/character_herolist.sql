/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:34:27
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_herolist
-- ----------------------------
DROP TABLE IF EXISTS `character_herolist`;
CREATE TABLE `character_herolist` (
  `charId` int(11) NOT NULL DEFAULT 0,
  `enddate` decimal(20,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
