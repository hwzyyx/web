package com.callke8.pridialqueueforbshbyquartz;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.StringUtil;

/**
 * 扫描外呼记录到排队机
 * 
 * @author 黄文周
 *
 */
public class BSHLoadRetryJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		//判断当前时间与系统生效时间对比结果，系统时间处于配置的：09:00 ~ 20:00 之间时,才执行守护程序
		int compareResult = BSHCallParamConfig.compareCurrTime2ActiveTime();
		
		if(compareResult == 2) {   // 1表示 00:00 至 09:00; 2 表示  09:00 至 20:00;  3 表示 20:00 至 24:00
			
			int queueCount = BSHQueueMachineManager.queueCount;				//先查看排队机中的数量
			int queueMaxCount = BSHCallParamConfig.getQueueMaxCount();      //排队机允许最大的排队数量
			int scanCount = BSHCallParamConfig.getScanCount();              //单次扫描的数量
			
			if(queueCount < queueMaxCount) {      //如果排队机中的数量已经大于或是等于允许的最大值，暂不加载数据到排队机
				
				int freeCount = queueMaxCount - queueCount;       //查看排队机中数量与允许最大量的差距
				if(freeCount<scanCount) {                         //如果空闲的数量小于单次扫描数量
					scanCount = freeCount;
				}
				
				//扫描一定数量的待重呼数据到队列中
				List<BSHOrderList> list = BSHOrderList.dao.loadOrderListRetryToQueue(scanCount);
				
				int BSHOrderListRetryCount = 0;
				
				if(!BlankUtils.isBlank(list) && list.size() >0) {
					BSHOrderListRetryCount = list.size();						//取出号码的数量
					
					//将取出的号码，加入排队机
					for(BSHOrderList orderList:list) {
						BSHQueueMachineManager.enQueue(orderList);
					}
				}
				
				StringUtil.log(this, "线程 BSHLoadRetryJob[444444444] : 扫描并加载订单信息到排队机，此次扫描 " + BSHOrderListRetryCount + " 条数据加入排队机! 排队机中未外呼数量：" + BSHQueueMachineManager.queueCount);
				
			}else {
				StringUtil.log(this, "线程 BSHLoadRetryJob[444444444] : 扫描并加载订单信息到排队机, 排队机中未外呼记录数大于设定的允许最大值 " + queueMaxCount + ",此次跳过扫描!");
			}
			
		}else {
			StringUtil.log(this, "线程 BSHLoadRetryJob[444444444] : 处于非生效时间,系统设定系统的生效时间为 :" + BSHCallParamConfig.getActiveStartTime() + " 至   " + BSHCallParamConfig.getActiveEndTime() );
		}
		
	}

}
