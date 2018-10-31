<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>系统参数</title>
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
    
    	var currParamType = null;
    
    	$(function() {
    		
    		//根据数据字典中的参数类型的定义，增加不同参数类型的 tab
    		reloadTab();     //加载 tab
    		
    		$('#tt').tabs({
    			onSelect:function(title,index) {
    				currParamType = $('#tt').tabs('getSelected').panel('options').id;    //设置当前的参数类型
    				findData();
    			}
    		});
    		
    		$('#paramDlg').dialog({
    			onClose:function() {
    				$('#paramForm').form('clear');
    			}
    		});
			
    		
    	});
    	
    	//重新加载 tab,通过向param的服务端请求数据，并加载 tab 数据
    	function reloadTab() {
    		
    		$.messager.progress({
				msg:'系统正在处理，请稍候...',
				interval:3000
			});
    		
    		$.ajax({
    			url:'param/reloadTab',
    			method:'post',
    			dataType:'json',
    			success:function(rs) {
    				$.messager.progress("close");
					for(var i=0;i<rs.length;i++) {      //遍历返回的数据列表
						var paramType = rs[i].paramType;
						var paramTypeId = rs[i].paramTypeId;
						var paramTypeDesc = rs[i].paramTypeDesc;
						
						addTab(paramType,paramTypeDesc,paramTypeId);
					}    				
    			}
    		});
    		
    	}
    	
    	//增加 tab
    	function addTab(paramType,paramTypeDesc,paramTypeId) {
    		
   			$("#tt").tabs('add',{
    			title:paramTypeDesc,
    			id:paramType,
    			content: getTabContent(paramTypeId,paramType)
    		});
    		
    		//初始化列表数据
    		$("#" + paramTypeId).datagrid({
    			pageSize:30,
				pagination:true,
				fit:true,
				toolbar:'#opertool'+paramType,
				title:paramTypeDesc,
				singleSelect:true,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[10,20,30],
				url:'param/datagrid?paramType=' + currParamType,
				queryParams:{
					paramCode:$("#paramCode" + currParamType).textbox('getValue'),
    				paramName:$("#paramName" + currParamType).textbox('getValue')
				}
    		});
    		
    	}
    	
    	function getTabContent(paramTypeId,paramType) {
    		var content = "";  
    		content += '<table id="' + paramTypeId + '">';
    		content += '<thead>';
    		content += '<tr style="height:12px;">';
    		content += '<th data-options="field:\'PARAM_CODE\',width:150,align:\'center\'">参数编码</th>';
    		content += '<th data-options="field:\'PARAM_NAME\',width:150,align:\'center\'">参数名称</th>';
    		content += '<th data-options="field:\'PARAM_VALUE\',width:400,align:\'center\'">参数赋值</th>';
    		content += '<th data-options="field:\'CREATE_TIME\',width:200,align:\'center\'">创建时间</th>';
    		content += '<th data-options="field:\'PARAM_DESC\',width:400,align:\'left\'">参数描述</th>';
    		content += '<th data-options="field:\'id\',width:120,align:\'center\',formatter:rowformater">操作</th>';
    		content += '</tr>';
    		content += '</thead>';
    		content += '</table>';
    		
    		content += '<div id="opertool' + paramType + '" style="padding:5px">';
    		content += '<span>参数编号：</span><input type="text" id="paramCode' + paramType + '" style="width:150px;" class="easyui-textbox" size=10 />';
    		content += '<span style="margin-left:100px;">参数名称：</span><input type="text" id="paramName' + paramType + '" style="width:150px;" class="easyui-textbox" size=10 />';
    		content += '<span style="padding-left:30px;padding-right:30px;">';
    		content += '<a href="javascript:findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:\'icon-search\'">查询</a>';
    		content += '</span>';
    		content += '<a href="#" onclick="paramAdd()" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加</a>';
    		content += '</div>';
    		
    		return content;
    	}
    	
    	//格式化：在每行输出 修改及删除
		function rowformater(value,data,index) {
    		//return "";
			return "<a href='#' onclick='javascript:paramEdit(\"" + data.PARAM_CODE +"\",\""+ data.PARAM_NAME +"\",\"" + data.PARAM_VALUE + "\",\"" + data.PARAM_DESC + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" +  
			"&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' onclick='javascript:paramDel(\"" + data.PARAM_CODE +"\")'><img src='themes/icons/cancel.png' border='0'>删除</a>";
		}
		
		function findData() {
			
			paramCodeValue = $('#paramCode' + currParamType).textbox('getValue');
			paramNameValue = $('#paramName' + currParamType).textbox('getValue');
			
			$("#paramTypeId" + currParamType).datagrid({url:'param/datagrid?paramType=' + currParamType}).datagrid('reload',{
				paramCode:paramCodeValue,
				paramName:paramNameValue
			});
			//alert("查询条件: 参数编号:" + paramCodeValue + ",参数名称:" + paramNameValue);
		}
		
		function paramAdd() {
			
			$("#paramDlg").dialog('setTitle','新增系统参数').dialog('open');
			
			$("#PARAM_CODE").textbox("readonly",false);
    		$("#saveBtn").attr('onclick','saveAdd()');
			
		}
		
		function paramEdit(paramCode,paramName,paramValue,paramDesc) {
			$("#paramDlg").dialog('setTitle','修改系统参数').dialog('open');
			
			$("#PARAM_CODE").textbox("readonly",true);
    		$("#saveBtn").attr('onclick','saveEdit()');
    		
    		$('#PARAM_CODE').textbox('setValue',paramCode);
    		$('#PARAM_NAME').textbox('setValue',paramName);
    		$('#PARAM_VALUE').textbox('setValue',paramValue);
    		$('#PARAM_DESC').textbox('setValue',paramDesc);
    		
		}
		
		function saveAdd() {
			$("#paramForm").form('submit',{
    			url:'param/add?paramType=' + currParamType,
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
						findData();
						closeDialog();					  //关闭对话框
					}
    			}
    		});
		}
		
		function saveEdit() {
			$('#paramForm').form('submit',{
    			
    			url:'param/update?paramType=' + currParamType,
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
						findData();
						closeDialog();					  //关闭对话框
					}
    			}
    			
    		});
		}
		
		function paramDel(paramCode) {
			if(paramCode == null || "" == paramCode) {
    			$.messager.alert('提示','删除记录失败,请选择删除的行','info');
    		}
    		
    		$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
    			
    			if(r) {
    				$.ajax({
    					type:'POST',
    					dataType:'json',
    					url:'param/delete?paramCode=' + paramCode + "&paramType=" + currParamType,
    					success:function(rs) {
    						var statusCode = rs.statusCode;   //返回的结果类型
							var message = rs.message;         //返回执行的信息
							
							window.parent.showMessage(message,statusCode);

							if(statusCode == 'success') {
								findData();
							}
    					}
    				});
    				
    			}
    			
    		});
		}
		
		function closeDialog() {
			$('#paramDlg').dialog('close');
		}
    	
    </script>
</head>
<body>

<!-- 页面加载效果 -->
<%@ include file="/base_loading.jsp" %>

<!-- 左置分隔的 tabs,fit:true 表示铺满父容器 -->
<div id="tt" class="easyui-tabs" data-options="tabPosition:'left',fit:true,selected:true">
</div>

<div id="paramDlg" class="easyui-dialog" style="width:600px;height:400px;padding:10px 20px;" modal="true" closed="true" buttons="#dialogBtn">
	<form id="paramForm" method="post">
		<!-- 包含表单 -->
		<%@ include file="/system/param/_form.jsp"%>
	</form>	
</div>

</body>
</html>