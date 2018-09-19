<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
			<tr style="height:40px;">
				<td>
					参数编码：<input name="param.PARAM_CODE" id="PARAM_CODE" style="width:200px;" class="easyui-textbox" type="text" required="true" missingMessage="参数编码不能为空!"></input>
				</td>
			</tr>
			<tr style="height:40px;">
				<td>
					参数名称：<input name="param.PARAM_NAME" id="PARAM_NAME" style="width:200px;" class="easyui-textbox" type="text" required="true" missingMessage="参数名称不能为空!"></input>
				</td>
			</tr>
			
			<tr tr style="height:40px;">
				<td>
					参数赋值：<input name="param.PARAM_VALUE" id="PARAM_VALUE" style="width:200px;" class="easyui-textbox" type="text" required="true" missingMessage="参数赋值不能为空!"></input>
				</td>
			</tr>

			<tr tr style="height:120px;">
				<td>
					参数描述：<input name="param.PARAM_DESC" id="PARAM_DESC" style="width:300px;height:100px;" class="easyui-textbox" type="text" data-options="multiline:true"></input>
				</td>
			</tr>
			
		</table>
		
		<div id="dialogBtn">
			<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
			<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="closeDialog()">取消</a>
		</div>  