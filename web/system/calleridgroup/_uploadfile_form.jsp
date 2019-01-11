<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>
			<div style="margin-top:10px;">
				<input class="easyui-filebox" id="callerIdFile" name="callerIdFile" data-options="prompt:'选择号码文件进行上传'" style="width:380px;" required="true" missingMessage="主叫号码文件不能为空!">
			</div>
			<div style="margin-top:10px;">
				模板下载：
				<a href="sysCallerId/template?type=txt&identify=callerId">TXT</a>&nbsp;|&nbsp;
				<a href="sysCallerId/template?type=excel&identify=callerId">EXCEL</a>
			</div>
			<div style="margin-top:15px;font-weight: bold;color: red;">
				*注意：<br/>
				(1)在上传主叫号码文件向主叫号码组分配主叫号码，系统将先删除原来分配的记录,然后将号码文件的主叫号码分配给选中的主叫号码组。<br/>
				(2)上传文件中的主叫号码可以是已经存在于主叫号码管理模块已添加的主叫号码，也可以是新的号码，系统会自动存入主叫号码管理并进行分配。
			</div>
		</td>
	</tr>
</table>
		
<div id="addCallerIdGroupAssignByUploadFileDlgBtn">
	<a href="#" id="saveUploadFileBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="uploadCallerIdFile()">上传号码</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="uploadCallerIdFileCancel()">取消</a>
</div>  