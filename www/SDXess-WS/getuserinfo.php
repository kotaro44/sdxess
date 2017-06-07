<?php
include_once("functions/DB_Functions.php");
include_once("functions/response.php");
header("Content-Type:application/json");
$db = new DB_Functions();
$rmethod =$db->Rmethod_validation($_SERVER['REQUEST_METHOD'], 'POST');
if($rmethod == true){
	$contentType = isset($_SERVER["CONTENT_TYPE"]) ? trim($_SERVER["CONTENT_TYPE"]) : '';
	$ctype =$db->Ctype_validation($contentType, 'application/json');	
	if($ctype == true){
		$decoded = json_decode(file_get_contents("php://input"), true);
		$isarray =$db->Array_validation($decoded);
		if($isarray == true){
			if(isset($decoded['authentication_data']['user']) && isset($decoded['authentication_data']['password']) && isset($decoded['get_user_info']['user'])){
				$user = $decoded['authentication_data']['user'];
				$pass = $decoded['authentication_data']['password'];	
				$getuser = $decoded['get_user_info']['user'];
			}
			if(!empty($user) && !empty($pass) && !empty($getuser)){
				$response=$db->get_user_info($user,$pass,$getuser);
				if(empty($response)){
					deliver_response(500,"Get User Information Failed","Unable to complete the operation");
				}else{
					if (is_array($response) == true){
						deliver_response($response[1],$response[2],$response[3]);
					}else{
						deliver_response(500,"Get User Information Failed","Unable to complete the operation");
					}
				}
			}else{
				deliver_response(400,"Get User Information Failed","Invalid parameters");
			}			
		}
	}
}
?>