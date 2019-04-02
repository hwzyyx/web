<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>博世订单信息</title>
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
	<script type="text/javascript" src="autocall/autocalltask/_autocall_base.js"></script>
	<script type="text/javascript">
		
		var currTaskId = null;
		var currTaskName = null;
		var currTaskType = null;
		var currReminderType = null;
		var isShowMore = 0;
		var conditionState = null;
		var conditionHangupCause = null;
		var isSearchHistoryCallTask = 0;          //是否为查询历史任务,0为否，1为是
		
		var currCreateType = 'voiceFile';
		
		//getDataFromServer();     //取得从后台传来的数据
		var orgComboTreeData = eval('${orgComboTreeData}');
		var voiceTypeComboboxDataFor0 = eval('${voiceTypeComboboxDataFor0}');
		var dateTypeComboboxDataFor1 = eval('${dateTypeComboboxDataFor1}');
		
		var taskTypeComboboxDataFor0 = eval('${taskTypeComboboxDataFor0}');
		var taskTypeComboboxDataFor1 = eval('${taskTypeComboboxDataFor1}');
		
		var allTaskTypeComboboxDataFor0 = eval('${allTaskTypeComboboxDataFor0}');
		var allTaskTypeComboboxDataFor1 = eval('${allTaskTypeComboboxDataFor1}');
		
		var reminderTypeComboboxDataFor0 = eval('${reminderTypeComboboxDataFor0}');
		var reminderTypeComboboxDataFor1 = eval('${reminderTypeComboboxDataFor1}');
		var allReminderTypeComboboxDataFor0 = eval('${allReminderTypeComboboxDataFor0}');
		var allReminderTypeComboboxDataFor1 = eval('${allReminderTypeComboboxDataFor1}');
		
		var taskStateComboboxDataFor0 = eval('${taskStateComboboxDataFor0}');
		var taskStateComboboxDataFor1 = eval('${taskStateComboboxDataFor1}');
		
		var messageStateComboboxDataFor1 = eval('${messageStateComboboxDataFor1}');
		
		//var lastCallResultComboboxDataFor1 = eval('${lastCallResultComboboxDataFor1}');
		var hangupCauseComboboxDataFor1 = eval('${hangupCauseComboboxDataFor1}');
		
		var stateComboboxDataFor1 = eval('${stateComboboxDataFor1}');
		
		var callerIdComboboxDataFor0 = eval('${callerIdComboboxDataFor0}');
		var callerIdComboboxDataFor1 = eval('${callerIdComboboxDataFor1}');
		
		//时间类型,0:创建时间;1:外呼时间, 默认为0。
    	var dateTimeType = 0;       //主要用于查询数据时，时间区段代表是以创建时间为查询区段，还是以外呼时间为查询区段
		
		$(function(){
			
			$("#isSendMessageCheckBox").change(function(){
				if($("#isSendMessageCheckBox").prop('checked')) {
					//alart("被选中了");
					$('#messageContentTr').css("display","");
					setMessageContentValue();
				}else {
					//alert("没有被选中");
					$("#MESSAGE_CONTENT").textbox("setValue",null);
					$('#messageContentTr').css("display","none");
				}
			});
			
			$("#isSearchHistoryCallTaskCheckBox").change(function(){
				if($("#isSearchHistoryCallTaskCheckBox").prop('checked')) {
					//alart("被选中了");
					isSearchHistoryCallTask = 1;
					$('#easyui-add').linkbutton('disable');
				}else {
					//alert("没有被选中");
					isSearchHistoryCallTask = 0;
					$('#easyui-add').linkbutton('enable');
				}
				findData();
			});
			
			$("#selectAllCallerIdCheckBox").change(function(){
				if($("#selectAllCallerIdCheckBox").prop('checked')) {
					//alart("被选中了");     //需要选中所有的选项
					var data = $("#CALLERID").combobox('getData');
					for(var i=0;i<data.length;i++) {
						$("#CALLERID").combobox('select',data[i].id);
					}
				}else {
					//alert("没有被选中");   //去除所有已选中
					var data = $("#CALLERID").combobox('getData');
					for(var i=0;i<data.length;i++) {
						$("#CALLERID").combobox('unselect',data[i].id);
					}
				}
			});
			
			
			showSimpleColumns();     //任务列表，显示简单的列
			
			//tts创建语音时，对于内容的长度限制,最长限制200个字
			ttsContentTextLengthLimit();
			
			initSelectCallerIdAssign();
			
			//事件绑定,主要是对创建语音的是通过  TTS 还是 语音文件 创建语音，然后做一些显示/隐藏 的操作
			eventBind();
			
			//加载外呼任务的搜索栏的 combobox 的初始化，搜索日期的初始化
			loadComboboxForAutoCallTaskSearch();
			//搜索栏的组织加载
			initOrgCodeForAutoCallTaskSearch();
			
			$("#notSendMessageButton").linkbutton('select');
			
			$("#voiceFile").filebox({
				buttonText:'选择文件'
			});
			
			$("#telephoneFile").filebox({
				buttonText:'选择文件'
			});
			
			//加载创建外呼任务时，初始化操作, 如 任务类型、主叫号码、任务开始及结束日期
			loadDataForCreateAutoCallTaskSearch();
			//创建任务选择语音、调度计划、调查问卷时，双击时做的一些操作
			selectResourceEvent();
			
			//初始化客户号码列表
			initAutoCallTaskTelephoneList();
			
			//初始化客户号码列表,是在外呼结果双击饼图时的号码列表
			initAutoCallTaskTelephoneList2();
			
			//autoCallTaskTelephoneDlg 弹窗关闭时触发事件
			autoCallTaskTelephoneDlgCloseEvent();
			
			//dateMonthCombobox 弹窗关闭时触发
			dateMonthComboboxCloseEvent();
			
			$("#dateTimeTypeBtn0").bind("click",function(){  
	        	dateTimeType = 0; 
	        });
    		
    		$("#dateTimeTypeBtn1").bind("click",function(){  
	        	dateTimeType = 1; 
	        });
    		
    		$("#dateInterval").combobox({
    			onChange:function(newValue,oldValue){
    				$('#startTimeForTelephone').datetimebox('setValue',getDateBefore(newValue-1) + ' 00:00:00');
    	    		$('#endTimeForTelephone').datetimebox('setValue',getDateAfter(1) + ' 00:00:00');
    			}
    		}).combobox('setValue','1');
    		
    		$('#autoCallTaskDlg').dialog({
				onClose:function() {
					currTaskId = null;
					if(isShowMore==1) { showMore();}   //如果显示更多时,关闭显示更多
					$("#autoCallTaskForm").form('clear');
					
					//清除导入号码表单的号码文件表单
					$("#uploadTelephoneForm").form('clear');
					//清除号码列表的搜索框信息
					$("#telephone").textbox('setValue','');
					$("#clientName").textbox('setValue','');
					
					//同时，要将号码列表数据清空
			    	$("#autoCallTaskTelephoneDg").datagrid('loadData',{total:0,rows:[]});    //号码列表清空
			    	$("#autoCallTaskTabs").tabs('select',"外呼任务管理");   //默认选中黑名单管理
					
					$('#customerName').textbox('setValue','');
					$('#customerTel').textbox('setValue','');
					$('#state').combobox('setValue','empty');
					$('#hangupCause').combobox('setValue','empty');
					$('#startTimeForTelephone').datetimebox('setValue','');
					
					
					$("#voiceForm").form('clear');

					//除了清空表单，还需要把创建类型复位，即是选择文件打开，TTS方式关闭
					$("#createType_voiceFile").linkbutton('select');  
					$("#voiceFileDiv").css('display','');
					$("#ttsDiv").css('display','none');
					currCreateType = 'voiceFile';
					
					//将信息内容关闭，并将信息内容清空
					$('#messageContentTr').css('display','none');
					$("#MESSAGE_CONTENT").textbox('setValue','');
					$("#notSendMessageButton").linkbutton('select');
					
					//任务类型和催缴类型要置为可编辑
					//$("#TASK_TYPE").combobox('setValue','1').combobox('enable');
					//$("#REMINDER_TYPE").combobox('setValue','1').combobox('enable');
					$("#TASK_TYPE").combobox('readonly',false);
					$("#REMINDER_TYPE").combobox('readonly',false);
				}
			});
    		
    		//填充汇总数据
    		$('#summaryDg').datagrid({toolbar:'#summaryDgTool'}).datagrid('loadData','');
		});
    	
    	
		
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
				sendMessage:sendMessage,
				isSearchHistoryCallTask:isSearchHistoryCallTask
			});
			
		}
		
		function findDataForTelephone() {
    		$("#autoCallTaskTelephoneDg").datagrid('load',{
        		taskId:currTaskId,
        		customerTel:$('#customerTel').textbox('getValue'),
    			customerName:$('#customerName').textbox('getValue'),
    			state:$("#state").combobox('getValue'),
    			hangupCause:$("#hangupCause").combobox('getValue'),
    			messageState:$("#messageState").combobox('getValue'),
    			startTimeForTelephone:$("#startTimeForTelephone").datebox('getValue'),
				endTimeForTelephone:$("#endTimeForTelephone").datebox('getValue'),
				dateTimeType:dateTimeType,
				isSearchHistoryCallTask:isSearchHistoryCallTask
        	});
    	}
		
		//导出 excel
		function autoCallTaskTelephoneExport() {
			
			var state = $("#state").combobox('getValue');
			var hangupCause = $("#hangupCause").combobox('getValue');
			var messageState = $("#messageState").combobox('getValue');
			var customerTel = $("#customerTel").textbox('getValue');
			var	customerName = $("#customerName").textbox('getValue');
			var startTimeForTelephone = $("#startTimeForTelephone").datebox('getValue');
			var endTimeForTelephone = $("#endTimeForTelephone").datebox('getValue');
			
			$("#exportForm").form('submit',{

				url:"autoCallTaskTelephone/exportExcel",
				onSubmit:function(param) {
					param.taskId = currTaskId;
					param.state = state;
					param.hangupCause = hangupCause;
					param.messageState = messageState;
					param.customerTel = customerTel;
					param.customerName = customerName;
					param.startTimeForTelephone = startTimeForTelephone;
					param.endTimeForTelephone = endTimeForTelephone;
					param.dateTimeType = dateTimeType;
					param.isSearchHistoryCallTask = isSearchHistoryCallTask;   //是否为历史任务
				},
				success:function(data) {
					
				}	
				
			});
			
		}
		
		//导出 excel
		function autoCallTaskTelephoneExport2() {

			var state = conditionState;
			var hangupCause = conditionHangupCause;
			$("#exportForm").form('submit',{

				url:"autoCallTaskTelephone/exportExcel",
				onSubmit:function(param) {
					param.taskId = currTaskId;
					param.state = state;
					param.hangupCause = hangupCause;
				},
				success:function(data) {
					
				}	
				
			});
			
		}
		
		function findDataForTelephoneFor2() {
    		$("#autoCallTaskTelephoneDg2").datagrid('load',{
        		taskId:currTaskId,
    			state:conditionState,
    			hangupCause:conditionHangupCause,
    			isSearchHistoryCallTask:isSearchHistoryCallTask
        	});
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
						选择组织：<select class="easyui-combotree" id="orgCode" name="orgCode" style="width:130px;" data-options="panelHeight:'auto',multiple:true"></select>
					</span>
					<span style="padding-left:40px;">
						<input type="checkbox" id="isSearchHistoryCallTaskCheckBox" value="1"><label for="isSearchHistoryCallTaskCheckBox">历史任务</label>
						<span style="color:red;margin-left: 20px;">*查询已归档任务</span>
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
						下发短信：<select class="easyui-combobox" id="sendMessage" name="sendMessage" style="width:130px;" data-options="panelHeight:'auto'">
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
					<th data-options="field:'CALL_RESULT',width:80,align:'center',formatter:callresultformatter">呼叫结果</th>
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
					<th data-options="field:'id',width:200,align:'center',formatter:rowformatter">操作</th>
				</tr>
				
			</thead>
		</table>
	</div>

</div>

<div id="searchtool" style="padding:5px;">
	<div style="display:inline;">
		<a href="#" id="easyui-add" onclick="autoCallTaskAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新建任务</a>
		<a id="allColumns" href="#" id="easyui-add" onclick="showAllColumns()" class="easyui-linkbutton" iconCls='icon-add' plain="true" style="margin-left:100px;display:inline">全部显示</a>
		<a id="simpleColumns" href="#" id="easyui-add" onclick="showSimpleColumns()" class="easyui-linkbutton" iconCls='icon-remove' plain="true" style="margin-left:100px;display: none;">精简显示</a>
	</div>
	<div style="display:inline;position:absolute;right:10px;">
		<button id="applyActiveBtn" class="easyui-linkbutton" data-options="disabled:true" onclick="changeState('applyActive')">申请激活</button>
		<button id="cancelApplyActiveBtn" class="easyui-linkbutton" data-options="disabled:true" onclick="changeState('cancelApplyActive')">取消激活</button>
		<button id="pauseBtn" class="easyui-linkbutton" data-options="disabled:true" onclick="changeState('pause')">暂停任务</button>
		<button id="cancelPauseBtn" class="easyui-linkbutton" data-options="disabled:true" onclick="changeState('cancelPause')">取消暂停</button>
		<button id="stopBtn" class="easyui-linkbutton" data-options="disabled:true" onclick="changeState('stop')">结束任务</button>
		<button id="archiveBtn" class="easyui-linkbutton" onclick="changeState('archive')">归档(标注历史)</button>
	</div>
	
</div>

<div id="autoCallTaskDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	<!-- 包含外呼任务的表单 -->
	<%@ include file="/autocall/autocalltask/_form.jsp" %>
</div>

<!-- 主叫号码选择窗 -->
<div id="callerIdDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/system/callerid/_selectlist.jsp"%>
</div>

<!-- 主叫号码组选择窗 -->
<div id="callerIdGroupDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/system/calleridgroup/_selectlist.jsp"%>
</div>

<!-- 调度计划选择窗 -->
<div id="scheduleDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/system/schedule/_selectlist.jsp"%>
</div>

<!-- 语音选择弹窗 -->
<div id="voiceDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/voice/selectlist.jsp"%>
</div>

<!-- 调查问卷选择窗 -->
<div id="questionnaireDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/questionnaire/selectlist.jsp"%>
</div>

<!-- 黑名单选择窗 -->
<div id="blackListDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/blacklist/selectlist.jsp"%>
</div>

<!-- 号码组选择窗 -->
<div id="autoNumberDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/number/selectlist.jsp"%>
</div>

<div id="autoCallTaskTelephoneDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
		<!-- 包含外呼任务号码表单 -->
		<%@ include file="/autocall/autocalltask/_telephoneform.jsp" %>
</div>

<!-- 调度详情弹窗 -->
<div id="scheduleDetailDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	<div style="text-align: center" id="scheduleDetailInfo">
				
	</div>
</div>

<!-- 语音创建弹窗 -->
<div id="voiceFormDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/voice/_form.jsp" %>
</div>

<!-- 外呼结果弹窗 -->
<div id="callResultDlg" class="easyui-dialog" style="width:80%;height:80%;padding:5px;" modal="true" closed="true">
	<%@ include file="/autocall/autocalltask/_callresult.jsp" %>
</div>

<!-- 外呼调查结果弹窗 -->
<div id="autoCallTaskSurveyResultDlg" class="easyui-dialog" style="width:60%;height:80%;padding:5px;" modal="true" closed="true">
	<%@ include file="/autocall/autocalltask/_surveyresult.jsp" %>
</div>

<!-- 任务号码列表 -->
<div id="autoCallTelephoneDlg" class="easyui-dialog" style="width:80%;height:80%;padding:10px 20px;" modal="true" closed="true">
	<div data-options="fit:true" class="easyui-layout">
		<div data-options="region:'center',split:true,border:false">
			<table id="autoCallTaskTelephoneDg2" class="easyui-datagrid">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'ck',checkbox:true"></th>		
						<th data-options="field:'CUSTOMER_NAME',width:120,align:'center'">客户姓名</th>
						<th data-options="field:'CUSTOMER_TEL',width:120,align:'center'">电话号码</th>
						<th data-options="field:'PROVINCE',width:120,align:'center'">省份</th>
						<th data-options="field:'CITY',width:120,align:'center'">城市</th>
						<th data-options="field:'CALLOUT_TEL',width:120,align:'center'">外呼号码</th>
						<th data-options="field:'CALLERID',width:120,align:'center'">主叫号码</th>
						<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
						<th data-options="field:'STATE_DESC',width:100,align:'center'">外呼结果</th>
						<th data-options="field:'HANGUP_CAUSE_DESC',width:200,align:'center'">失败原因</th>
						<th data-options="field:'RETRIED_DESC',width:150,align:'center'">呼叫次数</th>
						<th data-options="field:'LOAD_TIME',width:200,align:'center'">外呼时间</th>
						<th data-options="field:'BILLSEC',width:150,align:'center'">通话时长</th>
						<th data-options="field:'NEXT_CALLOUT_TIME',width:200,align:'center'">下次外呼时间</th>
						<th data-options="field:'MESSAGE_STATE_DESC',width:100,align:'center'">短信状态</th>
						<th data-options="field:'MESSAGE_FAILURE_CODE',width:100,align:'center'">短信失败代码</th>
													
						<th data-options="field:'PERIOD',width:120,align:'center'">日期</th>
						<th data-options="field:'DISPLAY_NUMBER',width:100,align:'center'">表显数量</th>
						<th data-options="field:'DOSAGE',width:100,align:'center'">使用量</th>							
						<th data-options="field:'CHARGE',width:100,align:'center'">费用</th>
						<th data-options="field:'ACCOUNT_NUMBER',width:120,align:'center'">户号</th>
						<th data-options="field:'ADDRESS',width:120,align:'center'">地址</th>
						<th data-options="field:'CALL_POLICE_TEL',width:100,align:'center'">报警人电话</th>
						<th data-options="field:'VEHICLE_TYPE',width:120,align:'center'">车辆类型</th>
						<th data-options="field:'PLATE_NUMBER',width:120,align:'center'">车牌号码</th>
						<th data-options="field:'ILLEGAL_CITY',width:100,align:'center'">违章城市</th>
						<th data-options="field:'PUNISHMENT_UNIT',width:150,align:'center'">处罚单位</th>
						<th data-options="field:'ILLEGAL_REASON',width:150,align:'center'">违章事由</th>
						<th data-options="field:'COMPANY',width:150,align:'center'">代缴单位</th>
						<th data-options="field:'id',width:100,align:'center',formatter:telephonerowformatter">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
</div>
</body>
</html>

