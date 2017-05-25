package com.callke8.astutils;

import java.io.IOException;
import java.util.Map;

import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.OriginateCallback;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.DefaultManagerConnection;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.response.CommandResponse;
import org.asteriskjava.manager.response.ManagerResponse;

import com.callke8.utils.BlankUtils;
import com.callke8.utils.StringUtil;

/**
 * 
 * AsteriskUtils 为通过 AMI 操作 Asterisk 的工具类
 * 所有的 CTI 功能，均由该类直接对asterisk 进行操作， CtiUtils 仅提供接口
 * 
 * @author hwz
 *
 */
public class AsteriskUtils {
	
	ManagerConnection conn = null;
	
	//构造方法，先连接asterisk
	public AsteriskUtils() {
		conn = new DefaultManagerConnection(AstMonitor.getAstHost(),AstMonitor.getAstPort(),AstMonitor.getAstUser(),AstMonitor.getAstPass());
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
	
	/**
	 * 呼出功能
	 * 
	 * @param channel
	 * 			通道名称,如： SIP/8004,  ss7/siuc/13512775995
	 * @param context
	 * 			context:如 from-exten
	 * @param exten
	 * @param priority
	 * @param timeout
	 * @param callerId
	 * @param variables
	 * @param cb
	 */
	public void doCallOut(java.lang.String channel,
            java.lang.String context,
            java.lang.String exten,
            int priority,
            long timeout,
            CallerId callerId,
            java.util.Map<java.lang.String,java.lang.String> variables,
            OriginateCallback cb) {
		
		DefaultAsteriskServer server = new DefaultAsteriskServer(conn);
		//System.out.println("conn.getHostname():" + conn.getHostname());
		//System.out.println("conn.getPassword():" + conn.getPassword());
		//System.out.println("conn.getUsername():" + conn.getUsername());
		//System.out.println("conn.getPort():" + conn.getPort());
		//System.out.println("server.getVersion():" + server.getVersion());
		
		server.originateToExtensionAsync(channel, context, exten, priority, timeout, callerId, variables,cb);
		
		try {
			Thread.sleep(timeout);
			
			server.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public void doCallOutToApplication(String channel,String application,String data,long timeout,CallerId callerId,Map<String,String> variables,OriginateCallback cb) {
		
		DefaultAsteriskServer server = new DefaultAsteriskServer(conn);
		
		server.originateToApplicationAsync(channel, application, data, timeout, callerId, variables, cb);
		
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 执行呼叫转移
	 * 
	 * @param targetNumber
	 * 			转移到目标号码
	 * @return
	 */
	public boolean doTransfer(String targetNumber) {
		
		boolean b = false;
		
		
		
		return b;
	}
	
	/**
	 * 检查通道是否存在
	 * 
	 * @param channel
	 * @return
	 */
	public boolean isExistChannel(String channel) {
		
		boolean b = false;
		
		try {
			
			CommandAction action = new CommandAction();
			
			action.setCommand("core show channels concise");
		
			ManagerResponse response = conn.sendAction(action,500);
			
			/**
			 * 执行之后，返回的数据如下:
			 * 
			 localhost*CLI> core show channels concise
			SIP/8003-000000ab!macro-dial-one!s!1!Up!AppDial!(Outgoing Line)!8003!!!3!20!Local/8003@from-queue-00000012;2!1441604413.209
			SIP/8004-000000aa!from-internal!401!10!Up!Queue!401,t,,,30!8004!!!3!21!Local/8003@from-queue-00000012;1!1441604412.206
			Local/8003@from-queue-00000012;1!from-queue!401!1!Up!AppQueue!(Outgoing Line)!8003!!!3!21!SIP/8004-000000aa!1441604413.207
			Local/8003@from-queue-00000012;2!macro-dial-one!s!37!Up!Dial!SIP/8003,"",trM(auto-blkvm)!8004!!!3!21!SIP/8003-000000ab!1441604413.208

			 * 
			 * 我们要做的，就是将所有的通道先找出来，然后对比目标通道，看看是否存在相关通道
			 */
			
			if(response != null) {
				
				CommandResponse res = (CommandResponse)response;
				
				for(String line:res.getResult()) {        //遍历数据
					
					String channelInfo = line.split("!")[0];                    //得到 SIP/8011-0000008b
					
					channelInfo = channelInfo.trim();                             //去掉空格
					//System.out.println("channelInfo:" + channelInfo + ",channel:" + channel + "||||||||||||");
					if(channelInfo.equals(channel)) {            //如果存在相同的通道时，设为 true
						b = true;
					}
				}
			}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
		return b;
		
	}
	
	/**
	 * 根据传入的通道名称，挂断该通话
	 * @return
	 */
	public void hangupByChannel(String channelName) {
		
		try {
			
			HangupAction action = new HangupAction(channelName);
			
			conn.sendAction(action,500);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 根据座席号，得到当前座席通话的通道名称
	 * 
	 * @param agentNumber
	 * 			座席号码
	 * @return
	 * 			如果在通话中，则返回通道名称；否则返回空值
	 */
	public String getChannelByAgentNumber (String agentNumber) {
		
		String channelName =  null;
		
		try { //发送指令，分析返回的结果
			
			CommandAction action = new CommandAction();
			
			action.setCommand("core show channels concise");
			
			ManagerResponse response = conn.sendAction(action,500);
			
			/**
			 * 发送 Action 后，返回的数据大概如下：
			 * 
			 localhost*CLI> core show channels concise
				SIP/8002-0000002e!from-exten-sip!!1!Up!AppDial!(Outgoing Line)!8002!!3!9!Local/8002@sub-queuefindnumber-bb88,2
				Local/8002@sub-queuefindnumber-bb88,2!sub-queuefindnumber!8002!5!Up!Dial!sip/8002|40|t!13512771995!!3!9!SIP/8002-0000002e
				Local/8002@sub-queuefindnumber-bb88,1!sub-queuefindnumber!83811599!1!Up!AppQueue!(Outgoing Line)!13512771995!0!3!9!DAHDI/1-1
				DAHDI/1-1!from-trunk-dahdi!83811599!11!Up!Queue!401|t|||100|agi://127.0.0.1/queue_answeragent?saymember=0!13512771995!0!3!19!Local/8002@sub-queuefindnumber-bb88,1
			 
			 * 我们要找的，就是与agentNumber对应的正在通话的通道,而我们要得到的就是 SIP/8002-0000002e通道号码
			 * 
			 */
			
			if(!BlankUtils.isBlank(response)) {
				
				CommandResponse res = (CommandResponse)response;
				
				res.getAttributes();
				
				String searchStr = "SIP/" + agentNumber;
				
				for(String line:res.getResult()) {
					
					if(line.startsWith(searchStr)) {     //如果判断返回的结果中是以 SIP/8002 开头, 即可得到通道号
						
						String[] elements = line.split("!");
						
						channelName = elements[0];
						
					}
					
				}
				
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
		return channelName;
	}
	
	/**
	 * 判断座席是否已经登录
	 * 
	 * @param agentNumber
	 * 			座席号码
	 * @return
	 * 			如果已经登录，返回 true; 如果离线状态，返回 false
	 */
	public boolean isLogined(String agentNumber) {
		
		String hostInfo = "";
		String statusInfo = "";
		boolean rs = false;
		
		try {
			
			CommandAction action = new CommandAction("SIP show peer " + agentNumber);
			
			ManagerResponse response = conn.sendAction(action,500);
			
			if(!BlankUtils.isBlank(response)) {
				
				CommandResponse res = (CommandResponse)response;
				
				for(String line:res.getResult()) {      //逐行分析 SIP 的信息
					
					if(StringUtil.containsAny(line, "Status")) {     		//如果其中一行包含为 Status 的字样时，则可以取出状态行的情况：Status       : UNKNOWN   或是 Status       : OK (210 ms)
						statusInfo = line;
						if(StringUtil.containsAny(statusInfo, "OK")) {      //如果其状态还包括 OK 字样时，表示登录状态为已经登录
							statusInfo = line;
						}else {
							statusInfo = null;                              //将其置为空
						}
					}
					
					if(StringUtil.containsAny(line, "Addr->IP")) {      //如果其中一行包含 Addr->IP的字样时，则可以取出登录的IP信息, 一般是这样的信息: Addr->IP:42.81.46.103 Port 3550  或是 Addr->IP     : (Unspecified) Port 0
						String hostAndPort = line.split(":")[1];		//以 冒号 分割,得到  192.168.11.119 Port 5060  或是 (Unspecified) Port 0
						String host = hostAndPort.split("Port")[0];     //以 Port 分割，到到  192.168.11.119  或是 (Unspecified)
						
						host = host.trim();
						
						if(StringUtil.isIP(host)) {
							hostInfo = host;
						}else {
							hostInfo = null;
						}
						
					}
					
				}
				
				
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			if(!BlankUtils.isBlank(statusInfo) && !BlankUtils.isBlank(hostInfo)) {    //只有两个信息都不为空时，才判断其为已经登录
				rs = true;
			}
			
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
		return rs;
		
	}
	
	/**
	 * 得到连接的状态
	 * 
	 * @return
	 * 		如果连接状态不为空且状态为 CONNECTED 时，返回真；否则返回 FALSE
	 */
	public boolean getConnectionState() {
		
		String state = conn.getState().toString();
		
		if(!BlankUtils.isBlank(state)&&state.equalsIgnoreCase("CONNECTED")) {
			return true;
		}
		
		return false;
	}
	
	public void logoff() {
		conn.logoff();
	}
	
}
