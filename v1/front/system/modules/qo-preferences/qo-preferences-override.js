/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

Ext.override(QoDesk.QoPreferences, {

	actions : null,
	cards : [
		'pref-win-card-1', // navigation
		'pref-win-card-2', // quickstart
		'pref-win-card-3', // color and appearance
		'pref-win-card-4', // wallpaper
		'pref-win-card-5', // autorun
		'pref-win-card-6'  // shortcuts
	],
	contentPanel : null,
	cardHistory : [
		'pref-win-card-1' // default
	],
	layout: null,
	win : null,

    createWindow : function(){
    	var desktop = this.app.getDesktop();
        this.win = desktop.getWindow(this.moduleId);
        
        if(!this.win){
        	var winWidth = 610;
			var winHeight = 460;
			
			this.contentPanel = new Ext.Panel({
				activeItem: 0,
                border: false,
				id: 'pref-win-content',
				items: [
                	new QoDesk.QoPreferences.NavPanel({owner: this, id: 'pref-win-card-1'}),
                	new QoDesk.QoPreferences.Shortcuts({owner: this, id: 'pref-win-card-6'}),
                	new QoDesk.QoPreferences.AutoRun({owner: this, id: 'pref-win-card-5'}),
                	new QoDesk.QoPreferences.QuickStart({owner: this, id: 'pref-win-card-2'}),
                	new QoDesk.QoPreferences.Appearance({owner: this, id: 'pref-win-card-3'}),
                	new QoDesk.QoPreferences.Background({owner: this, id: 'pref-win-card-4'})
                ],
				layout: 'card',
				tbar: [{
					disabled: true,
                	handler: this.navHandler.createDelegate(this, [-1]),
                	id: 'back',
                	scope: this,
                	text: 'Back'
                },{
                	disabled: true,
                	handler: this.navHandler.createDelegate(this, [1]),
                	id: 'next',
                	scope: this,
                	text: 'Next'
                }]
			});
			
            this.win = desktop.createWindow({
            	animCollapse: false,
                constrainHeader: true,
                id: this.moduleId,
                height: winHeight,
                iconCls: 'pref-icon',
                items: this.contentPanel,
                layout: 'fit',
                shim: false,
                taskbuttonTooltip: '<b>Preferences</b><br />Allows you to modify your desktop',
                title: 'Preferences',
                width: winWidth
            });
            
			this.layout = this.contentPanel.getLayout();
        }
        
        this.win.show();
    },
    
    handleButtonState : function(){
    	var cards = this.cardHistory, activeId = this.layout.activeItem.id,
    		items = this.contentPanel.getTopToolbar().items, back = items.get(0), next = items.get(1);
    	
    	for(var i = 0, len = cards.length; i < len; i++){
    		if(cards[i] === activeId){
    			if(i <= 0){
    				back.disable();
    				next.enable();
    			}else if(i >= (len-1)){
    				back.enable();
    				next.disable();
    			}else{
    				back.enable();
    				next.enable();
    			}
    			break;
    		}
    	}
    },
    
    navHandler : function(index){
    	var cards = this.cardHistory,
    		activeId = this.layout.activeItem.id,
    		nextId;
    	
    	for(var i = 0, len = cards.length; i < len; i++){
    		if(cards[i] === activeId){
    			nextId = cards[i+index];
    			break;
    		}
    	}
    	
    	this.layout.setActiveItem(nextId);
    	this.handleButtonState();
    },
    
    save : function(params){    	
    	var desktop = this.app.getDesktop();
    	var notifyWin = desktop.showNotification({
			html: 'Saving your data...'
			, title: 'Please wait'
		});
	    var callback = params.callback || null;
		var callbackScope = params.callbackScope || this;
		
		params.moduleId = this.moduleId;
		
	    Ext.Ajax.request({
			url: this.app.connection,
			/* Could also pass moduleId and action in querystring like this
			 * instead of in the params config option.
			 *
			 * url: this.app.connection+'?moduleId='+this.id+'&action=myAction', */
			params: params,
			success: function(o){
				if(o && o.responseText && Ext.decode(o.responseText).success){
					saveComplete('Finished', 'Save complete.');
				}else{
					saveComplete('Error', 'Errors encountered on the server.');
				}
			},
			failure: function(){
				saveComplete('Error', 'Lost connection to server.');
			},
			scope: this
		});
		
		function saveComplete(title, msg){
			notifyWin.setIconClass('x-icon-done');
			notifyWin.setTitle(title);
			notifyWin.setMessage(msg);
			desktop.hideNotification(notifyWin);
			
			if(callback){
				callback.call(callbackScope);
			}
		}
	},
    
    viewCard : function(card){
		this.layout.setActiveItem(card);
	    if(this.cardHistory.length > 1){
	    	this.cardHistory.pop();
	    }
	    this.cardHistory.push(card);
	    this.handleButtonState();
	}
});



