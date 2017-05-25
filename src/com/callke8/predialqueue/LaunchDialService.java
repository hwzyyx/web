package com.callke8.predialqueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;

import com.callke8.astutils.AstMonitor;
import com.callke8.astutils.CtiUtils;
import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;

/**
 * 执行外呼服务
 * 
 * @author hwz
 *
 */
public class LaunchDialService implements Runnable {

	AutoCallTaskTelephone autoCallTaskTelephone;
	
	String channel;   			    //外呼通道
	String application = "AGI";     //执行动作
	String data;					//动作参数
	long timeout;					//超时时间
	CallerId callerId;				//主叫号码
	int retried;               		//当前号码已重试的次数
	int retryTimes;               	//任务配置的重试次数
	int retryInterval;              //重试间隔
	Map<String,String> variables = new HashMap<String,String>();
	AutoCallTask autoCallTask = null;
	private Log log = LogFactory.getLog(LaunchDialService.class);
	public static Integer activeChannelCount = 0;      //活动的通道数量,要控制外呼的并发量,主要是由这个变量值与最大并发量做比较
	public static List<String> activeChannelList = new ArrayList<String>();   //储存接通的通道,用于在挂机时,做减除用
	
	public LaunchDialService(AutoCallTaskTelephone autoCallTaskTelephone) {
	
		this.autoCallTaskTelephone = autoCallTaskTelephone;
		String taskId = autoCallTaskTelephone.get("TASK_ID");   //取出外呼任务ID
		String telephone = autoCallTaskTelephone.get("TELEPHONE");
		String telId = autoCallTaskTelephone.get("TEL_ID").toString();
		variables.put("telId",telId);
		variables.put("taskId",taskId);
		
		//执行到这里时,将执行外呼时间更新
		AutoCallTaskTelephone.dao.setCallOutOperatorTime(Integer.valueOf(telId));
		
		//已重试次数
		retried = Integer.valueOf(autoCallTaskTelephone.get("RETRIED").toString());
		//超时时间
		timeout = Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("ac_timeout")) * 1000;
		
		//取出任务信息
		autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		String callerIdInfo = autoCallTask.get("CALLERID");   //主叫的ID信息
		String callerIdNumber = MemoryVariableUtil.getDictName("CALLERID", callerIdInfo);
		
		//生成主叫
		callerId = new CallerId(callerIdNumber,callerIdNumber);
		
		//任务的重试次数、重试间隔
		retryTimes = Integer.valueOf(autoCallTask.get("RETRY_TIMES").toString());
		retryInterval = Integer.valueOf(autoCallTask.get("RETRY_INTERVAL").toString());
		
		channel = MemoryVariableUtil.autoCallTaskMap.get("ac_channelPrefix") + "/0" + telephone;
		log.info("外呼通道:" + channel + "------========");
		data = MemoryVariableUtil.autoCallTaskMap.get("ac_agiUrl");
		
		activeChannelCount++;   //每做一次呼叫,活动的通道变量增加一个
		
