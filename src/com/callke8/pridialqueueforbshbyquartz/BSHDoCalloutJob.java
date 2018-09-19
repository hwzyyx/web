package com.callke8.pridialqueueforbshbyquartz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.live.CallerId;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.SendActionCallback;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.callke8.astutils.AsteriskConnectionPool;
import com.callke8.astutils.AsteriskUtils;
import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

/**
 * 执行外呼操作的  Job 
 *
 *	@author 黄文周
 *
 */
public class BSHDoCalloutJob implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap map = context.getJobDetail().getJobDataMap();
		int bshOrderListId = map.getInt("bshOrderListId");
		
		BSHOrderList bshOrderList = BSHOrderList.dao.getBSHOrderListById(String.valueOf(bshOrderListId));
		
		StringUtil.log(this, "线程 BSHDoCalloutJob[33333333] : 外呼Job接到一个外呼任务,ID值:" + bshOrderListId + ",任务详情:" + bshOrderList);
		
		String trunkInfo = ParamConfig.paramConfigMap.get("paramType_3_trunkInfo");
		String agiUrl = ParamConfig.paramConfigMap.get("paramType_3_agiUrl");
		String callerNumber = ParamConfig.paramConfigMap.get("paramType_3_callerNumber");
		
		//准备拼接外呼参数
		String channel = trunkInfo + "/" + bshOrderList.get("CALLOUT_TEL");
		String application = "AGI";
		String applicationData = agiUrl;
		long timeout = 30 * 1000L;
		String callerId = callerNumber;
		Map<String,String> virablesMap = new HashMap<String,String>();   //设置通道变量
		virablesMap.put("bshOrderListId", String.valueOf(bshOrderListId));
		
		int retried = bshOrderList.getInt("RETRIED");
		
		//创建外呼 Action
		OriginateAction action = new OriginateAction();
		action.setChannel(channel);
		action.setApplication(application);
		action.setData(applicationData);
		action.setCallerId(callerId);
		action.setTimeout(timeout);
		action.setVariable("bshOrderListId",String.valueOf(bshOrderListId));
		
		//判断连接状态
		AsteriskUtils au = new AsteriskUtils();    		//创建一个 Asterisk工具
		boolean connState = au.isAstConnSuccess();      //取出连接是否成功
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
				BSHPredial.updateBSHOrderListStateForFailure("DISCONNECTION", retried, bshOrderList);     //更改状态为失败或是重试，并指定最后失败原因为 未连接
				StringUtil.log(this, "再次重连接Asterisk后，PBX系统连接状态仍旧有异常,系统将直接更改状态为失败或是重试!");

				try {
					context.getScheduler().shutdown(); 
				} catch (SchedulerException e) {
					e.printStackTrace();
				}
			}
		}
		
		//执行发送外呼Action
		ManagerConnection conn = au.getMangerConnection();
		try {
			//发送外呼 Action 动作
			conn.sendAction(action,new SendActionCallbackForBSH(channel, retried, bshOrderList,au));
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