QoDesk.QoPreferences.NavPanel = function(config){
	this.owner = config.owner;
	
	QoDesk.QoPreferences.NavPanel.superclass.constructor.call(this, {
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
			owner.viewCard('pref-win-card-6');
		},
		
		'viewAutoRun' : function(owner){
			owner.viewCard('pref-win-card-5');
		},
		
		'viewQuickstart' : function(owner){
	   		owner.viewCard('pref-win-card-2');
	   	},
	   	
	   	'viewAppearance' : function(owner){
	   		owner.viewCard('pref-win-card-3');
	   	},
	   	
	   	'viewWallpapers' : function(owner){
	   		owner.viewCard('pref-win-card-4');
	   	}
	};
};

Ext.extend(QoDesk.QoPreferences.NavPanel, Ext.Panel, {
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
		
		QoDesk.QoPreferences.NavPanel.superclass.afterRender.call(this); // do sizing calcs last
	},
	
	doAction : function(e, t){
    	e.stopEvent();
    	this.actions[t.id](this.owner);  // pass owner for scope
    }
});



QoDesk.QoPreferences.AutoRun = function(config){
	this.owner = config.owner;
	this.app = this.owner.app;
	
	var ms = this.app.modules,
		ids = this.app.launchers.autorun,
		nodes = expandNodes(ms, ids);
				
	QoDesk.QoPreferences.AutoRun.superclass.constructor.call(this, {
		autoScroll: true,
		bodyStyle: 'padding:10px',
		border: false,
		buttons: [{
			disabled: this.app.isAllowedTo('saveAutorun', this.owner.moduleId) ? false : true,
			handler: onSave,
			scope: this,
			text: 'Save'
		},{
			handler: onClose,
			scope: this,
			text: 'Close'
		}],
		cls: 'pref-card pref-check-tree',
		id: config.id,
		lines: false,
		listeners: {
			'checkchange': {
				fn: onCheckChange,
				scope: this
			}
		},
		loader: new Ext.tree.TreeLoader(),
		rootVisible: false,
		root: new Ext.tree.AsyncTreeNode({
			text: 'Auto Run Apps',
			children: nodes
		}),
		title: 'Auto Run Apps'
	});
	
	new Ext.tree.TreeSorter(this, {dir: "asc"});
			
	function expandNodes(ms, ids){
		var nodes = [];
		
		for(var i = 0, len = ms.length; i < len; i++){
			if(ms[i].moduleType === 'menu'){
				/* nodes.push({
					leaf: false,
					text: ms[i].launcher.text,
					children: this.expandNodes(o.menu.items, ids)
				}); */
			}else{
				nodes.push({
		           	checked: isChecked(ms[i].moduleId, ids) ? true : false,
		           	iconCls: ms[i].launcher.iconCls,
		           	id: ms[i].moduleId,
		           	leaf: true,
		           	selected: true,
		           	text: ms[i].launcher.text
				});
			}
		}
		
		return nodes;
	}
	
	function isChecked(id, ids){
		for(var i = 0, len = ids.length; i < len; i++){
			if(id == ids[i]){
				return true;
			}
		}
	}

	function onCheckChange(node, checked){
		if(node.leaf && node.id){
    		if(checked){
				this.app.desktop.addAutoRun(node.id, true);
    		}else{
				this.app.desktop.removeAutoRun(node.id, true);
    		}
    	}
    	node.ownerTree.selModel.select(node);
    }
    
    function onClose(){
		this.owner.win.close();
	}
	
    function onSave(){
    	this.buttons[0].disable();
    	this.owner.save({
    		action: 'saveAutorun'
    		, callback: function(){
    			this.buttons[0].enable();
    		}
    		, callbackScope: this
    		, ids: Ext.encode(this.app.launchers.autorun)
    	});
    }
};

