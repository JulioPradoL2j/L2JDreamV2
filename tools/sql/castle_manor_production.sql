/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:33:34
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for castle_manor_production
-- ----------------------------
DROP TABLE IF EXISTS `castle_manor_production`;
CREATE TABLE `castle_manor_production` (
  `castle_id` int(11) NOT NULL DEFAULT 0,
  `seed_id` int(11) NOT NULL DEFAULT 0,
  `can_produce` int(11) NOT NULL DEFAULT 0,
  `start_produce` int(11) NOT NULL DEFAULT 0,
  `seed_price` int(11) NOT NULL DEFAULT 0,
  `period` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`castle_id`,`seed_id`,`period`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
