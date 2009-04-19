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
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`currentlyloggedin`
--

/*!40000 ALTER TABLE `currentlyloggedin` DISABLE KEYS */;
LOCK TABLES `currentlyloggedin` WRITE;
INSERT INTO `nova_front`.`currentlyloggedin` VALUES  (10,'6d51c3c6ec21dc91c5b488900938ef94','232','10'),
 (21,'a91aad3431077e5f4206f4096c81f28b','237','10000'),
 (23,'18c9f3e92e7596bdc38d73313fd827d5','259','10000');
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
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_domains_has_modules`
--

/*!40000 ALTER TABLE `qo_domains_has_modules` DISABLE KEYS */;
LOCK TABLES `qo_domains_has_modules` WRITE;
INSERT INTO `nova_front`.`qo_domains_has_modules` VALUES  (1,1,1),
 (2,1,2),
 (3,1,3),
 (4,1,4),
 (5,1,5),
 (6,1,8),
 (7,2,1),
 (8,1,90),
 (9,200,90),
 (10,11,2),
 (11,11,4),
 (12,11,8),
 (13,10,5),
 (14,10,3),
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
 (38,2,104);
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_error_log`
--

/*!40000 ALTER TABLE `qo_error_log` DISABLE KEYS */;
LOCK TABLES `qo_error_log` WRITE;
INSERT INTO `nova_front`.`qo_error_log` VALUES  (1,'Script: module.php, Method: find_files, Missing file: system/modules/superadmin-user-manager/superadmin-user-manager-override.js','2009-02-24 14:55:48'),
 (2,'Script: module.php, Method: find_files, Missing file: system/modules/superadmin-user-manager/superadmin-user-manager.css','2009-02-24 14:55:48'),
 (3,'Script: module.php, Method: find_files, Missing file: system/modules/superadmin-user-manager/superadmin-user-manager.js','2009-02-24 14:55:48'),
 (4,'Script: module.php, Method: find_files, Missing file: system/modules/superadmin-user-manager/superadmin-user-manager-override.js','2009-02-24 14:56:04'),
 (5,'Script: module.php, Method: find_files, Missing file: system/modules/superadmin-user-manager/superadmin-user-manager.css','2009-02-24 14:56:04'),
 (6,'Script: module.php, Method: find_files, Missing file: system/modules/superadmin-user-manager/superadmin-user-manager.js','2009-02-24 14:56:04'),
 (7,'Script: module.php, Method: find_files, Missing file: system/modules/user-manual/user-manual-override','2009-02-24 15:37:21'),
 (8,'Script: module.php, Method: find_files, Missing file: system/modules/user-manual/user-manual-override','2009-02-24 15:39:22');
INSERT INTO `nova_front`.`qo_error_log` VALUES  (9,'Script: module.php, Method: find_files, Missing file: system/modules/user-manual/user-manual-override','2009-02-24 15:39:46'),
 (10,'Script: module.php, Method: check_dependencies, Missing file: system/modules/vc-dependency/grid/filter/Filters.js','2009-02-26 05:46:41'),
 (11,'Script: module.php, Method: check_dependencies, Missing file: system/modules/vc-dependency/grid/filter/GridFilter.js','2009-02-26 05:46:41'),
 (12,'Script: module.php, Method: check_dependencies, Missing file: system/modules/vc-dependency/grid/filter/Filters.js','2009-02-26 05:46:50'),
 (13,'Script: module.php, Method: check_dependencies, Missing file: system/modules/vc-dependency/grid/filter/GridFilter.js','2009-02-26 05:46:50'),
 (14,'Script: module.php, Method: check_dependencies, Missing file: system/modules/vc-dependency/grid/filter/Filters.js','2009-02-26 05:47:17'),
 (15,'Script: module.php, Method: find_files, Missing file: system/modules/user-job-manager/user-job-manager.php','2009-04-19 17:44:51'),
 (16,'Script: module.php, Method: find_files, Missing file: system/modules/user-job-manager/user-job-manager.php','2009-04-19 17:44:54');
