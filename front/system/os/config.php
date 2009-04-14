<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

// If you want all Error Reporting on, use this:
//ini_set('display_errors',1);
//error_reporting(E_ALL|E_STRICT);

// If you want to see Warning Messages and not Notice Messages, use this:
//ini_set('display_errors',1);
//error_reporting(E_ALL);

// If you want all Error Reporting off, use this:
error_reporting(0);

class config {
	// document root
	public $DOCUMENT_ROOT = '';
	
	// directories
	public $MODULES_DIR = 'system/modules/';
	public $THEMES_DIR = 'resources/themes/';
	public $WALLPAPERS_DIR = 'resources/wallpapers/';
	
	// login url
	public $LOGIN_URL = 'login.html';
	
	// local database
	public $DB_HOST = 'localhost';
	public $DB_USERNAME = 'grid';
	public $DB_PASSWORD = 'chinagrid';
	public $DB_NAME = 'nova_front';
	
	public function __construct(){
		$_SERVER['DOCUMENT_ROOT'] = str_replace('\\', '/', getcwd());
		$this->DOCUMENT_ROOT = $_SERVER['DOCUMENT_ROOT'].'/';
	}
}
?>