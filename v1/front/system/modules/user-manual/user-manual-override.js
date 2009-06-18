/* Override the module code here.
 * This code will be Loaded on Demand.
 */

Ext.override(QoDesk.UserManualWindow, {
	
	createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow(this.moduleId);
        
        if(!win){
        	var winWidth = desktop.getWinWidth() / 1.1;
			var winHeight = desktop.getWinHeight() / 1.1;
			
            win = desktop.createWindow({
                id: this.moduleId,
                title: 'User Manual',
                width: winWidth,
                height: winHeight,
                iconCls: 'user-manual-icon',
                shim: false,
                constrainHeader: true,
                layout: 'fit',
                items:
                    new Ext.TabPanel({
                        activeTab:0,
                        items: [{
                        	autoScroll: true,
                            title: 'Page 1',
                            header: false,
                            html: '<p> (User)This is page 1.</p>',
                			border: false
                        },{
                            title: 'Page 2',
                            header:false,
                            html: '<p>This is page 2.</p>',
                            border: false
                        },{
                            title: 'Page 3',
                            header:false,
                            html: '<p>This is page 3.</p>',
                            border:false
                        }]
                    }),
                    taskbuttonTooltip: '<b>User Manaul</b><br />How to use VCluster as Super Administrator'
            });
        }
        win.show();
    }
});