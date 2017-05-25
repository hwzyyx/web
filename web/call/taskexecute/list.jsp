<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>任务执行</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript">

		var currTaskId = null;	
		$(function(){
			$("#callTaskDg").datagrid({
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				url:'taskExecute/datagrid?taskType=1'
			});

			//先将初始化两个 panel
			$("#listPanel").panel('open'); 
			$("#exePanel").panel('close');

			//先将请求数据的按钮不可用
			$("#reqDataBtn").removeAttr("disabled");
			$("#reqDataBtn").attr('disabled','disabled');

			$("#callTelephoneDg").datagrid({
				pageSize:10,
				pagination:true,      
				fit:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				singleSelect:true,
				pageList:[10],
				//checkbox:true,
				toolbar:'#callTelephoneDgtool',
				onLoadSuccess:function(data) {   //当数据加载成功时，先将所有的状态按钮不可操作
					var count = data.total;      //得到当前工号的可呼的数据的数量
					if(count>0) {                //如果有可用数据时，则不可请求数据
						$("#reqDataBtn").removeAttr("disabled");
						$("#reqDataBtn").attr('disabled','disabled');
					}else {
						$("#reqDataBtn").removeAttr("disabled");
					}

					//$(".cus-easyui-linkbutton").linkbutton({text:'外呼',plain:true,iconCls:'icon-dial'});
				}
			});
		});
		
		function rowformatter(value,data,index){

			return "<a href='#' style='text-decoration:none' onclick='javascript:taskExecute(\"" + data.CT_ID + "\",\"" + data.TASK_NAME + "\",\"" + data.CALLERID + "\")'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='执行任务' style='width:100px;padding:5px;float:top;'><img src='themes/icons/forward.png' style='margin-top:5px;' border='0'><img src='themes/icons/forward.png' style='margin-top:5px;' border='0'></a></div>"; 
			
		}

		//点击执行时，执行的方法
		function taskExecute(taskId,taskName,callerId) {
			$("#listPanel").panel('close');     //关闭任务列表
			$("#exePanel").panel('open');       //打开任务执行界面
			
			$("#exePanel").panel('setTitle','任务名称：【<span style="color:red;font-weight:bold;">' + taskName + '</span>】');       //打开任务执行界面

			$("#reqTelCombobox").combobox("select","10");

			currTaskId = taskId;                //设置当前taskId

			$("#callTelephoneDg").datagrid({
				url:'taskExecute/telephoneDatagrid?taskId=' + taskId
			});
			
		}

		//点击返回时执行的方法
		function returnTaskList() {
			$("#listPanel").panel('open');       //打开任务列表
			$("#exePanel").panel('close');       //关闭任务执行界面

			
		}
		
		function findData() {
			$("#callTaskDg").datagrid('load',{
				taskName:$("#taskName").textbox('getValue'),
				taskType:$("#taskType").val(),
				startTime:$("#startTime").datebox("getValue"),
				endTime:$("#endTime").datebox("getValue")
			});
		}

		function sexformatter(value,data,index) {
			if(value=="0") {
				return '<span style="color:red;">女</span>';
			}else if(value=="1") {
				return '<span style="color:blue;">男</span>';
			}
		}

		function telephoneStateformatter(value,data,index) {
			if(value=="0") {
				return '<span style="color:black;">新号码</span>';
			}else if(value=="1") {
				return '<span style="color:purple;">已分配</span>';
			}else if(value=="2") {
				return '<span style="color:green;">成功</span>';
			}else if(value=="3") {
				return '<span style="color:red;">失败</span>';
			}
		}

		//执行外呼
		function execformatter(value,data,index) {
			//return "<a data-options=\"iconCls:'icon-dial'\" href='javascript：void(0);'>外呼</a>";
			
			return "<div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='外呼' style='width:100px;padding:5px;float:top;'><a data-options=\"iconCls:'icon-dial'\" style='text-decoration:none' href='#' onclick='dial(\"" + data.TELEPHONE + "\",\"" + data.TEL_ID + "\",\"" + data.CT_ID + "\")'><img style='margin-top:2px;' src='themes/icons/dial-text.png' border='0'></a></div>";
		}

		function nocallformatter(value,data,index) {
			return data.TELEPHONE_COUNT - data.SUCCESS_COUNT - data.FAILURE_COUNT;
		}

		//点击外呼按钮时
		function dial(telephone,telId,taskId) {
			window.parent.showdialpanel(telephone,telId,taskId);
		}

		//请求外呼数据
		function reqCallDatas() {
			var reqCount = $("#reqTelCombobox").combobox('getValue');
			$.messager.progress({
				msg:'系统正在处理，请稍候...',
				interval:2000
			});
			$.ajax({
				type:'POST',
				dataType:"json",
				url:"taskExecute/reqCallDatas?taskId=" + currTaskId + "&reqCount=" + reqCount,
				success:function(rs) {
					$.messager.progress('close');
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode=='success') {     //如果请求成功时， 再加载已经请求的号码列表
						$("#callTelephoneDg").datagrid({
							url:'taskExecute/telephoneDatagrid?taskId=' + currTaskId
						});
					}
				}
			});

			
		}

		window.top['reload_callTelephoneDg']=function() {
			$("#callTelephoneDg").datagrid({
				url:'taskExecute/telephoneDatagrid?taskId=' + currTaskId
			});
		};
				
	</script>	
</head>

<body>
<div id="listPanel" style="padding:1px;width:99%" class="easyui-panel" title="外呼任务列表" data-options="fit:true">
	<!-- 包含任务列表 -->
	<%@ include file="/call/taskexecute/_taskList.jsp"%>
</div>

<div id="exePanel" style="padding:1px;width:99%" class="easyui-panel" title="外呼任务执行" data-options="fit:true,tools:'#exePanelTool'">
	<!-- 执行任务页面 -->
	<%@ include file="/call/taskexecute/_taskExecute.jsp"%>
	<div id="exePanelTool" style="padding-bottom:2px;">
		<div style="display:inline;padding-top:6px;" class="easyui-tooltip" title="返回" style="width:100px;padding:5px;float:top;"><a href="javascript:void(0)" onclick="returnTaskList()"><img src='themes/icons/back.png' border='0'><img src='themes/icons/back.png' border='0'></a></div>
	</div>
</div>

<div id="exePanel_bak" style="padding:1px;width:300px;display:none;" class="easyui-panel" title="外呼任务执行" data-options="fit:true,tools:'#exePanelTool'">
	<!-- 执行任务页面 -->
	<%@ include file="/call/taskexecute/_taskExecute.jsp"%>
	<div id="exePanelTool" style="padding-bottom:2px;">
		<div style="display:inline;padding-top:6px;" class="easyui-tooltip" title="返回" style="width:100px;padding:5px;float:top;"><a href="javascript:void(0)" onclick="returnTaskList()"><img src='themes/icons/back.png' border='0'><img src='themes/icons/back.png' border='0'></a></div>
	</div>
</div>
 
</body>
</html>

