package com.callke8.predialqueuforbsh;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.BlankUtils;

public class BSHLoadOrderListThread implements Runnable {

	private Log log = LogFactory.getLog(BSHLoadOrderListThread.class);
	
	public BSHLoadOrderListThread() {
		
	}
	
	@Override
	public void run() {
		
		try {     //为了等待环境变量加载完毕，先休眠5秒
			Thread.sleep(5 * 1000);
		}catch(InterruptedException ie) {
			ie.printStackTrace();
		}
		
		//执行到这里，表示虚拟机或是TOMCAT被重启过,需要将已经载入排队机中的数据恢复原状态
		//此操作主要是将记录的状态更改回去就可以了
		int count = BSHOrderList.dao.updateBSHOrderListState(0,"1","0",null);
		log.info("虚拟机或是TOMCAT被重启，系统回滚'已载入'的号码数据：" + count + " 条");
		
		int i = 1;     //标识扫描的次数，10次后重新归1，重新计算
		
		while(true) {
			//AsteriskConnectionPool.listAllConnectionState();
			//判断当前时间与系统生效时间对比，如果系统处于 09:00 至 20:00 之间时，才执行守护程序
			int compareResult = BSHCallParamConfig.compareCurrTime2ActiveTime();
			
			if(compareResult == 2) {
				
				int queueCount = BSHQueueMachineManager.queueCount;				//先查看排队机中的数量
				int queueMaxCount = BSHCallParamConfig.getQueueMaxCount();      //排队机允许最大的排队数量
				int scanCount = BSHCallParamConfig.getScanCount();              //单次扫描的数量
				
				if(queueCount < queueMaxCount) {        //如果排队机中的数量已经大于或是等于允许的最大值，暂不加载数据到排队机
					
					int freeCount = queueMaxCount - queueCount;       //查看排队机中数量与允许最大量的差距
					if(freeCount<scanCount) {                         //如果空闲的数量小于单次扫描数量
						scanCount = freeCount;
					}
					
					List<BSHOrderList> list = BSHOrderList.dao.loadOrderListToQueue(scanCount);
					
					int BSHOrderListCount = 0;
					
					if(!BlankUtils.isBlank(list) && list.size() >0) {
						BSHOrderListCount = list.size();						//取出号码的数量
						
						//将取出的号码，加入排队机
						for(BSHOrderList orderList:list) {
							BSHQueueMachineManager.enQueue(orderList);
						}
					}
					
					log.info("第 " + i + " 次扫描并加载订单信息到排队机,此次已扫描 " + BSHOrderListCount + " 条数据加入了排队机!");
					
					
				}else {
					log.info("第 " + i + " 次扫描并加载订单信息到排队机，由于已经加入排队机中的数量大于设定的允许最大量" + queueMaxCount + ",本次将不再载入号码到排队机!");
				}
				
			}else {
				log.info("WARNNING 提示=====:BSHLoadOrderListThread线程处于非生效时间，系统设定系统的生效时间为:" + BSHCallParamConfig.getActiveStartTime() + " 至 " + BSHCallParamConfig.getActiveEndTime());
			}
			
			
			i++;
			if(i > 10) {
				i = 1;
			}
			
			//修改一定的时间，时间间隔根据 BSHCallParamConfig 配置
			try {
				Thread.sleep(BSHCallParamConfig.getScanInterval() * 1000);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
	}

}



















