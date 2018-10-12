package com.callke8.predialqueueforautocallbyquartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.NumberUtils;
import com.callke8.utils.SendMessageUtils;
import com.callke8.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动外呼下发短信的 JOB
 * 
 * @author 黄文周
 *
 */
public class AutoCallSendMessageJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap map = context.getJobDetail().getJobDataMap();
		String telId = map.getString("autoCallTaskTelephoneId");     //取出号码的 id
		
		//(1)取出号码信息
		AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(telId);     
		if(BlankUtils.isBlank(actt)) {      return; }        //若号码信息为空，则直接返回退出
		String customerTel = actt.getStr("CUSTOMER_TEL");    //取出客户号码
		boolean b = NumberUtils.isCellPhone(customerTel);    //判断是否为手机号码
		if(b) {			//手机号码时，处理一下是否有前缀0
			boolean b2 = customerTel.startsWith("0");
			if(b2) {  customerTel = customerTel.substring(1, customerTel.length()); }    //有前缀0时，去掉前缀0 
		}else {         //非手机号码时，直接返回   
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneMessageState(3,"101", Integer.valueOf(telId));   //放弃下发短信
			return;   
		}  
		
		//(2)取出任务信息
		String taskId = actt.getStr("TASK_ID");     				//取出任务 ID
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);    //取得任务
		if(BlankUtils.isBlank(autoCallTask)) {  return; }           //如果任务也为空，则直接返回退出
		String taskType = autoCallTask.getStr("TASK_TYPE");                  //取出任务类型，  1：普通外呼（通知类），2：调查问卷， 3：催缴类外呼
		int sendMessage = autoCallTask.getInt("SEND_MESSAGE");               //是否下发短信,     0:不下发短信；  1：下发短信
		String messageContent = autoCallTask.getStr("MESSAGE_CONTENT");		 //配置的短信内容
		
		//System.out.println("taskType:" + taskType + ",sendMessage:" + sendMessage + ",messageContent:" + messageContent);
		
		//如果任务配置不下发短信、配置的短信内容为空、或是任务类型为调查问卷时，系统将不下发短信
		if(sendMessage != 1  || BlankUtils.isBlank(messageContent) || taskType.equals("2")) {
			StringUtil.log(this, "客户号码：" + customerTel + " 在下发短信中，因为 sendMessage != 1  || BlankUtils.isBlank(messageContent) || taskType.equals(2) 的原因将不下发短信!");
			return;
		}
		
		//(3)组织短信内容
		if(taskType.equalsIgnoreCase("1")) {         //如果外呼任务的类型为通知类外呼，那么只需要将任务配置的短信内容直接下发即可
			Record record = SendMessageUtils.sendMessage(messageContent, customerTel);
			if(!BlankUtils.isBlank(record)) {        //有返回
				String status = record.get("returnstatus");
				String message = record.get("message");
				
				if(BlankUtils.isBlank(status) || !status.equalsIgnoreCase("Success")) {
					AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneMessageState(2,message,Integer.valueOf(telId));
				}else {                           //发送成功
					AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneMessageState(1,"0",Integer.valueOf(telId));
				}
				
			}else {									 //无返回
				AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneMessageState(2, "102", Integer.valueOf(telId));      //请求错误
			}
			
		}
			
		
		
		
	}

}
