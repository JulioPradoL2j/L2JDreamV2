/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:33:42
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for castle_trapupgrade
-- ----------------------------
DROP TABLE IF EXISTS `castle_trapupgrade`;
CREATE TABLE `castle_trapupgrade` (
  `castleId` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `towerIndex` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `level` tinyint(3) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`towerIndex`,`castleId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
