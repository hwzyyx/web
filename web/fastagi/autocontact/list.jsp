<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>自动接触配置</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		var currAutoContactId = '';
	
		$(function() {

			$("#autoContactDg").datagrid({
				pageSize:15,
				pagination:true,
				fit:true,
				toolbar:'#searchtool',
				singleSelect:true,
				rowsnumber:true,
				rowrap:true,
				striped:true,
				pageList:[10,15,20],
				url:'autoContact/datagrid'
			});
			
			
		});

		// 添加自动接触
		function autoContactAdd() {
			$("#saveBtn").attr('onclick','saveAdd()');
			$("#autoContactDlg").dialog("setTitle","添加自动接触").dialog("open");
		}

		// 修改自动接触
		function autoContactEdit(id,contactName,agentNumber,clientNumber,identifier,callerId,urlInfo,memo) {
			$("#saveBtn").attr('onclick','saveEdit()');
			$("#autoContactDlg").dialog('setTitle','修改自动接触').dialog('open');

			$("#autoContactForm").form('load',{
				'autoContact.ID':id,
				'autoContact.CONTACT_NAME':contactName,
				'autoContact.AGENT_NUMBER':agentNumber,
				'autoContact.CLIENT_NUMBER':clientNumber,
				'autoContact.IDENTIFIER':identifier,
				'autoContact.CALLERID':callerId,
				'autoContact.URL_INFO':urlInfo,
				'autoContact.MEMO':memo
				
			});
		}

		// 保存添加
		function saveAdd() {
			$("#autoContactForm").form('submit',{
				url:'autoContact/add',
				onSubmit:function () {
					return $(this).form('validate');
				},
				success:function(data) {
					var result = JSON.parse(data);   //解析json数据

					var statusCode = result.statusCode;  //返回执行的结果
					var message = result.message;         //返回执行的信息
					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#autoContactDg').datagrid({url:'autoContact/datagrid'});
						initFormData();
						$('#autoContactDlg').dialog('close');//关闭对话框
					}
					
				}
			});
		}

		// 保存编辑
		function saveEdit() {

			$("#autoContactForm").form('submit',{
				url:'autoContact/update',
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function(data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#autoContactDg').datagrid({url:'autoContact/datagrid'});
						initFormData();
						$('#autoContactDlg').dialog('close');//关闭对话框
					}
				}
				
			});
			
		}

		function findData() {

			$('#autoContactDg').datagrid('load',{
				contactName:$('#contactName').textbox('getValue'),
				agentNumber:$('#agentNumber').numberbox('getValue'),
				clientNumber:$('#clientNumber').numberbox('getValue'),
				identifier:$('#identifier').textbox('getValue'),
				callerId:$('#callerId').numberbox('getValue')
			});
			
		}
		
		function autoContactDel(id) {
			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$("#autoContactForm").form('submit',{
						url:"autoContact/delete?id=" + id,
						onSubmit:function(){
							
						},
						success:function(data) {

							var result = JSON.parse(data);    //解析Json 数据

							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息

							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //保存成功时
								$('#autoContactDg').datagrid({url:'autoContact/datagrid'});
							}
						}
					});
				}
			});
		}

		function cancel() {
			$("#autoContactDlg").dialog("close");
			initFormData();
		}

		function rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:autoContactEdit(\"" + data.ID + "\",\"" + data.CONTACT_NAME + "\",\"" + data.AGENT_NUMBER + "\",\"" + data.CLIENT_NUMBER + "\",\"" + data.IDENTIFIER + "\",\"" + data.CALLERID + "\",\"" + data.URL_INFO + "\",\"" + data.MEMO + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:autoContactDel(\"" + data.ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}
		
		function initFormData() {
			$("#ID").val('');
			$('#autoContactForm').form('clear');
		}		
			
	</script>
</head>
<body>
<!-- 页面加载效果 -->
<%@ include file="/base_loading.jsp" %>
<table id="autoContactDg">
	<thead>
	
		<tr style="height:12px;">		
			<th data-options="field:'CONTACT_NAME',width:250,align:'center'">自动接触名字</th>
			<th data-options="field:'AGENT_NUMBER',width:150,align:'center'">座席号码</th>
			<th data-options="field:'CLIENT_NUMBER',width:150,align:'center'">服务号码</th>
			<th data-options="field:'IDENTIFIER',width:150,align:'center'">识别符</th>
			<th data-options="field:'CALLERID',width:150,align:'center'">主叫号码</th>
			<th data-options="field:'URL_INFO',width:300,align:'center'">URL信息</th>
			<th data-options="field:'id',width:100,align:'center',formatter:rowformatter">操作</th>
		</tr>
		
	</thead>
</table>

<div id="searchtool" style="padding:5px;">
	
	<table>
		<tr style="vertical-align: top;">
			<td>
				接触名字：<input id="contactName" style="width:150px;" class="easyui-textbox" name="contactName" type="text" />
			</td>
			<td>
				&nbsp;&nbsp;&nbsp;座席号码：<input id="agentNumber" style="width:150px;" class="easyui-numberbox" name="agentNumber"/>
			</td>
			<td>
				&nbsp;&nbsp;&nbsp;服务号码：<input id="clientNumber" style="width:150px;" class="easyui-numberbox" name="clientNumber" type="text" />
			</td>
			<td>
				&nbsp;&nbsp;&nbsp;识别符：<input id="identifier" style="width:150px;" class="easyui-textbox" name="identifier" type="text" />
			</td>
			<td>
				&nbsp;&nbsp;&nbsp;主叫号码：<input id="callerId" style="width:150px;" class="easyui-numberbox" name="callerId" type="text" />
			</td>
			<td>
				<div style="padding-left:30px;">
					<a href="javascript:findData()" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
				</div>
			</td>
		</tr>
	</table>
	<div>
		<a href="#" id="easyui-add" onclick="autoContactAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新建任务</a>
	</div>
</div>

<div id="autoContactDlg" class="easyui-dialog" style="width:700px;height:400px;padding:5px;" modal="true" closed="true" buttons="#addAutoContactBtn">

	<form id="autoContactForm" method="post">
		<!-- 包含调度配置的表单 -->
		<%@ include file="/fastagi/autocontact/_form.jsp" %>
	</form>

</div>

</body>
</html>

