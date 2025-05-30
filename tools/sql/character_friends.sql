/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:34:31
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_friends
-- ----------------------------
DROP TABLE IF EXISTS `character_friends`;
CREATE TABLE `character_friends` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `friendId` int(10) unsigned NOT NULL DEFAULT 0,
  `friend_name` varchar(35) NOT NULL DEFAULT '',
  PRIMARY KEY (`charId`,`friend_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
