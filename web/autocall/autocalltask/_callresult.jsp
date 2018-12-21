<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script type="text/javascript">

//各项数据的数据量及占比

//各项数据的数据量及占比
     var totalCount = 0;
     var totalRate = 100;
     var state1Count = 0;
     var state1Rate = 0;
     var state2Count = 0;
     var state2Rate = 0;
     var state3Count = 0;
     var state3Rate = 0;
     var state4Count = 0;
     var state4Rate = 0;
     var lastCallResult1Count = 0;
     var lastCallResult1Rate = 0;
     var lastCallResult2Count = 0;
     var lastCallResult2Rate = 0;
     var lastCallResult3Count = 0;
     var lastCallResult3Rate = 0;
     var lastCallResult4Count = 0;
     var lastCallResult4Rate = 0;

//*根据各项数据的数据量及占比情况，组织汇总数据，并在 summaryDg 的 datagrid 中显示
function getSummaryData() {
	
	var summaryData = '{"total":3,"rows":[';
	summaryData += '{"category":"数量","totalData":' + totalCount + ',"state1Data":' + state1Count + ',"state2Data":' + state2Count + ',"state3Data":' + state3Count + ',"state4Data":' + state4Count + ',"lastCallResult2Data":' + lastCallResult2Count + ',"lastCallResult3Data":' + lastCallResult3Count + ',"lastCallResult4Data":' + lastCallResult4Count + '},';
	summaryData += '{"category":"占比","totalData":"' + totalRate + '%' + '","state1Data":"' + state1Rate  + '%' + '","state2Data":"' + state2Rate  + '%' + '","state3Data":"' + state3Rate  + '%' + '","state4Data":"' + state4Rate  + '%' + '","lastCallResult2Data":"' + lastCallResult2Rate  + '%' + '","lastCallResult3Data":"' + lastCallResult3Rate  + '%' + '","lastCallResult4Data":"' + lastCallResult4Rate  + '%' + '"},';
	summaryData += '{"category":"","totalData":"' + "" + '","state1Data":"' + '导出' + '","state2Data":"' + '导出' + '","state3Data":"' + '导出' + '","state4Data":"' + '导出' + '","lastCallResult2Data":"' + '导出' + '","lastCallResult3Data":"' + '导出' + '","lastCallResult4Data":"' + '导出' + '"}';
	summaryData += "]}";
	
	return JSON.parse(summaryData);
}

//重载统计数据
function reloadStatistics() {
	
	totalCount = 0;
	$.messager.progress({
		msg:'系统正在处理，请稍候...',
		interval:3000
	});
	
	$.ajax({

		url:'autoCallTask/reloadStatistics?taskId=' + currTaskId,
		method:'post',
		dataType:'json',
		success:function(rs) {
			$.messager.progress("close");
			var legendData = [];
			var seriesData1 = [];
			var seriesData2 = [];
			var j = 0;
			var k = 0;
			//alert("rs.length的数量:" + rs.length);
			console.log(rs);
			for(var i=0;i<rs.length;i++) {
				legendData[i]=rs[i].name;  //将数据推给定义的数组对象
				var name = rs[i].name;
				
				var map = {};
				map.name = rs[i].name;
				map.value = rs[i].value;
				
				if(name=='已载入' || name=='已成功' || name=='待重呼' || name=='已失败') {
					totalCount += rs[i].value;
					seriesData1[j] = map;
					j++;
				}else if(name=='未处理') {
					state0Count = rs[i].value;
				}
				
				if(name=='已载入' || name=='已成功' || name=='无应答' || name=='客户忙' || name=='请求错误') {
					seriesData2[k] = map;
					k++;
				}
				//alert("name:" + rs[i].name + ",value:" + rs[i].value + ",name:" + name);
				
			}
			
			myChart.setOption({
				legend:{
					data:legendData,
					textStyle: {
						fontSize:14
					},
					formatter: function(name) {
						var pvV;
						var ppV;
						var space = null;
						
						if(name=='已载入') {   pvV = state1Count;  ppV = state1Rate;   space='       '}
						else if(name=='已成功') {pvV = state2Count;  ppV = state2Rate; space='       '}
						else if(name=='待重呼') {pvV = state3Count;  ppV = state3Rate; space='        '}
						else if(name=='已失败') {pvV = state4Count;  ppV = state4Rate; space='        '}
						else if(name=='无应答') {pvV = lastCallResult2Count; ppV = lastCallResult2Rate; space='       '}
						else if(name=='客户忙') {pvV = lastCallResult3Count; ppV = lastCallResult3Rate; space='       '}
						else if(name=='请求错误') {pvV = lastCallResult4Count; ppV = lastCallResult4Rate; space='       '}
						return  name + space + "(占比 ：" + ppV + "%)";
					}
				},
				title:{
					text:'任务名称:' + currTaskName,
					subtext:"任务号码总量：" + (totalCount + state0Count) + ",已呼数量：" + totalCount + ",未呼数量：" + state0Count,
				},
				series:[{
					data:seriesData1
				},{
					data:seriesData2
				}]
			});
			
			//填充汇总数据
			$('#summaryDg').datagrid('loadData',getSummaryData());
			
			//修改条目中的数据
			if(totalCount == 0) {
				window.parent.showMessage("温馨提示：当前任务暂未查询到已外呼数据，已经执行外呼数据总量为0!未处理数据有：" + state0Count + " 条" ,"ERROR");
			}
		}
		
	});
}

