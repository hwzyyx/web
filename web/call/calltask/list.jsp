<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>任务管理</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="jscharts.js"></script>
	<script type="text/javascript" src="highcharts.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript">
		//数据列表赋值
		
		var currTaskId = null;    //当前选择的项目
		$(function(){

			//disabledAllStateBtn();   //先让所有的按钮无法操作
			$("#callTaskDg").datagrid({
				url:'callTask/datagrid',
				pageSize:15,
				pagination:true,      
				fit:true,
				singleSelect:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,15,20],
				toolbar:'#searchtool',
				checkbox:true,
				idField:'CT_ID',
				onSelect:function(index,data) {
					var taskId = data.CT_ID;
					var state = data.TASK_STATE;
					var distributeCount = data.DISTRIBUTION;    //取得已经分配的号码数量    
					
					currTaskId = taskId;
					if(state==0) {   //如果当前任务的状态为0时，表示任务未启动，表示该任务可以做：启动操作、删除操作、标注为历史任务操作
						disabledAllStateBtn();
						$("#startBtn").removeAttr('disabled');
						$("#delBtn").removeAttr('disabled');
					}else if(state==1) {     //如果状态为1时，表示任务书已经启动，可以暂停或是停止操作
						disabledAllStateBtn();
						$("#pauseBtn").removeAttr('disabled');
						$("#stopBtn").removeAttr('disabled');
					}else if(state==2) {     //状态为2时，表示已经暂停，可以启动、停止，删除任务操作
						disabledAllStateBtn();
						$("#startBtn").removeAttr('disabled');
						$("#stopBtn").removeAttr('disabled');
						$("#delBtn").removeAttr('disabled');
					}else if(state==3) {     //状态为3时，表示已经停止，可以启动、删除任务操作,标注为历史操作
						disabledAllStateBtn();
						$("#startBtn").removeAttr('disabled');
						$("#delBtn").removeAttr('disabled');
						$("#historyBtn").removeAttr('disabled');
					}else if(state==4) {    //状态为4时，表示这是历史任务，只允许删除
						disabledAllStateBtn();
						$("#delBtn").removeAttr('disabled');
					}

					if(distributeCount>0) {
						$("#reuseBtn").removeAttr('disabled');
					}
					
				},
				onLoadSuccess:function(data) {   //当数据加载成功时，先将所有的状态按钮不可操作
					
					disabledAllStateBtn();
					if(currTaskId!=null) {       //加载后，要重选回操作的行
						$("#callTaskDg").datagrid('selectRecord',currTaskId);
					}

					//alert(data.length);
					for(var i=0;i<data.rows.length;i++) {

						//var taskId = data.rows[i].CT_ID; //先取出参数值
						var idInfo = "task" + data.rows[i].CT_ID;
						var pieChart1 = 'pie' + data.rows[i].CT_ID + "_1";
						var pieChart2 = 'pie' + data.rows[i].CT_ID + "_2";
						
						//addTooltip("aaaaa" + idInfo,'task' + taskId);
						$("#"+idInfo).tooltip({
							position:'bottom',
							//content:$('<div style="float:left"><div id=\"' + pieChart1 + '\" style="float:left;min-width: 240px; height: 250px; max-width: 250px; margin: 0 auto;border-color: red;border-width:0px;border-style:solid; ">aaaaaaa</div><div id=\"' + pieChart2 + '\" style="float:left;min-width: 400px; height: 250px; max-width: 300px; margin: 0 auto;border-color: red;border-width:0px;border-style:solid; ">aaaaaaa</div></div>'),
							content:$('<div id=\"' + pieChart2 + '\" style="float:left;min-width: 400px; height: 150px; max-width: 400px; margin: 0 auto;border-color: red;border-width:0px;border-style:solid; ">aaaaaaa</div></div>'),
							onShow:function() {
								var taskId = $(this).attr('id').substring(4);     //需要将前面的 task 字串去除
								//alert(taskId);
								//var chart = getPieChart("pie" + taskId + "_1","号码分配情况","百分比为");
								var chart2 = getPieChart("pie" + taskId + "_2","已分配的情况","百分比为");

								var t = $(this);
								//可以保持弹框可以用鼠标查看
								t.tooltip('tip').focus().unbind().bind('blur',function(){
									t.tooltip('hide');
								});
								
								$.ajax({
							    	type:'POST',
									dataType:"json",
									url:"callTask/getPieChartData?type=1&taskId=" + taskId,
									success:function(data) {
	
										var arr = [];
										var arr2 = [];
	
										$.each(data,function(i,d){
											if(i<2) {
											arr.push([d.name,parseInt(d.value)]);
											}else {
												arr2.push([d.name,parseInt(d.value)]);
											}
											console.log(d.name,d.value,i);
										});
	
										//chart.series[0].setData(arr);
										chart2.series[0].setData(arr2);
							    	}
								    
								});
							},
							onUpdate:function(cc) {
								cc.panel({
									width: 550,
									height: 220
								});
								
									
									//var taskId = $(this).attr('id').substring(4);     //需要将前面的 task 字串去除
									//var myChart = new JSChart('chart' + taskId, 'pie');
									/*var myData = new Array(['Communicate socially', 42], ['Engage in work for classes', 38], ['Be entertained', 10], ['Communicate professionally', 7], ['Not sure/Don\'t know', 2]);
									var colors = ['#C40000', '#750303', '#F9ECA2', '#FA9000', '#FA5400'];
										myChart.setDataArray(myData);
										myChart.colorizePie(colors);
										myChart.setTitle('Students use the Internet most often to (%)');
										myChart.setTitleColor('#8E8E8E');
										myChart.setTitleFontSize(11);
										myChart.setTextPaddingTop(30);
										myChart.setSize(500, 300);
										myChart.setPieRadius(85);
										myChart.setPieUnitsColor('#555');
										myChart.setBackgroundImage('chart_bg.jpg');
										myChart.draw();*/
									/*$.ajax({
										type:'POST',
										//dataType:"json",
										url:"callTask/getPieChartData?type=1&taskId=" + taskId,
										success:function(rs) {
											var statusCode = rs.statusCode; //返回的结果类型
											var message = rs.message;       //返回执行的信息
											alert(message);
											var msg = "{JSChart:{datasets:[{data:[{unit:'A',value:'40'},{unit:'B',value:'16'},{unit:'C',value:'20'},{unit:'D',value:'10'},{unit:'E',value:'4'}],type:'pie'}]}}";
											
											
											//var jsondata = $.parseJSON(eval(message));
											console.log(eval(msg));
											myChart.setDataJSON(eval("[" + msg + "]"));
											myChart.draw()
											
										}
										//success:function(rs) {

										//	var myData = $.toJSON(data);
										//	alert(myData);
											//var jsd = eval(rs);
											//myChart.setDataJSON(jsd);
											//myChart.draw()
											//console.log(jsd);
										
											//console.log(rs);
											//alert(rs.list[0]);
											//console.log(rs);
											//for(var j=0;j<rs.length;j++) {
											//	console.log(rs[j]);
											//}
											//var dk = '{"JSChart":{"datasets":[{"id":"blue","data":[{"unit":"'A'","value":"40"},{"unit":"'B'","value":"16"},{"unit":"'C'","value":"20"},{"unit":"'D'","value":"10"},{"unit":"'E'","value":"4"}],"type":"pie"}],"colorset":["#99CDFB","#3366FB","#0000FA","#F8CC00","#F89900","#F76600"],"optionset":[{"set":"setSize","value":"600,300"},{"set":"setTitle","value":"'Phd Reference Chart'"},{"set":"setTitleFontFamily","value":"'Times New Roman'"},{"set":"setTitleFontSize","value":"14"},{"set":"setTitleColor","value":"'#0F0F0F'"},{"set":"setPieRadius","value":"95"},{"set":"setPieValuesColor","value":"'#FFFFFF'"},{"set":"setPieValuesFontSize","value":"9"},{"set":"setPiePosition","value":"180,165"},{"set":"setShowXValues","value":"false"},{"set":"setLegend","value":"'#99CDFB', 'Papers where authors found'"},{"set":"setLegend","value":"'#3366FB', 'Papers which cite from other articles'"},{"set":"setLegend","value":"'#0000FA', 'Papers which cite from news'"},{"set":"setLegend","value":"'#F8CC00', 'Papers which lack crucial'"},{"set":"setLegend","value":"'#F89900', 'Papers with different conclusion'"},{"set":"setLegend","value":"'#F76600', 'Papers with useful information'"},{"set":"setLegendShow","value":"true"},{"set":"setLegendFontFamily","value":"'Times New Roman'"},{"set":"setLegendFontSize","value":"10"},{"set":"setLegendPosition","value":"350,120"},{"set":"setPieAngle","value":"30"},{"set":"set3D","value":"ture"}]}}';
											//myChart.setDataJSON(rs);
											//myChart.draw()
										//}
									});*/
									
																				
							}
						});
					}
										
				}
			
			});

			$("#addTaskDlg").dialog({
				onClose:function() {
					$("#taskForm").form("clear");
				}
			});

			$("#setupTaskDlg").dialog({
				onClose:function() {
					$("#setup-updateTaskForm").form("clear");
					$("#setup-addTelephone").form('clear');
					//清除上传文件表单内容
					var file = $("#phoneFile");
					file.after(file.clone().val(""));
					file.remove();
				}
			});

			$("#callTelephoneDg").datagrid({
				//url:'callTelephone/datagrid?taskId='+currTaskId,
				pageSize:30,
				pagination:true,      
				fit:true,
				rowrap:true,
				striped: true,
				rownumbers: true,
				pageList:[10,30,50],
				checkbox:true,
				toolbar:'#setup-callTelephoneDgtool'
			});

			$("#opertorUl").tree({
				//url:'callTask/opertorTree',
				animate:true,
				checkbox:true,
				onLoadSuccess:function(node,data) {    //在tree列表成功后，设置默认选中项

					$.ajax({
						type:'POST',
						dataType:"json",
						url:"callTask/getAuthData?taskId=" + currTaskId,
						success:function(rs) {
	
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								var operIds = message.split(",");
								for(var i=0;i<operIds.length;i++) {
									var n = $("#opertorUl").tree('find',operIds[i]);    //先根据id,查找出 node
									$("#opertorUl").tree('check',n.target);                 //然后根据 node.target 进行选中
								}
							}
						}
					});
					
				}
				
			});
			
			$("#state").combobox({
				url:'getCombobox?groupCode=CALL_STATE&flag=1',
				method:'POST',
				valueField:'id',
				textField:'text'
			});
		});

		function disabledAllStateBtn() {
			$("#startBtn").attr("disabled",'true');
			$("#pauseBtn").attr("disabled",'true');
			$("#stopBtn").attr("disabled",'true');
			$("#delBtn").attr("disabled",'true');
			$("#historyBtn").attr("disabled",'true');
			$("#reuseBtn").attr("disabled",'true');
		}
		
		function findData() {
			$("#callTaskDg").datagrid("load",{
				taskName:$("#taskName").textbox('getValue'),
				taskType:$("#taskType").val(),
				taskState:$("#taskState").combobox('getValue'),
				startTime:$("#startTime").datebox("getValue"),
				endTime:$("#endTime").datebox("getValue")
			});
		}

		function telephoneFindData() {
			$("#callTelephoneDg").datagrid('load',{
				telephone:$("#telephone").numberbox('getValue'),
				clientName:$("#clientName").textbox('getValue'),
				state:$("#state").combobox('getValue'),
				telephoneStartTime:$("#telephoneStartTime").datebox('getValue'),
				telephoneEndTime:$("#telephoneEndTime").datebox('getValue')
			});
		}
		
		function telephoneDel() {
			$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...'
					});
					$.ajax({
						url:'callTelephone/delete?taskId=' + currTaskId + '&ids=' + getTelephoneSelectedRows() + '&distributeCount=' + getTelephoneCount4SelectRows(1) + '&successCount=' + getTelephoneCount4SelectRows(2) + '&failureCount=' + getTelephoneCount4SelectRows(3),
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress("close");
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
							if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
								$("#callTelephoneDg").datagrid({url:'callTelephone/datagrid?taskId='+currTaskId});
								$('#callTaskDg').datagrid({url:'callTask/datagrid'});						
							}
						}
					});
				}
			});
		}

		//取得选中的号码数据			
		function getTelephoneSelectedRows() {
			
			var rows = $('#callTelephoneDg').datagrid('getSelections');
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].TEL_ID);
			}
			return	ids.join(",");			
		}


		//返回任务号码列表中，已经选中的成功及失败的数量,type: 1表示已经被分配的数据  2表示成功的数量； 3表示失败的数量 
		function getTelephoneCount4SelectRows(type) {
			var count = 0;
			
			var rows = $('#callTelephoneDg').datagrid('getSelections');
			var ids = [];
			for(var i=0; i<rows.length; i++){
				if(rows[i].STATE==type) {
					count += 1;
				}
			}
			return count;
		}

		
		
		function rowformatter(value,data,index) {
			return "<a href='#' style='text-decoration:none' onclick='javascript:taskSetup(\"" + data.CT_ID + "\",\"" + data.TASK_NAME + "\",\"" + data.CALLERID + "\")'><img src='themes/icons/setup.png' border='0'>设置</a>" ;
		}

		function stateformatter(value,data,index) {
			if(value==0) {
				return '<span style="color:black;">新任务</span>';
			}else if(value==1) {
				return '<span style="color:green;">已启动</span>';
			}else if(value==2) {
				return '<span style="color:purple;">已暂停</span>';
			}else if(value==3) {
				return '<span style="color:red;">已停止</span>';
			}else if(value==4) {
				return '<span style="color:gray;">已完成</span>';
			}
			
		}

		function telephoneStateformatter(value,data,index) {
			if(value=="0") {
				return '<span style="color:black;">新号码</span>';
			}else if(value=="1") {
				return '<span style="color:purple;">已分配</span>';
			}else if(value=="2") {
				return '<span style="color:green;">成功</span>';
			}else if(value=="3") {
				return '<span style="color:red;">失败</span>';
			}
		}
		
		function sexformatter(value,data,index) {
			if(value=="0") {
				return '<span style="color:red;">女</span>';
			}else if(value=="1") {
				return '<span style="color:blue;">男</span>';
			}
		}

		function taskcounterdetailformatter(value,data,index) {
			var idInfo = "task" + value;
			return "<a href='#' id='" + idInfo +"' style='text-decoration:none'  class='easyui-tooltip' border='0'>详情</a>" ;
		}

		function getDetail() {
			//alert("aaaaa");
			return "aaaahwz";
		}

		function nocallformatter(value,data,index) {

			var totalCount = data.TELEPHONE_COUNT;     //获得总量
			var successCount = data.SUCCESS_COUNT;     //获得已经成功的量
			var failureCount = data.FAILURE_COUNT;     //获得已经失败的量

			var nocallCount = totalCount - successCount - failureCount;    //未外呼数量为：  总量 - 成功的量 - 失败的量
			
			return nocallCount;
		}
		

		function taskSetup(taskId,taskName,callerId) {
			$("#setupTaskDlg").dialog("open").dialog('setTitle','任务设置');
			
			currTaskId = taskId;    //设置为当前的任务ID
			
			$("#setuptaskCT_ID").val(taskId);
			$("#setuptaskTASK_NAME").textbox('setValue',taskName);
			$("#setuptaskCALLERID").numberbox('setValue',callerId);
			
			$("#callTelephoneDg").datagrid({url:'callTelephone/datagrid?taskId='+currTaskId});
			$("#opertorUl").tree({url:'callTask/opertorTree'});
		}
		
		function taskAdd() {
			$("#addTaskDlg").dialog("open").dialog('setTitle','添加任务');

			currTaskId = null;
			$("#saveBtn").removeAttr("onclick");
			$("#saveBtn").attr("onclick","saveAdd()");

			
		}
		
		function saveAdd() {
			$("#taskForm").form("submit",{
				url:'callTask/add',
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function (data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#callTaskDg').datagrid({url:'callTask/datagrid'});
						$('#addTaskDlg').dialog('close');//关闭对话框
					}
				}
			});
		}

		function saveEdit() {
			$("#setup-updateTaskForm").form('submit',{
				url:'callTask/update',
				onSubmit:function() {
					return $(this).form('validate');
				},
				success:function (data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode == 'success') {         //保存成功时
						$('#callTaskDg').datagrid({url:'callTask/datagrid'});
					}
				}
			});
		}


		function uploadPhoneFile() {

			var f = $("#phoneFile").val();
			if(f==null || f.length==0){
				$.messager.alert("警告","请选择号码文件,再执行上传!","error");
				return;
			}
			
			$("#setup-uploadFileForm").form('submit',{
				url:'callTask/uploadFile?taskId=' + currTaskId,
				onSubmit:function() {
					$.messager.progress({
						msg:'系统正在处理，请稍候...',
						interval:3000
					});

					return true;
				},
				success:function(data) {
					$.messager.progress('close');
					
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode=='success') {
						$("#callTelephoneDg").datagrid({url:'callTelephone/datagrid?taskId='+currTaskId});
						
						//清除上传文件表单内容
						var file = $("#phoneFile");
						file.after(file.clone().val(""));
						file.remove();
						$("#callTaskDg").datagrid({url:'callTask/datagrid'});;
					}
				}
			});
		}

		function saveTelephone() {
			$("#setup-addTelephone").form('submit',{
				url:'callTask/addTelephone?taskId=' + currTaskId,
				onSubmit:function () {
					return $(this).form('validate');
				},
				success:function(data) {
					var result = JSON.parse(data);    //解析Json 数据

					var statusCode = result.statusCode; //返回的结果类型
					var message = result.message;       //返回执行的信息

					window.parent.showMessage(message,statusCode);
					if(statusCode=='success') {
						$("#callTelephoneDg").datagrid({url:'callTelephone/datagrid?taskId='+currTaskId});
						$("#callTaskDg").datagrid({url:'callTask/datagrid'});;
						$("#setup-addTelephone").form('clear');
					}
				}
			});
		}	

		function saveAuth() {
			$.messager.confirm('提示','你确定要更改授权吗?',function(r){
				if(r) {
					$.ajax({
						url:'callTask/auth?taskId=' + currTaskId + '&ids=' + getAuthOperId(),
						method:'POST',
						dataType:'json',
						success:function(rs) {
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);
						}
					});
				}
			});
		}

		//取得选中的授权工号			
		function getAuthOperId() {
			
			var nodes = $("#opertorUl").tree('getChecked');
			var ids = [];
			for(var i=0; i<nodes.length; i++){
				ids.push(nodes[i].id);
			}
			return	ids.join(",");			
		}
		
		function add_cancel() {
			$("#addTaskDlg").dialog('close');
		}

		//-----------------任务状态控制---------------------------------
		//控制任务的状态，type：1启动; 2暂停; 3停止; 4删除任务; 5标注为历史,6回收已经分配的号码
		function ctrlTaskStatus(type) {    
			var node = $("#callTaskDg").datagrid("getSelected");
			if(node==null) {
				$.messager.alert('警告','操作失败,请先选择任务', 'error');
			}
			var taskId = node.CT_ID;
			var taskName = node.TASK_NAME;
			var state = node.TASK_STATE;

			var notice = "";
			if(type==1) {
				notice = "确定 <span style='font-weight:bold;color:red;'> 启动 </span>【" + taskName + "】外呼任务吗?";
			}else if(type==2) {
				notice = "确定 <span style='font-weight:bold;color:red;'> 暂停</span>【" + taskName + "】外呼任务吗?暂停任务时，已经请求的数据将不会清除!";
			}else if(type==3) {
				notice = "确定 <span style='font-weight:bold;color:red;'> 停止</span> 【" + taskName + "】外呼任务吗?停止任务时，已经请求的数据将会回收!";
			}else if(type==4) {
				notice = "确定 <span style='font-weight:bold;color:red;'> 删除</span>【" + taskName + "】外呼任务吗?";
			}else if(type==5) {
				notice = "确定要将 <span style='font-weight:bold;color:red;'>" + taskName + "</span> 标注为历史任务吗?标注为历史任务，表示该任务将会无法恢复。";
			}else if(type==6) {
				notice = "确定 <span style='font-weight:bold;color:red;'> 回收</span>【" + taskName + "】外呼任务已经分配的号码吗?";
			}
			
			$.messager.confirm('提示',notice,function(r){
				if(r) {
					$.messager.progress({
						msg:'系统正在处理，请稍候...',
						interval:2000
					});
					$.ajax({
						url:'callTask/changeState?taskId=' + taskId + '&state=' + state + '&type=' + type,
						method:'POST',
						dataType:'json',
						success:function(rs) {
							$.messager.progress('close');
							var statusCode = rs.statusCode; //返回的结果类型
							var message = rs.message;       //返回执行的信息
							window.parent.showMessage(message,statusCode);

							if(statusCode=='success') {     //如果修改成功时，要更新任务列表
								$('#callTaskDg').datagrid({url:'callTask/datagrid'});
							}
						}
					});
				}
			});
				
			
		}
		
		//-----------------任务状态控制结束---------------------------------
		
		function getPieChart(renderIdInfo,titleInfo,nameInfo) {
		
		chart = new Highcharts.Chart({
	        chart: {
	        	renderTo:renderIdInfo,
	            plotBackgroundColor: null,
	            plotBorderWidth: null,
	            plotShadow: false,
	            type: 'pie'
	        },
	        credits:{
	     	    enabled:false
	        },
	        colors:[
	    	    "#42BDF6",
	    	    "#FFCE95",
	    	    "#96FEA9",
	    	    "#EB1EC2"
	    	],
	        title: {
	            text: titleInfo
	        },
	        tooltip: {
	            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
	        },
	        plotOptions: {
	            pie: {
	                allowPointSelect: true,
	                cursor: 'pointer',
	                size:100,
	                dataLabels: {
	                    enabled: false,
	                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
	                    style: {
	                        color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
	                    },
	                    connectorColor: 'silver'
	                },
	                showInLegend: true
	            }
	        },
	        series: [{
	            name: nameInfo,
	            colorByPoint: true,
	            type: 'pie'
	        }]
	    });

	    return chart;
	}
			
	</script>	
