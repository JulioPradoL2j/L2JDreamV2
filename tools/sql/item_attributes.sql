/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:39:47
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for item_attributes
-- ----------------------------
DROP TABLE IF EXISTS `item_attributes`;
CREATE TABLE `item_attributes` (
  `itemId` int(11) NOT NULL DEFAULT 0,
  `augAttributes` int(11) NOT NULL DEFAULT -1,
  `augSkillId` int(11) NOT NULL DEFAULT -1,
  `augSkillLevel` int(11) NOT NULL DEFAULT -1,
  PRIMARY KEY (`itemId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
