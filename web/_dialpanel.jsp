<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<div id="dialpanel" class="easyui-dialog" title="外呼面板" data-options="modal:true,closed:true,iconCls:'icon-dial'" style="width:1200px;height:530px;padding:1px;">
	
	<div class="easyui-layout" data-options="fit:true">
			<div data-options="region:'west',split:true" title="客户基本信息" style="width:250px">
				<table id="clientPg" class="easyui-propertygrid" dataoptions="fit:true" ></table>
				<a href="#" onclick="doDial()" id="doDialBtn" style="margin-top:10px;" class="easyui-linkbutton" data-options="iconCls:'icon-dial',width:'243px'">&nbsp;&nbsp;&nbsp;拨&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号</a>											
				<a href="#" onclick="doDialZero()" id="doDialZeroBtn" style="margin-top:3px;" class="easyui-linkbutton" data-options="iconCls:'icon-dial',width:'243px'">&nbsp;&nbsp;&nbsp;加&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;拨&nbsp;&nbsp;&nbsp;&nbsp;号</a>											
				<a href="#" onclick="doHangup()" id="doHangupBtn" style="margin-top:3px;" class="easyui-linkbutton" data-options="iconCls:'icon-hangup',width:'243px'">&nbsp;&nbsp;&nbsp;挂&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机</a>											
			</div>
			<div data-options="region:'center'" style="padding:1px;">
				<div class="easyui-layout" data-options="fit:true">
					<!-- 编辑区上半部分 -->
					<div data-options="region:'north',split:true,border:false" style="height:200px">
						<div class="easyui-panel" title="通话记录" data-options="fit:true">
							<table id="clientTouchRecordDg">
								<thead>  
									<tr style="height:12px;">                
										<th data-options="field:'TOUCH_TYPE',width:70,align:'center',formatter:touchTypeFormatter">通话方式</th>                
										<th data-options="field:'AGENT',width:130,align:'center'">主叫号码</th>                
										<th data-options="field:'CLIENT_TELEPHONE',width:130,align:'center'">被叫号码</th>
										<th data-options="field:'TOUCH_TIME',width:150,align:'center'">通话时间</th>                
										<th data-options="field:'TOUCH_OPERATOR',width:100,align:'center'">操作工号</th>                
										<th data-options="field:'TOUCH_NOTE',width:300,align:'center'">通话备注</th>                
									</tr>        
								</thead>
							</table>
						</div>
					</div>
					<!-- 编辑区下半部分列表  -> 呼出、呼入弹屏表单   -->
					<div data-options="region:'center',split:true,border:false">
						 <!-- 将呼出弹屏的表单包含进来 -->
						 <div id="callout_form">
						 	<%@ include file="/_callout_touchrecordresultform.jsp"%>
						 </div>

						 <!-- 将来电弹屏的表单包含进来 -->
						 <div id="incomingcall_form">
						 	<%@ include file="/_incomingcall_touchrecordresultform.jsp"%>
						 </div>

						<!-- 来电弹屏、呼出弹屏表单按钮 -->
						<div id="panelDlgBtn" style="margin-left:18%;margin-top:5px">
							<a href="#" id="saveTouchResultBtn" style="width:150px;" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveTouchRecord()">提交接触及客户资料</a>
							<a href="#" id="saveClientInfotBtn" style="width:150px;" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveClientInfo()">仅提交客户资料</a>
						</div>
					</div>
				</div>
			</div>
	</div>	
</div>