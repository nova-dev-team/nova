
Ext.override(QoDesk.UserJobManager, {


dummyData : [
	    ['3m Co',71.72,0.02,0.03,'9/1 12:00am'],
	    ['Alcoa Inc',29.01,0.42,1.47,'9/1 12:00am'],
	    ['American Express Company',52.55,0.01,0.02,'9/1 12:00am'],
	    ['American International Group, Inc.',64.13,0.31,0.49,'9/1 12:00am'],
	    ['AT&T Inc.',31.61,-0.48,-1.54,'9/1 12:00am'],
	    ['Caterpillar Inc.',67.27,0.92,1.39,'9/1 12:00am'],
	    ['Citigroup, Inc.',49.37,0.02,0.04,'9/1 12:00am'],
	    ['Exxon Mobil Corp',68.1,-0.43,-0.64,'9/1 12:00am'],
	    ['General Electric Company',34.14,-0.08,-0.23,'9/1 12:00am'],
	    ['General Motors Corporation',30.27,1.09,3.74,'9/1 12:00am'],
	    ['Hewlett-Packard Co.',36.53,-0.03,-0.08,'9/1 12:00am'],
	    ['Honeywell Intl Inc',38.77,0.05,0.13,'9/1 12:00am'],
	    ['Intel Corporation',19.88,0.31,1.58,'9/1 12:00am'],
	    ['Johnson & Johnson',64.72,0.06,0.09,'9/1 12:00am'],
	    ['Merck & Co., Inc.',40.96,0.41,1.01,'9/1 12:00am'],
	    ['Microsoft Corporation',25.84,0.14,0.54,'9/1 12:00am'],
	    ['The Coca-Cola Company',45.07,0.26,0.58,'9/1 12:00am'],
	    ['The Procter & Gamble Company',61.91,0.01,0.02,'9/1 12:00am'],
	    ['Wal-Mart Stores, Inc.',45.45,0.73,1.63,'9/1 12:00am'],
	    ['Walt Disney Company (The) (Holding Company)',29.89,0.24,0.81,'9/1 12:00am']
	],

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