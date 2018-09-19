package com.callke8.bsh.bshcallparam;

import java.util.Date;

import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

/**
 * 博世电器-呼叫配置（非管理类,在系统启动时自动加载至内存）
 * 
 * 然后在守护程序中，可以直接使用这些配置。
 * 
 * 避免反复查询数据库带来的资源开支
 * 
 * @author 黄文周
 *
 */
public class BSHCallParamConfig {
	
	/**
	 * 生效开始时间: 格式为：HH:mm
	 */
	private static String activeStartTime;
	
	/**
	 * 生效结束时间: 格式为: HH:mm
	 */
	private static String activeEndTime;
	
	
	
	public static String getActiveStartTime() {
		return activeStartTime;
	}

	public static void setActiveStartTime(String activeStartTime) {
		BSHCallParamConfig.activeStartTime = activeStartTime;
	}

	public static String getActiveEndTime() {
		return activeEndTime;
	}

	public static void setActiveEndTime(String activeEndTime) {
		BSHCallParamConfig.activeEndTime = activeEndTime;
	}

	/**
	 * 对比当前时间与系统生效时间（即是对比当前时间与开始生效时间、结束生效时间的情况）
	 * 
	 * 一般系统生效时间为早上9点至晚上20点，即是     09:00 ~ 20:00
	 * 
	 * 因为这样，一天就被分成了几个区间：
	 * （1）00:00 至 09:00
	 * （2）09:00 至 20:00
	 * （3）20:00 至 23:59
	 * 
	 * @return
	 * 		返回的结果：1 表示系统生效时间之前     2 表示系统生效时间   3：表示系统生效结束至12：00
	 */
	public static int compareCurrTime2ActiveTime() {
		
		if(BlankUtils.isBlank(activeStartTime) || BlankUtils.isBlank(activeEndTime)) {
			setActiveStartTimeAndActiveEndTime();
		}
		
		//（1）得到当前时间的毫秒数
		Date currDate = new Date();       
		long currDateLong = currDate.getTime();        //得到当前时间的毫秒数
		
		String currDateStr = DateFormatUtils.formatDateTime(new Date(), "yyyy-MM-dd");    //取得当前的日期，如 20180101
		//（2）取得生效开始时间点毫秒数
		String activeStartTimeStr = currDateStr + " " + activeStartTime + ":00";               //重新拼接成完整的日期字符串
		Date activeStartTimeDate = DateFormatUtils.parseDateTime(activeStartTimeStr, "yyyy-MM-dd HH:mm:ss");    //转成Date
		long activeStartTimeDateLong = activeStartTimeDate.getTime();  
		
		//（3）取得生效结束时间点毫秒数
		String activeEndTimeStr = currDateStr + " " + activeEndTime + ":00";               //重新拼接成完整的日期字符串
		Date activeEndTimeDate = DateFormatUtils.parseDateTime(activeEndTimeStr, "yyyy-MM-dd HH:mm:ss");    //转成Date
		long activeEndTimeDateLong = activeEndTimeDate.getTime();
		
		if(currDateLong < activeStartTimeDateLong) {         //如果当前时间小于生效时间，即是可能是 00:00 至 09：00 之间
			return 1;
		}else if(currDateLong >= activeStartTimeDateLong && currDateLong <= activeEndTimeDateLong) {   //在生效之后，且在结束之前时
			return 2;
		}else {
			return 3;      //表示 20：00 至 23：59：59 之前。
		}
	}
	
	/**
	 * 对于当前时间与系统的生效结束时间的对比
	 * (1)如果当前时间小于结束时间，且相差 30分钟以上,返回 1
	 * (2)如果当前时间小于结束时间，相差值在  0-30分钟之内，返回2
	 * (3)如果当前时间大于结束时间，返回 3
	 * 
	 * @return
	 * 		1：小于30分钟以上    2：0-30分钟之前       3：大于结束时间
	 */
	public static int compareCurrTime2ActiveEndTime() {
		
		if(BlankUtils.isBlank(activeStartTime) || BlankUtils.isBlank(activeEndTime)) {
			setActiveStartTimeAndActiveEndTime();
		}
		
		//(1)取得当前时间的毫秒数
		Date currDate = new Date();       
		long currDateLong = currDate.getTime();        //得到当前时间的毫秒数
		
		//(2)取得结束时间的毫秒数
		String currDateStr = DateFormatUtils.formatDateTime(new Date(), "yyyy-MM-dd");    //取得当前的日期，如 20180101
		String activeEndTimeStr = currDateStr + " " + activeEndTime + ":00";               //重新拼接成完整的日期字符串
		Date activeEndTimeDate = DateFormatUtils.parseDateTime(activeEndTimeStr, "yyyy-MM-dd HH:mm:ss");    //转成Date
		long activeEndTimeDateLong = activeEndTimeDate.getTime();
		
		//(3)对比两个时间
		if(currDateLong > activeEndTimeDateLong) {    //如果当前时间大于生效最晚时间，返回3
			return 3;
		}else {
			//时间差
			long timesDifference = activeEndTimeDateLong - currDateLong;
			
			if(timesDifference > (30 * 60 * 1000)) {
				return 1;
			}else {
				return 2;
			}
			
		}
		
	}
	
	
	public static void setActiveStartTimeAndActiveEndTime() {
		
		String activeTime = ParamConfig.paramConfigMap.get("paramType_3_activeTime");
		
		//如果配置参数为生效时间，一般格式为：      08:30-20:00  这种格式
		//所以我们需要将其分解，并将其设置到 activeStartTime 和  activeEndTime 这两个时间属性内
		if(!StringUtil.containsAny(activeTime, "-")) {    		//先查看是否包含 "-" 这个标识先,如果不包含，跳过此设置
			return;
		}
		String[] spRs = activeTime.split("-");
		if(spRs.length <2) {
			return;
		}
		
		String startTime = spRs[0];
		String endTime = spRs[1];
		
		activeStartTime = startTime;
		activeEndTime = endTime;
		
	}
	
}
