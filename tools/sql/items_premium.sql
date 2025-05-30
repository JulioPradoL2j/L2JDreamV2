/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:02
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for items_premium
-- ----------------------------
DROP TABLE IF EXISTS `items_premium`;
CREATE TABLE `items_premium` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_id` varchar(255) NOT NULL DEFAULT '0;',
  `preco` varchar(255) NOT NULL DEFAULT '0;',
  `quantidade` varchar(255) NOT NULL DEFAULT '1;',
  `enchant` varchar(255) NOT NULL DEFAULT '0;',
  `attribute_fire` varchar(255) NOT NULL DEFAULT '0;',
  `attribute_water` varchar(255) NOT NULL DEFAULT '0;',
  `attribute_wind` varchar(255) NOT NULL DEFAULT '0;',
  `attribute_earth` varchar(255) NOT NULL DEFAULT '0;',
  `attribute_holy` varchar(255) NOT NULL DEFAULT '0;',
  `attribute_unholy` varchar(255) NOT NULL DEFAULT '0;',
  `combo` int(11) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `key_owner_id` (`id`),
  KEY `key_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
