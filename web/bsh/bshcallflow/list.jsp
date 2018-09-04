<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
<title>博世呼叫流程</title>
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
	<script src="iconfont/iconfont.js"></script>
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    
    <script type="text/javascript">
  		var voiceTypeArr = new Array("开场","确认安装","暂不安装","延后安装","已经预约","错误回复","日期","产品");  
    	var currCreateType = 'voiceFile';
    	var currentSelectRowData = null;
    	var currVoiceType = null;
    	var currVoiceTypeDesc = null;
    
  
    	
    	$(function(){
    		
    		$("#bshVoiceDg").datagrid({
    			pageSize:50,
    			pagination:true,
    			fit:true,
    			rowrap:true,
    			striped:true,
    			singleSelect:true,
    			rownumbers:true,
    			pageList:[10,30,50],
    			url:'bshVoice/datagrid',
    			toolbar:'#bshVoiceDgTool',
    			queryParams:{
    				voiceType:$('#voiceTypeCombobox').combobox('getValue')
    			},
    			onSelect:function(rowIndex,rowData) {
    				
    				currentSelectRowData=rowData;
    				
    				voiceTypeRs = rowData.VOICE_TYPE;
    				voiceIndexRs = rowData.VOICE_INDEX;
    				voiceDescRs = rowData.VOICE_DESC;
    				
    				
    				//$("#voiceType" + voiceTypeRs + "-" + voiceIndex).css('font-size',20);
    				$("#voiceType"+voiceTypeRs + "-" + voiceIndexRs).text(voiceDescRs);
    				$("#voiceType"+voiceTypeRs + "-" + voiceIndexRs).css({"color":"red","font-weight":"bold"});
    				
    				
    			},
    			onUnselectAll:function(rows) {
    				if(currentSelectRowData != null) {
	    				voiceTypeRs = currentSelectRowData.VOICE_TYPE;
	    				voiceIndexRs = currentSelectRowData.VOICE_INDEX;
	    				$("#voiceType"+voiceTypeRs + "-" + voiceIndexRs).css({"color":"black","font-weight":"normal"});
    				}
    			},
    			onLoadSuccess:function(data) {    //加载成功后，需要加入该代码，才可以点击试听
					for(var i=0;i<data.rows.length;i++) {
						//window.parent.showMessage(data.rows[i].playerFunction);
						eval(data.rows[i].playerFunction);    //播放器设置语音
						
					}
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
				//$("#voiceFileDiv").css('display','none');
				//$("#ttsDiv").css('display','');
				//currCreateType = 'tts';
			});
    		
    		$("#bshVoiceDlg").dialog({
    			onClose:function() {
    				
	    			$('#bshVoiceDg').datagrid('loadData',{total:0,rows:[]});
    				
    				if(currentSelectRowData!=null) {
	    				voiceTypeRs = currentSelectRowData.VOICE_TYPE;
	    				voiceIndexRs = currentSelectRowData.VOICE_INDEX;
	    				
	    				$("#voiceType"+voiceTypeRs + "-" + voiceIndexRs).css({"color":"black","font-weight":"normal"});
    				}
    			}
    		});
    		
    		$("#bshVoiceFormDlg").dialog({
    			onClose:function() {
    				$("#bshVoiceForm").form('clear');
    			}
    		});
    		
    	});
    	
    	function voiceManager(vT) {
    		
    		currVoiceType = vT;
    		currVoiceTypeDesc = voiceTypeArr[vT];
    		$("#addVoiceBtnId").linkbutton({text:'增加  ' + currVoiceTypeDesc + " 语音"});
    		
    		$('#voiceTypeCombobox').combobox('setValue',vT);
    		//alert(vT + $('#voiceTypeCombobox').combobox('getValue'));
    		$("#bshVoiceDlg").dialog('setTitle','语音管理').dialog('open');
    		
    		$("#bshVoiceDg").datagrid("reload",{
    			voiceType:$('#voiceTypeCombobox').combobox('getValue')
    		});
    		
    	}
    	
    	//操作：编辑，删除
		function rowformatter(value,data,index) {
			/*"<a href='#' onclick='javascript:voiceEdit(\"" + data.VOICE_ID + "\",\"" + data.VOICE_TYPE + "\",\"" + data.VOICE_DESC + "\")'><img src='themes/icons/pencil.png' border='0'>更换语音</a>" + 
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' onclick='javascript:voiceDel(\"" + data.VOICE_ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>";
			*/
			return "<a href='#' onclick='javascript:voiceEdit(\"" + data.VOICE_ID + "\",\"" + data.VOICE_TYPE + "\",\"" + data.VOICE_DESC + "\",\"" + data.VOICE_NAME + "\")'><img src='themes/icons/pencil.png' border='0'>更换语音</a>" + 
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' onclick='javascript:voiceDel(\"" + data.VOICE_ID +"\")'><img src='themes/icons/clear.png' border='0'>删除</a>";
		}
    	
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
    		
    		$("#voiceTypeDescId").linkbutton({text:currVoiceTypeDesc + ' 语音'});
    		$("#bshVoiceFormDlg").dialog('setTitle','增加  ' + currVoiceTypeDesc + ' 语音').dialog('open');
    		
    		$("#saveVoiceBtn").attr("onclick","saveVoiceAdd()");
    	}
    	
    	function saveVoiceAdd() {
    		
    		var urlInfo = "bshVoice/add";
    		var flag = 1;      //何种上传语音方式：1:语音文件；2：tts
    		
    		//取得上传文件内容
    		var f = $("#voiceFile").filebox('getValue');
    		if(f==null || f.length==0) {    //如果没有选择语音文件时，设置为0，则表示仅修改语音描述
    			$.messager.alert("警告","语音文件不能为空!","error");
    			return;
    		}
    		
    		var vd = $("#VOICE_DESC").textbox('getValue');     //语音描述
    		var vn = $("#VOICE_NAME").textbox('getValue');     //语音命名
    		
    		//取得TTS内容
			var ttsContent = $("#ttsContent").val();
    		var ttsContent = encodeURI(encodeURI(ttsContent));
    		
    		if(currCreateType == 'voiceFile') {       //如果修改方式为上传文件的方式

				urlInfo = 'bshVoice/add?flag=' + flag + "&voiceType=" + currVoiceType;
				
			}else {                                   //如果修改方式为TTS生成语音文件

				//为了避免上传文件的框中有内容，在上传前，将文件框清空
				$("#voiceFile").filebox('clear');
				
				urlInfo = "bshVoice/updateForTTS?ttsContent=" + ttsContent;
			}
    		
    		if(vd==null || vd.length==0) {
				$.messager.alert("警告","语音描述不能为空!","error");
				return;
			}
    		
    		if(vn==null || vn.length==0) {
				$.messager.alert("警告","语音命名不能为空!","error");
				return;
			}
    		
    		$("#bshVoiceForm").form("submit",{

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
						$("#bshVoiceDg").datagrid("reload",{
			    			voiceType:$('#voiceTypeCombobox').combobox('getValue')
			    		});
						$("#bshVoiceFormDlg").dialog("close");
					}
				}
				
			});
    		
    	}
    	
		function voiceEdit(voiceId,voiceType,voiceDesc,voiceName) {
    		
    		$("#bshVoiceFormDlg").dialog('setTitle','更换  ' + currVoiceTypeDesc + ' 语音').dialog('open');
    		
    		$("#VOICE_ID").val(voiceId);
    		$("#VOICE_DESC").textbox('setValue',voiceDesc);
    		$("#VOICE_NAME").textbox('setValue',voiceName);
    		$("#VOICE_TYPE").combobox('setValue',$("#voiceTypeCombobox").combobox('getValue'));
    		$("#saveVoiceBtn").attr("onclick","saveVoiceEdit()");
    		
    		$("#voiceFile").filebox({
    			buttonText:'选择文件&nbsp;&nbsp;(注：语音编辑时语音文件可以为空!)'
    		});
    		
    	}
    	
    	//更改时，可以只修改语音描述，语音可以不变：如果语音为空时，传送0为标识符上去；不为空时，传送1。
		function saveVoiceEdit() {
			var urlInfo = "bshVoice/update";
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

				urlInfo = 'bshVoice/update?flag=' + flag;
				
			}else {                                   //如果修改方式为TTS生成语音文件

				//为了避免上传文件的框中有内容，在上传前，将文件框清空
				$("#voiceFile").filebox('clear');
				
				urlInfo = "bshVoice/updateForTTS?ttsContent=" + ttsContent;
			}
							
			if(vd==null || vd.length==0) {
				$.messager.alert("警告","语音描述不能为空!","error");
				return;
			}

			if(voiceId==null || voiceId.length==0) {
				$.messager.alert("警告","语音ID不能为空!","error");
				return;
			}
			$("#bshVoiceForm").form("submit",{

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
						$("#bshVoiceDg").datagrid("reload",{
			    			voiceType:$('#voiceTypeCombobox').combobox('getValue')
			    		});
						$("#bshVoiceFormDlg").dialog("close");
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
						url:'bshVoice/delete?voiceId=' + voiceId,
						success:function(rs) {
			
							var statusCode = rs.statusCode;   //返回的结果类型
							var message = rs.message;         //返回执行的信息
							
							window.parent.showMessage(message,statusCode);

							if(statusCode == 'success') {
								$("#bshVoiceDg").datagrid({url:'bshVoice/datagrid'});
							}
													
						}
					});
				}
			});
			
		}
    	
    	//试听
    	function listenrowformatter(value,data,index) {
    		if(data.path==null || ""==data.path) {
    			return "<a href='#' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='文件不存在' style='width:100px;padding:5px;float:top;'><img src='themes/icons/no.png' style='margin-top:2px;' border='0'></a>";
    		}else {
	    		return data.playerSkin;
    		}
    	}
    	
    	//下载
    	function downloadrowformatter(value,data,index) {
    		if(data.path==null || ""==data.path) {
    			return "<a href='#' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='文件不存在' style='width:100px;padding:5px;float:top;'><img src='themes/icons/no.png' style='margin-top:2px;' border='0'></a>";
    		}else {
	    		return "<a href='voice/download?path=" + data.path + "' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='下载录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/download.png' style='margin-top:2px;' border='0'></a>";
    		}
    	}
    	
    	function voiceCancel() {
			$("#bshVoiceForm").form("clear");
			$("#bshVoiceDlg").dialog("close");
		}
    	
    	function voiceFormCancel() {
    		$("#bshVoiceFormDlg").dialog('close');
    	}
    
    </script>
