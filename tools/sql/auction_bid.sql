/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:32:33
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for auction_bid
-- ----------------------------
DROP TABLE IF EXISTS `auction_bid`;
CREATE TABLE `auction_bid` (
  `id` int(11) NOT NULL DEFAULT 0,
  `auctionId` int(11) NOT NULL DEFAULT 0,
  `bidderId` int(11) NOT NULL DEFAULT 0,
  `bidderName` varchar(50) NOT NULL,
  `clan_name` varchar(50) NOT NULL,
  `maxBid` int(11) NOT NULL DEFAULT 0,
  `time_bid` decimal(20,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`auctionId`,`bidderId`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
