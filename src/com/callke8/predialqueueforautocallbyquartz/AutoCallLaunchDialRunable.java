package com.callke8.predialqueueforautocallbyquartz;

import java.util.Date;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.callke8.utils.QuartzUtils;

/**
 * 
 * 从排队机扫描数据的 Runable 线程，在主线程启动时，以线程的 Runable 循环启动 Job ，每间隔500毫秒去排队机查询
 * 
 * 替换原来直接在主线程定时启动 AutoCallLaunchDialJob ，每间隔500毫秒执行一次 Job
 * 
 * 在这个 runable 中，每隔500毫秒执行一次 job ,  这样，即使执行一次 job 时长无论是多少，都不会影响下次 job 的执行
 * 
 * @author 黄文周
 *
 */
public class AutoCallLaunchDialRunable implements Runnable {

	@Override
	public void run() {
		
		while(true) {         //每 500 毫秒执行一次 job
			
			try {
				String scheduleName = "AutoCallLaunchDialJob" + String.valueOf(System.currentTimeMillis()) + String.valueOf(Math.round(Math.random()*90000 + 10000));
				Scheduler scheduler = QuartzUtils.createScheduler(scheduleName,1);
				scheduler.scheduleJob(QuartzUtils.createJobDetail(AutoCallLaunchDialJob.class),QuartzUtils.createSimpleTrigger(new Date(System.currentTimeMillis() + 10),0,1));    //10毫秒后执行一次呼叫
				scheduler.start();
			
			}catch(SchedulerException se) {
				se.printStackTrace();
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}

}
