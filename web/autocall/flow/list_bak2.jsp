<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>呼叫流程管理</title>
		<style>
			.one{
			    width: 450px;
			    height: 40px;
			    margin: 2px;
			}
			.boxList{
			    border: 1px solid #ff0033;
			    height: 550px;
			    margin: 30px;
			    position: relative;
			    width: 500px;
			}
			
			.tezml{
			    border: 3px #ff00ff dashed;
			}
			
			.div8{
			    float: left;
			}
		</style>
		<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
		<link rel="stylesheet" type="text/css" href="themes/color.css">
		<link rel="stylesheet" type="text/css" href="themes/icon.css">
		<link rel="stylesheet" type="text/css" href="demo.css">
		<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
		<link rel="stylesheet" type="text/css" href="iconfont/iconfont.css">
		<script src="echarts/echarts.min.js"></script>
		<script src="iconfont/iconfont.js"></script>
		<script type="text/javascript" src="jquery.min.js"></script>
		<script type="text/javascript" src="jquery.easyui.min.js"></script>
		<script type="text/javascript" src="Tdrag.js"></script>
		<script type="text/javascript" src="js.date.utils.js"></script>
		<script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
		<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
		<script>
			jQuery(function(){
			    
			    //第八个例子的拖拽
			    $(".div8").Tdrag({
			        scope:".boxList",
			        pos:true,
			        dragChange:true
			    });
			    
			
			
			})
		</script>
	</head>
<body>

	<div class="box">
    
	    <div class="example_one">
	       
	        <div class="example">
	            <div class="boxList">
	                <div class="one div8"><input type="button" value="第一个" style="width:450px;height:30px"/></div>
	                <div class="one div8"><input type="button" value="第二个" style="width:450px;"/></div>
	                <div class="one div8"><input type="button" value="第三个" style="width:450px;"/></div>
	                <div class="one div8"><input type="button" value="第四个" style="width:450px;"/></div>
	                <div class="one div8"><input type="button" value="第五个" style="width:450px;"/></div>
	                <div class="one div8"><input type="button" value="第六个" style="width:450px;"/></div>
	                <div class="one div8"><input type="button" value="第七个" style="width:450px;"/></div>
	                <div class="one div8"><input type="button" value="第八个" style="width:450px;"/></div>
	                <div class="one div8"><input type="button" value="第九个" style="width:450px;"/></div>
	            </div>
	
	        </div>
	    </div>
	
	</div>

</body>
</html>
