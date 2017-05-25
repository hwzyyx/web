<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>客户接触记录</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
    <script type="text/javascript" src="base-loading.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		$(function(){

			//$("#startTime").datebox("setValue",getCurrDate(1));
			//$("#endTime").datebox("setValue",getCurrDate(0));
			
			$("#clientTouchRecordDg").datagrid({
				title:'客户接触信息',
				pageSize:15,
				fit:true,
				singleSelect:true,
				toolbar:"#searchtool",
				rowrap:true,
				striped: true,
				rownumbers: true,
				//checkbox:true,
				pageList:[10,15,20],
				url:'clientTouchRecord/datagrid',
				pagination:true      
			});
			
			$("#addClientTouchRecordDlg").dialog({
				onClose:function() {
					$("#clientTouchRecordForm").form("clear");
				}	
			});

			$("#touchOperator").combogrid({
				panelWidth:250,
				multiple:true,
				idField:'OPER_ID',
				textField:'OPER_ID',
				url:'clientTouchRecord/getAllOperator',
				method:'get',
				columns:[[
				       {field:'ck',checkbox:true},
				       {field:'OPER_ID',title:'操作工号',width:60},
				       {field:'OPER_NAME',title:'操作员',width:150}
				]],
				fitColmns:true
			});
			
		});

		function rowsformatter(val,data,index) {
			return "<a href='#' onclick='javascript:clientTouchRecordEdit(\"" + data.CLIENT_NO +"\",\""+ data.CLIENT_NAME +"\",\"" + data.CLIENT_TELEPHONE + "\",\"" + data.CLIENT_TELEPHONE2 + "\",\"" + data.CLIENT_LEVEL + "\",\"" + data.CLIENT_SEX +"\",\"" + data.CLIENT_QQ +"\",\"" + data.CLIENT_EMAIL +"\",\"" + data.CLIENT_COMPANY+"\",\"" + data.CLIENT_ADDRESS + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:clientTouchRecordDel(\"" + data.CLIENT_NO+ "\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}

		function typeformatter(val,data,index) {
			if(val==1) {
				return "呼出";
			}else {
				return "呼入";
			}
		}

		function findData() {
			//alert("clientName:" + clientName + ";clientTelephone:" + clientTelephone + ";clientLevel:" + clientLevel + ";clientSex:" + clientSex);
			var operIds = $("#touchOperator").combogrid("getValues");
			//alert(operIds);
			$("#clientTouchRecordDg").datagrid('load',{
				agent:$("#clientName").val(),
				clientTelephone:$("#clientTelephone").val(),
				touchType:$("#touchType").combobox("getValue"),
				operator:"" + operIds + "",
				startTime:$("#startTime").datebox("getValue"),
				endTime:$("#endTime").datebox("getValue")
			});
			
		}
		
		//导出客户数据
		function exportClientTouchRecord() {
			alert();
		}
					
	</script>	
</head>

<body>
	<div class="easyui-panel" title="客户接触信息" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:70px">
				<table>
								<tr>
									<td>座席号码</td>
									<td>
										<input width="30" id="agent" name="agent" class="easyui-numberbox"/>
									</td>
									<td>
										客户号码</td>
									<td width="40">
										<input width="30" id="clientTelephone" name="clientTelephone" class="easyui-numberbox"/>
									</td>
									<td>
										通话类型</td>
									<td>
										<select class="easyui-combobox" style="width: 155px;" name="touchType" id="touchType" data-options="panelHeight:'auto'">
								              <option value="2">请选择</option>
								              <option value="1">呼出</option>
								              <option value="0">呼入</option>
								        </select>
										
									</td>
									<td style="text-align: right;">
										操作工号</td>
									<td>
										<select class="easyui-combogrid" style="width: 150px;" name="touchOperator" id="touchOperator" data-options="panelHeight:'auto'">
								        </select>
									</td>
								</tr>
								<tr>
									<td>
										开始时间</td>
									<td>
										<input id="startTime" width="30" name="startTime" class="easyui-datebox" />
									</td>
									<td>
										结束时间</td>
									<td width="40">
										<input id="endTime" width="30" name="endTime" class="easyui-datebox" />
									</td>
									<td>
									</td>
									<td>
										<a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
									</td>
									<td>
									</td>
									<td>
									</td>
								</tr>
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="clientTouchRecordDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'AGENT',width:100,align:'center'">座席号码</th>                
									<th data-options="field:'CLIENT_TELEPHONE',width:120,align:'center'">客户号码</th>
									<th data-options="field:'TOUCH_OPERATOR',width:100,align:'center'">操作工号</th>
									<th data-options="field:'TOUCH_TYPE',width:80,align:'center',formatter:typeformatter">呼叫类型</th>                
									<th data-options="field:'TOUCH_CHANNEL',width:150,align:'center'">通道</th>
									<th data-options="field:'TOUCH_NOTE',width:350,align:'center'">备注</th>                
									<th data-options="field:'TOUCH_TIME',width:120,align:'center'">接触时间</th>                
									<th data-options="field:'id',width:80,align:'center'">操作</th>                
								</tr>        
							</thead>
					</table>					

			</div>
	
<!--			<div id="searchtool" style="padding:20px">  -->
			<div id="searchtool" style="height:22px;padding:3px;">
					<div style="display:inline;position:absolute;right:2px;" >
						<button id="exportBtn" onclick="exportClientTouchRecord();">导出</button>
					</div>
			 <div>
		</div>
	</div>

</body>
</html>

