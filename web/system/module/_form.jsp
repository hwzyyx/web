<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
			<tr>
				<td>菜单编码：
					<input name="module.MODULE_CODE" id="MODULE_CODE" style="width:200px;" class="easyui-textbox" required="true" missingMessage="菜单编码不能为空!"/>
				</td>
				<td style="padding-left:30px;">菜单名称：
					<input name="module.MODULE_NAME" id="MODULE_NAME" style="width:200px;" class="easyui-textbox" required="true" missingMessage="菜单名称不能为空"/>
				</td>
			</tr>
			
			<tr>
				<td>菜单链接：
					<input name="module.MODULE_URI" id="MODULE_URI" style="width:200px;" class="easyui-textbox">
				</td>
				<td style="padding-left:30px;">菜单描述：
					<input name="module.MODULE_DESC" id="MODULE_DESC" style="width:200px;" class="easyui-textbox">
					<input type="hidden" name="module.PARENT_CODE" id="PARENT_CODE">
					<span style="padding-left:30px;" id="addOrgDlgBtn">
						<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
						<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="add_cancel()">取消</a>
					</span>  
				</td>
			</tr>
		</table>
		