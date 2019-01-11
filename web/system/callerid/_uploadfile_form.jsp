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
		</td>
	</tr>
</table>
		
<div id="addCallerIdByUploadFileDlgBtn">
	<a href="#" id="saveUploadFileBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="uploadCallerIdFile()">上传号码</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="uploadCallerIdFileCancel()">取消</a>
</div>  