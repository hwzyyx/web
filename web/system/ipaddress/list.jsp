<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>IP地址权限管理</title>
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
			$(function(){
				$("#sys_ip_address_Dg").datagrid({
					pageSize:30,
					pagination:true,
					fit:true,
					toolbar:"#datagridTool",
					singleSelect:true,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[20,30,50],
					url:'sysIpAddress/datagrid',
					queryParams:{
						ipAddress:$("#ipAddress").textbox('getValue'),
						memo:$("#memo").textbox('getValue')
					}
				})
				$("#sys_ip_address_Dlg").dialog({
					onClose:function() {
						$("#sys_ip_address_Form").form('clear');
					}
				});
			});

			//查询数据
			function findData() {
				$("#sys_ip_address_Dg").datagrid('load',{
					ipAddress:$("#ipAddress").textbox('getValue'),
					memo:$("#memo").textbox('getValue')
				});
			}
			//编辑的超连接拼接
			function rowformatter(value,data,index) {
				return "<a href='#' onclick='javascript:doEdit(\"" + data.ID + "\",\"" + data.IP_ADDRESS + "\",\"" + data.MEMO + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>	<a href='#' onclick='javascript:doDel(\"" + data.ID +"\")'><img src='themes/icons/cancel.png' border='0'>删除</a>";
			}

			//删除操作
			function doDel(id) {
				$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
					if(r) {
						$("#sys_ip_address_Form").form('submit',{
							url:"sysIpAddress/delete?id=" + id,
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
			function doEdit(id,ipAddress,memo){
				$("#saveBtn").attr("onclick","saveEdit()");
				$("#sys_ip_address_Dlg").dialog("open").dialog("setTitle","编辑");
				$("#sys_ip_address_Form").form('load',{
					'sys_ip_address.ID':id,
					'sys_ip_address.IP_ADDRESS':ipAddress,
					'sys_ip_address.MEMO':memo
				});
			}
			//编辑操作
			function saveEdit() {
				$("#sys_ip_address_Form").form('submit',{
					url:"sysIpAddress/update",
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
							$('#sys_ip_address_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doAdd() {
				$("#saveBtn").attr("onclick","saveAdd()");
				$("#sys_ip_address_Dlg").dialog("setTitle","添加").dialog("open");
			}

			function saveAdd() {
				$("#sys_ip_address_Form").form("submit",{
					url:"sysIpAddress/add",
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
							$('#sys_ip_address_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doCancel(){
				$('#sys_ip_address_Dlg').dialog('close');//关闭对话框
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
						IP地址：<input id="ipAddress" class="easyui-textbox" style="width:200px;"/>
						<span style="padding-left:20px;">
							备注：<input id="memo" class="easyui-textbox" style="width:200px;"/>
						</span>
						<span style="padding-left:20px;">
							<a href="javascript:findData()" style="width:120px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
						</span>
					</td>
				</tr>
			</table>
		</div>
		<!-- 数据显示区 -->
		<div data-options="region:'center',split:true,border:false">
			<table id="sys_ip_address_Dg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'IP_ADDRESS',width:200,align:'center'">IP地址</th>
						<th data-options="field:'MEMO',width:200,align:'center'">备注</th>
						<th data-options="field:'CREATE_USERCODE_DESC',width:200,align:'center'">创建人</th>
						<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
						<th data-options="field:'rowColumn',width:150,align:'center',formatter:rowformatter">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="datagridTool" style="padding:5px;">
		<a href="#" id="easyui-add" onclick="doAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增</a>
	</div>

	<div id="sys_ip_address_Dlg" class="easyui-dialog" style="width:30%;height:30%;padding:10px 20px;" modal="true" closed="true" buttons="#formBtn">
		<form id="sys_ip_address_Form" method="post">
			<!-- 包含表单 -->
			<%@ include file="/system/ipaddress/_form.jsp"%>
		</form>
	</div>

</body>
</html>
