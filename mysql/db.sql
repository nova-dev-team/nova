-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.75-0ubuntu10


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema nova_core
--

CREATE DATABASE IF NOT EXISTS nova_core;
USE nova_core;

--
-- Definition of table `nova_core`.`net_pools`
--

DROP TABLE IF EXISTS `nova_core`.`net_pools`;
CREATE TABLE  `nova_core`.`net_pools` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `begin` varchar(255) NOT NULL,
  `mask` varchar(255) NOT NULL,
  `size` int(11) NOT NULL,
  `used` tinyint(1) default '0',
  `lock_version` int(11) default '0',
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `nova_core`.`net_pools`
--

/*!40000 ALTER TABLE `net_pools` DISABLE KEYS */;
LOCK TABLES `net_pools` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `net_pools` ENABLE KEYS */;


--
-- Definition of table `nova_core`.`notifies`
--

DROP TABLE IF EXISTS `nova_core`.`notifies`;
CREATE TABLE  `nova_core`.`notifies` (
  `id` int(11) NOT NULL auto_increment,
  `notify_uuid` varchar(255) default NULL,
  `notify_receiver_type` varchar(255) default NULL,
  `notify_receiver_id` int(11) default NULL,
  `notify_type` varchar(255) default NULL,
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `nova_core`.`notifies`
--

/*!40000 ALTER TABLE `notifies` DISABLE KEYS */;
LOCK TABLES `notifies` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `notifies` ENABLE KEYS */;


--
-- Definition of table `nova_core`.`pmachines`
--

DROP TABLE IF EXISTS `nova_core`.`pmachines`;
CREATE TABLE  `nova_core`.`pmachines` (
  `id` int(11) NOT NULL auto_increment,
  `ip` varchar(255) default NULL,
  `status` varchar(255) default 'working',
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `nova_core`.`pmachines`
--

/*!40000 ALTER TABLE `pmachines` DISABLE KEYS */;
LOCK TABLES `pmachines` WRITE;
INSERT INTO `nova_core`.`pmachines` VALUES  (1,'10.0.0.210','pending remove','2009-04-22 08:02:43','2009-04-27 03:56:19'),
 (2,'10.0.0.220','working','2009-04-27 03:56:29','2009-04-27 03:56:29');
UNLOCK TABLES;
/*!40000 ALTER TABLE `pmachines` ENABLE KEYS */;


--
-- Definition of table `nova_core`.`schema_migrations`
--

DROP TABLE IF EXISTS `nova_core`.`schema_migrations`;
CREATE TABLE  `nova_core`.`schema_migrations` (
  `version` varchar(255) NOT NULL,
  UNIQUE KEY `unique_schema_migrations` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `nova_core`.`schema_migrations`
--

/*!40000 ALTER TABLE `schema_migrations` DISABLE KEYS */;
LOCK TABLES `schema_migrations` WRITE;
INSERT INTO `nova_core`.`schema_migrations` VALUES  ('20090331031132'),
 ('20090331031137'),
 ('20090331031142'),
 ('20090331031146'),
 ('20090413073037'),
 ('20090417054023');
UNLOCK TABLES;
/*!40000 ALTER TABLE `schema_migrations` ENABLE KEYS */;


--
-- Definition of table `nova_core`.`users`
--

DROP TABLE IF EXISTS `nova_core`.`users`;
CREATE TABLE  `nova_core`.`users` (
  `id` int(11) NOT NULL auto_increment,
  `email` varchar(255) default NULL,
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=266 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `nova_core`.`users`
--

/*!40000 ALTER TABLE `users` DISABLE KEYS */;
LOCK TABLES `users` WRITE;
INSERT INTO `nova_core`.`users` VALUES  (1,'misamisa','2009-04-21 15:33:08','2009-04-21 15:33:08'),
 (262,'madao','2009-04-26 14:21:50','2009-04-26 14:21:50'),
 (263,'mako1','2009-04-26 14:26:00','2009-04-26 14:26:00'),
 (264,'hg@hg.com','2009-04-26 14:44:13','2009-04-26 14:44:13'),
 (265,'zhyang','2009-04-27 05:20:09','2009-04-27 05:20:09');
UNLOCK TABLES;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;


--
-- Definition of table `nova_core`.`vclusters`
--

DROP TABLE IF EXISTS `nova_core`.`vclusters`;
CREATE TABLE  `nova_core`.`vclusters` (
  `id` int(11) NOT NULL auto_increment,
  `user_id` int(11) default NULL,
  `vcluster_name` varchar(255) default '#unnamed#',
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=137 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `nova_core`.`vclusters`
--

/*!40000 ALTER TABLE `vclusters` DISABLE KEYS */;
LOCK TABLES `vclusters` WRITE;
INSERT INTO `nova_core`.`vclusters` VALUES  (1,NULL,'miko','2009-04-21 15:21:39','2009-04-22 06:29:43'),
 (2,NULL,'#unnamed#','2009-04-21 15:33:26','2009-04-22 06:29:21'),
 (3,NULL,'#unnamed#','2009-04-21 15:35:56','2009-04-21 15:35:56'),
 (4,NULL,'mina','2009-04-21 15:36:27','2009-04-21 15:36:27'),
 (5,NULL,'#unnamed#','2009-04-21 15:36:44','2009-04-21 15:36:44'),
 (6,NULL,'#unnamed#','2009-04-21 15:37:03','2009-04-21 15:37:03'),
 (7,NULL,'#unnamed#','2009-04-21 15:37:03','2009-04-21 15:37:03'),
 (8,NULL,'#unnamed#','2009-04-21 15:37:17','2009-04-21 15:37:17'),
 (9,NULL,'#unnamed#','2009-04-21 15:37:23','2009-04-21 15:37:23'),
 (10,NULL,'#unnamed#','2009-04-21 15:37:24','2009-04-21 15:37:24'),
 (11,NULL,'#unnamed#','2009-04-21 15:37:25','2009-04-21 15:37:25'),
 (12,NULL,'nana','2009-04-21 15:37:54','2009-04-21 15:37:54'),
 (13,NULL,'nana','2009-04-21 15:38:14','2009-04-21 15:38:14'),
 (14,NULL,'nana','2009-04-21 15:38:16','2009-04-21 15:38:16'),
 (15,NULL,'nana','2009-04-21 15:38:34','2009-04-21 15:38:34'),
 (16,NULL,'nana','2009-04-21 15:38:53','2009-04-21 15:38:53');
INSERT INTO `nova_core`.`vclusters` VALUES  (17,NULL,'nana','2009-04-21 15:38:53','2009-04-21 15:38:53'),
 (18,NULL,'nana','2009-04-21 15:39:02','2009-04-21 15:39:02'),
 (19,NULL,'nana','2009-04-21 15:39:13','2009-04-21 15:39:13'),
 (20,NULL,'nana','2009-04-21 15:39:19','2009-04-21 15:39:19'),
 (21,NULL,'nana','2009-04-21 15:39:21','2009-04-21 15:39:21'),
 (22,NULL,'nana','2009-04-21 15:39:22','2009-04-21 15:39:22'),
 (23,NULL,'moka','2009-04-21 15:48:05','2009-04-21 15:48:05'),
 (24,NULL,'moka','2009-04-21 15:48:06','2009-04-21 15:48:06'),
 (25,NULL,'moka','2009-04-21 15:48:27','2009-04-21 15:48:27'),
 (26,NULL,'#unnamed#','2009-04-21 15:48:54','2009-04-21 15:48:54'),
 (27,NULL,'moka','2009-04-21 15:49:07','2009-04-21 15:49:07'),
 (28,NULL,'nanamisamisa','2009-04-21 15:49:13','2009-04-21 15:49:13'),
 (29,NULL,'nanamisamisa','2009-04-21 15:50:19','2009-04-21 15:50:19'),
 (30,NULL,'nanamisamisa','2009-04-21 15:50:20','2009-04-21 15:50:20'),
 (31,NULL,'nana','2009-04-21 15:50:39','2009-04-21 15:50:39'),
 (32,NULL,'nana','2009-04-21 15:50:40','2009-04-21 15:50:40');
INSERT INTO `nova_core`.`vclusters` VALUES  (33,NULL,'nana','2009-04-21 15:50:52','2009-04-21 15:50:52'),
 (34,NULL,'nana','2009-04-21 15:50:52','2009-04-21 15:50:52'),
 (35,NULL,'misamisa','2009-04-21 15:52:40','2009-04-21 15:52:40'),
 (36,NULL,'misamisa','2009-04-21 15:52:45','2009-04-21 15:52:45'),
 (37,NULL,'misamisa','2009-04-21 15:52:49','2009-04-21 15:52:49'),
 (38,NULL,'nana','2009-04-21 15:52:56','2009-04-21 15:52:56'),
 (39,NULL,'nana','2009-04-21 15:52:57','2009-04-21 15:52:57'),
 (40,NULL,'nana','2009-04-21 15:52:58','2009-04-21 15:52:58'),
 (41,NULL,'nana','2009-04-21 15:52:59','2009-04-21 15:52:59'),
 (42,NULL,'nana','2009-04-21 15:53:02','2009-04-21 15:53:02'),
 (43,NULL,'nana','2009-04-21 15:53:07','2009-04-21 15:53:07'),
 (44,NULL,'nana','2009-04-21 15:53:08','2009-04-21 15:53:08'),
 (45,NULL,'nana','2009-04-21 15:53:08','2009-04-21 15:53:08'),
 (46,NULL,'nana','2009-04-21 15:53:08','2009-04-21 15:53:08'),
 (47,NULL,'nana','2009-04-21 15:53:08','2009-04-21 15:53:08'),
 (48,NULL,'nana','2009-04-21 15:53:09','2009-04-21 15:53:09');
INSERT INTO `nova_core`.`vclusters` VALUES  (49,NULL,'nana','2009-04-21 15:53:09','2009-04-21 15:53:09'),
 (50,NULL,'nana','2009-04-21 15:53:09','2009-04-21 15:53:09'),
 (51,NULL,'nana','2009-04-21 15:53:09','2009-04-21 15:53:09'),
 (52,NULL,'nana','2009-04-21 15:53:32','2009-04-21 15:53:32'),
 (53,NULL,'nana','2009-04-21 15:53:33','2009-04-21 15:53:33'),
 (54,NULL,'nana','2009-04-21 15:53:33','2009-04-21 15:53:33'),
 (55,NULL,'nana','2009-04-21 15:53:34','2009-04-21 15:53:34'),
 (56,NULL,'nana','2009-04-21 15:53:58','2009-04-21 15:53:58'),
 (57,NULL,'nana','2009-04-21 15:53:58','2009-04-21 15:53:58'),
 (58,NULL,'nana','2009-04-21 15:53:59','2009-04-21 15:53:59'),
 (59,NULL,'nana','2009-04-21 15:53:59','2009-04-21 15:53:59'),
 (60,NULL,'crappy-test','2009-04-21 15:54:10','2009-04-21 15:54:10'),
 (61,NULL,'crappy-test','2009-04-21 15:54:25','2009-04-21 15:54:25'),
 (62,NULL,'crappy-test','2009-04-21 15:54:26','2009-04-21 15:54:26'),
 (63,NULL,'crappy-test','2009-04-21 15:54:27','2009-04-21 15:54:27'),
 (64,NULL,'crappy-test','2009-04-21 15:54:28','2009-04-21 15:54:28');
INSERT INTO `nova_core`.`vclusters` VALUES  (65,NULL,'crappy-test','2009-04-21 15:54:29','2009-04-21 15:54:29'),
 (66,NULL,'crappy-test','2009-04-21 15:54:30','2009-04-21 15:54:30'),
 (67,NULL,'crappy-test','2009-04-21 15:54:31','2009-04-21 15:54:31'),
 (68,NULL,'crappy-test','2009-04-21 15:54:33','2009-04-21 15:54:33'),
 (69,NULL,'crappy-test','2009-04-21 15:54:35','2009-04-21 15:54:35'),
 (70,NULL,'crappytest','2009-04-21 15:55:29','2009-04-21 15:55:29'),
 (71,NULL,'nana','2009-04-21 15:55:36','2009-04-21 15:55:36'),
 (72,NULL,'crappytest','2009-04-21 16:15:07','2009-04-21 16:15:07'),
 (73,NULL,'test-TODO','2009-04-21 16:16:16','2009-04-21 16:16:16'),
 (74,NULL,'test-TODO','2009-04-21 16:16:17','2009-04-21 16:16:17'),
 (75,NULL,'test-TODO','2009-04-21 16:16:18','2009-04-21 16:16:18'),
 (76,NULL,'test-TODO','2009-04-21 16:16:18','2009-04-21 16:16:18'),
 (77,NULL,'test-TODO','2009-04-21 16:17:23','2009-04-21 16:17:23'),
 (78,NULL,'test-TODO','2009-04-21 16:17:24','2009-04-21 16:17:24'),
 (79,NULL,'test-TODO','2009-04-21 16:17:25','2009-04-21 16:17:25');
INSERT INTO `nova_core`.`vclusters` VALUES  (80,NULL,'test-TODO','2009-04-22 02:10:13','2009-04-22 02:10:13'),
 (81,NULL,'test-TODO','2009-04-22 02:10:14','2009-04-22 02:10:14'),
 (82,NULL,'test-TODO','2009-04-22 02:10:14','2009-04-22 02:10:14'),
 (83,NULL,'test-TODO','2009-04-22 14:27:47','2009-04-22 14:27:47'),
 (84,NULL,'test-TODO','2009-04-22 14:27:48','2009-04-22 14:27:48'),
 (85,NULL,'#unnamed#','2009-04-23 17:17:04','2009-04-23 17:17:04'),
 (86,NULL,'My_Cluster_9','2009-04-23 17:17:33','2009-04-23 17:17:33'),
 (87,NULL,'#unnamed#','2009-04-23 17:18:04','2009-04-23 17:18:04'),
 (88,NULL,'test','2009-04-24 07:53:17','2009-04-24 07:53:17'),
 (89,NULL,'My_Cluster_7','2009-04-24 11:56:06','2009-04-24 11:56:06'),
 (90,NULL,'My_Cluster_8','2009-04-25 16:10:37','2009-04-25 16:10:37'),
 (91,NULL,'My_Cluster_0','2009-04-26 14:52:35','2009-04-26 14:52:35'),
 (92,264,'My_Cluster_0','2009-04-26 15:05:12','2009-04-26 15:05:12'),
 (93,264,'My_Cluster_1','2009-04-26 15:05:15','2009-04-26 15:05:15'),
 (94,265,'My_Cluster_1','2009-04-27 05:33:40','2009-04-27 05:33:40');
INSERT INTO `nova_core`.`vclusters` VALUES  (95,265,'My_Cluster_2','2009-04-27 05:48:22','2009-04-27 05:48:22'),
 (96,NULL,'My_Cluster_1','2009-04-27 05:49:19','2009-04-27 05:49:19'),
 (97,NULL,'My_Cluster_2','2009-04-27 06:12:38','2009-04-27 06:12:38'),
 (98,NULL,'#unnamed#','2009-05-05 08:13:54','2009-05-05 08:13:54'),
 (99,NULL,'#unnamed#','2009-05-05 08:16:47','2009-05-05 08:16:47'),
 (100,NULL,'#unnamed#','2009-05-05 08:18:41','2009-05-05 08:18:41'),
 (101,NULL,'#unnamed#','2009-05-05 08:18:53','2009-05-05 08:18:53'),
 (102,NULL,'#unnamed#','2009-05-05 08:19:49','2009-05-05 08:19:49'),
 (103,NULL,'#unnamed#','2009-05-05 08:20:05','2009-05-05 08:20:05'),
 (104,NULL,'#unnamed#','2009-05-05 08:20:33','2009-05-05 08:20:33'),
 (105,1,'Hadoop_Cluster','2009-05-05 08:22:12','2009-05-05 08:22:12'),
 (106,NULL,'alice','2009-05-06 03:45:29','2009-05-06 03:45:29'),
 (107,NULL,'alice2','2009-05-06 03:46:10','2009-05-06 03:46:10'),
 (108,NULL,'alice3','2009-05-06 03:50:25','2009-05-06 03:50:25'),
 (109,NULL,'alice4','2009-05-06 03:51:18','2009-05-06 03:51:18');
INSERT INTO `nova_core`.`vclusters` VALUES  (110,NULL,'jonny','2009-05-06 03:53:27','2009-05-06 03:53:27'),
 (111,NULL,'jonny2','2009-05-06 03:54:00','2009-05-06 03:54:00'),
 (112,NULL,'jonny4','2009-05-06 03:54:22','2009-05-06 03:54:22'),
 (113,NULL,'jonnytheking','2009-05-06 03:55:15','2009-05-06 03:55:15'),
 (114,NULL,'jonnytheking','2009-05-06 03:57:36','2009-05-06 03:57:36'),
 (115,NULL,'jonnytheking','2009-05-06 03:59:10','2009-05-06 03:59:10'),
 (116,NULL,'jonnytheking','2009-05-06 03:59:17','2009-05-06 03:59:17'),
 (117,NULL,'jonnytheking','2009-05-06 03:59:49','2009-05-06 03:59:49'),
 (118,NULL,'jonnytheking','2009-05-06 03:59:50','2009-05-06 03:59:50'),
 (119,NULL,'bigc','2009-05-06 04:00:28','2009-05-06 04:00:28'),
 (120,NULL,'bigc','2009-05-06 04:00:31','2009-05-06 04:00:31'),
 (121,NULL,'bigc','2009-05-06 04:00:32','2009-05-06 04:00:32'),
 (122,NULL,'bigc','2009-05-06 04:00:33','2009-05-06 04:00:33'),
 (123,NULL,'bigc','2009-05-06 04:00:34','2009-05-06 04:00:34'),
 (124,NULL,'bigc','2009-05-06 04:00:34','2009-05-06 04:00:34'),
 (125,NULL,'bigc','2009-05-06 04:00:35','2009-05-06 04:00:35');
INSERT INTO `nova_core`.`vclusters` VALUES  (126,NULL,'bigc','2009-05-06 04:02:00','2009-05-06 04:02:00'),
 (127,NULL,'bigc','2009-05-06 04:02:26','2009-05-06 04:02:26'),
 (128,NULL,'bigc','2009-05-06 04:02:27','2009-05-06 04:02:27'),
 (129,NULL,'bigc','2009-05-06 04:02:27','2009-05-06 04:02:27'),
 (130,NULL,'bigc','2009-05-06 04:09:33','2009-05-06 04:09:33'),
 (131,NULL,'bigc1','2009-05-06 04:11:33','2009-05-06 04:11:33'),
 (132,NULL,'bigc1','2009-05-06 04:12:30','2009-05-06 04:12:30'),
 (133,NULL,'bigc1','2009-05-06 04:12:31','2009-05-06 04:12:31'),
 (134,NULL,'bigc1','2009-05-06 04:12:45','2009-05-06 04:12:45'),
 (135,NULL,'bigc1','2009-05-06 04:13:10','2009-05-06 04:13:10'),
 (136,NULL,'abccd','2009-05-06 04:16:35','2009-05-06 04:16:35');
UNLOCK TABLES;
/*!40000 ALTER TABLE `vclusters` ENABLE KEYS */;


--
-- Definition of table `nova_core`.`vimages`
--

DROP TABLE IF EXISTS `nova_core`.`vimages`;
CREATE TABLE  `nova_core`.`vimages` (
  `id` int(11) NOT NULL auto_increment,
  `iid` int(11) default NULL,
  `os_family` varchar(255) default NULL,
  `os_name` varchar(255) default NULL,
  `hidden` tinyint(1) default '0',
  `location` varchar(255) default NULL,
  `comment` varchar(255) default NULL,
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `nova_core`.`vimages`
--

/*!40000 ALTER TABLE `vimages` DISABLE KEYS */;
LOCK TABLES `vimages` WRITE;
INSERT INTO `nova_core`.`vimages` VALUES  (1,NULL,'Linux','Fedora 8',0,'ftp://localhost/f8.img','','2009-05-05 04:19:02','2009-05-05 04:19:02'),
 (2,NULL,'Linux','Ubuntu(Intrepid)',0,'ftp://localhost/intrepid2.img','','2009-05-05 04:19:53','2009-05-05 04:19:53'),
 (3,NULL,'Linux','Hadoop-Slave',0,'ftp://localhost/hadoop-slave.img','','2009-05-05 04:20:21','2009-05-05 04:20:21');
UNLOCK TABLES;
/*!40000 ALTER TABLE `vimages` ENABLE KEYS */;


--
-- Definition of table `nova_core`.`vmachines`
--

DROP TABLE IF EXISTS `nova_core`.`vmachines`;
CREATE TABLE  `nova_core`.`vmachines` (
  `id` int(11) NOT NULL auto_increment,
  `ip` varchar(255) default NULL,
  `pmachine_id` int(11) default NULL,
  `vcluster_id` int(11) default NULL,
  `vimage_id` int(11) default NULL,
  `pmon_vmachine_uuid` varchar(255) default NULL,
  `status` varchar(255) default 'not running',
  `settings` varchar(255) default NULL,
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=124 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `nova_core`.`vmachines`
--

/*!40000 ALTER TABLE `vmachines` DISABLE KEYS */;
LOCK TABLES `vmachines` WRITE;
INSERT INTO `nova_core`.`vmachines` VALUES  (1,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:29:50','2009-04-21 16:29:50'),
 (2,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:29:52','2009-04-23 06:16:40'),
 (3,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:29:52','2009-04-21 16:29:52'),
 (4,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:29:52','2009-04-23 06:45:13'),
 (5,NULL,NULL,64,NULL,NULL,'not running',NULL,'2009-04-21 16:30:34','2009-04-23 06:45:17'),
 (6,NULL,NULL,64,NULL,NULL,'not running',NULL,'2009-04-21 16:30:36','2009-04-23 06:49:23'),
 (7,NULL,NULL,64,NULL,NULL,'not running',NULL,'2009-04-21 16:31:44','2009-04-23 06:55:56'),
 (8,NULL,NULL,64,NULL,NULL,'not running',NULL,'2009-04-21 16:31:50','2009-04-21 16:31:50'),
 (9,NULL,NULL,64,NULL,NULL,'not running',NULL,'2009-04-21 16:31:51','2009-04-21 16:31:51'),
 (10,NULL,NULL,64,NULL,NULL,'not running',NULL,'2009-04-21 16:31:51','2009-04-21 16:31:51'),
 (11,NULL,NULL,64,NULL,NULL,'not running',NULL,'2009-04-21 16:31:51','2009-04-21 16:31:51'),
 (12,NULL,NULL,64,NULL,NULL,'not running',NULL,'2009-04-21 16:31:51','2009-04-21 16:31:51');
INSERT INTO `nova_core`.`vmachines` VALUES  (13,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:33:15','2009-04-23 06:39:47'),
 (14,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:33:44','2009-04-21 16:33:44'),
 (15,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:33:46','2009-04-21 16:33:46'),
 (16,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:33:47','2009-04-21 16:33:47'),
 (17,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:33:47','2009-04-21 16:33:47'),
 (18,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:33:50','2009-04-21 16:33:50'),
 (19,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:34:01','2009-04-21 16:34:01'),
 (20,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:34:02','2009-04-21 16:34:02'),
 (21,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:34:04','2009-04-21 16:34:04'),
 (22,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:34:06','2009-04-21 16:34:06'),
 (23,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:51:09','2009-04-21 16:51:09'),
 (24,NULL,NULL,79,NULL,NULL,'not running',NULL,'2009-04-21 16:51:14','2009-04-21 16:51:14');
INSERT INTO `nova_core`.`vmachines` VALUES  (25,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:51:14','2009-04-21 16:51:14'),
 (26,NULL,NULL,79,NULL,NULL,'not running',NULL,'2009-04-21 16:51:15','2009-04-21 16:51:15'),
 (27,NULL,NULL,79,NULL,NULL,'not running',NULL,'2009-04-21 16:51:16','2009-04-21 16:51:16'),
 (28,NULL,NULL,NULL,NULL,'','not running',NULL,'2009-04-21 16:51:19','2009-04-26 17:23:16'),
 (29,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:51:20','2009-04-23 06:16:58'),
 (30,'10.0.3.1',NULL,NULL,NULL,'','not running',NULL,'2009-04-21 16:51:21','2009-04-26 17:23:19'),
 (31,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:51:22','2009-04-23 16:26:22'),
 (32,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:51:24','2009-04-21 16:51:24'),
 (33,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:51:28','2009-04-21 16:51:28'),
 (34,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-21 16:51:30','2009-04-23 16:31:51'),
 (35,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 02:10:17','2009-04-23 06:16:06'),
 (39,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 05:47:33','2009-04-22 05:47:33');
INSERT INTO `nova_core`.`vmachines` VALUES  (40,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 05:47:33','2009-04-22 05:47:33'),
 (41,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 05:47:33','2009-04-22 05:47:33'),
 (42,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 06:12:16','2009-04-22 06:12:16'),
 (43,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 06:29:49','2009-04-22 06:29:49'),
 (44,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 14:27:50','2009-04-22 14:27:50'),
 (45,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 14:27:50','2009-04-22 14:27:50'),
 (46,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-22 14:27:50','2009-04-22 14:27:50'),
 (47,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-23 06:44:39','2009-04-23 06:44:39'),
 (48,NULL,1,NULL,NULL,NULL,'not running',NULL,'2009-04-24 05:16:50','2009-04-24 05:16:59'),
 (49,'10.0.3.1',NULL,NULL,NULL,'','not running',NULL,'2009-04-24 05:22:59','2009-04-26 17:25:00'),
 (50,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-24 05:26:36','2009-04-24 05:26:36'),
 (51,'10.0.3.1',NULL,NULL,NULL,'','not running',NULL,'2009-04-24 05:38:51','2009-04-26 17:25:04');
INSERT INTO `nova_core`.`vmachines` VALUES  (52,'10.0.3.1',NULL,NULL,NULL,'','not running',NULL,'2009-04-24 05:42:29','2009-04-26 17:25:06'),
 (53,'10.0.3.1',NULL,NULL,NULL,'','not running',NULL,'2009-04-24 06:10:22','2009-04-26 17:23:23'),
 (54,'10.0.3.1',NULL,NULL,NULL,'','not running',NULL,'2009-04-24 06:33:43','2009-04-24 06:34:50'),
 (55,NULL,1,NULL,NULL,NULL,'not running',NULL,'2009-04-24 07:53:20','2009-04-24 07:53:24'),
 (56,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-24 07:53:21','2009-04-24 07:53:21'),
 (57,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-24 07:53:22','2009-04-24 07:53:22'),
 (58,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-24 07:53:22','2009-04-24 07:53:23'),
 (59,NULL,1,NULL,NULL,NULL,'not running',NULL,'2009-04-24 08:57:27','2009-04-24 08:57:31'),
 (60,NULL,1,NULL,NULL,NULL,'not running',NULL,'2009-04-24 11:56:09','2009-04-24 11:56:14'),
 (61,'10.0.3.1',NULL,NULL,NULL,'','not running',NULL,'2009-04-24 11:57:42','2009-04-26 17:21:43'),
 (62,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-04-25 14:32:36','2009-04-25 14:32:36');
INSERT INTO `nova_core`.`vmachines` VALUES  (63,NULL,NULL,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\"}\n','2009-04-25 14:59:27','2009-04-25 14:59:27'),
 (64,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\"}\n','2009-04-25 15:05:09','2009-04-25 15:05:16'),
 (65,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\"}\n','2009-04-25 15:06:38','2009-04-25 15:06:45'),
 (66,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\"}\n','2009-04-25 15:08:38','2009-04-25 15:08:44'),
 (67,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"\",\"mac\":\"fuck\",\"vcpu\":1,\"mem\":512}','2009-04-25 15:09:23','2009-04-25 15:27:18'),
 (68,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:10:17','2009-04-25 15:10:21'),
 (69,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:12:00','2009-04-25 15:12:05'),
 (70,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:12:43','2009-04-25 15:12:48');
INSERT INTO `nova_core`.`vmachines` VALUES  (71,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:13:07','2009-04-25 15:13:12'),
 (72,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:14:01','2009-04-25 15:14:05'),
 (73,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:14:37','2009-04-25 15:14:41'),
 (74,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:15:47','2009-04-25 15:15:52'),
 (75,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:16:45','2009-04-25 15:16:58'),
 (76,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:18:42','2009-04-25 15:18:46'),
 (77,NULL,1,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:20:14','2009-04-25 15:20:20'),
 (78,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"234\"}','2009-04-25 15:48:45','2009-04-25 15:56:16');
INSERT INTO `nova_core`.`vmachines` VALUES  (79,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"322\"}','2009-04-25 15:50:12','2009-04-25 15:50:12'),
 (80,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"123\"}','2009-04-25 15:52:37','2009-04-25 15:52:52'),
 (81,NULL,NULL,NULL,NULL,NULL,'not running','{\"mem\":512, \"img\":\"\", \"vcpu\":1, \"mac\":\"\", \"ip\":\"\"}\n','2009-04-25 15:57:20','2009-04-25 15:57:20'),
 (82,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"234\"}','2009-04-25 15:57:32','2009-04-25 15:57:45'),
 (83,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"213\"}','2009-04-25 15:59:16','2009-04-25 15:59:22'),
 (84,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"111\"}','2009-04-25 16:00:12','2009-04-25 16:00:23'),
 (85,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"131\"}','2009-04-25 16:01:24','2009-04-25 16:02:16');
INSERT INTO `nova_core`.`vmachines` VALUES  (86,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"141\"}','2009-04-25 16:04:07','2009-04-26 17:22:19'),
 (87,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"222\"}','2009-04-25 16:08:21','2009-04-25 16:08:26'),
 (88,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"134\"}','2009-04-25 16:09:11','2009-04-25 16:09:24'),
 (89,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"144\"}','2009-04-25 16:10:47','2009-04-26 17:22:34'),
 (90,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"3\",\"mem\":\"192\"}','2009-04-25 16:13:52','2009-04-26 17:22:37'),
 (91,'10.0.3.1',1,92,NULL,'cef3842f-f4c8-4ddd-889e-fb0aabf7baac','running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"3\",\"mem\":\"123\"}','2009-04-26 15:05:29','2009-04-26 15:05:38'),
 (92,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"3\",\"mem\":\"123\"}','2009-04-26 16:30:49','2009-04-26 16:30:49');
INSERT INTO `nova_core`.`vmachines` VALUES  (93,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"\",\"mem\":\"\"}','2009-04-26 16:36:49','2009-04-26 16:36:49'),
 (94,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"\",\"mem\":\"\"}','2009-04-26 16:37:21','2009-04-26 16:37:21'),
 (95,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"\",\"mem\":\"\"}','2009-04-26 16:40:06','2009-04-26 16:40:06'),
 (96,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-04-26 17:04:06','2009-04-26 17:04:06'),
 (97,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"233\"}','2009-04-26 17:05:31','2009-04-26 17:05:31'),
 (98,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 03:57:22','2009-05-05 04:58:34'),
 (99,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 04:05:53','2009-05-05 04:58:44');
INSERT INTO `nova_core`.`vmachines` VALUES  (100,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"256\"}','2009-04-27 04:10:31','2009-04-27 04:12:46'),
 (101,NULL,2,NULL,NULL,NULL,'not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 04:26:24','2009-04-27 04:26:32'),
 (102,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 05:34:54','2009-04-27 05:38:21'),
 (103,'10.0.3.1',2,94,NULL,'464e53a5-6da5-4af4-922f-99e2fdaaa8e8','running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 05:42:04','2009-04-27 05:47:24'),
 (104,NULL,NULL,94,NULL,NULL,'not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 05:48:31','2009-04-27 05:48:31'),
 (105,NULL,NULL,94,NULL,NULL,'not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 05:48:41','2009-04-27 05:48:41'),
 (106,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"hadoop-slave.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 05:49:24','2009-04-29 09:23:57');
INSERT INTO `nova_core`.`vmachines` VALUES  (107,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-27 05:53:41','2009-04-29 09:23:53'),
 (108,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512000\"}','2009-04-27 05:54:42','2009-04-29 09:23:50'),
 (109,NULL,2,NULL,NULL,NULL,'not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"512000\"}','2009-04-27 05:55:40','2009-04-27 05:55:43'),
 (110,NULL,2,NULL,NULL,NULL,'not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"512000\"}','2009-04-27 06:04:12','2009-04-27 06:04:14'),
 (111,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"intrepid2.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"1\",\"mem\":\"512000\"}','2009-04-27 06:12:59','2009-04-29 09:23:29'),
 (112,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"f8.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-04-29 09:10:57','2009-04-29 09:10:58'),
 (113,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"f8.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512000\"}','2009-04-29 09:11:06','2009-05-05 04:58:12');
INSERT INTO `nova_core`.`vmachines` VALUES  (114,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:20:06','2009-05-05 08:20:07'),
 (115,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:20:07','2009-05-05 08:20:07'),
 (116,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:20:08','2009-05-05 08:20:08'),
 (117,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:20:08','2009-05-05 08:20:08'),
 (118,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:20:33','2009-05-05 08:20:33'),
 (119,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:20:33','2009-05-05 08:20:33'),
 (120,NULL,NULL,105,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:22:13','2009-05-05 08:22:13');
INSERT INTO `nova_core`.`vmachines` VALUES  (121,NULL,NULL,105,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:22:13','2009-05-05 08:22:13'),
 (122,NULL,NULL,105,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:22:13','2009-05-05 08:22:13'),
 (123,NULL,NULL,105,NULL,NULL,'not running','{\"img\":\"hadoop-a.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-05 08:22:13','2009-05-05 08:22:13');
UNLOCK TABLES;
/*!40000 ALTER TABLE `vmachines` ENABLE KEYS */;

--
-- Create schema nova_front
--

CREATE DATABASE IF NOT EXISTS nova_front;
USE nova_front;

--
-- Definition of table `nova_front`.`currentlyloggedin`
--

DROP TABLE IF EXISTS `nova_front`.`currentlyloggedin`;
CREATE TABLE  `nova_front`.`currentlyloggedin` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `session_id` varchar(45) NOT NULL,
  `member_id` varchar(45) NOT NULL,
  `group_id` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`currentlyloggedin`
--

/*!40000 ALTER TABLE `currentlyloggedin` DISABLE KEYS */;
LOCK TABLES `currentlyloggedin` WRITE;
INSERT INTO `nova_front`.`currentlyloggedin` VALUES  (10,'6d51c3c6ec21dc91c5b488900938ef94','232','10'),
 (31,'37a2e6dd5621b36ab9c5d0ef5acadf4d','200','1000'),
 (84,'40d7d7f55a4dc8bb5aa758c2b140e938','259','10000'),
 (85,'8b526d9fab2319c82e216720c645002e','259','10000'),
 (90,'9bad84792d31930774f27d98df9a7b9c','259','10000'),
 (95,'40167837611678f4efba733a6cd59703','259','10000'),
 (96,'0a42b3da6801c6423c7c24b36a92298c','259','10000'),
 (97,'eec1dec8b8443c2328fe33b0ddafe003','259','10000'),
 (98,'830ccd21bea9ade7ddcb4c76b000ba1e','259','10000'),
 (99,'50876f9406168dc1127fd27a09e7a304','259','10000'),
 (100,'ce30f2d718ff254f2a5ca31f79b61d70','259','10000'),
 (101,'f06b3eefc00bbc163df86a0ed36eba79','259','10000'),
 (102,'d1b930747bc0645e251bcba1f511aad7','259','10000'),
 (103,'5b803c4de7388eb3933e07e4ddc61092','259','10000');
UNLOCK TABLES;
/*!40000 ALTER TABLE `currentlyloggedin` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_dependencies`
--

DROP TABLE IF EXISTS `nova_front`.`qo_dependencies`;
CREATE TABLE  `nova_front`.`qo_dependencies` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `directory` varchar(255) default '' COMMENT 'The directory within the modules directory stated in the system/os/config.php',
  `file` varchar(255) default NULL COMMENT 'The file that contains the dependency',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=109 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_dependencies`
--

/*!40000 ALTER TABLE `qo_dependencies` DISABLE KEYS */;
LOCK TABLES `qo_dependencies` WRITE;
INSERT INTO `nova_front`.`qo_dependencies` VALUES  (100,'templateModule/','Ext.ux.AboutWindow.js'),
 (101,'vc-dependency/grid/filter/','Filter.js'),
 (102,'vc-dependency/grid/filter/','BooleanFilter.js'),
 (103,'vc-dependency/grid/filter/','ListFilter.js'),
 (104,'vc-dependency/grid/filter/','DateFilter.js'),
 (108,'vc-dependency/grid/','GridFilters.js'),
 (106,'vc-dependency/grid/filter/','NumericFilter.js'),
 (107,'vc-dependency/grid/filter/','StringFilter.js');
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_dependencies` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_domains`
--

DROP TABLE IF EXISTS `nova_front`.`qo_domains`;
CREATE TABLE  `nova_front`.`qo_domains` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `is_singular` tinyint(1) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_domains`
--

/*!40000 ALTER TABLE `qo_domains` DISABLE KEYS */;
LOCK TABLES `qo_domains` WRITE;
INSERT INTO `nova_front`.`qo_domains` VALUES  (1,'All Modules','All the modules',0),
 (2,'QoPreferences','The QoPreferences module',1),
 (9,'superadminModules',NULL,0),
 (10,'adminModules','Modules for vc admin',0),
 (11,'userModules','Modules for vc users',0),
 (200,'TemplateModule','Basic Module template.',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_domains` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_domains_has_modules`
--

DROP TABLE IF EXISTS `nova_front`.`qo_domains_has_modules`;
CREATE TABLE  `nova_front`.`qo_domains_has_modules` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_domains_id` int(11) unsigned default NULL,
  `qo_modules_id` int(11) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_domains_has_modules`
--

/*!40000 ALTER TABLE `qo_domains_has_modules` DISABLE KEYS */;
LOCK TABLES `qo_domains_has_modules` WRITE;
INSERT INTO `nova_front`.`qo_domains_has_modules` VALUES  (1,1,1),
 (7,2,1),
 (8,1,90),
 (9,200,90),
 (22,1,100),
 (23,1,101),
 (24,1,102),
 (25,1,103),
 (26,10,102),
 (27,11,103),
 (29,9,100),
 (30,9,101),
 (31,9,1),
 (32,9,104),
 (33,10,104),
 (34,11,104),
 (35,1,104),
 (36,11,105),
 (37,1,105),
 (38,2,104),
 (39,1,106),
 (40,11,106);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_domains_has_modules` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_error_log`
--

DROP TABLE IF EXISTS `nova_front`.`qo_error_log`;
CREATE TABLE  `nova_front`.`qo_error_log` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `text` text,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_error_log`
--

/*!40000 ALTER TABLE `qo_error_log` DISABLE KEYS */;
LOCK TABLES `qo_error_log` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_error_log` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_groups`
--

DROP TABLE IF EXISTS `nova_front`.`qo_groups`;
CREATE TABLE  `nova_front`.`qo_groups` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `importance` int(3) unsigned default '1',
  `active` tinyint(1) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=10001 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_groups`
--

/*!40000 ALTER TABLE `qo_groups` DISABLE KEYS */;
LOCK TABLES `qo_groups` WRITE;
INSERT INTO `nova_front`.`qo_groups` VALUES  (1,'administrator','System administrator',50,1),
 (10000,'user','General user',20,1),
 (10,'debug',NULL,1000,1),
 (1000,'super_admin','The almighty',100,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_groups` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_groups_has_domain_privileges`
--

DROP TABLE IF EXISTS `nova_front`.`qo_groups_has_domain_privileges`;
CREATE TABLE  `nova_front`.`qo_groups_has_domain_privileges` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_groups_id` int(11) unsigned default '0',
  `qo_domains_id` int(11) unsigned default '0',
  `qo_privileges_id` int(11) unsigned default '0',
  `is_allowed` tinyint(1) unsigned default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=224 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_groups_has_domain_privileges`
--

/*!40000 ALTER TABLE `qo_groups_has_domain_privileges` DISABLE KEYS */;
LOCK TABLES `qo_groups_has_domain_privileges` WRITE;
INSERT INTO `nova_front`.`qo_groups_has_domain_privileges` VALUES  (102,1000,2,2,1),
 (103,1000,200,3,1),
 (203,10000,2,2,1),
 (206,1,2,2,1),
 (208,1,10,10,1),
 (211,10000,2,1,1),
 (212,10000,11,11,1),
 (213,10000,11,1,1),
 (215,1,2,1,1),
 (216,1,10,1,1),
 (220,1000,9,9,1),
 (221,1000,9,1,1),
 (222,10,1,1,1),
 (223,10,2,2,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_groups_has_domain_privileges` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_groups_has_members`
--

DROP TABLE IF EXISTS `nova_front`.`qo_groups_has_members`;
CREATE TABLE  `nova_front`.`qo_groups_has_members` (
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member currently active in this group',
  `admin` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member the administrator of this group',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_groups_has_members`
--

/*!40000 ALTER TABLE `qo_groups_has_members` DISABLE KEYS */;
LOCK TABLES `qo_groups_has_members` WRITE;
INSERT INTO `nova_front`.`qo_groups_has_members` VALUES  (1,231,1,1),
 (10000,230,1,0),
 (1000,200,1,1),
 (10,232,1,1),
 (2,233,1,0),
 (2,234,1,0),
 (1000,235,1,0),
 (10000,236,1,0),
 (10000,237,1,0),
 (10000,238,1,0),
 (10000,239,1,0),
 (10000,240,1,0),
 (10000,241,1,0),
 (10000,242,1,0),
 (10000,243,1,0),
 (10000,244,1,0),
 (10000,245,1,0),
 (10000,246,1,0),
 (10000,247,1,0),
 (10000,248,1,0),
 (10000,249,1,0),
 (10000,250,1,0),
 (10000,251,1,0),
 (10000,252,1,0),
 (10000,253,1,0),
 (10000,254,1,0),
 (10000,255,1,0),
 (10000,256,1,0),
 (10000,257,1,0),
 (10000,258,1,0),
 (10000,259,1,0),
 (10000,260,1,0),
 (10000,261,1,0),
 (10000,262,1,0),
 (10000,263,1,0),
 (10000,264,1,0),
 (10000,265,1,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_groups_has_members` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_launchers`
--

DROP TABLE IF EXISTS `nova_front`.`qo_launchers`;
CREATE TABLE  `nova_front`.`qo_launchers` (
  `id` int(2) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_launchers`
--

/*!40000 ALTER TABLE `qo_launchers` DISABLE KEYS */;
LOCK TABLES `qo_launchers` WRITE;
INSERT INTO `nova_front`.`qo_launchers` VALUES  (1,'autorun'),
 (2,'contextmenu'),
 (3,'quickstart'),
 (4,'shortcut');
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_launchers` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_members`
--

DROP TABLE IF EXISTS `nova_front`.`qo_members`;
CREATE TABLE  `nova_front`.`qo_members` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `first_name` varchar(25) default NULL,
  `last_name` varchar(35) default NULL,
  `email_address` varchar(55) default NULL,
  `password` varchar(15) default NULL,
  `language` varchar(5) default 'en',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member currently active',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=266 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_members`
--

/*!40000 ALTER TABLE `qo_members` DISABLE KEYS */;
LOCK TABLES `qo_members` WRITE;
INSERT INTO `nova_front`.`qo_members` VALUES  (200,'Santa','Zhang','santa','zhang','en',1),
 (232,'debug','debug','debug','debug','en',1),
 (231,'admin','admin','admin','admin','en',1),
 (259,'misamisa','misamisa','misamisa','misamisa','en',1),
 (265,'Zhang','Yang','zhyang','zhyang','en',1),
 (262,'madao','madao','madao','madao','en',1),
 (263,'mako1','mako1','mako1','mako1','en',1),
 (264,'Huang','Gang','hg@hg.com','hghghg','en',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_members` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_members_has_module_launchers`
--

DROP TABLE IF EXISTS `nova_front`.`qo_members_has_module_launchers`;
CREATE TABLE  `nova_front`.`qo_members_has_module_launchers` (
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `qo_launchers_id` int(10) unsigned NOT NULL default '0',
  `sort_order` int(5) unsigned NOT NULL default '0' COMMENT 'sort within each launcher',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`,`qo_modules_id`,`qo_launchers_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_members_has_module_launchers`
--

/*!40000 ALTER TABLE `qo_members_has_module_launchers` DISABLE KEYS */;
LOCK TABLES `qo_members_has_module_launchers` WRITE;
INSERT INTO `nova_front`.`qo_members_has_module_launchers` VALUES  (230,10000,105,4,3),
 (230,10000,104,4,4),
 (230,10000,1,4,6),
 (231,1,1,4,3),
 (231,1,102,4,1),
 (231,1,104,4,0),
 (230,10000,2,4,2),
 (230,10000,103,4,5),
 (230,10000,8,4,1),
 (200,1000,8,4,1),
 (200,1000,4,4,0),
 (200,1000,2,4,2),
 (231,1,3,4,2),
 (200,1000,104,4,6),
 (231,1,5,4,4),
 (200,1000,3,4,4),
 (200,1000,100,4,7),
 (200,1000,5,4,3),
 (200,1000,5,3,0),
 (200,1000,90,4,5),
 (230,10000,4,4,0),
 (232,10,8,4,10),
 (230,10000,1,3,0),
 (232,10,104,4,1),
 (232,10,103,4,8),
 (232,10,2,4,12),
 (232,10,101,4,6),
 (232,10,100,4,5),
 (232,10,90,4,4),
 (232,10,1,4,3),
 (232,10,5,4,2),
 (232,10,3,4,0),
 (232,10,102,4,7),
 (232,10,105,4,9),
 (232,10,4,4,11),
 (236,10000,4,4,0),
 (236,10000,8,4,1),
 (236,10000,105,4,2),
 (236,10000,2,4,3),
 (236,10000,104,4,4),
 (236,10000,1,4,5),
 (236,10000,103,4,6),
 (237,10000,105,4,0),
 (242,10000,105,4,0),
 (243,10000,105,4,0),
 (244,10000,105,4,0),
 (245,10000,105,4,0),
 (246,10000,105,4,0),
 (247,10000,105,4,0);
INSERT INTO `nova_front`.`qo_members_has_module_launchers` VALUES  (248,10000,105,4,0),
 (249,10000,105,4,0),
 (250,10000,105,4,0),
 (251,10000,105,4,0),
 (252,10000,105,4,0),
 (253,10000,105,4,0),
 (254,10000,105,4,0),
 (255,10000,105,4,0),
 (256,10000,105,4,0),
 (257,10000,105,4,0),
 (258,10000,105,4,0),
 (259,10000,106,4,1),
 (260,10000,105,4,0),
 (259,10000,105,3,1),
 (265,10000,105,4,0),
 (259,10000,2,3,0),
 (261,10000,105,4,0),
 (232,10,106,4,13),
 (259,10000,105,4,0),
 (262,10000,106,4,0),
 (262,10000,105,4,1),
 (263,10000,105,4,0),
 (263,10000,106,4,0),
 (263,10000,104,4,0),
 (263,10000,103,4,0),
 (263,10000,1,4,0),
 (264,10000,105,4,0),
 (264,10000,106,4,0),
 (264,10000,104,4,0),
 (264,10000,103,4,0),
 (264,10000,1,4,0),
 (200,1000,1,4,8),
 (200,1000,101,4,9),
 (259,10000,104,4,2),
 (259,10000,1,4,3),
 (259,10000,103,4,4),
 (259,10000,106,3,2),
 (265,10000,106,4,0),
 (265,10000,104,4,0),
 (265,10000,103,4,0),
 (265,10000,1,4,0);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_members_has_module_launchers` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_modules`
--

DROP TABLE IF EXISTS `nova_front`.`qo_modules`;
CREATE TABLE  `nova_front`.`qo_modules` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `author` varchar(35) default NULL,
  `version` varchar(15) default NULL,
  `url` varchar(255) default NULL COMMENT 'Url which provides information',
  `description` text,
  `module_type` varchar(35) default NULL COMMENT 'The ''moduleType'' property of the client module',
  `module_id` varchar(35) default NULL COMMENT 'The ''moduleId'' property of the client module',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the module currently active',
  `load_on_demand` tinyint(1) unsigned NOT NULL default '1' COMMENT 'Preload this module at start up?',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=107 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_modules`
--

/*!40000 ALTER TABLE `qo_modules` DISABLE KEYS */;
LOCK TABLES `qo_modules` WRITE;
INSERT INTO `nova_front`.`qo_modules` VALUES  (1,'Todd Murdock','1.0','http://www.qwikioffice.com','A system application.  Allows users to set, and save their desktop preferences to the database.','system/preferences','qo-preferences',1,1),
 (2,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of window with grid.','demo','demo-grid',1,1),
 (3,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of window with tabs.','demo','demo-tabs',1,1),
 (4,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of window with accordion.','demo','demo-acc',1,1),
 (5,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of window with layout.','demo','demo-layout',1,1),
 (8,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of bogus window.','demo','demo-bogus',1,1),
 (90,'templateModule Author','0.0.1','http://www.qwikioffice.com','Basic Module Template.','templateModule','templateModule',1,1),
 (100,'Santa Zhang','0.0.1',NULL,NULL,'superadmin-user-manager','superadmin-user-manager',1,1),
 (101,'Santa Zhang','0.0.1',NULL,NULL,'superadmin-manual','superadmin-manual',1,1);
INSERT INTO `nova_front`.`qo_modules` VALUES  (102,'Santa Zhang',NULL,NULL,NULL,'admin-manual','admin-manual',1,1),
 (103,'Santa Zhang',NULL,NULL,NULL,'user-manual','user-manual',1,1),
 (104,'Santa Zhang',NULL,NULL,NULL,'account-setting','account-setting',1,1),
 (105,'Santa Zhang',NULL,NULL,NULL,'user-job-manager','user-job-manager',1,1),
 (106,'Santa Zhang','0.0.0',NULL,NULL,'hadoop-wizard','hadoop-wizard',1,1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_modules` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_modules_actions`
--

DROP TABLE IF EXISTS `nova_front`.`qo_modules_actions`;
CREATE TABLE  `nova_front`.`qo_modules_actions` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_modules_id` int(11) unsigned default NULL,
  `name` varchar(35) default NULL,
  `description` text,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_modules_actions`
--

/*!40000 ALTER TABLE `qo_modules_actions` DISABLE KEYS */;
LOCK TABLES `qo_modules_actions` WRITE;
INSERT INTO `nova_front`.`qo_modules_actions` VALUES  (1,0,'loadModule','Allow the user to load the module.  Give them access to it.  Does not belong to any particular module'),
 (2,1,'saveAppearance','Save appearance'),
 (3,1,'saveAutorun','Save autorun'),
 (4,1,'saveBackground','Save background'),
 (5,1,'saveQuickstart','Save quickstart'),
 (6,1,'saveShortcut','Save shortcut'),
 (7,1,'viewThemes','View themes'),
 (8,1,'viewWallpapers','View wallpapers'),
 (90,104,'doAccount',NULL),
 (92,90,'doTask','Get or Save data, depending on what is sent thru \"task\" request.'),
 (93,100,'viewUserInfo',NULL),
 (94,100,'toggleActive',NULL),
 (95,100,'kickAss',NULL),
 (96,104,'viewAccount',NULL),
 (97,104,'updateAccount',NULL),
 (98,105,'dummyTest','For dummy test purpose'),
 (99,105,'infoVM',NULL),
 (100,105,'resumeVM',NULL),
 (101,105,'pauseVM',NULL),
 (102,105,'listVM',NULL),
 (103,105,'stopVM',NULL),
 (104,105,'startVM',NULL),
 (105,105,'removeVM',NULL),
 (106,105,'newVM',NULL),
 (107,105,'removeCluster',NULL),
 (108,105,'addCluster',NULL);
INSERT INTO `nova_front`.`qo_modules_actions` VALUES  (109,105,'listCluster',NULL),
 (110,106,'progress','Hadoop installing progress'),
 (111,106,'create','Create new hadoop cluster'),
 (112,105,'listImage',NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_modules_actions` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_modules_files`
--

DROP TABLE IF EXISTS `nova_front`.`qo_modules_files`;
CREATE TABLE  `nova_front`.`qo_modules_files` (
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `directory` varchar(255) default '' COMMENT 'The directory within the modules directory stated in the system/os/config.php',
  `file` varchar(255) NOT NULL default '' COMMENT 'The file that contains the dependency',
  `is_stylesheet` tinyint(1) unsigned default '0',
  `is_server_module` tinyint(1) unsigned default '0',
  `is_client_module` tinyint(1) unsigned default '0',
  `class_name` varchar(55) default '',
  PRIMARY KEY  (`qo_modules_id`,`file`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_modules_files`
--

/*!40000 ALTER TABLE `qo_modules_files` DISABLE KEYS */;
LOCK TABLES `qo_modules_files` WRITE;
INSERT INTO `nova_front`.`qo_modules_files` VALUES  (1,'qo-preferences/','qo-preferences-override.js',0,0,0,''),
 (4,'acc-win/','acc-win-override.js',0,0,0,''),
 (5,'layout-win/','layout-win-override.js',0,0,0,''),
 (8,'bogus/bogus-win/','bogus-win-override.js',0,0,0,''),
 (2,'grid-win/','grid-win-override.js',0,0,0,''),
 (3,'tab-win/','tab-win-override.js',0,0,0,''),
 (1,'qo-preferences/','qo-preferences.js',0,0,1,'QoDesk.QoPreferences'),
 (1,'qo-preferences/','qo-preferences.php',0,1,0,'QoPreferences'),
 (2,'grid-win/','grid-win.js',0,0,1,'QoDesk.GridWindow'),
 (3,'tab-win/','tab-win.js',0,0,1,'QoDesk.TabWindow'),
 (4,'acc-win/','acc-win.js',0,0,1,'QoDesk.AccordionWindow'),
 (5,'layout-win/','layout-win.js',0,0,1,'QoDesk.LayoutWindow'),
 (8,'bogus/bogus-win/','bogus-win.js',0,0,1,'QoDesk.BogusWindow'),
 (1,'qo-preferences/','qo-preferences.css',1,0,0,''),
 (2,'grid-win/','grid-win.css',1,0,0,''),
 (3,'tab-win/','tab-win.css',1,0,0,''),
 (4,'acc-win/','acc-win.css',1,0,0,''),
 (5,'layout-win/','layout-win.css',1,0,0,''),
 (8,'bogus/bogus-win/','bogus-win.css',1,0,0,'');
INSERT INTO `nova_front`.`qo_modules_files` VALUES  (100,'superadmin-user-manager/','superadmin-user-manager.js',0,0,1,'QoDesk.SuperAdminManagerWindow'),
 (100,'superadmin-user-manager/','superadmin-user-manager-override.js',0,0,0,''),
 (90,'templateModule/','templateModule-override.js',0,0,0,''),
 (90,'templateModule/','templateModule.js',0,0,1,'QoDesk.TemplateModule'),
 (90,'templateModule/','templateModule.php',0,1,0,'TemplateModule'),
 (90,'templateModule/','templateModule.css',1,0,0,''),
 (100,'superadmin-user-manager/','superadmin-user-manager.css',1,0,0,''),
 (101,'superadmin-manual/','superadmin-manual.js',0,0,1,'QoDesk.SuperAdminManualWindow'),
 (101,'superadmin-manual/','superadmin-manual-override.js',0,0,0,''),
 (101,'superadmin-manual/','superadmin-manual.css',1,0,0,''),
 (102,'admin-manual/','admin-manual.js',0,0,1,'QoDesk.AdminManualWindow'),
 (102,'admin-manual/','admin-manual-override.js',0,0,0,''),
 (102,'admin-manual/','admin-manual.css',1,0,0,''),
 (103,'user-manual/','user-manual.js',0,0,1,'QoDesk.UserManualWindow'),
 (103,'user-manual/','user-manual-override.js',0,0,0,'');
INSERT INTO `nova_front`.`qo_modules_files` VALUES  (103,'user-manual/','user-manual.css',1,0,0,''),
 (104,'account-setting/','account-setting.js',0,0,1,'QoDesk.AccountSetting'),
 (104,'account-setting/','account-setting.php',0,1,0,'AccountSetting'),
 (104,'account-setting/','account-setting.css',1,0,0,''),
 (104,'account-setting/','account-setting-override.js',0,0,0,''),
 (105,'user-job-manager/','user-job-manager.js',0,0,1,'QoDesk.UserJobManager'),
 (105,'user-job-manager/','user-job-manager.css',1,0,0,''),
 (105,'user-job-manager/','user-job-manager-override.js',0,0,0,''),
 (100,'superadmin-user-manager/','superadmin-user-manager.php',0,1,0,'SuperAdminUserManager'),
 (106,'hadoop-wizard/','hadoop-wizard-override.js',0,0,0,''),
 (105,'user-job-manager/','user-job-manager.php',0,1,0,'UserJobManager'),
 (106,'hadoop-wizard/','hadoop-wizard.js',0,0,1,'QoDesk.HadoopWizard'),
 (106,'hadoop-wizard/','hadoop-wizard.php',0,1,0,'HadoopWizard'),
 (106,'hadoop-wizard/','hadoop-wizard.css',1,0,0,'');
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_modules_files` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_modules_has_dependencies`
--

DROP TABLE IF EXISTS `nova_front`.`qo_modules_has_dependencies`;
CREATE TABLE  `nova_front`.`qo_modules_has_dependencies` (
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `qo_dependencies_id` int(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (`qo_modules_id`,`qo_dependencies_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_modules_has_dependencies`
--

/*!40000 ALTER TABLE `qo_modules_has_dependencies` DISABLE KEYS */;
LOCK TABLES `qo_modules_has_dependencies` WRITE;
INSERT INTO `nova_front`.`qo_modules_has_dependencies` VALUES  (90,100),
 (100,101),
 (100,103),
 (100,108);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_modules_has_dependencies` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_privileges`
--

DROP TABLE IF EXISTS `nova_front`.`qo_privileges`;
CREATE TABLE  `nova_front`.`qo_privileges` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `is_singular` tinyint(1) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_privileges`
--

/*!40000 ALTER TABLE `qo_privileges` DISABLE KEYS */;
LOCK TABLES `qo_privileges` WRITE;
INSERT INTO `nova_front`.`qo_privileges` VALUES  (1,'Load Module','Allows the user access to the loadModule action',0),
 (2,'QoPreferences','Allows the user access to all the actions of the QoPreferences mdoule',1),
 (9,'superadminPrivilege',NULL,0),
 (10,'adminPrivilege',NULL,0),
 (11,'userPrivilege',NULL,0),
 (90,'accountPrivilege',NULL,0),
 (91,'TemplateModule','Allows the user access to the doTask action.',1);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_privileges` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_privileges_has_module_actions`
--

DROP TABLE IF EXISTS `nova_front`.`qo_privileges_has_module_actions`;
CREATE TABLE  `nova_front`.`qo_privileges_has_module_actions` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_privileges_id` int(11) unsigned default NULL,
  `qo_modules_actions_id` int(11) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_privileges_has_module_actions`
--

/*!40000 ALTER TABLE `qo_privileges_has_module_actions` DISABLE KEYS */;
LOCK TABLES `qo_privileges_has_module_actions` WRITE;
INSERT INTO `nova_front`.`qo_privileges_has_module_actions` VALUES  (1,1,1),
 (2,2,2),
 (3,2,3),
 (4,2,4),
 (5,2,5),
 (6,2,6),
 (7,2,7),
 (8,2,8),
 (9,91,92),
 (10,90,90),
 (11,9,93),
 (12,9,94),
 (13,9,95),
 (14,2,96),
 (15,2,97),
 (16,1,98),
 (17,1,109),
 (18,1,108),
 (19,1,107),
 (20,1,106),
 (21,1,105),
 (22,1,104),
 (23,1,103),
 (24,1,102),
 (25,1,101),
 (26,1,100),
 (27,1,99),
 (28,1,106),
 (29,1,110),
 (30,1,111),
 (31,1,112);
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_privileges_has_module_actions` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_sessions`
--

DROP TABLE IF EXISTS `nova_front`.`qo_sessions`;
CREATE TABLE  `nova_front`.`qo_sessions` (
  `id` varchar(128) NOT NULL default '' COMMENT 'a randomly generated id',
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned default NULL COMMENT 'Group the member signed in under',
  `ip` varchar(16) default NULL,
  `date` datetime default NULL,
  PRIMARY KEY  (`id`,`qo_members_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_sessions`
--

/*!40000 ALTER TABLE `qo_sessions` DISABLE KEYS */;
LOCK TABLES `qo_sessions` WRITE;
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('6d51c3c6ec21dc91c5b488900938ef94',232,10,'10.0.0.216','2009-04-17 17:14:27'),
 ('c031452b5b691fb98d843a67558c11f8',259,10000,'10.0.0.216','2009-04-20 10:48:28'),
 ('e972aabd58799167c8bf68fdc5c92005',259,10000,'10.0.0.216','2009-04-19 23:59:41'),
 ('4bda8101595fbd945f9a56ab80ee7b78',259,10000,'10.0.0.216','2009-04-20 10:42:46'),
 ('76e5e0297238fa608f759fa5f178f3a4',259,10000,'10.0.0.216','2009-04-20 00:00:06'),
 ('37a2e6dd5621b36ab9c5d0ef5acadf4d',200,1000,'10.0.0.216','2009-04-20 00:01:37'),
 ('a18d7bf915e4a8223c228855b3cc3713',259,10000,'10.0.0.216','2009-04-20 00:01:19'),
 ('5663ef3d793ffc0e9b45455b593a71c7',259,10000,'10.0.0.216','2009-04-20 21:42:50'),
 ('6168a84d755c16fb08a63103603b877a',259,10000,'10.0.0.216','2009-04-20 11:01:01'),
 ('f7bf945f33941ee631ac8fad0e5c0fcf',259,10000,'10.0.0.216','2009-04-20 12:25:45'),
 ('93551303bd1c180721b146f1746d8c3e',259,10000,'10.0.0.216','2009-04-22 13:47:06'),
 ('269c1712289a3692e922b922c327a179',259,10000,'10.0.0.216','2009-04-24 17:59:48'),
 ('a06ca950014372a6fc55132ec091b9cc',259,10000,'10.0.0.192','2009-04-21 15:51:48');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('0e6c767c48ccb44479ea55854d262a52',259,10000,'10.0.0.216','2009-04-22 22:27:41'),
 ('b41e26747c278f5e3faf383b0a8b6589',259,10000,'10.0.0.216','2009-04-23 14:01:37'),
 ('0556a4b8a4446502f57ecad59a3f4ee4',259,10000,'10.0.0.216','2009-04-23 14:42:52'),
 ('046c6b7b154a0bb4425497818b3d87fe',259,10000,'10.0.0.196','2009-04-24 00:23:27'),
 ('206559df7ca0a145672dd6897a4d2d83',259,10000,'10.0.0.216','2009-04-24 13:16:45'),
 ('1c2633256100a171c0ea1acdc7158c1e',259,10000,'10.0.0.216','2009-04-24 14:13:00'),
 ('288b497defa4669352de43ca91752bf4',259,10000,'10.0.0.216','2009-04-24 14:33:36'),
 ('40d7d7f55a4dc8bb5aa758c2b140e938',259,10000,'10.0.0.216','2009-04-26 23:07:07'),
 ('28230e5ff2fa90b7dda22dbf1b40b598',259,10000,'10.0.0.216','2009-04-24 19:24:46'),
 ('33b53394cbc0cfba6c0ed0cc8422432e',259,10000,'10.0.0.216','2009-04-24 19:47:53'),
 ('af1d30d63226cb259545dc288ccb169d',259,10000,'10.0.0.216','2009-04-25 22:24:12'),
 ('db6ec2672f88ec39a173a78f0363c52f',259,10000,'10.0.0.216','2009-04-25 23:52:07'),
 ('8b526d9fab2319c82e216720c645002e',259,10000,'10.0.0.216','2009-04-27 00:18:45');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('9bad84792d31930774f27d98df9a7b9c',259,10000,'10.0.0.216','2009-04-27 13:49:10'),
 ('40167837611678f4efba733a6cd59703',259,10000,'10.0.0.220','2009-04-29 17:20:22'),
 ('0a42b3da6801c6423c7c24b36a92298c',259,10000,'10.0.0.220','2009-04-29 17:22:56'),
 ('eec1dec8b8443c2328fe33b0ddafe003',259,10000,'127.0.0.1','2009-05-05 12:07:06'),
 ('830ccd21bea9ade7ddcb4c76b000ba1e',259,10000,'10.0.0.216','2009-05-05 13:41:25'),
 ('50876f9406168dc1127fd27a09e7a304',259,10000,'10.0.0.216','2009-05-05 13:41:58'),
 ('ce30f2d718ff254f2a5ca31f79b61d70',259,10000,'10.0.0.216','2009-05-05 15:53:56'),
 ('f06b3eefc00bbc163df86a0ed36eba79',259,10000,'127.0.0.1','2009-05-05 18:00:07'),
 ('d1b930747bc0645e251bcba1f511aad7',259,10000,'10.0.0.216','2009-05-05 22:12:58'),
 ('5b803c4de7388eb3933e07e4ddc61092',259,10000,'10.0.0.216','2009-05-05 22:13:44');
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_sessions` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_styles`
--

DROP TABLE IF EXISTS `nova_front`.`qo_styles`;
CREATE TABLE  `nova_front`.`qo_styles` (
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_themes_id` int(11) unsigned NOT NULL default '1',
  `qo_wallpapers_id` int(11) unsigned NOT NULL default '1',
  `backgroundcolor` varchar(6) NOT NULL default 'ffffff',
  `fontcolor` varchar(6) default NULL,
  `transparency` int(3) NOT NULL default '100',
  `wallpaperposition` varchar(6) NOT NULL default 'center',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_styles`
--

/*!40000 ALTER TABLE `qo_styles` DISABLE KEYS */;
LOCK TABLES `qo_styles` WRITE;
INSERT INTO `nova_front`.`qo_styles` VALUES  (232,9999,1,12,'ffffff','0',100,'center'),
 (231,1,1,12,'ffffff','0',100,'tile'),
 (200,1000,1,10,'f9f9f9','FCF8F8',100,'tile'),
 (230,2,2,8,'ffffff','2D53DB',100,'center'),
 (233,2,1,1,'ffffff','0',100,'center'),
 (234,2,1,1,'ffffff','0',100,'center'),
 (235,1000,1,1,'ffffff','0',100,'center'),
 (236,10000,1,11,'ffffff','0',100,'center'),
 (237,10000,3,4,'ffffff','F8E8E8',77,'tile'),
 (238,10000,1,1,'ffffff','0',100,'center'),
 (242,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (243,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (244,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (245,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (246,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (247,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (248,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (249,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (250,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (251,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (252,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (253,10000,1,10,'ffffff','fcf8f8',100,'center');
INSERT INTO `nova_front`.`qo_styles` VALUES  (254,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (255,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (256,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (257,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (258,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (259,10000,1,4,'ffffff','E0EBE2',100,'tile'),
 (260,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (261,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (262,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (263,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (264,10000,1,10,'ffffff','fcf8f8',100,'center'),
 (265,10000,1,4,'ffffff','fcf8f8',100,'center');
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_styles` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_themes`
--

DROP TABLE IF EXISTS `nova_front`.`qo_themes`;
CREATE TABLE  `nova_front`.`qo_themes` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL COMMENT 'The display name',
  `author` varchar(55) default NULL,
  `version` varchar(25) default NULL,
  `url` varchar(255) default NULL COMMENT 'Url which provides additional information',
  `path_to_thumbnail` varchar(255) default NULL,
  `path_to_file` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_themes`
--

/*!40000 ALTER TABLE `qo_themes` DISABLE KEYS */;
LOCK TABLES `qo_themes` WRITE;
INSERT INTO `nova_front`.`qo_themes` VALUES  (1,'Vista Blue','Todd Murdock','0.8',NULL,'xtheme-vistablue/xtheme-vistablue.png','xtheme-vistablue/css/xtheme-vistablue.css'),
 (2,'Vista Black','Todd Murdock','0.8',NULL,'xtheme-vistablack/xtheme-vistablack.png','xtheme-vistablack/css/xtheme-vistablack.css'),
 (3,'Vista Glass','Todd Murdock','0.8',NULL,'xtheme-vistaglass/xtheme-vistaglass.png','xtheme-vistaglass/css/xtheme-vistaglass.css');
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_themes` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`qo_wallpapers`
--

DROP TABLE IF EXISTS `nova_front`.`qo_wallpapers`;
CREATE TABLE  `nova_front`.`qo_wallpapers` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL COMMENT 'Display name',
  `author` varchar(55) default NULL,
  `url` varchar(255) default NULL COMMENT 'Url which provides information',
  `path_to_thumbnail` varchar(255) default NULL,
  `path_to_file` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_wallpapers`
--

/*!40000 ALTER TABLE `qo_wallpapers` DISABLE KEYS */;
LOCK TABLES `qo_wallpapers` WRITE;
INSERT INTO `nova_front`.`qo_wallpapers` VALUES  (1,'qWikiOffice','Todd Murdock',NULL,'thumbnails/qwikioffice.jpg','qwikioffice.jpg'),
 (2,'Colorado Farm',NULL,NULL,'thumbnails/colorado-farm.jpg','colorado-farm.jpg'),
 (3,'Curls On Green',NULL,NULL,'thumbnails/curls-on-green.jpg','curls-on-green.jpg'),
 (4,'Emotion',NULL,NULL,'thumbnails/emotion.jpg','emotion.jpg'),
 (5,'Eos',NULL,NULL,'thumbnails/eos.jpg','eos.jpg'),
 (6,'Fields of Peace',NULL,NULL,'thumbnails/fields-of-peace.jpg','fields-of-peace.jpg'),
 (7,'Fresh Morning',NULL,NULL,'thumbnails/fresh-morning.jpg','fresh-morning.jpg'),
 (8,'Ladybuggin',NULL,NULL,'thumbnails/ladybuggin.jpg','ladybuggin.jpg'),
 (9,'Summer',NULL,NULL,'thumbnails/summer.jpg','summer.jpg'),
 (10,'Blue Swirl',NULL,NULL,'thumbnails/blue-swirl.jpg','blue-swirl.jpg'),
 (11,'Blue Psychedelic',NULL,NULL,'thumbnails/blue-psychedelic.jpg','blue-psychedelic.jpg'),
 (12,'Blue Curtain',NULL,NULL,'thumbnails/blue-curtain.jpg','blue-curtain.jpg'),
 (13,'Blank',NULL,NULL,'thumbnails/blank.gif','blank.gif');
UNLOCK TABLES;
/*!40000 ALTER TABLE `qo_wallpapers` ENABLE KEYS */;


--
-- Definition of table `nova_front`.`templatemodule`
--

DROP TABLE IF EXISTS `nova_front`.`templatemodule`;
CREATE TABLE  `nova_front`.`templatemodule` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `firstName` varchar(25) default NULL,
  `lastName` varchar(35) default NULL,
  `emailAddress` varchar(55) default NULL,
  `password` varchar(15) default NULL,
  `active` set('false','true') NOT NULL default 'false',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`templatemodule`
--

/*!40000 ALTER TABLE `templatemodule` DISABLE KEYS */;
LOCK TABLES `templatemodule` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `templatemodule` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;