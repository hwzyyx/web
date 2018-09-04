package com.callke8.bsh.bshorderlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.common.CommonController;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

import net.sf.json.JSONObject;

/**
 * 数据统计 Controller 类
 * 
 * 用于显示时间区间内外呼的情况：已载入、已成功（确认安装、暂不安装、延后安装、提前预约、无/错回复）、已失败、待重呼、已过期、放弃呼叫 等状态的数量的情况
 * 
 * 并以双饼图的方式展示结果，当客户点击任意一状态值时，还可以弹出数据明细
 * 
 * 
 * 
 * @author 黄文周
 *
 */
public class BSHDataStatisticsController extends Controller {
	
	
	public void index() {
		setAttr("channelSourceComboboxDataFor1",CommonController.getComboboxToString("BSH_CHANNEL_SOURCE","1"));      		//购物平台带请选择的combobox
		
		render("list.jsp");
	}
	
	/**
	 * 重新加载统计数据
	 */
	public void reloadStatistics() {
		
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		String channelSource = getPara("channelSource");
		
		Record data = BSHOrderList.dao.getStatisticsData(startTime, endTime,channelSource);
		
		List<Record> list = new ArrayList<Record>();
		
		//已载入
		Record state1Data = new Record();
		state1Data.set("name", "已载入");
		state1Data.set("value", data.get("state1Data"));
		list.add(state1Data);
		
		//已成功
		Record state2Data = new Record();
		state2Data.set("name", "已成功");
		state2Data.set("value", data.get("state2Data"));
		list.add(state2Data);
		
		//确认安装
		Record respond1Data = new Record();
		respond1Data.set("name", "确认安装");
		respond1Data.set("value", data.get("respond1Data"));
		list.add(respond1Data);
		
		//暂不安装
		Record respond2Data = new Record();
		respond2Data.set("name", "暂不安装");
		respond2Data.set("value", data.get("respond2Data"));
		list.add(respond2Data);
		
		//延后安装
		Record respond3Data = new Record();
		respond3Data.set("name", "延后安装");
		respond3Data.set("value", data.get("respond3Data"));
		list.add(respond3Data);
		
		
		//提前预约
		Record respond4Data = new Record();
		respond4Data.set("name", "提前预约");
		respond4Data.set("value", data.get("respond4Data"));
		list.add(respond4Data);
		
		//无/错回复
		Record respond5Data = new Record();
		respond5Data.set("name", "无/错回复");
		respond5Data.set("value", data.get("respond5Data"));
		list.add(respond5Data);
		
		//待重呼
		Record state3Data = new Record();
		state3Data.set("name", "待重呼");
		state3Data.set("value", data.get("state3Data"));
		list.add(state3Data);
		
		//已失败
		Record state4Data = new Record();
		state4Data.set("name", "已失败");
		state4Data.set("value", data.get("state4Data"));
		list.add(state4Data);
		
		//已过期
		Record state5Data = new Record();
		state5Data.set("name", "已过期");
		state5Data.set("value", data.get("state5Data"));
		list.add(state5Data);
		
		//放弃呼叫
		Record state6Data = new Record();
		state6Data.set("name", "放弃呼叫");
		state6Data.set("value", data.get("state6Data"));
		list.add(state6Data);
		
		renderJson(list);
		
	}

}
