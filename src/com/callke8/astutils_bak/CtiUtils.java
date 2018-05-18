package com.callke8.astutils;

import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.OriginateCallback;

import com.callke8.utils.BlankUtils;

public class CtiUtils {
	
	//定义一个Map,用于储存通话保持（park） 时的记录, 键值对：座席号码 -> 目标通道
	public static Map<String,String> parkMap = new HashMap<String,String>();  

	/**
	 * 示闲
	 * @param agentNumber
	 * 			座席号
	 */
	public static void doDNDOff(String agentNumber) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		au.doDNDOff(agentNumber);
		
		au.logoff();
		
	}
	
	/**
	 * 示忙
	 * @param agentNumber
	 * 			座席号
	 */
	public static void doDNDOn(String agentNumber) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		au.doDNDOn(agentNumber);
		
		au.logoff();
		
	}
	
	public static String getAgentDNDValue(String agentNumber) {
		
		//AsteriskUtils au = new 
		return null;
	}
	
	
	/**
	 * 执行外呼,由座席发起呼叫,座席号码必填
	 * @param channel
	 * 			外呼通道
	 * @param context
	 * 			呼通后的 context
	 * @param exten
	 * 			呼通后的 exten
	 * @param priority
	 * 			优先级
	 * @param timeout
	 * 			超时时间
	 * @param callerId
	 * 			主叫号码
	 * @param variables
	 * 			通道参数
	 * @param cb
	 * 			回调类
	 * @return
	 */
	public static Map<String,String> doCallOut(java.lang.String channel,
            java.lang.String context,
            java.lang.String exten,
            int priority,
            long timeout,
            CallerId callerId,
            java.util.Map<java.lang.String,java.lang.String> variables,
            OriginateCallback cb) {
		
		Map<String,String> rs = new HashMap<String,String>();
		
		AsteriskUtils au = new AsteriskUtils();
		
		if(!au.getConnectionState()) {    //如果得到的 Asterisk 的连接状态为失败时返回
			rs.put("result", "0");
			rs.put("str", "拨号失败，连接 asterisk 服务器失败，请检查连接参数或是网络问题!");
			return rs;
		}
		
		
		//如果可以满足拨号条件时，执行呼出操作
		au.doCallOut(channel, context, exten, priority, timeout, callerId, variables, cb);
		
		au.logoff();
		
		rs.put("result", "1");
		rs.put("str", "执行外呼");
		return rs;
	}
	
	/**
	 * 执行外呼,接通后转到 application 执行应用
	 * 
	 * @param channel
	 * 			如： ss7/siuc/xxxxxx
	 * @param application
	 * 			执行应用：如 playback,fastagi 等
	 * @param data
	 * 			应用参数
	 * @param timeout
	 * 			超时时间
	 * @param callerId
	 * 			外呼号码
	 * @param variables
	 * 			通道参数
	 * @param cb
	 * 			回拨函数
	 * @return
	 */
	public static Map<String,String> doCallOutToApplication(String channel,String application,String data,long timeout,CallerId callerId,Map<String,String> variables,OriginateCallback cb) {
		
		Map<String,String> rs = new HashMap<String,String>();
		
		AsteriskUtils au = new AsteriskUtils();
		
		if(!au.getConnectionState()) {    //如果得到的 Asterisk 的连接状态为失败时返回
			rs.put("result", "0");
			rs.put("str", "拨号失败，连接 asterisk 服务器失败，请检查连接参数或是网络问题!");
			return rs;
		}
		
		au.doCallOutToApplication(channel,application,data,timeout,callerId,variables,cb);
		
		au.logoff();
		
		rs.put("result", "1");
		rs.put("str", "执行外呼");
		return rs;
		
	}
	
	/**
	 * 执行外呼,由座席发起呼叫,座席号码必填
	 * @param channel
	 * 			外呼通道
	 * @param context
	 * 			呼通后的 context
	 * @param exten
	 * 			呼通后的 exten
	 * @param priority
	 * 			优先级
	 * @param timeout
	 * 			超时时间
	 * @param callerId
	 * 			主叫号码
	 * @param variables
	 * 			通道参数
	 * @param cb
	 * 			回调类
	 * @return
	 */
	public static Map<String,String> doCallOutByAgent(String agentNumber,java.lang.String channel,
            java.lang.String context,
            java.lang.String exten,
            int priority,
            long timeout,
            CallerId callerId,
            java.util.Map<java.lang.String,java.lang.String> variables,
            OriginateCallback cb) {
		
		Map<String,String> rs = new HashMap<String,String>();
		
		AsteriskUtils au = new AsteriskUtils();
		
		if(!au.getConnectionState()) {    //如果得到的 Asterisk 的连接状态为失败时返回
			rs.put("result", "0");
			rs.put("str", "拨号失败，连接 asterisk 服务器失败，请检查连接参数或是网络问题!");
			return rs;
		}
		
		
		if(!au.isLogined(agentNumber)) {   //如果座席的登录状态为失败时，返回
			rs.put("result", "0");
			rs.put("str", "拨号失败，座席 " + agentNumber + " 的登录状态异常，请检查后再执行拨号!");
			au.logoff();   //断开连接
			return rs;
		}
		
		
		String channelName = au.getChannelByAgentNumber(agentNumber);  //先查看当前座席的通道情况，如果通道不为空时，则表示当前座席正在通话，系统将不允许执行外呼
		
		if(!BlankUtils.isBlank(channelName)) {      //如果当前座席的通道不为空时，则表示正在通话，系统将不允许执行外呼
			rs.put("result", "0");
			rs.put("str", "拨号失败，座席 " + agentNumber + " 正处于通话或是响铃状态，暂时不允许拨号!");
			au.logoff();   //断开连接
			return rs;
		}
		
		
		//如果可以满足拨号条件时，执行呼出操作
		au.doCallOut(channel, context, exten, priority, timeout, callerId, variables, cb);
		
		au.logoff();
		
		rs.put("result", "1");
		rs.put("str", "执行外呼");
		return rs;
	}
	
	/**
	 * 执行外呼
	 * @param channel
	 * 			外呼通道
	 * @param context
	 * 			呼通后的 context
	 * @param exten
	 * 			呼通后的 exten
	 * @param priority
	 * 			优先级
	 * @param timeout
	 * 			超时时间
	 * @param callerId
	 * 			主叫号码
	 * @param variables
	 * 			通道参数
	 * @param cb
	 * 			回调类
	 * @return
	 */
	public static Map<String,String> doCallOut4AutoContact(java.lang.String channel,
            java.lang.String context,
            java.lang.String exten,
            int priority,
            long timeout,
            CallerId callerId,
            java.util.Map<java.lang.String,java.lang.String> variables,
            OriginateCallback cb) {
		
		Map<String,String> rs = new HashMap<String,String>();
		
		AsteriskUtils au = new AsteriskUtils();
		
		if(!au.getConnectionState()) {    //如果得到的 Asterisk 的连接状态为失败时返回
			rs.put("result", "0");
			rs.put("str", "拨号失败，连接 asterisk 服务器失败，请检查连接参数或是网络问题!");
			return rs;
		}
		
		//如果可以满足拨号条件时，执行呼出操作
		au.doCallOut(channel, context, exten, priority, timeout, callerId, variables, cb);
		
		au.logoff();
		
		rs.put("result", "1");
		rs.put("str", "执行外呼");
		return rs;
	}
	
	/**
	 * 挂机
	 * @param agentNumber
	 */
	@SuppressWarnings("unchecked")
	public static void doHangup(String channel) {
		
		AsteriskUtils au = new AsteriskUtils();   
		
		au.hangupByChannel(channel);   //执行挂断通道
		
		au.logoff();                       //退出连接，返回结果
		
	}
	
	/**
	 * 查看PBX（ASTERISK）的连接状态
	 * 
	 * @return
	 */
	public static boolean getConnectionState() {
		
		boolean b = false;
		
		AsteriskUtils au = new AsteriskUtils();   
		
		b = au.getConnectionState();
		
		au.logoff();
		
		return b;
	}
	
	/**
	 * 检查通道是否还存在
	 * 
	 * @param channel
	 * 			通道名称
	 * @return
	 */
	public static boolean isExistChannel(String channel) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		boolean b = au.isExistChannel(channel);
		
		au.logoff();
		
		return b;
	}
	
	/**
	 * 针对通话中的座席号码,取得源通道及目标通道
	 * 
	 * 主要是用于通话保持及取消通话保持
	 * 
	 * @param agentNumber
	 * @return
	 */
	public static Map<String,String> getSrcChannelAndDstChannelByAgentNumber(String agentNumber) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		Map<String,String> channelMap = au.getSrcChannelAndDstChannelByAgentNumber(agentNumber);
		
		au.logoff();
		
		return channelMap;
	}
	
	
	/**
	 * 根据座席号码，取得与座席号码通道的目标通道
	 * 
	 * @param agentNumber
	 * @return
	 */
	public static String getDstChannelByAgentNumber(String agentNumber) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		String dstChannel = au.getDstChannelByAgentNumber(agentNumber);
		
		au.logoff();
		
		return dstChannel;
		
	}
	
	/**
	 * 根据座席号码，取得与座席号码通道的源通道
	 * 
	 * @param agentNumber
	 * @return
	 */
	public static String getSrcChannelByAgentNumber(String agentNumber) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		String srcChannel = au.getChannelByAgentNumber(agentNumber);
		
		au.logoff();
		
		return srcChannel;
		
	}
	
	
	/**
	 * 通话保持
	 * 
	 * @param agentNumber
	 * 			座席号码
	 * @return
	 * 		  Map
	 */
	public static void doPark(String srcChannel,String dstChannel) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		au.doPark(srcChannel, dstChannel);
		
		au.logoff();
		
	}
	
	public static void doBackPark(String agentNumber,String dstChannel) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		au.doBackPark(agentNumber, dstChannel);
		
		
		au.logoff();
	}
	
	/**
	 * 
	 * @return
	 */
	public static void doTransfer(String dstChannel,String forwardNumber) {
		
		
		AsteriskUtils au = new AsteriskUtils();
		
		au.doTransfer(dstChannel, forwardNumber);
		
		au.logoff();
		
	}

}
