package com.callke8.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;

import com.jfinal.render.Render;
import com.jfinal.render.RenderException;

@SuppressWarnings("serial")
public class RenderJson extends Render {
	
	private String statusCode;
	private String message;
	private String extraMessage;   //额外信息,如返回添加生成的ID
	private String extraMessage2;  //额外信息2
	
	public RenderJson() {

	}
	
	public static RenderJson success(String message) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("success");
		renderJson.setMessage(message);
		return renderJson;
	}
	
	public static RenderJson success(String message,String extraMessage) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("success");
		renderJson.setMessage(message);
		renderJson.setExtraMessage(extraMessage);
		return renderJson;
	}
	
	public static RenderJson success(String message,String extraMessage,String extraMessage2) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("success");
		renderJson.setMessage(message);
		renderJson.setExtraMessage(extraMessage);
		renderJson.setExtraMessage2(extraMessage2);
		return renderJson;
	}

	public static RenderJson error(String message) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("error");
		renderJson.setMessage(message);
		return renderJson;
	}
	
	public static RenderJson error(String message,String extraMessage) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("error");
		renderJson.setMessage(message);
		renderJson.setExtraMessage(extraMessage);
		return renderJson;
	}
	
	public static RenderJson error(String message,String extraMessage,String extraMessage2) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("error");
		renderJson.setMessage(message);
		renderJson.setExtraMessage(extraMessage);
		renderJson.setExtraMessage2(extraMessage2);
		return renderJson;
	}
	
	public static RenderJson warn(String message) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("warn");
		renderJson.setMessage(message);
		return renderJson;
	}
	
	public static RenderJson warn(String message,String extraMessage) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("warn");
		renderJson.setMessage(message);
		renderJson.setExtraMessage(extraMessage);
		return renderJson;
	}
	
	public static RenderJson warn(String message,String extraMessage,String extraMessage2) {
		RenderJson renderJson = new RenderJson();
		renderJson.setStatusCode("warn");
		renderJson.setMessage(message);
		renderJson.setExtraMessage(extraMessage);
		renderJson.setExtraMessage2(extraMessage2);
		return renderJson;
	}
	

	@Override
	public void render() {
		PrintWriter writer = null;
		Map m = new HashMap();
		m.put("statusCode", statusCode);
		m.put("message", message);
		m.put("extraMessage",extraMessage);
		m.put("extraMessage2", extraMessage2);
		JSONArray ja = JSONArray.fromObject(m);
		
//		String result = "\"result\":[{\"statusCode\":\"" + statusCode + "\",\"message\":\"" + message + "\"}]"; 
		String result = "{\"statusCode\":\"" + statusCode + "\",\"message\":\"" + message + "\",\"extraMessage\":\"" + extraMessage + "\",\"extraMessage2\":\"" + extraMessage2 + "\"}"; 
		
		try {
			response.setHeader("Pragma", "no-cache"); // HTTP/1.0 caches might
														// not implement
														// Cache-Control and
														// might only implement
														// Pragma: no-cache
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("text/html;charset=" + getEncoding());
			// response.setCharacterEncoding(super.getEncoding());
			writer = response.getWriter();
			writer.write(result);
			writer.flush();
		} catch (IOException e) {
			throw new RenderException(e);
		} finally {
			writer.close();
		}
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	public String getExtraMessage() {
		return extraMessage;
	}

	public void setExtraMessage(String extraMessage) {
		this.extraMessage = extraMessage;
	}

	public String getExtraMessage2() {
		return extraMessage2;
	}

	public void setExtraMessage2(String extraMessage2) {
		this.extraMessage2 = extraMessage2;
	}
	
	
	
}
