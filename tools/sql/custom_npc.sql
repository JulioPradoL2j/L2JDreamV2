/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:37:34
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for custom_npc
-- ----------------------------
DROP TABLE IF EXISTS `custom_npc`;
CREATE TABLE `custom_npc` (
  `id` decimal(11,0) NOT NULL DEFAULT 0,
  `idTemplate` int(11) NOT NULL DEFAULT 0,
  `name` varchar(200) DEFAULT NULL,
  `serverSideName` int(1) DEFAULT 0,
  `title` varchar(45) DEFAULT '',
  `race` int(11) NOT NULL DEFAULT 5,
  `serverSideTitle` int(1) DEFAULT 0,
  `collision_radius` decimal(5,2) DEFAULT NULL,
  `collision_height` decimal(5,2) DEFAULT NULL,
  `level` decimal(2,0) DEFAULT NULL,
  `sex` varchar(6) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `attackrange` int(11) DEFAULT NULL,
  `hp` decimal(8,0) DEFAULT NULL,
  `mp` decimal(8,0) DEFAULT NULL,
  `hpreg` decimal(8,2) DEFAULT NULL,
  `mpreg` decimal(5,2) DEFAULT NULL,
  `str` decimal(7,0) DEFAULT NULL,
  `con` decimal(7,0) DEFAULT NULL,
  `dex` decimal(7,0) DEFAULT NULL,
  `int` decimal(7,0) DEFAULT NULL,
  `wit` decimal(7,0) DEFAULT NULL,
  `men` decimal(7,0) DEFAULT NULL,
  `exp` decimal(9,0) DEFAULT NULL,
  `sp` decimal(8,0) DEFAULT NULL,
  `patk` decimal(5,0) DEFAULT NULL,
  `pdef` decimal(5,0) DEFAULT NULL,
  `matk` decimal(5,0) DEFAULT NULL,
  `mdef` decimal(5,0) DEFAULT NULL,
  `atkspd` decimal(3,0) DEFAULT NULL,
  `aggro` decimal(6,0) DEFAULT NULL,
  `matkspd` decimal(4,0) DEFAULT NULL,
  `rhand` decimal(8,0) DEFAULT NULL,
  `lhand` decimal(8,0) DEFAULT NULL,
  `armor` decimal(1,0) DEFAULT NULL,
  `walkspd` decimal(3,0) DEFAULT NULL,
  `runspd` decimal(3,0) DEFAULT NULL,
  `faction_id` varchar(40) DEFAULT NULL,
  `faction_range` decimal(4,0) DEFAULT NULL,
  `isUndead` int(11) DEFAULT 0,
  `absorb_level` decimal(2,0) DEFAULT 0,
  `absorb_type` enum('FULL_PARTY','LAST_HIT','PARTY_ONE_RANDOM') NOT NULL DEFAULT 'LAST_HIT',
  `ss` int(4) DEFAULT 0,
  `bss` int(4) DEFAULT 0,
  `ss_rate` int(3) DEFAULT 0,
  `AI` varchar(8) DEFAULT 'fighter',
  `drop_herbs` enum('true','false') NOT NULL DEFAULT 'false',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
