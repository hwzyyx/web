<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" content="ie=edge"/>
	<title>Client Side Pagination in DataGrid - jQuery EasyUI Demo</title>
	<link rel="stylesheet" type="text/css" href="themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="themes/icon.css">
	<link rel="stylesheet" type="text/css" href="demo.css">
	<link rel="stylesheet" type="text/css" href="jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.min.css">
	<script type="text/javascript" src="jquery.min.js"></script>
	<script type="text/javascript" src="jquery.easyui.min.js"></script>
	<script type="text/javascript" src="jplayer/dist/jplayer/jquery.jplayer.min.js"></script>
	<script type="text/javascript" src="locale/easyui-lang-zh_CN.js"></script>
    <script type="text/javascript" src="base-loading.js"></script>
	<script type="text/javascript">	

		var voiceSex = 0;
		var voiceSpeed = 5;
		var voiceVolume = 5;
		var tokInfo = '${tokInfo}';             //tok 依靠服务器端返回
		var execTtsUrl = '${execTtsUrl}';       //从服务器端返回执行TTS的URL地址
		
		function mouseoverF(){
			var contentValue = $("#tts_content").val();

			if(contentValue=='请输入文字并点击播放') {
				$("#tts_content").val('');
			}
		}
		function mouseoutF(){
			var contentValue = $("#tts_content").val();
			if(contentValue=='') {
				$("#tts_content").val('请输入文字并点击播放');
			}

			//设置语音流
			setStream();
		}

		/*
			文本上传模式
			    上传参数
			参数 	可需 	描述
			tex 	必填 	合成的文本，使用UTF-8编码，请注意文本长度必须小于1024字节
			lan 	必填 	语言选择,填写zh
			tok 	必填 	开放平台获取到的开发者 access_token
			ctp 	必填 	客户端类型选择，web端填写1
			cuid 	必填 	用户唯一标识，用来区分用户，填写机器 MAC 地址或 IMEI 码，长度为60以内
			spd 	选填 	语速，取值0-9，默认为5中语速
			pit 	选填 	音调，取值0-9，默认为5中语调
			vol 	选填 	音量，取值0-9，默认为5中音量
			per 	选填 	发音人选择，取值0-1, 0为女声，1为男声，默认为女声

			GET调用方式

			将所有的参数都填写到URL地址中，可以通过浏览器可以播放合成的语音结果。

			    http://tsn.baidu.com/text2audio?tex=***&lan=zh&cuid=***&ctp=1&tok=***
			--------------------------------------------------------------------
	    	错误码解释
	    	错误码 	含义
	    	500 	不支持输入
	    	501 	输入参数不正确
	    	502 	token验证失败
	    	503 	合成后端错误
		*/
		
		function setStream() {

			$("#ttsPlayer").jPlayer("stop");
			var urlInfo = execTtsUrl + "/text2audio?lan=zh&cuid=13512771995&ctp=1&spd=" + voiceSpeed + "&vol=" + voiceVolume + "&per=" + voiceSex + "&tok=" + tokInfo;

			var contentValue = $("#tts_content").val();   //输入框的内容

			urlInfo = urlInfo + "&tex=" + contentValue;

			//$("#ttsPlayer").jPlayer("setMedia",{title:"文字信息为：" + contentValue,mp3:urlInfo});
			$("#ttsPlayer").jPlayer("setMedia",{title:"请输入文字并点击播放",mp3:urlInfo});
			
		}

		function downloadFile() {

			var contentValue = $("#tts_content").val();   //输入框的内容
			contentValue = encodeURI(encodeURI(contentValue));
			var form = $("<form>");
			form.attr("style","display:none");
			form.attr("target","");
			form.attr("method","POST");
			form.attr("action","tts/download?lan=zh&cuid=13512771995&ctp=1&spd=" + voiceSpeed + "&vol=" + voiceVolume + "&per=" + voiceSex + "&tok=" + tokInfo + "&tex=" + contentValue);
			

			$('body').append(form);

			form.submit();
			form.remove();
			
		}
		
		$(function(){

				$('#tts_content').keyup(function(){
					//alert("输入了一次");
					var len = $(this).val().length;
	
					if(len>199) {
						$(this).val($(this).val().substring(0,200));
					}
	
					var lessNum = 200 - len;
	
					if(lessNum<0){lessNum=0;}
					
					$("#ttsContentLengthNotice").html("还能输入 " + lessNum + " 个字");
					
				});
			
				if(tokInfo == null || tokInfo == '') {
					alert("系统无法获取到TTS的  TOK(AccessToken),语音合成服务可能无法使用!");
				}
			
				var contentValue = $("#tts_content").val();
				var stream={
	    	    	title:"输入文字点击播放",
	    	    	mp3: execTtsUrl + "/text2audio?tex=" + contentValue + "&lan=zh&cuid=13512771995&ctp=1&tok=" + tokInfo
	    	    },
	    	    ready=false;

	    		$("#ttsPlayer").jPlayer({
	        		ready:function(event) {
	        			ready=true;
	        			$(this).jPlayer("setMedia",stream);
	    			},
	    			swfPath:"jplayer/dist/jplayer",
	        		supplied:"mp3,wav,m4a,oga",
	        		wmode:"window",
	        		useStateClassSkin:true,
	    			autoBlur:false,
	    			smoothPlayBar:true,
	    			keyEnabled:true,
	    			remainingDuration:false,
	    			toggleDuration:true
	        	});

				//男声
	        	$("#maleVoice").bind("click",function(){  
		        	if(voiceSex!=1) {
			        	voiceSex = 1;
			        	$("#voiceSexImg").attr("src","themes/icons/voiceSex-male.png");
		        		setStream();
		        	} 
		        });
		        //女声
	        	$("#femaleVoice").bind("click",function(){ 
		        	if(voiceSex != 0 ) {
		        		voiceSex = 0;
			        	$("#voiceSexImg").attr("src","themes/icons/voiceSex-female.png");
		        		setStream(); 
		        	}
		        });

				//语速---------
				//最慢
	        	$("#slowest").bind("click",function(){ 
		        	if(voiceSpeed != 1) {
		        		voiceSpeed = 1;
		        		setStream(); 
		        	}
		        });
				//较慢
	        	$("#slow").bind("click",function(){
	        		if(voiceSpeed != 3) {
		        		voiceSpeed = 3;
		        		setStream(); 
		        	}
				});
				//正常
	        	$("#normal").bind("click",function(){ 
	        		if(voiceSpeed != 5) {
		        		voiceSpeed = 5;
		        		setStream(); 
		        	}
				 });
				//较快
	        	$("#fast").bind("click",function(){
	        		if(voiceSpeed != 7) {
		        		voiceSpeed = 7;
		        		setStream(); 
		        	}
				});
				//最快
	        	$("#fastest").bind("click",function(){ 
	        		if(voiceSpeed != 9) {
		        		voiceSpeed = 9;
		        		setStream(); 
		        	}
		        });

				//声音音量调节
	        	$("#volumeControl").slider({
		        	onComplete:function(value) {
	        			voiceVolume = Math.round((value/10));
	        			if(voiceVolume>9) {
		        			voiceVolume = 9;
	        			}
	        			setStream();
	        		}
		        });
		        
		});
					
	</script>	
