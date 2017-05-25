<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

	<table>
			<tr>
				<td>操作工号：
					<input type="hidden" name="operator.ORG_CODE" id="ORG_CODE"/>
					<input name="operator.OPER_ID" onblur="operIdBlur()" style="width:150px;" id="OPER_ID" class="easyui-textbox" required="true" missingMessage="工号不能为空!"/>
				</td>

				<td style="padding-left:30px;">工号名称：
					<input name="operator.OPER_NAME" id="OPER_NAME" class="easyui-textbox" required="true" missingMessage="操作员名称不能为空"/>
				</td>
				<td style="padding-left:30px;">状态：
							<select class="easyui-combobox" style="width:150px;" name="operator.STATE" id="STATE" data-options="panelHeight:'auto'">
	              				<option value="1">有效</option>
	              				<option value="0">无效</option>
	        				</select>
				</td>
				<td style="padding-left:30px;">
					<div id="PWD_DIV">
						密码：
						<input name="operator.PASSWORD" id="PWD_ID"  type="password" class="easyui-validatebox textbox" data-options="validType:'length[3,10]'">
					</div>
				</td>
			</tr>
			<tr>
				<td>电话号码：
					<input name="operator.TELNO" style="width:150px;" id="TELNO" class="easyui-numberbox"/>
				</td>
				<td style="padding-left:30px;">座席号码：
					<input name="operator.CALL_NUMBER" style="width:150px;" id="CALL_NUMBER" class="easyui-numberbox">
				</td>
				<td style="padding-left:30px;">性别：
							<select class="easyui-combobox" style="width: 150px;" name="operator.SEX" id="SEX" data-options="panelHeight:'auto'">
	              				<option value="1">男</option>
	              				<option value="0">女</option>
	        				</select>
				</td>
				<td style="padding-left:30px;"> 
						<div id="#addOperatorDlgBtn">
							<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
							<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="add_cancel()">取消</a>
						</div>
				</td>
			</tr>
			<tr>
				<td height="280" colspan="8">
					<table id="roleDg">
						<thead>  
							<tr style="height:12px;">
								<th data-options="field:'ck',checkbox:true"></th>                                
								<th data-options="field:'ROLE_CODE',width:100,align:'center'">角色编码</th>                
								<th data-options="field:'ROLE_NAME',width:100,align:'center'">角色名称</th>                
								<th data-options="field:'ROLE_STATE',width:100,align:'center',formatter:roleStateFormatter">状态</th>
							</tr>        
						</thead>	
					</table>
				</td>
			</tr>
		</table>
		
	

		