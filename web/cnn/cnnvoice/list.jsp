<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>改号通知语音列表</title>
		<style>
			.font17{
				font-size: 17px;
			}
		</style>
		<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
		<link rel="stylesheet" type="text/css" href="themes/color.css">
		<link rel="stylesheet" type="text/css" href="themes/icon.css">
		<link rel="stylesheet" type="text/css" href="demo.css">
		<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
		<link rel="stylesheet" type="text/css" href="iconfont/iconfont.css">
		<script src="echarts/echarts.min.js"></script>
		<script src="iconfont/iconfont.js"></script>
		<script type="text/javascript" src="jquery.min.js"></script>
		<script type="text/javascript" src="jquery.easyui.min.js"></script>
		<script type="text/javascript" src="js.date.utils.js"></script>
		<script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
		<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
		<script type="text/javascript">
		
			var currCreateType = 'voiceFile';
			$(function(){
				
				$("#voiceFile").filebox({
					buttonText:'选择文件'
				});
				
				$("#cnn_voice_Dg").datagrid({
					pageSize:30,
					pagination:true,
					fit:true,
					toolbar:"#datagridTool",
					singleSelect:true,
					rownumbers:true,
					rowrap:true,
					striped:true,
					pageList:[20,30,50],
					url:'cnnVoice/datagrid',
					queryParams:{
						voiceDesc:$("#voiceDesc").textbox('getValue'),
						flag:$("#flag").combobox('getValue')
					},
					onLoadSuccess:function(data) {
						for(var i=0;i<data.rows.length;i++) {
							eval(data.rows[i].playerFunction);    //播放器设置语音
							
						}
					}
				})
				$("#cnn_voice_Dlg").dialog({
					onClose:function() {
						$("#cnn_voice_Form").form('clear');
					}
				});
				
				$('#ttsContent').keyup(function(){
					//alert("输入了一次");
					var len = $(this).val().length;

					if(len>199) {
						$(this).val($(this).val().substring(0,200));
					}

					var lessNum = 200 - len;

					if(lessNum<0){lessNum=0;}
					
					$("#ttsContentLengthNotice").html("还能输入 " + lessNum + " 个字");
					
				});
				
				$("#createType_voiceFile").bind('click',function(){
					$("#voiceFileDiv").css('display','');
					$("#ttsDiv").css('display','none');
					currCreateType = 'voiceFile';
				});
				$("#createType_tts").bind('click',function(){
					$("#voiceFileDiv").css('display','none');
					$("#ttsDiv").css('display','');
					currCreateType = 'tts';
				});
				
			});

			//查询数据
			function findData() {
				$("#cnn_voice_Dg").datagrid('load',{
					voiceDesc:$("#voiceDesc").textbox('getValue'),
					flag:$("#flag").combobox('getValue')
				});
			}
			//编辑的超连接拼接
			function rowformatter(value,data,index) {
				return "<a href='#' onclick='javascript:doEdit(\"" + data.ID + "\",\"" + data.VOICE_DESC + "\",\"" + data.FLAG + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>	<a href='#' onclick='javascript:doDel(\"" + data.ID +"\")'><img src='themes/icons/cancel.png' border='0'>删除</a>";
			}
			
			function flagformatter(value,data,index) {
				if(value=="1") {
					return "<span style='color:green;'>中文</span>";
				}else {
					return "<span style='color:red;'>英文</span>";
				}
			}
			
			//试听
			function listenrowformatter(value,data,index) {
				return data.playerSkin;
			}

			//删除操作
			function doDel(id) {
				$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
					if(r) {
						$("#cnn_voice_Form").form('submit',{
							url:"cnnVoice/delete?id=" + id,
							onSubmit:function(){
							},
							success:function(data) {
								var result = JSON.parse(data);    //解析Json 数据
								var statusCode = result.statusCode; //返回的结果类型
								var message = result.message;       //返回执行的信息

								window.parent.showMessage(message,statusCode);
								if(statusCode == 'success') {         //保存成功时
									findData();
								}
							}
						});
					}
				});
			}

			//编辑操作
			function doEdit(id,voiceDesc,flag){
				$("#saveBtn").attr("onclick","saveEdit()");
				$("#cnn_voice_Dlg").dialog("open").dialog("setTitle","编辑");
				$("#cnn_voice_Form").form('load',{
					'cnn_voice.ID':id,
					'cnn_voice.VOICE_DESC':voiceDesc,
					'cnn_voice.FLAG':flag
				});
			}
			//编辑操作
			/*function saveEdit() {
				$("#cnn_voice_Form").form('submit',{
					url:"cnnVoice/update",
					onSubmit:function(){
						var v = $(this).form('validate');
						if(v) {
							$.messager.progress({
								msg:'系统正在处理，请稍候...',
								interval:3000
							});
						}
						return $(this).form('validate');
					},
					success:function(data) {
						$.messager.progress("close");
						var result = JSON.parse(data);    //解析Json 数据
						var statusCode = result.statusCode; //返回的结果类型
						var message = result.message;       //返回执行的信息
						window.parent.showMessage(message,statusCode);
						if(statusCode == 'success') {         //保存成功时
							findData();
							$('#cnn_voice_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}*/
			
			//编辑操作
			function saveEdit() {
				
				var urlInfo = 'cnnVoice/update';
				var fType = 1;
				
				//取得上传文件内容
				var f = $("#voiceFile").filebox("getValue");
				if(f==null || f.length==0) {     //如果没有选择语音文件 时，设置为0，则表示仅修改语音描述
					fType = 0;
				}
				
				var vd = $("#VOICE_DESC").textbox('getValue');
				var id = $("#ID").val();
				var ttsContent = $("#ttsContent").val();
				ttsContent = encodeURI(encodeURI(ttsContent));
				
				
				if(currCreateType=='voiceFile') {         //如果修改方式为上传文件的方式
					urlInfo = 'cnnVoice/update?fType=' + fType;
				}else {                                   //修改修改方式为TTS生成语音文件
					
					//为了避免上传文件的框中有内容,在上传前，将文件框清空
					$("#voiceFile").filebox('clear');
					urlInfo = "cnnVoice/updateForTTS?ttsContent=" + ttsContent;
				}
				
				if(vd==null || vd.length==0) {
					$.messager.alert("警告","语音内容不能为空!","error");
					return;
				}
				
				if(id==null || id.length==0) {
					$.messager.alert("警告","语音ID不能为空!","error");
					return;
				
				}
				
				$("#cnn_voice_Form").form("submit",{
					url:urlInfo,
					onSubmit:function() {
						var v = $(this).form('validate');
						if(v) {
							$.messager.progress({
								msg:'系统正在处理，请稍候...',
								interval:3000
							});
						}
						return $(this).form('validate');
					},
					success:function(data) {
						$.messager.progress("close");
						var result = JSON.parse(data);    //解析Json 数据
						var statusCode = result.statusCode; //返回的结果类型
						var message = result.message;       //返回执行的信息
						window.parent.showMessage(message,statusCode);
						if(statusCode == 'success') {         //保存成功时
							findData();
							$('#cnn_voice_Dlg').dialog('close');//关闭对话框
						}
					}
				});
				
				
			}

			function doAdd() {
				
				$("#FLAG").combobox('setValue',"1");
				
				$("#saveBtn").attr("onclick","saveAdd()");
				$("#cnn_voice_Dlg").dialog("setTitle","添加").dialog("open");
			}

			function saveAdd() {
				
				var f = $("#voiceFile").filebox("getValue");
				var vd = $("#VOICE_DESC").textbox('getValue');
				var ttsContent = $("#ttsContent").val();
				ttsContent = encodeURI(encodeURI(ttsContent));
				
				var urlInfo = 'cnnVoice/add';
				
				if(currCreateType == 'voiceFile') {
					
					if(f==null || f.length==0){
						$.messager.alert("警告","请选择语音文件,再执行上传!","error");
						return;
					}
				}else {
					
					if(ttsContent==null || ttsContent=='') {
						$.messager.alert("警告","创建类型为TTS方式,TTS内容为空!","error");
						return;
					}

					//为了避免上传文件的框中有内容，在上传前，将文件框清空
					$("#voiceFile").filebox('clear');
					
					urlInfo = 'cnnVoice/addForTTS?ttsContent=' + ttsContent; 
					
				}
				
				
				if(vd==null || vd.length==0){
					$.messager.alert("警告","语音文件描述不能为空!","error");
					return;
				}
				
				$("#cnn_voice_Form").form("submit",{
					url:urlInfo,
					onSubmit:function() {
						var v = $(this).form('validate');
						if(v) {
							$.messager.progress({
								msg:'系统正在处理，请稍候...',
								interval:3000
							});
						}
						return $(this).form('validate');
					},
					success:function(data) {
						$.messager.progress("close");
						var result = JSON.parse(data);    //解析Json 数据
						var statusCode = result.statusCode; //返回的结果类型
						var message = result.message;       //返回执行的信息
						window.parent.showMessage(message,statusCode);
						if(statusCode == 'success') {         //保存成功时
							findData();
							$('#cnn_voice_Dlg').dialog('close');//关闭对话框
						}
					}
				});
			}

			function doCancel(){
				$('#cnn_voice_Dlg').dialog('close');//关闭对话框
			}
		</script>
	</head>
