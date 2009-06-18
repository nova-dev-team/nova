<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

/*
 * This script allows a module to connect to its server script.
 * 
 * Example ajax call:
 * 
 * Ext.Ajax.request({
 *     url: this.app.connection,
 *     // Could also pass moduleId and action in querystring like this,
 *     // instead of in the Ext.Ajax.request params config option.
 *      
 *     // url: this.app.connection+'?action=myAction&moduleId='+this.id,
 *      params: {
 *			action: 'myAction',
 *			moduleId: this.id,
 *			
 *			...
 *		},
 *		success: function(){
 *			...
 *		},
 *		failure: function(){
 *			...
 *		},
 *		scope: this
 *	});
 */

$action = (isset($_GET["action"])) ? $_GET["action"] : $_POST["action"];
$module_id = (isset($_GET["moduleId"])) ? $_GET["moduleId"] : $_POST["moduleId"];

if($action != "" && $module_id != ""){	
	require("system/os/os.php");
	if(class_exists('os')){
		$os = new os();
		$os->module->run_action($module_id, $action);
	}
}else{
	print "{success: false}";
}
?>