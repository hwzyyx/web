<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<script type="text/javascript">

	var currNumberId = null;

	function execSelectNumber(orgComboTreeData) {

		//$("#nubmer_startTime").datebox("setValue",getCurrMonthDay1());
		$("#number_endTime").datebox("setValue",getCurrDate());

		$("#number_orgCode").combotree('loadData',orgComboTreeData).combotree({

			onLoadSuccess:function(node,data){
			
				//设置默认全选
				var t = $("#number_orgCode").combotree("tree");
				for(var i=0;i<data.length;i++) {
					node = t.tree("find",data[i].id);
					t.tree('check',node.target);
				}
	
				var selectRs = $("#number_orgCode").combotree('getValues');
				var orgCodes = selectRs.toString();
				var startTime = $("#number_startTime").datebox('getValue');
				var endTime = $("#number_endTime").datebox('getValue');
				
				//加载之后,马上对号码组列表定义及加载
				$("#autoNumberDg").datagrid({
					pageSize:15,
					pagination:true,
					fit:true,
					singleSelect:true,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[10,15,20],
					url:'autoNumber/datagrid',
					queryParams:{
						numberName:$("#numberName").textbox('getValue'),
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
			singleSelect:true,
			rownumbers:true,
			rowrap:true,
			striped:true,
			pageList:[10,20,30],
			url:'autoNumberTelephone/datagrid'
		}).datagrid("loadData",{total:0,rows:[]});
		
		$("#autoNumberTelephoneDlg").dialog({
			onClose:function() {
				currNumberId = null;
			}
		});
		
	}

	function number_findData(){
		var selectRs = $("#number_orgCode").combotree('getValues');
		var orgCodes = selectRs.toString();
		var startTime = $("#number_startTime").datebox('getValue');
		var endTime = $("#number_endTime").datebox('getValue');

		$("#autoNumberDg").datagrid('load',{
			numberName:$("#numberName").textbox('getValue'),
			orgCode:orgCodes,
			startTime:startTime,
			endTime:endTime
		});
	}

	function numberrowformatter(value,data,index) {
		return "<a href='#' onclick='javascript:showNumberTelephone(\"" + data.NUMBER_ID + "\")'>号码列表</a>";
	}

	function number_findDataForTelephone() {

		$("#autoNumberTelephoneDg").datagrid("load",{
    		numberId:currNumberId,
    		customerTel:$('#numberTelephone_customerTel').textbox('getValue'),
			customerName:$('#numberTelephone_customerName').textbox('getValue')
    	});
		
	}
	
	function showNumberTelephone(numberId) {

		currNumberId = numberId;
		$("#autoNumberTelephoneDlg").dialog('setTitle','号码列表').dialog("open");
		
		number_findDataForTelephone();
	}

</script>
<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:75px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>号码组名字：<input id="numberName" type="text" class="easyui-textbox" style="width:200px;"/>
				
					<span style="padding-left:30px;">
						选择组织：<select id="number_orgCode" class="easyui-combotree" multiple style="width:200px;"></select>
					</span>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<span style="">
						&nbsp;&nbsp;&nbsp;创建时间：<input id="number_startTime" style="width:200px;" name="number_startTime" class="easyui-datebox" /><span style="padding-left:39px;padding-right:39px;">至</span> <input id="number_endTime" style="width:200px;" name="number_endTime" class="easyui-datebox" />
					</span>
					<span style="padding-left:30px;">
						<a href="javascript:number_findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
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
					<th data-options="field:'id',width:100,align:'center',formatter:numberrowformatter">操作</th>
				</tr>
				
			</thead>
		</table>	
		
	</div>
</div>

<div id="autoNumberTelephoneDlg" class="easyui-dialog" style="width:600px;height:600px;padding:5px;" modal="true" closed="true">
	<div data-options="fit:true" class="easyui-layout">
		<!-- 顶部查询区 -->
		<div data-options="region:'north',split:true,border:true" style="height:45px;padding-top:5px;padding-left:5px;">
			<table>
				<tr style="vertical-align: top;">
					<td>电话号码：<input id="numberTelephone_customerTel" type="text" class="easyui-textbox" style="width:150px;"/>
					
						<span style="padding-left:20px;">
							客户姓名：<input id="numberTelephone_customerName" type="text" class="easyui-textbox" style="width:150px;"/>
						</span>
						<span style="padding-left:10px;">
							<a href="javascript:number_findDataForTelephone()" style="width:80px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
						</span>
					</td>
				</tr>
			</table>
		</div>
	
		<!-- 数据显示区 -->
		<div data-options="region:'center',split:true,border:false">
			
			<table id="autoNumberTelephoneDg">
				<thead>
					<tr style="height:12px;">		
						<th data-options="field:'CUSTOMER_TEL',width:200,align:'center'">电话号码</th>
						<th data-options="field:'CUSTOMER_NAME',width:200,align:'center'">客户姓名</th>
					</tr>
				</thead>
			</table>	
		</div>
	</div>
</div>