<body>

	<%@ include file="/base_loading.jsp" %>
	<!-- 定义一个 layout -->
	<div data-options="fit:true" class="easyui-layout">
		<!-- 顶部查询区 -->
		<div data-options="region:'north',split:true,border:true" style="height:50px;padding-top:5px;padding-left:5px;">
			<table>
				<tr style="vertical-align: top;">
					<td>
						语音内容：<input id="voiceDesc" class="easyui-textbox" style="width:200px;"/>
						<span style="padding-left:20px;">
							（中/英）标识：
							<select class="easyui-combobox" id="flag" name="flag" style="width:80px;" data-options="panelHeight:'auto'">
									<option value="empty">请选择</option>
									<option value="1">中文</option>
									<option value="2">英文</option>
							</select>
						</span>
						<span style="padding-left:20px;">
							<a href="javascript:findData()" style="width:120px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
						</span>
					</td>
				</tr>
			</table>
		</div>
		<!-- 数据显示区 -->
		<div data-options="region:'center',split:true,border:false">
			<table id="cnn_voice_Dg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'VOICE_DESC',width:800,align:'left'">语音内容</th>
						<th data-options="field:'FLAG',width:200,align:'center',formatter:flagformatter">（中/英）标识</th>
						<th data-options="field:'CREATE_USERCODE_DESC',width:200,align:'center'">创建人</th>
						<th data-options="field:'CREATE_TIME',width:200,align:'center'">创建时间</th>
						<th data-options="field:'listen',width:40,align:'center',formatter:listenrowformatter">试听</th>
						<th data-options="field:'rowColumn',width:150,align:'center',formatter:rowformatter">操作</th>
					</tr>
				</thead>
			</table>
		</div>
	</div>
	<div id="datagridTool" style="padding:5px;">
		<a href="#" id="easyui-add" onclick="doAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增</a>
	</div>

	<div id="cnn_voice_Dlg" class="easyui-dialog" style="width:50%;height:50%;padding:10px 20px;" modal="true" closed="true" buttons="#formBtn">
		<form id="cnn_voice_Form" method="post" enctype="multipart/form-data">
			<!-- 包含表单 -->
			<%@ include file="/cnn/cnnvoice/_form.jsp"%>
		</form>
	</div>

</body>
</html>
