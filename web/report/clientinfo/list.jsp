<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>客户资料管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
    <script type="text/javascript" src="base-loading.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		//数据列表赋值
		//type: 0,表示当前的日期； 1，表示当月1号
		function getCurrDate(type) {
			var dateString = "";
			var myDate = new Date();
			dateString += myDate.getFullYear() + "-";
			dateString += (myDate.getMonth()+1) + "-";
			if(type==0) {
				dateString += myDate.getDate();
			}else {
				dateString += "01";
			}
			return dateString;			
		}
		
		$(function(){

			//$("#startTime").datebox("setValue",getCurrDate(1));
			//$("#endTime").datebox("setValue",getCurrDate(0));
			
			$("#clientInfoDg").datagrid({
				title:'客户信息',
				pageSize:15,
				fit:true,
				singleSelect:true,
				toolbar:"#searchtool",
				rowrap:true,
				striped: true,
				rownumbers: true,
				//checkbox:true,
				pageList:[10,15,20],
				//url:'clientInfo/datagrid?startTime='+$("#startTime").datebox("getValue") + "&endTime=" + $("#endTime").datebox("getValue"),
				url:'clientInfo/datagrid',
				pagination:true      
			});
			
			$("#addClientInfoDlg").dialog({
				onClose:function() {
					$("#clientInfoForm").form("clear");
				}	
			});

			$("#clientLevel").combobox({
				url:'getCombobox?groupCode=CLIENT_LEVEL&flag=1',
				method:'POST',
				valueField:'id',
				textField:'text'
			});

			$("#CLIENT_LEVEL").combobox({
				url:'getCombobox?groupCode=CLIENT_LEVEL&flag=0',
				method:'POST',
				valueField:'id',
				textField:'text'
			});
			
		});

		function rowsformatter(val,data,index) {
			return "<a href='#' onclick='javascript:clientInfoEdit(\"" + data.CLIENT_NO +"\",\""+ data.CLIENT_NAME +"\",\"" + data.CLIENT_TELEPHONE + "\",\"" + data.CLIENT_TELEPHONE2 + "\",\"" + data.CLIENT_LEVEL + "\",\"" + data.CLIENT_SEX +"\",\"" + data.CLIENT_QQ +"\",\"" + data.CLIENT_EMAIL +"\",\"" + data.CLIENT_COMPANY+"\",\"" + data.CLIENT_ADDRESS + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:clientInfoDel(\"" + data.CLIENT_NO+ "\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}

		function clientInfoEdit(clientNo,clientName,clientTelephone,clientTelephone2,clientLevel,clientSex,clientQQ,clientEmail,clientCompany,clientAddress) {
			//alert(data.CLIENT_NAME + "   " + data.CLIENT_SEX);
			$("#addClientInfoDlg").dialog('setTitle',"修改客户资料").dialog("open");

			$("#saveBtn").attr("onclick","saveEdit()");
			
			$("#clientInfoForm").form('load',{
				'clientInfo.CLIENT_NO':clientNo,
				'clientInfo.CLIENT_NAME':clientName,
				'clientInfo.CLIENT_TELEPHONE':clientTelephone,
				'clientInfo.CLIENT_TELEPHONE2':clientTelephone2,
				'clientInfo.CLIENT_QQ':clientQQ,
				'clientInfo.CLIENT_EMAIL':clientEmail,
				'clientInfo.CLIENT_COMPANY':clientCompany,
				'clientInfo.CLIENT_ADDRESS':clientAddress
			});

			if(clientSex==null) {
				$("#CLIENT_SEX").combobox("setValue","1");
			}else {
				$("#CLIENT_SEX").combobox("setValue",clientSex);
			}
			
			if(clientLevel==null) {
				$("#CLIENT_LEVEL").combobox("setValue","1");
			}else {
				$("#CLIENT_LEVEL").combobox("setValue",clientLevel);
			}
			
			//alert(clientNo + clientName + clientTelephone + clientTelephone2 + clientLevel + clientSex + clientQQ + clientEmail + clientCompany + clientAddress);
		}

		function clientInfoDel(clientNo) {
			//alert(clientNo);
			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...'
					});
					$.ajax({
						url:'clientInfo/delete?clientNo=' + clientNo,
						method:'POST',
						type:'json',
						success:function(data) {
							var result = JSON.parse(data);
			
							var statusCode = result.statusCode;
							var message = result.message;
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //保存成功时
								$('#clientInfoDg').datagrid({url:'clientInfo/datagrid'});
							}
						}
					});
				}
			});
		}

		function clientInfoAdd() {
			$("#addClientInfoDlg").dialog('setTitle',"添加客户资料").dialog("open");

			$("#saveBtn").removeAttr("onclick");
			$("#saveBtn").attr("onclick","saveAdd()");

			$("#CLIENT_SEX").combobox('setValue','1');
			$("#CLIENT_LEVEL").combobox('setValue',"1");
			
		}

		function sexformatter(val,data,index) {
			if(val==1) {
				return "男";
			}else {
				return "女";
			}
		}

		function findData() {
			$("#clientInfoDg").datagrid('load',{
				clientName:$("#clientName").textbox('getValue'),
				clientTelephone:$("#clientTelephone").numberbox('getValue'),
				clientLevel:$("#clientLevel").combobox("getValue"),
				clientSex:$("#clientSex").combobox("getValue"),
				startTime:$("#startTime").datebox("getValue"),
				endTime:$("#endTime").datebox("getValue")
			});
			
		}

		//保存添加客户信息
		function saveAdd() {
			//alert("--saveAdd()--");
			$("#clientInfoForm").form('submit',{
				url:'clientInfo/add',
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function(data) {
					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;
					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {         //保存成功时
						$('#clientInfoDg').datagrid({url:'clientInfo/datagrid'});
						$('#addClientInfoDlg').dialog('close');//关闭对话框
					}
					
				}
			});
		}

		//保存修改客户信息
		function saveEdit() {

			$("#clientInfoForm").form('submit',{
				url:'clientInfo/update',
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function(data) {

					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#clientInfoDg').datagrid({url:'clientInfo/datagrid'});
						$('#addClientInfoDlg').dialog('close');//关闭对话框
					}
					
				}	
			})
			
			//alert("--saveEdit()--");
		}

		function add_cancel() {
			$("#addClientInfoDlg").dialog("close");
		}
		
		//导出客户数据
		function exportClientInfo() {
			alert();
		}
					
	</script>	
