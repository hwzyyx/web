<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script type="text/javascript">

//各项数据的数据量及占比

var totalCount = 0;
var totalRate = 100;
var state0Count = 0;
var state1Count = 0;
var state1Rate = 0;
var state2Count = 0;
var state2Rate = 0;
var state3Count = 0;
var state3Rate = 0;
var state4Count = 0;
var state4Rate = 0;


//*根据各项数据的数据量及占比情况，组织汇总数据，并在 summaryDg 的 datagrid 中显示
function getSummaryData() {
	
	var summaryData = '{"total":2,"rows":[';
	summaryData += '{"category":"数量","totalData":' + totalCount + ',"state1Data":' + state1Count + ',"state2Data":' + state2Count + ',"state3Data":' + state3Count + ',"state4Data":' + state4Count + '},';
	summaryData += '{"category":"占比","totalData":"' + totalRate + '%' + '","state1Data":"' + state1Rate  + '%' + '","state2Data":"' + state2Rate  + '%' + '","state3Data":"' + state3Rate  + '%' + '","state4Data":"' + state4Rate  + '%' + '"}';
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
			var seriesData = [];
			var j = 0;
			
			for(var i=0;i<rs.length;i++) {
				legendData[i]=rs[i].name;  //将数据推给定义的数组对象
				var name = rs[i].name;
				
				var map = {};
				map.name = rs[i].name;
				map.value = rs[i].value;
				
				if(name=='已载入' || name=='已成功' || name=='待重呼' || name=='已失败') {
					totalCount += rs[i].value;
					seriesData[j] = map;
					j++;
				}else if(name='未处理') {
					state0Count = rs[i].value;
				}
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
						return  name + space + "(占比 ：" + ppV + "%)";
					}
				},
				title:{
					text:'任务名称:' + currTaskName,
					subtext:"任务号码总量：" + (totalCount + state0Count) + ",已呼数量：" + totalCount + ",未呼数量：" + state0Count,
				},
				series:[{
					data:seriesData
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
			param.taskName = currTaskName
		},
		success:function(data) {
			
		}
	});
}

function tota1DataFormatter(value,data,index){ return "<span style='font-weight:bolder'>" + value + "</span>"; }
function state1DataFormatter(value,data,index){ return "<span style='color:#f8d013;font-weight:bolder'>" + value + "</span>"; }
function state2DataFormatter(value,data,index){ return "<span style='color:#00ff00;font-weight:bolder'>" + value + "</span>"; }
function state3DataFormatter(value,data,index){ return "<span style='color:#fc00ff;font-weight:bolder'>" + value + "</span>"; }
function state4DataFormatter(value,data,index){ return "<span style='color:#ff0000;font-weight:bolder'>" + value + "</span>"; }
	
</script>

<div id="container" style="height:450px;width:1000px;"></div>

<!-- 数据汇总区 -->
<div class="easyui-tabs" style="width:1000px;height:180px;margin-left:10px;">
	<div title="数据统计汇总" style="padding:10px">
		
	<table id="summaryDg" data-options="fit:true,singleSelect:true,rownumbers:false">
		<thead>
			<tr style="font-weight: bold;">
				<th data-options="field:'category',width:150,align:'center'"></th>
				<th data-options="field:'totalData',width:150,align:'center',formatter:tota1DataFormatter">已呼数量</th>
				<th data-options="field:'state1Data',width:150,align:'center',formatter:state1DataFormatter">已载入</th>
				<th data-options="field:'state2Data',width:150,align:'center',formatter:state2DataFormatter">已成功</th>
				<th data-options="field:'state3Data',width:150,align:'center',formatter:state3DataFormatter">待重呼</th>
				<th data-options="field:'state4Data',width:150,align:'center',formatter:state4DataFormatter">已失败</th>
			</tr>
		</thead>
	</table>
		
	</div>
</div>

<div id="summaryDgTool" style="padding:5px;">
	<a href="#" id="easyui-export" onclick="summaryExport()" class="easyui-linkbutton" iconCls='icon-redo' plain="true">导出汇总数据</a>
</div>
<div id="telephoneopertool2" style="padding:5px;">
	<div>
		<a href="#" id="easyui-add2" onclick="autoCallTaskTelephoneExport2()" class="easyui-linkbutton" iconCls='icon-redo' plain="true">导出号码</a>
	</div>
</div>

<!-- 简单汇总 -->
<div class="easyui-panel" title="" style="width:150px;height:100px;font-size:22px;font-weight:bolder;" data-options="style:{position:'absolute',left:437,top:230}">
	<span style="padding-left:10px;">已呼数量：<span id="totalCountSpan"></span></span><br/>
	<span style="padding-left:10px;">成功数量：<span id="state2CountSpan"></span></span><br/>
	<span style="padding-left:10px;color:#00ff00;font-size: 16px;">成&nbsp;功&nbsp;率：<span id="state2RateSpan">%</span>
