<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script type="text/javascript">

//各项数据的数据量及占比

//各项数据的数据量及占比
     var totalCount = 0;
     var totalRate = 100;
     var state0Count = 0;
     var state0Rate = 0;
     var state1Count = 0;
     var state1Rate = 0;
     var state2Count = 0;
     var state2Rate = 0;
     var state3Count = 0;
     var state3Rate = 0;
     var state4Count = 0;
     var state4Rate = 0;
     var hangupCause1Count = 0;
     var hangupCause1Rate = 0;
     var hangupCause16Count = 0;
     var hangupCause16Rate = 0;
     var hangupCause19Count = 0;
     var hangupCause19Rate = 0;
     var hangupCause34Count = 0;
     var hangupCause34Rate = 0;
     var hangupCause38Count = 0;
     var hangupCause38Rate = 0;
     var hangupCause401Count = 0;
     var hangupCause401Rate = 0;
     var hangupCause402Count = 0;
     var hangupCause402Rate = 0;
     var hangupCause403Count = 0;
     var hangupCause403Rate = 0;
     
//*根据各项数据的数据量及占比情况，组织汇总数据，并在 summaryDg 的 datagrid 中显示
function getSummaryData() {
	
	var summaryData = '{"total":3,"rows":[';
	summaryData += '{"category":"数量","totalData":' + totalCount + ',"state1Data":' + state1Count + ',"state2Data":' + state2Count + ',"state3Data":' + state3Count + ',"state4Data":' + state4Count + ',"hangupCause1Data":' + hangupCause1Count + ',"hangupCause16Data":' + hangupCause16Count + ',"hangupCause19Data":' + hangupCause19Count + ',"hangupCause34Data":' + hangupCause34Count + ',"hangupCause38Data":' + hangupCause38Count + ',"hangupCause401Data":' + hangupCause401Count + ',"hangupCause402Data":' + hangupCause402Count + ',"hangupCause403Data":' + hangupCause403Count +'},';
	summaryData += '{"category":"占比","totalData":"' + totalRate + '%' + '","state1Data":"' + state1Rate  + '%' + '","state2Data":"' + state2Rate  + '%' + '","state3Data":"' + state3Rate  + '%' + '","state4Data":"' + state4Rate  + '%' + '","hangupCause1Data":"' + hangupCause1Rate  + '%' + '","hangupCause16Data":"' + hangupCause16Rate  + '%' + '","hangupCause19Data":"' + hangupCause19Rate  + '%' + '","hangupCause34Data":"' + hangupCause34Rate  + '%' + '","hangupCause38Data":"' + hangupCause38Rate  + '%' + '","hangupCause401Data":"' + hangupCause401Rate  + '%' + '","hangupCause402Data":"' + hangupCause402Rate  + '%' + '","hangupCause403Data":"' + hangupCause403Rate  + '%' +'"},';
	summaryData += '{"category":"","totalData":"' + "" + '","state1Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=1'>导出" + "</a>" + '","state2Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=2'>导出" + "</a>" + '","state3Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3'>导出" + "</a>" + '","state4Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=4'>导出" + "</a>" + '","hangupCause1Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&hangupCause=1'>导出</a>" + '","hangupCause16Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&hangupCause=16'>导出</a>" + '","hangupCause19Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&hangupCause=19'>导出</a>" + '","hangupCause34Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&hangupCause=34'>导出</a>" + '","hangupCause38Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&hangupCause=38'>导出</a>" + '","hangupCause401Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&hangupCause=401'>导出</a>" + '","hangupCause402Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&hangupCause=402'>导出</a>" + '","hangupCause403Data":"' + "<a href='autoCallTaskTelephone/exportExcel?taskId=" + currTaskId + "&state=3,4&hangupCause=403'>导出</a>" + '"}';
	summaryData += "]}";
	
	//alert(summaryData);
	
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
			
			var j = 0;		//seriesData1Index;
			var k = 0;      //seriesData2Index;
			var z = 0;      //legendIndex;
			var y = 0;
			//alert("rs.length的数量:" + rs.length);
			console.log(rs);
			for(var i=0;i<rs.length;i++) {
				var name = rs[i].name;           //名字
				var purpose = rs[i].purpose;     //数据的用途
				
				var map = {};
				map.name = rs[i].name;
				map.value = rs[i].value;
				
				//如果是外呼状态的数据
				if(purpose=='STATE') {
					if(name!='未处理') {
						totalCount += rs[i].value;        //统计总量
						seriesData1[j] = map;
						j++;
					}else {
						state0Count = rs[i].value;     //取出未外呼数量
					}
				}
				
				if((purpose=='STATE' && (name=='已载入' || name=='已成功')) || (purpose=='HANGUP_CAUSE')) {
					seriesData2[k] = map;
					k++;
				}
				
				if((purpose=='STATE' && name!='未处理') || purpose=='HANGUP_CAUSE') {
					legendData[z] = name;
					z++;
				}
				
				//alert("name:" + rs[i].name + ",value:" + rs[i].value + ",name:" + name);
				
			}
			
			//console.log(legendData);
			//console.log(seriesData1);
			//console.log(seriesData2);
			
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
						
						/*if(name=='已载入') {   pvV = state1Count;  ppV = state1Rate;   space='       '}
						else if(name=='已成功') {pvV = state2Count;  ppV = state2Rate; space='       '}
						else if(name=='待重呼') {pvV = state3Count;  ppV = state3Rate; space='        '}
						else if(name=='已失败') {pvV = state4Count;  ppV = state4Rate; space='        '}
						else if(name=='无应答') {pvV = lastCallResult2Count; ppV = lastCallResult2Rate; space='       '}
						else if(name=='客户忙') {pvV = lastCallResult3Count; ppV = lastCallResult3Rate; space='       '}
						else if(name=='请求错误') {pvV = lastCallResult4Count; ppV = lastCallResult4Rate; space='       '}*/
						return  name;
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
			//$('#summaryDg').datagrid('loadData','');
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
			param.hangupCause1Count = hangupCause1Count,
			param.hangupCause1Rate = hangupCause1Rate,
			param.hangupCause16Count = hangupCause16Count,
			param.hangupCause16Rate = hangupCause16Rate,
			param.hangupCause19Count = hangupCause19Count,
			param.hangupCause19Rate = hangupCause19Rate,
			param.hangupCause34Count = hangupCause34Count,
			param.hangupCause34Rate = hangupCause34Rate,
			param.hangupCause38Count = hangupCause38Count,
			param.hangupCause38Rate = hangupCause38Rate,
			param.hangupCause401Count = hangupCause401Count,
			param.hangupCause401Rate = hangupCause401Rate,
			param.hangupCause402Count = hangupCause402Count,
			param.hangupCause402Rate = hangupCause402Rate,
			param.hangupCause403Count = hangupCause403Count,
			param.hangupCause403Rate = hangupCause403Rate,
			param.taskName = currTaskName
		},
		success:function(data) {
			
		}
	});
}

