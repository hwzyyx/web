package com.callke8.pridialqueueforbshbyquartz;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

/**
 * BSH处理超时记录(即是对于加载到排队机,但是6分钟后,未得到处理的记录)的处理 Job
 * 
 * 一个通话总时长为3分钟内,如果一个已加入排队机的记录6分钟都没有得到处理，肯定是当前记录是无法获得通道了
 * 
 * 可以按超时处理,强制将外呼状态修改为失败、或是为待重呼
 * 
 * @author 黄文周
 */
public class BSHHandleTimeOutRecordJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		String befault6MinuteDateTime = DateFormatUtils.getBeforeSecondDateTime(360);    //取得 300 秒之前的时间字符串，格式为 yyyy-MM-dd HH:mm:ss
		
		List<Record> list = BSHOrderList.dao.getBSHOrderListByCondition(null, null, null, null, null, null, "1", null, null,null,null, null, befault6MinuteDateTime);
		if(!BlankUtils.isBlank(list) && list.size()>0) {     //如果查询出来的订单列表数据不为空，即是有未处理的状态为1（已载入）但是载入时间已超过6分钟的记录
			StringUtil.log(this, "线程 BSHLaunchDialJob[33333333]：处理状态为1，但是已经超时（6分钟）的记录,此次取出 " + list.size() + " 条数据进行处理!");
			
			for(Record bshOrderList:list) {                             //取出订单信息进行分钟
				int id = bshOrderList.getInt("ID");
				String orderId = bshOrderList.get("ORDER_ID");
				String customerName = bshOrderList.get("CUSTOMER_NAME");
				String customerTel = bshOrderList.get("CUSTOMER_TEL");
				String createTime = bshOrderList.getDate("CREATE_TIME").toString();
				String loadTime = bshOrderList.getDate("LOAD_TIME").toString();
				
				//System.out.println("bshOrderList: " + bshOrderList);
				
				int retried = bshOrderList.getInt("RETRIED_VALUE");
				
				StringUtil.log(this, "数据处理：id: " + id + ",订单ID：" + orderId + ",客户姓名: " + customerName + ", 客户号码: " + customerTel + ", 重试次数:" + retried + ",createTime: " + createTime + ",loadTime: " + loadTime + "属超时数据,需要强制处理!");
				
				StringUtil.writeString("/data/bsh_exec_log/bsh_timeout_record.log",DateFormatUtils.getCurrentDate() + "\t" + "数据处理：id: " + id + ",订单ID：" + orderId + ",客户姓名: " + customerName + ", 客户号码: " + customerTel + ", 重试次数:" + retried + ",createTime: " + createTime + ",loadTime: " + loadTime + "属超时数据,需要强制处理!\r\n", true);
				
				//更改记录的状态值,但是由于这类记录一般应该不会分配到活跃通道的，所以不必要将活跃通道减1
				BSHPredial.updateBSHOrderListStateForFailureByTimeOut(bshOrderList);
			}
			
		}else {
			StringUtil.log(this, "线程 BSHLaunchDialJob[33333333]：处理状态为1，但是已经超时（6分钟）的记录, 此次没有需要处理的数据!");
		}
		
	}

}
