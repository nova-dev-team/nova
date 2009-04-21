Ext.override(QoDesk.UserJobManager, {

	createWindow : function(){
	
		var desktop = this.app.getDesktop();
		var win = desktop.getWindow('user-job-manager-win');
		var info_pane = new Ext.Panel({
			region : 'center',
			margins : '3 0 3 3',
			cmargins : '3 3 3 3',
			split : true,
			html: "Select a virtual cluster, and choose one of its virtual machines to show the detail information."
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
//autoLoad:"True",
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
				iconCls:'demo-grid-add',
				
				
// Add a new vm
handler: function() {


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
        action: "newVM",
      vcluster_cid: cluster_cid
    },
    success: function(o){
        if (o && o.responseText && Ext.decode(o.responseText).success) {
            // refresh
          vm_store.reload();
        }
        else {
            // TODO when create failed
        }
    },
    failure: function(){
        // TODO when connect failed
    }
  });
  


				
}
				}, {
				text:'Remove',
				tooltip:'Remove the selected item',
				iconCls:'demo-grid-remove',

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
        }
        else {
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
            // refresh
          alert(Ext.decode(o.responseText).msg);
          
          // TODO change the info pane
        }
        else {
            // TODO when create failed
        }
    },
    failure: function(){
        // TODO when connect failed
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
        }
        else {
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
        action: "pauseVM",
      vm_vid: vid
    },
    success: function(o){
        if (o && o.responseText && Ext.decode(o.responseText).success) {
            // refresh
                   alert(Ext.decode(o.responseText).msg);
          
          // TODO change the info pane
        }
        else {
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
        action: "resumeVM",
      vm_vid: vid
    },
    success: function(o){
        if (o && o.responseText && Ext.decode(o.responseText).success) {
            // refresh
                   alert(Ext.decode(o.responseText).msg);
          
          // TODO change the info pane
        }
        else {
            // TODO when create failed
        }
    },
    failure: function(){
        // TODO when connect failed
    }
  });

				
}
				} , '-', {
				text : "Select All"
				}, '-' , {
				text : "Observe",
				handler: function() {
  				rows = vm_pane.getSelectionModel().getSelections();
          if (rows.length == 0) {
       
           alert("You need to select a vmachine first");
      
        } else {

          vmid = rows[0].data.vm_id;

          rows2 = cluster_pane.getSelectionModel().getSelections();
    
          cid = rows2[0].data.cluster_id;

			// for the vnc displays
			
			function createVNCwin(_cid, _vmid, _pmip) {
				desktop.createWindow({
					id: 'vnc_vm_' + _cid + "_" + _vmid , // TODO  change the win name
					title: "VNC of " + _cid + ", " + _vmid,
					width: 815,
					height: 633,
					minWidth: 640,
					minHeight: 480,
					shim: false,
					margins : '0 0 0 0',
					cmargins : '0 0 0 0',
					animeCollapse: false,
					constrainHeader: true,

					
					listeners: {
					  resize: {
					    fn: function() {
					    
					    var vncapp = Ext.get('crappyVNC');
					    var sz = this.getSize();

					    vncapp.dom.height = sz.height - 33;
					    vncapp.dom.width = sz.width - 15;
              }
					  }
					},
					
					
					html : "<applet archive='http://"
					+ _pmip
					+ ":3000/vncviewer.jar' id='crappyVNC' code='VncViewer.class' width='800' height='600'><param name='PORT' value='5900' /><param name='ENCODING' value='tight' /><param name='HOST' value='10.0.0.196'><param name='Show controls' value='no' /></applet>"
				});
			}


      var vncwin = desktop.getWindow('vnc_vm_' + cid + "_" + vmid);
			if (!vncwin) {
        createVNCwin(cid, vmid, "10.0.0.196");
        vncwin = desktop.getWindow('vnc_vm_' + cid + "_" + vmid);
			}    
			
			vncwin.show(); // TODO show it
			
}
	
        }
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
				iconCls:'demo-grid-add',
				
// XXX add cluster
handler:function() {
  
  // TODO choose cluster name
  

  
  var cluster_name = "test-TODO";
  
  
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
        }
        else {
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
				iconCls:'demo-grid-remove',

// XXX remove grid
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
            
        }
        else {
            // TODO when create failed
        }
    },
    failure: function(){
        // TODO when connect failed
    }
  });
  
}

			},'-', {text:'Refresh', handler:function(){
			
			
			      cluster_store.reload();
			      vm_store.removeAll();
			      
			      vm_store.proxy = new Ext.data.HttpProxy({
				url: '/connect.php?action=listVM&moduleId=user-job-manager'
			});
			   
//			        cluster_pane.getView().refresh(true);
//			        alert(3);
			
			}}]
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
           
    			
    			html = "TODO: detail of " + cid + ", " + vmid;
    			
    			html += "server: " + Ext.decode(o.responseText).info;
  
          	info_pane.body.update(html);
            
        }
        else {
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
			
		}
		win.show();
	}
});

