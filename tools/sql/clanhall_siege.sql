/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:36:50
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for clanhall_siege
-- ----------------------------
DROP TABLE IF EXISTS `clanhall_siege`;
CREATE TABLE `clanhall_siege` (
  `id` int(11) NOT NULL DEFAULT 0,
  `name` varchar(40) NOT NULL DEFAULT '',
  `siege_data` decimal(20,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `clanhall_siege` VALUES ('21', 'Fortress of Resistance', '1749258000804');
INSERT INTO `clanhall_siege` VALUES ('34', 'Devastated Castle', '1749258000522');
INSERT INTO `clanhall_siege` VALUES ('35', 'Bandit Stronghold', '1749258000644');
INSERT INTO `clanhall_siege` VALUES ('63', 'Wild Beast Farm', '1749258000649');
INSERT INTO `clanhall_siege` VALUES ('64', 'Fortress of Dead', '1749258000533');
