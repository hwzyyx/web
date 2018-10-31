<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>主叫号码</td>
		<td>
			<input type="hidden" name="sysCallerId.ID" id="ID"/>
			<input name="sysCallerId.CALLERID" id="CALLERID" style="width:150px;" class="easyui-textbox"  type="text" required="true" missingMessage="主叫号码不能为空!"></input>
		</td>
	</tr>
	<tr>
		<td>号码用途</td>
		<td>
			<input name="sysCallerId.PURPOSE" id="PURPOSE" style="width:300px;" class="easyui-textbox" type="text" required="true" missingMessage="号码用途不能为空!"></input>
		</td>
	</tr>
	
</table>
		
<div id="addCallerIdDlgBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="add_cancel()">取消</a>
</div>  