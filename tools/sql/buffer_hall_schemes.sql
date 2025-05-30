/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:32:52
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for buffer_hall_schemes
-- ----------------------------
DROP TABLE IF EXISTS `buffer_hall_schemes`;
CREATE TABLE `buffer_hall_schemes` (
  `object_id` int(10) unsigned NOT NULL DEFAULT 0,
  `scheme_name` varchar(16) NOT NULL DEFAULT 'default',
  `skills` varchar(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
