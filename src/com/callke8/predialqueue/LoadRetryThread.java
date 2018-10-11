package com.callke8.predialqueue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;

/**
 * 获取重试任务
 * 
 * @author hwz
 *
 */
public class LoadRetryThread extends Thread {

	int maxLoadCount = 0;   			//最大的载入量
	int scanInterval = 0;   			//扫描的时间间隔
	
	private Log log = LogFactory.getLog(LoadRetryThread.class);
	 
	
	public LoadRetryThread() {
		//将内存中的配置取出
		scanInterval = 0;//Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_scanInterval"));  //扫描时间间隔
		maxLoadCount = 0;//Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_maxLoadCount"));      //加载总数量 
		
	}
	
	public void run() {
		
		try {
			Thread.sleep(5 * 1000);   //为了等待环境变量加载完毕,先休眠5秒
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//执行到了这里,表示外呼任务java 的虚拟机被重启过,所以需要先将已经载入到内存中的数据恢复
		int i = 1;
		
		while(true) {
			//重试数量
			int retryCount = 0;
			
			//只要已载入的量小于最大的载入量,每次取10个重试的数量
			if(QueueMachineManager.queueCount < maxLoadCount) {
				
				List<AutoCallTaskTelephone> retryList = AutoCallTaskTelephone.dao.loadRetryData(10,null);
				
				
				if(!BlankUtils.isBlank(retryList) && retryList.size()>0) {
					
					retryCount = retryList.size();
					
					//如果有数据时,加入排队机
					for(AutoCallTaskTelephone autoCallTaskTelephone:retryList) {
						
						QueueMachineManager.enQueue(autoCallTaskTelephone);
						
					}
				}
				
			}
			
			//只有当重试的数量大于0时,才以日志提示
			if(retryCount > 0) {
				log.info("系统已载入 " + retryCount + " 条重试外呼数据到排队机!");
			}
			
			i++;
			if(i>10) {
				i = 1;
			}
			
			//休息3秒钟
			try {
				sleep(scanInterval * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
