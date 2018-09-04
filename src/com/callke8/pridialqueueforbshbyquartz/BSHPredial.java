package com.callke8.pridialqueueforbshbyquartz;

import java.util.Date;
import java.util.Properties;
import java.util.Timer;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.predialqueuforbsh.BSHLaunchDialService;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.plugin.activerecord.Record;

/**
 * 该类为BSH(博世电器)的外呼守护程序
 * 
 * 定义扫描外呼任务
 * 
 * @author 黄文周
 *
 */
public class BSHPredial {
	
	public static StdSchedulerFactory factory;
	public static int activeChannelCount = 0;
	
	public BSHPredial() {
		factory = new StdSchedulerFactory();    //创建 factory 线程工厂
	}
	
	//执行区
	public void exec() throws SchedulerException {
		
		System.out.println(DateFormatUtils.getCurrentDate() + "\t" + this.getClass().getSimpleName() + ",准备开始执行");
		
		long startSeconds = System.currentTimeMillis() + 5 * 1000;
		Date startTime = new Date(startSeconds);                         //定义为5秒后启动
		
		//执行到这里，表示服务器(tomcat)被重启过，需要将状态为“已载入”记录，重置为 0.  执行一次数据回滚
		Scheduler scheduler0 = createScheduler("BSHResetOrderListJob" + System.currentTimeMillis(),1);
		scheduler0.scheduleJob(createJobDetail(BSHResetOrderListJob.class), createSimpleTrigger(startTime, 0, 1));
		scheduler0.start();
		
		
		//线程一:扫描订单信息到排队机线程
		Scheduler scheduler1 = createScheduler("BSHLoadOrderListJob" + System.currentTimeMillis(),1);
		scheduler1.scheduleJob(createJobDetail(BSHLoadOrderListJob.class), createTrigger(startTime,3));
		scheduler1.start();
		
		//线程二:扫描排队机,若排队机中有数据,则执行外呼
		Scheduler scheduler2 = createScheduler("BSHLaunchDialJob" + System.currentTimeMillis(),1);
		scheduler2.scheduleJob(createJobDetail(BSHLaunchDialJob.class), createTrigger(startTime, 1));    //每秒钟发起一次呼叫
		scheduler2.start();
		
		//线程三:处理超时记录的 Job
		//(BSH处理超时记录(即是对于加载到排队机,但是6分钟后,未得到处理的记录)的处理 Job
		//一个通话总时长为3分钟内,如果一个已加入排队机的记录6分钟都没有得到处理，肯定是当前记录是无法获得通道了
		//可以按超时处理,强制将外呼状态修改为失败、或是为待重呼
		Scheduler scheduler3 = createScheduler("BSHHandleTimeOutRecordJob" + System.currentTimeMillis(), 1);
		scheduler3.scheduleJob(createJobDetail(BSHHandleTimeOutRecordJob.class),createTrigger(startTime, 30));    //像这种处理超时任务的记录,不需要那么频繁,30秒执行一次
		scheduler3.start();
		
		//线程四:扫描待重呼记录到排队机线程
		Scheduler scheduler4 = createScheduler("BSHLoadRetryJob" + System.currentTimeMillis(),1);
		scheduler4.scheduleJob(createJobDetail(BSHLoadRetryJob.class), createTrigger(startTime,3));
		scheduler4.start();
		
		//线程五:清理超时订单信息
		//若订单状态为:0(新建),或为:3(待重呼),但是安装日期却小于等是等于当前日期时,系统却强制处理该记录,将状态修改为放弃呼叫!
		String currDate = DateFormatUtils.formatDateTime(new Date(System.currentTimeMillis() + 24 * 3600 * 1000), "yyyy-MM-dd");    //第2天的日期
		String firstDateTime = currDate + " 02:00:00";                                 //凌晨2点钟
		Date firstTime = DateFormatUtils.parseDateTime(firstDateTime, "yyyy-MM-dd HH:mm:ss");    //转回为 Date 对象
		Timer cleanTimeOutTimer = new Timer();
		cleanTimeOutTimer.scheduleAtFixedRate(new BSHCleanTimeOutTask(), firstTime, 24 * 60 * 60 * 1000);
		
	}
	
