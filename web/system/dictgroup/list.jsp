<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>数据字典管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		var currGroupCode = null;
		$(function(){
			$("#dictGroupDg").datagrid({
				url:'dictGroup/datagrid',
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:false,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				toolbar:'#searchtool',
				checkbox:true,
				idFile:'GROUP_CODE'
			});

			$("#dictItemDg").datagrid({
				url:'dictItem/datagrid',
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:false,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				toolbar:'#dictitemsearchtool',
				checkbox:true,
				idFile:'DICT_CODE'
			});

			//弹出窗口关闭时
			$("#dictDialog").dialog({
				onClose:function(){
					currGroupCode = null;
					$("#dictGroupForm").form("clear");
					$("#itemAddBtn").removeAttr("disabled");
				}
			});
			
			//弹出窗口关闭时
			$("#dictItemDialog").dialog({
				onClose:function(){
					$("#dictItemForm").form("clear");
				}
			});
			
		});

		function findData() {
			$("#dictGroupDg").datagrid('load',{
				groupCode:$("#groupCode").textbox('getValue'),
				groupName:$("#groupName").textbox('getValue'),
				state:$("#state").combobox('getValue')
			});
		}
		
		function dictAdd() {
			$("#dictDialog").dialog("setTitle",'添加数据字典').dialog("open");
			//列表数据字典项
			$("#dictItemDg").datagrid('load',{
				groupCode:currGroupCode
			});

			$("#GROUP_CODE").textbox("readonly",false);
			$("#GROUP_CODE").textbox('textbox').css('background-color','');
			
			//先设置为有效
			$("#STATE").combobox("setValue","1");
			
			$("#itemAddBtn").attr("disabled","disabled");
			
			$("#dictGroupSave").attr("onclick","saveDictAdd()");
		}
		
		function dictEdit(groupCode,groupName,groupDesc,state) {
			currGroupCode = groupCode;
			$("#dictDialog").dialog("setTitle",'编辑数据字典').dialog("open");

			$("#GROUP_CODE").textbox('setValue',groupCode);
			$("#GROUP_NAME").textbox('setValue',groupName);
			$("#GROUP_DESC").textbox('setValue',groupDesc);
			$("#STATE").combobox('setValue',state);

			//列表数据字典项
			$("#dictItemDg").datagrid('load',{
				groupCode:currGroupCode
			});

			$("#dictGroupSave").attr("onclick","saveDictEdit()");
			$("#GROUP_CODE").textbox("readonly",true);
			$("#GROUP_CODE").textbox('textbox').css('background-color','#e3e3e3');
		}

		function saveDictAdd() {
			$("#dictGroupForm").form("submit",{
				url:'dictGroup/add',
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function(data) {
					var rs = JSON.parse(data);    //解析Json 数据
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
						findData();	
						currGroupCode = $("#GROUP_CODE").val();
						$("#itemAddBtn").removeAttr("disabled");					
					}
				}
			});
		}

		function saveDictEdit() {
			$("#dictGroupForm").form("submit",{
				url:'dictGroup/update',
				onSubmit:function(){
					return $(this).form('validate');
				},
				success:function(data) {
					var rs = JSON.parse(data);    //解析Json 数据
					
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         
						findData();						
					}
				}
			});
		}
		
		function dictDel() {

			if(getDictGroupSelectedRows().length==0) {
				window.parent.showMessage("请选中记录再删除!","error");
				return;
			}
			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候 ...'
					});
					$.ajax({
						url:'dictGroup/delete?ids=' + getDictGroupSelectedRows(),
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress("close");
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								findData();						
							}
						}
					});
				}
			});
			
		}

		//取得选中的数据字典			
		function getDictGroupSelectedRows() {
			
			var rows = $('#dictGroupDg').datagrid('getSelections');
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].GROUP_CODE);
			}
			return	ids.join(",");			
		}
		
		function stateformatter(val,data,index) {
			if(val==1) {
				return '有效';
			}else {
				return '无效';
			}
		}

		function rowformatter(val,data,index) {
			return "<a href='#' style='text-decoration:none' onclick='javascript:dictEdit(\"" + data.GROUP_CODE + "\",\"" + data.GROUP_NAME + "\",\"" + data.GROUP_DESC + "\",\"" + data.STATE + "\")'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='编辑' style='width:100px;padding:5px;float:top;'>编辑</a>&nbsp;&nbsp;";
		}

		function itemrowformatter(val,data,index) {
			return "<a href='#' style='text-decoration:none' onclick='javascript:dictItemEdit(\"" + data.DICT_CODE + "\",\"" + data.DICT_NAME + "\",\"" + data.DICT_DESC + "\")'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='编辑' style='width:100px;padding:5px;float:top;'>编辑</a>&nbsp;&nbsp;";
		}

		function dictItemAdd() {
			$("#dictItemDialog").dialog("setTitle",'添加数据字典项').dialog("open");

			$("#DICT_CODE").textbox("readonly",false);
			$("#DICT_CODE").textbox('textbox').css('background-color','');
			
			$("#dictItemSave").attr("onclick","saveDictItemAdd()");
		}

		//对数据字典项编辑时
		function dictItemEdit(dictCode,dictName,dictDesc) {
			$("#dictItemDialog").dialog("setTitle",'编辑数据字典项').dialog("open");

			$("#DICT_CODE").textbox('setValue',dictCode);
			$("#DICT_NAME").textbox('setValue',dictName);
			$("#DICT_DESC").textbox('setValue',dictDesc);

			$("#DICT_CODE").textbox("readonly",true);
			$("#DICT_CODE").textbox('textbox').css('background-color','#e3e3e3');

			$("#dictItemSave").attr("onclick","saveDictItemEdit()");
			
		}

		function saveDictItemAdd() {

			$("#dictItemForm").form('submit',{
				url:'dictItem/add?groupCode=' + currGroupCode,
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function(data) {
					var rs = JSON.parse(data);    //解析Json 数据
					
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {
						$("#dictItemDialog").dialog("close");         
						//列表数据字典项
						$("#dictItemDg").datagrid('load',{
							groupCode:currGroupCode
						});						
					}
				}	
			});
			
		}

		function saveDictItemEdit() {
			$("#dictItemForm").form('submit',{
				url:'dictItem/update?groupCode=' + currGroupCode,
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function(data) {
					var rs = JSON.parse(data);    //解析Json 数据
					
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         
						//列表数据字典项
						$("#dictItemDg").datagrid('load',{
							groupCode:currGroupCode
						});	

						$("#dictItemDialog").dialog('close');					
					}
				}	
			});
		}
		
		function dictItemDel() {

			if(getDictItemSelectedRows().length==0) {
				window.parent.showMessage("请选中记录再删除!","error");
				return;
			}
			
			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候 ...'
					});
					$.ajax({
						url:'dictItem/delete?ids=' + getDictItemSelectedRows() + '&groupCode=' + currGroupCode,
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress("close");
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								//列表数据字典项
								$("#dictItemDg").datagrid('load',{
									groupCode:currGroupCode
								});					
							}
						}
					});
				}
			});
		}

		//取得选中的数据字典字项			
		function getDictItemSelectedRows() {
			
			var rows = $('#dictItemDg').datagrid('getSelections');
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].DICT_CODE);
			}
			return	ids.join(",");			
		}
		
	</script>
