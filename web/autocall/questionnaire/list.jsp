<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>Client Side Pagination in DataGrid - jQuery EasyUI Demo</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		var currQuestionnaireId = null;   //当前的问卷ID
		var currQuestionId = null;        //当前的问题ID
		var currCreateType = 'voiceFile';

		var orgComboTreeData = eval('${orgComboTreeData}');
		var voiceTypeComboboxDataFor0 = eval('${voiceTypeComboboxDataFor0}');
		
		$(function(){

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

			$("#voiceFile").filebox({
				buttonText:'选择文件'
			});

			//弹窗关闭时，清空表单
			$("#voiceFormDlg").dialog({
				onClose:function() {
					$("#voiceForm").form('clear');

					//除了清空表单，还需要把创建类型复位，即是选择文件打开，TTS方式关闭
					$("#createType_voiceFile").linkbutton('select');  
					$("#voiceFileDiv").css('display','');
					$("#ttsDiv").css('display','none');
					currCreateType = 'voiceFile';
				}
			});
			

			//$("#startTime").datebox("setValue",getCurrMonthDay1());
			$("#endTime").datebox("setValue",getCurrDate());

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
	
					$("#questionnaireDg").datagrid({
						pageSize:15,
						pagination:true,
						fit:true,
						toolbar:'#searchtool',
						singleSelect:true,
						rownumbers:true,
						rowrap:true,
						striped:true,
						pageList:[10,15,20],
						url:'questionnaire/datagrid',
						queryParams:{
							questionnaireDesc:$("#questionnaireDesc").val(),
							orgCode:orgCodes,
							startTime:startTime,
							endTime:endTime
						}
					});
				
				}
				
			});

			//问题列表出来
			$("#questionDg").datagrid({
				url:'question/datagrid',
				pageSize:10,
				pagination:true,
				fit:true,
				singleSelect:true,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[5,10],
				toolbar:'#questiontool',
				queryParams:{
					questionnaireId:currQuestionnaireId
				},
				onLoadSuccess:function(data) {
					for(var i=0;i<data.rows.length;i++) {
						eval(data.rows[i].playerFunction);    //播放器设置语音
					}
				}
			});

			//选项列表
			$("#questionItemDg").datagrid({
				url:'questionItem/datagrid',
				toolbar:'#questionitemtool',
				pageSize:10,
				pagination:true,
				fit:true,
				singleSelect:false,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[5,10]
			});

			
			//问题列表出来
			//$("#questionDg").datagrid({
			/*$("#questionDg").datagrid({
				url:'question/datagrid',
				pageSize:10,
				pagination:true,
				fit:true,
				singleSelect:true,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[5,10],
				toolbar:'#questiontool',
				queryParams:{
					questionnaireId:currQuestionnaireId
				},
				onLoadSuccess:function(data) {
					for(var i=0;i<data.rows.length;i++) {
						eval(data.rows[i].playerFunction);    //播放器设置语音
					}
				}
			}).datagrid('loadData',{total:0,rows:[]});*/

			//选项列表
			/*$("#questionItemDg").datagrid({
				url:'questionItem/datagrid',
				toolbar:'#questionitemtool',
				pageSize:10,
				pagination:true,
				fit:true,
				singleSelect:false,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[5,10]
			}).datagrid('loadData',{total:0,rows:[]});*/

			//问卷弹窗关闭事件时
			$("#questionnaireDlg").dialog({
				onClose:function() {
					currQuestionnaireId = null;
					$("#questionAddBtn").linkbutton("disable");   //添加问题按钮不可用
					$("#questionnaireForm").form('clear');
					$("#questionDg").datagrid('loadData',{total:0,rows:[]});
				}
			});

			//问题弹窗关闭事件时
			$("#questionDlg").dialog({
				onClose:function() {

					currQuestionId = null;                           //当前问题清空
					$("#questionItemAddBtn").linkbutton('disable');  //添加问题选项按钮不可用
					$("#questionForm").form('clear');                //问题表单清空
					$("#questionItemDg").datagrid('loadData',{total:0,rows:[]});    //问题选项列表清空
				
				}
			});

			//问题选项弹窗关闭事件
			$("#questionItemDlg").dialog({
				onClose:function() {
					$("#ITEM_CODE").numberbox('enable');
					$("#questionItemForm").form('clear');
				}
			});

			//定义语音弹出选择框双击时赋值动作
        	$("#voiceDg").datagrid({
        		//onClickRow:function(index,row) {
        			//alert("用户选择了选项，选项值为:" + index);s
        		//}
        		onDblClickRow:function(index,row) {
        			//alert("用户选择了选项，选项值为:index=" + index + ",row['VOICE_ID']=" + row['VOICE_ID'] + ",row['VOICE_DESC']=" + row['VOICE_DESC']);
        			$("#QUESTION_VOICE_ID").val(row['VOICE_ID']);
        			$("#QUESTION_VOICE_DESC").textbox('setValue',row['VOICE_DESC']);
					
        			$("#voiceDlg").dialog("close");
        		}
            });

			$("#ITEM_CODE").numberbox("textbox").attr('maxlength',1);   //限定响应按键最多输入1位数字
			$('#questionAddBtn').linkbutton('disable');                 //添加问题的按钮默认不可用
			$('#questionItemAddBtn').linkbutton('disable');             //添加选项的按钮默认不可用

			$("#voiceType").combobox('disable');
            
		});

		//刷新问卷预览
		function refreshPreview() {

			$.ajax({

				type:'POST',
				dataType:'json',
				url:'questionnaire/preview?questionnaireId=' + currQuestionnaireId,
				success:function(rs) {

					var statusCode = rs.statusCode;
					var message = rs.message;

					if(statusCode = 'success') {
						$("#questionnairePreview").html(message);
					}else {
						window.parent.showMessage(message,statusCode);
					}
				
				}
				
			});
			
		}

		function findData() {
			var selectRs = $("#orgCode").combotree('getValues');
			var orgCodes = selectRs.toString();
			var startTime = $("#startTime").datebox('getValue');
			var endTime = $("#endTime").datebox('getValue');

			$("#questionnaireDg").datagrid('load',{
				questionnaireDesc:$("#questionnaireDesc").val(),
				orgCode:orgCodes,
				startTime:startTime,
				endTime:endTime
			});
			
		}

		function questionnaireDel(questionnaireId) {

			$.messager.confirm("提示","你确定要删除选中的记录吗?",function(r){

				if(r) {
					$.ajax({

						type:'POST',
						dataType:'json',
						url:'questionnaire/delete?questionnaireId=' + questionnaireId,
						success:function(rs) {

							var statusCode = rs.statusCode;     //返回结果类型
							var message = rs.message;           //返回处理信息

							window.parent.showMessage(message,statusCode);

							if(statusCode == 'success') {
								delInit();
							}
						
						}
						
					});
					
				}
				
			});
			
		}

		function questionnaireAdd() {

			$("#saveBtn").attr("onclick","saveAdd()");

			refreshPreview();   //重新刷新问卷预览
			
			$("#questionnaireDlg").dialog('setTitle',"添加问卷").dialog("open");
		}
		
		function saveAdd() {

			$("#questionnaireForm").form('submit',{

				url:'questionnaire/add',
				onSubmit:function() {
					$.messager.progress({
						msg:'系统正在处理，请稍候...',
						interval:3000
					});
					return $(this).form('validate');
				},
				success:function(data) {
					$.messager.progress('close');

					var result = JSON.parse(data);  //解析json数据

					var statusCode = result.statusCode;   //返回结果类型
					var message = result.message;         //返回执行的结果信息

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {         //如果保存成功时，执行的操作
						currQuestionnaireId = result.extraMessage;   //额外信息储存的是返回的ID 
						addInit();
					}
					
				}
				
			});
			
		}

		//问卷编辑
		function questionnaireEdit(questionnaireId,questionnaireDesc) {

			$("#saveBtn").attr("onclick","saveEdit()");
			
			$("#questionnaireForm").form('load',{
				'questionnaire.QUESTIONNAIRE_ID':questionnaireId,
				'questionnaire.QUESTIONNAIRE_DESC':questionnaireDesc
			});

			currQuestionnaireId = questionnaireId;
			$('#questionAddBtn').linkbutton('enable');                 //添加问题的按钮默认可用
			
			$("#questionDg").datagrid('load',{
				questionnaireId:currQuestionnaireId
			});

			refreshPreview();   //重新刷新问卷预览
			
			$("#questionnaireDlg").dialog('setTitle',"编辑问卷 " + $("#QUESTIONNAIRE_ID").val()).dialog("open");
			
		}

		function saveEdit() {

			$("#questionnaireForm").form('submit',{
				url:'questionnaire/update',
				onSubmit:function() {
					$.messager.progress({
						msg:'系统正在处理,请稍候...',
						interval:3000
					});
					return $(this).form('validate');
				},
				success:function(data) {

					$.messager.progress("close");

					var result = JSON.parse(data);   //解析返回的处理结果

					var statusCode = result.statusCode;      //结果类型
					var message = result.message;            //结果信息

					window.parent.showMessage(message,statusCode);

					if(statusCode == "success") {
						editInit();
					}
					
				}
			});
			
		}
		
		function rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:questionnaireEdit(\"" + data.QUESTIONNAIRE_ID + "\",\"" + data.QUESTIONNAIRE_DESC + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:questionnaireDel(\"" + data.QUESTIONNAIRE_ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}

		function question_rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:questionEdit(\"" + data.QUESTION_ID + "\",\"" + data.QUESTION_DESC + "\",\"" + data.VOICE_ID + "\",\"" + data.VOICE_DESC + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:questionDel(\"" + data.QUESTION_ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}

		//试听
		function listenrowformatter(value,data,index) {
			return data.playerSkin;
		}

		//下载
		function downloadrowformatter(value,data,index) {
			return "<a href='voice/download?path=" + data.path + "' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='下载录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/download.png' style='margin-top:2px;' border='0'></a>";
		}

		function item_rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:questionItemEdit(\"" + data.QUESTION_ID + "\",\"" + data.ITEM_CODE + "\",\"" + data.ITEM_DESC + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>";
		}


		function delInit() {
			var selectRs = $("#orgCode").combotree('getValues');
			var orgCodes = selectRs.toString();
			$("#questionnaireDg").datagrid('load',{
				questionnaireDesc:$("#questionnaireDesc").val(),
				orgCode:orgCodes
			});
			
		}

		//添加问卷成功时,重置相关信息
		function addInit() {

			//1 重新加载,问卷列表
			var selectRs = $("#orgCode").combotree('getValues');
			var orgCodes = selectRs.toString();
			$("#questionnaireDg").datagrid('load',{
				questionnaireDesc:$("#questionnaireDesc").val(),
				orgCode:orgCodes
			});

			//2 解开添加问题按钮的 disable
			$("#questionAddBtn").linkbutton('enable');

			//3 修改保存按钮时，为编辑保存
			$("#QUESTIONNAIRE_ID").val(currQuestionnaireId);
			$("#saveBtn").attr("onclick","saveEdit()");
			
			//$("#questionnaireForm").form('clear');
			//$("#questionnaireDlg").dialog('close');
			refreshPreview();   //重新刷新问卷预览
			
		}

		//问卷编辑保存成功执行的操作
		function editInit() {

			//重新加载问卷列表		
			var selectRs = $("#orgCode").combotree('getValues');
			var orgCodes = selectRs.toString();
			$("#questionnaireDg").datagrid('load',{
				questionnaireDesc:$("#questionnaireDesc").val(),
				orgCode:orgCodes
			});

			refreshPreview();   //重新刷新问卷预览

		}

		//问题添加成功时，执行的操作
		function questionAddInit() {

			//问题选项按钮变为可用
			$('#questionItemAddBtn').linkbutton('enable');

			//重新加载问题列表
			$("#questionDg").datagrid('load',{
				questionnaireId:currQuestionnaireId
			});

			//问题表单信息，如果再点击保存按钮时，转为保存编辑,同时将 questionId 赋值
			$("#QUESTION_ID").val(currQuestionId);
			$("#questionSaveBtn").attr("onclick","questionSaveEdit()");

			//问题弹框的标题修改
			$("#questionDlg").dialog('setTitle',"编辑问题");

			refreshPreview();   //重新刷新问卷预览
						
		}

		function questionEditInit() {

			//重新加载问题列表
			$("#questionDg").datagrid('load',{
				questionnaireId:currQuestionnaireId
			});

			refreshPreview();   //重新刷新问卷预览
			
		}

		function questionDelInit() {

			//重新加载问题列表
			$("#questionDg").datagrid('load',{
				questionnaireId:currQuestionnaireId
			});

			refreshPreview();   //重新刷新问卷预览
		}
		
		//问题选项添加成功后操作
		function questionItemAddInit() {

			$("#questionItemDg").datagrid('load',{
				questionId:currQuestionId
			});
			
			$("#questionItemForm").form('clear');
			$("#questionItemDlg").dialog('close');

			refreshPreview();   //重新刷新问卷预览
		}


		//问题选项删除成功之后操作
		function questionItemDelInit() {

			$("#questionItemDg").datagrid('load',{
				questionId:currQuestionId
			});

			refreshPreview();   //重新刷新问卷预览
			
			
		}

		

		//初值化数据
		//1 重新加载问卷列表
		//2 消除表单的数据
		//3 关闭弹出框
		//flag: 1：重新加载问卷数据；0：无须加载问卷数据
		function initial(flag) {

			//1 重新加载问卷列表
			var selectRs = $("#orgCode").combotree('getValues');
			var orgCodes = selectRs.toString();
			if(flag==1) {
				$("#questionnaireDg").datagrid('load',{
					questionnaireDesc:$("#questionnaireDesc").val(),
					orgCode:orgCodes
				});
			}

			//把当前的问卷ID，清空
			currQuestionnaireId = null;

			//2 消除表单的数据
			$("#questionnaireForm").form('clear');

			//3 关闭弹出框
			$("#questionnaireDlg").dialog("close");
		}

		//删除问题
		function questionDel(questionId) {

			$.messager.confirm('提示','您确定要删除选中的记录吗? 删除问题,问题选项将会一并删除',function(r){

				if(r) {

					$.ajax({

						type:'POST',
						dataType:'json',
						url:'question/delete?questionId=' + questionId,
						success:function(rs) {

							var statusCode = rs.statusCode;     //返回结果类型
							var message = rs.message;           //返回处理信息
	
							window.parent.showMessage(message,statusCode);
	
							if(statusCode == 'success') {
								questionDelInit();
							}
						
						}
						
					});
					
				}
				
			});
			
			
		}

		//添加问题
		function questionAdd() {

			//$("#questionDlg").dialog("增加问题").dialog("open");
			$("#questionDlg").dialog('setTitle',"新增问题").dialog('open');
			$("#questionSaveBtn").attr('onclick',"questionSaveAdd()");

		}

		function questionSaveAdd() {

			if(currQuestionnaireId == null) {
				window.parent.showMessage("无法新增问题,问卷ID为空","error");
				return;
			}

			if($("#QUESTION_DESC").val() == "") {
				window.parent.showMessage("无法新增问题,题目内容为空","error");
				return;
			}
			
			if($("#QUESTION_VOICE_ID").val()=='') {
				window.parent.showMessage("无法新增问题,语音文件不能为空","error");
				return;
			}  
			
			$("#questionForm").form('submit',{

				url:'question/add?questionnaireId='+currQuestionnaireId,
				onSubmit:function() {

					$.messager.progress({
						msg:'系统正在处理,请稍候...',
						interval:3000
					});

					return $(this).form('validate');
				},
				success:function(data) {
					$.messager.progress('close');

					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {

						currQuestionId = result.extraMessage;
						
						questionAddInit();
					}
				}
				
			});
			
		}

		//问题编辑
		function questionEdit(questionId,questionDesc,voiceId,voiceDesc) {

			//alert("questionId:" + questionId + ",questionDesc:" + questionDesc + ",voiceId:" + voiceId + ",voiceDesc:" + voiceDesc);
			//return;
			
			$("#questionSaveBtn").attr("onclick","questionSaveEdit()");
			
			$("#questionForm").form('load',{
				'question.QUESTION_ID':questionId,
				'question.QUESTION_DESC':questionDesc,
				'question.VOICE_ID':voiceId,
				'question.VOICE_DESC':voiceDesc
			});

			
			currQuestionId = questionId;      //设置当前问题Id
			$("#questionItemAddBtn").linkbutton("enable");    //添加问题选项按钮可用

			//问题选项列表
			$("#questionItemDg").datagrid('load',{
				questionId:currQuestionId
			});
			
			$("#questionDlg").dialog('setTitle','编辑问题').dialog('open');
			
						
			
			
		}
		
		function questionSaveEdit() {

			if($("#QUESTION_ID").val()=="") {
				window.parent.showMessage("无法修改问题,问题ID为空","error");
				return;
			}

			if($("#QUESTION_DESC").val()=="") {
				window.parent.showMessage("无法修改问题,问题内容为空","error");
				return;
			}

			if($("#QUESTION_VOICE_ID").val()=='') {
				window.parent.showMessage("无法新增问题,语音文件不能为空","error");
				return;
			}
			

			$("#questionForm").form('submit',{

				url:'question/update',
				onSubmit:function() {
					$.messager.progress({
						msg:'系统正在处理,请稍候...',
						interval:3000
					});
					return $(this).form('validate');
				},
				success:function(data) {

					$.messager.progress("close");

					var result = JSON.parse(data);   //解析返回的处理结果

					var statusCode = result.statusCode;      //结果类型
					var message = result.message;            //结果信息

					window.parent.showMessage(message,statusCode);

					if(statusCode == "success") {
						questionEditInit();
					}
					
				}
				
			})
			 
			
		}

		function questionItemDel() {
				
			if(getQuestionItemSelectedRows().length==0) {
				window.parent.showMessage("请选中记录再删除!","error");
				return;
			}

			if(currQuestionId == null || currQuestionId == '') {
				window.parent.showMessage("删除失败,问题ID不能为空!","error");
				return;
			}

			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){

				if(r) {
					$.messager.progress({
						msg:'系统正在处理,请稍候...',
						interval:3000
					});

					$.ajax({

						url:'questionItem/delete?ids=' + getQuestionItemSelectedRows() + "&questionId=" + currQuestionId,
						method:'POST',
						dataType:'json',
						success:function(rs) {

							$.messager.progress('close');

							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								questionItemDelInit();
							}
						
						}
						
					});
					
				}
				
			});
			
		}

		//取得选中的问题选项选中的记录			
		function getQuestionItemSelectedRows() {
			
			var rows = $('#questionItemDg').datagrid('getSelections');
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].ITEM_CODE);
			}
			return	ids.join(",");			
		}

		//问题选项编辑
		function questionItemEdit(questionId,itemCode,itemDesc) {
			
			$("#questionItemForm").form('load',{
				'questionItem.ITEM_CODE':itemCode,
				'questionItem.ITEM_DESC':itemDesc
			});

			$("#ITEM_CODE").numberbox('readonly');
			$("#questionItemSaveBtn").attr('onclick',"questionItemSaveEdit()");
			$("#questionItemDlg").dialog('setTitle',"选项编辑").dialog("open");
			
		}

		function questionItemSaveEdit() {

			if(currQuestionId == null || currQuestionId =='') {
				window.parent.showMessage("修改失败,问题ID不能为空!","error");
				return;
			}

			if($("#ITEM_CODE").val()==null || $("#ITEM_CODE").val()=='') {
				window.parent.showMessage("修改失败,响应按键不能为空!","error");
				return;
			}
			
			if($("#ITEM_DESC").val()==null || $("#ITEM_DESC").val()=='') {
				window.parent.showMessage("修改失败,选项内容不能为空!","error");
				return;
			}

			$("#questionItemForm").form('submit',{

				url:'questionItem/update?questionId=' + currQuestionId,
				onSubmit:function() {

					$.messager.progress({
						msg:'系统正在处理,请稍候...',
						interval:3000
					});
	
					return $(this).form('validate');
				
				},
				success:function(data) {

					$.messager.progress('close');

					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {
						questionItemAddInit();
					}
					
				}
				
			});
			
		}

		//得到当前日期
		function pad2(n) { return n < 10 ? '0' + n : n }
		function getCurrTimeToString() {    //以 yyyyMMddHHiiss 返回
		    var date = new Date();
		    return date.getFullYear().toString() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());
		}
		
		//添加问题选项
		function questionItemAdd() {

			$("#questionItemSaveBtn").attr('onclick',"questionItemSaveAdd()");
			$("#questionItemDlg").dialog('setTitle',"新增选项").dialog('open');
			
		}

		function questionItemSaveAdd() {

			//alert("ITEM_CODE:" + $("#ITEM_CODE").val() + ",ITEM_DESC:" + $("#ITEM_DESC").val());
			
			if(currQuestionId == null || currQuestionId =='') {
				window.parent.showMessage("新增失败,问题ID不能为空!","error");
				return;
			}

			if($("#ITEM_CODE").val()==null || $("#ITEM_CODE").val()=='') {
				window.parent.showMessage("新增失败,响应按键不能为空!","error");
				return;
			}
			
			if($("#ITEM_DESC").val()==null || $("#ITEM_DESC").val()=='') {
				window.parent.showMessage("新增失败,选项内容不能为空!","error");
				return;
			}
			
			$("#questionItemForm").form('submit',{
		
				url:'questionItem/add?questionId=' + currQuestionId,
				onSubmit:function() {

					$.messager.progress({
						msg:'系统正在处理,请稍候...',
						interval:3000
					});

					return $(this).form('validate');
				},
				success:function(data) {
					$.messager.progress('close');

					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {
						questionItemAddInit();
					}
				}
				
			});
			
		}

		//语音选择
		function selectVoice() {

			execSelectVoice(orgComboTreeData,voiceTypeComboboxDataFor0,'2');   //设置问题类型为2,即是类型为问题语音
			
			$("#voiceDlg").dialog("setTitle","选择语音文件").dialog("open");
		}

		//创建语音
		function createVoice() {

			var data = [{"id":2,"text":"问题语音"}];

			$("#VOICE_DESC").textbox('setValue',getCurrTimeToString());
			
			$("#VOICE_TYPE").combobox({
				valueField:'id',
				textField:'text'
			}).combobox("loadData",data).combobox("setValue","2");
			
			$("#voiceFormDlg").dialog("setTitle","创建语音文件").dialog("open");
			
		}

		function voiceCancel() {

			$("#voiceFormDlg").dialog("close");
			
		}

		function saveVoiceAdd() {

			//alert("保存语音!");
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
						//$('#voiceDg').datagrid({url:'voice/datagrid'});

						$("#QUESTION_VOICE_ID").val(voiceId);
						$("#QUESTION_VOICE_DESC").textbox("setValue",vd);
						
						$("#voiceFormDlg").dialog("close");
					}
					
				}
				
			});
			
		}

		
	</script>
	
