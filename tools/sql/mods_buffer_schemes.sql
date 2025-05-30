/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:11
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for mods_buffer_schemes
-- ----------------------------
DROP TABLE IF EXISTS `mods_buffer_schemes`;
CREATE TABLE `mods_buffer_schemes` (
  `ownerId` int(10) unsigned NOT NULL DEFAULT 0,
  `id` int(10) unsigned NOT NULL DEFAULT 0,
  `level` int(10) unsigned NOT NULL DEFAULT 0,
  `scheme` varchar(20) NOT NULL DEFAULT 'default'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
