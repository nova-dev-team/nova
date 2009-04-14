/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

QoDesk.AccountSetting = Ext.extend(Ext.app.Module, {
	moduleType : 'account-setting',
	moduleId : 'account-setting',
	menuPath : 'ToolMenu',
	launcher : {
        iconCls: 'account-setting-icon',
        shortcutIconCls: 'account-setting-shortcut',
        text: 'My Account',
        tooltip: '<b>Preferences</b><br />Allows you to modify your desktop'
    }
});