</head>
<body>
<%@ include file="/base_loading.jsp" %>
<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:45px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>问卷描述：<input id="questionnaireDesc" type="text" class="easyui-textbox" style="width:200px;"/></td>
				<td>
					<div style="padding-left:30px;">
						选择组织：<select id="orgCode" class="easyui-combotree" multiple style="width:200px;"></select>
					</div>
				</td>
				<td>
					<div style="padding-left:30px;">
						创建时间：<input id="startTime" width="30" name="startTime" class="easyui-datebox" /> 至 <input id="endTime" width="30" name="endTime" class="easyui-datebox" />
					</div>
				</td>
				<td>
					<div style="padding-left:30px;">
						<a href="javascript:findData()" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
					</div>
				</td>
			</tr>
		</table>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="questionnaireDg">
			<thead>
				<tr style="height:12px;">
							
					<th data-options="field:'QUESTIONNAIRE_DESC',width:400,align:'center'">问卷标题</th>
					<th data-options="field:'QUESTION_COUNT',width:100,align:'center'">问题数量</th>
					<th data-options="field:'CREATE_USERCODE_DESC',width:200,align:'center'">创建人</th>
					<th data-options="field:'ORG_CODE_DESC',width:200,align:'center'">部门(组织)名字</th>
					<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
					<th data-options="field:'id',width:200,align:'center',formatter:rowformatter">操作</th>
				</tr>
			</thead>
		</table>	
	</div>
