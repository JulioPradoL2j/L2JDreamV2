/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:37
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for passkey
-- ----------------------------
DROP TABLE IF EXISTS `passkey`;
CREATE TABLE `passkey` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `passkey` varchar(45) DEFAULT NULL,
  `question` varchar(55) NOT NULL,
  `answer` varchar(35) NOT NULL,
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
