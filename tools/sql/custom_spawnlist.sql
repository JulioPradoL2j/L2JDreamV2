/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:38:18
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for custom_spawnlist
-- ----------------------------
DROP TABLE IF EXISTS `custom_spawnlist`;
CREATE TABLE `custom_spawnlist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `location` varchar(40) NOT NULL DEFAULT '',
  `count` int(9) NOT NULL DEFAULT 0,
  `npc_templateid` int(9) NOT NULL DEFAULT 0,
  `locx` int(9) NOT NULL DEFAULT 0,
  `locy` int(9) NOT NULL DEFAULT 0,
  `locz` int(9) NOT NULL DEFAULT 0,
  `heading` int(9) NOT NULL DEFAULT 0,
  `respawn_delay` int(9) NOT NULL DEFAULT 0,
  `loc_id` int(9) NOT NULL DEFAULT 0,
  `periodOfDay` decimal(2,0) DEFAULT 0,
  `random_zone` int(9) NOT NULL DEFAULT -1,
  PRIMARY KEY (`id`),
  KEY `key_npc_templateid` (`npc_templateid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
