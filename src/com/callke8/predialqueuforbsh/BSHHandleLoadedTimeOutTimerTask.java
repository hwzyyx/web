package com.callke8.predialqueuforbsh;

import java.util.Timer;
import java.util.TimerTask;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.BlankUtils;

/**
 * 处理博世外呼订单信息的已载入但已超时（初始设定为3分钟）的记录。
 * 
 * 由于 Avaya 通道的状态有些异常，所以有时候，两个通话会出现串通道的问题
 * 
 * 导致有些通话一直处于”已载入“状态，主要是在挂机事件时，通过挂机事件无法处理订单的呼叫状态
 * 
 * 此线程就是在一定时间内，强制进行处理
 * 
 * 如何确定时间：播放全部语音（两遍） + 8 秒休息，大概需要1分半钟 ，加上呼叫等待时间30秒，
 * 
 * 所以我们暂定为 3分钟强制处理这些记录
 * 
 * @author 黄文周
 *
 */
public class BSHHandleLoadedTimeOutTimerTask extends TimerTask {

	private Timer timer;
	private String id;
	private BSHOrderList bshOrderList;
	
	/**
	 * 构造函数，传入订单信息的ID，和定时器 timer
	 * 
	 * @param id
	 * @param timer
	 */
	public BSHHandleLoadedTimeOutTimerTask(String id,Timer timer) {
		this.timer = timer;
		this.id = id;
	}
	
	@Override
	public void run() {
		
		//根据ID，从数据表中取出订单信息
		bshOrderList = BSHOrderList.dao.getBSHOrderListById(id);   
		
		//===========根据取出的订单，分析订单信息===========
		if(!BlankUtils.isBlank(bshOrderList)) {
			//取出回复的信息，如果回复的结果为 1、2、3、4 中的任意一个，都表示当前订单外呼已经成功
			int respond = bshOrderList.getInt("RESPOND");       //回复结果
			int retried = bshOrderList.getInt("RETRIED");       //已重试次数
			int state = bshOrderList.getInt("STATE");           //呼叫状态：0：未处理;1：已载入;2:已成功;3:待重呼;4:已失败;5:已过期;6:放弃呼叫;
			
			if(state==1 && respond==0) {               //如果状态为1，即是已载入时，表示同时保证 respond 不为 1,2,3,4，表示已经失败
				//执行强制修改呼叫状态，表示为失败
				updateBSHOrderListStateForFailure("NOANSWER",retried);
			}else if(state==1 && respond!=0) {         //如果状态为1，即是已载入时，同时回复不为0时，表示呼叫成功
				updateBSHOrderListStateForSuccess("SUCCESS","1",String.valueOf(respond));
			}
			
		}
		
		timer.cancel();    //结束此定时器
	}
	
	/**
	 * 外呼失败时,更改号码的状态
	 * 失败的原因有三：1 pbx的连接不成功;2呼叫失败;3 无人接听
	 * 
	 * @param lastCallResult
	 * 				最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 */
	public void updateBSHOrderListStateForFailure(String lastCallResult,int retried) {
		
		if(retried < BSHCallParamConfig.getRetryTimes()) {      //如果已重试次数小于限定的重试次数时
			
			//设置当前号码的状态为重试状态
			BSHOrderList.dao.updateBSHOrderListStateToRetry(Integer.valueOf(id), "3", BSHCallParamConfig.getRetryInterval(), lastCallResult);
		}else {
			
			//两次都失败同时，将这个未接听的结果反馈给BSH服务器
			BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(id,bshOrderList.getStr("ORDER_ID"), "0", "4");
			Thread httpRequestThread = new Thread(httpRequestT);
			httpRequestThread.start();
			
			//重试次数已经超过或是等于重试次数，直接设置为失败
			BSHOrderList.dao.updateBSHOrderListState(Integer.valueOf(id),null, "4", lastCallResult);
		}
		
		if(BSHLaunchDialService.activeChannelCount > 0) {   //返回之前,活动的通道数量减1
			BSHLaunchDialService.activeChannelCount--;
		}
		
	}
	
	/**
	 * 通话成功时保存
	 * 
	 * 通话成功时,通话并没有结束,所以活动通道不能减除,而是需要将通道加入内存
	 * 加一个事件监控线程,在挂机事件时，再将其解除
	 * 
	 *  *  * 参数	说明
		orderId	订单号id
		callType	外呼类型0.二次未接通1.一次接通/二次接通2放弃呼叫3已过期
		time	时间（yyyyMMddHHmmss）
		sign	签名（全小写）= md5(time + orderId+ key)key为约定好的密钥
		callResult	外呼结果 1：确认建单   2 暂不安装  3 短信确认   4 错误或无回复  5 放弃呼叫 6已过期
	 * 
	 * @param id
	 * 				订单对应的ID，用于储存反馈提交反馈json及由服务器返回的处理结果
	 * 
	 * @param orderId
	 * 				订单编号
	 * @param callType
	 * 				外呼类型
	 * @param callResult
	 * 				外呼结果
	 * 
	 * @param lastCallResult
	 */
	public void updateBSHOrderListStateForSuccess(String lastCallResult,String callType,String callResult) {
		//强制修改呼叫结果为2，即是已成功
		BSHOrderList.dao.updateBSHOrderListState(Integer.valueOf(id), null,"2", lastCallResult);
		//同时，将呼叫成功结果反馈给 BSH 服务器
		BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(id,bshOrderList.getStr("ORDER_ID"), callType, callResult);
		Thread httpRequestThread = new Thread(httpRequestT);
		httpRequestThread.start();
		
		if(BSHLaunchDialService.activeChannelCount > 0) {   //返回之前,活动的通道数量减1
			BSHLaunchDialService.activeChannelCount--;
		}
	}
	
}
