<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<link rel="stylesheet" type="text/css" href="iconfont/iconfont.css">

<style type="text/css">


.icon-red:hover{
  color:#ff0000;
}

.icon-green:hover {
  color:#00ff00;
  opacity:1;
}

.icon-yellow:hover {
  color:#f4ea2a;
  opacity:1;
}


.busyFreeCss:hover{
	
}


</style>

<script type="text/javascript">
	
	//打开签入dialog
	function execSignIn() {
		$("#signInDialog").dialog("setTitle","签入座席").dialog("open");
	}
	
	//执行签入操作
	function doSignIn() {
		
		var agentNumber = $("#agentNumber").numberbox('getValue');
		
		$("#signInForm").form('submit',{
			url:'doCti',
			onSubmit:function(param) {
				
				param.actionName = "signIn";
				
				var v = $(this).form('validate');
				
				if(v) {
					$.messager.progress({
						msg:'系统正在处理,请稍候...'
					});
				}
				
				return $(this).form('validate');
			},
			success:function(data) {
				
				$.messager.progress('close');
				
				var result = JSON.parse(data);

				var statusCode = result.statusCode;
				var message = result.message;

				window.parent.showMessage(message,statusCode);
				
				if(statusCode == 'success') {    //如果关联成功时,将会返回新的登录信息
					
					var loginInfo = result.extraMessage;
					var agentNumber = result.extraMessage2;
					
					$("#signInForm").form('clear');
					$("#signInDialog").dialog('close');
					
					$("#loginInfoDiv").html(loginInfo);
					
					//签入成功后，将签入按钮关闭，将签出按钮显示
					$("#signInDiv").css('display','none');
					$("#signOffDiv").css('display','');
					
					//签入成功后, 当前座席号码需要赋值
					currAgentNumber = agentNumber;    //用于外呼按钮判断
				}
			}
		});
		
	}
	
	//执行签出操作
	function execSignOff() {
		
		$.messager.confirm('提示','你确定要签出座席吗?座席签出后来电弹屏、话务操作等功能将无法使用!',function(r) {
			
			if(r) {
				
				$.messager.progress({
					msg:'系统正在处理,请稍候...'
				});
				
				$.ajax({
					url:'doCti?actionName=signOff',
					method:'POST',
					dataType:'json',
					success:function(rs) {
						
						$.messager.progress('close');
						
						var statusCode = rs.statusCode;    //返回结果类型
						var message = rs.message;          //返回执行信息
						var loginInfo = rs.extraMessage;   //返回登录信息
						window.parent.showMessage(message,statusCode);
						
						$("#loginInfoDiv").html(loginInfo);
						
						//签出后，将签入按钮显示，将签出按钮隐藏
						$("#signInDiv").css('display','');
						$("#signOffDiv").css('display','none');
						
						//同时,将当前座席号码变量置空
						currAgentNumber = '';
					}
				});
				
			}
			
		});
		
	}
	
	//打开准备外呼的弹窗，用于输入客户号码并准备执行外呼
	function execCallOut() {
		
		if(currAgentNumber == null || currAgentNumber == '') {
			window.parent.showMessage("当前登录账户暂未签入座席,无法执行外呼!",'error');
			return;
		}
		
		$("#callOutDialog").dialog("setTitle","执行外呼").dialog("open");
		
	}
	
	//执行外呼的过程
	function doCallOut() {
		
		var clientNumber = $("#clientNumber").textbox('getValue')
		
		if(isNaN(clientNumber)) {
			window.parent.showMessage("输入的客户号码非数字号码,请重新检查!",'error');
			return;
		}
		
		$("#callOutForm").form('submit',{
			url:'doCti',
			
			onSubmit:function(param) {
				
				param.actionName = 'callOut';
				
				var v = $(this).form('validate');
				
				if(v) {
					$.messager.progress({
						msg:'系统正在处理,请稍候...'
					});
				}
				
				return $(this).form('validate');
				
			},
			success:function(data) {
				
				$.messager.progress('close');

				var result = JSON.parse(data);

				var statusCode = result.statusCode;
				var message = result.message;

				window.parent.showMessage(message,statusCode);
				
				if(statusCode=='success') {
					$("#callOutDialog").dialog('close');
				}
				
			}
		});
		
	}
	
	//执行通话保持
	function execHoldOn() {
		//alert("执行通话保持");
		if(currAgentNumber == null || currAgentNumber == '') {
			window.parent.showMessage("当前登录账户暂未签入座席,无法执行保持!",'error');
			return;
		}
		
		$.messager.progress({
			msg:'系统正在处理,请稍候...'
		});
		
		$.ajax({
			url:'doCti?actionName=holdOn',
			method:'POST',
			dataType:'json',
			success:function(rs) {
				$.messager.progress('close');
				
				var statusCode = rs.statusCode;    //返回结果类型
				var message = rs.message;          //返回执行信息
				window.parent.showMessage(message,statusCode);
				
				if(statusCode == 'success') {      //如果执行通话保持成功
					$("#holdOnDiv").css("display","none");
					$("#cancelHoldOnDiv").css("display","");
				}
				
			}
		});
		
	}
	
	//执行取消通话保持
	function execCancelHoldOn() {
		//alert("执行通话保持");
		
		if(currAgentNumber == null || currAgentNumber == '') {
			
			$("#holdOnDiv").css("display","");
			$("#cancelHoldOnDiv").css("display","none");
			
			window.parent.showMessage("当前登录账户暂未签入座席,恢复通话失败!",'error');
			return;
		}
		
		$.messager.progress({
			msg:'系统正在处理,请稍候...'
		});
		
		$.ajax({
			url:'doCti?actionName=cancelHoldOn',
			method:'POST',
			dataType:'json',
			success:function(rs) {
				$.messager.progress('close');
				
				var statusCode = rs.statusCode;    //返回结果类型
				var message = rs.message;          //返回执行信息
				window.parent.showMessage(message,statusCode);
				
				if(statusCode == 'success') {
					$("#holdOnDiv").css("display","");
					$("#cancelHoldOnDiv").css("display","none");
				}
				
			}
		});
		
	}
	
	//执行呼叫转移,弹出弹窗，用于输入要转移到的目标号码
	function execCallForward() {
		//alert("执行呼叫转移");
		
		if(currAgentNumber == null || currAgentNumber == '') {
			window.parent.showMessage("当前登录账户暂未签入座席,无法执行转移!",'error');
			return;
		}
		
		$("#callForwardDialog").dialog('setTitle',"呼叫转移").dialog('open');
		
		
	}
	
	//执行呼叫转移操作
	function doCallForward() {
		
		var forwardNumber = $("#forwardNumber").textbox('getValue')
		
		if(isNaN(forwardNumber)) {
			window.parent.showMessage("输入的转移号码非数字号码,请重新检查!",'error');
			return;
		}	
		
		$("#callForwardForm").form('submit',{
			url:'doCti',
			onSubmit:function(param) {
				
				param.actionName = "callForward";
				
				var v = $(this).form('validate');
				
				if(v) {
					$.messager.progress({
						msg:'系统正在处理,请稍候...',
						interval:3000
					});
				}
				
				return $(this).form('validate');
			},
			success:function(data) {
				
				$.messager.progress('close');

				var result = JSON.parse(data);

				var statusCode = result.statusCode;
				var message = result.message;

				window.parent.showMessage(message,statusCode);
				
				if(statusCode == 'success') {
					
					$("#callForwardDialog").dialog('close');
					
				}
				
			}
		});
		
	}
	
	
	function execBusy() {
		//alert("执行示忙");
		
		if(currAgentNumber == null || currAgentNumber == '') {
			window.parent.showMessage("当前登录账户暂未签入座席,无法执行示忙!",'error');
			return;
		}
		
		$.messager.confirm('提示','您确定要将座席示忙吗？示忙后,座席将无法接听所有来电!',function(r) {
			
			if(r) {
				
				$.messager.progress({
					msg:'系统正在处理,请稍候...'
				});
				
				$.ajax({
					url:'doCti?actionName=busy',
					method:'POST',
					dataType:'json',
					success:function(rs) {
						
						$.messager.progress('close');
						
						var statusCode = rs.statusCode;    //返回结果类型
						var message = rs.message;          //返回执行信息
						window.parent.showMessage(message,statusCode);
						
						if(statusCode == 'success') {
							
							$("#busyDiv").css('display','none');
							$("#freeDiv").css('display','');
						}
						
					}
				});
				
			}
			
		});
		
	}
	
	function execFree() {
		
		if(currAgentNumber == null || currAgentNumber == '') {
			window.parent.showMessage("当前登录账户暂未签入座席,无法执行示闲!",'error');
			return;
		}
		
		$.messager.progress({
			msg:'系统正在处理,请稍候...'
		});
		
		$.ajax({
			url:'doCti?actionName=free',
			method:'POST',
			dataType:'json',
			success:function(rs) {
				
				$.messager.progress('close');
				
				var statusCode = rs.statusCode;    //返回结果类型
				var message = rs.message;          //返回执行信息
				window.parent.showMessage(message,statusCode);
				
				if(statusCode == 'success') {
					
					$("#busyDiv").css('display','');
					$("#freeDiv").css('display','none');
				}
				
			}
		});
		
	}
	
	function execHangup() {
		
		if(currAgentNumber == null || currAgentNumber == '') {
			window.parent.showMessage("当前登录账户暂未签入座席,无法挂机!",'error');
			return;
		}
		
		$.messager.progress({
			msg:'系统正在处理,请稍候...'
		});
		
		$.ajax({
			url:'doCti?actionName=hangup',
			method:'POST',
			contentType:'hangup-getAgentState',
			dataType:'json',
			success:function(rs) {
				$.messager.progress('close');
				
				var statusCode = rs.statusCode;    //返回结果类型
				var message = rs.message;          //返回执行信息
				window.parent.showMessage(message,statusCode);
			}
		});
		
	}
	
	
	
	
	
