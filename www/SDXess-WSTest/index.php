<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <script src="jquery-3.2.1.min.js"></script>
        <script src="core-min.js"></script>
        <script src="md5.js"></script>
        <title>WebService - Testing</title>
	</head>
    <body>
        <form>
            <h3>Login Function</h3>
            <label for="user">User:</label><input id='user' name="user" type="text" /><br />
            <label for="password">Password:</label><input id='password' name="password" type="password" />
        </form>
		<input type="submit" id="login" value="Login"/>
		<div id="message"></div>
		<hr />
		<form>
            <h3>Get User info Function</h3>
            <label for="user_2">User:</label><input id='user_2' name="user_2" type="text" /><br />
            <label for="password_2">password:</label><input id='password_2' name="password_2" type="password" /><br />
            <label for="user_3">Target user:</label><input id='user_3' name="user_3" type="text" />
		</form>
		<input type="submit" id="getuserinfo" value="Get User info"/>
		<div id="message2"></div><hr />
        <form>
            <h3>User Registration Function</h3>
            <label for="user_4">User:</label><input id='user_4' name="user_4" type="text" /><br />
            <label for="password_3">password:</label><input id='password_3' name="password_3" type="password" /><br />
            <label for="fname">fname:</label><input id='fname' name="fname" type="text" /><br />
            <label for="lname">lname:</label><input id='lname' name="lname" type="text" /><br />
            <label for="country">country:</label><input id='country' name="country" type="text" /><br />
            <label for="address">address:</label><textarea id='address' name="address" cols="20" rows="5"></textarea>
            </form>
		<input type="submit" id="register" value="Register"/>
		<div id="message3"></div>
		<hr />
        <form>
            <h3>Change Password</h3>
            <label for="user_5">User:</label><input id='user_5' name="user_5" type="text" /><br />
            <label for="password_4">password:</label><input id='password_4' name="password_4" type="password" /><br />
            <label for="user_6">Target User:</label><input id='user_6' name="user_6" type="text" /><br />
            <label for="password_5">New password:</label><input id='password_5' name="password_5" type="password" /><br />
        </form>
        <input type="submit" id="changepass" value="Change password"/>
        <div id="message4"></div>
        <hr />
    </body>
</html>
<script type="text/javascript">
$(document).ready(function(){
	var webservice = "http://inet101.ji8.net/SDXess-WS/";
	//var webservice = "http://localhost/SDXess-WS/";
	var webtest = "http://localhost/SDXess-WSTest/";
	function login(){
		var json = {'authentication_data': {
        	'user': $("#user").val().trim(),
        	'password': CryptoJS.MD5(($("#password").val().trim())).toString()}};
		var url = webservice+"user/login/";
		$.ajax({ 
	   		type: "post",
	   		url: webtest+"curl_linker.php",
	   		data: {'a':url,'b':'application/json','c':'post','d':JSON.stringify(json)},
	   		success: function(data){   
				var x = $.parseJSON(data);     
				if(x.data == '[object Object]'){
					$('#message').html("<h4>"+x.status_message+"</h4>");	 
				}else{
					$('#message').html("<h4>"+x.status_message+"</h4><h5>"+x.data+"</h5>");	 
				}
	   		}
		});
	}
	function get_user_info(){
		var json = {'authentication_data': {
        	'user': $("#user_2").val().trim(),
        	'password': CryptoJS.MD5(($("#password_2").val().trim())).toString()
    		},'get_user_info': {'user': $("#user_3").val().trim()}};	
		var url = webservice+"user/get/information/";
		$.ajax({ 
	   		type: "post",
	   		url: webtest+"curl_linker.php",
	   		data: {'a':url,'b':'application/json','c':'post','d':JSON.stringify(json)},
	   		success: function(data){   
				var x = $.parseJSON(data);     
				if(x.status == '200'){
				   $('#message2').html("<h4>"+x.status_message+"</h4>"+
				   "<table><tr><th colspan='2'>User information:</th></tr>"+
				   "<tr><th align='left'>Account Number:</th><td>"+x.data["account number"]+"</td></tr>"+
			       "<tr><th align='left'>First name:</th><td>"+x.data["name"]+"</td></tr>"+
				   "<tr><th align='left'>Last name:</th><td>"+x.data["last name"]+"</td></tr>"+
				   "<tr><th align='left'>Billing address:</th><td>"+x.data["address"]+"</td></tr>"+
  				   "<tr><th align='left'>Country:</th><td>"+x.data["country"]+"</td></tr>"+
				   "<tr><th align='left'>Registration date:</th><td>"+x.data["registration date"]+"</td></tr>"+
				   "<tr><th align='left'>Status:</th><td>"+x.data["status"]+"</td></tr>"+
				   "<tr><th colspan='2'>Subscription information:</th></tr>"+
				   "<tr><th align='left'>Start date:</th><td>"+x.data["start_time"]+"</td></tr>"+
				   "<tr><th align='left'>Expiration date:</th><td>"+x.data["expiry_time"]+"</td></tr>"+	
				   "<tr><th align='left'>Current status:</th><td>"+x.data["subscription"]+"</td></tr></table>"   
				   );	 
				}else{
					$('#message2').html("<h4>"+x.status_message+"</h4><h5>"+x.data+"</h5>");	 
				}
				//console.log(x.data);
	   		}
		});
	}
	function pass_change(){
		var json = {'authentication_data': {
			'user': $("#user_5").val().trim(),
			'password': CryptoJS.MD5(($("#password_4").val().trim())).toString()
			},'change_password': {'user': $("#user_6").val().trim(),
			'password': CryptoJS.MD5(($("#password_5").val().trim())).toString()}};
		var url = webservice+"user/update/password/";
		$.ajax({ 
	   		type: "post",
	   		url: webtest+"curl_linker.php",
	   		data: {'a':url,'b':'application/json','c':'put','d':JSON.stringify(json)},
	   		success: function(data){   
				var x = $.parseJSON(data);     
				if(x.status == '200'){
					$('#message4').html("<h4>"+x.status_message+"</h4>");	 
				}else{
					$('#message4').html("<h4>"+x.status_message+"</h4><h5>"+x.data+"</h5>");	 
				}
	   		}
		});
	}
	function user_registration(){
		var json = {'registration_data': {
        'user': $("#user_4").val().trim(),
        'password': CryptoJS.MD5(($("#password_3").val().trim())).toString(),
		'fname': $("#fname").val().trim(),
		'lname': $("#lname").val().trim(),
		'country': $("#country").val().trim(),
		'address': $("#address").val().trim()
    	}};
		var url = webservice+"user/registration/";
		$.ajax({ 
	   		type: "post",
	   		url: webtest+"curl_linker.php",
	   		data: {'a':url,'b':'application/json','c':'post','d':JSON.stringify(json)},
	   		success: function(data){   
				var x = $.parseJSON(data);     
				if(x.data == '[object Object]'){
					$('#message3').html("<h4>"+x.status_message+"</h4>");	 
				}else{
					$('#message3').html("<h4>"+x.status_message+"</h4><h5>"+x.data+"</h5>");	 
				}
	   		}
		});
	}		
	$("#login").on( "click", login);
	$("#register").on( "click", user_registration);
	$("#getuserinfo").on( "click", get_user_info);
	$("#changepass").on( "click", pass_change);
});
</script>