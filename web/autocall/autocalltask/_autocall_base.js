
//autoCallTaskTelephoneDlg 弹窗关闭时触发
function autoCallTaskTelephoneDlgCloseEvent() {
	$("#autoCallTaskTelephoneDlg").dialog({
		onClose:function() {
			$("#autoCallTaskTelephoneForm").form('clear');
		}
	});
}

//dateMonthCombobox 弹窗关闭时触发
function dateMonthComboboxCloseEvent() {
	$("#dateMonthCombobox").combobox({
		onSelect:function(record){
			var yearValue = $("#dateYearCombobox").combobox('getValue');
			var monthValue = $("#dateMonthCombobox").combobox('getValue');
			$("#PERIOD").textbox('setValue',yearValue + monthValue);
		}
	});
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

//事件绑定
function eventBind() {
	$("#createType_voiceFile").bind('click',function(){
		$("#voiceFileDiv").css('display','');
		$("#ttsDiv").css('display','none');
		currCreateType = 'voiceFile';
	});
	$("#createType_tts").bind('click',function(){
		$("#voiceFileDiv").css('display','none');
		$("#ttsDiv").css('display','');
		currCreateType = 'tts';
	});
}

//tts创建语音时，对于内容的长度限制,最长限制200个字
function ttsContentTextLengthLimit() {
	$('#ttsContent').keyup(function(){
		//alert("输入了一次");
		var len = $(this).val().length;

		if(len>199) {
			$(this).val($(this).val().substring(0,200));
		}

		var lessNum = 200 - len;

		if(lessNum<0){lessNum=0;}
		
		$("#ttsContentLengthNotice").html("还能输入 " + lessNum + " 个字");
		
	});
}

//点击新增任务按钮,打开添加任务的弹窗
function autoCallTaskAdd() {
	$("#CALLERID").combobox('setValue','empty');                   //默认第一个号码
	$("#REMINDER_TYPE").combobox('setValue','1');              //催缴类型为1，即是电话费
	$("#TASK_TYPE").combobox('setValue','1');                  //默认任务类型为普通任务
	$("#RETRY_TIMES").combobox('setValue','3');                //默认的重试次数3次

	$("#RETRY_INTERVAL").numberbox('setValue','10');           //默认重试间隔为10分钟
	
	$("#INTERVAL_TYPE").combobox("setValue","1");			   //间隔类型为 分钟

	$("#PRIORITY").combobox('setValue','2');                   //默认优先级为中
	
	$("#PLAN_START_TIME").datebox('setValue',getCurrDate());   //设置默认任务开始时间
	$("#PLAN_END_TIME").datebox('setValue',getDateAfter(3));   //设置默认任务结束时间
	
	$("#TASK_NAME").textbox('setValue',getCurrTimeToString());
	
	showExtraTabs(0);                                          //添加外呼任务时，导入号码、号码列表暂不显示
	
	$("#autoCallTaskSaveBtn").attr("onclick","autoCallTaskSaveAdd()");
	
	$("#autoCallTaskDlg").dialog('setTitle','添加外呼任务').dialog("open");
}

function autoCallTaskSaveAdd() {

	var chedkRs = checkOutInput();   //输入校验,主要是检查输入项是否为空
	if(!chedkRs) {
		return;
	}
	
	var callerId = $("#CALLERID").combobox('getValue');
	if(callerId=='empty') {
		alert("主叫号码不能为空 ,请重新选择!");
		return;
	}
	
	$('#autoCallTaskForm').form('submit',{
			
		url:'autoCallTask/add',
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

			var result = JSON.parse(data);  //解析json数据

			var statusCode = result.statusCode;   //返回结果类型
			var message = result.message;         //返回执行的结果信息

			window.parent.showMessage(message,statusCode);

			if(statusCode == 'success') {

				currTaskId = result.extraMessage;

				$("#autoCallTaskForm").form('load',{
					'autoCallTask.TASK_ID':currTaskId
				});
				
				showExtraTabs(1);    //如果保存成功，则显示导入号码及号码列表					
				findData();   //重新加载任务列表	

				//暂时不关闭窗口，所以再点击保存按钮时，就是修改操作了
				$("#autoCallTaskSaveBtn").attr("onclick","autoCallTaskSaveEdit()");
				$("#autoCallTaskDlg").dialog('setTitle','编辑外呼任务');
				//$("#autoCallTaskDlg").dialog('close');
			}
			
		}
		
		
	});
	
}

//外呼任务编辑
function autoCallTaskEdit(taskId,taskName,callerId,planStartTime,planEndTime,scheduleId,scheduleName,taskType,commonVoiceId,commonVoiceDesc,questionnaireId,questionnaireDesc,reminderType,startVoiceId,startVoiceDesc,endVoiceId,endVoiceDesc,blackListId,blackListName,retryTimes,retryInterval,intervalType,priority,sendMessage,messageContent) {
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
		'autoCallTask.PRIORITY':priority,
		'autoCallTask.TASK_TYPE':taskType,
		'autoCallTask.REMINDER':reminderType,
		'autoCallTask.MESSAGE_CONTENT':messageContent
	});

	//设置调度计划
	$("#SCHEDULE_ID_INFO").val(scheduleId);
	$("#SCHEDULE_NAME").textbox('setValue',scheduleName);

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

	showExtraTabs(1);                                          //修改外呼任务时，导入号码、号码列表暂不显示
	findDataForTelephone();                                    //加载任务的号码列表
	
	if(sendMessage == 1) {        //如果 sendMessage的值为1，表示有发送信息内容
		$('#messageContentTr').css('display','');
		$('#MESSAGE_CONTENT').textbox('setValue',messageContent);
		$("#isSendMessageCheckBox").prop("checked",true);
		
		if($("#TASK_TYPE").combobox('getValue')=='1') {          //普通任务
			$("#MESSAGE_CONTENT").textbox('textbox').attr('readonly',false);
		}else if($("#TASK_TYPE").combobox('getValue')=='3') {    //催缴任务
			$("#MESSAGE_CONTENT").textbox('textbox').attr('readonly',true);
		}
	}else {
		$('#MESSAGE_CONTENT').textbox('setValue','');
		$("#isSendMessageCheckBox").prop("checked",false);
		$('#messageContentTr').css('display','none');
	} 
	
	//如果是修改时，就不允许再修改任务类型和催缴类型了
	$("#TASK_TYPE").combobox('readonly',true);
	$("#REMINDER_TYPE").combobox('readonly',true);
	
	
	$("#autoCallTaskSaveBtn").attr("onclick","autoCallTaskSaveEdit()");
	$("#autoCallTaskDlg").dialog('setTitle','修改外呼任务').dialog("open");
	
}

