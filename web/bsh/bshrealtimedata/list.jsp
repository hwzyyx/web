<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
<title>博世实时数据</title>
	<style>
		.font17{
			font-size: 17px;
		}
	</style>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/color.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<link rel="stylesheet" type="text/css" href="iconfont/iconfont.css">
	<script src="echarts/echarts.min.js"></script><!-- 引入Echarts JS文件 -->
	<script src="iconfont/iconfont.js"></script>
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    
    <script type="text/javascript">
    
    	$(function(){
    		
    	});
    	
    </script>
</head>
<body>

<!-- 页面加载效果 -->
<%@ include file="/base_loading.jsp" %>

<!-- 定义一个div -->
<div id="container" style="width:1400px;height:800px;margin-top:10px;padding-left:20px; background-color: '#eee';"></div>

<script type="text/javascript">
	
	var dataName = [];
	var data1 = [];
	var data2 = [];

	var dom = document.getElementById("container");
	var myChart = echarts.init(dom);
	var app = {};
	option = null;
	option = {
		title: {
			text: 'BSH呼叫系统外呼情况实时数据监控',
			subtext: '排队机、活跃通道实时数据',
			show: 'true',
			align:'center',
			x: 'center',
			textStyle: {
				fontSize: 30,
				align: 'center'
			},
			subtextStyle: {
				fontSize: 20,
				color: '#ff0000'
			}
		},
		tooltip: {
			trigger: 'axis',
			formatter: function(params) {
				params1 = params[0];
				params2 = params[1];
				
				return '实时数据情况' + '<br/>排队机：' + params1.value + '<br/>活跃通道：' + params2.value;
			}
		},
		grid: {
			top: 120,
			left: 50
		},
		legend: {
			data: ['排队机数据','活跃通道数据'],
			top: 80
		},
		color: ['#f8d013','#00ff00'],
		xAxis: {
	        type: 'category',
	        axisLabel: {
	        	color: 'red',
	        	fontWeight: 'bolder',
	        	fontSize: 14
	        },
	        data: []
	    },
	    yAxis: {
	        type: 'value',
	        min: 0,
	        max: 50,
	        minInterval: 1
	    },
	    series: [{
	        name: '排队机数据',
	    	data: [],
	        type: 'line',
	        smooth: true
	    },
	    {
	        name: '活跃通道数据',
	        data: [],
	        type: 'line',
	        smooth: true
	    }]
	};
	;
	if (option && typeof option === "object") {
	    myChart.setOption(option, true);
	}
	
	setInterval(function(){
		
		$.ajax({
			url:'bshRealTimeData/getRealTimeData',
			method:'post',
			dataType:'json',
			success:function(rs) {
				
				if(dataName.length > 50) {
					dataName.shift();
					data1.shift();
					data2.shift();
				}
				
				dataName.push(rs.name);
				data1.push(rs.value1);
				data2.push(rs.value2);
				
				myChart.setOption({
					xAxis: {
						data: dataName
					},
					series:[{
						data: data1
					},{
						data: data2	
					}]
				});
			}
		});
		
	},1000);
	
</script>
	
</body>
</html>