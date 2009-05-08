var bigApp;



Ext.override(QoDesk.AdminPmachine, {

    createWindow: function(){
    
        bigApp = this.app;
        
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('admin-pmachine-win');
        
        function test1(){
            alert('hi');
        }
        
        if (!win) {
        
            var store = new Ext.data.JsonStore({
                //				autoLoad:"True",
                root: 'pm_info',
//                totalProperty: 'totalCount',
                idProperty: 'pm_ip',
                remoteSort: false,
                autoLoad:"True",
                fields: ["pm_ip", "is_working"],
                
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an HttpProxy would be better
                proxy: new Ext.data.HttpProxy({
                    url: '/connect.php?action=listPm&moduleId=admin-pmachine'
                })
            });
            store.setDefaultSort('pm_ip', 'desc');

            var checkColumn = new Ext.grid.CheckColumn({
                header: "Is Working?",
                dataIndex: 'is_working',
                width: 80,
                sortable: true
            });
            
            var cm = new Ext.grid.ColumnModel([{
                header: "Pmachine IP",
                width: 180,
                dataIndex: 'pm_ip',
                sortable: true
            }, checkColumn]);
            
            var grid = new Ext.grid.EditorGridPanel({
                store: store,
                
                //			trackMouseOver:false,
                disableSelection: true,
                
                loadMask: true,
                tbar: [{
                  text:'New',
				          tooltip:'Add a new Physical Machine',
				          iconCls:'admin-pmachine-add',
				          
				          handler: function() {
                    var new_ip = prompt("New Pmachine IP?");
                    Ext.Ajax.request({
                      url: '/connect.php',
                      params: {
                        moduleId: 'admin-pmachine',
                        action: "addPM",
                        pm_ip: new_ip
                      },
                      success: function(o) {
                        if (o && o.responseText && Ext.decode(o.responseText).success)
                          store.reload();
                      },
                      failure: function(o) {
                      }
                    });
                  }
				          
                }, {
                  text:'Delete',
				          tooltip:'Add a new Physical Machine',
				          iconCls:'admin-pmachine-remove',
				          
				          handler: function() {
				            Ext.Ajax.request({
                      url: '/connect.php',
                      params: {
                        moduleId: 'admin-pmachine',
                        action: "changeVMstatus",
                        is_working: "false"
                      },
                      success: function(o) {
                        if (o && o.responseText && Ext.decode(o.responseText).success)
                          store.reload();
                      },
                      failure: function(o) {
                      }
                    });
                  }
                  
                },{
                  text:'Refresh',
				          iconCls:'admin-pmachine-refresh',
				          
				          handler: function() {
                    store.reload();
                  }
                }],
                plugins: [checkColumn],
                clicksToEdit: 1,
                
                cm: cm
            });
            
            win = desktop.createWindow({
                id: 'admin-pmachine-win',
                title: "Pmachine Controller",
                width: 320,
                height: 280,
                iconCls: 'admin-pmachine-icon',
                shim: false,
                animCollapse: false,
                constrainHeader: true,
                layout: 'fit',
                items: grid,
                taskbuttonTooltip: '<b>Pmachine Controller</b><br />Manages pmachines in cluster',
            });
        }
        win.show();
    }
});

Ext.grid.CheckColumn = function(config){
    Ext.apply(this, config);
    if (!this.id) {
        this.id = Ext.id();
    }
    this.renderer = this.renderer.createDelegate(this);
};

Ext.grid.CheckColumn.prototype = {
    init: function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    
    onMouseDown: function(e, t){
        if (t.className && t.className.indexOf('x-grid4-cc-' + this.id) != -1) {
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            
            var desktop = bigApp.getDesktop();
            var notifyWin = desktop.showNotification({
                html: 'Sending request...',
                title: 'Please wait'
            });
            
            Ext.Ajax.request({
                url: bigApp.connection,
                params: {
                    moduleId: 'admin-pmachine',
                    action: "changePMstatus",
                    is_working: record.data.is_working,
                    pm_ip: record.data.pm_ip
                },
                success: function(o){
                    if (o && o.responseText && Ext.decode(o.responseText).success) {
                        saveComplete('Finished', 'Save complete.');
                    }
                    else {
                        saveComplete('Error', 'Errors encountered on the server.');
                    }
                },
                failure: function(){
                    saveComplete('Error', 'Lost connection to server.');
                },
                scope: this
            
            });
            
            function saveComplete(title, msg){
                notifyWin.setIconClass('x-icon-done');
                notifyWin.setTitle(title);
                notifyWin.setMessage(msg);
                desktop.hideNotification(notifyWin);
            }
        }
    },
    
    renderer: function(value, p, record){
    
        if (record.data.is_working == 1) {
            record.data.is_working = true;
        }
        else 
            if (record.data.is_working == 0) {
                record.data.is_working = false;
            }
        
        var html = '<div class="x-grid3-check-col' + ((record.data.is_working) ? '-on' : '') + ' x-grid4-cc-' + this.id + '" id="crapp_id3_' + record.data.is_working + '">&#160;</div>';
        
        return html;
    }
};