//外呼任务编辑保存
function autoCallTaskSaveEdit() {

	var chedkRs = checkOutInput();   //输入校验,主要是检查输入项是否为空
	if(!chedkRs) {
		return;
	}
	
	var callerId = $("#CALLERID").combobox('getValue');
	if(callerId=='empty') {
		alert("主叫号码不能为空 ,请重新选择!");
		return;
	}
	
	//alert($("#TASK_TYPE").combobox('getValue') + "---" + $('#REMINDER_TYPE').combobox('getValue'));
	$('#autoCallTaskForm').form('submit',{

		url:'autoCallTask/update',
		onSubmit:function(param){
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

			var result = JSON.parse(data);  //解析json数据

			var statusCode = result.statusCode;   //返回结果类型
			var message = result.message;         //返回执行的结果信息

			window.parent.showMessage(message,statusCode);

			if(statusCode == 'success') {
				findData();   //重新加载任务列表	
			}
		}
		
	});
	
}

function checkOutInput() {
	//判断调度计划是否为空
	var scheduleName = $("#SCHEDULE_NAME").textbox('getValue');
	if(scheduleName==null || scheduleName=='') {
		alert("调度计划不能为空!");
		return false;
	}

	//检查开始时间及结束时间,确保结束时间大于开始时间
	var planStartTime = $("#PLAN_START_TIME").datebox('getValue');
	var planEndTime = $("#PLAN_END_TIME").datebox('getValue');

	if(planStartTime > planEndTime) {
		alert("任务期限的开始日期不能大于结束日期!");
		return false;
	}
	

	//根据任务类型,判断：
		//普通任务：普通语音文件是否为空
		//调查问卷：调查问卷是否为空
	var taskType = $("#TASK_TYPE").combobox('getValue');
	if(taskType=='1'){          //普通任务
		var commonVoiceDesc = $("#COMMON_VOICE_DESC").textbox('getValue');
		if(commonVoiceDesc==null || commonVoiceDesc =='') {
			alert("普通外呼任务语音文件不能为空!");
			return false;
		}
	}else if(taskType=='2') {   //问卷调查任务
		var questionnaireDesc = $("#QUESTIONNAIRE_DESC").textbox('getValue');
		if(questionnaireDesc==null || questionnaireDesc =='') {
			alert("问卷调查任务问卷不能为空!");
			return false;
		}
	}

	return true;
}

function autoCallTaskDel(taskId) {
	//alert("准备删除" + taskId);
	$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
		if(r) {
			$.messager.progress({
				msg:'系统正在处理,请稍候...'
			});

			$.ajax({
				url:'autoCallTask/delete?taskId=' + taskId,
				method:'POST',
				dataType:'json',
				success:function(rs) {
					$.messager.progress('close');
					var statusCode = rs.statusCode;   
					var message = rs.message;
					window.parent.showMessage(message,statusCode);

					if(statusCode=='success') {
						findData();
					}
					
				}	
			});
		}
	});
}

//上传号码文件
function uploadPhoneFile() {
	$("#uploadTelephoneForm").form('submit',{

		url:'autoCallTaskTelephone/uploadFile?taskId=' + currTaskId,
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

			if(statusCode == 'success') {         //保存成功时
				$("#uploadTelephoneForm").form('clear');
				findDataForTelephone();
				window.parent.showMessage(message,"error");						
			}else {
				window.parent.showMessage(message,statusCode);						
			}
			
		}
		
	});
}



//加载外呼任务的几个搜索栏的 combobox
function loadComboboxForAutoCallTaskSearch() {
	
	//同时，初始外呼任务搜索栏中的结束时间
	$('#endTime').datebox('setValue',getCurrDate());
	
	//任务类型加载
	$("#taskType").combobox({
		valueField:'id',
		textField:'text',
		panelHeight:'auto'
	}).combobox('loadData',taskTypeComboboxDataFor1).combobox('setValue','empty');
	
	//催缴类型加载
	$("#reminderType").combobox({
		valueField:'id',
		textField:'text',
		panelHeight:'auto'
	}).combobox('loadData',reminderTypeComboboxDataFor1).combobox('setValue','empty');
	
	//任务状态加载
	$("#taskState").combobox({
		valueField:'id',
		textField:'text',
		panelHeight:'auto'
    }).combobox('loadData',taskStateComboboxDataFor1).combobox('setValue','empty');
	
	$("#sendMessage").combobox('setValue',"empty");
	
	$("#messageState").combobox({
		valueField:'id',
		textField:'text',
		panelHeight:'auto'
    }).combobox('loadData',messageStateComboboxDataFor1).combobox('setValue','empty');
	
	
}

//加载创建外呼任务时，初始化操作, 如 任务类型、主叫号码、任务开始及结束日期
function loadDataForCreateAutoCallTaskSearch() {
	
	$("#REMINDER_TYPE").combobox({
		valueField:'id',
		textField:'text',
		panelHeight:'auto',
		onChange:function(newValue,oldValue) {
			//是否下发短信的开合和数据
			if($("#isSendMessageCheckBox").prop('checked')) {
				setMessageContentValue();
			}
		}
	}).combobox('loadData',reminderTypeComboboxDataFor0).combobox('setValue','1');
	
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
			//是否下发短信的开合和数据
			if($("#isSendMessageCheckBox").prop('checked')) {
				setMessageContentValue();
			}
		}
	}).combobox('loadData',taskTypeComboboxDataFor0).combobox('setValue','1');
	
	
	$("#CALLERID").combobox({
		valueField:'id',
		textField:'text',
		panelHeight:'auto'
	}).combobox('loadData',callerIdComboboxDataFor1).combobox('setValue','empty');
	
	$("#START_DATE").datebox('setValue',getCurrDate());   	//任务开始时间
	$("#END_DATE").datebox('setValue',getDateAfter(3));     //任务结束时间
	
}

