<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" width="680"
					  style="border-collapse: collapse;">
			<tr>
				<td style="width:80px;text-align:center;">接触名称</td>
				<td>
					<input name="autoContact.ID" id="ID" class="easyui-validatebox" type="text" style="display:none;"></input>
					<input style="width:200px;" name="autoContact.CONTACT_NAME" id="CONTACT_NAME" class="easyui-textbox" type="text" required="true" missingMessage="自动接触名称不能为空!"></input>
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">座席号码</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<input style="width:150px;" name="autoContact.AGENT_NUMBER" id="AGENT_NUMBER" class="easyui-numberbox" type="text" required="true" missingMessage="坐席号码不能为空!"></input> <span style="color: red;">* 点击自动接触端的号码</span>
			        </div>
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">服务号码</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<input style="width:150px;" name="autoContact.CLIENT_NUMBER" id="CLIENT_NUMBER" class="easyui-numberbox" type="text" required="true" missingMessage="服务号码不能为空!"></input> <span style="color: red;">* 提供相关服务的号码</span>
			        </div>
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">识别符</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<input style="width:100px;" name="autoContact.IDENTIFIER" id="IDENTIFIER" class="easyui-textbox" type="text" required="true" missingMessage="识别符不能为空!"></input> <span style="color: red;">* 识别符是当远端调用接口时，根据识别符取出座席号和服务号进行自动连接</span>
			        </div>
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">主叫号码</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<input style="width:100px;" name="autoContact.CALLERID" id="CALLERID" class="easyui-numberbox" type="text"></input>
			        </div>
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">URL</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<input style="width:300px;" name="autoContact.URL_INFO" id="URL_INFO" class="easyui-textbox" type="text"></input> <span style="color: red;">* 仅用于在复制到远端作为调用方法</span>
			        </div>
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">备注</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<input name="autoContact.MEMO" id="MEMO" class="easyui-textbox" data-options="multiline:true" style="width:200px;height:50px;"></input>
			        </div>
				</td>
			</tr>
		</table>

<div id="addAutoContactBtn" style="padding-top:10px;padding-left:80px;">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="cancel()">取消</a>
</div>  