package com.callke8.predialqueuforbsh;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.TimeoutException;

import com.callke8.astutils.AsteriskDialParamConfig;
import com.callke8.astutils.AsteriskUtils;
import com.callke8.astutils.CtiUtils;
import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

public class BSHLaunchDialService implements Runnable {
	
	public static Integer activeChannelCount = 0;    //当前活动的通道数量
	
	//储存通话通道及订单信息,在本呼叫执行的流程中，不判断及修改呼叫状态，转到挂机时，再处理呼叫状态，及将呼叫结果反馈给服务器
	public static Map<String,BSHOrderList> activeChannelList = new HashMap<String,BSHOrderList>();
	
	
	private BSHOrderList bshOrderList;
	private int retried;                //已经已重试次数
	
	private AsteriskDialParamConfig dialPC = new AsteriskDialParamConfig();
	
	private Log log = LogFactory.getLog(BSHLaunchDialService.class);

	public BSHLaunchDialService(BSHOrderList bshOrderList) {
		activeChannelCount++;                        //当执行到这里时，当前活动通道即为1
		this.bshOrderList = bshOrderList;			 //将参数赋值
		
		retried = bshOrderList.getInt("RETRIED");    //已重试次数
		
		dialPC.setChannel(BSHCallParamConfig.getTrunkInfo() + "/" + bshOrderList.get("CALLOUT_TEL"));
		dialPC.setApplication("AGI");
		dialPC.setApplicationData(BSHCallParamConfig.getAgiUrl());
		dialPC.setTimeout(30 * 1000);
		dialPC.setCallerId(new CallerId(BSHCallParamConfig.getCallerNumber(), BSHCallParamConfig.getCallerNumber()));
		dialPC.setVariables(new HashMap<String,String>());
		
		//调用已载入超时处理线程，对于超过3分钟，呼叫状态仍为1（已载入）时，强制修改其呼叫状态
		/*BSHHandleLoadedTimeOutThread handleLoadedTimeOutT = new BSHHandleLoadedTimeOutThread(bshOrderList);
		Thread handleLoadedTimeOutThread = new Thread(handleLoadedTimeOutT);
		handleLoadedTimeOutThread.start();*/
		Timer timer = new Timer();
		timer.schedule(new BSHHandleLoadedTimeOutTimerTask(bshOrderList.get("ID").toString(), timer), 3 * 60 * 1000);
		
	}
	