		//执行一次,外呼时间要更新一次
		
	}

	@Override
	public void run() {
		
		//第一步：检查外呼任务状态
		//执行外呼操作之前,先判断当前外呼数据所在的外呼任务的状态,是否处于暂停状态
		if(!BlankUtils.isBlank(autoCallTask)) {
			
			//取出外呼任务的状态
			String taskState = autoCallTask.get("TASK_STATE");
			
			//只要外呼任务状态不为2,即是已激活时,将回滚外呼号码
			if(!taskState.equals("2")) { 
				log.info("由于外呼任务：" + autoCallTask.get("TASK_NAME") + " 非激活状态,外呼数据 :" + autoCallTaskTelephone.get("TELEPHONE") + "将放弃外呼,并将回滚数据!");
				//回滚数据
				updateTelephoneStateForTaskNotInActive();
				return;
			}
		}
		
		//第二步：检查PBX连接状态
		//1 执行外呼之前,先判断 asterisk 的连接状态
		boolean connState = CtiUtils.getConnectionState();
		
		log.info("连接状态为:" + connState);
		
		//连接状态
		if(!connState) {   //如果连接状态有问题,则不做外呼,直接更改号码的状态      
			
			log.info("PBX系统未接通........................");
			updateTelehponeStateForFailure("DISCONNECTION");      //更改状态,并指定为未连接
			
			return;
		}
		
		
		//第三步：执行外呼
		final DefaultAsteriskServer server = new DefaultAsteriskServer(AstMonitor.getAstHost(),AstMonitor.getAstPort(),AstMonitor.getAstUser(),AstMonitor.getAstPass());
		server.originateToApplicationAsync(channel, application, data, timeout, callerId, variables, new OriginateCallback() {
			
			@Override
			public void onDialing(AsteriskChannel channel) {
				
				log.info("onDialing(准备执行呼叫) ......channel:" + channel.getName() + ",channel:" + channel);
				
			}
			
			@Override
			public void onNoAnswer(AsteriskChannel channel) {
				
				updateTelehponeStateForFailure("NOANSWER");      //更改状态
				log.info("onNoAnswer ......");
				server.shutdown();
				return;
			}
			
			@Override
			public void onFailure(LiveException liveexception) {
				
				updateTelehponeStateForFailure("FAILURE");      //更改状态
				log.info("onFailure ......");
				server.shutdown();
				return;
			}
			
			
			@Override
			public void onBusy(AsteriskChannel channel) {
				
				updateTelehponeStateForFailure("BUSY");      //更改状态
				log.info("onBusy ......");
				server.shutdown();
				return;
			}
			
			@Override
			public void onSuccess(AsteriskChannel channel) {
				
				/**
				 * 通话成功时,通话并没有结束,所以活动通道不能减除,而是需要将通道加入内存
				 * 加一个事件监控线程,在挂机事件时，再将其解除
				 */
				activeChannelList.add(channel.getName());   //将通道名加入活动通道列表
				
				updateTelehponeStateForSuccess("SUCCESS");      //更改状态
				
				log.info("onSuccess,通道" + channel.getName() + " 被加入活动通道列表");
				server.shutdown();
				return;
			}
		});
		
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 外呼失败时,更改号码的状态
	 * 失败的原因有三：1 pbx的连接不成功;2呼叫失败;3 无人接听
	 * 
	 * @param lastCallResult
	 * 			最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * 
	 */
	public void updateTelehponeStateForFailure(String lastCallResult) {
		if(retried < retryTimes) {   //如果已重试次数小于限定的重试次数时
			//设置当前号码的状态为重试状态
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneStateToRetry(Integer.valueOf(autoCallTaskTelephone.get("TEL_ID").toString()),"3", retryInterval,lastCallResult);
		}else {
			//设置当前号码的状态为失败
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(Integer.valueOf(autoCallTaskTelephone.get("TEL_ID").toString()),null, "4",lastCallResult);	
		}
		
		if(activeChannelCount>0) {   //返回之前,活动的通道数量减1
			activeChannelCount--;
		}
		
	}
	
	/**
	 * 如果外呼任务处于非激活状态,外呼数据将放弃外呼并将回滚数据
	 * 
	 * //查看已经重试次数,如果已重试次数大于0,表示外呼任务需要回滚到重试
		//                如果已重试次数小于等于0,表示外呼任务需要回滚到新建状态
	 * 
	 */
	public void updateTelephoneStateForTaskNotInActive() {
		
		//查看已经重试次数,如果已重试次数大于0,表示外呼任务需要回滚到重试
		//                如果已重试次数小于等于0,表示外呼任务需要回滚到新建状态
		if(retried > 0) {
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(Integer.valueOf(autoCallTaskTelephone.getStr("TEL_ID")), null, "4", null);
		}else {
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(Integer.valueOf(autoCallTaskTelephone.getStr("TEL_ID")), null, "0", null);
		}
		
		if(activeChannelCount>0) {   //返回之前,活动的通道数量减1
			activeChannelCount--;
		}
		
	}
	
	/**
	 * 通话成功时保存
	 * 通话成功时,通话并没有结束,所以活动通道不能减除,而是需要将通道加入内存
	 * 加一个事件监控线程,在挂机事件时，再将其解除
	 * 
	 * @param lastCallResult
	 */
	public void updateTelehponeStateForSuccess(String lastCallResult) {
		
		AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(Integer.valueOf(autoCallTaskTelephone.get("TEL_ID").toString()),null,"2",lastCallResult);
		
	}
	
}
