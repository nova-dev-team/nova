function rmv12(){

    var fld1 = Ext.get('reg-fld-1');
    var fld2 = Ext.get('reg-fld-2');
    
    fld1.removeClass('qo-invalid-textarea');
    fld2.removeClass('qo-invalid-textarea');
}

function rmv34(){

    var fld3 = Ext.get('reg-fld-3');
    var fld4 = Ext.get('reg-fld-4');
    
    fld3.removeClass('qo-invalid-textarea');
    fld4.removeClass('qo-invalid-textarea');
}

function rmv_a() {
	var fld_a = Ext.get('reg-fld-a');
	fld_a.removeClass('qo-invalid-textarea');
}

function rmv_b() {
	var fld_b = Ext.get('reg-fld-b');
	fld_b.removeClass('qo-invalid-textarea');
}

function do_sign_up(){
    var win;
    
    if (!win) {
        win = new Ext.Window({
            width: 400,
            height: 290,
            title: "Register as a new member",
            modal: true,
            html: "<p /><center><font color='black'><table width='300' border='0'> <tr><td width='140'> First name</td> <td width='160'><input type='text' id='reg-fld-a' onkeypress='rmv_a();'></td> </tr>  <tr><td> Last name</td> <td ><input type='text' id='reg-fld-b' onkeypress='rmv_b();'></td> </tr> <tr><td>Email Address:</td><td ><input type='text' id='reg-fld-1' onkeypress='rmv12();'/></td> </tr> <tr>    <td>Email Address Again:</td>    <td><input type='text'  id='reg-fld-2'  onkeypress='rmv12();'  /></td>  </tr>  <tr>    <td>Password:</td>   <td><input type='password'  id='reg-fld-3'  onkeypress='rmv34();'></td>  </tr>  <tr>    <td>Password Again:</td>    <td><input type='password' id='reg-fld-4' onkeypress='rmv34();'></td>  </tr><tr><td>Group</td><td> <select id='sel'><option>User</option><option>Administrator</option></select></td></tr></table></font></center>",
            
            buttons: [{
                text: 'Signup',
                //disabled : true
                handler: function(){
                
                    var fld1 = Ext.get('reg-fld-1');
                    var fld2 = Ext.get('reg-fld-2');
                    var fld3 = Ext.get('reg-fld-3');
                    var fld4 = Ext.get('reg-fld-4');
					
					var fld_a = Ext.get('reg-fld-a');
					var fld_b = Ext.get('reg-fld-b');
                    
                    var loginPanel = Ext.get("qo-login-panel");
                    
                    var sel = Ext.get('sel');
                    
                    var uname = fld1.dom.value;
                    var uname2 = fld2.dom.value;
                    var pwd = fld3.dom.value;
                    var pwd2 = fld4.dom.value;
                    var utype = sel.dom.value;
					
					var fst_name = fld_a.dom.value;
					var lst_name = fld_b.dom.value;
					
					if (fst_name === "" || !(fst_name.length >= 2 && fst_name.length < 25)) {
						fld_a.addClass('qo-invalid-textarea');
						Ext.Msg.alert('Wrong username', "<font color='black'>First name must have lengh 2~25</font>");
                        return;
					}
					
					if (lst_name === "" || !(lst_name.length >= 2 && lst_name.length < 35)) {
						fld_b.addClass('qo-invalid-textarea');
						Ext.Msg.alert('Wrong username', "<font color='black'>Last name must have lengh 2~35</font>");
                        return;
					}
					
                    
                    if (uname === "" || !(uname.length >= 4 && uname.length < 55)) {
                        fld1.addClass('qo-invalid-textarea');
                        fld2.addClass('qo-invalid-textarea');
                        Ext.Msg.alert('Wrong email address', "<font color='black'>Email address must have lengh 4~55</font>");
                        return;
                    }
					
					
					if (uname != uname2) {
						fld1.addClass('qo-invalid-textarea');
                        fld2.addClass('qo-invalid-textarea');
                        Ext.Msg.alert('Wrong email address', "<font color='black'>You email address is not the same</font>");
                        return;
					}
					
					if (pwd != pwd2) {
						fld3.addClass('qo-invalid-textarea');
                        fld4.addClass('qo-invalid-textarea');
						Ext.Msg.alert('Wrong password', "<font color='black'>You password is not the same</font>");
						return;
					}
                    
                    if (pwd === "" || !(pwd.length >= 4 && pwd.length < 15)) {
                        fld3.addClass('qo-invalid-textarea');
                        fld4.addClass('qo-invalid-textarea');
                        Ext.Msg.alert('Wrong password', "<font color='black'>Password must have lengh 4~15</font>");
                        return;
                    }
					
                    loginPanel.mask('Please wait...', 'x-mask-loading');
                    
                    Ext.Ajax.request({
                        url: 'system/login/login.php',
                        params: {
                            module: 'signup',
                            first_name: fst_name,
                            last_name: lst_name,
                            email: uname,
							password: pwd,
							role: utype
/*                            comments: ""*/
                        },
                        success: function(o){
                            loginPanel.unmask();
                            
                            if (typeof o == 'object') {
                                var d = Ext.decode(o.responseText);
                                
                                if (typeof d == 'object') {
                                    if (d.success == true) {
                                        fld1.dom.value = "";
                                        fld2.dom.value = "";
                                        fld3.dom.value = "";
                                        fld4.dom.value = "";
										fld_a.dom.value = "";
										fld_b.dom.value = "";
                                        
                                        pretty_msg('Your sign up request has been sent. You will receive an email notification once we process your request.');
                                        
                                    }
                                    else {
                                        if (d.errors) {
                                            pretty_msg(d.errors[0].msg);
                                        }
                                        else {
                                            pretty_msg('Errors encountered on the server.');
                                        }
                                    }
                                }
                            }
                        },
                        failure: function(){
                            loginPanel.unmask();
                            alert('Lost connection to server.');
                        }
                    });
                    
                }
            }, {
                text: 'Close',
                handler: function(){
                    win.hide();
                }
            }]
        });
    }
    
    win.show();
}


function pretty_msg(msg){
    Ext.Msg.alert('Alert', "<font color=black>" + msg + "</font>");
}
