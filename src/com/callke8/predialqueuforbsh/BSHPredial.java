package com.callke8.predialqueuforbsh;

import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

public class BSHPredial {
	
	//private Log log = LogFactory.getLog(BSHPredial.class);
	
	public BSHPredial() {
		
	}
	
	public void exec() {
		
		System.out.println(DateFormatUtils.getCurrentDate() + "\t" + this.getClass().getSimpleName() + ",准备开始执行");
		StringUtil.log(this, "准备开始执行...");
		
		//检查连接池的连接状态
		
		
		//在执行各线程之前，先将主线程休眠5秒钟，目的是为了等待系统参数加载完毕
		try {  Thread.sleep(5 * 1000);  } catch (InterruptedException e) { e.printStackTrace(); }
		
		
		//线程一：扫描订单信息到排队机线程
		/*Thread loadOrderListThread = new Thread(new BSHLoadOrderListThread());
		loadOrderListThread.start();*/
		Timer timer = new Timer();
		timer.schedule(new BSHLoadOrderListTimerTask(), 1 * 1000, 3 * 1000);
		
		
		//线程二：执行外呼线程，从内存的排队机中取出呼叫数据，并执行外呼
		BSHLaunchDialThread bshLaunchDialT = new BSHLaunchDialThread();
		Thread bshLaunchDialThread = new Thread(bshLaunchDialT);
		bshLaunchDialThread.start();
		/*Timer bshLaunchDialTimer = new Timer();
		bshLaunchDialTimer.schedule(new BSHLaunchDialTimerTask(), 5 * 1000, 1 * 1000);*/
		
		//线程三：扫描待重呼数据,并载入排队机，将状态由”重试[3]“ 修改为 已载入[1]
		/*BSHLoadRetryThread bshLoadRetryT = new BSHLoadRetryThread();
		Thread bshLoadRetryThread = new Thread(bshLoadRetryT);
		bshLoadRetryThread.start();*/
		Timer bshLoadRetryTimer = new Timer();
		bshLoadRetryTimer.schedule(new BSHLoadRetryTimerTask(), 5 * 1000, 3 * 1000);
		
		//线程四：挂机监控线程
		BSHHangUpMonitor bshHangUpMonitor = new BSHHangUpMonitor();
		Thread bshHangUpMonitorThread = new Thread(bshHangUpMonitor);
		bshHangUpMonitorThread.start();
		
		//线程五：系统一般的外呼时间为早上09:00至晚上20:00,当超过20:00后,如果状态仍为：0（新建）、3（待重呼）时，则将其状态修改为6放弃呼叫
		/*BSHHandleTimeOutThread bshHandleTimeOutT = new BSHHandleTimeOutThread();
		Thread bshHandleTimeOutThread = new Thread(bshHandleTimeOutT);
		bshHandleTimeOutThread.start();*/
		Timer bshHandleTimeOutTimer = new Timer();
		bshHandleTimeOutTimer.schedule(new BSHHandleTimeOutTimerTask(), 5 * 1000, 3 * 1000);
		
		//线程六：处理状态为1（已载入），但是已经超时的记录(5分钟)
		Timer bshHandleState1Timer = new Timer();
		bshHandleState1Timer.schedule(new BSHHandleState1TimerTask(), 5 * 1000, 10 * 1000);
		
	}
	
}