//创建任务选择语音、调度计划、调查问卷、黑名单、号码组，双击时做的一些操作
function selectResourceEvent() {
	
	//定义语音弹出选择框双击时赋值动作
	$("#voiceDg").datagrid({
		//onClickRow:function(index,row) {
			//alert("用户选择了选项，选项值为:" + index);s
		//}
		onDblClickRow:function(index,row) {
			var vt = $("#voiceType").combobox('getValue');   //先查看语音类型： 1 表示普通语音；2 表示问题语音； 3 表示开始欢迎语音；4 表示结束语音
			
			if(vt == 1) {       //如果为普通语音，则赋值给普通语音输入框
			//alert("用户选择了选项，选项值为:index=" + index + ",row['VOICE_ID']=" + row['VOICE_ID'] + ",row['VOICE_DESC']=" + row['VOICE_DESC']);
    			$("#COMMON_VOICE_ID").val(row['VOICE_ID']);
    			$("#COMMON_VOICE_DESC").textbox('setValue',row['VOICE_DESC']);
				
    			$("#voiceDlg").dialog("close");
			}else if(vt == 3) {   //如果为开始语音，则赋值给开始语音输入框
				$("#START_VOICE_ID").val(row['VOICE_ID']);
    			$("#START_VOICE_DESC").textbox('setValue',row['VOICE_DESC']);
				
    			$("#voiceDlg").dialog("close");
			}else if(vt == 4) {   //如果为结束语音，则赋值给结束语音输入框
				$("#END_VOICE_ID").val(row['VOICE_ID']);
    			$("#END_VOICE_DESC").textbox('setValue',row['VOICE_DESC']);


				$("#voiceDg").datagrid('loadData',{total:0,rows:[]});
    			$("#voiceDlg").dialog("close");    //语音选择弹窗关闭前,先将数据清空
			}
        		
		}
    });
	
	//双击调度计划，将高度计划信息赋值
	$("#scheduleDg").datagrid({
		onDblClickRow:function(index,row) {
			$("#SCHEDULE_ID_INFO").val(row['SCHEDULE_ID']);
			$("#SCHEDULE_NAME").textbox('setValue',row['SCHEDULE_NAME']);

			$("#scheduleDlg").dialog("close");
		}
	});
	
	//双击选择调查问卷时,将问卷信息赋值
	$("#questionnaireDg").datagrid({
		onDblClickRow:function(index,row) {

			if(row['QUESTION_COUNT']<=0) {
				alert("该问卷的问题数量为0,不允许选择!");
				return;
			}
			
			//alert("选择了" + row['QUESTIONNAIRE_ID'] + "," + row['QUESTIONNAIRE_DESC']);
			$("#QUESTIONNAIRE_ID").val(row['QUESTIONNAIRE_ID']);
			$("#QUESTIONNAIRE_DESC").textbox('setValue',row['QUESTIONNAIRE_DESC']);
			
			$("#questionnaireDlg").dialog("close");
		}
	});
	
	//双击黑名单，将黑名单信息赋值
	$("#autoBlackListDg").datagrid({
		onDblClickRow:function(index,row) {
			$("#BLACKLIST_ID").val(row['BLACKLIST_ID']);
			$("#BLACKLIST_NAME").textbox('setValue',row['BLACKLIST_NAME']);

			$("#blackListDlg").dialog("close");
		}
	});

	//双击号码组，将号码组信息赋值
	$("#autoNumberDg").datagrid({
		onDblClickRow:function(index,row) {
			$("#NUMBER_ID").val(row['NUMBER_ID']);
			$("#NUMBER_NAME").textbox('setValue',row['NUMBER_NAME']);

			$("#autoNumberDlg").dialog("close");
		}
	});
	
}

//初始化客户号码列表
function initAutoCallTaskTelephoneList() {
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
		toolbar:'#telephoneopertool',
		url:'autoCallTaskTelephone/datagrid',
		queryParams:{
			taskId:currTaskId,
	    	customerTel:$('#customerTel').textbox('getValue'),
			customerName:$('#customerName').textbox('getValue'),
			state:$("#state").combobox('getValue'),
			messageState:$("#messageState").combobox('getValue'),
			startTimeForTelephone:$("#startTimeForTelephone").datebox('getValue'),
			endTimeForTelephone:$("#endTimeForTelephone").datebox('getValue'),
			dateTimeType:dateTimeType
		}
	});
}

//初始化客户号码列表,是在外呼结果双击饼图时的号码列表
function initAutoCallTaskTelephoneList2() {
	//外呼任务号码列表
	$("#autoCallTaskTelephoneDg2").datagrid({
		pageSize:30,
		pagination:true,      
		fit:true,
		rowrap:true,
		striped: true,
		rownumbers: true,
		pageList:[10,30,50],
		checkbox:true,
		toolbar:'#telephoneopertool2',
		url:'autoCallTaskTelephone/datagrid',
		queryParams:{
			taskId:currTaskId,
			state:conditionState,
			dateTimeType:dateTimeType
		}
	});
}

