package com.callke8.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.bsh.bshvoice.BSHVoice;
import com.callke8.bsh.bshvoice.BSHVoiceConfig;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;

public class StringTest {
	
	public static void main(String[] args) {
		
		
		DruidPlugin dp = new DruidPlugin("jdbc:mysql://localhost/freeiris2?characterEncoding=utf-8", "root", "123456");
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
		
		arp.addMapping("bsh_orderList", BSHOrderList.class);
		arp.addMapping("bsh_voice", BSHVoice.class);
		//arp.addMapping("bsh_call_param", BSHCallParam.class);
		
		dp.start();
		arp.start();
		
		BSHVoice.dao.loadBSHVoiceDataToMemoryTest();
		//BSHCallParam.dao.loadCallParamDataToMemory();
		
		String bshOrderListId = "220219";
		
		BSHOrderList bol = BSHOrderList.dao.getBSHOrderListById(bshOrderListId);
		
		System.out.println(bol);
		
		String fileToString = getReadVoiceFileToString(bol);
		
		System.out.println("开始调查语音:-------------------");
		System.out.println(fileToString);
		
		List<Record> list1 = getRespond1PlayList(bol);
		System.out.println("回复1结果时：---------------------");
		for(Record r:list1) {
			System.out.println(r);
		}
		System.out.println();
		System.out.println("回复2结果时：---------------------");
		List<Record> list2 = getRespond2PlayList(bol);
		for(Record r:list2) {
			System.out.println(r);
		}
		System.out.println();
		System.out.println("回复3结果时：---------------------");
		List<Record> list3 = getRespond3PlayList(bol);
		for(Record r:list3) {
			System.out.println(r);
		}
		System.out.println();
		System.out.println("回复4结果时：---------------------");
		List<Record> list4 = getRespond4PlayList(bol);
		for(Record r:list4) {
			System.out.println(r);
		}
		
		System.out.println();
		System.out.println("没有回复或是回复错误时：---------------------");
		List<Record> listError = getRespondErrorPlayList(bol);
		for(Record r:listError) {
			System.out.println(r);
		}

		
	}
	
