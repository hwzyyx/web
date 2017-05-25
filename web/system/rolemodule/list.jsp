<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>角色菜单管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		//数据列表赋值
		var currRoleCode = null;
		$(function(){
			
			$("#roleModuleDg").datagrid({
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				url:'role/datagrid'
			});	
		});

		//数据查询
		function FindData(){  
			        $('#roleModuleDg').datagrid('load',{  
			            roleCode:$('#roleCode').val(),  
			            roleName:$('#roleName').val(),
		            	roleState:$('#roleState').combobox('getValue')}  
			            );  
			    }  

	</script>	
	<script type="text/javascript">
		//格式化：在每行输出 修改及删除
		function rowformater(value,data,index) {
			return "<a href='#' onclick='javascript:roleModuleAuth(\"" + data.ROLE_CODE +"\")'><img src='themes/icons/pencil.png' border='0'>角色授权</a>";
		}

		function roleModuleAuth(roleCode) {
			//$.messager.alert("Info",roleCode,"info");
			$("#roleModuleAuthDlg").dialog("open").dialog("setTitle",'角色授权');
			currRoleCode = roleCode;
			
			$("#treeUl").tree({
				url:'module/tree',
				animate:true,
				checkbox:true,
				onLoadSuccess:function(node,data) {    //在tree列表成功后，设置默认选中项

					$.ajax({
						type:'POST',
						dataType:"json",
						url:"roleModule/getRoleModuleByRoleCode?roleCode=" + roleCode,
						success:function(rs) {
	
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息

							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								//alert(message);
								var moduleCodes = message.split(",");
								for(var i=0;i<moduleCodes.length;i++) {
									var n = $("#treeUl").tree('find',moduleCodes[i]);    //先根据id,查找出 node
									$("#treeUl").tree('check',n.target);                 //然后根据 node.target 进行选中
								}
							}
						}
					});
					
				}
			});
			
		}

		//格式化：将状态格式化，如果状态值为1,则为绿色，且定义为有效；状态值为0，则为红色，且定义为无效
		function roleStateFormatter(val,data) {
			if(val==1) {
				return '<span style="color:green;">有效</span>';
			}else {
				return '<span style="color:red;">无效</span>';
			}
		}

		function auth_cancel() {
			$("#roleModuleAuthDlg").dialog('close');
		}

		//取得已经选择
		function getChecked() {
			var nodes = $("#treeUl").tree('getChecked');

			for(var i=0; i<nodes.length; i++) {
				alert(nodes[i].text);
			}
			
			var nodes2 = $("#treeUl").tree('getSelected');

			for(var j=0; j<nodes2.length; j++) {
				alert(nodes2[i].text);
			}
		}
		
		//保存授权
		function saveAuth() {
			var moduleCode = "";
			var nodes = $("#treeUl").tree('getChecked');

			for(var i=0; i<nodes.length; i++) {
				//alert(nodes[i].text);
				moduleCode += nodes[i].id + ",";
			}

			//alert(moduleCode);
			//alert(currRoleCode);

			$("#roleModuleAuthForm").form('submit',{
				url:'roleModule/auth?roleCode=' + currRoleCode + "&moduleCode=" + moduleCode,
				success:function(data) {
					var result = JSON.parse(data);    //解析Json 数据
					
					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息
					
					$.messager.alert('Info',message,'info');
				}
			});
			
		}

	</script>
</head>

<body>

<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:42px;padding-top:5px;padding-left:5px;">
		<span>角色编码：</span><input type="text" style="width:150px;" class="easyui-textbox" id="roleCode" />  
        <span style="padding-left:30px;">角色名称：</span><input type="text" style="width:150px;" class="easyui-textbox" id="roleName" />
		<span style="padding-left:30px;">状态：</span><select class="easyui-combobox" style="width: 155px;" id="roleState" data-options="panelHeight:'auto'">
              <option value="2">请选择</option>
              <option value="1">有效</option>
              <option value="0">无效</option>
        </select>  
        <span style="padding-left:30px;">
        	<a href="javascript:FindData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
        </span>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="roleModuleDg">
			<thead>  
				<tr style="height:12px;">                
					<th data-options="field:'ROLE_CODE',width:100,align:'center'">角色编码</th>                
					<th data-options="field:'ROLE_NAME',width:100,align:'center'">角色名称</th>                
					<th data-options="field:'ROLE_DESC',width:200,align:'center'">角色描述</th>                
					<th data-options="field:'ROLE_STATE',width:100,align:'center',formatter:roleStateFormatter">状态</th>
					<th data-options="field:'CREATETIME',width:150,align:'center'">创建时间</th>
					<th data-options="field:'id',width:120,align:'center',formatter:rowformater">操作</th>
				</tr>        
			</thead>
		</table>
	</div>
</div>

<div id="roleModuleAuthDlg" class="easyui-dialog" style="width:400px;height:430px;padding:10px 20px;" modal="true" closed="true" buttons="#saveRoleModuleAuthBtn">
	<form id="roleModuleAuthForm" method="post">
		<!-- 包含表单 -->
		<%@ include file="/system/rolemodule/_form.jsp"%>
	</form>	
</div>

</body>
</html>

