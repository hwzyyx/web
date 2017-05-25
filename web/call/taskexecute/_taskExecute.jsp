<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<div data-options="fit:true" class="easyui-layout">
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="callTelephoneDg">
						<thead>  
							<tr style="height:12px;">
<!--								<th data-options="field:'ck',checkbox:true"></th>                -->
								<th data-options="field:'TELEPHONE',width:100,align:'center'">电话号码</th>                
								<th data-options="field:'STATE',width:50,align:'center',formatter:telephoneStateformatter">状态</th>                
								<th data-options="field:'CLIENT_NAME',width:80,align:'center'">客户姓名</th>                
								<th data-options="field:'CLIENT_SEX',width:50,align:'center',formatter:sexformatter">性别</th>                
								<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
								<th data-options="field:'OP_TIME',width:100,align:'center'">外呼时间</th>
								<th data-options="field:'LOCATION',width:80,align:'center'">归属地</th>                
								<th data-options="field:'EXEC',width:80,align:'center',formatter:execformatter">操作</th>                
							</tr>        
						</thead>
					</table>
					<div id="callTelephoneDgtool" style="padding:5px">
						<div>	
							<span>请求外呼数据：</span>
							<select class="easyui-combobox" style="width:80px;" id="reqTelCombobox" data-options="panelHeight:'auto'">
					              <option value="5">5条</option>
					              <option value="10">10条</option>
				        	</select>
							<button id="reqDataBtn" onclick="reqCallDatas()">请求数据</button>
						</div>  
					</div>	
			</div>
	</div>
   
