Ext.override(QoDesk.BogusWindow, {
	
	detailModule : null,
	
	init : function(){
		this.detailModule = new BogusDetailModule();
	},
	
	createWindow : function(){
		var desktop = this.app.getDesktop();
		var win = desktop.getWindow('bogus-win');
		
		if(!win){
            win = desktop.createWindow({
                autoScroll: true,
                id: 'bogus-win',
                title: 'Bogus Window',
                width:640,
                height:480,
                iconCls: 'bogus-icon',
                items: new QoDesk.BogusWindow.NavPanel({owner: this, id: 'nav-panel'}),
                shim:false,
                animCollapse:false,
                constrainHeader:true,
                maximizable: false,
                tbar: [{
                	handler: this.showDialog,
                	scope: this,
                	text: 'Open Dialog'
                }],
                taskbuttonTooltip: '<b>Bogus Window</b><br />A bogus window'
            });
        }
        
        win.show();
    },
    
    openDetail : function(id){
		this.detailModule.createWindow(this.app, id);
    },
    
    showDialog : function(){
    	var winManager = this.app.getDesktop().getManager();
    	
    	if(!this.dialog){
            this.dialog = new Ext.Window({
            	bodyStyle:'padding:10px',
                layout:'fit',
                width:500,
                height:300,
                closeAction:'hide',
                plain: true,
                html: 'Bogus dialog window',
                buttons: [{
                    text:'Submit',
                    disabled:true
                },{
                    text: 'Close',
                    handler: function(){
                        this.dialog.hide();
                    },
                    scope: this
                }],
                manager: winManager,
                modal: true
            });
        }
        this.dialog.show();
    }
});



QoDesk.BogusWindow.NavPanel = function(config){
	this.owner = config.owner;
	
	QoDesk.BogusWindow.NavPanel.superclass.constructor.call(this, {
		autoScroll: true,
		bodyStyle: 'padding:15px',
		border: false,
		html: '<ul id="bogus-nav-panel"> \
				<li> \
					<a id="openDetailOne" href="#">Detail 1</a><br /> \
					<span>Open detail window one.</span> \
				</li> \
				<li> \
					<a id="openDetailTwo" href="#">Detail 2</a><br /> \
					<span>Open detail window two.</span> \
				</li> \
				<li> \
					<a id="openDetailThree" href="#">Detail 3</a><br /> \
					<span>Open detail window three.</span> \
				</li> \
			</ul>',
		id: config.id
	});
	
	this.actions = {
		'openDetailOne' : function(owner){
			owner.openDetail(1);
		},
		
		'openDetailTwo' : function(owner){
			owner.openDetail(2);
		},
		
		'openDetailThree' : function(owner){
	   		owner.openDetail(3);
	   	}
	};
};

Ext.extend(QoDesk.BogusWindow.NavPanel, Ext.Panel, {
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
		
		QoDesk.BogusWindow.NavPanel.superclass.afterRender.call(this); // do sizing calcs last
	},
	
	doAction : function(e, t){
    	e.stopEvent();
    	this.actions[t.id](this.owner);  // pass owner for scope
    }
});



BogusDetailModule = Ext.extend(Ext.app.Module, {

	moduleType : 'demo',
	moduleId : 'demo-bogus-detail',
	
	init : function(){
		this.launcher = {
			handler: this.createWindow,
			iconCls: 'bogus-icon',
			scope: this,
			shortcutIconCls: 'demo-bogus-shortcut',
			text: 'Bogus Detail Window',
			tooltip: '<b>Bogus Detail Window</b><br />A bogus detail window'
		}
	},

	createWindow : function(app, id){
		this.moduleId = 'demo-bogus-detail-'+id;
		
		var desktop = app.getDesktop();
		var win = desktop.getWindow('bogus-detail'+id);
		
        if(!win){
            win = desktop.createWindow({
                id: 'bogus-detail'+id,
                title: 'Detail Window '+id,
                width: 540,
                height: 380,
                html : '<p>Something useful would be in here.</p>',
                iconCls: 'bogus-icon',
                shim:false,
                animCollapse:false,
                constrainHeader:true
            });
        }
        win.show();
    }
});