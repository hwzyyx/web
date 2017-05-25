<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" width="680"
					  style="border-collapse: collapse;">
			<tr>
				<td style="width:80px;text-align:center;">调度计划</td>
				<td>
					<input name="schedule.SCHEDULE_ID" id="SCHEDULE_ID" type="hidden"></input>
					<input name="schedule.SCHEDULE_NAME" style="width:300px;" id="SCHEDULE_NAME" class="easyui-textbox" type="text" required="true" missingMessage="调度计划名称不能为空!"></input>
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">日期类型</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<select class="easyui-combobox" style="width: 120px;" name="schedule.DATETYPE" id="DATETYPE" data-options="panelHeight:'auto'">
				              <option value="1">每天</option>
				              <option value="2">星期</option>
				        </select>
			        </div>
			        <div id="weekinfo" style="display:none;padding-top:10px;padding-bottom:10px;">
			        	<input id="w1" type="checkbox" value="1" name="week" checked="checked"><label for="w1">星期一</label></input>&nbsp;&nbsp;&nbsp;
			        	<input id="w2" type="checkbox" value="2" name="week" checked="checked"><label for="w2">星期二</label></input>&nbsp;&nbsp;&nbsp;
			        	<input id="w3" type="checkbox" value="3" name="week" checked="checked"><label for="w3">星期三</label></input>&nbsp;&nbsp;&nbsp;
			        	<input id="w4" type="checkbox" value="4" name="week" checked="checked"><label for="w4">星期四</label></input>&nbsp;&nbsp;&nbsp;
			        	<input id="w5" type="checkbox" value="5" name="week" checked="checked"><label for="w5">星期五</label></input>&nbsp;&nbsp;&nbsp;
			        	<input id="w6" type="checkbox" value="6" name="week" checked="checked"><label for="w6">星期六</label></input>&nbsp;&nbsp;&nbsp;
			        	<input id="w7" type="checkbox" value="7" name="week" checked="checked"><label for="w7">星期日</label></input>&nbsp;&nbsp;&nbsp;
			        </div>
				</td>
			</tr>
			
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">时间区间</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<a href="#" id="easyui-add" onclick="addTimeItem()" class="easyui-linkbutton" iconCls='icon-add' plain="true">增加时间区间</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<a href="#" id="easyui-add" onclick="delTimeItem()" class="easyui-linkbutton" iconCls='icon-cancel' plain="true">删除时间区间</a>
					</div>
					<div id="timeAddMain" style="padding-top:15px;">
						<div id="timeItemContainer">
						</div>
					</div>
				</td>
			</tr>
			
		</table>
		
		<div id="addScheduleBtn">
			<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
			<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="cancel()">取消</a>
		</div>  