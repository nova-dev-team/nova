/* Override the module code here.
 * This code will be Loaded on Demand.
 */

Ext.override(QoDesk.GridWindow, {

	// Array data for the grid
	dummyData : [
	    ['3m Co',71.72,0.02,0.03,'9/1 12:00am'],
	    ['Alcoa Inc',29.01,0.42,1.47,'9/1 12:00am'],
	    ['American Express Company',52.55,0.01,0.02,'9/1 12:00am'],
	    ['American International Group, Inc.',64.13,0.31,0.49,'9/1 12:00am'],
	    ['AT&T Inc.',31.61,-0.48,-1.54,'9/1 12:00am'],
	    ['Caterpillar Inc.',67.27,0.92,1.39,'9/1 12:00am'],
	    ['Citigroup, Inc.',49.37,0.02,0.04,'9/1 12:00am'],
	    ['Exxon Mobil Corp',68.1,-0.43,-0.64,'9/1 12:00am'],
	    ['General Electric Company',34.14,-0.08,-0.23,'9/1 12:00am'],
	    ['General Motors Corporation',30.27,1.09,3.74,'9/1 12:00am'],
	    ['Hewlett-Packard Co.',36.53,-0.03,-0.08,'9/1 12:00am'],
	    ['Honeywell Intl Inc',38.77,0.05,0.13,'9/1 12:00am'],
	    ['Intel Corporation',19.88,0.31,1.58,'9/1 12:00am'],
	    ['Johnson & Johnson',64.72,0.06,0.09,'9/1 12:00am'],
	    ['Merck & Co., Inc.',40.96,0.41,1.01,'9/1 12:00am'],
	    ['Microsoft Corporation',25.84,0.14,0.54,'9/1 12:00am'],
	    ['The Coca-Cola Company',45.07,0.26,0.58,'9/1 12:00am'],
	    ['The Procter & Gamble Company',61.91,0.01,0.02,'9/1 12:00am'],
	    ['Wal-Mart Stores, Inc.',45.45,0.73,1.63,'9/1 12:00am'],
	    ['Walt Disney Company (The) (Holding Company)',29.89,0.24,0.81,'9/1 12:00am']
	],

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('grid-win');
        
        if(!win){        	
        	var sm = new Ext.grid.RowSelectionModel({singleSelect:true});
        	
        	var grid = new Ext.grid.GridPanel({
				//autoExpandColumn:'company',
				border:false,
				ds: new Ext.data.Store({
					reader: new Ext.data.ArrayReader({}, [
						{name: 'company'},
						{name: 'price', type: 'float'},
						{name: 'change', type: 'float'},
						{name: 'pctChange', type: 'float'}
					]),
					data: this.dummyData
				}),
				cm: new Ext.grid.ColumnModel([
					new Ext.grid.RowNumberer(),
					{header: "Company", width: 120, sortable: true, dataIndex: 'company'},
					{header: "Price", width: 70, sortable: true, renderer: Ext.util.Format.usMoney, dataIndex: 'price'},
					{header: "Change", width: 70, sortable: true, dataIndex: 'change'},
					{header: "% Change", width: 70, sortable: true, dataIndex: 'pctChange'}
				]),
				shadow: false,
				shadowOffset: 0,
				sm: sm,
				tbar: [{
					text:'Add Something',
					tooltip:'Add a new row',
					iconCls:'demo-grid-add'
					}, '-', {
					text:'Options',
					tooltip:'Your options',
					iconCls:'demo-grid-option'
					},'-',{
					text:'Remove Something',
					tooltip:'Remove the selected item',
					iconCls:'demo-grid-remove'
				}],
				viewConfig: {
					forceFit:true
				}
			});
			
			// example of how to open another module on rowselect
			sm.on('rowselect',function(){
				//var tabWin = this.app.getModule('demo-tabs');
				//if(tabWin){
				//	tabWin.launcher.handler.call(this.scope || this);
				//}
			}, this);
			
            win = desktop.createWindow({
                id: 'grid-win',
                title:'Grid Window',
                width:740,
                height:480,
                iconCls: 'grid-icon',
                shim:false,
                animCollapse:false,
                constrainHeader:true,
				layout: 'fit',
                items: grid,
                taskbuttonTooltip: '<b>Grid Window</b><br />A window with a grid',
                tools: [
					{
						id: 'refresh',
						handler: Ext.emptyFn,
						scope: this
					}
				]
            });
            
            // begin: modify top toolbar
	        var tb = grid.getTopToolbar();
				
			// example of getting a reference to another module's launcher object
	        var tabWin;// = this.app.getModule('demo-tabs');
	        
			if(tabWin){
				var c = tabWin.launcher;
				
				tb.add({
					// example button to open another module
					text: 'Open Tab Window',
					handler: c.handler,
					scope: c.scope,
					iconCls: c.iconCls
				});
			}
				
			tb.add({
				text: 'Toggle My Taskbutton Text/Icon',
				handler: this.updateTaskButton,
				scope: this
			});
	        // end: modify top toolbar
	        
	        // could modify this windows taskbutton tooltip here (defaults to win.title)
	        //win.taskButton.setTooltip('Grid Window');
        }
        
        win.show();
    },
    
    updateTaskButton : function(){
    	var desktop = this.app.getDesktop(),
    		win = desktop.getWindow('grid-win'),
    		btn = win.taskButton;
    	
    	if(btn.getText() === 'Toggled'){
    		btn.setText('Grid Window');
    		// can pass in an object
    		btn.setTooltip({title: 'Grid Window', text: 'A window with a grid'});
    		// or could pass in a string
    		//btn.setTooltip('Grid Window');
    		btn.setIconClass('grid-icon');
    	}else{
    		btn.setText('Toggled');
    		btn.setTooltip({title: 'Toggled', text: 'You have toogled me'});
    		btn.setIconClass('bogus');
    	}
    }
});