<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 编辑/添加　窗口 -->
<div data-options="fit:true" class="easyui-layout">
		<form id="autoCallTaskTelephoneForm" method="post">
		
			<div style="padding-left:10px;padding-top:10px;">
				客户号码：
				<input type="hidden" name="autoCallTaskTelephone.TEL_ID" id="TEL_ID"/>
				<input style="width:200px;" id="CUSTOMER_TEL" name="autoCallTaskTelephone.CUSTOMER_TEL" class="easyui-numberbox" type="text" required="true" missingMessage="电话号码不能为空!"></input>
			</div>
			
			<div style="padding-left:10px;padding-top:5px;">
				客户姓名：
				<input style="width:200px;" id="CUSTOMER_NAME" name="autoCallTaskTelephone.CUSTOMER_NAME" class="easyui-textbox" type="text" required="true" missingMessage="客户姓名不能为空!"></input>
			</div>
			
			<div id="periodDiv" style="padding-left:10px;padding-top:5px;">
				日&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;期：
				<input style="width:200px;" value="20180101" readonly="true" id="PERIOD" name="autoCallTaskTelephone.PERIOD" class="easyui-textbox" type="text"></input>
				<select class="easyui-combobox" id="dateYearCombobox">
					<option value="2018">2018年</option>
					<option value="2019">2019年</option>
					<option value="2020">2020年</option>
					<option value="2021">2021年</option>
					<option value="2022">2022年</option>
					<option value="2023">2023年</option>
					<option value="2024">2024年</option>
					<option value="2025">2025年</option>
					<option value="2026">2026年</option>
					<option value="2027">2027年</option>
					<option value="2028">2028年</option>
					<option value="2029">2029年</option>
					<option value="2030">2030年</option>
				</select>
				<select class="easyui-combobox" id="dateMonthCombobox">
					<option value="01">01月</option>
					<option value="02">02月</option>
					<option value="03">03月</option>
					<option value="04">04月</option>
					<option value="05">05月</option>
					<option value="06">06月</option>
					<option value="07">07月</option>
					<option value="08">08月</option>
					<option value="09">09月</option>
					<option value="10">10月</option>
					<option value="11">11月</option>
					<option value="12">12月</option>
				</select>
				<div style="display:none;">
					<select class="easyui-combobox" id="dateDayCombobox">
						<option value="01">01日</option>
						<option value="02">02日</option>
						<option value="03">03日</option>
						<option value="04">04日</option>
						<option value="05">05日</option>
						<option value="06">06日</option>
						<option value="07">07日</option>
						<option value="08">08日</option>
						<option value="09">09日</option>
						<option value="10">10日</option>
						<option value="11">11日</option>
						<option value="12">12日</option>
						<option value="13">13日</option>
						<option value="14">14日</option>
						<option value="15">15日</option>
						<option value="16">16日</option>
						<option value="17">17日</option>
						<option value="18">18日</option>
						<option value="19">19日</option>
						<option value="20">20日</option>
						<option value="21">21日</option>
						<option value="22">22日</option>
						<option value="23">23日</option>
						<option value="24">24日</option>
						<option value="25">25日</option>
						<option value="26">26日</option>
						<option value="27">27日</option>
						<option value="28">28日</option>
						<option value="29">29日</option>
						<option value="30">30日</option>
						<option value="31">31日</option>
					</select>
				</div>
			</div>
			
			<div id="illegalCityDiv" style="padding-left:10px;padding-top:5px;">
				违章城市：
				<input style="width:200px;" data-options="prompt:'北京市'" id="ILLEGAL_CITY" name="autoCallTaskTelephone.ILLEGAL_CITY" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="punishmentUnitDiv" style="padding-left:10px;padding-top:5px;">
				处罚单位：
				<input style="width:200px;" data-options="prompt:'北京市朝阳交警大队'" id="PUNISHMENT_UNIT" name="autoCallTaskTelephone.PUNISHMENT_UNIT" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="illegalReasonDiv" style="padding-left:10px;padding-top:5px;">
				违章事由：
				<input style="width:200px;" data-options="prompt:'违章停车'" id="ILLEGAL_REASON" name="autoCallTaskTelephone.ILLEGAL_REASON" class="easyui-textbox" type="text"></input>
			</div>
			
			<div id="chargeDiv" style="padding-left:10px;padding-top:5px;">
				费&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用：
				<input style="width:200px;" id="CHARGE" name="autoCallTaskTelephone.CHARGE" class="easyui-numberbox" data-options="min:0,max:50000,precision:2,prompt:'0.00'">
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
