/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.AdminPclusterMon = Ext.extend(Ext.app.Module, {
	moduleType : 'admin-pcluster-mon',
    moduleId : 'admin-pcluster-mon',
    menuPath : 'StartMenu',
	launcher : {
        iconCls: 'superadmin-manual-icon',
        shortcutIconCls: 'superadmin-manual-shortcut',
        text: 'Clusters',
        tooltip: '<b>Clusters Mon</b><br />Collect detail info of the clusters'
    }
});
