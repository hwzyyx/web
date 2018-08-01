<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
<title>博世订单信息</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/color.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js.date.utils.js"></script>
    <script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js"></script>
    <script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    
    <script type="text/javascript">
    
    	//时间类型,0:创建时间;1:外呼时间, 默认为0。
    	var dateTimeType = 0;       //主要用于查询数据时，时间区段代表是以创建时间为查询区段，还是以外呼时间为查询区段
    
    	$(function(){
    		
    		//初始化搜索日期
    		$('#startTime').datetimebox('setValue',getCurrDate() + ' 00:00:00');
    		$('#endTime').datetimebox('setValue',getDateAfter(1) + ' 00:00:00');
    		
    		$("#timeInterval").combobox({
    			onChange:function(newValue,oldValue){
    				//alert("newValue:" + newValue + ";oldValue:" + nv + ";getDateBefore:" + getDateBefore(newValue));
    				$('#startTime').datetimebox('setValue',getDateBefore(newValue-1) + ' 00:00:00');
    				$('#endTime').datetimebox('setValue',getDateAfter(1) + ' 00:00:00');
    			}
    		}).combobox('setValue','1');
    		
    		//Combobox数据
    		var channelSourceComboboxDataFor1 = eval('${channelSourceComboboxDataFor1}');
    		var brandComboboxDataFor1 = eval('${brandComboboxDataFor1}');
    		var productNameComboboxDataFor1 = eval('${productNameComboboxDataFor1}');
    		var stateComboboxDataFor1 = eval('${stateComboboxDataFor1}');
    		var respondComboboxDataFor1 = eval('${respondComboboxDataFor1}');
    		
    		//购物平台Combobox
    		$("#channelSource").combobox({    
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',channelSourceComboboxDataFor1).combobox('setValue',"empty");
    		
    		//品牌信息Combobox
    		$("#brand").combobox({    
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',brandComboboxDataFor1).combobox('setValue',"empty");
    		
    		//产品名称Combobox
    		$("#productName").combobox({    
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',productNameComboboxDataFor1).combobox('setValue',"empty");
    		
    		//外呼状态Combobox
    		$("#state").combobox({    
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',stateComboboxDataFor1).combobox('setValue',"empty");
    		
    		//客户回复Combobox
    		$("#respond").combobox({    
				valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
			}).combobox('loadData',respondComboboxDataFor1).combobox('setValue',"empty");
    		
    		$("#bshOrderListDg").datagrid({
    			pageSize:30,
    			pagination:true,
    			fit:true,
    			rowrap:true,
    			striped:true,
    			pageList:[10,20,30],
    			url:'bshOrderList/datagrid',
    			toolbar:'#orderListDgTool',
    			queryParams:{
    				orderId:$('#orderId').textbox('getValue'),
    				channelSource:$('#channelSource').combobox('getValue'),
    				customerName:$('#customerName').textbox('getValue'),
    				customerTel:$('#customerTel').textbox('getValue'),
    				brand:$('#brand').combobox('getValue'),
    				productName:$('#productName').combobox('getValue'),
    				state:$('#state').combobox('getValue'),
    				respond:$('#respond').combobox('getValue'),
    				startTime:$('#startTime').datebox('getValue'),
    				endTime:$('#endTime').datebox('getValue'),
    				dateTimeType:dateTimeType
    			}
    		});
    		
    		$("#dateTimeTypeBtn0").bind("click",function(){  
	        	dateTimeType = 0; 
	        });
    		
    		$("#dateTimeTypeBtn1").bind("click",function(){  
	        	dateTimeType = 1; 
	        });
    		
    		
    		
    	});
    	
    	function findData() {
    		$("#bshOrderListDg").datagrid('reload',{
    			orderId:$("#orderId").textbox('getValue'),
    			channelSource:$('#channelSource').combobox('getValue'),
				customerName:$("#customerName").textbox('getValue'),
				customerTel:$("#customerTel").textbox('getValue'),
				brand:$('#brand').combobox('getValue'),
				productName:$("#productName").combobox('getValue'),
				state:$("#state").combobox('getValue'),
				respond:$('#respond').combobox('getValue'),
				startTime:$("#startTime").datebox('getValue'),
				endTime:$("#endTime").datebox('getValue'),
				dateTimeType:dateTimeType
    		});
    	}
    	
    	//状态描述设置颜色
    	function stateformatter(value,data,index) {
    		
    		state = data.STATE;     //状态
    		
    		if(state==1) {    		//已载入（黄色）
	    		return "<span style='color:#f8d013'>" + data.STATE_DESC + "</span>";
    		}else if(state==2) {    //已成功  （绿色）
	    		return "<span style='color:#00ff00'>" + data.STATE_DESC + "</span>";
    		}else if(state==3) {	//待重试   （紫色）
	    		return "<span style='color:#fc00ff'>" + data.STATE_DESC + "</span>";
    		}else if(state==4||state==5||state==6) {     //失败、过期、放弃呼叫  （红色）
	    		return "<span style='color:#ff0000'>" + data.STATE_DESC + "</span>";
    		}else {					//新建	（黑色）
	    		return "<span>" + data.STATE_DESC + "</span>";
    		}
    		
    	}
    	
    	//设置回复的字体颜色
    	function respondformatter(value,data,index) {
    		respond = data.RESPOND;
    		
    		if(respond==1) {		//同意安装	(绿色)
    			return "<span style='color:#00ff00'>" + data.RESPOND_DESC + "</span>";
    		}else {
    			return "<span>" + data.RESPOND_DESC + "</span>";
    		}
    		
    	}
    	
    	function orderListExport() {
    		
    		$("#exportForm").form('submit',{
    			
    			url:'bshOrderList/exportExcel',
    			onSubmit:function(param) {
    				param.orderId = $("#orderId").textbox('getValue'),
    				param.channelSource = $('#channelSource').combobox('getValue'),
    				param.customerName = $("#customerName").textbox('getValue'),
    				param.customerTel = $("#customerTel").textbox('getValue'),
    				param.brand = $('#brand').combobox('getValue'),
    				param.productName = $("#productName").combobox('getValue'),
    				param.state = $("#state").combobox('getValue'),
    				param.respond = $('#respond').combobox('getValue'),
    				param.startTime = $("#startTime").datebox('getValue'),
    				param.endTime = $("#endTime").datebox('getValue'),
    				param.dateTimeType = dateTimeType
    			},
    			success:function(data) {
    				
    			}
    			
    		});
    		
    		
    	}
    
    </script>
</head>
<body>

<!-- 页面加载效果 -->
<%@ include file="/base_loading.jsp" %>

<!-- 页面内容区 -->
<div data-options="fit:true" class="easyui-layout">
	
	<!-- 顶部查询区 -->
	<div data-options="region:'north',split:true,border:true" style="height:100px;padding-top:5px;padding-left:5px;">
		<table>
			<tr style="vertical-align: top;">
				<td> 
					订单编号：<input id="orderId" type="text" class="easyui-textbox" style="width:200px;"/>
					<span style="padding-left:20px;">
						购物平台：<select class="easyui-combobox" id="channelSource" name="channelSource" style="width:200px;"></select>
					</span>
					<span style="padding-left:20px;">客户姓名：<input id="customerName" type="text" class="easyui-textbox" style="width:200px;"/></span>
					<span style="padding-left:20px;">客户号码：<input id="customerTel" type="text" class="easyui-textbox" style="width:200px;"/></span>
				</td>
				
			</tr>
			<tr style="vertial-align:top;">
				<td>
					品牌信息：<select class="easyui-combobox" id="brand" name="brand" style="width:200px;"></select>
					<span style="padding-left:20px;">
						产品名称：<select class="easyui-combobox" id="productName" name="productName" style="width:200px;"></select>
					</span>
					<span style="padding-left:20px;">
						外呼状态：<select class="easyui-combobox" id="state" name="state" style="width:200px;"></select>
					</span>
					<span style="padding-left:20px;">
						客户回复：<select class="easyui-combobox" id="respond" name="respond" style="width:200px;"></select>
					</span>
				</td>
			</tr>
			<tr style="vertial-align:top;">
				<td>
					时间类型：
					<a href="#" id="dateTimeTypeBtn0" class="easyui-linkbutton" data-options="toggle:true,group:'g1',selected:true" style="width:95px;background-color: #00ff00;">创建时间</a>
					<a href="#" id="dateTimeTypeBtn1" class="easyui-linkbutton" data-options="toggle:true,group:'g1'" style="width:95px;margin-right:88px;background-color: #00ff00;">外呼时间</a>
					<input id="startTime" name="startTime" class="easyui-datetimebox" style="width:200px;"/><span style="padding-left:37px;padding-right:37px;">至</span> <input id="endTime" name="endTime" class="easyui-datetimebox" style="width:200px;"/>
					<span style="padding-left:20px;">
						时间间隔：
						<select class="easyui-combobox" id="timeInterval" name="timeInterval" style="width:80px;">
							<option value="1">1天</option>
							<option value="2">2天</option>
							<option value="3">3天</option>
							<option value="4">4天</option>
							<option value="5">5天</option>
							<option value="6">6天</option>
							<option value="7">7天</option>
							<option value="8">8天</option>
							<option value="9">9天</option>
							<option value="10">10天</option>
						</select>
					</span>
					<span style="padding-left:146px;"><a href="javascript:findData()" class="easyui-linkbutton" style="width:155px;" data-options="iconCls:'icon-search'">查询</a></span>
				</td>
			</tr>
		</table>
	</div>

	<!-- 数据显示区 -->
	<div data-options="region:'center',split:true,border:false">
		<table id="bshOrderListDg">
			<thead>
				<tr style="height:12px;">
					<th data-options="field:'ORDER_ID',width:170,align:'center'">订单编号</th>
					<th data-options="field:'CHANNEL_SOURCE_DESC',width:100,align:'center'">购物平台</th>
					<th data-options="field:'CUSTOMER_NAME',width:100,align:'center'">客户姓名</th>
					<th data-options="field:'CUSTOMER_TEL',width:120,align:'center'">客户号码</th>
					<th data-options="field:'PROVINCE',width:100,align:'center'">省份</th>
					<th data-options="field:'CITY',width:100,align:'center'">城市</th>
					<th data-options="field:'CALLOUT_TEL',width:130,align:'center'">外呼号码</th>
					<th data-options="field:'BRAND_DESC',width:100,align:'center'">品牌</th>
					<th data-options="field:'PRODUCT_NAME_DESC',width:100,align:'center'">产品名称</th>
					<th data-options="field:'EXPECT_INSTALL_DATE',width:100,align:'center'">计划安装日期</th>
					<th data-options="field:'RESPOND_DESC',width:120,align:'center',formatter:respondformatter">客户回复</th>
					<th data-options="field:'CREATE_TIME',width:170,align:'center'">创建时间</th>
					<th data-options="field:'STATE_DESC',width:100,align:'center',formatter:stateformatter">外呼结果</th>
					<th data-options="field:'LAST_CALL_RESULT',width:180,align:'center'">失败原因</th>
					<th data-options="field:'RETRIED',width:60,align:'center'">已重试</th>
					<th data-options="field:'LOAD_TIME',width:170,align:'center'">外呼时间</th>
					<th data-options="field:'BILLSEC',width:100,align:'center'">通话时长</th>
					<th data-options="field:'NEXT_CALLOUT_TIME',width:170,align:'center'">下次外呼时间</th>
					<th data-options="field:'CALLRESULT_JSON',width:1000,align:'center'">外呼结果JSON</th>
					<th data-options="field:'FEEDBACK_CALLRESULT_RESPOND',width:500,align:'center'">接口响应</th>
				</tr>
				
			</thead>
		</table>
	</div>

</div>
<form id="exportForm" action="#">
</form>
<div id="orderListDgTool" style="padding:5px;">
	<a href="#" id="easyui-export" onclick="orderListExport()" class="easyui-linkbutton" iconCls='icon-redo' plain="true">导出订单数据</a>
</div>
	
</body>
</html>