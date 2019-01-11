<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>主叫号码管理</title>
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
	<script src="echarts/echarts.min.js"></script><!-- 引入Echarts JS文件 -->
	<script src="iconfont/iconfont.js"></script>
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="system/callerid/_callerid.js"></script>
    <script type="text/javascript">
   	
    	$(function(){
    		
    		$("#callerIdDg").datagrid({
    		
    			pageSize:100,
				pagination:true,
				fit:true,
				toolbar:'#opertool',
				singleSelect:true,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[50,100,200],
				url:'sysCallerId/datagrid',
				queryParams:{
					callerId:$("#callerId").textbox('getValue'),					
    				purpose:$("#purpose").textbox('getValue')
				}
    			
    		});
    		
    		$("#addCallerIdDlg").dialog({
    			onClose:function() {
    				$("#callerIdForm").form('clear');
    			}
    		});
    		
    		$("#callerIdFile").filebox({
				buttonText:'选择文件'
			});
    		
    		$("#addCallerIdByUploadFileDlg").dialog({
    			onClose:function() {
    				$("#callerIdUploadFileForm").form('clear');
    			}
    		});
    		
    	});
    
    	
    	
    	function findData() {
    		$("#callerIdDg").datagrid('load',{
    			callerId:$("#callerId").textbox('getValue'),					
				purpose:$("#purpose").textbox('getValue')
    		});
    	}
    	
    	function rowformatter(value,data,index) {
    		return "<a href='#' onclick='javascript:callerIdEdit(\"" + data.ID + "\",\"" + data.CALLERID + "\",\"" + data.PURPOSE + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:callerIdDel(\"" + data.ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
    	}
    	
    	//function callerIdEdit(id,callerId,purpose) {
    		
    	//}
    	
    	function callerIdDel(id) {
    		
    		if(id == null) {
				$.messager.alert('提示', '删除记录失败，请选择要删除的行！','info');
			};

			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$("#callerIdForm").form('submit',{
						url:"sysCallerId/delete?id=" + id,
						onSubmit:function(){
							
						},
						success:function(data) {

							var result = JSON.parse(data);    //解析Json 数据

							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息

							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //保存成功时
								$('#callerIdDg').datagrid({url:'sysCallerId/datagrid'});
							}
						}
					});
				}
			});
    	}
    	
    	
    	//编辑赋值
		function callerIdEdit(id,callerId,purpose) {
    		
			$("#saveBtn").removeAttr("onclick");
			$("#saveBtn").attr("onclick","saveEdit()");
			$("#addCallerIdDlg").dialog("open").dialog("setTitle","修改主叫号码");
			
			$("#callerIdForm").form('load',{
				'sysCallerId.ID':id,
				'sysCallerId.CALLERID':callerId,
				'sysCallerId.PURPOSE':purpose
			});
			
		}
    	
		function saveEdit() {
			$("#callerIdForm").form('submit',{
				url:"sysCallerId/update",
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
						$('#callerIdDg').datagrid({url:'sysCallerId/datagrid'});
						$('#addCallerIdDlg').dialog('close');//关闭对话框
					}
				}	
			});
		}
		
		function callerIdUpLoad() {
			$("#addCallerIdByUploadFileDlg").dialog('setTitle','上传主叫号码').dialog('open');
		}
		
		function uploadCallerIdFileCancel() {
			$("#addCallerIdByUploadFileDlg").dialog('close');
		}
		
		//上传号码文件
		function uploadCallerIdFile() {
			$("#callerIdUploadFileForm").form('submit',{

				url:'sysCallerId/uploadFile',
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
					$.messager.progress('close');
					var result = JSON.parse(data); //解析Json数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					if(statusCode == 'success') {         //保存成功时
						findData();
						uploadCallerIdFileCancel();
						window.parent.showMessage(message,statusCode);						
					}else {
						window.parent.showMessage(message,statusCode);						
					}
					
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
						主叫号码：<input id="callerId" type="text" class="easyui-textbox" style="width:200px;"/>
						<span style="padding-left:30px;">
							号码用途：<input id="purpose" type="text" class="easyui-textbox" style="width:200px;"/>
						</span>
						<span style="padding-left:30px;">
							<a href="javascript:findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
						</span>
					</td>
				</tr>
			</table>
		</div>
	
		<!-- 数据显示区 -->
		<div data-options="region:'center',split:true,border:false">
			
			<table id="callerIdDg">
				<thead>
					<tr style="height:12px;">		
						<th data-options="field:'CALLERID',width:200,align:'center'">主叫号码</th>
						<th data-options="field:'PURPOSE',width:400,align:'center'">号码用途</th>
						<th data-options="field:'CREATE_USERCODE_DESC',width:300,align:'center'">创建人</th>
						<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
						<th data-options="field:'id',width:150,align:'center',formatter:rowformatter">操作</th>
					</tr>
					
				</thead>
			</table>	
			
		</div>
	</div>
	<div id="opertool" style="padding:5px;">
		<a href="#" id="easyui-add" onclick="callerIdAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增主叫号码</a>
		<a href="#" id="easyui-add" onclick="callerIdUpLoad()" class="easyui-linkbutton" iconCls='icon-add' plain="true" style="margin-left:100px;">上传主叫号码</a>
	</div>
	
	<div id="addCallerIdDlg" class="easyui-dialog" style="width:40%;height:40%;padding:10px 20px;" modal="true" closed="true" buttons="#addCallerIdDlgBtn">

		<form id="callerIdForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/system/callerid/_form.jsp"%>
		</form>	
	</div>
	
	<div id="addCallerIdByUploadFileDlg" class="easyui-dialog" style="width:30%;height:20%;padding:10px 20px;" modal="true" closed="true" buttons="#addCallerIdByUploadFileDlgBtn">

		<form id="callerIdUploadFileForm" method="post" enctype="multipart/form-data">
			<!-- 包含表单 -->
			<%@ include file="/system/callerid/_uploadfile_form.jsp"%>
		</form>	
	</div>
	
</body>
</html>