function summaryExport() {
	$("#exportForm").form('submit',{
		url:'autoCallTask/exportExcelForSummaryData',
		onSubmit:function(param) {
			param.totalCount = totalCount,
			param.totalRate = totalRate,
			param.state1Count = state1Count,
			param.state1Rate = state1Rate,
			param.state2Count = state2Count,
			param.state2Rate = state2Rate,
			param.state3Count = state3Count,
			param.state3Rate = state3Rate,
			param.state4Count = state4Count,
			param.state4Rate = state4Rate,
			param.lastCallResult2Count = lastCallResult2Count,
			param.lastCallResult2Rate = lastCallResult2Rate,
			param.lastCallResult3Count = lastCallResult3Count,
			param.lastCallResult3Rate = lastCallResult3Rate,
			param.lastCallResult4Count = lastCallResult4Count,
			param.lastCallResult4Rate = lastCallResult4Rate,
			param.taskName = currTaskName
		},
		success:function(data) {
			
		}
	});
}

function tota1DataFormatter(value,data,index){
	if(value=="导出") {
			return "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=-1'>导出" + "</a>";
	}else {
		return "<span style='font-weight:bolder'>" + value + "</span>"; 
	}
}
function state1DataFormatter(value,data,index){
	if(value=="导出") {
		return "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=1'>导出" + "</a>";
	}else {
		return "<span style='color:#f8d013;font-weight:bolder'>" + value + "</span>"; 
	}
}
function state2DataFormatter(value,data,index){
	if(value=="导出") {
		return "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=2'>导出" + "</a>";
	}else {
		return "<span style='color:#00ff00;font-weight:bolder'>" + value + "</span>"; 
	}
}
function state3DataFormatter(value,data,index){
	if(value=="导出") {
		return "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3'>导出" + "</a>";
	}else {
		return "<span style='color:#fc00ff;font-weight:bolder'>" + value + "</span>"; }
	}
function state4DataFormatter(value,data,index){ 
	if(value=="导出") {
		return "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=4'>导出" + "</a>";
	}else {
		return "<span style='color:#ff0000;font-weight:bolder'>" + value + "</span>"; 
	}
}

function lastCallResult2DataFormatter(value,data,index) {
	if(value=="导出") {
		return "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&lastCallResult=2'>导出" + "</a>";
	}else {
		return "<span style='color:#ff0000;font-weight:bolder'>" + value + "</span>"; 
	}
}

function lastCallResult3DataFormatter(value,data,index) {
	if(value=="导出") {
		return "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&lastCallResult=3'>导出" + "</a>";
	}else {
		return "<span style='color:#ff0000;font-weight:bolder'>" + value + "</span>"; 
	}
}

function lastCallResult4DataFormatter(value,data,index) {
	if(value=="导出") {
		return "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&lastCallResult=4'>导出" + "</a>";
	}else {
		return "<span style='color:#ff0000;font-weight:bolder'>" + value + "</span>"; 
	}
}
	
</script>

<div id="container" style="height:450px;width:1000px;"></div>

