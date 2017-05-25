<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<div id="autoNumberTab" class="easyui-tabs" data-options="fit:true">

	<div title="号码组管理">
	
		<form id="autoNumberForm" method="post">
		
			<table border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" style="width:100%;padding-top:15px;">
				<tr style="padding-top: 10px;">
					<td style="width:302px;">
						<span style="padding-left:10px;">
							号码组名称：
						</span>
						<input type="hidden" name="autoNumber.NUMBER_ID" id="NUMBER_ID"/>
						<input name="autoNumber.NUMBER_NAME" id="NUMBER_NAME" class="easyui-textbox" type="text" data-options="required:true" style="width:300px;"></input>
						<span style="padding-left:30px;">
							<a href="#" id="autoNumberSaveBtn" class="easyui-linkbutton" style="width:150px;" iconCls="icon-ok" onclick="saveAutoNumberAdd()">保存</a>
						</span>
					</td>
				</tr>
			</table>
		
		</form>
	</div>
	
	<div id="importTelephoneTab" title="导入号码">
		<form id="uploadTelephoneForm" method="post" enctype="multipart/form-data">
			<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" width="680"
					  style="border-collapse: collapse;">
				<tr>
					<td style="text-align:right;vertical-align: top;width:80px;">
						<div style="padding-top:10px;">号码文件：</div>
					</td>
					<td style="width:320px;">
						<div style="padding-top:10px;">
							 <input class="easyui-filebox" id="telephoneFile" name="telephoneFile" data-options="prompt:'选择号码文件进行上传'" style="width:300px;" required="true" missingMessage="号码文件不能为空!">
				        </div>
					</td>
					<td>
						<div style="display:inline;padding-top:6px;" class="easyui-tooltip" title="
								<table>
									<tr><td>号码</td><td>客户姓名</td></tr> 
									<tr><td>13800000000</td><td>张三</td></tr>
									<tr><td>13988887777</td><td>李四</td></tr>
									<tr><td>13899999999</td><td>王五</td></tr>
									<tr><td>13888888888</td><td>陈六</td></tr>
								<br>
								注意:<br>
								(1)号码文件必须为txt、xls或xlsx。txt文件时号码和姓名用英文逗号（即,）分隔 
								(2)第一列必须为号码，内容为数字号码，否则将会被过滤掉<br>
								(3)第二列为姓名,可以为空<br><br>" style="width:100px;padding:5px;float:top;"><a href="#">模板说明</a>
						</div>
					</td>
				</tr>
				<tr>
					<td colspan="3" style="padding-left:80px;">
						<a href="#" onclick="uploadPhoneFile()" class="easyui-linkbutton" iconCls="icon-ok" style="width:200px;;margin-top:10px;">上传号码</a>
					</td>
				</tr>
			</table>
			
		</form>
	</div>
	
	<div id="autoNumberTelephoneDgTab" title="号码列表">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 顶部查询区 -->
			<div data-options="region:'north',split:true,border:true" style="height:45px;padding-top:5px;padding-left:5px;">
				<table>
					<tr style="vertical-align: top;">
						<td>电话号码：<input id="telephone" type="text" class="easyui-textbox" style="width:150px;"/>
						
							<span style="padding-left:30px;">
								客户姓名：<input id="clientName" type="text" class="easyui-textbox" style="width:150px;"/>
							</span>
							<span style="padding-left:30px;">
								<a href="javascript:findDataForTelephone()" style="width:100px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
							</span>
						</td>
					</tr>
				</table>
			</div>
		
			<!-- 数据显示区 -->
			<div data-options="region:'center',split:true,border:false">
				
				<table id="autoNumberTelephoneDg">
					<thead>
						<tr style="height:12px;">
							<th data-options="field:'ck',checkbox:true"></th>		
							<th data-options="field:'TELEPHONE',width:200,align:'center'">电话号码</th>
							<th data-options="field:'CLIENT_NAME',width:200,align:'center'">客户姓名</th>
							<th data-options="field:'id',width:100,align:'center',formatter:telephonerowformatter">操作</th>
						</tr>
					</thead>
				</table>	
			</div>
		</div>
	</div>
</div>

<div id="telephoneopertool" style="padding:5px;">
	<a href="#" id="easyui-add" onclick="autoNumberTelephoneAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增号码</a>
	<span style="padding:10px;">
		<a href="#" id="easyui-delete" onclick="autoNumberTelephoneDel()" class="easyui-linkbutton" iconCls='icon-remove' plain="true">删除选中记录</a>
	</span>
</div>
