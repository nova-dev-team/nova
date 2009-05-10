Ext.override(QoDesk.HadoopWizard, {

	cards : [
		'hadoop-wizard-1', // settings
		'hadoop-wizard-2' // progress
	],
	
	contentPanel : null,
	
	currentCard : "",
	
	layout: null,
	win : null,
	
	cid:null,
	cname:null,
	
	bottomBar:null,
	
	
	progressPane: null,
	
	viewCard : function(card){
		this.layout.setActiveItem(card);    this.currentCard = card;
    
    if (card == "hadoop-wizard-2" && this.win.isVisible()) {
      this.progressPane.startAutorefresh();
    } else {
      this.progressPane.stopAutorefresh();
    }
	},
	
	hideWindow: function() {
    this.win.close();
    this.currentCardId = "";
  },
	
	createWindow : function(){
		var desktop = this.app.getDesktop();
		var win = desktop.getWindow('hadoop-wizard-win');
		
		if(!win){

      this.progressPane = new QoDesk.HadoopWizard.ProgressPanel({owner: this, id: 'hadoop-wizard-2'});
		
	    this.contentPanel = new Ext.Panel({
		    activeItem: 0,
        border: false,
		    id: 'pref-win-content',
		    items: [
        	new QoDesk.HadoopWizard.SettingPanel({owner: this, id: 'hadoop-wizard-1'}),
        	this.progressPane
        ],
        layout: 'card',
      });
		
      win = desktop.createWindow({
          autoScroll: true,
          id: 'hadoop-wizard-win',
          title: 'Cluster Wizard',
          width:480,
          height:260,
          iconCls: 'hadoop-wizard-icon',
          items: this.contentPanel,
          shim:false,
          layout:"fit",
          animCollapse:false,
          constrainHeader:true,
          maximizable: false,
          taskbuttonTooltip: '<b>Cluster Wizard</b><br />Create new clusters in a few clicks'
      });
      
      progressP = this.progressPane;
      win.on("hide", function() {progressP.stopAutorefresh()});
      
 			this.layout = this.contentPanel.getLayout();
 			this.win = win;
    }
        
    win.show();
    this.viewCard("hadoop-wizard-1");
  }  
});




QoDesk.HadoopWizard.SettingPanel = function(config){
	this.owner = config.owner;
	
 	helper = this;
	
	Ext.Ajax.request({
    url: '/connect.php',
    params: {
      moduleId: 'hadoop-wizard',
      action: "soft_list"
    },
    
    success: function(o){
      
      var list = Ext.decode(o.responseText);
      
      var html = "<div align='center'>Softwares to install: &nbsp;<input id='generated_soft_to_install_count' type='hidden' value='" + list.length + "'>";
      
      for (i = 0; i < list.length; i++) {
        html += "<input type='checkbox' id='generated_soft_to_install_" + i + "'> &nbsp;" + list[i] + "</input> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        html += "<input type='hidden' id='generated_soft_name_" + i + "' value='" + list[i] +"'/>";
      }
      
      html += '<p></div>';
    
      helper.basic_html = html  + helper.basic_html;
      helper.advanced_html = html  + helper.advanced_html;
      helper.body.update(helper.basic_html);
    },
    failure: function(){
      // TODO when connect failed
    }
  });
  

	
	this.basic_html = "<div align='center'>Cluster Size:<input id='hadoop-wiz-clu-size' value='4'></input><p></div>\
		  <div align='center'>Cluster Name:<input id='hadoop-wiz-clu-name' value='Wizard_Generated_Cluster'></input><p></div>\
		  <div align='center'><a id='advanced' href='#'>Show advanced options</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\
		  <a id='next' href='#'>Next</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a id='exit' href='#'>Exit</a></div>";
		  
	this.advanced_html = "<div align='center'>Cluster Size:<input id='hadoop-wiz-clu-size' value='4'></input><p></div>\
		  <div align='center'>Cluster Name:<input id='hadoop-wiz-clu-name' value='Wizard_Generated_Cluster'></input><p></div>\
 		  <div align='center'>Memory Size:<input id='hadoop-wiz-mem-size' value='512'></input><p></div>\
 		  <div align='center'>Cpu Count:<input id='hadoop-wiz-vcpu' value='1'></input><p></div>\
		  <div align='center'><a id='basic' href='#'>Show basic options</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\
		  <a id='next' href='#'>Next</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a id='exit' href='#'>Exit</a></div>";
	
	QoDesk.HadoopWizard.SettingPanel.superclass.constructor.call(this, {
		autoScroll: true,
		bodyStyle: 'padding:15px',
		border: false,
		html: this.basic_html,
		id: config.id
	});
	

	
  this.setHeight(260);
	
	this.actions = {
  	'basic' : function(owner) {
      helper.body.update(helper.basic_html);
      owner.win.setHeight(260);
    },
    
	  'advanced' : function(owner) {
      helper.body.update(helper.advanced_html);
      owner.win.setHeight(340);
    },
	
		'next' : function(owner){
      input_size = Ext.get("hadoop-wiz-clu-size");
      input_name = Ext.get("hadoop-wiz-clu-name");
      input_mem_size = Ext.get("hadoop-wiz-mem-size");
      input_vcpu = Ext.get("hadoop-wiz-vcpu");
      clu_size = input_size.dom.value;
      clu_name = input_name.dom.value;
      if (input_mem_size != null)
        mem_size_val = input_mem_size.dom.value;
      else
        mem_size_val = 512;
      if (input_vcpu != null)
        vcpu_count = input_vcpu.dom.value;
      else
        vcpu_count = 1;
      
      if (clu_name.indexOf(" ") != -1 || clu_name.indexOf("\t") != -1) {
        alert("Space is not allowed in cluster name!");
        return;
      }
      
      soft_list = [];
      
      chbox_count = Ext.get("generated_soft_to_install_count").dom.value;
      
      for (i = 0; i < chbox_count; i++) {
        chbox = Ext.get("generated_soft_to_install_" + i);
        if (chbox.dom.checked) {
          soft_list.push(Ext.get("generated_soft_name_" + i).dom.value);
        }
      }
      
      soft_list_req = "";
      for (i = 0; i < soft_list.length; i++) {
        soft_list_req += soft_list[i];
        if (i < soft_list.length - 1) {
          soft_list_req += '\n';
        }
      }
      
      Ext.Ajax.request({
        url: '/connect.php',
        params: {
          moduleId: 'hadoop-wizard',
          action: "create",
          vcluster_name: clu_name,
          vcluster_size: clu_size,
          software_list: soft_list_req,
          mem_size: mem_size_val,
          vcpu: vcpu_count,
          app_name: "hadoop"
        },
        
        success: function(o){
          //alert(o.responseText);
          owner.viewCard('hadoop-wizard-2');
          helper.owner.cid = Ext.decode(o.responseText).cid;
          helper.owner.cname = Ext.decode(o.responseText).cname;
        },
        failure: function(){
          // TODO when connect failed
        }
      });
      

		},
		
		'exit' : function(owner){
      owner.hideWindow();
		}
	};
};

