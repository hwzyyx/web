<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<table id="bshVoiceDg" class="easyui-datagrid">
	<thead>
	
		<tr style="height:12px;">		
			<th data-options="field:'VOICE_DESC',width:100,align:'center'">语音描述</th>
			<th data-options="field:'VOICE_TYPE_DESC',width:100,align:'center'">语音类型</th>
			<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
			<th data-options="field:'listen',width:35,align:'center',formatter:listenrowformatter">试听</th>
			<th data-options="field:'download',width:50,align:'center',formatter:downloadrowformatter">下载</th>
		</tr>
		
	</thead>
</table>