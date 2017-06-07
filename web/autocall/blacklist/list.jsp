<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>黑名单管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
<!--	<script type="text/javascript" src="jquery.min.js"></script>-->
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript">

    	var currBlackListId = null;

    	var orgComboTreeData = eval('${orgComboTreeData}');
    	
	    $(function(){

	    	//alert(orgComboTreeData);
	    	//alert(eval(orgComboTreeData));
	    	//$("#startTime").datebox("setValue",getCurrMonthDay1());
	    	$("#endTime").datebox("setValue",getCurrDate());

	    	$("#orgCode").combotree('loadData',orgComboTreeData).combotree({

	    		onLoadSuccess:function(node,data) {
		    		//设置默认全选
	    			var t = $("#orgCode").combotree("tree");
					for(var i=0;i<data.length;i++) {
						node = t.tree("find",data[i].id);
						t.tree('check',node.target);
					}

					var selectRs = $("#orgCode").combotree('getValues');
					var orgCodes = selectRs.toString();
					var startTime = $("#startTime").datebox('getValue');
					var endTime = $("#endTime").datebox('getValue');
					
					//加载之后,马上对黑名单列表定义及加载
					$("#autoBlackListDg").datagrid({
						pageSize:15,
						pagination:true,
						fit:true,
						toolbar:'#opertool',
						singleSelect:true,
						rownumbers:true,
						rowrap:true,
						striped:true,
						pageList:[10,15,20],
						url:'autoBlackList/datagrid',
						queryParams:{
							blackListName:$("#blackListName").textbox('getValue'),
							orgCode:orgCodes,
							startTime:startTime,
							endTime:endTime
						}
						
					});
	    		}
		    	
		    });
	    	
		    $("#autoBlackListTelephoneDg").datagrid({
		    	pageSize:30,
				pagination:true,      
				fit:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,30,50],
				checkbox:true,
				toolbar:'#telephoneopertool',
				url:'autoBlackListTelephone/datagrid',
				queryParams:{
					blackListId:currBlackListId,
			    	telephone:$('#telephone').textbox('getValue'),
	    			clientName:$('#clientName').textbox('getValue')
				}
			    
			});

		    $("#autoBlackListDlg").dialog({
			    onClose:function() {
			    	currBlackListId = null;
			    	$("#autoBlackListForm").form('clear');
			    	$("#uploadTelephoneForm").form('clear');
			    	//同时，要将号码列表数据清空
			    	$("#autoBlackListTelephoneDg").datagrid('loadData',{total:0,rows:[]});    //号码列表清空
			    	$("#autoBlackListTab").tabs('select',"黑名单管理");   //默认选中黑名单管理
		    	}
		    });	

		    
		    $("#autoBlackListTelephoneDlg").dialog({
			    onClose:function() {
			    	$("#autoBlackListTelephoneForm").form('clear');
		    	}
			});	

		    $("#telephoneFile").filebox({
				buttonText:'选择文件'
			});

    	});

	    function showExtraTabs(flag) {   //是否显示额外Tab,主要是导入号码和号码列表
	    	if(flag==1) {    //flag=1 时，显示
	    		$("#autoBlackListTab").tabs('getTab',"导入号码").panel('options').tab.show();
	    		$("#autoBlackListTab").tabs('getTab',"号码列表").panel('options').tab.show();
	    	}else {
	    		$("#autoBlackListTab").tabs('getTab',"导入号码").panel('options').tab.hide();
	    		$("#autoBlackListTab").tabs('getTab',"号码列表").panel('options').tab.hide();
	    	}
	    }
	    
	    function autoBlackListAdd() {

	    	showExtraTabs(0);

	    	$("#autoBlackListSaveBtn").attr("onclick","autoBlackListSaveAdd()");
	    	
	    	$("#autoBlackListDlg").dialog('setTitle','添加黑名单').dialog('open');
		    
	    }

	    
    	function autoBlackListEdit(blackListId,blackListName) {

    		showExtraTabs(1);

    		currBlackListId = blackListId;
        	
        	$("#BLACKLIST_ID").val(blackListId);
        	$("#BLACKLIST_NAME").textbox('setValue',blackListName);

        	//重新加载号码列表
        	findDataForTelephone();

	    	$("#autoBlackListSaveBtn").attr("onclick","autoBlackListSaveEdit()");
	    	//alert("aaaa");
        	$("#autoBlackListDlg").dialog('setTitle','修改黑名单').dialog('open');
    	}

    	function autoBlackListSaveAdd() {
        	     
			$("#autoBlackListForm").form('submit',{
    			
				url:'autoBlackList/add',
				onSubmit:function() {
					var v = $(this).form('validate');
					if(v) {
						$.messager.progress({
							msg:'系统正在处理,请稍候...',
							interval:3000
						});
					}

					return $(this).form('validate');
				},
				success:function(data) {
					$.messager.progress('close');

					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {  //添加成功之后，就可以取出返回的ID

						findData();            //重新加载黑名单数据
						
						showExtraTabs(1);     //显示导入号码tab 和 号码列表 tab
						
						var blId = result.extraMessage;   //黑名单ID值
						currBlackListId = blId;           //设置为当前黑名单ID

						$("#BLACKLIST_ID").val(blId);
						
						//重新加载号码列表
						findDataForTelephone();

						//暂时不关闭窗口，所以再点击保存按钮时，就是修改操作了
						$("#autoBlackListSaveBtn").attr("onclick","autoBlackListSaveEdit()");
						$("#autoBlackListDlg").dialog('setTitle','编辑黑名单');
						
						
					}
				}
				
			});
    	}

    	function autoBlackListSaveEdit() {

			$("#autoBlackListForm").form('submit',{
				url:'autoBlackList/update',
				onSubmit:function() {
					var v = $(this).form('validate');
					if(v) {
						$.messager.progress({
							msg:'系统正在处理,请稍候...',
							interval:3000
						});
					}

					return $(this).form('validate');
				},
				success:function(data) {
					$.messager.progress('close');

					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {

						findData();   //重新加载数据
						
						//$("#autoBlackListDlg").dialog('close');
					}
				}
				
			});
    	}

    	function autoBlackListDel(blackListId) {
    		$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...'
					});
					$.ajax({
						url:'autoBlackList/delete?&blackListId=' + blackListId,
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress("close");
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								findData();   //重新加载黑名单数据					
							}
						}
					});
				}
			});
        	
    	}

		function autoBlackListTelephoneAdd() {

			$("#autoBlackListTelephoneSaveBtn").attr("onclick","autoBlackListTelephoneSaveAdd()");
	    	
	    	$("#autoBlackListTelephoneDlg").dialog('setTitle','新增号码').dialog('open');
		    
	    }
	    
    	function autoBlackListTelephoneEdit(telId,telephone,clientName) {
    		$("#TEL_ID").val(telId);
    		$("#TELEPHONE").numberbox('setValue',telephone);
    		$("#CLIENT_NAME").textbox('setValue',clientName);

			$("#autoBlackListTelephoneSaveBtn").attr("onclick","autoBlackListTelephoneSaveEdit()");

			$("#autoBlackListTelephoneDlg").dialog('setTitle','修改号码').dialog('open');
        	
    	}
    	

    	//黑名单号码的添加保存
    	function autoBlackListTelephoneSaveAdd(){

    		$("#autoBlackListTelephoneForm").form('submit',{
    			
				url:'autoBlackListTelephone/add?blackListId=' + currBlackListId,
				onSubmit:function() {
					var v = $(this).form('validate');
					if(v) {
						$.messager.progress({
							msg:'系统正在处理,请稍候...',
							interval:3000
						});
					}

					return $(this).form('validate');
				},
				success:function(data) {
					$.messager.progress('close');

					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {
						findDataForTelephone();
						$("#autoBlackListTelephoneDlg").dialog('close');
					}
				}
				
			});
        	
    	}

    	//黑名单号码的修改保存
    	function autoBlackListTelephoneSaveEdit(){

    		$("#autoBlackListTelephoneForm").form('submit',{

				url:'autoBlackListTelephone/update?blackListId=' + currBlackListId,
				onSubmit:function() {
					var v = $(this).form('validate');
					if(v) {
						$.messager.progress({
							msg:'系统正在处理,请稍候...',
							interval:3000
						});
					}
					
					return $(this).form('validate');
				
				},
				success:function(data) {

					$.messager.progress('close');

					var result = JSON.parse(data);

					var statusCode = result.statusCode;
					var message = result.message;

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {
						findDataForTelephone();
						$("#autoBlackListTelephoneDlg").dialog('close');
					}
					
				}
				
			});
    		
        	
    	}
    	

    	function autoBlackListTelephoneDel() {

    		$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...'
					});
					$.ajax({
						url:'autoBlackListTelephone/delete?&ids=' + getTelephoneSelectedRows(),
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress("close");
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								findDataForTelephone();					
							}
						}
					});
				}
			});
        	
    	}

    	function uploadPhoneFile() {

    		$("#uploadTelephoneForm").form('submit',{

    			url:'autoBlackListTelephone/uploadFile?blackListId=' + currBlackListId,
    			onSubmit:function() {

    				var v = $(this).form('validate');
    				if(v) {
	    				$.messager.progress({
							msg:'系统正在处理，请稍候...',
							interval:3000
						});
    				}
					    			
					return $(this).form('validate');
    			},
    			success:function(data) {
    				$.messager.progress('close');
					var result = JSON.parse(data); //解析Json数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {         //保存成功时
						$("#uploadTelephoneForm").form('clear');
						findDataForTelephone();
					}
					
    			}
        		
        	});
        	
    	}

    	//取得选中的号码数据			
		function getTelephoneSelectedRows() {
			
			var rows = $('#autoBlackListTelephoneDg').datagrid('getSelections');
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].TEL_ID);
			}
			return	ids.join(",");			
		}

    	function findData() {

    		var selectRs = $("#orgCode").combotree('getValues');
			var orgCodes = selectRs.toString();
			var startTime = $("#startTime").datebox('getValue');
			var endTime = $("#endTime").datebox('getValue');

			$("#autoBlackListDg").datagrid('load',{
				blackListName:$("#blackListName").textbox('getValue'),
				orgCode:orgCodes,
				startTime:startTime,
				endTime:endTime
			});
        	
    	}
			
    	function findDataForTelephone() {
    		$("#autoBlackListTelephoneDg").datagrid('load',{
        		blackListId:currBlackListId,
        		telephone:$('#telephone').textbox('getValue'),
    			clientName:$('#clientName').textbox('getValue')
        	});
    	}

		function rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:autoBlackListEdit(\"" + data.BLACKLIST_ID + "\",\"" + data.BLACKLIST_NAME + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:autoBlackListDel(\"" + data.BLACKLIST_ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}

		function telephonerowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:autoBlackListTelephoneEdit(\"" + data.TEL_ID + "\",\"" + data.TELEPHONE + "\",\"" + data.CLIENT_NAME + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>";
		}    

    </script>
    
</head>
<body>
<%@ include file="/base_loading.jsp" %>
<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:75px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>黑名单名字：<input id="blackListName" type="text" class="easyui-textbox" style="width:200px;"/>
				
					<span style="padding-left:30px;">
						选择组织：<select id="orgCode" class="easyui-combotree" multiple style="width:200px;"></select>
					</span>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<span style="">
						&nbsp;&nbsp;&nbsp;创建时间：<input id="startTime" style="width:200px;" name="startTime" class="easyui-datebox" /><span style="padding-left:39px;padding-right:39px;">至</span> <input id="endTime" style="width:200px;" name="endTime" class="easyui-datebox" />
					</span>
					<span style="padding-left:30px;">
						<a href="javascript:findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
					</span>
				</td>
			</tr>
		</table>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		
		<table id="autoBlackListDg">
			<thead>
				<tr style="height:12px;">		
					<th data-options="field:'BLACKLIST_NAME',width:300,align:'center'">黑名单名称</th>
					<th data-options="field:'CREATE_USERCODE_DESC',width:100,align:'center'">创建人</th>
					<th data-options="field:'ORG_CODE_DESC',width:150,align:'center'">部门(组织)名字</th>
					<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
					<th data-options="field:'id',width:100,align:'center',formatter:rowformatter">操作</th -->
				</tr>
				
			</thead>
		</table>	
		
	</div>
</div>

<div id="opertool" style="padding:5px;">
	<a href="#" id="easyui-add" onclick="autoBlackListAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增黑名单</a>
</div>

<div id="autoBlackListDlg" class="easyui-dialog" style="width:850px;height:450px;padding:5px;" modal="true" closed="true">
		<!-- 包含黑名单表单 -->
		<%@ include file="/autocall/blacklist/_form.jsp" %>
</div>

<div id="autoBlackListTelephoneDlg" class="easyui-dialog" style="width:500px;height:400px;padding:5px;" modal="true" closed="true">
		<!-- 包含黑名单号码表单 -->
		<%@ include file="/autocall/blacklist/_telephoneform.jsp" %>
</div>

</body>
</html>