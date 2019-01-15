<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script type="text/javascript">
	
	function initOperatorDg() {
		$("#operatorDg").datagrid({
			pageSize:30,
			pagination:true,      
			fit:true,
			singleSelect:false,
			toolbar:"#operatorSelectTool",
			rowrap:true,
			striped: true,
			rownumbers: true,
			pageList:[20,30,50],
			url:'operator/datagrid',
			queryParams:{
				operId:$('#operId').textbox('getValue'),
				operName:$('#operName').textbox('getValue')
			}
		});
	}

	function findData_Operator() {
		$("#operatorDg").datagrid("reload",{
			operId:$('#operId').textbox('getValue'),
			operName:$('#operName').textbox('getValue')
		});
	}
	
	function getOperatorSelectedRowsForOperIdList() {
		var rows = $('#operatorDg').datagrid('getSelections');
		var ids = [];
		for(var i=0; i<rows.length; i++){
			ids.push(rows[i].OPER_ID);
		}
		return	ids.join(",");
	}
	
	function getOperatorSelectedRowsForOperNameList() {
		var rows = $('#operatorDg').datagrid('getSelections');
		var ids = [];
		for(var i=0; i<rows.length; i++){
			ids.push(rows[i].OPER_NAME);
		}
		return	ids.join(",");
	}
	
	function confirmOperator() {
		$("#operIdList").val(getOperatorSelectedRowsForOperIdList());
		$("#operNameList").textbox('setValue',getOperatorSelectedRowsForOperNameList());
		$("#operatorDlg").dialog('close');
	}
	
	function clearOperIdList() {
		$("#operIdList").val('');
		$("#operNameList").textbox('setValue','');
	}
	
</script>
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:50px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>
					<span style="">操作员工号：</span><input type="text" class="easyui-textbox" id="operId" style="width:150px;" />  
			        <span style="padding-left:30px;">操作员名字：</span><input type="text" id="operName" class="easyui-textbox" style="width:150px;" />
       				<span style="padding-left:30px;">
			        	<a href="javascript:findData_Operator()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
       				</span>
				</td>
			</tr>
		</table>
	</div>
	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="operatorDg">
				<thead>  
					<tr style="height:12px;">
						<th data-options="field:'ck',checkbox:true"></th>                
						<th data-options="field:'OPER_ID',width:200,align:'center'">操作员工号</th>                
						<th data-options="field:'OPER_NAME',width:300,align:'center'">操作员名称</th>                
						<th data-options="field:'ORG_CODE_DESC',width:300,align:'center'">所属组织</th>                
					</tr>        
				</thead>
		</table>
	</div>
</div>

<div id="operatorSelectTool" style="padding:5px;">
	<a href="#" id="confirmOperatorBtn" onclick="confirmOperator()" class="easyui-linkbutton" iconCls='icon-add' plain="true">提交选中的操作员</a>
</div>