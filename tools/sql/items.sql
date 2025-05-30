/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:39:54
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for items
-- ----------------------------
DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
  `owner_id` int(11) DEFAULT NULL,
  `object_id` int(11) NOT NULL DEFAULT 0,
  `item_id` int(11) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `enchant_level` int(11) DEFAULT NULL,
  `loc` varchar(10) DEFAULT NULL,
  `loc_data` int(11) DEFAULT NULL,
  `time_of_use` int(11) DEFAULT NULL,
  `custom_type1` int(11) DEFAULT 0,
  `custom_type2` int(11) DEFAULT 0,
  `mana_left` decimal(5,0) NOT NULL DEFAULT -1,
  `attributes` varchar(50) DEFAULT '',
  `process` varchar(64) NOT NULL DEFAULT '',
  `creator_id` int(11) DEFAULT NULL,
  `first_owner_id` int(11) NOT NULL,
  `creation_time` decimal(16,0) DEFAULT NULL,
  `data` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`object_id`),
  KEY `key_owner_id` (`owner_id`),
  KEY `key_loc` (`loc`),
  KEY `key_item_id` (`item_id`),
  KEY `key_time_of_use` (`time_of_use`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
