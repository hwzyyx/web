package com.callke8.predialqueuforbsh;

import java.util.List;
import java.util.TimerTask;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

/**
 * 处理状态为1（即是已载入）的记录
 * 
 * 如果状态为1，但是外呼时间已经超过了5分钟时，强制处理该记录
 * 
 * @author 黄文周
 *
 */
public class BSHHandleState1TimerTask extends TimerTask {

	private int i = 1;
	
	public BSHHandleState1TimerTask() {
		StringUtil.log(this, "（处理状态为1：已载入）线程BSHHandleState1TimerTask 准备启动!");
	}
	
	@Override
	public void run() {
		
		String befault5MinuteDateTime = DateFormatUtils.getBeforeSecondDateTime(300);    //取得 300 秒之前的时间字符串，格式为 yyyy-MM-dd HH:mm:ss
		
		
		List<Record> list = BSHOrderList.dao.getBSHOrderListByCondition(null, null, null, null, null, null, "1", null, null, null,null,null, befault5MinuteDateTime);
		
		if(!BlankUtils.isBlank(list) && list.size()>0) {     //如果查询出来的订单列表数据不为空，即是有未处理的状态为1（已载入）但是载入时间已超过5分钟的记录
			StringUtil.log(this, "（处理状态为1：已载入）线程BSHHandleState1TimerTask：第   " + i + " 次处理状态为1，但是已经超时（5分钟）的记录,此次取出 " + list.size() + " 条数据进行处理!");
			
			for(Record bshOrderList:list) {                             //取出订单信息进行分钟
				int id = bshOrderList.getInt("ID");
				String orderId = bshOrderList.get("ORDER_ID");
				String customerName = bshOrderList.get("CUSTOMER_NAME");
				String customerTel = bshOrderList.get("CUSTOMER_TEL");
				String createTime = bshOrderList.getDate("CREATE_TIME").toString();
				String loadTime = bshOrderList.getDate("LOAD_TIME").toString();
				
				System.out.println("bshOrderList: " + bshOrderList);
				
				int retried = bshOrderList.getInt("RETRIED_VALUE");
				
				StringUtil.log(this, "数据处理：id: " + id + ",订单ID：" + orderId + ",客户姓名: " + customerName + ", 客户号码: " + customerTel + ", 重试次数:" + retried + ",createTime: " + createTime + ",loadTime: " + loadTime + "属超时数据,需要强制处理!");
				
				StringUtil.writeString("/opt/force_handle_state1record.log",DateFormatUtils.getCurrentDate() + "\t" + "数据处理：id: " + id + ",订单ID：" + orderId + ",客户姓名: " + customerName + ", 客户号码: " + customerTel + ", 重试次数:" + retried + ",createTime: " + createTime + ",loadTime: " + loadTime + "属超时数据,需要强制处理!\r\n", true);
				
				updateBSHOrderListStateForFailure(bshOrderList);
				
			}
			
		}else {
			StringUtil.log(this, "（处理状态为1：已载入）线程BSHHandleState1TimerTask：第   " + i + " 次处理状态为1，但是已经超时（5分钟）的记录,此次没有数据需要处理!");
		}
		
		i++;
		if(i>10) {
			i = 1;
		}
		
	}
	
	/**
	 * 外呼失败时,更改号码的状态
	 * 失败的原因有三：1 pbx的连接不成功;2呼叫失败;3 无人接听
	 * 
	 * @param lastCallResult
	 * 				最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功);未知：UNKNOWN
	 */
	public void updateBSHOrderListStateForFailure(Record bshOrderList) {
		
		int retried = bshOrderList.getInt("RETRIED_VALUE");        //已重试
		String lastCallResult = "UNKNOWN";                   //最后的外呼结果
		
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
		
		if(BSHLaunchDialService.activeChannelCount > 0) {         //返回之前，活动的通道数量减1
			BSHLaunchDialService.activeChannelCount--;
		}
		
	}

}
