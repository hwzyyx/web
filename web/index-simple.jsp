<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<title>呼叫中心系统</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<style type="text/css">
		
		.icon {
          /* 通过设置 font-size 来改变图标大小 */
          width: 3em; height: 3em;
          /* 图标和文字相邻时，垂直对齐 */
          vertical-align: -2em;
          /* 通过设置 color 来改变 SVG 的颜色/fill */
          fill: currentColor;
          /* path 和 stroke 溢出 viewBox 部分在 IE 下会显示
             normalize.css 中也包含这行 */
          overflow: hidden;
        }
		
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
		
		//定义动态增加 tab 的方法
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
		
		var menuAccordionData = eval('${menuAccordionData}');    //取得从后端返回的菜单信息字符串，并转为 js 
		var loginInfo = '${loginInfo}';
		var currAgentNumber = '${currAgentNumber}';
		var webSiteName = '${webSiteName}';
		
		$(function(){
			
			$(document).attr('title',webSiteName);      //设置网页的标题
			$("#loginInfoDiv").html(loginInfo);    		//显示登录的信息，在底部显示
			
			menuShow();
			
			//定义修改密码对话框关闭时的动作，清除数据 
			$("#changepasswordpanel").dialog({
				onClose:function() {
					$("#changepasswordform").form("clear");
				}
			});
			
			
		});
		
		//点击退出登录触发的方法
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
		
		//修改密码表单弹窗弹出
		function modifyPassword() {
			//alert("aa");
			$("#changepasswordpanel").dialog('setTitle','修改登录密码').dialog('open');
		}
		
		//修改密码表单关闭
		function changePassword_cancel() {
			$("#changepasswordpanel").dialog("close");
		}
		
		//保存修改密码的结果
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
		
		//显示左则导航的菜单(MENU)，并定义 tab 的相关动作
		function menuShow() {
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
		}
		
	</script>
	
	<script type="text/javascript">
	
	</script>
</head>
<body class="easyui-layout">
	
	<!-- 顶部 Bannel start -->
	<div data-options="region:'north',border:false" style="overflow:hidden;height:50px;background:url('themes/icons/banner.png') repeat-x;">
		<!-- 
		<img src="themes/icons/bosch_logo.png" />
		 -->
		<div></div>
		<div style="vertical-align: bottom;position:absolute;right:120px;bottom:3px;">
			
			<a href="#" onClick="modifyPassword()" class="myStyle" style="width:120px;text-decoration: none;color: 14AFFF;">修改密码</a>&nbsp;&nbsp;
			<a href="#" onClick="logout()" class="myStyle" style="width:120px;text-decoration: none;">退出系统</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<a href="#" class="myStyle" style="width:120px;text-decoration: none;color:#00ff00;">座席状态：</a>&nbsp;&nbsp;
		</div>
		<!-- 座席状态图标 -->
		<div style="vertical-align: bottom;position:absolute;right:100px;top:20px;">
			<svg class="icon" aria-hidden="true">
             	<use id="agentStateIcon" xlink:href="#icon-zhuangtai1"></use>
         	</svg>
		</div>
		<div style="vertical-align: bottom;position:absolute;right:50px;bottom:3px;">
			<a href="#" id="agentStateDesc" class="myStyle" style="width:120px;text-decoration: none;">未知状态</a>
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
				<!-- %@ include file="/_sysinfo_charts.jsp"% -->
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

</body>
</html>