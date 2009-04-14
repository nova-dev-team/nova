-- phpMyAdmin SQL Dump
-- version 2.7.0-pl2
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Date: Sept 05, 2008 at 01:30 AM
-- Server version: 5.0.45
-- PHP Version: 4.4.7
-- 
-- Database: `qWikiOffice Desktop`
-- 

-- --------------------------------------------------------

-- 
-- Install the `templateModule` into QO
-- 

-- ----------------------------
-- Install structure for templateModule
-- ----------------------------

INSERT INTO `qo_modules` (
`id` ,
`author` ,
`version` ,
`url` ,
`description` ,
`module_type` ,
`module_id` ,
`active` ,
`load_on_demand` 
)
VALUES (
'90', 'templateModule Author', '0.0.1', 'http://www.qwikioffice.com', 'Basic Module Template.', 'templateModule', 'templateModule', '1', '1'
);

INSERT INTO `qo_modules_files` (
`qo_modules_id` ,
`directory` ,
`file` ,
`is_stylesheet` ,
`is_server_module` ,
`is_client_module` ,
`class_name` 
)
VALUES (
'90', 'templateModule/', 'templateModule-override.js', '0', '0', '0', ''
), (
'90', 'templateModule/', 'templateModule.js', '0', '0', '1', 'QoDesk.TemplateModule'
), (
'90', 'templateModule/', 'templateModule.php', '0', '1', '0', 'TemplateModule'
), (
'90', 'templateModule/', 'templateModule.css', '1', '0', '0', ''
);

INSERT INTO `qo_dependencies` (
`id` ,
`directory` ,
`file` 
)
VALUES (
'100' , 'templateModule/', 'Ext.ux.AboutWindow.js'
);

INSERT INTO `qo_modules_has_dependencies` (
`qo_modules_id` ,
`qo_dependencies_id` 
)
VALUES (
'90', '100'
);

INSERT INTO `qo_privileges` (
`id` ,
`name` ,
`description` ,
`is_singular` 
)
VALUES (
'91', 'TemplateModule', 'Allows the user access to the doTask action.', '1'
);

INSERT INTO `qo_modules_actions` (
`id` ,
`qo_modules_id` ,
`name` ,
`description` 
)
VALUES (
'92' , '90', 'doTask', 'Get or Save data, depending on what is sent thru "task" request.'
);

INSERT INTO `qo_privileges_has_module_actions` (
`id` ,
`qo_privileges_id` ,
`qo_modules_actions_id` 
)
VALUES (
NULL , '91', '92'
);

INSERT INTO `qo_domains` (
`id` ,
`name` ,
`description` ,
`is_singular` 
)
VALUES (
'200' , 'TemplateModule', 'Basic Module template.', '1'
);

INSERT INTO `qo_domains_has_modules` (
`id` ,
`qo_domains_id` ,
`qo_modules_id` 
)
VALUES (
NULL , '1', '90'
), (
NULL , '200', '90'
);

INSERT INTO `qo_groups_has_domain_privileges` (
`id` ,
`qo_groups_id` ,
`qo_domains_id` ,
`qo_privileges_id` ,
`is_allowed` 
)
VALUES (
NULL , '3', '200', '3', '1'
);

INSERT INTO `qo_members_has_module_launchers` (
`qo_members_id` ,
`qo_groups_id` ,
`qo_modules_id` ,
`qo_launchers_id` ,
`sort_order` 
)
VALUES (
'3', '3', '90', '3', '0'
);

-- End of QO Install of module.


-- 
-- Table structure for table `templateModule`
-- 

-- ----------------------------
-- Table structure for templateModule
-- ----------------------------
CREATE TABLE [IF NOT EXISTS] `templateModule` (
  `id` int(11) unsigned NOT NULL auto_increment,
  `firstName` varchar(25) default NULL,
  `lastName` varchar(35) default NULL,
  `emailAddress` varchar(55) default NULL,
  `password` varchar(15) default NULL,
  `active` set('false','true') NOT NULL default 'false',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
