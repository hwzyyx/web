package com.callke8.astutils;

import java.util.Map;

import org.asteriskjava.live.CallerId;

/**
 * Asterisk 通过AMI外呼时的参数配置模板 
 *
 */
public class AsteriskDialParamConfig {
	
	/**
	 * 通道
	 */
	private String channel;
	
	/**
	 * 
	 */
	private String application;
	
	private String applicationData;
	
	private long timeout;
	
	private CallerId callerId;
	
	private Map<String,String> variables;

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getApplicationData() {
		return applicationData;
	}

	public void setApplicationData(String applicationData) {
		this.applicationData = applicationData;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public CallerId getCallerId() {
		return callerId;
	}

	public void setCallerId(CallerId callerId) {
		this.callerId = callerId;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}
	
}
