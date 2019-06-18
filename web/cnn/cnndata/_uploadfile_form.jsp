<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<table>
	<tr>
		<td>
			<div style="margin-top:10px;">
				<input class="easyui-filebox" id="cnnDataFile" name="cnnDataFile" data-options="prompt:'选择号码文件进行上传'" style="width:380px;" required="true" missingMessage="改号通知数据文件不能为空!">
			</div>
			<div style="margin-top:20px;margin-bottom:10px;">
				备注：
					<br>&nbsp;&nbsp;&nbsp;&nbsp;第一列数据是客户号码
					<br>&nbsp;&nbsp;&nbsp;&nbsp;第二列数据是客户的新号码
					<br>&nbsp;&nbsp;&nbsp;&nbsp;第三列数据是标识符，如果为空时，播放中文；如果不为空时，播放英文语音
					<br>&nbsp;&nbsp;&nbsp;&nbsp;第四列数据是修改的时间
			</div>
			<div style="margin-top:10px;">
				模板下载：
				<a href="cnnData/template?type=txt&identify=cnn">TXT</a>&nbsp;|&nbsp;
				<a href="cnnData/template?type=excel&identify=cnn">EXCEL</a>
			</div>
		</td>
	</tr>
</table>
		
<div id="uploadFileDlgBtn">
	<a href="#" id="saveUploadFileBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="uploadCnnDataFile()">上传号码</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="uploadCnnDataFileCancel()">取消</a>
</div>  