<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 编辑/添加　窗口 -->
<div data-options="fit:true" class="easyui-layout">
		<form id="autoNumberTelephoneForm" method="post">
			<table>
				<tr>
					<td style="padding-left:10px;">
						客户号码：
						<input type="hidden" name="autoNumberTelephone.TEL_ID" id="TEL_ID"/>
						<input style="width:200px;" id="TELEPHONE" name="autoNumberTelephone.TELEPHONE" class="easyui-numberbox" type="text" required="true" missingMessage="电话号码不能为空!"></input>
					</td>
				</tr>
				<tr>
					<td style="padding-left:10px;">
						客户姓名：
						<input style="width:200px;" id="CLIENT_NAME" name="autoNumberTelephone.CLIENT_NAME" class="easyui-textbox" type="text" required="true" missingMessage="客户姓名不能为空!"></input>
						&nbsp;&nbsp;&nbsp;<a href="javascript:#" id="autoNumberTelephoneSaveBtn"  class="easyui-linkbutton" data-options="iconCls:'icon-save'" onclick="autoNumberTelephoneSaveAdd()">保存</a>
					</td>
				</tr>
			</table>
		</form>
</div>
