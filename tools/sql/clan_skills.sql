/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:36:30
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for clan_skills
-- ----------------------------
DROP TABLE IF EXISTS `clan_skills`;
CREATE TABLE `clan_skills` (
  `clan_id` int(11) NOT NULL DEFAULT 0,
  `skill_id` int(11) NOT NULL DEFAULT 0,
  `skill_level` int(5) NOT NULL DEFAULT 0,
  `skill_name` varchar(26) DEFAULT NULL,
  PRIMARY KEY (`clan_id`,`skill_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