</script>

<div style="vertical-align: bottom;position:absolute;right:200px;bottom:3px;">
	<!-- 
	<img src="themes/ctiicons/dialout.png" style="width:30px;height:30px" onclick="doCti(1)" /><span style="color:#62C7FB">外呼</span>
	<img src="themes/ctiicons/holdon.png" style="width:30px;height:30px" onclick="doCti(2)" /><span style="color:#62C7FB">保持</span>
	<img src="themes/ctiicons/transfer.png" style="width:30px;height:30px" onclick="doCti(3)" /><span style="color:#62C7FB">转移</span>
	<img src="themes/ctiicons/busy.png" style="width:30px;height:30px" onclick="doCti(4)" /><span style="color:#62C7FB">示忙</span>
	<img src="themes/ctiicons/hangup.png" style="width:30px;height:30px" onclick="doCti(5)" class="cti_hangup" onmouseover="this.src='themes/ctiicons/hangup_red.png'" onmouseout="this.src='themes/ctiicons/hangup.png'" /><span style="color:#62C7FB">挂机</span>
	 -->
	 
	 <!-- 签入 -->
	 <div id="signInDiv" style="float:left;">
		 <i class="iconfont icon-green" id="calloutIcon" onclick="execSignIn()">&#xe844;</i><span style="color:#ffffff;font-size: 14px;">&nbsp;签入&nbsp;</span>
	 </div>
	 <!-- 签出 -->
	 <div id="signOffDiv" style="display:none;float:left;">
		 <i class="iconfont icon-red" id="calloutIcon" onclick="execSignOff()">&#xe843;</i><span style="color:#ffffff;font-size: 14px;">&nbsp;签出&nbsp;</span>
	 </div>
	
	 <!-- 外呼 --> 
	 <div id="callOutDiv" style="float:left;">
		 <i class="iconfont icon-green" id="calloutIcon" onclick="execCallOut()">&#xe600;</i><span style="color:#ffffff;font-size: 14px;">&nbsp;外呼&nbsp;</span>
	 </div>
	 
	 <!-- 保持 -->
	 <div id="holdOnDiv" style="float:left;">
		 <i class="iconfont icon-yellow" id="holdIcon" onclick="execHoldOn()">&#xe67b;</i><span style="color:#ffffff;font-size: 14px;">&nbsp;保持&nbsp;</span>
	 </div>
	 <!-- 取消保持 -->
	 <div id="cancelHoldOnDiv" style="display:none;float:left;">
	 	<i class="iconfont icon-green" id="cancelHoldIcon" onclick="execCancelHoldOn()">&#xe633;</i><span style="color:#ffffff;font-size: 14px;">&nbsp;恢复&nbsp;</span>
	 </div>	 
	 
	 <!-- 转移 -->
	 <div style="float:left;">
		 <i class="iconfont icon-green" id="exchangeIcon" onclick="execCallForward()">&#xe678;</i><span style="color:#ffffff;font-size: 14px;">&nbsp;转移&nbsp;</span>
	 </div>
	 
	 <!-- 示忙 -->
	 <div id="busyDiv" style="float:left;">
		 <i class="iconfont icon-red" id="busyIcon" onclick="execBusy()">&#xe60f;</i><span style="color:#ffffff;font-size: 14px;">&nbsp;示忙&nbsp;</span>
	 </div>
	 <!-- 示闲 -->
	 <div id="freeDiv" style="display:none;float:left;">
	 	<i class="iconfont icon-green" id="freeIcon" onclick="execFree()">&#xe6a4;</i><span style="color:#ffffff;font-size: 14px;">示闲</span>
	 </div>
	
	 <!-- 挂机 --> 
	 <div style="float:left;">
		 <i class="iconfont icon-red" id="hangupIcon" onclick="execHangup()">&#xe848;</i><span style="color:#ffffff;font-size: 14px;">挂机</span>
	 </div>
	 
