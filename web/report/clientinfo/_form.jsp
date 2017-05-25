<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
			<tr>
				<td>客户号码：</td>
				<td>
					<input style="width:200px;" name="clientInfo.CLIENT_TELEPHONE" id="CLIENT_TELEPHONE" class="easyui-numberbox" type="text" required="true" missingMessage="客户号码不能为空"></input>
					<input name="clientInfo.CLIENT_NO" id="CLIENT_NO" class="easyui-validatebox" type="hidden"></input>
				</td>
			</tr>
			<tr>
				<td>客户姓名：</td>
				<td>
					<input style="width:200px;" name="clientInfo.CLIENT_NAME" id="CLIENT_NAME" class="easyui-textbox" type="text"></input>
				</td>
			</tr>
			<tr>
				<td>性&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;别：</td>
				<td>
					<select class="easyui-combobox" style="width: 120px;" name="clientInfo.CLIENT_SEX" id="CLIENT_SEX" data-options="panelHeight:'auto'">
			              <option value="1">男</option>
			              <option value="0">女</option>
			        </select>
				</td>
			</tr>
			
			<tr>
				<td>客户等级：</td>
				<td>
					<input style="width:120px;" class="easyui-combobox" name="clientInfo.CLIENT_LEVEL" id="CLIENT_LEVEL" />
				</td>
			</tr>

			<tr>
				<td>备用号码：</td>
				<td>
					<input style="width:200px;" name="clientInfo.CLIENT_TELEPHONE2" id="CLIENT_TELEPHONE2" class="easyui-numberbox" type="text"></input>
				</td>
			</tr>
			<tr>
				<td>QQ&nbsp;&nbsp;号码：</td>
				<td>
					<input style="width:200px;" name="clientInfo.CLIENT_QQ" id="CLIENT_QQ" class="easyui-numberbox" type="text"></input>
				</td>
			</tr>
			<tr>
				<td>&nbsp;&nbsp;E-mail：</td>
				<td>
					<input style="width:200px;" name="clientInfo.CLIENT_EMAIL" id="CLIENT_EMAIL" class="easyui-textbox" type="text"></input>
				</td>
			</tr>
			<tr>
				<td>公&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;司：</td>
				<td>
					<input style="width:200px;" name="clientInfo.CLIENT_COMPANY" id="CLIENT_COMPANY" class="easyui-textbox" type="text"></input>
				</td>
			</tr>
			<tr>
				<td>地&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;址：</td>
				<td>
					<input name="clientInfo.CLIENT_ADDRESS" id="clientInfo.ADDRESS" class="easyui-textbox" data-options="multiline:true" style="width:200px;height:50px;"></input>
				</td>
			</tr>
			
		</table>
		
		<div id="addCLientInfoDlgBtn" style="margin-top:15px;margin-left:70px;">
			<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>&nbsp;&nbsp;&nbsp;&nbsp;
			<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="add_cancel()">取消</a>
		</div>  