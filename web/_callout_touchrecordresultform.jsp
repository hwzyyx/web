<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<form id="callout_touchrecordresultform" type="POST">
		<input type="hidden" id="telId" name="telId"/>
		<input type="hidden" id="taskId" name="taskId"/>
		<div class="easyui-panel" title="本次通话资料登记">
				<div>
					<table width="100%" class="zl_main_center03" border="0" cellspacing="0" cellpadding="0" >
                          <tr align="center">
					           <td width="18%" align="right">外呼结果:&nbsp;</td>
				               <td align="left">
	                               <input  type="radio"  name="callout_touchresult" id="callout_result1"  value="1"   onclick="checkcalloutresult('1')"/>
															<label for="callout_result1">接触失败</label>
								   <input  type="radio"  name="callout_touchresult" id="callout_result2" checked="checked"  value="2"   onclick="checkcalloutresult('2')"/>
															<label for="callout_result2">接触成功-感兴趣</label>    
		                           <input  type="radio"  name="callout_touchresult" id="callout_result3"  value="3"   onclick="checkcalloutresult('3')"/>
															<label for="callout_result3">接触成功-不感兴趣</label>     
	                           </td> 
                          </tr>
						  
						  <!--外呼弹屏表单 -> 接触不成功  -->
						  <tr id="callout_touchfailure"  style="display:none;">
								<td colspan="2">
									<table class="zl_main_center03" width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:5px;">
										<tr>
											<td width="18%" align="right"> 接触不成功原因:&nbsp;</td>
											<td width="82%" align="left">
												<div id="touchfailurereason">
													
												</div>
							                 </td>
										 </tr>
									</table>
								</td>
						  </tr>

						  <!--外呼弹屏表单 -> 接触成功  -->
						  <tr id="callout_touchsuccess"  style="display:none;">
								<td colspan="2">
									<table class="zl_main_center03" width="100%" border="0" cellspacing="0" cellpadding="0" >
										 <!-- 对本楼盘感兴趣的表单 -->
										 <tr id="callout_insteresting">
											<td colspan="2">
												<table class="zl_main_center03" width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:3px;">
													<tr>
														<td align="right">客户关注热点:&nbsp;</td>
														<td align="left">
															<div id="attention_focus_callout"></div>
														</td>
												    </tr>
													<tr align="right">
														<td align="right" width="18%">到访时间是否明确:&nbsp;</td>
														<td align="left">
															<div style="margin-top: 2px;"></div>
															<input id="visitTime_callout" style="width:200px" name="visitTime_callout" class="easyui-datebox" /> *不到访或到访时间不确定请留空
														</td>
												    </tr>
												</table>
											</td>
										 </tr>
											<!-- 对本楼盘不感兴趣的表单 -->
											<tr>
												<td colspan="2">
													<table class="zl_main_center03" width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:3px;">
														<tr id="callout_uninsteresting" style="display:none;">
															<td align="right" width="18%">不感兴趣原因:&nbsp;</td>
															<td align="left">
																<div id="uninsteresting_reason"></div>
															</td>
													    </tr>
													</table>
												</td>
										    </tr>
											
											<!-- 呼叫成功的公用表单字段 -->
											 
											<tr>
												<td colspan="2">
													<table class="zl_main_center03" width="100%" border="0" cellspacing="0" cellpadding="0">
														<tr>
															<td align="right" width="18%">客户需求收集:&nbsp;</td>
															<td align="left">
																<div style="margin-top: 3px;"></div>
																<!-- 需求类型 -->
																需求类型：<input class="easyui-combobox" name="require_type_callout" id="require_type_callout" style="width:110px;"/>
																<!-- 需求户型 -->
																&nbsp;&nbsp;需求户型：<input class="easyui-combobox" name="require_housetype_callout" id="require_housetype_callout" style="width:110px;"/>
																<!-- 需求面积 -->
																&nbsp;&nbsp;需求面积：<input class="easyui-combobox" name="require_area_callout" id="require_area_callout" style="width:110px;"/>
																<!-- 意向价格 -->
																&nbsp;&nbsp;意向价格：<input class="easyui-combobox" name="intend_price_callout" id="intend_price_callout" style="width:130px;"/><br/>
																<div style="margin-top: 3px;"></div>
																<!-- 置业意向->是指客户的关注程度 -->
																置业意向：<input class="easyui-combobox" name="zyintend_callout" id="zyintend_callout" style="width:110px;"/>
																<!-- 置业目的 -->
																&nbsp;&nbsp;置业目的：<input class="easyui-combobox" name="properties_purpose_callout" id="properties_purpose_callout" style="width:110px;"/>
																<!-- 需求区位 -->
																&nbsp;&nbsp;需求区位：<input class="easyui-combobox" name="require_location_callout" id="require_location_callout" style="width:110px;"/>
																<!-- 交房时间 -->
																&nbsp;&nbsp;交房时间：<input class="easyui-combobox" name="makingroom_time_callout" id="makingroom_time_callout" style="width:130px;"/>
															</td>
													    </tr>
														<tr>
															<td align="right">是否发送短信:&nbsp;</td>
															<td align="left">
																<input  type="radio"  name="send_message_callout" id="send_message_callout" value="1"/>
																			<label for="send_message_callout">发送</label>    
											                      	<input  type="radio"  name="send_message_callout" id="donot_send_message_callout" checked="checked" value="2"/>
																			<label for="donot_send_message_callout">不发送</label>
															</td>
													    </tr>
													</table>
												</td>
										    </tr>
									</table>
								</td>
						  </tr>					  
						 <tr height="27" style="margin-top:3px;">
								<td align="right">再次外呼时间:&nbsp;</td>
								<td align="left">
									<input id="recallTime" class="easyui-datetimebox" style="width:200px">   *若无需再次外呼请留空
								</td>
							</tr>
							<tr style="margin-top:3px;margin-bottom: 1px;">
								<td align="right">备注:&nbsp;</td>
								<td align="left"><textarea name="touchnote_callout" id="touchnote_callout" rows="2" style="width:60%"></textarea> &nbsp;</td>
					     	</tr>
                   	</table>
				</div>
		 </div>
		
</form>
