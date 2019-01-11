<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>自动外呼任务报表</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/color.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="http://echarts.baidu.com/gallery/vendors/echarts/echarts.min.js"></script>
	<script type="text/javascript">
	
		var currTaskId = null;
		var currTaskName = null;
		var taskCount = 0;
	
		//获取后台传来的数据
	    var orgComboTreeData = eval('${orgComboTreeData}');
		
	    var allTaskTypeComboboxDataFor0 = eval('${allTaskTypeComboboxDataFor0}');
		var allTaskTypeComboboxDataFor1 = eval('${allTaskTypeComboboxDataFor1}');
		
		var allReminderTypeComboboxDataFor0 = eval('${allReminderTypeComboboxDataFor0}');
		var allReminderTypeComboboxDataFor1 = eval('${allReminderTypeComboboxDataFor1}');
		
		var taskStateComboboxDataFor0 = eval('${taskStateComboboxDataFor0}');
		var taskStateComboboxDataFor1 = eval('${taskStateComboboxDataFor1}');
		
		var messageStateComboboxDataFor1 = eval('${messageStateComboboxDataFor1}');
	
		$(function(){
			
			initSelectCallerIdAssign();
			
			loadSearchData();
			
			showSimpleColumns();     //任务列表，显示简单的列
			
			$('#summaryDg').datagrid({toolbar:'#summaryDgTool'}).datagrid('loadData','');
		});
		
		//加载搜索栏的数据，主要是一些 combobox 数据的加载
		function loadSearchData() {
			//任务类型加载
			$("#taskType").combobox({
				valueField:'id',
				textField:'text',
				panelHeight:'auto'
			}).combobox('loadData',allTaskTypeComboboxDataFor1).combobox('setValue','empty');
			
			//催缴类型
			$("#reminderType").combobox({
				valueField:'id',
				textField:'text',
				panelHeight:'auto'
			}).combobox('loadData',allReminderTypeComboboxDataFor1).combobox('setValue','empty');
			
			//任务状态
			$("#taskState").combobox({
				valueField:'id',
				textField:'text',
				panelHeight:'auto'
		    }).combobox('loadData',taskStateComboboxDataFor1).combobox('setValue','empty');
			
			$("#messageState").combobox({
				valueField:'id',
				textField:'text',
				panelHeight:'auto'
		    }).combobox('loadData',messageStateComboboxDataFor1).combobox('setValue','empty');
			
			//搜索栏的组织加载
			$("#orgCode").combotree('loadData',orgComboTreeData).combotree({
				onLoadSuccess:function(node,data) {
					var t = $("#orgCode").combotree("tree");

					for(var i=0;i<data.length;i++) {
						node = t.tree("find",data[i].id);
						t.tree('check',node.target);
					}
					

					var selectRs = $("#orgCode").combotree('getValues');
					var orgCodes = selectRs.toString();
					var startTime = $("#startTime").datebox('getValue');
					var endTime = $("#endTime").datebox('getValue');
					var taskType = $("#taskType").combobox('getValue');
					var taskState = $("#taskState").combobox('getValue');

					$("#autoCallTaskDg").datagrid({
						pageSize:50,
						pagination:true,
						fit:true,
						toolbar:'#searchtool',
						rownumbers:true,
						rowrap:true,
						striped:true,
						checkbox:true,
						pageList:[30,50,100],
						url:'autoCallTask/datagrid',
						queryParams:{
							taskName:$("#taskName").val(),
							orgCode:orgCodes,
							startTime:startTime,
							endTime:endTime,
							taskType:taskType,
							taskState:taskState
						},
						onSelect:function(index,data) {
							var taskId = data.TASK_ID;         //取出任务ID
							var taskState = data.TASK_STATE;   //得到任务状态
							
						},
						onLoadSuccess:function(data) {         //加载成功后，先禁用所有的按钮

							for(var i=0;i<data.rows.length;i++) {
								var taskState = data.rows[i].TASK_STATE;   //获取任务状态
								var taskId = data.rows[i].TASK_ID;         //获取ID
								var reviewAdvice = data.rows[i].REVIEW_ADVICE;   //审核建议
								//如果状态为3,即是审核不通过时,显示审核不通过的提示
								if(taskState == '3') {
									var idInfo = "task" + taskId; 
									$("#" + idInfo).tooltip({
										position:'bottom',
										content:'<span style="color:#FF0000;">' + reviewAdvice + '</span>',
										onShow:function(){
										}
									});
								}
								
								//主叫号码列表显示
								var callerIdDescRs = data.rows[i].CALLERID_DESC;
								callerIdDescRs = callerIdDescRs.replace(/\|/gm,'<br>');
								//console.log("hwz---" + callerIdDescRs);
								$("#calleriddesc_" + i).tooltip({
									position:'top',
									content:callerIdDescRs
								});
							}
							
						}
					});
				
				}
			});
			
		}
		
		//任务的完成率
		function finishrateformatter(value,data,index) {
			htmlstr='<div class="easyui-progressbar progressbar easyui-fluid" style="width: 100%; height: 20px;">'
		         +'<div class="progressbar-value" style="width: 100%; height: 20px; line-height: 20px;"> '
		             +'<div class="progressbar-text" style="background-color:#6eff6e;width: '+ value +'; height: 20px; line-height: 20px;">'+ value +'</div>'
		         +'</div>'
		   +'</div>';
			return htmlstr;
		}
		
		//是否下发短信
		function sendmessageformatter(value,data,index) {
			if(value==1) {
				return "<span style='color:#00ff00'>是</span>";
			}else {
				return "<span style='color:#ff0000'>否</span>";
			}
		}
		
		//主叫号码
		function calleridformatter(value,data,index) {
			//return "<div id='calleriddesc_" + index + "' style='width:auto;' class='easyui-panel easyui-tooltip'>主叫号码</div>";
			return "<a href='#' onclick='javascript:showSelectedCallerId(\"" + data.CALLERID + "\",\"" + data.TASK_ID + "\",\"" + data.TASK_NAME + "\")'>主叫号码</a>";
		}
		
		function showSelectedCallerId(callerId,taskId,taskName) {
			$("#callerIdSearchBtnByOperId").css("display","none");
			$("#callerIdSearchBtnByTaskId").css("display","");
			$("#confirmCallerIdBtn").linkbutton('disable');
			currTaskId = taskId;
			callerId_findData_taskId();
			$('#callerIdDlg').dialog("setTitle","外呼任务：" + taskName + " 选择的主叫号码列表").dialog('open');
		}
		
	
		//格式化输出任务状态
		function taskstaterowformatter(value,data,index) {

			var taskState = data.TASK_STATE;
			var taskId = data.TASK_ID;
			var idInfo = "task" + taskId;

			if(taskState=='0') {
				return "<span style='color:#0000ff'>未激活</span>";      //蓝色
			}else if(taskState=='1') {
				return "<span style='color:#FF7F00'>待审核</span>";  //橙色
			}else if(taskState=='2') {
				var runningNotice = data.runningNotice;
				return "<span style='color:#009900'>已激活(" + runningNotice + ")</span>";    //绿色
			}else if(taskState=='3') {
				return "<a href='#' id='" + idInfo + "' style='text-decoration:none'  class='easyui-tooltip' border='0'><span style='color:#ff0000'>审核不通过</span></a>" ;
			}else if(taskState=='4') {
				return "<span style='color:#333333'>已暂停</span>";      //灰色
			}else if(taskState=='5') {
				return "<span style='color:#000000'>已停止</span>";      //黑色
			}
			
		}
		
		//格式化输出有效期
		function validitydaterowformatter(value,data,index) {
			return data.PLAN_START_TIME + " 至 " + data.PLAN_END_TIME;
		}
		
		//调度详情
		function scheduledetailformatter(value,data,index) {
			return "<a href='#' onclick='javascript:showScheduleDetail(\"" + data.schedule.SCHEDULE_ID + "\",\"" + data.schedule.SCHEDULE_NAME + "\",\"" + data.schedule.DATETYPE + "\",\"" + data.schedule.DATETYPE_DETAIL + "\",\"" + data.schedule.MAXTIMEITEM + "\",\"" + data.schedule.STARTHOUR1 + "\",\"" + data.schedule.STARTMINUTE1 + "\",\"" + data.schedule.ENDHOUR1 + "\",\"" + data.schedule.ENDMINUTE1 + "\",\"" + data.schedule.STARTHOUR2 + "\",\"" + data.schedule.STARTMINUTE2 + "\",\"" + data.schedule.ENDHOUR2 + "\",\"" + data.schedule.ENDMINUTE2 + "\",\"" + data.schedule.STARTHOUR3 + "\",\"" + data.schedule.STARTMINUTE3 + "\",\"" + data.schedule.ENDHOUR3 + "\",\"" + data.schedule.ENDMINUTE3 + "\",\"" + data.schedule.STARTHOUR4 + "\",\"" + data.schedule.STARTMINUTE4 + "\",\"" + data.schedule.ENDHOUR4 + "\",\"" + data.schedule.ENDMINUTE4 + "\",\"" + data.schedule.STARTHOUR5 + "\",\"" + data.schedule.STARTMINUTE5 + "\",\"" + data.schedule.ENDHOUR5 + "\",\"" + data.schedule.ENDMINUTE5 + "\")'>详情</a>";
		}
		
		function findData() {
			var selectRs = $("#orgCode").combotree('getValues');
			if(selectRs.length<1) {
				alert("查询时,组织不能为空!");
				return;
			}
			var orgCodes = selectRs.toString();
			var startTime = $("#startTime").datebox('getValue');
			var endTime = $("#endTime").datebox('getValue');
			var taskType = $("#taskType").combobox('getValue');
			var reminderType = $("#reminderType").combobox('getValue');
			var taskState = $("#taskState").combobox('getValue');
			var sendMessage = $("#sendMessage").combobox('getValue');

			$("#autoCallTaskDg").datagrid('load',{
				taskName:$("#taskName").val(),
				orgCode:orgCodes,
				startTime:startTime,
				endTime:endTime,
				taskType:taskType,
				reminderType:reminderType,
				taskState:taskState,
				sendMessage:sendMessage
			});
			
		}
		
		function showScheduleDetail(scheduleId,scheduleName,dateType,dateTypeDetail,maxTimeItem,startHour1,startMinute1,endHour1,endMinute1,startHour2,startMinute2,endHour2,endMinute2,startHour3,startMinute3,endHour3,endMinute3,startHour4,startMinute4,endHour4,endMinute4,startHour5,startMinute5,endHour5,endMinute5) {

			//$("#scheduleTitle").html("调度名称：" + scheduleName);
			var scheduleDetailHtml = "";

			scheduleDetailHtml += "<h2 style='color:#0099FF;'>调度名称：" + scheduleName + "</h2>";
			scheduleDetailHtml += "<HR style='FILTER:alpha(opacity=100,finishopacity=0,style=3);margin-left:3px;' width='95%' color=#cccccc SIZE=1>";
			scheduleDetailHtml += "<table border='1' cellspacing='0' cellpadding='0' bordercolor='#c4e1ff' style='margin-left:5px;width:95%;height:220px;'>";
			scheduleDetailHtml += "<tr><td style='width:50px;vertical-align: top;' rowspan='2'>";

			//循环打印周一至周日
			if(dateType==2) {   //如果日期类型为星期
				for(var i=1;i<=7;i++) {
					var weekDayX = weekArr[i];
					var isContain = false;

					var weekdays = dateTypeDetail.split(",");
					for(var j=0;j<weekdays.length;j++) {
						if(i==weekdays[j]) {
							isContain = true;
						}
					}

					if(isContain) {
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:50px;background:#11fa5e;margin-top:1px;'>" + weekDayX + "</a><br/>";	
					}else {
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:50px;background:#e7e4e4;margin-top:1px;'><span style='color:#c9c5c5'>" + weekDayX + "</span></a><br/>";
					}
					
				}
			}else {             //如果日期类型为每天
				for(var i=1;i<=7;i++) {
					var weekDayX = weekArr[i];
					scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:50px;background:#11fa5e;margin-top:1px;'>" + weekDayX + "</a><br/>";
				}
			}
			//循环打印周一至周日结束
			
			
			scheduleDetailHtml += "</td><td style='text-align: left;'>";

			//循环打印时间段
			var startTimeArr = new Array();       //创建一个数组，用于储存所有的开始时间
			var endTimeArr = new Array();         //创建一个数组，用于储存所有的结束时间
			var startTimeTextArr = new Array();  //创建一个数组，用于储存所有开始区间的显示
			var endTimeTextArr = new Array();  //创建一个数组，用于储存所有结束区间的显示
			for(var i=1;i<=maxTimeItem;i++) {
				if(i==1) {
					var sM1 = "00";
					var eM1 = "00";
					if(startMinute1=='30'){ sM1="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute1=='30'){eM1="50";};      //主要是为了linkButton 的长度考虑
					startTime1 = startHour1 + sM1; //  
					endTime1 = endHour1 + eM1;
					startTimeArr[(i-1)] = parseInt(startTime1);
					endTimeArr[(i-1)]= parseInt(endTime1);
					startTimeTextArr[(i-1)] = startHour1 + ":" + startMinute1;
					endTimeTextArr[(i-1)] = endHour1 + ":" + endMinute1;
				}else if(i==2) {
					var sM2 = "00";
					var eM2 = "00";
					if(startMinute2=='30'){ sM2="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute2=='30'){eM2="50";};      //主要是为了linkButton 的长度考虑
					startTime2 = startHour2 + sM2; //  
					endTime2 = endHour2 + eM2;
					startTimeArr[(i-1)] = parseInt(startTime2);
					endTimeArr[(i-1)]= parseInt(endTime2);
					startTimeTextArr[(i-1)] = startHour2 + ":" + startMinute2;
					endTimeTextArr[(i-1)] = endHour2 + ":" + endMinute2;
				}else if(i==3) {
					var sM3 = "00";
					var eM3 = "00";
					if(startMinute3=='30'){ sM3="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute3=='30'){eM3="50";};      //主要是为了linkButton 的长度考虑
					startTime3 = startHour3 + sM3; //  
					endTime3 = endHour3 + eM3;
					startTimeArr[(i-1)] = parseInt(startTime3);
					endTimeArr[(i-1)]= parseInt(endTime3);
					startTimeTextArr[(i-1)] = startHour3 + ":" + startMinute3;
					endTimeTextArr[(i-1)] = endHour3 + ":" + endMinute3;
				}else if(i==4) {
					var sM4 = "00";
					var eM4 = "00";
					if(startMinute4=='30'){ sM4="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute4=='30'){eM4="50";};      //主要是为了linkButton 的长度考虑
					startTime4 = startHour4 + sM4; //  
					endTime4 = endHour4 + eM4;
					startTimeArr[(i-1)] = parseInt(startTime4);
					endTimeArr[(i-1)]= parseInt(endTime4);
					startTimeTextArr[(i-1)] = startHour4 + ":" + startMinute4;
					endTimeTextArr[(i-1)] = endHour4 + ":" + endMinute4;
				}else if(i==5) {
					var sM5 = "00";
					var eM5 = "00";
					if(startMinute5=='30'){ sM5="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute5=='30'){eM5="50";};      //主要是为了linkButton 的长度考虑
					startTime5 = startHour5 + sM5; //  
					endTime5 = endHour5 + eM5;
					startTimeArr[(i-1)] = parseInt(startTime5);
					endTimeArr[(i-1)]= parseInt(endTime5);
					startTimeTextArr[(i-1)] = startHour5 + ":" + startMinute5;
					endTimeTextArr[(i-1)] = endHour5 + ":" + endMinute5;
				}
			}

			var buttonLength = "0";
			//定义
			for(var i=0;i<startTimeArr.length;i++) {
				if(i==0) {   //第一个时间区间
					if(startTimeArr[0]!=0) {   //如果第一个时间区间的开始时间不是零点
						//定义长度
						buttonLength = Math.ceil((startTimeArr[0]-0)*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#e7e4e4;margin-left:1px;'><span style='color:#c9c5c5'>" + "00:00至" + startTimeTextArr[i] + "</span></a>";
						buttonLength = Math.ceil((endTimeArr[0]-startTimeArr[0])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#11fa5e;margin-left:1px;'>" + startTimeTextArr[i] + "至" + endTimeTextArr[i] + "</a>";
					}else {
						buttonLength = Math.ceil((endTimeArr[0]-startTimeArr[0])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#11fa5e;margin-left:1px;'>" + startTimeTextArr[i] + "至" + endTimeTextArr[i] + "</a>";
					}
				}else {

					if(startTimeArr[i] != endTimeArr[(i-1)]) {
						buttonLength=Math.ceil((startTimeArr[i]-endTimeArr[(i-1)])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#e7e4e4;margin-left:1px;'><span style='color:#c9c5c5'>" + endTimeTextArr[(i-1)] + "至" + startTimeTextArr[i] + "</span></a>";
						buttonLength = Math.ceil((endTimeArr[i]-startTimeArr[i])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#11fa5e;margin-left:1px;'>" + startTimeTextArr[i] + "至" + endTimeTextArr[i] + "</a>";
					}else {
						buttonLength = Math.ceil((endTimeArr[i]-startTimeArr[i])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#11fa5e;margin-left:1px;'>" + startTimeTextArr[i] + "至" + endTimeTextArr[i] + "</a>";
					}
					
				}
			}

			if(endTimeArr[(startTimeArr.length-1)]!=0) {   //最后，再查看最后的时间段是否是24：00,否则还要有一段留白
				buttonLength = Math.ceil((2400-endTimeArr[(startTimeArr.length-1)])*820/2400) + "px";
				scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#e7e4e4;margin-left:1px;'><span style='color:#c9c5c5'>" + endTimeTextArr[((startTimeArr.length-1))] + "至24:00" + "</span></a>";
			}

			
			
			//循环打印时间段结束
			
			scheduleDetailHtml += "</td><tr style='height:20px;'><td style='text-align: left;'>";
			scheduleDetailHtml += "<img src='themes/icons/timer.png' style='width:840px' border='0'></td></tr></table>";

			$.parser.parse($("#scheduleDetailInfo").html(scheduleDetailHtml));
			
			$("#scheduleDetailDlg").dialog('setTitle','调度计划详情').dialog("open");
			
		}
		
		function showCallResult() {
			
			//取得当前已经选择的所有的任务的ID,以逗号连接
			var ids = getAutoCallTaskSelectedRows();
			if(ids=='' || ids==null) {
				alert("没有选择任何任务,请先选择任务，再执行查看外呼结果!");
				return;
			}
			
			$("#callResultDlg").dialog('setTitle',"显示多个外呼结果").dialog('open');
			reloadStatistics();
		}
		
		//取得选中的号码数据			
		function getAutoCallTaskSelectedRows() {
			
			var rows = $('#autoCallTaskDg').datagrid('getSelections');
			var ids = [];
			taskCount = 0;
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].TASK_ID);
				taskCount++;
			}
			return	ids.join(",");			
		}
		
		function showAllColumns() {
			$("#allColumns").css("display","none");
			$("#simpleColumns").css("display","inline");
			
			$("#autoCallTaskDg").datagrid("showColumn","SEND_MESSAGE");    			//是否下发信息
			$("#autoCallTaskDg").datagrid("showColumn","CALLERID_DESC");   			//主叫号码
			$("#autoCallTaskDg").datagrid("showColumn","scheduleDetail");  			//调度方案
			$("#autoCallTaskDg").datagrid("showColumn","RETRY_TIMES");     			//重试次数
			$("#autoCallTaskDg").datagrid("showColumn","RETRY_INTERVAL_DESC");  			//重试间隔
			$("#autoCallTaskDg").datagrid("showColumn","CREATE_USERCODE_DESC");     //创建人
			$("#autoCallTaskDg").datagrid("showColumn","CREATE_TIME");              //创建时间
		}
		
		function showSimpleColumns() {
			$("#allColumns").css("display","inline");
			$("#simpleColumns").css("display","none");
			
			$("#autoCallTaskDg").datagrid("hideColumn","SEND_MESSAGE");
			$("#autoCallTaskDg").datagrid("hideColumn","CALLERID_DESC");
			$("#autoCallTaskDg").datagrid("hideColumn","scheduleDetail");
			$("#autoCallTaskDg").datagrid("hideColumn","RETRY_TIMES");
			$("#autoCallTaskDg").datagrid("hideColumn","RETRY_INTERVAL_DESC");
			$("#autoCallTaskDg").datagrid("hideColumn","CREATE_USERCODE_DESC");
			$("#autoCallTaskDg").datagrid("hideColumn","CREATE_TIME");
		}
		
	</script>
	
</head>
<body>
<%@ include file="/base_loading.jsp" %>
<!-- 页面内容区 -->
<div data-options="fit:true" class="easyui-layout">

	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:70px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td> 
					任务名称：<input id="taskName" type="text" class="easyui-textbox" style="width:130px;"/>
					<span style="padding-left:25px;">
						任务类型：<select class="easyui-combobox" id="taskType" name="taskType" style="width:130px;" data-options="panelHeight:'auto'"></select>
					</span>
					<span style="padding-left:20px;">
						催缴类型：<select class="easyui-combobox" id="reminderType" name="reminderType" style="width:130px;" data-options="panelHeight:'auto'"></select>
					</span>
					<span style="padding-left:20px;">
						选择组织：<select class="easyui-combotree" id="orgCode" name="orgCode" style="width:180px;" data-options="panelHeight:'auto',multiple:true"></select>
					</span>
				</td>
				
			</tr>
			<tr style="vertial-align:top;">
				<td>
					创建时间：<input id="startTime" name="startTime" class="easyui-datebox" style="width:130px;"/><span style="padding-left:38px;padding-right:36px;">至</span> <input id="endTime" name="endTime" class="easyui-datebox" style="width:130px;" />
					<span style="padding-left:20px;">
						任务状态：<select class="easyui-combobox" id="taskState" name="taskState" style="width:130px;" data-options="panelHeight:'auto'"></select>
					</span>
					<span style="padding-left:20px;">
						下发短信：<select class="easyui-combobox" id="sendMessage" name="sendMessage" style="width:180px;" data-options="panelHeight:'auto'">
									<option value="empty">请选择</option>
									<option value="1">是</option>
									<option value="0">否</option>
								</select>
					</span>
					<span style="padding-left:40px;">
						<a href="javascript:findData()" class="easyui-linkbutton" style="width:100px;" data-options="iconCls:'icon-search'">查询</a>
					</span>
				</td>
			</tr>
		</table>
	</div>
	
	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="autoCallTaskDg" class="easyui-datagrid">
			<thead>
				<tr style="height:12px;">
					<th data-options="field:'ck',checkbox:true"></th>		
					<th data-options="field:'TASK_NAME',width:250,align:'center'">任务名称</th>
					<th data-options="field:'FINISH_RATE',width:100,align:'center',formatter:finishrateformatter">任务完成情况</th>
					<th data-options="field:'TASK_TYPE_DESC',width:150,align:'center'">任务类型</th>
					<th data-options="field:'SEND_MESSAGE',width:80,align:'center',formatter:sendmessageformatter">下发短信</th>
					<th data-options="field:'CALLERID_DESC',width:150,align:'center',formatter:calleridformatter">主叫号码</th>
					<th data-options="field:'taskStateField',width:120,align:'center',formatter:taskstaterowformatter">状态</th>
					<th data-options="field:'validityDate',width:350,align:'center',formatter:validitydaterowformatter">有效期</th>
					<th data-options="field:'scheduleDetail',width:50,align:'center',formatter:scheduledetailformatter">调度</th>
					<th data-options="field:'RETRY_TIMES',width:80,align:'center'">呼叫总次数</th>
					<th data-options="field:'RETRY_INTERVAL_DESC',width:100,align:'center'">重试间隔</th>
					
					<th data-options="field:'CREATE_USERCODE_DESC',width:150,align:'center'">创建人</th>
					<!-- th data-options="field:'ORG_CODE_DESC',width:150,align:'center'">部门(组织)名字</th -->
					<th data-options="field:'CREATE_TIME',width:180,align:'center'">创建时间</th>
				</tr>
				
			</thead>
		</table>
	</div>

</div>

<div id="searchtool" style="padding:5px;">
	<a href="#" id="easyui-add" onclick="showCallResult()" class="easyui-linkbutton" iconCls='icon-search' plain="true" style="margin-left:50px;">查看外呼结果</a>
	<a id="allColumns" href="#" id="easyui-add" onclick="showAllColumns()" class="easyui-linkbutton" iconCls='icon-add' plain="true" style="margin-left:150px;display:inline">全部显示</a>
		<a id="simpleColumns" href="#" id="easyui-add" onclick="showSimpleColumns()" class="easyui-linkbutton" iconCls='icon-remove' plain="true" style="margin-left:150px;display: none;">精简显示</a>
</div>

<!-- 调度详情弹窗 -->
<div id="scheduleDetailDlg" class="easyui-dialog" style="width:1000px;height:400px;padding:5px;" modal="true" closed="true">
	<div style="text-align: center" id="scheduleDetailInfo">
				
	</div>
</div>

<!-- 外呼结果弹窗 -->
<div id="callResultDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	<%@ include file="/autocall/autocalltaskreport/_callresult.jsp" %>
</div>

<!-- 主叫号码选择窗 -->
<div id="callerIdDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/system/callerid/_selectlist.jsp"%>
</div>

<form id="exportForm"></form>

</body>
</html>