package com.callke8.astutils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.event.BridgeEvent;
import org.asteriskjava.manager.event.DialEvent;
import org.asteriskjava.manager.event.DndStateEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;

import com.callke8.call.incoming.InComing;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;


/**
 * AsteriskMonitor 主要是用于监控 asterisk 的事件，用于分析来电信息用于前端来电弹屏功能
 * 
 * @author Administrator
 */
public class AstMonitor implements Runnable,ManagerEventListener {

	private ManagerConnection conn;
	
	private Log log = LogFactory.getLog(AstMonitor.class);
	
	private String state;     //Asterisk 连接状态
	private static Map<String,InComing> inComingMap = new HashMap<String,InComing>();     //当客户来电接通时，存入该变量，用于前端扫描并弹屏用
	
	public AstMonitor() {
		
		conn = AsteriskUtils.connPool.getConnection();
		conn.addEventListener(this);
		
	}
	
	@Override
	public void run() {
		
		int i = 1;
		
		while(true) {
			
			state = BlankUtils.isBlank(conn)?null:conn.getState().toString();   //得到连接的状态
			log.info("第 " + i + " 次检测 Asterisk连接状态，连接状态为: " +  state);
			
			if(BlankUtils.isBlank(state) || !state.equalsIgnoreCase("CONNECTED")) {   //如果连接状态为无连接时
				try {
					if(state.equalsIgnoreCase("RECONNECTING")) {   //如果状态为 RECONNECTING 时，需要先 logoff ，然后再重新连接
						conn.logoff();
					};
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
			
			if(i == 10) { i = 1;} else { i++;};
			try {
				Thread.sleep(60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 事件探测方法
	 */
	@Override
	public void onManagerEvent(ManagerEvent event) {
		
		/**
		 * 监控 Dial事件，得到的 DialEvent 的监控数据如下：
		 * 
		 org.asteriskjava.manager.event.DialEvent[
				dateReceived='Wed Sep 02 10:26:34 CST 2015',
				privilege='call,all',
				subevent='Begin',
				callerid='8004',
				dialstatus=null,
				sequencenumber=null,
				destuniqueid='1441105774.15',
				srcuniqueid='1441105771.14',
				dialstring='8003',
				destination='SIP/8003-0000000f',    目标通道也可能为空
				timestamp=null,calleridname='8004',
				uniqueid='1441105771.14',
				server=null,src='SIP/8004-0000000e',
				calleridnum='8004',
				channel='SIP/8004-0000000e',
				systemHashcode=20306499
		 ]
		 * 
		 */
		if(event instanceof DialEvent){
			
			DialEvent dialEvent = (DialEvent)event;
			
			
			Record inComing = new Record();
			
			String agentNumber = null;                                //座席号码
			String client = null;                                     //客户号码
			String channel = null;		  							  //通道ID
			String callDate = DateFormatUtils.getCurrentDate();		  //来电时间
			
			//先取出被叫通道，通过被叫通道分析来电信息
			String dest = dialEvent.getDestination();       // 得到的 dest 样例数据格式： SIP/8002-0000000c4, 我们要的被叫号码即是 8002
			
			if(!BlankUtils.isBlank(dest) && StringUtil.containsAny(dest, "SIP")) {     //判断被叫通道，是否含有 SIP 关键字
				
				log.info("监控到一个 dial 事件" + "  dialEvent:  " + dialEvent);
				
				int beginPosition = dest.indexOf("/");
				int endPosition = dest.indexOf("-");
				
				if(beginPosition != -1 && endPosition != -1) {     //主要是快速将座席号码取出
					agentNumber = dest.substring(beginPosition + 1, endPosition);        //得到座席号码
				}
				
				client = dialEvent.getCallerIdNum();               //得到客户号码
				channel = dialEvent.getChannel();                  //得到通道
				
				log.info("监控到的来电信息,座席号码：" + agentNumber + ",客户号码: " + client + ",通道名称为: " + channel);
				
				log.info("接下来需要对客户号码进行预处理...");
				
				//如果客户号码非空且不等于 unKnown时，需要对客户号码进行预处理（注：可能运营商会将来电号码加入特定前缀，如 9,等等）
				if(!BlankUtils.isBlank(client) && !client.equalsIgnoreCase("unKnown")) {       
					
					if(client.startsWith("9")) {     //去掉以9开头的号码
						log.info("号码" + client + "是以9开头，系统准备将前缀9去除");
						client = client.substring(1, client.length());
						log.info("将前缀9去除后号码为" + client);
					}
					
					//有些号码是以 010018631188298 开头，需要做一个配置(主要是石家庄保利的楼盘)
					if(client != null && client.startsWith("0100") && client.length()>=15) {
						log.info("号码" + client + "是以0100开头，系统准备将前缀0100去除");
						client = client.substring(3, client.length());
						log.info("将前缀0100去除后号码为" + client);
					}
					
					//以0开始的，可能是 手机号码，或是座机号码，座机号码不用管，但是01开始的，有可能是 010 ， 所以要判断010的情况
					if(client.startsWith("01")) {
						if(!client.startsWith("010")) {
							log.info("号码" + client + "是以01开头，系统准备将前缀0去除");
							client = client.substring(1, client.length());
							log.info("将前缀0去除后号码为" + client);
						}
					}
					
				}
				
				inComing.set("AGENT", agentNumber);
				inComing.set("CLIENT",client);
				inComing.set("CHANNEL",channel);
				inComing.set("STATUS","0");
				inComing.set("UNIQUEID",dialEvent.getUniqueId());
				inComing.set("CALLDATE",callDate);
				inComing.set("PROVINCE","");
				inComing.set("ARCHIVE","0");     //是否已经弹屏
				
				
				
				int inComingId = InComing.dao.add(inComing);
				
				StringBuilder sb = new StringBuilder();
				sb.append("座席号码：" + agentNumber);
				sb.append(",客户号码: " + client);
				sb.append(",通道名称：" + channel);
				sb.append(",通道标识：" + dialEvent.getUniqueId());
				sb.append(",来电时间: " + callDate);
				sb.append(",归属地：null" );
				sb.append(",状态为：0" );
				
				log.info("来电事件分析的结果 为:" + sb.toString());
				
				if(inComingId > 0) {             //如果插入到 incoming 的记录返回的Id大于0，则表示插入来电数据成功
					log.info("来电信息已经被插入到 inComing 表中，返回的ID为: " + inComingId);
				}else{
					log.info("来电信息插入 InComing 数据表失败！");
				}
				
			}
			
			
		}else if(event instanceof BridgeEvent) {          //当来电已经被接通时或是被挂机时，系统都会返回 桥接事件，只是桥接的状态不一样，分别是 link  和 unlink 两种
			BridgeEvent bridgeEvent = (BridgeEvent)event;
			
			String bridgeState = bridgeEvent.getBridgeState();     //得到桥接状态 ,  状态为 link 和 unlink 两种，我们要得到的就是 link 状态
			
			if(!BlankUtils.isBlank(bridgeState) && bridgeState.equalsIgnoreCase("link")) {     //只有桥接状态不为空且为 link 时，才是真正的接通事件
				
				//首先要注意，客户通过呼叫队列到座席时，可能不止返回一个桥接状态为link的桥接事件
				//这时，我们就要根据通过查找 uniqueid1 的通道标识符进行确认，当返回桥接事件时，得到标识符，再去 incoming 表查找是否有符合的未接通的标识符并返回该 incoming 信息
				
				String uniqueId = bridgeEvent.getUniqueId1();     //先得到标识符,再去 incoming 查找与之相同标识符的未接来电记录
				
				InComing inComing = InComing.dao.getIncomingByUniqueId(uniqueId);      //根据 uniqueId 查找未接听的记录
				
				if(!BlankUtils.isBlank(inComing)) {    //如果存在这样的记录，则表示该桥接事件可用于弹屏
					
					//log.info("监控到一个有效的桥接事件：" + bridgeEvent);   这时就可以去修改inComing表中该记录的状态，将其由 0 修改为 1,即来电被接听                                 
					
					boolean b = InComing.dao.updateStatusByUnqueIdWhereChannelBeAnswer(1, uniqueId);  
					
					if(b) {                                                   //如果状态被修改成功，则可以打印出来被接听的信息
						
						log.info("由于来电客户号码：" + inComing.getStr("CLIENT") + " 已经被座席 " + inComing.getStr("AGENT") + " 接听，通道标识为 " + uniqueId + " 记录被修改为 1， 即已经接听!");
						
						//MessageUtils.sendMsg(inComing.getStr("AGENT"), "客户号码：" + inComing.getStr("CLIENT") + " 来电，座席："  + inComing.getStr("AGENT"));
						
						//修改完了之后，需要同时将该接通信息发送到变量 inComingCall 中，用于被前端扫描并弹屏
						//同时要将来电接通信息发送到变量 inComingMap 之前，还需要先判断之前是否因为系统的原因已经存在Key为座席号码的记录，如果有则需要先移除
						if(inComingMap.containsKey(inComing.getStr("AGENT"))) {
							inComingMap.remove(inComing.getStr("AGENT"));
						}
						inComingMap.put(inComing.getStr("AGENT"), inComing);
						
					}else {                                                   //如果修改状态失败，需要提示错误信息
						log.info("系统监控到一个有用桥接事件：" + bridgeEvent);
						log.info("修改来电信息状态失败，来电号码：" + inComing.getStr("CLINET") + ",座席号码:" + inComing.getStr("AGENT") + ",通道标识符：" + inComing.getStr("UNIQUEID"));
					}
					
				}
				
			}
			
		} else if(event instanceof HangupEvent) {               //挂机事件
			
			HangupEvent hangupEvent = (HangupEvent) event;
			
			String uniqueId = hangupEvent.getUniqueId();        //得到挂机件的标识符
			
			InComing inComing = InComing.dao.getIncomingByUniqueId(uniqueId);   //在 inComing 表中查找是否存在相关记录
			
			if(!BlankUtils.isBlank(inComing)) {                //如果存在状态为新建或是被接听后，再挂机的记录则需要修改其状态
				
				//在修改之前，先要确定原状态：如果原状态为0，即是未接而被挂机的，则需要修改为3,即是超时或是主叫主动挂断；如果原状态为1,即是已经被接听后再被挂断的，需要修改为 2, 即是正常 hangup 动作
				//先取得原状态
				String oldStatus = inComing.getStr("STATUS");
				
				if(!BlankUtils.isBlank(oldStatus)) {
					
					String newStatus = oldStatus.equals("1")?"2":"3";
					
					log.info("由于来电:" + inComing.getStr("CLIENT") + " 已经被挂断，通道标识：" + inComing.getStr("UNIQUEID") + " 状态被修改为 " + newStatus + "!");
					
					//当通道被挂断时，更改来电记录的状态
					boolean b = inComing.dao.updateStatusByUnqueIdWhereChannelBeHangup(newStatus, uniqueId);
					
					if(b) {                   //如果来电没有被弹屏，则需要将当前座席的记录从 inComingMap 移除，以免来电被挂断，但是弹屏还在继续
						
						boolean b1 = inComingMap.containsKey(inComing.getStr("AGENT"));    //
						
						if(b1) {             //如果存在时，则需要移除
							inComingMap.remove(inComing.getStr("AGENT"));   
						}
						
					}
					
				}
				
			}
			
		}else if(event instanceof DndStateEvent) {
			
			DndStateEvent dndEvent = (DndStateEvent)event;
			
			System.out.println("获取到座席状态的DND变化 : " + dndEvent);
			
		}
		
	}

	
	/**
	 * 根据座席号，取出是否有可用于弹屏的记录
	 * @param agentNumber
	 * 				座席号码
	 * @return
	 */
	public static InComing getInComingByAgent(String agentNumber) {
		
		boolean b = inComingMap.containsKey(agentNumber);
		if(!b) {              //如果不存在座席号的来电弹屏记录，则返回空       
			return null;
		}
		
		//如果存在，则取出记录，并要先检查通道是否已经被挂断，如果被挂断，也表示无需做弹屏了
		InComing inComing = inComingMap.get(agentNumber);  
		
		//取出后，就要马上从 inComingMap 移除,避免重复弹屏
		inComingMap.remove(agentNumber);
		
		String channel = inComing.getStr("CHANNEL");    //取出通道名称
		//检查通道是否还在通话中
		boolean exist = CtiUtils.isExistChannel(channel);
		
		if(exist) {               //如果通道还在通话中，则可以返回并进行弹屏
			return inComing;
		}else {
			return null;
		}
	}

	
}











