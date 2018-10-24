<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<form id="bshVoiceForm" method="post" enctype="multipart/form-data" data-options="novalidate:true">
<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" style="border-collapse: collapse;">
			<tr style="padding-top: 10px;margin-top:30px;">
				<td style="width:120px;text-align:center;">
					语音描述
				</td>
				<td>
					<input name="bshVoice.VOICE_ID" id="VOICE_ID" type="hidden"></input>
					<input class="easyui-textbox" name="bshVoice.VOICE_DESC" id="VOICE_DESC" data-options="multiline:true"  width="300" height="100" required="true" missingMessage="语音描述名称不能为空!">
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:10px;">语音类型</div>
				</td>
				<td>
					<div style="padding-top:10px;">
						<a href="#" id="voiceTypeDescId" onclick="voiceAdd()" class="easyui-linkbutton" plain="true">语音</a>
			        </div>
				</td>
			</tr>
			<tr style="padding-top: 10px;">
				<td style="width:120px;text-align:center;">
					语音命名
				</td>
				<td>
					<input width="300" name="bshVoice.VOICE_NAME" id="VOICE_NAME" class="easyui-textbox" type="text" required="true" missingMessage="语音命名!"></input>
				</td>
			</tr>
			<tr>
				<td style="text-align:center;vertical-align: top;">
					<div style="padding-top:0px;">
						
						<div style="padding-top:10px;">
							<a href="#" class="easyui-linkbutton" id="createType_voiceFile" data-options="toggle:true,group:'g1',selected:true">创建方式：文件</a>
						</div>
						<div style="padding-top:5px;">
							<a href="#" class="easyui-linkbutton" id="createType_tts" data-options="toggle:true,group:'g1',disabled:true">创建方式：TTS</a>
						</div>
					</div>
				</td>
				<td>
					<div style="padding-top:10px;" id="voiceFileDiv">
						<div>
							<input class="easyui-filebox" id="voiceFile" name="voiceFile" data-options="prompt:'选择一个语音文件进行上传'" style="width:400px;">
						</div>
						<div style="padding-left:10px;padding-top:10px;padding-bottom:15px;">
							<span style="color: red;">** 注意：语音格式限于 wav **</span>
				        </div>
			        </div>
			        <div style="padding-top:10px;display:none;" id="ttsDiv">
			        	<div>
			        		<textarea name="ttsContent" id="ttsContent" cols="55" rows="7"></textarea>
			        	</div>
			        	<div>
			        		<span id="ttsContentLengthNotice">还可以输入200字</span>
			        	</div>
			        </div>
				</td>
			</tr>
			
		</table>

</form>

<div id="addVoiceBtn" style="padding-left:20px;">
	<a href="#" id="saveVoiceBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveVoiceEdit()">保存</a>
	<a href="#" id="cancelVoiceBtn" class="easyui-linkbutton" iconCls="icon-cancel" onclick="voiceFormCancel()">取消</a>
</div>  
