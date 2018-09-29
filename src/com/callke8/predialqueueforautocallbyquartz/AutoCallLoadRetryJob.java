package com.callke8.predialqueueforautocallbyquartz;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
		
		if(queueCount < queueMaxCount) {      //如果排队机中的数量已经大于或是等于允许的最大值，暂不加载数据到排队机
			
			List<AutoCallTaskTelephone> retryList = AutoCallTaskTelephone.dao.loadRetryData(scanCount);
			
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
			StringUtil.log(this, "===系统已载入 " + retryCount + " 条重试外呼数据到排队机!==");
		}
		
	}

}
