package com.callke8.pridialqueueforbshbyquartz;

import java.util.List;
import java.util.TimerTask;

import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

/**
 * 强制清理超时订单数据的 TASK
 * 
 * BSH每天限定 09:00-20:00 为有效的呼叫时间
 * 
 * 如果状态仍为：0（新建）、3（待重呼）时，而安装日期是小于或是等于当前日期时，将状态修改为放弃外呼
 * 
 * 该程序在每天凌晨2点钟执行一次,每隔24小时执行一次
 * 
 * @author 黄文周
 */
public class BSHCleanTimeOutTask extends TimerTask {

	public BSHCleanTimeOutTask() {
		
	}
	
	@Override
	public void run() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("BSH处理 TIMEOUT 数据的线程准备执行!");
		sb.append("若订单状态为:0(新建),或为:3(待重呼),但是安装日期却小于等是等于当前日期时,系统却强制处理该记录,将状态修改为放弃呼叫!");
		
		//处理超时数据
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
		
		sb.append("此次系统共处理 " + timeOutOrderListCount + " 条超时数据!");
		
		StringUtil.log(this, sb.toString());
		StringUtil.writeString("/data/bsh_exec_log/clean_timeout.log", DateFormatUtils.getCurrentDate() + "\t" + sb.toString() + "\r\n", true);
	}
	
}
