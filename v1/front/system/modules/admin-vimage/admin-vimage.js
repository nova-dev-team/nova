/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.AdminVimage = Ext.extend(Ext.app.Module, {
	moduleType : 'admin-vimage',
    moduleId : 'admin-vimage',
    menuPath : 'StartMenu',
	launcher : {
        iconCls: 'admin-vimage-icon',
        shortcutIconCls: 'admin-vimage-shortcut',
        text: 'Vimage',
        tooltip: '<b>Vimage manager</b><br />A user manager for Super Administrator'
    }
});