	/**
	 * 创建 JobDetail
	 * @param jobClass
	 * 			Job 类
	 * @return
	 */
	public static JobDetail createJobDetail(Class<? extends Job> jobClass) {
		
		JobDetail jobDetail = JobBuilder.newJob(jobClass)
				.withDescription("This Class Is: " + jobClass.getName())
				.withIdentity(String.valueOf(System.currentTimeMillis()))
				.build();
		
		return jobDetail;
	}
	
	/**
	 * 创建Scheduler
	 * 
	 * @param schedulerName
	 * @param threadCount
	 * @return
	 */
	public static Scheduler createScheduler(String schedulerName,int threadCount) {
		
		Scheduler scheduler = null;
		
		Properties props = new Properties();
		props.put("org.quartz.scheduler.instanceName", schedulerName);
		props.put("org.quartz.threadPool.threadCount", String.valueOf(threadCount));
		
		try {
			factory.initialize(props);
			scheduler = factory.getScheduler();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
		return scheduler;
		
	}
	
	/**
	 * 创建触发器
	 * 
	 * @param date
	 * 			开始时间
	 * @param interval
	 * 			时间间隔
	 * @return
	 */
	public static Trigger createTrigger(Date triggerStartTime,int interval) {
		
		Trigger trigger = TriggerBuilder.newTrigger()
				.withDescription("")
				.withIdentity(String.valueOf(System.currentTimeMillis()), String.valueOf(System.currentTimeMillis()))
				.startAt(triggerStartTime)
				.withSchedule(CronScheduleBuilder.cronSchedule("0/"+interval + " * * * * ?"))    //几秒执行一次
				.build();
		return trigger;
	}
	
	/**
	 * 创建普通触发器（只执行一次）
	 * 
	 * @param triggerStartTime
	 * @param interval
	 * @return
	 */
	public static SimpleTriggerImpl createSimpleTrigger(Date triggerStartTime,int repeatCount,int interval) {
		
		SimpleTriggerImpl trigger = new SimpleTriggerImpl();
		trigger.setName(String.valueOf(System.currentTimeMillis()));
		trigger.setStartTime(triggerStartTime);
		trigger.setRepeatCount(repeatCount);
		trigger.setRepeatInterval(interval);
		
		return trigger;
	}
	
	/**
	 * 判断记录原状态是否为1，即是（已载入）
	 * 
	 * @param bshOrderList
	 * @return
	 */
	public static boolean checkBshOrderListStateIs1(BSHOrderList bshOrderList) {
		
		boolean b = false;
		
		int id = bshOrderList.getInt("ID");   //取出ID
		
		//从数据库中取出订单记录
		BSHOrderList bol = BSHOrderList.dao.getBSHOrderListById(String.valueOf(id));
		
		//根据取出的 bol 判断是原状态是否仍为 1
		
		if(!BlankUtils.isBlank(bol)) {
			
			int state = bol.getInt("STATE");    //取出状态
			
			if(state == 1) {
				b = true;
			}
			
		}
		
		return b;
	}
	
	/**
	 * 外呼失败时,更改号码的状态
	 * 失败的原因有三：1 pbx的连接不成功;2呼叫失败;3 无人接听
	 * 
	 * @param lastCallResult
	 * 				最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * @param retried
	 * 				已重试次数
	 * @param bshOrderList
	 * 				传入订单信息
	 */
	public static void updateBSHOrderListStateForFailure(String lastCallResult,int retried,BSHOrderList bshOrderList) {
		
		if(retried < BSHCallParamConfig.getRetryTimes()) {      //如果已重试次数小于限定的重试次数时
			
			//设置当前号码的状态为重试状态
			BSHOrderList.dao.updateBSHOrderListStateToRetry(bshOrderList.getInt("ID"), "3", BSHCallParamConfig.getRetryInterval(), lastCallResult);
		}else {
			
			//两次都失败同时，将这个未接听的结果反馈给BSH服务器
			BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), "0", "5");
			Thread httpRequestThread = new Thread(httpRequestT);
			httpRequestThread.start();
			
			//重试次数已经超过或是等于重试次数，直接设置为失败
			BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"),null, "4", lastCallResult);
		}
		
