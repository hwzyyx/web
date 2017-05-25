<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>菜单管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		
		var currSelectNodeId = null;    //当前选择的组织Id
		var currOperId = null;          //当前的 OperId
		$(function(){
			//设置只可读
			$("#moduleCode").textbox('readonly',true);
			$("#moduleName").textbox('readonly',true);
			$("#moduleUri").textbox('readonly',true);
			$("#moduleDesc").textbox('readonly',true);
			//设置背景颜色
			$("#moduleCode").textbox('textbox').css('background-color','#e3e3e3');
			$("#moduleName").textbox('textbox').css('background-color','#e3e3e3');
			$("#moduleUri").textbox('textbox').css('background-color','#e3e3e3');
			$("#moduleDesc").textbox('textbox').css('background-color','#e3e3e3');
			
			$("#treeUl").tree({
				checkbox:false,
				url:'module/tree',
				lines:true,
				onSelect:function(node) {
					//alert(node.text + node.uri);
					if(node.pid!='root') {
						
						currSelectNodeId = node.id;
						
						$("#moduleCode").textbox('setValue',node.id);
						$("#moduleName").textbox('setValue',node.text);
						$("#moduleUri").textbox('setValue',node.uri);
						$("#moduleDesc").textbox('setValue',node.desc);
						$("#parentModule_edit").attr("onclick","moduleEdit(\"" + node.id + "\",\"" + node.text +"\",\"" + node.desc + "\",\"" + node.uri + "\",\"" + node.pid + "\")");
						$("#parentModule_delete").attr("onclick","moduleDel(\"" + node.id + "\",\"" + node.pid + "\")");
	
						$("#moduleDg").datagrid({
							pageSize:15,
							pagination:true,      
							fit:true,
							singleSelect:true,
							rowrap:true,
							striped: true,
							rownumbers: true,
							pageList:[10,15,20],
							url:'module/datagrid?moduleCode=' + node.id
						});
					}
				},
				onLoadSuccess:function(node,data) {
					if(currSelectNodeId==null || currSelectNodeId=="") {
						//var n2 = $("#treeUl").tree("getRoot");
						//$("#treeUl").tree("select",n2.target);
					}else {
						var currNode = $("#treeUl").tree('find',currSelectNodeId);
						$("#treeUl").tree("select",currNode.target);
					}
				}
			});

			$("#moduleDg").datagrid({
				//url:'module/datagrid',
				toolbar:'#searchtool'
			});

			$("#addModuleDlg").dialog({
				onClose:function() {
					$("#moduleForm").form("clear");
				}
			});
		});

		//格式化：在每行输出 修改及删除
		function rowformater(value,data,index) {
			return "<a href='#' onclick='javascript:moduleEdit(\"" + data.MODULE_CODE +"\",\""+ data.MODULE_NAME +"\",\"" + data.MODULE_DESC + "\",\"" + data.MODULE_URI + "\",\"" + data.PARENT_CODE + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:moduleDel(\"" + data.MODULE_CODE + "\",\""+ data.PARENT_CODE + "\")'><img src='themes/icons/cancel.png' border='0'>删除</a>";
		}

		function moduleEdit(moduleCode,moduleName,moduleDesc,moduleUri,parentCode) {
			$("#addModuleDlg").dialog("open").dialog("setTitle","修改菜单");

			$("#saveBtn").attr("onclick","saveEdit()");

			$("#MODULE_CODE").textbox('setValue',moduleCode);
			$("#MODULE_NAME").textbox('setValue',moduleName);
			$("#MODULE_DESC").textbox('setValue',moduleDesc);
			$("#MODULE_URI").textbox('setValue',moduleUri);
			$("#PARENT_CODE").val(parentCode);
			
			$("#MODULE_CODE").textbox("readonly",true);
			$("#MODULE_CODE").textbox('textbox').css('background-color','#e3e3e3');
			
			//alert(moduleCode + "  " + moduleName + " " + moduleDesc + "  " + moduleUri);
			
		}

		//moduleType 如果是root时，表示这是添加根目录，否则就是普通菜单
		function moduleAdd(moduleType) {
			$("#saveBtn").attr("onclick","saveAdd()");
						
			$("#PARENT_CODE").val($("#moduleCode").val());
			if(moduleType=='root') {
				$("#PARENT_CODE").val("-1");
			}

			$("#MODULE_CODE").textbox("readonly",false);
			$("#MODULE_CODE").textbox('textbox').css('background-color','');
			
			$("#addModuleDlg").dialog("open").dialog("setTitle","增加菜单");
		}

		function moduleDel(moduleCode,parentCode) {

			if(moduleCode == null) {
				$.messager.alert("提示","请选择要删除的记录","warnning");
			}

			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$("#moduleForm").form("submit",{
						url:"module/delete?moduleCode=" + moduleCode,
						onSubmit:function(){},
						success:function(data){
							var result = JSON.parse(data);    //解析Json 数据
							
							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息

							window.parent.showMessage(message,statusCode);
							if(statusCode=="success") {
								$("#moduleDg").datagrid({
									url:'module/datagrid?moduleCode=' + currSelectNodeId
								});

								$("#treeUl").tree({
									url:'module/tree'
								});
									
							}
						}
					});
				}
			});

			
		}

		function saveEdit() {
			$("#moduleForm").form('submit',{
				url:"module/update",
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
					if(statusCode == 'success') {         //成功
						$('#addModuleDlg').dialog('close');//关闭对话框
						$("#moduleDg").datagrid({
							url:'module/datagrid?moduleCode=' + currSelectNodeId
						});
						$("#treeUl").tree({
							url:'module/tree'
						});
					}
				}
			});

		}

		function saveAdd() {

			$("#moduleForm").form('submit',{
				url:"module/add",
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
					if(statusCode == 'success') {         //成功
						$('#addModuleDlg').dialog('close');//关闭对话框
						$("#moduleDg").datagrid({
							url:'module/datagrid?moduleCode=' + currSelectNodeId
						});
						$("#treeUl").tree({
							url:'module/tree'
						});
					}
				}
			});
			
		}
		
		function add_cancel() {
			$('#addModuleDlg').dialog('close');//关闭对话框
		}

	</script>
