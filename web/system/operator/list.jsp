<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>操作工号管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="custom_js/custom_messager.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		
		var currSelectNodeId = null;    //当前选择的组织Id
		var currOperId = null;          //当前的 OperId
		$(function(){
			$("#treeUl").tree({
				checkbox:false,
				url:'org/tree',
				lines:true,
				onSelect:function(node) {
					//alert("onSelect" + node.text + "," + node.id + "," + node.pid);
					currSelectNodeId = node.id;
					$("#operId").val("");
					$("#operName").val("");
					$("#operState").combobox("setValue","2");    //当设置为2时，为 "请选择"
					$('#operatorDg').datagrid({url:'operator/datagrid?orgCode=' + node.id});//实现Datagrid重新刷新效果
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

			$("#operatorDg").datagrid({
				pageSize:10,
				pagination:true,      
				fit:true,
				singleSelect:true,
				toolbar:"#opertool",
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				url:'operator/datagrid?orgCode=' + currSelectNodeId 
			});

			
			$("#roleDg").datagrid({
				title:'角色信息',
				pageSize:10,
				fit:true,
				singleSelect:false,
				//toolbar:"#searchtool2",
				rowrap:true,
				striped: true,
				rownumbers: true,
				checkbox:true,
				pageList:[10,30,50],
				url:'role/datagrid',
				pagination:true,      
				idField:'ROLE_CODE',
				onLoadSuccess:function(data) {
					//检查当前操作员对应的角色
					if(currOperId!=null&&currOperId != "") {
						$("#operatorForm").form('submit',{
							url:"operator/getRoleByOperId?operId=" + currOperId,
							onSubmit:function() {
							},
							success:function(rs) {
								
								var result = JSON.parse(rs);    //解析Json 数据

								var statusCode = result.statusCode; //返回的结果类型
								var message = result.message;       //返回执行的信息
								
								if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
									//alert(message);
									var roleCodes = message.split(",");
									for(var i=0;i<roleCodes.length;i++) {
										//alert(roleCodes[i]);
										$("#roleDg").datagrid('selectRecord',roleCodes[i]);
									}
								}
							}
						});
					}
					
				}
			});

			//用于关闭弹出窗时的操作，用于清理表单数据
			$("#addOperatorDlg").dialog({
				onClose:function() {
					$("#operatorForm").form("clear");
					currOperId=null;
					$("#roleDg").datagrid("clearSelections");
				}
			});
		});
		
		function operatorAdd() {
			$("#addOperatorDlg").dialog("open").dialog("setTitle","添加操作员");

			//$("#PARENT_ORG_CODE").val($("#orgCode").val());
			$("#OPER_ID").textbox("readonly",false);
			$("#OPER_ID").textbox('textbox').css('background-color','');

			
			$("#saveBtn").attr("onclick","saveAdd()");
			$("#OPER_ID").attr("onblur","operIdBlur()");   //添加事件绑定

			$("#PWD_DIV").css("display","");
			$("#SEX").combobox("setValue","1");
			$("#STATE").combobox('setValue',"1");
			$('#roleDg').datagrid('load',{});//实现Datagrid重新刷新效果
		}

		//修改操作员
		function operatorEdit(operId,operName,state,sex,password,telno,orgCode,callNumber) {

			currOperId = operId;
			
			//alert("operId:" + operId + ',operName:' + operName + ',state:' + state + ',sex:' + sex + ',email:' + email + ',telno:' + telno + ',orgCode:' + orgCode + ',callNumber:' + callNumber);
			$("#addOperatorDlg").dialog("open").dialog("setTitle","修改操作员");

			$("#saveBtn").attr('onclick','saveEdit()');
			$("#OPER_ID").removeAttr("onblur");
			
			//operId不允许编辑
			$("#OPER_ID").textbox("readonly",true);
			$("#OPER_ID").textbox('textbox').css('background-color','#e3e3e3');

			$("#PWD_DIV").css("display","none"); 
			  
			$("#OPER_ID").textbox('setValue',operId);
			$("#OPER_NAME").textbox('setValue',operName);
			$("#STATE").combobox('setValue',state);
			$("#SEX").combobox('setValue',sex);
			$("#TELNO").numberbox('setValue',telno);
			$("#CALL_NUMBER").numberbox('setValue',callNumber);
			//$("#PWD_ID").val(password);
			$("#ORG_CODE").val(orgCode);

			$('#roleDg').datagrid('load');//实现Datagrid重新刷新效果
			
			$("#OPER_ID").blur(function(){});
		}

		/**
		* 在添加工号时做检查用
		*/
		function operIdBlur() {
			var operId = $("#OPER_ID").val();
			//失去焦点时，检查工号维一性
			if(operId!=null && operId!="") {   //只有当不为空时才进行唯一性检查
				$("#operatorForm").form('submit',{
					url:"operator/chkOperId?operId=" + operId,
					onSubmit:function(){},
					success:function(data) {
						if(data!=null && data!="") {
							var result = JSON.parse(data);    //解析Json 数据
	
							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息

							$.messager.alert("提示",message,'info',function(){
								$("#OPER_ID").focus();
							});
						}
					}
				});
			}
		}
		
		function add_cancel() {
			$('#addOperatorDlg').dialog('close');//关闭对话框
		}
		
		//修改删除
		function operatorDel(operId) {
			if(operId == null) {
				$.messager.alert('提示', '删除记录失败，请选择要删除的行！','info');
			};

			$.messager.confirm('提示','你确定要删除这条信息吗?',function(r){
				if(r) {
					$("#operatorForm").form('submit',{
						url:"operator/delete?operId=" + operId,
						onSubmit:function(){
							
						},
						success:function(data) {
	
							var result = JSON.parse(data);    //解析Json 数据
	
							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息
	
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //删除成功，重新加载数据
								$("#operatorDg").datagrid({url:'operator/datagrid?orgCode=' + currSelectNodeId});
							}
						}
					});
				}
			});
		}
		
		//重置密码
		function operatorInitPassword(operId) {
			
			var newPassword = "aaa123";
			
			$.messager.confirm('提示','你确定要重置操作员  '+ operId + ' 的密码为' + newPassword + ' 吗?',function(r){
				
				if(r) {
					
					$("#operatorForm").form('submit',{
						url:"operator/initPassword?operId=" + operId + "&newPassword=" + newPassword,
						onSubmit:function() {
							
						},
						success:function(data) {
							var result = JSON.parse(data);    //解析Json 数据
							
							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息
	
							window.parent.showMessage(message,statusCode);
							
							//由于是重置密码，并不需要重新刷新加载列表
						}
					});
				}
			});
			
		}

		function saveAdd() {
			var ids = [];      //记录已经选中roleCode
			var rows = $("#roleDg").datagrid("getSelections");    //获取已经选中的行
			for(var i=0;i<rows.length;i++) {
				ids.push(rows[i].ROLE_CODE);
			}
			$("#operatorForm").form('submit',{
				url:"operator/add?ids=" + ids + "&orgCode=" + currSelectNodeId,
				onSubmit:function(){

					var v = $(this).form('validate');

					if(v) {

						var pwd = $("#PWD_ID").val();  
						if(pwd==null || pwd=='') {   //判断密码是否已输入
							$.messager.alert("提示","密码不能为空!","info");
							return false;
						}
			

						if(rows.length==0) { //如果没有选择角色时，提示错误
							$.messager.alert("提示","请选择角色!","info");
							return false;
						}
						
						$.messager.progress({
							msg:'系统正在处理，请稍候...',
							interval:3000
						});
					}else {
						return $(this).form('validate');
					}
					
					return $(this).form('validate');
				},
				success:function(data) {

					$.messager.progress("close");

					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if('success') {         //保存成功时，才关闭窗口及加载数据
						$("#operatorDg").datagrid({url:'operator/datagrid?orgCode=' + currSelectNodeId});
						$("#addOperatorDlg").dialog("close");
					}
				}
			});
			
		}

		function saveEdit() {
			var ids = [];      //记录已经选中roleCode
			var rows = $("#roleDg").datagrid("getSelections");    //获取已经选中的行
			for(var i=0;i<rows.length;i++) {
				ids.push(rows[i].ROLE_CODE);
			}
			//alert("rows的值:" + rows + ",ids=" +ids );
			$("#operatorForm").form('submit',{
				url:"operator/update?ids=" + ids + "&orgCode=" + currSelectNodeId,
				onSubmit:function(){

					var v = $(this).form('validate');

					if(v) {

						if(rows.length==0) { //如果没有选择角色时，提示错误
							$.messager.alert("提示","请选择角色!","info");
							return false;
						}
						
						$.messager.progress({
							msg:'系统正在处理，请稍候...',
							interval:3000
						});
					}else {
						return $(this).form('validate');
					}
					
					return $(this).form('validate');
				},
				success:function(data) {

					$.messager.progress("close");

					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if('success') {         //保存成功时，才关闭窗口及加载数据
						$("#operatorDg").datagrid({url:'operator/datagrid?orgCode=' + currSelectNodeId});
						$("#addOperatorDlg").dialog("close");
					}
				}
			});

		}

		function stateFormat(val,data,index){
			if(val==1) {
				return '<span style="color:green;">有效</span>';
			}else {
				return '<span style="color:red;">无效</span>';
			}
		}

		function sexFormat(val,data,index){
			if(val==1) {
				return '<span style="color:red;">男</span>';
			}else {
				return '<span style="color:red;">女</span>';
			}
		}

		//格式化：在每行输出 修改及删除
		function rowformater(value,data,index) {
			return "<a href='#' onclick='javascript:operatorEdit(\"" + data.OPER_ID +"\",\""+ data.OPER_NAME +"\",\"" + data.STATE + "\",\"" + data.SEX + "\",\"" + data.PASSWORD + "\",\"" + data.TELNO + "\",\"" + data.ORG_CODE + "\",\"" + data.CALL_NUMBER + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"&nbsp;&nbsp;<a href='#' onclick='javascript:operatorDel(\"" + data.OPER_ID +"\")'><img src='themes/icons/cancel.png' border='0'>删除</a>" +
			"&nbsp;&nbsp;<a href='#' onclick='javascript:operatorInitPassword(\"" + data.OPER_ID +"\")'><img src='themes/icons/reload.png' border='0'>重置密码为:aaa123</a>";
		}
		
		//格式化：将状态格式化，如果状态值为1,则为绿色，且定义为有效；状态值为0，则为红色，且定义为无效
		function roleStateFormatter(val,data) {
			if(val==1) {
				return '<span style="color:green;">有效</span>';
			}else {
				return '<span style="color:red;">无效</span>';
			}
		}
		
		//数据查询
		function FindData(){  
			   $('#operatorDg').datagrid('load',{  
			         operId:$('#operId').val(),  
			         operName:$('#operName').val(),
		             operState:$('#operState').combobox('getValue')}  
			    );  
		}
		
	</script>
</head>
<body id="orgBody" style="margin-top:1px;margin-left:1px;">
	<!-- 页面加载效果 -->
	<%@ include file="/base_loading.jsp" %>
	<div class="easyui-panel" title="操作员管理" data-options="fit:true" style="padding:1px;">
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
						<span style="">工号：</span><input type="text" class="easyui-textbox" id="operId" style="width:150px;" />  
				        <span style="padding-left:30px;">操作员：</span><input type="text" id="operName" class="easyui-textbox" style="width:150px;" />
						<span style="padding-left:30px;">状态：</span>
							<select class="easyui-combobox" style="width: 155px;" id="operState" data-options="panelHeight:'auto'">
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
						<table id="operatorDg">
								<thead>  
									<tr style="height:12px;">                
										<th data-options="field:'OPER_ID',width:100,align:'center'">工号</th>                
										<th data-options="field:'ORG_CODE',width:100,align:'center'">所属组织</th>                
										<th data-options="field:'OPER_NAME',width:100,align:'center'">操作员名称</th>                
										<th data-options="field:'STATE',width:80,align:'center',formatter:stateFormat">状态</th>
										<th data-options="field:'SEX',width:60,align:'center',formatter:sexFormat">性别</th>
										<th data-options="field:'TELNO',width:120,align:'center'">联系电话</th>
										<th data-options="field:'CALL_NUMBER',width:70,align:'center'">座席号码</th>
										<th data-options="field:'CREATETIME',width:110,align:'center'">创建时间</th>
										<th data-options="field:'id',width:300,align:'center',formatter:rowformater">操作</th>
									
									</tr>        
								</thead>
						</table>
					</div>
				</div>
			</div>
		</div>
		
	</div>
	
	<div id="opertool">
		<a href="#" id="easyui-add" onclick="operatorAdd()" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加</a>
	</div>

	<div id="addOperatorDlg" class="easyui-dialog" style="width:1000px;height:410px;padding:5px 5px;" modal="true" closed="true" buttons="#addOperatorDlgBtn">
		<form id="operatorForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/system/operator/_form.jsp"%>
		</form>	
	</div>	
	
</body>
</html>

