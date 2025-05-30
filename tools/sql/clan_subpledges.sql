/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:36:35
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for clan_subpledges
-- ----------------------------
DROP TABLE IF EXISTS `clan_subpledges`;
CREATE TABLE `clan_subpledges` (
  `clan_id` int(11) NOT NULL DEFAULT 0,
  `sub_pledge_id` int(11) NOT NULL DEFAULT 0,
  `name` varchar(45) DEFAULT NULL,
  `leader_id` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`clan_id`,`sub_pledge_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
