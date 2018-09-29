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
							<select class="easyui-combobox" style="width: 150px;" name="autoCallTask.CALLERID" id="CALLERID" data-options="panelHeight:'auto'">
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
							<input name="autoCallTask.PLAN_START_TIME" id="PLAN_START_TIME" style="width:100px;" class="easyui-datebox" required='true' data-options="editable:false"></input> 
							&nbsp;&nbsp;&nbsp;至&nbsp;&nbsp;&nbsp; 
							<input name="autoCallTask.PLAN_END_TIME" id="PLAN_END_TIME" style="width:100px;" class="easyui-datebox" required='true' data-options="editable:false"></input> 
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
							<select class="easyui-combobox" style="width:150px;" name="autoCallTask.REMINDER_TYPE" id="REMINDER_TYPE" data-options="editable:false,panelHeight:'auto'">
					              <option value="1">电话费</option>
					              <option value="2">电费</option>
					              <option value="3">水费</option>
					              <option value="4">燃气费</option>
					              <option value="5">物业费</option>
					              <option value="6">车辆违章</option>
					              <option value="7">社保催缴</option>
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
	
			<table id="more" width="680" style="display:none;border-collapse: collapse;">
	
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
						<div style="padding-top:5px;">
							失败重试
						</div>
					</td>
					<td>
						<div style="padding-top:5px;">
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
							<input id="RETRY_INTERVAL" name="autoCallTask.RETRY_INTERVAL" style="width:50px;" class="easyui-numberbox" type="text" required="true" missingMessage="重试间隔不能为空!"></input> 分钟
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
					<td style="text-align:right;width:100px;" valign="middle">
						<div style="padding-top:10px;"><span style="font-weight: bold;">号码文件：</span></div>
					</td>
					<td style="width:400px;height:50px;" valign="middle">
						<div style="padding-top:10px;">
							 <input class="easyui-filebox" id="telephoneFile" name="telephoneFile" data-options="prompt:'选择号码文件进行上传'" style="width:380px;" required="true" missingMessage="号码文件不能为空!">
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
					  				电话费、水电气费：
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=telephone">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=telephone">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				物业费：
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=property">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=property">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				车辆违章：
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=violation">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=violation">EXCEL</a>
					  			</td>
					  		</tr>
					  		<tr>
					  			<td align="right">
					  				社保催缴：
					  			</td>
					  			<td align="center">
					  				<a href="autoCallTask/template?type=txt&identify=social">TXT</a>&nbsp;|&nbsp;<a href="autoCallTask/template?type=excel&identify=social">EXCEL</a>
					  			</td>
					  		</tr>
					  		
					  	</table>
					</td>
				</tr>
				<tr>
					<td colspan="3" style="padding-left:120px;" valign="top">
						<a href="#" onclick="uploadPhoneFile()" class="easyui-linkbutton" iconCls="icon-ok" style="width:200px;;margin-top:10px;">上传号码</a>
					</td>
				</tr>
				
				<tr>
					<td colspan="3" valign="top">
						<div style="padding-left:60px;display:none;" id="selectAutoNumberDiv">
							<div style="padding-bottom:10px;">
								<span style="font-weight:bold;padding-left:60px;">或通过选择号码组的方式上传号码</span>
							</div>
							<div>
								号&nbsp;码&nbsp;组：
								<input type="hidden" name="NUMBER_ID" id="NUMBER_ID"/>
								<input style="width:200px;" name="NUMBER_NAME" id="NUMBER_NAME" class="easyui-textbox" type="text" disabled="true" required="true"></input>
								<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-search'" style="width:100px" onClick="selectAutoNumber()">选&nbsp;&nbsp;择</a>
								<a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-remove'" style="width:70px" onClick="clearAutoNumber()">清&nbsp;&nbsp;除</a>
								
							</div>
							<div style="padding-left:60px;padding-top:10px;">
								<a href="#" onclick="uploadPhoneByNumber()" class="easyui-linkbutton" iconCls="icon-ok" style="width:200px;;margin-top:10px;">上传号码</a>
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
			<div data-options="region:'north',split:true,border:true" style="height:45px;padding-top:5px;padding-left:5px;">
				<table>
					<tr style="vertical-align: top;">
						<td>电话号码：<input id="telephone" type="text" class="easyui-textbox" style="width:150px;"/>
						
							<span style="padding-left:30px;">
								客户姓名：<input id="clientName" type="text" class="easyui-textbox" style="width:150px;"/>
							</span>
							<span style="padding-left:30px;">
								外呼状态：<select id="state" class="easyui-combobox" name="state" style="width:80px;">
												<option value="5">请选择</option>
												<option value="0">未处理</option>
												<option value="1">已载入</option>
												<option value="2">已成功</option>
												<option value="3">待重呼</option>
												<option value="4">已失败</option>
										</select>
							</span>
							<span style="padding-left:30px;">
								<a href="javascript:findDataForTelephone()" style="width:100px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
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
							<th data-options="field:'CUSTOMER_NAME',width:120,align:'center'">客户姓名</th>
							<th data-options="field:'CALLOUT_TEL',width:120,align:'center'">外呼号码</th>
							<th data-options="field:'PROVINCE',width:120,align:'center'">省份</th>
							<th data-options="field:'CITY',width:120,align:'center'">城市</th>
							
							<th data-options="field:'VIOLATION_CITY',width:100,align:'center'">违章城市</th>
							<th data-options="field:'PUNISHMENT_UNIT',width:150,align:'center'">处罚单位</th>
							<th data-options="field:'VIOLATION_REASON',width:150,align:'center'">违章事由</th>
							<th data-options="field:'PERIOD',width:120,align:'center'">日期</th>
							<th data-options="field:'CHARGE',width:100,align:'center'">费用</th>
							<th data-options="field:'COMPANY',width:150,align:'center'">代缴单位</th>
							<th data-options="field:'state',width:100,align:'center',formatter:telephonestateformatter">状态</th>
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
  