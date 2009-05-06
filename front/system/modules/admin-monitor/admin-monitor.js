/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.AdminMonitor = Ext.extend(Ext.app.Module, {
	moduleType : 'admin-monitor',
    moduleId : 'admin-monitor',
    menuPath : 'StartMenu',
  	launcher : {
      iconCls: 'admin-monitor-icon',
      shortcutIconCls: 'admin-monitor-shortcut',
      text: 'Admin Monitor',
      tooltip: '<b>Admin Monitor</b><br />Monitor every cluster'
    }
});
