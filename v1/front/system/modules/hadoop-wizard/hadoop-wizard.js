/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.HadoopWizard = Ext.extend(Ext.app.Module, {
	moduleType : 'hadoop-wizard',
	moduleId : 'hadoop-wizard',
	menuPath : 'StartMenu',
	launcher : {
		iconCls: 'hadoop-wizard-icon',
		shortcutIconCls: 'hadoop-wizard-shortcut',
		text: 'Cluster Wizard',
		tooltip: '<b>Cluster Wizard</b><br />Deploy new clusters in a few clicks'
	}
});
