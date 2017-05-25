<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>角色管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="jquery.jqprint-0.3.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		//数据列表赋值
		$(function(){
			
			$("#roleDg").datagrid({
				pageSize:15,
				pagination:true,      
				fit:true,
				toolbar:'#opertool',
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				url:'role/datagrid'
			});	

			$("#addRoleDlg").dialog({
				onClose:function() {
					$("#roleForm").form("clear");
				}
			});
			
		});

		//数据查询
		function FindData(){  
			        $('#roleDg').datagrid('load',{  
			            roleCode:$('#roleCode').val(),  
			            roleName:$('#roleName').val(),
		            	roleState:$('#roleState').combobox('getValue')}  
			            );  
			    }  

		//添加角色
		function roleAdd() {
			$("#addRoleDlg").dialog("open").dialog("setTitle","添加角色");

			$("#ROLE_CODE").textbox("readonly",false);
			$("#ROLE_CODE").textbox('textbox').css('background-color','#ffffff'); 

			$("#ROLE_STATE").combobox("setValue",'1');

			$("#saveBtn").removeAttr("onclick");
			$("#saveBtn").attr("onclick","saveAdd()");
			
		}

		//删除角色
		function roleDel(roleCode) {

			if(roleCode == null) {
				$.messager.alert('提示', '删除记录失败，请选择要删除的行！','info');
			};

			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$("#roleForm").form('submit',{
						url:"role/delete?roleCode=" + roleCode,
						onSubmit:function(){
							
						},
						success:function(data) {

							var result = JSON.parse(data);    //解析Json 数据

							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息

							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //保存成功时
								$('#roleDg').datagrid({url:'role/datagrid'});
								$('#addRoleDlg').dialog('close');//关闭对话框
							}
						}
					});
				}
			});
			
		}

		//编辑赋值
		function roleEdit(roleCode,roleName,roleDesc,roleState) {
			//可能状态没有赋值时，状态为null的字串，需要转化为无效
			if(roleState=='null') {
				roleState=0;
			}
			$("#addRoleDlg").dialog("open").dialog("setTitle","修改角色");

			$("#saveBtn").removeAttr("onclick");
			$("#saveBtn").attr("onclick","saveEdit()");
			
			//角色编码不允许编辑
			$("#ROLE_CODE").textbox("readonly",true);
			$("#ROLE_CODE").textbox('textbox').css('background-color','#e3e3e3'); 
			  
			$("#ROLE_CODE").textbox('setValue',roleCode);
			$("#ROLE_NAME").textbox('setValue',roleName);
			$("#ROLE_DESC").textbox('setValue',roleDesc);
			$("#ROLE_STATE").combobox("setValue",roleState);
			
		}
		
		function add_cancel() {
			$("#addRoleDlg").dialog("close");
		}

		function saveAdd() {

			$("#roleForm").form('submit',{
				url:"role/add",
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
						$('#roleDg').datagrid({url:'role/datagrid'});
						$('#addRoleDlg').dialog('close');//关闭对话框
					}
				}	
			});
		}

		function saveEdit() {
			$("#roleForm").form('submit',{
				url:"role/update",
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
						$('#roleDg').datagrid({url:'role/datagrid'});
						$('#addRoleDlg').dialog('close');//关闭对话框
					}
				}	
			});
		}
					
	</script>	
	<script type="text/javascript">
		//格式化：在每行输出 修改及删除
		function rowformater(value,data,index) {
			return "<a href='#' onclick='javascript:roleEdit(\"" + data.ROLE_CODE +"\",\""+ data.ROLE_NAME +"\",\"" + data.ROLE_DESC + "\",\"" + data.ROLE_STATE + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:roleDel(\"" + data.ROLE_CODE +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}

		//格式化：将状态格式化，如果状态值为1,则为绿色，且定义为有效；状态值为0，则为红色，且定义为无效
		function roleStateFormatter(val,data) {
			if(val==1) {
				return '<span style="color:green;">有效</span>';
			}else {
				return '<span style="color:red;">无效</span>';
			}
		}
		
	</script>
</head>

<body>

<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:42px;padding-top:5px;padding-left:5px;">
		<span>角色编码：</span><input type="text" id="roleCode" style="width:150px;" class="easyui-textbox" size=10 />  
        <span style="padding-left:30px;">角色名称：</span><input type="text" id="roleName" style="width:150px;" class="easyui-textbox" value="" size=10 />
		<span style="padding-left:30px;">状态：</span><select class="easyui-combobox" style="width: 155px;" id="roleState" data-options="panelHeight:'auto'">
              <option value="2">请选择</option>
              <option value="1">有效</option>
              <option value="0">无效</option>
        </select>
        <span style="padding-left:30px;">
	        <a href="javascript:FindData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">
				查询
			</a>
        </span>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="roleDg">
			<thead>  
			          
				<tr style="height:12px;">                
					<th data-options="field:'ROLE_CODE',width:100,align:'center'">角色编码</th>                
					<th data-options="field:'ROLE_NAME',width:100,align:'center'">角色名称</th>                
					<th data-options="field:'ROLE_DESC',width:200,align:'center'">角色描述</th>                
					<th data-options="field:'ROLE_STATE',width:100,align:'center',formatter:roleStateFormatter">角色状态</th>
					<th data-options="field:'CREATETIME',width:150,align:'center'">创建时间</th>
					<th data-options="field:'id',width:120,align:'center',formatter:rowformater">操作</th>
				</tr>        
			</thead>
		</table>
	</div>
</div>

<div id="opertool" style="padding:5px">  
		<a href="#" id="easyui-add" onclick="roleAdd()" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加</a>
</div>


<div id="addRoleDlg" class="easyui-dialog" style="width:580px;height:250px;padding:10px 20px;" modal="true" closed="true" buttons="#addRoleDlgBtn">

	<form id="roleForm" method="post">
		<!-- 包含表单 -->
		<%@ include file="/system/role/_form.jsp"%>
	</form>	
</div>

</body>
</html>