//初始化外呼任务搜索栏中，组织代码的情况
function initOrgCodeForAutoCallTaskSearch() {
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

//外呼结果弹窗
function callresultformatter(value,data,index) {
	
	return "<a href='#' onclick='javascript:showCallResult(\"" + data.TASK_ID + "\",\"" + data.TASK_NAME + "\",\"" + data.TASK_TYPE + "\",\"" + data.REMINDER_TYPE + "\")'>呼叫结果</a>";
}

function showCallResult(taskId,taskName,taskType,reminderType) {
	currTaskName = taskName;
	currTaskId = taskId;
	currTaskType = taskType;
	currReminderType = reminderType;
	$("#callResultDlg").dialog('setTitle',"外呼任务:" + taskName + "--外呼结果").dialog('open');
	reloadStatistics();
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

function sendmessageformatter(value,data,index) {
	if(value==1) {
		return "<span style='color:#00ff00'>是</span>";
	}else {
		return "<span style='color:#ff0000'>否</span>";
	}
}


function telephonestateformatter(value,data,index) {

	var state = data.STATE;

	if(state=='0') {
		return "<span style='color:#00ccff'>未处理</span>";
	}else if(state=='1') {
		return "<span style='color:#61a0a8'>已载入</span>";
	}else if(state=='2') {
		return "<span style='color:#49a849'>已成功</span>";
	}else if(state=='3') {
		return "<span style='color:#e9bfb0'>待重呼</span>";
	}else if(state=='4') {
		return "<span style='color:#c23531'>已失败</span>";
	}
	
}

function telephonerowformatter(value,data,index) {
	return "<a href='#' onclick='javascript:autoCallTaskTelephoneEdit(\"" + data.TEL_ID + "\",\"" + data.CUSTOMER_TEL + "\",\"" + data.CUSTOMER_NAME + "\",\"" + data.PERIOD + "\",\"" + data.DISPLAY_NUMBER + "\",\"" + data.DOSAGE + "\",\"" + data.CHARGE + "\",\"" + data.ACCOUNT_NUMBER + "\",\"" + data.ADDRESS + "\",\"" + data.CALL_POLICE_TEL + "\",\"" + data.VEHICLE_TYPE + "\",\"" + data.PLATE_NUMBER + "\",\"" + data.ILLEGAL_CITY + "\",\"" + data.PUNISHMENT_UNIT + "\",\"" + data.ILLEGAL_REASON + "\",\"" + data.COMPANY + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>";
}

//格式化输出有效期
function validitydaterowformatter(value,data,index) {
	return data.PLAN_START_TIME + " 至 " + data.PLAN_END_TIME;
}

//调度详情
function scheduledetailformatter(value,data,index) {
	return "<a href='#' onclick='javascript:showScheduleDetail(\"" + data.schedule.SCHEDULE_ID + "\",\"" + data.schedule.SCHEDULE_NAME + "\",\"" + data.schedule.DATETYPE + "\",\"" + data.schedule.DATETYPE_DETAIL + "\",\"" + data.schedule.MAXTIMEITEM + "\",\"" + data.schedule.STARTHOUR1 + "\",\"" + data.schedule.STARTMINUTE1 + "\",\"" + data.schedule.ENDHOUR1 + "\",\"" + data.schedule.ENDMINUTE1 + "\",\"" + data.schedule.STARTHOUR2 + "\",\"" + data.schedule.STARTMINUTE2 + "\",\"" + data.schedule.ENDHOUR2 + "\",\"" + data.schedule.ENDMINUTE2 + "\",\"" + data.schedule.STARTHOUR3 + "\",\"" + data.schedule.STARTMINUTE3 + "\",\"" + data.schedule.ENDHOUR3 + "\",\"" + data.schedule.ENDMINUTE3 + "\",\"" + data.schedule.STARTHOUR4 + "\",\"" + data.schedule.STARTMINUTE4 + "\",\"" + data.schedule.ENDHOUR4 + "\",\"" + data.schedule.ENDMINUTE4 + "\",\"" + data.schedule.STARTHOUR5 + "\",\"" + data.schedule.STARTMINUTE5 + "\",\"" + data.schedule.ENDHOUR5 + "\",\"" + data.schedule.ENDMINUTE5 + "\")'>详情</a>";
}

function rowformatter(value,data,index) {
	return "<a href='#' onclick='javascript:autoCallTaskEdit(\"" + data.TASK_ID + "\",\"" + data.TASK_NAME + "\",\"" + data.CALLERID + "\",\"" + data.PLAN_START_TIME + "\",\"" + data.PLAN_END_TIME + "\",\"" + data.SCHEDULE_ID + "\",\"" + data.SCHEDULE_NAME + "\",\"" + data.TASK_TYPE + "\",\"" + data.COMMON_VOICE_ID + "\",\"" + data.COMMON_VOICE_DESC + "\",\"" + data.QUESTIONNAIRE_ID + "\",\"" + data.QUESTIONNAIRE_DESC + "\",\"" + data.REMINDER_TYPE + "\",\"" + data.START_VOICE_ID + "\",\"" + data.START_VOICE_DESC + "\",\"" + data.END_VOICE_ID + "\",\"" + data.END_VOICE_DESC + "\",\"" + data.BLACKLIST_ID + "\",\"" + data.BLACKLIST_NAME + "\",\"" + data.RETRY_TIMES + "\",\"" + data.RETRY_INTERVAL + "\",\""  + data.INTERVAL_TYPE + "\",\"" + data.PRIORITY + "\",\"" + data.SEND_MESSAGE + "\",\"" + data.MESSAGE_CONTENT + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
	"<a href='#' onclick='javascript:autoCallTaskDel(\"" + data.TASK_ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
}

function disabledAllStateBtn() {
	$("#applyActiveBtn").linkbutton("disable");
	$("#cancelApplyActiveBtn").linkbutton("disable");
	$("#pauseBtn").linkbutton("disable");
	$("#cancelPauseBtn").linkbutton("disable");
	$("#stopBtn").linkbutton("disable");
	$("#historyBtn").linkbutton("disable");
}

//修改任务状态
//action: applyActive(申请激活);  cancelApplyActive(取消激活); pause(暂停任务); cancelPause(重新开始); stop(结束任务); history(归档任务)
function changeState(action) { 

	var node = $("#autoCallTaskDg").datagrid("getSelected");   //取得当前选中的记录
	if(node==null) {
		$.messager.alert('警告','操作失败:请先选择任务','error');
	}  

	var taskId = node.TASK_ID;
	var taskName = node.TASK_NAME;
	var taskState = node.TASK_STATE;

	var notice = "";

	if(action=='applyActive') {
		notice = "确定申请激活【" + taskName + "】 任务吗?";
	}else if(action=='cancelApplyActive') {
		notice = "确定取消激活【" + taskName + "】 任务吗?";
	}else if(action=='pause') {
		notice = "确定暂停【" + taskName + "】 任务吗?";
	}else if(action=='cancelPause') {
		notice = "确定重新开始【" + taskName + "】 任务吗?";
	}else if(action=='stop') {
		notice = "确定停止开始【" + taskName + "】 任务吗?任务停止后,要再次启动任务,必须重新申请激活!";
	}else if(action=='archive') {
		notice = "确定归档【" + taskName + "】 任务吗?任务归档后，任务将不能再次编辑和激活!";
	}

	$.messager.confirm('提示',notice,function(r){
		if(r) {
			$.messager.progress({
				msg:'系统正在处理，请稍候...',
				interval:2000
			});

			$.ajax({
				url:'autoCallTask/changeState?taskId=' + taskId + "&taskState=" + taskState + "&action=" + action,
				method:'POST',
				dataType:'json',
				success:function(rs) {
					$.messager.progress('close');
					var statusCode = rs.statusCode;
					var message = rs.message;

					window.parent.showMessage(message,statusCode);

					//if(statusCode=='success') {
					findData();
					//}
				}
			});
			
		}
	});

}

//得到当前日期
function pad2(n) { return n < 10 ? '0' + n : n }
function getCurrTimeToString() {    //以 yyyyMMddHHiiss 返回
    var date = new Date();
    return date.getFullYear().toString() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());
}

function showExtraTabs(flag) {   //是否显示额外Tab,主要是导入号码和号码列表
	if(flag==1) {    //flag=1 时，显示
		$("#autoCallTaskTabs").tabs('getTab',"导入号码").panel('options').tab.show();
		$("#autoCallTaskTabs").tabs('getTab',"号码列表").panel('options').tab.show();

		//显示列表时,则要将催缴类的相关字段显示做一些控制，针对不同的催缴类型，显示不同的字段
		var currTaskType = $("#TASK_TYPE").combobox('getValue');         //当前任务类型
		var currReminderType = $("#REMINDER_TYPE").combobox('getValue'); //当前催缴类型

		hideAllExtraTh();         //先隐藏所有的号码列表的额外字段

		if(currTaskType=="3") {   //催缴类型

			$("#selectAutoNumberDiv").css("display","none");     //如果是催缴类型时，不能选择号码组的方式添加号码
    		
			if(currReminderType=="1") {         //电费催缴
				
				//额外字段的显示
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ACCOUNT_NUMBER');       //户号
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','CHARGE');               //费用
				
				//表单输入字段显示
				$("#periodDiv").css('display','');
				$("#accountNumberDiv").css('display','');
				$("#addressDiv").css('display','');
				$("#chargeDiv").css('display','');
			}else if(currReminderType=="2") {    //水费催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','DISPLAY_NUMBER');       //表显数量
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','DOSAGE');               //使用量
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','CHARGE');               //费用
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ACCOUNT_NUMBER');       //户号
				
				//表单输入字段显示
				$("#periodDiv").css('display','');
				$("#addressDiv").css('display','');
				$("#displayNumberDiv").css('display','');
				$("#dosageDiv").css('display','');
				$("#chargeDiv").css('display','');
				$("#accountNumberDiv").css('display','');
			}else if(currReminderType=="3") {    //电话费催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ACCOUNT_NUMBER');       //户号
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','CHARGE');               //费用
				
				//表单输入字段显示
				$("#periodDiv").css('display','');
				$("#accountNumberDiv").css('display','');
				$("#addressDiv").css('display','');
				$("#chargeDiv").css('display','');
			}else if(currReminderType=="4") {    //燃气费催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ACCOUNT_NUMBER');       //户号
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','CHARGE');               //费用
				
				//表单输入字段显示
				$("#periodDiv").css('display','');
				$("#accountNumberDiv").css('display','');
				$("#addressDiv").css('display','');
				$("#chargeDiv").css('display','');
			}else if(currReminderType=="5") {    //物业费催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','CHARGE');               //费用
				
				//表单输入字段显示
				$("#periodDiv").css('display','');
				$("#addressDiv").css('display','');
				$("#chargeDiv").css('display','');
			}else if(currReminderType=="6") {    //车辆违章
				//额外字段的显示
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PLATE_NUMBER');         //车牌号码
			    $("#autoCallTaskTelephoneDg").datagrid('showColumn','ILLEGAL_CITY');         //违法城市
			    $("#autoCallTaskTelephoneDg").datagrid('showColumn','PUNISHMENT_UNIT');      //处罚单位
			    $("#autoCallTaskTelephoneDg").datagrid('showColumn','ILLEGAL_REASON');       //违法理由
				
				//表单输入字段显示
				$("#periodDiv").css('display','');
				$("#plateNumberDiv").css('display','');
			    $("#illegalCityDiv").css('display','');
			    $("#punishmentUnitDiv").css('display','');
			    $("#illegalReasonDiv").css('display','');
			}else if(currReminderType=="7") {    //交警移车
				//额外字段的显示
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','CALL_POLICE_TEL');      //报警人电话
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','VEHICLE_TYPE');         //车辆类型
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PLATE_NUMBER');         //车牌号码
				
				//表单输入字段显示
				$("#callPoliceTelDiv").css('display','');
				$("#vehicleTypeDiv").css('display','');
				$("#plateNumberDiv").css('display','');
			}else if(currReminderType=="8") {    //社保催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg").datagrid('showColumn','CHARGE');               //费用
				
				//表单输入字段显示
				$("#periodDiv").css('display','');
				$("#chargeDiv").css('display','');
			}
    		
		}else {
			$("#selectAutoNumberDiv").css("display","");    //如果是非催缴类型时，可以选择号码组的方式添加号码
		}
		
	}else {
		$("#autoCallTaskTabs").tabs('getTab',"导入号码").panel('options').tab.hide();
		$("#autoCallTaskTabs").tabs('getTab',"号码列表").panel('options').tab.hide();
	}
}

function hideAllExtraTh() {   //隐藏所有的号码列表的额外字段（主要是催缴类外呼任务）
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','PERIOD');               //日期
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','DISPLAY_NUMBER');       //表显数量
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','DOSAGE');               //使用量
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','CHARGE');               //费用
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','ACCOUNT_NUMBER');       //户号
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','ADDRESS');              //地址
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','CALL_POLICE_TEL');      //报警人电话
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','VEHICLE_TYPE');         //车辆类型
	$("#autoCallTaskTelephoneDg").datagrid('hideColumn','PLATE_NUMBER');         //车牌号码
    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','ILLEGAL_CITY');         //违法城市
    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','PUNISHMENT_UNIT');      //处罚单位
    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','ILLEGAL_REASON');       //违法理由
    $("#autoCallTaskTelephoneDg").datagrid('hideColumn','COMPANY');              //公司

    //同时，要将添加号码的表单额外输入项全部隐藏
    $("#periodDiv").css('display','none');
    $("#displayNumberDiv").css('display','none');
    $("#dosageDiv").css('display','none');
    $("#chargeDiv").css('display','none');
    $("#accountNumberDiv").css('display','none');
    $("#addressDiv").css('display','none');
    $("#callPoliceTelDiv").css('display','none');
    $("#vehicleTypeDiv").css('display','none');
    $("#plateNumberDiv").css('display','none');
    $("#illegalCityDiv").css('display','none');
    $("#punishmentUnitDiv").css('display','none');
    $("#illegalReasonDiv").css('display','none');
    $("#companyDiv").css('display','none');
}

function showExtraTabsFor2() {   //是否显示额外Tab,主要是导入号码和号码列表
	
		hideAllExtraThFor2();         //先隐藏所有的号码列表的额外字段
		//alert(currTaskType + "," + currReminderType);
		if(currTaskType=="3") {   //催缴类型

			if(currReminderType=="1") {         //电费催缴
				
				//额外字段的显示
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ACCOUNT_NUMBER');       //户号
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','CHARGE');               //费用
				
			}else if(currReminderType=="2") {    //水费催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','DISPLAY_NUMBER');       //表显数量
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','DOSAGE');               //使用量
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','CHARGE');               //费用
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ACCOUNT_NUMBER');       //户号
				
			}else if(currReminderType=="3") {    //电话费催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ACCOUNT_NUMBER');       //户号
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','CHARGE');               //费用
				
			}else if(currReminderType=="4") {    //燃气费催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ACCOUNT_NUMBER');       //户号
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','CHARGE');               //费用
				
			}else if(currReminderType=="5") {    //物业费催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','ADDRESS');              //地址
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','CHARGE');               //费用
				
			}else if(currReminderType=="6") {    //车辆违章
				//额外字段的显示
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PLATE_NUMBER');         //车牌号码
			    $("#autoCallTaskTelephoneDg2").datagrid('showColumn','ILLEGAL_CITY');         //违法城市
			    $("#autoCallTaskTelephoneDg2").datagrid('showColumn','PUNISHMENT_UNIT');      //处罚单位
			    $("#autoCallTaskTelephoneDg2").datagrid('showColumn','ILLEGAL_REASON');       //违法理由
				
			}else if(currReminderType=="7") {    //交警移车
				//额外字段的显示
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','CALL_POLICE_TEL');      //报警人电话
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','VEHICLE_TYPE');         //车辆类型
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PLATE_NUMBER');         //车牌号码
				
			}else if(currReminderType=="8") {    //社保催缴
				//额外字段的显示
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','PERIOD');               //日期
				$("#autoCallTaskTelephoneDg2").datagrid('showColumn','CHARGE');               //费用
				
			}
    		
		}
		
}

function hideAllExtraThFor2() {   //隐藏所有的号码列表的额外字段（主要是催缴类外呼任务）
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','PERIOD');               //日期
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','DISPLAY_NUMBER');       //表显数量
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','DOSAGE');               //使用量
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','CHARGE');               //费用
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','ACCOUNT_NUMBER');       //户号
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','ADDRESS');              //地址
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','CALL_POLICE_TEL');      //报警人电话
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','VEHICLE_TYPE');         //车辆类型
	$("#autoCallTaskTelephoneDg2").datagrid('hideColumn','PLATE_NUMBER');         //车牌号码
    $("#autoCallTaskTelephoneDg2").datagrid('hideColumn','ILLEGAL_CITY');         //违法城市
    $("#autoCallTaskTelephoneDg2").datagrid('hideColumn','PUNISHMENT_UNIT');      //处罚单位
    $("#autoCallTaskTelephoneDg2").datagrid('hideColumn','ILLEGAL_REASON');       //违法理由
    $("#autoCallTaskTelephoneDg2").datagrid('hideColumn','COMPANY');              //公司
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

function selectVoice() {

	execSelectVoice(orgComboTreeData,voiceTypeComboboxDataFor0,'1');
	
	$("#voiceDlg").dialog("setTitle","选择普通语音文件").dialog("open");
	
}

function selectSchedule() {

	execSelectSchedule(dateTypeComboboxDataFor1,"empty");
	
	$("#scheduleDlg").dialog('setTitle',"选择调度计划").dialog("open");
	
}

function selectStartVoice() {

	execSelectVoice(orgComboTreeData,voiceTypeComboboxDataFor0,'3');
	
	$("#voiceDlg").dialog("setTitle","选择开始语音文件").dialog("open");
	
}

function selectEndVoice() {

	execSelectVoice(orgComboTreeData,voiceTypeComboboxDataFor0,'4');
	
	$("#voiceDlg").dialog("setTitle","选择结束语音文件").dialog("open");
	
}

function selectBlackList() {

	execSelectBlackList(orgComboTreeData);
	
	$("#blackListDlg").dialog("setTitle","选择黑名单").dialog("open");
}

function selectAutoNumber() {
	
	execSelectNumber(orgComboTreeData);

	$("#autoNumberDlg").dialog("setTitle","选择号码组").dialog("open");
	
}

function clearStartVoice() {
	$("#START_VOICE_ID").val("");
	$("#START_VOICE_DESC").textbox('setValue','');
}

function clearEndVoice() {
	$("#END_VOICE_ID").val("");
	$("#END_VOICE_DESC").textbox('setValue','');
}

function clearBlackList() {
	$("#BLACKLIST_ID").val("");
	$("#BLACKLIST_NAME").textbox('setValue','');
}

function clearAutoNumber() {
	$("#NUMBER_ID").val("");
	$("#NUMBER_NAME").textbox('setValue','');
}

function selectQuestionnaire() {

	execSelectQuestionnaire(orgComboTreeData);

	$("#questionnaireDlg").dialog('setTitle',"选择调查问卷").dialog('open');
	
}

function cancel() {
	$("#autoCallTaskDlg").dialog("close");
}

//=========单个增加号码操作-======
function autoCallTaskTelephoneAdd() {

	$("#autoCallTaskTelephoneSaveBtn").attr("onclick","autoCallTaskTelephoneSaveAdd()");
	
	$("#autoCallTaskTelephoneDlg").dialog('setTitle','新增号码').dialog('open');
    
}

function autoCallTaskTelephoneEdit(telId,customerTel,customerName,period,displayNumber,dosage,charge,accountNumber,address,callPoliceTel,vehicleType,plateNumber,illegalCity,punishmentUnit,illegalReason,company) {
	/*$("#TEL_ID").val(telId);
	$("#CUSTOMER_TEL").numberbox('setValue',customerTel);
	$("#CUSTOMER_NAME").textbox('setValue',customerName);

	$("#PERIOD").textbox('setValue',period);                         //日期
	$("#DISPLAY_NUMBER").numberbox('setValue',displayNumber);		 //表显数量
	$("#DOSAGE").numberbox('setValue',dosage);						 //使用量
	$("#CHARGE").numberbox('setValue',charge);						 //费用
	$("#ACCOUNT_NUMBER").numberbox('setValue',accountNumber);		 //户号
	$("#ADDRESS").textbox('setValue',address);						 //地址
	$("#CALL_POLICE_TEL").numberbox('setValue',callPoliceTel);		 //报警人电话
	$("#VEHICLE_TYPE").textbox('setValue',vehicleType);				 //车辆型号
	$("#PLATE_NUMBER").textbox('setValue',plateNumber);				 //车牌号码
	$("#ILLEGAL_CITY").textbox('setValue',illegalCity);				 //违法城市
	$("#PUNISHMENT_UNIT").textbox('setValue',punishmentUnit);		 //处罚单位
	$("#ILLEGAL_REASON").textbox('setValue',illegalReason);			 //违法理由
	$("#COMPANY").textbox('setValue',company);						 //公司
	*/
	$("#autoCallTaskTelephoneSaveBtn").attr("onclick","autoCallTaskTelephoneSaveEdit()");

	$("#autoCallTaskTelephoneForm").form('load',{
		'autoCallTaskTelephone.TEL_ID':telId,
		'autoCallTaskTelephone.CUSTOMER_TEL':customerTel,
		'autoCallTaskTelephone.CUSTOMER_NAME':customerName,
		'autoCallTaskTelephone.PERIOD':period,
		'autoCallTaskTelephone.DISPLAY_NUMBER':displayNumber,
		'autoCallTaskTelephone.DOSAGE':dosage,
		'autoCallTaskTelephone.CHARGE':charge,
		'autoCallTaskTelephone.ACCOUNT_NUMBER':accountNumber,
		'autoCallTaskTelephone.ADDRESS':address,
		'autoCallTaskTelephone.CALL_POLICE_TEL':callPoliceTel,
		'autoCallTaskTelephone.VEHICLE_TYPE':vehicleType,
		'autoCallTaskTelephone.PLATE_NUMBER':plateNumber,
		'autoCallTaskTelephone.ILLEGAL_CITY':illegalCity,
		'autoCallTaskTelephone.PUNISHMENT_UNIT':punishmentUnit,
		'autoCallTaskTelephone.ILLEGAL_REASON':illegalReason,
		'autoCallTaskTelephone.COMPANY':company
	});
	
	$("#autoCallTaskTelephoneDlg").dialog('setTitle','修改号码').dialog('open');
	
}


//自动外呼号码的添加保存
function autoCallTaskTelephoneSaveAdd(){

	$("#autoCallTaskTelephoneForm").form('submit',{
		
		url:'autoCallTaskTelephone/add?taskId=' + currTaskId,
		onSubmit:function() {
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
				findDataForTelephone();
				$("#autoCallTaskTelephoneDlg").dialog('close');
			}
		}
		
	});
	
}

//外呼号码的修改保存
function autoCallTaskTelephoneSaveEdit(){

	$("#autoCallTaskTelephoneForm").form('submit',{

		url:'autoCallTaskTelephone/update?taskId=' + currTaskId,
		onSubmit:function() {
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
				findDataForTelephone();
				$("#autoCallTaskTelephoneDlg").dialog('close');
			}
			
		}
		
	});
	
	
}

