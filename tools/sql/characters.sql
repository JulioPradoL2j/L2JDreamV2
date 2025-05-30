/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:36:16
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for characters
-- ----------------------------
DROP TABLE IF EXISTS `characters`;
CREATE TABLE `characters` (
  `account_name` varchar(45) DEFAULT NULL,
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `char_name` varchar(35) NOT NULL,
  `level` tinyint(3) unsigned DEFAULT NULL,
  `maxHp` mediumint(8) unsigned DEFAULT NULL,
  `curHp` mediumint(8) unsigned DEFAULT NULL,
  `maxCp` mediumint(8) unsigned DEFAULT NULL,
  `curCp` mediumint(8) unsigned DEFAULT NULL,
  `maxMp` mediumint(8) unsigned DEFAULT NULL,
  `curMp` mediumint(8) unsigned DEFAULT NULL,
  `face` tinyint(3) unsigned DEFAULT NULL,
  `hairStyle` tinyint(3) unsigned DEFAULT NULL,
  `hairColor` tinyint(3) unsigned DEFAULT NULL,
  `sex` tinyint(3) unsigned DEFAULT NULL,
  `heading` mediumint(9) DEFAULT NULL,
  `x` mediumint(9) DEFAULT NULL,
  `y` mediumint(9) DEFAULT NULL,
  `z` mediumint(9) DEFAULT NULL,
  `exp` bigint(20) unsigned DEFAULT 0,
  `expBeforeDeath` bigint(20) unsigned DEFAULT 0,
  `sp` int(10) unsigned NOT NULL DEFAULT 0,
  `karma` int(10) unsigned DEFAULT NULL,
  `pvpkills` smallint(5) unsigned DEFAULT NULL,
  `pkkills` smallint(5) unsigned DEFAULT NULL,
  `clanid` int(10) unsigned DEFAULT NULL,
  `race` tinyint(3) unsigned DEFAULT NULL,
  `classid` tinyint(3) unsigned DEFAULT NULL,
  `base_class` tinyint(3) unsigned DEFAULT NULL,
  `deletetime` bigint(20) DEFAULT NULL,
  `cancraft` tinyint(3) unsigned DEFAULT NULL,
  `title` varchar(16) DEFAULT NULL,
  `rec_have` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `rec_left` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `online` tinyint(3) unsigned DEFAULT NULL,
  `onlinetime` int(11) DEFAULT NULL,
  `char_slot` tinyint(3) unsigned DEFAULT NULL,
  `newbie` mediumint(8) unsigned DEFAULT 1,
  `lastAccess` bigint(20) unsigned DEFAULT NULL,
  `clan_privs` mediumint(8) unsigned DEFAULT 0,
  `wantspeace` tinyint(3) unsigned DEFAULT 0,
  `isin7sdungeon` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `in_jail` tinyint(3) unsigned DEFAULT 0,
  `jail_timer` int(10) unsigned DEFAULT 0,
  `nobless` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `subpledge` smallint(6) NOT NULL DEFAULT 0,
  `last_recom_date` bigint(20) unsigned NOT NULL DEFAULT 0,
  `pledge_rank` mediumint(9) NOT NULL DEFAULT 0,
  `lvl_joined_academy` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `apprentice` int(10) unsigned NOT NULL DEFAULT 0,
  `sponsor` int(10) unsigned NOT NULL DEFAULT 0,
  `varka_ketra_ally` tinyint(4) NOT NULL DEFAULT 0,
  `clan_join_expiry_time` bigint(20) unsigned NOT NULL DEFAULT 0,
  `clan_create_expiry_time` bigint(20) unsigned NOT NULL DEFAULT 0,
  `death_penalty_level` smallint(5) unsigned NOT NULL DEFAULT 0,
  `pccaffe_points` int(11) NOT NULL DEFAULT 0,
  `isCensor` decimal(1,0) NOT NULL DEFAULT 0,
  `isBanned` decimal(1,0) NOT NULL DEFAULT 0,
  `vip` decimal(1,0) NOT NULL DEFAULT 0,
  `vip_end` decimal(20,0) NOT NULL DEFAULT 0,
  `aio` decimal(1,0) NOT NULL DEFAULT 0,
  `aio_end` decimal(20,0) NOT NULL DEFAULT 0,
  `arena_wins` int(11) NOT NULL DEFAULT 0,
  `arena_defeats` int(11) NOT NULL DEFAULT 0,
  `haswhacc` int(15) NOT NULL DEFAULT 0,
  `whaccid` varchar(45) DEFAULT NULL,
  `whaccpwd` varchar(45) DEFAULT NULL,
  `buffshop_slots` tinyint(3) unsigned NOT NULL DEFAULT 8,
  `accesslevel` mediumint(9) DEFAULT 0,
  PRIMARY KEY (`charId`),
  UNIQUE KEY `CHAR_NAME` (`char_name`),
  KEY `clanid` (`clanid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
