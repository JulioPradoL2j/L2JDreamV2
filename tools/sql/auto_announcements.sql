/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:32:41
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for auto_announcements
-- ----------------------------
DROP TABLE IF EXISTS `auto_announcements`;
CREATE TABLE `auto_announcements` (
  `id` int(11) NOT NULL,
  `initial` bigint(20) NOT NULL,
  `delay` bigint(20) NOT NULL,
  `cycle` int(11) NOT NULL,
  `memo` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