function autoCallTaskTelephoneDel() {


	$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
		if(r) {
			$.messager.progress({
				msg:'系统正在处理，请稍候...'
			});
			$.ajax({
				url:'autoCallTaskTelephone/delete?&ids=' + getTelephoneSelectedRows(),
				method:'POST',
				dataType:'json',
				success:function(rs) {
					$.messager.progress("close");
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
						findDataForTelephone();					
					}
				}
			});
		}
	});
	
}

//取得选中的号码数据			
function getTelephoneSelectedRows() {
	
	var rows = $('#autoCallTaskTelephoneDg').datagrid('getSelections');
	var ids = [];
	for(var i=0; i<rows.length; i++){
		ids.push(rows[i].TEL_ID);
	}
	return	ids.join(",");			
}

//通过号码组方式上传号码
function uploadPhoneByNumber() {

	var numberId = $("#NUMBER_ID").val();

	if(numberId == null || numberId =='') {
		alert("通过号码组新增号码失败,请选择号码组后再执行上传号码!");
		return;
	}

	$.messager.progress({
		msg:'系统正在处理，请稍候...'
	});
	$.ajax({
		
		url:'autoCallTaskTelephone/addByAutoNumber?taskId=' + currTaskId + "&numberId=" + numberId,
		method:'POST',
		dataType:'json',

		success:function (rs) {
		
			$.messager.progress("close");
			
			var statusCode = rs.statusCode; //返回的结果类型
			var message = rs.message;       //返回执行的信息
			
			window.parent.showMessage(message,statusCode);
			
			if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
				findDataForTelephone();					

				$("#NUMBER_ID").val("");
				$("#NUMBER_NAME").textbox("setValue","");
				
			}
		
		}
		
		
	});
	
	
}

