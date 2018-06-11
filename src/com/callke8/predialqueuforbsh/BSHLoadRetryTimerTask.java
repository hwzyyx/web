package com.callke8.predialqueuforbsh;

import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.BlankUtils;

/**
 * 获取重试订单信息线程，并将其加入排队机
 * 
 * @author 黄文周
 */
public class BSHLoadRetryTimerTask extends TimerTask {

	private Log log = LogFactory.getLog(BSHLoadRetryTimerTask.class);
	private int i = 1;
	
	public BSHLoadRetryTimerTask() {
		log.info("线程BSHLoadRetryTimerTask： 准备执行 ...");
	}
	
	@Override
	public void run() {
		
		//判断当前时间与系统生效时间对比，如果系统处于 09：00至 20:00时
		int compareResult = BSHCallParamConfig.compareCurrTime2ActiveTime();
		
		if(compareResult == 2) {
			
			int queueCount = BSHQueueMachineManager.queueCount;    			//当前排队机数量
			int queueMaxCount = BSHCallParamConfig.getQueueMaxCount();      //排队机允许的最大数量
			int scanCount = BSHCallParamConfig.getScanCount();				//单次扫描的数量
			
			if(queueCount < queueMaxCount) {        //如果排队机中的数量已经大于或是等于允许的最大值，暂不加载数据到排队机
				
				int freeCount = queueMaxCount - queueCount;       //查看排队机中数量与允许最大量的差距
				if(freeCount<scanCount) {                         //如果空闲的数量小于单次扫描数量
					scanCount = freeCount;
				}
				
				//扫描一定数量的待重呼数据到排队机中
				List<BSHOrderList> list = BSHOrderList.dao.loadOrderListRetryToQueue(scanCount);
				
				int BSHOrderListRetryCount = 0;
				if(!BlankUtils.isBlank(list) && list.size() >0) {
					BSHOrderListRetryCount = list.size();						//取出号码的数量
					
					//将取出的号码，加入排队机
					for(BSHOrderList orderList:list) {
						BSHQueueMachineManager.enQueue(orderList);
					}
				}
				
				log.info("线程 BSHLoadRetryTimerTask 第 " + i + " 次扫描并加载待重呼订单信息到排队机,此次扫描 " + BSHOrderListRetryCount + " 条数据,排队机中未外呼的数量为:" + BSHQueueMachineManager.queueCount);
			}
			
		}else {
			log.info("线程BSHLoadRetryTimerTask ：未到执行时间，系统执行时间为 : " + BSHCallParamConfig.getActiveStartTime() + " 至  " + BSHCallParamConfig.getActiveEndTime());
		}
		
		i++;
		if(i>10) {
			i = 1;
		}
	}

}
