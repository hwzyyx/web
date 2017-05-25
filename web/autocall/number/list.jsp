<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>号码组管理</title>
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

    	var currNumberId = null;

    	var orgComboTreeData = eval('${orgComboTreeData}');
    	
	    $(function(){

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
					$("#autoNumberDg").datagrid({
						pageSize:15,
						pagination:true,
						fit:true,
						toolbar:'#opertool',
						singleSelect:true,
						rownumbers:true,
						rowrap:true,
						striped:true,
						pageList:[10,15,20],
						url:'autoNumber/datagrid',
						queryParams:{
							blackListName:$("#numberName").textbox('getValue'),
							orgCode:orgCodes,
							startTime:startTime,
							endTime:endTime
						}
						
					});
	    		}
		    	
		    });
	    	
		    $("#autoNumberTelephoneDg").datagrid({
		    	pageSize:30,
				pagination:true,      
				fit:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,30,50],
				checkbox:true,
				toolbar:'#telephoneopertool',
				url:'autoNumberTelephone/datagrid',
				queryParams:{
					numberId:currNumberId,
			    	telephone:$('#telephone').textbox('getValue'),
	    			clientName:$('#clientName').textbox('getValue')
				}
			    
			});

		    $("#autoNumberDlg").dialog({
			    onClose:function() {
			    	currNumberId = null;
			    	$("#autoNumberForm").form('clear');
			    	$("#uploadTelephoneForm").form('clear');
			    	//同时，要将号码列表数据清空
			    	$("#autoNumberTelephoneDg").datagrid('loadData',{total:0,rows:[]});    //号码列表清空
			    	$("#autoNumberTab").tabs('select',"号码组管理");   //默认选中号码组管理
		    	}
		    });	

		    
		    $("#autoNumberTelephoneDlg").dialog({
			    onClose:function() {
			    	$("#autoNumberTelephoneForm").form('clear');
		    	}
			});	

		    $("#telephoneFile").filebox({
				buttonText:'选择文件'
			});

    	});

	    function showExtraTabs(flag) {   //是否显示额外Tab,主要是导入号码和号码列表
	    	if(flag==1) {    //flag=1 时，显示
	    		$("#autoNumberTab").tabs('getTab',"导入号码").panel('options').tab.show();
	    		$("#autoNumberTab").tabs('getTab',"号码列表").panel('options').tab.show();
	    	}else {
	    		$("#autoNumberTab").tabs('getTab',"导入号码").panel('options').tab.hide();
	    		$("#autoNumberTab").tabs('getTab',"号码列表").panel('options').tab.hide();
	    	}
	    }
	    
	    function autoNumberAdd() {

	    	showExtraTabs(0);

	    	$("#autoNumberSaveBtn").attr("onclick","autoNumberSaveAdd()");
	    	
	    	$("#autoNumberDlg").dialog('setTitle','添加号码组').dialog('open');
		    
	    }

	    
    	function autoNumberEdit(numberId,numberName) {

    		showExtraTabs(1);

    		currNumberId = numberId;
        	
        	$("#NUMBER_ID").val(numberId);
        	$("#NUMBER_NAME").textbox('setValue',numberName);

        	//重新加载号码列表
        	findDataForTelephone();

	    	$("#autoNumberSaveBtn").attr("onclick","autoNumberSaveEdit()");
	    	//alert("aaaa");
        	$("#autoNumberDlg").dialog('setTitle','修改号码组').dialog('open');
    	}

    	function autoNumberSaveAdd() {
        	     
			$("#autoNumberForm").form('submit',{
    			
				url:'autoNumber/add',
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

						findData();            //重新加载号码组数据
						
						showExtraTabs(1);     //显示导入号码tab 和 号码列表 tab
						
						var nId = result.extraMessage;   //号码组ID值
						currNumberId = nId;           //设置为当前号码组ID

						$("#NUMBER_ID").val(nId);
						
						//重新加载号码列表
						findDataForTelephone();

						//暂时不关闭窗口，所以再点击保存按钮时，就是修改操作了
						$("#autoNumberSaveBtn").attr("onclick","autoNumberSaveEdit()");
						$("#autoNumberDlg").dialog('setTitle','编辑号码组');
						
						
					}
				}
				
			});
    	}

    	function autoNumberSaveEdit() {

			$("#autoNumberForm").form('submit',{
				url:'autoNumber/update',
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

    	function autoNumberDel(numberId) {
    		$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...'
					});
					$.ajax({
						url:'autoNumber/delete?&numberId=' + numberId,
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress("close");
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								findData();   //重新加载号码组数据					
							}
						}
					});
				}
			});
        	
    	}

		function autoNumberTelephoneAdd() {

			$("#autoNumberTelephoneSaveBtn").attr("onclick","autoNumberTelephoneSaveAdd()");
	    	
	    	$("#autoNumberTelephoneDlg").dialog('setTitle','新增号码').dialog('open');
		    
	    }
	    
    	function autoNumberTelephoneEdit(telId,telephone,clientName) {
    		$("#TEL_ID").val(telId);
    		$("#TELEPHONE").numberbox('setValue',telephone);
    		$("#CLIENT_NAME").textbox('setValue',clientName);

			$("#autoNumberTelephoneSaveBtn").attr("onclick","autoNumberTelephoneSaveEdit()");

			$("#autoNumberTelephoneDlg").dialog('setTitle','修改号码').dialog('open');
        	
    	}
    	

    	//号码组号码的添加保存
    	function autoNumberTelephoneSaveAdd(){

    		$("#autoNumberTelephoneForm").form('submit',{
    			
				url:'autoNumberTelephone/add?numberId=' + currNumberId,
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
						$("#autoNumberTelephoneDlg").dialog('close');
					}
				}
				
			});
        	
    	}

    	//号码组号码的修改保存
    	function autoNumberTelephoneSaveEdit(){

    		$("#autoNumberTelephoneForm").form('submit',{

				url:'autoNumberTelephone/update?numberId=' + currNumberId,
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
						$("#autoNumberTelephoneDlg").dialog('close');
					}
					
				}
				
			});
    		
        	
    	}
    	

    	function autoNumberTelephoneDel() {

    		$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...'
					});
					$.ajax({
						url:'autoNumberTelephone/delete?&ids=' + getTelephoneSelectedRows(),
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

    			url:'autoNumberTelephone/uploadFile?numberId=' + currNumberId,
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
			
			var rows = $('#autoNumberTelephoneDg').datagrid('getSelections');
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

			$("#autoNumberDg").datagrid('load',{
				numberName:$("#numberName").textbox('getValue'),
				orgCode:orgCodes,
				startTime:startTime,
				endTime:endTime
			});
        	
    	}
			
    	function findDataForTelephone() {
    		$("#autoNumberTelephoneDg").datagrid('load',{
        		numberId:currNumberId,
        		telephone:$('#telephone').textbox('getValue'),
    			clientName:$('#clientName').textbox('getValue')
        	});
    	}

		function rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:autoNumberEdit(\"" + data.NUMBER_ID + "\",\"" + data.NUMBER_NAME + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:autoNumberDel(\"" + data.NUMBER_ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}

		function telephonerowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:autoNumberTelephoneEdit(\"" + data.TEL_ID + "\",\"" + data.TELEPHONE + "\",\"" + data.CLIENT_NAME + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>";
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
				<td>号码组名字：<input id="numberName" type="text" class="easyui-textbox" style="width:200px;"/>
				
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
		
		<table id="autoNumberDg">
			<thead>
				<tr style="height:12px;">		
					<th data-options="field:'NUMBER_NAME',width:300,align:'center'">号码组名称</th>
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
	<a href="#" id="easyui-add" onclick="autoNumberAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增号码组</a>
</div>

<div id="autoNumberDlg" class="easyui-dialog" style="width:850px;height:450px;padding:5px;" modal="true" closed="true">
		<!-- 包含号码组表单 -->
		<%@ include file="/autocall/number/_form.jsp" %>
</div>

<div id="autoNumberTelephoneDlg" class="easyui-dialog" style="width:500px;height:400px;padding:5px;" modal="true" closed="true">
		<!-- 包含号码组号码表单 -->
		<%@ include file="/autocall/number/_telephoneform.jsp" %>
</div>

</body>
</html>