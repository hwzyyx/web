<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<div data-options="fit:true" class="easyui-layout">

	<!-- 左边的结果图表 -->
	<div data-options="region:'west',split:true" title="结果图表" style="width:350px;">
		
		<div id="chartDiv" style="width:330px;height:420px;"></div>
		
	</div>
	
	<!-- 右边结果显示区 -->
	<div data-options="region:'center'" title="外呼结果信息">
		<div class="easyui-layout" data-options="fit:true" style="width:100%;">
		
			<div data-options="region:'north',split:true" style="height:105px;vertical-align: middle;">
				<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" style="width:700px;text-align:center">
			
					<tr style="height:30px;">
						<td colspan="8">
							<div id="taskInfo"></div>
							<div id="respondBtn">
								<a href="#" id="easyui-add" onclick="showAutoCallTaskRespond()" class="easyui-linkbutton c4" iconCls='icon-undo' plain="false">调查问卷回复统计</a>
							</div>
						</td>
					</tr>
			
					<tr style="vertical-align: center;height:25px;">
						<td style="width:90px;">
							号码总数
						</td>
						<td style="width:90px;"> 
							未处理
						</td>
						<td style="width:90px;"> 
							已载入
						</td>
						<td style="width:90px;">
							成功
						</td>
						<td style="width:90px;">
							待重试
						</td>
						<td style="width:90px;">
							失败
						</td>
						<td style="width:100px;">
							<span style="font-weight:bolder">呼通率</span>
						</td>
						<td style="width:100px;">
							<span style="font-weight:bolder">失败率</span>
						</td>				
					</tr>
					<tr style="vertical-align: top;">
						<td>
							<a href="#" class="easyui-linkbutton c7" id="totalCountBtn" style="width:80px;">0</a>
						</td>
						<td> 
							<a href="#" class="easyui-linkbutton" id="noCallCountBtn" style="width:70px;">0</a>
						</td>
						<td> 
							<a href="#" class="easyui-linkbutton" id="loadCountBtn" style="width:70px;">0</a>
						</td>
						<td>
							<a href="#" class="easyui-linkbutton" id="successCountBtn" style="width:70px;">0</a>
						</td>
						<td>
							<a href="#" class="easyui-linkbutton" id="retryCountBtn" style="width:70px;">0</a>
						</td>
						<td>
							<a href="#" class="easyui-linkbutton" id="failureCountBtn" style="width:70px;">0</a>
						</td>
						<td>
							<a href="#" class="easyui-linkbutton c1" id="successRateBtn" style="width:80px;">0.00%</a>
						</td>
						<td>
							<a href="#" class="easyui-linkbutton c5" id="failureRateBtn" style="width:80px;">0.00%</a>
						</td>				
					</tr>
				</table>
			</div>
			
			<div data-options="region:'center'">
				
				<table id="autoCallTaskTelephoneDg">
					<thead>
						<tr style="height:12px;">
							<th data-options="field:'ck',checkbox:true"></th>		
							<th data-options="field:'CALLERID_DESC',width:120,align:'center',formatter:calleridformatter">主叫号码</th>
							<th data-options="field:'TELEPHONE',width:120,align:'center'">电话号码</th>
							<th data-options="field:'CLIENT_NAME',width:100,align:'center'">客户姓名</th>
							
							<th data-options="field:'OP_TIME',width:150,align:'center'">外呼时间</th>
							<th data-options="field:'STATE',width:100,align:'center',formatter:stateformatter">外呼结果</th>
							<th data-options="field:'FAILURE',width:100,align:'center',formatter:failureformatter">失败原因</th>
							<th data-options="field:'RETRIED',width:50,align:'center'">已重试</th>
							<th data-options="field:'NEXTCALLOUTTIME',width:150,align:'center',formatter:nextcallouttimeformatter">再次外呼时间</th>
							
						</tr>
					</thead>
				</table>
				
			</div>
			
		
		</div>
	
	</div>
	
</div>

<form id="exportForm" action="#">
</form>
<div id="autoCallTaskTelephoneDgTool" style="padding:5px;">
	<a href="#" id="easyui-add" onclick="callOutResultExport()" class="easyui-linkbutton" iconCls='icon-redo' plain="true">导出外呼结果</a>
	
	<div style="display:inline;position:absolute;right:10px;">
		外呼号码：<input id="telephone" name="telephone" type="text" class="easyui-numberbox" />
		&nbsp;&nbsp;外呼状态：<select class="easyui-combobox" id="state" name="state" style="width:80px;">
			<option value="5">请选择</option>
			<option value="0">未处理</option>
			<option value="1">已载入</option>
			<option value="2">已成功</option>
			<option value="3">待重呼</option>
			<option value="4">已失败</option>
		</select>
		<a href="#" id="easyui-add" onclick="refreshAutoCallTaskResult()" class="easyui-linkbutton" iconCls='icon-reload' plain="true">刷新结果</a>
	</div>
</div>
 

  