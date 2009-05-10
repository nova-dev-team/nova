Ext.override(QoDesk.HadoopWizard, {

	tabWin: null,
	
	bottomBar:null,
	
	hideWindow: function() {
    //this.win.close();
    this.tabWin.close();
  },
	
	createWindow : function(){
		var desktop = this.app.getDesktop();
		var tabWin = desktop.getWindow('hadoop-wizard-win');
		
		if(!tabWin){

      tab_app = this;
      
      create_tab1 = new Ext.Panel({
        region: 'west',
        border: false,
        html:'<img src="/image/wizard.png" />'
      });
      
      create_options = [{
        fieldLabel: 'Cluster Size',
        xtype:'textfield',
        id: 'wizard_opt_csize',
        value:'4'
      }, {
        fieldLabel: 'Cluster Name',
        xtype:'textfield',
        value:'Wizard_Cluster',
        id: 'wizard_opt_cname'
      }, {
        fieldLabel: 'Cpu Count',
        xtype:'textfield',
        id: 'wizard_opt_vcpu',
        value: "1"
      }, {
        fieldLabel: 'Memory Size',
        xtype:'textfield',
        id: 'wizard_opt_mem',
        value: "512"
      }, {
        fieldLabel: 'Install Hadoop',
        xtype:'checkbox',
        soft_name: 'hadoop',
        id: 'wizard_app_hadoop'
      }, {
        fieldLabel: 'Install MPI',
        xtype:'checkbox',
        soft_name: 'mpi',
        id: 'wizard_app_mpi'
      }, {
        fieldLabel: 'Install Ganglia',
        xtype:'checkbox',
        soft_name: 'ganglia',
        id: 'wizard_app_ganglia'
      }];
      
      create_tab2 = new Ext.FormPanel({
        region: 'center',
        margins : '3 3 3 3',
        cmargins : '3 3 3 3',
        autoScroll:true,
        border: false,
        
        items: create_options,
        
        buttons: [{
          text: "Create",
          handler: function() {
            
      clu_size = Ext.get("wizard_opt_csize").dom.value;
      clu_name = Ext.get("wizard_opt_cname").dom.value;

        mem_size_val = Ext.get("wizard_opt_mem").dom.value;
        vcpu_count = Ext.get("wizard_opt_vcpu").dom.value;
      
      if (clu_name.indexOf(" ") != -1 || clu_name.indexOf("\t") != -1) {
        alert("Space is not allowed in cluster name!");
        return;
      }
      
      soft_list = "";
      if (Ext.get("wizard_app_mpi").dom.checked) {
        soft_list += "mpi\n";
      }
      
      if (Ext.get("wizard_app_hadoop").dom.checked) {
        soft_list += "hadoop\n";
      }
      
      if (Ext.get("wizard_app_ganglia").dom.checked) {
        soft_list += "ganglia\n";
      }
      
      var notifyWin = desktop.showNotification({
          html: 'Sending request...',
          title: 'Please wait'
      });
      
      Ext.Ajax.request({
        url: '/connect.php',
        params: {
          moduleId: 'hadoop-wizard',
          action: "create",
          vcluster_name: clu_name,
          vcluster_size: clu_size,
          software_list: soft_list,
          mem_size: mem_size_val,
          vcpu: vcpu_count,
          app_name: "hadoop"
        },
        
        success: function(o){
          if (o && o.responseText &&  o.responseText != "" && Ext.decode(o.responseText).success) {
              obj = Ext.decode(o.responseText);
              saveComplete('Finished', "Created new cluster " + obj.cid + "(" + obj.cname + ")");
          }
          else {
              saveComplete('Error', 'Errors encountered on the server.');
          }
        },
        failure: function(){
          saveComplete('Error', 'Lost connection to server.');
        }
      });



            function saveComplete(title, msg){
                notifyWin.setIconClass('x-icon-done');
                notifyWin.setTitle(title);
                notifyWin.setMessage(msg);
                desktop.hideNotification(notifyWin);
            }            
            
            
          }
        }, {
          text: "Exit",
          handler: function() {
            tab_app.hideWindow();
          }
        }]
      });
      
      
      
      create_tab = new Ext.Panel({
        layout:'border',
        title: "Create new cluster",
        border: false,
        items:[create_tab1, create_tab2]
      });
      
      
      var progressPane = new Ext.Panel({
        title: "Deploy Progress",
        html: "<div id='oh_my_crappy_div_should_work'><table align='center' valign='center' height=280><tr></tr>\
              <tr><td ><font size=74 color=gray>Collecting data...</font></td></tr></table></div>",
        margins : '3 3 3 3',
        cmargins : '3 3 3 3',
        autoScroll:true
      });
      
      var refresh_pid;
      
      function progress_tab_refresh() {
        my_div = Ext.get("oh_my_crappy_div_should_work");
        
        
      Ext.Ajax.request({
        url: '/connect.php',
        params: {
          moduleId: 'hadoop-wizard',
          action: "progress"
        },
        
        success: function(o){
          if (o && o.responseText != null &&  o.responseText != "" && Ext.decode(o.responseText).success) {
            var html = "<center><p><p>";
            progress_list = Ext.decode(o.responseText).progress;
            for (i = 0; i < progress_list.length; i++) {
              cid = progress_list[i].vcluster_cid;
              cname = progress_list[i].vcluster_cname;
              html += "Progress of virtual cluster " + cid + "(" + cname + ")<br>";
              html += "<table class='hadoop-wizard-progresstable'>";
              
              progrs_info = Ext.decode(progress_list[i].progress_info);
              
              vm_count = progrs_info.length;
              app_count = progrs_info[0].progress.length;
              
              html += "<tr>";
              cluster_progress = "finished";
              for (vm_i = 0; vm_i < vm_count; vm_i++) {
                vm_progress = "finished";
                
                for (k = 0; k < app_count; k++) {
                  if (progrs_info[vm_i].progress[k].indexOf("Waiting") != -1) {
                    vm_progress = "waiting";
                    break;
                  }
                }
                
                if (vm_progress == "waiting") {
                  cluster_progress = "waiting";
                }
              }
              
              html += "<td rowspan='" + (vm_count * app_count) + "' class='hadoop-wizard-" + cluster_progress + "'>"
                  + "Vcluster ID: " + cid + "<br>Vcluster Name: " +  cname + "</td>";
              
              for (vm_i = 0; vm_i < vm_count; vm_i++) {
                vm_progress = "finished";
                
                for (k = 0; k < app_count; k++) {
                  if (progrs_info[vm_i].progress[k].indexOf("Waiting") != -1) {
                    vm_progress = "waiting";
                    break;
                  }
                }
                
                  html += "<td rowspan='" + (app_count) + "' class='hadoop-wizard-" + vm_progress + "'>" + 
                     + "Vmachine IP: " + progrs_info[vm_i].ip + "<br>Vmachine Name: " + progrs_info[vm_i].node_name + "</td>";
                
                for (app_i = 0; app_i < app_count; app_i++) {
                  status = progrs_info[vm_i].progress[app_i];
                  status1 = "";
                  if (status[1] == "Waiting") {
                    status1 = "waiting";
                  } else if (status[1] == "Finished") {
                    status1 = "finished";
                  }
                  html += "<td class='hadoop-wizard-" + status1 + "'>";
                  html += status[0];
                  html += "</td>";
                  html += "</tr>";
                }
              }
              
              
              html += "</table><p><p>";
            }
            html += "</center>";
            my_div.update(html);
          }
          else {            my_div.update("Error on server");
          }
        },
        failure: function(){
          my_div.update("Failed to connect server");
        }
      });
        
      }
      
      function progress_tab_start_auto_refresh() {
        refresh_pid = setInterval(progress_tab_refresh, 1000);
      }
      
      function progress_tab_stop_auto_refresh() {
        clearInterval(refresh_pid);
      }
      
      progressPane.on("show", progress_tab_start_auto_refresh);
      
      progressPane.on("hide", progress_tab_stop_auto_refresh);
      
      
      tabPane = new Ext.TabPanel({
        activeTab:0,
        items:[create_tab, progressPane]
      });
      
      
      
      tabWin = desktop.createWindow({
          autoScroll: true,
          id: 'hadoop-wizard-win1',
          title: 'Cluster Wizard',
          width:560,
          height:360,
          iconCls: 'hadoop-wizard-icon',
          items: tabPane,
          shim:false,
          layout:"fit",
          animCollapse:false,
          constrainHeader:true,
          maximizable: false,
          taskbuttonTooltip: '<b>Cluster Wizard</b><br />Create new clusters in a few clicks'
      });
      
      tabWin.show();
      
      this.tabWin = tabWin;
      
      tabWin.on("hide", progress_tab_stop_auto_refresh);
      

 			this.tabWin = tabWin;
    }
    
    tabWin.show();

  }  
});