Ext.extend(QoDesk.QoPreferences.AutoRun, Ext.tree.TreePanel);



QoDesk.QoPreferences.Shortcuts = function(config){
	this.owner = config.owner;
	this.app = this.owner.app;
	
	var ms = this.app.modules,
		ids = this.app.launchers.shortcut,
		nodes = expandNodes(ms, ids);
	
	QoDesk.QoPreferences.Shortcuts.superclass.constructor.call(this, {
		autoScroll: true,
		bodyStyle: 'padding:10px',
		border: false,
		buttons: [{
			disabled: this.app.isAllowedTo('saveShortcut', this.owner.moduleId) ? false : true,
			handler: onSave,
			scope: this,
			text: 'Save'
		},{
			handler: onClose,
			scope: this,
			text: 'Close'
		}],
		cls: 'pref-card pref-check-tree',
		id: config.id,
		lines: false,
		listeners: {
			'checkchange': {
				fn: onCheckChange,
				scope: this
			}
		},
		loader: new Ext.tree.TreeLoader(),
		rootVisible: false,
		root: new Ext.tree.AsyncTreeNode({
			text: 'Shortcuts',
			children: nodes
		}),
		title: 'Shortcuts'
	});
	
	new Ext.tree.TreeSorter(this, {dir: "asc"});
			
	function expandNodes(ms, ids){
		var nodes = [];
		
		for(var i = 0, len = ms.length; i < len; i++){
			if(ms[i].moduleType === 'menu'){
				/* nodes.push({
					leaf: false,
					text: ms[i].launcher.text,
					children: this.expandNodes(o.menu.items, ids)
				}); */
			}else{
				nodes.push({
		           	checked: isChecked(ms[i].moduleId, ids) ? true : false,
		           	iconCls: ms[i].launcher.iconCls,
		           	id: ms[i].moduleId,
		           	leaf: true,
		           	selected: true,
		           	text: ms[i].launcher.text
				});
			}
		}
		
		return nodes;
	}
	
	function isChecked(id, ids){
		for(var i = 0, len = ids.length; i < len; i++){
			if(id == ids[i]){
				return true;
			}
		}
	}

	function onCheckChange(node, checked){
		if(node.leaf && node.id){
    		if(checked){
				this.app.desktop.addShortcut(node.id, true);
    		}else{
				this.app.desktop.removeShortcut(node.id, true);
    		}
    	}
    	node.ownerTree.selModel.select(node);
    }
    
    function onClose(){
		this.owner.win.close();
	}
	
    function onSave(){
    	this.buttons[0].disable();
    	this.owner.save({
    		action: 'saveShortcut'
    		, callback: function(){
    			this.buttons[0].enable();
    		}
    		, callbackScope: this
    		, ids: Ext.encode(this.app.launchers.shortcut)
    	});
    }
};

Ext.extend(QoDesk.QoPreferences.Shortcuts, Ext.tree.TreePanel);



