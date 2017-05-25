<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
			<tr>
				<td>客户号码</td>
				<td>
					<input name="blacklist.CLIENT_TELEPHONE" id="CLIENT_TELEPHONE" style="width:200px;" class="easyui-numberbox" required="true" missingMessage="客户号码不能为空!"></input>
					<input type="hidden" name="blacklist.BLACKLIST_ID" id="BLACKLIST_ID" class="easyui-validatebox"></input>
				</td>
			</tr>
			<tr>
				<td>客户姓名</td>
				<td>
					<input name="blacklist.CLIENT_NAME"  style="width:200px;" id="CLIENT_NAME" class="easyui-textbox" type="text" required="true" missingMessage="客户姓名不能为空!"></input>
				</td>
			</tr>
			
			<tr>
				<td>状态</td>
				<td>
					<select class="easyui-combobox" style="width: 155px;" name="blacklist.STATE" id="STATE"  data-options="panelHeight:'auto'">
         				<option value="1">有效</option>
         				<option value="0">无效</option>
   					</select>
				</td>
			</tr>
			<tr>
				<td>黑名单原因</td>
				<td>
					<input name="blacklist.REASON" id="REASON" data-options="multiline:true" style="width:200px;height:50px;" class="easyui-textbox" type="text" required="true" missingMessage="黑名单原因不能为空!"></input>
				</td>
			</tr>
		</table>
		
		<div id="addBlacklistDlgBtn">
			<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
			<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="add_cancel()">取消</a>
		</div>  