<!-- 数据汇总区 -->
<div class="easyui-tabs" style="width:1000px;height:210px;margin-left:10px;">
	<div title="数据统计汇总" style="padding:10px">
		
	<table id="summaryDg" data-options="fit:true,singleSelect:true,rownumbers:false">
		<thead>
			<tr style="font-weight: bold;">
				<th data-options="field:'category',width:100,align:'center'"></th>
				<th data-options="field:'totalData',width:100,align:'center',formatter:tota1DataFormatter">已呼数量</th>
				<th data-options="field:'state1Data',width:100,align:'center',formatter:state1DataFormatter">已载入</th>
				<th data-options="field:'state2Data',width:100,align:'center',formatter:state2DataFormatter">已成功</th>
				<th data-options="field:'state3Data',width:100,align:'center',formatter:state3DataFormatter">待重呼</th>
				<th data-options="field:'state4Data',width:100,align:'center',formatter:state4DataFormatter">已失败</th>
				<th data-options="field:'lastCallResult2Data',width:100,align:'center',formatter:lastCallResult2DataFormatter">无应答</th>
				<th data-options="field:'lastCallResult3Data',width:100,align:'center',formatter:lastCallResult3DataFormatter">客户忙</th>
				<th data-options="field:'lastCallResult4Data',width:100,align:'center',formatter:lastCallResult4DataFormatter">请求错误</th>
			</tr>
		</thead>
	</table>
		
	</div>
</div>

<div id="summaryDgTool" style="padding:5px;">
	<a href="#" id="easyui-export" onclick="summaryExport()" class="easyui-linkbutton" iconCls='icon-redo' plain="true">导出汇总数据</a>
	<a href="#" id="easyui-reload-data" style="margin-left:700px;" onclick="reloadStatistics()" class="easyui-linkbutton" iconCls='icon-reload' plain="true">重载数据</a>
</div>
<div id="telephoneopertool2" style="padding:5px;">
	<div>
		<a href="#" id="easyui-add2" onclick="autoCallTaskTelephoneExport2()" class="easyui-linkbutton" iconCls='icon-redo' plain="true">导出号码</a>
	</div>
</div>

<!-- 简单汇总 -->
<div class="easyui-panel" title="" style="display:none;width:160px;height:100px;" data-options="style:{position:'absolute',left:432,top:230}">
	<br/><span style="padding-left:10px;font-weight:bolder;">已呼数量：<span id="totalCountSpan"></span></span><br/>
	<span style="padding-left:10px;font-weight:bolder;">成功数量：<span id="state2CountSpan"></span></span><br/><br/>
	<span style="padding-left:10px;color:#00ff00;font-size: 16px;font-weight:bolder;">成功率：<span id="state2RateSpan">%</span></span>
</div>

