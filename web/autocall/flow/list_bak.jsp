<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>呼叫流程管理</title>
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
		<script type="text/javascript" src="Tdrag.js"></script>
		<script type="text/javascript" src="js.date.utils.js"></script>
		<script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
		<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
		<script type="text/javascript">
			$(function(){
				$("#ac_flow_Dg").datagrid({
					pageSize:30,
					pagination:true,
					fit:true,
					toolbar:"#datagridTool",
					singleSelect:true,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[20,30,50],
					url:'autoFlow/datagrid',
					queryParams:{
						flowName:$("#flowName").textbox('getValue')
					}
				})
				$("#ac_flow_Dlg").dialog({
					onClose:function() {
						$("#ac_flow_Form").form('clear');
					}
				});
			});

			//查询数据
			function findData() {
				$("#ac_flow_Dg").datagrid('load',{
					flowName:$("#flowName").textbox('getValue')
				});
			}
			//编辑的超连接拼接
			function rowformatter(value,data,index) {
				return "<a href='#' onclick='javascript:doEdit(\"" + data.FLOW_ID + "\",\"" + data.FLOW_NAME + "\",\"" + data.FLOW_RULE + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>	<a href='#' onclick='javascript:doDel(\"" + data.FLOW_ID +"\")'><img src='themes/icons/cancel.png' border='0'>删除</a>";
			}

			//删除操作
			function doDel(id) {
				$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
					if(r) {
						$("#ac_flow_Form").form('submit',{
							url:"autoFlow/delete?id=" + id,
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
			function doEdit(flowId,flowName,flowRule){
				$("#saveBtn").attr("onclick","saveEdit()");
				$("#ac_flow_Dlg").dialog("open").dialog("setTitle","编辑");
				$("#ac_flow_Form").form('load',{
					'ac_flow.FLOW_ID':flowId,
					'ac_flow.FLOW_NAME':flowName,
					'ac_flow.FLOW_RULE':flowRule
				});
			}
			//编辑操作
			function saveEdit() {
				$("#ac_flow_Form").form('submit',{
					url:"autoFlow/update",
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
							$('#ac_flow_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doAdd() {
				$("#saveBtn").attr("onclick","saveAdd()");
				$("#ac_flow_Dlg").dialog("setTitle","添加").dialog("open");
			}

			function saveAdd() {
				$("#ac_flow_Form").form("submit",{
					url:"autoFlow/add",
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
							$('#ac_flow_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doCancel(){
				$('#ac_flow_Dlg').dialog('close');//关闭对话框
			}
			
			//插入文本的数据
			function insertText() {
				
			}
			
			function defineFlow() {
				$("#define_flow_Dlg").dialog('setTitle','定义呼叫流程').dialog('open');
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
						流程名称：<input id="flowName" class="easyui-textbox" style="width:200px;"/>
						<span style="padding-left:20px;">
							<a href="javascript:findData()" style="width:120px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
						</span>
					</td>
				</tr>
			</table>
		</div>
		<!-- 数据显示区 -->
		<div data-options="region:'center',split:true,border:false">
			<table id="ac_flow_Dg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'FLOW_NAME',width:200,align:'center'">流程名称</th>
						<th data-options="field:'FLOW_RULE',width:800,align:'center'">流程规则</th>
						<th data-options="field:'rowColumn',width:150,align:'center',formatter:rowformatter">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="datagridTool" style="padding:5px;">
		<a href="#" id="easyui-add" onclick="doAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增</a>
		<a href="#" id="easyui-defineFlow" onclick="defineFlow()" class="easyui-linkbutton" iconCls='icon-add' plain="true">定义流程</a>
	</div>

	<div id="ac_flow_Dlg" class="easyui-dialog" style="width:80%;height:80%;padding:10px 20px;" modal="true" closed="true" buttons="#formBtn">
		<form id="ac_flow_Form" method="post">
			<!-- 包含表单 -->
			<%@ include file="/autocall/flow/_form.jsp"%>
		</form>
	</div>
	
	<div id="define_flow_Dlg" class="easyui-dialog" style="width:80%;height:80%;padding:10px 20px;" modal="true" closed="true">
		<form id="define_flow_Form" method="post">
			<!-- 包含表单 -->
			<%@ include file="/autocall/flow/_define_flow_form.jsp"%>
		</form>
	</div>

</body>
</html>
