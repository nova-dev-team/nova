<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

require_once("system/os/os.php");	
if(class_exists('os')){
	$os = new os();
	
	$module_id = (isset($_GET["moduleId"])) ? $_GET["moduleId"] : $_POST["moduleId"];
	
	if($module_id != ""){
		$os->module->load($module_id);
	}
}
?>