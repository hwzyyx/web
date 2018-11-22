<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>登录</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="md5.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		var webSiteName = '${webSiteName}';
		var copyrightInfo = '${copyrightInfo}';
		//alert("webSiteName:" + webSiteName + ",copyrightInfo:" + copyrightInfo);
		$(function(){
			$(document).keydown(function(event) { 
				if (event.keyCode == 13) { 
					doLogin();
				}; 
			});
			
			$("#webSiteNameSpan").text(webSiteName);
			$("#copyrightSpan").text(copyrightInfo);

		});
		
		function doLogin() {
			
			
			var operId = $.trim($("#OPER_ID").textbox('getValue'));
			var password = $.trim($("#PASSWORD").textbox('getValue'));
			//alert(operId + "    ==   " + password);
			if(operId.length==0 || password.length==0) {
				$.messager.alert("提示",'工号及密码不能为空，请重新填写登录信息!',"error");
			}else {
				
				var md5_password = md5(password);
				
				$.ajax({
					//url:'doLogin?operId=' + operId + '&password=' + password + '&callNumber=' + callNumber,
					url:'doLogin?operId=' + operId + '&password=' + md5_password,
					method:'POST',
					dataType:'json',
					success:function(rs) {
						var statusCode = rs.statusCode; //返回的结果类型
						var message = rs.message;       //返回执行的信息
						var extraMessage = rs.extraMessage;     //是否需要重新更改密码提示
						//alert("message:" + message + ",extraMessage:" + extraMessage);
						if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
							if(extraMessage==null || extraMessage=='' || extraMessage  == 'null') {
								
							}else {
								alert("登录成功," + extraMessage);
							}
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

<body style="text-align: center;background:url('themes/icons/login_bg_2.png') repeat-x;">
	<div style="position: absolute;top:120px;left:350px;">
		<span style="color:white;font-weight: bold;font-size: 30px;" id="webSiteNameSpan"></span>
	</div>
	
	<div style="position: absolute;top:230px;left:690px;">
		工&nbsp;&nbsp;&nbsp;&nbsp;号：<input style="width:180px;" name="operId" id="OPER_ID" class="easyui-textbox" type="text"></input><br/><br/>
		密&nbsp;&nbsp;&nbsp;&nbsp;码：<input style="width:180px;" name="password" id="PASSWORD" class="easyui-textbox" type="password"></input><br/><br/><br/>
		<a href="#" id="autoCallTaskSaveBtn" style="width:240px;" class="easyui-linkbutton" iconCls="icon-man" onclick="doLogin()">登录</a>
	</div>

	<div style="position:absolute;top:470px;left:540px;">
		<span style="color:#9E9FA0;font-size: 13px;" id="copyrightSpan"></span>
	</div>
			
</body>

</html>