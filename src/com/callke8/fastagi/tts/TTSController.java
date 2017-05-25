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
import com.callke8.utils.HttpRequestUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;

public class TTSController extends Controller implements IController {

	
	@Override
	public void index() {
		
		String tok = CommonController.getTTSTok();
		setAttr("tokInfo",tok);
		setAttr("execTtsUrl",MemoryVariableUtil.ttsParamMap.get("exec_tts_url"));
		
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
			inputStream = HttpRequestUtils.httpRequestForTTS(lan, cuid, ctp, spd, vol, per, tok, tex);
			
			HttpServletResponse res = getResponse();
			res.setHeader("Content-Disposition", "attachment;filename=voice_download.mp3");
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
