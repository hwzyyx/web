package com.callke8.predialqueueforautocallbyquartz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.callke8.astutils.AsteriskUtils;
import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TelephoneLocationUtils;
import com.callke8.utils.TelephoneNumberLocationUtil;

public class AutoCallDoCallOutJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap map = context.getJobDetail().getJobDataMap();
		String autoCallTaskTelephoneId = map.getString("autoCallTaskTelephoneId");     //取出号码的 id
		AutoCallTaskTelephone autoCallTaskTelephone = null;
		AutoCallTask autoCallTask = null;
		int retried = 0;           //已经重试次数
		
		//从数据库中取出要外呼的记录
		autoCallTaskTelephone = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(autoCallTaskTelephoneId);
		if(BlankUtils.isBlank(autoCallTaskTelephone)) {    //如果待外呼在数据不存在记录，即跳出外呼
			AutoCallPredial.activeChannelCount--;     //释放资源
			return;    
		}else {
			retried = autoCallTaskTelephone.getInt("RETRIED");                      //赋值当前任务重试的次数
			String taskId = autoCallTaskTelephone.get("TASK_ID");    				//取出任务的ID
			autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);        //从数据库中取出任务的信息
			if(BlankUtils.isBlank(autoCallTask)) {     //如果从数据库取不出来，直接返回，不执行外呼
				AutoCallPredial.activeChannelCount--;     //释放资源
				return;
			}
			
		}
		
		StringUtil.log(this, "线程 AutoCallDoCallOutJob[333333]: 外呼 Job 接到并准备一个外呼任务, ID 值：" + autoCallTaskTelephoneId + ",任务详情:" + autoCallTaskTelephone);
		
		// 第一步：检查外呼任务状态
		// 执行外呼操作之前,先判断当前外呼数据所在的外呼任务的状态,是否处于暂停状态
		//取出外呼任务的状态
		String taskState = autoCallTask.get("TASK_STATE");
		//只要外呼任务状态不为2,即是已激活时,将回滚外呼号码
		if(!taskState.equals("2")) { 
			StringUtil.log(this, "由于外呼任务：" + autoCallTask.get("TASK_NAME") + " 非激活状态,外呼数据 :" + autoCallTaskTelephone.get("TELEPHONE") + "将放弃外呼,并将回滚数据!");
			//回滚数据
			AutoCallPredial.updateTelephoneStateForTaskNotInActive(retried, autoCallTaskTelephone);
			AutoCallPredial.activeChannelCount--;     //释放资源
			return;
		}
		
		// 第二步：检查Asterisk 的连接状态
		//判断 Asterisk 的连接状态
		AsteriskUtils au = new AsteriskUtils();      //创建一个 连接工具
		boolean connState = au.isAstConnSuccess();    //连接是否成功
		StringUtil.log(this, "系统准备执行呼叫,Asterisk服务器的连接状态为:" + connState);
		
		if(!connState) {        //如果连接状态有问题,则暂不做外呼,强制当前的conn再执行一次连接
			StringUtil.log(this, "PBX系统连接状态有异常....");
			//如果失败，将再一次连接，如果连接，然后再去判断是否连接成功
			try {
				au.doLogin();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (AuthenticationFailedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
			
			connState = au.isAstConnSuccess();    //再检查一次
			if(!connState) {    //如果还是连接失败，将直接保存其为失败状态
				au.close();
				AutoCallPredial.updateTelehponeStateForFailure("DISCONNECTION", retried, autoCallTaskTelephone);    //更改状态为失败或是重试，并指定最后失败原因为 未连接
				StringUtil.log(this, "再次重连接Asterisk后，PBX系统连接状态仍旧有异常,系统将直接更改状态为失败或是重试!");
				
				try {
					AutoCallPredial.activeChannelCount--;     //释放资源
					context.getScheduler().shutdown(); 
					return;
				} catch (SchedulerException e) {
					e.printStackTrace();
				}
			}
		}
		
		//第三步：做外呼的准备工作
		String trunkInfo = ParamConfig.paramConfigMap.get("paramType_4_trunkInfo");
		String agiUrl = ParamConfig.paramConfigMap.get("paramType_4_agiUrl");
		String telephone = autoCallTaskTelephone.get("TELEPHONE");     		//取出客户的号码
		boolean isLocalNumber = TelephoneNumberLocationUtil.isLocalNumber(telephone, "常州", "0519");
		
		String numberPrefix = ParamConfig.paramConfigMap.get("paramType_4_numberPrefix");   //增加前缀
		
		String callOutTel = numberPrefix + telephone;
		if(!isLocalNumber) {
			callOutTel = numberPrefix + "0" + telephone;
		}
		//获取主叫号码
		String callerIdInfo = autoCallTask.get("CALLERID");   //主叫的ID信息
		String callerIdNumber = MemoryVariableUtil.getDictName("CALLERID", callerIdInfo);
		
		
		
		//准备拼接外呼参数
		String channel = trunkInfo + "/" + callOutTel;
		String application = "AGI";
		String applicationData = agiUrl;
		long timeout = 30 * 1000L;
		String callerId = callerIdNumber;
		Map<String,String> virablesMap = new HashMap<String,String>();   //设置通道变量
		virablesMap.put("autoCallTaskTelephoneId", String.valueOf(autoCallTaskTelephoneId));
				
		
		
		//创建外呼 Action
		OriginateAction action = new OriginateAction();
		action.setChannel(channel);
		action.setApplication(application);
		action.setData(applicationData);
		action.setCallerId(callerId);
		action.setTimeout(timeout);
		action.setVariable("autoCallTaskTelephoneId",autoCallTaskTelephoneId);
		
		
		//执行发送外呼Action
		ManagerConnection conn = au.getMangerConnection();
		try {
			//发送外呼 Action 动作
			conn.sendAction(action,new SendActionCallbackForAutoCall(channel, retried, autoCallTaskTelephone,au));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//执行到最后，主动关闭该 job,以释放资源
			try {
				context.getScheduler().shutdown(); 
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
		
	}
	

}