//====================================
//创建语音
function createCommonVoice() {

	var data = [{"id":1,"text":"普通语音"}];

	$("#VOICE_DESC").textbox('setValue',getCurrTimeToString());
	
	$("#VOICE_TYPE").combobox({
		valueField:'id',
		textField:'text'
	}).combobox("loadData",data).combobox("setValue","1");
	
	$("#voiceFormDlg").dialog("setTitle","创建普通语音").dialog("open");
	
}

function createStartVoice() {

	var data = [{"id":3,"text":"开始语音"}];

	$("#VOICE_DESC").textbox('setValue',getCurrTimeToString());
	
	$("#VOICE_TYPE").combobox({
		valueField:'id',
		textField:'text'
	}).combobox("loadData",data).combobox("setValue","3");
	
	$("#voiceFormDlg").dialog("setTitle","创建开始语音").dialog("open");
	
}

function createEndVoice() {

	var data = [{"id":4,"text":"结束语音"}];

	$("#VOICE_DESC").textbox('setValue',getCurrTimeToString());
	
	$("#VOICE_TYPE").combobox({
		valueField:'id',
		textField:'text'
	}).combobox("loadData",data).combobox("setValue","4");
	
	$("#voiceFormDlg").dialog("setTitle","创建结束语音").dialog("open");
	
}

