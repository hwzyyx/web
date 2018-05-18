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
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.min.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript">
		//数据列表赋值
		
		$(function(){
			
			$("#transferRecordDg").datagrid({
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				url:'transferRecord/datagrid'
			});

			if (!window.applicationCache) { 
                $.messager.alert("提示","你的浏览器不支持HTML5,试听录音功能无效，请升级浏览器或是使用最新版chrome浏览器","info"); 
            }
		});

		function findData() {
			$("#transferRecordDg").datagrid("load",{
				did:$("#did").numberbox('getValue'),
				destination:$("#destination").numberbox('getValue'),
				startTime:$("#startTime").datebox("getValue"),
				endTime:$("#endTime").datebox("getValue")
			});
		}

		function rowformatter(val,data,index) {
			if(data.RECORDING_FILE==null) {
				return "";
			}			
			return "<a href='#' style='text-decoration:none' onclick='javascript:listenFile(\"" + data.path + "\",\"" + data.RECORDING_FILE + "\")'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='试听录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/listen.png' style='margin-top:2px;' border='0'></a>&nbsp;&nbsp;" +
			"<a href='transferRecord/download?path=" + data.path + "&file=" + data.RECORDING_FILE + "' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='下载录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/download.png' style='margin-top:2px;' border='0'></a>";
		}

		function listenFile(path,recordingfile) {
			var file = path + recordingfile;
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
	<div class="easyui-panel" title="呼叫转移记录查询" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:40px">
				<table>
								<tr>
									<td>特服号</td>
									<td>
										<input data-options="width:180" id="did" name="did" class="easyui-numberbox"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;
										目标号码</td>
									<td>
										<input data-options="width:180" id="destination" name="destination" class="easyui-numberbox"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;
										拦截时间</td>
									<td>
										<input id="startTime" data-options="width:190" name="startTime" class="easyui-datebox" />
											至
										<input id="endTime" data-options="width:190" name="endTime" class="easyui-datebox" />
									</td>
									<td>&nbsp;&nbsp;&nbsp;<a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search',width:135">查询</a></td>
								</tr>
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="transferRecordDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'CALLERID',width:150,align:'center'">来电号码</th>                
									<th data-options="field:'DID',width:150,align:'center'">特服号码</th>                
									<th data-options="field:'DESTINATION',width:150,align:'center'">目标号码</th>
									<th data-options="field:'TRUNK_DESC',width:120,align:'center'">中继信息</th>
									<th data-options="field:'CALLDATE',width:150,align:'center'">转移时间</th>
									<th data-options="field:'MEMO',width:250,align:'center'">转移原因</th>                
									<th data-options="field:'RECORDING_FILE',width:100,align:'center',formatter:rowformatter">通话录音</th>                
								</tr>        
							</thead>
					</table>					

			</div>

		</div>
	</div>
	
	<!-- 将播放录音的窗口包含进来，在点击试听时，可以显示该窗口并播放录音 -->
	<%@ include file="_playvoicepanel.jsp"%>
</body>
</html>