</head>

<body>
	
	<div class="easyui-panel" title='数据字典管理' data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 顶部查询区 -->			
			<div data-options="region:'north',split:true,border:true" style="height:40px">
				<table>
								<tr>
									<td>字典组编码：
										<input id="groupCode" name="groupCode" style="width:200px;" type="text" class="easyui-textbox"/>
									</td>
									<td style="padding-left:30px;">字典组名称：
										<input id="groupName" name="groupName" style="width:200px;" type="text" class="easyui-textbox"/>
									</td>
									<td style="padding-left:30px;">状态：
										<select class="easyui-combobox" style="width: 155px;" id="state" data-options="panelHeight:'auto'">
								              <option value="2">请选择</option>
								              <option value="1">有效</option>
								              <option value="0">无效</option>
								        </select> 
										<span style="padding-left:30px;">
											<a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
										</span>
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
									<th data-options="field:'GROUP_CODE',width:220,align:'center'">字典组编码</th>                
									<th data-options="field:'GROUP_NAME',width:220,align:'center'">字典组名称</th>                
									<th data-options="field:'GROUP_DESC',width:220,align:'center'">字典组描述</th>                
									<th data-options="field:'STATE',width:80,align:'center',formatter:stateformatter">状态</th>                
									<th data-options="field:'id',width:100,align:'center',formatter:rowformatter">操作</th>                
								</tr>        
							</thead>
					</table>
			</div>
		</div>
	</div>
	
	<div id="searchtool" style="padding:5px">
		<div style="display:inline;">
			<button id="addBtn" onclick="dictAdd()">添加</button>
			<button id="delBtn" onclick="dictDel()">删除</button>
		</div>
	</div>
		
	<div id="dictitemsearchtool" style="padding:5px">
		<div style="display:inline;">
			<button id="itemAddBtn" onclick="dictItemAdd()">添加子项</button>
			<button id="itemDelBtn" onclick="dictItemDel()">删除子项</button>
		</div>
	</div>		
	
	<!-- 编辑/添加　窗口 -->
	<div id="dictDialog" class="easyui-dialog" title="添加数据字典" data-options="width:800,height:400" modal="true" closed="true">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 顶部查询区 -->			
			<div data-options="region:'north',split:true,border:true" style="height:65px">
				<form id="dictGroupForm" method="post">
				<table>
								<tr>
									<td>字典组编码：
										<input id="GROUP_CODE" name="dictgroup.GROUP_CODE" style="width:200px;" class="easyui-textbox" type="text" required="true" missingMessage="数据字典编码不能为空!"></input>
									</td>
									<td style="padding-left:30px;">字典组名称：
										<input id="GROUP_NAME" name="dictgroup.GROUP_NAME" style="width:200px;" class="easyui-textbox" type="text" required="true" missingMessage="数据字典名称不能为空!"></input>
									</td>
								</tr>
								<tr>
									<td>字典组描述：
										<input id="GROUP_DESC" name="dictgroup.GROUP_DESC" style="width:200px;" class="easyui-textbox" type="text"></input>
									</td>
									<td style="padding-left:30px;">状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：
										<select class="easyui-combobox" style="width: 200px;" id="STATE" name="dictgroup.STATE" data-options="panelHeight:'auto'">
								              <option value="1">有效</option>
								              <option value="0">无效</option>
								        </select> 
										<span style="padding-left:30px;">
											<a href="javascript:#" id="dictGroupSave"  class="easyui-linkbutton" data-options="iconCls:'icon-save'">保存</a>
										</span>
									</td>
								</tr>
							</table>
					</form>
			</div>
			
			<!-- 数据显示区 -->
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
				<table id="dictItemDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'ckct',checkbox:true"></th>                       
									<th data-options="field:'DICT_CODE',width:200,align:'center'">字典编码</th>                
									<th data-options="field:'DICT_NAME',width:200,align:'center'">字典名称</th>                
									<th data-options="field:'DICT_DESC',width:200,align:'center'">字典描述</th>                
									<th data-options="field:'id',width:100,align:'center',formatter:itemrowformatter">操作</th>                
								</tr>        
							</thead>
					</table>
			</div>
		</div>
	</div>

	<!-- 编辑/添加　窗口 -->
	<div id="dictItemDialog" class="easyui-dialog" title="添加数据字典" data-options="width:300,height:200" modal="true" closed="true">
				<form id="dictItemForm" method="post">
				<table>
								<tr>
									<td style="padding-left:10px;padding-top:10px;">字典编码：
										<input id="DICT_CODE" name="dictitem.DICT_CODE" class="easyui-textbox" style="width:150px;" type="text" required="true" missingMessage="字典编码不能为空!"></input>
									</td>
								</tr>
								<tr>
									<td style="padding-left:10px;">字典名称：
										<input id="DICT_NAME" name="dictitem.DICT_NAME" class="easyui-textbox" style="width:150px;" type="text" required="true" missingMessage="字典名称不能为空!"></input>
									</td>
								</tr>
								<tr>
									<td style="padding-left:10px;">字典描述：
										<input id="DICT_DESC" name="dictitem.DICT_DESC" class="easyui-textbox" style="width:150px;" type="text"></input>
										
									</td>
								</tr>
								
								<tr>
									<td style="padding-left:10px;padding-top:10px;">
										<span style="padding-left:65px;">
											<a href="javascript:#" id="dictItemSave" style="width:150px;"  class="easyui-linkbutton" data-options="iconCls:'icon-save'">保存</a>
										</span>
									</td>
								</tr>
							</table>
					</form>
	</div>

</body>
</html>