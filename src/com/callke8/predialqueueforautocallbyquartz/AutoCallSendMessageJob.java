package com.callke8.predialqueueforautocallbyquartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.autocall.flow.AutoFlow;
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
			try {
				context.getScheduler().shutdown();
				return;   
			} catch (SchedulerException e) {
				e.printStackTrace();
			}  
		}  
		
		//(2)取出任务信息
		String taskId = actt.getStr("TASK_ID");     				//取出任务 ID
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId,null);    //取得任务
		if(BlankUtils.isBlank(autoCallTask)) {    //如果任务也为空，则直接返回退出
			try {
				context.getScheduler().shutdown();
				return;   
			} catch (SchedulerException e) {
				e.printStackTrace();
			}  
		}          
		
		String taskType = autoCallTask.getStr("TASK_TYPE");                  //取出任务类型，  1：普通外呼（通知类），2：调查问卷， 3：催缴类外呼
		String reminderType = autoCallTask.getStr("REMINDER_TYPE");          //催缴类型：1  电话费、 2 电费、3 水费、4 燃气费、5 物业费、6 车辆违章、 7社保催缴
		int sendMessage = autoCallTask.getInt("SEND_MESSAGE");               //是否下发短信,     0:不下发短信；  1：下发短信
		String messageContent = autoCallTask.getStr("MESSAGE_CONTENT");		 //配置的短信内容
		
		//System.out.println("taskType:" + taskType + ",sendMessage:" + sendMessage + ",messageContent:" + messageContent);
		
		//如果任务配置不下发短信、配置的短信内容为空、或是任务类型为调查问卷时，系统将不下发短信
		if(sendMessage != 1  || BlankUtils.isBlank(messageContent) || taskType.equals("2")) {
			StringUtil.log(this, "客户号码：" + customerTel + " 在下发短信中，因为 sendMessage != 1  || BlankUtils.isBlank(messageContent) || taskType.equals(2) 的原因将不下发短信!");
			try {
				context.getScheduler().shutdown();
				return;   
			} catch (SchedulerException e) {
				e.printStackTrace();
			}  
		}
		
		//(3)组织短信内容
		String msgContent = null;
		if(taskType.equalsIgnoreCase("1")) {         //如果外呼任务的类型为通知类外呼，那么只需要将任务配置的短信内容直接下发即可
			msgContent = messageContent;
		}else if(taskType.equalsIgnoreCase("3")) {   //如果外呼任务的类型为催缴类外呼，需要再判断催缴类型
			msgContent = getContent(actt, reminderType);
		}
		System.out.println("---@@@@@8*********----============---------------:" + msgContent);
		Record record = SendMessageUtils.sendMessage(msgContent, customerTel);
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
			
		try {
			context.getScheduler().shutdown();
			return;   
		} catch (SchedulerException e) {
			e.printStackTrace();
		}  
	}
	
	/**
	 * 根据号码信息，取出下发催缴类型的信息的内容
	 * 
	 * @param actt
	 * @param reminderType
	 * @return
	 */
	public String getContent(AutoCallTaskTelephone actt,String reminderType) {
		if(BlankUtils.isBlank(actt) || BlankUtils.isBlank(reminderType)) {
			return null;
		}
		AutoFlow autoFlow = AutoFlow.dao.getAutoFlowByReminderType(reminderType);
		
		if(!BlankUtils.isBlank(autoFlow)) {
			
			String flowRule = autoFlow.getStr("FLOW_RULE");    //取出规则
			String content = null;
			
			String address = actt.getStr("ADDRESS");
			String accountNumber = actt.getStr("ACCOUNT_NUMBER");
			String charge = actt.getStr("CHARGE");
			String displayNumber = actt.getStr("DISPLAY_NUMBER");
			String dosage = actt.getStr("DOSAGE");
			String plateNumber = actt.getStr("PLATE_NUMBER");
			String vehicleType = actt.getStr("VEHICLE_TYPE");
			String period = actt.getStr("PERIOD");
			String year = null;
			String month = null;
			if(!BlankUtils.isBlank(period)) {
				year = period.substring(0,4);
				month = period.substring(4,6);
			}
			
			if(reminderType.equals("1")) {     //电费催缴时
				//常州供电公司友情提醒：您户地址%s，总户号%s于%s发生电费%s元，请按时缴纳，逾期缴纳将产生滞纳金。详情可关注“国网江苏电力”公众微信号或下载掌上电力app。如您本次收到的用电地址有误，可在工作时间致电83272222。若已缴费请忽略本次提醒。
				content = String.format(flowRule, address,accountNumber,year + "年" + month + "月",charge);
			}else if(reminderType.equals("2")) {    //水费催缴
				//尊敬的自来水用户您好，下面为您播报本期水费对账单。您水表所在地址%s于%s抄见数为%s，月用水量为%s吨，水费为%s元。特此提醒。详情可凭用户号%s登录常州通用自来水公司网站或致电常水热线：88130008查询。
				content = String.format(flowRule, address,year + "年" + month + "月",displayNumber,dosage,charge,accountNumber);
			}else if(reminderType.equals("3")) {    //电话费催缴
				//尊敬的客户您好，你%s的电话费为%s元。
				content = String.format(flowRule,year + "年" + month + "月",charge);
			}else if(reminderType.equals("4")) {    //燃气费催缴
				//尊敬的客户您好，你%s的燃气费为%s元。
				content = String.format(flowRule,year + "年" + month + "月",charge);
			}else if(reminderType.equals("5")) {    //物业费催缴
				//尊敬的客户您好，你%s的物业费为%s元。
				content = String.format(flowRule,year + "年" + month + "月",charge);
			}else if(reminderType.equals("6")) {    //交通违章
				//您的%s汽车于%s违反了相关的交通条例，请收到本告知之日起30日内接受处理。
				content = String.format(flowRule,plateNumber,year + "年" + month + "月");
			}else if(reminderType.equals("7")) {    //交警移车
				//您好，这是常州公安微警务051981990110挪车服务专线，您是%s车主吗？你的%s占用他人车位，请按任意键接听车位业主电话。
				content = String.format(flowRule,plateNumber,vehicleType);
			}else if(reminderType.equals("8")) {    //交警移车
				//尊敬的客户您好，你%s的社保费为%s元。
				content = String.format(flowRule,year + "年" + month + "月",charge);
			}
			
			return content;
			
		}else {
			return null;
		}
		
	}

}
