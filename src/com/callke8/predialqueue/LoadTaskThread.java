package com.callke8.predialqueue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;

public class LoadTaskThread extends Thread {

	int maxLoadCount = 0;   			//最大的载入量
	int scanInterval = 0;   			//扫描的时间间隔
	
	private Log log = LogFactory.getLog(LoadTaskThread.class);
	 
	
	public LoadTaskThread() {
		//将内存中的配置取出
		scanInterval = Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_scanInterval"));  	//扫描时间间隔
		maxLoadCount = Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_maxLoadCount"));      //加载总数量 
		
	}
	
	public void run() {
		
		try {
			Thread.sleep(5 * 1000);   //为了等待环境变量加载完毕,先休眠5秒
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//执行到了这里,表示外呼任务java 的虚拟机被重启过,所以需要先将已经载入到内存中的数据恢复
		int count = AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(0, "1", "0",null);
		log.info("虚拟机被重启,系统回滚“已载入”号码数据：" + count + " 条");
		
		int i = 1;
		
		while(true) {
			
			int activeCalltaskCount = 0;    //激活的任务数量
			int validCallTaskCount = 0;     //有效的激活任务的数量
			List<AutoCallTask> activeCallTaskList = new ArrayList<AutoCallTask>();   //已激活且处于有效期内的任务列表
			List<AutoCallTask> validCallTaskList = new ArrayList<AutoCallTask>();    //有效的任务列表（当外呼任务没有状态为新建的号码时）
			StringBuilder logSb = new StringBuilder();
			
			activeCallTaskList = AutoCallTask.dao.getActiveCallTasks();
			if(!BlankUtils.isBlank(activeCallTaskList)) {
				activeCalltaskCount = activeCallTaskList.size();
			}
			
			if(!BlankUtils.isBlank(activeCallTaskList) && activeCallTaskList.size()>0) {
				for(AutoCallTask autoCallTask:activeCallTaskList) {   //遍历激活的外呼任务列表
					//根据任务ID，取出状态为0,即是“新建”的号码的数量
					int newTelephoneCount = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("0",autoCallTask.get("TASK_ID").toString());
					if(newTelephoneCount>0) {
						validCallTaskList.add(autoCallTask);   //
					}
				}
				validCallTaskCount = validCallTaskList.size();   //有效的任务列表的数量
			}
				
			
			logSb.append(" 第  " + i + " 次扫描外呼任务,已激活可用外呼任务共有: " + activeCalltaskCount + " 条;\r\n 有效任务(即除去无“新建”号码任务)共有：" + validCallTaskCount + " 条。");
			
			//如果已经激活有效的外呼任务列表不为空时
			if(!BlankUtils.isBlank(validCallTaskList) && validCallTaskList.size()>0) {   
				
				//查看已经加载待外呼的数量
				int loadedCount = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("1", null);
				
				if(loadedCount >= maxLoadCount) {        //如果已经加载（未外呼）的量大于或是等于限定的加载量时,将不再加载入待外呼
					logSb.append(" \r\n 由于状态为”已载入“号码数量大于配置设定最大载入量：" + maxLoadCount + ",本次将不再载入号码.");
				}else {
				
					//每个任务要载入的量
					int perTaskLoadCount = (int)Math.floor((maxLoadCount-loadedCount)/validCallTaskCount); 
					int taskNumber = 1;
					
					//遍历外呼任务
					for(AutoCallTask autoCallTask:validCallTaskList) {
						
						String taskId = autoCallTask.get("TASK_ID");       //取出外呼任务ID
						String taskName = autoCallTask.get("TASK_NAME");   //取出外呼任务名称
						
						//根据外呼任务ID,取出状态为0的一定数量的外呼号码
						List<AutoCallTaskTelephone> list = AutoCallTaskTelephone.dao.loadAutoCallTask(taskId, perTaskLoadCount);
						
						int autoCallTaskTelephoneCount = 0;
						
						if(!BlankUtils.isBlank(list) && list.size() > 0) {
							
							autoCallTaskTelephoneCount = list.size();  //取出号码的数量
							
							//将取出的号码,加入排队机
							for(AutoCallTaskTelephone autoCallTaskTelephone:list) {
								QueueMachineManager.enQueue(autoCallTaskTelephone);
							}
							
						}
						
						logSb.append("\r\n 任务(" + taskNumber + ")：" + taskName + ": 共载入 " + autoCallTaskTelephoneCount + " 个外呼号码,排队机中总号码为：" + QueueMachineManager.queueCount);
						taskNumber ++;
					}
				
				}
				
			}
			
			log.info(logSb.toString());  //输出日志
			
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
