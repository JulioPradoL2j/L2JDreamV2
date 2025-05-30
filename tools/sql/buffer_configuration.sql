/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:32:48
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for buffer_configuration
-- ----------------------------
DROP TABLE IF EXISTS `buffer_configuration`;
CREATE TABLE `buffer_configuration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `configDesc` varchar(30) DEFAULT NULL,
  `configInfo` varchar(150) DEFAULT NULL,
  `configName` varchar(30) DEFAULT NULL,
  `configValue` varchar(30) DEFAULT NULL,
  `usableValues` varchar(40) DEFAULT NULL,
  `canEditOnline` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `buffer_configuration` VALUES ('1', 'Window Title', 'Seperate new words with \',\' ! Example: My,buffer', 'title', 'test', 'string,3,36', '1');
INSERT INTO `buffer_configuration` VALUES ('2', 'Window Style', 'Select a value from box and hit update', 'style', 'Icons+Buttons', 'custom,0,0', '1');
INSERT INTO `buffer_configuration` VALUES ('3', 'Consumable ID', 'Enter a valid item ID', 'consumableId', '57', 'range,1,10000', '1');
INSERT INTO `buffer_configuration` VALUES ('4', 'Minimum level', 'Enter a value from 1 to 85', 'minLevel', '2', 'range,1,85', '1');
INSERT INTO `buffer_configuration` VALUES ('5', 'Buff player with karma', 'Select a value from box and hit update', 'buffWithKarma', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('6', 'Buffs for free', 'Select a value from box and hit update', 'freeBuffs', 'False', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('7', 'Enable healing option', 'Select a value from box and hit update', 'enableHeal', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('8', 'Healing price', 'Enter a value from 1 to 2000000000', 'healPrice', '1000', 'range,1,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('9', 'Enable removing buffs option', 'Select a value from box and hit update', 'enableBuffRemove', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('10', 'Buff removing price', 'Enter a value from 1 to 2000000000', 'buffRemovePrice', '5000', 'range,1,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('11', 'Enable Buffs', 'Select a value from box and hit update', 'enableBuffs', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('12', 'Enable Songs', 'Select a value from box and hit update', 'enableSongs', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('13', 'Enable Dances', 'Select a value from box and hit update', 'enableDances', 'False', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('14', 'Enable Chants', 'Select a value from box and hit update', 'enableChants', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('15', 'Enable Kamael buffs', 'Select a value from box and hit update', 'enableKamael', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('16', 'Enable Special buffs', 'Select a value from box and hit update', 'enableSpecial', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('17', 'Buff price', 'Enter a value from 1 to 2000000000', 'buffPrice', '2500', 'range,1,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('18', 'Song price', 'Enter a value from 1 to 2000000000', 'songPrice', '10000', 'range,1,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('19', 'Dance price', 'Enter a value from 1 to 2000000000', 'dancePrice', '10000', 'range,1,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('20', 'Chant price', 'Enter a value from 1 to 2000000000', 'chantPrice', '10000', 'range,1,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('21', 'Kamael buff price', 'Enter a value from 1 to 2000000000', 'kamaelPrice', '10000', 'range,1,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('22', 'Special buff price', 'Enter a value from 1 to 2000000000', 'specialPrice', '100000', 'range,1,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('23', 'Enable time out', 'Select a value from box and hit update', 'timeOut', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('24', 'Time out time', 'Enter a value from 1 to 10', 'timeOutTime', '2', 'range,1,10', '1');
INSERT INTO `buffer_configuration` VALUES ('25', 'GM minimum access level', 'Enter a value from 0 to 200', 'gmAccessLevel', '100', 'range,0,200', '1');
INSERT INTO `buffer_configuration` VALUES ('26', 'Enable online editing', 'Select a value from box and hit update', 'enableOnlineEditing', 'True', 'bool,True,False', '0');
INSERT INTO `buffer_configuration` VALUES ('27', 'Enable VIP players', 'Select a value from box and hit update', 'enableVipSystem', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('28', 'Vip Buff Price (SP)', 'Enter a value from 0 to 2000000000', 'vipBuffPrice', '1000000', 'range,0,2000000000', '1');
INSERT INTO `buffer_configuration` VALUES ('29', 'Sort Buffs', 'Select a value from box and hit update', 'sortBuffs', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('30', 'Enable Buff Set', 'Select a value from box and hit update', 'enableBuffSet', 'True', 'bool,True,False', '1');
INSERT INTO `buffer_configuration` VALUES ('31', 'Buff Set Price', 'Enter a value from 0 to 2000000000', 'buffSetPrice', '1000000', 'range,0,2000000000', '1');
