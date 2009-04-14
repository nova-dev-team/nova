/*
MySQL Data Transfer
Source Host: localhost
Source Database: qwikioffice-distro
Target Host: localhost
Target Database: qwikioffice-distro
Date: 10/29/2008 3:29:22 AM
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for qo_dependencies
-- ----------------------------
DROP TABLE IF EXISTS `qo_dependencies`;
CREATE TABLE `qo_dependencies` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `directory` varchar(255) default '' COMMENT 'The directory within the modules directory stated in the system/os/config.php',
  `file` varchar(255) default NULL COMMENT 'The file that contains the dependency',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_domains
-- ----------------------------
DROP TABLE IF EXISTS `qo_domains`;
CREATE TABLE `qo_domains` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `is_singular` tinyint(1) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_domains_has_modules
-- ----------------------------
DROP TABLE IF EXISTS `qo_domains_has_modules`;
CREATE TABLE `qo_domains_has_modules` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_domains_id` int(11) unsigned default NULL,
  `qo_modules_id` int(11) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_error_log
-- ----------------------------
DROP TABLE IF EXISTS `qo_error_log`;
CREATE TABLE `qo_error_log` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `text` text,
  `timestamp` datetime default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=188 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_groups
-- ----------------------------
DROP TABLE IF EXISTS `qo_groups`;
CREATE TABLE `qo_groups` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `importance` int(3) unsigned default '1',
  `active` tinyint(1) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_groups_has_domain_privileges
-- ----------------------------
DROP TABLE IF EXISTS `qo_groups_has_domain_privileges`;
CREATE TABLE `qo_groups_has_domain_privileges` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_groups_id` int(11) unsigned default '0',
  `qo_domains_id` int(11) unsigned default '0',
  `qo_privileges_id` int(11) unsigned default '0',
  `is_allowed` tinyint(1) unsigned default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_groups_has_members
-- ----------------------------
DROP TABLE IF EXISTS `qo_groups_has_members`;
CREATE TABLE `qo_groups_has_members` (
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member currently active in this group',
  `admin` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member the administrator of this group',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_launchers
-- ----------------------------
DROP TABLE IF EXISTS `qo_launchers`;
CREATE TABLE `qo_launchers` (
  `id` int(2) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_members
-- ----------------------------
DROP TABLE IF EXISTS `qo_members`;
CREATE TABLE `qo_members` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `first_name` varchar(25) default NULL,
  `last_name` varchar(35) default NULL,
  `email_address` varchar(55) default NULL,
  `password` varchar(15) default NULL,
  `language` varchar(5) default 'en',
  `active` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is the member currently active',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_members_has_module_launchers
-- ----------------------------
DROP TABLE IF EXISTS `qo_members_has_module_launchers`;
CREATE TABLE `qo_members_has_module_launchers` (
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned NOT NULL default '0',
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `qo_launchers_id` int(10) unsigned NOT NULL default '0',
  `sort_order` int(5) unsigned NOT NULL default '0' COMMENT 'sort within each launcher',
  PRIMARY KEY  (`qo_members_id`,`qo_groups_id`,`qo_modules_id`,`qo_launchers_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_modules
-- ----------------------------
DROP TABLE IF EXISTS `qo_modules`;
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
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_modules_actions
-- ----------------------------
DROP TABLE IF EXISTS `qo_modules_actions`;
CREATE TABLE `qo_modules_actions` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_modules_id` int(11) unsigned default NULL,
  `name` varchar(35) default NULL,
  `description` text,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_modules_files
-- ----------------------------
DROP TABLE IF EXISTS `qo_modules_files`;
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

-- ----------------------------
-- Table structure for qo_modules_has_dependencies
-- ----------------------------
DROP TABLE IF EXISTS `qo_modules_has_dependencies`;
CREATE TABLE `qo_modules_has_dependencies` (
  `qo_modules_id` int(11) unsigned NOT NULL default '0',
  `qo_dependencies_id` int(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (`qo_modules_id`,`qo_dependencies_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_privileges
-- ----------------------------
DROP TABLE IF EXISTS `qo_privileges`;
CREATE TABLE `qo_privileges` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `name` varchar(35) default NULL,
  `description` text,
  `is_singular` tinyint(1) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_privileges_has_module_actions
-- ----------------------------
DROP TABLE IF EXISTS `qo_privileges_has_module_actions`;
CREATE TABLE `qo_privileges_has_module_actions` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `qo_privileges_id` int(11) unsigned default NULL,
  `qo_modules_actions_id` int(11) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_sessions
-- ----------------------------
DROP TABLE IF EXISTS `qo_sessions`;
CREATE TABLE `qo_sessions` (
  `id` varchar(128) NOT NULL default '' COMMENT 'a randomly generated id',
  `qo_members_id` int(11) unsigned NOT NULL default '0',
  `qo_groups_id` int(11) unsigned default NULL COMMENT 'Group the member signed in under',
  `ip` varchar(16) default NULL,
  `date` datetime default NULL,
  PRIMARY KEY  (`id`,`qo_members_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for qo_styles
-- ----------------------------
DROP TABLE IF EXISTS `qo_styles`;
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

-- ----------------------------
-- Table structure for qo_themes
-- ----------------------------
DROP TABLE IF EXISTS `qo_themes`;
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

-- ----------------------------
-- Table structure for qo_wallpapers
-- ----------------------------
DROP TABLE IF EXISTS `qo_wallpapers`;
CREATE TABLE `qo_wallpapers` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(25) default NULL COMMENT 'Display name',
  `author` varchar(55) default NULL,
  `url` varchar(255) default NULL COMMENT 'Url which provides information',
  `path_to_thumbnail` varchar(255) default NULL,
  `path_to_file` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `qo_domains` VALUES ('1', 'All Modules', 'All the modules', '0');
INSERT INTO `qo_domains` VALUES ('2', 'QoPreferences', 'The QoPreferences module', '1');
INSERT INTO `qo_domains_has_modules` VALUES ('1', '1', '1');
INSERT INTO `qo_domains_has_modules` VALUES ('2', '1', '2');
INSERT INTO `qo_domains_has_modules` VALUES ('3', '1', '3');
INSERT INTO `qo_domains_has_modules` VALUES ('4', '1', '4');
INSERT INTO `qo_domains_has_modules` VALUES ('5', '1', '5');
INSERT INTO `qo_domains_has_modules` VALUES ('6', '1', '8');
INSERT INTO `qo_domains_has_modules` VALUES ('7', '2', '1');
INSERT INTO `qo_groups` VALUES ('1', 'administrator', 'System administrator', '100', '1');
INSERT INTO `qo_groups` VALUES ('2', 'user', 'General user', '50', '1');
INSERT INTO `qo_groups` VALUES ('3', 'demo', 'Demo user', '1', '1');
INSERT INTO `qo_groups_has_domain_privileges` VALUES ('1', '3', '1', '1', '1');
INSERT INTO `qo_groups_has_domain_privileges` VALUES ('2', '3', '2', '2', '1');
INSERT INTO `qo_groups_has_members` VALUES ('3', '3', '1', '1');
INSERT INTO `qo_groups_has_members` VALUES ('1', '4', '1', '1');
INSERT INTO `qo_launchers` VALUES ('1', 'autorun');
INSERT INTO `qo_launchers` VALUES ('2', 'contextmenu');
INSERT INTO `qo_launchers` VALUES ('3', 'quickstart');
INSERT INTO `qo_launchers` VALUES ('4', 'shortcut');
INSERT INTO `qo_members` VALUES ('3', 'Todd', 'Murdock', 'demo', 'demo', 'en', '1');
INSERT INTO `qo_members` VALUES ('4', 'Todd', 'Murdock', 'info@qwikioffice.com', 'Todd', 'en', '1');
INSERT INTO `qo_members_has_module_launchers` VALUES ('0', '0', '1', '2', '0');
INSERT INTO `qo_members_has_module_launchers` VALUES ('3', '3', '1', '3', '1');
INSERT INTO `qo_members_has_module_launchers` VALUES ('3', '3', '4', '3', '0');
INSERT INTO `qo_members_has_module_launchers` VALUES ('3', '3', '2', '4', '5');
INSERT INTO `qo_members_has_module_launchers` VALUES ('3', '3', '3', '4', '4');
INSERT INTO `qo_members_has_module_launchers` VALUES ('3', '3', '8', '4', '3');
INSERT INTO `qo_members_has_module_launchers` VALUES ('3', '3', '5', '4', '2');
INSERT INTO `qo_members_has_module_launchers` VALUES ('3', '3', '4', '4', '1');
INSERT INTO `qo_members_has_module_launchers` VALUES ('3', '3', '1', '4', '0');
INSERT INTO `qo_modules` VALUES ('1', 'Todd Murdock', '1.0', 'http://www.qwikioffice.com', 'A system application.  Allows users to set, and save their desktop preferences to the database.', 'system/preferences', 'qo-preferences', '1', '1');
INSERT INTO `qo_modules` VALUES ('2', 'Jack Slocum', '1.0', 'http://www.qwikioffice.com', 'Demo of window with grid.', 'demo', 'demo-grid', '1', '1');
INSERT INTO `qo_modules` VALUES ('3', 'Jack Slocum', '1.0', 'http://www.qwikioffice.com', 'Demo of window with tabs.', 'demo', 'demo-tabs', '1', '1');
INSERT INTO `qo_modules` VALUES ('4', 'Jack Slocum', '1.0', 'http://www.qwikioffice.com', 'Demo of window with accordion.', 'demo', 'demo-acc', '1', '1');
INSERT INTO `qo_modules` VALUES ('5', 'Jack Slocum', '1.0', 'http://www.qwikioffice.com', 'Demo of window with layout.', 'demo', 'demo-layout', '1', '1');
INSERT INTO `qo_modules` VALUES ('8', 'Jack Slocum', '1.0', 'http://www.qwikioffice.com', 'Demo of bogus window.', 'demo', 'demo-bogus', '1', '1');
INSERT INTO `qo_modules_actions` VALUES ('1', '0', 'loadModule', 'Allow the user to load the module.  Give them access to it.  Does not belong to any particular module');
INSERT INTO `qo_modules_actions` VALUES ('2', '1', 'saveAppearance', 'Save appearance');
INSERT INTO `qo_modules_actions` VALUES ('3', '1', 'saveAutorun', 'Save autorun');
INSERT INTO `qo_modules_actions` VALUES ('4', '1', 'saveBackground', 'Save background');
INSERT INTO `qo_modules_actions` VALUES ('5', '1', 'saveQuickstart', 'Save quickstart');
INSERT INTO `qo_modules_actions` VALUES ('6', '1', 'saveShortcut', 'Save shortcut');
INSERT INTO `qo_modules_actions` VALUES ('7', '1', 'viewThemes', 'View themes');
INSERT INTO `qo_modules_actions` VALUES ('8', '1', 'viewWallpapers', 'View wallpapers');
INSERT INTO `qo_modules_files` VALUES ('1', 'qo-preferences/', 'qo-preferences-override.js', '0', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('4', 'acc-win/', 'acc-win-override.js', '0', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('5', 'layout-win/', 'layout-win-override.js', '0', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('8', 'bogus/bogus-win/', 'bogus-win-override.js', '0', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('2', 'grid-win/', 'grid-win-override.js', '0', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('3', 'tab-win/', 'tab-win-override.js', '0', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('1', 'qo-preferences/', 'qo-preferences.js', '0', '0', '1', 'QoDesk.QoPreferences');
INSERT INTO `qo_modules_files` VALUES ('1', 'qo-preferences/', 'qo-preferences.php', '0', '1', '0', 'QoPreferences');
INSERT INTO `qo_modules_files` VALUES ('2', 'grid-win/', 'grid-win.js', '0', '0', '1', 'QoDesk.GridWindow');
INSERT INTO `qo_modules_files` VALUES ('3', 'tab-win/', 'tab-win.js', '0', '0', '1', 'QoDesk.TabWindow');
INSERT INTO `qo_modules_files` VALUES ('4', 'acc-win/', 'acc-win.js', '0', '0', '1', 'QoDesk.AccordionWindow');
INSERT INTO `qo_modules_files` VALUES ('5', 'layout-win/', 'layout-win.js', '0', '0', '1', 'QoDesk.LayoutWindow');
INSERT INTO `qo_modules_files` VALUES ('8', 'bogus/bogus-win/', 'bogus-win.js', '0', '0', '1', 'QoDesk.BogusWindow');
INSERT INTO `qo_modules_files` VALUES ('1', 'qo-preferences/', 'qo-preferences.css', '1', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('2', 'grid-win/', 'grid-win.css', '1', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('3', 'tab-win/', 'tab-win.css', '1', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('4', 'acc-win/', 'acc-win.css', '1', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('5', 'layout-win/', 'layout-win.css', '1', '0', '0', '');
INSERT INTO `qo_modules_files` VALUES ('8', 'bogus/bogus-win/', 'bogus-win.css', '1', '0', '0', '');
INSERT INTO `qo_privileges` VALUES ('1', 'Load Module', 'Allows the user access to the loadModule action', '0');
INSERT INTO `qo_privileges` VALUES ('2', 'QoPreferences', 'Allows the user access to all the actions of the QoPreferences mdoule', '1');
INSERT INTO `qo_privileges_has_module_actions` VALUES ('1', '1', '1');
INSERT INTO `qo_privileges_has_module_actions` VALUES ('2', '2', '2');
INSERT INTO `qo_privileges_has_module_actions` VALUES ('3', '2', '3');
INSERT INTO `qo_privileges_has_module_actions` VALUES ('4', '2', '4');
INSERT INTO `qo_privileges_has_module_actions` VALUES ('5', '2', '5');
INSERT INTO `qo_privileges_has_module_actions` VALUES ('6', '2', '6');
INSERT INTO `qo_privileges_has_module_actions` VALUES ('7', '2', '7');
INSERT INTO `qo_privileges_has_module_actions` VALUES ('8', '2', '8');
INSERT INTO `qo_sessions` VALUES ('5952e83912a4c5b176c23a12654335dd', '3', '3', '127.0.0.1', '2008-10-23 03:17:30');
INSERT INTO `qo_sessions` VALUES ('2f1fabae41cb5161ccd80876eded441a', '3', '3', '127.0.0.1', '2008-10-23 05:23:21');
INSERT INTO `qo_sessions` VALUES ('cbf69770fb2c05c1224a1a3e8806cafc', '3', '3', '127.0.0.1', '2008-10-23 05:52:16');
INSERT INTO `qo_sessions` VALUES ('7222e00389238b129e386d4e39d18df3', '3', '3', '127.0.0.1', '2008-10-24 23:56:46');
INSERT INTO `qo_sessions` VALUES ('ad17be8c78cbc3a1d9c69067c2a3fa9c', '3', '3', '127.0.0.1', '2008-10-25 03:30:54');
INSERT INTO `qo_sessions` VALUES ('b57f97755605f38adc9cb85fe644b16a', '3', '3', '127.0.0.1', '2008-10-25 13:09:30');
INSERT INTO `qo_sessions` VALUES ('42532ef1c2c63fe154feb3b01a8a28c8', '3', '3', '127.0.0.1', '2008-10-27 20:04:55');
INSERT INTO `qo_sessions` VALUES ('3a0a6c10b14ea9edf6d3d7c19cb7f037', '3', '3', '127.0.0.1', '2008-10-29 03:27:14');
INSERT INTO `qo_styles` VALUES ('0', '0', '2', '1', 'f9f9f9', '000000', '100', 'center');
INSERT INTO `qo_styles` VALUES ('3', '3', '3', '11', '390A0A', 'FFFFFF', '100', 'tile');
INSERT INTO `qo_themes` VALUES ('1', 'Vista Blue', 'Todd Murdock', '0.8', null, 'xtheme-vistablue/xtheme-vistablue.png', 'xtheme-vistablue/css/xtheme-vistablue.css');
INSERT INTO `qo_themes` VALUES ('2', 'Vista Black', 'Todd Murdock', '0.8', null, 'xtheme-vistablack/xtheme-vistablack.png', 'xtheme-vistablack/css/xtheme-vistablack.css');
INSERT INTO `qo_themes` VALUES ('3', 'Vista Glass', 'Todd Murdock', '0.8', null, 'xtheme-vistaglass/xtheme-vistaglass.png', 'xtheme-vistaglass/css/xtheme-vistaglass.css');
INSERT INTO `qo_wallpapers` VALUES ('1', 'qWikiOffice', 'Todd Murdock', null, 'thumbnails/qwikioffice.jpg', 'qwikioffice.jpg');
INSERT INTO `qo_wallpapers` VALUES ('2', 'Colorado Farm', null, null, 'thumbnails/colorado-farm.jpg', 'colorado-farm.jpg');
INSERT INTO `qo_wallpapers` VALUES ('3', 'Curls On Green', null, null, 'thumbnails/curls-on-green.jpg', 'curls-on-green.jpg');
INSERT INTO `qo_wallpapers` VALUES ('4', 'Emotion', null, null, 'thumbnails/emotion.jpg', 'emotion.jpg');
INSERT INTO `qo_wallpapers` VALUES ('5', 'Eos', null, null, 'thumbnails/eos.jpg', 'eos.jpg');
INSERT INTO `qo_wallpapers` VALUES ('6', 'Fields of Peace', null, null, 'thumbnails/fields-of-peace.jpg', 'fields-of-peace.jpg');
INSERT INTO `qo_wallpapers` VALUES ('7', 'Fresh Morning', null, null, 'thumbnails/fresh-morning.jpg', 'fresh-morning.jpg');
INSERT INTO `qo_wallpapers` VALUES ('8', 'Ladybuggin', null, null, 'thumbnails/ladybuggin.jpg', 'ladybuggin.jpg');
INSERT INTO `qo_wallpapers` VALUES ('9', 'Summer', null, null, 'thumbnails/summer.jpg', 'summer.jpg');
INSERT INTO `qo_wallpapers` VALUES ('10', 'Blue Swirl', null, null, 'thumbnails/blue-swirl.jpg', 'blue-swirl.jpg');
INSERT INTO `qo_wallpapers` VALUES ('11', 'Blue Psychedelic', null, null, 'thumbnails/blue-psychedelic.jpg', 'blue-psychedelic.jpg');
INSERT INTO `qo_wallpapers` VALUES ('12', 'Blue Curtain', null, null, 'thumbnails/blue-curtain.jpg', 'blue-curtain.jpg');
INSERT INTO `qo_wallpapers` VALUES ('13', 'Blank', null, null, 'thumbnails/blank.gif', 'blank.gif');