</head>
<body id="orgBody" style="margin-top:1px;margin-left:1px;" data-options="fit:true">
	<div class="easyui-panel" title="菜单管理" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 左侧的树形 -->
			<div data-options="region:'west',split:true" style="width:220px;padding:10px">
				<ul id="treeUl" class="easyui-tree">
				</ul>
			</div>

			<!-- 显示区 -->
			<div data-options="region:'center'" style="padding:1px">
				<div class="easyui-layout" data-options="fit:true">
					<!-- 编辑区上半部分 -->
					<div data-options="region:'north',split:true,border:false" style="height:93px">
<!--						<div class="easyui-panel" title="菜单修改">-->
						<div class="easyui-panel" title="菜单信息" data-options="tools:'#panel-tool'">
							<table>
								<tr>
									<td>菜单编码：
										<input id="moduleCode" style="width:200px;" type="text" class="easyui-textbox"/>
									</td>
									
									<td style="padding-left:30px;">菜单名称：
										<input id="moduleName" style="width:200px;" type="text" class="easyui-textbox"/>
									</td>
								</tr>
								<tr>
									<td>菜单链接：
										<input id="moduleUri" style="width:200px;" type="text" class="easyui-textbox"/>
									</td>
									<td style="padding-left:30px;">菜单描述：
										<input id="moduleDesc" style="width:200px;" type="text" class="easyui-textbox"/>
										
										<span style="padding-left:30px;">
											<a href="#" id="parentModule_edit" class="easyui-linkbutton" data-options="iconCls:'icon-edit'">编辑</a>
											&nbsp;&nbsp;<a href="#" id="parentModule_delete" class="easyui-linkbutton" data-options="iconCls:'icon-cancel'">删除</a>
										</span>
									</td>
								</tr>
							</table>
						</div>
					</div>
					
					<!-- 编辑区下半部分列表 -->
					<div data-options="region:'center',split:true,border:false">
						<table id="moduleDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'MODULE_NAME',width:150,align:'center'">菜单名称</th>                
									<th data-options="field:'MODULE_URI',width:300,align:'center'">菜单链接</th>
									<th data-options="field:'MODULE_DESC',width:200,align:'center'">菜单编码</th>                
									<th data-options="field:'id',width:100,align:'center',formatter:rowformater">操作</th>
								
								</tr>        
							</thead>
						</table>	
					</div>

					<div id="searchtool" style="padding:5px">  
							<div>	
								<a href="#" id="easyui-add" onclick="moduleAdd('root')" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加根菜单</a>
								<a href="#" id="easyui-add" onclick="moduleAdd()" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加子菜单</a>
							</div>  
					 <div>
				</div>
			</div>
		</div>
		
	</div>

	<div id="addModuleDlg" class="easyui-dialog" style="width:800px;height:300px;padding:10px 20px;" modal="true" closed="true" buttons="#addModuleDlgBtn">
		<form id="moduleForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/system/module/_form.jsp"%>
		</form>	
	</div>	


</body>
</html>

