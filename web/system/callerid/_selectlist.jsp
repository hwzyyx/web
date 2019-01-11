<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	
	function initSelectCallerIdAssign() {
		
		$("#callerIdDg").datagrid({
			pageSize:100,
			pagination:true,
			fit:true,
			toolbar:'#calleridSelectTool',
			singleSelect:false,
			rownumbers:true,
			rowrap:true,
			striped:true,
			pageList:[50,100,200],
			url:'sysCallerId/datagridForOperIdOrAutoCallTask',
			queryParams:{
				targetTaskId:null,
				callerId:$("#callerId").textbox('getValue'),					
				purpose:$("#purpose").textbox('getValue')
			}
		});
	}
	
	function callerId_findData_operId() {
		$("#callerIdDg").datagrid('load',{
			targetTaskId:null,
			callerId:$("#callerId").textbox('getValue'),					
			purpose:$("#purpose").textbox('getValue')
		});
	}
	
	function callerId_findData_taskId() {
		$("#callerIdDg").datagrid('load',{
			targetTaskId:currTaskId,
			callerId:$("#callerId").textbox('getValue'),					
			purpose:$("#purpose").textbox('getValue')
		});
	}
	
	function confirmCallerId() {
		
		$("#callerIdSpan").css("display","");
		
		$("#CALLERID").val(getCallerIdSelectedRowsForId());
		$("#CALLERID_NUMBER").textbox("setValue",getCallerIdSelectedRowsForCallerIdNumber);
		
		//还需要将主叫号码组的内容隐藏并消除
		$("#callerIdGroupSpan").css("display","none");
		$('#CALLERID_GROUP_ID').val('');
		$('#CALLERID_GROUP_NAME').textbox('setValue','');
		
		$("#callerIdDlg").dialog('close');
	}
	
	//取得选中的号码数据			
	function getCallerIdSelectedRowsForId() {
		
		var rows = $('#callerIdDg').datagrid('getSelections');
		var ids = [];
		for(var i=0; i<rows.length; i++){
			ids.push(rows[i].ID);
		}
		return	ids.join(",");			
	}
	
	//取得选中的号码数据			
	function getCallerIdSelectedRowsForCallerIdNumber() {
		
		var rows = $('#callerIdDg').datagrid('getSelections');
		var ids = [];
		for(var i=0; i<rows.length; i++){
			ids.push(rows[i].CALLERID);
		}
		return	ids.join(",");			
	}
	
</script>

<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:50px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>
					主叫号码：<input id="callerId" type="text" class="easyui-textbox" style="width:200px;"/>
					<span style="padding-left:30px;">
						号码用途：<input id="purpose" type="text" class="easyui-textbox" style="width:200px;"/>
					</span>
					<span id="callerIdSearchBtnByOperId" style="padding-left:30px;">
						<a href="javascript:callerId_findData_operId()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
					</span>
					<span id="callerIdSearchBtnByTaskId" style="padding-left:30px;display:none;">
						<a href="javascript:callerId_findData_taskId()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
					</span>
				</td>
			</tr>
		</table>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		
		<table id="callerIdDg">
			<thead>
				<tr style="height:12px;">
					<th data-options="field:'ck',checkbox:true"></th>		
					<th data-options="field:'CALLERID',width:200,align:'center'">主叫号码</th>
					<th data-options="field:'PURPOSE',width:400,align:'center'">号码用途</th>
					<th data-options="field:'CREATE_USERCODE_DESC',width:300,align:'center'">创建人</th>
					<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
				</tr>
				
			</thead>
		</table>	
		
	</div>
</div>

<div id="calleridSelectTool" style="padding:5px;">
	<a href="#" id="confirmCallerIdBtn" onclick="confirmCallerId()" class="easyui-linkbutton" iconCls='icon-add' plain="true">提交选中的号码</a>
</div>