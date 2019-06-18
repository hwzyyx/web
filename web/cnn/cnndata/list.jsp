<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>改号通知数据表</title>
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
		
			var uploadFlag = 1;     //上传数据的操作标识，1：批量增加，2：批量删除
		
			$(function(){
				
				//$("#startTime").datetimebox("setValue",getCurrDate() + " 00:00:00");
				$("#endTime").datetimebox("setValue",getDateAfter(1) + " 00:00:00");
				
				$("#cnnDataFile").filebox({
					buttonText:'选择文件'
				});
				
				$("#cnn_data_Dg").datagrid({
					pageSize:100,
					pagination:true,
					fit:true,
					toolbar:"#datagridTool",
					singleSelect:false,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[50,100,200],
					url:'cnnData/datagrid',
					queryParams:{
						customerName:$("#customerName").textbox('getValue'),
						customerTel:$("#customerTel").textbox('getValue'),
						customerNewTel:$("#customerNewTel").textbox('getValue'),
						startTime:$("#startTime").textbox('getValue'),
						endTime:$("#endTime").textbox('getValue')
					}
				})
				$("#cnn_data_Dlg").dialog({
					onClose:function() {
						$("#cnn_data_Form").form('clear');
					}
				});
			});

			//查询数据
			function findData() {
				$("#cnn_data_Dg").datagrid('load',{
					customerName:$("#customerName").textbox('getValue'),
					customerTel:$("#customerTel").textbox('getValue'),
					customerNewTel:$("#customerNewTel").textbox('getValue'),
					flag:$("#flag").combobox('getValue'),
					startTime:$("#startTime").textbox('getValue'),
					endTime:$("#endTime").textbox('getValue')
				});
			}
			
			//编辑的超连接拼接
			function rowformatter(value,data,index) {
				return "<a href='#' onclick='javascript:doEdit(\"" + data.ID + "\",\"" + data.CUSTOMER_NAME + "\",\"" + data.CUSTOMER_TEL + "\",\"" + data.CUSTOMER_NEW_TEL + "\",\"" + data.FLAG + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>	<a href='#' onclick='javascript:doDel(\"" + data.ID +"\")'><img src='themes/icons/cancel.png' border='0'>删除</a>";
			}
			
			function flagformatter(value,data,index) {
				if(value=="1") {
					return "<span style='color:green;'>中文</span>";
				}else {
					return "<span style='color:red;'>英文</span>";
				}
			}

			//删除操作
			function doDel(id) {
				$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
					if(r) {
						$("#cnn_data_Form").form('submit',{
							url:"cnnData/delete?id=" + id,
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
			
			function doBatchDelete() {
				
				$.messager.confirm('提示','你确定要批量删除选中的记录吗?',function(r){
					if(r) {
						$("#cnn_data_Form").form('submit',{
							url:"cnnData/batchDelete?ids=" + getSelectedRows(),
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
			
			//取得选中的号码数据			
			function getSelectedRows() {
				
				var rows = $('#cnn_data_Dg').datagrid('getSelections');
				var ids = [];
				for(var i=0; i<rows.length; i++){
					ids.push(rows[i].ID);
				}
				return	ids.join(",");			
			}

			//编辑操作
			function doEdit(id,customerName,customerTel,customerNewTel,flag){
				$("#saveBtn").attr("onclick","saveEdit()");
				$("#cnn_data_Dlg").dialog("open").dialog("setTitle","编辑");
				if(flag=="2") {
					$("#FLAG").prop("checked",true);
				}
				$("#cnn_data_Form").form('load',{
					'cnn_data.ID':id,
					'cnn_data.CUSTOMER_NAME':customerName,
					'cnn_data.CUSTOMER_TEL':customerTel,
					'cnn_data.CUSTOMER_NEW_TEL':customerNewTel
				});
			}
			//编辑操作
			function saveEdit() {
				$("#cnn_data_Form").form('submit',{
					url:"cnnData/update",
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
							$('#cnn_data_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doAdd() {
				$("#saveBtn").attr("onclick","saveAdd()");
				$("#cnn_data_Dlg").dialog("setTitle","添加").dialog("open");
			}

			function saveAdd() {
				$("#cnn_data_Form").form("submit",{
					url:"cnnData/add",
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
							$('#cnn_data_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doCancel(){
				$('#cnn_data_Dlg').dialog('close');//关闭对话框
			}
			
			function doUploadData(flag) {
				
				uploadFlag = flag;    //更改上传的标识
				
				$("#uploadFileDlg").dialog('setTitle',"上传数据文件").dialog("open");
			}
			
			function uploadCnnDataFileCancel() {
				$("#uploadFileForm").form("clear");
				$("#uploadFileDlg").dialog("close");
			}
			
			//上传号码文件
			function uploadCnnDataFile() {
				$("#uploadFileForm").form('submit',{

					url:'cnnData/uploadFile',
					onSubmit:function(param) {
						param.uploadFlag = uploadFlag;
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
						$.messager.progress('close');
						var result = JSON.parse(data); //解析Json数据

						var statusCode = result.statusCode; //返回的结果类型
						var message = result.message;       //返回执行的信息

						if(statusCode == 'success') {         //保存成功时
							findData();
							uploadCnnDataFileCancel();
							window.parent.showMessage(message,statusCode);						
						}else {
							window.parent.showMessage(message,statusCode);						
						}
						
					}
					
				});
			}
			
			//导出操作
			function doExport() {
				$("#exportForm").form("submit",{
					url:'cnnData/exportExcel',
					onSubmit:function(param) {
						param.customerName = $("#customerName").textbox('getValue');
						param.customerTel = $("#customerTel").textbox('getValue');
						param.customerNewTel = $("#customerNewTel").textbox('getValue');
						param.flag = $("#flag").combobox('getValue');
						param.startTime = $("#startTime").textbox('getValue');
						param.endTime = $("#endTime").textbox('getValue');
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
						客户号码：<input id="customerTel" class="easyui-textbox" style="width:180px;"/>
						<span style="padding-left:20px;">
							客户新号码：<input id="customerNewTel" class="easyui-textbox" style="width:180px;"/>
						</span>
						<span style="padding-left:20px;">
							客户名字：<input id="customerName" class="easyui-textbox" style="width:180px;"/>
						</span>
						<span style="padding-left:20px;">
							（中/英）标识：
							<select class="easyui-combobox" id="flag" name="flag" style="width:80px;" data-options="panelHeight:'auto'">
									<option value="empty">请选择</option>
									<option value="1">中文</option>
									<option value="2">英文</option>
							</select>
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
			<table id="cnn_data_Dg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'ck',checkbox:true"></th>
						<th data-options="field:'CUSTOMER_TEL',width:200,align:'center'">客户号码</th>
						<th data-options="field:'CUSTOMER_NEW_TEL',width:200,align:'center'">客户新号码</th>
						<th data-options="field:'CUSTOMER_NAME',width:200,align:'center'">客户姓名</th>
						<th data-options="field:'FLAG',width:120,align:'center',formatter:flagformatter">（中/英）标识</th>
						<th data-options="field:'CHANGE_TIME',width:200,align:'center'">修改时间</th>
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
		<a href="#" id="easyui-add" onclick="doBatchDelete()" class="easyui-linkbutton" style="margin-left:20px;" iconCls='icon-remove' plain="true">删除选中</a>
		
		<a href="#" id="easyui-add" onclick="doUploadData(1)" class="easyui-linkbutton" style="margin-left:100px;" iconCls='icon-add' plain="true">批量导入</a>
		<a href="#" id="easyui-add" onclick="doUploadData(2)" class="easyui-linkbutton" style="margin-left:20px;" iconCls='icon-remove' plain="true">批量删除</a>
		
		<a href="#" id="easyui-add" onclick="doExport()" class="easyui-linkbutton" style="margin-left:300px;" iconCls='icon-redo' plain="true">导出数据</a>
	</div>

	<div id="cnn_data_Dlg" class="easyui-dialog" style="width:40%;height:40%;padding:10px 20px;" modal="true" closed="true" buttons="#formBtn">
		<form id="cnn_data_Form" method="post">
			<!-- 包含表单 -->
			<%@ include file="/cnn/cnndata/_form.jsp"%>
		</form>
	</div>
	
	<div id="uploadFileDlg" class="easyui-dialog" style="width:60%;height:50%;padding:10px 20px;" modal="true" closed="true" buttons="#uploadFileDlgBtn">

		<form id="uploadFileForm" method="post" enctype="multipart/form-data">
			<!-- 包含表单 -->
			<%@ include file="/cnn/cnndata/_uploadfile_form.jsp"%>
		</form>	
	</div>
	<form method="post" id="exportForm"></form>
</body>
</html>
