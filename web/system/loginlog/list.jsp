<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>登录日志</title>
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
			
			$("#loginLogDg").datagrid({
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				url:'loginLog/datagrid'
			});
		});

		function findData() {
			$("#loginLogDg").datagrid("load",{
				operId:$("#operId").textbox('getValue'),
				orgCode:$("#orgCode").textbox('getValue'),
				loginStartTime:$("#loginStartTime").datebox("getValue"),
				loginEndTime:$("#loginEndTime").datebox("getValue")
			});
		}
					
	</script>	
</head>

<body>
	<div class="easyui-panel" title="登录日志查询" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:70px">
				<table>
								<tr>
									<td>操作工号：
										<input width="30" id="operId" name="operId" style="150px;" class="easyui-textbox"/>
									</td>
									
									<td style="padding-left:30px;">组织编码：
										<input id="orgCode" name="orgCode" style="150px;" class="easyui-textbox" />
									</td>
								</tr>
								<tr>
									<td>开始时间：
										<input id="loginStartTime" style="width:150px;" name="loginStartTime" class="easyui-datebox" />
									</td>
									<td style="padding-left:30px;">结束时间：
										<input id="loginEndTime" style="width:150px;" name="loginEndTime" class="easyui-datebox" />
										<span style="padding-left:30px;">
											<a href="javascript:findData()"  class="easyui-linkbutton" style="width:150px;" data-options="iconCls:'icon-search'">查询</a>
										</span>
									</td>
								</tr>
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="loginLogDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'OPER_ID',width:150,align:'center'">工号</th>                
									<th data-options="field:'ORG_CODE',width:300,align:'center'">组织代码</th>
									<th data-options="field:'LOGIN_TIME',width:200,align:'center'">登录时间</th>                
									<th data-options="field:'LOGOUT_TIME',width:200,align:'center'">退出时间</th>                
									<th data-options="field:'IP_ADDRESS',width:200,align:'center'">IP地址</th>                
								</tr>        
							</thead>
					</table>					

			</div>

		</div>
	</div>
</body>
</html>

