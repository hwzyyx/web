package com.callke8.bsh.bshcallparam;

import java.util.Date;

import com.callke8.utils.DateFormatUtils;

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
	 * 主叫号码，外呼时指定的呼出号码
	 */
	private static String callerNumber;
	
	/**
	 * 中继信息
	 */
	private static String trunkInfo;
	
	/**
	 * 扫描时间间隔（单位：秒）（即将待呼数据扫描至排队机的时间间隔）
	 */
	private static long scanInterval;
	
	/**
	 * 扫描数据数量（扫描数据至排队机的数量）
	 */
	private static int scanCount;
	
	/**
	 * 排队机最大的数量（超过该数量将暂时不再扫描）
	 */
	private static int queueMaxCount;
	
	/**
	 * 中继并发量
	 */
	private static int trunkMaxCapacity;
	
	/**
	 * 呼叫失败重试次数
	 */
	private static int retryTimes;
	
	/**
	 * 呼叫失败重试时间间隔（单位：分钟）
	 */
	private static int retryInterval;
	
	/**
	 * 语音文件存放地址
	 */
	private static String voicePath;
	
	/**
	 * 语音文件存放地址(单声道，主要用于电话通道中播放)
	 */
	private static String voicePathSingle;
	
	/**
	 * 单声道语音文件类型
	 */
	private static String mimeTypeForSingle;
	
	/**
	 * FastAgi地址，用于执行呼叫流程
	 */
	private static String agiUrl;
	
	/**
	 * 生效开始时间: 格式为：HH:mm
	 */
	private static String activeStartTime;
	
	/**
	 * 生效结束时间: 格式为: HH:mm
	 */
	private static String activeEndTime;
	
	/**
	 * 聚合数据请求URL，主要是用于手机归属地查询
	 */
	private static String juHeUrl;
	
	/**
	 * 聚合数据请求数据时，需要 appKey 支持
	 */
	private static String juHeAppKey;
	
	/**
	 * SOX 命令执行文件路径
	 */
	private static String soxBinPath;
	
	private static String bshCallResultUrl;
	
	private static String bshCallResultKey;

	public static String getCallerNumber() {
		return callerNumber;
	}

	public static void setCallerNumber(String callerNumber) {
		BSHCallParamConfig.callerNumber = callerNumber;
	}

	public static String getTrunkInfo() {
		return trunkInfo;
	}

	public static void setTrunkInfo(String trunkInfo) {
		BSHCallParamConfig.trunkInfo = trunkInfo;
	}

	public static long getScanInterval() {
		return scanInterval;
	}

	public static void setScanInterval(long scanInterval) {
		BSHCallParamConfig.scanInterval = scanInterval;
	}

	public static int getScanCount() {
		return scanCount;
	}

	public static void setScanCount(int scanCount) {
		BSHCallParamConfig.scanCount = scanCount;
	}

	public static int getQueueMaxCount() {
		return queueMaxCount;
	}

	public static void setQueueMaxCount(int queueMaxCount) {
		BSHCallParamConfig.queueMaxCount = queueMaxCount;
	}

	public static int getTrunkMaxCapacity() {
		return trunkMaxCapacity;
	}

	public static void setTrunkMaxCapacity(int trunkMaxCapacity) {
		BSHCallParamConfig.trunkMaxCapacity = trunkMaxCapacity;
	}

	public static int getRetryTimes() {
		return retryTimes;
	}

	public static void setRetryTimes(int retryTimes) {
		BSHCallParamConfig.retryTimes = retryTimes;
	}

	public static int getRetryInterval() {
		return retryInterval;
	}

	public static void setRetryInterval(int retryInterval) {
		BSHCallParamConfig.retryInterval = retryInterval;
	}

	public static String getVoicePath() {
		return voicePath;
	}

	public static void setVoicePath(String voicePath) {
		BSHCallParamConfig.voicePath = voicePath;
	}

	public static String getVoicePathSingle() {
		return voicePathSingle;
	}

	public static void setVoicePathSingle(String voicePathSingle) {
		BSHCallParamConfig.voicePathSingle = voicePathSingle;
	}

	public static String getAgiUrl() {
		return agiUrl;
	}

	public static void setAgiUrl(String agiUrl) {
		BSHCallParamConfig.agiUrl = agiUrl;
	}

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

	public static String getJuHeUrl() {
		return juHeUrl;
	}

	public static void setJuHeUrl(String juHeUrl) {
		BSHCallParamConfig.juHeUrl = juHeUrl;
	}

	public static String getJuHeAppKey() {
		return juHeAppKey;
	}

	public static void setJuHeAppKey(String juHeAppKey) {
		BSHCallParamConfig.juHeAppKey = juHeAppKey;
	}
	
	public static String getMimeTypeForSingle() {
		return mimeTypeForSingle;
	}

	public static void setMimeTypeForSingle(String mimeTypeForSingle) {
		BSHCallParamConfig.mimeTypeForSingle = mimeTypeForSingle;
	}

	public static String getSoxBinPath() {
		return soxBinPath;
	}

	public static void setSoxBinPath(String soxBinPath) {
		BSHCallParamConfig.soxBinPath = soxBinPath;
	}
	

	public static String getBshCallResultUrl() {
		return bshCallResultUrl;
	}

	public static void setBshCallResultUrl(String bshCallResultUrl) {
		BSHCallParamConfig.bshCallResultUrl = bshCallResultUrl;
	}

	public static String getBshCallResultKey() {
		return bshCallResultKey;
	}

	public static void setBshCallResultKey(String bshCallResultKey) {
		BSHCallParamConfig.bshCallResultKey = bshCallResultKey;
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
	

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("呼叫参数配置详情:\r\n");
		
		sb.append("主叫号码:" + callerNumber + "\r\n");
		sb.append("中继信息:" + trunkInfo + "\r\n");
		sb.append("扫描时间间隔:" + scanInterval + " 秒\r\n");
		sb.append("扫描数量:" + scanCount + " 条/次\r\n");
		sb.append("排队机最大数量:" + queueMaxCount + " 条\r\n");
		sb.append("中继最大并发量:" + trunkMaxCapacity + "\r\n");
		sb.append("呼叫失败重试次数:" + retryTimes + " 次\r\n");
		sb.append("呼叫失败重试间隔:" + retryInterval + " 分钟\r\n");
		sb.append("语音文件路径:" + voicePath + " \r\n");
		sb.append("语音文件路径 (单声道):" + voicePathSingle + " \r\n");
		sb.append("单声道语音类型:" + mimeTypeForSingle + "\r\n");
		sb.append("FastAgi地址:" + agiUrl + " \r\n");
		sb.append("聚合数据URL:" + juHeUrl + " \r\n");
		sb.append("聚合数据AppKey:" + juHeAppKey + " \r\n");
		sb.append("SOX执行路径:" + soxBinPath + " \r\n");
		sb.append("BSH呼叫结果URL:" + bshCallResultUrl + " \r\n");
		sb.append("BSH呼叫结果Key:" + bshCallResultKey + " \r\n");
		
		return sb.toString();
	}
	
}