function lastCallResultStyler(value,data,index) {
	return 'background-color:#fef4ef;';
}

function stateStyler(value,data,index) {
	return 'background-color:#fdfc8c;';
}
	
</script>

<div id="container" style="height:600px;width:1000px;"></div>

<!-- 数据汇总区 -->
<div class="easyui-tabs" style="width:1000px;height:210px;margin-left:10px;">
	<div title="数据统计汇总" style="padding:10px">
		
	<table id="summaryDg" data-options="fit:true,singleSelect:true,rownumbers:false">
		<thead>
			<tr style="font-weight: bold;">
				<th data-options="field:'category',width:100,align:'center'"></th>
				<th data-options="field:'totalData',width:100,align:'center'">已呼数量</th>
				<th data-options="field:'state1Data',width:100,align:'center',styler:stateStyler">已载入</th>
				<th data-options="field:'state2Data',width:100,align:'center',styler:stateStyler">已成功</th>
				<th data-options="field:'state3Data',width:100,align:'center',styler:stateStyler">待重呼</th>
				<th data-options="field:'state4Data',width:100,align:'center',styler:stateStyler">已失败</th>
				<th data-options="field:'hangupCause1Data',width:100,align:'center',styler:lastCallResultStyler">空号</th>
				<th data-options="field:'hangupCause16Data',width:100,align:'center',styler:lastCallResultStyler">关机</th>
				<th data-options="field:'hangupCause19Data',width:100,align:'center',styler:lastCallResultStyler">未接听</th>
				<th data-options="field:'hangupCause34Data',width:100,align:'center',styler:lastCallResultStyler">线路拥塞</th>
				<th data-options="field:'hangupCause38Data',width:100,align:'center',styler:lastCallResultStyler">呼转服务</th>
				<th data-options="field:'hangupCause401Data',width:100,align:'center',styler:lastCallResultStyler">归属地异常</th>
				<th data-options="field:'hangupCause402Data',width:100,align:'center',styler:lastCallResultStyler">超时未处理</th>
				<th data-options="field:'hangupCause403Data',width:100,align:'center',styler:lastCallResultStyler">PBX链接异常</th>
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

