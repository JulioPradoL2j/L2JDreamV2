/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:41
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for petitions
-- ----------------------------
DROP TABLE IF EXISTS `petitions`;
CREATE TABLE `petitions` (
  `petition_id` int(11) NOT NULL AUTO_INCREMENT,
  `charId` int(11) NOT NULL DEFAULT 0,
  `petition_txt` text NOT NULL,
  `status` varchar(255) NOT NULL DEFAULT 'New',
  PRIMARY KEY (`petition_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
