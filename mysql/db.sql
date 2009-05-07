-- MySQL dump 10.11
--
-- Host: localhost    Database: nova_core
-- ------------------------------------------------------
-- Server version	5.0.75-0ubuntu10.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `nova_core`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `nova_core` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `nova_core`;

--
-- Table structure for table `net_pools`
--

DROP TABLE IF EXISTS `net_pools`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `net_pools` (
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
) ENGINE=InnoDB AUTO_INCREMENT=832494619 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `net_pools`
--

LOCK TABLES `net_pools` WRITE;
/*!40000 ALTER TABLE `net_pools` DISABLE KEYS */;
INSERT INTO `net_pools` VALUES (832494616,'neta','10.0.3.2','28',13,1,67,'2009-05-06 08:04:59','2009-05-07 03:45:14'),(832494617,'netb','10.0.3.18','28',13,1,23,'2009-05-06 08:04:59','2009-05-07 07:16:16'),(832494618,'netc','10.0.3.34','28',13,0,10,'2009-05-06 08:04:59','2009-05-06 11:43:01');
/*!40000 ALTER TABLE `net_pools` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifies`
--

DROP TABLE IF EXISTS `notifies`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `notifies` (
  `id` int(11) NOT NULL auto_increment,
  `notify_uuid` varchar(255) default NULL,
  `notify_receiver_type` varchar(255) default NULL,
  `notify_receiver_id` int(11) default NULL,
  `notify_type` varchar(255) default NULL,
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `notifies`
--

LOCK TABLES `notifies` WRITE;
/*!40000 ALTER TABLE `notifies` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pmachines`
--

DROP TABLE IF EXISTS `pmachines`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `pmachines` (
  `id` int(11) NOT NULL auto_increment,
  `ip` varchar(255) default NULL,
  `status` varchar(255) default 'working',
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `pmachines`
--

LOCK TABLES `pmachines` WRITE;
/*!40000 ALTER TABLE `pmachines` DISABLE KEYS */;
INSERT INTO `pmachines` VALUES (1,'10.0.0.220','working','2009-05-06 08:12:57','2009-05-06 08:12:57');
/*!40000 ALTER TABLE `pmachines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schema_migrations`
--

DROP TABLE IF EXISTS `schema_migrations`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `schema_migrations` (
  `version` varchar(255) NOT NULL,
  UNIQUE KEY `unique_schema_migrations` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `schema_migrations`
--

LOCK TABLES `schema_migrations` WRITE;
/*!40000 ALTER TABLE `schema_migrations` DISABLE KEYS */;
INSERT INTO `schema_migrations` VALUES ('20090331031132'),('20090331031137'),('20090331031142'),('20090331031146'),('20090413073037'),('20090417054023'),('20090420012138');
/*!40000 ALTER TABLE `schema_migrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `users` (
  `id` int(11) NOT NULL auto_increment,
  `email` varchar(255) default NULL,
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=270 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (267,'tomcat2','2009-05-06 08:10:48','2009-05-06 08:10:48'),(268,'misa2','2009-05-06 18:09:44','2009-05-06 18:09:44'),(269,'jimmycart','2009-05-07 07:15:00','2009-05-07 07:15:00');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vclusters`
--

DROP TABLE IF EXISTS `vclusters`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vclusters` (
  `id` int(11) NOT NULL auto_increment,
  `user_id` int(11) default NULL,
  `vcluster_name` varchar(255) default '#unnamed#',
  `net_pool_name` varchar(255) default '',
  `created_at` datetime default NULL,
  `updated_at` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vclusters`
--

LOCK TABLES `vclusters` WRITE;
/*!40000 ALTER TABLE `vclusters` DISABLE KEYS */;
INSERT INTO `vclusters` VALUES (1,NULL,'My_Cluster_1','','2009-05-06 08:11:37','2009-05-06 08:11:37'),(2,NULL,'somecluster','','2009-05-06 10:25:15','2009-05-06 10:25:15'),(3,NULL,'link','','2009-05-06 10:27:42','2009-05-06 10:27:42'),(4,NULL,'link','','2009-05-06 10:27:42','2009-05-06 10:27:42'),(5,NULL,'misanet','','2009-05-06 10:32:10','2009-05-06 10:32:10'),(6,NULL,'misanet','','2009-05-06 10:32:27','2009-05-06 10:32:27'),(7,NULL,'misanet2','netb','2009-05-06 10:32:55','2009-05-06 10:32:55'),(8,NULL,'misanet2','netb','2009-05-06 10:50:51','2009-05-06 10:50:51'),(9,NULL,'misanet2','neta','2009-05-06 10:52:04','2009-05-06 10:52:04'),(10,NULL,'misanet2','netb','2009-05-06 10:52:51','2009-05-06 10:52:51'),(11,NULL,'misanet2','netc','2009-05-06 10:52:52','2009-05-06 10:52:52'),(12,NULL,'misanet2',NULL,'2009-05-06 10:52:53','2009-05-06 10:52:53'),(13,NULL,'misanet2','neta','2009-05-06 10:54:36','2009-05-06 10:54:36'),(14,NULL,'misanet2','netb','2009-05-06 10:55:39','2009-05-06 10:55:39'),(15,NULL,'misanet2','netc','2009-05-06 10:55:40','2009-05-06 10:55:40'),(16,NULL,'misanet2',NULL,'2009-05-06 10:55:41','2009-05-06 10:55:41'),(17,NULL,'misanet2','neta','2009-05-06 10:58:03','2009-05-06 10:58:03'),(18,NULL,'misanet2','netb','2009-05-06 10:59:17','2009-05-06 10:59:17'),(19,NULL,'misanet2','netc','2009-05-06 10:59:24','2009-05-06 10:59:24'),(20,NULL,'misanet2','neta','2009-05-06 11:02:06','2009-05-06 11:02:06'),(21,NULL,'cattyc','netb','2009-05-06 11:13:26','2009-05-06 11:13:26'),(22,NULL,'cattyc','netc','2009-05-06 11:15:52','2009-05-06 11:15:52'),(23,NULL,'cattyc','neta','2009-05-06 11:18:17','2009-05-06 11:18:17'),(24,NULL,'cattyc','neta','2009-05-06 11:20:09','2009-05-06 11:20:09'),(28,NULL,'Hadoop_Cluster_test','neta','2009-05-06 11:35:20','2009-05-06 11:35:21'),(29,NULL,'Hadoop_Cluster','netb','2009-05-06 11:40:50','2009-05-06 11:40:50'),(30,NULL,'cattyc','netc','2009-05-06 11:41:58','2009-05-06 11:41:59'),(41,NULL,'Hadoop_Cluster','neta','2009-05-06 12:19:41','2009-05-06 12:19:41'),(42,NULL,'Hadoop_Cluster','neta','2009-05-06 12:22:48','2009-05-06 12:22:49'),(43,NULL,'Hadoop_Cluster','netb','2009-05-06 12:24:29','2009-05-06 12:24:29'),(44,NULL,'Hadoop_Cluster','neta','2009-05-06 12:25:51','2009-05-06 12:25:51'),(45,NULL,'Hadoop_Cluster','neta','2009-05-06 12:27:38','2009-05-06 12:27:38'),(47,NULL,'Hadoop_Cluster','neta','2009-05-06 12:42:41','2009-05-06 12:42:41'),(52,NULL,'Hadoop_Cluster','neta','2009-05-06 13:51:24','2009-05-06 13:51:24'),(53,NULL,'Hadoop_Cluster','neta','2009-05-06 14:10:00','2009-05-06 14:10:01'),(59,268,'My_Cluster_1','','2009-05-06 18:10:01','2009-05-06 18:10:01'),(60,268,'My_Cluster_2','','2009-05-06 18:10:02','2009-05-06 18:10:02'),(61,268,'My_Cluster_3','','2009-05-06 18:10:05','2009-05-06 18:10:05'),(62,268,'My_Cluster_4','','2009-05-06 18:10:06','2009-05-06 18:10:06'),(63,268,'My_Cluster_5','','2009-05-06 18:10:07','2009-05-06 18:10:07'),(64,NULL,'Hadoop_Cluster','neta','2009-05-07 03:40:54','2009-05-07 03:40:54'),(65,267,'Hadoop_Cluster','neta','2009-05-07 03:45:14','2009-05-07 03:45:14'),(66,267,'My_Cluster_2','','2009-05-07 07:16:01','2009-05-07 07:16:01'),(67,267,'Hadoop_Cluster','netb','2009-05-07 07:16:16','2009-05-07 07:16:16');
/*!40000 ALTER TABLE `vclusters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vimages`
--

DROP TABLE IF EXISTS `vimages`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vimages` (
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vimages`
--

LOCK TABLES `vimages` WRITE;
/*!40000 ALTER TABLE `vimages` DISABLE KEYS */;
INSERT INTO `vimages` VALUES (1,NULL,'Windows','Fedora_8',0,'f8.img','','2009-05-06 08:12:18','2009-05-06 08:12:18'),(2,NULL,'Other','os100m',0,'os100m.img','','2009-05-06 14:53:58','2009-05-06 14:53:58');
/*!40000 ALTER TABLE `vimages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vmachines`
--

DROP TABLE IF EXISTS `vmachines`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vmachines` (
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
) ENGINE=InnoDB AUTO_INCREMENT=249 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vmachines`
--

LOCK TABLES `vmachines` WRITE;
/*!40000 ALTER TABLE `vmachines` DISABLE KEYS */;
INSERT INTO `vmachines` VALUES (195,NULL,NULL,45,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:03\",\"ip\":\"10.0.3.3\",\"hostname\":\"neta_1\",\"vcpu\":\"4\",\"mem\":\"4001\"}','2009-05-06 12:27:38','2009-05-06 12:27:39'),(197,NULL,NULL,45,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:05\",\"ip\":\"10.0.3.5\",\"hostname\":\"neta_3\",\"vcpu\":\"4\",\"mem\":\"4001\"}','2009-05-06 12:27:38','2009-05-06 12:27:39'),(218,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"f8.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-06 13:43:17','2009-05-06 13:43:19'),(219,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"f8.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-06 13:45:03','2009-05-06 13:45:05'),(230,NULL,NULL,53,NULL,NULL,'not running','{\"img\":\"f8.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-06 14:13:19','2009-05-06 14:13:19'),(231,NULL,NULL,53,NULL,NULL,'not running','{\"img\":\"f8.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-06 14:13:25','2009-05-06 14:13:25'),(232,NULL,1,NULL,NULL,NULL,'not running','{\"img\":\"f8.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"512\"}','2009-05-06 14:13:45','2009-05-06 14:13:48'),(233,'10.0.3.1',NULL,NULL,NULL,'','not running','{\"img\":\"os100m.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"133\"}','2009-05-06 14:54:17','2009-05-06 14:54:32'),(234,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:02\",\"ip\":\"10.0.3.2\",\"hostname\":\"neta_0\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 03:40:54','2009-05-07 03:40:55'),(235,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:03\",\"ip\":\"10.0.3.3\",\"hostname\":\"neta_1\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 03:40:54','2009-05-07 03:40:55'),(236,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:04\",\"ip\":\"10.0.3.4\",\"hostname\":\"neta_2\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 03:40:54','2009-05-07 03:40:55'),(237,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:05\",\"ip\":\"10.0.3.5\",\"hostname\":\"neta_3\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 03:40:54','2009-05-07 03:40:55'),(238,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"os100m.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"123\"}','2009-05-07 03:41:33','2009-05-07 03:41:33'),(239,NULL,NULL,NULL,NULL,NULL,'not running','{\"img\":\"f8.img\",\"mac\":\"\",\"ip\":\"\",\"vcpu\":\"2\",\"mem\":\"123\"}','2009-05-07 03:41:40','2009-05-07 03:41:40'),(240,NULL,NULL,65,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:02\",\"ip\":\"10.0.3.2\",\"hostname\":\"neta_0\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 03:45:14','2009-05-07 03:45:15'),(241,NULL,NULL,65,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:03\",\"ip\":\"10.0.3.3\",\"hostname\":\"neta_1\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 03:45:14','2009-05-07 03:45:15'),(242,NULL,NULL,65,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:04\",\"ip\":\"10.0.3.4\",\"hostname\":\"neta_2\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 03:45:14','2009-05-07 03:45:15'),(243,NULL,NULL,65,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:05\",\"ip\":\"10.0.3.5\",\"hostname\":\"neta_3\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 03:45:14','2009-05-07 03:45:15'),(244,NULL,NULL,NULL,NULL,NULL,'not running',NULL,'2009-05-07 07:14:31','2009-05-07 07:14:31'),(245,NULL,NULL,67,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:12\",\"ip\":\"10.0.3.18\",\"hostname\":\"netb_0\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 07:16:16','2009-05-07 07:16:16'),(246,NULL,NULL,67,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:13\",\"ip\":\"10.0.3.19\",\"hostname\":\"netb_1\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 07:16:16','2009-05-07 07:16:16'),(247,NULL,NULL,67,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:14\",\"ip\":\"10.0.3.20\",\"hostname\":\"netb_2\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 07:16:16','2009-05-07 07:16:17'),(248,NULL,NULL,67,NULL,NULL,'not running','{\"img\":null,\"mac\":\"54:7E:10:00:03:15\",\"ip\":\"10.0.3.21\",\"hostname\":\"netb_3\",\"vcpu\":\"1\",\"mem\":\"512\"}','2009-05-07 07:16:16','2009-05-07 07:16:17');
/*!40000 ALTER TABLE `vmachines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `nova_front`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `nova_front` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `nova_front`;

--
-- Table structure for table `currentlyloggedin`
--

DROP TABLE IF EXISTS `currentlyloggedin`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `currentlyloggedin` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `session_id` varchar(45) NOT NULL,
  `member_id` varchar(45) NOT NULL,
  `group_id` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `currentlyloggedin`
--

LOCK TABLES `currentlyloggedin` WRITE;
/*!40000 ALTER TABLE `currentlyloggedin` DISABLE KEYS */;
INSERT INTO `currentlyloggedin` VALUES (10,'6d51c3c6ec21dc91c5b488900938ef94','232','10'),(31,'37a2e6dd5621b36ab9c5d0ef5acadf4d','200','1000'),(84,'40d7d7f55a4dc8bb5aa758c2b140e938','259','10000'),(85,'8b526d9fab2319c82e216720c645002e','259','10000'),(90,'9bad84792d31930774f27d98df9a7b9c','259','10000'),(95,'40167837611678f4efba733a6cd59703','259','10000'),(96,'0a42b3da6801c6423c7c24b36a92298c','259','10000'),(97,'eec1dec8b8443c2328fe33b0ddafe003','259','10000'),(98,'830ccd21bea9ade7ddcb4c76b000ba1e','259','10000'),(99,'50876f9406168dc1127fd27a09e7a304','259','10000'),(100,'ce30f2d718ff254f2a5ca31f79b61d70','259','10000'),(101,'f06b3eefc00bbc163df86a0ed36eba79','259','10000'),(102,'d1b930747bc0645e251bcba1f511aad7','259','10000'),(103,'5b803c4de7388eb3933e07e4ddc61092','259','10000'),(106,'d518bebf29a5add76b14af4bf526483f','266','10000'),(116,'7600285ab9bfeadf74a41ebda400efe6','268','10000'),(118,'4af3883843cd5cb622c207546369c786','267','10000');
/*!40000 ALTER TABLE `currentlyloggedin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_dependencies`
--

DROP TABLE IF EXISTS `qo_dependencies`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_dependencies` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `directory` varchar(255) default '' COMMENT 'The directory within the modules directory stated in the system/os/config.php',
  `file` varchar(255) default NULL COMMENT 'The file that contains the dependency',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=109 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_dependencies`
--

LOCK TABLES `qo_dependencies` WRITE;
/*!40000 ALTER TABLE `qo_dependencies` DISABLE KEYS */;
INSERT INTO `qo_dependencies` VALUES (100,'templateModule/','Ext.ux.AboutWindow.js'),(101,'vc-dependency/grid/filter/','Filter.js'),(102,'vc-dependency/grid/filter/','BooleanFilter.js'),(103,'vc-dependency/grid/filter/','ListFilter.js'),(104,'vc-dependency/grid/filter/','DateFilter.js'),(108,'vc-dependency/grid/','GridFilters.js'),(106,'vc-dependency/grid/filter/','NumericFilter.js'),(107,'vc-dependency/grid/filter/','StringFilter.js');
/*!40000 ALTER TABLE `qo_dependencies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_domains`
--

DROP TABLE IF EXISTS `qo_domains`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_domains` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `is_singular` tinyint(1) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_domains`
--

LOCK TABLES `qo_domains` WRITE;
/*!40000 ALTER TABLE `qo_domains` DISABLE KEYS */;
INSERT INTO `qo_domains` VALUES (1,'All Modules','All the modules',0),(2,'QoPreferences','The QoPreferences module',1),(9,'superadminModules',NULL,0),(10,'adminModules','Modules for vc admin',0),(11,'userModules','Modules for vc users',0),(200,'TemplateModule','Basic Module template.',1);
/*!40000 ALTER TABLE `qo_domains` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_domains_has_modules`
--

DROP TABLE IF EXISTS `qo_domains_has_modules`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_domains_has_modules` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_domains_id` int(11) unsigned default NULL,
  `qo_modules_id` int(11) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_domains_has_modules`
--

LOCK TABLES `qo_domains_has_modules` WRITE;
/*!40000 ALTER TABLE `qo_domains_has_modules` DISABLE KEYS */;
INSERT INTO `qo_domains_has_modules` VALUES (1,1,1),(7,2,1),(8,1,90),(9,200,90),(22,1,100),(23,1,101),(24,1,102),(25,1,103),(26,10,102),(27,11,103),(29,9,100),(30,9,101),(31,9,1),(32,9,104),(33,10,104),(34,11,104),(35,1,104),(36,11,105),(37,1,105),(38,2,104),(39,1,106),(40,11,106),(41,1,107),(43,10,107),(44,9,107);
/*!40000 ALTER TABLE `qo_domains_has_modules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_error_log`
--

DROP TABLE IF EXISTS `qo_error_log`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_error_log` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `text` text,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_error_log`
--

LOCK TABLES `qo_error_log` WRITE;
/*!40000 ALTER TABLE `qo_error_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `qo_error_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_groups`
--

DROP TABLE IF EXISTS `qo_groups`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_groups` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `importance` int(3) unsigned default '1',
  `active` tinyint(1) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=10001 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_groups`
--

LOCK TABLES `qo_groups` WRITE;
/*!40000 ALTER TABLE `qo_groups` DISABLE KEYS */;
INSERT INTO `qo_groups` VALUES (1,'administrator','System administrator',50,1),(10000,'user','General user',20,1),(10,'debug',NULL,1000,1),(1000,'super_admin','The almighty',100,1);
/*!40000 ALTER TABLE `qo_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_groups_has_domain_privileges`
--

DROP TABLE IF EXISTS `qo_groups_has_domain_privileges`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_groups_has_domain_privileges` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_groups_id` int(11) unsigned default '0',
  `qo_domains_id` int(11) unsigned default '0',
  `qo_privileges_id` int(11) unsigned default '0',
  `is_allowed` tinyint(1) unsigned default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=224 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_groups_has_domain_privileges`
--

LOCK TABLES `qo_groups_has_domain_privileges` WRITE;
/*!40000 ALTER TABLE `qo_groups_has_domain_privileges` DISABLE KEYS */;
INSERT INTO `qo_groups_has_domain_privileges` VALUES (102,1000,2,2,1),(103,1000,200,3,1),(203,10000,2,2,1),(206,1,2,2,1),(208,1,10,10,1),(211,10000,2,1,1),(212,10000,11,11,1),(213,10000,11,1,1),(215,1,2,1,1),(216,1,10,1,1),(220,1000,9,9,1),(221,1000,9,1,1),(222,10,1,1,1),(223,10,2,2,1);
/*!40000 ALTER TABLE `qo_groups_has_domain_privileges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_groups_has_members`
--

DROP TABLE IF EXISTS `qo_groups_has_members`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_groups_has_members` (
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member currently active in this group',
  `admin` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member the administrator of this group',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_groups_has_members`
--

LOCK TABLES `qo_groups_has_members` WRITE;
/*!40000 ALTER TABLE `qo_groups_has_members` DISABLE KEYS */;
INSERT INTO `qo_groups_has_members` VALUES (1,231,1,1),(10000,230,1,0),(1000,200,1,1),(10,232,1,1),(2,233,1,0),(2,234,1,0),(1000,235,1,0),(10000,236,1,0),(10000,237,1,0),(10000,238,1,0),(10000,239,1,0),(10000,240,1,0),(10000,241,1,0),(10000,242,1,0),(10000,243,1,0),(10000,244,1,0),(10000,245,1,0),(10000,246,1,0),(10000,247,1,0),(10000,248,1,0),(10000,249,1,0),(10000,250,1,0),(10000,251,1,0),(10000,252,1,0),(10000,253,1,0),(10000,254,1,0),(10000,255,1,0),(10000,256,1,0),(10000,257,1,0),(10000,258,1,0),(10000,259,1,0),(10000,260,1,0),(10000,261,1,0),(10000,262,1,0),(10000,263,1,0),(10000,264,1,0),(10000,265,1,0),(10000,266,1,0),(10000,267,1,0),(10000,268,1,0);
/*!40000 ALTER TABLE `qo_groups_has_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_launchers`
--

DROP TABLE IF EXISTS `qo_launchers`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_launchers` (
  `id` int(2) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_launchers`
--

LOCK TABLES `qo_launchers` WRITE;
/*!40000 ALTER TABLE `qo_launchers` DISABLE KEYS */;
INSERT INTO `qo_launchers` VALUES (1,'autorun'),(2,'contextmenu'),(3,'quickstart'),(4,'shortcut');
/*!40000 ALTER TABLE `qo_launchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_members`
--

DROP TABLE IF EXISTS `qo_members`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_members` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `first_name` varchar(25) default NULL,
  `last_name` varchar(35) default NULL,
  `email_address` varchar(55) default NULL,
  `password` varchar(15) default NULL,
  `language` varchar(5) default 'en',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member currently active',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=269 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_members`
--

LOCK TABLES `qo_members` WRITE;
/*!40000 ALTER TABLE `qo_members` DISABLE KEYS */;
INSERT INTO `qo_members` VALUES (200,'Santa','Zhang','santa','zhang','en',1),(232,'debug','debug','debug','debug','en',1),(231,'admin','admin','admin','admin','en',1),(267,'tomcat2','tomcat2','tomcat2','tomcat2','en',1),(268,'misa2','misa2','misa2','misa2','en',1);
/*!40000 ALTER TABLE `qo_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_members_has_module_launchers`
--

DROP TABLE IF EXISTS `qo_members_has_module_launchers`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_members_has_module_launchers` (
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `qo_launchers_id` int(10) unsigned NOT NULL default '0',
  `sort_order` int(5) unsigned NOT NULL default '0' COMMENT 'sort within each launcher',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`,`qo_modules_id`,`qo_launchers_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_members_has_module_launchers`
--

LOCK TABLES `qo_members_has_module_launchers` WRITE;
/*!40000 ALTER TABLE `qo_members_has_module_launchers` DISABLE KEYS */;
INSERT INTO `qo_members_has_module_launchers` VALUES (230,10000,105,4,3),(230,10000,104,4,4),(230,10000,1,4,6),(231,1,1,4,3),(231,1,102,4,1),(231,1,104,4,0),(230,10000,2,4,2),(230,10000,103,4,5),(230,10000,8,4,1),(200,1000,3,4,4),(200,1000,104,4,6),(200,1000,1,4,8),(231,1,3,4,2),(200,1000,4,4,0),(231,1,5,4,4),(200,1000,100,4,7),(200,1000,2,4,2),(200,1000,90,4,5),(200,1000,5,3,0),(200,1000,5,4,3),(230,10000,4,4,0),(232,10,8,4,10),(230,10000,1,3,0),(232,10,104,4,1),(232,10,103,4,8),(232,10,2,4,12),(232,10,101,4,6),(232,10,100,4,5),(232,10,90,4,4),(232,10,1,4,3),(232,10,5,4,2),(232,10,3,4,0),(232,10,102,4,7),(232,10,105,4,9),(232,10,4,4,11),(236,10000,4,4,0),(236,10000,8,4,1),(236,10000,105,4,2),(236,10000,2,4,3),(236,10000,104,4,4),(236,10000,1,4,5),(236,10000,103,4,6),(237,10000,105,4,0),(242,10000,105,4,0),(243,10000,105,4,0),(244,10000,105,4,0),(245,10000,105,4,0),(246,10000,105,4,0),(247,10000,105,4,0),(248,10000,105,4,0),(249,10000,105,4,0),(250,10000,105,4,0),(251,10000,105,4,0),(252,10000,105,4,0),(253,10000,105,4,0),(254,10000,105,4,0),(255,10000,105,4,0),(256,10000,105,4,0),(257,10000,105,4,0),(258,10000,105,4,0),(259,10000,106,4,1),(260,10000,105,4,0),(259,10000,105,3,1),(265,10000,105,4,0),(259,10000,2,3,0),(261,10000,105,4,0),(232,10,106,4,13),(259,10000,105,4,0),(262,10000,106,4,0),(262,10000,105,4,1),(263,10000,105,4,0),(263,10000,106,4,0),(263,10000,104,4,0),(263,10000,103,4,0),(263,10000,1,4,0),(264,10000,105,4,0),(264,10000,106,4,0),(264,10000,104,4,0),(264,10000,103,4,0),(264,10000,1,4,0),(200,1000,101,4,9),(200,1000,8,4,1),(259,10000,104,4,2),(259,10000,1,4,3),(259,10000,103,4,4),(259,10000,106,3,2),(265,10000,106,4,0),(265,10000,104,4,0),(265,10000,103,4,0),(265,10000,1,4,0),(266,10000,105,4,0),(266,10000,106,4,0),(266,10000,104,4,0),(266,10000,103,4,0),(266,10000,1,4,0),(267,10000,105,4,0),(267,10000,106,4,0),(267,10000,104,4,0),(267,10000,103,4,0),(267,10000,1,4,0),(200,1000,107,4,10),(268,10000,105,4,0),(268,10000,106,4,0),(268,10000,104,4,0),(268,10000,103,4,0),(268,10000,1,4,0);
/*!40000 ALTER TABLE `qo_members_has_module_launchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_modules`
--

DROP TABLE IF EXISTS `qo_modules`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_modules` (
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
) ENGINE=MyISAM AUTO_INCREMENT=108 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_modules`
--

LOCK TABLES `qo_modules` WRITE;
/*!40000 ALTER TABLE `qo_modules` DISABLE KEYS */;
INSERT INTO `qo_modules` VALUES (1,'Todd Murdock','1.0','http://www.qwikioffice.com','A system application.  Allows users to set, and save their desktop preferences to the database.','system/preferences','qo-preferences',1,1),(2,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of window with grid.','demo','demo-grid',1,1),(3,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of window with tabs.','demo','demo-tabs',1,1),(4,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of window with accordion.','demo','demo-acc',1,1),(5,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of window with layout.','demo','demo-layout',1,1),(8,'Jack Slocum','1.0','http://www.qwikioffice.com','Demo of bogus window.','demo','demo-bogus',1,1),(90,'templateModule Author','0.0.1','http://www.qwikioffice.com','Basic Module Template.','templateModule','templateModule',1,1),(100,'Santa Zhang','0.0.1',NULL,NULL,'superadmin-user-manager','superadmin-user-manager',1,1),(101,'Santa Zhang','0.0.1',NULL,NULL,'superadmin-manual','superadmin-manual',0,1),(102,'Santa Zhang',NULL,NULL,NULL,'admin-manual','admin-manual',0,1),(103,'Santa Zhang',NULL,NULL,NULL,'user-manual','user-manual',0,1),(104,'Santa Zhang',NULL,NULL,NULL,'account-setting','account-setting',1,1),(105,'Santa Zhang',NULL,NULL,NULL,'user-job-manager','user-job-manager',1,1),(106,'Santa Zhang','0.0.0',NULL,NULL,'hadoop-wizard','hadoop-wizard',1,1),(107,'Santa Zhang','0.0.0',NULL,NULL,'admin-monitor','admin-monitor',1,1);
/*!40000 ALTER TABLE `qo_modules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_modules_actions`
--

DROP TABLE IF EXISTS `qo_modules_actions`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_modules_actions` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_modules_id` int(11) unsigned default NULL,
  `name` varchar(35) default NULL,
  `description` text,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=117 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_modules_actions`
--

LOCK TABLES `qo_modules_actions` WRITE;
/*!40000 ALTER TABLE `qo_modules_actions` DISABLE KEYS */;
INSERT INTO `qo_modules_actions` VALUES (1,0,'loadModule','Allow the user to load the module.  Give them access to it.  Does not belong to any particular module'),(2,1,'saveAppearance','Save appearance'),(3,1,'saveAutorun','Save autorun'),(4,1,'saveBackground','Save background'),(5,1,'saveQuickstart','Save quickstart'),(6,1,'saveShortcut','Save shortcut'),(7,1,'viewThemes','View themes'),(8,1,'viewWallpapers','View wallpapers'),(90,104,'doAccount',NULL),(92,90,'doTask','Get or Save data, depending on what is sent thru \"task\" request.'),(93,100,'viewUserInfo',NULL),(94,100,'toggleActive',NULL),(95,100,'kickAss',NULL),(96,104,'viewAccount',NULL),(97,104,'updateAccount',NULL),(98,105,'dummyTest','For dummy test purpose'),(99,105,'infoVM',NULL),(100,105,'resumeVM',NULL),(101,105,'pauseVM',NULL),(102,105,'listVM',NULL),(103,105,'stopVM',NULL),(104,105,'startVM',NULL),(105,105,'removeVM',NULL),(106,105,'newVM',NULL),(107,105,'removeCluster',NULL),(108,105,'addCluster',NULL),(109,105,'listCluster',NULL),(110,106,'progress','Hadoop installing progress'),(111,106,'create','Create new hadoop cluster'),(112,105,'listImage',NULL),(113,107,NULL,NULL),(114,107,'removeCluster',NULL),(115,107,'removeVM',NULL),(116,107,'listVM',NULL);
/*!40000 ALTER TABLE `qo_modules_actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_modules_files`
--

DROP TABLE IF EXISTS `qo_modules_files`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_modules_files` (
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `directory` varchar(255) default '' COMMENT 'The directory within the modules directory stated in the system/os/config.php',
  `file` varchar(255) NOT NULL default '' COMMENT 'The file that contains the dependency',
  `is_stylesheet` tinyint(1) unsigned default '0',
  `is_server_module` tinyint(1) unsigned default '0',
  `is_client_module` tinyint(1) unsigned default '0',
  `class_name` varchar(55) default '',
  PRIMARY KEY  (`qo_modules_id`,`file`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_modules_files`
--

LOCK TABLES `qo_modules_files` WRITE;
/*!40000 ALTER TABLE `qo_modules_files` DISABLE KEYS */;
INSERT INTO `qo_modules_files` VALUES (1,'qo-preferences/','qo-preferences-override.js',0,0,0,''),(4,'acc-win/','acc-win-override.js',0,0,0,''),(5,'layout-win/','layout-win-override.js',0,0,0,''),(8,'bogus/bogus-win/','bogus-win-override.js',0,0,0,''),(2,'grid-win/','grid-win-override.js',0,0,0,''),(3,'tab-win/','tab-win-override.js',0,0,0,''),(1,'qo-preferences/','qo-preferences.js',0,0,1,'QoDesk.QoPreferences'),(1,'qo-preferences/','qo-preferences.php',0,1,0,'QoPreferences'),(2,'grid-win/','grid-win.js',0,0,1,'QoDesk.GridWindow'),(3,'tab-win/','tab-win.js',0,0,1,'QoDesk.TabWindow'),(4,'acc-win/','acc-win.js',0,0,1,'QoDesk.AccordionWindow'),(5,'layout-win/','layout-win.js',0,0,1,'QoDesk.LayoutWindow'),(8,'bogus/bogus-win/','bogus-win.js',0,0,1,'QoDesk.BogusWindow'),(1,'qo-preferences/','qo-preferences.css',1,0,0,''),(2,'grid-win/','grid-win.css',1,0,0,''),(3,'tab-win/','tab-win.css',1,0,0,''),(4,'acc-win/','acc-win.css',1,0,0,''),(5,'layout-win/','layout-win.css',1,0,0,''),(8,'bogus/bogus-win/','bogus-win.css',1,0,0,''),(100,'superadmin-user-manager/','superadmin-user-manager.js',0,0,1,'QoDesk.SuperAdminManagerWindow'),(100,'superadmin-user-manager/','superadmin-user-manager-override.js',0,0,0,''),(90,'templateModule/','templateModule-override.js',0,0,0,''),(90,'templateModule/','templateModule.js',0,0,1,'QoDesk.TemplateModule'),(90,'templateModule/','templateModule.php',0,1,0,'TemplateModule'),(90,'templateModule/','templateModule.css',1,0,0,''),(100,'superadmin-user-manager/','superadmin-user-manager.css',1,0,0,''),(101,'superadmin-manual/','superadmin-manual.js',0,0,1,'QoDesk.SuperAdminManualWindow'),(101,'superadmin-manual/','superadmin-manual-override.js',0,0,0,''),(101,'superadmin-manual/','superadmin-manual.css',1,0,0,''),(102,'admin-manual/','admin-manual.js',0,0,1,'QoDesk.AdminManualWindow'),(102,'admin-manual/','admin-manual-override.js',0,0,0,''),(102,'admin-manual/','admin-manual.css',1,0,0,''),(103,'user-manual/','user-manual.js',0,0,1,'QoDesk.UserManualWindow'),(103,'user-manual/','user-manual-override.js',0,0,0,''),(103,'user-manual/','user-manual.css',1,0,0,''),(104,'account-setting/','account-setting.js',0,0,1,'QoDesk.AccountSetting'),(104,'account-setting/','account-setting.php',0,1,0,'AccountSetting'),(104,'account-setting/','account-setting.css',1,0,0,''),(104,'account-setting/','account-setting-override.js',0,0,0,''),(105,'user-job-manager/','user-job-manager.js',0,0,1,'QoDesk.UserJobManager'),(105,'user-job-manager/','user-job-manager.css',1,0,0,''),(105,'user-job-manager/','user-job-manager-override.js',0,0,0,''),(100,'superadmin-user-manager/','superadmin-user-manager.php',0,1,0,'SuperAdminUserManager'),(106,'hadoop-wizard/','hadoop-wizard-override.js',0,0,0,''),(105,'user-job-manager/','user-job-manager.php',0,1,0,'UserJobManager'),(106,'hadoop-wizard/','hadoop-wizard.js',0,0,1,'QoDesk.HadoopWizard'),(106,'hadoop-wizard/','hadoop-wizard.php',0,1,0,'HadoopWizard'),(106,'hadoop-wizard/','hadoop-wizard.css',1,0,0,''),(107,'admin-monitor/','admin-monitor-override.js',0,0,0,''),(107,'admin-monitor/','admin-monitor.js',0,0,1,'QoDesk.AdminMonitor'),(107,'admin-monitor/','admin-monitor.php',0,1,0,'AdminMonitor'),(107,'admin-monitor/','admin-monitor.css',1,0,0,'');
/*!40000 ALTER TABLE `qo_modules_files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_modules_has_dependencies`
--

DROP TABLE IF EXISTS `qo_modules_has_dependencies`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_modules_has_dependencies` (
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `qo_dependencies_id` int(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (`qo_modules_id`,`qo_dependencies_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_modules_has_dependencies`
--

LOCK TABLES `qo_modules_has_dependencies` WRITE;
/*!40000 ALTER TABLE `qo_modules_has_dependencies` DISABLE KEYS */;
INSERT INTO `qo_modules_has_dependencies` VALUES (90,100),(100,101),(100,103),(100,108);
/*!40000 ALTER TABLE `qo_modules_has_dependencies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_privileges`
--

DROP TABLE IF EXISTS `qo_privileges`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_privileges` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `is_singular` tinyint(1) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_privileges`
--

LOCK TABLES `qo_privileges` WRITE;
/*!40000 ALTER TABLE `qo_privileges` DISABLE KEYS */;
INSERT INTO `qo_privileges` VALUES (1,'Load Module','Allows the user access to the loadModule action',0),(2,'QoPreferences','Allows the user access to all the actions of the QoPreferences mdoule',1),(9,'superadminPrivilege',NULL,0),(10,'adminPrivilege',NULL,0),(11,'userPrivilege',NULL,0),(90,'accountPrivilege',NULL,0),(91,'TemplateModule','Allows the user access to the doTask action.',1);
/*!40000 ALTER TABLE `qo_privileges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_privileges_has_module_actions`
--

DROP TABLE IF EXISTS `qo_privileges_has_module_actions`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_privileges_has_module_actions` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_privileges_id` int(11) unsigned default NULL,
  `qo_modules_actions_id` int(11) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_privileges_has_module_actions`
--

LOCK TABLES `qo_privileges_has_module_actions` WRITE;
/*!40000 ALTER TABLE `qo_privileges_has_module_actions` DISABLE KEYS */;
INSERT INTO `qo_privileges_has_module_actions` VALUES (1,1,1),(2,2,2),(3,2,3),(4,2,4),(5,2,5),(6,2,6),(7,2,7),(8,2,8),(9,91,92),(10,90,90),(11,9,93),(12,9,94),(13,9,95),(14,2,96),(15,2,97),(16,1,98),(17,1,109),(18,1,108),(19,1,107),(20,1,106),(21,1,105),(22,1,104),(23,1,103),(24,1,102),(25,1,101),(26,1,100),(27,1,99),(28,1,106),(29,1,110),(30,1,111),(31,1,112);
/*!40000 ALTER TABLE `qo_privileges_has_module_actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_sessions`
--

DROP TABLE IF EXISTS `qo_sessions`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_sessions` (
  `id` varchar(128) NOT NULL default '' COMMENT 'a randomly generated id',
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned default NULL COMMENT 'Group the member signed in under',
  `ip` varchar(16) default NULL,
  `date` datetime default NULL,
  PRIMARY KEY  (`id`,`qo_members_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_sessions`
--

LOCK TABLES `qo_sessions` WRITE;
/*!40000 ALTER TABLE `qo_sessions` DISABLE KEYS */;
INSERT INTO `qo_sessions` VALUES ('6d51c3c6ec21dc91c5b488900938ef94',232,10,'10.0.0.216','2009-04-17 17:14:27'),('c031452b5b691fb98d843a67558c11f8',259,10000,'10.0.0.216','2009-04-20 10:48:28'),('e972aabd58799167c8bf68fdc5c92005',259,10000,'10.0.0.216','2009-04-19 23:59:41'),('4bda8101595fbd945f9a56ab80ee7b78',259,10000,'10.0.0.216','2009-04-20 10:42:46'),('76e5e0297238fa608f759fa5f178f3a4',259,10000,'10.0.0.216','2009-04-20 00:00:06'),('37a2e6dd5621b36ab9c5d0ef5acadf4d',200,1000,'10.0.0.216','2009-04-20 00:01:37'),('a18d7bf915e4a8223c228855b3cc3713',259,10000,'10.0.0.216','2009-04-20 00:01:19'),('5663ef3d793ffc0e9b45455b593a71c7',259,10000,'10.0.0.216','2009-04-20 21:42:50'),('6168a84d755c16fb08a63103603b877a',259,10000,'10.0.0.216','2009-04-20 11:01:01'),('f7bf945f33941ee631ac8fad0e5c0fcf',259,10000,'10.0.0.216','2009-04-20 12:25:45'),('93551303bd1c180721b146f1746d8c3e',259,10000,'10.0.0.216','2009-04-22 13:47:06'),('269c1712289a3692e922b922c327a179',259,10000,'10.0.0.216','2009-04-24 17:59:48'),('a06ca950014372a6fc55132ec091b9cc',259,10000,'10.0.0.192','2009-04-21 15:51:48'),('0e6c767c48ccb44479ea55854d262a52',259,10000,'10.0.0.216','2009-04-22 22:27:41'),('b41e26747c278f5e3faf383b0a8b6589',259,10000,'10.0.0.216','2009-04-23 14:01:37'),('0556a4b8a4446502f57ecad59a3f4ee4',259,10000,'10.0.0.216','2009-04-23 14:42:52'),('046c6b7b154a0bb4425497818b3d87fe',259,10000,'10.0.0.196','2009-04-24 00:23:27'),('206559df7ca0a145672dd6897a4d2d83',259,10000,'10.0.0.216','2009-04-24 13:16:45'),('1c2633256100a171c0ea1acdc7158c1e',259,10000,'10.0.0.216','2009-04-24 14:13:00'),('288b497defa4669352de43ca91752bf4',259,10000,'10.0.0.216','2009-04-24 14:33:36'),('40d7d7f55a4dc8bb5aa758c2b140e938',259,10000,'10.0.0.216','2009-04-26 23:07:07'),('28230e5ff2fa90b7dda22dbf1b40b598',259,10000,'10.0.0.216','2009-04-24 19:24:46'),('33b53394cbc0cfba6c0ed0cc8422432e',259,10000,'10.0.0.216','2009-04-24 19:47:53'),('af1d30d63226cb259545dc288ccb169d',259,10000,'10.0.0.216','2009-04-25 22:24:12'),('db6ec2672f88ec39a173a78f0363c52f',259,10000,'10.0.0.216','2009-04-25 23:52:07'),('8b526d9fab2319c82e216720c645002e',259,10000,'10.0.0.216','2009-04-27 00:18:45'),('9bad84792d31930774f27d98df9a7b9c',259,10000,'10.0.0.216','2009-04-27 13:49:10'),('40167837611678f4efba733a6cd59703',259,10000,'10.0.0.220','2009-04-29 17:20:22'),('0a42b3da6801c6423c7c24b36a92298c',259,10000,'10.0.0.220','2009-04-29 17:22:56'),('eec1dec8b8443c2328fe33b0ddafe003',259,10000,'127.0.0.1','2009-05-05 12:07:06'),('830ccd21bea9ade7ddcb4c76b000ba1e',259,10000,'10.0.0.216','2009-05-05 13:41:25'),('50876f9406168dc1127fd27a09e7a304',259,10000,'10.0.0.216','2009-05-05 13:41:58'),('ce30f2d718ff254f2a5ca31f79b61d70',259,10000,'10.0.0.216','2009-05-05 15:53:56'),('f06b3eefc00bbc163df86a0ed36eba79',259,10000,'127.0.0.1','2009-05-05 18:00:07'),('d1b930747bc0645e251bcba1f511aad7',259,10000,'10.0.0.216','2009-05-05 22:12:58'),('5b803c4de7388eb3933e07e4ddc61092',259,10000,'10.0.0.216','2009-05-05 22:13:44'),('d518bebf29a5add76b14af4bf526483f',266,10000,'10.0.0.216','2009-05-06 14:52:20'),('4af3883843cd5cb622c207546369c786',267,10000,'10.0.0.216','2009-05-07 15:15:56'),('aaffcbcf80a0b1562ce54710d03b52a2',267,10000,'10.0.0.216','2009-05-06 19:40:45'),('91629119f3c8d609b626c2927f63a951',267,10000,'10.0.0.216','2009-05-06 19:47:35'),('0d945b6213ca5b13a3b210affb08573d',267,10000,'10.0.0.216','2009-05-06 21:09:02'),('903f9ef96af83f5ac21587998ba5b5ec',267,10000,'10.0.0.216','2009-05-06 21:19:17'),('7600285ab9bfeadf74a41ebda400efe6',268,10000,'10.0.0.216','2009-05-07 13:51:19');
/*!40000 ALTER TABLE `qo_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_styles`
--

DROP TABLE IF EXISTS `qo_styles`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_styles` (
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
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_styles`
--

LOCK TABLES `qo_styles` WRITE;
/*!40000 ALTER TABLE `qo_styles` DISABLE KEYS */;
INSERT INTO `qo_styles` VALUES (232,9999,1,12,'ffffff','0',100,'center'),(231,1,1,12,'ffffff','0',100,'tile'),(200,1000,1,4,'f9f9f9','FCF8F8',100,'tile'),(230,2,2,8,'ffffff','2D53DB',100,'center'),(233,2,1,1,'ffffff','0',100,'center'),(234,2,1,1,'ffffff','0',100,'center'),(235,1000,1,1,'ffffff','0',100,'center'),(236,10000,1,11,'ffffff','0',100,'center'),(237,10000,3,4,'ffffff','F8E8E8',77,'tile'),(238,10000,1,1,'ffffff','0',100,'center'),(242,10000,1,10,'ffffff','fcf8f8',100,'center'),(243,10000,1,10,'ffffff','fcf8f8',100,'center'),(244,10000,1,10,'ffffff','fcf8f8',100,'center'),(245,10000,1,10,'ffffff','fcf8f8',100,'center'),(246,10000,1,10,'ffffff','fcf8f8',100,'center'),(247,10000,1,10,'ffffff','fcf8f8',100,'center'),(248,10000,1,10,'ffffff','fcf8f8',100,'center'),(249,10000,1,10,'ffffff','fcf8f8',100,'center'),(250,10000,1,10,'ffffff','fcf8f8',100,'center'),(251,10000,1,10,'ffffff','fcf8f8',100,'center'),(252,10000,1,10,'ffffff','fcf8f8',100,'center'),(253,10000,1,10,'ffffff','fcf8f8',100,'center'),(254,10000,1,10,'ffffff','fcf8f8',100,'center'),(255,10000,1,10,'ffffff','fcf8f8',100,'center'),(256,10000,1,10,'ffffff','fcf8f8',100,'center'),(257,10000,1,10,'ffffff','fcf8f8',100,'center'),(258,10000,1,10,'ffffff','fcf8f8',100,'center'),(259,10000,1,4,'ffffff','E0EBE2',100,'tile'),(260,10000,1,10,'ffffff','fcf8f8',100,'center'),(261,10000,1,10,'ffffff','fcf8f8',100,'center'),(262,10000,1,10,'ffffff','fcf8f8',100,'center'),(263,10000,1,10,'ffffff','fcf8f8',100,'center'),(264,10000,1,10,'ffffff','fcf8f8',100,'center'),(265,10000,1,4,'ffffff','fcf8f8',100,'center'),(266,10000,1,10,'ffffff','fcf8f8',100,'center'),(267,10000,1,10,'ffffff','fcf8f8',100,'center'),(268,10000,1,10,'ffffff','fcf8f8',100,'center');
/*!40000 ALTER TABLE `qo_styles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_themes`
--

DROP TABLE IF EXISTS `qo_themes`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_themes` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL COMMENT 'The display name',
  `author` varchar(55) default NULL,
  `version` varchar(25) default NULL,
  `url` varchar(255) default NULL COMMENT 'Url which provides additional information',
  `path_to_thumbnail` varchar(255) default NULL,
  `path_to_file` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_themes`
--

LOCK TABLES `qo_themes` WRITE;
/*!40000 ALTER TABLE `qo_themes` DISABLE KEYS */;
INSERT INTO `qo_themes` VALUES (1,'Vista Blue','Todd Murdock','0.8',NULL,'xtheme-vistablue/xtheme-vistablue.png','xtheme-vistablue/css/xtheme-vistablue.css'),(2,'Vista Black','Todd Murdock','0.8',NULL,'xtheme-vistablack/xtheme-vistablack.png','xtheme-vistablack/css/xtheme-vistablack.css'),(3,'Vista Glass','Todd Murdock','0.8',NULL,'xtheme-vistaglass/xtheme-vistaglass.png','xtheme-vistaglass/css/xtheme-vistaglass.css');
/*!40000 ALTER TABLE `qo_themes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qo_wallpapers`
--

DROP TABLE IF EXISTS `qo_wallpapers`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `qo_wallpapers` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL COMMENT 'Display name',
  `author` varchar(55) default NULL,
  `url` varchar(255) default NULL COMMENT 'Url which provides information',
  `path_to_thumbnail` varchar(255) default NULL,
  `path_to_file` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `qo_wallpapers`
--

LOCK TABLES `qo_wallpapers` WRITE;
/*!40000 ALTER TABLE `qo_wallpapers` DISABLE KEYS */;
INSERT INTO `qo_wallpapers` VALUES (1,'qWikiOffice','Todd Murdock',NULL,'thumbnails/qwikioffice.jpg','qwikioffice.jpg'),(2,'Colorado Farm',NULL,NULL,'thumbnails/colorado-farm.jpg','colorado-farm.jpg'),(3,'Curls On Green',NULL,NULL,'thumbnails/curls-on-green.jpg','curls-on-green.jpg'),(4,'Emotion',NULL,NULL,'thumbnails/emotion.jpg','emotion.jpg'),(5,'Eos',NULL,NULL,'thumbnails/eos.jpg','eos.jpg'),(6,'Fields of Peace',NULL,NULL,'thumbnails/fields-of-peace.jpg','fields-of-peace.jpg'),(7,'Fresh Morning',NULL,NULL,'thumbnails/fresh-morning.jpg','fresh-morning.jpg'),(8,'Ladybuggin',NULL,NULL,'thumbnails/ladybuggin.jpg','ladybuggin.jpg'),(9,'Summer',NULL,NULL,'thumbnails/summer.jpg','summer.jpg'),(10,'Blue Swirl',NULL,NULL,'thumbnails/blue-swirl.jpg','blue-swirl.jpg'),(11,'Blue Psychedelic',NULL,NULL,'thumbnails/blue-psychedelic.jpg','blue-psychedelic.jpg'),(12,'Blue Curtain',NULL,NULL,'thumbnails/blue-curtain.jpg','blue-curtain.jpg'),(13,'Blank',NULL,NULL,'thumbnails/blank.gif','blank.gif');
/*!40000 ALTER TABLE `qo_wallpapers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `templatemodule`
--

DROP TABLE IF EXISTS `templatemodule`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `templatemodule` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `firstName` varchar(25) default NULL,
  `lastName` varchar(35) default NULL,
  `emailAddress` varchar(55) default NULL,
  `password` varchar(15) default NULL,
  `active` set('false','true') NOT NULL default 'false',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `templatemodule`
--

LOCK TABLES `templatemodule` WRITE;
/*!40000 ALTER TABLE `templatemodule` DISABLE KEYS */;
/*!40000 ALTER TABLE `templatemodule` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-05-07  7:19:48
