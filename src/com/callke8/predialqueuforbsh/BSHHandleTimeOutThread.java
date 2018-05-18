package com.callke8.predialqueuforbsh;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;

/**
 * BSH超时订单处理线程
 * 
 * BSH每天限定 09:00-20:00 为有效的呼叫时间
 * 
 * 当超过20:00 - 23：59：59 之间时,如果状态仍为：0（新建）、3（待重呼）时，而安装日期是小于或是等于当前日期时，将状态修改为放弃外呼
 * 
 * 该程序在 20:00后至23:59:59 之间运行
 * 
 * @author 黄文周
 *
 */
public class BSHHandleTimeOutThread implements Runnable {

	private Log log = LogFactory.getLog(BSHHandleTimeOutThread.class);
	
	public BSHHandleTimeOutThread() {
		
	}
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(5 * 1000);   //为了等待环境变量加载完毕,先休眠5秒
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		int i = 1;
		
		while(true) {
			
			//判断当前时间与系统生效时间对比，如果系统处于 20：00至 23：59：59时
			int compareResult = BSHCallParamConfig.compareCurrTime2ActiveTime();
			
			if(compareResult == 3) {      //即是时间牌： 20：00 至 23：59:59 之间时
				log.info("BSH处理 TIMEOUT 数据的线程准备执行");
				
				//处理超时间数据
				List<BSHOrderList> timeOutOrderList = BSHOrderList.dao.handleTimeOutOrderList();
				
				int timeOutOrderListCount = 0;
				
				if(!BlankUtils.isBlank(timeOutOrderList)) {
					
					timeOutOrderListCount = timeOutOrderList.size();
					
					for(BSHOrderList bshOrderList:timeOutOrderList) {    //遍历，用于将结果反馈给BSH服务器
						
						//对于超时的记录，将结果反馈给BSH服务器
						BSHHttpRequestThread httpRequestT = new BSHHttpRequestThread(bshOrderList.get("ID").toString(),bshOrderList.getStr("ORDER_ID"), "2", "5");
						Thread httpRequestThread = new Thread(httpRequestT);
						httpRequestThread.start();
						
					}
				}
				
				log.info("BSH 处理 TIMEOUT 数据线程 第 " + i + " 次,此处处理 " + timeOutOrderListCount + " 条超时数据!");
				
			}else {
				log.info("BSH处理 TIMEOUT 数据的线程暂时未到执行时间，因限定当前的活动时间为" + BSHCallParamConfig.getActiveStartTime() + " 至 " + BSHCallParamConfig.getActiveEndTime());
			}
			
			
			i++;
			if(i>10) {
				i = 1;
			}
			
			//休息3秒钟
			try {
				Thread.sleep(BSHCallParamConfig.getScanInterval() * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
	}

}
