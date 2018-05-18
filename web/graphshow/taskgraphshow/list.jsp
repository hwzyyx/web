<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>任务图表展示</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript">
		$(function(){

			$("#taskList").combobox({
				url:'taskGraphShow/getTaskCombobox',
				method:'POST',
				valueField:'id',
				textField:'text'
			});
			
		});

		
		
	</script>
</head>

<body>
	
	<div class="easyui-panel" title='任务图表展示' data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 顶部查询区 -->			
			<div data-options="region:'north',split:true,border:true" style="height:40px">
				<table>
								<tr>
									<td>选择任务</td>
									<td>
										<input class="easyui-combobox" name="taskList" id="taskList" />
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										字典组编码</td>
									<td>
										<input width="30" id="groupCode" name="groupCode" class="easyui-validatebox"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										状态</td>
									<td>
										<select class="easyui-combobox" style="width: 155px;" id="state" data-options="panelHeight:'auto'">
								              <option value="2">请选择</option>
								              <option value="1">有效</option>
								              <option value="0">无效</option>
								        </select> 
										&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										<a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
									</td>
								</tr>
							</table>
			</div>
			
			<!-- 数据显示区 -->
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
				<table id="dictGroupDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'ckct',checkbox:true"></th>                       
									<th data-options="field:'GROUP_NAME',width:220,align:'center'">字典组名称</th>                
									<th data-options="field:'GROUP_CODE',width:220,align:'center'">字典组编码</th>                
									<th data-options="field:'GROUP_DESC',width:220,align:'center'">字典组描述</th>                
									<th data-options="field:'STATE',width:80,align:'center',formatter:stateformatter">状态</th>                
									<th data-options="field:'id',width:100,align:'center',formatter:rowformatter">操作</th>                
								</tr>        
							</thead>
					</table>
			</div>
		</div>
	</div>

</body>
</html>