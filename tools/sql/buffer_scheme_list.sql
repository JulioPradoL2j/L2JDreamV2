/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:33:00
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for buffer_scheme_list
-- ----------------------------
DROP TABLE IF EXISTS `buffer_scheme_list`;
CREATE TABLE `buffer_scheme_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` varchar(40) DEFAULT NULL,
  `scheme_name` varchar(36) DEFAULT NULL,
  `mod_accepted` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
