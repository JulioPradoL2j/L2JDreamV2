/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:33:23
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for castle_doorupgrade
-- ----------------------------
DROP TABLE IF EXISTS `castle_doorupgrade`;
CREATE TABLE `castle_doorupgrade` (
  `doorId` int(11) NOT NULL DEFAULT 0,
  `hp` tinyint(4) NOT NULL DEFAULT 0,
  `castleId` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`doorId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
