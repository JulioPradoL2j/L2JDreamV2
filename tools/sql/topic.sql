/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:41:38
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for topic
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic` (
  `topic_id` int(8) NOT NULL DEFAULT 0,
  `topic_forum_id` int(8) NOT NULL DEFAULT 0,
  `topic_name` varchar(255) NOT NULL DEFAULT '',
  `topic_date` decimal(20,0) NOT NULL DEFAULT 0,
  `topic_ownername` varchar(255) NOT NULL DEFAULT '0',
  `topic_ownerid` int(8) NOT NULL DEFAULT 0,
  `topic_type` int(8) NOT NULL DEFAULT 0,
  `topic_reply` int(8) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
