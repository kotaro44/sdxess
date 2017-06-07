<?php
include_once("response.php");
class DB_Functions {
	public function login($user, $password){
		require_once 'DB_Connect.php';
		$DB = new DB_Connect();
		$con = $DB->connect();
		$sum =0;
		if ($this->email_validation($user,5,100) == false){
			$sum++;
		}
		 if ($this->alphanumetic_validation($password,32,32)== false){
			if(!empty($error_msg)){
				$error_msg	=$error_msg.", ";
			}
			$sum++;
		}
		if($sum == 0){
			$sql = "SELECT `users`.`account_number`,`users`.`first_name`,`users`.`last_name`,`users`.`status`,`subs`.`expiry_time` FROM `sdxess_users` as `users` inner join `sdxess_subs_active` as `subs` ON `users`.`account_number` = `subs`.`account_number` WHERE `users`.`username` = '".$user."' and `users`.`password` = '".$password."'";
			if(($result = mysqli_query($con,$sql))){
				$rows = mysqli_num_rows($result);
				if ($rows > 0) {
					$result = mysqli_fetch_array($result);
					$date1 = new DateTime("now");
					$date2 = new DateTime($result[4]);
					if($date1 < $date2){
						$subs = "Active";
					}else{
						$subs = "Expired";		
					}
					$userdata = array(
					"account number" => $result[0],
					"name" => $result[1], 
					"last name" => $result[2], 
					"status" =>$result[3], 
					"expiry time" =>$result[4], 
					"subscription"=> $subs);
					$response = array("0","200","User Authentication Correct",$userdata);
					return $response;
				} else {
					$response = array("1","400","User Authentication Failed","Incorrect username or password");
					return $response;
				}
				$DB->close($con); 	
			}else{
				$response = array("2","500","User Authentication Failed","Unable to complete the operation"); //"MySQL Error: ".mysqli_error($con)
				return $response;	
			}
		}else{
			$response = array("1","400","User Authentication Failed","Invalid parameters");
			return $response;
		}
	}
	
	public function get_user_info($user, $password, $getuser){	
		$login = $this->login($user, $password);
		if (is_array($login) && $login[0] != 0){
			$response = array("4","400","Get User Information Failed","User Authentication Failed");
			return $response;
		}else{
			$sum =0;
			if ($this->email_validation($getuser,5,100) == false){
				$sum++;
			}
			if($sum == 0){
				if($login[3]["status"]==2 || $user == $getuser){
					require_once 'DB_Connect.php';
					$DB = new DB_Connect();
					$con = $DB->connect();
					$sql = "SELECT * FROM `sdxess_users` as `users` inner join `sdxess_subs_active` as `subs` ON `users`.`account_number` = `subs`.`account_number` WHERE `users`.`username` = '".$getuser."'";
					if(($result = mysqli_query($con,$sql))) {
						$rows = mysqli_num_rows($result);
						if ($rows > 0) {
							$result = mysqli_fetch_array($result);
							$date1 = new DateTime("now");
							$date2 = new DateTime($result[12]);	
							if($date1 < $date2){
								$subs = "Active";
							}else{
								$subs = "Expired";		
							}
							$userdata = array(
							"account number" => $result[1],
							"name" => $result[4], 
							"last name" => $result[5],
							"country" => $result[6],
							"address" =>$result[7],
							"registration date" =>$result[8],
							"status" =>$result[9],
							"start_time" =>$result[11],
							"expiry_time" =>$result[12],
							"subscription"=> $subs);			
							$response = array("0","200","Get User Information Success",$userdata);
							return $response;
						} else {
							$response = array("2","400","Get User Information Failed","User information not found");
							return $response;
						}
						$DB->close($con); 
					}else{
						$response = array("5","400","Get User Information Failed","Unable to complete the operation");//or die(mysqli_error($con));
						return $response;
					}
				}else{
					$response = array("1","401","Get User Information Failed","Access Forbidden");
					return $response;
				}
			}else{
				$response = array("3","400","Get User Information Failed","Invalid target user parameters");
				return $response;
			}
		}
	}
	
