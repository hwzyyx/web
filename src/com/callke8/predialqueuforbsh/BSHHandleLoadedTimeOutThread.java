package com.callke8.predialqueuforbsh;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;

/**
 * 处理已载入，但超时的状态
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
public class BSHHandleLoadedTimeOutThread implements Runnable {

	private BSHOrderList bshOrderList;
	
	public BSHHandleLoadedTimeOutThread(BSHOrderList bshOrderList) {
		this.bshOrderList = bshOrderList;
	}
	
	@Override
	public void run() {
		
		/**
		 * 先休眠3分钟后，再进行处理，已载入，但是已超时任务
		 */
		try {  Thread.sleep(3 * 60 * 1000); } catch (InterruptedException e) { e.printStackTrace(); }
		
		//取出 ID 
		String id = bshOrderList.get("ID").toString();
		
		//根据ID，从数据表中取出订单信息
		BSHOrderList bol = BSHOrderList.dao.getBSHOrderListById(id);
		
		//取出回复的信息，如果回复结果为 1、2、3、4 中的任意一个，都表示当前订单通道已经成功
		//因为表示了客户回复了按键，即使是回复了错误按键，或是无回复按键
		//但是至少表明 BSHCallFlowAgi 流程已经被执行
		int respond = bol.getInt("RESPOND");     //回复结果
		int retried = bol.getInt("RETRIED");     //已重试
		int state = bol.getInt("STATE");         //呼叫状态：0：未处理;1：已载入;2:已成功;3:待重呼;4:已失败;5:已过期;6:放弃呼叫;
		
		if(state==1 && respond == 0) {                           //如果状态仍为1，即是已载入时，才表示条件内记录，同时保证 respond 不为 1,2,3,4
			//执行强制修改呼叫状态，表示为失败
			updateBSHOrderListStateForFailure("NOANSWER",retried);
		}else if(state==1 && respond != 0) {                     //如果状态仍为1，但是回复已经修改为非0，即是客户有输入时，表示该通话已经呼叫成功。
			updateBSHOrderListStateForSuccess("SUCCESS","1",String.valueOf(respond));
		}
		
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
			BSHOrderList.dao.updateBSHOrderListStateToRetry(bshOrderList.getInt("ID"), "3", BSHCallParamConfig.getRetryInterval(), lastCallResult);
		}else {
			
			//两次都失败同时，将这个未接听的结果反馈给BSH服务器
			BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), "0", "4");
			Thread httpRequestThread = new Thread(httpRequestT);
			httpRequestThread.start();
			
			//重试次数已经超过或是等于重试次数，直接设置为失败
			BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"),null, "4", lastCallResult);
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
		BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"), null,"2", lastCallResult);
		//同时，将呼叫成功结果反馈给 BSH 服务器
		BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), callType, callResult);
		Thread httpRequestThread = new Thread(httpRequestT);
		httpRequestThread.start();
		
		if(BSHLaunchDialService.activeChannelCount > 0) {   //返回之前,活动的通道数量减1
			BSHLaunchDialService.activeChannelCount--;
		}
	}

}
