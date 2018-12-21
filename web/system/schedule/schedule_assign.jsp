<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>调度任务分配</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="custom_js/custom_messager.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="system/callerid/_callerid.js"></script>
	<script type="text/javascript">
		
		var currSelectNodeId = null;    //当前选择的组织Id
		var currOperId = null;          //当前的 OperId
		var targetOperId = null;        //目标操作员
		
		var weekArr = new Array();
		weekArr[1] = "星期一";
		weekArr[2] = "星期二";
		weekArr[3] = "星期三";
		weekArr[4] = "星期四";
		weekArr[5] = "星期五";
		weekArr[6] = "星期六";
		weekArr[7] = "星期日";
		
		$(function(){
			$("#treeUl").tree({
				checkbox:false,
				url:'org/tree',
				lines:true,
				onSelect:function(node) {
					//alert("onSelect" + node.text + "," + node.id + "," + node.pid);
					currSelectNodeId = node.id;
					$("#operId").val("");
					$("#operName").val("");
					$("#operState").combobox("setValue","2");    //当设置为2时，为 "请选择"
					$('#operatorDg').datagrid({url:'operator/datagrid?orgCode=' + node.id});//实现Datagrid重新刷新效果
				},
				onLoadSuccess:function(node,data) {
					if(currSelectNodeId==null || currSelectNodeId=="") {
						var n2 = $("#treeUl").tree("getRoot");
						$("#treeUl").tree("select",n2.target);
					}else {
						var currNode = $("#treeUl").tree('find',currSelectNodeId);
						$("#treeUl").tree("select",currNode.target);
					}
				}
			});

			$("#operatorDg").datagrid({
				pageSize:10,
				pagination:true,      
				fit:true,
				singleSelect:true,
				toolbar:"#opertool",
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				url:'operator/datagrid?orgCode=' + currSelectNodeId 
			});

			
			$("#roleDg").datagrid({
				title:'角色信息',
				pageSize:10,
				fit:true,
				singleSelect:false,
				//toolbar:"#searchtool2",
				rowrap:true,
				striped: true,
				rownumbers: true,
				checkbox:true,
				pageList:[10,30,50],
				url:'role/datagrid',
				pagination:true,      
				idField:'ROLE_CODE',
				onLoadSuccess:function(data) {
					//检查当前操作员对应的角色
					if(currOperId!=null&&currOperId != "") {
						$("#operatorForm").form('submit',{
							url:"operator/getRoleByOperId?operId=" + currOperId,
							onSubmit:function() {
							},
							success:function(rs) {
								
								var result = JSON.parse(rs);    //解析Json 数据

								var statusCode = result.statusCode; //返回的结果类型
								var message = result.message;       //返回执行的信息
								
								if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
									//alert(message);
									var roleCodes = message.split(",");
									for(var i=0;i<roleCodes.length;i++) {
										//alert(roleCodes[i]);
										$("#roleDg").datagrid('selectRecord',roleCodes[i]);
									}
								}
							}
						});
					}
					
				}
			});

			//用于关闭弹出窗时的操作，用于清理表单数据
			$("#addOperatorDlg").dialog({
				onClose:function() {
					$("#operatorForm").form("clear");
					currOperId=null;
					$("#roleDg").datagrid("clearSelections");
				}
			});
			//调度任务列表
			$("#scheduleDg").datagrid({
	    		
    			pageSize:100,
				pagination:true,
				fit:true,
				toolbar:'#scheduleTool',
				singleSelect:false,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[50,100,200],
				url:'sysSchedule/datagrid',
				idField:'SCHEDULE_ID',
				onLoadSuccess:function(data) {
					if(targetOperId!=null) {
						$.ajax({
							url:'sysScheduleAssign/getSysScheduleAssignResult?targetOperId=' + targetOperId,
							method:'post',
							dataType:'json',
							success:function(rs) {
								var statusCode = rs.statusCode; //返回的结果类型
								var message = rs.message;       //返回执行的信息
								//window.parent.showMessage(message,statusCode);
								if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
									var callerid_ids = message.split(",");
									for(var i=0;i<callerid_ids.length;i++) {
										$("#scheduleDg").datagrid('selectRecord',callerid_ids[i]);
									}
								}
							}
						});
					}
				}
    			
    		});
			
			$("#scheduleAssignDlg").dialog({
				onClose:function() {
					targetOperId=null;
					$("#scheduleDg").datagrid("clearSelections");
				}
			});
			
			$("#addScheduleDlg").dialog({
    			onClose:function() {
    				$("#scheduleForm").form('clear');
    			}
    		});
		});
		
		function stateFormat(val,data,index){
			if(val==1) {
				return '<span style="color:green;">有效</span>';
			}else {
				return '<span style="color:red;">无效</span>';
			}
		}

		function sexFormat(val,data,index){
			if(val==1) {
				return '<span style="color:red;">男</span>';
			}else {
				return '<span style="color:red;">女</span>';
			}
		}

		//格式化：在每行输出 修改及删除
		function rowformater(value,data,index) {
			return "<a href='#' onclick='javascript:scheduleAssign(\"" + data.OPER_ID +"\",\""+ data.OPER_NAME + "\")'><img src='themes/icons/dial.png' border='0'>调度任务分配</a>";
		}
		
		
		//格式化：将状态格式化，如果状态值为1,则为绿色，且定义为有效；状态值为0，则为红色，且定义为无效
		function roleStateFormatter(val,data) {
			if(val==1) {
				return '<span style="color:green;">有效</span>';
			}else {
				return '<span style="color:red;">无效</span>';
			}
		}
		
		//数据查询
		function FindData(){  
			   $('#operatorDg').datagrid('load',{  
			         operId:$('#operId').val(),  
			         operName:$('#operName').val(),
		             operState:$('#operState').combobox('getValue')}  
			    );  
		}
		
		//主叫号码分配
		function scheduleAssign(operId,operName) {
			
			targetOperId = operId;
			//alert(targetOperId);
			$("#scheduleDg").datagrid("load",{
				targetOperId:targetOperId
			});
			
			$("#scheduleAssignDlg").dialog('setTitle',"操作员：" + operName + "(" + operId + ")的调度任务分配").dialog('open');
			
		}
		
		//保存调度任务的分配
		function saveSysScheduleAssign() {
			
			ids = getSelectedRows();       //取得选中的 ID
			
			$.ajax({
				url:'sysScheduleAssign/saveSysScheduleAssign?targetOperId=' + targetOperId + "&ids=" + ids,
				method:'post',
				dataType:'json',
				success:function(rs) {
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
						$("#scheduleDg").datagrid("load",{
							
						});
					}
				}
			});
			
		}
		
		//取得选中的调度任务数据			
		function getSelectedRows() {
			
			var rows = $('#scheduleDg').datagrid('getSelections');
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].SCHEDULE_ID);
			}
			return	ids.join(",");			
		}
		
		
		function schedulerowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:showScheduleDetail(\"" + data.SCHEDULE_ID + "\",\"" + data.SCHEDULE_NAME + "\",\"" + data.DATETYPE + "\",\"" + data.DATETYPE_DETAIL + "\",\"" + data.MAXTIMEITEM + "\",\"" + data.STARTHOUR1 + "\",\"" + data.STARTMINUTE1 + "\",\"" + data.ENDHOUR1 + "\",\"" + data.ENDMINUTE1 + "\",\"" + data.STARTHOUR2 + "\",\"" + data.STARTMINUTE2 + "\",\"" + data.ENDHOUR2 + "\",\"" + data.ENDMINUTE2 + "\",\"" + data.STARTHOUR3 + "\",\"" + data.STARTMINUTE3 + "\",\"" + data.ENDHOUR3 + "\",\"" + data.ENDMINUTE3 + "\",\"" + data.STARTHOUR4 + "\",\"" + data.STARTMINUTE4 + "\",\"" + data.ENDHOUR4 + "\",\"" + data.ENDMINUTE4 + "\",\"" + data.STARTHOUR5 + "\",\"" + data.STARTMINUTE5 + "\",\"" + data.ENDHOUR5 + "\",\"" + data.ENDMINUTE5 + "\")'><img src='themes/icons/search.png' border='0'>详情</a>";
		}
		
		function showScheduleDetail(scheduleId,scheduleName,dateType,dateTypeDetail,maxTimeItem,startHour1,startMinute1,endHour1,endMinute1,startHour2,startMinute2,endHour2,endMinute2,startHour3,startMinute3,endHour3,endMinute3,startHour4,startMinute4,endHour4,endMinute4,startHour5,startMinute5,endHour5,endMinute5) {
			//$("#scheduleTitle").html("调度名称：" + scheduleName);
			var scheduleDetailHtml = "";

			scheduleDetailHtml += "<h2 style='color:#0099FF;padding-top:0px;padding-bottom:0px;'>调度名称：" + scheduleName + "</h2>";
			scheduleDetailHtml += "<HR style='FILTER:alpha(opacity=100,finishopacity=0,style=3);margin-left:3px;' width='95%' color=#cccccc SIZE=1>";
			scheduleDetailHtml += "<table border='1' cellspacing='0' cellpadding='0' bordercolor='#c4e1ff' style='margin-left:5px;width:95%;height:220px;'>";
			scheduleDetailHtml += "<tr><td style='width:80px;vertical-align: top;' rowspan='2'>";

			//循环打印周一至周日
			if(dateType==2) {   //如果日期类型为星期
				for(var i=1;i<=7;i++) {
					var weekDayX = weekArr[i];
					var isContain = false;

					var weekdays = dateTypeDetail.split(",");
					for(var j=0;j<weekdays.length;j++) {
						if(i==weekdays[j]) {
							isContain = true;
						}
					}

					if(isContain) {
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:70px;background:#11fa5e;margin-top:1px;'>" + weekDayX + "</a><br/>";	
					}else {
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:70px;background:#e7e4e4;margin-top:1px;'><span style='color:#c9c5c5'>" + weekDayX + "</span></a><br/>";
					}
					
				}
			}else {             //如果日期类型为每天
				for(var i=1;i<=7;i++) {
					var weekDayX = weekArr[i];
					scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:70px;background:#11fa5e;margin-top:1px;'>" + weekDayX + "</a><br/>";
				}
			}
			//循环打印周一至周日结束
			
			
			scheduleDetailHtml += "</td><td style='text-align: left;'>";

			//循环打印时间段
			var startTimeArr = new Array();       //创建一个数组，用于储存所有的开始时间
			var endTimeArr = new Array();         //创建一个数组，用于储存所有的结束时间
			var startTimeTextArr = new Array();  //创建一个数组，用于储存所有开始区间的显示
			var endTimeTextArr = new Array();  //创建一个数组，用于储存所有结束区间的显示
			for(var i=1;i<=maxTimeItem;i++) {
				if(i==1) {
					var sM1 = "00";
					var eM1 = "00";
					if(startMinute1=='30'){ sM1="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute1=='30'){eM1="50";};      //主要是为了linkButton 的长度考虑
					startTime1 = startHour1 + sM1; //  
					endTime1 = endHour1 + eM1;
					startTimeArr[(i-1)] = parseInt(startTime1);
					endTimeArr[(i-1)]= parseInt(endTime1);
					startTimeTextArr[(i-1)] = startHour1 + ":" + startMinute1;
					endTimeTextArr[(i-1)] = endHour1 + ":" + endMinute1;
				}else if(i==2) {
					var sM2 = "00";
					var eM2 = "00";
					if(startMinute2=='30'){ sM2="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute2=='30'){eM2="50";};      //主要是为了linkButton 的长度考虑
					startTime2 = startHour2 + sM2; //  
					endTime2 = endHour2 + eM2;
					startTimeArr[(i-1)] = parseInt(startTime2);
					endTimeArr[(i-1)]= parseInt(endTime2);
					startTimeTextArr[(i-1)] = startHour2 + ":" + startMinute2;
					endTimeTextArr[(i-1)] = endHour2 + ":" + endMinute2;
				}else if(i==3) {
					var sM3 = "00";
					var eM3 = "00";
					if(startMinute3=='30'){ sM3="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute3=='30'){eM3="50";};      //主要是为了linkButton 的长度考虑
					startTime3 = startHour3 + sM3; //  
					endTime3 = endHour3 + eM3;
					startTimeArr[(i-1)] = parseInt(startTime3);
					endTimeArr[(i-1)]= parseInt(endTime3);
					startTimeTextArr[(i-1)] = startHour3 + ":" + startMinute3;
					endTimeTextArr[(i-1)] = endHour3 + ":" + endMinute3;
				}else if(i==4) {
					var sM4 = "00";
					var eM4 = "00";
					if(startMinute4=='30'){ sM4="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute4=='30'){eM4="50";};      //主要是为了linkButton 的长度考虑
					startTime4 = startHour4 + sM4; //  
					endTime4 = endHour4 + eM4;
					startTimeArr[(i-1)] = parseInt(startTime4);
					endTimeArr[(i-1)]= parseInt(endTime4);
					startTimeTextArr[(i-1)] = startHour4 + ":" + startMinute4;
					endTimeTextArr[(i-1)] = endHour4 + ":" + endMinute4;
				}else if(i==5) {
					var sM5 = "00";
					var eM5 = "00";
					if(startMinute5=='30'){ sM5="50";};   //主要是为了linkButton 的长度考虑
					if(endMinute5=='30'){eM5="50";};      //主要是为了linkButton 的长度考虑
					startTime5 = startHour5 + sM5; //  
					endTime5 = endHour5 + eM5;
					startTimeArr[(i-1)] = parseInt(startTime5);
					endTimeArr[(i-1)]= parseInt(endTime5);
					startTimeTextArr[(i-1)] = startHour5 + ":" + startMinute5;
					endTimeTextArr[(i-1)] = endHour5 + ":" + endMinute5;
				}
			}

			var buttonLength = "0";
			//定义
			for(var i=0;i<startTimeArr.length;i++) {
				if(i==0) {   //第一个时间区间
					if(startTimeArr[0]!=0) {   //如果第一个时间区间的开始时间不是零点
						//定义长度
						buttonLength = Math.ceil((startTimeArr[0]-0)*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#e7e4e4;margin-left:1px;'><span style='color:#c9c5c5'>" + "00:00至" + startTimeTextArr[i] + "</span></a>";
						buttonLength = Math.ceil((endTimeArr[0]-startTimeArr[0])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#11fa5e;margin-left:1px;'>" + startTimeTextArr[i] + "至" + endTimeTextArr[i] + "</a>";
					}else {
						buttonLength = Math.ceil((endTimeArr[0]-startTimeArr[0])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#11fa5e;margin-left:1px;'>" + startTimeTextArr[i] + "至" + endTimeTextArr[i] + "</a>";
					}
				}else {

					if(startTimeArr[i] != endTimeArr[(i-1)]) {
						buttonLength=Math.ceil((startTimeArr[i]-endTimeArr[(i-1)])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#e7e4e4;margin-left:1px;'><span style='color:#c9c5c5'>" + endTimeTextArr[(i-1)] + "至" + startTimeTextArr[i] + "</span></a>";
						buttonLength = Math.ceil((endTimeArr[i]-startTimeArr[i])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#11fa5e;margin-left:1px;'>" + startTimeTextArr[i] + "至" + endTimeTextArr[i] + "</a>";
					}else {
						buttonLength = Math.ceil((endTimeArr[i]-startTimeArr[i])*820/2400) + "px";
						scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#11fa5e;margin-left:1px;'>" + startTimeTextArr[i] + "至" + endTimeTextArr[i] + "</a>";
					}
					
				}
			}

			if(endTimeArr[(startTimeArr.length-1)]!=0) {   //最后，再查看最后的时间段是否是24：00,否则还要有一段留白
				buttonLength = Math.ceil((2400-endTimeArr[(startTimeArr.length-1)])*820/2400) + "px";
				scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:" + buttonLength + ";height:80px;background:#e7e4e4;margin-left:1px;'><span style='color:#c9c5c5'>" + endTimeTextArr[((startTimeArr.length-1))] + "至24:00" + "</span></a>";
			}

			
			
			//循环打印时间段结束
			
			scheduleDetailHtml += "</td><tr style='height:20px;'><td style='text-align: left;'>";
			scheduleDetailHtml += "<img src='themes/icons/timer.png' style='width:840px' border='0'></td></tr></table>";

			$.parser.parse($("#scheduleDetailInfo").html(scheduleDetailHtml));
			
			$("#scheduleDetailDlg").dialog('setTitle','调度计划详情').dialog("open");
			
		}
		
	</script>
</head>
<body id="orgBody" style="margin-top:1px;margin-left:1px;">
	<!-- 页面加载效果 -->
	<%@ include file="/base_loading.jsp" %>
	<div class="easyui-panel" title="操作员管理" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 左侧的树形 -->
			<div data-options="region:'west',split:true" style="width:220px;padding:10px">
				<ul id="treeUl" class="easyui-tree">
				</ul>
			</div>

			<!-- 显示区 -->
			<div data-options="region:'center'" style="padding:1px">
			
			
				<!-- 定义一个 layout -->
				<div data-options="fit:true" class="easyui-layout">
					<!-- 顶部查询区 -->
					<div data-options="region:'north',split:true,border:true" style="height:42px;padding-top:5px;padding-left:5px;">
						<span style="">工号：</span><input type="text" class="easyui-textbox" id="operId" style="width:150px;" />  
				        <span style="padding-left:30px;">操作员：</span><input type="text" id="operName" class="easyui-textbox" style="width:150px;" />
						<span style="padding-left:30px;">状态：</span>
							<select class="easyui-combobox" style="width: 155px;" id="operState" data-options="panelHeight:'auto'">
              					<option value="2">请选择</option>
              					<option value="1">有效</option>
              					<option value="0">无效</option>
        					</select>
        				<span style="padding-left:30px;">
				        	<a href="javascript:FindData()" style="width:150px;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
        				</span>	
					</div>
				
					<!-- 数据显示区 -->
					<div data-options="region:'center',split:true,border:false">
						<table id="operatorDg">
								<thead>  
									<tr style="height:12px;">                
										<th data-options="field:'OPER_ID',width:100,align:'center'">工号</th>                
										<th data-options="field:'ORG_CODE_DESC',width:100,align:'center'">所属组织</th>                
										<th data-options="field:'OPER_NAME',width:100,align:'center'">操作员名称</th>                
										<th data-options="field:'STATE',width:80,align:'center',formatter:stateFormat">状态</th>
										<th data-options="field:'SEX',width:60,align:'center',formatter:sexFormat">性别</th>
										<th data-options="field:'TELNO',width:120,align:'center'">联系电话</th>
										<th data-options="field:'CALL_NUMBER',width:70,align:'center'">座席号码</th>
										<th data-options="field:'CREATETIME',width:200,align:'center'">创建时间</th>
										<th data-options="field:'id',width:150,align:'center',formatter:rowformater">操作</th>
									
									</tr>        
								</thead>
						</table>
					</div>
				</div>
			</div>
		</div>
		
	</div>
	<!-- 调度任务弹窗 -->
	<div id="scheduleAssignDlg" class="easyui-dialog" style="width:80%;height:80%;padding:10px 20px;" modal="true" closed="true" buttons="#scheduleAssignDlgBtn">
		<table id="scheduleDg">
				<thead>
					<tr style="height:12px;">
						<th data-options="field:'ck',checkbox:true"></th>		
						<th data-options="field:'SCHEDULE_NAME',width:200,align:'center'">调度名称</th>
						<th data-options="field:'CREATE_USERCODE',width:100,align:'center'">创建人</th>
						<th data-options="field:'DATETYPE_DETAIL_DESC',width:370,align:'center'">日期类型</th>
						<th data-options="field:'CREATETIME',width:150,align:'center'">创建时间</th>
						<th data-options="field:'id',width:60,align:'center',formatter:schedulerowformatter">操作</th>
					</tr>
				</thead>
			</table>
	</div>
	<div id="scheduleAssignDlgBtn">
		<a href="#" id="easyui-add" onclick="saveSysScheduleAssign()" class="easyui-linkbutton" iconCls="icon-ok" plain="true">保存调度任务分配</a>
	</div>
	
	<!--  为了简化界面，暂时不在分配界面提供新增调度任务的功能
	<div id="scheduleTool" style="padding:5px;">
		<a href="#" id="easyui-add" onclick="scheduleAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">新增调度任务</a>
	</div>
	
	<div id="addScheduleDlg" class="easyui-dialog" style="width:40%;height:40%;padding:10px 20px;" modal="true" closed="true" buttons="#addScheduleDlgBtn">

		<form id="scheduleForm" method="post">
			<!-- 包含表单 -->
			<%@ include file="/system/schedule/_form.jsp"%>
		</form>	
	</div>
	 -->
	
	<!-- 调度详情弹窗 -->
	<div id="scheduleDetailDlg" class="easyui-dialog" style="width:1000px;height:350px;padding:5px;" modal="true" closed="true">
		<div style="text-align: center" id="scheduleDetailInfo">
					
		</div>
	</div>
	
</body>
</html>

