package com.callke8.pridialqueueforbshbyquartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

/**
 * 博世电器: 处理外呼记录结果的 Job
 * 
 * 外呼记录的外呼结果状态我们不在别的线程中更改，对于"已载入"即是已加入排队机的记录,
 * 我们将会载入之后3分钟后，根据客户的回复情况，强制更改外呼记录的外呼状态
 * 
 * （1）只要客户的回复结果不为0，即是表示当前记录外呼成功
 * （2）确定为3分钟的依据：
 *      如何确定时间：播放全部语音（两遍） + 8 秒休息，大概需要1分半钟 ，加上呼叫等待时间30秒，
 *      所以我们暂定为 3分钟强制处理这些记录
 *      如果一个通话得到处理，3分钟之内一定会完成
 * 
 * @author 黄文周
 *
 */
public class BSHHandleCallOutRecordResultJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap map = context.getJobDetail().getJobDataMap();    //取得 Map
		int bshOrderListId = map.getInt("bshOrderListId");          //得到当前外呼记录的 ID
		
		BSHOrderList bshOrderList = BSHOrderList.dao.getBSHOrderListById(String.valueOf(bshOrderListId));   //从数据库中取出该记录
		
		//取出回复的信息，如果回复结果为 1、2、3、4 中的任意一个，都表示当前订单通道已经成功
		//因为表示了客户回复了按键，即使是回复了错误按键，或是无回复按键
		//但是至少表明 BSHCallFlowAgi 流程已经被执行
		int respond = bshOrderList.getInt("RESPOND");     //回复结果
		int retried = bshOrderList.getInt("RETRIED");     //已重试
		int state = bshOrderList.getInt("STATE");         //呼叫状态：0：未处理;1：已载入;2:已成功;3:待重呼;4:已失败;5:已过期;6:放弃呼叫;
				
		if(state==1 && respond == 0) {                           //如果状态仍为1，即是已载入时，才表示条件内记录，同时保证 respond 不为 1,2,3,4
			//执行强制修改呼叫状态，表示为失败
			BSHPredial.updateBSHOrderListStateForFailure("NOANSWER", retried, bshOrderList);
		}else if(state==1 && respond != 0) {                     //如果状态仍为1，但是回复已经修改为非0，即是客户有输入时，表示该通话已经呼叫成功。
			BSHPredial.updateBSHOrderListStateForSuccess("SUCCESS", "1", String.valueOf(respond), bshOrderList);
		}
		
		if(state == 1) {
			StringUtil.writeString("/data/bsh_exec_log/bsh_handle_callresult.log", DateFormatUtils.getCurrentDate() + "\t 系统处理外呼结果：订单Id:" + bshOrderListId + ",客户号码:" + bshOrderList.get("CUSTOMER_TEL") + ",原状态:" + state + ",客户回复：" + respond + "\r\n", true);
			StringUtil.log(this, "线程 BSHLaunchDialJob[99999999]:3分钟后处理外呼记录的外呼状态,订单Id:" + bshOrderListId + ",客户号码:" + bshOrderList.get("CUSTOMER_TEL") + ",原状态:" + state + ",客户回复：" + respond);
		}else {
			StringUtil.writeString("/data/bsh_exec_log/bsh_handle_callresult.log", DateFormatUtils.getCurrentDate() + "\t 系统处理外呼结果：订单Id:" + bshOrderListId + ",客户号码:" + bshOrderList.get("CUSTOMER_TEL") + ",状态已被改变:" + state + ",客户回复：" + respond + "\r\n", true);
			StringUtil.log(this, "线程 BSHLaunchDialJob[99999999]:3分钟后处理外呼记录的外呼状态,订单Id:" + bshOrderListId + ",客户号码:" + bshOrderList.get("CUSTOMER_TEL") + ",状态已被改变:" + state + ",客户回复：" + respond);
		}
		
		
		//执行结束后,及时需要将 schedule 关闭，以释放资源
		try {
			context.getScheduler().shutdown();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
	}
	
}
