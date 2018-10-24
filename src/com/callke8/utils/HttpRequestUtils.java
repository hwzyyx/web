package com.callke8.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.callke8.system.param.ParamConfig;

public class HttpRequestUtils {
	
	public InputStream httpRequest() {
		return null;
	}
	
	/**
	 * 用于 TTS 对 http 的请求，并返回 inputStream 流
	 * 
	 * 调用后，方便写出到前端或是储存为语音文件
	 * 
	 * @param lan   
	 * 			语音
	 * @param cuid
	 * 			标识符
	 * @param ctp
	 * 			访问方式
	 * @param spd
	 * 			语速
	 * @param vol
	 * 			音量
	 * @param per
	 * 			语音性别
	 * @param tok
	 * 			验证码标识符
	 * @param aue
	 * 			语音文件格式：3为mp3格式(默认)； 4为pcm-16k；5为pcm-8k；6为wav（内容同pcm-16k）;
	 * @param tex
	 * 			TTS内容
	 * @return
	 */
	public static InputStream httpRequestForTTS(String lan,String cuid,String ctp,String spd,String vol,String per,String tok,String aue,String tex) {
		
		//String execTtsUrl = MemoryVariableUtil.ttsParamMap.get("exec_tts_url");   //执行TTS转换的URL
		String ttsExecTtsUrl = ParamConfig.paramConfigMap.get("paramType_2_ttsExecTtsUrl");   //执行TTS转换的URL
		
		InputStream is = null;
		
		HttpClient httpClient = new HttpClient();
		
		HttpMethod method = new GetMethod(ttsExecTtsUrl + "/text2audio?lan=" + lan + "&cuid=" + cuid + "&ctp=" + ctp + "&spd=" + spd + "&vol=" + vol + "&per=" + per + "&tok=" + tok + "&aue=" + aue + "&tex=" + tex);
		System.out.println("URL地址：" + ttsExecTtsUrl + "/text2audio?lan=" + lan + "&cuid=" + cuid + "&ctp=" + ctp + "&spd=" + spd + "&vol=" + vol + "&per=" + per + "&tok=" + tok + "&aue=" + aue + "&tex=" + tex);
		try {
			httpClient.executeMethod(method);
		
			is = method.getResponseBodyAsStream();
		
		}catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return is;
		
	}
	
	/**
	 * 调用TTS接口，将传入的文字内容转成语音，并保存到传入的文件中去
	 * 
	 * 
	 * @param tok
	 * 			验证码标识符
	 * @param tex
	 * 			TTS内容
	 * @param file
	 * 			存储到文件,文件格式为 mp3 格式
	 * @return
	 */
	public static void httpRequestForTTSToFile(String tok,String tex,String file) {
		
		String lan = "zh";          	//语言：zh表示中文
		String cuid = "13512771995";    //唯一标识符
		String ctp = "1";          		//ctp 1表示web 端访问
		String spd = "3";          		//语速，正常为5，较慢为3，最慢为1，7为较快，最快为9
		String vol = "5";          		//音量：5为正常，9最大
		String per = "0";          		//１：男性  ０：女性
		String aue = "6";               //语音文件格式：3为mp3格式(默认)； 4为pcm-16k；5为pcm-8k；6为wav（内容同pcm-16k）;
		
		String execTtsUrl = ParamConfig.paramConfigMap.get("paramType_2_ttsExecTtsUrl");   //执行TTS转换的URL
		String defaultSpeed = ParamConfig.paramConfigMap.get("paramType_2_defaultSpeed");   //默认语速
		
		if(!BlankUtils.isBlank(defaultSpeed)) {
			spd = defaultSpeed;
		}
		
		InputStream is = null;
		FileOutputStream fos = null;
		
		try {
			
			fos = new FileOutputStream(file);
			
			HttpClient httpClient = new HttpClient();
			
			HttpMethod method = new GetMethod(execTtsUrl + "/text2audio?lan=" + lan + "&cuid=" + cuid + "&ctp=" + ctp + "&spd=" + spd + "&vol=" + vol + "&per=" + per + "&tok=" + tok + "&aue=" + aue + "&tex=" + tex);
		
			
			httpClient.executeMethod(method);
			
			is = method.getResponseBodyAsStream();       //得到输入流
			
			//缓冲文件输入流
			BufferedInputStream bis = new BufferedInputStream(is);
			//缓冲文件输出流
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			int data = 0;
			byte[] b = new byte[1024 * 1024];
			
			while((data = bis.read(b)) != -1) {
				bos.write(b,0,data);
				//强制清空缓冲区的内容
				bos.flush();
			}
			
			
			//流的关闭
			bis.close();
			bos.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			
			try{
				if(fos!=null) {
					fos.close();
				}
				if(is!=null) {
					is.close();
				}
			}catch(IOException e) {
				throw new RuntimeException("没有文件");
			}
			
			
		}
		
		
		
	}
	
	
	/**
	 * 执行 Url 请求，主要是从百度语音开放平台取回 tok(access_token)
	 * 
	 * @return
	 * 		如果请求到 tok, 则以字符串返回；否则返回空字符串
	 */
	public static String httpRequestForTok() {
		
		String tok = null;
		
		String grant_type = ParamConfig.paramConfigMap.get("paramType_2_ttsGrantType");
		String client_id = ParamConfig.paramConfigMap.get("paramType_2_ttsClientId");
		String client_secret = ParamConfig.paramConfigMap.get("paramType_2_ttsClientSecret");
		String access_token_url = ParamConfig.paramConfigMap.get("paramType_2_ttsAccessTokenUrl");
		
		HttpClient httpClient = new HttpClient();
		
		HttpMethod method = new GetMethod(access_token_url + "/token?grant_type=" + grant_type + "&client_id=" + client_id + "&client_secret=" + client_secret);
		try {
			System.out.println("method:----" + method.getName() + "," + method.getURI());
		} catch (URIException e1) {
			e1.printStackTrace();
		}
		try {
			httpClient.executeMethod(method);
		
		
			//打印服务器返回的状态
			System.out.println("打印服务器返回的状态：" + method.getStatusLine());
			
			//打印服务器返回的信息
			String resToString = method.getResponseBodyAsString();
			
			System.out.println("打印服务器返回的信息：" + resToString);
			
			JSONObject json = JSONObject.fromObject(resToString);
			
			//取出 access_token
			Object accessTokenObject = json.get("access_token");
			
			if(!BlankUtils.isBlank(accessTokenObject)) {
				tok = accessTokenObject.toString();
			}
			
			System.out.println("返回的accessToken:" + json.get("access_token"));
			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tok;
		
	}
	
	
}
