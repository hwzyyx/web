<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>项目名称</td>
		<td>
			<input name="calltask.TASK_NAME" id="TASK_NAME" class="easyui-validatebox" type="text" required="true" missingMessage="项目名称不能为空!"></input>
		</td>
	</tr>
	<tr>
		<td>主叫号码</td>
		<td>
			<input name="calltask.CALLERID" id="CALLERID" class="easyui-numberbox" type="text" required="true" missingMessage="主叫号码不能为空!"></input>
		</td>
	</tr>
</table>

		
<div id="addTaskDlgBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="add_cancel()">取消</a>
</div> 