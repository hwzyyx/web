<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<form id="callout_touchrecordresultform" type="POST">
		<input type="hidden" id="telId" name="telId"/>
		<input type="hidden" id="taskId" name="taskId"/>
		<div class="easyui-panel" title="本次通话资料登记">
				<div>
					<table width="100%" class="zl_main_center03" border="1" cellspacing="0" cellpadding="0" >
                          <tr align="center" height="27">
				           <td width="60px">外呼结果:</td>
			               <td align="left">
                               <input  type="radio"  name="callout_touchresult" id="callout_result1"  value="1"   onclick="checkcalloutresult('1')"/>
														<label for="callout_result1">接触失败</label>    
	                           <input  type="radio"  name="callout_touchresult" id="callout_result2"  value="2"   onclick="checkcalloutresult('2')"/>
														<label for="callout_result2">接触成功</label>     
                                    </td> 
                          </tr>
                   	</table>
				</div>
		</div>
		<!--外呼弹屏表单 -> 接触不成功  -->
		<div   id="callout_touchfailure"  style="display:none;height:150px;">
		       <table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="0" >
			   		<tr height="27">
						<td width="18%" align="right"> 接触不成功原因：&nbsp;</td>
						<td width="82%" align="left">
							<div id="radioInfo">
								
							</div>
		                 </td>
			   		</tr>
				</table>
		</div>

		<!--外呼弹屏表单 -> 接触成功  -->
		<div   id="callout_touchsuccess"  style="display:none;height:150px;">
		       <table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="0" >
			   		<tr height="27">
						<td align="right">是否对本楼盘感兴趣：</td>
						<td align="left">
							<input  type="radio"  name="callout_success" id="callout_success_insteresting"  value="1"   onclick="checkcalloutsuccess('1')"/>
											<label for="callout_result1">对本楼盘感兴趣</label>    
	                        <input  type="radio"  name="callout_success" id="callout_success_insteresting"  value="2"   onclick="checkcalloutsuccess('2')"/>
											<label for="callout_result2">对本楼盘不感兴趣</label>
						</td>
			     	</tr>
				</table>
				
				<!-- 对本楼盘感兴趣的表单 -->
				<div id="callout_insteresting">
					<table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="0" >
						<tr height="27">
							<td align="right">到访时间是否明确： &nbsp;</td>
							<td align="left">
								<input id="startTime" width="30" name="startTime" class="easyui-datebox" /> 不到访或到访时间不确定请留空
							</td>
					    </tr>
						<tr height="27">
							<td align="right">客户关注热点： &nbsp;</td>
							<td align="left">
								<input id="startTime" width="30" name="startTime" class="easyui-datebox" /> 不到访或到访时间不确定请留空
							</td>
					    </tr>
					</table>
				</div>
				
				<!-- 对本楼盘不感兴趣的表单 -->
				<div id="callout_uninsteresting" style="display:none;">
					<table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="0" >
						<tr height="27">
							<td align="right">不感兴趣原因： &nbsp;</td>
							<td align="left">
								
							</td>
					    </tr>
					</table>
				</div>
				
				<!-- 呼叫成功的公用表单字段 -->
				<div>
					<table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="0" >
						<tr height="27">
							<td align="right">客户需求收集： &nbsp;</td>
							<td align="left">
								需求类型 :
							</td>
					    </tr>
						<tr height="27">
							<td align="right">是否发送短信： &nbsp;</td>
							<td align="left">
								<input  type="radio"  name="callout_success_insteresting" id="callout_success_insteresting"  value="1"   onclick="checkcalloutsuccess('1')"/>
											<label for="callout_result1">发送</label>    
		                       	<input  type="radio"  name="callout_success_uninsteresting" id="callout_success_insteresting"  value="2"   onclick="checkcalloutsuccess('2')"/>
											<label for="callout_result2">不发送</label>
							</td>
					    </tr>
					</table>
				</div>				

		</div>
		
		<!-- 公用字段：备注 -->
		<div>
			<table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="0" >
				<tr height="27" id ="anginCallTimeTr">
					<td align="right">再次外呼时间：&nbsp;</td>
					<td align="left">
						<input id="recallTime" class="easyui-datetimebox" style="width:200px">   *若无需再次外呼请留空
					</td>
				</tr>
				<tr height="27">
					<td align="right">备注 ： &nbsp;</td>
					<td align="left"><textarea name="note" id="note" rows="3" style="width:60%"></textarea> &nbsp;</td>
		     	</tr>
			</table>
		</div>
		
</form>
