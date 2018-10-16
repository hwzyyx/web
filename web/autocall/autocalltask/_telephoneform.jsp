<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 编辑/添加　窗口 -->
<div data-options="fit:true" class="easyui-layout">
		<form id="autoCallTaskTelephoneForm" method="post">
		
			<div style="padding-left:10px;padding-top:10px;">
				客户号码：&nbsp;&nbsp;&nbsp;
				<input type="hidden" name="autoCallTaskTelephone.TEL_ID" id="TEL_ID"/>
				<input style="width:200px;" data-options="prompt:'13512771995'" id="CUSTOMER_TEL" name="autoCallTaskTelephone.CUSTOMER_TEL" class="easyui-numberbox" type="text" required="true" missingMessage="电话号码不能为空!"></input>
			</div>
			
			<div style="padding-left:10px;padding-top:5px;">
				客户姓名：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'张三'" id="CUSTOMER_NAME" name="autoCallTaskTelephone.CUSTOMER_NAME" class="easyui-textbox" type="text" required="true" missingMessage="客户姓名不能为空!"></input>
			</div>
			
			<div id="periodDiv" style="padding-left:10px;padding-top:5px;">
				日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;期：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'201801'"; id="PERIOD" name="autoCallTaskTelephone.PERIOD" class="easyui-textbox" type="text"></input>
			</div>
			<div id="displayNumberDiv" style="padding-left:10px;padding-top:5px;">
				表显数量：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" id="DISPLAY_NUMBER" name="autoCallTaskTelephone.DISPLAY_NUMBER" class="easyui-numberbox" data-options="min:0,max:50000,prompt:'0'">
			</div>
			<div id="dosageDiv" style="padding-left:10px;padding-top:5px;">
				使&nbsp;用&nbsp;&nbsp;量：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" id="DOSAGE" name="autoCallTaskTelephone.DOSAGE" class="easyui-numberbox" data-options="min:0,max:50000,prompt:'0'">
			</div>
			<div id="chargeDiv" style="padding-left:10px;padding-top:5px;">
				费&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" id="CHARGE" name="autoCallTaskTelephone.CHARGE" class="easyui-numberbox" data-options="min:0,max:50000,precision:2,prompt:'0.00'">
			</div>
			<div id="accountNumberDiv" style="padding-left:10px;padding-top:5px;">
				户&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'1001692206'" id="ACCOUNT_NUMBER" name="autoCallTaskTelephone.ACCOUNT_NUMBER" class="easyui-numberbox" data-options="min:0,prompt:'0'">
			</div>
			<div id="addressDiv" style="padding-left:10px;padding-top:5px;">
				地&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;址：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'南京市玄武区21号幸福小区'" id="ADDRESS" name="autoCallTaskTelephone.ADDRESS" class="easyui-textbox" type="text"></input>
			</div>
			<div id="callPoliceTelDiv" style="padding-left:10px;padding-top:5px;">
				报警人电话：
				<input style="width:200px;" data-options="prompt:'13512771995'" id="CALL_POLICE_TEL" name="autoCallTaskTelephone.CALL_POLICE_TEL" class="easyui-numberbox" type="text"></input>
			</div>
			<div id="vehicleTypeDiv" style="padding-left:10px;padding-top:5px;">
				车辆类型：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'小型车辆'" id="VEHICLE_TYPE" name="autoCallTaskTelephone.VEHICLE_TYPE" class="easyui-textbox" type="text"></input>
			</div>
			<div id="plateNumberDiv" style="padding-left:10px;padding-top:5px;">
				车牌号码：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'苏DR1179'" id="PLATE_NUMBER" name="autoCallTaskTelephone.PLATE_NUMBER" class="easyui-textbox" type="text"></input>
			</div>
			<div id="illegalCityDiv" style="padding-left:10px;padding-top:5px;">
				违章城市：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'北京市'" id="ILLEGAL_CITY" name="autoCallTaskTelephone.ILLEGAL_CITY" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="punishmentUnitDiv" style="padding-left:10px;padding-top:5px;">
				处罚单位：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'北京市朝阳交警大队'" id="PUNISHMENT_UNIT" name="autoCallTaskTelephone.PUNISHMENT_UNIT" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="illegalReasonDiv" style="padding-left:10px;padding-top:5px;">
				违章事由：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'违章停车'" id="ILLEGAL_REASON" name="autoCallTaskTelephone.ILLEGAL_REASON" class="easyui-textbox" type="text"></input>
			</div>
			
			
			<div id="companyDiv" style="padding-left:10px;padding-top:5px;">
				代缴单位：&nbsp;&nbsp;&nbsp;
				<input style="width:200px;" data-options="prompt:'XX有限公司'" id="COMPANY" name="autoCallTaskTelephone.COMPANY" class="easyui-textbox" type="text"></input>
			</div>
			
			<div style="padding-left:80px;padding-top:10px;">
				<a href="javascript:#" style="width:150px;" id="autoCallTaskTelephoneSaveBtn"  class="easyui-linkbutton" data-options="iconCls:'icon-save'" onclick="autoCallTaskTelephoneSaveAdd()">保存</a>
			</div>			
		</form>
</div>
