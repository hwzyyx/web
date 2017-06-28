package com.callke8.astutils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.OriginateCallback;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.DefaultManagerConnection;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.DbDelAction;
import org.asteriskjava.manager.action.DbPutAction;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.action.ParkAction;
import org.asteriskjava.manager.action.RedirectAction;
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
	public void doTransfer(String dstChannel,String forwardNumber) {
		
		//AtxferAction action  = new AtxferAction(dstChannel, AstMonitor.getAstCallOutContext(),forwardNumber, 1);
		RedirectAction action  = new RedirectAction(dstChannel,AstMonitor.getAstCallOutContext(),forwardNumber,1);
		
		try {
			ManagerResponse response = conn.sendAction(action,3000);
			
			System.out.println("执行转移返回的 Response: " + response);
			
			try {
				Thread.sleep(3000);
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
		}finally {
			conn.logoff();
		}
		
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
	 * 针对通话中的座席号码,取得源通道及目标通道
	 * 
	 * 主要是用于通话保持及取消通话保持
	 * 
	 * @param agentNumber
	 * @return
	 */
	public Map<String,String> getSrcChannelAndDstChannelByAgentNumber(String agentNumber) {
		
		Map<String,String> channelMap = new HashMap<String,String>();
		
		try {
			
			CommandAction action  = new CommandAction();
			
			action.setCommand("core show channels concise");
			
			ManagerResponse response = conn.sendAction(action,500);
			
			/**
			 * 发送 Action 后，返回的数据大概如下：
			 * 
			 localhost*CLI> core show channels concise
			 	
			 	（1）座席直接对呼的情况
			 	SIP/8003-0000002f!from-internal!!1!Up!AppDial!(Outgoing Line)!8003!!!3!24!SIP/8004-0000002e!1497344055.47
				SIP/8004-0000002e!macro-dial-one!s!37!Up!Dial!SIP/8003,"",tr!8004!!!3!24!SIP/8003-0000002f!1497344054.46
				
				（2）座席呼叫外线号码时
				SIP/JM-Trunk-00000031!from-trunk-sip-JM-Trunk!!1!Up!AppDial!(Outgoing Line)!013512771995!!!3!22!SIP/8004-00000030!1497344139.49
				SIP/8004-00000030!macro-dialout-trunk!s!19!Up!Dial!SIP/JM-Trunk/013512771995,300,!3282114!!!3!22!SIP/JM-Trunk-00000031!1497344139.48
			 
			 	（3）外线来电通过拨打队列号分配给座席接听的情况
				SIP/8002-0000002e!from-exten-sip!!1!Up!AppDial!(Outgoing Line)!8002!!3!9!Local/8002@sub-queuefindnumber-bb88,2
				Local/8002@sub-queuefindnumber-bb88,2!sub-queuefindnumber!8002!5!Up!Dial!sip/8002|40|t!13512771995!!3!9!SIP/8002-0000002e
				Local/8002@sub-queuefindnumber-bb88,1!sub-queuefindnumber!83811599!1!Up!AppQueue!(Outgoing Line)!13512771995!0!3!9!DAHDI/1-1
				DAHDI/1-1!from-trunk-dahdi!83811599!11!Up!Queue!401|t|||100|agi://127.0.0.1/queue_answeragent?saymember=0!13512771995!0!3!19!Local/8002@sub-queuefindnumber-bb88,1
				
				（4）座席通过拨打队列号分配给其他座席接听的情况
				SIP/8003-00000008!from-internal!401!9!Up!Queue!401,t,,!8003!!!3!22!Local/8004@from-queue-00000000;1!1497598151.11
				SIP/8004-00000009!macro-dial-one!s!1!Up!AppDial!(Outgoing Line)!8004!!!3!21!Local/8004@from-queue-00000000;2!1497598152.14
				Local/8004@from-queue-00000000;1!from-queue!401!1!Up!AppQueue!(Outgoing Line)!8004!!!3!21!SIP/8003-00000008!1497598152.12
				Local/8004@from-queue-00000000;2!macro-dial-one!s!37!Up!Dial!SIP/8004,"",trM(auto-blkvm)!8003!!!3!21!SIP/8004-00000009!1497598152.13
			 
			 * 我们要找的，就是根据上面三种情况，分别进行分析：
			 * 
			 * 第一种情况（座席对呼）：以  SIP/座席号码  开头进行分析，得到源通道和目标通道
			 * 第二种情况（座席外呼）：以 SIP/座席号码  开头进行分析,目标通道为  SIP/JM-Trunk-00000031
			 * 第三种情况（队列号）：座席主要是以 Local/座席号码 得到目标通道，这里要注意，目标通道可能会得到相同座席的，需要进行筛选
			 * 第四种情况（队列号2）：座席
			 * 
			 * 综上所述：
			 * 必须即使得到源通道和目标通道，还需要通过通道取出座席号码，查看是否相同，只有不同时，才算是真正找到了两个通道。
			 * 
			 */
			
			if(!BlankUtils.isBlank(response)) {
				
				CommandResponse res = (CommandResponse)response;
				
				String searchStr = "SIP/" + agentNumber;
				String searchStrForLocal = "Local/" + agentNumber;
				
				//遍历返回的结果
				for(String line:res.getResult()) {
					
					if(line.startsWith(searchStr)) {      //如果判断返回的结果中是以 SIP/8002 开头
						
						String[] elements = line.split("!");
						
						if(elements.length == 14) {
							
							channelMap = new HashMap<String,String>();
							
							String srcChannel = elements[0];
							String dstChannel = elements[12];
							
							//得到了源通道及目标通道,针对以上四种情况
							//源通道为：SIP/8003-0000002f
							//目标通道可能为：
							/*
							 * （1）SIP/8003-0000002e
							 * （2）SIP/JM-Trunk-00000031
							 * （3）Local/8002@sub-queuefindnumber-bb88
							 * （4）Local/8002@from-queue-00000000
							 */
							
							//主要对目标通道进行判断，看看目标通道是否是以     SIP/座席   或  Local/座席 ,如果是，表示目标通道是不对的
							if(!BlankUtils.isBlank(srcChannel) && !BlankUtils.isBlank(dstChannel)) {
								
								//为了避免因版本不同,返回目标通道不在第13位,先要判断目标通道包含 /
								if(dstChannel.contains("/")) {
									
									//以 "/" 分解目标通道,分解目标通道后,查看是否以座席号开始
									if(!dstChannel.split("/")[1].startsWith(agentNumber)) {
										channelMap.put("srcChannel", srcChannel);
										channelMap.put("dstChannel", dstChannel);
										break;
									}
									
								}
								
							}
							
						}
						
					}else if(line.startsWith(searchStrForLocal)) {      //如果判断返回的结果中是以 Local/8002 开头
						
						String[] elements = line.split("!");
						
						if(elements.length == 14) {
							
							channelMap = new HashMap<String,String>();
							
							String srcChannel = elements[0];
							String dstChannel = elements[12];
							
							//得到了源通道及目标通道,针对以上四种情况
							//源通道为：Local/8002@sub-queuefindnumber-bb88
							//目标通道可能为：
							/*
							 * （1）SIP/8002-0000002e
							 * （2）DAHDI/1-1
							 *  (3)SIP/8003-00000008
							 */
							
							//主要对目标通道进行判断，看看目标通道是否是以     SIP/座席   或  Local/座席 ,如果是，表示目标通道是不对的
							if(!BlankUtils.isBlank(srcChannel) && !BlankUtils.isBlank(dstChannel)) {
								
								//为了避免因版本不同,返回目标通道不在第13位,先要判断目标通道包含 /
								if(dstChannel.contains("/")) {
									
									//以 "/" 分解目标通道,分解目标通道后,查看是否以座席号开始
									if(!dstChannel.split("/")[1].startsWith(agentNumber)) {
										channelMap.put("srcChannel", srcChannel);
										channelMap.put("dstChannel", dstChannel);
										break;
									}
									
								}
								
							}
							
							
						}
						
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
		
		
		return channelMap;
		
	}
	
	
	/**
	 * 根据座席号码,取出与座席通话的目标通道，无论是来电或是去电
	 * 
	 * @param agentNumber
	 * @return
	 */
	public String getDstChannelByAgentNumber(String agentNumber) {
		
		String dstChannel = null;
		
		Map<String,String> channelMap = getSrcChannelAndDstChannelByAgentNumber(agentNumber);
		
		if(!BlankUtils.isBlank(channelMap)) {
			
			dstChannel = channelMap.get("dstChannel");
			
		}
		
		return dstChannel;
		
	}
	
	//通话保持
	public void doPark(String srcChannel,String dstChannel) {
		
		try {
		
			ParkAction action = new ParkAction(dstChannel,srcChannel,24 * 60 * 60 * 1000);
			
			ManagerResponse response = conn.sendAction(action);
			
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
	
	//取消保持
	public void doBackPark(String agentNumber,String dstChannel) {
		
		
		try {
			
			OriginateAction action  = new OriginateAction();
			
			action.setActionId("originateAction " + agentNumber + " actionId ");
			
			action.setChannel("SIP/" + agentNumber);
			
			action.setApplication("Bridge");
			
			action.setData(dstChannel);
			
			action.setAsync(true);
			
			ManagerResponse response =  conn.sendAction(action);
			
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
		
		String srcChannel = null;
		
		Map<String,String> channelMap = getSrcChannelAndDstChannelByAgentNumber(agentNumber);
		
		if(!BlankUtils.isBlank(channelMap)) {
			
			srcChannel = channelMap.get("srcChannel");
			
		}
		
		return srcChannel;
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
	 * 根据座席号码,查看当前座席的示忙示闲状态
	 * 
	 * @param agentNumber
	 * @return
	 */
	public boolean getDNDValue(String agentNumber) {
		
		boolean agentDNDState = false;
		
		CommandAction action = new CommandAction("database get DND " + agentNumber);
		
		try {
			
			ManagerResponse response = conn.sendAction(action, 500);
			
			if(response instanceof CommandResponse) {
				
				CommandResponse res = (CommandResponse)response;
				
				List<String> results = res.getResult();
				
				if(!BlankUtils.isBlank(results)) {
					
					for(String line:results) {
						
						if(!BlankUtils.isBlank(line)) {
							
							if(line.contains("Value") && line.contains("yes")) {
								agentDNDState = true;
								break;
							}
							
						}
						
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
		
		
		return agentDNDState;
		
	}
	
	
	/**
	 * 示忙
	 * 
	 * @param agentNumber
	 */
	public void doDNDOn(String agentNumber) {
		
		DbPutAction action = new DbPutAction("DND",agentNumber,"yes");
		
		try {
			ManagerResponse response = conn.sendAction(action);
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
	 * 示闲
	 * 
	 * @param agentNumber
	 */
	public void doDNDOff(String agentNumber) {
		
		DbDelAction action = new DbDelAction("DND",agentNumber);
		
		try {
			ManagerResponse response  = conn.sendAction(action);
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
