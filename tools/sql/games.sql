/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:39:17
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for games
-- ----------------------------
DROP TABLE IF EXISTS `games`;
CREATE TABLE `games` (
  `id` int(11) NOT NULL DEFAULT 0,
  `idnr` int(11) NOT NULL DEFAULT 0,
  `number1` int(11) NOT NULL DEFAULT 0,
  `number2` int(11) NOT NULL DEFAULT 0,
  `prize` int(11) NOT NULL DEFAULT 0,
  `newprize` int(11) NOT NULL DEFAULT 0,
  `prize1` int(11) NOT NULL DEFAULT 0,
  `prize2` int(11) NOT NULL DEFAULT 0,
  `prize3` int(11) NOT NULL DEFAULT 0,
  `enddate` decimal(20,0) NOT NULL DEFAULT 0,
  `finished` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`,`idnr`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `games` VALUES ('1', '1', '0', '0', '50000', '50000', '0', '0', '0', '1661637600404', '0');
