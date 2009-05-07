Ext.override(QoDesk.HadoopWizard, {

	cards : [
		'hadoop-wizard-1', // settings
		'hadoop-wizard-2' // progress
	],
	
	contentPanel : null,
	
	currentCard : "",
	
	layout: null,
	win : null,
	
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
	
	this.basic_html = "<img src='/image/hadoop-logo.jpg'><p>\
		  Cluster Size:<input id='hadoop-wiz-clu-size' value='4'></input><p>\
		  Cluster Name:<input id='hadoop-wiz-clu-name' value='Hadoop_Cluster'></input><p>\
		  <div align='center'><a id='advanced' href='#'>Show advanced options</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\
		  <a id='next' href='#'>Next</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a id='exit' href='#'>Exit</a></div>";
		  
	this.advanced_html = "<img src='/image/hadoop-logo.jpg'><p>\
		  Cluster Size:<input id='hadoop-wiz-clu-size' value='4'></input><p>\
		  Cluster Name:<input id='hadoop-wiz-clu-name' value='Hadoop_Cluster'></input><p>\
 		  Memory Size:<input id='hadoop-wiz-mem-size' value='512'></input><p>\
 		  Cpu Count:<input id='hadoop-wiz-vcpu' value='1'></input><p>\
		  <div align='center'><a id='basic' href='#'>Show basic options</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\
		  <a id='next' href='#'>Next</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a id='exit' href='#'>Exit</a></div>";
	
	QoDesk.HadoopWizard.SettingPanel.superclass.constructor.call(this, {
		autoScroll: true,
		bodyStyle: 'padding:15px',
		border: false,
		html: this.basic_html,
		id: config.id
	});
	
	helper = this;
	
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
      mem_size_val = input_mem_size.dom.value;
      vcpu_count = input_vcpu.dom.value;
      
      
      if (clu_name.indexOf(" ") != -1 || clu_name.indexOf("\t") != -1) {
        alert("Space is not allowed in cluster name!");
        return;
      }
      
      soft_list = ["hadoop", "ganglia"];
      
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
          alert(o.responseText);
          owner.viewCard('hadoop-wizard-2');
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
		id: config.id
	});


	this.actions = {
		
	};

	
};


Ext.extend(QoDesk.HadoopWizard.ProgressPanel, Ext.Panel, {

  startAutorefresh: function() {
    this.autorefreshProcId = setInterval(this.freshFunc,3 * 1000);
  },
  
  freshFunc: function () {
    alert("TODO: autorefresh");
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