</head>

<body>
	<!-- 页面加载效果 -->
	<%@ include file="/base_loading.jsp" %>
	<div class="easyui-panel" title="在线TTS(语音合成)" data-options="fit:true" style="text-align: center;">
			<!-- 编辑区下半部分列表 -->
		<div class="easyui-panel" style="padding-top:5px;padding-left:5px;"  data-options="fit:true">
			
					<table border="1" cellspacing="0" cellpadding="0" bordercolor="#c4e1ff" width="680"
					  style="border-collapse: collapse;">
						<tr height="450px;">
							<td width="450px;" style="vertical-align: top;padding-top:4px;">
								<img alt="" id="voiceSexImg" src="themes/icons/voiceSex-female.png" style="vertical-align: top;">
								 <textarea onmouseout="mouseoutF()" id="tts_content" name="tts_content" rows="20" cols="50"
								 onmouseover="mouseoverF()"
								 >请输入文字并点击播放</textarea>
								<div style="padding-left:65px;padding-top:10px;">
					        		<span id="ttsContentLengthNotice">还可以输入200字</span>
					        	</div>
								<!-- 播放器定义 -->
								<div id="player" style="padding-left:160px;padding-top:30px;">
									
									<div id="ttsPlayer" class="jp-jplayer"></div>
										
									<div id="jp_container_1" style="" class="jp-audio-stream" role="application" aria-label="media player">
										<div class="jp-type-single">
											<div class="jp-gui jp-interface">
												<div class="jp-controls">
													<button class="jp-play" role="button" tabindex="0">play</button>
												</div>
												<div class="jp-volume-controls">
													<button class="jp-mute" role="button" tabindex="0">mute</button>
													<button class="jp-volume-max" role="button" tabindex="0">max volume</button>
													<div class="jp-volume-bar">
														<div class="jp-volume-bar-value"></div>
													</div>
												</div>
												<div class="jp-time-holder">
													<div class="jp-current-time" role="timer" aria-label="time">&nbsp;</div>
													<div class="jp-duration" role="timer" aria-label="duration">&nbsp;</div>
												</div>
											</div>
											<div class="jp-details">
												<div class="jp-title" aria-label="title">&nbsp;aaaa</div>
											</div>
											
										</div>
									</div>
									
									
								</div>
								<div style="padding-left:180px;padding-top:10px;">
									<!-- 播放器定义 结束-->
									<input type="button" value="语音文件导出(wav文件)" onclick="downloadFile()"></input>
								</div>                                           
							</td>
							<td style="vertical-align: top;text-align: left;">
								<div style="padding-top:10px;"></div>
								&nbsp;&nbsp;<span style="font-size: 14px;">发声人性别：</span>
								<div class="easyui-panel" style="padding-top:5px;padding-left:5px;padding-bottom:50px;vertical-align: top;top;border: 0px;">
									<a href="#" id="maleVoice" class="easyui-linkbutton" data-options="toggle:true,group:'g1'" style="width:80px;">男 &nbsp;&nbsp;&nbsp;性</a>
									<a href="#" id="femaleVoice" class="easyui-linkbutton" data-options="toggle:true,group:'g1',selected:true" style="width:80px;">女&nbsp;&nbsp;&nbsp;性</a>
								</div>
								&nbsp;&nbsp;<span style="font-size: 14px;">发声语速：</span>
								<div class="easyui-panel" style="padding-left:5px;padding-top:5px;padding-bottom:50px;vertical-align: top;border: 0px;">
									<a href="#" id="slowest" class="easyui-linkbutton" data-options="toggle:true,group:'g2'">最慢</a>
									<a href="#" id="slow" class="easyui-linkbutton" data-options="toggle:true,group:'g2'">较慢</a>
									<a href="#" id="normal" class="easyui-linkbutton" data-options="toggle:true,group:'g2',selected:true">正常</a>
									<a href="#" id="fast" class="easyui-linkbutton" data-options="toggle:true,group:'g2'">较快</a>
									<a href="#" id="fastest" class="easyui-linkbutton" data-options="toggle:true,group:'g2'">最快</a>
								</div>
								
								&nbsp;&nbsp;<span style="font-size: 14px;">音量：</span>
								<div style="padding-left:15px;padding-top:15px;">
									<input id="volumeControl" class="easyui-slider" value="50" style="width:180px" data-options="
										showTip:true,
										rule: [0,'|',25,'|',50,'|',75,'|',100]
									">
								</div>
							</td>
						</tr>
					</table>
				
			
		</div>	
			
	</div>

</body>
</html>

