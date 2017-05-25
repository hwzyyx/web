<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!--来电弹屏表单 -->
<form id="incomingcall_touchrecordresultform" type="POST">
		<div class="easyui-panel" title="本次通话资料登记">
				<div>
					<table width="100%" class="zl_main_center03" border="0" cellspacing="0" cellpadding="0" >
                          <tr align="center">
					           <td width="18%" align="right">来电原因:&nbsp;</td>
				               <td align="left">
	                               <input  type="radio"  name="incomingcall_callreason" id="callreason_consult" checked="checked"  value="1"   onclick="checkIncomingCallCallReasonResult('1')"/>
															<label for="callreason_consult">来电咨询</label>
								   <input  type="radio"  name="incomingcall_callreason" id="callreason_complaint" value="2"   onclick="checkIncomingCallCallReasonResult('2')"/>
															<label for="callreason_complaint">来电投诉</label>    
		                           <input  type="radio"  name="incomingcall_callreason" id="callreason_abnormal"  value="3"   onclick="checkIncomingCallCallReasonResult('3')"/>
															<label for="callreason_abnormal">异常来电</label>     
	                           </td> 
                          </tr>
						  
						  <!--来电弹屏表单 -> 来电咨询  -->
						  <tr id="incomingcall_consult"  style="display:none;">
								<td colspan="2">
									<table class="zl_main_center03" width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:5px;">
										<tr>
											<td width="18%" align="right">客户咨询热点:&nbsp;</td>
											<td width="82%" align="left">
												<div id="attention_focus_incomingcall"></div>
							                 </td>
										 </tr>
										 <tr>
											<td align="right">客户认知途径:&nbsp;</td>
											<td align="left">
												<div id="coginition_way" style="margin-top:5px;margin-bottom:2px;"> 
													
												</div>
							                 </td>
										 </tr>
										<tr>
											<td align="right" width="18%">客户需求收集:&nbsp;</td>
											<td align="left">
												<div style="margin-top: 3px;"></div>
												<!-- 需求类型 -->
												需求类型：<input class="easyui-combobox" name="require_type_incomingcall" id="require_type_incomingcall" style="width:110px;"/>
												<!-- 需求户型 -->
												&nbsp;&nbsp;需求户型：<input class="easyui-combobox" name="require_housetype_incomingcall" id="require_housetype_incomingcall" style="width:110px;"/>
												<!-- 需求面积 -->
												&nbsp;&nbsp;需求面积：<input class="easyui-combobox" name="require_area_incomingcall" id="require_area_incomingcall" style="width:110px;"/>
												<!-- 意向价格 -->
												&nbsp;&nbsp;意向价格：<input class="easyui-combobox" name="intend_price_incomingcall" id="intend_price_incomingcall" style="width:130px;"/><br/>
												<div style="margin-top: 3px;"></div>
												<!-- 置业意向->是指客户的关注程度 -->
												置业意向：<input class="easyui-combobox" name="zyintend_incomingcall" id="zyintend_incomingcall" style="width:110px;"/>
												<!-- 置业目的 -->
												&nbsp;&nbsp;置业目的：<input class="easyui-combobox" name="properties_purpose_incomingcall" id="properties_purpose_incomingcall" style="width:110px;"/>
												<!-- 需求区位 -->
												&nbsp;&nbsp;需求区位：<input class="easyui-combobox" name="require_location_incomingcall" id="require_location_incomingcall" style="width:110px;"/>
												<!-- 交房时间 -->
												&nbsp;&nbsp;交房时间：<input class="easyui-combobox" name="makingroom_time_incomingcall" id="makingroom_time_incomingcall" style="width:130px;"/>
											</td>
									    </tr>
										<tr align="right">
											<td align="right" width="18%">到访时间是否明确:&nbsp;</td>
											<td align="left">
												<div style="margin-top: 4px;"></div>
												<input id="visitTime_incomingcall" style="width:200px" name="visitTime_incomingcall" class="easyui-datetimebox" /> *不到访或到访时间不确定请留空
											</td>
									    </tr>
										<tr>
											<td align="right">是否发送短信:&nbsp;</td>
											<td align="left">
												<input  type="radio"  name="send_message_incomingcall" id="send_message_incomingcall" value="1"/>
															<label for="send_message_incomingcall">发送</label>    
							                      	<input  type="radio"  name="send_message_incomingcall" id="donot_send_message_incomingcall" checked="checked" value="2"/>
															<label for="donot_send_message_incomingcall">不发送</label>
											</td>
									    </tr>
									</table>
								</td>
						  </tr>

						  <!--来电弹屏表单 -> 来电投诉  -->
						  <tr id="incomingcall_complaint"  style="display:none;">
								<td colspan="2">
									<table class="zl_main_center03" width="100%" border="0" cellspacing="0" cellpadding="0" >
										 <tr>
											<td align="right" width="18%">来电投诉登记:&nbsp;</td>
											<td align="left">
												<div id="complaint_item"> 
													
												</div>
							                 </td>
										 </tr>
									</table>
								</td>
						  </tr>

						  <!--来电弹屏表单 -> 异常来电  -->
						  <tr id="incomingcall_abnormal"  style="display:none;">
								<td colspan="2">
									<table class="zl_main_center03" width="100%" border="0" cellspacing="0" cellpadding="0" >
										 <tr>
											<td align="right" width="18%">异常来电原因:&nbsp;</td>
											<td align="left">
												<div id="abnormal_reason"> 
													
												</div>
							                 </td>
										 </tr>
									</table>
								</td>
						  </tr>		
						
						<!-- 公共字段 -->				  
						<tr style="margin-top:3px;margin-bottom: 1px;">
							<td align="right">备注:&nbsp;</td>
							<td align="left"><textarea name="touchnote_incomingcall" id="touchnote_incomingcall" rows="2" style="width:60%;margin-top:3px;"></textarea> &nbsp;</td>
				     	</tr>
                   	</table>
				</div>
		 </div>
		
</form>