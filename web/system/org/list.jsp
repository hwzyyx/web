<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>组织管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
    <script type="text/javascript" src="base-loading.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		
		var currSelectNodeId = null;
		$(function(){

			//父组织信息显示框先不允许写入和修改
			$("#orgCode").textbox("disable");
			$("#orgName").textbox("disable");
			$("#orgDesc").textbox("disable");
			
			$("#treeUl").tree({
				checkbox:false,
				url:'org/tree',
				lines:true,
				onSelect:function(node) {
					//alert("onSelect" + node.text + "," + node.id + "," + node.pid);
					currSelectNodeId = node.id;
					$("#orgCode").textbox('setValue',node.id);
					$("#orgName").textbox('setValue',node.text);
					$("#orgDesc").textbox('setValue',node.desc);
					$("#parentOrgCode").val(node.pid);
					$("#parentOrg-edit").attr("onclick","orgEdit(\"" + node.id + "\",\"" + node.text +"\",\"" + node.desc +"\",\"" + node.pid +"\")");
					
					$('#orgDg').datagrid({url:'org/show?moduleCode=' + node.id});//实现Datagrid重新刷新效果
				},
				onLoadSuccess:function(node,data) {
					if(currSelectNodeId==null || currSelectNodeId=="") {
						var n2 = $("#treeUl").tree("getRoot");
						$("#treeUl").tree("select",n2.target);
					}else {
						var currNode = $("#treeUl").tree('find',currSelectNodeId);
						$("#treeUl").tree("select",currNode.target);
					}
				}
			});

			$("#orgDg").datagrid({
				pageSize:10,
				//pagination:true,      
				fit:true,
				singleSelect:true,
				toolbar:"#opertool",
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20]
				//url:'org/show?moduleCode=' + $("#orgCode").val() 
			});


			//用于关闭弹出窗时的操作，用于清理表单数据
			$("#addOrgDlg").dialog({
				onClose:function() {
					$("#orgForm").form("clear");
				}
			});
			
		});

		function orgAdd() {
			$("#addOrgDlg").dialog("open").dialog("setTitle","添加");

			$("#ORG_CODE").textbox("readonly",false);
			$("#ORG_CODE").textbox('textbox').css('background-color','');
			
			$("#PARENT_ORG_CODE").val($("#orgCode").val());
			$("#saveBtn").removeAttr("onclick");
			$("#saveBtn").attr("onclick","saveAdd()");
		}

		//修改组织
		function orgEdit(orgCode,orgName,orgDesc,pid) {
			$("#addOrgDlg").dialog("open").dialog("setTitle","编辑");

			$("#saveBtn").removeAttr("onclick");
			$("#saveBtn").attr("onclick","saveEdit()");
			
			//组织编码不允许编辑
			$("#ORG_CODE").textbox("readonly",true);
			$("#ORG_CODE").textbox('textbox').css('background-color','#e3e3e3');
			  
			$("#ORG_CODE").textbox('setValue',orgCode);
			$("#ORG_NAME").textbox('setValue',orgName);
			$("#ORG_DESC").textbox('setValue',orgDesc);
			$("#PARENT_ORG_CODE").val(pid);
			
		}
		
		function add_cancel() {
			$("#addOrgDlg").dialog("close");
		}
		
		//修改删除
		function orgDel(orgCode) {
			if(orgCode == null) {
				$.messager.alert('提示', '删除记录失败，请选择要删除的行！','info');
			};

			$.messager.confirm('提示','你确定要删除这条信息吗?',function(r){
				if(r) {
					$("#orgForm").form('submit',{
						url:"org/delete?orgCode=" + orgCode,
						onSubmit:function(){
							$.messager.progress({
								msg:'系统正在处理，请稍候...',
								interval:3000
							});
						},
						success:function(data) {

							$.messager.progress("close");
							
							var result = JSON.parse(data);    //解析Json 数据
	
							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息

							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //删除成功时
								$('#treeUl').tree({url:'org/tree'});//实现数重新刷新效果
							}
						}
					});
				}
			});
		}

		//格式化：在每行输出 修改及删除
		function rowformater(value,data,index) {
			return "<a href='#' onclick='javascript:orgEdit(\"" + data.ORG_CODE +"\",\""+ data.ORG_NAME +"\",\"" + data.ORG_DESC + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:orgDel(\"" + data.ORG_CODE +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}

		function saveAdd() {

			$("#orgForm").form('submit',{
				url:"org/add",
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
					if(statusCode == 'success'){          //添加成功时，关闭窗口，并重加载树
						$('#addOrgDlg').dialog('close');//关闭对话框
						$('#treeUl').tree({url:'org/tree'});//实现数重新刷新效果
					}
				}
			});
			
		}

		function saveEdit() {

			var oc = $("#ORG_CODE").textbox('getValue');

			$("#orgForm").form('submit',{
				url:"org/update",
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
					if(statusCode == 'success') {         //失败
						$('#addOrgDlg').dialog('close');//关闭对话框
						$('#treeUl').tree({url:'org/tree'});//实现tree重新刷新效果();
					}
				}
			});

		}

	</script>
