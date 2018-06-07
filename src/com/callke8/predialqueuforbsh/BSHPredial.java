package com.callke8.predialqueuforbsh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.test.CallOutDemo;

public class BSHPredial {
	
	private Log log = LogFactory.getLog(BSHPredial.class);
	
	public BSHPredial() {
		
	}
	
	public void exec() {
		
		log.info("PredialQueueForBSH 准备开始执行");
		
		//检查连接池的连接状态
		System.out.println("BSHPridial......准备开始执行。。。。");
		CallOutDemo cod = new CallOutDemo();
		cod.doCallOut();
		System.out.println("BSHPridial......执行结束。。。。");
		/*
		//线程一：扫描订单信息到排队机线程
		Thread loadOrderListThread = new Thread(new BSHLoadOrderListThread());
		loadOrderListThread.start();
		
		//线程二：执行外呼线程，从内存的排队机中取出呼叫数据，并执行外呼
		BSHLaunchDialThread bshLaunchDialT = new BSHLaunchDialThread();
		Thread bshLaunchDialThread = new Thread(bshLaunchDialT);
		bshLaunchDialThread.start();
		
		//线程三：扫描待重呼数据,并载入排队机，将状态由”重试[3]“ 修改为 已载入[1]
		BSHLoadRetryThread bshLoadRetryT = new BSHLoadRetryThread();
		Thread bshLoadRetryThread = new Thread(bshLoadRetryT);
		bshLoadRetryThread.start();
		
		//线程四：挂机监控线程
		BSHHangUpMonitor bshHangUpMonitor = new BSHHangUpMonitor();
		Thread bshHangUpMonitorThread = new Thread(bshHangUpMonitor);
		bshHangUpMonitorThread.start();
		
		//线程五：系统一般的外呼时间为早上09:00至晚上20:00,当超过20:00后,如果状态仍为：0（新建）、3（待重呼）时，则将其状态修改为6放弃呼叫
		BSHHandleTimeOutThread bshHandleTimeOutT = new BSHHandleTimeOutThread();
		Thread bshHandleTimeOutThread = new Thread(bshHandleTimeOutT);
		bshHandleTimeOutThread.start();
		*/
		
	}
	
}
