<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
			<tr>
				<td>组织编码</td>
				<td>
					<input name="org.ORG_CODE" id="ORG_CODE" style="width:150px;background-color: '#e3e3e3'" class="easyui-textbox" required="true" missingMessage="组织编码不能为空!"/>
				</td>
			</tr>
			<tr>
				<td>组织名称</td>
				<td>
					<input name="org.ORG_NAME" id="ORG_NAME" style="width:150px;" class="easyui-textbox" required="true" missingMessage="组织名称不能为空"/>
				</td>
			</tr>
			
			<tr>
				<td>组织描述</td>
				<td>
					<input name="org.ORG_DESC" id="ORG_DESC" style="width:150px;" type="text" class="easyui-textbox">
				</td>
			</tr>

			<tr>
			<tr style="display:none;">
				<td>父组织</td>
				<td>
					<input name="org.PARENT_ORG_CODE" id="PARENT_ORG_CODE">
				</td>
			</tr>
			
		</table>
		
		<div id="addOrgDlgBtn">
			<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
			<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="add_cancel()">取消</a>
		</div>  