INSERT INTO `nova_front`.`qo_error_log` VALUES  (17,'Script: module.php, Method: find_files, Missing file: system/modules/user-job-manager/user-job-manager.php','2009-04-19 17:46:38'),
 (18,'Script: module.php, Method: find_files, Missing file: system/modules/user-job-manager/user-job-manager.php','2009-04-19 17:49:06'),
 (19,'Script: module.php, Method: find_files, Missing file: system/modules/user-job-manager/user-job-manager.php','2009-04-19 17:49:19'),
 (20,'Script: module.php, Method: find_files, Missing file: system/modules/user-job-manager/user-job-manager.php','2009-04-19 17:49:34');
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
 (10000,259,1,0);
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
) ENGINE=MyISAM AUTO_INCREMENT=260 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nova_front`.`qo_members`
--

/*!40000 ALTER TABLE `qo_members` DISABLE KEYS */;
LOCK TABLES `qo_members` WRITE;
INSERT INTO `nova_front`.`qo_members` VALUES  (200,'Santa','Zhang','santa','zhang','en',1),
 (232,'debug','debug','debug','debug','en',1),
 (231,'admin','admin','admin','admin','en',1),
 (237,'user','user','user','user','en',1),
 (238,'user2','user2','user2','user2','en',1),
 (239,'user10','user10','user10','user10','en',1),
 (240,'user102','user102','user102','user102','en',1),
 (241,'user1024','user1024','user1024','user1024','en',1),
 (242,'santaxxx','santaxxx','santaxxx','santaxxx','en',1),
 (243,'johny','johny','johny','johny','en',1),
 (244,'sdfsdfasdf','sdfsdfasdf','sdfsdfasdf','sdfsdfasdf','en',1),
 (245,'sammy','sammy','sammy','sammy','en',1),
 (246,'bigboy','bigboy','bigboy','bigboy','en',1),
 (247,'bigbig','bigbig','bigbig','bigbig','en',1),
 (248,'qwerty','qwerty','qwerty','qwerty','en',1),
 (249,'jimmy','jimmy','jimmy','jimmy','en',1),
 (250,'markden','markden','markden','markden','en',1),
 (251,'tomcat','tomcat','tomcat','tomcat','en',1),
 (252,'joonnnh','joonnnh','joonnnh','joonnnh','en',1),
 (253,'crappy1000','crappy1000','crappy1000','crappy1000','en',1);
INSERT INTO `nova_front`.`qo_members` VALUES  (254,'bigman','bigman','bigman','bigman','en',1),
 (255,'pppppppp','pppppppp','pppppppp','pppppppp','en',1),
 (256,'llllllllll','llllllllll','llllllllll','llllllllll','en',1),
 (257,'nvida','nvida','nvida','nvida','en',1),
 (258,'macjohn','macjohn','macjohn','macjohn','en',1),
 (259,'misamisa','misamisa','misamisa','misamisa','en',1);
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
 (200,1000,100,4,7),
 (200,1000,104,4,6),
 (200,1000,90,4,5),
 (231,1,3,4,2),
 (200,1000,3,4,4),
 (231,1,5,4,4),
 (200,1000,5,4,3),
 (200,1000,2,4,2),
 (200,1000,8,4,1),
 (200,1000,5,3,0),
 (200,1000,4,4,0),
 (230,10000,4,4,0),
 (232,10,3,4,0),
 (230,10000,1,3,0),
 (232,10,104,4,1),
 (232,10,5,4,2),
 (232,10,1,4,3),
 (232,10,90,4,4),
 (232,10,100,4,5),
 (232,10,101,4,6),
 (232,10,102,4,7),
 (232,10,103,4,8),
 (232,10,105,4,9),
 (232,10,8,4,10),
 (232,10,4,4,11),
 (232,10,2,4,12),
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
 (259,10000,105,4,0);
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
) ENGINE=MyISAM AUTO_INCREMENT=106 DEFAULT CHARSET=latin1;

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
 (105,'Santa Zhang',NULL,NULL,NULL,'user-job-manager','user-job-manager',1,1);
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
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=latin1;

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
INSERT INTO `nova_front`.`qo_modules_actions` VALUES  (109,105,'listCluster',NULL);
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
 (105,'user-job-manager/','user-job-manager.php',0,1,0,'UserJobManager');
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
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1;

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
 (27,1,99);
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
 ('a91aad3431077e5f4206f4096c81f28b',237,10000,'10.0.0.216','2009-04-19 21:06:17'),
 ('18c9f3e92e7596bdc38d73313fd827d5',259,10000,'10.0.0.216','2009-04-19 21:27:04');
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
 (259,10000,1,10,'ffffff','fcf8f8',100,'center');
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
