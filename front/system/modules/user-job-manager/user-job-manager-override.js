Ext.override(QoDesk.UserJobManager, {

	createWindow : function(){
	
		var desktop = this.app.getDesktop();
		var win = desktop.getWindow('user-job-manager-win');
		
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
			width: 134,
			dataIndex: 'cluster_name',
			sortable: true
		}]);

    // information of Virtual machines
    var vm_store = new Ext.data.JsonStore({
      root: 'all_vms',
      idProperty: 'vm_id',
      fields: ["vm_id", "vm_ip", "vm_image", "status"],
      //autoLoad:"True",
			proxy: new Ext.data.HttpProxy({
				url: '/connect.php?action=listVM&moduleId=user-job-manager'
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
				text:'New',
				tooltip:'Add a new Virtual Machine',
				iconCls:'user-job-manager-add',
				
				// this is a "member variable", a window for creating new virtual machines
				new_vm_dialog : null,
// Handler for adding a new virual machine
        handler: function() {
        
          // first of all, test if a cluster was selected
          var rows = cluster_pane.getSelectionModel().getSelections();
          if (rows.length == 0) {
            alert("Select a cluster first!");
            return;
          }

          // if a cluster was selected, get its id
          var cluster_cid = rows[0].data.cluster_id;

          // Ajax request to get list of images
          Ext.Ajax.request({
            url: '/connect.php',
            params: {
              moduleId: 'user-job-manager',
              action: "listImage"
            },
            // callback function, called when request successfully finished
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                // if request successfly (got vmachine image list), create "new vmachine form"
                vimageList = Ext.decode(o.responseText).imglist;
                formHtml = "<table><tr><td>CPU count:</td><td><input id='new_vm_dialog_vcpu'></td></tr><tr><td>Memory:</td><td><input id='new_vm_dialog_mem'></td></tr>";
                formHtml += "<tr><td>OS Image:</td><td><select id='new_vm_dialog_img'>";
                for (i = 0; i < vimageList.length; i+=2) {
                  formHtml += "<option value='" + vimageList[i + 1] + "'>" + vimageList[i] + "</option>";
                }
                formHtml += "</select></td></tr>";
                formHtml += "</table>";
                if (vm_pane.new_vm_dialog == null) {
                  // now, create a new "new vmachine dialog" if necessary
                
                  // this "winManager" is a component of QoDesktop
                  var winManager = desktop.getManager();
                  vm_pane.new_vm_dialog = new Ext.Window({
                  	bodyStyle:'padding:10px',
                    layout:'fit',
                    width:300,
                    height:200,
                    closeAction:'hide',
                    plain: true,
                    title: "Create a new virtual machine",
                    html: formHtml,
                    buttons: [{
                      text:'Submit',
                      handler: function() {
                        var mem_val = Ext.get("new_vm_dialog_mem").dom.value;
                        var img_val = Ext.get("new_vm_dialog_img").dom.value;
                        var vcpu_val = Ext.get("new_vm_dialog_vcpu").dom.value;
                        
                        // Ajax to get 
                        Ext.Ajax.request({
                          url: '/connect.php',
                          params: {
                            moduleId: 'user-job-manager',
                            action: "newVM",
                            vcluster_cid: cluster_cid,
                            mem: mem_val,
                            img: img_val,
                            vcpu: vcpu_val
                          },
                          success: function(o){
                            if (o && o.responseText && Ext.decode(o.responseText).success) {
                              // refresh
                              vm_store.reload();
                            } else {
                              // TODO when create failed
                              vm_pane.new_vm_dialog.hide();
                            }
                          },
                          failure: function(){
                            // TODO when connect failed
                            vm_pane.new_vm_dialog.hide();
                          }
                        });
                        vm_pane.new_vm_dialog.hide();
                      }
                    },{
                      text: 'Close',
                      handler: function(){
                        vm_pane.new_vm_dialog.hide();
                      }
                    }],
                    manager: winManager,
                    modal: true
                  });
                }

                vm_pane.new_vm_dialog.show();

              } else {
              // TODO when failed to load image list
                if (vm_pane.new_vm_dialog != null)
                  vm_pane.new_vm_dialog.hide();
              }
            },
            failure: function(){
              // TODO when connect failed
              vm_pane.new_vm_dialog.hide();
            }
          });
        }
			}, {
				text:'Remove',
				tooltip:'Remove the selected item',
				iconCls:'user-job-manager-remove',
				
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
              moduleId: 'user-job-manager',
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
				text : "Start",
				
        // start an vm
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
              moduleId: 'user-job-manager',
              action: "startVM",
              vm_vid: vid
            },
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                alert(o.responseText);
              } else {                alert(o.responseText);
              }
            },
            failure: function(){
            }
          });
        }
		  } , {
				text : "Stop",

        // stop an vm
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
              moduleId: 'user-job-manager',
              action: "stopVM",
              vm_vid: vid
            },
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                // refresh
                alert(Ext.decode(o.responseText).msg);
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
  		} , {
				text : "Pause",

        // pause an vm
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
              moduleId: 'user-job-manager',
              action: "pauseVM",
              vm_vid: vid
            },
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                // refresh
                alert(Ext.decode(o.responseText).msg);
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
			} , {
				text : "Resume",

        // resume an vm
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
              moduleId: 'user-job-manager',
              action: "resumeVM",
              vm_vid: vid
            },
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                // refresh
                alert(Ext.decode(o.responseText).msg);
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
		  } ,
				
				/* TODO "select all"
				'-', {
				text : "Select All"
				},  
				*/
				
				'-' , {
				text : "Observe",
				handler: function() {
  				rows = vm_pane.getSelectionModel().getSelections();
          if (rows.length == 0) {
            alert("You need to select a vmachine first");
          } else {
            vmid = rows[0].data.vm_id;
            vm_image_name = rows[0].data.vm_image;
            rows2 = cluster_pane.getSelectionModel().getSelections();  
            cid = rows2[0].data.cluster_id;


      			// for the vnc displays, create a new vnc window			
      			function createVNCwin(_cid, _vmid, _pmip, _vnc_port, win_width, win_height) {
      				desktop.createWindow({
			      		id: 'vnc_vm_' + _cid + "_" + _vmid , // TODO  change the win name
      					title: "VNC of " + _cid + ", " + _vmid,
      					width: win_width + 15,
					      height: win_height + 33,
					      minWidth: 640,
					      minHeight: 400,
					      shim: false,
					      margins : '0 0 0 0',
					      cmargins : '0 0 0 0',
					      animeCollapse: false,
					      constrainHeader: true,

      					listeners: {
      					  resize: {
					          fn: function() {
        					    var vncapp = Ext.get('vnc_vm_' + _cid + "_" + _vmid);
//        					    console.log(vncapp);
        					    var sz = this.getSize();
        					    vncapp.dom.height = sz.height - 33;
        					    vncapp.dom.width = sz.width - 15;
                    }
      					  }
      					},

      					html : "<applet archive='http://" + _pmip + ":3000/vncviewer.jar' id='" + 'vnc_vm_' + _cid + "_" + _vmid +
      					        "' code='VncViewer.class' width='" + win_width + "' height='" + win_height + "'><param name='PORT' value='" + _vnc_port + 
      					        "' /><param name='ENCODING' value='tight' /><param name='PASSWORD' value='MacOSX10.5'><param name='HOST' value='" + _pmip +
      					        "'><param name='Show controls' value='no' /></applet>"
      				});
      			}

            var vncwin = desktop.getWindow('vnc_vm_' + cid + "_" + vmid);
      			if (!vncwin) {
      			
			        Ext.Ajax.request({
                url: '/connect.php',
                params: {
                  moduleId: 'user-job-manager',
                  action: "infoVM",
                  vm_id: vmid
                },
                success: function(o){
                  if (o && o.responseText && Ext.decode(o.responseText).success) {
                    // refresh
                    
                    // Different win size for different type of images (console-based, gui-based)
                    if (vm_image_name.indexOf("-console")  != -1) {
                      createVNCwin(cid, vmid, Ext.decode(o.responseText).pm_ip, Ext.decode(o.responseText).vnc_port, 640, 300);
                    } else {
                      createVNCwin(cid, vmid, Ext.decode(o.responseText).pm_ip, Ext.decode(o.responseText).vnc_port, 800, 600);
                    }
                    vncwin = desktop.getWindow('vnc_vm_' + cid + "_" + vmid);
                    vncwin.show();
                  } else {
                    // TODO when create failed
                  }
                },
                failure: function(){
                  // TODO when connect failed
                }
              });
    			  }
    			  
      			if (vncwin)
        			vncwin.show(); // TODO show it
            }
            
          }
  			}, '-' ,{
  			  text: "Refresh",
	  		  iconCls:'user-job-manager-refresh',
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
			width : 210,
			margins : '3 0 3 3',
			cmargins : '3 3 3 3',
    	cm: cluster_cm,
			tbar: [{
				text:'New',
				tooltip:'Add a new row',
				iconCls:'user-job-manager-add',
				
        // add new cluster
        handler:function() {
          var cluster_name = prompt("", "My_Cluster_" + (cluster_store.getCount() + 1));
          if (cluster_name == null || cluster_name.length < 1) {
            return;
          } else if (cluster_name.indexOf(" ") != -1 || cluster_name.indexOf("\t") != -1) {
            alert("Space is not allowed in cluster name!");
            return;
          }
          Ext.Ajax.request({
            url: '/connect.php',
            params: {
              moduleId: 'user-job-manager',
              action: "addCluster",
              vcluster_name: cluster_name 
            },
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                // refresh
                cluster_store.reload();
              } else {
                // TODO when create failed
              }
            },
            failure: function(){
              // TODO when connect failed
            }
          });
        }
		  }, '-', {
				text:'Remove',
				tooltip:'Remove the selected item',
				iconCls:'user-job-manager-remove',
				
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
              moduleId: 'user-job-manager',
              action: "removeCluster",
              vcluster_cid: cluster_cid
            },
            success: function(o){
              if (o && o.responseText && Ext.decode(o.responseText).success) {
                // refresh
                cluster_store.reload();
                vm_store.removeAll();			      

    			      vm_store.proxy = new Ext.data.HttpProxy({
    		    		  url: '/connect.php?action=listVM&moduleId=user-job-manager'
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
  			iconCls:'user-job-manager-refresh',
  			handler:function(){
		      cluster_store.reload();
		      vm_store.removeAll();

		      vm_store.proxy = new Ext.data.HttpProxy({
    				url: '/connect.php?action=listVM&moduleId=user-job-manager'
    			});
  			}
  		}]
		});
		
  	function cluster_row_click() {
  	
      // XXX how to change the vmachine lists -> change the cid
      rows = cluster_pane.getSelectionModel().getSelections();

      vm_store.proxy = new Ext.data.HttpProxy({
				url: '/connect.php?action=listVM&moduleId=user-job-manager&cid=' + rows[0].data.cluster_id
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
          moduleId: 'user-job-manager',
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
				id: 'user-job-manager-win',
				title:'Cluster Manager',
				width:750,
				height:480,
				iconCls: 'user-job-manager-icon',
				shim:false,
				animCollapse:false,
				constrainHeader:true,
				layout: 'border',
				items: [right_pane, cluster_pane],
				taskbuttonTooltip: '<b>Cluster Manger</b><br>Application for managing virtual clusters'
			});
		}
		win.show();
	}
});

