<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script type="text/javascript">


function execSelectVoice(orgComboTreeData,voiceTypeComboboxDataFor0,selectVoiceType) {

	//$("#voice_startTime").datebox("setValue",getCurrMonthDay1());   //设置默认的开始时间
	$("#voice_endTime").datebox("setValue",getCurrDate());          //设置默认的结束时间

	$("#voice_orgCode").combotree('loadData',orgComboTreeData).combotree({

		onLoadSuccess:function(node,data) {

			// 默认全选
			var t = $("#voice_orgCode").combotree("tree");
			for(var i=0;i<data.length;i++) {
				node = t.tree("find",data[i].id);
				t.tree('check',node.target);
			}

			//语音类型加载
			$("#voiceType").combobox({
				valueField:'id',
				textField:'text'
			}).combobox('loadData',voiceTypeComboboxDataFor0).combobox('setValue',selectVoiceType).combobox('disable');
			
			var selectRs = $("#orgCode").combotree('getValues');
			var voiceTypeRs = $("#voiceType").combobox('getValue');
			var orgCodes = selectRs.toString();
			var startTime = $("#voice_startTime").datebox('getValue');
			var endTime = $("#voice_endTime").datebox('getValue');
			
			$("#voiceDg").datagrid({
				pageSize:10,
				pagination:true,
				fit:true,
				toolbar:'#voice_searchtool',
				singleSelect:true,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[10,15,20],
				url:'voice/datagrid',
				queryParams:{
					voiceDesc:$("#voiceDesc").val(),
					voiceType:voiceTypeRs,
					orgCode:orgCodes,
					startTime:startTime,
					endTime:endTime
				},
				onLoadSuccess:function(data) {
					for(var i=0;i<data.rows.length;i++) {
						eval(data.rows[i].playerFunction);    //播放器设置语音
					}
				}
			});
			
		}
		
	});
	
}

//试听
function listenrowformatter(value,data,index) {
	return data.playerSkin;
}

//下载
function downloadrowformatter(value,data,index) {
	return "<a href='voice/download?path=" + data.path + "' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='下载录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/download.png' style='margin-top:2px;' border='0'></a>";
}

function voice_findData() {

	var selectRs = $("#voice_orgCode").combotree('getValues');
	if(selectRs.length<1) {
		alert("查询时,组织不能为空!");
		return;
	}
	
	var orgCodes = selectRs.toString();
	var voiceTypeRs = $("#voiceType").combobox('getValue');
	//alert("voice_findData().....orgCodes=" + orgCodes + ";voiceTypeRs=" + voiceTypeRs);
	var startTime = $("#voice_startTime").datebox('getValue');
	var endTime = $("#voice_endTime").datebox('getValue');
	
	$('#voiceDg').datagrid('load',{
		voiceDesc:$("#voiceDesc").val(),
		voiceType:voiceTypeRs,
		orgCode:orgCodes,
		startTime:startTime,
		endTime:endTime
	});
	
}

</script>

<table id="voiceDg">
	<thead>
	
		<tr style="height:12px;">		
			<th data-options="field:'VOICE_DESC',width:100,align:'center'">语音描述</th>
			<th data-options="field:'VOICE_TYPE_DESC',width:100,align:'center'">语音类型</th>
			<th data-options="field:'CREATE_USERCODE_DESC',width:100,align:'center'">创建人</th>
			<th data-options="field:'ORG_CODE_DESC',width:150,align:'center'">部门(组织)名字</th>
			<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
			<th data-options="field:'listen',width:35,align:'center',formatter:listenrowformatter">试听</th>
			<th data-options="field:'download',width:50,align:'center',formatter:downloadrowformatter">下载</th>
		</tr>
		
	</thead>
</table>

<div id="voice_searchtool" style="padding:5px;">
	
	<table border="0">
		<tr style="vertical-align: top;">
			<td>语音描述：<input id="voiceDesc" type="text" class="easyui-textbox" style="width:150px;"/></td>
			<td style="padding-left:15px;">语音类型：<input id="voiceType" class="easyui-combobox" style="width:100px;"/></td>
			<td style="padding-left:15px;">
				选择组织：<select id="voice_orgCode" class="easyui-combotree" multiple style="width:150px;"></select>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				创建时间：<input id="voice_startTime" width="35" name="voice_startTime" class="easyui-datebox" /><span style="padding-left:15px;padding-right:15px;">至</span> <input id="voice_endTime" width="35" name="voice_endTime" class="easyui-datebox" />
			</td>
			<td style="padding-left:75px;">
				<a href="javascript:voice_findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
			</td>
		</tr>
	</table>
</div>


