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
		var firstNameField = Ext.get("field1");
		var firstName = firstNameField.dom.value;
		
		var lastNameField = Ext.get("field2");
		var lastName = lastNameField.dom.value;
		
		var emailField = Ext.get("field3");
		var email = emailField.dom.value;
		
		var emailVerifyField = Ext.get("field4");
		var emailVerify = emailVerifyField.dom.value;
		
		var commentsField = Ext.get("field5");
		var comments = commentsField.dom.value;
		
		if(validate(firstName) === false){
			alert("Your first name is required");
			return false;
		}
		
		if(validate(lastName) === false){
			alert("Your last name is required");
			return false;
		}
		
		if(validate(email) === false){
			alert("Your email address is required");
			return false;
		}
		
		if(validate(emailVerify) === false || (email !== emailVerify)){
			alert("Please verify your email address again");
			return false;
		}
		
		loginPanel.mask('Please wait...', 'x-mask-loading');
		
		Ext.Ajax.request({
			url: 'system/login/login.php'
			, params: {
				module: 'signup'
				, first_name: firstName
				, last_name: lastName
				, email: email
				, email_verify: emailVerify
				, comments: comments
			}
			, success: function(o){
				loginPanel.unmask();
				
				if(typeof o == 'object'){
					var d = Ext.decode(o.responseText);
					
					if(typeof d == 'object'){
						if(d.success == true){
							firstNameField.dom.value = "";
							lastNameField.dom.value = "";
							emailField.dom.value = "";
							emailVerifyField.dom.value = "";
							commentsField.dom.value = "";
							
							alert('Your sign up request has been sent. \n\nPlease wait for the administrators to process your request.');
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
    
    function showGroupField(){
		Ext.get("field3-label").setDisplayed(true);
		Ext.get("field3").setDisplayed(true);
	}
    
  function validate(field){
		if(field === ""){
			return false;
		}
		return true;
	}
});