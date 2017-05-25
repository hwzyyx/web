package com.callke8.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

import com.jfinal.render.Render;
import com.jfinal.render.RenderException;

@SuppressWarnings("serial")
public class DwzRenderJson extends Render {
	private String statusCode = "200";
	private String message = "";
	private String navTabId = "";
	private String callbackType = "";
	private String forwardUrl = "";
	private String rel = "";
	private String confirmMsg = "";

	public DwzRenderJson(String message, String navTabId, String callbackType) {
		this.message = message;
		this.navTabId = navTabId;
		this.callbackType = callbackType;

	}

	public DwzRenderJson() {

	}

	public static DwzRenderJson success() {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.setMessage("操作成功");
		return dwzRenderJson;
	}

	public static DwzRenderJson success(String successMsg) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.setMessage(successMsg);
		return dwzRenderJson;
	}

	public static DwzRenderJson success(String successMsg, String navTabId) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.setMessage(successMsg);
		dwzRenderJson.setNavTabId(navTabId);
		return dwzRenderJson;
	}
	
	public static DwzRenderJson success(String successMsg, String navTabId,String rel,String forwardUrl) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.setMessage(successMsg);
		dwzRenderJson.setNavTabId(navTabId);
		dwzRenderJson.setRel(rel);
		dwzRenderJson.setForwardUrl(forwardUrl);
		return dwzRenderJson;
	}
	
	public static DwzRenderJson success(String successMsg, String navTabId,String rel,String forwardUrl,String callbackType) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.setMessage(successMsg);
		dwzRenderJson.setNavTabId(navTabId);
		dwzRenderJson.setRel(rel);
		dwzRenderJson.setForwardUrl(forwardUrl);
		dwzRenderJson.setCallbackType(callbackType);
		return dwzRenderJson;
	}

	public static DwzRenderJson error() {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.statusCode = "300";
		dwzRenderJson.message = "操作失败";
		return dwzRenderJson;
	}

	public static DwzRenderJson error(String errorMsg) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.statusCode = "300";
		dwzRenderJson.message = errorMsg;
		return dwzRenderJson;
	}

	public static Render refresh(String refreshNavTabId) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.navTabId = refreshNavTabId;
		return dwzRenderJson;
	}

	public static DwzRenderJson closeCurrentAndRefresh(String refreshNavTabId) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.navTabId = refreshNavTabId;
		dwzRenderJson.callbackType = "closeCurrent";
		return dwzRenderJson;
	}

	public static DwzRenderJson closeCurrentAndFoward(String refreshNavTabId,
			String fowardUrl) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.navTabId = refreshNavTabId;
		dwzRenderJson.callbackType = "closeCurrent";
		dwzRenderJson.forwardUrl = fowardUrl;
		return dwzRenderJson;
	}

	public static DwzRenderJson closeCurrentAndRefresh(String refreshNavTabId,
			String message) {
		DwzRenderJson dwzRenderJson = new DwzRenderJson();
		dwzRenderJson.navTabId = refreshNavTabId;
		dwzRenderJson.message = message;
		dwzRenderJson.callbackType = "closeCurrent";
		return dwzRenderJson;
	}

	@Override
	public void render() {
		PrintWriter writer = null;
		String dwz = "\"statusCode\":\"{0}\",\"message\":\"{1}\",\"navTabId\":\"{2}\",\"rel\":\"{3}\",\"callbackType\":\"{4}\",\"forwardUrl\":\"{5}\",\"confirmMsg\":\"{6}\"";
		dwz = "{\n"
				+ MessageFormat.format(dwz, statusCode, message, navTabId, rel,
						callbackType, forwardUrl, confirmMsg) + "\n}";
		System.out.println(dwz);
		try {
			System.out.println(dwz);
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
			writer.write(dwz);
			writer.flush();
		} catch (IOException e) {
			throw new RenderException(e);
		} finally {
			writer.close();
		}
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getNavTabId() {
		return navTabId;
	}

	public void setNavTabId(String navTabId) {
		this.navTabId = navTabId;
	}

	public String getCallbackType() {
		return callbackType;
	}

	public void setCallbackType(String callbackType) {
		this.callbackType = callbackType;
	}

	public String getForwardUrl() {
		return forwardUrl;
	}

	public void setForwardUrl(String forwardUrl) {
		this.forwardUrl = forwardUrl;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getConfirmMsg() {
		return confirmMsg;
	}

	public void setConfirmMsg(String confirmMsg) {
		this.confirmMsg = confirmMsg;
	}

}
