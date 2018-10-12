<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
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
		var isShowMore = 0;
		var conditionState = null;
		
		var currCreateType = 'voiceFile';
		
		//getDataFromServer();     //取得从后台传来的数据
		var orgComboTreeData = eval('${orgComboTreeData}');
		var voiceTypeComboboxDataFor0 = eval('${voiceTypeComboboxDataFor0}');
		var dateTypeComboboxDataFor1 = eval('${dateTypeComboboxDataFor1}');
		
		var taskTypeComboboxDataFor0 = eval('${taskTypeComboboxDataFor0}');
		var taskTypeComboboxDataFor1 = eval('${taskTypeComboboxDataFor1}');
	
		var taskStateComboboxDataFor0 = eval('${taskStateComboboxDataFor0}');
		var taskStateComboboxDataFor1 = eval('${taskStateComboboxDataFor1}');
		
		var messageStateComboboxDataFor1 = eval('${messageStateComboboxDataFor1}');
	
		var callerIdComboboxDataFor0 = eval('${callerIdComboboxDataFor0}');
		
		//时间类型,0:创建时间;1:外呼时间, 默认为0。
    	var dateTimeType = 0;       //主要用于查询数据时，时间区段代表是以创建时间为查询区段，还是以外呼时间为查询区段
		
		$(function(){
			//tts创建语音时，对于内容的长度限制,最长限制200个字
			ttsContentTextLengthLimit();
			
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
					$('#state').combobox('setValue','5');
					$('#startTimeForTelephone').datetimebox('setValue','');
					
					
					$("#voiceForm").form('clear');

					//除了清空表单，还需要把创建类型复位，即是选择文件打开，TTS方式关闭
					$("#createType_voiceFile").linkbutton('select');  
					$("#voiceFileDiv").css('display','');
					$("#ttsDiv").css('display','none');
					currCreateType = 'voiceFile';
					
					//将信息内容关闭，并将信息内容清空
					$('#messageContentTr').css('display','none');
					$("#MESSAGE_CONTENT_PREFIXNOTE_DIV").css('display','none');
					$("#MESSAGE_CONTENT").textbox('setValue','');
					$("#notSendMessageButton").linkbutton('select');
				}
			});
    		
    		$('#summaryDg').datagrid('loadData','');
    		
    		/*$("#dateYearCombobox").combobox({
				onChange:function(newValue,oldValue){
					var yearValue = $("#dateYearCombobox").combobox('getValue');
					var monthValue = $("#dateMonthCombobox").combobox('getValue');
					$("#PERIOD").textbox('setValue',yearValue + monthValue);
				}
			});
			
			$("#dateMonthCombobox").combobox({
				onChange:function(newValue,oldValue){
					var yearValue = $("#dateYearCombobox").combobox('getValue');
					var monthValue = $("#dateMonthCombobox").combobox('getValue');
					$("#PERIOD").textbox('setValue',yearValue + monthValue);
				}
			});*/
			
		});
    	
    	
		
		function findData() {
			var selectRs = $("#orgCode").combotree('getValues');
			var orgCodes = selectRs.toString();
			var startTime = $("#startTime").datebox('getValue');
			var endTime = $("#endTime").datebox('getValue');
			var taskType = $("#taskType").combobox('getValue');
			var taskState = $("#taskState").combobox('getValue');
			var sendMessage = $("#sendMessage").combobox('getValue');

			$("#autoCallTaskDg").datagrid('load',{
				taskName:$("#taskName").val(),
				orgCode:orgCodes,
				startTime:startTime,
				endTime:endTime,
				taskType:taskType,
				taskState:taskState,
				sendMessage:sendMessage
			});
			
		}
		
		function findDataForTelephone() {
    		$("#autoCallTaskTelephoneDg").datagrid('load',{
        		taskId:currTaskId,
        		customerTel:$('#customerTel').textbox('getValue'),
    			customerName:$('#customerName').textbox('getValue'),
    			state:$("#state").combobox('getValue'),
    			messageState:$("#messageState").combobox('getValue'),
    			startTimeForTelephone:$("#startTimeForTelephone").datebox('getValue'),
				endTimeForTelephone:$("#endTimeForTelephone").datebox('getValue'),
				dateTimeType:dateTimeType
        	});
    	}
		
		//导出 excel
		function autoCallTaskTelephoneExport() {
			
			var state = $("#state").combobox('getValue');
			var messageState = $("#messageState").combobox('getValue');
			var customerTel = $("#customerTel").numberbox('getValue');
			var	customerName = $("#customerName").textbox('getValue');
			var startTimeForTelephone = $("#startTimeForTelephone").datebox('getValue');
			var endTimeForTelephone = $("#endTimeForTelephone").datebox('getValue');
			
			$("#exportForm").form('submit',{

				url:"autoCallTaskTelephone/exportExcel",
				onSubmit:function(param) {
					param.taskId = currTaskId;
					param.state = state;
					param.messageState = messageState;
					param.customerTel = customerTel;
					param.customerName = customerName;
					param.startTimeForTelephone = startTimeForTelephone;
					param.endTimeForTelephone = endTimeForTelephone;
					param.dateTimeType = dateTimeType;
				},
				success:function(data) {
					
				}	
				
			});
			
		}
		
		//导出 excel
		function autoCallTaskTelephoneExport2() {

			var state = conditionState;

			$("#exportForm").form('submit',{

				url:"autoCallTaskTelephone/exportExcel",
				onSubmit:function(param) {
					param.taskId = currTaskId;
					param.state = state;
				},
				success:function(data) {
					
				}	
				
			});
			
		}
		
		function findDataForTelephoneFor2() {
    		$("#autoCallTaskTelephoneDg2").datagrid('load',{
        		taskId:currTaskId,
    			state:conditionState
        	});
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
					任务名称：<input id="taskName" type="text" class="easyui-textbox" style="width:200px;"/>
					<span style="padding-left:20px;">
						任务类型：<select class="easyui-combobox" id="taskType" name="taskType" style="width:200px;" data-options="panelHeight:'auto'"></select>
					</span>
					<span style="padding-left:20px;">
						任务状态：<select class="easyui-combobox" id="taskState" name="taskState" style="width:200px;" data-options="panelHeight:'auto'"></select>
					</span>
					<span style="padding-left:20px;">
						选择组织：<select class="easyui-combotree" id="orgCode" name="orgCode" style="width:200px;" data-options="panelHeight:'auto'"></select>
					</span>
				</td>
				
			</tr>
			<tr style="vertial-align:top;">
				<td>
					创建时间：<input id="startTime" name="startTime" class="easyui-datebox" style="width:200px;"/><span style="padding-left:38px;padding-right:36px;">至</span> <input id="endTime" name="endTime" class="easyui-datebox" style="width:200px;" />
					<span style="padding-left:20px;">
						下发短信：<select class="easyui-combobox" id="sendMessage" name="sendMessage" style="width:200px;" data-options="panelHeight:'auto'">
									<option value="empty">请选择</option>
									<option value="1">是</option>
									<option value="0">否</option>
								</select>
					</span>
					<span style="padding-left:88px;">
						<a href="javascript:findData()" class="easyui-linkbutton" style="width:200px;" data-options="iconCls:'icon-search'">查询</a>
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
					<th data-options="field:'taskTypeField',width:150,align:'center',formatter:tasktyperowformatter">任务类型</th>
					<th data-options="field:'SEND_MESSAGE',width:80,align:'center',formatter:sendmessageformatter">下发短信</th>
					<th data-options="field:'CALLERID_DESC',width:150,align:'center'">主叫号码</th>
					<th data-options="field:'taskStateField',width:120,align:'center',formatter:taskstaterowformatter">状态</th>
					<th data-options="field:'validityDate',width:220,align:'center',formatter:validitydaterowformatter">有效期</th>
					<th data-options="field:'scheduleDetail',width:50,align:'center',formatter:scheduledetailformatter">调度</th>
					<th data-options="field:'RETRY_TIMES',width:80,align:'center'">呼叫总次数</th>
					<th data-options="field:'RETRY_INTERVAL',width:100,align:'center'">重试间隔(分钟)</th>
					
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
		<a href="#" id="easyui-add" onclick="autoCallTaskAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新建任务</a>
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

<div id="autoCallTaskDlg" class="easyui-dialog" style="width:1200px;height:700px;padding:5px;" modal="true" closed="true">
	<!-- 包含外呼任务的表单 -->
	<%@ include file="/autocall/autocalltask/_form.jsp" %>
</div>

<!-- 调度计划选择窗 -->
<div id="scheduleDlg" class="easyui-dialog" style="width:950px;height:450px;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/schedule/selectlist.jsp"%>
</div>

<!-- 语音选择弹窗 -->
<div id="voiceDlg" class="easyui-dialog" style="width:750px;height:400px;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/voice/selectlist.jsp"%>
</div>

<!-- 调查问卷选择窗 -->
<div id="questionnaireDlg" class="easyui-dialog" style="width:880px;height:400px;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/questionnaire/selectlist.jsp"%>
</div>

<!-- 黑名单选择窗 -->
<div id="blackListDlg" class="easyui-dialog" style="width:880px;height:400px;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/blacklist/selectlist.jsp"%>
</div>

<!-- 号码组选择窗 -->
<div id="autoNumberDlg" class="easyui-dialog" style="width:880px;height:400px;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/number/selectlist.jsp"%>
</div>

<div id="autoCallTaskTelephoneDlg" class="easyui-dialog" style="width:600px;height:400px;padding:5px;" modal="true" closed="true">
		<!-- 包含外呼任务号码表单 -->
		<%@ include file="/autocall/autocalltask/_telephoneform.jsp" %>
</div>

<!-- 调度详情弹窗 -->
<div id="scheduleDetailDlg" class="easyui-dialog" style="width:1000px;height:400px;padding:5px;" modal="true" closed="true">
	<div style="text-align: center" id="scheduleDetailInfo">
				
	</div>
</div>

<!-- 语音创建弹窗 -->
<div id="voiceFormDlg" class="easyui-dialog" style="width:750px;height:400px;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/voice/_form.jsp" %>
</div>

<!-- 外呼结果弹窗 -->
<div id="callResultDlg" class="easyui-dialog" style="width:1050px;height:700px;padding:5px;" modal="true" closed="true">
	<%@ include file="/autocall/autocalltask/_callresult.jsp" %>
</div>

<!-- 任务号码列表 -->
<div id="autoCallTelephoneDlg" class="easyui-dialog" style="width:1200px;height:800px;padding:10px 20px;" modal="true" closed="true">
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
						<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
						<th data-options="field:'state_result',width:100,align:'center',formatter:telephonestateformatter">外呼结果</th>
						<th data-options="field:'LAST_CALL_RESULT',width:200,align:'center'">失败原因</th>
						<th data-options="field:'RETRIED_DESC',width:150,align:'center'">呼叫次数</th>
						<th data-options="field:'LOAD_TIME',width:200,align:'center'">外呼时间</th>
						<th data-options="field:'BILLSEC',width:150,align:'center'">通话时长</th>
						<th data-options="field:'NEXT_CALLOUT_TIME',width:200,align:'center'">下次外呼时间</th>
						<th data-options="field:'MESSAGE_STATE_DESC',width:100,align:'center'">短信状态</th>
						<th data-options="field:'MESSAGE_FAILURE_CODE',width:100,align:'center'">短信失败代码</th>
													
						<th data-options="field:'ILLEGAL_CITY',width:100,align:'center'">违章城市</th>
						<th data-options="field:'PUNISHMENT_UNIT',width:150,align:'center'">处罚单位</th>
						<th data-options="field:'ILLEGAL_REASON',width:150,align:'center'">违章事由</th>
						<th data-options="field:'PERIOD',width:120,align:'center'">日期</th>
						<th data-options="field:'CHARGE',width:100,align:'center'">费用</th>
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

