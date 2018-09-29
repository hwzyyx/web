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
		this.server = new DefaultAsteriskServer("localhost",6038,"admin","vvopadmin");
	}
	
	public static void main(String[] args) {
		CallOutDemo cod = new CallOutDemo();
		cod.doCallOut();
		
		try {
			Thread.sleep(50 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void doCallOut() {
		
		String channel = "SIP/Trunk-1432/3056013512771995";
		
		String context = "playvoice";
		String exten = "8856";
		int priority = 1;
		long timeout = 30 * 1000;
		CallerId callerId = new CallerId("008651986626735","008651986626735");
		Map<String,String> m = new HashMap<String,String>();
		//m.put("voiceName", "helloworld");
		m.put("voiceName", "1537399539000");
		System.out.println("准备外呼");
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
		
		System.out.println("呼叫结束");
		
	}
	

}
