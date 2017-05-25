package com.callke8.astutils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.event.ManagerEvent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AstMonitor_bak extends HttpServlet implements ManagerEventListener {

	ManagerConnectionFactory factory;
	private ManagerConnection conn;
	private static Log log = LogFactory.getLog(AstMonitor_bak.class);
	private static String astHost;
	private static int astPort;
	private static String astUser;
	private static String astPass;
	private int i = 0;

	public AstMonitor_bak() {
		factory = new ManagerConnectionFactory(astHost, astPort, astUser,
				astPass);
		conn = factory.createManagerConnection();
	}

	public void init() throws ServletException {
		log.info("aaaaaaaaaaa-----------aaaaaaaaaaaaaaaaaaa");
		conn.addEventListener(this);
		try {
			conn.login();

			if (i == 0) {
				//checkStatus();
				i++;
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			conn = null;
			conn = factory.createManagerConnection();
			try {
				init();
			} catch (ServletException e2) {
				e2.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	/*public static void checkStatus() { while(true) { 
		//String state = conn.getState().toString();
	  
		//if(state == null || !state.equalsIgnoreCase("CONNECTED")) {
		System.out.println("连接异常，重新连接..."); conn = null; 
			conn = factory.createManagerConnection(); 
			try { 
				init(); 
			}catch(ServletException e) { 
				e.printStackTrace(); 
			}
		}
	  
		try { 
			Thread.sleep(3 * 1000); 
		}catch(InterruptedException e) {
		  e.printStackTrace(); 
		  } 
		}
	  
	  }*/

	@Override
	public void onManagerEvent(ManagerEvent event) {
		log.info(event);
	}

	public static String getAstHost() {
		return astHost;
	}

	public static void setAstHost(String astHost) {
		AstMonitor_bak.astHost = astHost;
	}

	public static int getAstPort() {
		return astPort;
	}

	public static void setAstPort(int astPort) {
		AstMonitor_bak.astPort = astPort;
	}

	public static String getAstUser() {
		return astUser;
	}

	public static void setAstUser(String astUser) {
		AstMonitor_bak.astUser = astUser;
	}

	public static String getAstPass() {
		return astPass;
	}

	public static void setAstPass(String astPass) {
		AstMonitor_bak.astPass = astPass;
	}
}
