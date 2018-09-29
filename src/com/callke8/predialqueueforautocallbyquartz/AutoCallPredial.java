package com.callke8.predialqueueforautocallbyquartz;

import java.util.Date;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.pridialqueueforbshbyquartz.BSHLaunchDialJob;
import com.callke8.pridialqueueforbshbyquartz.BSHLoadOrderListJob;
import com.callke8.pridialqueueforbshbyquartz.BSHLoadRetryJob;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.QuartzUtils;
import com.callke8.utils.StringUtil;

/**
 * 自动外呼任务的调整主守护程序
 * 
 * 定时扫描外呼任务及加载数据到排队机并执行外呼的守护过程
 * 
 * @author 黄文周
 *
 */
public class AutoCallPredial {
	
	public static int activeChannelCount = 0;     //当前的活跃通道
	
	//构造函数
	public AutoCallPredial() {
		
	}
	
	//执行区
	public void exec() throws SchedulerException {
		
		System.out.println(DateFormatUtils.getCurrentDate() + "\t" + this.getClass().getSimpleName() + "，准备开始执行!");
		
		int scanInterval = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_4_scanInterval"));
		long startSeconds = System.currentTimeMillis() + 5 * 1000;       //在当前的毫秒基础上加5秒
		Date startTime = new Date(startSeconds);                         //定义 5 秒后启动守护程序
		
		//执行到这里，表示服务器（tomcat）被重启过，需要将状态为 "已载入"记录，重置为0，执行一次数据回滚
		int rollBackCount = AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(0, "1", "0", null);
		StringUtil.log(this, "服务器(Tomcat)被重启，系统回滚'已载入'号码数据,此次回滚数据量为:" + rollBackCount);
		
		//线程一:扫描任务到排队机线程
		Scheduler scheduler1 = QuartzUtils.createScheduler("AutoCallLoadTaskJob" + System.currentTimeMillis(),1);
		scheduler1.scheduleJob(QuartzUtils.createJobDetail(AutoCallLoadTaskJob.class),QuartzUtils.createTrigger(startTime,scanInterval));
		scheduler1.start();
		
		//线程二:扫描排队机,若排队机中有数据,则执行外呼
		Scheduler scheduler2 = QuartzUtils.createScheduler("AutoCallLaunchDialJob" + System.currentTimeMillis(),1);
		scheduler2.scheduleJob(QuartzUtils.createJobDetail(AutoCallLaunchDialJob.class),QuartzUtils.createTrigger(startTime, 1));    //每秒钟发起一次呼叫
		scheduler2.start();
		
		//线程三:扫描待重呼记录到排队机线程
		Scheduler scheduler3 = QuartzUtils.createScheduler("AutoCallLoadRetryJob" + System.currentTimeMillis(),1);
		scheduler3.scheduleJob(QuartzUtils.createJobDetail(AutoCallLoadRetryJob.class),QuartzUtils.createTrigger(startTime,scanInterval));
		scheduler3.start();
		
		
		
		
	}
	
	/**
	 * 外呼失败时,更改号码的状态
	 * 失败的原因有三：1 pbx的连接不成功;2呼叫失败;3 无人接听
	 * 
	 * @param lastCallResult
	 * 			最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * 
	 */
	public static void updateTelehponeStateForFailure(String lastCallResult,int retried,AutoCallTaskTelephone autoCallTaskTelephone) {
		
		String taskId = autoCallTaskTelephone.getStr("TASK_ID");    //取出任务的ID
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);    //取得任务的信息
		
		int retryTimes = autoCallTask.getInt("RETRY_TIMES");        //任务设置的重呼次数
		int retryInterval = autoCallTask.getInt("RETRY_INTERVAL");  //重呼的时间间隔（单位为：分钟）
		
		if(retried < retryTimes) {   //如果已重试次数小于限定的重试次数时
			//设置当前号码的状态为重试状态
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneStateToRetry(Integer.valueOf(autoCallTaskTelephone.get("TEL_ID").toString()),"3", retryInterval,lastCallResult);
		}else {
			//设置当前号码的状态为失败
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(Integer.valueOf(autoCallTaskTelephone.get("TEL_ID").toString()),null, "4",lastCallResult);	
		}
		
		if(activeChannelCount>0) {   //返回之前,活动的通道数量减1
			activeChannelCount--;
		}
		
	}
	
	
	/**
	 * 如果外呼任务处于非激活状态,外呼数据将放弃外呼并将回滚数据
	 * 
	 * //查看已经重试次数,如果已重试次数大于0,表示外呼任务需要回滚到重试
		//                如果已重试次数小于等于0,表示外呼任务需要回滚到新建状态
	 * 
	 */
	public static void updateTelephoneStateForTaskNotInActive(int retried,AutoCallTaskTelephone autoCallTaskTelephone) {
		
		//查看已经重试次数,如果已重试次数大于0,表示外呼任务需要回滚到重试
		//                如果已重试次数小于等于0,表示外呼任务需要回滚到新建状态
		if(retried > 0) {
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(Integer.valueOf(autoCallTaskTelephone.getStr("TEL_ID")), null, "4", null);
		}else {
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(Integer.valueOf(autoCallTaskTelephone.getStr("TEL_ID")), null, "0", null);
		}
		
		if(activeChannelCount>0) {   //返回之前,活动的通道数量减1
			activeChannelCount--;
		}
		
	}
	
	
	/**
	 * 通话成功时保存
	 * 通话成功时,通话并没有结束,所以活动通道不能减除,而是需要将通道加入内存
	 * 加一个事件监控线程,在挂机事件时，再将其解除
	 * 
	 * @param lastCallResult
	 */
	public static void updateTelehponeStateForSuccess(String lastCallResult, AutoCallTaskTelephone autoCallTaskTelephone) {
		
		AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(Integer.valueOf(autoCallTaskTelephone.get("TEL_ID").toString()),null,"2",lastCallResult);
		
	}

}
