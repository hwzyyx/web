package com.callke8.astutils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;

/**
 * 
 * CtiService 为 CtiUtils 的执行CTI线程
 * 
 * @author hasee
 *
 */
public class LaunchCtiService implements Runnable {
	
	String methodName;
	
	String agentNumber;
	
	String clientNumber;
	
	Logger log = Logger.getLogger(LaunchCtiService.class);
	
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
		
		System.out.println("执行呼叫结束...");
		
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
		String context = AstMonitor.getAstCallOutContext();
		String exten = clientNumber;
		int priority = 1;
		int timeout = 30 * 1000;
		CallerId callerId = null;
		if(agentNumber.length()<=4) {
			callerId = new CallerId(agentNumber,agentNumber);
		}else {
			callerId = new CallerId(AstMonitor.getAstCallerId(),AstMonitor.getAstCallerId());
		}
		
		Map variables = new HashMap();
		
		final DefaultAsteriskServer server = new DefaultAsteriskServer(AstMonitor.getAstHost(),AstMonitor.getAstPort(),AstMonitor.getAstUser(),AstMonitor.getAstPass());
		
		server.originateToExtensionAsync(channel, context, exten, priority, timeout, callerId, variables, new OriginateCallback() {
			
			@Override
			public void onDialing(AsteriskChannel channel) {
				log.info("系统正在呼叫:" + channel.getName());
			}
			
			@Override
			public void onNoAnswer(AsteriskChannel channel) {
				log.info(channel.getName() + " 未接听!");
				server.shutdown();
			}
			
			@Override
			public void onFailure(LiveException channel) {
				log.info("外呼失败!");
				server.shutdown();
			}
			
			@Override
			public void onBusy(AsteriskChannel channel) {
				log.info(channel.getName() + " 正忙!");
				server.shutdown();
			}
			
			@Override
			public void onSuccess(AsteriskChannel channel) {
				log.info(channel.getName() + " 接通成功!");
				server.shutdown();
			}
			
		});
		
	}
	
}
