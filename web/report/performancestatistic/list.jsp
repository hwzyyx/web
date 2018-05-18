<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>统计列表</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.min.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.min.js"></script>
	<script type="text/javascript" src="highcharts.js"></script>
    <script type="text/javascript" src="base-loading.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
		
	$(function () {
		//var chart;
		//var chart2;

	    //$('#mypie').highcharts({
	    /*chart = new Highcharts.Chart({
	        chart: {
	        	renderTo:'mypie',
	            plotBackgroundColor: null,
	            plotBorderWidth: null,
	            plotShadow: false,
	            type: 'pie'
	        },
	        colors:[
	    	    "#42BDF6",
	    	    "#FFCE95"
	    	    //"#96FEA9",
	    	    //"#FFCE95"
	    	],
	        title: {
	            text: '号码分配情况'
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
	            name: "占比为 ",
	            colorByPoint: true,
	            type: 'pie'
	            //data: [
	            //    {name: "Microsoft Internet Explorer", y: 56.33},
	            //    {
	            //        name: "Chrome",
	              //      y: 24.03,
	              //      sliced: true,
	              //      selected: true
	              //  },
	              //  {name: "Firefox", y: 10.38},
	              //  {name: "Safari", y: 4.77}, {name: "Opera", y: 0.91},
	              //  {name: "Proprietary or Undetectable", y: 0.2}
	            //]
	        }]
	    });*/
		
	    var chart = getPieChart("mypie","号码分配情况","百分比为");
		var chart2 = getPieChart("mypie2","已分配的情况","百分比为");

	    $.ajax({
	    	type:'POST',
			dataType:"json",
			url:"callTask/getPieChartData?type=1&taskId=24",
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

				chart.series[0].setData(arr);
				chart2.series[0].setData(arr2);
	    	}
		    
		});
	    
	});


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
	    	    "#FFCE95"
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
	<!-- 页面加载效果 -->
	<%@ include file="/base_loading.jsp" %>
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
							</table>
			</div>
			<!-- 编辑区下半部分列表 -->
			<div data-options="region:'center',split:true,border:false">
				<div id="mypie" style="float:left;min-width: 300px; height: 250px; max-width: 300px; margin: 0 auto;border-color: red;border-width:0px;border-style:solid; "></div>
				<div id="mypie2" style="float:left;min-width: 400px; height: 250px; max-width: 300px; margin: 0 auto;border-color: red;border-width:0px;border-style:solid; "></div>
			</div>
		</div>
	</div>

</body>
</html>

