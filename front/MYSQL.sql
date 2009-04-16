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
CREATE TABLE  `nova_front`.`currentlyloggedin` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `session_id` varchar(45) NOT NULL,
  `member_id` varchar(45) NOT NULL,
  `group_id` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`currentlyloggedin` VALUES  (32,'529282793e595b21410fe25d47d3325c','200','1000'),
 (33,'30c2e1ef845ee35e94023e3178ab9430','200','1000'),
 (35,'f1aa44365097aabb749ddbfe3492bd8e','232','10'),
 (45,'003160f3b209fe3bd22c56cd2eebb034','232','10'),
 (47,'5649707a9b8924f8bbf9789ba08a2942','200','1000'),
 (66,'cd0e747e64e94cbbfb1e1861b9e6abdb','200','1000');
CREATE TABLE  `nova_front`.`qo_dependencies` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `directory` varchar(255) default '' COMMENT 'The directory within the modules directory stated in the system/os/config.php',
  `file` varchar(255) default NULL COMMENT 'The file that contains the dependency',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=109 DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_dependencies` VALUES  (100,'templateModule/','Ext.ux.AboutWindow.js'),
 (101,'vc-dependency/grid/filter/','Filter.js'),
 (102,'vc-dependency/grid/filter/','BooleanFilter.js'),
 (103,'vc-dependency/grid/filter/','ListFilter.js'),
 (104,'vc-dependency/grid/filter/','DateFilter.js'),
 (108,'vc-dependency/grid/','GridFilters.js'),
 (106,'vc-dependency/grid/filter/','NumericFilter.js'),
 (107,'vc-dependency/grid/filter/','StringFilter.js');
CREATE TABLE  `nova_front`.`qo_domains` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `is_singular` tinyint(1) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_domains` VALUES  (1,'All Modules','All the modules',0),
 (2,'QoPreferences','The QoPreferences module',1),
 (9,'superadminModules',NULL,0),
 (10,'adminModules','Modules for vc admin',0),
 (11,'userModules','Modules for vc users',0),
 (200,'TemplateModule','Basic Module template.',1);
CREATE TABLE  `nova_front`.`qo_domains_has_modules` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_domains_id` int(11) unsigned default NULL,
  `qo_modules_id` int(11) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=latin1;
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
CREATE TABLE  `nova_front`.`qo_error_log` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `text` text,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
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
 (14,'Script: module.php, Method: check_dependencies, Missing file: system/modules/vc-dependency/grid/filter/Filters.js','2009-02-26 05:47:17');
CREATE TABLE  `nova_front`.`qo_groups` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `importance` int(3) unsigned default '1',
  `active` tinyint(1) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=10001 DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_groups` VALUES  (1,'administrator','System administrator',50,1),
 (10000,'user','General user',20,1),
 (10,'debug',NULL,1000,1),
 (1000,'super_admin','The almighty',100,1);
CREATE TABLE  `nova_front`.`qo_groups_has_domain_privileges` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_groups_id` int(11) unsigned default '0',
  `qo_domains_id` int(11) unsigned default '0',
  `qo_privileges_id` int(11) unsigned default '0',
  `is_allowed` tinyint(1) unsigned default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=224 DEFAULT CHARSET=latin1;
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
CREATE TABLE  `nova_front`.`qo_groups_has_members` (
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member currently active in this group',
  `admin` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member the administrator of this group',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_groups_has_members` VALUES  (1,231,1,1),
 (10000,230,1,0),
 (1000,200,1,1),
 (10,232,1,1),
 (2,233,1,0),
 (2,234,1,0),
 (1000,235,1,0),
 (10000,236,1,0),
 (10000,237,1,0);
