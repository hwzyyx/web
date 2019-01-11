<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>
			<div style="padding-top:10px;">
				<input type="hidden" name="sys_callerid_group.GROUP_ID" id="GROUP_ID"/>
				主叫号码组名：<input name="sys_callerid_group.GROUP_NAME" id="GROUP_NAME" style="width:300px;" class="easyui-textbox"  required="true" missingMessage="主叫号码组名不能为空!"></input>
			</div>
		</td>
	</tr>
</table>

<div id="formBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="doCancel()">取消</a>
</div>
