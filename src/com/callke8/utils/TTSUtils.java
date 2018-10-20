package com.callke8.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.callke8.common.CommonController;
import com.callke8.system.param.ParamConfig;
import com.jfinal.kit.PathKit;

/**
 * TTS工具
 * 
 * @author 黄文周
 *
 */
public class TTSUtils {

	
	/**
	 * 执行TTS,执行 TTS 语音转换，将语音文件存放在 voicePath，然后再转为8000的单声道语音文件，存放在 voicePathSingle
	 * 
	 * @param fileName
	 * @param content
	 * @param voicePath
	 * @param voicePathSingle
	 */
	public static void doTTS(String fileName,String content,String voicePath,String voicePathSingle) {
		
		try {   //要进行 TTS 的内容
			content = URLDecoder.decode(content,"UTF-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			content = URLEncoder.encode(content,"UTF-8");   //加一层转换
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String tok = CommonController.getTTSTok();
		
		//组织放置语音文件的绝对路径
		String voicePathFullDir = PathKit.getWebRootPath() + File.separator + voicePath + File.separator + fileName + ".wav";
		String voicePathSingleFullDir = PathKit.getWebRootPath() + File.separator + voicePathSingle + File.separator + fileName + ".wav";
		
		//执行TTS操作,并保存到 outputDir
		HttpRequestUtils.httpRequestForTTSToFile(tok, content, voicePathFullDir);
		
		//然后执行sox转换
		String chmodCmd = "chmod 777 " + voicePathFullDir;
		
		String cmd = ParamConfig.paramConfigMap.get("paramType_1_soxBinPath") + " " + voicePathFullDir + " -r 8000 -c 1 " + voicePathSingleFullDir;
		System.out.println("执行的 CMD  命令为:" + cmd);
		
		try {
			Process chmodP = Runtime.getRuntime().exec(chmodCmd);
			
			Process p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.out.println("执行语音格式转换失败!");
			e.printStackTrace(); 
		}
		
	}
	
}
