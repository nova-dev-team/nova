/* Override the module code here.
 * This code will be Loaded on Demand.
 */

Ext.override(QoDesk.AdminPclusterMon, {
	
createWindow : function(){
  var desktop = this.app.getDesktop();
  var win = desktop.getWindow(this.moduleId);
  
  if(!win){
  	var winWidth = desktop.getWinWidth() / 1.1;
		var winHeight = desktop.getWinHeight() / 1.1;
    win = desktop.createWindow({
      id: this.moduleId,
      title: 'Clusters Mon',
      width: winWidth,
      height: winHeight,
      iconCls: 'superadmin-manual-icon',
      shim: false,
      constrainHeader: true,
      layout: 'fit',
      items:
        new Ext.TabPanel({
          activeTab:0,
          items: [{
          	autoScroll: true,
            title: 'Physical Clusters',
            header: false,
            html: '<p>This is page 1.</p>',
      			border: false
          },{
            title: 'Virtual Clusters',
            header:false,
            html: '<p>This is page 2.</p>',
            border: false
          }]
        }),
        taskbuttonTooltip: '<b>Clusters Mon</b><br />Collect detail info of the clusters'
      });
    }
  win.show();
}
});
