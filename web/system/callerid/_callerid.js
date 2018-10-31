function add_cancel(){
	$("#callerIdForm").form('clear');
	$('#addCallerIdDlg').dialog('close');//关闭对话框
}

function callerIdAdd() {
	$("#saveBtn").removeAttr("onclick");
	$("#saveBtn").attr("onclick","saveAdd()");
	
	$("#addCallerIdDlg").dialog("setTitle","添加主叫号码").dialog("open");
}

function saveAdd() {
	
	$("#callerIdForm").form("submit",{
		url:"sysCallerId/add",
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
			$.messager.progress("close");
			
			var result = JSON.parse(data);    //解析Json 数据

			var statusCode = result.statusCode; //返回的结果类型
			var message = result.message;       //返回执行的信息

			window.parent.showMessage(message,statusCode);
			if(statusCode == 'success') {         //保存成功时
				$('#callerIdDg').datagrid({url:'sysCallerId/datagrid'});
				$('#addCallerIdDlg').dialog('close');//关闭对话框
			}
		}
	});
}