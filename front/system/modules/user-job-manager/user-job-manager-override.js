Ext.override(QoDesk.UserJobManager, {
	createWindow : function(){
		var desktop = this.app.getDesktop();
		var win = desktop.getWindow('user-job-manager-win');

		/*
		Ext.Ajax.request({
			url: '/connect.php',
			params: {
				moduleId: 'user-job-manager',
				action: "dummyTest"
			},
			success: function(o){
				if (o && o.responseText && Ext.decode(o.responseText).success) {
					dummyText = "success: ";
				}
				else {
					dummyText = "Failure 1";
				}
				//alert(dummyText + " : " + o.responseText);
			},
			failure: function(){
				dummyText = "Failure 2";
				// alert(dummyText);
			}
		}
		);
		*/
		
		var info_pane = new Ext.Panel({
			region : 'center',
			margins : '3 0 3 3',
			cmargins : '3 3 3 3',
			split : true,
			html: "Pieces of dummy html code here"
		});
		
		
		var vm_cm = new Ext.grid.ColumnModel([{
			header: "Vmachine ID",
			width: 70,
			dataIndex: 'vm_id',
			sortable: true
			}, {
			header: "Vmachine IP",
			width: 130,
			dataIndex: 'vm_ip',
			sortable: true
			}, {
			header: "System Image",
			width: 200,
			dataIndex: 'vm_image',
			//renderer: renderEmail,
			sortable: true
			}, {
			header: "Created on",
			width: 120,
			dataIndex: 'create_time',
			sortable: true,
		}]);
		
		var cluster_store = new Ext.data.JsonStore({
			autoLoad:"True",
			root: 'all_clusters',
//			totalProperty: 'totalCount',
			idProperty: 'cluster_id',
			//remoteSort: true,
			fields: ["cluster_id", "cluster_name"],
			// load using script tags for cross domain, if the data in on the same domain as
			// this page, an HttpProxy would be better
			proxy: new Ext.data.HttpProxy({
				url: '/connect.php?action=listCluster&moduleId=user-job-manager'
			})
		});
//		cluster_store.setDefaultSort('email', 'desc');


		var cluster_cm = new Ext.grid.ColumnModel([{
			header: "Cluster ID",
			width: 70,
			dataIndex: 'cluster_id',
			sortable: true
			}, {
			header: "Cluster Name",
			width: 130,
			dataIndex: 'cluster_name',
			sortable: true
		}]);


var vm_store = new Ext.data.JsonStore({
  root: 'all_vms',
idProperty: 'vm_id',
fields: ["vm_id", "vm_ip", "vm_image", "create_time"],
autoLoad:"True",
			proxy: new Ext.data.HttpProxy({
				url: '/connect.php?action=listVM&moduleId=user-job-manager'
			})
});

		var vm_pane = new Ext.grid.GridPanel({
			region : "north",
			store: vm_store,
			split : true,
			height: 200,
			disableSelection: false,
			loadMask: true,
			cm: vm_cm,
			margins : '3 0 3 3',
			cmargins : '3 3 3 3',
			tbar: [{
				text:'New',
				tooltip:'Add a new row',
				iconCls:'demo-grid-add'
				}, {
				text:'Remove',
				tooltip:'Remove the selected item',
				iconCls:'demo-grid-remove'
				} ,'-', {
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
			}, '-' ,{
			  text: "Refresh",
			  handler: function() {
			    vm_store.reload();
}
			}
			]
		});
		
		
		var cluster_pane = new Ext.grid.GridPanel({
			title : "Clusters",
			region : "west",
	store:cluster_store,
			split : true,
			loadMask: true,
			width : 200,
			margins : '3 0 3 3',
			cmargins : '3 3 3 3',
	cm: cluster_cm,
			tbar: [{
				text:'New',
				tooltip:'Add a new row',
				iconCls:'demo-grid-add'
				}, '-', {
				text:'Remove',
				tooltip:'Remove the selected item',
				iconCls:'demo-grid-remove'
			},'-', {text:'Refresh', handler:function(){
			
			
			      cluster_store.reload();
			      vm_store.removeAll();
			      
			      
			      // XXX how to change the vmachine lists -> change the cid
			      vm_store.proxy = new Ext.data.HttpProxy({
				url: '/connect.php?action=listVM&moduleId=user-job-manager&cid=2'
			})
			      vm_store.reload();
//			        cluster_pane.getView().refresh(true);
//			        alert(3);
			
			}}]
		});
		
		var right_pane = new Ext.Panel({
			region: "center",
			layout: 'border',
			items: [vm_pane, info_pane]
		});
		
		
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
				taskbuttonTooltip: '<b>Grid Window</b><br>A window with a grid'
			});
			// for the vnc displays
			function createVNCwin() {
				desktop.createWindow({
					id: 'vnc_vm_' , // TODO  change the win name
					title: "VNC",
					width: 700,
					height: 550,
					shim: false,
					animeCollapse: false,
					constrainHeader: true,
					html : "<font color='red'>You might want to minimize other windows to get best using experience.</font><p><applet archive='/vncviewer.jar' code='VncViewer.class' width='640' height='480'><param name='PORT' value='5900' /><param name='ENCODING' value='tight' /><param name='Show controls' value='yes' /></applet>"
				});
			}
			var vncwin = desktop.getWindow('vnc_vm_');
			if (!vncwin) {
        createVNCwin();
        vncwin = desktop.getWindow('vnc_vm_');
				//vncwin.show();  TODO show it
			}
		}
		win.show();
	}
});

