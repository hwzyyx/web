package com.callke8.astutils;

import java.io.IOException;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;


/**
 * 执行外呼的工具类，当点击外呼时，由 controller 调用该方法执行外呼
 * 
 * @author Administrator
 *
 */
public class CallOutUtil {
	
	public static void doCallOut(java.lang.String channel,
            java.lang.String context,
            java.lang.String exten,
            int priority,
            long timeout,
            CallerId callerId,
            java.util.Map<java.lang.String,java.lang.String> variables,
            OriginateCallback cb) throws Exception {
		
		DefaultAsteriskServer server = CallOutUtil.getAsteriskServer();
		//DefaultAsteriskServer server = new DefaultAsteriskServer("10.8.0.53",5038,"freeiris","freeiris");
		
		System.out.println("server:" + server);
		server.originateToExtensionAsync(channel, context, exten, priority, timeout, callerId, variables,cb);
	}
	
	public static DefaultAsteriskServer getAsteriskServer() {
		DefaultAsteriskServer server = new DefaultAsteriskServer(AstMonitor.getAstHost(),AstMonitor.getAstPort(),AstMonitor.getAstUser(),AstMonitor.getAstPass());
		return server;
	}
	
}
