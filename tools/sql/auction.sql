/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:32:28
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for auction
-- ----------------------------
DROP TABLE IF EXISTS `auction`;
CREATE TABLE `auction` (
  `id` int(11) NOT NULL DEFAULT 0,
  `sellerId` int(11) NOT NULL DEFAULT 0,
  `sellerName` varchar(50) NOT NULL DEFAULT 'NPC',
  `sellerClanName` varchar(50) NOT NULL DEFAULT '',
  `itemType` varchar(25) NOT NULL DEFAULT '',
  `itemId` int(11) NOT NULL DEFAULT 0,
  `itemObjectId` int(11) NOT NULL DEFAULT 0,
  `itemName` varchar(40) NOT NULL DEFAULT '',
  `itemQuantity` int(11) NOT NULL DEFAULT 0,
  `startingBid` int(11) NOT NULL DEFAULT 0,
  `currentBid` int(11) NOT NULL DEFAULT 0,
  `endDate` decimal(20,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`itemType`,`itemId`,`itemObjectId`),
  KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `auction` VALUES ('22', '0', 'NPC', 'NPC Clan', 'ClanHall', '22', '0', 'Moonstone Hall', '1', '500000000', '0', '1749177128967');
INSERT INTO `auction` VALUES ('23', '0', 'NPC', 'NPC Clan', 'ClanHall', '23', '0', 'Onyx Hall', '1', '500000000', '0', '1749177128968');
INSERT INTO `auction` VALUES ('24', '0', 'NPC', 'NPC Clan', 'ClanHall', '24', '0', 'Topaz Hall', '1', '500000000', '0', '1749177128969');
INSERT INTO `auction` VALUES ('25', '0', 'NPC', 'NPC Clan', 'ClanHall', '25', '0', 'Ruby Hall', '1', '500000000', '0', '1749177128970');
INSERT INTO `auction` VALUES ('26', '0', 'NPC', 'NPC Clan', 'ClanHall', '26', '0', 'Crystal Hall', '1', '500000000', '0', '1749177128971');
INSERT INTO `auction` VALUES ('27', '0', 'NPC', 'NPC Clan', 'ClanHall', '27', '0', 'Onyx Hall', '1', '500000000', '0', '1749177128972');
INSERT INTO `auction` VALUES ('28', '0', 'NPC', 'NPC Clan', 'ClanHall', '28', '0', 'Sapphire Hall', '1', '500000000', '0', '1749177128973');
INSERT INTO `auction` VALUES ('29', '0', 'NPC', 'NPC Clan', 'ClanHall', '29', '0', 'Moonstone Hall', '1', '500000000', '0', '1749177128974');
INSERT INTO `auction` VALUES ('30', '0', 'NPC', 'NPC Clan', 'ClanHall', '30', '0', 'Emerald Hall', '1', '500000000', '0', '1749177128974');
INSERT INTO `auction` VALUES ('31', '0', 'NPC', 'NPC Clan', 'ClanHall', '31', '0', 'Atramental Barracks', '1', '500000000', '0', '1749177128975');
INSERT INTO `auction` VALUES ('32', '0', 'NPC', 'NPC Clan', 'ClanHall', '32', '0', 'Scarlet Barracks', '1', '500000000', '0', '1749177128976');
INSERT INTO `auction` VALUES ('33', '0', 'NPC', 'NPC Clan', 'ClanHall', '33', '0', 'Viridian Barracks', '1', '500000000', '0', '1749177128977');
INSERT INTO `auction` VALUES ('36', '0', 'NPC', 'NPC Clan', 'ClanHall', '36', '0', 'The Golden Chamber', '1', '900000000', '0', '1749177128977');
INSERT INTO `auction` VALUES ('37', '0', 'NPC', 'NPC Clan', 'ClanHall', '37', '0', 'Silver Chamber', '1', '900000000', '0', '1749177128978');
INSERT INTO `auction` VALUES ('38', '0', 'NPC', 'NPC Clan', 'ClanHall', '38', '0', 'Mithril Chamber', '1', '900000000', '0', '1749177128979');
INSERT INTO `auction` VALUES ('39', '0', 'NPC', 'NPC Clan', 'ClanHall', '39', '0', 'Silver Manor', '1', '900000000', '0', '1749177128980');
INSERT INTO `auction` VALUES ('40', '0', 'NPC', 'NPC Clan', 'ClanHall', '40', '0', 'Gold Manor', '1', '900000000', '0', '1749177128980');
INSERT INTO `auction` VALUES ('43', '0', 'NPC', 'NPC Clan', 'ClanHall', '43', '0', 'The Silver Chamber', '1', '900000000', '0', '1749177128983');
INSERT INTO `auction` VALUES ('44', '0', 'NPC', 'NPC Clan', 'ClanHall', '44', '0', 'Mithril Chamber', '1', '900000000', '0', '1749177128984');
INSERT INTO `auction` VALUES ('46', '0', 'NPC', 'NPC Clan', 'ClanHall', '46', '0', 'Silver Manor', '1', '900000000', '0', '1749177128986');
INSERT INTO `auction` VALUES ('47', '0', 'NPC', 'NPC Clan', 'ClanHall', '47', '0', 'Moonstone Hall', '1', '900000000', '0', '1749177128988');
INSERT INTO `auction` VALUES ('49', '0', 'NPC', 'NPC Clan', 'ClanHall', '49', '0', 'Emerald Hall', '1', '900000000', '0', '1749177128989');
INSERT INTO `auction` VALUES ('50', '0', 'NPC', 'NPC Clan', 'ClanHall', '50', '0', 'Sapphire Hall', '1', '900000000', '0', '1749177128991');
INSERT INTO `auction` VALUES ('51', '0', 'NPC', 'NPC Clan', 'ClanHall', '51', '0', 'Mont Chamber', '1', '900000000', '0', '1749177128992');
INSERT INTO `auction` VALUES ('52', '0', 'NPC', 'NPC Clan', 'ClanHall', '52', '0', 'Astaire Chamber', '1', '900000000', '0', '1749177128993');
INSERT INTO `auction` VALUES ('53', '0', 'NPC', 'NPC Clan', 'ClanHall', '53', '0', 'Aria Chamber', '1', '900000000', '0', '1749177128994');
INSERT INTO `auction` VALUES ('54', '0', 'NPC', 'NPC Clan', 'ClanHall', '54', '0', 'Yiana Chamber', '1', '900000000', '0', '1749177128995');
INSERT INTO `auction` VALUES ('55', '0', 'NPC', 'NPC Clan', 'ClanHall', '55', '0', 'Roien Chamber', '1', '900000000', '0', '1749177128996');
INSERT INTO `auction` VALUES ('56', '0', 'NPC', 'NPC Clan', 'ClanHall', '56', '0', 'Luna Chamber', '1', '900000000', '0', '1749177128997');
INSERT INTO `auction` VALUES ('57', '0', 'NPC', 'NPC Clan', 'ClanHall', '57', '0', 'Traban Chamber', '1', '900000000', '0', '1749177128998');
INSERT INTO `auction` VALUES ('58', '0', 'NPC', 'NPC Clan', 'ClanHall', '58', '0', 'Eisen Hall', '1', '500000000', '0', '1749177128999');
INSERT INTO `auction` VALUES ('59', '0', 'NPC', 'NPC Clan', 'ClanHall', '59', '0', 'Heavy Metal Hall', '1', '500000000', '0', '1749177129000');
INSERT INTO `auction` VALUES ('60', '0', 'NPC', 'NPC Clan', 'ClanHall', '60', '0', 'Molten Ore Hall', '1', '500000000', '0', '1749177129001');
INSERT INTO `auction` VALUES ('61', '0', 'NPC', 'NPC Clan', 'ClanHall', '61', '0', 'Titan Hall', '1', '500000000', '0', '1749177129002');
INSERT INTO `auction` VALUES ('41', '0', 'NPC', 'NPC Clan', 'ClanHall', '41', '0', 'The Bronze Chamber', '1', '900000000', '0', '1749177128981');
INSERT INTO `auction` VALUES ('48', '0', 'NPC', 'NPC Clan', 'ClanHall', '48', '0', 'Onyx Hall', '1', '500000000', '0', '1749177128988');
INSERT INTO `auction` VALUES ('42', '0', 'NPC', 'NPC Clan', 'ClanHall', '42', '0', 'The Golden Chamber', '1', '50000000', '0', '1749177128982');
INSERT INTO `auction` VALUES ('45', '0', 'NPC', 'NPC Clan', 'ClanHall', '45', '0', 'The Bronze Chamber', '1', '50000000', '0', '1749177128985');
