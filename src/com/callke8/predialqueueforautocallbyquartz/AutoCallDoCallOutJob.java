package com.callke8.predialqueueforautocallbyquartz;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.callke8.astutils.AsteriskConfig;
import com.callke8.astutils.AsteriskUtils;
import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.system.callerid.SysCallerId;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.NumberUtils;
import com.callke8.utils.QuartzUtils;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TelephoneLocationUtils;
import com.callke8.utils.TelephoneNumberLocationUtil;

public class AutoCallDoCallOutJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap map = context.getJobDetail().getJobDataMap();
		String autoCallTaskTelephoneId = map.getString("autoCallTaskTelephoneId");     //取出号码的 id
		
		AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(autoCallTaskTelephoneId);
		if(BlankUtils.isBlank(actt)) { 		 //如果查询出来的记录为空，则表示该记录已经被删除
			if(AutoCallPredial.activeChannelCount > 0){
				AutoCallPredial.activeChannelCount--;     //释放资源
			}  
			return; 
		}			
		
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(actt.getStr("TASK_ID"));    //取出任务信息
		if(BlankUtils.isBlank(autoCallTask)) { 	//如果任务不存在了，也直接返回，不执行外呼	
			if(AutoCallPredial.activeChannelCount > 0){
				AutoCallPredial.activeChannelCount--;     //释放资源
			}  
			return;  
		}    
		
		
		try {
			StringUtil.log(this, "线程 AutoCallDoCallOutJob[" + context.getScheduler().getSchedulerName() + "]: 外呼 Job 接到并准备一个外呼任务, ID 值：" + autoCallTaskTelephoneId + ",任务详情:" + actt);
		} catch (SchedulerException e2) {
			e2.printStackTrace();
		}
		
		// 第一步：检查外呼任务状态,先判断当前外呼数据所在的外呼任务的状态,是否处于暂停状态,取出外呼任务的状态
		String taskState = autoCallTask.get("TASK_STATE");
		//只要外呼任务状态不为2,即是已激活时,将回滚外呼号码
		if(!taskState.equals("2")) { 
			StringUtil.log(this, "由于外呼任务：" + autoCallTask.get("TASK_NAME") + " 非激活状态,外呼数据 :" + actt.get("TELEPHONE") + "将放弃外呼,并将回滚数据!");
			//回滚数据
			int count = AutoCallTaskTelephone.dao.rollBackAutoCallTaskTelephoneWhenTaskNoActive(Integer.valueOf(autoCallTaskTelephoneId));
			
			if(AutoCallPredial.activeChannelCount > 0){
				AutoCallPredial.activeChannelCount--;     //释放资源
			}
			return;
		}
		
		// 第二步，判断上是否需要下发短信
		int sendMessage = autoCallTask.getInt("SEND_MESSAGE");          //该任务是否配置需要下发短信：0表示 不下发短信；1表示要下发短信
		if(sendMessage == 1) {       //表示可能要下发短信
			int messageState = actt.getInt("MESSAGE_STATE");         //当前记录的下发短信状态结果：    0表示 暂未下发（新建号码时）；1表示发送成功；2表示发送失败；3表示放弃发送(如非手机号码)
			if(messageState == 0 || messageState == 2) {    //只有当短信状态为 0 或 2 时，才执行下发短信
				//然后再判断客户号码是否为手机号码，只有是手机号码时，才执行短信下发
				String customerTel = actt.getStr("CUSTOMER_TEL");
				boolean isCellPhone = NumberUtils.isCellPhone(customerTel);    //是否为手机号码
				if(isCellPhone) {      
					
					//执行下发短信操作
					//调用下发短信的 Job 
					try {
						Scheduler schedulerForSendMessage = QuartzUtils.createScheduler("AutoCallSendMessageJob" + System.currentTimeMillis(),1);
						JobDetail jobDetail = QuartzUtils.createJobDetail(AutoCallSendMessageJob.class);
						jobDetail.getJobDataMap().put("autoCallTaskTelephoneId", String.valueOf(actt.getInt("TEL_ID")));    								 //将ID以参数传入到quartz的执行区
						schedulerForSendMessage.scheduleJob(jobDetail, QuartzUtils.createSimpleTrigger(new Date((System.currentTimeMillis() + 1000)), 0, 1));    //一秒后，执行一次
						schedulerForSendMessage.start();
					} catch (SchedulerException e) {
						e.printStackTrace();
					}   
					
				}else {                //若不是手机号码，则直接将该记录的短信状态，设置为放弃下发短信
					AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneMessageState(3,"101", actt.getInt("TEL_ID"));
				}
			}
		}
		
		// 第三步：检查Asterisk 的连接状态
		//判断 Asterisk 的连接状态
		AsteriskUtils au = new AsteriskUtils();      //创建一个 连接工具
		boolean connState = au.isAstConnSuccess();    //连接是否成功
		StringUtil.log(this, "系统准备执行呼叫,Asterisk服务器的连接状态为:" + connState);
		
		if(!connState) {        //如果连接状态有问题,则暂不做外呼,强制当前的conn再执行一次连接
			StringUtil.log(this, "PBX系统连接状态有异常....");
			//如果失败，将再一次连接，如果连接，然后再去判断是否连接成功
			try {
				au.doLogin();
			} catch(Exception e) {
				e.printStackTrace();
				try {
					if(AutoCallPredial.activeChannelCount > 0){
						AutoCallPredial.activeChannelCount--;     //释放资源
					}
					context.getScheduler().shutdown();
					//return;
				} catch (SchedulerException e1) {
					e1.printStackTrace();
				}
			}
		
			/*catch (IllegalStateException e) {
				context.getScheduler().shutdown();
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (AuthenticationFailedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}*/
			
			connState = au.isAstConnSuccess();    //再检查一次
			if(!connState) {    //如果还是连接失败，将直接保存其为失败状态
				au.close();
				AutoCallPredial.updateTelehponeStateForFailure("4","DISCONNECTION", actt, autoCallTask);    //更改状态为失败或是重试，并指定最后失败原因为 未连接
				StringUtil.log(this, "再次重连接Asterisk后，PBX系统连接状态仍旧有异常,系统将直接更改状态为失败或是重试!");
				
				try {
				  	if(AutoCallPredial.activeChannelCount > 0){
						AutoCallPredial.activeChannelCount--;     //释放资源
					}
					context.getScheduler().shutdown(); 
					return;
				} catch (SchedulerException e) {
					e.printStackTrace();
				}
			}
		}
		
		//第三步：做外呼的准备工作
		String trunkInfo = ParamConfig.paramConfigMap.get("paramType_4_trunkInfo");      	//外呼中继信息
		String agiUrl = ParamConfig.paramConfigMap.get("paramType_4_agiUrl");            	//外呼后转到的 AGI 信息
		String callOutTel = actt.get("CALLOUT_TEL");                                    	//取出客户号码的外呼号码（外呼号码与客户号码有些不一样，做一些处理的，比如，外地号码前缀要加一个0）
		
		String numberPrefix = ParamConfig.paramConfigMap.get("paramType_4_numberPrefix");   //增加前缀
		callOutTel = numberPrefix + callOutTel;                                             //将前缀增加到这个号码
		
		//获取主叫号码
		String callerIdNumber = actt.getStr("CALLERID");             //在从排队机扫描数据外呼的归属地时，已经通过轮循的方式将主叫号码设置到了记录中，直接取出即可
		//String callerIdInfo = autoCallTask.get("CALLERID");   								//主叫的ID信息
		//SysCallerId sysCallerId = SysCallerId.dao.getSysCallerIdById(Integer.valueOf(callerIdInfo));
		//if(!BlankUtils.isBlank(sysCallerId)) {
		//	callerIdNumber = sysCallerId.getStr("CALLERID");
		//}
		
		//准备拼接外呼参数
		String channel = trunkInfo + "/" + callOutTel;
		String application = "AGI";
		String applicationData = agiUrl;
		long timeout = 60 * 1000L;
		//String callerId = callerIdNumber;
		CallerId callerId = new CallerId(callerIdNumber, callerIdNumber);
		Map<String,String> virablesMap = new HashMap<String,String>();   //设置通道变量
		virablesMap.put("autoCallTaskTelephoneId", String.valueOf(autoCallTaskTelephoneId));
				
		au.doCallOutToApplication(channel, application, applicationData, timeout, callerId, virablesMap,new MyOriginateCallback(actt, au));
	}
	

}
