package com.callke8.pridialqueueforbshbyquartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.utils.StringUtil;

/**
 * 重置订单状态
 * 
 * 即是系统重启后，将原来已载入（即是状态为1）的记录，重置为0
 * 
 * @author 黄文周
 *
 */
public class BSHResetOrderListJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		int count = BSHOrderList.dao.updateBSHOrderListState(0, "1", "0", null);
		
		StringUtil.log(this, "Tomcat 被重启过，系统回滚'已载入'的号码数据: " + count + "条!");
	}

}
