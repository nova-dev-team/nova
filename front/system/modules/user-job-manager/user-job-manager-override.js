
Ext.override(QoDesk.UserJobManager, {

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('user-job-manager-win');
        
        var info_pane = new Ext.Panel({
          region : 'center',
           margins     : '3 0 3 3',
            cmargins    : '3 3 3 3',
          html: "Detailed information goes here"
        });
        
        
        
        
         var cm = new Ext.grid.ColumnModel([{
                header: "Vmachine ID",
                width: 70,
                dataIndex: 'first_name',
                sortable: true
            }, {
                header: "Vmachine IP",
                width: 130,
                dataIndex: 'last_name',
                sortable: true
            }, {
                header: "System Image",
                width: 200,
                dataIndex: 'email',
                //renderer: renderEmail,
                sortable: true
            }, {
                header: "Created on",
                width: 120,
                dataIndex: 'user_role',
                sortable: true,
            }]);
            
        
        var store = new Ext.data.JsonStore({
                //				autoLoad:"True",
                root: 'all_users',
                totalProperty: 'totalCount',
                idProperty: 'email',
                remoteSort: true,
                
                fields: ["first_name", "last_name", "email", "user_role", "is_active", "user_id", "is_logged_in"],
                
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an HttpProxy would be better
                proxy: new Ext.data.HttpProxy({
                    url: '/connect.php?action=viewUserInfo&moduleId=superadmin-user-manager'
                })
            });
            store.setDefaultSort('email', 'desc');
        
        
        
         var vm_pane = new Ext.grid.GridPanel({
                region : "north",
                store: store,
                
				height: 200,
                disableSelection: false,
                loadMask: true,
                cm: cm,
                
                 margins     : '3 0 3 3',
            cmargins    : '3 3 3 3',
            
            
             tbar: [{
					text:'New',
					tooltip:'Add a new row',
					iconCls:'demo-grid-add'
					}, {
					text:'Remove',
					tooltip:'Remove the selected item',
					iconCls:'demo-grid-remove'
				} ,'-',  {
          text : "Start"
				} , {
          text : "Stop"
				} , {
          text : "Pause"
				} , {
          text : "Resume"
				} , '-', {
          text : "Select All"
				}, '-' , {
          text : "Observe"
        }
				
				
				]
            
            
            
            });

        
        
        var cluster_pane = new Ext.Panel({
          title : "Clusters",
          region : "west",
          split : true,
          width : 200,
          collapsible: true,
          margins     : '3 0 3 3',
            cmargins    : '3 3 3 3',
            
            tbar: [{
					text:'New',
					tooltip:'Add a new row',
					iconCls:'demo-grid-add'
					}, '-', {
					text:'Remove',
					tooltip:'Remove the selected item',
					iconCls:'demo-grid-remove'
				}]

        });
        
        var right_pane = new Ext.Panel({
        region: "center",
        
          layout: 'border',
          items: [vm_pane, info_pane]
        })
        
        if(!win){        	
        	  win = desktop.createWindow({
                id: 'user-job-manager-win',
                title:'Cluster Manager',
                width:740,
                height:480,
                iconCls: 'user-job-manager-icon',
                shim:false,
                animCollapse:false,
                constrainHeader:true,
                layout: 'border',
                items: [right_pane, cluster_pane],
                taskbuttonTooltip: '<b>Grid Window</b><br />A window with a grid'
				
            });
         }
        win.show();
    }
});
