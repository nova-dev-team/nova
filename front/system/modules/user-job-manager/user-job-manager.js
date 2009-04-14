/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.UserJobManager = Ext.extend(Ext.app.Module, {
	moduleType : 'user-job-manager',
    moduleId : 'user-job-manager',
    menuPath : 'StartMenu',
	launcher : {
        iconCls: 'user-job-manager-icon',
        shortcutIconCls: 'user-job-manager-shortcut',
        text: 'Job Manager',
        tooltip: '<b>Grid Window</b><br />A window with a grid'
    }
});