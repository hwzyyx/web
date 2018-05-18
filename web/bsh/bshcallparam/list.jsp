<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
<title>博世呼叫参数</title>
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
	<script src="iconfont/iconfont.js"></script>
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    
    <script type="text/javascript">
    
    	$(function(){
    		
    		$("#callParamDg").datagrid({
    			pageSize:30,
				pagination:true,
				fit:true,
				toolbar:'#opertool',
				singleSelect:true,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[10,20,30],
				url:'bshCallParam/datagrid',
				queryParams:{
					paramCode:$("#paramCode").textbox('getValue'),
    				paramName:$("#paramName").textbox('getValue')
				}
    		});
    		
    		$('#callParamDlg').dialog({
    			onClose:function() {
    				$("#callParamForm").form('clear');
    			}
    		});
    		
    	});
    	
    	//数据查询
    	function findData() {
    		$("#callParamDg").datagrid("reload",{
    			paramCode:$("#paramCode").textbox('getValue'),
    			paramName:$("#paramName").textbox('getValue')
    		});
    	}
    	
    	//添加呼叫参数
    	function callParamAdd() {
    		
    		$("#callParamDlg").dialog('setTitle','添加呼叫参数').dialog('open');
    		
    		$("#PARAM_CODE").textbox("readonly",false);
    		$("#saveBtn").attr('onclick','saveAdd()');
    	}
    	
    	
    	
    	//编辑赋值
    	function callParamEdit(paramCode,paramName,paramValue,paramDesc) {
    		
    		$("#callParamDlg").dialog('setTitle','修改呼叫参数').dialog('open');
    		
    		$("#PARAM_CODE").textbox("readonly",true);
    		$('#saveBtn').attr('onclick','saveEdit()');
    		
    		$('#PARAM_CODE').textbox('setValue',paramCode);
    		$('#PARAM_NAME').textbox('setValue',paramName);
    		$('#PARAM_VALUE').textbox('setValue',paramValue);
    		$('#PARAM_DESC').textbox('setValue',paramDesc);
    		
    	}
    	
    	function closeDialog() {
    		$('#callParamDlg').dialog('close');
    	}
    	
    	function callParamDel(paramCode) {
    		if(paramCode == null || "" == paramCode) {
    			$.messager.alert('提示','删除记录失败,请选择删除的行','info');
    		}
    		
    		$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
    			
    			if(r) {
    				$.ajax({
    					type:'POST',
    					dataType:'json',
    					url:'bshCallParam/delete?paramCode=' + paramCode,
    					success:function(rs) {
    						var statusCode = rs.statusCode;   //返回的结果类型
							var message = rs.message;         //返回执行的信息
							
							window.parent.showMessage(message,statusCode);

							if(statusCode == 'success') {
								$("#callParamDg").datagrid({url:'bshCallParam/datagrid'});
							}
    					}
    				});
    				
    			}
    			
    		});
    	}
    	
    	//保存新增
    	function saveAdd() {
    		
    		$("#callParamForm").form('submit',{
    			url:'bshCallParam/add',
    			onSubmit:function() {
    				var v = $(this).form('validate');
    				
    				if(v) {
    					$.messager.progress({
    						msg:'系统正在处理,请稍候...',
    						interval:3000
    					});
    				}
    				return $(this).form('validate');
    			},
    			success:function(data) {
    				$.messager.progress('close');
    				
    				var result = JSON.parse(data);       //解析json
    				
    				var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#callParamDg').datagrid({url:'bshCallParam/datagrid'});
						closeDialog();					  //关闭对话框
					}
    			}
    		});
    		
    	}
    	
    	//保存修改
    	function saveEdit() {
    		
    		$('#callParamForm').form('submit',{
    			
    			url:'bshCallParam/update',
    			onSubmit:function() {
					var v = $(this).form('validate');
    				if(v) {
    					$.messager.progress({
    						msg:'系统正在处理,请稍候...',
    						interval:3000
    					});
    				}
    				return $(this).form('validate');
    			},
    			success:function(data) {
					$.messager.progress('close');
    				
    				var result = JSON.parse(data);       //解析json
    				
    				var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#callParamDg').datagrid({url:'bshCallParam/datagrid'});
						closeDialog();					  //关闭对话框
					}
    			}
    			
    		});
    		
    	}
    	
    	
    	//格式化：在每行输出 修改及删除
		function rowformater(value,data,index) {
    		//return "";
			return "<a href='#' onclick='javascript:callParamEdit(\"" + data.PARAM_CODE +"\",\""+ data.PARAM_NAME +"\",\"" + data.PARAM_VALUE + "\",\"" + data.PARAM_DESC + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>"; 
			//"<a href='#' onclick='javascript:callParamDel(\"" + data.PARAM_CODE +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}
    	
    </script>
</head>
<body>

<!-- 页面加载效果 -->
<%@ include file="/base_loading.jsp" %>

<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:42px;padding-top:5px;padding-left:5px;">
		<span>参数编号：</span><input type="text" id="paramCode" style="width:150px;" class="easyui-textbox" size=10 />
		<span style="margin-left:100px;">参数名称：</span><input type="text" id="paramName" style="width:150px;" class="easyui-textbox" size=10 />  
        <span style="padding-left:30px;">
	        <a href="javascript:findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">
				查询
			</a>
        </span>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="callParamDg">
			<thead>  
			          
				<tr style="height:12px;">                
					<th data-options="field:'PARAM_CODE',width:150,align:'center'">参数编码</th>                
					<th data-options="field:'PARAM_NAME',width:150,align:'center'">参数名称</th>                
					<th data-options="field:'PARAM_VALUE',width:400,align:'center'">参数赋值</th>                
					<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
					<th data-options="field:'PARAM_DESC',width:400,align:'left'">参数描述</th>                
					<th data-options="field:'id',width:120,align:'center',formatter:rowformater">操作</th>
				</tr>        
			</thead>
		</table>
	</div>
</div>

<div id="opertool" style="padding:5px">  
		<a href="#" id="easyui-add" disabled="true" onclick="callParamAdd()" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加</a>
</div>

<div id="callParamDlg" class="easyui-dialog" style="width:600px;height:400px;padding:10px 20px;" modal="true" closed="true" buttons="#dialogBtn">

	<form id="callParamForm" method="post">
		<!-- 包含表单 -->
		<%@ include file="/bsh/bshcallparam/_form.jsp"%>
	</form>	
</div>
	
</body>
</html>