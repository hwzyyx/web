<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!--<div class="easyui-panel" title="问卷表单" style="width:100%;height:560px;padding:1px;">-->
	
	<div class="easyui-layout" data-options="fit:true"  style="width:100%;height:450px;padding:1px;">
		
		<!-- 左边的问卷预览 -->
		<div data-options="region:'west',split:true" title="问卷预览" style="width:300px;">
			
			<div id="questionnairePreview">
			</div>	
			
		</div>
		<!-- 右边表单编辑区 -->
		<div data-options="region:'center'" title="表单信息">
			
			<div class="easyui-layout" data-options="fit:true" style="width:100%;">
				<!-- 表单编辑区->问卷标题 -->
				<div data-options="region:'north',split:true" style="height:60px;vertical-align: middle;">
					<form id="questionnaireForm" method="post">
						<table border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" style="width:100%;padding-top:15px;">
							<tr style="padding-top: 10px;">
								<td style="width:80px;text-align:center;">
									问卷标题
								</td>
								<td style="width:302px;">
									<input type="hidden" name="questionnaire.QUESTIONNAIRE_ID" id="QUESTIONNAIRE_ID" class="easyui-validatebox"/>
									<input name="questionnaire.QUESTIONNAIRE_DESC" id="QUESTIONNAIRE_DESC" class="easyui-validatebox" type="text" data-options="required:true" style="width:300px;"></input>
									
								</td>
								<td style="width:80px;text-align:center;">
									<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
								</td>
							</tr>
						</table>
					</form>
					
				</div>
				
				<!-- 表单编辑区->问题管理 -->
				<div data-options="region:'center'">
					<!-- 问题列表 -->
					<table id="questionDg">
						<thead>
							<tr style="height:12px;">		
								<th data-options="field:'QUESTION_DESC',width:600,align:'left'">题目</th>
								<th data-options="field:'listen',width:40,align:'center',formatter:listenrowformatter">试听</th>
								<th data-options="field:'download',width:60,align:'center',formatter:downloadrowformatter">下载</th>
								<th data-options="field:'id',width:120,align:'center',formatter:question_rowformatter">操作</th>
							</tr>
						</thead>
					</table>
					
				</div>
				
			</div>
			
		</div>
			
	</div>
	
<!--</div>-->
