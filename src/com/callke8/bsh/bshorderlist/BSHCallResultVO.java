package com.callke8.bsh.bshorderlist;

import java.util.Date;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.Md5Utils;

/**
 *
 * 呼叫结果反馈实体类,用于在呼结束后，将结果反馈给BSH服务器
 * 
 *  参数	说明
 *  
	orderId	订单号id
	callType	外呼类型0.二次未接通1.一次接通/二次接通2放弃呼叫3已过期
	time	时间（yyyyMMddHHmmss）
	sign	签名（全小写）= md5(time + orderId+ key)key为约定好的密钥
	callResult	外呼结果 1：确认建单   2 暂不安装  3 短信确认   4 错误或无回复  5 放弃呼叫 6已过期
 * 
 * @author 黄文周
 *
 */
public class BSHCallResultVO {

	private String orderId;
	
	private String callType;
	
	private String time;
	
	private String sign;
	
	private String callResult;
	
	public BSHCallResultVO() {
		
	}
	
	/**
	 * 构造函数
	 * 
	 * @param orderId
	 * 			订单ID
	 * @param callType
	 * 			外呼类型0.二次未接通1.一次接通/二次接通2放弃呼叫3已过期
	 * @param callResult
	 * 			外呼结果 1：确认建单   2 暂不安装  3 短信确认   4 错误或无回复  5 放弃呼叫 6已过期
	 * @param bshCallResultKey
	 * 			呼叫结果反馈密钥
	 */
	public BSHCallResultVO(String orderId,String callType,String callResult,String bshCallResultKey) {
		
		this.orderId = orderId;
		this.callType = callType;
		this.time = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmmss");
		this.sign = Md5Utils.Md5(this.time + this.orderId + bshCallResultKey);
		this.callResult = callResult;
		
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getCallResult() {
		return callResult;
	}

	public void setCallResult(String callResult) {
		this.callResult = callResult;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		sb.append("\"orderId\":" + getOrderId() + ",");
		sb.append("\"callType\":\"" + getCallType() + "\",");
		sb.append("\"time\":\"" + getTime() + "\",");
		sb.append("\"sign\":\"" + getSign() + "\",");
		sb.append("\"callResult\":\"" + getCallResult() + "\"");
		sb.append("}");
		
		return sb.toString();
	}
	
	
}
