var bigApp;



Ext.override(QoDesk.SuperAdminManagerWindow, {

    createWindow: function(){
    
        bigApp = this.app;
        
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('superadmin-user-manager-win');
        
        function test1(){
            alert('hi');
        }
        
        if (!win) {
        
            var store = new Ext.data.JsonStore({
                //				autoLoad:"True",
                root: 'all_users',
                totalProperty: 'totalCount',
                idProperty: 'email',
                remoteSort: true,
                
                fields: ["first_name", "last_name", "email", "user_role", "is_active", "user_id", "is_logged_in"],
                
                // load using script tags for cross domain, if the data in on the same domain as
                // this page, an HttpProxy would be better
                proxy: new Ext.data.HttpProxy({
                    url: '/connect.php?action=viewUserInfo&moduleId=superadmin-user-manager'
                })
            });
            store.setDefaultSort('email', 'desc');
            
            var pagingBar = new Ext.PagingToolbar({
                pageSize: 20,
                store: store,
                displayInfo: true,
                displayMsg: 'Displaying users {0} - {1} of {2}',
                emptyMsg: "No user to display",
                
                plugins: new Ext.ux.SlidingPager(),
                
                items: ['-'                //				, {
                //                    pressed: false,
                //                    //		            enableToggle:false,
                //                    text: 'Commit Changes',
                //                    //		            cls: 'x-btn-text-icon details',
                //                    handler: function(btn){
                //                    
                //                        Ext.Msg.alert("HAHA");
                //                        
                //                    }
                //                }
                ]
            });
            
            function renderEmail(value, p, record){
                return String.format('<a href="mailto:{0}">{0}</a>', value, record.data.user_id);
            }
            
            function renderRole(value, p, record){
            
                var color = "";
                
                if (value == "Administrator") {
                    color = "red";
                }
                else 
                    if (value == "Super Administrator") {
                        color = "cyan";
                    }
                    else 
                        if (value == "Debug") {
                            color = "orange";
                        }
                        else 
                            if (value == "User") {
                                color = "green";
                            }
                
                if (color != "") {
                    return String.format('<font color="{0}">{1}</font>', color, value);
                }
                else {
                    return String.format('<u>{0}</u>', value);
                }
                
            }
            
            var filters = new Ext.grid.GridFilters({
                filters: [{
                    type: 'list',
                    dataIndex: 'user_role',
                    options: ['Administrator', 'User'],
                    phpMode: true
                }, {
                    type: 'list',
                    dataIndex: 'is_active',
                    options: ['Yes', 'No'],
                    phpMode: true
                }]
            });
            
            var checkColumn = new Ext.grid.CheckColumn({
                header: "Is Active?",
                dataIndex: 'is_active',
                width: 60,
                sortable: true
            });
            
            var kickAss = new Ext.grid.KickAssColumn({
                header: "Logged in?",
                dataIndex: 'is_logged_in',
                width: 80
            });
            
            
            var cm = new Ext.grid.ColumnModel([{
                header: "First Name",
                width: 130,
                dataIndex: 'first_name',
                sortable: true
            }, {
                header: "Last Name",
                width: 130,
                dataIndex: 'last_name',
                sortable: true
            }, {
                header: "Email Address",
                width: 200,
                dataIndex: 'email',
                renderer: renderEmail,
                sortable: true
            }, {
                header: "User Role",
                width: 120,
                dataIndex: 'user_role',
                sortable: true,
                renderer: renderRole,
            }, checkColumn, kickAss]);
            
            
            
            
            var grid = new Ext.grid.EditorGridPanel({
                store: store,
                
                //			trackMouseOver:false,
                disableSelection: true,
                
                loadMask: true,
                bbar: pagingBar,
                plugins: [checkColumn, filters, kickAss],
                clicksToEdit: 1,
                
                cm: cm
            });
            
            win = desktop.createWindow({
                id: 'superadmin-user-manager-win',
                title: "User Manager for Super Administrator",
                width: 740,
                height: 480,
                iconCls: 'superadmin-user-manager-icon',
                shim: false,
                animCollapse: false,
                constrainHeader: true,
                layout: 'fit',
                items: grid,
                taskbuttonTooltip: '<b>User Manager</b><br />A user manager for Super Administrator',
            });
            
            store.load({
                params: {
                    start: 0,
                    limit: 20
                }
            });
            
        }
        
        win.show();
        
    }
    
});





/**
 * @class Ext.ux.SliderTip
 * @extends Ext.Tip
 * Simple plugin for using an Ext.Tip with a slider to show the slider value
 */
