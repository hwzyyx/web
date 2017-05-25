<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>Client Side Pagination in DataGrid - jQuery EasyUI Demo</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript"><!--

		var orgComboTreeData = eval('${orgComboTreeData}');
		var voiceTypeComboboxDataFor0 = eval('${voiceTypeComboboxDataFor0}');
		var voiceTypeComboboxDataFor1 = eval('${voiceTypeComboboxDataFor1}');
		var currCreateType = 'voiceFile';
	
		$(function(){

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
			
			$("#VOICE_TYPE").combobox({
				valueField:'id',
				textField:'text'
			}).combobox('loadData',voiceTypeComboboxDataFor0).combobox('setValue',1);

			$("#voiceType").combobox({
				valueField:'id',
				textField:'text'
			}).combobox('loadData',voiceTypeComboboxDataFor1).combobox('setValue','empty');
			
			//$("#startTime").datebox("setValue",getCurrMonthDay1());   //设置默认的开始时间
			$("#endTime").datebox("setValue",getCurrDate());          //设置默认的结束时间

			$("#orgCode").combotree('loadData',orgComboTreeData).combotree({

				onLoadSuccess:function(node,data) {

					//全选
					var t = $("#orgCode").combotree("tree");
					for(var i=0;i<data.length;i++) {
						node = t.tree("find",data[i].id);
						t.tree('check',node.target);
					}
	
					var selectRs = $("#orgCode").combotree('getValues');
					var voiceTypeRs = $("#voiceType").combobox('getValue');
					var orgCodes = selectRs.toString();
					var startTime = $("#startTime").datebox('getValue');
					var endTime = $("#endTime").datebox('getValue');
	
					$("#voiceDg").datagrid({
						pageSize:15,
						pagination:true,
						fit:true,
						toolbar:'#searchtool',
						singleSelect:true,
						rownumbers:true,
						rowrap:true,
						striped:true,
						pageList:[10,15,20],
						url:'voice/datagrid',
						queryParams:{
							voiceDesc:$("#voiceDesc").val(),
							voiceType:voiceTypeRs,
							orgCode:orgCodes,
							startTime:startTime,
							endTime:endTime
						},
						onLoadSuccess:function(data) {
							for(var i=0;i<data.rows.length;i++) {
								//window.parent.showMessage(data.rows[i].playerFunction);
								eval(data.rows[i].playerFunction);    //播放器设置语音
								
							}
						}
					});
										
				}
				
			});
			

			//弹窗关闭时，清空表单
			$("#voiceDlg").dialog({
				onClose:function() {
					$("#voiceForm").form('clear');

					//除了清空表单，还需要把创建类型复位，即是选择文件打开，TTS方式关闭
					$("#createType_voiceFile").linkbutton('select');  
					$("#voiceFileDiv").css('display','');
					$("#ttsDiv").css('display','none');
					currCreateType = 'voiceFile';
				}
			});
						
		});

		//得到当前日期
		function pad2(n) { return n < 10 ? '0' + n : n }
		function getCurrTimeToString() {    //以 yyyyMMddHHiiss 返回
		    var date = new Date();
		    return date.getFullYear().toString() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());
		}

		function LessThan(){
		    //获得textarea的maxlength属性
		    var MaxLength = 200;
		    var num=MaxLength-$("#ttsContent").val().length;  
		    if(num==MaxLength){
		           $('#ttsContentLengthNotice').attr('visi','yes').hide();
		    }else{
		           $('#ttsContentLengthNotice').attr('visi','yes').show();
		           $('#ttsContentLengthNotice').html("<font font-size='13px'>还能输入："+num+"字</font>");
		    }
		    //返回文本框字符个数是否符号要求的boolean值
		    return oTextArea.value.length < oTextArea.getAttribute("maxlength");
		}
		
		function voiceAdd() {

			$("#voiceFile").filebox({
				buttonText:'选择文件'
			});

			$("#VOICE_TYPE").combobox('setValue','1');

			$("#VOICE_DESC").textbox('setValue',getCurrTimeToString());
			
			$("#voiceDlg").dialog("setTitle","新增语音");
			$("#voiceDlg").dialog("open");

			
			
			$("#saveVoiceBtn").attr("onclick","saveVoiceAdd()");
		}

		function saveVoiceAdd() {

			//var f = $("#voiceFile").val();
			var f = $("#voiceFile").filebox("getValue");
			var vd = $("#VOICE_DESC").textbox("getValue");
			var ttsContent = $("#ttsContent").val();
			ttsContent = encodeURI(encodeURI(ttsContent));
			var urlInfo = 'voice/add';

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
				
				urlInfo = 'voice/addForTTS?ttsContent=' + ttsContent; 
				
			}
			
			if(vd==null || vd.length==0){
				$.messager.alert("警告","语音文件描述不能为空!","error");
				return;
			}

			
			
			$("#voiceForm").form("submit",{

				url:urlInfo,
				onSubmit:function() {
					$.messager.progress({
						msg:'系统正在处理，请稍候...',
						interval:3000
					});
					return $(this).form('validate');
				},
				success:function(data) {
					$.messager.progress('close');
					var result = JSON.parse(data); //解析Json数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {         //保存成功时
						$('#voiceDg').datagrid({url:'voice/datagrid'});
						$("#voiceDlg").dialog("close");
					}
					
				}
				
			});
			
		}

		function voiceDel(voiceId) {

			$.messager.confirm("提示","你确定要删除选中的记录吗?",function(r) {
				if(r) {
					$.ajax({
						type:'POST',
						dataType:'json',
						url:'voice/delete?voiceId=' + voiceId,
						success:function(rs) {
			
							var statusCode = rs.statusCode;   //返回的结果类型
							var message = rs.message;         //返回执行的信息
							
							window.parent.showMessage(message,statusCode);

							if(statusCode == 'success') {
								$("#voiceDg").datagrid({url:'voice/datagrid'});
							}
													
						}
					});
				}
			});
			
		}

		function voiceEdit(voiceId,voiceType,voiceDesc) {

			$("#voiceDlg").dialog("setTitle","编辑语音");

			$("#VOICE_ID").val(voiceId);
			$("#VOICE_DESC").textbox("setValue",voiceDesc);
			$("#VOICE_TYPE").combobox("setValue",voiceType);
			$("#saveVoiceBtn").attr("onclick","saveVoiceEdit()");

			$("#voiceFile").filebox({
				buttonText:'选择文件&nbsp;&nbsp;(注：语音编辑时语音文件可以为空!)'
			});
			
			$("#voiceDlg").dialog("open");

			
		}

		
		//更改时，可以只修改语音描述，语音可以不变：如果语音为空时，传送0为标识符上去；不为空时，传送1。
		function saveVoiceEdit() {

			var urlInfo = "voice/update";
			var flag = 1;

			//取得上传文件内容
			var f = $("#voiceFile").filebox("getValue");
			if(f==null || f.length==0){   //如果没有选择语音文件时，设置为0，则表示仅修改语音描述
				flag = 0;
			}

			var vd = $("#VOICE_DESC").textbox("getValue");
			var voiceId = $("#VOICE_ID").val();

			//取得TTS内容
			var ttsContent = $("#ttsContent").val();
			ttsContent = encodeURI(encodeURI(ttsContent));


			if(currCreateType == 'voiceFile') {       //如果修改方式为上传文件的方式

				urlInfo = 'voice/update?flag=' + flag;
				
			}else {                                   //如果修改方式为TTS生成语音文件

				//为了避免上传文件的框中有内容，在上传前，将文件框清空
				$("#voiceFile").filebox('clear');
				
				urlInfo = "voice/updateForTTS?ttsContent=" + ttsContent;
			}
							

			if(vd==null || vd.length==0) {
				$.messager.alert("警告","语音描述不能为空!","error");
				return;
			}

			if(voiceId==null || voiceId.length==0) {
				$.messager.alert("警告","语音ID不能为空!","error");
				return;
			}
			
			$("#voiceForm").form("submit",{

				url:urlInfo,
				onSubmit:function() {
					$.messager.progress({
						msg:'系统正在处理，请稍候...',
						interval:3000
					});
					return $(this).form('validate');
				},
				success:function(data) {

					$.messager.progress("close");

					var result = JSON.parse(data);

					var statusCode = result.statusCode;   //返回结果类型
					var message = result.message;         //返回执行的信息

					window.parent.showMessage(message,statusCode);

					if(statusCode == 'success') {
						$("#voiceDg").datagrid({url:'voice/datagrid'});
						$("#voiceDlg").dialog("close");
					}
				}
				
			});
			
		}

		//操作
		function rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:voiceEdit(\"" + data.VOICE_ID + "\",\"" + data.VOICE_TYPE + "\",\"" + data.VOICE_DESC + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:voiceDel(\"" + data.VOICE_ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
		}
		
		//试听
		function listenrowformatter(value,data,index) {
			return data.playerSkin;
		}

		//下载
		function downloadrowformatter(value,data,index) {
			return "<a href='voice/download?path=" + data.path + "' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='下载录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/download.png' style='margin-top:2px;' border='0'></a>";
		}

		
		function findData() {

			var selectRs = $("#orgCode").combotree('getValues');
			if(selectRs.length<1) {
				alert("查询时,组织不能为空!");
				return;
			}

			var orgCodes = selectRs.toString();
			var voiceTypeRs = $("#voiceType").combobox('getValue');
			var startTime = $("#startTime").datebox('getValue');
			var endTime = $("#endTime").datebox('getValue');
			
			$('#voiceDg').datagrid('load',{
				voiceDesc:$("#voiceDesc").val(),
				voiceType:voiceTypeRs,
				orgCode:orgCodes,
				startTime:startTime,
				endTime:endTime
			});
		}

		function voiceCancel() {
			$("#voiceForm").form("clear");
			$("#voiceDlg").dialog("close");
		}
		
	</script>
</head>
<body>
<%@ include file="/base_loading.jsp" %>
<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:70px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>语音描述：<input id="voiceDesc" type="text" class="easyui-textbox" style="width:150px;"/></td>
				
				<td style="padding-left:30px;">语音类型：<input id="voiceType" class="easyui-combobox" style="width:150px;"/></td>
				
				<td>
					<div style="padding-left:30px;">
						选择组织：<select id="orgCode" class="easyui-combotree" multiple style="width:200px;"></select>
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<div>
						创建时间：<input id="startTime" style="width:150px;" name="startTime" class="easyui-datebox" /><span style="padding-left:39px;padding-right:39px;">至</span> <input id="endTime" style="width:150px;" name="endTime" class="easyui-datebox" />
					</div>
				</td>
				<td>
					<div style="padding-left:90px;">
						<a href="javascript:findData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
					</div>
				</td>
			</tr>
		</table>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="voiceDg">
			<thead>
			
				<tr style="height:12px;">		
					<th data-options="field:'VOICE_DESC',width:300,align:'center'">语音描述</th>
					<th data-options="field:'FILE_NAME',width:150,align:'center'">文件名</th>
					<th data-options="field:'VOICE_TYPE_DESC',width:100,align:'center'">语音类型</th>
					<th data-options="field:'MIME_TYPE',width:50,align:'center'">格式</th>
					<th data-options="field:'CREATE_USERCODE_DESC',width:100,align:'center'">创建人</th>
					<th data-options="field:'ORG_CODE_DESC',width:150,align:'center'">部门(组织)名字</th>
					<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
					<th data-options="field:'listen',width:35,align:'center',formatter:listenrowformatter">试听</th>
					<th data-options="field:'download',width:50,align:'center',formatter:downloadrowformatter">下载</th>
					<th data-options="field:'id',width:100,align:'center',formatter:rowformatter">操作</th>
				</tr>
				
			</thead>
		</table>	
	</div>
</div>


<div id="searchtool" style="padding:5px;">
	<div>
		<a href="#" id="easyui-add" onclick="voiceAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增语音</a>
	</div>
</div>

<div id="voiceDlg" class="easyui-dialog" style="width:700px;height:400px;padding:5px;" modal="true" closed="true" buttons="#addVoiceBtn">
		<!-- 包含语音信息的表单 -->
		<%@ include file="/autocall/voice/_form.jsp" %>
</div>

	<!-- 将播放录音的窗口包含进来，在点击试听时，可以显示该窗口并播放录音 -->
	<!-- %@ include file="/_playpanel.jsp"% -->
</body>
</html>

