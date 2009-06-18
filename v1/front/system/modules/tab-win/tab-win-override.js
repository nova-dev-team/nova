/* Override the module code here.
 * This code will be Loaded on Demand.
 */

Ext.override(QoDesk.TabWindow, {
	
	createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow(this.moduleId);
        
        if(!win){
        	var winWidth = desktop.getWinWidth() / 1.1;
			var winHeight = desktop.getWinHeight() / 1.1;
			
            win = desktop.createWindow({
                id: this.moduleId,
                title: 'Tab Window',
                width: winWidth,
                height: winHeight,
                iconCls: 'tab-icon',
                shim: false,
                constrainHeader: true,
                layout: 'fit',
                items:
                    new Ext.TabPanel({
                        activeTab:0,
                        items: [{
                        	autoScroll: true,
                            title: 'Tab 1',
                            header: false,
                            html: '<p>Something useful would be in here.</p>',
                			border: false
                        },{
                            title: 'Tab 2',
                            header:false,
                            html: '<p>Something useful would be in here.</p>',
                            border: false
                        },{
                            title: 'Tab 3',
                            header:false,
                            html: '<p>Something useful would be in here.</p>',
                            border:false
                        }]
                    }),
                    taskbuttonTooltip: '<b>Tab Window</b><br />A window with tabs'
            });
        }
        win.show();
    }
});