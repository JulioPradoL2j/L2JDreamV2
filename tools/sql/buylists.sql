/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:33:11
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for buylists
-- ----------------------------
DROP TABLE IF EXISTS `buylists`;
CREATE TABLE `buylists` (
  `buylist_id` int(10) unsigned NOT NULL,
  `item_id` int(10) unsigned NOT NULL,
  `count` int(10) unsigned NOT NULL DEFAULT 0,
  `next_restock_time` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`buylist_id`,`item_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
