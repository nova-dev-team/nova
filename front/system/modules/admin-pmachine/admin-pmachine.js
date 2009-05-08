/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.AdminPmachine = Ext.extend(Ext.app.Module, {
	moduleType : 'admin-pmachine',
    moduleId : 'admin-pmachine',
    menuPath : 'StartMenu',
	launcher : {
        iconCls: 'admin-pmachine-icon',
        shortcutIconCls: 'admin-pmachine-shortcut',
        text: 'Pmachine',
        tooltip: '<b>Pmachine Controller</b>'
    }
});
