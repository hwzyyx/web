package com.callke8.astutils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;
import org.asteriskjava.live.internal.AsteriskServerImpl;

import com.callke8.fastagi.autocontact.AutoContactRecord;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Record;

public class AutoContactCallOutService {

	private Record acr;
	public Log log = LogFactory.getLog(AutoContactCallOutService.class);
	
	public AutoContactCallOutService(Record acr) {
		this.acr = acr;
	}
	
	public void doCallOut() {
		
		String agentNumber = acr.getStr("AGENT_NUMBER");
		String clientNumber = acr.getStr("CLIENT_NUMBER");
		final String cid = acr.getStr("CALLERID");
		String channel = null;
		//定义属性：语音文件名，用于对自动接触进行录音，事先定义好录音文件名。
		//文件格式：日期  + 座席号码 + 客户号码 + 主叫号码
		final String fileName = "autocontact_" + DateFormatUtils.formatDateTime(new Date(), "yyyyMMddhhmmss") + "_" + agentNumber + "_" + clientNumber + "_" + cid + ".wav";
		
		final String voiceFile = "autocontact_record/" + fileName;   //语音文件，录音目录 + 文件名
		
		
		if(agentNumber.length() > 4) {
			channel = MemoryVariableUtil.autoContactMap.get("autoContactChannel") + "/" + agentNumber;
		}else {
			channel = "SIP" + "/" + agentNumber;
		}
		
		String context = MemoryVariableUtil.autoContactMap.get("autoContactContext");
		String exten = clientNumber;
		int priority = 1;
		long timeout = 30 * 1000;
		CallerId callerId = new CallerId(cid,cid);
		Map<String,String> variables = new HashMap<String,String>();
		
		System.out.println("外呼的通道Channel:" + channel);
		
		System.out.println("channel:" + channel);
		System.out.println("context:" + context);
		System.out.println("priority:" + priority);
		System.out.println("timeout:" + timeout);
		System.out.println("callerId:" + callerId);
		
		System.out.println("---==----:channel:" + channel + ",context:" + context + ",exten:" + exten + ",priority:" + priority + ",timeout:" + timeout + ",callerId:" + callerId + ",variables:" + variables);
		
		Map callOutRs = CtiUtils.doCallOut4AutoContact(channel, context, exten, priority, timeout, callerId, variables, new OriginateCallback() {
			
			@Override
			public void onDialing(AsteriskChannel channel) {
				
				channel.setVariable("voiceFile",voiceFile);
				channel.setVariable("CALLERID(num)", cid);
				System.out.println("channel's callerId: " + channel.getCallerId());
				System.out.println("onDialing....");
			}
			
			@Override
			public void onNoAnswer(AsteriskChannel channel) {
				System.out.println("onNoAnswer....");
				AutoContactRecord.dao.updateStatus("3", acr.getInt("ID"),false);  //只要没有接通的，都设置为3，即是外呼失败
			}
			
			@Override
			public void onBusy(AsteriskChannel channel) {
				System.out.println("onBusy....");
				AutoContactRecord.dao.updateStatus("3", acr.getInt("ID"),false);   //只要没有接通的，都设置为3，即是外呼失败
			}
			
			@Override
			public void onFailure(LiveException liveE) {
				System.out.println("onFailure....");
				AutoContactRecord.dao.updateStatus("3", acr.getInt("ID"),false);   //只要没有接通的，都设置为3，即是外呼失败
			}
			
			@Override
			public void onSuccess(AsteriskChannel channel) {
				System.out.println("onSuccess....");
				AutoContactRecord.dao.updateStatus("2", acr.getInt("ID"),false);   //接通时，将状态修改为2,即外呼成功
				AutoContactRecord.dao.updateVoiceFile(fileName, acr.getInt("ID"));
			}
			
		});
		
		System.out.println("外呼结果:" + callOutRs);
	}
	
}
