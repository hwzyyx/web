package com.callke8.astutils;

import java.io.IOException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;

/**
 * 生成Asterisk的连接后，注册的线程，因为注册需要延时
 * 
 * 且需要阻塞式注册，所以必须写到一个线程里执行，以确保主程序可以快速执行
 * 
 * @author 黄文周
 *
 */
public class AsteriskLoginThread implements Runnable {
	
	private ManagerConnection conn;
	
	public AsteriskLoginThread(ManagerConnection conn) {
		this.conn = conn;
	}

	@Override
	public void run() {
		try {
			conn.login();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

}
