/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:35:20
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_offline_shop
-- ----------------------------
DROP TABLE IF EXISTS `character_offline_shop`;
CREATE TABLE `character_offline_shop` (
  `shopid` int(11) NOT NULL,
  `itemid` int(11) NOT NULL,
  `count` int(11) DEFAULT NULL,
  `price` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
