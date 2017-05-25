<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 编辑/添加　窗口 -->
<div data-options="fit:true" class="easyui-layout">
		<form id="autoBlackListTelephoneForm" method="post">
			<table>
				<tr>
					<td style="padding-left:10px;">
						客户号码：
						<input type="hidden" name="autoBlackListTelephone.TEL_ID" id="TEL_ID"/>
						<input style="width:200px;" id="TELEPHONE" name="autoBlackListTelephone.TELEPHONE" class="easyui-numberbox" type="text" required="true" missingMessage="电话号码不能为空!"></input>
					</td>
				</tr>
				<tr>
					<td style="padding-left:10px;">
						客户姓名：
						<input style="width:200px;" id="CLIENT_NAME" name="autoBlackListTelephone.CLIENT_NAME" class="easyui-textbox" type="text" required="true" missingMessage="客户姓名不能为空!"></input>
						&nbsp;&nbsp;&nbsp;<a href="javascript:#" id="autoBlackListTelephoneSaveBtn"  class="easyui-linkbutton" data-options="iconCls:'icon-save'" onclick="autoBlackListTelephoneSaveAdd()">保存</a>
					</td>
				</tr>
			</table>
		</form>
</div>
