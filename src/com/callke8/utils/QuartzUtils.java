package com.callke8.utils;

import java.util.Date;
import java.util.Properties;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.SimpleTriggerImpl;

/**
 * 
 * 开源作业调度框架(quartz)的工具类
 * 
 * 主要是用于快速创建 JobDetail、Scheduler 及 触发器的静态工具方法
 * 
 * @author 黄文周
 *
 */
public class QuartzUtils {
	
	public static StdSchedulerFactory factory = new StdSchedulerFactory();    //创建 factory 线程工厂
	
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
	
}
