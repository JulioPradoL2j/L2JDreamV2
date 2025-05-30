/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:37:09
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for custom_armorsets
-- ----------------------------
DROP TABLE IF EXISTS `custom_armorsets`;
CREATE TABLE `custom_armorsets` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `chest` smallint(5) unsigned NOT NULL DEFAULT 0,
  `legs` smallint(5) unsigned NOT NULL DEFAULT 0,
  `head` smallint(5) unsigned NOT NULL DEFAULT 0,
  `gloves` smallint(5) unsigned NOT NULL DEFAULT 0,
  `feet` smallint(5) unsigned NOT NULL DEFAULT 0,
  `skill_id` smallint(5) unsigned NOT NULL DEFAULT 0,
  `skill_lvl` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `skillset_id` smallint(5) unsigned NOT NULL DEFAULT 0,
  `shield` smallint(5) unsigned NOT NULL DEFAULT 0,
  `shield_skill_id` smallint(5) unsigned NOT NULL DEFAULT 0,
  `enchant6skill` smallint(5) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`,`chest`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
