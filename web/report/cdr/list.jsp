<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>通话记录列表</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
    <script type="text/javascript" src="base-loading.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		//数据列表赋值
		function getCurrDate() {
			var dateString = "";
			var myDate = new Date();
			dateString += myDate.getFullYear() + "-";
			dateString += (myDate.getMonth()+1) + "-";
			dateString += myDate.getDate();
			return dateString;			
		}
		
		var isSuportHtml5  = null;
		$(function(){
			$("#startTime").datebox("setValue",getCurrDate());
			$("#endTime").datebox("setValue",getCurrDate());
			
			//disabledAllStateBtn();   //先让所有的按钮无法操作
			$("#cdrDg").datagrid({
				url:'cdr/datagrid?startTime='+$("#startTime").datebox("getValue") + "&endTime=" + $("#endTime").datebox("getValue"),
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				toolbar:'#searchtool'
			});

			//用于关闭弹出窗时的操作，用于清理表单数据
			$("#playvoicepanel").dialog({
				onClose:function() {
					//将播放器停掉
					$("#jquery_jplayer_1").jPlayer("stop");
				}
			});

			if (!window.applicationCache) { 
                $.messager.alert("提示","你的浏览器不支持HTML5,试听录音功能无效，请升级浏览器或是使用最新版chrome浏览器","info"); 
            } 

		});
		
		function findData() {
			$("#cdrDg").datagrid("load",{
				src:$("#src").val(),
				dst:$("#dst").val(),
				seq:$("#seq").val(),
				startTime:$("#startTime").datebox("getValue"),
				endTime:$("#endTime").datebox("getValue")
			}).datagrid({url:'cdr/datagrid'});
		}
		
		function rowformatter(value,data,index) {
			if(data.recordingfile==null || data.recordingfile=="") {
				return "";
			}			
			return "<a href='#' style='text-decoration:none' onclick='javascript:listenFile(\"" + data.path + "\",\"" + data.recordingfile + "\")'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='试听录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/listen.png' style='margin-top:2px;' border='0'></a>&nbsp;&nbsp;" +
			"<a href='cdr/download?path=" + data.path + "&file=" + data.recordingfile + "' style='text-decoration:none'><div style='display:inline;padding-top:6px;' class='easyui-tooltip' title='下载录音' style='width:100px;padding:5px;float:top;'><img src='themes/icons/download.png' style='margin-top:2px;' border='0'></a>";
		}

		function durationformatter(value,data,index) {
			return data.duration + "(" + value + ")";
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
		
		function exportCdr() {
		}
		//-----------------任务状态控制结束---------------------------------
					
	</script>	
</head>

<body>
	<div class="easyui-panel" title="通话记录" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:70px">
				<table>
								<tr>
									<td>主叫号码</td>
									<td>
										<input width="30" id="src" name="src" class="easyui-numberbox"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										被叫号码</td>
									<td>
										<input width="30" id="dst" name="dst" class="easyui-numberbox"/>
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										序列号</td>
									<td>
										<input width="30" id="seq" name="seq" class="easyui-numberbox"/>
									</td>
								</tr>
								<tr>
									<td>开始时间</td>
									<td>
										<input id="startTime" width="30" name="startTime" class="easyui-datebox" />
									</td>
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										结束时间</td>
									<td>
										<input id="endTime" width="30" name="endTime" class="easyui-datebox" />
									</td>
										
									<td></td><td><a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a></td>

								</tr>
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="cdrDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'src',width:150,align:'center'">主叫号码</th>                
									<th data-options="field:'dst',width:150,align:'center'">被叫号码</th>
									<th data-options="field:'billsec',width:100,align:'center',formatter:durationformatter">通话时长</th>
									<th data-options="field:'calldate',width:200,align:'center'">通话时间</th>                
									<th data-options="field:'seq',width:200,align:'center'">序列号</th>                
									<th data-options="field:'id',width:60,align:'center',formatter:rowformatter">操作</th>                
								</tr>        
							</thead>
					</table>					

			</div>
	
<!--			<div id="searchtool" style="padding:20px">  -->
			<div id="searchtool" style="height:22px;padding:3px;">  
					<div style="display:none;position:absolute;right:2px;" >
						<button id="exportBtn" onclick="exportCdr()">导出</button>
					</div>
			 <div>
		</div>
	</div>

	<!-- 将播放录音的窗口包含进来，在点击试听时，可以显示该窗口并播放录音 -->
	<%@ include file="/_playpanel.jsp"%>

</body>
</html>

