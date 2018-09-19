package com.callke8.astutils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;

import com.callke8.system.param.ParamConfig;

/**
 * CtiService 为 CtiUtils 的执行 CTI 线程
 * 
 * @author hasee
 *
 */
public class LaunchCtiService implements Runnable{

	String methodName;
	
	String agentNumber;
	
	String clientNumber;
	
	Log log = LogFactory.getLog(LaunchCtiService.class);
	
	public LaunchCtiService(String methodName,String agentNumber,String clientNumber) {
		this.methodName = methodName;
		this.agentNumber = agentNumber;
		this.clientNumber = clientNumber;
	}

	@Override
	public void run() {
		
		System.out.println("准备执行呼叫...");
		if(methodName.equalsIgnoreCase("doCallOutByAgent")) {
			doCallOutByAgent(agentNumber,clientNumber);
		}
		
	}
	
	/**
	 * 
	 * 执行外呼操作（通过座席进行外呼）
	 * 
	 * @param agentNumber
	 * @param clientNumber
	 */
	public void doCallOutByAgent(String agentNumber,String clientNumber) {
		
		String channel = "SIP/" + agentNumber;
		String context = ParamConfig.paramConfigMap.get("paramType_1_defaultCallOutContext");
		String exten = clientNumber;
		int priority = 1;
		int timeout = 30 * 1000;
		CallerId callerId = null;
		if(agentNumber.length()<=4) {
			callerId = new CallerId(agentNumber,agentNumber);
		}else {
			String defaultCallerId = ParamConfig.paramConfigMap.get("paramType_1_defaultCallerId");
			callerId = new CallerId(defaultCallerId,defaultCallerId);
		}
		
		Map variables = new HashMap();
		
		CtiUtils.doCallOutToExtension(channel, context, exten, priority, timeout, callerId, variables, new OriginateCallback(){

			@Override
			public void onDialing(AsteriskChannel channel) {
				log.info("系统正在呼叫:" + channel.getName());
			}
			
			@Override
			public void onNoAnswer(AsteriskChannel channel) {
				log.info(channel.getName() + " 未接听!");
			}
			
			@Override
			public void onFailure(LiveException channel) {
				log.info("外呼失败!");
			}
			
			@Override
			public void onBusy(AsteriskChannel channel) {
				log.info(channel.getName() + " 正忙!");
			}
			
			@Override
			public void onSuccess(AsteriskChannel channel) {
				log.info(channel.getName() + " 接通成功!");
			}
			
		});
		
	}
	
	
	
	
}
