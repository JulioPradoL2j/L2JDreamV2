/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:39:58
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for items_on_ground
-- ----------------------------
DROP TABLE IF EXISTS `items_on_ground`;
CREATE TABLE `items_on_ground` (
  `object_id` int(11) NOT NULL DEFAULT 0,
  `item_id` int(11) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `enchant_level` int(11) DEFAULT NULL,
  `x` int(11) NOT NULL DEFAULT 0,
  `y` int(11) NOT NULL DEFAULT 0,
  `z` int(11) NOT NULL DEFAULT 0,
  `drop_time` decimal(20,0) NOT NULL DEFAULT 0,
  `equipable` int(1) DEFAULT 0,
  PRIMARY KEY (`object_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
