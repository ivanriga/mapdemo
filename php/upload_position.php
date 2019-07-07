<?php

error_reporting(0); 
$mysql_host = "fdb26.awardspace.net";
$mysql_database = "3087854_mapdemo";
$mysql_user = "3087854_mapdemo";
$mysql_password = "vxfpzad23xReGvC";

$link = mysql_connect($mysql_host, $mysql_user, $mysql_password);
if (!$link) {
    die('connection error: ' . mysql_error());
}



$conn=mysql_select_db($mysql_database , $link) or die('Could not select database.');
if (!mysql_set_charset('utf8', $link)) {
    echo "Error: Unable to set the character set.\n";
    exit;
}
//echo 'Your current character set is: ' .  mysql_client_encoding($link);

 //echo "$conn=".$conn;

$latitude= $_GET['latitude'];
$longitude= $_GET['longitude'];
$user_id= $_GET['user_id'];



  $sth = mysql_query( "INSERT INTO  locations ( latitude,longitude,user_id)   VALUES (".$latitude.", ".$longitude.",  ".$user_id.")");

  //mysql_query($q)OR die(mysql_error());
  echo "upload success!";



mysql_close($link);
?>