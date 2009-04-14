/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 *
 * http://www.qwikioffice.com/license
 */
Ext.override(QoDesk.AccountSetting, {

    createWindow: function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow(this.moduleId);
        
        if (!win) {
        
            var db_email = "";
            
            
            var noti = this.app.getDesktop().showNotification({
                html: 'Please wait...',
                title: 'Loading Account Info'
            });
            
            
            Ext.Ajax.request({
                url: this.app.connection,
                params: {
                    moduleId: 'account-setting',
                    action: "viewAccount"
                },
                success: function(o){
                    if (o && o.responseText && Ext.decode(o.responseText).success) {
                        var decoded = Ext.decode(o.responseText);
                        
                        db_email = decoded.email_address;
                        
                        desktop.hideNotification(noti);
                        
                        win = desktop.createWindow({
                            id: this.moduleId,
                            title: 'Account Setting',
                            width: 370,
                            height: 270,
                            iconCls: 'account-setting-icon',
                            shim: false,
                            constrainHeader: true,
                            layout: 'fit',
                            items: new Ext.form.FormPanel({
                            
                            
                                buttons: [{
                                    text: 'Save',
                                    handler: function(){
                                    
                                    
                                        var fld1 = Ext.get('crappy_first_name_field');
                                        
                                        var fld2 = Ext.get('crappy_last_name_field');
                                        
                                        var fld3 = Ext.get('crappy_old_pwd_field');
                                        var fld4 = Ext.get('crappy_new_pwd_field');
                                        var fld5 = Ext.get('crappy_new_pwd2_field');
                                        
                                        var v1 = fld1.dom.value;
                                        var v2 = fld2.dom.value;
                                        var v3 = fld3.dom.value;
                                        var v4 = fld4.dom.value;
                                        var v5 = fld5.dom.value;
                                        
                                        
                                        var notify = desktop.showNotification({
                                            title: 'Please wait',
                                            html: 'Updating Account Info...'
                                        });
                                        
                                        if (v1 == "" || !(v1.length >= 4 && v1.length <= 20)) {
                                            notify_error("First Name must have length 4~20");
                                            return;
                                        }
                                        
                                        if (v2 == "" || !(v2.length >= 4 && v2.length <= 20)) {
                                            notify_error("First Name must have length 4~20");
                                            return;
                                        }
										
										if (v4 != "" || v5 != "") {
										
											if (v4 != v5) {
												notify_error("Your have mistyped your new password, they are not the same");
												return;
											}
											
											if (v4 == "" || !(v1.length >= 4 && v1.length <= 20)) {
												notify_error("New Password must have length 4~20");
												return;
											}
										} else {
											v4 = v3;
										}
										
										
										
                                        function notify_error(msg){
                                            notify.setIconClass('x-icon-done');
                                            notify.setTitle("Error");
                                            notify.setMessage(msg);
                                            desktop.hideNotification(notify);
                                        }
                                        
                                        Ext.Ajax.request({
                                            url: '/connect.php',
                                            params: {
                                                moduleId: 'account-setting',
                                                action: "updateAccount",
                                                old_password: v3,
                                                new_first_name: v1,
                                                new_last_name: v2,
                                                new_password: v4
                                            },
                                            success: function(o){
                                                if (o && o.responseText && Ext.decode(o.responseText).success) {
                                                    notify.setIconClass('x-icon-done');
                                                    notify.setTitle("Success");
                                                    notify.setMessage("Successfully changed your account settings");
                                                    desktop.hideNotification(notify);
                                                }
                                                else {
													if (Ext.decode(o.responseText).pwd_wrong) {
													notify_error("Incorrect password");
													}
													else {
														notify_error("Error occured on server");
													}
                                                }
                                            },
                                            failure: function(o){
                                                notify_error("Failed to connect server");
                                            }
                                        });
                                        
                                        
                                        
                                    }
                                }, {
                                    text: 'Close',
                                    handler: function(){
                                        win.close();
                                    }
                                }],
                                
                                
                                
                                baseCls: 'x-plain',
                                labelWidth: 130,
                                url: 'save-form.php',
                                defaultType: 'NumberField',
                                
                                items: new Ext.form.FieldSet({
                                    title: db_email + "， " + decoded.user_role,
                                    autoHeight: true,
                                    items: [new Ext.form.TextField({
                                        fieldLabel: 'First Name',
                                        name: 'first_nm',
                                        value: decoded.first_name,
                                        minLength: 4,
                                        maxLength: 20,
                                        width: 190,
                                        id: "crappy_first_name_field"
                                    }), new Ext.form.TextField({
                                        fieldLabel: 'Last Name',
                                        name: 'last_nm',
                                        value: decoded.last_name,
                                        width: 190,
                                        minLength: 4,
                                        maxLength: 20,
                                        id: "crappy_last_name_field"
                                    }), new Ext.form.TextField({
                                        fieldLabel: 'Old Password',
                                        type: 'password',
                                        name: 'old_pwd',
                                        width: 190,
                                        minLength: 4,
                                        maxLength: 20,
                                        id: "crappy_old_pwd_field",
                                        inputType: "password"
                                    }), new Ext.form.TextField({
                                        fieldLabel: 'New Password',
                                        name: 'new_pwd',
                                        width: 190,
                                        minLength: 4,
                                        maxLength: 20,
                                        id: "crappy_new_pwd_field",
                                        inputType: "password"
                                    }), new Ext.form.TextField({
                                        fieldLabel: 'New Password Again',
                                        name: 'new_pwd2',
                                        width: 190,
                                        minLength: 4,
                                        maxLength: 20,
                                        id: "crappy_new_pwd2_field",
                                        inputType: "password"
                                    })]
                                })
                            
                            }),
                            taskbuttonTooltip: '<b>Account Setting</b><br />Modify you account settings'
                        });
                        
                        win.show();
                        
                    }
                    else {
                        this.app.getDesktop().showNotification({
                            html: 'Error occured on server',
                            title: 'Error'
                        });
                    }
                },
                failure: function(){
                    this.app.getDesktop().showNotification({
                        html: 'Error occured on server',
                        title: 'Error'
                    });
                },
                scope: this
            
            });
            
        }
        
        if (!win) {
        
            win.show();
        }
    }
});
