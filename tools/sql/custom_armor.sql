/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:37:05
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for custom_armor
-- ----------------------------
DROP TABLE IF EXISTS `custom_armor`;
CREATE TABLE `custom_armor` (
  `item_id` int(11) NOT NULL DEFAULT 0,
  `item_display_id` int(11) NOT NULL DEFAULT 0,
  `name` varchar(70) DEFAULT NULL,
  `bodypart` varchar(15) NOT NULL DEFAULT '',
  `crystallizable` varchar(5) NOT NULL DEFAULT '',
  `armor_type` varchar(5) NOT NULL DEFAULT '',
  `weight` int(5) NOT NULL DEFAULT 0,
  `material` varchar(15) NOT NULL DEFAULT '',
  `crystal_type` varchar(4) NOT NULL DEFAULT '',
  `avoid_modify` int(1) NOT NULL DEFAULT 0,
  `duration` int(3) NOT NULL DEFAULT 0,
  `lifetime` int(11) DEFAULT -1,
  `p_def` int(3) NOT NULL DEFAULT 0,
  `m_def` int(2) NOT NULL DEFAULT 0,
  `mp_bonus` int(3) NOT NULL DEFAULT 0,
  `price` int(11) NOT NULL DEFAULT 0,
  `crystal_count` int(4) DEFAULT NULL,
  `sellable` varchar(5) DEFAULT NULL,
  `dropable` varchar(5) NOT NULL DEFAULT 'true',
  `destroyable` varchar(5) NOT NULL DEFAULT 'true',
  `tradeable` varchar(5) NOT NULL DEFAULT 'true',
  `skills_item` varchar(70) NOT NULL DEFAULT '',
  PRIMARY KEY (`item_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