</div>

<<script type="text/javascript">
	var dom = document.getElementById("container");
	myChart = echarts.init(dom);
	
	var app = {};
	option = {
		    color: ['#f8d013','#00ff00', '#fc00ff', '#ff0000', '#fb5c5c','#fa1616'],
		    title: {
		    	text:'任务名称：' + currTaskName,
		    	subtext:"任务号码总量：0,已呼数量：0,未呼数量：0",
		    	show:true,
		    	x:'center',
		    	textStyle:{
		    		fontSize:25
		    	},
		    	subtextStyle:{
		    		fontSize:20,
		    		color:'red',
		    		align:'center'
		    	}
				
		    },
		    tooltip: {
		        trigger: 'item',
		        formatter: "{a} <br/>{b}: {c} ({d}%)"
		    },
		    legend: {
				textStyle: {
					fontSize:15
				},	    	
		        orient: 'vertical',
		        x: 'left',
		        data:['已载入','已成功','待重呼','已失败']
		    },
		    series: [
		        {
		            name:'访问来源',
		            type:'pie',
		            radius: ['40%', '60%'],
		            center:['500px','250px'],
		            avoidLabelOverlap: false,
		            label: {
		                normal: {
		                    show: true,
		                    position: 'left',
		                    formatter:function(params){
		                        pn = params['name'];
		                        pv = params['value'];
		                        pp = params['percent'];
		                        pa = params['data']['a'];
		                        
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
		                    		$("#totalCountSpan").text(totalCount);
		                    		$("#state2CountSpan").text(state2Count);
		                    		$("#state2RateSpan").text(state2Rate + " %");
		                    	}
		                        
		                        return '{a|' + '呼叫结果' + '}{abg|}\n{hr|}\n  {b|' + pn + ': }' + pv + '  {per|' + pp + '%}';
		                        
		                    },
		                    backgroundColor: '#eee',
		                    borderColor: '#aaa',
		                    borderWidth: 1,
		                    barBorderRadius: 4,
		                    rich:{
		                        a:{
		                            fontSize:16,
		                            lineHeight:33,
		                            align:'center'
		                        },
		                        hr:{
		                            borderColor: '#aaa',
		                            width: '100%',
		                            borderWidth: 0.5,
		                            height:0
		                        },
		                        b:{
		                            fontSize:16,
		                            lineHeight:33
		                        },
		                        per:{
		                            color:'#eee',
		                            backgroundColor:'#334455',
		                            padding:[2,4],
		                            barBorderRadius:2
		                        }
		                        
		                    }
		                },
		                emphasis: {
		                    show: true,
		                    textStyle: {
		                        fontSize: '16',
		                        fontWeight: 'bold'
		                    }
		                }
		            },
		            labelLine: {
		                normal: {
		                    show: true
		                }
		            },
		            data:[
		                {value:0, name:'已载入'},
		                {value:0, name:'已成功'},
		                {value:0, name:'待重呼'},
		                {value:0, name:'已失败'},
		            ]
		        }
		    ]
		};
	if (option && typeof option === "object") {
	    myChart.setOption(option, true);
	}
	
	//填充汇总数据
	$('#summaryDg').datagrid({toolbar:'#summaryDgTool'}).datagrid('loadData',getSummaryData());
	
	
	myChart.on('dblclick',function(params){
		
		var name = params.name;
		var value = params.value;
		
		//为了减少系统的开支,对于数据值为0时,不查询数据列表
		if(value == 0) {
			window.parent.showMessage("温馨提示：当前选择的 " + name + " 数据量为0,订单列表暂不显示!","ERROR");
			return;
		}
		
		var title = "任务：" + currTaskName;
		
		if(name=='已载入') {
			title += ",呼叫状态：已载入";
			conditionState = 1;
		}else if(name=='已成功') {
			title += ",呼叫状态：已成功";
			conditionState = 2;
		}else if(name=='待重呼') {
			title += ",呼叫状态：待重呼";
			conditionState = 3;
		}else if(name=='已失败') {
			title += ",呼叫状态：已失败";
			conditionState = 4;
		}
		
		//alert("name=" + name + ",value=" + value + ",conditionState=" + conditionState);
		
		title += " 的号码列表";		
		findDataForTelephoneFor2();
		
		$("#autoCallTelephoneDlg").dialog('setTitle',title).dialog('open');		
		
	});
	
</script>

