/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:34:46
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_items
-- ----------------------------
DROP TABLE IF EXISTS `character_items`;
CREATE TABLE `character_items` (
  `owner_id` int(11) DEFAULT NULL,
  `item_id` int(11) DEFAULT NULL,
  `count` bigint(20) DEFAULT 1,
  `enchant_level` int(11) DEFAULT 0
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
