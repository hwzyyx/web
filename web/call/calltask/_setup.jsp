<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<div class="easyui-tabs" data-options="tabWidth:112,fit:true">
<!--<div class="easyui-tabs" data-options="tabWidth:112" style="width:700px;height:400px">-->
	<!-- 项目信息 -->
	<div title="项目信息" style="padding:10px">
		<form id="setup-updateTaskForm" method="post">
			<table>
				<tr>
					<td>项目名称：</td>
					<td>
						<input name="setuptask.CT_ID" id="setuptaskCT_ID" class="easyui-validatebox" type="text" required="true" style="display:none;"></input>
						<input name="setuptask.TASK_NAME" id="setuptaskTASK_NAME" style="width:200px;" class="easyui-textbox" type="text" required="true" missingMessage="项目名称不能为空!"></input>
						<input name="setuptask.TASK_TYPE" id="setuptaskTASK_TYPE" value="1" class="easyui-validatebox" type="text" style="display:none;"></input>
					</td>
				</tr>
				<tr>
					<td>主叫号码：</td>
					<td>
						<input name="setuptask.CALLERID" id="setuptaskCALLERID" style="width:200px;" class="easyui-numberbox" type="text" required="true" missingMessage="主叫号码不能为空!"></input>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<a href="#" id="setupSaveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveEdit()" style="width:100px">保存</a>
					</td>
				</tr>
			</table>
		</form>
	</div>
	<!-- 项目信息结束 -->
	
	<!-- 添加号码 -->
	<div title="添加号码" style="padding:10px">
			<form id="setup-addTelephone" method="post">
				<table>
					<tr>
						<td>电话号码：</td>
						<td>
							<input name="telephone.TELEPHONE" id="TELEPHONE" style="width:200px;" class="easyui-numberbox" type="text" required="true" missingMessage="电话号码不能为空!"></input>
						</td>
					</tr>
					<tr>
						<td>客户姓名：</td>
						<td>
							<input name="telephone.CLIENT_NAME" id="CLIENT_NAME" style="width:200px;" class="easyui-textbox" type="text"></input>
						</td>
					</tr>
					<tr>
						<td>性　　别：</td>
						<td>
							<select class="easyui-combobox" style="width: 155px;" name="telephone.CLIENT_SEX" id="CLINET_SEX" data-options="panelHeight:'auto'">
					              <option value="1">男</option>
					              <option value="0">女</option>
					        </select>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<a href="#" style="margin-top:10px;width:220px;" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveTelephone()">保存</a>
						</td>
					</tr>
					
				</table>
				
						
			</form>
	</div>
	<!-- 添加号码结束 -->

	<!-- 导入号码 -->
	<div title="导入号码" style="padding:3px">
			<form id="setup-uploadFileForm" method="post" enctype="multipart/form-data" >
				<div style="display:inline;" id="row1"><input type="file" id="phoneFile" name="phoneFile" style="float: left;width:400px;margin-bottom:5px;padding-bottom:3px;padding-top:3px;" data-options="prompt:'选择号码文件...'"></div>
				<div style="display:inline;height:80px;" id="row2">号码文件模板&nbsp;<a href="callTelephone/template">下载</a></div>
				<div style="display:inline;padding-top:6px;" class="easyui-tooltip" title="<table><tr><td>号码</td><td>姓名</td><td>性别</td></tr> 
						<tr><td>13800000000</td><td>张三</td><td>男</td></tr>
						<tr><td>13988887777</td><td>李四</td><td>男</td></tr>
						<tr><td>13899999999</td><td>王五</td><td>男</td></tr>
						<tr><td>13888888888</td><td>陈六</td><td>男</td></tr>
						<br>
						注意:<br>
						(1)第一列必须为号码，内容为数字号码，否则将会被过滤掉<br>
						(2)姓名及性别可以为空<br><br>" style="width:100px;padding:5px;float:top;"><a href="#">模板说明</a></div>
				<div>
					<a href="#" onclick="uploadPhoneFile()" class="easyui-linkbutton" iconCls="icon-ok" style="width:90%;margin-top:2px;">上传号码</a>
				</div>
			</form>
	</div>
	<!-- 导入号码结束 -->



	<!-- 号码列表 -->
	<div title="号码列表" style="padding:2px;height:200px;">
				<table id="callTelephoneDg">
					<thead>  
						<tr style="height:12px;">
							<th data-options="field:'ck',checkbox:true"></th>                
							<th data-options="field:'TELEPHONE',width:100,align:'center'">电话号码</th>                
							<th data-options="field:'STATE_DESC',width:120,align:'center'">状态</th>                
							<th data-options="field:'CLIENT_NAME',width:70,align:'center'">客户姓名</th>                
							<th data-options="field:'CLIENT_SEX',width:40,align:'center',formatter:sexformatter">性别</th>                
							<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
							<th data-options="field:'CREATE_USERCODE',width:70,align:'center'">添加人</th>                
							<th data-options="field:'OPER_ID',width:70,align:'center'">外呼工号</th>
							<th data-options="field:'OP_TIME',width:150,align:'center'">外呼时间</th>
							<th data-options="field:'LOCATION',width:80,align:'center'">归属地</th>                
						</tr>        
					</thead>
				</table>
				<div id="setup-callTelephoneDgtool" style="padding:5px">
					<span>电话号码：</span><input type="text" id="telephone" class="easyui-numberbox" style="width:100px;"/>  
			        <span>客户姓名：</span><input type="text" id="clientName" class="easyui-textbox" style="width:100px;" />
					<span>状态：<input class="easyui-combobox" name="state" id="state" /></span>
			        <span>开始时间：</span><input id="telephoneStartTime" style="width:120px;" name="telephoneStartTime" class="easyui-datebox" />
			        <span>结束时间：</span><input id="telephoneEndTime" style="width:120px;" name="telephoneEndTime" class="easyui-datebox" />
					
			        <a href="javascript:telephoneFindData()" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>  
					<div>	
						<a href="#" id="easyui-add" onclick="telephoneDel()" class="easyui-linkbutton" iconCls="icon-cancel" plain="true">删除选中项</a>
					</div>  
				</div>	
	</div>

	

	<!-- 号码列表结束 -->

	<!-- 任务授权 -->
	<div title="任务授权" style="padding:10px">
		<div id="opertorUl"></div>
		<div id="taskAuthBtn">
			<a href="#" id="saveBtn" style="margin-top:10px;width:200px;" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAuth()">保存授权</a>
		</div>
	</div>
	<!-- 任务授权结束 -->

</div>