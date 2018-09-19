package com.callke8.pridialqueueforbshbyquartz;

import java.util.Date;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

/**
 * 扫描排队机，如果排队机有数据，则执行外呼 Job
 * 
 * @author 黄文周
 *
 */
public class BSHLaunchDialJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		int activeChannelCount = BSHPredial.activeChannelCount;       			//当前活动的通道数量
		
		int trunkMaxCapacity = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_3_trunkMaxCapacity"));		//中继的最大并发量
		
		if(BSHQueueMachineManager.queueCount > 0) {     //如果排队机中有未外呼的号码时
			
			//先判断中继最大并发量与当前活跃通话量对比,如果最大并发量大于当前活跃通话量时，表示还有空闲通道可用
			if(trunkMaxCapacity > activeChannelCount) {
				
				StringUtil.log(this, "线程 BSHLaunchDialJob[22222222] : 排队机中有未外呼数据:" + BSHQueueMachineManager.queueCount + " 条,系统将取出一条数据执行外呼!");
				
				BSHOrderList bshOrderList = BSHQueueMachineManager.deQueue();
				//在执行外呼之前，还需要增加一个判断，判断当前记录的外科状态是否已经被修改了（即是数据库中，是否还保持为1（已载入）状态）
				//为什么要加入这一判断：主要是因为对于状态为1（已载入）状态的数据，如果5分钟还没有得到空闲通道时，守护程序 BSHHandleState1TimerTask.java 
				//会强制将状态修改为3（待重呼）或是4（已失败）。如果此时仍执行外呼时，会造成数据不统一。固需要加一个判断，判断记录状态是否仍为1（已载入）
				boolean b = BSHPredial.checkBshOrderListStateIs1(bshOrderList);
				
				if(b) {      //如果订单外呼状态仍为1，执行外呼
					
					try {
						
						//同时将活跃通道增加一个,表示有一个新外呼已在执行
						BSHPredial.activeChannelCount++;               //活跃通道增加 1
						
						Date startTime = new Date();
						//(1)调用执行外呼的 Job 进行外呼
						Scheduler schedulerForCallOut = BSHPredial.createScheduler("BSHDoCalloutJob" + System.currentTimeMillis(),1);
						JobDetail jobDetail = BSHPredial.createJobDetail(BSHDoCalloutJob.class);
						jobDetail.getJobDataMap().put("bshOrderListId", bshOrderList.getInt("ID"));    //将ID以参数传入到quartz的执行区
						schedulerForCallOut.scheduleJob(jobDetail, BSHPredial.createSimpleTrigger(startTime, 0, 1));   //执行一次
						schedulerForCallOut.start();
						
						//(2)同时再增加一个  Job, 用于 3分钟后,强制处理这条记录
						/*long timeAfter180sec = System.currentTimeMillis() + 180000;   //180 后的时间
						Date startTimeAfter180sec = new Date(timeAfter180sec);                  //3分钟为开始执行的时间
						Scheduler schedulerForHandleResult = BSHPredial.createScheduler("BSHHandleCallOutRecordResultJob" + System.currentTimeMillis(),1);
						JobDetail jdForHandleResult = BSHPredial.createJobDetail(BSHHandleCallOutRecordResultJob.class);
						jdForHandleResult.getJobDataMap().put("bshOrderListId", bshOrderList.getInt("ID"));
						schedulerForHandleResult.scheduleJob(jdForHandleResult, BSHPredial.createSimpleTrigger(startTimeAfter180sec, 0, 1));
						schedulerForHandleResult.start();*/  //由于系统能正常处理这些状态值的变化,所以这个 Job 就没有必要定时处理这些状态值了。
						
					} catch (SchedulerException e) {
						e.printStackTrace();
					}
					
					
				}else {		 //如果订单外呼状态被修改了，则不执行外呼，并记录进日志
					StringUtil.log(this,"线程 BSHLaunchDialJob[22222222] : 数据处理：id: " + bshOrderList.getInt("ID") + ",订单ID：" + bshOrderList.get("ORDER_ID") + ",客户姓名: " + bshOrderList.get("CUSTOMER_NAME") + ", 客户号码: " + bshOrderList.get("CUSTOMER_TEL") + ",createTime: " + bshOrderList.getDate("CREATE_TIME").toString() + " 放弃执行外呼，属超时数据,已被强制处理!" );
					StringUtil.writeString("/data/bsh_exec_log/giveup_call_record.log", DateFormatUtils.getCurrentDate() + "\t 数据处理：id: " + bshOrderList.getInt("ID") + ",订单ID：" + bshOrderList.get("ORDER_ID") + ",客户姓名: " + bshOrderList.get("CUSTOMER_NAME") + ", 客户号码: " + bshOrderList.get("CUSTOMER_TEL") + ",createTime: " + bshOrderList.getDate("CREATE_TIME").toString() + " 放弃执行外呼，属超时数据,已被强制处理!\r\n", true);
				}
				
			}else {
				StringUtil.log(this, "线程 BSHLaunchDialJob[22222222] : 排队机中有未外呼数据:" + BSHQueueMachineManager.queueCount + " 条，但当前活跃通道已达到最大并发量：" + trunkMaxCapacity + "，系统暂不执行外呼!");
			}
			
		}else {
			StringUtil.log(this, "线程 BSHLaunchDialJob[22222222] : 当前排队机中没有未外呼数据,暂不执行外呼!");
		}
		
	}
	
	

}
