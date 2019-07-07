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

$user_id= $_GET['user_id'];
if (isset($_GET['id']) ) {
    $id= $_GET['id'];
} else {
    $id=0;
}



$conn=mysql_select_db($mysql_database , $link) or die('Could not select database.');
if (!mysql_set_charset('utf8', $link)) {
    echo "Error: Unable to set the character set.\n";
    exit;
}


$sth = mysql_query("SELECT * from locations where  user_id = ".$user_id." and id>".$id." order by id desc limit 100");



$rows = array();
while($r = mysql_fetch_assoc($sth)) {
    $rows[] = $r;
}
print json_encode($rows);



mysql_close($link);
?>