<script type="text/javascript">
	var dom = document.getElementById("container");
	myChart = echarts.init(dom);
	var app = {};
	option = null;
	app.title = '嵌套环形图';
	
	option = {
		//color: ['#f8d013','#00ff00', '#fc00ff', '#ff0000','#07b3fa','#55cafa','#8cdcfc','#666666', '#001100'],
		color: ['#f8d013','#00ff00', '#fc00ff', '#ff0000','#ea9595','#ec5a5a','#f10303','#666666', '#001100'],
		title:{
			text:'BSH外呼系统时间区间内的外呼情况展示',
			subtext:'时间区间:2018-05-01 00:00:00 至 2018-05-02 00:00:00',
			show:true,
			x:'center',
			align:'center',
			textStyle:{
				fontSize:24
			},
			subtextStyle:{
				fontSize:16,
				color:'red',
				align:'center'
			}
		},
		toolbox:{
			feature:{
				saveAsImage:{
					type:'png',
					name:'pie-nest',
					show:true,
					title:'保存',
					pixelRatio:3,
					iconStyle:{
						normal:{
							textPosition:'top',
							textAlign:'left'
						}
						
					}
				}
			}
		},
		tooltip: {
	        trigger: 'item',
	        formatter: "{a} <br/>{b}: {c} ({d}%)"
	    },
	    legend: {
	        orient: 'vertical',
	        x: 'left',
	        data:['已载入','已成功','待重呼','已失败','无应答','客户忙','请求错误']
	    },
	    series: [
	        {
	            name:'访问来源',
	            type:'pie',
	            selectedMode: 'single',
	            radius: [0, '60%'],
				center:['500px','280px'],
	            label: {
	                normal: {
	                    position: 'center',
	                    verticalAlign:'middle',
	                    show: true,
	                    //formatter:'{b}:{c}{d}%'
	                    formatter:function(params) {
	                    	pn = params['name'];
	                    	pv = params['value'];
	                    	pp = params['percent'];
	                    	pd = params['data'];
	                    	pd0 = pd['0'];
	                    	
	                    	//为数据统计汇总表格赋值
	                    	if(pn == '已载入') {
	                    		state1Count = pv;
	                    		state1Rate = pp;
	                    	}else if(pn == '已成功') {
	                    		state2Count = pv;
	                    		state2Rate = pp;
	                    	}else if(pn == '待重呼') {
	                    		state3Count = pv;
	                    		state3Rate = pp;
	                    	}else if(pn == '已失败') {
	                    		state4Count = pv;
	                    		state4Rate = pp;
	                    	}
	                    	
	                    	if(pn == '已成功') {
	                    		//return pn + ":" + pv + "\n 成功率:" + pp + "%";
	                    		//return "{title|" + pn + "}";
	                    		//
	                    		//return "总呼叫量:300 \n 呼叫成功:200 \n 成功率: 33.33% \n 数量:" + pd0;
	                    		//return '{title|情况汇总{abg|}} \n {stateHead|状态}{valueHead|数量}{rateHead|占比} \n {hr|}';
	                    		return '{abg|成功率情况}\n {hr|}\n {totalHead|总   量：' + totalCount + '\n}{successHead|成   功：' + pv + '\n}{rateHead|成功率：' + pp + '%} \n'
	                    	}else {
	                    		return "";
	                    	}
	                    },
	                    backgroundColor: 'transparent',
	                    borderColor: '#0000ff',
	                    borderWidth: 2,
	                    borderRadius: 4,
	                    rich: {
	                        abg: {
	                        	backgroundColor: 'transparent',
	                            width: 150,
	                            align: 'center',
	                            height: 25,
	                            borderRadius: [40, 40, 0, 0],
	                            color: '#0000ff',
	                            //textShadowColor: '#666666',
	                            //textShadowOffsetX: 0,
	                            //textShadowOffsetY: 0,
	                            //fontWeight: 'bolder',
	                            fontSize: 16,
	                            padding:[0,0,2,0]
	                        },
	                        
	                        hr: {
	                            borderColor: '#0000ff',
	                            width: 150,
	                            borderWidth: 2,
	                            height: 0,
	                            align: 'left'
	                        },
	                        totalHead: {
	                            height: 24,
	                            align: 'left',
	                            fontSize: 16,
	                            //fontWeight: 'bolder',
	                            color: '#0000ff'
	                        },
	                        successHead: {
	                            width: 20,
	                            padding: [0, 0, 5, 0],
	                            fontSize: 16,
	                            //fontWeight: 'bolder',
	                            color: '#0000ff'
	                        },
	                        
	                        rateHead: {
	                            width: 40,
	                            align: 'center',
	                            padding: [0, 0, 10, 0],
	                            fontSize: 16,
	                            //fontWeight: 'bolder',
	                            color: '#0000ff'
	                        }
	                        
	                    }
	                }
	            },
	            labelLine: {
	                normal: {
	                    show: false
	                }
	            },
	            data:[
	                {value:0, name:'已载入'},
	                {value:0, name:'已成功'},
	                {value:0, name:'待重呼'},
	                {value:0, name:'已失败'},
	            ]
	        },
	        {
	            name:'访问来源',
	            type:'pie',
	            radius: ['65%', '75%'],
	            center:['500px','280px'],
	            label: {
	                normal: {
	                	formatter: function(params) {
	                    	pn = params['name'];
	                    	pv = params['value'];
	                    	pp = params['percent'];
	                    	pa = params['data']['a'];
	                    	
	                    	var pnEnglish = null;    //项目转英文翻译
	                    	
	                    	if(pn=='无应答') {
	                    		lastCallResult2Count = pv;
	                    		lastCallResult2Rate = pp;
	                    		pnEnglish = '无应答';
	                    	}else if(pn=='客户忙') {
	                    		lastCallResult3Count = pv;
	                    		lastCallResult3Rate = pp;
	                    		pnEnglish = '客户忙';
	                    	}else if(pn=='请求错误') {
	                    		lastCallResult4Count = pv;
	                    		lastCallResult4Rate = pp;
	                    		pnEnglish = '请求错误';
	                    	}else if(pn=='已载入') {
	                    		pnEnglish = '已载入';
	                    	}else if(pn=='待重呼') {
	                    		pnEnglish = '待重呼';
	                    	}else if(pn=='已失败') {
	                    		pnEnglish = '已失败';
	                    	}else if(pn=='已成功') {
	                    		pnEnglish = '已成功';
	                    	}
	                    	
	                    	return '{a|' + pnEnglish + '}{abg|}\n{hr|}\n  {b|' + pn + '：}' + pv + '  {per|' + pp + '%}  ';
	                    	
	                    },
	                    backgroundColor: '#eee',
	                    borderColor: '#aaa',
	                    borderWidth: 1,
	                    borderRadius: 4,
	                    // shadowBlur:3,
	                    // shadowOffsetX: 2,
	                    // shadowOffsetY: 2,
	                    // shadowColor: '#999',
	                    // padding: [0, 7],
	                    rich: {
	                        a: {
	                        	fontSize: 16,
	                            lineHeight: 33,
	                            align: 'center'
	                        },
	                        // abg: {
	                        //     backgroundColor: '#333',
	                        //     width: '100%',
	                        //     align: 'right',
	                        //     height: 22,
	                        //     borderRadius: [4, 4, 0, 0]
	                        // },
	                        hr: {
	                            borderColor: '#aaa',
	                            width: '100%',
	                            borderWidth: 0.5,
	                            height: 0
	                        },
	                        b: {
	                            fontSize: 16,
	                            lineHeight: 33
	                        },
	                        per: {
	                            color: '#eee',
	                            backgroundColor: '#334455',
	                            padding: [2, 4],
	                            borderRadius: 2
	                        }
	                    }
	                }
	            },
	            data:[
	                {value:0, name:'已载入'},
	                {value:0, name:'已成功'},
	                {value:0, name:'无应答'},
	                {value:0, name:'客户忙'},
	                {value:0, name:'请求错误'}
	            ]
	        }
	    ]
	};
	if (option && typeof option === "object") {
	    myChart.setOption(option, true);
	}
	
	myChart.on('dblclick',function(params){
		var name = params.name;
		var value = params.value;
		
		//为了减少系统的开支,对于数据值为0时,不查询数据列表
		if(value == 0) {
			window.parent.showMessage("温馨提示：当前选择的 " + name + " 数据量为0,号码列表暂不显示!","ERROR");
			return;
		}
		
		var title = "任务：" + currTaskName;
		
		if(name=='已载入') {
			title += ",呼叫结果：已载入";
			conditionState = 1;
			conditionLastCallResult = null;
		}else if(name=='已成功') {
			title += ",呼叫结果：已成功";
			conditionState = 2;
			conditionLastCallResult = null;
		}else if(name=='待重呼') {
			title += ",呼叫结果：待重呼";
			conditionState = 3;
			conditionLastCallResult = null;
		}else if(name=='已失败') {
			title += ",呼叫结果：已失败";
			conditionState = 4;
			conditionLastCallResult = null;
		}else if(name=='无应答') {
			title += ",呼叫状态：无应答";
			conditionState = "3,4";
			conditionLastCallResult = 2;
		}else if(name=='客户忙') {
			title += ",呼叫状态：客户忙";
			conditionState = "3,4";
			conditionLastCallResult = 3;
		}else if(name=='请求错误') {
			title += ",呼叫状态：请求错误";
			conditionState = "3,4";
			conditionLastCallResult = 4;
		}
		
		//alert("name=" + name + ",value=" + value + ",conditionState=" + conditionState);
		
		title += " 的号码列表";		
		findDataForTelephoneFor2();
		
		//在打开之前，先做些显示和隐藏的操作
		showExtraTabsFor2();
		
		$("#autoCallTelephoneDlg").dialog('setTitle',title).dialog('open');		
		
	});
	
</script>

