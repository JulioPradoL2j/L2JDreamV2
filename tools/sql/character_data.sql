/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:34:16
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_data
-- ----------------------------
DROP TABLE IF EXISTS `character_data`;
CREATE TABLE `character_data` (
  `charId` int(11) NOT NULL,
  `valueName` varchar(32) NOT NULL,
  `valueData` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`charId`,`valueName`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
