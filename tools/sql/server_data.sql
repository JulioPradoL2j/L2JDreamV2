/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:41:13
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for server_data
-- ----------------------------
DROP TABLE IF EXISTS `server_data`;
CREATE TABLE `server_data` (
  `valueName` varchar(64) NOT NULL,
  `valueData` varchar(200) NOT NULL,
  PRIMARY KEY (`valueName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `server_data` VALUES ('GameTime.day', '20');
INSERT INTO `server_data` VALUES ('GameTime.hour', '22');
INSERT INTO `server_data` VALUES ('GameTime.minute', '36');
INSERT INTO `server_data` VALUES ('GameTime.month', '2');
INSERT INTO `server_data` VALUES ('GameTime.started', '1651420059331');
INSERT INTO `server_data` VALUES ('GameTime.year', '1282');
INSERT INTO `server_data` VALUES ('Olympiad.CurrentCycle', '3');
INSERT INTO `server_data` VALUES ('Olympiad.NextWeeklyChange', '1749177821424');
INSERT INTO `server_data` VALUES ('Olympiad.OlympiadEnd', '1748746860424');
INSERT INTO `server_data` VALUES ('Olympiad.Period', '0');
INSERT INTO `server_data` VALUES ('Olympiad.ValdationEnd', '1659927660118');
