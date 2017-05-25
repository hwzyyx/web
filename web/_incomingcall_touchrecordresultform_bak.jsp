<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!--来电弹屏表单 -->
<form id="incomingcall_touchrecordresultform" type="POST">
	<div class="easyui-panel" title="本次来电资料登记">
	</div>
	<div   id="incomingcall"  style="height:150px;">
	       <table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="1" >
		   		<tr height="27">
					<td width="10%" align="right"> 来电目的：&nbsp;</td>
					<td width="92%" align="left">
						<input name="callReason" id="callReason_consult" checked="checked" type="radio" value="1" onclick="checkCallReasonResult(1)"><label for="callReason_consult">来电咨询</label>
						<input name="callReason" id="callReason_complaint" type="radio" value="2" onclick="checkCallReasonResult(2)"><label for="callReason_complaint">来电投诉</label>
						<input name="callReason" id="callReason_abnormal" type="radio" value="3" onclick="checkCallReasonResult(3)"><label for="callReason_abnormal">异常来电</label>
	                 </td>
		   		</tr>
				<tr height="27">
					<td colspan="2" width="100%">
							<!-- 咨询类来电表单内容 -->
							<div id="consult"> 
								<table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="1" >    
									<tr height="27">
										<td width="10%" align="right"> 客户咨询热点：&nbsp;</td>
										<td align="left">
											<input name="需求类型" type="checkbox" value="1"  />区位
											<input name="需求户型" type="checkbox" value="2"  />交通
											<input name="需求面积" type="checkbox" value="3"  />价格
											<input name="需求面积" type="checkbox" value="4"  />户型
											<input name="需求面积" type="checkbox" value="5"  />配套
										</td>
									</tr>
									<tr height="27">
										<td align="right">  客户需求收集：&nbsp;</td>
										<td align="left">
											<input name="需求类型" type="checkbox" value="1"  />需求类型
											<input name="需求户型" type="checkbox" value="2"  />需求户型
											<input name="需求面积" type="checkbox" value="3"  />需求面积
										</td>
									</tr>
								</table>
							</div>
			
							<!-- 投诉类来电表单内容 -->
							<div id="complaint" style="display:none;">
								<table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="1" >     
									<tr height="27">
										<td width="10%" align="right"> 投诉登记：&nbsp;</td>
										<td align="left">
											<input name="服务不满意" type="checkbox" value="1"  />服务不满意
											<input name="物业不到位" type="checkbox" value="2"  />物业不到位
											<input name="交通不便利" type="checkbox" value="3"  />交通不便利
										</td>
									</tr>
								</table>
							</div>
							
							<!-- 异常类来电表单内容 -->
							<div id="abnormal" style="display:none;">  
								<table class="zl_main_center03" width="100%" border="1" cellspacing="0" cellpadding="1" >   
									<tr height="27">
										<td width="10%" align="right"> 异常登记：&nbsp;</td>
										<td align="left">
											<input name="abnormal" type="radio" value="1" />来电骚扰
											<input name="abnormal" type="radio" value="2" />来电无声
											<input name="abnormal" type="radio" value="3" />其他异常
										</td>
									</tr>
								</table>
							</div>
					</td>
				</tr>
				<!-- 公共字段：备注 -->
				<tr height="27">
					<td align="right">备注 ： &nbsp;</td>
					<td align="left"><textarea name="note" id="note" rows="3" style="width:60%"></textarea> &nbsp;</td>
				</tr>
			</table>
	</div>
</form>