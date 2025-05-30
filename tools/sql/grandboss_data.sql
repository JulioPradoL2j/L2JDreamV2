/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:39:33
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for grandboss_data
-- ----------------------------
DROP TABLE IF EXISTS `grandboss_data`;
CREATE TABLE `grandboss_data` (
  `boss_id` int(11) NOT NULL DEFAULT 0,
  `loc_x` int(11) NOT NULL DEFAULT 0,
  `loc_y` int(11) NOT NULL DEFAULT 0,
  `loc_z` int(11) NOT NULL DEFAULT 0,
  `heading` int(11) NOT NULL DEFAULT 0,
  `respawn_time` bigint(20) NOT NULL DEFAULT 0,
  `currentHP` decimal(8,0) DEFAULT NULL,
  `currentMP` decimal(8,0) DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`boss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `grandboss_data` VALUES ('29001', '-21610', '181594', '-5734', '0', '0', '2179536', '667', '0');
INSERT INTO `grandboss_data` VALUES ('29006', '17726', '108915', '-6480', '0', '0', '162561', '575', '0');
INSERT INTO `grandboss_data` VALUES ('29014', '55024', '17368', '-5412', '10126', '0', '325124', '1660', '0');
INSERT INTO `grandboss_data` VALUES ('29019', '185708', '114298', '-8221', '32768', '0', '13090000', '22197', '0');
INSERT INTO `grandboss_data` VALUES ('29020', '115213', '16623', '10080', '41740', '0', '790857', '3347', '0');
INSERT INTO `grandboss_data` VALUES ('29022', '55312', '219168', '-3223', '0', '0', '858518', '1975', '0');
INSERT INTO `grandboss_data` VALUES ('29028', '213004', '-114890', '-1595', '0', '0', '16660000', '22197', '0');
INSERT INTO `grandboss_data` VALUES ('29045', '0', '0', '0', '0', '0', '790857', '1859', '0');
INSERT INTO `grandboss_data` VALUES ('29046', '0', '0', '0', '0', '0', '63', '44', '0');
INSERT INTO `grandboss_data` VALUES ('29047', '0', '0', '0', '0', '0', '350000', '85', '0');
INSERT INTO `grandboss_data` VALUES ('29054', '0', '0', '0', '0', '0', '300000', '2000', '0');
INSERT INTO `grandboss_data` VALUES ('29062', '0', '0', '0', '0', '0', null, null, '1');
INSERT INTO `grandboss_data` VALUES ('29066', '185708', '114298', '-8221', '32768', '0', '11186000', '1998000', '0');
INSERT INTO `grandboss_data` VALUES ('29067', '185708', '114298', '-8221', '32768', '0', '14518000', '1998000', '0');
INSERT INTO `grandboss_data` VALUES ('29068', '185708', '114298', '-8221', '32768', '0', '17850000', '1998000', '0');
