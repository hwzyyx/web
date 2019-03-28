<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>改号通知来电数据表</title>
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
				
				$("#endTime").datetimebox("setValue",getDateAfter(1) + " 00:00:00");
				
				$("#cnn_callin_data_Dg").datagrid({
					pageSize:100,
					pagination:true,
					fit:true,
					toolbar:"#datagridTool",
					singleSelect:false,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[50,100,200],
					url:'cnnCallinData/datagrid',
					queryParams:{
						callerId:$("#callerId").textbox('getValue'),
						callee:$("#callee").textbox('getValue'),
						state:$("#state").combobox('getValue'),
						customerNewTel:$("#customerNewTel").textbox('getValue'),
						startTime:$("#startTime").datetimebox('getValue'),
						endTime:$("#endTime").datetimebox('getValue')
					}
				})
				$("#cnn_callin_data_Dlg").dialog({
					onClose:function() {
						$("#cnn_callin_data_Form").form('clear');
					}
				});
			});

			//查询数据
			function findData() {
				$("#cnn_callin_data_Dg").datagrid('load',{
					callerId:$("#callerId").textbox('getValue'),
					callee:$("#callee").textbox('getValue'),
					state:$("#state").combobox('getValue'),
					customerNewTel:$("#customerNewTel").textbox('getValue'),
					startTime:$("#startTime").datetimebox('getValue'),
					endTime:$("#endTime").datetimebox('getValue')
				});
			}
			//编辑的超连接拼接
			function rowformatter(value,data,index) {
				return "<a href='#' onclick='javascript:doEdit(\"" + data.ID + "\",\"" + data.CALLERID + "\",\"" + data.CALLEE + "\",\"" + data.STATE + "\",\"" + data.CALL_DATE + "\",\"" + data.PK_CNN_DATA_ID + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>	<a href='#' onclick='javascript:doDel(\"" + data.ID +"\")'><img src='themes/icons/cancel.png' border='0'>删除</a>";
			}
			
			function stateformatter(value,data,index) {
				if(value=="1") {
					return "<span style='color:green;'>已改号</span>";
				}else {
					return "<span style='color:red;'>未改号</span>";
				}
			}
			

			//删除操作
			function doDel(id) {
				$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
					if(r) {
						$("#cnn_callin_data_Form").form('submit',{
							url:"cnnCallinData/delete?id=" + id,
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
			function doEdit(id,callerId,callee,state,callDate,pkCnnDataId){
				$("#saveBtn").attr("onclick","saveEdit()");
				$("#cnn_callin_data_Dlg").dialog("open").dialog("setTitle","编辑");
				$("#cnn_callin_data_Form").form('load',{
					'cnn_callin_data.ID':id,
					'cnn_callin_data.CALLERID':callerId,
					'cnn_callin_data.CALLEE':callee,
					'cnn_callin_data.STATE':state,
					'cnn_callin_data.CALL_DATE':callDate,
					'cnn_callin_data.PK_CNN_DATA_ID':pkCnnDataId
				});
			}
			//编辑操作
			function saveEdit() {
				$("#cnn_callin_data_Form").form('submit',{
					url:"cnnCallinData/update",
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
							$('#cnn_callin_data_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doAdd() {
				$("#saveBtn").attr("onclick","saveAdd()");
				$("#cnn_callin_data_Dlg").dialog("setTitle","添加").dialog("open");
			}

			function saveAdd() {
				$("#cnn_callin_data_Form").form("submit",{
					url:"cnnCallinData/add",
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
							$('#cnn_callin_data_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doCancel(){
				$('#cnn_callin_data_Dlg').dialog('close');//关闭对话框
			}
			
			//导出操作
			function doExport() {
				
				$("#exportForm").form("submit",{
					url:'cnnCallinData/exportExcel',
					onSubmit:function(param) {
						param.callerId = $("#callerId").textbox('getValue');
						param.callee = $("#callee").textbox('getValue');
						param.state = $("#state").combobox('getValue');
						param.customerNewTel = $("#customerNewTel").textbox('getValue');
						param.startTime = $("#startTime").datetimebox('getValue');
						param.endTime = $("#endTime").datetimebox('getValue');
					},
					success:function(data) {
						
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
		<div data-options="region:'north',split:true,border:true" style="height:50px;padding-top:5px;padding-left:5px;">
			<table>
				<tr style="vertical-align: top;">
					<td>
						主叫号码：<input id="callerId" class="easyui-textbox" style="width:150px;"/>
						<span style="padding-left:20px;">
							被叫号码：<input id="callee" class="easyui-textbox" style="width:150px;"/>
						</span>
						<span style="padding-left:20px;">
							被叫是否已改号：
							<select class="easyui-combobox" id="state" name="state" style="width:100px;" data-options="panelHeight:'auto'">
								<option value="empty">全部</option>
								<option value="1">已改号</option>
								<option value="2">未改号</option>
							</select>
						</span>
						<span style="padding-left:20px;">
							被叫新号码：<input id="customerNewTel" class="easyui-textbox" style="width:150px;"/>
						</span>
						<span style="padding-left:20px;">
							开始时间：<input id="startTime" class="easyui-datetimebox" style="width:180px;"/>
						</span>
						<span style="padding-left:20px;">
							结束时间：<input id="endTime" class="easyui-datetimebox" style="width:180px;"/>
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
			<table id="cnn_callin_data_Dg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'ck',checkbox:true"></th>
						<th data-options="field:'CALLERID',width:200,align:'center'">主叫号码</th>
						<th data-options="field:'CALLEE',width:200,align:'center'">被叫号码</th>
						<th data-options="field:'STATE',width:150,align:'center',formatter:stateformatter">被叫是否已改号</th>
						<th data-options="field:'CUSTOMER_NEW_TEL',width:200,align:'center'">被叫新号码</th>
						<th data-options="field:'CALL_DATE',width:200,align:'center'">来电时间</th>
						<!-- 
						<th data-options="field:'PK_CNN_DATA_ID',width:200,align:'center'">改号信息</th>
						<th data-options="field:'rowColumn',width:150,align:'center',formatter:rowformatter">操作</th>
						 -->
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="datagridTool" style="padding:5px;">
		<!-- 
		<a href="#" id="easyui-add" onclick="doAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增</a>
		 -->
		<a href="#" id="easyui-add" onclick="doExport()" class="easyui-linkbutton" style="margin-left:20px;" iconCls='icon-redo' plain="true">导出数据</a>
	</div>

	<div id="cnn_callin_data_Dlg" class="easyui-dialog" style="width:80%;height:80%;padding:10px 20px;" modal="true" closed="true" buttons="#formBtn">
		<form id="cnn_callin_data_Form" method="post">
			<!-- 包含表单 -->
			<%@ include file="/cnn/cnncallindata/_form.jsp"%>
		</form>
	</div>
	<form method="post" id="exportForm"></form>
</body>
</html>
