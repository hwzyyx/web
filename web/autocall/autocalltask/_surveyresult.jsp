<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script type="text/javascript">
	
	/**
	 * 显示调查结果
	 * @returns
	 */
	function autoCallTaskShowSurveyResult(taskId,questionnaireId) {
		
		
		$("#autoCallTaskSurveyResultDlg").dialog('setTitle',"调查问卷的调查结果").dialog("open");
		
		$.ajax({
	
			type:'POST',
			dataType:'json',
			url:'questionnaire/surveyResultPreview?questionnaireId=' + questionnaireId + '&taskId=' + taskId,
			success:function(rs) {
	
				var statusCode = rs.statusCode;
				var message = rs.message;
				//alert(message);
				if(statusCode = 'success') {
					$("#surveyResultShow").html(message);
				}else {
					window.parent.showMessage(message,statusCode);
				}
			
			}
			
		});
		
	}
	
	
	//导出客户回复结果的数据
	function exportTelDataToExcel(taskId,questionId,respond) {
		//alert("taskId=" + taskId + ",questionId=" + questionId + ",respond:" + respond);
		$("#exportForm").form('submit',{
			url:"questionnaire/exportTelData",
			onSubmit:function(param) {
				param.taskId = taskId;
				param.questionId = questionId;
				param.respond = respond;
				param.isSearchHistoryCallTask = isSearchHistoryCallTask;
			},
			success:function(data) {
				
			}
		});
	}
	
</script>
<div class="easyui-layout" data-options="fit:true"  style="width:100%;height:450px;padding:1px;">
		
	<div id="surveyResultShow">
		
	</div>	
</div>

