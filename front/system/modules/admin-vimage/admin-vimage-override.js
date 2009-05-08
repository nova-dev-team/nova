var bigApp;



Ext.override(QoDesk.AdminVimage, {

    createWindow: function(){
    
        bigApp = this.app;
        
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('admin-vimage-win');
        
        function test1(){
            alert('hi');
        }
        
        if (!win) {
        
            var store = new Ext.data.JsonStore({
                //				autoLoad:"True",
                root: 'all_images',
                autoLoad:"True",
                idProperty: 'iid',
                //remoteSort: true,
                
                fields: ["os_family", "os_name", "location", "hidden", "iid"],
                
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an HttpProxy would be better
                proxy: new Ext.data.HttpProxy({
                    url: '/connect.php?action=listImg&moduleId=admin-vimage'
                })
            });
//            store.setDefaultSort('iid', 'asc');
            
            var checkColumn = new Ext.grid.CheckColumn({
                header: "Is Active?",
                dataIndex: 'hidden',
                width: 60,
                sortable: true
            });
            
            var cm = new Ext.grid.ColumnModel([{
                header: "Image ID",
                width: 130,
                dataIndex: 'iid',
                sortable: true
            }, {
                header: "OS Family",
                width: 130,
                dataIndex: 'os_family',
                sortable: true
            }, {
                header: "OS Name",
                width: 130,
                dataIndex: 'os_name',
                sortable: true
            }, {
                header: "Location",
                width: 200,
                dataIndex: 'location',
                sortable: true
            }, checkColumn]);
            
            
            var grid = new Ext.grid.EditorGridPanel({
                store: store,
                
                //			trackMouseOver:false,
                disableSelection: true,
                
                loadMask: true,

                plugins: [checkColumn],
                clicksToEdit: 1,
                
                
                tbar: [{
                  text:'New',
				          tooltip:'Add a new OS Image',
				          iconCls:'admin-vimage-add',
				          
				          handler: function() {
				          
				            var os_family = prompt("OS Family?");
				            var os_name = prompt("OS Name?");
				            var imglocation = prompt("Image location?");
				            
                    Ext.Ajax.request({
                      url: '/connect.php',
                      params: {
                        moduleId: 'admin-vimage',
                        action: "addImg",
                        osfamily: os_family,
                        osname: os_name,
                        img_location: imglocation
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
                
                cm: cm
            });
            
            win = desktop.createWindow({
                id: 'admin-vimage-win',
                title: "Vimage Management",
                width: 740,
                height: 480,
                iconCls: 'admin-vimage-icon',
                shim: false,
                animCollapse: false,
                constrainHeader: true,
                layout: 'fit',
                items: grid,
                taskbuttonTooltip: '<b>User Manager</b><br />A user manager for Super Administrator',
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
        if (t.className && t.className.indexOf('x-grid3-cc-' + this.id) != -1) {
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
                    moduleId: 'admin-vimage',
                    action: "delImg",
                    img_id: record.data.iid,
                    hidden: record.data[this.dataIndex]
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
    /*
        if (record.data.hidden == 1) {
            record.data.hidden = true;
        }
        else 
            if (record.data.hidden == 0) {
                record.data.hidden = false;
            }
      */  
        var html = '<div class="x-grid3-check-col' + ((record.data.hidden) ? '' : '-on') + ' x-grid3-cc-' + this.id + '" id="crapp_id199_' + record.data.iid + '">&#160;</div>';
        
        return html;
    }
};