Ext.extend(QoDesk.HadoopWizard.SettingPanel, Ext.Panel, {
	afterRender : function(){
		this.body.on({
			'mousedown': {
				fn: this.doAction,
				scope: this,
				delegate: 'a'
			},
			'click': {
				fn: Ext.emptyFn,
				scope: null,
				delegate: 'a',
				preventDefault: true
			}
		});
		
		QoDesk.HadoopWizard.SettingPanel.superclass.afterRender.call(this); // do sizing calcs last
	},
	
	doAction : function(e, t){
    	e.stopEvent();
    	this.actions[t.id](this.owner);  // pass owner for scope
    }
});





QoDesk.HadoopWizard.ProgressPanel = function(config){
	this.owner = config.owner;
	
	
	QoDesk.HadoopWizard.ProgressPanel.superclass.constructor.call(this, {
		autoScroll: true,
		bodyStyle: 'padding:15px',
		border: false,
		html: '', // this will be updated automatically by the following function
		id: config.id,
		freshFunc: function() {
		  alert("HI");
    }
	});
	
	
	this.refresh_counter = 0;
	
	helper123 = this;

	this.freshFunc2 = function () {
	
    if (helper123.owner.win.hidden) {
      clearInterval(helper123.refreshing_proc_id);
      return;
    } else if (helper123.owner.currentCard != "hadoop-wizard-2") {
      return;
    }
    
    var vc_cid = helper123.owner.cid;
    var vc_name = helper123.owner.cname;
    
    var htmltxt = "<div><h2>Deploying progress of " + vc_cid + "(" + vc_name + "):</h2></div><p>";

    Ext.Ajax.request({
      url: '/connect.php',
      params: {
        moduleId: 'hadoop-wizard',
        action: "progress",
        vcluster_cid: vc_cid
      },
      
      success: function(o){
//        htmltxt += o.responseText;
//        htmltxt += "<p>";
        // TODO pretty table
        htmltxt += "<table><tr><td>Vmachine</td><td>Progress</td></tr>";
        resultObj = Ext.decode(o.responseText);
//        console.log(resultObj);
        for (i = 0; i < resultObj.length; i++) {
          htmltxt += "<tr>";
          htmltxt += "<td>";
          htmltxt += resultObj[i].node_name + " (ip =&gt; " + resultObj[i].ip + ")";
          htmltxt += "</td>";
          
          htmltxt += "<td>";

          for (j = 0; j < resultObj[i].progress.length; j++) {
            if (resultObj[i].progress[j][1] == "Waiting") {
              htmltxt += resultObj[i].progress[j][0] + " is <font color='red'>" + resultObj[i].progress[j][1] + "</font><br>";
            } else {
              htmltxt += resultObj[i].progress[j][0] + " is <font color='green'>" + resultObj[i].progress[j][1] + "</font><br>";
            }
          }
          htmltxt += "</td>";
          
          htmltxt += "</tr>";
        }
        
        htmltxt += "</table>";
        
        helper123.body.update(htmltxt);
        
      },
      failure: function(){
        // TODO when connect failed
      }
    });
    
  },
  
	this.refreshing_proc_id = setInterval(this.freshFunc2, 3000);


	this.actions = {
		
	};

	
};


Ext.extend(QoDesk.HadoopWizard.ProgressPanel, Ext.Panel, {

  startAutorefresh: function() {
    this.autorefreshProcId = setInterval(this.freshFunc1xxxxx,3 * 1000);
  },
    
  freshFunc1xxxxx: function() {

  },

  
  autorefreshProcId:null,
  
  stopAutorefresh: function() {
    if (this.autorefreshProcId) {
      clearInterval(this.autorefreshProcId);
    }
    this.autorefreshProcId = null;
  },

	afterRender : function(){
		this.body.on({
			'mousedown': {
				fn: this.doAction,
				scope: this,
				delegate: 'a'
			},
			'click': {
				fn: Ext.emptyFn,
				scope: null,
				delegate: 'a',
				preventDefault: true
			}
		});
		
		QoDesk.HadoopWizard.ProgressPanel.superclass.afterRender.call(this); // do sizing calcs last
		
	},
	
	doAction : function(e, t){
    	e.stopEvent();
    	this.actions[t.id](this.owner);  // pass owner for scope
    }
});


