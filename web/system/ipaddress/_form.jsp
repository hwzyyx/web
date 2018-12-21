<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>
			<div style="padding-top:10px;">
				<input type="hidden" name="sys_ip_address.ID" id="ID"/>
				IP地址：<input name="sys_ip_address.IP_ADDRESS" id="IP_ADDRESS" style="width:150px;" class="easyui-textbox"  required="true" missingMessage="IP地址不能为空!"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				备&nbsp;&nbsp;&nbsp;&nbsp;注：<input name="sys_ip_address.MEMO" id="MEMO" style="width:150px;" class="easyui-textbox"  required="true" missingMessage="备注不能为空!"></input>
			</div>
		</td>
	</tr>
</table>

<div id="formBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="doCancel()">取消</a>
</div>
