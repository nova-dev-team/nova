Ext.onReady(function(){
    Ext.EventManager.onWindowResize(centerPanel);
    
    var loginPanel = Ext.get("qo-login-panel");
    
    var loginBtn = Ext.get("submitBtn");
    loginBtn.on({
        'click': {
            fn: login
        },
        'mouseover': {
            fn: function(){
                loginBtn.addClass('qo-login-submit-over');
            }
        },
        'mouseout': {
            fn: function(){
                loginBtn.removeClass('qo-login-submit-over');
            }
        }
    });
    
//    Ext.get("field3-label").setDisplayed('none');
//    Ext.get("field3").setDisplayed('none');
    
    centerPanel();
    
    var emailField = Ext.get("field1");
    emailField.on({
        'keypress': {
            fn: function(e){
                test_enter(e);
                emailField.removeClass('qo-invalid-textarea');
            }
        }
    })
    var pwdField = Ext.get("field2");
    pwdField.on({
        'keypress': {
            fn: function(e){
                test_enter(e);
                pwdField.removeClass('qo-invalid-textarea');
            }
        }
    })
    
    function test_enter(event){
        if (event.browserEvent.keyCode == event.RETURN) {
            login();
        }
    }
    
    function centerPanel(){
        var xy = loginPanel.getAlignToXY(document, 'c-c');
        positionPanel(loginPanel, xy[0], xy[1]);
    }
    
    function hideLoginFields(){
        Ext.get("field1-label").setDisplayed('none');
        Ext.get("field1").setDisplayed('none');
        Ext.get("field2-label").setDisplayed('none');
        Ext.get("field2").setDisplayed('none');
    }
    
    function loadGroupField(d){
        var combo = Ext.get("field3");
        var comboEl = combo.dom;
        
        while (comboEl.options.length) {
            comboEl.options[0] = null;
        }
        
        for (var i = 0, len = d.length; i < len; i++) {
            comboEl.options[i] = new Option(d[i][1], d[i][0]);
        }
    }
    
    function login(){
        var emailField = Ext.get("field1");
        var email = emailField.dom.value;
        var pwdField = Ext.get("field2");
        var pwd = pwdField.dom.value;
//        var groupField = Ext.get("field3");
  //      var group = groupField.dom.value;
        
        if (validate(email) === false) {
            emailField.addClass('qo-invalid-textarea');
            pretty_alert("Your email address is required");
            return false;
        }
        
        if (validate(pwd) === false) {
            pwdField.addClass('qo-invalid-textarea');
            pretty_alert("Your password is required");
            return false;
        }
        
        loginPanel.mask('Please wait...', 'x-mask-loading');
        
        Ext.Ajax.request({
            url: 'system/login/login.php',
            params: {
                module: 'login',
                user: email,
                pass: pwd
//                ,group: group
            },
            success: function(o){
                loginPanel.unmask();
                
                if (typeof o == 'object') {
                    var d = Ext.decode(o.responseText);
                    
                    if (typeof d == 'object') {
                        if (d.success == true) {
                            var g = d.groups;
                            
                            if (g && g.length > 0) {
                                hideLoginFields();
                                showGroupField();
                                
                                var d = [];
                                for (var i = 0, len = g.length; i < len; i++) {
                                    d.push([g[i].id, g[i].name]);
                                }
                                
                                loadGroupField(d);
                                
                            }
                            else 
                                if (d.sessionId !== "") {
                                
                                    loginPanel.mask('Redirecting...', 'x-mask-loading');
                                    
                                    // get the path
                                    var path = window.location.pathname, path = path.substring(0, path.lastIndexOf('/') + 1);
                                    
                                    // set the cookie
                                    set_cookie('sessionId', d.sessionId, '', path, '', '');
                                    
                                    // redirect the window
                                    window.location = path;
                                    
                                }
                        }
                        else {
                            if (d.errors && d.errors[0].msg) {
                                pretty_alert(d.errors[0].msg);
                            }
                            else {
                                pretty_alert('Errors encountered on the server.');
                            }
                        }
                    }
                }
            },
            failure: function(){
                loginPanel.unmask();
                pretty_alert('Lost connection to server.');
            }
        });
    }
    
    function positionPanel(el, x, y){
        if (x && typeof x[1] == 'number') {
            y = x[1];
            x = x[0];
        }
        el.pageX = x;
        el.pageY = y;
        
        if (x === undefined || y === undefined) { // cannot translate undefined points
            return;
        }
        
        if (y < 0) {
            y = 10;
        }
        
        var p = el.translatePoints(x, y);
        el.setLocation(p.left, p.top);
        return el;
    }
    
    function showGroupField(){
        Ext.get("field3-label").setDisplayed(true);
        Ext.get("field3").setDisplayed(true);
    }
    
    function validate(field){
        if (field === "") {
            return false;
        }
        return true;
    }
    
    function pretty_alert(message){
        Ext.Msg.alert("Alert", "<font color='red'>" + message + "</font>");
    }
    
});
