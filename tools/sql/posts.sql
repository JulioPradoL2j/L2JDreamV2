/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 30/05/2025 09:40:52
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for posts
-- ----------------------------
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts` (
  `post_id` int(8) NOT NULL DEFAULT 0,
  `post_owner_name` varchar(255) NOT NULL DEFAULT '',
  `post_ownerid` int(8) NOT NULL DEFAULT 0,
  `post_date` decimal(20,0) NOT NULL DEFAULT 0,
  `post_topic_id` int(8) NOT NULL DEFAULT 0,
  `post_forum_id` int(8) NOT NULL DEFAULT 0,
  `post_txt` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
