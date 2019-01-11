<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	
	function execSelectCallerIdGroup() {
		$("#sys_callerid_group_Dg").datagrid({
			pageSize:30,
			pagination:true,
			fit:true,
			toolbar:"#datagridTool",
			singleSelect:true,
			rownumbers:true,
			rowrap:true,
			striped:true,
			pageList:[20,30,50],
			url:'sysCallerIdGroup/datagrid',
			queryParams:{
				groupName:$("#groupName").textbox('getValue')
			}
		});
	}
	
	function callerIdGroup_findData() {
		$("#sys_callerid_group_Dg").datagrid("reload",{
			groupName:$("#groupName").textbox('getValue')
		});
	}
	
</script>

<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:50px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>
					主叫组名称：<input id="groupName" class="easyui-textbox" style="width:200px;"/>
					<span style="padding-left:20px;">
						<a href="javascript:callerIdGroup_findData()" style="width:120px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
					</span>
				</td>
			</tr>
		</table>
	</div>
	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="sys_callerid_group_Dg">
			<thead>
				<tr style="height:12px;">
					<th data-options="field:'GROUP_NAME',width:200,align:'center'">主叫号码组名</th>
					<th data-options="field:'CREATE_USERCODE_DESC',width:300,align:'center'">创建人</th>
					<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
				</tr>
			</thead>
		</table>
	</div>
</div>