	public static String getReadVoiceFileToString(BSHOrderList bshOrderList) {
		
		StringBuilder sb = new StringBuilder();
		String voicePath = ParamConfig.paramConfigMap.get("paramType_3_voicePath");   //取出配置的语音文件（单声道）路径
		
		int brand = bshOrderList.getInt("BRAND");                            //品牌，0：西门子；1：博世
		int channelSource = bshOrderList.getInt("CHANNEL_SOURCE");           //购物平台，1：京东；2：苏宁；3：天猫；4：国美
		int timeType = bshOrderList.getInt("TIME_TYPE");                     //日期类型，1：安装日期；2：送货日期
		int productName = bshOrderList.getInt("PRODUCT_NAME");               //产品名称
		
		/** 一、组织第一条语音
		  begin_1_brand_0_timeType_1：您好，这里是西门子家电客服中心，来电跟您确认
          begin_1_brand_1_timeType_1：您好，这里是博世家电客服中心，来电跟您确认
          begin_1_brand_0_timeType_2：您好，这里是西门子家电客服中心
          begin_1_brand_1_timeType_2：您好，这里是博世家电客服中心
		 */
		String voiceNameFor1 = "begin_1_brand_" + brand + "_timeType_" + timeType;
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameFor1)) {
			sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameFor1));
		}
		
		/**
		 * 二、再根据日期类型,决定直接报产品名称，还是报：您在国美选购的
		 */
		if(timeType==1) {      //表示安装日期，需要直接报出产品的名称
			/**
			 * 整句即是：
			 * produceName_*:洗衣机   
			 * begin_2_timeType_1：的安装日期
			 */
			String voiceNameForProductName = "productName_" + productName;
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameForProductName)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForProductName));
			}
			//紧接着第二条语音: 的安装日期
			String voiceNameFor2 = "begin_2_timeType_1";
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameFor2)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameFor2));
			}
			
		}else {              //如果日期类型为送货日期，则需要先报出： 您在国美选购的
			/**
			 * 整句为：
			 * begin_2_timeType_2：您在国美选购的
			 * productName_*:  洗衣机
			 */
			//先紧接着第二条语音: 您在国美选购的
			String voiceNameFor2 = "begin_2_timeType_2";
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameFor2)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameFor2));
			}
			
			//产品语音播报
			String voiceNameForProductName = "productName_" + productName;
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameForProductName)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForProductName));
			}
			
		}
		
		/**
		 * 三、组织第三条语音
		 * 
		    begin_3_channelSource_1：根据京东平台传来的信息，我们将于
            begin_3_channelSource_2：根据苏宁平台传来的信息，我们将于
		    begin_3_channelSource_3：根据天猫平台传来的信息，我们将于
		    begin_3_channelSource_4：根据国美平台传来的信息，我们将于
		    begin_3_timeType_2：将于
		 */
		if(timeType==1) {     //日期类型为：安装日期
			String voiceNameFor3 = "begin_3_channelSource_" + channelSource;   
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameFor3)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameFor3));
			}
		}else {               //日期类型为：送货日期
			String voiceNameFor3 = "begin_3_timeType_2";   
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameFor3)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameFor3));
			}
		}
		
		/**
		 * 安装日期或是送货日期 组织
		 * 
		 * 还有一种情况需要考虑：
		 * 如果安装/送货日期为明天（即是第二天时），即无需报出具体时间，只需要播报”明天“即可
		 * 
		 */
		String expectInstallDate = bshOrderList.getDate("EXPECT_INSTALL_DATE").toString();      //取出期望安装日期
		
		boolean b = checkInstallDateIsNextDay(expectInstallDate);
		if(b) {
			//System.out.println("安装日期为明天");
			String voiceNameForDate = "tomorrow";
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameForDate)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForDate));
			}
		}else {
			//System.out.println("安装日期不是明天,而是" + expectInstallDate);
			Date installDate = DateFormatUtils.parseDateTime(expectInstallDate, "yyyy-MM-dd");
			String monthStr = DateFormatUtils.formatDateTime(installDate, "MM");
			String dayStr = DateFormatUtils.formatDateTime(installDate,"dd");
			String voiceNameForMonth = "month_" + monthStr;
			String voiceNameForDay = "day_" + dayStr;
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameForMonth)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForMonth));
			}
			if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameForDay)) {
				sb.append("&");
				sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForDay));
			}
		}
		
		
		/**
		 * 四、组织第四条语音
		 *
		   begin_4_timeType_1：上门安装
           begin_4_timeType_2：送货，我们将于送货当天上门安装，需要您进一步确认
		 */
		String voiceNameFor4 = "begin_4_timeType_" + timeType;
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameFor4)) {
			sb.append("&");
			sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameFor4));
		}
		
		/**
		 * 五、组织第五条语音
		 * begin_5_timeType_1：确认请按1，暂不安装请按2，如需改约到后面3天，请按3,如果您已经提前预约好服务，请按4。
           begin_5_timeType_2：确认送货当天安装请按1，暂不安装请按2，如需改约到后面3天请按3,如果您已经提前预约好服务,请按4。
		 */
		String voiceNameFor5 = "begin_5_timeType_" + timeType;
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameFor5)) {
			sb.append("&");
			sb.append(voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameFor5));
		}
		
		return sb.toString();
		
	}
	
	public static List<Record> getRespond1PlayList(BSHOrderList bshOrderList) {
		
		String voicePath = ParamConfig.paramConfigMap.get("paramType_3_voicePath");
		List<Record> list = new ArrayList<Record>();
		
		//（1）您的机器安装日期已确认为
		String voiceName = "respond_1_1";
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceName)) {
			list.add(setRecord("wait","1"));         //先停顿1秒
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceName))); 
			//list.add(setRecord("wait","1"));         //先停顿1秒
		}
		
		//（2）12月10号
		String expectInstallDate = bshOrderList.getDate("EXPECT_INSTALL_DATE").toString();      //取出期望安装日期
		Date installDate = DateFormatUtils.parseDateTime(expectInstallDate, "yyyy-MM-dd");
		String monthStr = DateFormatUtils.formatDateTime(installDate, "MM");
		String dayStr = DateFormatUtils.formatDateTime(installDate,"dd");
		
		String voiceNameForMonth = "month_" + monthStr;
		String voiceNameForDay = "day_" + dayStr;
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameForMonth)) {
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForMonth))); 
		}
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceNameForDay)) {
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForDay))); 
		}
		
		//（3）
		//     A:工程师最迟会在当天早上9点半之前与您联系具体上门时间，感谢您的配合，再见。
		//     B:工程师最迟会在当天早上9点半之前与您联系具体上门时间，为确保您的权益，请认准西门子厂家的专业工程师，感谢您的配合，再见。
		//     C:工程师最迟会在当天早上9点半之前与您联系具体上门时间，为确保您的权益，请认准博世厂家的专业工程师，感谢您的配合，再见。
		int timeType = bshOrderList.getInt("TIME_TYPE");     //日期类型: 1:安装日期；  2：送货日期
		int brand = bshOrderList.getInt("BRAND");            //品牌： 0：西门子；  1：博世
		int channelSource = bshOrderList.getInt("CHANNEL_SOURCE");   //购物平台：1：京东 2：苏宁  3：天猫 4：国美
		
		if(timeType==1) {        //日期类型为安装日期
			String voiceNameForTimeType1 = "respond_1_2_timeType_1";
			if(channelSource==4) {   //如果购物平台为国美
				voiceNameForTimeType1 = "respond_1_2_timeType_2_brand_" + brand;
			}
			list.add(setRecord("wait","0.5"));      //先停半秒
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForTimeType1)));
		}else {                  //日期类型为送货日期
			
			String voiceNameForTimeType2 = "respond_1_2_timeType_2_brand_" + brand;
			list.add(setRecord("wait","0.5"));      //先停半秒
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceNameForTimeType2)));
		}
		
		return list;
	}
	
	public static List<Record> getRespond2PlayList(BSHOrderList bshOrderList) {
		
		String voicePath = ParamConfig.paramConfigMap.get("paramType_3_voicePath");
		List<Record> list = new ArrayList<Record>();
		
		int brand = bshOrderList.getInt("BRAND");           //取得品牌
		String voiceName = "respond_2_brand_" + brand;				
		
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceName)) {
			list.add(setRecord("wait","0.5"));         //先停顿1秒
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceName))); 
		}
		
		return list;
	}
	
	public static List<Record> getRespond3PlayList(BSHOrderList bshOrderList) {
		
		String voicePath = ParamConfig.paramConfigMap.get("paramType_3_voicePath");
		List<Record> list = new ArrayList<Record>();
		
		//(1) 
		String voiceName = "respond_3";
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceName)) {
			list.add(setRecord("wait","0.5"));         //先停顿1秒
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceName))); 
		}
		
		return list;
	}
	
	public static List<Record> getRespond4PlayList(BSHOrderList bshOrderList) {
		
		String voicePath = ParamConfig.paramConfigMap.get("paramType_3_voicePath");
		List<Record> list = new ArrayList<Record>();
		
		int brand = bshOrderList.getInt("BRAND");           //取得品牌
		String voiceName = "respond_4_brand_" + brand;	
		
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceName)) {
			list.add(setRecord("wait","0.5"));         //先停顿1秒
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceName))); 
		}
		
		return list;
	}
	
	public static List<Record> getRespondErrorPlayList(BSHOrderList bshOrderList) {
		
		String voicePath = ParamConfig.paramConfigMap.get("paramType_3_voicePath");
		List<Record> list = new ArrayList<Record>();
		
		String voiceName = "respond_error";
		
		if(BSHVoiceConfig.getVoiceMap().containsKey(voiceName)) {
			list.add(setRecord("wait","0.5"));         //先停顿1秒
			list.add(setRecord("PlayBack",voicePath + "/" + BSHVoiceConfig.getVoiceMap().get(voiceName))); 
		}
		
		return list;
	}
	
	/**
	 * 检查安装/送货日期是否为第二天
	 * 
	 * @param expectInstallDate
	 * 				安装/送货 日期，格式：yyyy-MM-dd,如:2018-12-10
	 * 
	 * @return
	 * 		是：返回true; 否: 返回 false
	 */
	public static boolean checkInstallDateIsNextDay(String expectInstallDate) {
		
		//先判断当天日期与安装日期是否相差一天
		String installDateTime = expectInstallDate + " 00:00:00";
		String currDateTime = DateFormatUtils.formatDateTime(new Date(), "yyyy-MM-dd") + " 00:00:00";
				
		Date installDate = DateFormatUtils.parseDateTime(installDateTime, "yyyy-MM-dd HH:mm:ss");
		Date currDate = DateFormatUtils.parseDateTime(currDateTime, "yyyy-MM-dd HH:mm:ss");
		
		long installDateTimes = installDate.getTime();
		long currDateTimes = currDate.getTime();
		
		long intervalTimes = installDateTimes - currDateTimes;
		
		System.out.println("安装日期：expectInstallDate 为 " + expectInstallDate + ",与今天相差毫秒数：" + intervalTimes);
		
		if(intervalTimes == 24 * 60 * 60 * 1000) {
			return true;
		}else {
			return false;
		}
		
	}
	
	public static Record setRecord(String action,String path) {
		
		Record record = new Record();
		
		record.set("action", action);
		record.set("path", path);
		
		return record;
		
	}

}
