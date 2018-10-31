<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>呼叫转移管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript">
		//数据列表赋值
		
		$(function(){
			
			$("#transferDg").datagrid({
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				toolbar:'#searchtool',
				url:'transfer/datagrid'
			});

			$("#addTransferDlg").dialog({
				onClose:function() {
					$("#transferForm").form('clear');
				}
			});

			$("#TRUNK").combobox({
				url:'getCombobox?groupCode=TRUNK_INFO',
				method:'POST',
				valueField:'id',
				textField:'text'
			});
			
		});

		function findData() {
			$("#transferDg").datagrid("load",{
				did:$("#did").numberbox('getValue'),
				destination:$("#destination").numberbox('getValue'),
			});
		}

		function rowsformatter(val,data,index) {
			return "<a href='#' onclick='javascript:transferEdit(\"" + data.TRANSFER_ID +"\",\""+ data.DID +"\",\"" + data.DESTINATION + "\",\"" + data.TRUNK + "\",\"" + data.START_TIME + "\",\"" + data.END_TIME + "\",\"" + data.MEMO + "\")'>编辑</a>" + 
			"   <a href='#' onclick='javascript:transferDel(\"" + data.TRANSFER_ID +"\")'>删除</a>";
		}

		function getCurrDayDateTime() {
			var d = new Date();
			var vY = d.getFullYear();
			var vM = d.getMonth() + 1;
			var vD = d.getDate();
			var startTime = vY + "-" + vM + "-" + vD + " " + "00:00:00";

			return startTime;
		}

		function getNextDayDateTime() {

			var d = new Date();
						
			d.setDate(d.getDate()+ 1);
				
			var vY = d.getFullYear();
			var vM = d.getMonth() + 1;
			var vD = d.getDate();
			var endTime = vY + "-" + vM + "-" + vD + " " + "00:00:00";
			
			return endTime;
		}
		
		function transferAdd() {
			$("#addTransferDlg").dialog('setTitle','添加呼叫转移').dialog("open");

			
			$("#START_TIME").datetimebox('setValue',getCurrDayDateTime());
			$("#END_TIME").datetimebox('setValue',getNextDayDateTime());
			
			$("#saveBtn").attr('onclick','saveAdd()');
			
		}

		function transferEdit(transferId,did,destination,trunk,startTime,endTime,memo) {
			$("#addTransferDlg").dialog('setTitle','修改呼叫转移').dialog("open");
			$("#saveBtn").attr('onclick','saveEdit()');

			$("#transferForm").form('load',{
				'transfer.TRANSFER_ID':transferId,
				'transfer.DID':did,
				'transfer.DESTINATION':destination,
				'transfer.TRUNK':trunk,
				'transfer.START_TIME':startTime,
				'transfer.END_TIME':endTime,
				'transfer.MEMO':memo
			});
			
		}

		function transferDel(transferId) {
			
			$.messager.confirm('提示','你确定要删除这条记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...'
					});
					$.ajax({
						url:'transfer/delete?transferId=' + transferId,
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress("close");
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								$("#transferDg").datagrid({url:'transfer/datagrid'});
							}
						}
					});
				}
			});
			
			
		}

		function saveAdd() {
			$("#transferForm").form('submit',{
				url:'transfer/add',
				onSubmit:function() {
					var ifValid = $(this).form('validate');
					if(ifValid) {
						var startTime = $("#START_TIME").datetimebox('getValue');
						var endTime = $("#END_TIME").datetimebox('getValue');
						var d1 = new Date(startTime.replace("-","/"));
						var d2 = new Date(endTime.replace("-","/"));
	
						if(d1 > d2) {
							alert("生效的结束时间必须大于开始时间!");
							return false;
						}
						
						return ifValid;
					}else {
						return ifValid;
					}
				},
				success:function(data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#transferDg').datagrid({url:'transfer/datagrid'});
						$('#addTransferDlg').dialog('close');//关闭对话框
					}
				}
			});
		}

		function saveEdit() {
			$("#transferForm").form('submit',{
				url:'transfer/update',
				onSubmit:function() {
					var ifValid = $(this).form('validate');
					if(ifValid) {
						var startTime = $("#START_TIME").datetimebox('getValue');
						var endTime = $("#END_TIME").datetimebox('getValue');
						var d1 = new Date(startTime.replace("-","/"));
						var d2 = new Date(endTime.replace("-","/"));

						if(d1 > d2) {
							alert("生效的结束时间必须大于开始时间!");
							return false;
						}
						return ifValid;
					}else {
						return ifValid;
					}
				},
				success:function(data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#transferDg').datagrid({url:'transfer/datagrid'});
						$('#addTransferDlg').dialog('close');//关闭对话框
					}
				}
			});
		}

		function add_cancel() {
			$("#addTransferDlg").dialog('close');
		}
					
	</script>	
</head>

<body>
	<!-- 页面加载效果 -->
	<%@ include file="/base_loading.jsp" %>
	<div class="easyui-panel" title="呼叫转移功能" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:40px">
				<table>
								<tr>
									<td>
										DID(特服号)：
										<input data-options="panelHeight:400" style="width:150px;" id="did" name="did" class="easyui-numberbox"/>
									</td>
									<td>
										<div style="padding-left:30px;">
										目标号码：
											<input id="destination" style="width:150px;" name="destination" class="easyui-numberbox"/>
										</div>
									</td>
									<td>
										<div style="padding-left:30px;">
											<a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search',width:135">查询</a>
										</div>
									</td>
								</tr>
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="transferDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'DID',width:120,align:'center'">DID(特服号)</th>                
									<th data-options="field:'DESTINATION',width:120,align:'center'">目标号码</th>
									<th data-options="field:'TRUNK_DESC',width:120,align:'center'">中继信息</th>
									<th data-options="field:'START_TIME',width:150,align:'center'">生效时间</th>
									<th data-options="field:'END_TIME',width:150,align:'center'">失效时间</th>
									<th data-options="field:'MEMO',width:170,align:'center'">转移原因</th>
									<th data-options="field:'OPER_ID',width:60,align:'center'">创建者</th>                
									<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
									<th data-options="field:'id',width:80,align:'center',formatter:rowsformatter">操作</th>                
								</tr>        
							</thead>
					</table>					

			</div>

		</div>
	</div>
	<div id="searchtool" style="padding:5px">  
		<div style="display:inline;">
			<button id="addBtn" onclick="transferAdd()">添加呼叫转移</button>
		</div>
	</div>

	<div id="addTransferDlg" class="easyui-dialog" style="width:580px;height:250px;padding:10px 20px;" modal="true" closed="true" buttons="#addTransferDlgBtn">
		<form id="transferForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/fastagi/transfer/_form.jsp"%>
		</form>	
	</div>	

</body>
</html>

