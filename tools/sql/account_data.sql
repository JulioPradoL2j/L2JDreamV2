/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:31:49
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for account_data
-- ----------------------------
DROP TABLE IF EXISTS `account_data`;
CREATE TABLE `account_data` (
  `account_name` varchar(32) NOT NULL,
  `valueName` varchar(32) NOT NULL,
  `valueData` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`account_name`,`valueName`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
