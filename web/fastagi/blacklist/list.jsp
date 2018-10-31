<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>黑名单功能</title>
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
			
			$("#blacklistDg").datagrid({
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				toolbar:'#searchtool',
				url:'blacklist/datagrid'
			});

			$("#addBlacklistDlg").dialog({
				onClose:function() {
					$("#blacklistForm").form('clear');
				}
			});
			
		});

		function findData() {
			$("#blacklistDg").datagrid("load",{
				clientTelephone:$("#clientTelephone").numberbox('getValue'),
				clientName:$("#clientName").textbox('getValue'),
				state:$("#state").combobox('getValue')
			});
		}

		function rowsformatter(val,data,index) {
			return "<a href='#' onclick='javascript:blacklistEdit(\"" + data.BLACKLIST_ID +"\",\""+ data.CLIENT_TELEPHONE +"\",\"" + data.CLIENT_NAME + "\",\"" + data.STATE + "\",\"" + data.REASON + "\")'>编辑</a>" + 
			"   <a href='#' onclick='javascript:blacklistDel(\"" + data.BLACKLIST_ID +"\")'>删除</a>";
		}

		function stateformatter(val,data,index) {
			if(val==1) {
				return "<span style='color:green;'>有效</span>";
			}else {
				return "<span style='color:gray;'>无效</span>";
			}
		}
		//======-----======
		function blacklistAdd() {
			$("#addBlacklistDlg").dialog('setTitle','添加黑名单').dialog("open");

			$("#STATE").combobox('setValue','1');
			$("#saveBtn").attr('onclick','saveAdd()');
			
		}

		function blacklistEdit(blacklistId,clientTelephone,clientName,state,reason) {
			$("#addBlacklistDlg").dialog('setTitle','修改黑名单').dialog("open");
			$("#saveBtn").attr('onclick','saveEdit()');

			$("#blacklistForm").form('load',{
				'blacklist.BLACKLIST_ID':blacklistId,
				'blacklist.CLIENT_TELEPHONE':clientTelephone,
				'blacklist.CLIENT_NAME':clientName,
				'blacklist.STATE':state,
				'blacklist.REASON':reason
			});
		}

		function blacklistDel(blacklistId) {
			
			$.messager.confirm('提示','你确定要删除这条记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...'
					});
					$.ajax({
						url:'blacklist/delete?blacklistId=' + blacklistId,
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress("close");
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								$("#blacklistDg").datagrid({url:'blacklist/datagrid'});
							}
						}
					});
				}
			});
			
			
		}

		function saveAdd() {
			$("#blacklistForm").form('submit',{
				url:'blacklist/add',
				onSubmit:function() {
					return $(this).form('validate');;
				},
				success:function(data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#blacklistDg').datagrid({url:'blacklist/datagrid'});
						$('#addBlacklistDlg').dialog('close');//关闭对话框
					}
				}
			});
		}

		function saveEdit() {
			$("#blacklistForm").form('submit',{
				url:'blacklist/update',
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function(data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#blacklistDg').datagrid({url:'blacklist/datagrid'});
						$('#addBlacklistDlg').dialog('close');//关闭对话框
					}
				}
			});
		}

		function add_cancel() {
			$("#addBlacklistDlg").dialog('close');
		}
					
	</script>	
</head>

<body>

	<!-- 页面加载效果 -->
	<%@ include file="/base_loading.jsp" %>

	<div class="easyui-panel" title="黑名单管理" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:40px">
				<table>
								<tr>
									<td>客户号码</td>
									<td>
										<input id="clientTelephone" style="width:150px;" name="clientTelephone" class="easyui-numberbox"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;
										客户姓名</td>
									<td>
										<input id="clientName" style="width:150px;" name="clientName" class="easyui-textbox" type="text"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;
										状态</td>
									<td>
										<select class="easyui-combobox" style="width: 155px;" id="state" name="state" data-options="panelHeight:'auto'">
			              					<option value="2">请选择</option>
			              					<option value="1">有效</option>
			              					<option value="0">无效</option>
			        					</select>
									</td>
									<td>&nbsp;&nbsp;&nbsp;<a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search',width:135">查询</a></td>
								</tr>
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="blacklistDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'CLIENT_TELEPHONE',width:150,align:'center'">客户号码</th>                
									<th data-options="field:'CLIENT_NAME',width:150,align:'center'">客户姓名</th>
									<th data-options="field:'STATE',width:100,align:'center',formatter:stateformatter">状态</th>                
									<th data-options="field:'REASON',width:250,align:'center'">黑名单原因</th>                
									<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
									<th data-options="field:'OPER_ID',width:100,align:'center'">创建人</th>
									<th data-options="field:'id',width:150,align:'center',formatter:rowsformatter">操作</th>                
								</tr>        
							</thead>
					</table>					

			</div>
			<div id="searchtool" style="padding:5px">  
				<div style="display:inline;">
					<button id="addBtn" onclick="blacklistAdd()">添加黑名单</button>
				</div>
			</div>

		</div>
	</div>

	<div id="addBlacklistDlg" class="easyui-dialog" style="width:580px;height:250px;padding:10px 20px;" modal="true" closed="true" buttons="#addBlacklistDlgBtn">
		<form id="blacklistForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/fastagi/blacklist/_form.jsp"%>
		</form>	
	</div>
</body>
</html>

