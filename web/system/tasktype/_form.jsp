<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>
			<div style="padding-top:10px;">
				<input type="hidden" name="sys_task_type.ID" id="ID"/>
				任务类型：<input name="sys_task_type.TASK_TYPE" id="TASK_TYPE" style="width:150px;" class="easyui-textbox"  required="true" missingMessage="任务类型不能为空!"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				任务序号：<input name="sys_task_type.NUMBER_ORDER" id="NUMBER_ORDER" style="width:150px;" class="easyui-numberbox" required="true" missingMessage="任务序号不能为空!"></input>
			</div>
		</td>
	</tr>
</table>

<div id="formBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="doCancel()">取消</a>
</div>