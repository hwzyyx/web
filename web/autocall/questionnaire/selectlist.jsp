<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script type="text/javascript">
	var currQuestionnaireId = null;   //当前的问卷ID
	var currQuestionId = null;        //当前的问题ID

	function execSelectQuestionnaire(orgCombotreeData) {

		//$("#questionnaire_startTime").datebox("setValue",getCurrMonthDay1());   //设置默认的开始时间
		$("#questionnaire_endTime").datebox("setValue",getCurrDate());          //设置默认的结束时间

		$("#questionnaire_orgCode").combotree('loadData',orgCombotreeData).combotree({

			onLoadSuccess:function(node,data) {

				//全选
				var t = $("#questionnaire_orgCode").combotree("tree");
	
				for(var i=0;i<data.length;i++) {
					node = t.tree("find",data[i].id);
					t.tree('check',node.target);
				}

				var selectRs = $("#questionnaire_orgCode").combotree('getValues');
				var orgCodes = selectRs.toString();
				var startTime = $("#questionnaire_startTime").datebox('getValue');
				var endTime = $("#questionnaire_endTime").datebox('getValue');

				$("#questionnaireDg").datagrid({
					pageSize:15,
					pagination:true,
					fit:true,
					toolbar:'#questionnaire_searchtool',
					singleSelect:true,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[10,15,20],
					url:'questionnaire/datagrid',
					queryParams:{
						questionnaireDesc:$("#questionnaire_questionnaireDesc").val(),
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

		//问卷预览弹窗关闭事件时
		$("#questionnairePreviewDlg").dialog({
			onClose:function() {
				currQuestionnaireId = null;
				$("#questionDg").datagrid('loadData',{total:0,rows:[]});
			}
		});

		
	}
	
	function preview(questionnaireId) {

		//alert("aaaaa");
		$("#questionnairePreviewDlg").dialog('setTitle','问卷预览').dialog('open');

		currQuestionnaireId = questionnaireId;

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

		$("#questionDg").datagrid('load',{
			questionnaireId:currQuestionnaireId
		});
		
	}
	
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

	function questionnaire_findData() {
		//alert('aaaaaaaaaddddddddd');

		var selectRs = $("#questionnaire_orgCode").combotree('getValues');
		var orgCodes = selectRs.toString();
		var startTime = $("#questionnaire_startTime").datebox('getValue');
		var endTime = $("#questionnaire_endTime").datebox('getValue');

		$("#questionnaireDg").datagrid('load',{
			questionnaireDesc:$("#questionnaire_questionnaireDesc").val(),
			orgCode:orgCodes,
			startTime:startTime,
			endTime:endTime
		});
	}

	function preview_rowformatter(value,data,index) {
		return "<a href='#' onclick='javascript:preview(\"" + data.QUESTIONNAIRE_ID +"\")'><img src='themes/icons/search.png' border='0'>预览</a>";
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

</script>

<table id="questionnaireDg">
	<thead>
	
		<tr style="height:12px;">		
			<th data-options="field:'QUESTIONNAIRE_DESC',width:230,align:'center'">问卷标题</th>
			<th data-options="field:'QUESTION_COUNT',width:80,align:'center'">问题数量</th>
			<th data-options="field:'CREATE_USERCODE_DESC',width:150,align:'center'">创建人</th>
			<th data-options="field:'ORG_CODE_DESC',width:150,align:'center'">部门(组织)名字</th>
			<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
			<th data-options="field:'id',width:60,align:'center',formatter:preview_rowformatter">操作</th>
		</tr>
		
	</thead>
</table>
 

<div id="questionnaire_searchtool" style="padding:5px;">
	<table>
		<tr style="vertical-align: top;">
			<td>问卷描述：<input id="questionnaire_questionnaireDesc" type="text" class="easyui-textbox" style="width:310px;"/></td>
			<td>
				<div style="padding-left:30px;">
					选择组织：<select id="questionnaire_orgCode" class="easyui-combotree" multiple style="width:200px;"></select>
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div style="">
					创建时间：<input id="questionnaire_startTime" width="30" class="easyui-datebox" /> 至 <input id="questionnaire_endTime" width="30" class="easyui-datebox" />
				</div>
			</td>
			<td>
				<div style="padding-left:90px;">
					<a onclick="questionnaire_findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
				</div>
			</td>
		</tr>
	</table>
</div>

<!-- 问卷预览框 -->
<div id="questionnairePreviewDlg" class="easyui-dialog" style="width:900px;height:450px;padding:5px;" modal="true" closed="true">
	
	<div class="easyui-layout" data-options="fit:true"  style="width:100%;height:400px;padding:1px;">
	
		<!-- 左边的问卷预览 -->
		<div data-options="region:'west',split:true" style="width:380px;">
			
			<div id="questionnairePreview">
			</div>	
			
		</div>
		<!-- 右边表单编辑区 -->
		<div data-options="region:'center'">
		
			<!-- 问题列表 -->
			<table id="questionDg">
				<thead>
					<tr style="height:12px;">		
						<th data-options="field:'QUESTION_DESC',width:360,align:'left'">题目</th>
						<th data-options="field:'listen',width:35,align:'center',formatter:listenrowformatter">试听</th>
						<th data-options="field:'download',width:50,align:'center',formatter:downloadrowformatter">下载</th>
					</tr>
				</thead>
			</table>
		
		</div>
		
	
	</div>

</div>
