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
			document.onkeydown = function(e) {
				if(e.keyCode=='13') {
					$("#loginButton").focus();
					$.trim($("#PASSWORD").textbox('getValue'));
					doLogin();					
				}
			}
			
		});

		
		function doLogin() {
			var operId = $.trim($("#OPER_ID").textbox('getValue'));
			var password = $.trim($("#PASSWORD").textbox('getValue'));
			var callNumber = $.trim($("#CALL_NUMBER").textbox('getValue')); 
			
			if(operId.length==0 || password.length==0) {
				$.messager.alert("提示",'工号及密码不能为空，请重新填写登录信息!',"error");
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
<<body style="text-align: center;background:url('themes/icons/login_bg.png') repeat-x;">-->
<!--<body style="text-align: center;">-->
		<div class="easyui-dialog" title="系统登录 " style="display:none;width:400px;padding:30px 70px 20px 70px;background:url('themes/icons/login_bg.png') repeat-x; " data-options="closable:false">
			<form id="loginForm" method="post">
				<div style="margin-bottom:2px">
					工　号：<input class="easyui-textbox" id="OPER_ID"  name="operator.OPER_ID" style="width:80%;height:25px;padding:12px" data-options="prompt:'Username',iconCls:'icon-man',iconWidth:38"></input>
				</div>
				<div style="margin-bottom:5px">
					密　码：<input class="easyui-textbox" id="PASSWORD"  name="operator.PASSWORD" type="password" style="width:80%;height:25px;padding:12px" data-options="prompt:'Password',iconCls:'icon-lock',iconWidth:38"></input>
				</div>
				<div style="margin-bottom:20px">
					座席号：<input class="easyui-textbox" id="CALL_NUMBER" name="operator.CALL_NUMBER" style="width:80%;height:25px;padding:12px" data-options="prompt:'座席号(选填)',iconCls:'icon-phone',iconWidth:38">
				</div>
				<div style="margin-bottom:5px;display:none;">
					<input type="checkbox" checked="checked">
					<span>Remember me</span>
				</div>
				<div>
					<a href="#" id="loginButton" onclick="doLogin()" class="easyui-linkbutton" data-options="iconCls:'icon-ok'" style="padding:5px 0px;height:30px;width:100%;">
						<span style="font-size:14px;">登录</span>
					</a>
				</div>
			</form>
		</div>
</body>
</html>