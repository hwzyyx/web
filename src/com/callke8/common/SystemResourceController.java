package com.callke8.common;

import com.callke8.utils.SystemResourceThread;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;


/**
 * 系统资源Controller,主要是用于显示系统资源使用情况
 * 
 * 用于前端 “我的工作台” 中显示的系统资源使用情况显示
 * 
 * 1 CPU、内存 使用情况
 * 
 * @author hasee
 *
 */
public class SystemResourceController extends Controller {

	/**
	 * cpu 数据
	 */
	public void getCpuRamResourceData() {
		
		Record data = new Record(); 
		data.set("cpuValue",SystemResourceThread.systemResourceDataRecord.get("cpuValue"));
		data.set("ramValue",SystemResourceThread.systemResourceDataRecord.get("ramValue"));
		data.set("cpuInfo",SystemResourceThread.systemResourceDataRecord.get("cpuInfo"));
		data.set("upTime",SystemResourceThread.systemResourceDataRecord.get("upTime"));
		data.set("cpuSpeed",SystemResourceThread.systemResourceDataRecord.get("cpuSpeed"));
		data.set("memoryUsage",SystemResourceThread.systemResourceDataRecord.get("memoryUsage"));
		
		renderJson(data);
		
	}
	
	
}