</div>


<div id="searchtool" style="padding:2px;">
	<a href="#" id="easyui-add" onclick="questionnaireAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">添加问卷</a>
</div>

<div id="questiontool" style="padding:2px;">
	<a href="#" id="questionAddBtn" onclick="questionAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">添加问题</a>
</div>

<div id="questionitemtool" style="padding:2px;">
	<a href="#" id="questionItemAddBtn" onclick="questionItemAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">添加问题项</a>
	<a href="#" id="questionItemDelBtn" onclick="questionItemDel()" class="easyui-linkbutton" iconCls='icon-cancel' plain="true">删除问题项</a>
</div>

<div id="questionnaireDlg" class="easyui-dialog" data-options="title:'添加问卷'" style="width:1200px;height:600px;padding:2px;" modal="true" closed="true" buttons="#addQuestionnaireBtn">
		<!-- 包含问卷的表单 -->
		<%@ include file="/autocall/questionnaire/_form.jsp" %>
</div>

<!-- 包含问题的表单弹窗 -->
<div id="questionDlg" class="easyui-dialog" data-options="title:'新增问题'" style="width:1000px;height:500px;padding:2px;" modal="true" closed="true" buttons="#addQuestionBtn"> 
		<!-- 包含问卷的表单 -->
		<%@ include file="/autocall/questionnaire/_questionform.jsp" %>

</div>

<div id="questionItemDlg" class="easyui-dialog" data-options="title:'新增选项'" style="width:350px;height:150px;padding:10px;" modal="true" closed="true">
	<%@ include file="/autocall/questionnaire/_questionitemform.jsp" %>
</div>

<!-- 语音选择弹窗 -->
<div id="voiceDlg" class="easyui-dialog" style="width:750px;height:400px;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/voice/selectlist.jsp" %>
</div>


<!-- 语音创建弹窗 -->
<div id="voiceFormDlg" class="easyui-dialog" style="width:750px;height:400px;padding:5px;" modal="true" closed="true">
	 <%@ include file="/autocall/voice/_form.jsp" %>
</div>



</body>
</html>