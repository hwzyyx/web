package com.callke8.predialqueuforbsh;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;

/**
 * 获取重试订单信息线程，并将其加入排队机
 * 
 * @author 黄文周
 *
 */
public class BSHLoadRetryThread implements Runnable{

	private Log log = LogFactory.getLog(BSHLoadRetryThread.class);
	
	public BSHLoadRetryThread() {
		
	}

	@Override
	public void run() {
		
		try {
			Thread.sleep(5 * 1000);   //为了等待环境变量加载完毕,先休眠5秒
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		int i = 1;
		
		while(true) {
			
			//判断当前时间与系统生效时间对比，如果系统处于 09：00至 20:00时
			int compareResult = BSHCallParamConfig.compareCurrTime2ActiveTime();
			
			if(compareResult == 2) {
			
				int queueCount = BSHQueueMachineManager.queueCount;    			//当前排队机数量
				
				int queueMaxCount = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_3_queueMaxCount"));		//排队机允许最大的排队数量
				int scanCount = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_3_scanCount"));;              //单次扫描的数量
				
				if(queueCount < queueMaxCount) {        //如果排队机中的数量已经大于或是等于允许的最大值，暂不加载数据到排队机
					
					int freeCount = queueMaxCount - queueCount;       //查看排队机中数量与允许最大量的差距
					if(freeCount<scanCount) {                         //如果空闲的数量小于单次扫描数量
						scanCount = freeCount;
					}
					
					//扫描一定数量的待重呼数据到队列中
					List<BSHOrderList> list = BSHOrderList.dao.loadOrderListRetryToQueue(scanCount);
					
					int BSHOrderListRetryCount = 0;
					
					if(!BlankUtils.isBlank(list) && list.size() >0) {
						BSHOrderListRetryCount = list.size();						//取出号码的数量
						
						//将取出的号码，加入排队机
						for(BSHOrderList orderList:list) {
							BSHQueueMachineManager.enQueue(orderList);
						}
					}
					
					log.info("第 " + i + " 次扫描并加载待重呼订单信息到排队机,此次已扫描 " + BSHOrderListRetryCount + " 条数据加入了排队机!");
					
				}
				
			}else {
				log.info("WARNNING 提示=====:BSHLoadRetryThread线程处于非生效时间，系统设定系统的生效时间为:" + BSHCallParamConfig.getActiveStartTime() + " 至 " + BSHCallParamConfig.getActiveEndTime());
			}
			
			i++;
			if(i>10) {
				i = 1;
			}
			
			//休息3秒钟
			try {
				int scanInterval = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_3_scanInterval"));
				Thread.sleep(scanInterval * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
