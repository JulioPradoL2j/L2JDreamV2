/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:41:10
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for reward_list
-- ----------------------------
DROP TABLE IF EXISTS `reward_list`;
CREATE TABLE `reward_list` (
  `charid` int(11) NOT NULL DEFAULT 0,
  `itemId` int(11) NOT NULL DEFAULT 0,
  `count` int(22) NOT NULL DEFAULT 0,
  `castle_name` varchar(50) NOT NULL DEFAULT '',
  `rewarded` int(2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charid`,`itemId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
