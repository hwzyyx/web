<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script type="text/javascript">

	var weekArr = new Array();
	weekArr[1] = "星期一";
	weekArr[2] = "星期二";
	weekArr[3] = "星期三";
	weekArr[4] = "星期四";
	weekArr[5] = "星期五";
	weekArr[6] = "星期六";
	weekArr[7] = "星期日";

	function execSelectSchedule(dateTypeComboboxDataFor1,selectDateType) {

		//搜索栏中的日期类型初始化
		$("#schedule_dateType").combobox({
			valueField:'id',
			textField:'text'
		}).combobox('loadData',dateTypeComboboxDataFor1).combobox('setValue',selectDateType);
		
		$("#scheduleDg").datagrid({
			pageSize:15,
			pagination:true,
			fit:true,
			singleSelect:true,
			rownumbers:true,
			rowrap:true,
			striped:true,
			pageList:[10,15,20],
			url:'schedule/datagrid'
		});

		schedule_findData();
		
	}
	
	function schedule_findData() {

		$("#scheduleDg").datagrid("load",{
			scheduleName:$("#schedule_scheduleName").textbox('getValue'),
			dateType:$("#schedule_dateType").combobox('getValue')
		});
		
	}

	
	
	function schedulerowformatter(value,data,index) {
		return "<a href='#' onclick='javascript:showScheduleDetail(\"" + data.SCHEDULE_ID + "\",\"" + data.SCHEDULE_NAME + "\",\"" + data.DATETYPE + "\",\"" + data.DATETYPE_DETAIL + "\",\"" + data.MAXTIMEITEM + "\",\"" + data.STARTHOUR1 + "\",\"" + data.STARTMINUTE1 + "\",\"" + data.ENDHOUR1 + "\",\"" + data.ENDMINUTE1 + "\",\"" + data.STARTHOUR2 + "\",\"" + data.STARTMINUTE2 + "\",\"" + data.ENDHOUR2 + "\",\"" + data.ENDMINUTE2 + "\",\"" + data.STARTHOUR3 + "\",\"" + data.STARTMINUTE3 + "\",\"" + data.ENDHOUR3 + "\",\"" + data.ENDMINUTE3 + "\",\"" + data.STARTHOUR4 + "\",\"" + data.STARTMINUTE4 + "\",\"" + data.ENDHOUR4 + "\",\"" + data.ENDMINUTE4 + "\",\"" + data.STARTHOUR5 + "\",\"" + data.STARTMINUTE5 + "\",\"" + data.ENDHOUR5 + "\",\"" + data.ENDMINUTE5 + "\")'><img src='themes/icons/search.png' border='0'>详情</a>";
	}

	function showScheduleDetail(scheduleId,scheduleName,dateType,dateTypeDetail,maxTimeItem,startHour1,startMinute1,endHour1,endMinute1,startHour2,startMinute2,endHour2,endMinute2,startHour3,startMinute3,endHour3,endMinute3,startHour4,startMinute4,endHour4,endMinute4,startHour5,startMinute5,endHour5,endMinute5) {

		//$("#scheduleTitle").html("调度名称：" + scheduleName);
		var scheduleDetailHtml = "";

		scheduleDetailHtml += "<h2 style='color:#0099FF;'>调度名称：" + scheduleName + "</h2>";
		scheduleDetailHtml += "<HR style='FILTER:alpha(opacity=100,finishopacity=0,style=3);margin-left:3px;' width='95%' color=#cccccc SIZE=1>";
		scheduleDetailHtml += "<table border='1' cellspacing='0' cellpadding='0' bordercolor='#c4e1ff' style='margin-left:5px;width:95%;height:220px;'>";
		scheduleDetailHtml += "<tr><td style='width:50px;vertical-align: top;' rowspan='2'>";

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
					scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:50px;background:#11fa5e;margin-top:1px;'>" + weekDayX + "</a><br/>";	
				}else {
					scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:50px;background:#e7e4e4;margin-top:1px;'><span style='color:#c9c5c5'>" + weekDayX + "</span></a><br/>";
				}
				
			}
		}else {             //如果日期类型为每天
			for(var i=1;i<=7;i++) {
				var weekDayX = weekArr[i];
				scheduleDetailHtml += "<a href='#' class='easyui-linkbutton linkButtonGray' style='width:50px;background:#11fa5e;margin-top:1px;'>" + weekDayX + "</a><br/>";
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

<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:45px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>调度名称：<input id="schedule_scheduleName" type="text" style="width:150px;" class="easyui-textbox" /></td>
				<td>
					<div style="padding-left:30px;">
						日期类型：<select id="schedule_dateType" class="easyui-combobox" style="width:150px;">
								 </select>
					</div>
				</td>
				<td>
					<div style="padding-left:30px;">
						<a href="javascript:schedule_findData()" class="easyui-linkbutton" style="width:150px;" data-options="iconCls:'icon-search'">查询</a>
					</div>
				</td>
			</tr>
		</table>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="scheduleDg">
			<thead>
				<tr style="height:12px;">		
					<th data-options="field:'SCHEDULE_NAME',width:200,align:'center'">调度名称</th>
					<th data-options="field:'CREATE_USERCODE',width:100,align:'center'">创建人</th>
					<th data-options="field:'DATETYPE_DETAIL_DESC',width:370,align:'center'">日期类型</th>
					<th data-options="field:'CREATETIME',width:150,align:'center'">创建时间</th>
					<th data-options="field:'id',width:60,align:'center',formatter:schedulerowformatter">操作</th>
				</tr>
				
			</thead>
		</table>	
	</div>
</div>

<!-- 调度详情弹窗 -->
<div id="scheduleDetailDlg" class="easyui-dialog" style="width:1000px;height:400px;padding:5px;" modal="true" closed="true">
	<div style="text-align: center" id="scheduleDetailInfo">
				
	</div>
</div>

