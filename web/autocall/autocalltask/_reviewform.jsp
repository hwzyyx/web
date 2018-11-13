<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<div id="reviewLayout" class="easyui-layout" data-options="fit:true"  style="width:100%;padding:1px;">
		
		<!-- 审核提示 -->
		<div data-options="region:'west',split:true" title="审核提示" style="width:310px;">
			<div id="reviewNote"></div>
			
			<form id="reviewForm" method="post">
					<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" width="95%"
									  style="border-collapse: collapse;">
							<tr>
								<td>
									<div style="padding-top:2px;padding-left:5px;">
										<input style="width:270px;height:40px" name="review.reviewAdvice" id="REVIEW_ADVICE" data-options="multiline:true,prompt:'输入审核意见'" class="easyui-textbox" type="text"></input>
									</div>
								</td>
							</tr>
							<tr>
								<td style="padding-top:5px;padding-left:10px;">
									<div id="reviewResult" />
								</td>
							</tr>
							<tr>
								<td style="padding-top:5px;padding-left:5px;">
									&nbsp;&nbsp;<a href="#" style="width:80px;" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveReview()">保存</a>
									&nbsp;<a href="#" id="" style="width:80px;" class="easyui-linkbutton" iconCls="icon-cancel" onclick="reviewCancel()">取消</a>
									&nbsp;<a href="#" id="" style="width:80px;" class="easyui-linkbutton" iconCls="icon-save" onclick="archive()">直接归档</a>
								</td>
							</tr>
					</table>
			</form>
			
			<table border='0' cellspacing='0' cellpadding='0' style='width:100%'>
			   	<tr>
			   		<td style='padding-top:10px;' align='center'>
						<HR style='FILTER:alpha(opacity=100,finishopacity=0,style=3);margin-left:3px;' width='95%' color=#cccccc SIZE=1>
			        	<span style='font-weight:bolder;font-size:14px'>模拟外呼</span>
						<HR style='FILTER:alpha(opacity=100,finishopacity=0,style=3);margin-left:3px;' width='95%' color=#cccccc SIZE=1>
					</td>
				</tr>
				<tr>
					<td style='padding-top:5px;' align='center'>
						<!-- 动态加载播放器 -->
						<div id="jplayerDiv"></div>
					</td>
				</tr>
			</table>
			
		</div>
		
		<!-- 任务信息展示区 -->
		<div data-options="region:'center',split:true" title="外呼任务信息">
			<div id="autoCallTaskTabs" class="easyui-tabs" data-options="fit:true">
	
				<!-- 外呼任务Tab -->
				<div title="外呼任务管理" buttons="#addAutoCallTaskBtn">
					<form id="autoCallTaskForm" method="post">
					<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" width="680"
									  style="border-collapse: collapse;">
							<tr>
								<td style="width:80px;text-align:center;">
									<div style="padding-top:5px;">
										任务名称
									</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<input name="autoCallTask.TASK_ID" id="TASK_ID" class="easyui-validatebox" type="text" style="display:none;"></input>
										<input style="width:250px;" name="autoCallTask.TASK_NAME" id="TASK_NAME" class="easyui-textbox" type="text" required="true" disabled="true" missingMessage="任务名称不能为空!"></input>
									</div>
								</td>
							</tr>
							<tr>
								<td style="text-align:center;vertical-align: top;">
									<div style="padding-top:5px;">主叫号码</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<select class="easyui-combobox" style="width: 150px;" disabled="true" name="autoCallTask.CALLERID" id="CALLERID" data-options="editable:false,panelHeight:'auto'">
								        </select>
							        </div>
								</td>
							</tr>
							<tr>
								<td style="text-align:center;vertical-align: top;">
									<div style="padding-top:5px;">任务期限</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<input name="autoCallTask.PLAN_START_TIME" id="PLAN_START_TIME" style="width:100px;" class="easyui-datebox" required='true' disabled="true" data-options="editable:false"></input> 
										&nbsp;&nbsp;&nbsp;至&nbsp;&nbsp;&nbsp; 
										<input name="autoCallTask.PLAN_END_TIME" id="PLAN_END_TIME" style="width:100px;" class="easyui-datebox" required='true' disabled="true" data-options="editable:false"></input> 
							        </div>
								</td>
							</tr>
							<tr id="schedule_tr" style="padding-top:10px;">
								<td style="text-align:center;vertical-align: top;">
									<div style="padding-top:10px;">调度计划</div>
								</td>
								<td style="padding-top:10px;">
									<input type="hidden" name="autoCallTask.SCHEDULE_ID" id="SCHEDULE_ID_INFO" />
									<input style="width:200px;" name="autocallTask.SCHEDULE_NAME" id="SCHEDULE_NAME" class="easyui-textbox" type="text" disabled="true"></input>
									<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" disabled="true" style="width:100px" onClick="selectSchedule()">选&nbsp;&nbsp;择</a>
									<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="showScheduleDetail()">详&nbsp;&nbsp;情</a>
								</td>
							</tr>
							<tr>
								<td style="text-align:center;vertical-align: top;">
									<div style="padding-top:10px;">任务类型</div>
								</td>
								<td>
									<div style="padding-top:10px;">
										<select class="easyui-combobox" style="width: 120px;" name="autoCallTask.TASK_TYPE" id="TASK_TYPE" disabled="true" data-options="editable:false,panelHeight:'auto'">
								        </select>
							        </div>
								</td>
							</tr>
							<tr id="common_voice_tr" style="padding-top:20px;">
								<td style="text-align:center;vertical-align: top;padding-top:10px;">
									<div style="padding-top:5px;">语音文件</div>
								</td>
								<td style="padding-top:5px;">
									<input type="hidden" name="autoCallTask.COMMON_VOICE_ID" id="COMMON_VOICE_ID"/>
									<input style="width:200px;" name="autoCallTask.COMMON_VOICE_DESC" id="COMMON_VOICE_DESC" class="easyui-textbox" type="text" disabled="true" required="true"></input>
									<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" disabled="true" style="width:100px" onClick="selectVoice()">选&nbsp;&nbsp;择</a>
								</td>
							</tr>
							
							<tr id="questionnaire_tr" style="display:none;">
								<td style="text-align:center;vertical-align: top;padding-top:10px;">
									<div style="padding-top:5px;">调查问卷</div>
								</td>
								<td style="padding-top:5px;">
									<input type="hidden" name="autoCallTask.QUESTIONNAIRE_ID" id="QUESTIONNAIRE_ID"/>
									<input style="width:200px;" name="autoCallTask.QUESTIONNAIRE_DESC" id="QUESTIONNAIRE_DESC" class="easyui-textbox" type="text" disabled="true" required="true"></input>
									<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" disabled="true" onClick="selectQuestionnaire()">选&nbsp;&nbsp;择</a>
								</td>
							</tr>
							<tr id="reminderType_tr" style="display:none;">
								<td style="text-align:center;vertical-align: top;">
									<div style="padding-top:5px;">催缴类型</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<!-- 
										<select class="easyui-combobox" style="width:150px;" name="autoCallTask.REMINDER_TYPE" id="REMINDER_TYPE" disabled="true" data-options="editable:false,panelHeight:'auto'">
								              <option value="1">电话费</option>
								              <option value="2">电费</option>
								              <option value="3">水费</option>
								              <option value="4">燃气费</option>
								              <option value="5">物业费</option>
								              <option value="6">车辆违章</option>
								              <option value="7">社保催缴</option>
								        </select>
										 -->
								        <select class="easyui-combobox" style="width: 150px;" name="autoCallTask.REMINDER_TYPE" id="REMINDER_TYPE" readonly="true" data-options="editable:false,panelHeight:'auto'">
					       	 			</select>
					                </div>
								</td>
							</tr>
							
							<tr>
								<td colspan="2">
									<div style="padding-left:80px;padding-top:10px;">
										<a href="#" onclick="showMore()" style="width:200px;" class="easyui-linkbutton" iconCls='icon-add' plain="true">...更多...</a>
							        </div>
								</td>
							</tr>
							
						</table>
				
						<table id="more" width="680" style="border-collapse: collapse;">
				
							<tr id="start_voice_tr">
								<td style="text-align:center;vertical-align: top;">
									<div style="padding-top:5px;">开始语音</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<input type="hidden" name="autoCallTask.START_VOICE_ID" id="START_VOICE_ID"/>
										<input style="width:200px;" name="autoCallTask.START_VOICE_DESC" id="START_VOICE_DESC" class="easyui-textbox" type="text" disabled="true" required="true"></input>
										<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" disabled="true" onClick="selectStartVoice()">选&nbsp;&nbsp;择</a>
										<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-remove'" style="width:70px" disabled="true" onClick="clearStartVoice()">清&nbsp;&nbsp;除</a>
									</div>
								</td>
							</tr>
							
							<tr id="end_voice_tr">
								<td style="text-align:center;vertical-align: top;">
									<div style="padding-top:5px;">结束语音</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<input type="hidden" name="autoCallTask.END_VOICE_ID" id="END_VOICE_ID"/>
										<input style="width:200px;" name="autoCallTask.END_VOICE_DESC" id="END_VOICE_DESC" class="easyui-textbox" type="text" disabled="true" required="true"></input>
										<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" disabled="true" style="width:100px" onClick="selectEndVoice()">选&nbsp;&nbsp;择</a>
										<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-remove'" disabled="true" style="width:70px" onClick="clearEndVoice()">清&nbsp;&nbsp;除</a>
									</div>
								</td>
							</tr>
							
							<tr id="black_list_tr">
								<td style="text-align:center;vertical-align: top;">
									<div style="padding-top:5px;">黑 名 单</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<input type="hidden" name="autoCallTask.BLACKLIST_ID" id="BLACKLIST_ID" />
										<input style="width:200px;" name="autoCallTask.BLACKLIST_NAME" id="BLACKLIST_NAME" class="easyui-textbox" type="text" disabled="true" required="true"></input>
										<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" disabled="true" onClick="selectBlackList()">选&nbsp;&nbsp;择</a>
										<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-remove'" style="width:70px" disabled="true" onClick="clearBlackList()">清&nbsp;&nbsp;除</a>
									</div>
								</td>
							</tr>
							
							<tr>
								<td style="width:80px;text-align:center;">
									<div style="padding-top:5px;">
										失败重试
									</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<select class="easyui-combobox" style="width:50px;" name="autoCallTask.RETRY_TIMES" id="RETRY_TIMES" disabled="true" data-options="editable:false,panelHeight:'auto'">
								              <option value="1">1</option>
								              <option value="2">2</option>
								              <option value="3">3</option>
								              <option value="4">4</option>
								              <option value="5">5</option>
								        </select> 次
					                </div>
								</td>
							</tr>
							<tr>
								<td style="width:80px;text-align:center;">
									<div style="padding-top:5px;">
										重试间隔
									</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<input id="RETRY_INTERVAL" name="autoCallTask.RETRY_INTERVAL" style="width:50px;" class="easyui-numberbox" disabled="true" type="text" required="true" missingMessage="重试间隔不能为空!"></input> 分钟
									</div>
								</td>
							</tr>
							<tr style="">
								<td style="width:80px;text-align:center;">
									<div style="padding-top:5px;">
										优 先 级
									</div>
								</td>
								<td>
									<div style="padding-top:5px;">
										<select class="easyui-combobox" style="width:50px;" name="autoCallTask.PRIORITY" id="PRIORITY" disabled="true" data-options="editable:false,panelHeight:'auto'">
								              <option value="1">低</option>
								              <option value="2">中</option>
								              <option value="3">高</option>
								        </select> 
					                </div>
								</td>
							</tr>
					</table>
					</form>
				</div>
				
				<div id="autoCallTaskTelephoneDgTab" title="号码列表">
				
					<div data-options="fit:true" class="easyui-layout">
						<!-- 顶部查询区 -->
						<div data-options="region:'north',split:true,border:true" style="height:45px;padding-top:5px;padding-left:5px;">
							<table>
								<tr style="vertical-align: top;">
									<td>电话号码：<input id="customerTel" type="text" class="easyui-textbox" style="width:120px;"/>
									
										<span style="padding-left:30px;">
											客户姓名：<input id="customerName" type="text" class="easyui-textbox" style="width:120px;"/>
										</span>
										<span style="padding-left:30px;">
											<a href="javascript:findDataForTelephone()" style="width:80px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
										</span>
									</td>
								</tr>
							</table>
						</div>
					
						<!-- 数据显示区 -->
						<div data-options="region:'center',split:true,border:false">
							
							<table id="autoCallTaskTelephoneDg">
								<thead>
									<tr style="height:12px;">
										<th data-options="field:'ck',checkbox:true"></th>		
										<th data-options="field:'CUSTOMER_TEL',width:120,align:'center'">电话号码</th>
										<th data-options="field:'CUSTOMER_NAME',width:100,align:'center'">客户姓名</th>
										
										<th data-options="field:'ILLEGAL_CITY',width:100,align:'center'">违章城市</th>
										<th data-options="field:'PUNISHMENT_UNIT',width:150,align:'center'">处罚单位</th>
										<th data-options="field:'ILLEGAL_REASON',width:150,align:'center'">违章事由</th>
										<th data-options="field:'PERIOD',width:120,align:'center'">日期</th>
										<th data-options="field:'CHARGE',width:100,align:'center'">费用</th>
										<th data-options="field:'COMPANY',width:150,align:'center'">代缴单位</th>
										<th data-options="field:'state',width:150,align:'center',formatter:telephonestateformatter">状态</th>
									</tr>
								</thead>
							</table>	
						</div>
					</div>
				
				</div>
				
			</div>
		</div>
</div>		
		
 

  