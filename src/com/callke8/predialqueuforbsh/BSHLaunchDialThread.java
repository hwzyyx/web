package com.callke8.predialqueuforbsh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;

public class BSHLaunchDialThread implements Runnable {
	
	
	private Log log = LogFactory.getLog(BSHLaunchDialThread.class);
	
	public BSHLaunchDialThread() {
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
			
			StringBuilder logSb = new StringBuilder();
			
			int activeChannelCount = BSHLaunchDialService.activeChannelCount;       //当前活动的通道数量
			int trunkMaxCapacity = BSHCallParamConfig.getTrunkMaxCapacity();		//中继的最大并发量
			
			
			logSb.append("线程(BSHLaunchDialThread): 第 " + i + " 次扫描排队机,排队机未呼号码数量为：" + BSHQueueMachineManager.queueCount + "个\r\n");
			
			if(BSHQueueMachineManager.queueCount > 0) {     //如果排队机中有未外呼的号码时
				logSb.append("当前活跃的通道数量为:" + activeChannelCount + ",中继最大并发量为:" + trunkMaxCapacity);
				
				//先判断中继最大并发量与当前活跃通话量对比,如果最大并发量大于当前活跃通话量时，表示还有空闲通道可用
				if(trunkMaxCapacity > activeChannelCount) {
					
					//计算空闲的通道数量
					int freeChannelCount = trunkMaxCapacity - activeChannelCount;           //空闲的通道数量，最大并发量-活动的通道数量
					
					int launchDialCount = 0; 		//定义一个变量,用于存储本次将从排队机中取出多少数据进行外呼
					if(freeChannelCount > BSHQueueMachineManager.queueCount) {
						launchDialCount = BSHQueueMachineManager.queueCount;
					}else {
						launchDialCount = freeChannelCount;
					}
					
					logSb.append("本次将取出 " + launchDialCount + " 条数据，执行外呼!");
					
					for(int j = 1;j<=launchDialCount;j++) {
						BSHOrderList bshOrderList = BSHQueueMachineManager.deQueue();
						
						Thread bshDialServiceThread = new Thread(new BSHLaunchDialService(bshOrderList));
						
						bshDialServiceThread.start();
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}else {
					logSb.append("由于当前活跃通道数量达到中继最大并发量" + BSHCallParamConfig.getTrunkMaxCapacity() + "系统本次循环将不再外呼!");
				}
			}
			
			log.info(logSb.toString());
			
			
			i++;
			if(i > 0) {
				i = 1;
			}
			
			try {
				Thread.sleep(BSHCallParamConfig.getScanInterval() * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
	}

}