	@Override
	public void run() {
		
		//在执行之前取出一个连接，并获取连接状态是否正确
		final AsteriskUtils au = new AsteriskUtils();
		boolean connState = au.isAstConnSuccess();    //检查是否连接成功
		log.info("系统准备执行呼叫,Asterisk服务器的连接状态为:" + connState);
		
		if(!connState) {    //如果连接状态有问题,则不做外呼,直接更改号码的状态
			log.info("PBX系统连接状态有异常....");
			
			//如果失败，将再一次连接，如果连接，然后再去判断是否连接成功
			try {
				au.doLogin();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (AuthenticationFailedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
			
			connState = au.isAstConnSuccess();
			if(!connState) {    //如果还是连接失败，将直接保存其为失败状态
				au.close();
				updateBSHOrderListStateForFailure("DISCONNECTION");       //更改状态为失败或是重试，并指定最后失败原因为 未连接
				return;
			}
			
		}
		
		CtiUtils.doCallOutToApplication(au,dialPC.getChannel(), dialPC.getApplication(), dialPC.getApplicationData(), dialPC.getTimeout(), dialPC.getCallerId(), dialPC.getVariables(), new OriginateCallback() {
			
			@Override
			public void onDialing(AsteriskChannel channel) {
				channel.setVariable("bshOrderListId", bshOrderList.get("ID").toString());    //设置通道变量,以便在 BSHCallFlowAGI 中用于查询订单信息
				activeChannelList.put(channel.getName(), bshOrderList);                      //onDialing时通道已生成，在这里将activeChannel指向 Map
				log.info("onDialing(准备执行呼叫并生成通道):" + dialPC.getChannel() + ",通道标识:" + channel.getName());
				StringUtil.writeString("/opt/dial-log.log",DateFormatUtils.getCurrentDate() + ",onDialing(准备呼叫)：" + dialPC.getChannel() + ",通道标识:" + channel.getName() + ",UNIQUEID:" + channel.getId(), true);
			}
			
			@Override
			public void onNoAnswer(AsteriskChannel channel) {   //未接听，更改状态
				log.info("onNoAnswer,通道：" + dialPC.getChannel() + " 未接听,通道channel.getName():" + channel.getName());
				StringUtil.writeString("/opt/dial-log.log",DateFormatUtils.getCurrentDate() + ",onNoAnswer(未接事件)：" + dialPC.getChannel() + ",通道标识:" + channel.getName() + ",UNIQUEID:" + channel.getId(), true);
				
				au.close();   //回收通道
			}
			
			@Override
			public void onFailure(LiveException le) {				//呼叫失败,生成通道异常
				updateBSHOrderListStateForFailure("FAILURE");       //当执行到这里，通道异常时，执行失败储存
				log.info("onFailure,通道：" + dialPC.getChannel() + " 建立通道失败!");
				StringUtil.writeString("/opt/dial-log.log",DateFormatUtils.getCurrentDate() + ",onFailure(生成通道异常)：" + dialPC.getChannel(), true);
				au.close();   //回收通道
			}
			
			@Override
			public void onBusy(AsteriskChannel channel) {			//用户忙
				log.info("onBusy,通道：" + dialPC.getChannel() + " 用户忙!" + ",UNIQUEID:" + channel.getId());
				StringUtil.writeString("/opt/dial-log.log",DateFormatUtils.getCurrentDate() + ",onBusy(用户忙)：" + dialPC.getChannel() + ",通道标识:" + channel.getName() + ",UNIQUEID:" + channel.getId(), true);
				au.close();   //回收通道
			}
			
			@Override
			public void onSuccess(AsteriskChannel channel) {    //通话成功
				
				/**
				 * 通话成功时,通话并没有结束,所以活动通道不能减除,而是需要将通道加入内存
				 * 加一个事件监控线程,在挂机事件时，再将其解除
				 */
				log.info("onSuccess,通道：" + dialPC.getChannel() + " 通话成功SUCCESS! ");
				StringUtil.writeString("/opt/dial-log.log",DateFormatUtils.getCurrentDate() + ",onSuccess(接听事件)：" + dialPC.getChannel() + ",通道标识:" + channel.getName() + ",UNIQUEID:" + channel.getId(), true);
				
				au.close();   //回收通道
			}
		});
		
	}
	
	/**
	 * 外呼失败时,更改号码的状态
	 * 失败的原因有三：1 pbx的连接不成功;2呼叫失败;3 无人接听
	 * 
	 * @param lastCallResult
	 * 				最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 */
	public void updateBSHOrderListStateForFailure(String lastCallResult) {
		
		if(retried < BSHCallParamConfig.getRetryTimes()) {      //如果已重试次数小于限定的重试次数时
			
			//设置当前号码的状态为重试状态
			BSHOrderList.dao.updateBSHOrderListStateToRetry(bshOrderList.getInt("ID"), "3", BSHCallParamConfig.getRetryInterval(), lastCallResult);
		}else {
			
			//两次都失败同时，将这个未接听的结果反馈给BSH服务器
			BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), "0", "4");
			Thread httpRequestThread = new Thread(httpRequestT);
			httpRequestThread.start();
			
			//重试次数已经超过或是等于重试次数，直接设置为失败
			BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"),null, "4", lastCallResult);
		}
		
		if(activeChannelCount > 0) {         //返回之前，活动的通道数量减1
			activeChannelCount--;
		}
		
	}
	
	/**
	 * 通话成功时保存
	 * 
	 * 通话成功时,通话并没有结束,所以活动通道不能减除,而是需要将通道加入内存
	 * 加一个事件监控线程,在挂机事件时，再将其解除
	 * 
	 * @param lastCallResult
	 */
	public void updateBSHOrderListStateForSuccess(String lastCallResult) {
		BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"), null,"2", lastCallResult);
	}
	
	
	
	
	
	
	

}
