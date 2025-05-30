/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:33:08
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for buffer_vip_list
-- ----------------------------
DROP TABLE IF EXISTS `buffer_vip_list`;
CREATE TABLE `buffer_vip_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerId` varchar(40) DEFAULT NULL,
  `playerName` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
