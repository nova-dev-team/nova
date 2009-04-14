/* This code will be loaded at start up.
 * 
 * When the user selects to open this module, it's override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.LayoutWindow = Ext.extend(Ext.app.Module, {
	moduleType : 'demo',
	moduleId : 'demo-layout',
	menuPath : 'StartMenu/Bogus Menu',
	launcher : {
		iconCls: 'layout-icon',
		shortcutIconCls: 'demo-layout-shortcut',
		text: 'Layout Window',
		tooltip: '<b>Layout Window</b><br />A window with a layout'
	}
});