/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:37:12
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for custom_droplist
-- ----------------------------
DROP TABLE IF EXISTS `custom_droplist`;
CREATE TABLE `custom_droplist` (
  `mobId` int(11) NOT NULL DEFAULT 0,
  `itemId` int(11) NOT NULL DEFAULT 0,
  `min` int(11) NOT NULL DEFAULT 0,
  `max` int(11) NOT NULL DEFAULT 0,
  `category` int(11) NOT NULL DEFAULT 0,
  `chance` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`mobId`,`itemId`,`category`),
  KEY `key_mobId` (`mobId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
