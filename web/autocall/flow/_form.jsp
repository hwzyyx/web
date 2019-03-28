<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- 
<table>
	<tr>
		<td>
			<div style="padding-top:10px;">
				<input type="hidden" name="ac_flow.FLOW_ID" id="FLOW_ID"/>
				流程名称：<input name="ac_flow.FLOW_NAME" id="FLOW_NAME" style="width:300px;" class="easyui-textbox"  required="true" missingMessage="流程名称不能为空!"></input>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div style="padding-top:10px;">
				流程规则：<input name="ac_flow.FLOW_RULE" id="FLOW_RULE" style="width:500px;height:100px;" class="easyui-textbox" data-options="multiline:true" required="true" missingMessage="流程规则不能为空!"></input>
			</div>
		</td>
	</tr>
</table>
 -->

<div class="easyui-panel" title="" style="height:90%;padding:10px;" data-options="fit:true">
	<div class="easyui-layout" style="width:450px;height:500px;" data-options="fit:true">
		<div data-options="region:'west',split:true" style="width:450px;height:600px;padding:10px">
			<table>
				<tr>
					<td>
						<div style="padding-top:10px;">
							<input type="hidden" name="ac_flow.FLOW_ID" id="FLOW_ID"/>
							流程名称：<input name="ac_flow.FLOW_NAME" id="FLOW_NAME" style="width:300px;" class="easyui-textbox"  required="true" missingMessage="流程名称不能为空!"></input>
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div style="padding-top:10px;">
							流程规则：<input name="ac_flow.FLOW_RULE" id="FLOW_RULE" style="width:300px;height:100px;" class="easyui-textbox" data-options="multiline:true" required="true" missingMessage="流程规则不能为空!"></input>
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div style="padding-top:10px;">
							选择并插入参数类型：
						</div>
						<div style="padding-top:10px;">
							<a href="#" class="easyui-linkbutton c1" onclick="insertText()" style="width:80px;">文字文本(%s)</a>
							<a href="#" class="easyui-linkbutton c3" onclick="insertNumber()" style="width:80px;margin-left:10px;">数字号码(%i)</a>
							<a href="#" class="easyui-linkbutton c4" onclick="insertNumber()" style="width:80px;margin-left:10px;">数字金额(%f)</a>
							<a href="#" class="easyui-linkbutton c5" onclick="insertDate()" style="width:80px;margin-left:10px;">日期类型($d)</a>
						</div>
						<hr>
						<div style="padding-top:10px;">
							参数列表：
						</div>
						<div style="padding-top:10px;">
							<a href="#" class="easyui-linkbutton c1" onclick="insertText()" style="width:350px;">参数1：同一个是困惑堁荔地柜橱fdsafdsafdsafdsafdsafdsafdsafdsafdsafdsafdsafdadfadsafdsafsd</a>
						</div>
					</td>
				</tr>
			</table>
		</div>
		<div data-options="region:'center'" style="width:100px;padding:10px">
			Right Content
		</div>
	</div>
</div>
 

<div id="formBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="doCancel()">取消</a>
</div>
