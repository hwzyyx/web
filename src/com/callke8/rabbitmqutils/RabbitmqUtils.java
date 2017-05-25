package com.callke8.rabbitmqutils;

import java.io.IOException;
import java.util.ArrayList;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Rabbitmq 消息功能实现类，
 * 	主要是用于发送和接收来电消息，用于弹屏使用
 * 
 * @author Administrator
 *
 */
public class RabbitmqUtils {
	
	ConnectionFactory factory = null;
	Connection conn = null;
	Channel channel = null;
	
	public RabbitmqUtils() {
		factory = new ConnectionFactory();
		factory.setHost("localhost");
		try {
			conn = factory.newConnection();
			channel = conn.createChannel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送消息功能
	 * 
	 * @param queueName
	 * 			队列名称
	 * @param msg
	 * 			消息内容
	 */
	public void sendMsg(String queueName,String msg) {
		
		try {
			channel.queueDeclare(queueName,false, false, false, null);
			
			channel.basicPublish("", queueName, null, msg.getBytes());
			
			channel.close();
			conn.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 接收消息功能
	 * 
	 * @param queueName
	 * @return
	 */
	public String receiveMsg(String queueName) {
		
		String message = null;
		
		try {
			channel.queueDeclare(queueName,false, false, false, null);
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			
			channel.basicConsume(queueName, true,consumer);
			
			QueueingConsumer.Delivery delivery;
			
			delivery = consumer.nextDelivery();
			
			message = new String(delivery.getBody());
			
			channel.close();
			
			conn.close();
			
		}catch (ShutdownSignalException e) {
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return message;
	}
	
}
