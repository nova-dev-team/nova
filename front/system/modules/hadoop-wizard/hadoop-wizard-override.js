Ext.override(QoDesk.HadoopWizard, {

	cards : [
		'hadoop-wizard-1', // settings
		'hadoop-wizard-2' // progress
	],
	
	contentPanel : null,
	
	layout: null,
	win : null,
	
	bottomBar:null,
	
	viewCard : function(card){
		this.layout.setActiveItem(card);
	},
	
	createWindow : function(){
		var desktop = this.app.getDesktop();
		var win = desktop.getWindow('hadoop-wizard-win');
		
		if(!win){
		
	    this.contentPanel = new Ext.Panel({
			    activeItem: 0,
          border: false,
			    id: 'pref-win-content',
			    items: [
          	new QoDesk.HadoopWizard.SettingPanel({owner: this, id: 'hadoop-wizard-1'}),
          	new QoDesk.HadoopWizard.ProgressPanel({owner: this, id: 'hadoop-wizard-2'})
          ],
          layout: 'card',
        });
		
		
      win = desktop.createWindow({
          autoScroll: true,
          id: 'hadoop-wizard-win',
          title: 'Hadoop Wizard Window',
          width:640,
          height:480,
          iconCls: 'hadoop-wizard-icon',
          items: this.contentPanel,
          shim:false,
          layout:"fit",
          animCollapse:false,
          constrainHeader:true,
          maximizable: false,
          taskbuttonTooltip: '<b>HadoopWizard Window</b><br />A HadoopWizard window'
      });
      
 			this.layout = this.contentPanel.getLayout();
    }
        
    win.show();
  }  
});




QoDesk.HadoopWizard.SettingPanel = function(config){
	this.owner = config.owner;
	
	QoDesk.HadoopWizard.SettingPanel.superclass.constructor.call(this, {
		autoScroll: true,
		bodyStyle: 'padding:15px',
		border: false,
		html: '<ul id="pref-nav-panel"> \
				<li> \
					<img src="'+Ext.BLANK_IMAGE_URL+'" class="icon-pref-autorun"/> \
					<a id="viewShortcuts" href="#">Shortcuts</a><br /> \
					<span>Choose which applications appear in your shortcuts.</span> \
				</li> \
				<li> \
					<img src="'+Ext.BLANK_IMAGE_URL+'" class="icon-pref-autorun"/> \
					<a id="viewAutoRun" href="#">Auto Run Apps</a><br /> \
					<span>Choose which applications open automatically once logged in.</span> \
				</li> \
				<li> \
					<img src="'+Ext.BLANK_IMAGE_URL+'" class="icon-pref-quickstart"/> \
					<a id="viewQuickstart" href="#">Quick Start Apps</a><br /> \
					<span>Choose which applications appear in your Quick Start panel.</span> \
				</li> \
				<li> \
					<img src="'+Ext.BLANK_IMAGE_URL+'" class="icon-pref-appearance"/> \
					<a id="viewAppearance" href="#">Window Color and Appearance</a><br /> \
					<span>Fine tune window color and style of your windows.</span> \
				</li> \
				<li> \
					<img src="'+Ext.BLANK_IMAGE_URL+'" class="icon-pref-wallpaper"/> \
					<a id="viewWallpapers" href="#">Desktop Background</a><br /> \
					<span>Choose from available wallpapers or colors to decorate you desktop.</span> \
				</li> \
			</ul>',
		id: config.id
	});
	
	this.actions = {
		'viewShortcuts' : function(owner){
			owner.viewCard('hadoop-wizard-2');
		},
		
		'viewAutoRun' : function(owner){
			owner.viewCard('hadoop-wizard-2');
		},
		
		'viewQuickstart' : function(owner){
	   		owner.viewCard('hadoop-wizard-2');
	   	},
	   	
	   	'viewAppearance' : function(owner){
	   		owner.viewCard('hadoop-wizard-2');
	   	},
	   	
	   	'viewWallpapers' : function(owner){
	   		owner.viewCard('hadoop-wizard-2');
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
		html: 'BAGA',
		id: config.id
	});
	
	this.actions = {
		
	};
};


Ext.extend(QoDesk.HadoopWizard.ProgressPanel, Ext.Panel, {
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