</div>


<!-- 座席签入 Dialog -->
<div id="signInDialog" class="easyui-dialog" title="签入座席" data-options="width:300,height:200" modal="true" closed="true" buttons="#signInBtn" >
	
	<form id="signInForm" >
		
		<table>
			<tr>
				<td style="height: 100px;width:200px;padding-left:20px;">座席号码：<input name="agentNumber" id="agentNumber" class="easyui-numberbox" type="text" required="true" missingMessage="座席号码不能为空!"></input></td>
			</tr>
		</table>
		
		<div id="signInBtn">
			<a href="#" class="easyui-linkbutton" iconCls="icon-cancel" onClick="javascript: $('#signInForm').form('clear');$('#signInDialog').dialog('close');">取消</a> &nbsp;&nbsp;
			<a href="#" class="easyui-linkbutton" iconCls="icon-ok" onClick="doSignIn()">签入</a>
		</div>
		
	</form>
	
</div>


<!-- 执行外呼 dialog -->
<div id="callOutDialog" class="easyui-dialog" title="执行外呼" data-options="width:300,height:200" modal="true" closed="true" buttons="#callOutBtn">
	
	<form id="callOutForm">
		<table>
			<tr>
				<td style="height:100px;width:200px;padding-left:20px;">客户号码：<input name="clientNumber" id="clientNumber" class="easyui-textbox" type="text" required="true" missingMessage="请输入客户号码（客户手机号码、电话号码或是其他座席号码）"></td>
			</tr>
		</table>
	</form>
	
	<div id="callOutBtn">
		<a href="#" class="easyui-linkbutton" iconCls="icon-cancel" onClick="javascript: $('#callOutForm').form('clear');$('#callOutDialog').dialog('close');">取消</a> &nbsp;&nbsp;
		<a href="#" class="easyui-linkbutton" iconCls="icon-ok" onClick="doCallOut()">外呼</a>
	</div>
	
</div>


<!-- 执行呼叫转移 -->
<div id="callForwardDialog" class="easyui-dialog" title="执行呼叫转移" data-options="width:300,height:200" modal="true" closed="true" buttons="#callForwardBtn">
	
	<form id="callForwardForm">
		<table>
			<tr>
				<td style="height:100px;width:200px;padding-left:20px;">目标号码：<input name="forwardNumber" id="forwardNumber" class="easyui-textbox" type="text" required="true" missingMessage="请输入客户号码（客户手机号码、电话号码或是其他座席号码）"></td>
			</tr>
		</table>
	</form>
	
	<div id="callForwardBtn">
		<a href="#" class="easyui-linkbutton" iconCls="icon-cancel" onClick="javascript: $('#callForwardForm').form('clear');$('#callForwardDialog').dialog('close');">取消</a> &nbsp;&nbsp;
		<a href="#" class="easyui-linkbutton" iconCls="icon-ok" onClick="doCallForward()">转移</a>
	</div>
	
</div>






























