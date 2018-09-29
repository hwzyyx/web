package com.callke8.predialqueueforautocallbyquartz;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.QuartzUtils;
import com.callke8.utils.StringUtil;

public class AutoCallLaunchDialJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		int activeChannelCount = AutoCallPredial.activeChannelCount;                                                 //当前活跃的通道数量，即有几路通话正在进行
		int trunkMaxCapacity = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_4_trunkMaxCapacity"));      //中继的最大并发量
		
		if(AutoCallQueueMachineManager.queueCount > 0) {                   //如果排队机中有示外呼任务时，将执行外呼操作
			
			//先判断中继最大的并发量与当前活动通话量对比，如果最大并发量大于当前活跃的通话量时，表示还有空闲的通道可用
			if(trunkMaxCapacity > activeChannelCount) {
				
				StringUtil.log(this, "线程 AutoCallLaunchDialJob[22222222] : 排队机中有未外呼数据:" + AutoCallQueueMachineManager.queueCount + " 条,系统将取出一条数据执行外呼!");
				
				AutoCallTaskTelephone autoCallTaskTelephone = AutoCallQueueMachineManager.deQueue();   //从排队机中取出数据，准备外呼
				
				try {
					//准备执行外呼
					AutoCallPredial.activeChannelCount++;               //活跃通道增加1
					
					Date startTime = new Date();
					// 调用执行外呼的 Job 进行外呼
					Scheduler schedulerForCallOut = QuartzUtils.createScheduler("AutoCallLaunchDialJob" + System.currentTimeMillis(),1);
					JobDetail jobDetail = QuartzUtils.createJobDetail(AutoCallDoCallOutJob.class);
					jobDetail.getJobDataMap().put("autoCallTaskTelephoneId", String.valueOf(autoCallTaskTelephone.getInt("TEL_ID")));    //将ID以参数传入到quartz的执行区
					schedulerForCallOut.scheduleJob(jobDetail, QuartzUtils.createSimpleTrigger(startTime, 0, 1));   //执行一次
					schedulerForCallOut.start();
					
				}catch (SchedulerException e) {
					e.printStackTrace();
				}
				
				
			}else {
				StringUtil.log(this, "线程 AutoCallLaunchDialJob[22222222] : 排队机中有未外呼数据:" + AutoCallQueueMachineManager.queueCount + " 条，但当前活跃通道已达到最大并发量：" + trunkMaxCapacity + "，系统暂不执行外呼!");
			}
			
			
		}else {
			StringUtil.log(this, "线程 AutoCallLaunchDialJob(22222): 当前排队机中没有未外呼数据，暂不执行外呼操作!");
		}
		
		
	}

}
