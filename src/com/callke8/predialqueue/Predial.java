package com.callke8.predialqueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.SystemResourceThread;


public class Predial {
	
	private Log log = LogFactory.getLog(Predial.class);
	
	public Predial() {
		
	}
	
	public void execDial() {
		
		log.info("准备执行外呼.................................");
		//线程一：扫描外呼任务、并载入外呼号码（由”新建[0]“修改为”已载入[1]“）,并加载到内存的排队机
		/*LoadTaskThread loadTaskThread = new LoadTaskThread();
		loadTaskThread.start();
		
		//线程二：执行外呼线程,从内存的排队机取出外呼数据,并执行外呼
		LaunchDialThread launchDialThread = new LaunchDialThread();
		launchDialThread.start();
		
		//线程三：扫描重试外呼数据,并载入排队机,将状态由 "重试[3]" 修改为 "已载入[1]"
		LoadRetryThread loadRetry = new LoadRetryThread();
		loadRetry.start();
		
		//线程四：挂机监控线程
		HangUpMonitor hangUpMonitor = new HangUpMonitor();
		hangUpMonitor.start();*/
		
		//线程五：系统资源线程，用于统计系统资源使用情况，并存储在静态变量 systemResourceDataRecord 中
		
		SystemResourceThread systemResourceThread = new SystemResourceThread();
		systemResourceThread.start();
		
	}
	
}
