/*
 * qWikiOffice Desktop 1.0.0 Alpha
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

QoDesk.TemplateModule = Ext.extend(Ext.app.Module, {
	moduleType	: 'templateModule',
	moduleId	: 'templateModule',

	//menuPath	: 'ToolMenu',
	menuPath	: 'StartMenu',

	launcher : {
	        iconCls 	: 'templateModule-icon',
       		shortcutIconCls : 'templateModule-shortcut-icon',
       		text		: 'Template Module',
       		tooltip		: '<b>Template Module</b><br />Basic Module Template.'
	}

	//Coming in next version - Causes error in App.js with 1.0 Alpha?
	//init: function(){
		//this.language = QoDesk.QoPreferences.Language;

		//this.launcher = {
		        //iconCls 		: 'templateModule-icon',
       			//shortcutIconCls	: 'templateModule-shortcut-icon',
			//text			: this.language.launcherText,
			//tooltip		: this.language.launcherTooltip
		//}
	//}
});
