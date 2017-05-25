<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>操作日志管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		//数据列表赋值
		
		$(function(){
			
			$("#operationLogDg").datagrid({
				pageSize:30,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,20,30],
				url:'operationLog/datagrid',
				onLoadSuccess:function(data) {
					for(var i=0;i<data.rows.length;i++) {

						var params = data.rows[i].PARAMS; //先取出参数值

						params = params.replace(/\|/gm,'<br>');

						//addTooltip("aaaaa",'param' + i);
						$("#param" + i).tooltip({
							position:'top',
							content:params
						});
					}
				}
			});

			$("#moduleCode").combotree({
				url:'module/tree',
				method:'POST',
				valueField:'id',
				textField:'text',
				onSelect:function(node) {
					if(node.pid=="-1") {
						$("#moduleCode").combotree("clear");
						alert("请选择有效的菜单");
					}

					if(node.pid=='root') {
						$("#moduleCode").combotree("clear");
					}
					
				}
			});

			$("#operId").combobox({
				url:'operator/getCombobox',
				method:'POST',
				valueField:'id',
				textField:'text'
			});

			$("#operation").combobox({
				url:'getCombobox?groupCode=OPERATION_TYPE',
				method:'POST',
				valueField:'id',
				textField:'text'
			});
			
		});

		function findData() {
			$("#operationLogDg").datagrid("load",{
				moduleCode:$("#moduleCode").combotree('getValue'),
				operation:$("#operation").combobox('getValue'),
				operId:$("#operId").combobox('getValue'),
				startTime:$("#startTime").datebox("getValue"),
				endTime:$("#endTime").datebox("getValue")
			});
		}

		function rowsformatter(val,data,index) {
			return "<div id='param" + index + "' style='width:auto;' class='easyui-panel easyui-tooltip'>参数信息</div>";
		}
					
	</script>	
</head>

<body>
	<div class="easyui-panel" title="操作日志查询" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:65px">
				<table>
								<tr>
									<td>菜单编码：
										<input data-options="width:180,panelHeight:400" id="moduleCode" name="moduleCode" class="easyui-combotree"/>
									</td>
									
									<td style="padding-left:30px;">执行操作：
										<input data-options="width:180" id="operation" name="operation" class="easyui-combobox"/>
									</td>
									<td style="padding-left:30px;">操作员：
										<input data-options="width:180" id="operId" name="operId" class="easyui-combobox"/>
									</td>
								</tr>
								<tr>
									<td>开始时间：
										<input data-options="width:180" id="startTime" name="startTime" class="easyui-datebox" />
									</td>
									<td style="padding-left:30px;">结束时间：
										<input data-options="width:180" id="endTime" name="endTime" class="easyui-datebox" />
										&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										
									</td>
									<td>
										<a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search',width:135">查询</a>
									</td>
								</tr>
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="operationLogDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'MODULE_NAME',width:250,align:'center'">菜单名称</th>                
									<th data-options="field:'OPERATION_DESC',width:120,align:'center'">操作</th>
									<th data-options="field:'OPER_NAME',width:200,align:'center'">操作员</th>                
									<th data-options="field:'IP_ADDRESS',width:150,align:'center'">IP地址</th>
									<th data-options="field:'OPERATION_TIME',width:200,align:'center'">操作时间</th>
									<th data-options="field:'PARAMS',width:150,align:'center',formatter:rowsformatter">参数信息</th>                
								</tr>        
							</thead>
					</table>					

			</div>

		</div>
	</div>
</body>
</html>