CREATE TABLE  `nova_front`.`qo_launchers` (
  `id` int(2) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_launchers` VALUES  (1,'autorun'),
 (2,'contextmenu'),
 (3,'quickstart'),
 (4,'shortcut');
CREATE TABLE  `nova_front`.`qo_members` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `first_name` varchar(25) default NULL,
  `last_name` varchar(35) default NULL,
  `email_address` varchar(55) default NULL,
  `password` varchar(15) default NULL,
  `language` varchar(5) default 'en',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member currently active',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=238 DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_members` VALUES  (200,'Santa','Zhang','santa','zhang','en',1),
 (232,'debug','debug','debug','debug','en',1),
 (231,'admin','admin','admin','admin','en',1),
 (237,'user','user','user','user','en',1);
CREATE TABLE  `nova_front`.`qo_members_has_module_launchers` (
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `qo_launchers_id` int(10) unsigned NOT NULL default '0',
  `sort_order` int(5) unsigned NOT NULL default '0' COMMENT 'sort within each launcher',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`,`qo_modules_id`,`qo_launchers_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
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
 (237,10000,105,4,0);
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
CREATE TABLE  `nova_front`.`qo_modules_actions` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_modules_id` int(11) unsigned default NULL,
  `name` varchar(35) default NULL,
  `description` text,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=latin1;
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
 (97,104,'updateAccount',NULL);
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
 (100,'superadmin-user-manager/','superadmin-user-manager.php',0,1,0,'SuperAdminUserManager');
CREATE TABLE  `nova_front`.`qo_modules_has_dependencies` (
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `qo_dependencies_id` int(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (`qo_modules_id`,`qo_dependencies_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_modules_has_dependencies` VALUES  (90,100),
 (100,101),
 (100,103),
 (100,108);
CREATE TABLE  `nova_front`.`qo_privileges` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `is_singular` tinyint(1) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_privileges` VALUES  (1,'Load Module','Allows the user access to the loadModule action',0),
 (2,'QoPreferences','Allows the user access to all the actions of the QoPreferences mdoule',1),
 (9,'superadminPrivilege',NULL,0),
 (10,'adminPrivilege',NULL,0),
 (11,'userPrivilege',NULL,0),
 (90,'accountPrivilege',NULL,0),
 (91,'TemplateModule','Allows the user access to the doTask action.',1);
CREATE TABLE  `nova_front`.`qo_privileges_has_module_actions` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_privileges_id` int(11) unsigned default NULL,
  `qo_modules_actions_id` int(11) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;
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
 (15,2,97);
CREATE TABLE  `nova_front`.`qo_sessions` (
  `id` varchar(128) NOT NULL default '' COMMENT 'a randomly generated id',
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned default NULL COMMENT 'Group the member signed in under',
  `ip` varchar(16) default NULL,
  `date` datetime default NULL,
  PRIMARY KEY  (`id`,`qo_members_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('5952e83912a4c5b176c23a12654335dd',3,3,'127.0.0.1','2008-10-23 03:17:30'),
 ('2f1fabae41cb5161ccd80876eded441a',3,3,'127.0.0.1','2008-10-23 05:23:21'),
 ('cbf69770fb2c05c1224a1a3e8806cafc',3,3,'127.0.0.1','2008-10-23 05:52:16'),
 ('7222e00389238b129e386d4e39d18df3',3,3,'127.0.0.1','2008-10-24 23:56:46'),
 ('ad17be8c78cbc3a1d9c69067c2a3fa9c',3,3,'127.0.0.1','2008-10-25 03:30:54'),
 ('b57f97755605f38adc9cb85fe644b16a',3,3,'127.0.0.1','2008-10-25 13:09:30'),
 ('42532ef1c2c63fe154feb3b01a8a28c8',3,3,'127.0.0.1','2008-10-27 20:04:55'),
 ('3a0a6c10b14ea9edf6d3d7c19cb7f037',3,3,'127.0.0.1','2008-10-29 03:27:14'),
 ('622c3b6364de3cf27b463ae2067bede2',5,2,'127.0.0.1','2009-02-22 15:43:45'),
 ('250c249ae45b6610b009879432dd9c0b',3,3,'127.0.0.1','2009-02-22 15:55:38'),
 ('84835e50bbedc657664263bed72c4ec0',5,2,'127.0.0.1','2009-02-24 06:52:34'),
 ('7145cb786d2458c6960b25dec708defd',5,2,'127.0.0.1','2009-02-24 06:52:44'),
 ('8eec2068216461676917a8167c3c96ca',218,2,'127.0.0.1','2009-02-24 06:28:22'),
 ('437ab1f10e0a9a9e2650111a44f0a334',5,2,'127.0.0.1','2009-02-24 06:53:43');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('853c8f5b6071a765e41df81ba67b8a0f',5,2,'127.0.0.1','2009-02-24 06:55:56'),
 ('fc5a91f2d0bb84a54c95c8940acac8ca',5,2,'127.0.0.1','2009-02-24 07:06:23'),
 ('55fca824678f46a80796cfb74ff14dc7',5,2,'127.0.0.1','2009-02-24 07:07:12'),
 ('1adeb0303b82d9594010f7846148c97e',218,2,'127.0.0.1','2009-02-24 07:19:16'),
 ('daedf0ab59c6568da40582494bebf859',218,2,'127.0.0.1','2009-02-24 07:19:16'),
 ('2304f70fe56da2acb3b5e4c113f99c5a',220,2,'127.0.0.1','2009-02-24 07:28:50'),
 ('bc5fb3106058dfc5fed4444a3c929d70',220,2,'127.0.0.1','2009-02-24 07:29:20'),
 ('9ce218bb3d6f5c974efcdc4d9dc5b9d2',218,2,'127.0.0.1','2009-02-24 07:24:17'),
 ('09838045f65ed2795179b0752a147434',218,2,'127.0.0.1','2009-02-24 07:24:17'),
 ('9dca24ff4523f62e549af87d66054fa6',218,2,'127.0.0.1','2009-02-24 07:24:42'),
 ('d258021ca642e5722b16236b835b0cdc',218,2,'127.0.0.1','2009-02-24 07:24:42'),
 ('358289971edcb3de3b6ea0c16212ee3e',220,2,'127.0.0.1','2009-02-24 07:30:16'),
 ('59eaff40c13249783e29737d0ccea836',220,2,'127.0.0.1','2009-02-24 07:31:21'),
 ('3e7c6025c2250f6f79988d4d2e7e4f21',220,2,'127.0.0.1','2009-02-24 07:31:54');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('b752bed4801b9ee9a3a5859b08d5fc10',220,2,'127.0.0.1','2009-02-24 07:32:23'),
 ('f05c981c35846b6c50b05d4b5a70819c',220,2,'127.0.0.1','2009-02-24 07:33:42'),
 ('21252a75c22d9bdda9f169e1b7de7dcf',220,2,'127.0.0.1','2009-02-24 07:33:42'),
 ('451b7f897700658465f1669b1f1776f0',220,2,'127.0.0.1','2009-02-24 07:36:49'),
 ('59aecc63bc11eff60c31619e08d800d9',220,2,'127.0.0.1','2009-02-24 07:36:49'),
 ('faeb3b4fd40864243136ef7eadbfc038',218,2,'127.0.0.1','2009-02-24 07:37:06'),
 ('ea728e1668645e2ffd3f86478b1fe070',218,2,'127.0.0.1','2009-02-24 07:37:06'),
 ('5485b4d97bb18262a57e9638f35856ce',218,2,'127.0.0.1','2009-02-24 07:45:59'),
 ('a9f331639874b87b362e69106ce5944e',218,2,'127.0.0.1','2009-02-24 07:45:59'),
 ('ee74a12d1dc9bd0a0fef977565765829',218,2,'127.0.0.1','2009-02-24 07:46:10'),
 ('1b289d34caf45088e62edf089eed5343',218,2,'127.0.0.1','2009-02-24 07:46:10'),
 ('d176a0b45a265c1854eae7596ca86b2e',224,2,'127.0.0.1','2009-02-24 07:50:07'),
 ('d311398f9009f0dd57dcb5cc512985c7',224,2,'127.0.0.1','2009-02-24 07:50:07'),
 ('010752de8b40cce305fe7777e24e7921',216,2,'127.0.0.1','2009-02-24 07:57:38');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('c9489aa4d9288cd6592f9d2cd2749563',216,2,'127.0.0.1','2009-02-24 07:57:54'),
 ('63e41688232d04694a857ab6a04c599b',216,2,'127.0.0.1','2009-02-24 07:58:19'),
 ('cc7f5dcbfad14d5ec724193651a7a2ee',216,2,'127.0.0.1','2009-02-24 07:58:19'),
 ('8cb34d458082170c8c5001e1a96e0aed',216,2,'127.0.0.1','2009-02-24 07:58:30'),
 ('ec017e6a4000fa3f8dd7e8efe47ebdc7',218,2,'10.0.0.192','2009-02-24 08:02:50'),
 ('e4273437a54bcd399d6b953cca5445d3',218,2,'10.0.0.192','2009-02-24 08:02:50'),
 ('9357384f93a12c4309f36aa1eb672484',216,2,'127.0.0.1','2009-02-24 08:07:47'),
 ('8e04946b098f24961e09261152ea0af3',216,2,'127.0.0.1','2009-02-24 08:07:47'),
 ('dc50d5d982ecdd1a0ccca6f2b32562a0',216,2,'127.0.0.1','2009-02-24 08:08:00'),
 ('751dd69fcd942647b3d7cc86daa52918',216,2,'127.0.0.1','2009-02-24 08:08:00'),
 ('bae95fa0cdefb67042c76ad9d5b2bcc6',227,2,'127.0.0.1','2009-02-24 08:29:16'),
 ('03c160f14e923508219b3ad1eff2d822',218,2,'127.0.0.1','2009-02-24 09:10:08'),
 ('3c24fa4d5246c2c7f75fc34b492c8541',227,2,'127.0.0.1','2009-02-24 08:31:18'),
 ('b274c11f95b9eceb5a4c17950beafc16',227,2,'127.0.0.1','2009-02-24 08:29:35');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('f792c1e6e2fc9bddcaa093652eccba2e',218,2,'127.0.0.1','2009-02-24 08:39:59'),
 ('1d4b9a6291e0ac2e29fb4b7503782c81',218,2,'127.0.0.1','2009-02-24 08:39:36'),
 ('f1c0114cac978b6486dd2ce252953369',218,2,'127.0.0.1','2009-02-24 08:39:36'),
 ('ba9ddf421e8a19ba490a22684ebbda54',218,2,'127.0.0.1','2009-02-24 08:39:59'),
 ('4aee80e3f8abe1a35fd53828cf95c91b',218,2,'127.0.0.1','2009-02-24 08:44:39'),
 ('2c38e2e8df24324e24a6272d16fbbcbc',218,2,'127.0.0.1','2009-02-24 08:44:39'),
 ('a7bd915c9270a2e18c4643a3b730d7de',221,1,'127.0.0.1','2009-02-24 09:18:19'),
 ('4ab3d0825a8748678da111ae02832617',218,2,'127.0.0.1','2009-02-24 08:53:12'),
 ('e3020afdd90b9658f6397f200b03ce76',218,2,'127.0.0.1','2009-02-24 08:53:12'),
 ('5c8746bbef1084ad61e5091da85c53ae',218,2,'127.0.0.1','2009-02-24 08:54:17'),
 ('d65ca801bed6189a6267d62ec69aa3e9',218,2,'127.0.0.1','2009-02-24 08:54:17'),
 ('ba84cfb960fb7a11d23f3a46fbd80f8b',218,2,'127.0.0.1','2009-02-24 08:57:32'),
 ('1e3f080f7c7350fc8aaef37ebc944fc5',218,2,'127.0.0.1','2009-02-24 08:57:32'),
 ('10d9302af387155eae01df45f3748a75',218,2,'127.0.0.1','2009-02-24 09:13:49');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('c562c3164aad144b903ead6c8f31875a',218,2,'127.0.0.1','2009-02-24 09:01:55'),
 ('09df163ef8c8030af60bc90c58a08f58',218,2,'127.0.0.1','2009-02-24 09:04:50'),
 ('ed07bada6942b826f44b1ef30d4815d7',218,2,'127.0.0.1','2009-02-24 09:10:08'),
 ('feaa9c814051cf3aa5534ca048a872be',218,2,'127.0.0.1','2009-02-24 09:11:22'),
 ('313e5962c5fa3e11187c3b6b16f5b066',218,2,'127.0.0.1','2009-02-24 09:11:22'),
 ('9a9dd670396581136de7928fe60f25bb',218,2,'127.0.0.1','2009-02-24 09:11:38'),
 ('454431db694c686b24a5ccdefe9b6844',218,2,'127.0.0.1','2009-02-24 09:11:38'),
 ('f26d9580f82fd0ebd5fed43cac646562',218,2,'127.0.0.1','2009-02-24 09:11:59'),
 ('ea5b7f0b8ca95f96affffbb7f236407a',218,2,'127.0.0.1','2009-02-24 09:11:59'),
 ('2573a2e33f13d0f6b44652b46e463409',218,2,'127.0.0.1','2009-02-24 09:13:35'),
 ('acbc39d7a2bebd5798b9fcf1ed433bcf',218,2,'127.0.0.1','2009-02-24 09:13:35'),
 ('49b08c7e43e0ec80d4b1c5350257e7b8',229,2,'127.0.0.1','2009-02-24 09:24:46'),
 ('d70441e4f55347adf64ab49a065c581a',229,2,'127.0.0.1','2009-02-24 09:27:12'),
 ('98a4f3dfdacf343421079f072728bc55',229,2,'127.0.0.1','2009-02-24 09:27:12');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('43235bf7a96fc437e60d6eeb888b3074',229,2,'127.0.0.1','2009-02-24 09:28:00'),
 ('1048818987dc4347eb31e8b248ee6dd1',229,2,'127.0.0.1','2009-02-24 09:28:00'),
 ('d964ce98d61d1eb967a06543b215ae20',230,10000,'127.0.0.1','2009-03-11 10:49:18'),
 ('35cd38b9a4c7cb974a14b845e5caabe8',229,2,'127.0.0.1','2009-02-24 09:30:19'),
 ('2f7344d2269a7f4470fad11e251fe70a',229,2,'127.0.0.1','2009-02-24 09:30:27'),
 ('f423b8d9c1a2aea26370fa3cc03a5e1d',229,2,'127.0.0.1','2009-02-24 09:30:27'),
 ('18f1792273798e795f792f3af0c3fee1',229,2,'127.0.0.1','2009-02-24 09:32:28'),
 ('c93313daaa1197b21545e4b6e0b6af1c',230,2,'127.0.0.1','2009-02-24 09:40:33'),
 ('003160f3b209fe3bd22c56cd2eebb034',232,10,'127.0.0.1','2009-03-11 10:49:15'),
 ('813c2653e31d90f4980cded5f3384a1f',231,1,'127.0.0.1','2009-03-11 10:49:00'),
 ('7b7d1388d3d2ee8058154562448c25c0',237,10000,'10.0.0.216','2009-04-16 20:39:53'),
 ('5649707a9b8924f8bbf9789ba08a2942',200,1000,'10.0.0.216','2009-03-26 08:59:42'),
 ('1ad89ce7aff3a32ac99d53c54ae0e168',230,2,'127.0.0.1','2009-02-24 15:45:55');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('9c383a9394dbfa2c99c7898dd648a10e',230,10000,'127.0.0.1','2009-03-11 07:21:20'),
 ('e3ee5377400fe8886af9d4bc36868e87',230,2,'127.0.0.1','2009-02-24 15:45:55'),
 ('1532793e94fa4349046d058248fe17dd',231,1,'127.0.0.1','2009-02-24 15:46:01'),
 ('ffd98b85dd3c9915fd96f2f0f0df71b8',231,1,'127.0.0.1','2009-02-24 15:46:01'),
 ('a8df9adc08bbad8c0bbf5c38645a2c89',231,1,'127.0.0.1','2009-02-25 02:52:35'),
 ('086300b374dd8d8fdff14e7d37f4ee28',237,10000,'10.0.0.216','2009-04-16 20:39:53'),
 ('dab1350040b7a1201bcb95c09aafb56a',231,1,'127.0.0.1','2009-02-25 02:52:35'),
 ('f1aa44365097aabb749ddbfe3492bd8e',232,10,'127.0.0.1','2009-03-04 06:10:18'),
 ('4e189de9a09b5de07f086cc0feb52b9c',231,1,'127.0.0.1','2009-02-25 03:23:46'),
 ('529282793e595b21410fe25d47d3325c',200,1000,'127.0.0.1','2009-02-27 05:14:33'),
 ('d59d3bcf147100e45977ebe9b28db44b',231,1,'127.0.0.1','2009-02-25 03:19:34'),
 ('b58cf6f1e6249fa53c2f15784a529596',231,1,'127.0.0.1','2009-02-25 03:19:34'),
 ('a1feb45a3550792457a7e8e75bbea008',230,2,'127.0.0.1','2009-02-25 03:19:43');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('d153117401baf617086d730054fbc28e',230,2,'127.0.0.1','2009-02-25 03:19:43'),
 ('30c2e1ef845ee35e94023e3178ab9430',200,1000,'10.0.0.216','2009-03-02 09:13:30'),
 ('ded4d07348869b027832e78973b58ec9',230,2,'127.0.0.1','2009-02-25 03:38:34'),
 ('8e43f40f8cd61b2574574d9540dabcaa',230,2,'127.0.0.1','2009-02-25 03:39:15'),
 ('e8aaab5369e2c10daae5b0db1006ca2d',230,2,'127.0.0.1','2009-02-25 03:39:33'),
 ('369f8bd8c538fc1b607ecb32ea6b90f7',200,1000,'127.0.0.1','2009-02-27 05:12:26'),
 ('caeb2f6164fb8217ec06fc1ad4d28011',230,10000,'127.0.0.1','2009-02-27 00:50:41'),
 ('763f2352b789e68c980cc23ea493e945',231,1,'127.0.0.1','2009-02-27 00:50:34'),
 ('c73b15cd4222ed160d377c9d9cae6fea',230,10000,'127.0.0.1','2009-02-25 13:54:02'),
 ('fb45d09ef658fe0c6a0d447e4b28ffd7',231,1,'127.0.0.1','2009-02-25 13:54:09'),
 ('f2fb80fb6983e23029d71ef3264f3dfd',231,1,'127.0.0.1','2009-02-25 13:54:09'),
 ('b7c31a7ae87c75427ede425e463ae231',232,10,'127.0.0.1','2009-02-25 13:54:19'),
 ('c61b8fc61a658981be049565a0c3ebe0',230,10000,'127.0.0.1','2009-03-11 06:57:48');
INSERT INTO `nova_front`.`qo_sessions` VALUES  ('926946dec203d368feebce62d8e648ed',231,1,'127.0.0.1','2009-02-25 13:54:51'),
 ('15fc389f1c4990d0171ac6c4c9c6953f',231,1,'127.0.0.1','2009-02-25 13:54:51'),
 ('2d88e5ca39a7c35f42a0af595e91c499',200,1000,'127.0.0.1','2009-02-27 00:50:18'),
 ('5748a64510507023096a1f09f2b8e6b6',231,1,'127.0.0.1','2009-02-25 13:56:14'),
 ('d5180da8336494c5a5fee4ec1cdbda14',200,1000,'127.0.0.1','2009-02-27 00:50:18'),
 ('c6c53ce1b89523d18b36c8930b01abe8',200,1000,'127.0.0.1','2009-02-26 13:53:00'),
 ('6c5a505b71026f7090f68d18787458f5',231,1,'127.0.0.1','2009-02-26 08:19:15'),
 ('b6c2ddc736f11007b9d7cb411e711004',230,10000,'10.0.0.216','2009-04-16 16:36:54'),
 ('4c3995ac709235556d6ae44f22a031f0',231,1,'10.0.0.216','2009-04-16 20:40:07'),
 ('cd0e747e64e94cbbfb1e1861b9e6abdb',200,1000,'10.0.0.216','2009-04-16 20:40:13');
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
INSERT INTO `nova_front`.`qo_styles` VALUES  (232,9999,1,12,'ffffff','0',100,'center'),
 (231,1,1,12,'ffffff','0',100,'tile'),
 (200,1000,1,10,'f9f9f9','FCF8F8',100,'tile'),
 (230,2,2,8,'ffffff','2D53DB',100,'center'),
 (233,2,1,1,'ffffff','0',100,'center'),
 (234,2,1,1,'ffffff','0',100,'center'),
 (235,1000,1,1,'ffffff','0',100,'center'),
 (236,10000,1,11,'ffffff','0',100,'center'),
 (237,10000,1,1,'ffffff','0',100,'center');
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
INSERT INTO `nova_front`.`qo_themes` VALUES  (1,'Vista Blue','Todd Murdock','0.8',NULL,'xtheme-vistablue/xtheme-vistablue.png','xtheme-vistablue/css/xtheme-vistablue.css'),
 (2,'Vista Black','Todd Murdock','0.8',NULL,'xtheme-vistablack/xtheme-vistablack.png','xtheme-vistablack/css/xtheme-vistablack.css'),
 (3,'Vista Glass','Todd Murdock','0.8',NULL,'xtheme-vistaglass/xtheme-vistaglass.png','xtheme-vistaglass/css/xtheme-vistaglass.css');
CREATE TABLE  `nova_front`.`qo_wallpapers` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL COMMENT 'Display name',
  `author` varchar(55) default NULL,
  `url` varchar(255) default NULL COMMENT 'Url which provides information',
  `path_to_thumbnail` varchar(255) default NULL,
  `path_to_file` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
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
CREATE TABLE  `nova_front`.`templatemodule` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `firstName` varchar(25) default NULL,
  `lastName` varchar(35) default NULL,
  `emailAddress` varchar(55) default NULL,
  `password` varchar(15) default NULL,
  `active` set('false','true') NOT NULL default 'false',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
