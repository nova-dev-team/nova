
function do_forgot_pwd(){

    var loginPanel = Ext.get("qo-login-panel");
    
    Ext.Msg.prompt("Forgot password?", "<font color=black>Tell me your email address</font>", function(btn, email){
        if (btn == 'ok') {
            loginPanel.mask('Please wait...', 'x-mask-loading');
			
            Ext.Ajax.request({
                url: 'system/login/login.php',
                params: {
                    module: 'forgotPassword',
                    user: email
                },
                success: function(o){
                    loginPanel.unmask();
                    
                    if (typeof o == 'object') {
                        var d = Ext.decode(o.responseText);
						
                        if (typeof d == 'object') {
                            if (d.success == true) {
                                forgot_pwd_pretty_alert('Your password has been sent to your email.');
                            }
                            else {
                                if (d.errors) {
                                    forgot_pwd_pretty_alert(d.errors[0].msg);
                                }
                                else {
                                    forgot_pwd_pretty_alert('Errors encountered on the server.');
                                }
                            }
                        }
                    }
                },
                failure: function(){
                    loginPanel.unmask();
                    forgot_pwd_pretty_alert('Lost connection to server.');
                }
            });
        }
        else {
            // do nothing
        }
    });
}

function forgot_pwd_pretty_alert(msg){
    Ext.Msg.alert("Alert", "<font color=black>" + msg + "</font>");
}