QoDesk.QoPreferences.QuickStart = function(config){
	this.owner = config.owner;
	this.app = this.owner.app;
	
	var ms = this.app.modules,
		ids = this.app.launchers.quickstart,
		nodes = expandNodes(ms, ids);
				
    QoDesk.QoPreferences.QuickStart.superclass.constructor.call(this, {
    	autoScroll: true,
		bodyStyle: 'padding:10px',
		border: false,
		buttons: [{
			disabled: this.app.isAllowedTo('saveQuickstart', this.owner.moduleId) ? false : true,
			handler: onSave,
			scope: this,
			text: 'Save'
		},{
			handler: onClose,
			scope: this,
			text: 'Close'
		}],
		cls: 'pref-card pref-check-tree',
		id: config.id,
		lines: false,
		listeners: {
			'checkchange': {
				fn: onCheckChange,
				scope: this
			}
		},
		loader: new Ext.tree.TreeLoader(),
		rootVisible: false,
		root: new Ext.tree.AsyncTreeNode({
			text: 'Quick Start Apps',
			children: nodes
		}),
		title: 'Quick Start Apps'
    });
    
    new Ext.tree.TreeSorter(this, {dir: "asc"});
			
	function expandNodes(ms, ids){
		var nodes = [];
		
		for(var i = 0, len = ms.length; i < len; i++){
			if(ms[i].moduleType === 'menu'){
				/* nodes.push({
					leaf: false,
					text: ms[i].launcher.text,
					children: this.expandNodes(o.menu.items, ids)
				}); */
			}else{
				nodes.push({
		           	checked: isChecked(ms[i].moduleId, ids) ? true : false,
		           	iconCls: ms[i].launcher.iconCls,
		           	id: ms[i].moduleId,
		           	leaf: true,
		           	selected: true,
		           	text: ms[i].launcher.text
				});
			}
		}
		
		return nodes;
	}
	
	function isChecked(id, ids){
		for(var i = 0, len = ids.length; i < len; i++){
			if(id == ids[i]){
				return true;
			}
		}
	}
	
	function onCheckChange(node, checked){
		if(node.leaf && node.id){
    		if(checked){
				this.app.desktop.addQuickStartButton(node.id, true);
    		}else{
				this.app.desktop.removeQuickStartButton(node.id, true);
    		}
    	}
    	node.ownerTree.selModel.select(node);
    }
    
    function onClose(){
		this.owner.win.close();
	}
	
    function onSave(){
    	this.buttons[0].disable();
    	this.owner.save({
    		action: 'saveQuickstart'
    		, callback: function(){
    			this.buttons[0].enable();
    		}
    		, callbackScope: this
    		, ids: Ext.encode(this.app.launchers.quickstart)
    	});
    }
};

Ext.extend(QoDesk.QoPreferences.QuickStart, Ext.tree.TreePanel);



