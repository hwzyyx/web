<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>主叫号码组管理</title>
		<style>
			.font17{
				font-size: 17px;
			}
		</style>
		<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
		<link rel="stylesheet" type="text/css" href="themes/color.css">
		<link rel="stylesheet" type="text/css" href="themes/icon.css">
		<link rel="stylesheet" type="text/css" href="demo.css">
		<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
		<link rel="stylesheet" type="text/css" href="iconfont/iconfont.css">
		<script src="echarts/echarts.min.js"></script>
		<script src="iconfont/iconfont.js"></script>
		<script type="text/javascript" src="jquery.min.js"></script>
		<script type="text/javascript" src="jquery.easyui.min.js"></script>
		<script type="text/javascript" src="js.date.utils.js"></script>
		<script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
		<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
		<script type="text/javascript">
		
			var currGroupId = null;
			var currGroupName = null;
		
			$(function(){
				$("#sys_callerid_group_Dg").datagrid({
					pageSize:30,
					pagination:true,
					fit:true,
					toolbar:"#datagridTool",
					singleSelect:true,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[20,30,50],
					url:'sysCallerIdGroup/datagrid',
					queryParams:{
						groupName:$("#groupName").textbox('getValue')
					}
				});
				
				$('#callerIdGroupAssignDg').datagrid({
					pageSize:50,
					pagination:true,
					fit:true,
					toolbar:'#datagridToolForCallerIdGroupAssign',
					singleSelect:false,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[30,50,100],
					url:'sysCallerIdGroupAssign/datagrid',
					queryParams: {
						groupId:currGroupId
					}
				});
				
				$("#sys_callerid_group_Dlg").dialog({
					onClose:function() {
						$("#sys_callerid_group_Form").form('clear');
					}
				});
				
				$("#callerIdFile").filebox({
					buttonText:'选择文件'
				});
				
				$("#addCallerIdGroupAssignByUploadFileDlg").dialog({
	    			onClose:function() {
	    				$("#callerIdGroupAssignUploadFileForm").form('clear');
	    			}
	    		});
				
			});

			//查询数据
			function findData() {
				$("#sys_callerid_group_Dg").datagrid('load',{
					groupName:$("#groupName").textbox('getValue')
				});
			}
			//编辑的超连接拼接
			function rowformatter(value,data,index) {
				return "<a href='#' onclick='javascript:doEdit(\"" + data.GROUP_ID + "\",\"" + data.GROUP_NAME + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>	<a href='#' onclick='javascript:doDel(\"" + data.GROUP_ID +"\")'><img src='themes/icons/cancel.png' border='0'>删除</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' onclick='javascript:findDataForCallerIdGroupAssign(\"" + data.GROUP_ID + "\",\"" + data.GROUP_NAME + "\")'><img src='themes/icons/folderopened.png' border='0'>号码列表</a>";
			}
			
			function findDataForCallerIdGroupAssign(groupId,groupName) {
				currGroupId = groupId;
				currGroupName = groupName;
				
				$("#callerIdGroupAssignDg").datagrid('load',{
					groupId:currGroupId
				});
				
				$("#callerIdGroupAssignDlg").dialog('setTitle',"主叫号码组：" + groupName + " 的号码列表").dialog('open');
				
			}
			
			//删除操作
			function doDel(id) {
				$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
					if(r) {
						$("#sys_callerid_group_Form").form('submit',{
							url:"sysCallerIdGroup/delete?id=" + id,
							onSubmit:function(){
							},
							success:function(data) {
								var result = JSON.parse(data);    //解析Json 数据
								var statusCode = result.statusCode; //返回的结果类型
								var message = result.message;       //返回执行的信息

								window.parent.showMessage(message,statusCode);
								if(statusCode == 'success') {         //保存成功时
									findData();
								}
							}
						});
					}
				});
			}

			//编辑操作
			function doEdit(groupId,groupName){
				$("#saveBtn").attr("onclick","saveEdit()");
				$("#sys_callerid_group_Dlg").dialog("open").dialog("setTitle","编辑");
				$("#sys_callerid_group_Form").form('load',{
					'sys_callerid_group.GROUP_ID':groupId,
					'sys_callerid_group.GROUP_NAME':groupName
				});
			}
			//编辑操作
			function saveEdit() {
				$("#sys_callerid_group_Form").form('submit',{
					url:"sysCallerIdGroup/update",
					onSubmit:function(){
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
							findData();
							$('#sys_callerid_group_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doAdd() {
				$("#saveBtn").attr("onclick","saveAdd()");
				$("#sys_callerid_group_Dlg").dialog("setTitle","添加").dialog("open");
			}

			function saveAdd() {
				$("#sys_callerid_group_Form").form("submit",{
					url:"sysCallerIdGroup/add",
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
							findData();
							$('#sys_callerid_group_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doCancel(){
				$('#sys_callerid_group_Dlg').dialog('close');//关闭对话框
			}
			
			//上传主叫号码文件，向主叫号码组分配主叫号码
			function addCallerIdGroupAssign() {
				$('#addCallerIdGroupAssignByUploadFileDlg').dialog('setTitle','向主叫号码组：' + currGroupName + ",增加主叫号码").dialog('open');
			}
			
			function uploadCallerIdFileCancel() {
				$('#addCallerIdGroupAssignByUploadFileDlg').dialog('close');
			}
			
			//上传主叫号码文件
			function uploadCallerIdFile() {
				
				$('#callerIdGroupAssignUploadFileForm').form('submit',{
					url:'sysCallerIdGroupAssign/uploadFile',
					onSubmit:function(param) {
						param.groupId = currGroupId
					},
					success:function(data) {
						$.messager.progress('close');
						$('#addCallerIdGroupAssignByUploadFileDlg').dialog('close');
						var result = JSON.parse(data); //解析Json数据

						var statusCode = result.statusCode; //返回的结果类型
						var message = result.message;       //返回执行的信息
						
						if(statusCode == 'success') {         //保存成功时
							findDataForCallerIdGroupAssign(currGroupId,currGroupName);
							window.parent.showMessage(message,statusCode);	
						}else {
							window.parent.showMessage(message,statusCode);	
						}
					}
				});
				
			}
			
			//根据选择的分配号码，将分配进行移除
			function removeCallerIdGroupAssign() {
				
				ids = getSelectedRows();       //取得选中的 ID
				$.messager.confirm('提示','你确定要移除选中的分配记录吗?',function(r){
					if(r) {
						$.ajax({
							url:'sysCallerIdGroupAssign/removeAssign?groupId=' + currGroupId + "&ids=" + ids,
							method:'post',
							dataType:'json',
							success:function(rs) {
								var statusCode = rs.statusCode; //返回的结果类型
								var message = rs.message;       //返回执行的信息
								window.parent.showMessage(message,statusCode);
								if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
									findDataForCallerIdGroupAssign(currGroupId,currGroupName);
								}
							}
						});
					}
				});
				
			}
			
			//取得选中的号码数据			
			function getSelectedRows() {
				
				var rows = $('#callerIdGroupAssignDg').datagrid('getSelections');
				var ids = [];
				for(var i=0; i<rows.length; i++){
					ids.push(rows[i].ID);
				}
				return	ids.join(",");			
			}
			
		</script>
	</head>
<body>

	<%@ include file="/base_loading.jsp" %>
	<!-- 定义一个 layout -->
	<div data-options="fit:true" class="easyui-layout">
		<!-- 顶部查询区 -->
		<div data-options="region:'north',split:true,border:true" style="height:50px;padding-top:5px;padding-left:5px;">
			<table>
				<tr style="vertical-align: top;">
					<td>
						主叫组名称：<input id="groupName" class="easyui-textbox" style="width:200px;"/>
						<span style="padding-left:20px;">
							<a href="javascript:findData()" style="width:120px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
						</span>
					</td>
				</tr>
			</table>
		</div>
		<!-- 数据显示区 -->
		<div data-options="region:'center',split:true,border:false">
			<table id="sys_callerid_group_Dg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'GROUP_NAME',width:200,align:'center'">主叫号码组名</th>
						<th data-options="field:'CREATE_USERCODE_DESC',width:300,align:'center'">创建人</th>
						<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
						<th data-options="field:'rowColumn',width:200,align:'center',formatter:rowformatter">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="datagridTool" style="padding:5px;">
		<a href="#" id="easyui-add" onclick="doAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增</a>
	</div>
	
	<div id="datagridToolForCallerIdGroupAssign" style="padding:5px;">
		<a href="#" id="removeCallerIdGroupAssignBtn" onclick="removeCallerIdGroupAssign()" title="该操作只将号码从组中移除，并不删除主叫号码!" class="easyui-linkbutton easyui-tooltip" iconCls='icon-remove' plain="true">移除主叫号码</a>
		<a href="#" id="addCallerIdGroupAssignBtn" onclick="addCallerIdGroupAssign()" title="通过上传号码文件向主叫号码组分配主叫号码!" class="easyui-linkbutton easyui-tooltip" iconCls='icon-add' plain="true" style="margin-left:50px;">分配主叫号码</a>
	</div>

	<div id="sys_callerid_group_Dlg" class="easyui-dialog" style="width:40%;height:30%;padding:10px 20px;" modal="true" closed="true" buttons="#formBtn">
		<form id="sys_callerid_group_Form" method="post">
			<!-- 包含表单 -->
			<%@ include file="/system/calleridgroup/_form.jsp"%>
		</form>
	</div>

	<div id="callerIdGroupAssignDlg" class="easyui-dialog" style="width:80%;height:80%;padding:10px 20px;" modal="true" closed="true">
		<table id="callerIdGroupAssignDg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'ck',checkbox:true"></th>		
						<th data-options="field:'CALLERID',width:180,align:'center'">主叫号码</th>
						<th data-options="field:'PURPOSE',width:250,align:'center'">号码用途</th>
						<th data-options="field:'CREATE_USERCODE_DESC',width:200,align:'center'">创建人</th>
						<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
						<th data-options="field:'GROUP_NAME',width:300,align:'center'">主叫号码组</th>
					</tr>
				</thead>
			</table>
	</div>
	
	<div id="addCallerIdGroupAssignByUploadFileDlg" class="easyui-dialog" style="width:35%;height:30%;padding:10px 20px;" modal="true" closed="true" buttons="#addCallerIdGroupAssignByUploadFileDlgBtn">

		<form id="callerIdGroupAssignUploadFileForm" method="post" enctype="multipart/form-data">
			<!-- 包含表单 -->
			<%@ include file="/system/calleridgroup/_uploadfile_form.jsp"%>
		</form>	
	</div>
</body>
</html>
