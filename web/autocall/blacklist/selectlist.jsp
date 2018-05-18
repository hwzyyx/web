<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<script type="text/javascript">

	var currBlackListId = null;

	function execSelectBlackList(orgComboTreeData) {

		//$("#blackList_startTime").datebox("setValue",getCurrMonthDay1());
		$("#blackList_endTime").datebox("setValue",getCurrDate());

		$("#blackList_orgCode").combotree('loadData',orgComboTreeData).combotree({

			onLoadSuccess:function(node,data){
			
				//设置默认全选
				var t = $("#blackList_orgCode").combotree("tree");
				for(var i=0;i<data.length;i++) {
					node = t.tree("find",data[i].id);
					t.tree('check',node.target);
				}
	
				var selectRs = $("#blackList_orgCode").combotree('getValues');
				var orgCodes = selectRs.toString();
				var startTime = $("#blackList_startTime").datebox('getValue');
				var endTime = $("#blackList_endTime").datebox('getValue');
				
				//加载之后,马上对黑名单列表定义及加载
				$("#autoBlackListDg").datagrid({
					pageSize:15,
					pagination:true,
					fit:true,
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
			singleSelect:true,
			rownumbers:true,
			rowrap:true,
			striped:true,
			pageList:[10,20,30],
			url:'autoBlackListTelephone/datagrid'
		}).datagrid("loadData",{total:0,rows:[]});

		$("#autoBlackListTelephoneDlg").dialog({
			onClose:function() {
				currBlackListId = null;
			}
		});
		
	}


	function blackList_findData(){
		var selectRs = $("#blackList_orgCode").combotree('getValues');
		var orgCodes = selectRs.toString();
		var startTime = $("#blackList_startTime").datebox('getValue');
		var endTime = $("#blackList_endTime").datebox('getValue');

		$("#autoBlackListDg").datagrid('load',{
			blackListName:$("#blackListName").textbox('getValue'),
			orgCode:orgCodes,
			startTime:startTime,
			endTime:endTime
		});
	}

	function blacklistrowformatter(value,data,index) {
		return "<a href='#' onclick='javascript:showBlackListTelephone(\"" + data.BLACKLIST_ID + "\")'>号码列表</a>";
	}

	function blackList_findDataForTelephone() {

		$("#autoBlackListTelephoneDg").datagrid("load",{
    		blackListId:currBlackListId,
    		telephone:$('#blackListTelephone_telephone').textbox('getValue'),
			clientName:$('#blackListTelephone_clientName').textbox('getValue')
    	});
		
	}
	
	function showBlackListTelephone(blackListId) {

		currBlackListId = blackListId;
		$("#autoBlackListTelephoneDlg").dialog('setTitle','号码列表').dialog("open");
		
		blackList_findDataForTelephone();
	}

</script>
<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:75px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>黑名单名字：<input id="blackListName" type="text" class="easyui-textbox" style="width:200px;"/>
				
					<span style="padding-left:30px;">
						选择组织：<select id="blackList_orgCode" class="easyui-combotree" multiple style="width:200px;"></select>
					</span>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<span style="">
						&nbsp;&nbsp;&nbsp;创建时间：<input id="blackList_startTime" style="width:200px;" name="blackList_startTime" class="easyui-datebox" /><span style="padding-left:39px;padding-right:39px;">至</span> <input id="blackList_endTime" style="width:200px;" name="blackList_endTime" class="easyui-datebox" />
					</span>
					<span style="padding-left:30px;">
						<a href="javascript:blackList_findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
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
					<th data-options="field:'id',width:100,align:'center',formatter:blacklistrowformatter">操作</th -->
				</tr>
				
			</thead>
		</table>	
		
	</div>
</div>

<div id="autoBlackListTelephoneDlg" class="easyui-dialog" style="width:600px;height:600px;padding:5px;" modal="true" closed="true">
	<div data-options="fit:true" class="easyui-layout">
		<!-- 顶部查询区 -->
		<div data-options="region:'north',split:true,border:true" style="height:45px;padding-top:5px;padding-left:5px;">
			<table>
				<tr style="vertical-align: top;">
					<td>电话号码：<input id="blackListTelephone_telephone" type="text" class="easyui-textbox" style="width:150px;"/>
					
						<span style="padding-left:20px;">
							客户姓名：<input id="blackListTelephone_clientName" type="text" class="easyui-textbox" style="width:150px;"/>
						</span>
						<span style="padding-left:10px;">
							<a href="javascript:blackList_findDataForTelephone()" style="width:80px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
						</span>
					</td>
				</tr>
			</table>
		</div>
	
		<!-- 数据显示区 -->
		<div data-options="region:'center',split:true,border:false">
			
			<table id="autoBlackListTelephoneDg">
				<thead>
					<tr style="height:12px;">		
						<th data-options="field:'TELEPHONE',width:200,align:'center'">电话号码</th>
						<th data-options="field:'CLIENT_NAME',width:200,align:'center'">客户姓名</th>
					</tr>
				</thead>
			</table>	
		</div>
	</div>
</div>




