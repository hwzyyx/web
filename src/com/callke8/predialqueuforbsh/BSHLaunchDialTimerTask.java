package com.callke8.predialqueuforbsh;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;

public class BSHLaunchDialTimerTask extends TimerTask {

	private Log log = LogFactory.getLog(BSHLaunchDialTimerTask.class);
	private int i = 1;
	
	public BSHLaunchDialTimerTask() {
		log.info("线程BSHLaunchDialTimerTask： 准备执行 ...");
	}
	
	@Override
	public void run() {
		
		int activeChannelCount = BSHLaunchDialService.activeChannelCount;       //当前活动的通道数量
		int trunkMaxCapacity = BSHCallParamConfig.getTrunkMaxCapacity();		//中继的最大并发量
		
		if(BSHQueueMachineManager.queueCount > 0) {        //如果排队机中有未外呼的号码时
			
			if(trunkMaxCapacity > activeChannelCount) {    //查看是否还有空闲的通道可用
				log.info("排队机中有未外呼数据,系统将取出一条数据执行外呼!");
				
				BSHOrderList bshOrderList = BSHQueueMachineManager.deQueue();
				Thread bshDialServiceThread = new Thread(new BSHLaunchDialService(bshOrderList));
				
				bshDialServiceThread.start();
			}else {
				log.info("排队机中有未外呼数据，但当前活跃通道已达到最大并发量：" + BSHCallParamConfig.getTrunkMaxCapacity() + "，系统暂不执行外呼!");
			}
			
		}else {
			//log.info("当前排队机中没有未外呼数据,暂不执行外呼!");
		}
		
		i++;
		if(i > 10) {
			i = 1;
		}
		
	}

}
