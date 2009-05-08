/* Override the module code here.
 * This code will be Loaded on Demand.
 */

Ext.override(QoDesk.AdminPclusterMon, {
	
createWindow : function(){
  var desktop = this.app.getDesktop();
  var win = desktop.getWindow(this.moduleId);
  
  if(!win){
  	var winWidth = 800;//desktop.getWinWidth() / 1.1;
		var winHeight = 600;//desktop.getWinHeight() / 1.1;
		
		my_tab_pane = new Ext.TabPanel({
      activeTab:0,
      items: [make_chart("Disk"), make_chart("Network"), make_chart("NFS"), make_chart("Processes"), make_chart("System"), make_chart("Virtual Machines")]
    });
    
    win = desktop.createWindow({
      id: this.moduleId,
      title: 'Charts',
      width: winWidth,
      height: winHeight,
      iconCls: 'superadmin-manual-icon',
      shim: false,
      constrainHeader: true,
      layout: 'fit',
      items: [my_tab_pane],
      taskbuttonTooltip: '<b>Clusters Mon</b><br />Collect detail info of the clusters'
      });
    }
  win.show();
}
});



function get_charts(measure_name, pm_ip) {
  pm_host = hostname_mapping(pm_ip);
  
  var munin_prefix = "http://10.0.0.220/cgi-bin/munin-cgi-graph/localdomain/";
  
  html = "";
  
  function get_img(img_item_name) {
    return "<img src='" + munin_prefix + pm_host + "/" + img_item_name + ".png' /><p>";
  }
  
  if (measure_name == "Disk") {
    html += get_img("df-day");
    html += get_img("df_inode-day");
    html += get_img("iostat-day");
  } else if (measure_name == "Network") {
    html += get_img("if_err_eth0-day");
    html += get_img("if_err_eth1-day");
    html += get_img("if_eth0-day");
    html += get_img("if_eth1-day");
    html += get_img("netstat-day");
  } else if (measure_name == "NFS") {
    html += get_img("nfs_client-day");
  } else if (measure_name == "Processes") {
    html += get_img("forks-day");
    html += get_img("processes-day");
    html += get_img("vmstat-day");
  } else if (measure_name == "System") {
    html += get_img("cpu-day");
    html += get_img("entropy-day");
    html += get_img("interrupts-day");
    html += get_img("irqstats-day");
    html += get_img("load-day");
    html += get_img("memory-day");
    html += get_img("open_files-day");
    html += get_img("open_inodes-day");
    html += get_img("swap-day");
  } else if (measure_name == "Virtual Machines") {
    html += get_img("libvirt_blkstat-day");
    html += get_img("libvirt_cputime-day");
    html += get_img("libvirt_ifstat-day");
    html += get_img("libvirt_mem-day");
  }
  
  return html;
}


function make_chart(measure_name) {


  var default_chart_message = "Select a virtual cluster, and choose one of its virtual machines to show the detail information.";
  
  var chart_pane = new Ext.Panel({
		region : 'center',
		margins : '3 3 3 3',
		cmargins : '3 3 3 3',
		split : true,
    autoScroll:true,
		html: default_chart_message
	})
	
	
  var cluster_store = new Ext.data.JsonStore({
		autoLoad:"True",
		root: 'pm_list', // JSON root
		// the JSON from server is like this:
		/*
		    {xxx:yyy, zzz:www, all_clusters:[{cluster_id:???, cluster_name:???}, {cluster_id:???, cluster_name:???}...{cluster_id:???, cluster_name:???}]}
		*/
//			totalProperty: 'totalCount',
		idProperty: 'pm_ip', // the item that serves as "PRIMARY KEY"
		//remoteSort: true,
		fields: ["pm_ip"],
		// load using script tags for cross domain, if the data in on the same domain as
		// this page, an HttpProxy would be better
		proxy: new Ext.data.HttpProxy({
			url: '/connect.php?action=workingPM&moduleId=admin-pmachine'
		})
	});
//		cluster_store.setDefaultSort('email', 'desc');

	var cluster_cm = new Ext.grid.ColumnModel([{
		header: "Pmachine IP",
		width: 100,
		dataIndex: 'pm_ip',
		sortable: true
		}
	]);
	
	
	var cluster_pane = new Ext.grid.GridPanel({

		region : "west",
		
  	store:cluster_store,
		split : true,
		loadMask: true,
		width : 105,
		margins : '3 0 3 3',
		cmargins : '3 3 3 3',
  	cm: cluster_cm,
		tbar: [{
		  text:'Refresh',
			iconCls:'admin-monitor-refresh',
			handler:function(){
	      cluster_store.reload();
	      chart_pane.body.update(default_chart_message);
  // TODO
			}
		}]
	});
	
	function cluster_row_click() {
	
    // XXX how to change the vmachine lists -> change the cid
    rows = cluster_pane.getSelectionModel().getSelections();

    html = "<h1>&nbsp;&nbsp;&nbsp;&nbsp;Measure of " + measure_name + " on physical machine " + rows[0].data.pm_ip + "</h1><p>";
    
    html += get_charts(measure_name, rows[0].data.pm_ip);
    chart_pane.body.update(html);
  }
	
	cluster_pane.addListener("rowclick", cluster_row_click);
  
  measure_tab = {
  	autoScroll: true,
    title: measure_name,
    header: false,
		layout: 'border',
    items: [cluster_pane, chart_pane],
	  border: false,
  };
  
  return measure_tab;
}

// map a ip address to hostname
function hostname_mapping(ip_addr) {
  if (ip_addr.indexOf(".12") != -1) {
    return "node12";
  } else if (ip_addr.indexOf(".13") != -1) {
    return "node13";
  } else if (ip_addr.indexOf(".16") != -1) {
    return "node16";
  } else {
    return ip_addr;
  }
}
