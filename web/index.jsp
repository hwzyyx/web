<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge" content="text/html;charset=UTF-8"/>
	<title>呼叫中心系统</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<style type="text/css">
		
		
		
		.myStyle{
			text-decoration:none;
			color:#1fffff;
			font-size: 12px;
		}
		
		.myStyle:visited
		{
			color:#B2C0D0;
		}
		
		.myStyle:hover
		{
			color:#00ff00;
		}
		
	</style>

	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript" src="echarts/echarts.min.js"></script>
	<script type="text/javascript" src="echarts/macarons.js"></script>
	<script type="text/javascript">
		var currTelephone = null;      //当前外呼的号码
		var callType= 1;               //呼叫类型，1：呼入  2：呼出  

		var cpuResourceChart = null;
		var ramResourceChart = null;
		

		var menuAccordionData = eval('${menuAccordionData}');
		var loginInfo = '${loginInfo}';
		var currAgentNumber = '${currAgentNumber}';
		
		function addTab(title, url){
			if ($('#layout_center_tabs').tabs('exists', title)){
				$('#layout_center_tabs').tabs('select', title);
			} else {
				var content = '<iframe scrolling="auto" frameborder="0"  src="'+url+'" style="width:99.5%;padding-left:3px;padding-top:0px;height:99%;margin:0 auto;"></iframe>';
				$('#layout_center_tabs').tabs('add',{
					title:title,
					content:content,
					closable:true
				});
			}
		}

		function cancelCheckCalloutResult() {
			$("#callout_touchfailure").css("display","none");
			$("#callout_touchsuccess").css("display","none");

			$("#consult").css("display","");
			$("#complaint").css("display","none");
			$("#abnormal").css("display","none");

			$("#attention_focus_callout").html("");
			$("#attention_focus_incomingcall").html("");
		}
		//执行 cti 功能，参数: 1 外呼; 2 通话保持; 3 呼叫转移; 4 示忙; 5 挂机
		function doCti(flg) {
			$.ajax({
				type:'POST',
				dataType:"json",
				url:'doCti?flg=' + flg,
				success:function(rs) {
					var statusCode = rs.statusCode; //返回的结果类型
					var message = rs.message;       //返回执行的信息
					window.parent.showMessage(message,statusCode);
				}
			});
		}
		
	</script>
	
	<script type="text/javascript">
	
	//用于控制 tab 的右击事件
	$(function(){

		initSystemResourceChart();       //初始化系统资源
		
		$("#loginInfoDiv").html(loginInfo);    //赋值登录信息
		
		if(currAgentNumber == null || currAgentNumber == '') {    //当前座席号为空时
			$("#signInDiv").css('display','');
			$("#signOffDiv").css('display','none');
		}else {
			$("#signInDiv").css('display','none');
			$("#signOffDiv").css('display','');
		}
		
		$('#layout_center_tabsMenu').menu({
			onClick : function(item) {
				var curTabTitle = $(this).data('tabTitle');
				var type = $(item.target).attr('type');
				
	
				if (type === 'refresh') {

					var t = $('#layout_center_tabs').tabs('getTab', curTabTitle);
					var iframe = $(t.panel('options').content);
					var url=iframe.attr("src");
					var currTab = $('#layout_center_tabs').tabs('getSelected');
					var content = '<iframe scrolling="auto" frameborder="0"  src="'+url+'" style="width:99.5%;padding-left:3px;padding-top:0px;height:99%;margin:0 auto;"></iframe>';

					$('#layout_center_tabs').tabs('update',{
						tab:currTab,
						options:{
							content:content
						}
					});
						
					//layout_center_refreshTab(curTabTitle);
					return;
				}
	
				if (type === 'close') {
					var t = $('#layout_center_tabs').tabs('getTab', curTabTitle);
					if (t.panel('options').closable) {
						$('#layout_center_tabs').tabs('close', curTabTitle);
					}
					return;
				}
	
				var allTabs = $('#layout_center_tabs').tabs('tabs');
				var closeTabsTitle = [];
	
				$.each(allTabs, function() {
					var opt = $(this).panel('options');
					if (opt.closable && opt.title != curTabTitle && type === 'closeOther') {
						closeTabsTitle.push(opt.title);
					} else if (opt.closable && type === 'closeAll') {
						closeTabsTitle.push(opt.title);
					}
				});
	
				for ( var i = 0; i < closeTabsTitle.length; i++) {
					$('#layout_center_tabs').tabs('close', closeTabsTitle[i]);
				}
			}
		});
		$('#layout_center_tabs').tabs({
			fit : true,
			border : false,
			onContextMenu : function(e, title) {
				e.preventDefault();
				$('#layout_center_tabsMenu').menu('show', {
					left : e.pageX,
					top : e.pageY
				}).data('tabTitle', title);
			},
			tools : [ {
				iconCls : 'icon-reload',
				handler : function() {
					var href = $('#layout_center_tabs').tabs('getSelected').panel('options').href;
					if (href) {    //说明tab是以href方式引入的目标页面
						var index = $('#layout_center_tabs').tabs('getTabIndex', $('#layout_center_tabs').tabs('getSelected'));
						$('#layout_center_tabs').tabs('getTab', index).panel('refresh');
					} 
				}
			}]
		});


		//输出模块菜单
		var ndata = menuAccordionData[0].children;
		
		$.each(ndata,function(idx,item){
			var con = "";
			$.each(item.children,function(i,n){
				con += '<div style="padding:2px;"><a href="#" class="easyui-linkbutton" data-options="plain:true,iconCls:\'icon-folderopened\'" onclick="addTab(\'' + n.text + '\',\'' + n.uri + '\')">' + n.text + '</a></div>';
			});
			$("#menu-accordion").accordion('add',{
				title:item.text,
				content:con,
				selected:item.state
			});
			
		});
		
		/*$.ajax({
			type:'POST',
			dataType:"json",
			url:'module/menu?currOperId=${currOperId}',
			success:function(data) {
				var ndata = data[0].children;

				$.each(ndata,function(idx,item){
					var con = "";
					$.each(item.children,function(i,n){
						con += '<div style="padding:2px;"><a href="#" class="easyui-linkbutton" data-options="plain:true,iconCls:\'icon-folderopened\'" onclick="addTab(\'' + n.text + '\',\'' + n.uri + '\')">' + n.text + '</a></div>';
					});
					$("#menu-accordion").accordion('add',{
						title:item.text,
						content:con,
						selected:item.state
					});
					
				});
						
			}
		});*/
		//关联页面关闭事件(IE浏览器下可正常工作)
		//$(window).unload(function() {
		//	logout();
		//});
		//客户接触 datagrid 数据列表
		$("#clientTouchRecordDg").datagrid({
			pageSize:10,
			pagination:true,      
			fit:true,
			singleSelect:true,
			rowrap:true,
			striped: true,
			rownumbers: true,
			pageList:[10,15,20]
		});
		//客户基本资料属性数据
		$("#clientPg").propertygrid({
			//url:'taskExecute/propertygrid',
			type:'post',
			showGroup:false,
			showHeader:false,
			onLoadSuccess:function() {
			}
		});

		//修改密码对话框
		$("#changepasswordpanel").dialog({
			onClose:function() {
				$("#changepasswordform").form("clear");
			}
		});

		//在弹屏退出时，先将呼入弹屏表单、呼出弹屏表单的数据消除，以避免重复提交
		$("#dialpanel").dialog({
			onClose:function() {
				$("#callout_touchrecordresultform").form("clear");
				$("#incomingcall_touchrecordresultform").form("clear");
				cancelCheckCalloutResult();
			}
		});

		$("#jquery_jplayer_3").jPlayer({
			ready: function (event) {
				$(this).jPlayer("setMedia", {
					title: "Bubble",
					m4a: "http://jplayer.org/audio/m4a/Miaow-07-Bubble.m4a",
					oga: "http://jplayer.org/audio/ogg/Miaow-07-Bubble.ogg"
				});
			},
			swfPath: "jplayer/dist/jplayer",
			supplied: "m4a, oga",
			cssSelectorAncestor: "#jp_container_3",
			wmode: "window",
			useStateClassSkin: true,
			autoBlur: false,
			smoothPlayBar: true,
			keyEnabled: true,
			remainingDuration: true,
			toggleDuration: true
		});
		
	});
	
	//根据传入的checkbox的名，取得值，并返回以 逗号分隔的结果 
	function getCheckBoxValue(checkboxName) {
		var result = [];       //定义结果变量名
        $("input[name='" + checkboxName + "']:checked").each(function(){ 
        	result.push($(this).val());
		})
		return result.join(",");	
	}

	//根据 radioName 取得 radio 选中的值
	function getRadioValue(radioName) {
		return $("input[name='" + radioName + "']:checked").val();
	}

	//外呼成功与否的模块显示：  flg为1时，#callout_touchfailure 显示， flg为2时，#callout_touchsuccess 显示
	function checkcalloutresult(flg){
		//cancelCheckCalloutResult();
		if(flg==1) {           //接触失败
			$("#callout_touchfailure").css("display","");
			$("#callout_touchsuccess").css("display","none");
		}else if(flg==2){      //接触成功-感兴趣
			$("#callout_touchfailure").css("display","none");
			
			$("#callout_touchsuccess").css("display","");
			$("#callout_insteresting").css("display","");
			$("#callout_uninsteresting").css("display","none");
		}else if(flg==3){	   //接触成功-不感兴趣
			$("#callout_touchfailure").css("display","none");
			
			$("#callout_touchsuccess").css("display","");
			$("#callout_insteresting").css("display","none");
			$("#callout_uninsteresting").css("display","");
		}	
	}

	//来电原因更改显示的表单内容
	function checkIncomingCallCallReasonResult(flg) {
		if(flg==1) {    				//来电咨询     
			$("#incomingcall_consult").css("display","");
			$("#incomingcall_complaint").css("display","none");
			$("#incomingcall_abnormal").css("display","none");
		}else if(flg==2) {   			//来电投诉
			$("#incomingcall_consult").css("display","none");
			$("#incomingcall_complaint").css("display","");
			$("#incomingcall_abnormal").css("display","none");
		}else if(flg==3) {              //异常来电
			$("#incomingcall_consult").css("display","none");
			$("#incomingcall_complaint").css("display","none");
			$("#incomingcall_abnormal").css("display","");
		}
	}

	function logout() {
		$.ajax({
			url:'logout',
			type:'POST',
			dataType:'json',
			success:function(rs) {
				var statusCode = rs.statusCode; //返回的结果类型
				var message = rs.message;       //返回执行的信息

				if(statusCode == "success") {     //出现无法外呼时，才弹屏提示外呼结果
					document.location = "login";
				}
			}
		});
	}

	function modifyPassword() {
		//alert("aa");
		$("#changepasswordpanel").dialog('open');
	}
	
	function changePassword_cancel() {
		$("#changepasswordpanel").dialog("close");
	}

	function changePassword_save() {
		
		$("#changepasswordform").form('submit',{
			url:"operator/changePassword",
			onSubmit:function(){
				var vld = $(this).form('validate');

				if(vld==false) {
					return vld;
				}
				
				var pass1 = $("#NEW_PASSWORD").val();
				var pass2 = $("#RE_NEW_PASSWORD").val();
				if(pass1!=pass2){
					$.messager.alert("提示","修改失败，新密码及确认新密码不相同!","error");
					return false;
				}
				return vld;
			},
			success:function(data) {
				var result = JSON.parse(data);    //解析Json 数据
				
				var statusCode = result.statusCode; //返回的结果类型
				var message = result.message;       //返回执行的信息
				
				showMessage(message,statusCode);
				if(statusCode == 'success') {         //保存成功时，才关闭窗口及重新刷新数据
					$("#changepasswordpanel").dialog("close");
				}
								
			}
		});
	}

	function touchTypeFormatter(value,data,index) {
		if(value=='1') {
			return "呼出";
		}else {
			return "呼入";
		}	
	}
	
	function myformatter(value,data,index) {
		if(data.name=='客户性别') {
			return value=="1"?"男":"女";
		}else if(data.name=='客户级别'){
			var index2 = 0;

			if(value=="1") {
				index2 = 0;
			}else if(value=="2") {
				index2 = 1;
			}else if(value=="3") {
				index2 = 2;
			}else if(value=="4") {
				index2 = 3;
			}else if(value=="5") {
				index2 = 4;
			}else if(value=="6") {
				index2 = 5;
			}else if(value=="7") {
				index2 = 6;
			}else if(value=="8") {
				index2 = 7;
			}else if(value=="9") {
				index2 = 8;
			}else if(value=="10") {
				index2 = 9;
			}
			if(data.editor.options.data[index2].text==null) {
				return ""; 
			}else{
				return data.editor.options.data[index2].text;
			}
		}else {
			return value;
		}
	}

	//客户资料显示及修改的 propertyGrid的定义
	var mycolumns=[[
			{field:'name',title:'name',width:78,sortable:true,align:'center'},
   			{field:'value',title:'value',width:172,formatter:myformatter}   
	    ]];

    //外呼、来电时弹屏
    //外呼时， telId和taskId 不为空，来电弹屏时，telId,taskId为空
	function showdialpanel(telephone,telId,taskId) {
    	if(telId==null) {     //telId 为空时，表示为呼入来电弹屏
			callType=1;       //呼入时，设置呼叫类型为1
        	
    		$("#dialpanel").dialog("setTitle","来电弹屏面板");
    		$("#callout_form").css("display","none");                  //_dialplan中的呼出弹屏表单不可见
    		$("#incomingcall_form").css("display","");				   //_dialplan中的呼入弹屏表单可见
    		$("#callreason_consult").attr("checked","checked");        //默认选中来电咨询

    		//默认显示来电咨询的表单
    		$("#incomingcall_consult").css("display","");
			$("#incomingcall_complaint").css("display","none");
			$("#incomingcall_abnormal").css("display","none");
    	}else {
    		callType=2;       //呼出时，设置呼叫类型为2
        	
    		$("#dialpanel").dialog("setTitle","外呼面板");
    		$("#callout_form").css("display","");                      //_dialplan中的呼出弹屏表单可见
    		$("#incomingcall_form").css("display","none");             //_dialplan中的呼入弹屏表单不可见
    		$("#callout_result2").attr("checked","checked");   		   //默认外呼成功-感兴趣
    		$("#donot_send_message_callout").attr("checked","checked");//默认不发送短信

    		//默认显示感兴趣的表单
			$("#callout_touchfailure").css("display","none");
			$("#callout_touchsuccess").css("display","");
			$("#callout_insteresting").css("display","");
			$("#callout_uninsteresting").css("display","none");
    	}
		
		//先判断是否登录时，已经输入了座席号，如果座席号为空时，则提交接触记录的结果的按钮要设置为不可用
		//alert(${currCallNumber});
		var currAgent = "${currAgentNumber}";
		if(currAgent==null || currAgent=="") {      //如果座席号为空时，保存接触信息按钮不可用, 拨号按钮也不可用
			$("#saveTouchResultBtn").linkbutton("disable");      //保存接触信息按钮禁用

			$("#doDialBtn").linkbutton("disable");
			$("#doDialZeroBtn").linkbutton("disable");
			$("#doHangupBtn").linkbutton("disable");
			
		}else {
			$("#saveTouchResultBtn").linkbutton("enable");         ////保存接触信息按钮恢复可用

			$("#doDialBtn").linkbutton("enable"); 
			$("#doDialZeroBtn").linkbutton("enable"); 
			$("#doHangupBtn").linkbutton("enable"); 
		}
		
		currTelephone = telephone;
		$("#dialpanel").dialog('open');

		//设置号码id及任务id
		$("#telId").val(telId);
		$("#taskId").val(taskId);
		
		//列表客户信息
		$("#clientPg").propertygrid({
			url:'taskExecute/propertygrid?telephone=' + telephone + '&telId=' + telId,
			columns:mycolumns
		});
		//列表通话记录
		$("#clientTouchRecordDg").datagrid({
			url:'taskExecute/touchRecordDatagrid?telephone=' + currTelephone
		});

		//加载自动生成的表单，如：客户咨询热点、客户需求收集
		loadFormData(telId);
	}

    // 载入客户关注热点及需求收集（如：需求类型、需求户型、需求面积、意向价格、置业意向、置业目的、需求区位、交房时间）
	function loadFormData(telId) {    //flg为空时，来电弹屏； flg不为空时，呼出弹屏

    	if(telId==null) {
    		//将认知途径的 radio由服务端返回
    		$.ajax({
    			url:'getRadio?groupCode=COGINITION_WAY',
    			method:'POST',
    			dataType:'json',
    			success:function(rs) {
    				var statusCode = rs.statusCode; //返回的结果类型
    				var message = rs.message;       //返回执行的信息
    				//window.parent.showMessage(message,statusCode);
    				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
    					$("#coginition_way").html(message);						
    				}
    			}
    		});
        	
    		//将关注热点由服务端返回,返回 checkbox 类型
    		$.ajax({
    			url:'getCheckBox?groupCode=ATTENTION_FOCUS',
    			method:'POST',
    			dataType:'json',
    			success:function(rs) {
    				var statusCode = rs.statusCode; //返回的结果类型
    				var message = rs.message;       //返回执行的信息
    				//window.parent.showMessage(message,statusCode);
    				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
    					$("#attention_focus_incomingcall").html(message);						
    				}
    			}
    		});
        	
    		//将需求类型由服务器返回，返回 combobox 类型
    		$("#require_type_incomingcall").combobox({
    			url:'getCombobox?groupCode=REQUIRE_TYPE&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将需求户型由服务器返回，返回 combobox 类型
    		$("#require_housetype_incomingcall").combobox({
    			url:'getCombobox?groupCode=REQUIRE_HOUSETYPE&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});
    		

    		//将需求面积由服务器返回，返回 combobox 类型
    		$("#require_area_incomingcall").combobox({
    			url:'getCombobox?groupCode=REQUIRE_AREA&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将意向价格由服务器返回，返回 combobox 类型
    		$("#intend_price_incomingcall").combobox({
    			url:'getCombobox?groupCode=INTEND_PRICE&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将置业意向由服务器返回，返回 combobox 类型
    		$("#zyintend_incomingcall").combobox({
    			url:'getCombobox?groupCode=ZYINTEND&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将置业目的由服务器返回，返回 combobox 类型
    		$("#properties_purpose_incomingcall").combobox({
    			url:'getCombobox?groupCode=PROPERTIES_PURPOSE&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});
    		

    		//将需求区位由服务器返回，返回 combobox 类型
    		$("#require_location_incomingcall").combobox({
    			url:'getCombobox?groupCode=REQUIRE_LOCATION&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将交房时间由服务器返回，返回 combobox 类型
    		$("#makingroom_time_incomingcall").combobox({
    			url:'getCombobox?groupCode=MAKINGROOM_TIME&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将客户来电投诉的子项由服务端返回,返回 checkbox 类型
    		$.ajax({
    			url:'getCheckBox?groupCode=COMPLAINT_ITEM',
    			method:'POST',
    			dataType:'json',
    			success:function(rs) {
    				var statusCode = rs.statusCode; //返回的结果类型
    				var message = rs.message;       //返回执行的信息
    				//window.parent.showMessage(message,statusCode);
    				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
    					$("#complaint_item").html(message);						
    				}
    			}
    		});

    		//将异常来电的原因的 radio由服务端返回
    		$.ajax({
    			url:'getRadio?groupCode=ABNORMAL_REASON',
    			method:'POST',
    			dataType:'json',
    			success:function(rs) {
    				var statusCode = rs.statusCode; //返回的结果类型
    				var message = rs.message;       //返回执行的信息
    				//window.parent.showMessage(message,statusCode);
    				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
    					$("#abnormal_reason").html(message);						
    				}
    			}
    		});
    		
    	}else {
    		//将关注热点由服务端返回,返回 checkbox 类型
    		$.ajax({
    			url:'getCheckBox?groupCode=ATTENTION_FOCUS',
    			method:'POST',
    			dataType:'json',
    			success:function(rs) {
    				var statusCode = rs.statusCode; //返回的结果类型
    				var message = rs.message;       //返回执行的信息
    				//window.parent.showMessage(message,statusCode);
    				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
    					$("#attention_focus_callout").html(message);						
    				}
    			}
    		});
        	
    		//将呼叫失败的原因的 radio由服务端返回
    		$.ajax({
    			url:'getRadio?groupCode=TOUCH_FAILURE_REASON',
    			method:'POST',
    			dataType:'json',
    			success:function(rs) {
    				var statusCode = rs.statusCode; //返回的结果类型
    				var message = rs.message;       //返回执行的信息
    				//window.parent.showMessage(message,statusCode);
    				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
    					$("#touchfailurereason").html(message);						
    				}
    			}
    		});
        	
    		//将需求类型由服务器返回，返回 combobox 类型
    		$("#require_type_callout").combobox({
    			url:'getCombobox?groupCode=REQUIRE_TYPE&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将需求户型由服务器返回，返回 combobox 类型
    		$("#require_housetype_callout").combobox({
    			url:'getCombobox?groupCode=REQUIRE_HOUSETYPE&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});
    		

    		//将需求面积由服务器返回，返回 combobox 类型
    		$("#require_area_callout").combobox({
    			url:'getCombobox?groupCode=REQUIRE_AREA&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将意向价格由服务器返回，返回 combobox 类型
    		$("#intend_price_callout").combobox({
    			url:'getCombobox?groupCode=INTEND_PRICE&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将置业意向由服务器返回，返回 combobox 类型
    		$("#zyintend_callout").combobox({
    			url:'getCombobox?groupCode=ZYINTEND&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将置业目的由服务器返回，返回 combobox 类型
    		$("#properties_purpose_callout").combobox({
    			url:'getCombobox?groupCode=PROPERTIES_PURPOSE&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});
    		

    		//将需求区位由服务器返回，返回 combobox 类型
    		$("#require_location_callout").combobox({
    			url:'getCombobox?groupCode=REQUIRE_LOCATION&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将交房时间由服务器返回，返回 combobox 类型
    		$("#makingroom_time_callout").combobox({
    			url:'getCombobox?groupCode=MAKINGROOM_TIME&flag=1',
    			method:'POST',
    			valueField:'id',
    			textField:'text',
    			panelHeight:'auto'
    		});

    		//将不感兴趣原因由服务端返回,返回 checkbox 类型
    		$.ajax({
    			url:'getCheckBox?groupCode=UNINSTERESTING_REASON',
    			method:'POST',
    			dataType:'json',
    			success:function(rs) {
    				var statusCode = rs.statusCode; //返回的结果类型
    				var message = rs.message;       //返回执行的信息
    				//window.parent.showMessage(message,statusCode);
    				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
    					$("#uninsteresting_reason").html(message);	
    				}
    			}
    		});
			
    	}
		
	}

	//用于显示各种操作结果信息，如添加、修改、删除等信息
	function showMessage(message,status) {
		if(status=="success") {
			$.messager.show({
				title:'提示',
				msg:message,
				showType:'slide',
				style: {
					right:'',
					left:document.documentElement.offsetWidth/2-125,
					top:-5,
					bottom:''
				},
				timeout:2500
			});
		}else {
			$.messager.alert("提示",message,"error");
		}	
	}

	/**
	* 保存接触记录，及保存客户的基本资料
	* 保存之前，先判断 callType 的值， callType 为 1 时，表示呼入； callType 为 2时，表示呼出
	**/
	function saveTouchRecord() {
		var uriInfo = "taskExecute/addTouchRecord?callType=" + callType;     //定义用于提交接触信息的变量
		
		if(callType==1) {       //呼入表单数据获取
			
			var incomingcall_callreason = $("input[name='incomingcall_callreason']:checked").val();    //来电原因，1：来电咨询；2：来电投诉；3：异常来电
			var touchNoteValue = encodeURI(encodeURI($("#touchnote_incomingcall").val()));                  //备注
			
			uriInfo += "&callReason=" + incomingcall_callreason + "&touchNote=" + touchNoteValue;
			
			if(incomingcall_callreason==1) {        			//来电咨询
				var attentionFocusValue = getCheckBoxValue("ATTENTION_FOCUS");        						//客户咨询热点
				var coginitionWayValue = getRadioValue("COGINITION_WAY");                   					//客户认知途径
				var visitTimeValue = $("#visitTime_incomingcall").datetimebox("getValue");         		    //答应到访时间
				var sendMessageValue = getRadioValue("send_message_incomingcall");          //是否发送短信
				
				//下面为客户需求收集
				var requireTypeValue = $("#require_type_incomingcall").combobox("getValue");       			 //需求类型，1：住宅；2：公寓；3别墅；
				var requireHouseTypeValue = $("#require_housetype_incomingcall").combobox("getValue");       //需求户型，1：一居；2：两居；3三居；
				var requireAreaValue = $("#require_area_incomingcall").combobox("getValue");       			 //需求面积，1：50-80平米；2：80-100平米
				var intendPriceValue = $("#intend_price_incomingcall").combobox("getValue");       			 //意向价格，1：5000以下
				var	zyIntendValue = $("#zyintend_incomingcall").combobox("getValue");       				 //置业意向，1：A(强烈关注)；2：B(一般关注)；3：C(暂不考虑)；
				var propertiesPurposeValue = $("#properties_purpose_incomingcall").combobox("getValue");     //置业目的，1：自住；2：投资；
				var requireLocationValue = $("#require_location_incomingcall").combobox("getValue");         //需求区位，1：西山区；2：呈贡区；3:官渡区；4：五华区；
				var makingRoomTimeValue = $("#makingroom_time_incomingcall").combobox("getValue");       	 //交房时间，1：2015年；2：2016年；3：2017年；

				uriInfo += "&attentionFocus=" + attentionFocusValue + "&coginitionWay=" + coginitionWayValue + "&visitTime=" + visitTimeValue + 
				           "&sendMessage=" + sendMessageValue + "&requireType=" + requireTypeValue + "&requireHouseType=" + requireHouseTypeValue +
				           "&requireArea=" + requireAreaValue + "&intendPrice=" + intendPriceValue + "&zyIntend=" + zyIntendValue +
				           "&propertiesPurpose=" + propertiesPurposeValue + "&requireLocation=" + requireLocationValue + "&makingRoomTime=" + makingRoomTimeValue;
				
				/*alert("来电咨询的结果，关注热点：" + attentionFocusValue + ",认知途径：" + coginitionWayValue + ",答应到访时间：" + visitTimeValue + 
						",是否发送短信:" + sendMessageValue + ",需求类型:" + requireTypeValue + ",需求户型:" + requireHouseTypeValue +
						",需求面积:" + requireAreaValue + ",意向价格:" + intendPriceValue + ",置业意向:" + zyIntendValue + ",置业目的:" + propertiesPurposeValue +
						",需求区位:" + requireLocationValue + ",交房时间:" + makingRoomTimeValue);*/
			}else if(incomingcall_callreason==2) {				//来电投诉

				var complaintItemValue = getCheckBoxValue("COMPLAINT_ITEM");                         		 //客户来电投诉选项的结果

				uriInfo += "&complainItem=" + complaintItemValue;
				
				//alert("来电投诉的结果，来电投诉选项：" + complaintItemValue);
			}else if(incomingcall_callreason==3) {              //异常来电
				var abnormalReasonValue = getRadioValue("ABNORMAL_REASON");                  								//异常来电原因

				uriInfo += "&abnormalReason=" + abnormalReasonValue;
				
				//alert("异常来电的结果，异常来电原因：" + abnormalReasonValue);
			}

			
			
		}else {					//呼出表单数据获取
			
			var callout_touchresult = getRadioValue("callout_touchresult");      			//外呼结果，1：接触失败；2：接触成功-感兴趣；3：接触成功-不感兴趣;
			var touchNoteValue = encodeURI(encodeURI($("#touchnote_callout").val()));       //备注
			var recallTimeValue = $("#recallTime").datetimebox("getValue");	 	//再次外呼时间
			var telId = $("#telId").val();    //对于呼出，还需要得到 telId,用于修改这个任务的状态
			var taskId = $("#taskId").val(); 
			
			uriInfo += "&touchResult=" + callout_touchresult + "&touchNote=" + touchNoteValue + "&recallTime=" + recallTimeValue + 
			           "&telId=" + telId + "&taskId=" + taskId;
			
			//先定义用于获取表单值
			var touchFailureReasonValue = null;          		//接触不成功的原因
			var attentionFocusValue = null;              		//客户咨询热点
			var visitTimeValue = null;     				 		//答应到访时间
			var sendMessageValue = null;                 		//是否发送信息

			var requireTypeValue = null;      			 //需求类型，1：住宅；2：公寓；3别墅；
			var requireHouseTypeValue = null;       	 //需求户型，1：一居；2：两居；3三居；
			var requireAreaValue = null;       			 //需求面积，1：50-80平米；2：80-100平米
			var intendPriceValue = null;       			 //意向价格，1：5000以下
			var	zyIntendValue = null;       			 //置业意向，1：A(强烈关注)；2：B(一般关注)；3：C(暂不考虑)；
			var propertiesPurposeValue = null;           //置业目的，1：自住；2：投资；
			var requireLocationValue = null;             //需求区位，1：西山区；2：呈贡区；3:官渡区；4：五华区；
			var makingRoomTimeValue = null;       	     //交房时间，1：2015年；2：2016年；3：2017年；

			
			
			if(callout_touchresult==1) {						//接触失败

				touchFailureReasonValue = getRadioValue("TOUCH_FAILURE_REASON");         //接触不成功的原因

				uriInfo += "&touchFailureReason=" + touchFailureReasonValue;
				
				//alert("接触失败，接触不成功的原因:" + touchFailureReasonValue + ",再次外呼时间:" + recallTimeValue);
			}else if(callout_touchresult==2 || callout_touchresult==3) {				 //接触成功-感兴趣

				sendMessageValue = getRadioValue("send_message_callout");           	 //是否发送短信

				//下面为客户需求收集
				requireTypeValue = $("#require_type_callout").combobox("getValue");       			 //需求类型，1：住宅；2：公寓；3别墅；
				requireHouseTypeValue = $("#require_housetype_callout").combobox("getValue");       //需求户型，1：一居；2：两居；3三居；
				requireAreaValue = $("#require_area_callout").combobox("getValue");       			 //需求面积，1：50-80平米；2：80-100平米
				intendPriceValue = $("#intend_price_callout").combobox("getValue");       			 //意向价格，1：5000以下
				zyIntendValue = $("#zyintend_callout").combobox("getValue");       				 //置业意向，1：A(强烈关注)；2：B(一般关注)；3：C(暂不考虑)；
				propertiesPurposeValue = $("#properties_purpose_callout").combobox("getValue");     //置业目的，1：自住；2：投资；
				requireLocationValue = $("#require_location_callout").combobox("getValue");         //需求区位，1：西山区；2：呈贡区；3:官渡区；4：五华区；
				makingRoomTimeValue = $("#makingroom_time_callout").combobox("getValue");       	 //交房时间，1：2015年；2：2016年；3：2017年；

				uriInfo += "&sendMessage=" + sendMessageValue + "&requireType=" + requireTypeValue + "&requireHouseType=" + requireHouseTypeValue +
		           "&requireArea=" + requireAreaValue + "&intendPrice=" + intendPriceValue + "&zyIntend=" + zyIntendValue +
		           "&propertiesPurpose=" + propertiesPurposeValue + "&requireLocation=" + requireLocationValue + "&makingRoomTime=" + makingRoomTimeValue;
				
				if(callout_touchresult == 2) {        
					attentionFocusValue = getCheckBoxValue("ATTENTION_FOCUS");        		 			//客户咨询热点
					visitTimeValue = $("#visitTime_callout").datetimebox("getValue");   			//答应到访时间

					uriInfo += "&attentionFocus=" + attentionFocusValue + "&visitTime=" + visitTimeValue;
					
					/*alert("接触成功-感兴趣，关注热点：" + attentionFocusValue + ",答应到访时间：" + visitTimeValue + ",再次呼叫时间:" + recallTimeValue +
							",是否发送短信:" + sendMessageValue + ",需求类型:" + requireTypeValue + ",需求户型:" + requireHouseTypeValue +
							",需求面积:" + requireAreaValue + ",意向价格:" + intendPriceValue + ",置业意向:" + zyIntendValue + ",置业目的:" + propertiesPurposeValue +
							",需求区位:" + requireLocationValue + ",交房时间:" + makingRoomTimeValue);*/
				}else if(callout_touchresult == 3) {
					var uninsterestingReasonValue = getCheckBoxValue("UNINSTERESTING_REASON");         	//不感兴趣的原因

					uriInfo += "&uninsterestingReason=" + uninsterestingReasonValue;
					
					/*alert("接触成功-不感兴趣，不感兴趣的原因是：" + uninsterestingReasonValue + ",再次呼叫时间:" + recallTimeValue +
							",是否发送短信:" + sendMessageValue + ",需求类型:" + requireTypeValue + ",需求户型:" + requireHouseTypeValue +
							",需求面积:" + requireAreaValue + ",意向价格:" + intendPriceValue + ",置业意向:" + zyIntendValue + ",置业目的:" + propertiesPurposeValue +
							",需求区位:" + requireLocationValue + ",交房时间:" + makingRoomTimeValue);*/
				}
				
			}
			
		}

		//上面的内容用于组织接触信息
		//下面的内容用于组织客户的基本信息
		//获取客户信息
		var rows = $("#clientPg").propertygrid("getRows");
		
		for(var i=0; i<rows.length; i++){
			if(i==0) {         //客户编号
				uriInfo += "&clientNo=" + rows[i].value;
			}else if(i==1) {　  //客户号码
				uriInfo += "&clientTelephone=" + rows[i].value;
			}else if(i==2) {　  //备用号码
				uriInfo += "&clientTelephone2=" + rows[i].value;
			}else if(i==3) {　  //客户名称
				uriInfo += "&clientName=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==4) {　  //客户级别
				uriInfo += "&clientLevel=" + rows[i].value;
			}else if(i==5) {　  //客户性别
				uriInfo += "&clientSex=" + rows[i].value;
			}else if(i==6) {　  //归属地
				uriInfo += "&location=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==7) {　  //客户ＱＱ
				uriInfo += "&clientQq=" + rows[i].value;
			}else if(i==8) {　  //电子邮箱
				uriInfo += "&clientEmail=" + rows[i].value;
			}else if(i==9) {　  //公司信息
				uriInfo += "&clientCompany=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==10) {  //地址信息
				uriInfo += "&clientAddress=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==11) {　 //添加时间
				uriInfo += "&clientCreateTime=" + rows[i].value;
			}
			//s += rows[i].name + '(' + i + '):' + rows[i].value + ',\r\n';
		}

		//组织 uriInfo 后，开始提交数据
		$.messager.progress({
			msg:'系统正在处理，请稍候...',
			interval:3000
		});
		//alert("uriInfo的信息为：" + uriInfo);
		$.ajax({
			url:uriInfo,
			type:'POST',
			dataType:'json',
			success:function(rs) {
				$.messager.progress('close');
				var statusCode = rs.statusCode; //返回的结果类型
				var message = rs.message;       //返回执行的信息
				window.parent.showMessage(message,statusCode);
				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
					
					$("#saveTouchResultBtn").linkbutton("disable");   //如果提交成功时，需要将保存接触结果的按钮设置为不可用，避免重复提交
					//$("#saveTouchResultBtn").attr("disabled",'true');   //如果提交成功时，需要将保存接触结果的按钮设置为不可用，避免重复提交
					$("#clientTouchRecordDg").datagrid({                //刷新接触信息
						url:'taskExecute/touchRecordDatagrid?telephone=' + currTelephone
					});
					//同时还要刷新呼叫号码列表中的数据
					reloadTelephoneDg("任务执行");
				}
			}
		});
		
	}
	
	function saveTouchRecord_bak() {
		
		var urlInfo = "taskExecute/add?flg=" + currFlg + "&telId=" + $("#telId").val() + "&taskId=" + $("#taskId").val();
		
		if(currFlg == 1) { 			//如果currFlg==1，即是接触失败
			var touchFailureReason = $("input[name='TOUCH_FAILURE_REASON']:checked").val();    //接触失败的原因
			if(typeof(touchFailureReason)=="undefined") {   //如果未选择接触失败原因
				alert("无法提交：请选择接触失败原因!");
				return;
			}
			var recallTime = $("#recallTime").datetimebox("getValue");              	  //再次外呼的时间
			var note = encodeURI(encodeURI($("#note").val()));                            //备注

			
			
			urlInfo += "&touchFailureReason=" + touchFailureReason;
			urlInfo += "&recallTime=" + recallTime;
			urlInfo += "&note=" + note;
		}else if(currFlg == 2) {    //如果currFlg==2，接触成功
			alert("接触成功...");
		}else {                     //否则则是客户感兴趣
		}
		
		//获取客户信息
		var rows = $("#clientPg").propertygrid("getRows");
		
		for(var i=0; i<rows.length; i++){
			if(i==0) {         //客户编号
				uriInfo += "&clientNo=" + rows[i].value;
			}else if(i==1) {　  //客户号码
				uriInfo += "&clientTelephone=" + rows[i].value;
			}else if(i==2) {　  //备用号码
				uriInfo += "&clientTelephone2=" + rows[i].value;
			}else if(i==3) {　  //客户名称
				uriInfo += "&clientName=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==4) {　  //客户级别
				uriInfo += "&clientLevel=" + rows[i].value;
			}else if(i==5) {　  //客户性别
				uriInfo += "&clientSex=" + rows[i].value;
			}else if(i==6) {　  //归属地
				uriInfo += "&location=" + rows[i].value;
			}else if(i==7) {　  //客户ＱＱ
				uriInfo += "&clientQq=" + rows[i].value;
			}else if(i==8) {　  //电子邮箱
				uriInfo += "&clientEmail=" + rows[i].value;
			}else if(i==9) {　  //公司信息
				uriInfo += "&clientCompany=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==10) {  //地址信息
				uriInfo += "&clientAddress=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==11) {　 //添加时间
				uriInfo += "&clientCreateTime=" + rows[i].value;
			}
			//s += rows[i].name + '(' + i + '):' + rows[i].value + ',\r\n';
		}

		/*$("#touchrecordresultform").form('submit',{
			url:urlInfo,
			type:'POST',
			success:function(data) {
				
			}			
		});*/

		$.messager.progress({
			msg:'系统正在处理，请稍候...',
			interval:3000
		});
		$.ajax({
			url:urlInfo,
			type:'POST',
			dataType:'json',
			success:function(rs) {
				$.messager.progress('close');
				var statusCode = rs.statusCode; //返回的结果类型
				var message = rs.message;       //返回执行的信息
				window.parent.showMessage(message,statusCode);
				if(statusCode == 'success') {         //返回的数据不为空时，才进行勾选当前操作员的角色
					$("#saveTouchResultBtn").attr("disabled",'true');   //如果提交成功时，需要将保存接触结果的按钮设置为不可用，避免重复提交
					$("#clientTouchRecordDg").datagrid({                //刷新接触信息
						url:'taskExecute/touchRecordDatagrid?telephone=' + currTelephone
					});
					//同时还要刷新呼叫号码列表中的数据
					reloadTelephoneDg("任务执行");
				}
			}
		});
	}

	//fucntion，仅保存客户资料方法
	function saveClientInfo() {
		var uriInfo = "taskExecute/updateClientInfo?";
		
		//获取客户信息
		var rows = $("#clientPg").propertygrid("getRows");
		
		for(var i=0; i<rows.length; i++){
			if(i==0) {         //客户编号
				uriInfo += "&clientNo=" + rows[i].value;
			}else if(i==1) {　  //客户号码
				uriInfo += "&clientTelephone=" + rows[i].value;
			}else if(i==2) {　  //备用号码
				uriInfo += "&clientTelephone2=" + rows[i].value;
			}else if(i==3) {　  //客户名称
				uriInfo += "&clientName=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==4) {　  //客户级别
				uriInfo += "&clientLevel=" + rows[i].value;
			}else if(i==5) {　  //客户性别
				uriInfo += "&clientSex=" + rows[i].value;
			}else if(i==6) {　  //归属地
				uriInfo += "&location=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==7) {　  //客户ＱＱ
				uriInfo += "&clientQq=" + rows[i].value;
			}else if(i==8) {　  //电子邮箱
				uriInfo += "&clientEmail=" + rows[i].value;
			}else if(i==9) {　  //公司信息
				uriInfo += "&clientCompany=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==10) {  //地址信息
				uriInfo += "&clientAddress=" + encodeURI(encodeURI(rows[i].value));
			}else if(i==11) {　 //添加时间
				uriInfo += "&clientCreateTime=" + rows[i].value;
			}
			//s += rows[i].name + '(' + i + '):' + rows[i].value + ',\r\n';
		}

		$.messager.progress({
			msg:'系统正在处理，请稍候...',
			interval:3000
		});
		$.ajax({
			url:uriInfo,
			type:'POST',
			dataType:'json',
			success:function(rs) {
				$.messager.progress('close');
				var statusCode = rs.statusCode; //返回的结果类型
				var message = rs.message;       //返回执行的信息
				window.parent.showMessage(message,statusCode);
			}
		});
		
	}
	
	//重新加载待号号码列表
	function reloadTelephoneDg(title) {
		if($("#layout_center_tabs").tabs('exists',title)) {
			$("#layout_center_tabs").tabs('select',title);
			window.top.reload_callTelephoneDg.call();
		}
	}

	function doDial(tel) {
		//alert("doDial(" + currTelephone + " -- " + ${currCallNumber} + ")");
		var teletphone;
		if(tel==null) {
			telephone = currTelephone;
		}else {
			telephone = tel;
		}
		
		var url = "doDial?telephone=" + telephone;
		$.ajax({
			url:url,
			type:'POST',
			dataType:'json',
			success:function(rs) {
				var statusCode = rs.statusCode; //返回的结果类型
				var message = rs.message;       //返回执行的信息
				if(statusCode == "error") {     //出现无法外呼时，才弹屏提示外呼结果
					window.parent.showMessage(message,statusCode);
				}
			}
		});
	}

	function doDialZero() {
		var telephone = "0" + currTelephone;
		doDial(telephone);
	}

	function doHangup() {
		$.ajax({
			url:"doHangup",
			type:'POST',
			dataType:'json',
			success:function(rs) {
				var statusCode = rs.statusCode; //返回的结果类型
				var message = rs.message;       //返回执行的信息
				window.parent.showMessage(message,statusCode);
			}
		});
	}

	//弹屏扫描...	
	function scan() {
		//showMessage("准备扫描。。。。","success");
		$.ajax({
			type:'POST',
			dataType:"json",
			url:'scan',
			success:function(rs) {
			
				var statusCode = rs.statusCode; 	//返回的结果类型
				var message = rs.message;       	//返回执行的信息
				if(statusCode == 'success') {
					//showMessage(message,statusCode);							
					//showincomingpanel("13512771995");
					showdialpanel("13512771995");
				}
			}
		});
	}

	//setInterval(scan,2000);    //定义执行扫描
	
</script>
</head>
<body class="easyui-layout">
	<!-- 顶部 Bannel start -->
	<div data-options="region:'north',border:false" style="overflow:hidden;height:50px;background:url('themes/icons/banner.png') repeat-x;">

		<img src="themes/icons/large_logo.png" />
		<div><%@ include file="/_cti_icons.jsp"%></div>
		<div style="vertical-align: bottom;position:absolute;right:30px;bottom:3px;">
			<a href="#" onClick="modifyPassword()" class="myStyle" style="width:120px;text-decoration: none;color: 14AFFF;">修改密码</a>&nbsp;&nbsp;
			<a href="#" onClick="logout()" class="myStyle" style="width:120px;text-decoration: none;">退出系统</a>
		</div>

	</div>
	<!-- Bannel end -->
	
	<!-- 左则导航 start -->
	<div data-options="region:'west',split:true" title="主菜单" style="width:200px;padding1:1px;overflow:hidden;">
		<div id="menu-accordion" class="easyui-accordion" data-options="fit:true,border:false" style="background-color: #e0ecff">
			
		</div>
	</div>
	<!-- 左则导航 end -->
	
	<!-- 底部 start -->
	<div data-options="region:'south',border:false" style="height:25px;background:#dddddd;">
		<a href="#" class="easyui-linkbutton" data-options="fit:true" style="text-align: left;">
			<span style="font-weight: bold;"> <div id="loginInfoDiv"></div> </span>
			 
			<div style="display: inline" id="currentTime"></div>
		</a>
		
	</div> 
	<!-- 底部 end -->
	
	<!-- 显示区 start -->
	<div data-options="region:'center'" style="overflow:hidden;">
<!--		<div id="layout_center_tabs" style="text-align: center;" class="easyui-tabs" data-options="fit:true,border:false">-->
		<div id="layout_center_tabs" class="easyui-tabs" data-options="fit:true">
			<div title="我的工作台" style="padding:20px;overflow:hidden;" data-options="fit:true"> 
				<%@ include file="/_sysinfo_charts.jsp"%>
				
			</div>
		</div>
	</div>
	<!-- 显示区 end -->

	<!-- tab 右击显示 -->
	<div id="layout_center_tabsMenu" style="width: 120px;display:none;">  
	    <div type="refresh">刷新</div>  
	    <div class="menu-sep"></div>  
	    <div type="close">关闭</div>  
	    <div type="closeOther">关闭其他</div>  
	    <div type="closeAll">关闭所有</div>  
	</div>	
	
	<!-- 将修改密码的窗口包含进来，在点击修改密码时，可以显示该窗口 -->
	<div id="changepasswordpanel" class="easyui-dialog" title="修改密码" data-options="width:300,height:200" modal="true" closed="true" buttons="#changePasswordBtn">
		<form id="changepasswordform">
				<!-- %@ include file="/_changepasswordform.jsp"% -->
				<table>
				<tr>
					<td>原密码</td>
					<td>
						<input name="operator.OLD_PASSWORD" id="OLD_PASSWORD" class="easyui-validatebox" type="text" required="true" missingMessage="原密码不能为空!"></input>
					</td>
				</tr>
				<tr>
					<td>新密码</td>
					<td>
						<input name="operator.NEW_PASSWORD" id="NEW_PASSWORD" type="password" class="easyui-validatebox" type="text" required="true" missingMessage="新密码不能为空!"></input>
					</td>
				</tr>
				<tr>
					<td>确认新密码</td>
					<td>
						<input name="operator.RE_NEW_PASSWORD" id="RE_NEW_PASSWORD" type="password" class="easyui-validatebox" type="text" required="true" missingMessage="确认新密码不能为空!"></input>
					</td>
				</tr>
			</table>
			
			<div id="changePasswordBtn">
				<a href="#" id="saveBtn" class="easyui-linkbutton" iconCls="icon-ok" onclick="changePassword_save()">保存</a>
				<a href="#" id="" class="easyui-linkbutton" iconCls="icon-cancel" onclick="changePassword_cancel()">取消</a>
			</div>
		</form>
	</div>

	<!-- 将外呼的窗口包含进来，在点击外呼时，可以显示该窗口并执行外呼 -->
	<%@ include file="/_dialpanel.jsp"%>	
</body>
</html>