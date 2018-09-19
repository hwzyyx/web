package com.callke8.predialqueuforbsh;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;

/**
 * 反馈呼叫结果给服务器线程
 * 
 * 此线程属于非实时执行线程，在收到反馈呼叫结果指令后，3秒钟后才执行
 * 
 * @author 黄文周
 *
 */
public class BSHFeedBackCallResultThread implements Runnable{

	private BSHOrderList bshOrderList;
	
	public BSHFeedBackCallResultThread(BSHOrderList bshOrderList) {
		this.bshOrderList = bshOrderList;
	}
	
	@Override
	public void run() {
		
		/**
		 * 先休眠3秒后，再执行反馈结果
		 */
		try {  Thread.sleep(3 * 1000); } catch (InterruptedException e) { e.printStackTrace(); }
		
		//取出 ID 
		String id = bshOrderList.get("ID").toString();
		
		//根据ID，从数据表中取出订单信息
		BSHOrderList bol = BSHOrderList.dao.getBSHOrderListById(id);
		
		//取出回复的信息，如果回复结果为 1、2、3、4 中的任意一个，都表示当前订单通道已经成功
		//因为表示了客户回复了按键，即使是回复了错误按键，或是无回复按键
		//但是至少表明 BSHCallFlowAgi 流程已经被执行
		int respond = bol.getInt("RESPOND");     //回复结果
		int retried = bol.getInt("RETRIED");     //已重试
		
		if(respond >= 1 && respond <=4) {     //即是 respond 在 1,2,3,4 表示通话成功
			updateBSHOrderListStateForSuccess("SUCCESS",respond);
		}else {                               //表示通道失败
			updateBSHOrderListStateForFailure("NOANSWER",retried);
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
		
		int retryTimes = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_3_retryTimes"));
		int retryInterval = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_3_retryInterval"));
		if(retried < retryTimes) {      //如果已重试次数小于限定的重试次数时
			
			//设置当前号码的状态为重试状态
			BSHOrderList.dao.updateBSHOrderListStateToRetry(bshOrderList.getInt("ID"), "3", retryInterval, lastCallResult);
		}else {
			
			//两次都失败同时，将这个未接听的结果反馈给BSH服务器
			BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), "0", "4");
			Thread httpRequestThread = new Thread(httpRequestT);
			httpRequestThread.start();
			
			//重试次数已经超过或是等于重试次数，直接设置为失败
			BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"),null, "4", lastCallResult);
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
	public void updateBSHOrderListStateForSuccess(String lastCallResult,int respond) {
		BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"), null,"2", lastCallResult);
		
		//将结果返回给服务器
		BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), "1", String.valueOf(respond));
		Thread httpRequestThread = new Thread(httpRequestT);
		httpRequestThread.start();
	}

}
