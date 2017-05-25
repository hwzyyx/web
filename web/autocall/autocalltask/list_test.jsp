<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>Client Side Pagination in DataGrid - jQuery EasyUI Demo</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/color.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="base-loading.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	
	<script>
        function closes() {
            $("#Loading").fadeOut("normal", function () {


                $(this).remove();
                alert("数据载入完毕");


            });
        }
        var pc;
        $.parser.onComplete = function () {
            if (pc) {
                clearTimeout(pc);
            }
            pc = setTimeout(closes, 1000);

        }
    </script>
	
</head>
<body>

<div id='Loading'>
    <image src='themes/default/images/loading.gif'/>
    <font color="#2bd4cd" size="4">页面正在载入中···</font>
</div>


</body>
</html>

