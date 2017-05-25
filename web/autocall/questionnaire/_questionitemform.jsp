<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 编辑/添加　窗口 -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->			
		<form id="questionItemForm" method="post">
			<table>
				<tr>
					<td style="width:70px;">
						&nbsp;&nbsp;响应按键</td>
					<td>
						<input width="40" id="ITEM_CODE" name="questionItem.ITEM_CODE" class="easyui-numberbox" type="text" required="true" missingMessage="响应按键不能为空!"></input>
					</td>
				</tr>
				<tr>
					<td>&nbsp;&nbsp;选项内容</td>
					<td>
						<input width="40" id="ITEM_DESC" name="questionItem.ITEM_DESC" class="easyui-validatebox" type="text" required="true" missingMessage="选项内容不能为空!"></input>
						&nbsp;&nbsp;&nbsp;<a href="javascript:#" id="questionItemSaveBtn"  class="easyui-linkbutton" data-options="iconCls:'icon-save'" onclick="questionItemSaveAdd()">保存</a>
					</td>
				</tr>
			</table>
			
		</form>
</div>
