/* This code defines the module and will be loaded at start up.
 * 
 * When the user selects to open this module, the override code will
 * be loaded to provide the functionality.
 * 
 * Allows for 'Module on Demand'.
 */

QoDesk.TabWindow = Ext.extend(Ext.app.Module, {
	moduleType : 'demo',
    moduleId : 'demo-tabs',
    menuPath : 'StartMenu',
	launcher : {
        iconCls: 'tab-icon',
        shortcutIconCls: 'demo-tab-shortcut',
        text: 'Tab Window',
        tooltip: '<b>Tab Window</b><br />A window with tabs'
    }
});