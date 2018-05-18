<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!-- 数据显示区 -->
<div data-options="fit:true" class="easyui-layout">
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
					<th data-options="field:'NEXT_CALLOUT_TIME',width:170,align:'center'">下次外呼时间</th>
					<th data-options="field:'CALLRESULT_JSON',width:1000,align:'center'">外呼结果JSON</th>
					<th data-options="field:'FEEDBACK_CALLRESULT_RESPOND',width:500,align:'center'">接口响应</th>
				</tr>
				
			</thead>
		</table>
	</div>
</div>