</head>

<body>
	<!-- 页面加载效果 -->
	<%@ include file="/base_loading.jsp" %>
	<div class="easyui-panel" title="客户资料" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:70px">
				<table>
								<tr>
									<td>客户号码</td>
									<td>
										<input width="30" id="clientTelephone" name="clientTelephone" class="easyui-numberbox"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										客户姓名</td>
									<td width="40">
										<input width="30" id="clientName" name="clientName" class="easyui-textbox"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										性别</td>
									<td>
										<select class="easyui-combobox" style="width: 155px;" name="clientSex" id="clientSex" data-options="panelHeight:'auto'">
								              <option value="2">请选择</option>
								              <option value="1">男</option>
								              <option value="0">女</option>
								        </select>
										
									</td>
									<td style="text-align: right;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										客户等级</td>
									<td>
										<input class="easyui-combobox" name="clientLevel" id="clientLevel" />
									</td>
								</tr>
								<tr>
									<td>
										开始时间</td>
									<td>
										<input id="startTime" width="30" name="startTime" class="easyui-datebox" />
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
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
					<table id="clientInfoDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'CLIENT_TELEPHONE',width:100,align:'center'">客户号码</th>
									<th data-options="field:'CLIENT_NAME',width:80,align:'center'">客户姓名</th>                
									<th data-options="field:'CLIENT_SEX',width:50,align:'center',formatter:sexformatter">性别</th>                
									<th data-options="field:'CLIENT_LEVEL_DESC',width:100,align:'center'">客户等级</th>
									<th data-options="field:'CLIENT_TELEPHONE2',width:100,align:'center'">备用号码</th>
									<th data-options="field:'CLIENT_QQ',width:100,align:'center'">QQ号码</th>                
									<th data-options="field:'CLIENT_EMAIL',width:100,align:'center'">Email</th>                
									<th data-options="field:'CLIENT_COMPANY',width:220,align:'center'">公司名称</th>                
									<th data-options="field:'CREATE_TIME',width:150,align:'center'">添加时间</th>                
									<th data-options="field:'id',width:100,align:'center',formatter:rowsformatter">操作</th>                
								</tr>        
							</thead>
					</table>					

			</div>
	
<!--			<div id="searchtool" style="padding:20px">  -->
			<div id="searchtool" style="height:22px;padding:3px;">
					  
					<div style="display:inline;position:absolute;left:2px;" >
						<a href="#" id="easyui-add" onclick="clientInfoAdd()" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加</a>
					</div>
					<div style="display:inline;position:absolute;right:2px;" >
						<button id="exportBtn" onclick="exportClientInfo();">导出</button>
					</div>
			 <div>
		</div>
	</div>

	<div id="addClientInfoDlg" class="easyui-dialog" style="width:580px;height:400px;padding:10px 20px;" modal="true" closed="true" buttons="#addClientInfoDlgBtn">
		<form id="clientInfoForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/report/clientinfo/_form.jsp"%>
		</form>	
	</div>

</body>
</html>

