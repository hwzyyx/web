package com.callke8.astutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;

import com.callke8.utils.BlankUtils;

/**
 * Asterisk 连接池
 * 
 * @author hasee
 *
 */
public class AsteriskConnectionPool {
	
	private static AsteriskConnectionPool instance;
	private static List<ManagerConnection> pool;
	
	private ManagerConnectionFactory factory;
	
	/**
	 * 初始化连接池，创建 poolMinSize 连接数量（即连接池的最小连接数量）
	 */
	private void initPool() {
		
		factory = new ManagerConnectionFactory(AsteriskConfig.getAstHost(), AsteriskConfig.getAstPort(), AsteriskConfig.getAstUser(), AsteriskConfig.getAstPassword());

		if(BlankUtils.isBlank(pool)) {
			pool = new ArrayList<ManagerConnection>();
		}
		
		while(pool.size() < AsteriskConfig.getAstPoolMinSize()) {
			
			ManagerConnection conn = factory.createManagerConnection();
			
			//调用连接注册线程进行注册
			Thread loginThread = new Thread(new AsteriskLoginThread(conn));
			loginThread.start();
			
			pool.add(conn);
		}
		
	}
	
	/**
	 * 私有构造方法-用于单例生成，避免多次创建连接池，控制 asterisk 连接池的连接数量
	 * 
	 * @param hostName
	 * @param port
	 * @param userName
	 * @param password
	 */
	private AsteriskConnectionPool() {
		initPool();      //初始化Asterisk连接池
	}
	
	/**
	 * 列出连接池中所有连接的状态
	 */
	public static void listAllConnectionState() {
		StringBuilder sb = new StringBuilder();
		if(!BlankUtils.isBlank(pool)) {
			
			int i = 1;
			for(ManagerConnection conn:pool) {
				sb.append("连接池连接第 " + i + " 个连接的连接状态为:" + conn.getState().toString() + "\r\n");
				i++;
			}
			
		}
		
		System.out.println("连接中所有连接的状态：\r\n" + sb.toString());
		
	}
	
	/**
	 * 生成实例-单例
	 * @return
	 */
	public static AsteriskConnectionPool newInstance() {
		
		if(instance == null) {
			instance = new AsteriskConnectionPool();
		}
		
		return instance;
		
	}
	
	
	/**
	 * 从连接池中取出一个连接
	 * 
	 * 如果连接池中的数量为0时，则先向连接池中增加一个连接
	 * 
	 * @return
	 */
	public synchronized ManagerConnection getConnection() {
		
		//如果连接池中的数量等于0时，则向连接池中增加一个连接
		if(pool.size() <= 0) {
			addConnection();
		}
		
		int last_index = pool.size() - 1;
		
		ManagerConnection conn = pool.get(last_index);
		
		//将取出的连接从连接池中删除
		pool.remove(last_index);
		
		return conn;
		
	}
	
	/**
	 * 
	 * 关闭连接（假关闭）：主要是将取出的连接放回连接池
	 * 
	 * 当连接池的数量大于允许最大连接数量时，直接 logoff 并去除
	 * 
	 * @param conn
	 */
	public synchronized void close(ManagerConnection conn) {
		
		if(pool.size() >= AsteriskConfig.getAstPoolMaxSize()) {
			conn.logoff();
			conn = null;
		}else {
			pool.add(conn);
		}
		
	}
	
	/**
	 * 查看连接池的连接数
	 * @return
	 */
	public int getConnectionPoolSize() {
		return pool.size();
	}
	
	
	/**
	 * 私有方法：向连接池中增加连接
	 */
	private void addConnection() {
		
		/**
		 * 只有当连接池数小于允许最大数量时，才新增连接
		 */
		if(pool.size() < AsteriskConfig.getAstPoolMaxSize()) {
			
			ManagerConnection conn = factory.createManagerConnection();
			
			//调用连接注册线程进行注册
			Thread loginThread = new Thread(new AsteriskLoginThread(conn));
			loginThread.start();
			
			pool.add(conn);
			
		}
		
	}
	
	
}
