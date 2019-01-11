<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 
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
							<input style="width:250px;" name="autoCallTask.TASK_NAME" id="TASK_NAME" class="easyui-textbox" type="text" required="true" missingMessage="任务名称不能为空!"></input>
						</div>
					</td>
				</tr>
				<tr>
					<td style="text-align:center;vertical-align: top;">
						<div style="padding-top:5px;">主叫号码</div>
					</td>
					<td>
						<div style="padding-top:5px;">
							<!-- 
							<select class="easyui-combobox" style="width: 400px;" name="autoCallTask.CALLERID" id="CALLERID" data-options="multiple:true,panelHeight:'auto'" required="true">
					        </select>
					        <input type="checkbox" id="selectAllCallerIdCheckBox" value="1"><label for="selectAllCallerIdCheckBox">全选/取消全选</label>
							 -->
							<span id="callerIdSpan" style="display: none;">
								<input type="hidden" name="autoCallTask.CALLERID" id="CALLERID"/>
								<input style="width:300px;" name="autoCallTask.CALLERID_NUMBER" id="CALLERID_NUMBER" class="easyui-textbox" type="text" disabled="true" required="true"></input>
							</span>
							<span id="callerIdGroupSpan" style="display: none;">
								<input type="hidden" name="autoCallTask.CALLERID_GROUP_ID" id="CALLERID_GROUP_ID"/>
								<input style="width:300px;" name="autoCallTask.CALLERID_GROUP_NAME" id="CALLERID_GROUP_NAME" class="easyui-textbox" type="text" disabled="true" required="true"></input>
							</span>
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:120px" onClick="selectCallerId()">选择[分配]</a>
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:120px" onClick="selectCallerIdGroup()">选择[号码组]</a> 
				        </div>
					</td>
				</tr>
				<tr>
					<td style="text-align:center;vertical-align: top;">
						<div style="padding-top:5px;">任务期限</div>
					</td>
					<td>
						<div style="padding-top:5px;">
							<input name="autoCallTask.PLAN_START_TIME" id="PLAN_START_TIME" style="width:200px;" class="easyui-datetimebox" required='true' data-options="editable:false"></input> 
							&nbsp;&nbsp;&nbsp;至&nbsp;&nbsp;&nbsp; 
							<input name="autoCallTask.PLAN_END_TIME" id="PLAN_END_TIME" style="width:200px;" class="easyui-datetimebox" required='true' data-options="editable:false"></input> 
				        </div>
					</td>
				</tr>
				<tr id="schedule_tr" style="padding-top:10px;">
					<td style="text-align:center;vertical-align: top;">
						<div style="padding-top:10px;">调度计划</div>
					</td>
					<td style="padding-top:10px;">
						<input type="hidden" name="autoCallTask.SCHEDULE_ID" id="SCHEDULE_ID_INFO" />
						<input style="width:200px;" name="autoCallTask.SCHEDULE_NAME" id="SCHEDULE_NAME" class="easyui-textbox" type="text" disabled="true"></input>
						<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectSchedule()">选&nbsp;&nbsp;择</a>
					</td>
				</tr>
				<tr>
					<td style="text-align:center;vertical-align: top;">
						<div style="padding-top:10px;">任务类型</div>
					</td>
					<td>
						<div style="padding-top:10px;">
							<select class="easyui-combobox" style="width: 120px;" name="autoCallTask.TASK_TYPE" id="TASK_TYPE" data-options="editable:false,panelHeight:'auto'">
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
						<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectVoice()">选&nbsp;&nbsp;择</a>
						&nbsp;&nbsp;
						<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="createCommonVoice()">创建语音</a>
					</td>
				</tr>
				
				<tr id="questionnaire_tr" style="display:none;">
					<td style="text-align:center;vertical-align: top;padding-top:10px;">
						<div style="padding-top:5px;">调查问卷</div>
					</td>
					<td style="padding-top:5px;">
						<input type="hidden" name="autoCallTask.QUESTIONNAIRE_ID" id="QUESTIONNAIRE_ID"/>
						<input style="width:200px;" name="autoCallTask.QUESTIONNAIRE_DESC" id="QUESTIONNAIRE_DESC" class="easyui-textbox" type="text" disabled="true" required="true"></input>
						<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectQuestionnaire()">选&nbsp;&nbsp;择</a>
					</td>
				</tr>
				<tr id="reminderType_tr" style="display:none;">
					<td style="text-align:center;vertical-align: top;">
						<div style="padding-top:5px;">催缴类型</div>
					</td>
					<td>
						<div style="padding-top:5px;">
						
							<select class="easyui-combobox" style="width: 150px;" name="autoCallTask.REMINDER_TYPE" id="REMINDER_TYPE" data-options="editable:false,panelHeight:'auto'">
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
	
			<table id="more" width="1000" style="display:none;border-collapse: collapse;">
	
				<tr id="start_voice_tr">
					<td style="text-align:center;vertical-align: top;">
						<div style="padding-top:5px;">开始语音</div>
					</td>
					<td>
						<div style="padding-top:5px;">
							<input type="hidden" name="autoCallTask.START_VOICE_ID" id="START_VOICE_ID"/>
							<input style="width:200px;" name="autoCallTask.START_VOICE_DESC" id="START_VOICE_DESC" class="easyui-textbox" type="text" disabled="true" required="true"></input>
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectStartVoice()">选&nbsp;&nbsp;择</a>
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="createStartVoice()">创建语音</a>
							&nbsp;&nbsp;
							
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-remove'" style="width:70px" onClick="clearStartVoice()">清&nbsp;&nbsp;除</a>
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
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectEndVoice()">选&nbsp;&nbsp;择</a>
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="createEndVoice()">创建语音</a>
							&nbsp;&nbsp;
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-remove'" style="width:70px" onClick="clearEndVoice()">清&nbsp;&nbsp;除</a>
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
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectBlackList()">选&nbsp;&nbsp;择</a>
							<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-remove'" style="width:70px" onClick="clearBlackList()">清&nbsp;&nbsp;除</a>
						</div>
					</td>
				</tr>
				
				<tr>
					<td style="width:80px;text-align:center;">
						<div style="padding-top:10px;">
							下发短信
						</div>
					</td>
					<td>
						<div style="padding-top:10px;">
							<input type="checkbox" id="isSendMessageCheckBox" value="1"><label for="isSendMessageCheckBox">下发短信</label>
							<span style="color:red;margin-left: 20px;">*注：在执行外呼时，同时下发短信</span>
						</div>
					</td>
				</tr>
				<tr id="messageContentTr" style="display:none;">
					<td style="width:80px;text-align:center;">
						<div style="padding-top:5px;">
							短信内容
						</div>
					</td>
					<td>
						<div style="padding-top:5px;">
							<input class="easyui-textbox" name="autoCallTask.MESSAGE_CONTENT" id="MESSAGE_CONTENT" data-options="multiline:true"  style="width:300px;height:100px"></input>
		                </div>
					</td>
				</tr>
				
				<tr>
					<td style="width:80px;text-align:center;">
						<div style="padding-top:25px;">
							呼叫总数
						</div>
					</td>
					<td>
						<div style="padding-top:25px;">
							<select class="easyui-combobox" style="width:50px;" name="autoCallTask.RETRY_TIMES" id="RETRY_TIMES" data-options="editable:false,panelHeight:'auto'">
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
							<input id="RETRY_INTERVAL" name="autoCallTask.RETRY_INTERVAL" style="width:50px;" class="easyui-numberbox" type="text" required="true" missingMessage="重试间隔不能为空!"></input> 
							<select class="easyui-combobox" style="width:60px;margin-left:10px;" name="autoCallTask.INTERVAL_TYPE" id="INTERVAL_TYPE" data-options="editable:false,panelHeight:'auto'">
					              <option value="1">分钟</option>
					              <option value="2">小时</option>
					              <option value="3">天</option>
					        </select>
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
							<select class="easyui-combobox" style="width:50px;" name="autoCallTask.PRIORITY" id="PRIORITY" data-options="editable:false,panelHeight:'auto'">
					              <option value="1">低</option>
					              <option value="2">中</option>
					              <option value="3">高</option>
					        </select> 
		                </div>
					</td>
				</tr>
		</table>
		<div style="padding-top:10px;padding-left:80px;">
			<a href="#" id="autoCallTaskSaveBtn" style="width:150px;" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
			<a href="#" id="" style="width:150px;margin-left:10px;" class="easyui-linkbutton" iconCls="icon-cancel" onclick="cancel()">取消</a>
		</div>
		</form>
	</div>
	
	<div id="importTelephoneTab" title="导入号码">
		
		<form id="uploadTelephoneForm" method="post" enctype="multipart/form-data">
			
			<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff"
					  style="border-collapse: collapse;" width="100%">
				<tr>
					<td style="text-align:right;width:100px;" valign="top">
						<div style="padding-top:10px;"><span style="font-weight: bold;">号码文件：</span></div>
					</td>
					<td style="width:400px;height:50px;" valign="top">
						<div style="padding-top:10px;">
							 <input class="easyui-filebox" id="telephoneFile" name="telephoneFile" data-options="prompt:'选择号码文件进行上传'" style="width:380px;" required="true" missingMessage="号码文件不能为空!">
							 <a href="#" onclick="uploadPhoneFile()" class="easyui-linkbutton" iconCls="icon-ok" style="width:100px;;">上传号码</a>
				        </div>
					</td>
					<td rowspan="2" style="width:300px;" align="right">
						<table id="formTable" border="1" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" width="250"
					  style="border-collapse: collapse;margin-top:5px;margin-right:2px;">
					  		
					  		<tr>
					  			<td align="center" colspan="2">
					  				<span style="font-weight: bold;font-size: 14px;">模板下载：</span>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td colspan="2">
					  				<span style="font-weight: bold;">&nbsp;普通、调查外呼类</span>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right" style="width:160px;">
					  				标准模版：
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=standard">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=standard">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td colspan="2">
					  				<span style="font-weight: bold;">&nbsp;费用催缴类</span>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				电费
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=reminderType1">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=reminderType1">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				水费
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=reminderType2">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=reminderType2">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				电话费
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=reminderType3">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=reminderType3">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				燃气费
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=reminderType4">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=reminderType4">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				物业费
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=reminderType5">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=reminderType5">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				车辆违章
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=reminderType6">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=reminderType6">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				交警移车
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=reminderType7">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=reminderType7">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				社保催缴
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=reminderType8">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=reminderType8">EXCEL</a>
					  			</td>
					  		</tr>
					  	</table>
					</td>
				</tr>

				<tr>
					<td colspan="3" valign="top">
						<div style="display:none;" id="selectAutoNumberDiv">
							<div style="margin-left:70px;margin-top:30px;">
								<span style="font-weight: bold;">号&nbsp;码&nbsp;&nbsp;组：</span>
								<input type="hidden" name="NUMBER_ID" id="NUMBER_ID"/>
								<input style="width:200px;" name="NUMBER_NAME" id="NUMBER_NAME" class="easyui-textbox" type="text" disabled="true" required="true"></input>
								<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectAutoNumber()">选&nbsp;&nbsp;择</a>
								<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-remove'" style="width:70px" onClick="clearAutoNumber()">清&nbsp;&nbsp;除</a>
								
								<a href="#" onclick="uploadPhoneByNumber()" class="easyui-linkbutton" iconCls="icon-ok" style="width:100px;">上传号码</a> &nbsp;&nbsp;<span style="color:red;">*仅普通外呼和调查问卷外呼可选</span>
								
							</div>
						</div>
					</td>
				</tr>
				
			</table>
			
		</form>
		
	</div>
	
	<div id="autoCallTaskTelephoneDgTab" title="号码列表">
	
		<div data-options="fit:true" class="easyui-layout">
			<!-- 顶部查询区 -->
			<div data-options="region:'north',split:true,border:true" style="height:70px;padding-top:5px;padding-left:5px;">
				<table>
					<tr style="vertical-align: top;">
						<td>
							客户姓名：<input id="customerName" type="text" class="easyui-textbox" style="width:100px;"/>
							<span style="padding-left:20px;">
								电话号码：<input id="customerTel" type="text" class="easyui-textbox" style="width:100px;"/>
							</span>
							<span style="padding-left:20px;">
								外呼结果：<select id="state" class="easyui-combobox" name="state" style="width:100px;">
										</select>
							</span>
							<span style="padding-left:20px;">
								失败原因：<select id="hangupCause" class="easyui-combobox" name="hangupCause" style="width:100px;">
										</select>
							</span>
							<span style="padding-left:20px;">
								短信状态：<select class="easyui-combobox" id="messageState" name="messageState" style="width:130px;" data-options="panelHeight:'auto'"></select>
							</span>
						</td>
					</tr>
					<tr style="vertial-align:top;">
						<td>
							时间类型：
							<a href="#" id="dateTimeTypeBtn0" class="easyui-linkbutton" data-options="toggle:true,group:'g2',selected:true" style="width:70px;background-color: #00ff00;">创建时间</a>
							<a href="#" id="dateTimeTypeBtn1" class="easyui-linkbutton" data-options="toggle:true,group:'g2'" style="width:70px;margin-right:20px;background-color: #00ff00;">外呼时间</a>
							<input id="startTimeForTelephone" name="startTimeForTelephone" class="easyui-datetimebox" style="width:150px;"/><span style="padding-left:10px;padding-right:10px;">至</span> <input id="endTimeForTelephone" name="endTimeForTelephone" class="easyui-datetimebox" style="width:150px;"/>
							<span style="padding-left:20px;">
								时间间隔：
								<select id="dateInterval" style="width:70px;">
									<option value="1">1天</option>
									<option value="2">2天</option>
									<option value="3">3天</option>
									<option value="4">4天</option>
									<option value="5">5天</option>
									<option value="6">6天</option>
									<option value="7">7天</option>
									<option value="8">8天</option>
									<option value="9">9天</option>
									<option value="10">10天</option>
									<option value="30">一个月</option>
									<option value="90">三个月</option>
									<option value="180">半年月</option>
								</select>
							</span>
							<span style="padding-left:20px;"><a href="javascript:findDataForTelephone()" class="easyui-linkbutton" style="width:60px;" data-options="iconCls:'icon-search'">查询</a></span>
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
							<th data-options="field:'CUSTOMER_NAME',width:120,align:'center'">客户姓名</th>
							<th data-options="field:'CUSTOMER_TEL',width:120,align:'center'">电话号码</th>
							<th data-options="field:'PROVINCE',width:120,align:'center'">省份</th>
							<th data-options="field:'CITY',width:120,align:'center'">城市</th>
							<th data-options="field:'CALLOUT_TEL',width:120,align:'center'">外呼号码</th>
							<th data-options="field:'CALLERID',width:120,align:'center'">主叫号码</th>
							<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
							<th data-options="field:'STATE_DESC',width:100,align:'center'">外呼结果</th>
							<th data-options="field:'HANGUP_CAUSE_DESC',width:200,align:'center'">失败原因</th>
							<th data-options="field:'RETRIED_DESC',width:150,align:'center'">呼叫次数</th>
							<th data-options="field:'LOAD_TIME',width:200,align:'center'">外呼时间</th>
							<th data-options="field:'BILLSEC',width:150,align:'center'">通话时长</th>
							<th data-options="field:'NEXT_CALLOUT_TIME',width:200,align:'center'">下次外呼时间</th>
							<th data-options="field:'MESSAGE_STATE_DESC',width:100,align:'center'">短信状态</th>
							<th data-options="field:'MESSAGE_FAILURE_CODE',width:100,align:'center'">短信失败代码</th>
														
							<th data-options="field:'PERIOD',width:120,align:'center'">日期</th>
							<th data-options="field:'DISPLAY_NUMBER',width:100,align:'center'">表显数量</th>
							<th data-options="field:'DOSAGE',width:100,align:'center'">使用量</th>							
							<th data-options="field:'CHARGE',width:100,align:'center'">费用</th>
							<th data-options="field:'ACCOUNT_NUMBER',width:120,align:'center'">户号</th>
							<th data-options="field:'ADDRESS',width:120,align:'center'">地址</th>
							<th data-options="field:'CALL_POLICE_TEL',width:100,align:'center'">报警人电话</th>
							<th data-options="field:'VEHICLE_TYPE',width:120,align:'center'">车辆类型</th>
							<th data-options="field:'PLATE_NUMBER',width:120,align:'center'">车牌号码</th>
							<th data-options="field:'ILLEGAL_CITY',width:100,align:'center'">违章城市</th>
							<th data-options="field:'PUNISHMENT_UNIT',width:150,align:'center'">处罚单位</th>
							<th data-options="field:'ILLEGAL_REASON',width:150,align:'center'">违章事由</th>
							<th data-options="field:'COMPANY',width:150,align:'center'">代缴单位</th>
							<th data-options="field:'id',width:100,align:'center',formatter:telephonerowformatter">操作</th>
						</tr>
					</thead>
				</table>	
			</div>
		</div>
	
	</div>
	
</div>
<form id="exportForm"></form>
<div id="telephoneopertool" style="padding:5px;">
	<a href="#" id="easyui-add" onclick="autoCallTaskTelephoneAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增号码</a>
	<span style="padding:10px;">
		<a href="#" id="easyui-delete" onclick="autoCallTaskTelephoneDel()" class="easyui-linkbutton" iconCls='icon-remove' plain="true">删除选中记录</a>
	</span>
	<div style="display:inline;position:absolute;right:10px;">
		<a href="#" id="easyui-add" onclick="autoCallTaskTelephoneExport()" class="easyui-linkbutton" iconCls='icon-redo' plain="true">导出号码</a>
	</div>
</div>
  