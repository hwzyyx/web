<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="jf" uri="/Jfinal.tld"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>Client Side Pagination in DataGrid - jQuery EasyUI Demo</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
	<script type="text/javascript">
		$(function(){

			$.ajax({
				url:'getOrgComboTree?flag=1',
				method:"POST",
				dataType:'json',
				success:function(rs) {
				}
			});

			$("#orgCode").combotree({

				url:'getOrgComboTree?flag=1',
				onLoadSuccess:function(node,data){
				}
			});	
			
		});	
	</script>
</head>
<body>
<div id="aa">lllllljjj</div>

<div id="orgCode"></div>
	
</body>
</html>

