package com.callke8.pridialqueueforbshbyquartz;

import org.asteriskjava.manager.SendActionCallback;
import org.asteriskjava.manager.response.ManagerResponse;

import com.callke8.astutils.AsteriskUtils;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

/**
 * 发送 外呼 Action 的 Callback 实现类
 * @author 黄文周
 *
 */
public class SendActionCallbackForBSH implements SendActionCallback {

	private String channel;
	private int retried;
	private BSHOrderList bshOrderList;
	private AsteriskUtils au;
	
	public SendActionCallbackForBSH(String channel,int retried,BSHOrderList bshOrderList,AsteriskUtils au) {
		this.channel = channel;
		this.retried = retried;
		this.bshOrderList = bshOrderList;
		this.au = au;
	}
	
	@Override
	public void onResponse(ManagerResponse response) {
		
		//在最后，将连接关闭，回收 Asterisk 的连接资源
		au.close();
		
		System.out.println("response: " + response);
		String responseResult = response.getResponse();      //执行外呼Action的结果
		StringUtil.writeString("/data/bsh_exec_log/bsh_callout.log", DateFormatUtils.getCurrentDate() + "\t 系统发送呼叫Action,通道(Channel)：" + channel + ",Response结果:" + responseResult + ",\tResponse详情:" + response + "\r\n", true);
		StringUtil.log(this, "系统发送呼叫Action,通道(Channel)：" + channel + ",Response结果:" + responseResult + ",Response详情:" + response);
		if(responseResult.equalsIgnoreCase("Error")) {       
			//如果执行的结果为 error，则可以提前将结果更改，尽快的释放外呼资源,这样子,外呼失败的记录，
			//在 BSHHandleCallOutRecordResultJob中的操作并不一定会去更改状态了
			BSHPredial.updateBSHOrderListStateForFailure("NOANSWER_RES_ERROR", retried, bshOrderList);
			StringUtil.log(this, "执行外呼 Response结果为:" + responseResult + ",订单:" + bshOrderList + " 外呼失败!");
		}
		
	}

}
