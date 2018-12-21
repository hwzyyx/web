<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>调度计划管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">

		var weekArr = new Array();
		weekArr[1] = "星期一";
		weekArr[2] = "星期二";
		weekArr[3] = "星期三";
		weekArr[4] = "星期四";
		weekArr[5] = "星期五";
		weekArr[6] = "星期六";
		weekArr[7] = "星期日";
	
		var currMaxTimeItem = 0;     //当前最大时间项的值
		var currScheduleId=null;          //当前调度计划ID

		var dateTypeComboboxDataFor0 = eval('${dateTypeComboboxDataFor0}');
		var dateTypeComboboxDataFor1 = eval('${dateTypeComboboxDataFor1}');
		
		$(function(){

			$("#scheduleDg").datagrid({
				pageSize:15,
				pagination:true,
				fit:true,
				toolbar:'#opertool',
				singleSelect:true,
				rownumbers:true,
				rowrap:true,
				striped:true,
				pageList:[10,15,20],
				url:'sysSchedule/datagrid'
			});
				
			//将日期类型由服务器返回，返回 combobox 类型
			
			$("#DATETYPE").combobox({
				valueField:'id',
    			textField:'text',
    			onSelect:function(record) {
    				if(record.id==1) {              //当值为1时，表示选择的是日期类型按每天
        				$("#weekinfo").css('display','none');
    				}else if(record.id==2) {        //当值为2时，表示选择的是日期类型按星期类型
        				$("#weekinfo").css('display','');
    				}
    			}
			}).combobox('loadData',dateTypeComboboxDataFor0).combobox("setValue","1");
			
			$("#dateType").combobox({
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',dateTypeComboboxDataFor1).combobox('setValue',"empty");

        	//搜索栏中的组织树形
        	$("#orgTree").tree({
            	checkbox:false,
            	url:'org/tree',
            	lines:true
            });

        	$("#scheduleDlg").dialog({   //当弹窗关闭时,执行表单内容的归整
            	onClose:function() {
        			initFormData();
        		}
            });
        	
		});

		function scheduleAdd() {
			$("#saveBtn").attr('onclick','saveAdd()');

			addTimeItem();
			
			$("#scheduleDlg").dialog("setTitle","添加调度计划").dialog("open");
		}

		function findData() {

			$('#scheduleDg').datagrid('load',{  
	            scheduleName:$('#scheduleName').val(),  
	            dateType:$('#dateType').combobox('getValue')
            });  
		}

		//删除记录
		function scheduleDel(scheduleId) {

			$.messager.confirm('提示','你确定要删除选中的记录吗',function(r){
				if(r) {
					$("#scheduleForm").form('submit',{
						url:"sysSchedule/delete?scheduleId=" + scheduleId,
						onSubmit:function(){
							
						},
						success:function(data) {

							var result = JSON.parse(data);    //解析Json 数据

							var statusCode = result.statusCode; //返回的结果类型
							var message = result.message;       //返回执行的信息

							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //保存成功时
								$('#scheduleDg').datagrid({url:'sysSchedule/datagrid'});
							}
						}
					});
				}
			});
			
		}

		//调度计划编辑
		function scheduleEdit(scheduleId,scheduleName,dateType,dateTypeDetail,maxTimeItem,startHour1,startMinute1,endHour1,endMinute1,startHour2,startMinute2,endHour2,endMinute2,startHour3,startMinute3,endHour3,endMinute3,startHour4,startMinute4,endHour4,endMinute4,startHour5,startMinute5,endHour5,endMinute5) {

			$("#saveBtn").attr('onclick','saveEdit()');
			
			$("#scheduleDlg").dialog("setTitle","修改调度计划").dialog("open");
			
			currScheduleId = scheduleId;   //设置当前的调度计划的ID
			
			$("#SCHEDULE_ID").val(scheduleId);                      //设置计划名字
			$("#SCHEDULE_NAME").textbox('setValue',scheduleName);   //设置计划名字
			if(dateType==2) {    		//按星期
				$("#DATETYPE").combobox('setValue','2');
				$("#weekinfo").css('display','');

				var weekdays = dateTypeDetail.split(",");
				$("input:checkbox").each(function(){//先将星期选项全部取消选择
					$(this).attr('checked',false);
				});
				for(var i=0;i<weekdays.length;i++) {
					$("input:checkbox[value='" + weekdays[i] + "']").attr('checked','true');
				}
				
			}else if(dateType==1){      //按每天
				$("#DATETYPE").combobox('setValue','1');
				$("#weekinfo").css('display','none');
			}
			//alert("maxTimeItem:" + maxTimeItem);
			for(var i=0;i<maxTimeItem;i++) {  
				addTimeItem();   //增加一个时间项
			}

			//赋值
			for(var i=0;i<maxTimeItem;i++) {
				if(i==0) {
					$("#STARTHOUR1").combobox('setValue',startHour1);
					$("#STARTMINUTE1").combobox('setValue',startMinute1);
					$("#ENDHOUR1").combobox('setValue',endHour1);
					$("#ENDMINUTE1").combobox('setValue',endMinute1);
				}else if(i==1) {
					$("#STARTHOUR2").combobox('setValue',startHour2);
					$("#STARTMINUTE2").combobox('setValue',startMinute2);
					$("#ENDHOUR2").combobox('setValue',endHour2);
					$("#ENDMINUTE2").combobox('setValue',endMinute2);
				}else if(i==2) {
					$("#STARTHOUR3").combobox('setValue',startHour3);
					$("#STARTMINUTE3").combobox('setValue',startMinute3);
					$("#ENDHOUR3").combobox('setValue',endHour3);
					$("#ENDMINUTE3").combobox('setValue',endMinute3);
				}else if(i==3) {
					$("#STARTHOUR4").combobox('setValue',startHour4);
					$("#STARTMINUTE4").combobox('setValue',startMinute4);
					$("#ENDHOUR4").combobox('setValue',endHour4);
					$("#ENDMINUTE4").combobox('setValue',endMinute4);
				}else if(i==4) {
					$("#STARTHOUR5").combobox('setValue',startHour5);
					$("#STARTMINUTE5").combobox('setValue',startMinute5);
					$("#ENDHOUR5").combobox('setValue',endHour5);
					$("#ENDMINUTE5").combobox('setValue',endMinute5);
				}
			}
			
			
		}

		//保存新增调度计划
		function saveAdd() {

			if(!checkWeek()) {
				return;
			}
			
			if(checkTimeItem()) {    //如果时间段有交叉时，不做保存
				return;
			}
			
			$("#scheduleForm").form("submit",{
				url:'sysSchedule/add?maxTimeItem=' + currMaxTimeItem,
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function (data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#scheduleDg').datagrid({url:'sysSchedule/datagrid'});
						$('#scheduleDlg').dialog('close');//关闭对话框
					}
				}
			});
			
		}

		//检查各个时间段或时间段间是否有交叉的地方
		//
		function checkTimeItem() {

			var startTimeArr = new Array();    //创建一个数组，用于储存所有的开始时间
			var endTimeArr = new Array();      //创建一个数组，用于储存所有的结束时间
			var isCross = false;               //是否有交叉

			//赋值并判断是否每个时间区都是前面的时间小于后面的时间
			for(var i=1;i<=currMaxTimeItem;i++) {
				//先检查每个时间项顺序，会不会开始时间大于结束时间
				var startHourXId = "STARTHOUR" + i;
				var startMinuteXId = "STARTMINUTE" + i;
				var endHourXId = "ENDHOUR" + i;
				var endMinuteXId = "ENDMINUTE" + i;

				var startTime = $("#" + startHourXId).combobox('getValue') + $("#" + startMinuteXId).combobox('getValue');
				var endTime = $("#" + endHourXId).combobox('getValue') + $("#" + endMinuteXId).combobox('getValue');

				//先判断时间的大小的问题
				var parseStartTime = parseInt(startTime);
				var parseEndTime = parseInt(endTime);
				startTimeArr[(i-1)] = parseStartTime;
				endTimeArr[(i-1)] = parseEndTime;
				
				if(parseStartTime > parseEndTime) {
					alert("错误警告：时间项的第" + i + "项,开始时间大于结束时间!");
					isCross =true;
					return isCross;
				}
			}

			//检查前一个时间区间的开始时间,是否小于后面的时间区间,主要是为了保证时间区间是按顺序从小到大的
			for(var i=0;i<startTimeArr.length;i++) {

				var sT = startTimeArr[i];   //取第i个开始时间

				for(var j=0;j<startTimeArr.length;j++) {
					if(j>i) {
						var sTj = startTimeArr[j];   //取出第j个开始时间

						if(sT>sTj) {
							alert("错误警告：第 " + (j+1) + "个时间项的开始时间小于第" + (i+1) + "个时间项的开始时间!");
							isCross =true;
							return isCross;
						}
						
					}
				}
				
			}

			//检查是否有交叉
			for(var i=0;i<startTimeArr.length;i++) {

				var sT = startTimeArr[i];   //取第i个开始时间
				var eT = endTimeArr[i];     //取第i个结束时间

				for(var j=0;j<startTimeArr.length;j++) {
					if(i!=j) {
						var sTj = startTimeArr[j];   //取出第j个开始时间
						var eTj = endTimeArr[j];     //取出第j个结束时间

						//alert("eTj:" + eTj + ",sT:" + sT + ",sTj:" + sTj + ",eT:" + eT);    
						if(!((sTj<=sT && eTj<=sT) || (eT<=eTj && eT<=sTj))) {
							alert("错误警告：第 " + (j+1) + "个时间项与第" + (i+1) + "个时间项有交叉!");
							isCross =true;
							return isCross;
						}
					}
				}
				
			}
			
		}

		//检查星期值是否选中
		function checkWeek() {

			var rs = true;
			
			var weekRs = '';

			var dateType = $("#DATETYPE").combobox('getValue');

			if(dateType=='2') {   //如果日期类型为2，即是星期类型,就要检查是否已经选择了星期，如果都没有选，就要提示错误
				$("input[name='week']:checked").each(function(){
					weekRs += $(this).val();
				});
				
				if(weekRs == null || weekRs == '') {   //如果日期没有选中,则提示警告
					rs = false;
					alert("日期类型为星期,未选中任何值,禁止保存!");
				}
			}


			return rs;
		}
		
		//保存修改调度计划
		function saveEdit() {

			if(!checkWeek()) {
				return;
			}
			
			if(checkTimeItem()) {    //如果时间段有交叉时，不做保存
				return;
			}
			
			$("#scheduleForm").form("submit",{
				url:'sysSchedule/update?maxTimeItem=' + currMaxTimeItem,
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function (data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#scheduleDg').datagrid({url:'sysSchedule/datagrid'});
						$('#scheduleDlg').dialog('close');//关闭对话框
					}
				}
			});
			
		}

		//退出新增/编辑调度计划
		function cancel() {

			$("#scheduleDlg").dialog("close");
			
		}

		function addTimeItem() {

			if(currMaxTimeItem < 5) {

				currMaxTimeItem += 1;   //当前最大时间间隔数量

				createTimeItem(currMaxTimeItem);	//创建时间区间			
			}
			
		}

		//添加时间项
		function createTimeItem(index) {

			var currItem = "timeItem" + currMaxTimeItem;
			
			var startHourXName = "schedule.STARTHOUR" + currMaxTimeItem;
			var startHourXId = "STARTHOUR" + currMaxTimeItem;
			var startMinuteXName = "schedule.STARTMINUTE" + currMaxTimeItem;
			var startMinuteXId = "STARTMINUTE" + currMaxTimeItem;
			var endHourXName = "schedule.ENDHOUR" + currMaxTimeItem;
			var endHourXId = "ENDHOUR" + currMaxTimeItem;
			var endMinuteXName = "schedule.ENDMINUTE" + currMaxTimeItem;
			var endMinuteXId = "ENDMINUTE" + currMaxTimeItem;

			var newItem = "<div id='" + currItem + "' style='padding-top:10px;'>开始时间:&nbsp;&nbsp;<select class='easyui-combobox' name='" + startHourXName + "' id='" + startHourXId + "' style='width:50px;'></select>&nbsp;&nbsp;时&nbsp;&nbsp;<select class='easyui-combobox' name='" + startMinuteXName + "' id='" + startMinuteXId + "' style='width:50px;'></select>&nbsp;&nbsp;分&nbsp;&nbsp;至&nbsp;&nbsp;<select class='easyui-combobox' name='" + endHourXName + "' id='" + endHourXId + "' style='width:50px;'></select>&nbsp;&nbsp;时&nbsp;&nbsp;<select class='easyui-combobox' name='" + endMinuteXName + "' id='" + endMinuteXId + "' style='width:50px;'></select>&nbsp;&nbsp;分</div>";

			$("#timeItemContainer").append(newItem);

			$("#" + startHourXId).combobox({
				url:'getCombobox?groupCode=HOURITEM&flag=0',
				method:'POST',
				valueField:'id',
				textField:'text',
				panelHeight:'300'
			});

			$("#" + startMinuteXId).combobox({
				url:'getCombobox?groupCode=MINUTEITEM&flag=0',
				method:'POST',
				valueField:'id',
				textField:'text',
				panelHeight:'300'
			});

			$("#" + endHourXId).combobox({
				url:'getCombobox?groupCode=HOURITEM&flag=0',
				method:'POST',
				valueField:'id',
				textField:'text',
				panelHeight:'300'
			});

			$("#" + endMinuteXId).combobox({
				url:'getCombobox?groupCode=MINUTEITEM&flag=0',
				method:'POST',
				valueField:'id',
				textField:'text',
				panelHeight:'300'
			});

			$("#" + startHourXId).combobox('setValue','08');
			$("#" + startMinuteXId).combobox('setValue','00');
			$("#" + endHourXId).combobox('setValue','18');
			$("#" + endMinuteXId).combobox('setValue','00');
		}
		
		//删除时间项
		function delTimeItem() {
			if(currMaxTimeItem > 1) {

				$("#timeItem" + currMaxTimeItem).remove();
				
				currMaxTimeItem = currMaxTimeItem - 1;
			}
		}

		//在保存编辑、保存、取消退出时，重置表单的数据
		function initFormData() {

			//移除动态新增的时间段的时间项
			for(var i=currMaxTimeItem;i>0;i--) {
				$("#timeItem" + i).remove();
			}
			currMaxTimeItem = 0;

			currScheduleId = null;
			$("#SCHEDULE_ID").val('');      //清空调度计划ID
			$("#SCHEDULE_NAME").textbox('setValue','');    //清空调度计划的名字
			
			$("#DATETYPE").combobox('setValue','1');   //时间类型重置为1，即是按每天
			$("#weekinfo").css('display','none');
			$("input[name='week']").attr("checked",'true');   //按星期时，周一至周日都选中
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
		
		function rowformatter(value,data,index) {
			return "<a href='#' onclick='javascript:scheduleEdit(\"" + data.SCHEDULE_ID + "\",\"" + data.SCHEDULE_NAME + "\",\"" + data.DATETYPE + "\",\"" + data.DATETYPE_DETAIL + "\",\"" + data.MAXTIMEITEM + "\",\"" + data.STARTHOUR1 + "\",\"" + data.STARTMINUTE1 + "\",\"" + data.ENDHOUR1 + "\",\"" + data.ENDMINUTE1 + "\",\"" + data.STARTHOUR2 + "\",\"" + data.STARTMINUTE2 + "\",\"" + data.ENDHOUR2 + "\",\"" + data.ENDMINUTE2 + "\",\"" + data.STARTHOUR3 + "\",\"" + data.STARTMINUTE3 + "\",\"" + data.ENDHOUR3 + "\",\"" + data.ENDMINUTE3 + "\",\"" + data.STARTHOUR4 + "\",\"" + data.STARTMINUTE4 + "\",\"" + data.ENDHOUR4 + "\",\"" + data.ENDMINUTE4 + "\",\"" + data.STARTHOUR5 + "\",\"" + data.STARTMINUTE5 + "\",\"" + data.ENDHOUR5 + "\",\"" + data.ENDMINUTE5 + "\")'><img src='themes/icons/pencil.png' border='0'>编辑</a>" + 
			"<a href='#' onclick='javascript:scheduleDel(\"" + data.SCHEDULE_ID +"\")'><img src='themes/icons/pencil.png' border='0'>删除</a>" + 
			"<a href='#' onclick='javascript:showScheduleDetail(\"" + data.SCHEDULE_ID + "\",\"" + data.SCHEDULE_NAME + "\",\"" + data.DATETYPE + "\",\"" + data.DATETYPE_DETAIL + "\",\"" + data.MAXTIMEITEM + "\",\"" + data.STARTHOUR1 + "\",\"" + data.STARTMINUTE1 + "\",\"" + data.ENDHOUR1 + "\",\"" + data.ENDMINUTE1 + "\",\"" + data.STARTHOUR2 + "\",\"" + data.STARTMINUTE2 + "\",\"" + data.ENDHOUR2 + "\",\"" + data.ENDMINUTE2 + "\",\"" + data.STARTHOUR3 + "\",\"" + data.STARTMINUTE3 + "\",\"" + data.ENDHOUR3 + "\",\"" + data.ENDMINUTE3 + "\",\"" + data.STARTHOUR4 + "\",\"" + data.STARTMINUTE4 + "\",\"" + data.ENDHOUR4 + "\",\"" + data.ENDMINUTE4 + "\",\"" + data.STARTHOUR5 + "\",\"" + data.STARTMINUTE5 + "\",\"" + data.ENDHOUR5 + "\",\"" + data.ENDMINUTE5 + "\")'><img src='themes/icons/search.png' border='0'>详情</a>";
		}
		
			
	</script>
</head>
<body>
<%@ include file="/base_loading.jsp" %>
<!-- 定义一个 layout -->
<div data-options="fit:true" class="easyui-layout">
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:45px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td>调度名称：<input id="scheduleName" type="text" style="width:150px;" class="easyui-textbox" /></td>
				<td>
					<div style="padding-left:30px;">
						日期类型：<select id="dateType" class="easyui-combobox" style="width:150px;"></select>
					</div>
				</td>
				<td>
					<div style="padding-left:30px;">
						<a href="javascript:findData()" class="easyui-linkbutton" style="width:150px;" data-options="iconCls:'icon-search'">查询</a>
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
					<th data-options="field:'SCHEDULE_NAME',width:400,align:'center'">调度名称</th>
					<th data-options="field:'CREATE_USERCODE',width:200,align:'center'">创建人</th>
					<th data-options="field:'ORG_CODE_DESC',width:200,align:'center'">部门(组织)名字</th>
					<th data-options="field:'DATETYPE_DETAIL_DESC',width:500,align:'center'">日期类型</th>
					<th data-options="field:'CREATETIME',width:200,align:'center'">创建时间</th>
					<th data-options="field:'id',width:200,align:'center',formatter:rowformatter">操作</th>
				</tr>
				
			</thead>
		</table>	
	</div>
</div>

<div id="opertool" style="padding:5px;">
	<a href="#" id="easyui-add" onclick="scheduleAdd()" class="easyui-linkbutton" iconCls='icon-add' plain="true">添加</a>
</div>

<div id="scheduleDlg" class="easyui-dialog" style="width:70%;height:70%;padding:5px;" modal="true" closed="true" buttons="#addScheduleBtn">

	<form id="scheduleForm" method="post">
		<!-- 包含调度配置的表单 -->
		<%@ include file="/autocall/schedule/_form.jsp" %>
	</form>

</div>

<!-- 调度详情弹窗 -->
<div id="scheduleDetailDlg" class="easyui-dialog" style="width:1000px;height:350px;padding:5px;" modal="true" closed="true">
	<div style="text-align: center" id="scheduleDetailInfo">
				
	</div>
</div>

</body>
</html>

