<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 编辑/添加　窗口 -->
<div data-options="fit:true" class="easyui-layout">
		<form id="autoCallTaskTelephoneForm" method="post">
		
			<div style="padding-left:10px;padding-top:10px;">
				客户号码：
				<input type="hidden" name="autoCallTaskTelephone.TEL_ID" id="TEL_ID"/>
				<input style="width:200px;" id="TELEPHONE" name="autoCallTaskTelephone.TELEPHONE" class="easyui-numberbox" type="text" required="true" missingMessage="电话号码不能为空!"></input>
			</div>
			
			<div style="padding-left:10px;padding-top:5px;">
				客户姓名：
				<input style="width:200px;" id="CLIENT_NAME" name="autoCallTaskTelephone.CLIENT_NAME" class="easyui-textbox" type="text" required="true" missingMessage="客户姓名不能为空!"></input>
			</div>
			
			<div id="periodDiv" style="padding-left:10px;padding-top:5px;">
				日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;期：
				<input style="width:200px;" data-options="prompt:'20170101'" id="PERIOD" name="autoCallTaskTelephone.PERIOD" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="violationCityDiv" style="padding-left:10px;padding-top:5px;">
				违章城市：
				<input style="width:200px;" data-options="prompt:'北京市'" id="VIOLATION_CITY" name="autoCallTaskTelephone.VIOLATION_CITY" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="punishmentUnitDiv" style="padding-left:10px;padding-top:5px;">
				处罚单位：
				<input style="width:200px;" data-options="prompt:'北京市朝阳交警大队'" id="PUNISHMENT_UNIT" name="autoCallTaskTelephone.PUNISHMENT_UNIT" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="violationReasonDiv" style="padding-left:10px;padding-top:5px;">
				违章事由：
				<input style="width:200px;" data-options="prompt:'违章停车'" id="VIOLATION_REASON" name="autoCallTaskTelephone.VIOLATION_REASON" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="chargeDiv" style="padding-left:10px;padding-top:5px;">
				费&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用：
				<input style="width:200px;" data-options="prompt:'38.60'" id="CHARGE" name="autoCallTaskTelephone.CHARGE" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="companyDiv" style="padding-left:10px;padding-top:5px;">
				代缴单位：
				<input style="width:200px;" data-options="prompt:'XX有限公司'" id="COMPANY" name="autoCallTaskTelephone.COMPANY" class="easyui-textbox" type="text"></input>
			</div>
			
			<div style="padding-left:80px;padding-top:10px;">
				<a href="javascript:#" style="width:150px;" id="autoCallTaskTelephoneSaveBtn"  class="easyui-linkbutton" data-options="iconCls:'icon-save'" onclick="autoCallTaskTelephoneSaveAdd()">保存</a>
			</div>			
		</form>
</div>
