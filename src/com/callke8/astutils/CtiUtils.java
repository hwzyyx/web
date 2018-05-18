package com.callke8.astutils;

import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.OriginateCallback;
import org.asteriskjava.manager.ManagerConnection;

import com.callke8.utils.BlankUtils;

/**
 * CtiUtils 即 CTI 执行工具
 * 
 * 里面提供了各种 CTI方法： 示忙、示闲、呼叫转移、外呼、挂机等等
 * 
 * @author <a href="mailto:120077407@qq.com">黄文周</a>
 *
 */
public class CtiUtils {

	//定义一个Map,用于储存通话保持（park） 时的记录, 键值对：座席号码 -> 目标通道
	public static Map<String,String> parkMap = new HashMap<String,String>();  
	
	/**
	 * 执行示忙
	 * 
	 * @param agentNumber
	 */
	public static String doDNDOn(String agentNumber) {
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		au.doDNDOn(agentNumber);
		au.close();
		
		return "执行示忙操作指令已发送";
	}
	
	/**
	 * 执行示闲
	 * 
	 * @param agentNumber
	 */
	public static String doDNDOff(String agentNumber) {
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		au.doDNDOff(agentNumber);
		au.close();

		return "执行示闲操作指令已发送";
	}
	
	/**
	 * 查看座席的示忙、示闲状态
	 * 
	 * @param agentNumber
	 */
	public static String getAgentDNDState(String agentNumber) {
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		String dndValue = au.getDNDValue(agentNumber);
		au.close();
		
		return dndValue;
	}
	
	/**
	 * 执行外呼,接通后转到 Extension
	 * 
	 * @param channel
	 * @param context
	 * @param exten
	 * @param priority
	 * @param timeout
	 * @param callerId
	 * @param variables
	 * @param cb
	 * @return
	 */
	public static String doCallOutToExtension(String channel,String context,String exten,int priority,long timeout,CallerId callerId,Map<String,String> variables,OriginateCallback cb) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		au.doCallOutToExtension(channel, context, exten, priority, timeout, callerId, variables, cb);
		
