package com.callke8.test;

import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;

public class CallOutDemo {
	
	private DefaultAsteriskServer server;
	
	public CallOutDemo() {
		this.server = new DefaultAsteriskServer("localhost",5041,"admin","vvopadmin");
	}
	
	public void doCallOut() {
		
		String channel = "SIP/AvayaTrunk/0013512771995";
		
		String context = "from-sip";
		String exten = "4400";
		int priority = 1;
		long timeout = 15 * 1000;
		CallerId callerId = new CallerId("4009286999","4009286999");
		Map<String,String> m = new HashMap<String,String>();
		System.out.println("׼��ִ�к���...");
		server.originateToExtensionAsync(channel,context, exten, priority, timeout, callerId, m, new OriginateCallback() {

			@Override
			public void onDialing(AsteriskChannel arg0) {
				System.out.println("CallOutDemo.doCallOut().new OriginateCallback() {...}.onDialing()");
			}
			
			@Override
			public void onNoAnswer(AsteriskChannel arg0) {
				System.out.println("CallOutDemo.doCallOut().new OriginateCallback() {...}.onNoAnswer()");
				server.shutdown();
			}
			
			@Override
			public void onBusy(AsteriskChannel arg0) {
				System.out.println("CallOutDemo.doCallOut().new OriginateCallback() {...}.onBusy()");
				server.shutdown();
			}


			@Override
			public void onFailure(LiveException arg0) {
				System.out.println("CallOutDemo.doCallOut().new OriginateCallback() {...}.onFailure()");
				server.shutdown();
			}


			@Override
			public void onSuccess(AsteriskChannel arg0) {
				System.out.println("CallOutDemo.doCallOut().new OriginateCallback() {...}.onSuccess()");
				server.shutdown();
			}
			
		});
		
		System.out.println("����ִ�н���...");
		
	}
	

}
