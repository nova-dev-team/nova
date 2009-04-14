<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

require('config.php');
require('lib/error.php');
require('lib/group.php');
require('lib/launcher.php');
require('lib/member.php');
require('lib/module.php');
require('lib/preference.php');
require('lib/privilege.php');
require('lib/session.php');
require('lib/theme.php');

class os {	
	
	public $connected_to_db = false;
	
	
	
	public function __construct(){
		// initialise the $_SESSION superglobal array
		// session is destroyed in session->logout()
		session_start();
		
		// config
		if(class_exists('config')){ $this->config = new config(); }
			else{ die("Config class is missing!"); }
		
		// error
		if(class_exists('error')){ $this->error = new error($this); }
			else{ die("Error class is missing!"); }
		
		// group
		if(class_exists('group')){ $this->group = new group($this); }
			else{ die("Group class is missing!"); }
		
		// launcher
		if(class_exists('launcher')){ $this->launcher = new launcher($this); }
			else{ die("Launcher class is missing!"); }
		
		// member
		if(class_exists('member')){ $this->member = new member($this); }
			else{ die("Member class is missing!"); }
		
		// module
		if(class_exists('module')){ $this->module = new module($this); }
			else{ die("Module class is missing!"); }
		
		// privileges
		if(class_exists('privilege')){ $this->privilege = new privilege($this); }
			else{ die("Privilege class is missing!"); }
		
		// preference
		if(class_exists('preference')){ $this->preference = new preference($this); }
			else{ die("Preference class is missing!"); }
		
		// session
		if(class_exists('session')){ $this->session = new session($this); }
			else{ die("Session class is missing!"); }
		
		// theme
		if(class_exists('theme')){ $this->theme = new theme($this); }
			else{ die("Theme class is missing!"); }
		
		// json support
		if(!function_exists('json_encode')){
			require("lib/json.php");
			$GLOBALS['JSON_OBJECT'] = new Services_JSON();
			
			function json_encode($value){
				return $GLOBALS['JSON_OBJECT']->encode($value);
			}
   
			function json_decode($value){
				return $GLOBALS['JSON_OBJECT']->decode($value);
			}
		}
		
		// connect to the database
		$this->connect_to_db();

	}
	
	
	
	/** init() Initial page load or refresh has occured 
	  **/
	public function init(){
		$this->module->init();
		$this->privilege->init();
	}
	

	
	/** connect_to_db()
	  * 
	  * @access private
	  **/
	private function connect_to_db(){
		mysql_connect ($this->config->DB_HOST, $this->config->DB_USERNAME, $this->config->DB_PASSWORD) or die ('I cannot connect to mysql because: ' . mysql_error());
		mysql_select_db ($this->config->DB_NAME) or die ('I cannot select the database because: '.mysql_error());
		
		$this->connected_to_db = true;
	} // end connect_to_db()
	
	
	
	/** get_theme_dir()
	  **/
	public function get_theme_dir(){
	    return $this->config->THEME_DIR;
	} // end get_theme_dir()
	
	
	
	/** get_module_dir()
	  **/
	public function get_module_dir(){
	    return $this->config->MODULES_DIR;
	} // end get_module_dir()
	
	
	
	/** get_document_root()
	 **/
	public function get_document_root(){
	    return $this->config->DOCUMENT_ROOT;
	} // end get_document_root()
	
	
	
	/** get_login_url()
	 **/
	public function get_login_url(){
	    return $this->config->LOGIN_URL;
	} // end get_login_url()
	
	
	
	/** Mod_addslashes()
	  * 
	  * @param {string} string to be escaped
	  * @return {string} escaped string
	  **/
	public function Mod_addslashes($string){
		if(get_magic_quotes_gpc()==1){
			return ($string);
		}else{
			return (addslashes($string ));
		}
	} // end Mod_addslashes()
	
	
	
	/** concat_arrays()
	  * @access private
	  *
	  * @param {array}
	  * @param {array}
	  * @return {array} concated array
	  **/
	public function concat_arrays($a, $b){
		$c = $a;  
	    while(list(,$v)=each($b)){
	        $c[] = $v;
	    }
	    
	    return $c;
	} // end concat_arrays()
	
	
	
	/** overwrite_assoc_array()
	  * 
	  * @param {array}
	  * @param {array}
	  * @return {array} Overwritten associative array
	  **/
	public function overwrite_assoc_array($a, $b){
	    $c = $a;  
	    while(list($k,$v)=each($b)){
	        if(!is_array($v) || ( is_array($v) && count($v) > 0 )){
	        	$c[$k] = $v;
	        }
	    }
	    
	    return $c;
	} // end overwrite_assoc_array()
	
	
	
	/** build_random_id()
	  * 
	  * @return {string} A random id
	  **/
	public function build_random_id(){
		return md5(uniqid(rand(), true));
	} // end build_random_id()

}
?>