		if(activeChannelCount > 0) {         //返回之前，活动的通道数量减1
			activeChannelCount--;
		}
		
	}
	
	/**
	 * 更改外呼状态为失败,原因是因为超时，即是已载入到排队机6分钟未得到处理的记录
	 * 
	 * @param bshOrderList
	 */
	public static void updateBSHOrderListStateForFailureByTimeOut(Record bshOrderList) {
		
		int retried = bshOrderList.getInt("RETRIED_VALUE");        //已重试
		String lastCallResult = "UNKNOWN";                   //最后的外呼结果
		
		if(retried < BSHCallParamConfig.getRetryTimes()) {      //如果已重试次数小于限定的重试次数时
			//设置当前号码的状态为重试状态
			BSHOrderList.dao.updateBSHOrderListStateToRetry(bshOrderList.getInt("ID"), "3", BSHCallParamConfig.getRetryInterval(), lastCallResult);
		}else {
			
			//两次都失败同时，将这个未接听的结果反馈给BSH服务器
			BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), "0", "5");
			Thread httpRequestThread = new Thread(httpRequestT);
			httpRequestThread.start();
			
			//重试次数已经超过或是等于重试次数，直接设置为失败
			BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"),null, "4", lastCallResult);
		}
		
		//由于超时6分钟未得到处理,固应该确定为无法正式获得外呼通道的，固在这里并不需要将活跃通道减掉1条
	}
	
	/**
	 * 通话成功时保存
	 * 
	 * 通话成功时,通话并没有结束,所以活动通道不能减除,而是需要将通道加入内存
	 * 加一个事件监控线程,在挂机事件时，再将其解除
	 * 
	 *  *  * 参数	说明
		orderId	订单号id
		callType	外呼类型0.二次未接通1.一次接通/二次接通2放弃呼叫3已过期
		time	时间（yyyyMMddHHmmss）
		sign	签名（全小写）= md5(time + orderId+ key)key为约定好的密钥
		callResult	外呼结果 1：确认建单   2 暂不安装  3 短信确认   4 错误或无回复  5 放弃呼叫 6已过期
	 * 
	 * @param id
	 * 				订单对应的ID，用于储存反馈提交反馈json及由服务器返回的处理结果
	 * 
	 * @param orderId
	 * 				订单编号
	 * @param callType
	 * 				外呼类型
	 * @param callResult
	 * 				其实基本就是客户回复的结果，外呼结果, 1：确认建单   2 暂不安装  3 短信确认 4提前预约  5 错误或无回复  6 放弃呼叫 7已过期
	 * 
	 * @param lastCallResult
	 */
	public static void updateBSHOrderListStateForSuccess(String lastCallResult,String callType,String callResult,BSHOrderList bshOrderList) {
		//强制修改呼叫结果为2，即是已成功
		BSHOrderList.dao.updateBSHOrderListState(bshOrderList.getInt("ID"), null,"2", lastCallResult);
		//同时，将呼叫成功结果反馈给 BSH 服务器
		BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), callType, callResult);
		Thread httpRequestThread = new Thread(httpRequestT);
		httpRequestThread.start();
		
		if(activeChannelCount > 0) {         //返回之前，活动的通道数量减1
			activeChannelCount--;
		}
	}
	
	
	
}