</head>

<body>
	<div class="easyui-panel" title="外呼任务管理" data-options="fit:true" style="padding:1px;">
		<div data-options="fit:true" class="easyui-layout">
			<!-- 查询区部分 -->
			<div data-options="region:'north',split:true,border:true" style="height:70px">
				<table>
								<tr>
									<td>项目名称</td>
									<td>
										<input width="30" id="taskName" name="taskName" class="easyui-textbox"/>
									</td>
									
									<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										项目状态</td>
									<td>
										<select class="easyui-combobox" style="width: 155px;" id="taskState" data-options="panelHeight:'auto'">
								              <option value="5">请选择</option>
								              <option value="0">新任务</option>
								              <option value="1">已启动</option>
								              <option value="2">已暂停</option>
								              <option value="3">已完成</option>
								        </select> 
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
										&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
										<a href="javascript:findData()"  class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>
									</td>
								</tr>
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
					<table id="callTaskDg">
							<thead>  
								<tr style="height:12px;">                
									<th data-options="field:'ckct',checkbox:true"></th>                       
									<th data-options="field:'TASK_NAME',width:200,align:'center'">项目名称</th>                
									<th data-options="field:'TASK_STATE',width:80,align:'center',formatter:stateformatter">项目状态</th>                
									<th data-options="field:'CALLERID',width:120,align:'center'">主叫号码</th>
									<th data-options="field:'TOTAL',width:80,align:'center'">号码总量</th>
									<th data-options="field:'UNDISTRIBUTION',width:80,align:'center'">未分配</th>
									<th data-options="field:'DISTRIBUTION',width:80,align:'center'">已分配</th>
									<th data-options="field:'CT_ID',width:80,align:'center',formatter:taskcounterdetailformatter">已分配详情</th>
									<th data-options="field:'CREATE_TIME',width:160,align:'center'">创建日期</th>                
									<th data-options="field:'CREATE_USERCODE',width:120,align:'center'">创建人</th>
									<th data-options="field:'id',width:60,align:'center',formatter:rowformatter">操作</th>                             
								</tr>        
							</thead>
					</table>					

			</div>
	
			<div id="searchtool" style="padding:5px">  
