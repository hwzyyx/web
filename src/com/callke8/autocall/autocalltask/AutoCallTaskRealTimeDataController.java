package com.callke8.autocall.autocalltask;

import java.util.Date;
import java.util.Random;

import com.callke8.predialqueueforautocallbyquartz.AutoCallPredial;
import com.callke8.predialqueueforautocallbyquartz.AutoCallQueueMachineManager;
import com.callke8.pridialqueueforbshbyquartz.BSHPredial;
import com.callke8.pridialqueueforbshbyquartz.BSHQueueMachineManager;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class AutoCallTaskRealTimeDataController extends Controller {

	public void index() {
		String queueMaxCountStr = ParamConfig.paramConfigMap.get("paramType_4_queueMaxCount");     //排队机最大数量
	
		setAttr("queueMaxCount", queueMaxCountStr);                            //设置页面中限制的最大数量
		
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
		//data.set("value1", getQueueCount());
		data.set("value1", AutoCallQueueMachineManager.queueCount);
		//data.set("value2", BSHLaunchDialService.activeChannelCount);
		data.set("value2", AutoCallPredial.activeChannelCount);
		
		renderJson(data);
	}
	
	/**
	 * 取得排队机的排队的数量，主要是可能会因为扫描的不精准，导致排队机的数量超过了系统限定的数量
	 * 
	 * 所以如果排队机中的数量大于系统限定的数量时，就以系统限定数量为当前排队机的数量
	 * 
	 * @return
	 */
	public int getQueueCount() {
		
		String queueMaxCountStr = ParamConfig.paramConfigMap.get("paramType_4_queueMaxCount");     //排队机最大数量
		int queueMaxCount = Integer.valueOf(queueMaxCountStr);            //系统限定的最大排队量
		int queueCount = AutoCallQueueMachineManager.queueCount;          //排队机中现在排队的数量
		if(queueCount >= queueMaxCount) {      //如果排队机中的数量大于限定最大量时
			queueCount = queueMaxCount;
		}
		
		return queueCount;
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
