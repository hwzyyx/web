package com.callke8.astutils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.OriginateCallback;
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
 * Asterisk工具类,直接调用 asterisk接口，实现相应的功能： 示忙、示闲、呼叫转移、外呼、挂机等等
 * 
 * @author <a href="mailto:120077407@qq.com">黄文周</a>
 *
 */
public class AsteriskUtils {
	
	/**
	 * 定义静态Asterisk连接池，项目启动时，执行连接池初始化
	 */
	public static AsteriskConnectionPool connPool;
	
	/**
	 * 连接柄
	 */
	private ManagerConnection conn;
	
	public AsteriskUtils() {
		
		conn = connPool.getConnection();
		
	}
	
	/**
	 * 执行外呼,接通后转到 Extension
	 * 
	 * @param channel
	 * @param context
	 * @param exten
	 * @param priority
	 * @param timeOut
	 * @param callerId
	 * @param variable
	 * @param cb
	 */
	public void doCallOutToExtension(String channel,
			String context,
			String exten,
			int priority,
			long timeout,
			CallerId callerId,
			Map<String,String> variables,
			OriginateCallback cb) {
		
		DefaultAsteriskServer server = new DefaultAsteriskServer(conn);
		
		server.originateToExtensionAsync(channel, context, exten, priority, timeout, callerId, variables, cb);
		
		try {
			Thread.sleep(timeout);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			close(); 			//将连接放回连接池中
		}
		
	}
	
	/**
	 * 执行外呼，呼通后转到 application 
	 * 
	 * @param channel
	 * @param application
	 * @param data
	 * @param timeout
	 * @param callerId
	 * @param variable
	 * @param db
	 */
	public void doCallOutToApplication(String channel,
			String application,
			String data,
			long timeout,
			CallerId callerId,
			Map<String,String> variables,
			OriginateCallback cb) {
		
		DefaultAsteriskServer server = new DefaultAsteriskServer(conn);
		
		server.originateToApplicationAsync(channel, application, data, timeout, callerId, variables, cb);
		
		try {
			Thread.sleep(timeout);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			close();    //将连接放回连接池中
		}
		
	}
	
