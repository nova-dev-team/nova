Ext.onReady(function(){
	var loginPanel = Ext.get("qo-login-panel");
	var xy = loginPanel.getAlignToXY(document, 'c-c');
	setPagePosition(loginPanel, xy[0], xy[1]);
	
	var loginBtn = Ext.get("submitBtn");
	loginBtn.on({
		'click': { fn: login }
		, 'mouseover': { fn: function(){ loginBtn.addClass('qo-login-submit-over'); } }
		, 'mouseout': { fn: function(){ loginBtn.removeClass('qo-login-submit-over'); } }
	});
	
	function login(){		
		var emailField = Ext.get("field1");
		var email = emailField.dom.value;
		
		if(validate(email) === false){
			alert("Your email address is required");
			return false;
		}
		

		
		loginPanel.mask('Please wait...', 'x-mask-loading');
		
		Ext.Ajax.request({
			url: 'system/login/login.php'
			, params: {
				module: 'forgotPassword'
				, user: email
			}
			, success: function(o){
				loginPanel.unmask();
				
				if(typeof o == 'object'){
					var d = Ext.decode(o.responseText);
					
					if(typeof d == 'object'){
						if(d.success == true){
							alert('Your password has been sent to your email.');
						}else{
							if(d.errors){
								alert(d.errors[0].msg);
							}else{
								alert('Errors encountered on the server.');
							}
						}
					}
				}
			}
			, failure: function(){
				loginPanel.unmask();
				alert('Lost connection to server.');
			}
		});
	}
	
	function setPagePosition(el, x, y){
        if(x && typeof x[1] == 'number'){
            y = x[1];
            x = x[0];
        }
        el.pageX = x;
        el.pageY = y;
       	
        if(x === undefined || y === undefined){ // cannot translate undefined points
            return;
        }
        
        if(y < 0){ y = 10; }
        
        var p = el.translatePoints(x, y);
        el.setLocation(p.left, p.top);
        return el;
    }
    
    function validate(field){
		if(field === ""){
			//field.markInvalid();
			return false;
		}
		return true;
	}
});