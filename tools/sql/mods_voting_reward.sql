/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:17
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for mods_voting_reward
-- ----------------------------
DROP TABLE IF EXISTS `mods_voting_reward`;
CREATE TABLE `mods_voting_reward` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `data` varchar(255) NOT NULL,
  `scope` varchar(255) NOT NULL,
  `time` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
