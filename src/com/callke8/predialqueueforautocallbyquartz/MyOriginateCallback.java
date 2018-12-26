package com.callke8.predialqueueforautocallbyquartz;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;

import com.callke8.astutils.AsteriskUtils;
import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.StringUtil;

public class MyOriginateCallback implements OriginateCallback {

	private AutoCallTaskTelephone actt;
	private AutoCallTask autoCallTask;
	private AsteriskUtils au;
	//private DefaultAsteriskServer server;
	
	/**
	 * 构造函数
	 */
	public MyOriginateCallback(AutoCallTaskTelephone actt,AsteriskUtils au) {
		this.actt = actt;
		this.autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(actt.getStr("TASK_ID"));
		this.au = au;
		//this.server = server;
	}
	
	/**
	 * 外呼开始
	 */
	@Override
	public void onDialing(AsteriskChannel channel) {
		StringUtil.log(this, "MyOriginateCallback->onDialing(准备执行外呼并生成通道)：主叫号码:" + actt.getStr("CALLERID") + ",客户号码:" + actt.getStr("CALLOUT_TEL") + ",通道:" + channel.getName());
	}

	/**
	 * 未接
	 */
	@Override
	public void onNoAnswer(AsteriskChannel channel) {
		System.out.println("-----channel.PROPERTY_STATE：" + channel.PROPERTY_STATE);
		StringUtil.log(this, "MyOriginateCallback->onNoAnswer(通道未接听)：主叫号码:" + actt.getStr("CALLERID") + ",客户号码:" + actt.getStr("CALLOUT_TEL") + ",通道:" + channel.getName() + ",挂机原因:" + channel.getHangupCause());
		
		
		//执行外呼失败时，将外呼结果存储到数据表
		AutoCallPredial.updateTelehponeStateForFailure("2",String.valueOf(channel.getHangupCause()), actt, autoCallTask);
		
		//外呼失败，释放外呼资源
		if(AutoCallPredial.activeChannelCount > 0) {
			AutoCallPredial.activeChannelCount--;        //释放资源
			//关闭Asterisk连接，释放外呼资源
		}
		au.close();
		//server.shutdown();
	}

	/**
	 * 用户忙
	 */
	@Override
	public void onBusy(AsteriskChannel channel) {
		System.out.println("-----channel.PROPERTY_STATE：" + channel.PROPERTY_STATE);
		StringUtil.log(this, "MyOriginateCallback->onBusy(客户忙)：主叫号码:" + actt.getStr("CALLERID") + ",客户号码:" + actt.getStr("CALLOUT_TEL") + ",通道:" + channel.getName() + ",挂机原因:" + channel.getHangupCause());
		
		//执行外呼失败时，将外呼结果存储到数据表
		AutoCallPredial.updateTelehponeStateForFailure("3",String.valueOf(channel.getHangupCause()), actt, autoCallTask);
		
		//外呼失败，释放外呼资源
		if(AutoCallPredial.activeChannelCount > 0) {
			AutoCallPredial.activeChannelCount--;        //释放资源
			//关闭Asterisk连接，释放外呼资源
		}
		au.close();
		//server.shutdown();
	}

	/**
	 * 请求通道失败
	 */
	@Override
	public void onFailure(LiveException liveException) {
		StringUtil.log(this, "MyOriginateCallback->onFailure(请求通道失败)：主叫号码:" + actt.getStr("CALLERID") + ",客户号码:" + actt.getStr("CALLOUT_TEL"));
		//关闭Asterisk连接，释放外呼资源
		
		//执行外呼失败时，将外呼结果存储到数据表
		AutoCallPredial.updateTelehponeStateForFailure("4","请求通道失败", actt, autoCallTask);
		
		//外呼失败，释放外呼资源
		if(AutoCallPredial.activeChannelCount > 0) {
			AutoCallPredial.activeChannelCount--;        //释放资源
		}
		au.close();
		//server.shutdown();
	}

	/**
	 * 呼叫成功
	 */
	@Override
	public void onSuccess(AsteriskChannel channel) {
		StringUtil.log(this, "MyOriginateCallback->onSuccess(呼叫成功)：主叫号码:" + actt.getStr("CALLERID") + ",客户号码:" + actt.getStr("CALLOUT_TEL"));
		//关闭Asterisk连接，释放外呼资源
		au.close();
		//server.shutdown();
		//呼叫成功后，会转到 AGI 执行播放语音，暂不在这里修改状态和释放资源。
	}

}
