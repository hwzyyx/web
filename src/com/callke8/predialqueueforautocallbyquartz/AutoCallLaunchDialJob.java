package com.callke8.predialqueueforautocallbyquartz;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.QuartzUtils;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TelephoneNumberLocationUtil;
import com.jfinal.plugin.activerecord.Record;

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
					
					boolean b = checkLocation(autoCallTaskTelephone);     //检查外呼记录的号码归属地情况，如果没有定位，则重新定位，并将定位结果存储到数据库中，如果定位失败，返回 false
					if(!b) {
						return;   
					}
					
					//准备执行外呼
					AutoCallPredial.activeChannelCount++;               //活跃通道增加1
					
					// 调用执行外呼的 Job 进行外呼
					Scheduler schedulerForCallOut = QuartzUtils.createScheduler("AutoCallLaunchDialJob" + System.currentTimeMillis(),1);
					JobDetail jobDetail = QuartzUtils.createJobDetail(AutoCallDoCallOutJob.class);
					jobDetail.getJobDataMap().put("autoCallTaskTelephoneId", String.valueOf(autoCallTaskTelephone.getInt("TEL_ID")));    //将ID以参数传入到quartz的执行区
					schedulerForCallOut.scheduleJob(jobDetail, QuartzUtils.createSimpleTrigger(new Date((System.currentTimeMillis() + 1000)), 0, 1));   //执行一次
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
	
	/**
	 * 检查归属地情况，如果归属地已定位，直接返回 true
	 * 
	 * 如果归属地没有定位，则调用相关接口，将归属地和外呼号码赋值，并返回 true
	 * 
	 * 如果定位失败，或是定位到了，但是更改时失败，则返回
	 * 
	 * @param actt
	 * @return
	 */
	public boolean checkLocation(AutoCallTaskTelephone autoCallTaskTelephone) {
		
		int telId = autoCallTaskTelephone.getInt("TEL_ID");				   //取出号码的ID
		
		//先从数据库中取出 telId 对应的最新的记录，因为在将该待呼记录加入排队机后，系统还更改了该记录的几个值：STATE=1（记录状态）,LOAD_TIME=?（外呼时间）,RETRIED=RETRIED+1（已呼次数）,LAST_CALL_RESULT=''
		AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(String.valueOf(telId));
		if(BlankUtils.isBlank(actt)) {    return true; }    //如果记录无法查询出来，很可以该记录已经被删除，直接返回 false 即可
		
		String callOutTel = actt.getStr("CALLOUT_TEL");      //取出记录的外呼号码，如果外呼号码不为空，则表示该记录肯定已经定位了归属地，直接返回 true
		if(!BlankUtils.isBlank(callOutTel)) {                //如果外呼号码不为空，则直接返回 true
			return true; 
		}else {                                              //如果外呼号码为空，则表示该记录没有定位归属地
			
			String taskId = actt.getStr("TASK_ID");                  //取出任务 ID
			String customerTel = actt.getStr("CUSTOMER_TEL");		 //取出客户号码（上传时的号码）
			int retried = actt.getInt("RETRIED");                    //已外呼次数
			
			AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);     //取出任务的信息
			int retryTimes = autoCallTask.getInt("RETRY_TIMES");                              //任务设置的最大外呼次数
			
			Record customerTelLocation = TelephoneNumberLocationUtil.getLocation(customerTel);    //取得号码归属地
			
			if(!BlankUtils.isBlank(customerTelLocation)) {        //如果客户号码归属地不为空,则将归属地信息插入数据库
				String provinceRs = customerTelLocation.getStr("province");                     //省份
				String cityRs = customerTelLocation.getStr("city");								//城市
				String callOutTelRs = customerTelLocation.getStr("callOutTel");					//外呼号码
				boolean isLocalCity = customerTelLocation.getBoolean("isLocalCity");			//是否为本地号码
				boolean isLandlineNumber = customerTelLocation.getBoolean("isLandlineNumber");	//是否为固定电话号码
				
				StringUtil.log(this, "客户号码：" + customerTel + " 的归属地定位信息为:" + customerTelLocation);
				
				int count = AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneLocationAndCallOutTel(telId, provinceRs, cityRs, callOutTelRs);
				if(count < 0) {    //如果更改号码归属地和外呼号码不成功，则将外呼记录的状态修改为 已失败（或待重呼），原因为：更改归属地异常
					if(retried < retryTimes) {      //如果已经外呼次数小于允许的最大外呼次数，则将记录状态修改为待重呼
						AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneStateToRetry(telId, "3", autoCallTask.getInt("RETRY_INTERVAL"), "更改归属地异常");
					}else {                         //否则,修改为已失败
						AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(telId, null, "4", "更改归属地异常");
					}
					return false;
				}
				
				return true;
				
			}else {			//如果客户的归属地无法定位，那么就直接将这条外呼号码的状态，修改为失败（或待重呼），原因定为定位归属地异常
				
				if(retried < retryTimes) {      //如果已经外呼次数小于允许的最大外呼次数，则将记录状态修改为待重呼
					AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneStateToRetry(telId, "3", autoCallTask.getInt("RETRY_INTERVAL"), "定位归属地异常");
				}else {                         //否则,修改为已失败
					AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(telId, null, "4", "定位归属地异常");
				}
				return false;
			}
			
		}
		
	}

}
