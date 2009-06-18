/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

QoDesk.QoPreferences = Ext.extend(Ext.app.Module, {
	moduleType : 'system/preferences',
	moduleId : 'qo-preferences',
	menuPath : 'ToolMenu',
	launcher : {
        iconCls: 'pref-icon',
        shortcutIconCls: 'pref-shortcut-icon',
        text: 'Preferences',
        tooltip: '<b>Preferences</b><br />Allows you to modify your desktop'
    }
});