/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:33:15
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for castle
-- ----------------------------
DROP TABLE IF EXISTS `castle`;
CREATE TABLE `castle` (
  `id` int(11) NOT NULL DEFAULT 0,
  `name` varchar(25) NOT NULL,
  `taxPercent` int(11) NOT NULL DEFAULT 15,
  `newTaxPercent` int(11) NOT NULL DEFAULT 15,
  `newTaxDate` decimal(20,0) NOT NULL DEFAULT 0,
  `treasury` int(11) NOT NULL DEFAULT 0,
  `bloodaliance` int(11) NOT NULL DEFAULT 0,
  `siegeDate` decimal(20,0) NOT NULL DEFAULT 0,
  `regTimeOver` enum('true','false') NOT NULL DEFAULT 'true',
  `regTimeEnd` decimal(20,0) NOT NULL DEFAULT 0,
  `AutoTime` enum('true','false') NOT NULL DEFAULT 'false',
  `showNpcCrest` enum('true','false') NOT NULL DEFAULT 'false',
  PRIMARY KEY (`name`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `castle` VALUES ('1', 'Gludio', '0', '0', '0', '0', '0', '1749081600007', 'false', '1748658717162', 'true', 'false');
INSERT INTO `castle` VALUES ('2', 'Dion', '0', '0', '0', '0', '0', '1749081600007', 'false', '1748658717207', 'true', 'false');
INSERT INTO `castle` VALUES ('3', 'Giran', '0', '0', '0', '0', '0', '1749081600001', 'false', '1748658717249', 'true', 'false');
INSERT INTO `castle` VALUES ('4', 'Oren', '0', '0', '0', '0', '0', '1749081600001', 'false', '1748658717293', 'true', 'false');
INSERT INTO `castle` VALUES ('5', 'Aden', '0', '0', '0', '0', '0', '1749081600007', 'false', '1748658717342', 'true', 'false');
INSERT INTO `castle` VALUES ('6', 'Innadril', '0', '0', '0', '0', '0', '1749081600001', 'false', '1748658717385', 'true', 'false');
INSERT INTO `castle` VALUES ('7', 'Goddard', '0', '0', '0', '0', '0', '1749081600001', 'false', '1748658717443', 'true', 'false');
INSERT INTO `castle` VALUES ('8', 'Rune', '0', '0', '0', '0', '0', '1749081600007', 'false', '1748658717496', 'true', 'false');
INSERT INTO `castle` VALUES ('9', 'Schuttgart', '0', '0', '0', '0', '0', '1749081600001', 'false', '1748658717516', 'true', 'false');