</head>
<body id="orgBody" style="margin-top:1px;margin-left:1px;">
	<div class="easyui-panel" title="组织管理" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 左侧的树形 -->
			<div data-options="region:'west',split:true" style="width:220px;padding:10px">
				<ul id="treeUl" class="easyui-tree">
				</ul>
			</div>

			<!-- 显示区 -->
			<div data-options="region:'center'" style="padding:1px">
			
				<!-- 定义一个 layout -->
				<div data-options="fit:true" class="easyui-layout">
					<!-- 顶部查询区 -->
					<div data-options="region:'north',split:true,border:true" style="height:42px;padding-top:5px;padding-left:5px;">
						<div style="background-color: #fff;">
					        <span style="font-weight: bold;">组织编码：</span><input type="text" id="orgCode" style="width:150px;" class="easyui-textbox" />  
					        <span style="font-weight: bold;padding-left:20px;">组织名称：</span><input type="text" id="orgName" style="width:150px;" class="easyui-textbox" />
					        <span style="font-weight: bold;padding-left:20px;">组织描述：</span><input type="text" id="orgDesc" style="width:150px;" class="easyui-textbox" />
					        <span style="font-weight: bold; display:none;">父组织:</span><input type="hidden" id="parentOrgCode" size=15 />
					        <span style="padding-left:20px;">
					        &nbsp;&nbsp;<a href="#" style="150px;" id="parentOrg-edit" class="easyui-linkbutton" data-options="iconCls:'icon-edit'">编辑</a>
					        </span>
						</div>
					</div>
				
					<!-- 数据显示区 -->
					<div data-options="region:'center',split:true,border:false">
						<table id="orgDg">
								<thead>  
									<tr style="height:12px;">                
										<th data-options="field:'ORG_CODE',width:200,align:'center'">组织编码</th>                
										<th data-options="field:'ORG_NAME',width:200,align:'center'">组织名称</th>                
										<th data-options="field:'ORG_DESC',width:300,align:'center'">组织描述</th>                
										<th data-options="field:'CREATETIME',width:150,align:'center',formatter:rowformater">操作</th>
									</tr>        
								</thead>
						</table>
					</div>
				</div>
			</div>
		</div>
		
	</div>
	
	<div id="opertool">	
		<a href="#" id="easyui-add" onclick="orgAdd()" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加子组织</a>
	</div>

	<div id="addOrgDlg" class="easyui-dialog" style="width:580px;height:250px;padding:10px 20px;" modal="true" closed="true" buttons="#addOrgDlgBtn">
		<form id="orgForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/system/org/_form.jsp"%>
		</form>	
	</div>	


</body>
</html>

