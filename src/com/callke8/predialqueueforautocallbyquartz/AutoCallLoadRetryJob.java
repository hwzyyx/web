package com.callke8.predialqueueforautocallbyquartz;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.predialqueue.QueueMachineManager;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.StringUtil;

public class AutoCallLoadRetryJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		int retryCount = 0;
		
		int queueCount = AutoCallQueueMachineManager.queueCount;												//先查看排队机中的数量
		int queueMaxCount = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_4_queueMaxCount"));		//排队机允许最大的排队数量
		int scanCount = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_4_scanCount"));;              //单次扫描的数量
		
		
		StringBuilder sb = new StringBuilder();    //拼接日志
		sb.append("====系统准备运行加载待重呼线程任务(AutoCallLoadRetryJob)====。现时排队机中排队数量为:" + queueCount + ",系统允许排队机最大数量为:" + queueMaxCount + "。");
		
		if(queueCount < queueMaxCount) {      //如果排队机中的数量已经大于或是等于允许的最大值，暂不加载数据到排队机 
			
			int freeQueueCount = queueMaxCount - queueCount;   //查看空闲的排队机空间
			if(freeQueueCount > scanCount) {
				freeQueueCount = scanCount;
			}
			
			//而且还有一个条件要考虑进去，如果外呼任务处于非激活状态时，系统就不要扫描未激活任务的待重呼数据。
			List<AutoCallTask> activeCallTaskList = AutoCallTask.dao.getActiveCallTasks();       //取出所有已经激活状态的任务列表
			if(BlankUtils.isBlank(activeCallTaskList) || activeCallTaskList.size()<=0) {         //如果没有处理激活状态的任务，则不再扫描待重呼记录
				sb.append("由于当前没有已激活且在调度周期内的任务，故不再扫描系统中待重呼的数据到排队机!");
				StringUtil.log(this, sb.toString());
				return;
			}
			
			//遍历已激活的任务，取出任务 ID 列表，并以 taskId1,taskId2,taskId3 的形式拼接成字符串
			StringBuilder idSB = new StringBuilder();
			for(AutoCallTask act:activeCallTaskList) {
				//并非只要处理激活状态的任务，就加入进行查询，而是必须要保证当前任务是否有待重呼的记录，即是要保证该任务的待重呼的记录数量大于0
				String taskId = act.getStr("TASK_ID");   //取出 任务ID
				int retryCountTaskId = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("3", taskId);    //查询该任务待重呼的数量
				if(retryCountTaskId>0) {     //只有该任务待重呼数量大于0时，才加入
					idSB.append("\'" + taskId + "\',");
				}
			}
			String taskIds = idSB.toString();
			if(BlankUtils.isBlank(taskIds)) {
				sb.append("由于在已激活的任务中，没有待重呼数据，故不再扫描待重呼数据到排队机。");
				StringUtil.log(this, sb.toString());
				return;
			}
			taskIds = taskIds.substring(0, taskIds.length()-1);  //去掉最后的逗号
			sb.append("当前处理激活状态的任务有:" + activeCallTaskList.size() + ",这些任务的taskId列表是:" + taskIds + ",系统将从这些任务获取待重呼数据。");
			
			List<AutoCallTaskTelephone> retryList = AutoCallTaskTelephone.dao.loadRetryData(freeQueueCount,taskIds);
			
			if(!BlankUtils.isBlank(retryList) && retryList.size()>0) {
				
				retryCount = retryList.size();
				
				//如果有数据时,加入排队机
				for(AutoCallTaskTelephone autoCallTaskTelephone:retryList) {
					
					AutoCallQueueMachineManager.enQueue(autoCallTaskTelephone);
					
				}
			}
		}
		
		sb.append("实际从这些任务中共扫描到：" + retryCount + " 条待重呼数据到排队机中,现排队机共有：" + AutoCallQueueMachineManager.queueCount + " 条待呼数据!");
		
		StringUtil.log(this, sb.toString());
		
	}

}