QoDesk.QoPreferences.Appearance = function(config){
	this.owner = config.owner;
	this.app = this.owner.app;
	
	var desktop = this.app.getDesktop();
	
	var store = new Ext.data.JsonStore({
		baseParams: {
			action: 'viewThemes',
			moduleId: this.owner.moduleId
		},
		fields: ['id', 'name', 'pathtothumbnail', 'pathtofile'],
		id: 'id',
		root: 'images',
		url: this.app.connection
	});
	
	this.store = store;
	
	store.on('load', function(store, records){
		if(records){
			defaults.setTitle('Themes Available (' + records.length + ')');
			
			var id = this.app.styles.theme.id;
			if(id){
				view.select('theme-'+id);
			}
		}				
	}, this);
	
	var tpl = new Ext.XTemplate(
		'<tpl for=".">',
			'<div class="pref-view-thumb-wrap" id="theme-{id}">',
				'<div class="pref-view-thumb"><img src="{pathtothumbnail}" title="{name}" /></div>',
			'<span>{shortName}</span></div>',
		'</tpl>',
		'<div class="x-clear"></div>'
	);

	var view = new Ext.DataView({
		autoHeight:true,
		cls: 'pref-thumnail-view',
		emptyText: 'No themes to display',
		itemSelector:'div.pref-view-thumb-wrap',
		loadingText: 'loading...',
		singleSelect: true,
		overClass:'x-view-over',
		prepareData: function(data){
			data.shortName = Ext.util.Format.ellipsis(data.name, 17);
			return data;
		},
		store: store,
		tpl: tpl
	});
	view.on('selectionchange', onSelectionChange, this);
	
	var defaults = new Ext.Panel({
		animCollapse: false,
		baseCls:'collapse-group',
		border: false,
		cls: 'pref-thumbnail-viewer',
		collapsible: true,
		hideCollapseTool: true,
		id: 'pref-theme-view',
		items: view,
		title: 'Default Themes',
		titleCollapse: true
	});
	
	var themes = new Ext.Panel({
		autoScroll: true,
		bodyStyle: 'padding:10px',
		border: true,
		cls: 'pref-card-subpanel',
		id: 'themes',
		items: defaults,
		margins: '10 15 0 15',
		region: 'center'
	});
	
	this.slider = createSlider({
		handler: new Ext.util.DelayedTask(updateTransparency, this)
		, min: 0
		, max: 100
		, x: 15
		, y: 35
		, width: 100
	});
	
	var formPanel = new Ext.FormPanel({
		border: false,
		height: 70,
		items: [
			{x: 15, y: 15, xtype: 'label', text: 'Taskbar Transparency'},
			this.slider.slider,
			this.slider.display
		],
		layout: 'absolute',
		split: false,
		region: 'south'
	});
	
	QoDesk.QoPreferences.Appearance.superclass.constructor.call(this, {
		border: false,
		buttons: [{
			disabled: this.app.isAllowedTo('saveAppearance', this.owner.moduleId) ? false : true,
			handler: onSave,
			scope: this,
			text: 'Save'
			},{
			handler: onClose,
			scope: this,
			text: 'Close'
		}],
		cls: 'pref-card',
		id: config.id,
		items: [
			themes,
			formPanel
		],
		layout: 'border',
		title: 'Window Color And Appearance'
	});
	
	// private functions
	function createSlider(config){
		var handler = config.handler, min = config.min, max = config.max
			, width = config.width || 100, x = config.x, y = config.y;

		var slider = new Ext.Slider({
			minValue: min
			, maxValue: max
			, width: width
			, x: x
			, y: y
		});
		
		var display =  new Ext.form.NumberField({
			cls: 'pref-percent-field'
			, enableKeyEvents: true
			, maxValue: max
			, minValue: min
			, width: 45
			, x: x + width + 15
			, y: y - 1
		});
			
		function sliderHandler(slider){
			var v = slider.getValue();
			display.setValue(v);
			handler.delay(100, null, null, [v]); // delayed task prevents IE bog
		}
		
		slider.on({
			'change': { fn: sliderHandler, scope: this }
			, 'drag': { fn: sliderHandler, scope: this }
		});
		
		display.on({
			'keyup': {
				fn: function(field){
					var v = field.getValue();
					if(v !== '' && !isNaN(v) && v >= field.minValue && v <= field.maxValue){
						slider.setValue(v);
					}
				}
				, buffer: 350
				, scope: this
			}
		});

		return { slider: slider, display: display }
	}
	
	function onClose(){
		this.owner.win.close();
	}
	
	function onSave(){
		var c = this.app.styles;
		
		this.buttons[0].disable();
    	this.owner.save({
    		action: 'saveAppearance'
    		, callback: function(){
    			this.buttons[0].enable();
    		}
    		, callbackScope: this
    		, backgroundcolor: c.backgroundcolor
    		, fontcolor: c.fontcolor
    		, theme: c.theme.id
    		, transparency: c.transparency
    		, wallpaper: c.wallpaper.id
    		, wallpaperposition: c.wallpaperposition
    	});
	}
	
	function onSelectionChange(view, sel){
		if(sel.length > 0){
			var cId = this.app.styles.theme.id,
				r = view.getRecord(sel[0]),
				d = r.data;
			
			if(parseInt(cId) !== parseInt(r.id)){
				if(r && r.id && d.name && d.pathtofile){
					desktop.setTheme({
						id: r.id,
						name: d.name,
						pathtofile: d.pathtofile
					});
				}
			}
		}
	}
	
	function updateTransparency(v){
		desktop.setTransparency(v);
	}
};

Ext.extend(QoDesk.QoPreferences.Appearance, Ext.Panel, {
	afterRender : function(){
		QoDesk.QoPreferences.Appearance.superclass.afterRender.call(this);
		
		this.on('show', this.loadStore, this, {single: true});
	},
	
	loadStore : function(){
		this.store.load();
		this.slider.slider.setValue(this.app.styles.transparency);
	}
});



