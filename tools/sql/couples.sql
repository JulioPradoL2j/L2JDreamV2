/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:36:57
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for couples
-- ----------------------------
DROP TABLE IF EXISTS `couples`;
CREATE TABLE `couples` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player1Id` int(11) NOT NULL DEFAULT 0,
  `player2Id` int(11) NOT NULL DEFAULT 0,
  `maried` varchar(5) DEFAULT NULL,
  `affiancedDate` decimal(20,0) DEFAULT 0,
  `weddingDate` decimal(20,0) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
