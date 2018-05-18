<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>自动接触记录</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.min.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.min.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		var currAutoContactId = '';
	
		$(function() {

			$("#status").combobox('setValue','5');
			
			$("#autoContactRecordDg").datagrid({
				pageSize:15,
				pagination:true,
				fit:true,
				toolbar:'#searchtool',
				singleSelect:true,
				rowsnumber:true,
				rowrap:true,
				striped:true,
				pageList:[10,15,20],
				url:'autoContactRecord/datagrid',
				queryParams:{
					status:$('#status').combobox('getValue')
				}
			});

			
			
		});

		function statusformatter(value,data,index) {
			if(value=='0') {
				return "<span style='color:black'>新记录(未外呼)</span>";
			}else if(value=='1') {
				return "<span style='color:yellow'>已载入</span>";
			}else if(value=='2') {
				return "<span style='color:green'>接触成功</span>";
			}else if(value=='3') {
				return "<span style='color:red'>接触失败</span>";
			}else if(value=='4') {
				return "<span style='color:gray'>超时</span>";
			}
		}

		function findData() {

			$('#autoContactRecordDg').datagrid('load',{
				agentNumber:$('#agentNumber').numberbox('getValue'),
				clientNumber:$('#clientNumber').numberbox('getValue'),
				identifier:$('#identifier').textbox('getValue'),
				callerId:$('#callerId').numberbox('getValue'),
				status:$('#status').combobox('getValue'),
				startTime:$('#startTime').datebox('getValue'),
				endTime:$('#endTime').datebox('getValue')
			});
			
		}

		function rowformatter(value,data,index) {
			if(!data.STATUS=='2') {
				return "";
			}
			
			if(data.recordingfile==null || data.recordingfile=="") {
				return "";
			}
			
			return "<a href='#' style='text-decoration:none' onclick='javascript:listenFile(\"" + data.path + "\",\"" + data.recordingfile + "\")'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='试听录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/listen.png' style='margin-top:2px;' border='0'></a>&nbsp;&nbsp;" +
			"<a href='autoContactRecord/download?path=" + data.path + "&file=" + data.recordingfile + "' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='下载录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/download.png' style='margin-top:2px;' border='0'></a>";
		}

		function listenFile(path,recordingfile) {
			//alert("path:" + path);
			//alert("recordingfile:" + recordingfile);
			var file = path + "/" + recordingfile;
			$("#playvoicepanel").dialog("open");
			
			$("#jquery_jplayer_1").jPlayer({
				ready: function (event) {
					$(this).jPlayer("setMedia", {
						title: "文件名：" + recordingfile,
						wav:file
					});
				},
				swfPath: "jplayer/dist/jplayer",
				//supplied: "wav,m4a,oga,mp3",
				supplied: "wav",
				wmode: "window",
				useStateClassSkin: true,
				autoBlur: false,
				autoPlay: true,
				smoothPlayBar: true,
				keyEnabled: true,
				remainingDuration: true,
				toggleDuration: true
			});
			$("#jquery_jplayer_1").jPlayer("setMedia", {
				title: "文件名：" + recordingfile,
				wav:file
			});
		}
		
	</script>
</head>
<body>
<!-- 页面加载效果 -->
<%@ include file="/base_loading.jsp" %>

<table id="autoContactRecordDg">
	<thead>
	
		<tr style="height:12px;">		
			<th data-options="field:'AGENT_NUMBER',width:150,align:'center'">座席号码</th>
			<th data-options="field:'CLIENT_NUMBER',width:150,align:'center'">服务号码</th>
			<th data-options="field:'IDENTIFIER',width:150,align:'center'">识别符</th>
			<th data-options="field:'CREATE_TIME',width:150,align:'center'">创建时间</th>
			<th data-options="field:'CALLERID',width:150,align:'center'">主叫号码</th>
			<th data-options="field:'CONTACT_TIME',width:150,align:'center'">接触时间</th>
			<th data-options="field:'STATUS',width:150,align:'center',formatter:statusformatter">状态</th>
			<!-- th data-options="field:'id',width:150,align:'center',formatter:rowformatter">状态</th -->
		</tr>
		
	</thead>
</table>

<div id="searchtool" style="padding:5px;">
	
	<table>
		<tr style="vertical-align: top;">
			<td>
				座席号码：<input id="agentNumber" name="agentNumber" class="easyui-textbox" type="text" />
			</td>
			<td>
				<div style="padding-left:30px;">
					服务号码：<input id="clientNumber" class="easyui-numberbox" name="clientNumber" type="text" />
				</div>
			</td>
			<td>
				<div style="padding-left:30px;">
					识别符：<input id="identifier" class="easyui-textbox" name="identifier" type="text" />
				</div>
			</td>
			<td>
				<div style="padding-left:30px;">
					主叫号码：<input id="callerId" class="easyui-numberbox" name="callerId" type="text" />
				</div>
			</td>
		</tr>
		<tr style="height:40px;">
			<td>
			             开始时间：<input id="startTime" width="30" name="startTime" class="easyui-datebox" />
			</td>
			<td>
				<div style="padding-left:30px;">
					结束时间：<input id="endTime" width="30" name="endTime" class="easyui-datebox" />
				</div>
			</td>
			<td>
				<div style="padding-left:30px;">
					接触状态：
					<select class="easyui-combobox" id="status">
						<option value="5">请选择</option>
						<option value="0">新记录(未外呼)</option>
						<option value="1">已载入</option>
						<option value="2" selected="selected">接触成功</option>
						<option value="3">接触失败</option>
						<option value="4">超时(不外呼)</option>
					</select>
				</div>
			</td>
			<td>
				<div style="padding-left:30px;">
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:findData()" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
				</div>
			</td>
			<td></td>
		</tr>
	</table>
</div>
<!-- 将播放录音的窗口包含进来，在点击试听时，可以显示该窗口并播放录音 -->
<%@ include file="_playvoicepanel.jsp"%>
</body>
</html>