Ext.ux.SliderTip = Ext.extend(Ext.Tip, {
    minWidth: 10,
    offsets: [0, -10],
    init: function(slider){
        slider.on('dragstart', this.onSlide, this);
        slider.on('drag', this.onSlide, this);
        slider.on('dragend', this.hide, this);
        slider.on('destroy', this.destroy, this);
    },
    
    onSlide: function(slider){
        this.show();
        this.body.update(this.getText(slider));
        this.doAutoWidth();
        this.el.alignTo(slider.thumb, 'b-t?', this.offsets);
    },
    
    getText: function(slider){
        return slider.getValue();
    }
});



Ext.ux.SlidingPager = Ext.extend(Ext.util.Observable, {
    init: function(pbar){
        this.pagingBar = pbar;
        
        pbar.on('render', this.onRender, this);
        pbar.on('beforedestroy', this.onDestroy, this);
    },
    
    onRender: function(pbar){
        Ext.each(pbar.items.getRange(2, 6), function(c){
            c.hide();
        });
        var td = document.createElement("td");
        pbar.tr.insertBefore(td, pbar.tr.childNodes[5]);
        
        td.style.padding = '0 5px';
        
        this.slider = new Ext.Slider({
            width: 114,
            minValue: 1,
            maxValue: 1,
            plugins: new Ext.ux.SliderTip({
                bodyStyle: 'padding:5px;',
                getText: function(s){
                    return String.format('Page <b>{0}</b> of <b>{1}</b>', s.value, s.maxValue);
                }
            })
        });
        this.slider.render(td);
        
        this.slider.on('changecomplete', function(s, v){
            pbar.changePage(v);
        });
        
        pbar.on('change', function(pb, data){
            this.slider.maxValue = data.pages;
            this.slider.setValue(data.activePage);
        }, this);
    },
    
    onDestroy: function(){
        this.slider.destroy();
    }
});




Ext.grid.CheckColumn = function(config){
    Ext.apply(this, config);
    if (!this.id) {
        this.id = Ext.id();
    }
    this.renderer = this.renderer.createDelegate(this);
};

Ext.grid.CheckColumn.prototype = {
    init: function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    
    onMouseDown: function(e, t){
        if (t.className && t.className.indexOf('x-grid3-cc-' + this.id) != -1) {
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            
            var desktop = bigApp.getDesktop();
            var notifyWin = desktop.showNotification({
                html: 'Sending request...',
                title: 'Please wait'
            });
            
            Ext.Ajax.request({
                url: bigApp.connection,
                params: {
                    moduleId: 'superadmin-user-manager',
                    action: "toggleActive",
                    user_id: record.data.user_id,
                    value: record.data[this.dataIndex]
                },
                success: function(o){
                    if (o && o.responseText && Ext.decode(o.responseText).success) {
                        saveComplete('Finished', 'Save complete.');
                    }
                    else {
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
            }
        }
    },
    
    renderer: function(value, p, record){
    
        if (record.data.is_active == 1) {
            record.data.is_active = true;
        }
        else 
            if (record.data.is_active == 0) {
                record.data.is_active = false;
            }
        
        var html = '<div class="x-grid3-check-col' + ((record.data.is_active) ? '-on' : '') + ' x-grid3-cc-' + this.id + '" id="crapp_id_' + record.data.user_id + '">&#160;</div>';
        
        return html;
    }
};





Ext.grid.KickAssColumn = function(config){
    Ext.apply(this, config);
    if (!this.id) {
        this.id = Ext.id();
    }
    this.renderer = this.renderer.createDelegate(this);
};

Ext.grid.KickAssColumn.prototype = {
    init: function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    
    onMouseDown: function(e, t){
        if (t.className && t.className.indexOf('x-grid3-cc-' + this.id) != -1  && t.id.indexOf('crapp_id2_') != -1) {
            e.stopEvent();
			
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
			
			if (record.data.is_logged_in == "false") {
				return;
			}
			
            var desktop = bigApp.getDesktop();
            var notifyWin = desktop.showNotification({
                html: 'Sending request...',
                title: 'Please wait'
            });
            
            Ext.Ajax.request({
                url: bigApp.connection,
                params: {
                    moduleId: 'superadmin-user-manager',
                    action: "kickAss",
                    user_id: record.data.user_id,
                    value: record.data[this.dataIndex]
                },
                success: function(o){
                    if (o && o.responseText && Ext.decode(o.responseText).success) {
                        saveComplete('Finished', 'Kicked out.');
						
						record.set(this.dataIndex, "false");
                    }
                    else {
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
            }
        }
    },
    
    renderer: function(value, p, record){
    
        var html = '<div class="x-grid3-cc-' + this.id + ' superadmin-user-manager-' +
			(value=='true'?"on":"off")
		+ 'line" id="crapp_id2_' + record.data.user_id + '" >&#160;  </div>';
        
        return html;
    }
};
