/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.UserManualWindow = Ext.extend(Ext.app.Module, {
	moduleType : 'user-manual',
    moduleId : 'user-manual',
    menuPath : 'StartMenu',
	launcher : {
        iconCls: 'user-manual-icon',
        shortcutIconCls: 'user-manual-shortcut',
        text: 'User Manual',
        tooltip: '<b>User Management</b><br />A tool for user management'
    }
});