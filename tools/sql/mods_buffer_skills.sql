/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:14
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for mods_buffer_skills
-- ----------------------------
DROP TABLE IF EXISTS `mods_buffer_skills`;
CREATE TABLE `mods_buffer_skills` (
  `id` int(10) unsigned NOT NULL DEFAULT 0,
  `level` int(10) unsigned NOT NULL DEFAULT 0,
  `skill_group` varchar(20) NOT NULL DEFAULT 'default',
  `adena` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
