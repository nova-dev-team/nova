/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.SuperAdminManagerWindow = Ext.extend(Ext.app.Module, {
	moduleType : 'superadmin-user-manager',
    moduleId : 'superadmin-user-manager',
    menuPath : 'StartMenu',
	launcher : {
        iconCls: 'superadmin-user-manager-icon',
        shortcutIconCls: 'superadmin-user-manager-shortcut',
        text: 'User Manager',
        tooltip: '<b>User Manager</b><br />A user manager for Super Administrator'
    }
});