function voiceCancel() {

	$("#voiceFormDlg").dialog("close");
	
}

function saveVoiceAdd() {

	var voiceType = $("#VOICE_TYPE").combobox("getValue");

	var f = $("#voiceFile").filebox("getValue");
	var vd = $("#VOICE_DESC").textbox("getValue");

	var ttsContent = $("#ttsContent").val();

	ttsContent = encodeURI(encodeURI(ttsContent));
	var urlInfo = 'voice/add';

	if(currCreateType == 'voiceFile') {
		if(f==null || f.length==0){
			$.messager.alert("警告","请选择语音文件,再执行上传!","error");
			return;
		}
	}else {
		if(ttsContent==null || ttsContent=='') {
			$.messager.alert("警告","创建类型为TTS方式,TTS内容为空!","error");
			return;
		}

		//为了避免上传文件的框中有内容，在上传前，将文件框清空
		$("#voiceFile").filebox('clear');
		
		urlInfo = 'voice/addForTTS?ttsContent=' + ttsContent; 
		
	}

	if(vd==null || vd.length==0){
		$.messager.alert("警告","语音文件描述不能为空!","error");
		return;
	}

	$("#voiceForm").form("submit",{

		url:urlInfo,
		onSubmit:function() {
			$.messager.progress({
				msg:'系统正在处理，请稍候...',
				interval:3000
			});
			return $(this).form('validate');
		},
		success:function(data) {
			$.messager.progress('close');
			var result = JSON.parse(data); //解析Json数据

			var statusCode = result.statusCode; //返回的结果类型
			var message = result.message;       //返回执行的信息
			var voiceId = result.extraMessage;  //收集返回的语音ID信息

			window.parent.showMessage(message,statusCode);

			if(statusCode == 'success') {         //保存成功时

				if(voiceType == '1') {
					$("#COMMON_VOICE_ID").val(voiceId);
					$("#COMMON_VOICE_DESC").textbox("setValue",vd);
				}else if(voiceType == '3') {
					$("#START_VOICE_ID").val(voiceId);
					$("#START_VOICE_DESC").textbox("setValue",vd);
				}else if(voiceType == '4') {
					$("#END_VOICE_ID").val(voiceId);
					$("#END_VOICE_DESC").textbox("setValue",vd);
				}
				
				$("#voiceFormDlg").dialog("close");
			}
			
		}
		
	});
	
}

function setMessageContentValue() {
	
	var taskType = $("#TASK_TYPE").combobox('getValue');
	var reminderType = $("#REMINDER_TYPE").combobox("getValue");
	
	if(taskType == 1) {    //如果任务类型为1，普通外呼（通知类），则需要给一个大概模板
		$("#MESSAGE_CONTENT").textbox({prompt:'普通外呼短信内容,自定义，与播报语音内容一致即可!'}).textbox('textbox').attr('readonly',false);
	}else if(taskType == 2) {
		$("#MESSAGE_CONTENT").textbox('setValue','');
		$('#messageContentTr').css("display","none");
	}else if(taskType==3){    //如果是催缴类外呼
		$.ajax({
			url:'autoCallTask/getMssageContentTemplate?reminderType=' + reminderType,
			method:'POST',
			dataType:'json',
			success:function(rs) {
				$.messager.progress('close');
				var statusCode = rs.statusCode;   
				var message = rs.message;
				if(statusCode=='success') {
					$('#messageContentTr').css("display","");
					$("#MESSAGE_CONTENT").textbox("setValue","短信内容样例如下（系统定义，无需填写）：" + message).textbox('textbox').attr('readonly',true);
				}
				
			}	
		});
	}
	
}





