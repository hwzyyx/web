<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 编辑/添加　窗口 -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->			
	<div data-options="region:'north',split:true,border:true" style="height:65px">
		<form id="questionForm" method="post">
		<table>
						<tr>
							<td align="right" style="width:80px;">题　　目：</td>
							<td>
								<input type="hidden" name="question.QUESTION_ID" id="QUESTION_ID" class="easyui-validatebox"/>
								<input style="width:300px;" id="QUESTION_DESC" name="question.QUESTION_DESC" class="easyui-validatebox" type="text" required="true" missingMessage="题目不能为空!"></input>
							</td>
						</tr>
						<tr>
							<td align="right">
								语音文件：</td>
							<td>
								<input type="hidden" name="question.VOICE_ID" id="QUESTION_VOICE_ID" class="easyui-validatebox"/>
					 			<input style="width:250px;" name="question.VOICE_DESC" id="QUESTION_VOICE_DESC" class="easyui-textbox" type="text" disabled="true" required="true"></input>
						 		<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectVoice()">选&nbsp;&nbsp;择</a>
								&nbsp;&nbsp;
								<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="createVoice()">创建语音</a>
								&nbsp;&nbsp;
								<a href="javascript:#" id="questionSaveBtn" onclick="questionSaveAdd()"  class="easyui-linkbutton" data-options="iconCls:'icon-save'">保&nbsp;&nbsp;存</a>
							</td>
						</tr>
					</table>
			</form>
	</div>
	
	<!-- 数据显示区 -->
	<!-- 编辑区下半部分列表 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="questionItemDg">
					<thead>  
						<tr style="height:12px;">                
							<th data-options="field:'ckct',checkbox:true"></th>                       
							<th data-options="field:'ITEM_CODE',width:200,align:'center'">响应按键</th>                
							<th data-options="field:'ITEM_DESC',width:200,align:'center'">选项内容</th>                
							<th data-options="field:'id',width:100,align:'center',formatter:item_rowformatter">操作</th>                
						</tr>        
					</thead>
			</table>
	</div>
</div>