	public function user_change_password($user, $password, $targe_user, $newpassword){	
		$login = $this->login($user, $password);
		if (is_array($login) && $login[0] != 0){
			$response = array("5","400","Password Change Failed","User Authentication Failed");
			return $response;
		}else{
			$sum = 0;
			if ($this->email_validation($targe_user,5,100) == false){
				$sum++;
			}
			if ($this->alphanumetic_validation($newpassword,32,32)== false){
				if(!empty($error_msg)){
					$error_msg	=$error_msg.", ";
				}
				$sum++;
			}
			if($sum == 0){
				if($login[3]["status"]==2 || $user == $targe_user){
					require_once 'DB_Connect.php';
					$DB = new DB_Connect();
					$con = $DB->connect();
					$sql = "SELECT * FROM `sdxess_users` where `username` = '".$targe_user."'";
					if(($result = mysqli_query($con,$sql))){
						$rows = mysqli_num_rows($result);
						if ($rows == 1) {
							$sql = "UPDATE `db_sdxess`.`sdxess_users` SET `password` = '".$newpassword."' WHERE `sdxess_users`.`username` = '".$targe_user."'";
							if(mysqli_query($con,$sql)){
								$response = array("3","200","Password Changed Successfully",NULL);
								return $response;
							}else{
								$response = array("2","500","Password Change Failed","Unable to complete the operation");
								return $response;						
							} 
							$DB->close($con); 
						}else{
							$response = array("5","400","Password Change Failed","Target user does not exists");
							return $response;		
						}
					}else{
						$response = array("2","500","Password Change Failed","Unable to complete the operation");
						return $response;		
					}	
				}else{
					$response = array("1","401","Password Change Failed","Access Forbidden");
					return $response;			
				}
			}else{
				$response = array("4","400","Password Change Failed","Invalid target user parameters");
				return $response;				
			}
		}
	}
	public function user_registration($user,$pass,$fname,$lname,$country,$address){
		$sum = 0;
		$error_msg = '';
        if ($this->email_validation($user,5,100) == false){
			$error_msg = $error_msg."username";
			$sum++;
		}
		 if ($this->alphanumetic_validation($pass,32,32)== false){
			if(!empty($error_msg)){
				$error_msg	=$error_msg.", ";
			}
			$error_msg = $error_msg."password";
			$sum++;
		}
		if ($this->alphabetic_validation($fname,3,35)== false){
			if(!empty($error_msg)){
				$error_msg	=$error_msg.", ";
			}
			$error_msg = $error_msg."first name";
			$sum++;
		}	
		if ($this->alphanumetic_validation($lname,3,35)== false){
			if(!empty($error_msg)){
				$error_msg	=$error_msg.", ";
			}
			$error_msg = $error_msg."last name";
			$sum++;
		}
		if ($this->alphabetic_validation($country,2,2)== false){
			if(!empty($error_msg)){
				$error_msg	=$error_msg.", ";
			}
			$error_msg = $error_msg."country";
			$sum++;
		}
		if ($this->text_validation($address,10,300)== false){
			if(!empty($error_msg)){
				$error_msg	=$error_msg.", ";
			}
			$error_msg = $error_msg."address";
			$sum++;
		}
		
		if($sum ==0){
			require_once 'DB_Connect.php';
			$DB = new DB_Connect();
			$con = $DB->connect();      
		    $sql = "SELECT * FROM `sdxess_users` where `username` = '".$user."'";
			if(($result = mysqli_query($con,$sql))){
				$rows = mysqli_num_rows($result);
				if ($rows > 0) {
					$response = array("0", "409","User Registration Failed", "User: '".$user."' already exists");
					$DB->close($con); 
					return  $response ;	
				}else{
					$sql = "INSERT INTO `db_sdxess`.`sdxess_users` (`user_id`, `account_number`, `username`, `password`, `first_name`, `last_name`, `country`, `address`, `registration_date`, `status`) 
					VALUES (NULL,  (SELECT CONCAT('A', CAST((SELECT LPAD((SELECT AUTO_INCREMENT FROM information_schema.tables WHERE table_name = 'sdxess_users' AND table_schema = 'db_sdxess'), 7, '0')) AS CHAR))), 
					'".$user."', '".$pass."', '".$fname."', '".$lname."','".$country."', '".$address."', CURRENT_TIMESTAMP, '1')";   
					if(mysqli_query($con,$sql)){
						$userinfo = $this->get_user_info($user, $pass, $user);
						$DB->close($con);
						$response = array("1","201","User Registered Successfully",$userinfo[3]);
						return $response;
					}else{
						$response = array("0","500","User Registration Failed:", "Unable to complete the operation" ); //"MySQL Error: ".mysqli_error($con)
						$DB->close($con); 
						return  $response ;	
					}	
				}					
			}else{
				$response = array("0","500","User Registration Failed:", "Unable to complete the operation" ); //"MySQL Error: ".mysqli_error($con)
				$DB->close($con); 
				return  $response ;	
			}
		}else{
			if($sum == 1){
				$error_msg = "Check wrong field: ".$error_msg;
			}else{
				$error_msg = "Check wrong fields: ".$error_msg;				
			}
			$response = array("0","400","User Registration Failed",$error_msg);
			return $response;
		}
	}
	
	public function alphanumetic_validation($value,$minlength,$maxlength){
		if(strlen($value)>= $minlength && strlen($value)<= $maxlength){
			if(ctype_alnum($value)==true){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
    public function alphabetic_validation($value,$minlength,$maxlength){
		if(strlen($value)>= $minlength && strlen($value)<= $maxlength){
			if(ctype_alpha($value)==true){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
    public function numeric_validation($value,$minlength,$maxlength){
		if(strlen((string)$value)>= $minlength && strlen((string)$value)<= $maxlength){
			if(is_numeric($value)==true){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
    public function email_validation($value,$minlength,$maxlength){
		if(strlen($value)>= $minlength && strlen($value)<= $maxlength){
			if (filter_var($value, FILTER_VALIDATE_EMAIL)) {
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	 public function text_validation($value,$minlength,$maxlength){ // allowed space , . - ( )
	 if(strlen($value)>= $minlength && strlen($value)<= $maxlength){
		    $valid_chars = array(' ',',','.','-','_','(',')');
			foreach ($valid_chars as &$current_char) {
				$value = str_replace($current_char,'', $value);
				//echo $value."\n";
			}
			if(ctype_alnum($value)==true){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	public function Rmethod_validation($received_method, $method){
		if(strcasecmp($received_method, $method) != 0){
			return deliver_response(400,"Invalid Request","Request method must be ".$method);
		}else{
			return true;
		}
	}
	public function Ctype_validation($received_contentype, $contentType){
		if(strcasecmp($received_contentype, $contentType) != 0){
			return deliver_response(400,"Invalid Request",'Content type must be: '.$contentType);
		}else{
			return true;
		}
	}
	public function Array_validation($array){
		if(!is_array($array)){
			return	deliver_response(400,"Invalid Request","Received content contained invalid JSON");
		}else{
			return true;
		}
	}	
}
?>