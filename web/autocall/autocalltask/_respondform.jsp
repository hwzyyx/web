<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<div data-options="fit:true" class="easyui-layout">
	<table id="formTable" border="1" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" style="width:100%;height:400px;text-align:center;border-collapse: collapse;">
		<tr style="height:30px;">
			<td style="width:150px;background-color: #f3f6f9;vertical-align: top;padding-top: 30px;">
				<div id="questionCountDiv"></div>
				
				<a href="#" id="question1" style="width:140px;margin-top: 0px;" class="easyui-linkbutton c1" iconCls='icon-undo'><span style="font-weight: bolder;">&nbsp;&nbsp;&nbsp;&nbsp;问题 1</span></a>
				<a href="#" id="question2" style="width:140px;margin-top:10px;" class="easyui-linkbutton c3" iconCls='icon-undo'><span style="font-weight: bolder;">&nbsp;&nbsp;&nbsp;&nbsp;问题 2</span></a>
				<a href="#" id="question3" style="width:140px;margin-top:10px;" class="easyui-linkbutton c7" iconCls='icon-undo'><span style="font-weight: bolder;">&nbsp;&nbsp;&nbsp;&nbsp;问题 3</span></a>
				<a href="#" id="question4" style="width:140px;margin-top:10px;" class="easyui-linkbutton c8" iconCls='icon-undo'><span style="font-weight: bolder;">&nbsp;&nbsp;&nbsp;&nbsp;问题 4</span></a>
				<a href="#" id="question5" style="width:140px;margin-top:10px;" class="easyui-linkbutton c5" iconCls='icon-undo'><span style="font-weight: bolder;">&nbsp;&nbsp;&nbsp;&nbsp;问题 5</span></a>
				<a href="#" id="question6" style="width:140px;margin-top:10px;" class="easyui-linkbutton c6" iconCls='icon-undo'><span style="font-weight: bolder;">&nbsp;&nbsp;&nbsp;&nbsp;问题 6</span></a>
				<a href="#" id="question7" style="width:140px;margin-top:10px;" class="easyui-linkbutton c2" iconCls='icon-undo'><span style="font-weight: bolder;">&nbsp;&nbsp;&nbsp;&nbsp;问题 7</span></a>
				<a href="#" id="question8" style="width:140px;margin-top:10px;" class="easyui-linkbutton c4" iconCls='icon-undo'><span style="font-weight: bolder;">&nbsp;&nbsp;&nbsp;&nbsp;问题 8</span></a>
			</td>
			<td style="vertical-align: top;text-align: left;padding-left:5px;">
				
				
				<div id="respondChartDiv" style="width:800px;height:360px;border"></div>
				
				<div style="padding-top:10px;padding-left:250px;">
					<select class="easyui-combobox" style="width: 300px;" id="questionItemCombobox" data-options="panelHeight:'auto'">
					</select>
					<a href="#" id="exportRespondResult" onclick="exportRespondResult()" style="width:140px;" class="easyui-linkbutton" iconCls='icon-search'><span style="font-weight: bolder;">导出客户回复结果</span></a>
				</div>
				
			</td>
		</tr>
	</table>
</div>
 

  