QoDesk.QoPreferences.Background = function(config){
	this.owner = config.owner;
	this.app = this.owner.app;
	
	var desktop = this.app.getDesktop();
	
	var store = new Ext.data.JsonStore({
		baseParams: {
			action: 'viewWallpapers',
			moduleId: this.owner.moduleId
		},
		fields: ['id', 'name', 'pathtothumbnail', 'pathtofile'],
		id: 'id',
		root: 'images',
		url: this.app.connection
	});
	
	this.store = store;
	
	store.on('load', function(store, records){
		if(records){
			defaults.setTitle('Default Wallpapers (' + records.length + ')');
			
			var id = this.app.styles.wallpaper.id;
			if(id){
				view.select('wallpaper-'+id);
			}
		}				
	}, this);

	var tpl = new Ext.XTemplate(
		'<tpl for=".">',
			'<div class="pref-view-thumb-wrap" id="wallpaper-{id}">',
				'<div class="pref-view-thumb"><img src="{pathtothumbnail}" title="{name}" /></div>',
			'<span>{shortName}</span></div>',
		'</tpl>',
		'<div class="x-clear"></div>'
	);

	var view = new Ext.DataView({
		autoHeight:true,
		cls: 'pref-thumnail-view',
		emptyText: 'No wallpapers to display',
		itemSelector:'div.pref-view-thumb-wrap',
		loadingText: 'loading...',
		singleSelect: true,
		overClass:'x-view-over',
		prepareData: function(data){
			data.shortName = Ext.util.Format.ellipsis(data.name, 17);
			return data;
		},
		store: store,
		tpl: tpl
	});
	view.on('selectionchange', onSelectionChange, this);
	
	var defaults = new Ext.Panel({
		animCollapse: false,
		baseCls:'collapse-group',
		border: false,
		cls: 'pref-thumbnail-viewer',
		collapsible: true,
		hideCollapseTool: true,
		id: 'pref-wallpaper-view',
		items: view,
		title: 'Default Wallpapers',
		titleCollapse: true
	});
	
	var wallpapers = new Ext.Panel({
		autoScroll: true,
		bodyStyle: 'padding:10px',
		border: true,
		cls: 'pref-card-subpanel',
		id: 'wallpapers',
		items: defaults,
		margins: '10 15 0 15',
		region: 'center'
	});
	
	var wpp = this.app.styles.wallpaperposition;
	var tileRadio = createRadio('tile', wpp == 'tile' ? true : false, 90, 40);
	var centerRadio = createRadio('center', wpp == 'center' ? true : false, 200, 40);
	
	var position = new Ext.FormPanel({
		border: false,
		height: 140,
		id: 'position',
		items: [{
				border: false,
				items: {border: false, html:'How should the wallpaper be positioned?'},
				x: 15,
				y: 15
			},{
				border: false,
				items: {border: false, html: '<img class="bg-pos-tile" src="'+Ext.BLANK_IMAGE_URL+'" width="64" height="44" border="0" alt="" />'},
				x: 15,
				y: 40
			},
				tileRadio,
			{
				border: false,
				items: {border: false, html: '<img class="bg-pos-center" src="'+Ext.BLANK_IMAGE_URL+'" width="64" height="44" border="0" alt="" />'},
				x: 125,
				y: 40
			},
				centerRadio,
			{
				border: false,
				items: {border: false, html:'Choose a background color'},
				x: 245,
				y: 15
			},{
				border: false,
				/* items: new Ext.ColorPalette({
					listeners: {
						'select': {
							fn: onColorSelect
							, scope: this
						}
					}
				}), */
				items: new Ext.Button({
					handler: onChangeBgColor,
					//menu: new Ext.ux.menu.ColorMenu(),
					scope: this,
					text: 'Background color'
				}),
				x: 245,
				y: 40
			},{
				border: false,
				items: {border: false, html:'Choose a font color'},
				x: 425,
				y: 15
			},{
				border: false,
				/* items: new Ext.ColorPalette({
					listeners: {
						'select': {
							fn: onFontColorSelect
							, scope: this
						}
					}
				}), */
				items: new Ext.Button({
					handler: onChangeFontColor,
					scope: this,
					text: 'Font color'
				}),
				x: 425,
				y: 40
				
		}],
		layout: 'absolute',
		region: 'south',
		split: false
	});

	QoDesk.QoPreferences.Background.superclass.constructor.call(this, {
		border: false,
		buttons: [{
			disabled: this.app.isAllowedTo('saveBackground', this.owner.moduleId) ? false : true,
			handler: onSave,
			scope: this,
			text: 'Save'
			},{
			handler: onClose,
			scope: this,
			text: 'Close'
		}],
		cls: 'pref-card',
		id: config.id,
		items: [
			wallpapers,
			position
		],
		layout: 'border',
		title: 'Desktop Background'
	});
	
	function createRadio(value, checked, x, y){
		if(value){
			radio = new Ext.form.Radio({
				name: 'position',
				inputValue: value,
				checked: checked,
				x: x,
				y: y
			});
			
			radio.on('check', togglePosition, radio);
			
			return radio;
		}
	}
    
    function onChangeBgColor(){
    	var dialog = new Ext.ux.ColorDialog({
			border: false
			, closeAction: 'close'
			, listeners: {
				'select': { fn: onColorSelect, scope: this, buffer: 350 }
			}
			, manager: this.app.getDesktop().getManager()
			, resizable: false
			, title: 'Color Picker'
		});
		dialog.show(this.app.styles.backgroundcolor);
    }
    
    function onColorSelect(p, hex){
		desktop.setBackgroundColor(hex);
	}
	
	function onChangeFontColor(){
    	var dialog = new Ext.ux.ColorDialog({
			border: false
			, closeAction: 'close'
			, listeners: {
				'select': { fn: onFontColorSelect, scope: this, buffer: 350 }
			}
			, manager: this.app.getDesktop().getManager()
			, resizable: false
			, title: 'Color Picker'
		});
		dialog.show(this.app.styles.fontcolor);
    }
	
	function onFontColorSelect(p, hex){
		desktop.setFontColor(hex);
	}
	
	function onClose(){
		this.owner.win.close();
	}
	
	function onSave(){
		var c = this.app.styles;
		
		this.buttons[0].disable();
    	this.owner.save({
    		action: 'saveBackground'
    		, callback: function(){
    			this.buttons[0].enable();
    		}
    		, callbackScope: this
    		, backgroundcolor: c.backgroundcolor
    		, fontcolor: c.fontcolor
    		, theme: c.theme.id
    		, transparency: c.transparency
    		, wallpaper: c.wallpaper.id
    		, wallpaperposition: c.wallpaperposition
    	});
	}
	
	function onSelectionChange(view, sel){
		if(sel.length > 0){
			var cId = this.app.styles.wallpaper.id,
				r = view.getRecord(sel[0]),
				d = r.data;
			
			if(parseInt(cId) !== parseInt(r.id)){
				if(r && r.id && d.name && d.pathtofile){
					desktop.setWallpaper({
						id: r.id,
						name: d.name,
						pathtofile: d.pathtofile
					});
				}
			}
		}
	}
	
	function togglePosition(field, checked){
		if(checked === true){
			desktop.setWallpaperPosition(field.inputValue);
		}
	}
};

Ext.extend(QoDesk.QoPreferences.Background, Ext.Panel, {
	afterRender : function(){
		QoDesk.QoPreferences.Background.superclass.afterRender.call(this);
		
		this.on('show', this.loadStore, this, {single: true});
	},
	
	loadStore : function(){
		this.store.load();
	}
});



/* 
 * Will ensure that the checkchange event is fired on 
 * node double click
 */
Ext.override(Ext.tree.TreeNodeUI, {
	toggleCheck : function(value){		
        var cb = this.checkbox;
        if(cb){
            cb.checked = (value === undefined ? !cb.checked : value);
            this.fireEvent('checkchange', this.node, cb.checked);
        }
    }
});