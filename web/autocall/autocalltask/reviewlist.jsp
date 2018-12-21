<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>自动任务审核</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/color.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/pink.flag/css/jplayer.pink.flag.min.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.min.js"></script>
    <script type="text/javascript" src="jplayer/dist/add-on/jplayer.playlist.min.js""></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		var currTaskId = null;
		var currScheduleId = null;
		var isShowMore = 1;

		var orgCombotreeData = eval('${orgCombotreeData}');
		var taskTypeComboboxDataFor0 = eval('${taskTypeComboboxDataFor0}');
		var taskTypeComboboxDataFor1 = eval('${taskTypeComboboxDataFor1}');
		
		var reminderTypeComboboxDataFor0 = eval('${reminderTypeComboboxDataFor0}');
		var reminderTypeComboboxDataFor1 = eval('${reminderTypeComboboxDataFor1}');
		
		var taskStateComboboxDataFor0 = eval('${taskStateComboboxDataFor0}');
		var taskStateComboboxDataFor1 = eval('${taskStateComboboxDataFor1}');

		var callerIdComboboxDataFor0 = eval('${callerIdComboboxDataFor0}');

		var reviewResultRadioData = '${reviewResultRadioData}';
		
		$(function(){
			$('#endTime').datebox('setValue',getCurrDate());

			//任务类型加载
			$("#taskType").combobox({
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',taskTypeComboboxDataFor1).combobox('setValue','empty');

			//任务状态
			$("#taskState").combobox({
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',taskStateComboboxDataFor0).combobox('setValue','1').combobox('disable');
			

			//搜索栏的组织加载
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
						onLoadSuccess:function(data) {         
							for(var i=0;i<data.rows.length;i++) {
								//主叫号码列表显示
								var callerIdDescRs = data.rows[i].CALLERID_DESC;
								callerIdDescRs = callerIdDescRs.replace(/\|/gm,'<br>');
								$("#calleriddesc_" + i).tooltip({
									position:'top',
									content:callerIdDescRs
								});
							}
						}
					});
				
				}
			});

			showSimpleColumns();     //任务列表，显示简单的列

			$("#TASK_TYPE").combobox({
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto',
    			onChange:function(newValue,oldValue) {

    				$("#common_voice_tr").css('display','none');
    				$("#questionnaire_tr").css('display','none');
    				$("#reminderType_tr").css('display','none');

    				if(newValue=='1') {    //如果选择的任务类型为1，即普通外呼任务
    					$("#common_voice_tr").css('display','');
    				}else if(newValue=="2") {    //如果选择的任务类型为2,即问卷调查任务
    					$("#questionnaire_tr").css('display','');
    				}else if(newValue=="3") {
    					$("#reminderType_tr").css('display','');
    				} 
    				
				}
			}).combobox('loadData',taskTypeComboboxDataFor0).combobox('setValue','1');

			$("#REMINDER_TYPE").combobox({
				valueField:'id',
				textField:'text',
				panelHeight:'auto'
			}).combobox('loadData',reminderTypeComboboxDataFor0).combobox('setValue','1');

			$("#CALLERID").combobox({
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',callerIdComboboxDataFor0).combobox('setValue','1');
			
			//外呼任务号码列表
			$("#autoCallTaskTelephoneDg").datagrid({
				pageSize:30,
				pagination:true,      
				fit:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,30,50],
				checkbox:true,
				url:'autoCallTaskTelephone/datagrid',
				queryParams:{
					taskId:currTaskId,
			    	customerTel:$('#customerTel').textbox('getValue'),
	    			customerName:$('#customerName').textbox('getValue')
				}
			});

			$("#reviewDlg").dialog({  //当任务表单弹窗关闭时执行的动作
				onClose:function(){

					$("#jplayDiv").html("");
				
					currTaskId = null;
					$("#autoCallTaskForm").form('clear');

					if(isShowMore==0) { showMore();}   //如果隐藏时,打开显示更多
					
					//清除号码列表的搜索框信息
					$("#customerTel").textbox('setValue','');
					$("#customerName").textbox('setValue','');

					//同时，要将号码列表数据清空
			    	$("#autoCallTaskTelephoneDg").datagrid('loadData',{total:0,rows:[]});    //号码列表清空

			    	$("#autoCallTaskTabs").tabs('select',"外呼任务管理");   //默认选中黑名单管理
				}
			});


			//审核结果radio
			$("#reviewResult").html(reviewResultRadioData);						
		});


		function findData() {
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

		function findDataForTelephone() {
    		$("#autoCallTaskTelephoneDg").datagrid('load',{
        		taskId:currTaskId,
        		customerTel:$('#customerTel').textbox('getValue'),
    			customerName:$('#customerName').textbox('getValue')
        	});
    	}

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

			if(taskState=='0') {
				return "<span style='color:#0000ff'>未激活</span>";      //蓝色
			}else if(taskState=='1') {
				return "<span style='color:#FF7F00'>待审核</span>";  //橙色
			}else if(taskState=='2') {
				return "<span style='color:#00ff00'>审核通过</span>";    //绿色
			}else if(taskState=='3') {
				return "<span style='color:#ff0000'>审核不通过</span>";  //红色
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
			return "<a href='#' onclick='javascript:scheduleDetail(\"" + data.schedule.SCHEDULE_ID + "\",\"" + data.schedule.SCHEDULE_NAME + "\",\"" + data.schedule.DATETYPE + "\",\"" + data.schedule.DATETYPE_DETAIL + "\",\"" + data.schedule.MAXTIMEITEM + "\",\"" + data.schedule.STARTHOUR1 + "\",\"" + data.schedule.STARTMINUTE1 + "\",\"" + data.schedule.ENDHOUR1 + "\",\"" + data.schedule.ENDMINUTE1 + "\",\"" + data.schedule.STARTHOUR2 + "\",\"" + data.schedule.STARTMINUTE2 + "\",\"" + data.schedule.ENDHOUR2 + "\",\"" + data.schedule.ENDMINUTE2 + "\",\"" + data.schedule.STARTHOUR3 + "\",\"" + data.schedule.STARTMINUTE3 + "\",\"" + data.schedule.ENDHOUR3 + "\",\"" + data.schedule.ENDMINUTE3 + "\",\"" + data.schedule.STARTHOUR4 + "\",\"" + data.schedule.STARTMINUTE4 + "\",\"" + data.schedule.ENDHOUR4 + "\",\"" + data.schedule.ENDMINUTE4 + "\",\"" + data.schedule.STARTHOUR5 + "\",\"" + data.schedule.STARTMINUTE5 + "\",\"" + data.schedule.ENDHOUR5 + "\",\"" + data.schedule.ENDMINUTE5 + "\")'>详情</a>";
		}

		function rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:autoCallTaskReview(\"" + data.TASK_ID + "\",\"" + data.TASK_NAME + "\",\"" + data.CALLERID + "\",\"" + data.PLAN_START_TIME + "\",\"" + data.PLAN_END_TIME + "\",\"" + data.SCHEDULE_ID + "\",\"" + data.SCHEDULE_NAME + "\",\"" + data.TASK_TYPE + "\",\"" + data.COMMON_VOICE_ID + "\",\"" + data.COMMON_VOICE_DESC + "\",\"" + data.QUESTIONNAIRE_ID + "\",\"" + data.QUESTIONNAIRE_DESC + "\",\"" + data.REMINDER_TYPE + "\",\"" + data.START_VOICE_ID + "\",\"" + data.START_VOICE_DESC + "\",\"" + data.END_VOICE_ID + "\",\"" + data.END_VOICE_DESC + "\",\"" + data.BLACKLIST_ID + "\",\"" + data.BLACKLIST_NAME + "\",\"" + data.RETRY_TIMES + "\",\"" + data.RETRY_INTERVAL + "\",\"" + data.INTERVAL_TYPE + "\",\"" + data.PRIORITY + "\")'><img src='themes/icons/ok.png' border='0'>审核</a>";
		}

		//任务审核
		function autoCallTaskReview(taskId,taskName,callerId,planStartTime,planEndTime,scheduleId,scheduleName,taskType,commonVoiceId,commonVoiceDesc,questionnaireId,questionnaireDesc,reminderType,startVoiceId,startVoiceDesc,endVoiceId,endVoiceDesc,blackListId,blackListName,retryTimes,retryInterval,intervalType,priority) {

			currTaskId = taskId;

			//设置任务类型
			$("#TASK_TYPE").combobox('setValue',taskType);

			$("#autoCallTaskForm").form('load',{
				'autoCallTask.TASK_ID':taskId,
				'autoCallTask.TASK_NAME':taskName,
				'autoCallTask.CALLERID':callerId,
				'autoCallTask.PLAN_START_TIME':planStartTime,
				'autoCallTask.PLAN_END_TIME':planEndTime,
				'autoCallTask.RETRY_TIMES':retryTimes,
				'autoCallTask.RETRY_INTERVAL':retryInterval,
				'autoCallTask.INTERVAL_TYPE':intervalType,
				'autoCallTask.PRIORITY':priority
			});

			//设置调度计划
			$("#SCHEDULE_ID_INFO").val(scheduleId);
			$("#SCHEDULE_NAME").textbox('setValue',scheduleName);
			currScheduleId = scheduleId;

			//先给定一个默认的催缴类型
			$("#REMINDER_TYPE").combobox('setValue','1');

			if(taskType=='1') {   //为普通外呼时，设置语音文件
				if(commonVoiceId != null && commonVoiceId != 'null' && commonVoiceId != '') {
					$("#COMMON_VOICE_ID").val(commonVoiceId);
					$("#COMMON_VOICE_DESC").textbox('setValue',commonVoiceDesc);
				}
			}else if(taskType=='2') {    //为问卷调查任务
				if(questionnaireId != null && questionnaireId != 'null' && questionnaireId != '') {
					$("#QUESTIONNAIRE_ID").val(questionnaireId);
					$("#QUESTIONNAIRE_DESC").textbox('setValue',questionnaireDesc);
				}
			}else if(taskType=='3'){     //为催缴外呼时
				if(reminderType != null && reminderType != 'null' && reminderType != '') {
					$("#REMINDER_TYPE").combobox('setValue',reminderType);
				}
			}
			//设置开始语音
			if(startVoiceId !=null && startVoiceId != 'null' && startVoiceId != '') {
				$("#START_VOICE_ID").val(startVoiceId);
				$("#START_VOICE_DESC").textbox('setValue',startVoiceDesc);
			}
			//设置结束语音
			if(endVoiceId !=null && endVoiceId != 'null' && endVoiceId != '') {
				$("#END_VOICE_ID").val(endVoiceId);
				$("#END_VOICE_DESC").textbox('setValue',endVoiceDesc);
			}
			//设置黑名单
			if((blackListId !=null && blackListId != 'null' && blackListId != '')) {
				$("#BLACKLIST_ID").val(blackListId);
				$("#BLACKLIST_NAME").textbox('setValue',blackListName);
			}

			showExtraTabs();      //
			
			findDataForTelephone();                                    //加载任务的号码列表

			refreshReviewNote();                                       //加载审核提示
			
			$("#reviewDlg").dialog('setTitle','审核外呼任务').dialog('open');
			
		}

		function showMore() {
			if(isShowMore == 0 ) {
				
				$("#more").css('display','');
				isShowMore = 1;
			}else if(isShowMore==1) {
				$("#more").css('display','none');
				isShowMore = 0;
			}
		}


		function showExtraTabs() {
			
    		//显示列表时,则要将催缴类的相关字段显示做一些控制，针对不同的催缴类型，显示不同的字段
    		var currTaskType = $("#TASK_TYPE").combobox('getValue');         //当前任务类型
    		var currReminderType = $("#REMINDER_TYPE").combobox('getValue'); //当前催缴类型

    		hideAllExtraTh();         //先隐藏所有的号码列表的额外字段

    		if(currTaskType=="3") {   //催缴类型
	    		if(currReminderType=='6') {           //车辆违章
	    			$("#autoCallTaskTelephoneDg").datagrid('showColumn','ILLEGAL_CITY');
	    		    $("#autoCallTaskTelephoneDg").datagrid('showColumn','PUNISHMENT_UNIT');
	    		    $("#autoCallTaskTelephoneDg").datagrid('showColumn','ILLEGAL_REASON');
	    		    $("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');

	    		    //显示添加号码表单输入项
	    		    $("#periodDiv").css('display','');
	    		    $("#illegalCityDiv").css('display','');
	    		    $("#punishmentUnitDiv").css('display','');
	    		    $("#illegalReasonDiv").css('display','');
	    		    
	    		}else if(currReminderType=='7') {     //社保催缴
	    		    $("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');
	    		    $("#autoCallTaskTelephoneDg").datagrid('showColumn','COMPANY');

	    		  	//显示添加号码表单输入项
	    		    $("#periodDiv").css('display','');
	    		    $("#companyDiv").css('display','');
	    		    
	    		}else {                               //电话、水电气及物业催缴
	    		    $("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');
	    		    $("#autoCallTaskTelephoneDg").datagrid('showColumn','CHARGE');

	    		    $("#periodDiv").css('display','');
	    		    $("#chargeDiv").css('display','');
	    		}
    		}
	    	
	    }

	    function hideAllExtraTh() {   //隐藏所有的号码列表的额外字段（主要是催缴类外呼任务）
		    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','ILLEGAL_CITY');
		    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','PUNISHMENT_UNIT');
		    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','ILLEGAL_REASON');
		    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','PERIOD');
		    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','CHARGE');
		    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','COMPANY');

		    //同时，要将添加号码的表单额外输入项全部隐藏
		    $("#periodDiv").css('display','none');
		    $("#illegalCityDiv").css('display','none');
		    $("#punishmentUnitDiv").css('display','none');
		    $("#illegalReasonDiv").css('display','none');
		    $("#companyDiv").css('display','none');
		    $("#chargeDiv").css('display','none');
	    }

	    function getJplayer() {
		    var jp = "<div id='jquery_jplayer_1' class='jp-jplayer'></div>";
		    jp += "<div id='jp_container_1' class='jp-audio' role='application' aria-label='media player'>";
		    jp += "<div class='jp-type-playlist'>";
		    jp += "<div class='jp-gui jp-interface'>";
		    jp += "<div class='jp-controls-holder'>";
		    jp += "<div class='jp-controls'>";
		    jp += "<button class='jp-previous' role='button' tabindex='0'>previous</button>";
		    jp += "<button class='jp-play' role='button' tabindex='0'>play</button>";
		    jp += "<button class='jp-stop' role='button' tabindex='0'>stop</button>";
		    jp += "<button class='jp-next' role='button' tabindex='0'>next</button>";
		    jp += "</div>";
		    jp += "<div class='jp-progress'>";
		    jp += "<div class='jp-seek-bar'>";
		    jp += "<div class='jp-play-bar'></div>";
		    jp += "</div>";
		    jp += "</div>";
		    jp += "<div class='jp-current-time' role='timer' aria-label='time'>&nbsp;</div>";
		    jp += "<div class='jp-duration' role='timer' aria-label='duration'>&nbsp;</div>";
		    jp += "</div>";
		    jp += "</div>";
		    jp += "<div class='jp-playlist'>";
		    jp += "<ul>";
		    jp += "<li>&nbsp;</li>";
		    jp += "</ul>";
		    jp += "</div>";
		    jp += "</div>";
		    jp += "</div>";

		    return jp;
	    }
	    
	  	//刷新审核提示
		function refreshReviewNote() {

			$.ajax({

				type:'POST',
				dataType:'json',
				url:'autoCallTaskReview/reviewNote?taskId=' + currTaskId,
				success:function(rs) {

					var statusCode = rs.statusCode;
					var message = rs.message;

					if(statusCode = 'success') {
						$("#reviewNote").html(message);

						//动态加载播放器
						$("#jplayerDiv").html(getJplayer());

						var extraMessage = rs.extraMessage;   //额外信息为播放列表信息
						eval(extraMessage);                   //加载播放列表
						//window.parent.showMessage(extraMessage,"error");
					}else {
						window.parent.showMessage(message,statusCode);
					}
				
				}
				
			});
			
		}

		function showScheduleDetail() {

			$.ajax({
				type:'POST',
				dataType:'json',
				url:'schedule/getSchedule?scheduleId=' + currScheduleId,
				success:function(schedule) {
					if(schedule == null) {
						window.parent.showMessage("调度方案不存在,无法预览详情","error");
						return;
					}

					var scheduleId = schedule.SCHEDULE_ID;
					var scheduleName = schedule.SCHEDULE_NAME;
					var dateType = schedule.DATETYPE;
					var dateTypeDetail = schedule.DATETYPE_DETAIL;
					var maxTimeItem = schedule.MAXTIMEITEM;

					var startHour1 = schedule.STARTHOUR1;
					var startMinute1 = schedule.STARTMINUTE1;
					var endHour1 = schedule.ENDHOUR1;
					var endMinute1 = schedule.ENDMINUTE1;
					
					var startHour2 = schedule.STARTHOUR2;
					var startMinute2 = schedule.STARTMINUTE2;
					var endHour2 = schedule.ENDHOUR2;
					var endMinute2 = schedule.ENDMINUTE2;
					
					var startHour3 = schedule.STARTHOUR3;
					var startMinute3 = schedule.STARTMINUTE3;
					var endHour3 = schedule.ENDHOUR3;
					var endMinute3 = schedule.ENDMINUTE3;
					
					var startHour4 = schedule.STARTHOUR4;
					var startMinute4 = schedule.STARTMINUTE4;
					var endHour4 = schedule.ENDHOUR4;
					var endMinute4 = schedule.ENDMINUTE4;
					
					var startHour5 = schedule.STARTHOUR5;
					var startMinute5 = schedule.STARTMINUTE5;
					var endHour5 = schedule.ENDHOUR5;
					var endMinute5 = schedule.ENDMINUTE5;
				
					scheduleDetail(scheduleId,scheduleName,dateType,dateTypeDetail,maxTimeItem,startHour1,startMinute1,endHour1,endMinute1,startHour2,startMinute2,endHour2,endMinute2,startHour3,startMinute3,endHour3,endMinute3,startHour4,startMinute4,endHour4,endMinute4,startHour5,startMinute5,endHour5,endMinute5);
				}
			});
			
		}

		function scheduleDetail(scheduleId,scheduleName,dateType,dateTypeDetail,maxTimeItem,startHour1,startMinute1,endHour1,endMinute1,startHour2,startMinute2,endHour2,endMinute2,startHour3,startMinute3,endHour3,endMinute3,startHour4,startMinute4,endHour4,endMinute4,startHour5,startMinute5,endHour5,endMinute5) {

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


		//根据 radioName 取得 radio 选中的值
		function getRadioValue(radioName) {
			return $("input[name='" + radioName + "']:checked").val();
		}
		
		//保存审核
		function saveReview() {

			var reviewResult = getRadioValue("REVIEW_RESULT");   //取得审核结果

			if(reviewResult==null || reviewResult=='') {
				window.parent.showMessage("保存失败,未选择审核结果!","error");
				return;
			}

			if(reviewResult == '2') {   //即是审核不通过时,审核意见不能为空
				var reviewAdvice = $("#REVIEW_ADVICE").textbox('getValue');
				if(reviewAdvice==null || reviewAdvice == '') {
					window.parent.showMessage("保存失败,审核不通过时,审核意见不能为空!","error");
					return;
				}
			}
			
			$("#reviewForm").form('submit',{

				url:'autoCallTaskReview/saveReview?taskId=' + currTaskId + '&reviewResult=' + reviewResult,
				onSubmit:function() {
					var v = $(this).form('validate');
					if(v) {
	    				$.messager.progress({
							msg:'系统正在处理，请稍候...',
							interval:3000
						});
					}
					    			
					return $(this).form('validate');
				},
				success:function(data) {

					$.messager.progress('close');
					var result = JSON.parse(data); //解析Json数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {         //保存成功时
						$("#reviewForm").form('clear');
						$("#reviewDlg").dialog('close');
						findData();
					}
					
				}
				
			});
			
		}

		//归档
		function archive() {

			$.messager.confirm("提示","你确定要直接归档该任务吗?任务归档后,任务将置为历史任务,不可恢复!",function(r){

				if(r) {

					$.messager.progress({
						msg:'系统正在处理，请稍候...',
						interval:2000
					});
					
					$.ajax({

						type:'POST',
						dataType:'json',
						url:'autoCallTask/archive?taskId=' + currTaskId,
						success:function(rs) {
							$.messager.progress('close');
							var statusCode = rs.statusCode;     //返回结果类型
							var message = rs.message;           //返回处理信息

							window.parent.showMessage(message,statusCode);

							if(statusCode == 'success') {
								findData();
								$("#reviewDlg").dialog('close');
							}
						
						}
						
					});
					
				}
				
			});
			
		}
		
		function reviewCancel() {
			$("#reviewDlg").dialog('close');
		}

		function telephonestateformatter(value,data,index) {

			var telephoneState = data.STATE;

			if(telephoneState=='0') {
				return "新号码";
			}else if(telephoneState=='1') {
				return "已载入";
			}else if(telephoneState=='2') {
				return "呼叫成功";
			}else if(telephoneState=='3') {
				return "待重试";
			}else if(telephoneState=='4') {
				return "呼叫失败";
			}
			
		}
		
		function showAllColumns() {
			$("#allColumns").css("display","none");
			$("#simpleColumns").css("display","inline");
			
			$("#autoCallTaskDg").datagrid("showColumn","CALLERID_DESC");   			//主叫号码
			$("#autoCallTaskDg").datagrid("showColumn","scheduleDetail");  			//调度方案
			$("#autoCallTaskDg").datagrid("showColumn","RETRY_TIMES");     			//重试次数
			$("#autoCallTaskDg").datagrid("showColumn","RETRY_INTERVAL_DESC");  			//重试间隔
			$("#autoCallTaskDg").datagrid("showColumn","CREATE_USERCODE_DESC");     //创建人
			$("#autoCallTaskDg").datagrid("showColumn","ORG_CODE_DESC");     		//部门组织名字
			$("#autoCallTaskDg").datagrid("showColumn","CREATE_TIME");              //创建时间
		}
		
		function showSimpleColumns() {
			$("#allColumns").css("display","inline");
			$("#simpleColumns").css("display","none");
			
			$("#autoCallTaskDg").datagrid("hideColumn","CALLERID_DESC");
			$("#autoCallTaskDg").datagrid("hideColumn","scheduleDetail");
			$("#autoCallTaskDg").datagrid("hideColumn","RETRY_TIMES");
			$("#autoCallTaskDg").datagrid("hideColumn","RETRY_INTERVAL_DESC");
			$("#autoCallTaskDg").datagrid("hideColumn","CREATE_USERCODE_DESC");
			$("#autoCallTaskDg").datagrid("hideColumn","ORG_CODE_DESC");
			$("#autoCallTaskDg").datagrid("hideColumn","CREATE_TIME");
		}
		
		function calleridformatter(value,data,index) {
			return "<div id='calleriddesc_" + index + "' style='width:auto;' class='easyui-panel easyui-tooltip'>主叫号码</div>";
		}
			
	</script>
</head>
<body>

<%@ include file="/base_loading.jsp" %>

<!-- 定义一个 layout -->
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
					<select class="easyui-combobox" style="width: 155px;" id="taskState" data-options="panelHeight:'auto'">
					</select>
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
					<th data-options="field:'TASK_NAME',width:250,align:'center'">任务名称</th>
					<th data-options="field:'TASK_TYPE_DESC',width:150,align:'center'">任务类型</th>
					<th data-options="field:'CALLERID_DESC',width:150,align:'center',formatter:calleridformatter">主叫号码</th>
					<th data-options="field:'taskStateField',width:120,align:'center',formatter:taskstaterowformatter">状态</th>
					<th data-options="field:'validityDate',width:220,align:'center',formatter:validitydaterowformatter">有效期</th>
					<th data-options="field:'scheduleDetail',width:50,align:'center',formatter:scheduledetailformatter">调度</th>
					<th data-options="field:'RETRY_TIMES',width:80,align:'center'">重试次数</th>
					<th data-options="field:'RETRY_INTERVAL_DESC',width:100,align:'center'">重试间隔</th>
					
					<th data-options="field:'CREATE_USERCODE_DESC',width:150,align:'center'">创建人</th>
					<th data-options="field:'ORG_CODE_DESC',width:150,align:'center'">部门(组织)名字</th>
					<th data-options="field:'CREATE_TIME',width:180,align:'center'">创建时间</th>
					<th data-options="field:'id',width:100,align:'center',formatter:rowformatter">操作</th>
				</tr>
				
			</thead>
		</table>	
	</div>
</div>

<div id="searchtool" style="padding:5px;">
	<div style="display:inline;">
		<a id="allColumns" href="#" id="easyui-add" onclick="showAllColumns()" class="easyui-linkbutton" iconCls='icon-add' plain="true" style="margin-left:10px;display:inline">全部显示</a>
		<a id="simpleColumns" href="#" id="easyui-add" onclick="showSimpleColumns()" class="easyui-linkbutton" iconCls='icon-remove' plain="true" style="margin-left:10px;display: none;">精简显示</a>
	</div>
</div>

<div id="reviewDlg" class="easyui-dialog" style="width:950px;height:550px;padding:5px;" modal="true" closed="true">
	<!-- 包含审核的表单 -->
	<%@ include file="/autocall/autocalltask/_reviewform.jsp" %>
</div>

<!-- 调度详情弹窗 -->
<div id="scheduleDetailDlg" class="easyui-dialog" style="width:1000px;height:400px;padding:5px;" modal="true" closed="true">
	<div style="text-align: center" id="scheduleDetailInfo">
				
	</div>
</div>

</body>
</html>

