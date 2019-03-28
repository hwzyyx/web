<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>
			<div style="padding-top:10px;">
				<input type="hidden" name="cnn_callin_data.ID" id="ID"/>
				主叫号码：<input name="cnn_callin_data.CALLERID" id="CALLERID" style="width:150px;" class="easyui-textbox"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				被叫号码：<input name="cnn_callin_data.CALLEE" id="CALLEE" style="width:150px;" class="easyui-textbox"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				状态：<input name="cnn_callin_data.STATE" id="STATE" style="width:150px;" class="easyui-textbox"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				来电时间：<input name="cnn_callin_data.CALL_DATE" id="CALL_DATE" style="width:150px;" class="easyui-textbox"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				改号信息：<input name="cnn_callin_data.PK_CNN_DATA_ID" id="PK_CNN_DATA_ID" style="width:150px;" class="easyui-textbox"></input>
			</div>
		</td>
	</tr>
</table>

<div id="formBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="doCancel()">取消</a>
</div>