</head>
<body>

<!-- 页面加载效果 -->
<%@ include file="/base_loading.jsp" %>

<div style="display: none;">
	<select id="voiceTypeCombobox" class="easyui-combobox" style="width:150px;">
		<option value="0">开场语音</option>
		<option value="1">确认安装</option>
		<option value="2">暂不安装</option>
		<option value="3">延后安装</option>
		<option value="4">已经预约</option>
		<option value="5">错误回复</option>
		<option value="6">日期语音</option>
		<option value="7">产品语音</option>
	</select>
</div>
<div id="callFlowPanel" class="easyui-panel" title="呼叫流程图" style="width:1250px;height:700px;padding:10px;background:url('themes/icons/bsh_callflow.png') no-repeat;">
	<div style="background-color: #fffff;height:128px;width:640px;margin-top:52px;margin-left:216px;position: absolute;">
		<span>开场1：您好，这里是(西门子/博世)家电客服中心，来电跟您确认(洗衣机/XXX)的安装日期。根据(京东/苏宁/国美/天猫)平台传来的信息，我们将于(明天/12月10号)上门安装。确认请按1，暂不安装请按2，如需改约到后面3天，请按3,如果您已经提前预约好服务，请按4。</span>
		<span><br><br>开场2：您好，这里是(西门子/博世)家电客服中心。您在国美选购的(洗衣机/XXX)将于(明天/12月10号)送货，我们将于送货当天上门安装，需要您进一步确认。确认送货当天安装请按1，暂不安装请按2，如需改约到后面3天请按3,如果您已经提前预约好服务,请按4。</span>
	</div>
	<div style="height:50px;width:50px;margin-top:150px;margin-left:820px;position: absolute;font-size:15px;">
		<i class="iconfont icon-green" id="welcomeVoiceIcon" onclick="voiceManager(0)" style="color:#0076ff;font-size:30px;">&#xe601;</i>
	</div>
	
	<div style="background-color: #fffff;height:140px;width:185px;margin-top:418px;margin-left:5px;position: absolute;">
		<span>对不起，输入有误。我们可能会再次和您联系，再见。</span>
	</div>
	<div style="height:50px;width:50px;margin-top:525px;margin-left:155px;position: absolute;font-size:15px;">
		<i class="iconfont icon-green" id="wrongRespondIcon" onclick="voiceManager(5)" style="color:#0076ff;font-size:30px;">&#xe601;</i>
	</div>
	
	<div style="background-color: #fffff;height:140px;width:395px;margin-top:418px;margin-left:205px;position: absolute;">
		<span>场景1：京东、苏宁、天猫<br>您的机器安装日期已确认为12月10号，工程师最迟会在当天早上9:30之前与您联系具体上门时间。感谢您的配合，再见。<br></span>
		<br>
		<span>场景2：国美<br>您的机器安装日期已确认为12月10号，工程师最迟会在当天早上9:30之前与您联系具体上门时间。为确保您的权益，请认准(西门子/博世)厂家的专业工程师。感谢您的配合，再见。</span>
	</div>
	<div style="height:50px;width:50px;margin-top:525px;margin-left:560px;position: absolute;font-size:15px;">
		<i class="iconfont icon-green" id="comfirmInstallIcon" onclick="voiceManager(1)" style="color:#0076ff;font-size:30px;">&#xe601;</i>
	</div>
	
	<div style="background-color: #fffff;height:140px;width:185px;margin-top:418px;margin-left:620px;position: absolute;font-size: 15px;">
		<span>您的机器，暂时将不会安排上门安装。如后期仍有需要，欢迎您拨打(4008899999/4008855888)，或者关注“西门子家电/博世家电”微信公众号预约。感谢您的配合，再见。</span>
	</div>
	<div style="height:50px;width:50px;margin-top:525px;margin-left:770px;position: absolute;font-size:15px;">
		<i class="iconfont icon-green" id="notInstallIcon" onclick="voiceManager(2)" style="color:#0076ff;font-size:30px;">&#xe601;</i>
	</div>
	
	<div style="background-color: #fffff;height:140px;width:185px;margin-top:418px;margin-left:825px;position: absolute;">
		<span>稍后您会收到1条确认短信，请您按短信提示，直接回复数字即可。感谢您的配合，再见。</span>
	</div>
	<div style="height:50px;width:50px;margin-top:525px;margin-left:979px;position: absolute;font-size:15px;">
		<i class="iconfont icon-green" id="delayInstallIcon" onclick="voiceManager(3)" style="color:#0076ff;font-size:30px;">&#xe601;</i>
	</div>
	
	<div style="background-color: #fffff;height:140px;width:185px;margin-top:418px;margin-left:1030px;position: absolute;">
		<span>我们会按您提前预约好的日期上门。 感谢您选购博世家电，再见。</span>
	</div>
	<div style="height:50px;width:50px;margin-top:525px;margin-left:1180px;position: absolute;font-size:15px;">
		<i class="iconfont icon-green" id="delayInstallIcon" onclick="voiceManager(4)" style="color:#0076ff;font-size:30px;">&#xe601;</i>
	</div>
	
	<div style="background-color: #fffff;height:140px;width:185px;margin-top:200px;margin-left:1290px;position: absolute;">
		<span class="font17">日期语音</span>
	</div>
	<div style="height:50px;width:50px;margin-top:193px;margin-left:1250px;position: absolute;font-size:15px;">
		<i class="iconfont icon-green" id="dateIcon" onclick="voiceManager(6)" style="color:#0076ff;font-size:30px;">&#xe601;</i>
	</div>
	
	<div style="background-color: #fffff;height:140px;width:185px;margin-top:260px;margin-left:1290px;position: absolute;">
		<span class="font17">产品语音</span>
	</div>
	<div style="height:50px;width:50px;margin-top:253px;margin-left:1250px;position: absolute;font-size:15px;">
		<i class="iconfont icon-green" id="productIcon" onclick="voiceManager(7)" style="color:#0076ff;font-size:30px;">&#xe601;</i>
	</div>
	
