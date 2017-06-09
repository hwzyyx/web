package com.callke8.astutils;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.SendActionCallback;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.response.CommandResponse;
import org.asteriskjava.manager.response.ManagerResponse;

import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.StringUtil;

/**
 * 
 * 用于监控座席状态的守护程序
 * 
 * @author hasee
 *
 */
public class AgentStateMonitor extends Thread {
	
	ManagerConnectionFactory factory;     //定义一个 asterisk 的连接工厂
	ManagerConnection conn;
	String connState;                     //Asterisk 连接状态 
	
	Log log = LogFactory.getLog(AgentStateMonitor.class);
	
	public AgentStateMonitor() {
		
		factory = new ManagerConnectionFactory(AstMonitor.getAstHost(),AstMonitor.getAstPort(),AstMonitor.getAstUser(), AstMonitor.getAstPass());
		
		conn = factory.createManagerConnection();
	}
	
	public void run() {
		
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		int i = 1;
		
		while(true) {
			
			connState = BlankUtils.isBlank(conn)?null:conn.getState().toString();
			
			log.info("系统第 " + i + " 次更新获取并更新座席状态!" );
			
			if(BlankUtils.isBlank(connState) || !connState.equalsIgnoreCase("CONNECTED")) {     //判断 Asterisk 的连接状态,如果连接状态异常时
				
				log.info("Asterisk(PBX) 连接状态异常,当前连接状态为 " + connState + ", 系统将尝试重新连接!");
				
				try {
					
					if(connState.equalsIgnoreCase("RECONNECTING")) {     //如果状态为 RECONNECTING 时, 需要先 logoff, 然后再重新连接
						conn.logoff();
					}
					conn.login();
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (AuthenticationFailedException e1) {
					e1.printStackTrace();
				} catch (TimeoutException e1) {
					e1.printStackTrace();
				}
			}else {
				
				log.info("Asterisk(PBX)连接状态正常");
				
				CommandAction action = new CommandAction("core show hints");
				
				try {
					conn.sendAction(action, new SendActionCallback() {
						
						@Override
						public void onResponse(ManagerResponse response) {
							
							CommandResponse cr = (CommandResponse)response;
							
							/**
							 * 取出 执行的结果,将结果返回到 listRs 字串组中， listRs 的数据格式如下
							 * 
							 * -= Registered Asterisk Dial Plan Hints =-
							 * 
				                   8005@ext-local           : SIP/8005              State:Unavailable     Watchers  0
				                   8004@ext-local           : SIP/8004              State:Idle            Watchers  0
				                   8006@ext-local           : SIP/8006              State:Unavailable     Watchers  0
				                   8001@ext-local           : SIP/8001              State:Unavailable     Watchers  0
				                   8003@ext-local           : SIP/8003              State:Idle            Watchers  0
				                   8002@ext-local           : SIP/8002              State:Unavailable     Watchers  0
								----------------
								- 6 hints registered
							 * 
							 * 
							 * 接下来,遍历Hints 的结果,取出  SIP/XXX 或是   IAX2/XXX 及      State:XXXXXXX 进行分析座席的状态
							 */
							List<String> listRs = cr.getResult();
							
							for(String line:listRs) {
								
								if(!BlankUtils.isBlank(line)) {
									boolean b = StringUtil.containsAny(line, "State");    //只有包含 State 字符串的行，才进行判断
									
									//8004@ext-local           : SIP/8004              State:Idle            Watchers  0          取出的行类似这种字符串
									//然后根据多个空格进行分隔字符串
									if(b) {
										String[] list = line.split("\\s+");    //以多个空格分解
										String agentInfo = null;    //定义座席信息,用于存储（SIP/8001   IAX2/5001 类似的信息）
										String stateInfo = null;    //定义状态信息,用于存储（State:Idle   State:Unavailable State:Ringing 类似的信息）
										for(String str:list) {
											if(!BlankUtils.isBlank(str)) {
												if(StringUtil.containsAny(str,"SIP")) {
													agentInfo = str;
												}
												
												if(StringUtil.containsAny(str,"IAX2")) {
													agentInfo = str;
												}
												
												if(StringUtil.containsAny(str, "State")) {
													stateInfo = str;
												}
												
											}
										}
										
										//取出之后,取出座席号码及状态
										if(!BlankUtils.isBlank(agentInfo) && !BlankUtils.isBlank(stateInfo)) {   //两者都不为空时，才能取出座席号码及状态
											
											String agentNumber = agentInfo.split("/")[1];
											String agentState = stateInfo.split(":")[1];
											
											//System.out.println("座席号码为:" + agentNumber + ",状态为:" + agentState);
											MemoryVariableUtil.agentStateMap.put(agentNumber,agentState);
											
										}
										
									}
								}
							}
							
						}
						
					});
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			
			
			
			if(i > 10) {  i = 0; }else {  i++;  }    //10次后 初始化 i 的值 
			
			try {
				Thread.sleep(3 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
} 

