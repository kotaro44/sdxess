<?php
function deliver_response($status,$status_message,$data){
	header("HTTP/1.1 $status $status_message");
	$response['status']=$status;
	$response['status_message']=$status_message;
	if (is_array($data) == true){
		$response['data']=$data;	
	}else{
		$response['data']=$data;	
	}
	$json_response = json_encode($response);
	echo $json_response;
}
?>