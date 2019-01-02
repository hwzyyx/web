package com.callke8.predialqueueforautocallbyquartz;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 处理超时外呼记录
 * 
 * 即是处理状态为1，即是已载入，但是时间已经超过 8 分钟的记录，未得到正确处理的记录。
 * 
 * 
 * 一个通话总时长为一分钟左右，如果再加上一个重复收听，大概在3分钟左右。我们在这个时间翻倍的情况，再延长一定的时间，暂定8分钟
 * 
 * 一个通话从加入排队机，8分钟未处理，即表示需要强制处理，将其设置为待重呼，或是已失败
 * 
 * @author 黄文周
 *
 */
public class AutoCallHandleTimeOutRecordJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		String before8MinuteDateTime = DateFormatUtils.getBeforeSecondDateTime(480);     //480秒前的时间（即是8分钟前的时间），格式为: yyyy-MM-dd HH:mm:ss
		//System.out.println("8分钟前的时间为：" + before8MinuteDateTime);
		List<Record> list = AutoCallTaskTelephone.dao.getAutoCallTaskTelephonesByTaskIdAndState(null,"1",null,null,null,null,null,null, null,before8MinuteDateTime,0);
		if(!BlankUtils.isBlank(list) && list.size()>0) {    
			StringUtil.log(this, "处理超时数据线程AutoCallHandleTimeOutRecordJob:处理状态为1,但已超过（8分钟）的记录，此次取出 " + list.size() + " 条数据进行处理!" );
			for(Record record:list) {
				String taskId = record.getStr("TASK_ID");
				int telId = record.getInt("TEL_ID");    //
				String customerTel = record.getStr("CUSTOMER_TEL");
				String customerName = record.getStr("CUSTOMER_NAME");
				String createTime = record.getDate("CREATE_TIME").toString();
				String loadTime = record.getDate("LOAD_TIME").toString();
				int retried = record.getInt("RETRIED");
				
				AutoCallTask autoTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);   //任务
				AutoCallTaskTelephone actt = new AutoCallTaskTelephone();
				actt.set("TEL_ID", telId);
				actt.set("TASK_ID", taskId);
				actt.set("CUSTOMER_TEL", customerTel);
				actt.set("CUSTOMER_NAME", customerName);
				actt.set("RETRIED", retried);
				
				StringUtil.log(this, "数据处理，telId:" + telId + ",taskId:" + taskId + ",customerName:" + customerName + ",customerTel:" + customerTel + ",createTime:" + createTime + ",loadTime:" + loadTime + ",retried:" + retried + " 属超时数据,需要强制处理!");
				StringUtil.writeString("/data/autocall_timeout_data.log", DateFormatUtils.getCurrentDate() + "\t" + "数据处理，telId:" + telId + ",taskId:" + taskId + ",customerName:" + customerName + ",customerTel:" + customerTel + ",createTime:" + createTime + ",loadTime:" + loadTime + ",retried:" + retried + " 属超时数据,需要强制处理!\r\n", true);
				
				//在更改状态前，需要将活跃通道减掉一个
				if(AutoCallPredial.activeChannelCount > 0) {
					AutoCallPredial.activeChannelCount--;
				}
				
				//更改记录的状态，需要将记录设置为 待重呼或是已失败
				//AutoCallPredial.updateTelehponeStateForFailure("4","TIME_OUT", actt, autoTask);
				AutoCallPredial.updateTelehponeStateForFailure("4","402", actt, autoTask);    //更改状态为失败或是重试，并指定最后失败原因为 超时数据
				
			}
			
		}else {
			StringUtil.log(this, "处理超时数据线程AutoCallHandleTimeOutRecordJob:没有超时数据需要处理,系统将跳过此次处理..." );
		}
		
	}

}
