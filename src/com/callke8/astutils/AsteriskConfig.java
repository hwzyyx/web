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
	
}
