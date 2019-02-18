<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>自动外呼任务报表-以操作员作为查询依据</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/color.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="http://echarts.baidu.com/gallery/vendors/echarts/echarts.min.js"></script>
	<script type="text/javascript">
	
		var orgComboTreeData = eval('${orgComboTreeData}');
		var isSearchHistoryCallTask = 0;          //是否为查询历史任务,0为否，1为是
	
		$(function(){
			$('#startTime').datebox('setValue',getCurrMonthDay1());
			$('#endTime').datebox('setValue',getCurrDate());
			
			initOrgCodeForAutoCallTaskSearch();
			
			$("#isSearchHistoryCallTaskCheckBox").change(function(){
				if($("#isSearchHistoryCallTaskCheckBox").prop('checked')) {
					//alart("被选中了");
					isSearchHistoryCallTask = 1;
				}else {
					//alert("没有被选中");
					isSearchHistoryCallTask = 0;
				}
				findData();
			});
			
		});
		
		//初始化外呼任务搜索栏中，组织代码的情况
		function initOrgCodeForAutoCallTaskSearch() {
			//搜索栏的组织加载
			$("#orgCode").combotree('loadData',orgComboTreeData).combotree({
				onLoadSuccess:function(node,data) {
					var t = $("#orgCode").combotree("tree");

					for(var i=0;i<data.length;i++) {
						node = t.tree("find",data[i].id);
						t.tree('check',node.target);
					}

					var selectRs = $("#orgCode").combotree('getValues');
					var orgCodes = selectRs.toString();
					$("#autoCallTaskReportDg").datagrid({
						pageSize:30,
						pagination:true,
						fit:true,
						toolbar:'#searchtool',
						rownumbers:true,
						rowrap:true,
						striped:true,
						checkbox:true,
						pageList:[20,30,50],
						url:'autoCallTaskReportGroupByOperId/datagrid',
						queryParams:{
							startTime:$('#startTime').datebox('getValue'),
							endTime:$('#endTime').datebox('getValue'),
							orgCodes:orgCodes,
							isSearchHistoryCallTask:isSearchHistoryCallTask
						}
					});
				}
			});
		}
		
		function findData() {
			
			var startTime = $('#startTime').datebox('getValue');
			var endTime = $('#endTime').datebox('getValue');
			var selectRs = $("#orgCode").combotree('getValues');
			
			if(startTime==null || startTime=='') {
				alert("查询时,开始时间不能为空!");
				return;
			}
			
			if(endTime==null || endTime=='') {
				alert("查询时,结束时间不能为空!");
				return;
			}
			
			if(selectRs.length<1) {
				alert("查询时,组织不能为空!");
				return;
			}
			var orgCodes = selectRs.toString();
			$("#autoCallTaskReportDg").datagrid("reload",{
				startTime:$('#startTime').datebox('getValue'),
				endTime:$('#endTime').datebox('getValue'),
				orgCodes:orgCodes,
				isSearchHistoryCallTask:isSearchHistoryCallTask
			});
		}
		
	</script>
	
</head>
<body>
<%@ include file="/base_loading.jsp" %>
<!-- 页面内容区 -->
<div data-options="fit:true" class="easyui-layout">

	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:50px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertial-align:top;">
				<td>
					日期区间：<input id="startTime" name="startTime" class="easyui-datebox" required="true" style="width:130px;"/><span style="padding-left:38px;padding-right:36px;">至</span> <input id="endTime" name="endTime" class="easyui-datebox" required="true" style="width:130px;" />
					<span style="padding-left:20px;">
						选择组织：<select class="easyui-combotree" id="orgCode" name="orgCode" style="width:130px;" required="true" data-options="panelHeight:'auto',multiple:true"></select>
					</span>
					
					<span style="padding-left:40px;">
						<input type="checkbox" id="isSearchHistoryCallTaskCheckBox" value="1"><label for="isSearchHistoryCallTaskCheckBox">历史任务</label>
						<span style="color:red;margin-left: 20px;">*查询已归档任务</span>
					</span>
					
					<span style="padding-left:40px;">
						<a href="javascript:findData()" class="easyui-linkbutton" style="width:100px;" data-options="iconCls:'icon-search'">查询</a>
					</span>
				</td>
			</tr>
		</table>
	</div>
	
	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="autoCallTaskReportDg" class="easyui-datagrid">
			<thead>
				<tr style="height:12px;">
					<th data-options="field:'ck',checkbox:true"></th>
					<th data-options="field:'ORG_NAME',width:250,align:'center'">组织信息</th>
					<th data-options="field:'OPER_ID_DESC',width:200,align:'center'">操作员</th>
					<th data-options="field:'taskCount',width:100,align:'center'">外呼任务数量</th>
					<th data-options="field:'totalCount',width:100,align:'center'">号码总量</th>
					<th data-options="field:'notCalledCount',width:100,align:'center'">未呼数量</th>
					<th data-options="field:'calledCount',width:100,align:'center'">已呼数量</th>
					<th data-options="field:'successCount',width:100,align:'center'">已成功</th>
					<th data-options="field:'successRate',width:100,align:'center'">成功率</th>
					<th data-options="field:'failureCount',width:100,align:'center'">已失败</th>
					<th data-options="field:'failureRate',width:100,align:'center'">失败率</th>
				</tr>
			</thead>
		</table>
	</div>

</div>

</body>
</html>