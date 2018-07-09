package com.callke8.predialqueuforbsh;

import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.StringUtil;

/**
 * BSH 超时订单处理线程
 * 
 * BSH 每天限定 09:00 - 20:00 为有效的呼叫时间
 * 
 * 当超过 20:00 - 23:59:59 之间时,如果状态为： 0(新建)、3(待重呼)时,而安装日期是小于或是等于当前日期时,将状态修改为放弃呼叫
 * 
 * 该程序在 20:00 后至 23:59:59 之间运行
 * 
 * @author 黄文周
 */
public class BSHHandleTimeOutTimerTask extends TimerTask {

	//private Log log = LogFactory.getLog(BSHHandleTimeOutTimerTask.class);
	private int i = 1;
	
	public BSHHandleTimeOutTimerTask() {
		StringUtil.log(this, "线程BSHHandleTimeOutTimerTask： 准备执行 ...");
	}
	
	@Override
	public void run() {
		
		//判断当前时间与系统生效时间对比，如果系统处于 20：00至 23：59：59时
		int compareResult = BSHCallParamConfig.compareCurrTime2ActiveTime();
		
		if(compareResult == 3) {      //即是时间牌： 20：00 至 23：59:59 之间时
			
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
			
			StringUtil.log(this, "线程BSHHandleTimeOutTimerTask 第  " + i + " 次执行，此次处理 " + timeOutOrderListCount + " 条超时数据!(超时数据：指的是结束时间 至 23:59:59 状态为： 0(新建)、3(待重呼)的数据!");
			
		}else {
			StringUtil.log(this, "线程BSHHandleTimeOutTimerTask 未到执行时间,执行时间为 : " + BSHCallParamConfig.getActiveEndTime() + " 至  " + " 23:59:59 ");
		}
		
		i++;
		if(i > 10) {
			i = 1;
		}
		
	}

}
