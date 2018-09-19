package com.callke8.fastagi.tts;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.HttpRequestUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;

public class TTSController extends Controller implements IController {

	
	@Override
	public void index() {
		
		String tok = CommonController.getTTSTok();
		setAttr("tokInfo",tok);
		
		setAttr("execTtsUrl",ParamConfig.paramConfigMap.get("paramType_2_ttsExecTtsUrl"));
		
		render("list.jsp");
	}
	
	
	public void download() {
		
		InputStream inputStream = null;
		ServletOutputStream sos = null;
		
		String lan = getPara("lan");          //语言：zh表示中文
		String cuid = getPara("cuid");        //唯一标识符
		String ctp = getPara("ctp");          //ctp 表示web 端访问
		String spd = getPara("spd");          //语速，正常为5，较慢为3，最慢为1，7为较快，最快为9
		String vol = getPara("voiceVolume");  //音量：5为正常，9最大
		String per = getPara("per");          //男性女性
		String tok = getPara("tok");          //taken session 授权码
		String aue = "6";                     //语音文件格式：3为mp3格式(默认)； 4为pcm-16k；5为pcm-8k；6为wav（内容同pcm-16k）; 注意aue=4或者6是语音识别要求的格式，但是音频内容不是语音识别要求的自然人发音，所以识别效果会受影响。
		String tex = null;
		try {   //要进行 TTS 的内容
			tex = URLDecoder.decode(getPara("tex").toString(),"utf-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {  //要进行 TTS 的内容
			tex = URLEncoder.encode(tex,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		};
		System.out.println("lan:" + lan);
		System.out.println("cuid:" + cuid);
		System.out.println("ctp:" + ctp);
		System.out.println("spd:" + spd);
		System.out.println("vol:" + vol);
		System.out.println("per:" + per );
		System.out.println("tok:" + tok );
		System.out.println("tex:" + tex );
		
		try {
			inputStream = HttpRequestUtils.httpRequestForTTS(lan, cuid, ctp, spd, vol, per, tok,aue,tex);
			
			HttpServletResponse res = getResponse();
			res.setHeader("Content-Disposition", "attachment;filename=voice_download.wav");
			res.setContentType("application/octet-stream");
			res.setContentType("application/OCTET-STREAM;charset=UTF-8");
			
			byte b[] = new byte[1024*1024];
			
			int read = 0;
			
			sos = res.getOutputStream();
			
			while((read=inputStream.read(b))!= -1) {
				
				sos.write(b, 0, read);
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			
			try{
				if(sos!=null) {
					sos.close();
				}
				if(inputStream!=null) {
					inputStream.close();
				}
			}catch(IOException e) {
				throw new RuntimeException("没有文件");
			}
			
			
		}
		
		
		
	}
	
	@Override
	public void add() {
		
	}

	@Override
	public void datagrid() {
		
	}

	@Override
	public void delete() {
		
	}


	@Override
	public void update() {
		
	}

}
