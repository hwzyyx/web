package com.callke8.predialqueueforautocallbyquartz;

import org.asteriskjava.manager.SendActionCallback;
import org.asteriskjava.manager.response.ManagerResponse;

import com.callke8.astutils.AsteriskUtils;
import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

public class SendActionCallbackForAutoCall implements SendActionCallback {
	
	private String channel;
	private AutoCallTaskTelephone actt;
	private AutoCallTask autoCallTask;
	private AsteriskUtils au;

	public SendActionCallbackForAutoCall(String channel,AutoCallTaskTelephone actt,AsteriskUtils au) {
		this.channel = channel;
		this.actt = actt;
		this.autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(actt.getStr("TASK_ID"));
		this.au = au;
	}
	
	@Override
	public void onResponse(ManagerResponse response) {
		
		//在最后，将连接关闭，回收 Asterisk 的连接资源
		au.close();
		
		System.out.println("response: " + response);
		String responseResult = response.getResponse();      //执行外呼Action的结果
		StringUtil.writeString("/data/autocall_exec_log/autocall_callout.log", DateFormatUtils.getCurrentDate() + "\t 系统发送呼叫Action,通道(Channel)：" + channel + ",Response结果:" + responseResult + ",\tResponse详情:" + response + "\r\n", true);
		StringUtil.log(this, "系统发送呼叫Action,通道(Channel)：" + channel + ",Response结果:" + responseResult + ",Response详情:" + response);
		if(responseResult.equalsIgnoreCase("Error")) {       
			//如果执行的结果为 error，则可以提前将结果更改，尽快的释放外呼资源,这样子,外呼失败的记录，
			//在 BSHHandleCallOutRecordResultJob中的操作并不一定会去更改状态了
			AutoCallPredial.updateTelehponeStateForFailure("未接或请求通道失败", actt, autoCallTask);
			StringUtil.log(this, "执行外呼 Response结果为:" + responseResult + ",外呼信息:" + actt + " 外呼失败!");
			
			//如果外呼失败，则应该马上释放外呼资源
			AutoCallPredial.activeChannelCount--;
		}
		
	}

}
