<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>登录</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		$(function(){
			$(document).keydown(function(event) { 
				if (event.keyCode == 13) { 
					doLogin();
				}; 
			});

			$.ajax({
				url:'getDictName?groupCode=COPYRIGHT&dictCode=1',
				method:'POST',
				dataType:'json',
				success:function(rs) {
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
						$("#copyright").text(message);						
					}
				}
			});
		});
		
		function doLogin() {
			var operId = $.trim($("#OPER_ID").val());
			var password = $.trim($("#PASSWORD").val());
			var callNumber = $.trim($("#CALL_NUMBER").val());
			//alert(isNaN(callNumber));

			//alert("OPERID:" + operId + ",password:" + password + ",callNumber=:" + callNumber);
			
			if(operId.length==0 || password.length==0) {
				$.messager.alert("提示",'工号及密码不能为空，请重新填写登录信息!',"error");
			}else if(isNaN(callNumber)){
				$.messager.alert("提示",'座席号只能为数字!',"error");
			}else {
				$.ajax({
					url:'doLogin?operId=' + operId + '&password=' + password + '&callNumber=' + callNumber,
					method:'POST',
					dataType:'json',
					success:function(rs) {
						var statusCode = rs.statusCode; //返回的结果类型
						var message = rs.message;       //返回执行的信息
						if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
							document.location = "index";						
						}else {
							$.messager.alert("提示","登录失败:" + message,"error");
						}
					}
				});
			};
		}

	</script>
</head>
<body style="text-align: center;background:url('themes/icons/login_bg.png') repeat-x;">
	<div style="position: absolute;top:120px;left:350px;">
		<span style="color:white;font-weight: bold;font-size: 30px;">呼叫中心系统</span>
	</div>
	<input name="operator.OPER_ID" id="OPER_ID" style="position:absolute;top:225px;left:755px;width:175px;border:0px;"/>
	<input type="password" name="operator.PASSWORD" id="PASSWORD" style="position:absolute;top:262px;left:755px;width:175px;border:0px;"/>
	<input name="operator.CALL_NUMBER" id="CALL_NUMBER" style="position:absolute;top:300px;left:755px;width:104px;border:0px;"/>
	</div>		
	<div style="position:absolute;top:342px;left:732px;width:177px;heigth:26px;text-align: left;" onclick="doLogin();">
		<span style="color:#4D90FE">|</span>
	</div>

	<div style="position:absolute;top:470px;left:540px;">
		<span style="color:#9E9FA0;font-size: 13px;" id="copyright"></span>
	</div>

			
</body>
</html>