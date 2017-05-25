package com.callke8.utils;

import java.io.IOException;

import javax.servlet.http.HttpServlet;

import org.asteriskjava.fastagi.AgiServer;
import org.asteriskjava.fastagi.AgiServerThread;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@SuppressWarnings("serial")
public class Fastagi extends HttpServlet {

	public Fastagi() {
		
		AgiServerThread thread = new AgiServerThread();
		
		thread.setAgiServer(new DefaultAgiServer());
		thread.startup();
		
		System.out.println("Fastagi 构造方法...");
		
	}

}
