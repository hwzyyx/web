<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>外呼结果查询</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/color.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
<!--    <script type="text/javascript" src="base-loading.js"></script>-->
    <script type="text/javascript" src="echarts/echarts.min.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
	
		var currTaskId = null;
		var currTaskName = null;
		var currCallerId = null;
		var currChart = null;
		var currRespondChart = null;
		var questionTitleDetail=[];
		var currQuestionId = null;

		var orgCombotreeData = eval('${orgCombotreeData}');
		var taskTypeComboboxData = eval('${taskTypeComboboxData}');
		var taskStateComboboxData = eval('${taskStateComboboxData}');
		
	
		$(function(){
			
			$('#endTime').datebox('setValue',getCurrDate());

			$("#taskType").combobox({
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',taskTypeComboboxData).combobox('setValue','empty');

			$("#taskState").combobox({
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',taskStateComboboxData).combobox('setValue','empty');

			
			$("#orgCode").combotree('loadData',orgCombotreeData).combotree({

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
					//alert("orgCodes:" + orgCodes + ",startTime:" + startTime + ",endTime:" + endTime + "taskType:" + taskType + ",taskState:" + taskState);
					$("#autoCallTaskDg").datagrid({
						pageSize:15,
						pagination:true,
						fit:true,
						toolbar:'#searchtool',
						singleSelect:true,
						rownumbers:true,
						rowrap:true,
						striped:true,
						checkbox:true,
						pageList:[10,15,20],
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
	
							if(taskState==0) {                 //状态为0,即是未激活可申请激活
								disabledAllStateBtn();
								$("#applyActiveBtn").linkbutton("enable");
								$("#historyBtn").linkbutton("enable");
							}else if(taskState==1) {           //状态为1，即是审核中，可以取消激活
								disabledAllStateBtn();
								$("#cancelApplyActiveBtn").linkbutton("enable");
								$("#historyBtn").linkbutton("enable");
							}else if(taskState==2) {           //状态为2，即是审核通过，可以暂停、可以停止
								disabledAllStateBtn();
								$("#pauseBtn").linkbutton("enable");
								$("#stopBtn").linkbutton("enable");
								$("#historyBtn").linkbutton("enable");
							}else if(taskState==3) {           //状态为3，即是审核不通过，可以申请激活
								disabledAllStateBtn();
								$("#applyActiveBtn").linkbutton("enable");
								$("#historyBtn").linkbutton("enable");
							}else if(taskState==4) {           //状态为4，即是暂停中，可以重新开始、可以停止
								disabledAllStateBtn();
								$("#cancelPauseBtn").linkbutton("enable");
								$("#stopBtn").linkbutton("enable");
								$("#historyBtn").linkbutton("enable");
							}else if(taskState==5) {           //状态为5，即是任务停止，可以重新申请激活
								disabledAllStateBtn();
								$("#applyActiveBtn").linkbutton("enable");
								$("#historyBtn").linkbutton("enable");
							}
							
						},
						onLoadSuccess:function(data) {         //加载成功后，先禁用所有的按钮
							disabledAllStateBtn();
	
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
							}
							
						}
					});
				
				}
					
			});
			
		});
		
		//格式化输出任务类型
		function tasktyperowformatter(value,data,index) {
			var taskType = data.TASK_TYPE;
			var reminderType = data.REMINDER_TYPE;

			var reminderTypeLable = null;
			
			if(taskType=='1') {
				return "普通外呼";
			}else if(taskType=='2') {
				return "调查问卷";
			}else if(taskType=='3') {
				if(reminderType=='1') {
					reminderTypeLable = "(电话费)";
				}else if(reminderType=='2') {
					reminderTypeLable = "(电费)";
				}else if(reminderType=='3') {
					reminderTypeLable = "(水费)";
				}else if(reminderType=='4') {
					reminderTypeLable = "(燃气费)";
				}else if(reminderType=='5') {
					reminderTypeLable = "(物业费)";
				}else if(reminderType=='6') {
					reminderTypeLable = "(车辆违章)";
				}else if(reminderType=='7') {
					reminderTypeLable = "(社保催缴)";
				}
				
				return "催缴费" + reminderTypeLable;
			}
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

		function findData(){

			var selectRs = $("#orgCode").combotree('getValues');
			var orgCodes = selectRs.toString();
			var startTime = $("#startTime").datebox('getValue');
			var endTime = $("#endTime").datebox('getValue');
			var taskType = $("#taskType").combobox('getValue');
			var taskState = $("#taskState").combobox('getValue');

			$("#autoCallTaskDg").datagrid('load',{
				taskName:$("#taskName").val(),
				orgCode:orgCodes,
				startTime:startTime,
				endTime:endTime,
				taskType:taskType,
				taskState:taskState
			});
			
		}

		function findData4Telephone() {

			var telephone = $("#telephone").numberbox('getValue');
			var state = $("#state").combobox('getValue');

			$("#autoCallTaskTelephoneDg").datagrid('load',{
				telephone:telephone,
				state:state,
				taskId:currTaskId
			});
			
		}
		
		function rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:showAutoCallTaskResult(\"" + data.TASK_ID + "\",\"" + data.TASK_NAME + "\",\"" + data.TASK_TYPE + "\",\"" + data.CALLERID_DESC + "\")'>外呼结果</a>";
		}

		//刷新外呼结果
		function refreshAutoCallTaskResult() {

			//分两步
			//初始化图表
			initChart();

			//刷新数据
			findData4Telephone();
			
		}
		
		//显示外呼结果		
		function showAutoCallTaskResult(taskId,taskName,taskType,callerId) {

			currCallerId = callerId;
			currTaskId = taskId;
			currTaskName = taskName;

			var taskTypeDesc = null;
			if(taskType=='1') {
				taskTypeDesc = '普通外呼';
				$("#respondBtn").css("display","none");
			}else if(taskType=='2') {
				taskTypeDesc = '调查问卷';
				$("#respondBtn").css("display","");
			}else if(taskType=='3') {
				taskTypeDesc = '催缴外呼';
				$("#respondBtn").css("display","none");
			}
			
			var taskInfo = "<span style='font-size: 14px;font-weight: bolder;'>任务名称:" + taskName + ",任务类型：" + taskTypeDesc + "</span>";
			$("#taskInfo").html(taskInfo);
			
			var telephone = $("#telephone").numberbox('getValue');
			var state = $("#state").combobox('getValue');

			$("#autoCallTaskTelephoneDg").datagrid({
				pageSize:15,
				pagination:true,
				fit:true,
				singleSelect:true,
				rownumbers:true,
				toolbar:'#autoCallTaskTelephoneDgTool',
				rowrap:true,
				striped:true,
				checkbox:true,
				pageList:[10,15,20],
				url:'autoCallTaskTelephone/datagrid?taskId=' + currTaskId,
				queryParams:{
					telephone:telephone,
					state:state
				}
			});

			//初始化图表
			initChart();
			
			$("#autoCallTaskResultDlg").dialog("setTitle","外呼结果").dialog("open");
			
		}

		function initChart() {

			currChart = echarts.init(document.getElementById('chartDiv'));

			var option = {
			    title : {
			        text: currTaskName,
			        subtext: '外呼结果饼图',
			        x:'center',
				    top:5
			    },
			    toolbox:{
				    feature:{
				    	saveAsImage:{
			    			type:'png',
			    			name:currTaskName + "-外呼饼图",
			    			show:true,
			    			title:'保存',
			    			pixelRatio:3,
			    			iconStyle:{
	    						normal:{
									textPosition:'top',
									textAlign:'left'
								}
								
	    					}
			    		}
				    }
				},
			    tooltip : {
			        trigger: 'item',
			        formatter: "{a} <br/>{b} : {c} ({d}%)"
			    },
			    color:['#00ccff','#61a0a8','#49a849','#e9bfb0','#c23531'],
			    legend: {
			        orient: 'vertical',
			        left: 'left',
			      	top:60,
			        data: [],
			        formatter:function(name) {
			        
						var optionData = currChart.getOption().series[0].data;

						var noCallCount = optionData[0].value;       //未处理数量
						var loadCount = optionData[1].value;         //已载入数量
						var successCount = optionData[2].value;      //已成功数量
						var retryCount = optionData[3].value;		 //待重呼数量 
						var failureCount = optionData[4].value;		 //已失败数量

						//总数量
						var totalCount = noCallCount + loadCount + successCount + retryCount + failureCount;
						
						//设置显示按钮的数字及成功率、失败率。
						$("#totalCountBtn").linkbutton({text:totalCount==0?'0':totalCount});    				 		//总数
						$("#noCallCountBtn").linkbutton({text:noCallCount==0?'0':noCallCount});    		//未处理数量
						$("#loadCountBtn").linkbutton({text:loadCount==0?'0':loadCount});      			//已载入数量
						$("#successCountBtn").linkbutton({text:successCount==0?'0':successCount});   	//成功数量
						$("#retryCountBtn").linkbutton({text:retryCount==0?'0':retryCount});     		//待重呼数量
						$("#failureCountBtn").linkbutton({text:failureCount==0?'0':failureCount});   	//失败数量

						if(totalCount > 0) {
							$("#successRateBtn").linkbutton({text:(successCount/totalCount*100).toFixed(2) + "%"});    //成功率
							$("#failureRateBtn").linkbutton({text:(failureCount/totalCount*100).toFixed(2) + "%"});   //失败率
						}else {
							$("#successRateBtn").linkbutton({text:"0.00%"});    //成功率
							$("#failureRateBtn").linkbutton({text:"0.00%"});    //失败率
						}

						
						for(var i=0;i<optionData.length;i++) {
				        	if(name==optionData[i].name) {
					        	return name + '   ' + optionData[i].value + ' ( ' + (optionData[i].value/totalCount * 100).toFixed(2) + "% ) ";
				        	}
			        	}
						
					}
			    },
			    series : [
			        {
			            name: '外呼结果',
			            type: 'pie',
			            radius : '30%',
			            center: ['50%', '70%'],
			            data:[],
			            itemStyle: {
			                emphasis: {
			                    shadowBlur: 10,
			                    shadowOffsetX: 0,
			                    shadowColor: 'rgba(0, 0, 0, 0.5)'
			                }
			            }
			        }
			    ]
			};
				
			currChart.setOption(option);

			
			//定义两个数组对象，用于存储 legend及series的数据
			var legendData=[];
			var seriesData=[];
			
			$.ajax({

				url:'autoCallTaskResult/getPieData?taskId=' + currTaskId,
				method:'post',
				dataType:'json',
				success:function(rs) {
					for(var i=0;i<rs.length;i++) {
						legendData[i]=rs[i].name;  //将数据推给定义的数组对象

						var map = {};
						map.name = rs[i].name;
						map.value = rs[i].value;
						seriesData[i] = map;
					}

					currChart.setOption({
						legend:{
							data:legendData
						},
						series:[{
							data:seriesData
						}]
					});
					
				}
				
			});
			
			
		}

		function calleridformatter(value,data,index) {
			return currCallerId;
		}

		function stateformatter(value,data,index) {

			var state = data.STATE;
			if(state=='0') {
				return "<span style='color:#00ccff'>未处理</span>";
			}else if(state=='1') {
				return "<span style='color:#61a0a8'>已载入</span>";
			}else if(state=='2') {
				return "<span style='color:#49a849'>已成功</span>";
			}else if(state=='3') {
				return "<span style='color:#e9bfb0'>待重试</span>";
			}else if(state=='4') {
				return "<span style='color:#c23531'>已失败</span>";
			}
			
		}

		function failureformatter(value,data,index) {

			var lastCallResult = data.LAST_CALL_RESULT;
			var state = data.STATE;

			//只有状态为3或4时,才有失败原因
			if(state!='3' && state!='4') {
				return "";
			}

			if(lastCallResult=='SUCCESS') {
				return "";
			}else if(lastCallResult=='NOANSWER') {
				return "未接听";
			}else if(lastCallResult=='BUSY') {
				return "用户忙";
			}else if(lastCallResult=='FAILURE') {
				return "外呼失败";
			}else if(lastCallResult=='DISCONNECTION') {
				return "PBX连接故障";
			}else if(lastCallResult=='DISCONNECTION') {
				return "PBX连接故障";
			}else {
				
			}
			
		}

		function nextcallouttimeformatter(value,data,index) {

			var nextCallOutTime = data.NEXT_CALLOUT_TIME;
			var state = data.STATE;

			if(state=='3') {    //即,只有当状态为3（待重试）时，才显示下次外呼时间
				return nextCallOutTime;
			}else {
				return "";
			}
			
			
		}

		//结果导出
		function callOutResultExport() {

			var state = $("#state").combobox('getValue');
			var telephone = $("#telephone").numberbox('getValue');

			$("#exportForm").form('submit',{

				url:"autoCallTaskResult/exportExcel",
				onSubmit:function(param) {
					param.taskId = currTaskId;
					param.state = state;
					param.telephone = telephone;
				},
				success:function(data) {
					
				}	
				
			});
			
		}

		function showAutoCallTaskRespond() {

			hideAllQuestion();     //先隐藏所有问题
			questionTitleDetail = [];     //先清空问题详情

			//第一步，先列出问题列表
			$.messager.progress({
						msg:'系统正在处理，请稍候...',
						interval:3000
			});
			$.ajax({

				url:'autoCallTaskResult/getQuestionList?taskId=' + currTaskId,
				method:'post',
				dataType:'json',
				success:function(rs) {
					$.messager.progress('close');

					for(var i=0;i<rs.length;i++) {

					 	var title = rs[i].title;
					 	var titleDetail = rs[i].titleDetail;
					 	var onClickTarget = rs[i].onClickTarget;
					 	var questionId = rs[i].questionId;

					 	questionTitleDetail[questionId] = titleDetail;

					 	
						
						$("#" + title).css("display","");
						$("#" + title).attr('onclick',onClickTarget);

						if(i == 0 ) {
							showRespondChart(questionId);
						}
						
					}
					
				}
				
			});
			
			$("#autoCallTaskRespondDlg").dialog('setTitle','调查问卷回复结果统计').dialog('open');
			
		}


		function showRespondChart(questionId) {
			$("#questionTitleDetailBtn").linkbutton({text:questionTitleDetail[questionId]});

			initRespondChart(questionId);
			
		}

		//初始化客户回复饼图表
		function initRespondChart(questionId) {

			currRespondChart = echarts.init(document.getElementById('respondChartDiv'));

			var resOption = {
					title : {
				        text: questionTitleDetail[questionId],
				        //subtext: '客户回复统计',
				        x:'left',
					    top:5
				    },
				    tooltip : {
				        trigger: 'item',
				        formatter: "{a} <br/>{b} : {c} ({d}%)"
				    },
				    legend: {
				        orient : 'vertical',
				        x : 'left',
				        y : 'top',
				        top:40,
				        data:[],
				        formatter:function(name) {

				    		var optionData = currRespondChart.getOption().series[0].data;
				    	
							//先计算总的数量
				    		var totalCount = 0;
				    		for(var  i=0;i<optionData.length;i++) {
					    		totalCount += optionData[i].value;
				    		}

				    		
					    	//再返回固定格式文字提示
				    		for(var  i=0;i<optionData.length;i++) {
					    		if(name==optionData[i].name) {
					    			return name + '   ' + optionData[i].value + ' ( ' + (optionData[i].value/totalCount * 100).toFixed(2) + "% ) ";
					    		}
				    		}
			        	
				    	}
				    },
				    toolbox: {
				        show : true,
				        feature : {
				            mark : {show: true},
				            dataView : {show: true, readOnly: false},
				            magicType : {
				                show: true, 
				                type: ['pie', 'funnel'],
				                option: {
				                    funnel: {
				                        x: '25%',
				                        width: '50%',
				                        funnelAlign: 'left',
				                        max: 1548
				                    }
				                }
				            },
				            restore : {show: true},
				            saveAsImage : {show: true}
				        }
				    },
				    calculable : true,
				    series : [
				        {
				            name:'客户回复统计',
				            type:'pie',
				            radius : '40%',
				            center: ['55%', '55%'],
				            data:[]
				        }
				    ]
				};

			currRespondChart.setOption(resOption);

			//定义两个数组对象，用于存储 legend及series的数据
			var respondLegendData=[];
			var respondSeriesData=[];
			$.messager.progress({
				msg:'系统正在处理，请稍候...',
				interval:3000
			});
			$.ajax({

				url:'autoCallTaskResult/getRespondPieData?taskId=' + currTaskId + '&questionId=' + questionId,
				method:'post',
				dataType:'json',
				success:function(rs) {

					$.messager.progress('close');

					currQuestionId = questionId;
					
					var questionItemComboboxData = "[";

					questionItemComboboxData += "{'id':'','text':'全部'},";
					
					for(var i=0;i<rs.length;i++) {
						respondLegendData[i]=rs[i].name;  //将数据推给定义的数组对象
						var map = {};
						map.name = rs[i].name;
						map.value = rs[i].value;
						respondSeriesData[i] = map;
						
						questionItemComboboxData += "{'id':'" + rs[i].itemCode + "','text':'" + rs[i].name + "'},";
					}

					questionItemComboboxData += "]";

					currRespondChart.setOption({
						legend:{
							data:respondLegendData
						},
						series:[{
							data:respondSeriesData
						}]
					});

					//将导出的 combobox 加载数据
					$("#questionItemCombobox").combobox({
						valueField:'id',
		    			textField:'text',
		    			panelHeight:'auto'
					}).combobox('loadData',eval(questionItemComboboxData));
					
				}
				
			});
			
		}

		//导出客户回复结果
		function exportRespondResult() {

			//取出当前问题选项的值
			var questionItemValue = $("#questionItemCombobox").combobox('getValue');

			$("#exportForm").form('submit',{

				url:'autoCallTaskResult/exportRespondExcel',
				onSubmit:function(param) {
					param.taskId = currTaskId;
					param.questionId = currQuestionId;
					param.questionItemValue = questionItemValue;
				},
				success:function(data) {
					
				}
				
			});
			
		}
		

		//隐藏所有问题列表: 隐藏所有问题;
		function hideAllQuestion() {
				$("#question1").css("display","none");
				$("#question2").css("display","none");
				$("#question3").css("display","none");
				$("#question4").css("display","none");
				$("#question5").css("display","none");
				$("#question6").css("display","none");
				$("#question7").css("display","none");
				$("#question8").css("display","none");
		}

		
			
	</script>
</head>
<body>

	<%@ include file="/base_loading.jsp" %>

	<!-- 定义一个layout -->
	<div data-options="fit:true" class="easyui-layout">
		
		<!-- 顶部查询区 -->
		<div data-options="region:'north',split:true,border:true" style="height:70px;padding-top:5px;padding-left:5px;">
			<table>
				<tr style="vertical-align: top;">
					<td> 任务名称：<input id="taskName" type="text" class="easyui-textbox" /></td>
					<td style="padding-left:30px;"> 任务类型：
						<select class="easyui-combobox" style="width: 145px;" id="taskType" data-options="panelHeight:'auto'"></select>
					</td>
					
					<td style="padding-left:30px;"> 任务状态：
						<select class="easyui-combobox" style="width: 155px;" id="taskState" data-options="panelHeight:'auto'"></select>
					</td>
					<td>
						<div style="padding-left:30px;">
							选择组织：<select id="orgCode" class="easyui-combotree" multiple style="width:200px;"></select>
						</div>
					</td>
				
				</tr>
				<tr>
					<td colspan="2">
						<div>
							创建时间：<input id="startTime" width="40" name="startTime" class="easyui-datebox" /><span style="padding-left:40px;padding-right:40px;">至</span> <input id="endTime" width="40" name="endTime" class="easyui-datebox" />
						</div>
					</td>
					<td colspan="2">
						<div style="padding-left:92px;">
							<a href="javascript:findData()" class="easyui-linkbutton" style="width:155px;" data-options="iconCls:'icon-search'">查询</a>
						</div>
					</td>
				</tr>
			</table>
		</div>		
		
		<!-- 数据显示区 -->
		<div data-options="region:'center',split:true,border:false">
			<table id="autoCallTaskDg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'ck',checkbox:true"></th>		
						<th data-options="field:'TASK_NAME',width:200,align:'center'">任务名称</th>
						<th data-options="field:'taskTypeField',width:100,align:'center',formatter:tasktyperowformatter">任务类型</th>
						<th data-options="field:'CALLERID_DESC',width:100,align:'center'">主叫号码</th>
						<th data-options="field:'taskStateField',width:100,align:'center',formatter:taskstaterowformatter">状态</th>
						<th data-options="field:'validityDate',width:180,align:'center',formatter:validitydaterowformatter">有效期</th>
						<th data-options="field:'scheduleDetail',width:40,align:'center',formatter:scheduledetailformatter">调度</th>
						<th data-options="field:'RETRY_TIMES',width:80,align:'center'">重试次数</th>
						<th data-options="field:'RETRY_INTERVAL',width:100,align:'center'">重试间隔(分钟)</th>
						
						<th data-options="field:'CREATE_USERCODE_DESC',width:100,align:'center'">创建人</th>
						<th data-options="field:'ORG_CODE_DESC',width:150,align:'center'">部门(组织)名字</th>
						<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
						<th data-options="field:'id',width:100,align:'center',formatter:rowformatter">操作</th>
					</tr>
				</thead>
			</table>	
		</div>
		
	</div>
	
	
	<!-- 任务外呼结果弹窗 -->
	<div id="autoCallTaskResultDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
		
		<!-- 包含外呼结果显示页面 -->
		<%@ include file="/autocall/autocalltask/_resultform.jsp" %>
	</div>
	
	<!-- 调度详情弹窗 -->
	<div id="scheduleDetailDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
		<div style="text-align: center" id="scheduleDetailInfo">
					
		</div>
	</div>
	
	<!-- 调查问卷回复统计弹窗 -->	
	<div id="autoCallTaskRespondDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	
		<!-- 包含调查问卷外呼回复统计显示 -->
		<%@ include file="/autocall/autocalltask/_respondform.jsp" %>
	</div>

</body>
</html>