<!--					<div>	-->
<!--						<a href="#" id="easyui-add" onclick="taskAdd()" class="easyui-linkbutton" iconCls="icon-add" plain="true">添加任务</a>-->
<!--					</div>-->
					<div style="display:inline;">
						<button id="addBtn" onclick="taskAdd()">添加任务</button>
					</div>
					<div style="display:inline;position:absolute;right:0px;" >
						<button id="startBtn"  onclick="ctrlTaskStatus(1)">启动</button>
						<button id="pauseBtn" onclick="ctrlTaskStatus(2)">暂停</button>
						<button id="stopBtn" onclick="ctrlTaskStatus(3)">停止</button>
						<button id="delBtn" onclick="ctrlTaskStatus(4)">删除任务</button>
						<button id="historyBtn" onclick="ctrlTaskStatus(5)">标注历史任务</button>
						<button id="reuseBtn" onclick="ctrlTaskStatus(6)">回收已分配号码</button>
					</div>
			 <div>
		</div>
	</div>

<div id="addTaskDlg" class="easyui-dialog" style="width:580px;height:250px;padding:10px 20px;" modal="true" closed="true" buttons="#addTaskDlgBtn">

	<form id="taskForm" method="post">
		<!-- 包含表单 -->
		<%@ include file="/call/calltask/_form.jsp"%>
	</form>	
</div>

<div id="setupTaskDlg" class="easyui-dialog" style="width:950px;height:463px;padding:1px 1px;" modal="true" closed="true">
	<!-- 包含表单 -->
	<%@ include file="/call/calltask/_setup.jsp"%>
</div>



</body>
</html>