</div>

<div id="bshVoiceDlg" class="easyui-dialog" style="width:1200px;height:400px;padding:5px;" modal="false" closed="true">

		<table class="easyui-datagrid" id="bshVoiceDg">
		    <thead>
				<tr>
					<th data-options="field:'VOICE_DESC',width:400,align:'right'">语音描述</th>
					<th data-options="field:'VOICE_NAME',width:150,align:'center'">语音命名</th>
					<th data-options="field:'VOICE_TYPE_DESC',width:100,align:'center'">语音类型</th>
					<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
					<th data-options="field:'listen',width:40,align:'center',formatter:listenrowformatter">试听</th>
					<th data-options="field:'download',width:50,align:'center',formatter:downloadrowformatter">下载</th>
					<th data-options="field:'edit',width:150,align:'center',formatter:rowformatter">编辑</th>
				</tr>
		    </thead>
		</table>
</div>	

<div id="bshVoiceFormDlg" class="easyui-dialog" style="width:700px;height:400px;padding:5px;" modal="true" closed="true" buttons="#addVoiceBtn">
		<!-- 包含语音信息的表单 -->
		<%@ include file="/bsh/bshcallflow/_form.jsp" %>
</div>

<div id="bshVoiceDgTool" style="padding:5px;">
	<a href="#" id="addVoiceBtnId" onclick="voiceAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">增加语音</a>
</div>
	
</body>
</html>