<?php
include_once("functions/DB_Functions.php");
include_once("functions/response.php");
header("Content-Type: application/json");
$db = new DB_Functions();
$rmethod =$db->Rmethod_validation($_SERVER['REQUEST_METHOD'], 'POST');
if($rmethod == true){
	$contentType = isset($_SERVER["CONTENT_TYPE"]) ? trim($_SERVER["CONTENT_TYPE"]) : '';
	$ctype =$db->Ctype_validation($contentType, 'application/json');	
	if($ctype == true){
		$decoded = json_decode(file_get_contents("php://input"), true);
		$isarray =$db->Array_validation($decoded);
		if($isarray == true){
			if(isset($decoded['registration_data']['user']) && isset($decoded['registration_data']['password']) && isset($decoded['registration_data']['fname']) && isset($decoded['registration_data']['lname']) /*&& isset($decoded['registration_data']['phone']) && isset($decoded['registration_data']['email'])*/ && isset($decoded['registration_data']['country']) && isset($decoded['registration_data']['address'])){
				$user = $decoded['registration_data']['user'];
				$pass = $decoded['registration_data']['password'];	
				$fname=$decoded['registration_data']['fname'];	
				$lname=$decoded['registration_data']['lname'];	
				//$phone=$decoded['registration_data']['phone'];	
				//$email=$decoded['registration_data']['email'];	
				$country=$decoded['registration_data']['country'];	
				$address=$decoded['registration_data']['address'];
			}
			if(!empty($user) && !empty($pass) && !empty($fname) && !empty($lname)/*|| !empty($phone)|| !empty($email)*/&& !empty($country) && !empty($address)){
				$response=$db->user_registration($user,$pass,$fname,$lname/*,$phone,$email*/,$country,$address);
				if(empty($response)){
					deliver_response(400,"User Registration Failed","Invalid parameters");
				}else{
					if (is_array($response) == true){
						deliver_response($response[1],$response[2],$response[3]);
					}
				}
			}else{
				deliver_response(400,"User Registration Failed","Invalid parameters");
			}	
		}
	}
}
?>