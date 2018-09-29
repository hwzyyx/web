package com.callke8.predialqueueforautocallbyquartz;

import org.asteriskjava.manager.SendActionCallback;
import org.asteriskjava.manager.response.ManagerResponse;

import com.callke8.astutils.AsteriskUtils;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

public class SendActionCallbackForAutoCall implements SendActionCallback {
	
	private String channel;
	private int retried;
	private AutoCallTaskTelephone autoCallTaskTelephone;
	private AsteriskUtils au;

	public SendActionCallbackForAutoCall(String channel,int retried,AutoCallTaskTelephone autoCallTaskTelephone,AsteriskUtils au) {
		this.channel = channel;
		this.retried = retried;
		this.autoCallTaskTelephone = autoCallTaskTelephone;
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
			AutoCallPredial.updateTelehponeStateForFailure("NOANSWER_RES_ERROR", retried, autoCallTaskTelephone);
			StringUtil.log(this, "执行外呼 Response结果为:" + responseResult + ",外呼信息:" + autoCallTaskTelephone + " 外呼失败!");
		}
		
	}

}
