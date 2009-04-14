<?php
Header("content-type: application/x-javascript");

require_once("system/os/os.php");
if(!class_exists('os')){ die('os class is missing!'); }
$os = new os();
?>
Ext.namespace('QoDesk');
<?php
$os->module->load_all();
?>

/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 *
 * NOTE:
 * This code is based on code from the original Ext JS desktop demo.
 * I have made many modifications/additions.
 *
 * The Ext JS licensing can be viewed here:
 *
 * Ext JS Library 2.0 Beta 2
 * Copyright(c) 2006-2007, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 *
 */

QoDesk.App = new Ext.app.App({
	
	init : function(){
		Ext.QuickTips.init();
	},
	
	/**
     * Returns the privileges object.
     *
     * Example
     *
     * {
     *		saveAppearence: [
     *			'qo-preferences'
     *		],
     *		saveAutorun: [
     *			'qo-preferences'
     *		]
     *		...
     * }
     */
	getPrivileges : function(){
	    return <?php print $os->privilege->get_all(); ?>;
	},
	
	/**
	 * Returns an array of the module instances.
	 *
	 * Example:
	 * 
	 * [
	 * 		new QoDesk.GridWindow(),
	 *		...
	 * ]
	 * 
	 */
    getModules : function(){
    	return [ <?php print $os->module->get_all(); ?> ];
    },
    
    /**
	 * Returns the launchers object.
	 * Contains the moduleId's of the modules to add to each launcher.
	 * 
	 * Example:
	 *
	 * {
	 *		autorun: [
	 *		
	 *		],
	 *		contextmenu: [
	 *			'qo-preferences'
	 *		],
	 *		quickstart: [
	 *			'qo-preferences'
	 *		],
	 *		shortcut: [
	 *			'qo-preferences'
	 *		]
	 *	}
	 */
    getLaunchers : function(){
    	return <?php print $os->launcher->get_all(); ?>;
    },
	
    /**
	 * Returns the Styles object.
	 *
	 * Example
	 *
	 * {
	 *		backgroundcolor: '575757',
	 *		fontcolor: 'FFFFFF',
	 *		transparency: 100,
	 *		theme: {
	 *			id: 2,
	 *			name: 'Vista Black',
	 *			pathtofile: 'resources/themes/xtheme-vistablack/css/xtheme-vistablack.css'
	 *		},
	 *		wallpaper: {
	 *			id: 10,
	 *			name: 'Blue Swirl',
	 *			pathtofile: 'resources/wallpapers/blue-swirl.jpg'
	 *		},
	 *		wallpaperposition: 'tile'
	 *	}
	 */
    getStyles : function(){
    	return <?php print $os->preference->get_styles(); ?>;
    },
	
	/**
	 * Returns the Start Menu's logout button configuration
	 */
	getLogoutConfig : function(){
		return {
			text: 'Logout',
			iconCls: 'logout',
			handler: function(){ window.location = "logout.php"; },
			scope: this
		};
	},
	
	/**
	 * Returns the Start Menu configuration
	 */
    getStartConfig : function(){
    	return {
        	 iconCls: 'user',
            title: '<?php print $os->member->get_name($os->session->get_member_id()); ?>',
			toolPanelWidth: 115
        };
    },
    
    /**
	 * Function that handles sorting of the Start Menu
	 * Return true to swap a and b
	 */
	startMenuSortFn : function(a, b){
	    // Sort in ASC alphabetical order
		// if( b.text < a.text ){
		//		return true;
		// }
		
		// Sort in ASC alphabetical order with menus at the bottom
		// if( (b.text < a.text) && !b.menu ){
		//		return true;
		// }
		
		// Sort in ASC alphabetical order with menus at the top
		if( ( ( b.menu && a.menu ) && ( b.text < a.text ) ) || ( b.menu && !a.menu ) || ( (b.text < a.text) && !a.menu ) ){
			return true;
		}
		
		return false;
	}
});