<?php

$secret_key = "YOUR API SECRET HERE";

$api_key = urlencode("YOUR API KEY HERE");
$campaign_id = "111111"; //YOUR CAMPAIGN ID here
$timestamp = urlencode(date("Y-n-d\TH:i:s\Z"));
$url = urlencode("http://urlencode/"); // QR code URL here.

$code_name = "YOUR CODE NAME HERE";
$data = "codename=".$code_name."&codetype=premiumweb&url=".$url."&timestamp=".$timestamp."&apikey=".$api_key;

$hash = hash_hmac( "sha256", $data, $secret_key, true);
$hash = urlencode(base64_encode($hash));

//PROD url
$url = "https://api.scanlife.com/api/code/createcode?".$data."&signature=".$hash;
$data = file_get_contents($url);

echo $url . "<BR><br>";
echo $data;
