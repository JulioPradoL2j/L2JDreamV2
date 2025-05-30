/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:32:37
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for auction_watch
-- ----------------------------
DROP TABLE IF EXISTS `auction_watch`;
CREATE TABLE `auction_watch` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `auctionId` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`auctionId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
