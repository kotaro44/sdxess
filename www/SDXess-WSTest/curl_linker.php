<?php 
	$url = $_POST["a"];
	//Initiate cURL.
	$ch = curl_init($url);
	$jsonDataEncoded = json_encode($jsonData);
	//Choose Request method
	switch($_POST["c"]){
		case "post":
			curl_setopt($ch, CURLOPT_POST, 1);	
			curl_setopt($ch, CURLOPT_POSTFIELDS, $_POST["d"]);
			break;
		case "put":
			curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "PUT");	
			curl_setopt($ch, CURLOPT_POSTFIELDS, $_POST["d"]);
			break;
		default:
			curl_setopt($ch, CURLOPT_POST, 1);	
			curl_setopt($ch, CURLOPT_POSTFIELDS, $_POST["d"]);
			break;	
	}
	//Set the content type to application/json
	curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: '.$_POST["b"])); 
	//Execute the request
	$result = curl_exec($ch);
?>