package com.callke8.predialqueueforautocallbyquartz;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alibaba.druid.util.StringUtils;
import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.pridialqueueforbshbyquartz.BSHQueueMachineManager;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.StringUtil;

public class AutoCallLoadTaskJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		int activeCallTaskCount = 0;         //已激活的外呼任务数量
		int validCallTaskCount = 0;          //有效的激活任务的数量
		
		List<AutoCallTask> activeCallTaskList = new ArrayList<AutoCallTask>();   //已激活且处于有效期内的任务列表
		List<AutoCallTask> validCallTaskList = new ArrayList<AutoCallTask>();    //有效的任务列表（当外呼任务没有状态为新建的号码时）
		
		StringBuilder logSb = new StringBuilder();      //日志的拼接字符串
		
		//取得已经激活、且任务的生效时间处于当时间内的任务
		activeCallTaskList = AutoCallTask.dao.getActiveCallTasks();
		if(!BlankUtils.isBlank(activeCallTaskList)) {
			activeCallTaskCount = activeCallTaskList.size();
		}
		
		/**
		 * 遍历取出的外呼任务，并根据该任务是否有状态为新建（即状态值为0）的号码数据
		 * 如果有状态为0的号码，表示该任务为有效的任务列表，如果没有则为非有效任务列表，没有进入外呼环节
		 */
		if(!BlankUtils.isBlank(activeCallTaskList) && activeCallTaskCount>0) {
			for(AutoCallTask autoCallTask:activeCallTaskList) {   //遍历激活的外呼任务列表
				//根据任务ID，取出状态为0,即是“新建”的号码的数量
				int newTelephoneCount = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("0",autoCallTask.get("TASK_ID").toString());
				if(newTelephoneCount>0) {
					validCallTaskList.add(autoCallTask);   //
				}
			}
			validCallTaskCount = validCallTaskList.size();   //有效的任务列表的数量
		}
		StringUtil.log(this, "==排队机中未外呼的数据量为:" + AutoCallQueueMachineManager.queueCount + "，当前活跃通道数量为:" + AutoCallPredial.activeChannelCount + ",中继并发量为:" + Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_4_trunkMaxCapacity")));
		logSb.append("线程:AutoCallLoadTaskJob(111111111): 扫描外呼任务，已激活可外呼任务共有: " + activeCallTaskList.size() + " 个;有效任务(除去无外呼号码的任务)，已激活的可外呼任务共有: " + validCallTaskList.size() + " 个。");
		
		//如果已经激活有效的外呼任务列表不为空时
		if(!BlankUtils.isBlank(validCallTaskList) && validCallTaskList.size()>0) {
			
			int queueCount = AutoCallQueueMachineManager.queueCount;												//先查看排队机中的数量
			int queueMaxCount = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_4_queueMaxCount"));		//排队机允许最大的排队数量
			int scanCount = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_4_scanCount"));;              //单次扫描的数量
			
			if(queueCount < queueMaxCount) {      //如果排队机中的数量已经大于或是等于允许的最大值，暂不加载数据到排队机
				
				int freeCount = queueMaxCount - queueCount;       //查看排队机中数量与允许最大量的差距，即是最大允许此次扫描多的数据量到排队机
				//计算为个任务要载入的量
				int perTaskLoadCount = (int)Math.floor(freeCount/validCallTaskCount); 
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
							AutoCallQueueMachineManager.enQueue(autoCallTaskTelephone);
						}
						
					}
					
					logSb.append("\r\n 任务(" + taskNumber + ")：" + taskName + ": 共载入 " + autoCallTaskTelephoneCount + " 个外呼号码,排队机中总号码为：" + AutoCallQueueMachineManager.queueCount);
					StringUtil.log(this, logSb.toString());
					
					taskNumber ++;
				}
				
				
			}else {                               //排队机未外呼的数据量大于或等于了排队允许的最大未呼量
				logSb.append("不过由于排队机中未外呼的记录数大于设定的允许最大值 :" + queueMaxCount + "，此次将跳过扫描数据到排队机!");
				
				StringUtil.log(this, logSb.toString());
			}
			
			
		}else {
			
			logSb.append(",由于没有已激活的可外呼任务,系统暂时不会向扫描数据加入排队机!");
			
			StringUtil.log(this, logSb.toString());
		}
		
		
	}

}
