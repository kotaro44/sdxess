<?php
include_once("response.php");
class DB_Connect {
	public function connect()
	{
	   require_once 'DB_config.php';
	   if (!($link=mysqli_connect(DB_HOST,DB_USER,DB_PASSWORD,DB_DATABASE)))
	   { 
		  deliver_response(500,"Unable to establish database connection","Unable to complete the operation");
		  exit();
	   }
	   return $link;
	} 	
    public function close($link) {
        mysqli_close($link);
    }
}
?>
