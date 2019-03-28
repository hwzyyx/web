<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>
			<div style="padding-top:10px;">
				<input type="hidden" name="cnn_data.ID" id="ID"/>
				&nbsp;&nbsp;&nbsp;客户姓名：<input name="cnn_data.CUSTOMER_NAME" id="CUSTOMER_NAME" style="width:200px;" class="easyui-textbox"  required="true" missingMessage="客户姓名不能为空!"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				&nbsp;&nbsp;&nbsp;客户号码：<input name="cnn_data.CUSTOMER_TEL" id="CUSTOMER_TEL" style="width:200px;" class="easyui-textbox"  required="true" missingMessage="客户号码不能为空!"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				客户新号码：<input name="cnn_data.CUSTOMER_NEW_TEL" id="CUSTOMER_NEW_TEL" style="width:200px;" class="easyui-textbox"  required="true" missingMessage="客户新号码不能为空!"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;标识符：
				<input type="checkbox" name="cnn_data.FLAG" id="FLAG" value="2"><label for="FLAG">中/英文标识符</label>
				<span style="color:red;margin-left: 20px;">*注：改号播报时,勾选标识符时播放英文语音;否则播放中文语音!</span>
			</div>
		</td>
	</tr>
</table>

<div id="formBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="doCancel()">取消</a>
</div>
