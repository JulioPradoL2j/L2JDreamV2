/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:39:36
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for grandboss_intervallist
-- ----------------------------
DROP TABLE IF EXISTS `grandboss_intervallist`;
CREATE TABLE `grandboss_intervallist` (
  `boss_id` int(11) NOT NULL,
  `respawn_time` decimal(20,0) NOT NULL,
  `state` int(11) NOT NULL,
  PRIMARY KEY (`boss_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `grandboss_intervallist` VALUES ('29019', '1661200105304', '0');
INSERT INTO `grandboss_intervallist` VALUES ('29020', '1661149818671', '0');
INSERT INTO `grandboss_intervallist` VALUES ('29022', '1660751587894', '1');
INSERT INTO `grandboss_intervallist` VALUES ('29028', '1655926319436', '0');
INSERT INTO `grandboss_intervallist` VALUES ('29045', '1657600932699', '0');
INSERT INTO `grandboss_intervallist` VALUES ('29062', '1661961732963', '1');
INSERT INTO `grandboss_intervallist` VALUES ('29065', '1661315285526', '0');
INSERT INTO `grandboss_intervallist` VALUES ('29099', '0', '0');
INSERT INTO `grandboss_intervallist` VALUES ('29001', '1661150214695', '1');
INSERT INTO `grandboss_intervallist` VALUES ('29006', '1661352687713', '1');
INSERT INTO `grandboss_intervallist` VALUES ('29014', '1661266264988', '1');
INSERT INTO `grandboss_intervallist` VALUES ('29096', '0', '1');