	/**
	 * 执行呼叫转移
	 * 
	 * @param dstChannel
	 * @param forwardNumber
	 */
	public void doTransfer(String dstChannel,String forwardNumber) {
		
		RedirectAction action = new RedirectAction(dstChannel,AsteriskConfig.getAstCallOutContext(),forwardNumber,1);
		
		try {
			
			ManagerResponse response = conn.sendAction(action, 3000);
			
			System.out.println("执行呼叫转移返回 Response: " + response);
			
			try {
				Thread.sleep(3000);
			}catch(InterruptedException ie) {
				ie.printStackTrace();
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
	 * 根据座席号码，取得当前座席通话的通道名称
	 * 
	 * @param agentNumber
	 * 			座席号码
	 * @return
	 * 			如果座席在通话中时，返回通道名称，否则返回空值
	 */
	public String getChannelByAgentNumber(String agentNumber) {
		
		String srcChannel = null;
		
		Map<String,String> channelMap = getSrcChannelAndDstChannelByAgentNumber(agentNumber);
		
		if(!BlankUtils.isBlank(channelMap)) {
			srcChannel = channelMap.get("srcChannel");
		}
		
		return srcChannel;
		
	}
	
	
	/**
	 * 通话保持
	 * 
	 * @param srcChannel
	 * 			源通道
	 * @param dstChannel
	 * 			目标通道
	 */
	public void doPark(String srcChannel,String dstChannel) {
		
		try {
			
			ParkAction action  = new ParkAction(dstChannel,srcChannel,24 * 60 * 60 * 1000);
			
			ManagerResponse response = conn.sendAction(action);
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally {
				connPool.close(conn);
			}
			
		}catch (IllegalArgumentException e) {
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
	 * 取消通话保持
	 * 
	 * @param agentNumber
	 * @param dstChannel
	 */
	public void doBackPark(String agentNumber,String dstChannel) {
		
		try {
			
			OriginateAction action = new OriginateAction();
			
			action.setActionId("originateAction " + agentNumber + " actionId ");
			
			action.setChannel("SIP/" + agentNumber);
			
			action.setApplication("Bridge");
			
			action.setData(dstChannel);
			
			action.setAsync(true);
			
			ManagerResponse response = conn.sendAction(action);
			
			
		}catch (IllegalArgumentException e) {
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
	 * 根据座席号码取得目标通道
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
	
	/**
	 * 根据坐席号码取得源通道
	 * 
	 * @param agentNumber
	 * @return
	 */
	public String getSrcChannelByAgentNumber(String agentNumber) {
		
		String srcChannel = null;
		
		Map<String,String> channelMap = getSrcChannelAndDstChannelByAgentNumber(agentNumber);
		
		if(!BlankUtils.isBlank(channelMap)) {
			srcChannel = channelMap.get("srcChannel");
		}
		
		return srcChannel;
	}
	
	/**
	 * 取得通话中座席号码源通道及目标通道
	 * 
	 * 主要是用于通话保持及取消通话保持
	 * 
	 * @param agentNumber
	 * 				座席号码
	 * @return
	 */
	public Map<String,String> getSrcChannelAndDstChannelByAgentNumber(String agentNumber) {
		
		Map<String,String> channelMap = new HashMap<String,String>();
		
		try {
			CommandAction action = new CommandAction("core show channels concise");
		
			ManagerResponse response = conn.sendAction(action);
			
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
	 * 检查通道是否存在
	 * 
	 * @param channel
	 * @return
	 */
	public boolean isExistChannel(String channel) {
		
		boolean b = false;
		
		try{
			
			CommandAction action = new CommandAction();
			
			action.setCommand("core show channels concise");
			
			ManagerResponse response = conn.sendAction(action);
			
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
			
			connPool.close(conn);
			
		}catch (IllegalArgumentException e) {
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
	 * 根据传入的通道名称，挂断当前通话
	 * 
	 * @param channelName
	 */
	public void hangupByChannel(String channelName) {
		
		
		try {
			
			HangupAction action = new HangupAction(channelName);
			
			conn.sendAction(action,500);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally {
				connPool.close(conn);
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
	 * 检查座席是否已经处于登录状态
	 * 
	 * @param agentNumber
	 * 			座席号码
	 * @return
	 * 			登录状态：返回true;否则返回 false
	 * 		
	 */
	public boolean isAgentLogin(String agentNumber) {
		
		String hostInfo = "";
		String statusInfo = "";
		boolean rs = false;
		
		try {
			
			CommandAction action = new CommandAction("SIP show peer " + agentNumber);
		
			ManagerResponse response = conn.sendAction(action);
			
			if(!BlankUtils.isBlank(response)) {
				
				CommandResponse res = (CommandResponse)response;
				
				//逐行分析 SIP 信息,主要是分析：状态行的情况：Status:UNKNOWN   或是 Status:OK (210 ms)
				for(String line:res.getResult()) {
					
					if(StringUtil.containsAny(line, "Status")) {
						if(StringUtil.containsAny(line, "OK")) {
							statusInfo = line;
						}
						
					}
					
					if(StringUtil.containsAny(line, "Addr->IP")) { 		 //如果其中一行包含 Addr->IP的字样时，则可以取出登录的IP信息, 一般是这样的信息: Addr->IP:42.81.46.103 Port 3550  或是 Addr->IP 
						
						String hostAndPort = line.split(":")[1];         //以 冒号 分割,得到  192.168.11.119 Port 5060  或是 (Unspecified) Port 0
						String host = hostAndPort.split("Port")[0];      //以 Port 分割，到到  192.168.11.119  或是 (Unspecified)
						
						host = host.trim();
						
						if(StringUtil.isIP(host)) {
							hostInfo = host;
						}else {
							hostInfo = null;
						}
					}
					
					
				}
				
			}
			
			if(!BlankUtils.isBlank(statusInfo) && !BlankUtils.isBlank(hostInfo)) {		//只有两个信息都不为空时，才判断其为已经登录
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
	 * 示忙
	 * 
	 * @param agentNumber
	 */
	public void doDNDOn(String agentNumber) {
		
		DbPutAction action = new DbPutAction("DND",agentNumber,"yes");
		
		try {
			ManagerResponse response = conn.sendAction(action);
		}catch (IllegalArgumentException e) {
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
	 * 根据座席号码,查看当前座席的示忙示闲状态
	 * 
	 * @param agentNumber
	 * @return
	 * 		如果处于示忙状态时，返回YES；如果处于示闲状态，返回NO
	 */
	public String getDNDValue(String agentNumber) {
		
		String agentDNDState = "NO";
		
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
								agentDNDState = "YES";
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
	 * 取得Asterisk连接状态
	 * 
	 * Asterisk连接状态主要有：
	 * 	1.CONNECTED 连接成功
	 * 	2.CONNECTING 连接中
	 * 	3.DISCONNECTING 连接失败
	 * 	4.INITIAL 初始化
	 * 	5.RECONNECTING 重连接中（中断后自动重新连接）
	 * 
	 * @return
	 */
	public String getAsteriskConnectionState() {
		String state = null;
		if(conn != null) {
			state = conn.getState().toString();
		}
		return state;
	}
	
	/**
	 * 检查连接状态是否连接成功
	 * 
	 * @return
	 */
	public boolean isAstConnSuccess() {
		
		boolean isConnected = false;
		
		String connState = getAsteriskConnectionState();
		System.out.println("连接状态为:" + connState);
		if(!BlankUtils.isBlank(connState) && connState.equalsIgnoreCase("CONNECTED")) {
			isConnected = true;
		}
		
		return isConnected;
	}
	
	/**
	 * 关闭asterisk工具类,主要是用于将取出的连接，放回连接池中
	 */
	public void close() {
		connPool.close(conn);
	}
	
}	
