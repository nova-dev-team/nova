/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.BogusWindow = Ext.extend(Ext.app.Module, {
	moduleType : 'demo',
	moduleId : 'demo-bogus',
	menuPath : 'StartMenu/Bogus Menu/Bogus Sub Menu',
	launcher : {
		iconCls: 'bogus-icon',
		shortcutIconCls: 'demo-bogus-shortcut',
		text: 'Bogus Window',
		tooltip: '<b>Bogus Window</b><br />A bogus window'
	}
});