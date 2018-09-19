package com.callke8.predialqueue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.live.CallerId;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;

public class LaunchDialThread extends Thread {

	int scanInterval = 0;            //扫描间隔
	int scanCount = 0;               //每次扫描的数量
	int timeout = 0;                 //外呼超时时间
	int maxConcurrentCount = 0;   	 //并发量（同时支持几路通话）
	String channelPrefix = "";       //通道前缀
	String agiUrl = "";              //AGI地址
	private Log log = LogFactory.getLog(LaunchDialThread.class);
	
	public LaunchDialThread() {
		scanInterval = 0;// Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_scanInterval"));
		scanCount = 0;//Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_scanCount"));
		timeout = 30;//Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_timeout"));
		maxConcurrentCount = 0;//Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_maxConcurrentCount"));
		channelPrefix = "";//MemoryVariableUtil.autoCallTaskMap.get("ac_channelPrefix");
		agiUrl = "";//MemoryVariableUtil.autoCallTaskMap.get("ac_agiUrl");
	}
	
	public void run() {
		
		try {
			Thread.sleep(5 * 1000);   //为了等待环境变量加载完毕,先休眠5秒
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		int i = 1;
		
		while(true) {
			
			StringBuilder logSb = new StringBuilder();
			
			logSb.append(" 第  " + i + " 次扫描排队机,未外呼的号码数量为: " + QueueMachineManager.queueCount + " 个\r\n");
			
			//log.info(logSb.toString());
			
			if(QueueMachineManager.queueCount >0) {   //如果排队机中有未外呼的号码时
				
				logSb.append("当前活动通道数量：" + LaunchDialService.activeChannelCount + ",最大并发量为:" + maxConcurrentCount + "\r\n");
				
				//先判断最大的并发量与现在已经在外呼的通道数量
				if(maxConcurrentCount > LaunchDialService.activeChannelCount) {   
					
					//如果最大并发量比活动的通道量大时
					//计算空闲的通道数量
					Integer freeChannelCount = maxConcurrentCount - LaunchDialService.activeChannelCount; 
					
					Integer launchDialCount  = 0;    //定义一个变量,用于存储本次将从排队机中取出多少数据进行外呼
					if(freeChannelCount >  QueueMachineManager.queueCount) {   //如果可用通道数量大于排队机中的数量
						launchDialCount = QueueMachineManager.queueCount;
					}else {
						launchDialCount = freeChannelCount;
					}
					
					logSb.append("本次将取出：" + launchDialCount + " 条外呼数据,执行外呼!");
					
					for(int j = 1;j<=launchDialCount; j++) {   //循环空闲通道数量,并从排队机中取出
						
						//从排队机中取出外呼数据
						AutoCallTaskTelephone autoCallTaskTelephone = QueueMachineManager.deQueue();
						
						if(!BlankUtils.isBlank(autoCallTaskTelephone)) {   //如果取出的号码不为空时执行外呼
							
							LaunchDialService dialService = new LaunchDialService(autoCallTaskTelephone);
							
							Thread dialServiceThread = new Thread(dialService);
							
							dialServiceThread.start();
							
						}
						
					}
					
				}else {
					
					logSb.append("由于当前活动通道数量达到最大并发量,系统将跳过外呼!");
				}
				
			}
			
			log.info(logSb.toString());
			
			i++;
			if(i>10) {
				i = 1;
			}
			
			try {
				sleep(scanInterval * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