<script type="text/javascript">
	var dom = document.getElementById("container");
	myChart = echarts.init(dom);
	var app = {};
	option = null;
	app.title = '嵌套环形图';
	
	option = {
		//color: ['#f8d013','#00ff00', '#fc00ff', '#ff0000','#07b3fa','#55cafa','#8cdcfc','#666666', '#001100'],
		color: ['#f8d013','#00ff00', '#fc00ff', '#ff0000','#fb8d8d','#fb4b4b','#fb0202','#df0202', '#bb0303','#980202','#790202','#530101','#3f0808','#1c0505'],
		title:{
			text:'任务名称:NULL',
			subtext:"任务号码总量：0,已呼数量：0,未呼数量：0",
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
	            radius: [0, '40%'],
				center:['600px','350px'],
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
	            radius: ['45%', '55%'],
	            center:['600px','350px'],
	            label: {
	                normal: {
	                	formatter: function(params) {
	                    	pn = params['name'];
	                    	pv = params['value'];
	                    	pp = params['percent'];
	                    	pa = params['data']['a'];
	                    	
	                    	if(pn=='空号') {
	                    		hangupCause1Count = pv;
	                    		hangupCause1Rate = pp;
	                    	}else if(pn=='关机') {
	                    		hangupCause16Count = pv;
	                    		hangupCause16Rate = pp;
	                    	}else if(pn=='未接听') {
	                    		hangupCause19Count = pv;
	                    		hangupCause19Rate = pp;
	                    	}else if(pn=='线路拥塞') {
	                    		hangupCause34Count = pv;
	                    		hangupCause34Rate = pp;
	                    	}else if(pn=='呼转服务') {
	                    		hangupCause38Count = pv;
	                    		hangupCause38Rate = pp;
	                    	}else if(pn=='归属地异常') {
	                    		hangupCause401Count = pv;
	                    		hangupCause401Rate = pp;
	                    	}else if(pn=='超时未处理') {
	                    		hangupCause402Count = pv;
	                    		hangupCause402Rate = pp;
	                    	}else if(pn=='PBX链接异常') {
	                    		hangupCause403Count = pv;
	                    		hangupCause403Rate = pp;
	                    	}
	                    	
	                    	return '{a|' + pn + '}{abg|}\n{hr|}\n  {b|' + pn + '：}' + pv + '  {per|' + pp + '%}  ';
	                    	
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
			conditionHangupCause = null;
		}else if(name=='已成功') {
			title += ",呼叫结果：已成功";
			conditionState = 2;
			conditionHangupCause = null;
		}else if(name=='待重呼') {
			title += ",呼叫结果：待重呼";
			conditionState = 3;
			conditionHangupCause = null;
		}else if(name=='已失败') {
			title += ",呼叫结果：已失败";
			conditionState = 4;
			conditionHangupCause = null;
		}else if(name=='空号') {
			title += ",失败原因：空号";
			conditionState = "3,4";
			conditionHangupCause = 1;
		}else if(name=='关机') {
			title += ",失败原因：关机";
			conditionState = "3,4";
			conditionHangupCause = 16;
		}else if(name=='未接听') {
			title += ",失败原因：未接听";
			conditionState = "3,4";
			conditionHangupCause = 19;
		}else if(name=='线路拥塞') {
			title += ",失败原因：线路拥塞";
			conditionState = "3,4";
			conditionHangupCause = 34;
		}else if(name=='呼转服务') {
			title += ",失败原因：呼转服务";
			conditionState = "3,4";
			conditionHangupCause = 38;
		}else if(name=='归属地异常') {
			title += ",失败原因：归属地异常";
			conditionState = "3,4";
			conditionHangupCause = 401;
		}else if(name=='超时未处理') {
			title += ",失败原因：超时未处理";
			conditionState = "3,4";
			conditionHangupCause = 402;
		}else if(name=='PBX链接异常') {
			title += ",失败原因：PBX链接异常";
			conditionState = "3,4";
			conditionHangupCause = 403;
		}
		
		//alert("name=" + name + ",value=" + value + ",conditionState=" + conditionState);
		
		title += " 的号码列表";		
		findDataForTelephoneFor2();
		
		//在打开之前，先做些显示和隐藏的操作
		showExtraTabsFor2();
		
		$("#autoCallTelephoneDlg").dialog('setTitle',title).dialog('open');		
		
	});
	
</script>

