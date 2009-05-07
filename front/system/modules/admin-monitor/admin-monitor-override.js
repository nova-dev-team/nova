Ext.override(QoDesk.AdminMonitor, {

	createWindow : function(){
	
		var desktop = this.app.getDesktop();
		var win = desktop.getWindow('admin-monitor-win');
		
		// Infomation panel that located at bottom-right side
		var info_pane = new Ext.Panel({
			region : 'center',
			margins : '3 0 3 3',
			cmargins : '3 3 3 3',
			split : true,
			html: "Select a virtual cluster, and choose one of its virtual machines to show the detail information."
		});
		
		var vm_cm = new Ext.grid.ColumnModel([{
			header: "Vmachine ID",
			width: 80,
			dataIndex: 'vm_id',
			sortable: true
			}, {
			header: "Vmachine IP",
			width: 90,
			dataIndex: 'vm_ip',
			sortable: true
			}, {
			header: "System Image",
			width: 220,
			dataIndex: 'vm_image',
			//renderer: renderEmail,
			sortable: true
			}, {
			header: "Status",
			width: 100,
			dataIndex: 'status',
			sortable: true,
		}]);
		
		// The Store object that binds with cluster data, it will automatically query information from server
		var cluster_store = new Ext.data.JsonStore({
			autoLoad:"True",
			root: 'all_clusters', // JSON root
			// the JSON from server is like this:
			/*
			    {xxx:yyy, zzz:www, all_clusters:[{cluster_id:???, cluster_name:???}, {cluster_id:???, cluster_name:???}...{cluster_id:???, cluster_name:???}]}
			*/
//			totalProperty: 'totalCount',
			idProperty: 'cluster_id', // the item that serves as "PRIMARY KEY"
			//remoteSort: true,
			fields: ["cluster_id", "cluster_name", "owner_email"],
			// load using script tags for cross domain, if the data in on the same domain as
			// this page, an HttpProxy would be better
			proxy: new Ext.data.HttpProxy({
				url: '/connect.php?action=listCluster&moduleId=admin-monitor'
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
		  }, {
			header: "Owner",
			width: 134,
			dataIndex: 'owner_email',
			sortable: true
		}]);

    // information of Virtual machines
    var vm_store = new Ext.data.JsonStore({
      root: 'all_vms',
      idProperty: 'vm_id',
      fields: ["vm_id", "vm_ip", "vm_image", "status"],
      //autoLoad:"True",
			proxy: new Ext.data.HttpProxy({
				url: '/connect.php?action=listVM&moduleId=admin-monitor'
			})
    });

		var vm_pane = new Ext.grid.GridPanel({
			region : "north", // indicator for "border-layout" policy
			store: vm_store,
			split : true,
			height: 200,
			disableSelection: false,
			loadMask: true,
			cm: vm_cm,
			margins : '3 0 3 3',
			cmargins : '3 3 3 3',
			tbar: [{
				text:'Remove',
				tooltip:'Remove the selected item',
				iconCls:'admin-monitor-remove',
				
        // rm an vm
        handler: function() {
          var rows = vm_pane.getSelectionModel().getSelections();
          if (rows.length == 0) {
            alert("Select a vmachine first!");
            return;
          }
          var vid = rows[0].data.vm_id;
          Ext.Ajax.request({
            url: '/connect.php',
            params: {
              moduleId: 'admin-monitor',
              action: "removeVM",
              vm_vid: vid
            },
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                // refresh
                vm_store.reload();
                // TODO change the info pane
              } else {
                // TODO when create failed
              }
            },
            failure: function(){
              // TODO when connect failed
            }
          });
        }
      } ,'-', {
  			  text: "Refresh",
	  		  iconCls:'admin-monitor-refresh',
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
			width : 340,
			margins : '3 0 3 3',
			cmargins : '3 3 3 3',
    	cm: cluster_cm,
			tbar: [{
				text:'Remove',
				tooltip:'Remove the selected item',
				iconCls:'admin-monitor-remove',
				
        //  remove grid
        handler:function() {
          var rows = cluster_pane.getSelectionModel().getSelections();
          if (rows.length == 0) {
            alert("Select a cluster first!");
            return;
          }
          
          var cluster_cid = rows[0].data.cluster_id;

          Ext.Ajax.request({
            url: '/connect.php',
            params: {
              moduleId: 'admin-monitor',
              action: "removeCluster",
              vcluster_cid: cluster_cid
            },
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                // refresh
                cluster_store.reload();
                vm_store.removeAll();			      

    			      vm_store.proxy = new Ext.data.HttpProxy({
    		    		  url: '/connect.php?action=listVM&moduleId=admin-monitor'
          			});            
              } else {
                // TODO when create failed
              }
            },
            failure: function(){
              // TODO when connect failed
            }
          });
        }
        
			},'-', {
			  text:'Refresh',
  			iconCls:'admin-monitor-refresh',
  			handler:function(){
		      cluster_store.reload();
		      vm_store.removeAll();

		      vm_store.proxy = new Ext.data.HttpProxy({
    				url: '/connect.php?action=listVM&moduleId=admin-monitor'
    			});
  			}
  		}]
		});
		
  	function cluster_row_click() {
  	
      // XXX how to change the vmachine lists -> change the cid
      rows = cluster_pane.getSelectionModel().getSelections();

      vm_store.proxy = new Ext.data.HttpProxy({
				url: '/connect.php?action=listVM&moduleId=admin-monitor&cid=' + rows[0].data.cluster_id
			});
      vm_store.reload();
    }
		
		cluster_pane.addListener("rowclick", cluster_row_click);
				
		function vm_row_click() {
		  //  alert("TODO row click of vm list");
      var html = "";
      rows = vm_pane.getSelectionModel().getSelections();
      vmid = rows[0].data.vm_id;
      rows2 = cluster_pane.getSelectionModel().getSelections();
      cid = rows2[0].data.cluster_id;

      Ext.Ajax.request({
        url: '/connect.php',
        params: {
          moduleId: 'admin-monitor',
          action: "infoVM",
          vm_id: vmid
        },
        success: function(o){
          if (o && o.responseText && Ext.decode(o.responseText).success) {
            // refresh
      			html = o.responseText;
          	info_pane.body.update(html);            
          } else {
            // TODO when create failed
          }
        },
        failure: function(){
          // TODO when connect failed
        }
      });
    }

		vm_pane.addListener("rowclick", vm_row_click);
		var right_pane = new Ext.Panel({
			region: "center",
			layout: 'border',
			items: [vm_pane, info_pane]
		});
		
		if(!win){
			win = desktop.createWindow({
				id: 'admin-monitor-win',
				title:'Admin Monitor',
				width:880,
				height:540,
				iconCls: 'admin-monitor-icon',
				shim:false,
				animCollapse:false,
				constrainHeader:true,
				layout: 'border',
				items: [right_pane, cluster_pane],
				taskbuttonTooltip: '<b>Admin Monitor</b><br />Monitor every cluster'
			});
		}
		win.show();
	}
});