		return "外呼请求已成功发送!";
		
	}
	
	/**
	 * 执行外呼，接通后转到 Application
	 * 
	 * @param channel
	 * @param application
	 * @param data
	 * @param timeout
	 * @param callerId
	 * @param variables
	 * @param cb
	 * @return
	 */
	public static String doCallOutToApplication(String channel,String application,String data,long timeout,CallerId callerId,Map<String,String> variables,OriginateCallback cb) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		au.doCallOutToApplication(channel, application, data, timeout, callerId, variables, cb);
		
		return "外呼请求已经成功发送!";
		
	}
	
	/**
	 * 执行外呼，接通后转到 Application(针对博西项目写的方法)
	 * 
	 * @param channel
	 * @param application
	 * @param data
	 * @param timeout
	 * @param callerId
	 * @param variables
	 * @param cb
	 * @return
	 */
	public static String doCallOutToApplication(AsteriskUtils au,String channel,String application,String data,long timeout,CallerId callerId,Map<String,String> variables,OriginateCallback cb) {
		
		if(BlankUtils.isBlank(au)) {
			au = new AsteriskUtils();
		}
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		au.doCallOutToApplication(channel, application, data, timeout, callerId, variables, cb);
		
		return "外呼请求已经成功发送!";
		
	}
	
	
	/**
	 * 执行外呼，由座席发起外呼，座席号码必填
	 * 
	 * @param agentNumber
	 * @param channel
	 * @param context
	 * @param exten
	 * @param priority
	 * @param timeout
	 * @param callerId
	 * @param variables
	 * @param cb
	 */
	public static String doCallOutByAgent(String agentNumber,
			String channel,
			String context,
			String exten,
			int priority,
			long timeout,
			CallerId callerId,
			Map<String,String> variables,
			OriginateCallback cb) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		//检查传入的座席是否已经登录 
		boolean isLogin = au.isAgentLogin(agentNumber);
		if(!isLogin) {
			au.close();
			return "无法执行操作,座席 " + agentNumber + " 登录状态异常，请检查后再执行拨号!";
		}
		
		//检查传入的座席是否处于通话状态,通话状态则不允许执行外呼
		String channelName = au.getChannelByAgentNumber(agentNumber);
		
		if(!BlankUtils.isBlank(channelName)) {
			au.close();
			return "无法执行操作,座席 " + agentNumber + " 正处于通话或是响铃状态,暂无法执行外呼!";
		}
		
		//执行到此，表示该外呼可以满足拨号条件，执行外呼操作
		au.doCallOutToExtension(channelName, context, exten, priority, timeout, callerId, variables, cb);
		
		return "success";
		
	}
	
	/**
	 * 挂机,挂断通话通道
	 * 
	 * @param channel
	 */
	public static String doHangUp(String channel) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		au.hangupByChannel(channel);
		au.close();
		
		return "执行挂机操作成功!";
	}
	
	/**
	 * 挂机，根据坐席号码挂机
	 * 
	 * （1）先根据坐席号码取得通话通道
	 * （2）挂机操作，根据通话通道挂机
	 * 
	 * @param agentNumber
	 * @return
	 */
	public static String doHangUpByAgentNumber(String agentNumber) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "无法执行操作,Asterisk服务器连接异常!";
		}
		
		String channel = au.getChannelByAgentNumber(agentNumber);
		
		au.hangupByChannel(channel);
		au.close();
		
		return "执行挂机操作成功!";
	}
	
	/**
	 * 检查通道是否存在
	 * 
	 * @param channel
	 * @return
	 */
	public static boolean isExistChannel(String channel) {
		
		boolean b = false;
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) { 
			return b;
		}
		
		b = au.isExistChannel(channel);
		au.close();
		
		return b;
	}
	
	/**
	 * 根据座席号码，取得当前通话的原通道及目标通道
	 * 
	 * 主要是用于通话保持及取消通话保持
	 * 
	 * @param agentNumber
	 * @return
	 */
	public static Map<String,String> getSrcChannelAndDstChannelByAgentNumber(String agentNumber) {
		
		Map<String,String> m = null;
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return m;
		}
		
		m = au.getSrcChannelAndDstChannelByAgentNumber(agentNumber);
		au.close();
		
		return m;
		
	}
	
	/**
	 * 根据座席号码，取得与座席号码通话的目标通道
	 * 
	 * @param agentNumber
	 * @return
	 */
	public static String getDstChannelByAgentNumber(String agentNumber) {
		
		String dstChannel = null;
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) { 
			au.close();
			return dstChannel;
		}	
		
		dstChannel = au.getDstChannelByAgentNumber(agentNumber);
		au.close();
		
		return dstChannel;
		
	}
	
	/**
	 * 
	 * 根据座席号码，取得与座席号码通话的源通道
	 * 
	 * @param agentNumber
	 * @return
	 */
	public static String getSrcChannelByAgentNumber(String agentNumber) {
		
		String srcChannel = null;
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) { 
			return srcChannel;
		}	
		
		srcChannel = au.getSrcChannelByAgentNumber(agentNumber);
		au.close();
		
		return srcChannel;
		
	}
	
	/**
	 * 通话保持，根据源通道及目标通道，执行通话保持
	 * 
	 * @param srcChannel
	 * @param dstChannel
	 */
	public static String doPark(String srcChannel,String dstChannel) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) {
			au.close();
			return "执行操作失败,Asterisk连接状态异常!";
		}
		
		au.doPark(srcChannel, dstChannel);
		au.close();
		
		return "通话保持请求已经执行";
	}
	
	/**
	 * 通话保持,根据座席号码执行通话保持
	 * 
	 * @param agentNumber
	 */
	public static String doPark(String agentNumber) {
		
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(!au.isAstConnSuccess()) { 
			au.close();
			return "执行操作失败,Asterisk连接状态异常!";
		}
		
		Map<String,String> channelMap = au.getSrcChannelAndDstChannelByAgentNumber(agentNumber);
		
		String srcChannel = null;
		String dstChannel = null;
		if(!BlankUtils.isBlank(channelMap)) {
			srcChannel = channelMap.get("srcChannel");	
			dstChannel = channelMap.get("dstChannel");
		}
		
		
		if(BlankUtils.isBlank(srcChannel) || BlankUtils.isBlank(dstChannel)) {
			au.close();
			return "执行通话保持失败,座席未在通话状态!";
		}
		
		doPark(srcChannel,dstChannel);
		au.close();
		
		return "执行通话保持指令已发送!";
		
	}
	
	/**
	 *取消通话保持
	 *
	 * @param agentNumber
	 * @param dstChannel
	 */
	public static String doBackPark(String agentNumber,String dstChannel){
		
		AsteriskUtils au = new AsteriskUtils();
		//检查连接状态
		if(!au.isAstConnSuccess()) { 
			au.close();
			return "执行操作失败,Asterisk连接状态异常!";
		}
		
		if(BlankUtils.isBlank(agentNumber) || BlankUtils.isBlank(dstChannel)) {
			au.close();
			return "执行取消通话保持失败,座席为空或是目标通道为空!";
		}
		
		au.doBackPark(agentNumber, dstChannel);
		au.close();
		
		return "执行取消通话保持指令已发送!";
	}
	
	/**
	 * 呼叫转移
	 * 
	 * @param dstChannel
	 * @param forwardNumber
	 */
	public static String doTransfer(String dstChannel,String forwardNumber) {
		
		AsteriskUtils au = new AsteriskUtils();
		//检查连接状态
		if(!au.isAstConnSuccess()) { 
			au.close();
			return "执行操作失败,Asterisk连接状态异常!";
		}
		
		if(BlankUtils.isBlank(dstChannel) || BlankUtils.isBlank(forwardNumber)) {
			au.close();
			return "执行呼叫转移失败,目标通道为空或是目标号码为空!";
		}
		
		au.doTransfer(dstChannel, forwardNumber);
		au.close();
		
		return "执行呼叫转移指令已经发送!";
	}
	
	/**
	 * 检查 Asterisk 的链接状态
	 * 
	 * 先从连接池中取一个连接出来，然后判断连接状态是否正常
	 * 
	 * 得到连接结果后，将其放回连接池
	 * 
	 * @return
	 */
	public static boolean checkConnectionState() {
		
		//从连接池中取出 一个连接
		AsteriskUtils au = new AsteriskUtils();
		
		//检查连接状态
		if(au.isAstConnSuccess()) { 
			au.close();
			return true;
		}else {
			au.close();
			return false;
		}
		
		
	}
	
}
