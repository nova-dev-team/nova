/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.GridWindow = Ext.extend(Ext.app.Module, {
	moduleType : 'demo',
    moduleId : 'demo-grid',
    menuPath : 'StartMenu',
	launcher : {
        iconCls: 'grid-icon',
        shortcutIconCls: 'demo-grid-shortcut',
        text: 'Grid Window',
        tooltip: '<b>Grid Window</b><br />A window with a grid'
    }
});