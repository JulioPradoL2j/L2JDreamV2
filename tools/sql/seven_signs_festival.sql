/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:41:21
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for seven_signs_festival
-- ----------------------------
DROP TABLE IF EXISTS `seven_signs_festival`;
CREATE TABLE `seven_signs_festival` (
  `festivalId` int(1) NOT NULL DEFAULT 0,
  `cabal` varchar(4) NOT NULL DEFAULT '',
  `cycle` int(4) NOT NULL DEFAULT 0,
  `date` bigint(50) DEFAULT 0,
  `score` int(5) NOT NULL DEFAULT 0,
  `members` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`festivalId`,`cabal`,`cycle`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `seven_signs_festival` VALUES ('0', 'dawn', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dawn', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dawn', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dawn', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dawn', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dusk', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dusk', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dusk', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dusk', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dusk', '1', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dusk', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dusk', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dusk', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dusk', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dusk', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dawn', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dawn', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dawn', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dawn', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dawn', '2', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dusk', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dusk', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dusk', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dusk', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dusk', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dawn', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dawn', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dawn', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dawn', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dawn', '3', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dusk', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dusk', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dusk', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dusk', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dusk', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dawn', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dawn', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dawn', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dawn', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dawn', '4', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dusk', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dusk', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dusk', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dusk', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dusk', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dawn', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dawn', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dawn', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dawn', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dawn', '5', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dusk', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dusk', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dusk', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dusk', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dusk', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('0', 'dawn', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('1', 'dawn', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('2', 'dawn', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('3', 'dawn', '6', '0', '0', '');
INSERT INTO `seven_signs_festival` VALUES ('4', 'dawn', '6', '0', '0', '');
