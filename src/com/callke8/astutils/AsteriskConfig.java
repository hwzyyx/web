package com.callke8.astutils;

/**
 * 
 * Asterisk配置信息
 * 
 * @author <a href="mailto:120077407@qq.com">黄文周</a>
 *
 */
public class AsteriskConfig {

	/**
	 * asterisk 服务器地址
	 */
	private static String astHost;
	
	/**
	 * asterisk 连接端口
	 */
	private static int astPort;
	
	/**
	 * asterisk 用户名
	 */
	private static String astUser;
	
	/**
	 * asterisk 用户密码
	 */
	private static String astPassword;
	
	/**
	 * 外呼 context
	 */
	private static String astCallOutContext;
	
	/**
	 * 外呼主叫号码
	 */
	private static String astCallerId;
	
	/**
	 * 呼叫保持 Context
	 */
	private static String astHoldOnContext;
	
	/**
	 * 自动接触的 Context
	 */
	private static String astAutoContactContext;
	
	/**
	 * 自动接触的 通道
	 */
	private static String astAutoContactChannel;
	
	/**
	 * 自动接触的录音路径
	 */
	private static String astAutoContactRecordDir;
	
	/**
	 * Asterisk连接池的最小连接数
	 */
	private static int astPoolMinSize;
	
	/**
	 * Asterisk连接池的最大连接数
	 */
	private static int astPoolMaxSize;

	public static String getAstHost() {
		return astHost;
	}

	public static void setAstHost(String astHost) {
		AsteriskConfig.astHost = astHost;
	}

	public static int getAstPort() {
		return astPort;
	}

	public static void setAstPort(int astPort) {
		AsteriskConfig.astPort = astPort;
	}

	public static String getAstUser() {
		return astUser;
	}

	public static void setAstUser(String astUser) {
		AsteriskConfig.astUser = astUser;
	}

	public static String getAstPassword() {
		return astPassword;
	}

	public static void setAstPassword(String astPassword) {
		AsteriskConfig.astPassword = astPassword;
	}

	public static String getAstCallOutContext() {
		return astCallOutContext;
	}

	public static void setAstCallOutContext(String astCallOutContext) {
		AsteriskConfig.astCallOutContext = astCallOutContext;
	}

	public static String getAstCallerId() {
		return astCallerId;
	}

	public static void setAstCallerId(String astCallerId) {
		AsteriskConfig.astCallerId = astCallerId;
	}

	public static String getAstHoldOnContext() {
		return astHoldOnContext;
	}

	public static void setAstHoldOnContext(String astHoldOnContext) {
		AsteriskConfig.astHoldOnContext = astHoldOnContext;
	}

	public static String getAstAutoContactContext() {
		return astAutoContactContext;
	}

	public static void setAstAutoContactContext(String astAutoContactContext) {
		AsteriskConfig.astAutoContactContext = astAutoContactContext;
	}

	public static String getAstAutoContactChannel() {
		return astAutoContactChannel;
	}

	public static void setAstAutoContactChannel(String astAutoContactChannel) {
		AsteriskConfig.astAutoContactChannel = astAutoContactChannel;
	}

	public static String getAstAutoContactRecordDir() {
		return astAutoContactRecordDir;
	}

	public static void setAstAutoContactRecordDir(String astAutoContactRecordDir) {
		AsteriskConfig.astAutoContactRecordDir = astAutoContactRecordDir;
	}

	public static int getAstPoolMinSize() {
		return astPoolMinSize;
	}

	public static void setAstPoolMinSize(int astPoolMinSize) {
		AsteriskConfig.astPoolMinSize = astPoolMinSize;
	}

	public static int getAstPoolMaxSize() {
		return astPoolMaxSize;
	}

	public static void setAstPoolMaxSize(int astPoolMaxSize) {
		AsteriskConfig.astPoolMaxSize = astPoolMaxSize;
	}
	
}
