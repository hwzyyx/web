<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<table id="formTable" border="0" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" width="680"
					  style="border-collapse: collapse;">
	<tr style="padding-top: 10px;">
		<td style="width:120px;text-align:center;">
			语音内容
		</td>
		<td>
			<input type="hidden" name="cnn_voice.ID" id="ID"/>
			<input name="cnn_voice.VOICE_DESC" id="VOICE_DESC" style="width:300px;height:100px;" class="easyui-textbox" data-options="multiline:true"  required="true" missingMessage="语音内容不能为空!"></input>
		</td>
	</tr>
	<tr>
		<td style="text-align:center;vertical-align: top;">
			<div style="padding-top:10px;">语音类型</div>
		</td>
		<td>
			<div style="padding-top:10px;">
				<select class="easyui-combobox" name="cnn_voice.FLAG" id="FLAG" style="width:150px;" data-options="panelHeight:'auto'">
						<option value="1">中文</option>
						<option value="2">英文</option>
				</select>
	        </div>
		</td>
	</tr>
	<tr>
		<td style="text-align:center;vertical-align: top;">
			<div style="padding-top:0px;">
				
				<div style="padding-top:10px;">
					<a href="#" class="easyui-linkbutton" id="createType_voiceFile" data-options="toggle:true,group:'g1',selected:true">创建方式：文件</a>
				</div>
				<div style="padding-top:5px;">
					<a href="#" class="easyui-linkbutton" id="createType_tts" data-options="toggle:true,group:'g1'">创建方式：TTS</a>
				</div>
			</div>
		</td>
		<td>
			<div style="padding-top:10px;" id="voiceFileDiv">
				<div>
					<input class="easyui-filebox" id="voiceFile" name="voiceFile" data-options="prompt:'选择一个语音文件进行上传'" style="width:400px;">
				</div>
				<div style="padding-left:10px;padding-top:10px;padding-bottom:15px;">
					<span style="color: red;">** 注意：语音格式限定为 wav</span>
		        </div>
	        </div>
	        <div style="padding-top:10px;display:none;" id="ttsDiv">
	        	<div>
	        		<!-- input id="ttsContent" class="easyui-textbox" data-options="multiline:true,required:true,validType:{length:[0,200]}" 
	        		style="width:300px;height:100px" onkeyup="LessThan()" -->
	        		<textarea name="ttsContent" id="ttsContent" cols="55" rows="7"></textarea>
	        	</div>
	        	<div>
	        		<span id="ttsContentLengthNotice">还可以输入200字</span>
	        	</div>
	        </div>
		</td>
	</tr>
	
</table>

<div id="formBtn">
	<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="saveAdd()">保存</a>
	<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="doCancel()">取消</a>
</div>
