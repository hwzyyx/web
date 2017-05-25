package com.callke8.rabbitmqutils;

/**
 * 消息工具，主要是用于发送和接收来电消息，用于弹屏使用
 * 			需要调用 rabbitmqUtils 实现消息功能
 * 
 * @author Administrator
 *
 */
public class MessageUtils {
	
	/**
	 * 发送消息
	 * 
	 * @param queueName
	 * 			队列名称
	 * @param msg
	 * 			消息内容
	 * @return
	 */
	public static void sendMsg(String queueName, String msg) {
		
		RabbitmqUtils ru = new RabbitmqUtils();
		
		ru.sendMsg(queueName, msg);
		
	}
	
	/**
	 * 接收消息
	 * 
	 * @param queueName
	 * @return
	 */
	public static String receiveMsg(String queueName) {
		
		RabbitmqUtils ru = new RabbitmqUtils();
		
		String msg = ru.receiveMsg(queueName);
		
		return msg;
		
	}
	
}	
