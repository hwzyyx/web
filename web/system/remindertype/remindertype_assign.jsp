<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>催缴类型分配</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="custom_js/custom_messager.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="system/callerid/_callerid.js"></script>
	<script type="text/javascript">
		
		var currSelectNodeId = null;    //当前选择的组织Id
		var currOperId = null;          //当前的 OperId
		var targetOperId = null;        //目标操作员
		
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
			//主叫号码列表
			$("#reminderTypeDg").datagrid({
	    		
    			pageSize:100,
				pagination:true,
				fit:true,
				toolbar:'#reminderTypeTool',
				singleSelect:false,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[50,100,200],
				url:'sysReminderTypeAssign/datagrid',
				idField:'ID',
				onLoadSuccess:function(data) {
					if(targetOperId!=null) {
						$.ajax({
							url:'sysReminderTypeAssign/getSysReminderTypeAssignResult?targetOperId=' + targetOperId,
							method:'post',
							dataType:'json',
							success:function(rs) {
								var statusCode = rs.statusCode; //返回的结果类型
								var message = rs.message;       //返回执行的信息
								//window.parent.showMessage(message,statusCode);
								if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
									var callerid_ids = message.split(",");
									for(var i=0;i<callerid_ids.length;i++) {
										$("#reminderTypeDg").datagrid('selectRecord',callerid_ids[i]);
									}
								}
							}
						});
					}
				}
    			
    		});
			
			$("#reminderTypeAssignDlg").dialog({
				onClose:function() {
					targetOperId=null;
					$("#reminderTypeDg").datagrid("clearSelections");
				}
			});
			
			$("#addReminderTypeDlg").dialog({
    			onClose:function() {
    				$("#reminderTypeForm").form('clear');
    			}
    		});
		});
		
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
			return "<a href='#' onclick='javascript:reminderTypeAssign(\"" + data.OPER_ID +"\",\""+ data.OPER_NAME + "\")'><img src='themes/icons/dial.png' border='0'>催缴类型分配</a>";
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
		
		//主叫号码分配
		function reminderTypeAssign(operId,operName) {
			
			targetOperId = operId;
			
			$("#reminderTypeDg").datagrid("load",{
				targetOperId:targetOperId
			});
			
			$("#reminderTypeAssignDlg").dialog('setTitle',"操作员：" + operName + "(" + operId + ")的催缴类型分配").dialog('open');
			
		}
		
		//保存催缴类型的分配
		function saveSysReminderTypeAssign() {
			
			ids = getSelectedRows();       //取得选中的 ID
			
			$.ajax({
				url:'sysReminderTypeAssign/saveSysReminderTypeAssign?targetOperId=' + targetOperId + "&ids=" + ids,
				method:'post',
				dataType:'json',
				success:function(rs) {
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
						$("#reminderTypeDg").datagrid("load",{
							
						});
					}
				}
			});
			
		}
		
		//取得选中的号码数据			
		function getSelectedRows() {
			
			var rows = $('#reminderTypeDg').datagrid('getSelections');
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].ID);
			}
			return	ids.join(",");			
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
										<th data-options="field:'ORG_CODE_DESC',width:100,align:'center'">所属组织</th>                
										<th data-options="field:'OPER_NAME',width:100,align:'center'">操作员名称</th>                
										<th data-options="field:'STATE',width:80,align:'center',formatter:stateFormat">状态</th>
										<th data-options="field:'SEX',width:60,align:'center',formatter:sexFormat">性别</th>
										<th data-options="field:'TELNO',width:120,align:'center'">联系电话</th>
										<th data-options="field:'CALL_NUMBER',width:70,align:'center'">座席号码</th>
										<th data-options="field:'CREATETIME',width:200,align:'center'">创建时间</th>
										<th data-options="field:'id',width:150,align:'center',formatter:rowformater">操作</th>
									
									</tr>        
								</thead>
						</table>
					</div>
				</div>
			</div>
		</div>
		
	</div>
	<!-- 主叫分配弹窗 -->
	<div id="reminderTypeAssignDlg" class="easyui-dialog" style="width:80%;height:80%;padding:10px 20px;" modal="true" closed="true" buttons="#reminderTypeAssignDlgBtn">
		<table id="reminderTypeDg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'ck',checkbox:true"></th>		
						<th data-options="field:'REMINDER_TYPE',width:200,align:'center'">催缴类型</th>
						<th data-options="field:'NUMBER_ORDER',width:400,align:'center'">催缴序号</th>
						<th data-options="field:'CREATE_USERCODE_DESC',width:300,align:'center'">创建人</th>
						<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
					</tr>
					
				</thead>
			</table>
	</div>
	<div id="reminderTypeAssignDlgBtn">
		<a href="#" id="easyui-add" onclick="saveSysReminderTypeAssign()" class="easyui-linkbutton" iconCls="icon-ok" plain="true">保存催缴类型分配</a>
	</div>
	
	<div id="reminderTypeTool" style="padding:5px;">
		<a href="#" id="easyui-add" onclick="reminderTypeAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增催缴类型</a>
	</div>
	
	<div id="addReminderTypeDlg" class="easyui-dialog" style="width:40%;height:40%;padding:10px 20px;" modal="true" closed="true" buttons="#addReminderTypeDlgBtn">

		<form id="reminderTypeForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/system/remindertype/_form.jsp"%>
		</form>	
	</div>
	
</body>
</html>

