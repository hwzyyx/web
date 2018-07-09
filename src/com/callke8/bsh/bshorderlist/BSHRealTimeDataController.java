package com.callke8.bsh.bshorderlist;

import java.util.Date;
import java.util.Random;

import com.callke8.pridialqueueforbshbyquartz.BSHPredial;
import com.callke8.pridialqueueforbshbyquartz.BSHQueueMachineManager;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class BSHRealTimeDataController extends Controller {

	public void index() {
		render("list.jsp");
	}
	
	/**
	 * 取得实时数据
	 * 
	 * 返回三个数据：当前时间(时：分：秒)、排队机数据、活跃通道数据
	 * 
	 */
	public void getRealTimeData() {
		
		//当前时间
		String currTime = DateFormatUtils.formatDateTime(new Date(), "HH:mm:ss");
		
		Record data = new Record();
		data.set("name", currTime);
		data.set("value1", BSHQueueMachineManager.queueCount);
		//data.set("value2", BSHLaunchDialService.activeChannelCount);
		data.set("value2", BSHPredial.activeChannelCount);
		
		renderJson(data);
	}
	
	/**
	 * 取得随机数据
	 * 
	 * 范围从： 0,60
	 * 
	 * @return
	 */
	public int getRandomInt() {
		
		Random rand = new Random();
		
		return rand.nextInt(10) + 0;
	}
	
}
