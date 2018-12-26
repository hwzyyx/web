package com.callke8.predialqueueforautocallbyquartz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.DefaultManagerConnection;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.event.DialEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;

public class AutoCallHangupCauseMonitorJob implements ManagerEventListener,Job {

	//创建一个 Map 用于储存 uniqueId 与 telId 的键值对
	public static Map<String,String> uniqueIdAndTelIdMap = new HashMap<String,String>();
	public static int uniqueIdAndTelIdMapCount = 0;                                    //当前的uniqueIdAndTelIdMap数量
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		ManagerConnection conn = new DefaultManagerConnection("192.168.143.2",5038, "admin", "vvopadmin");
		conn.addEventListener(this);
		int i = 0;
		
		while(true) {
			if(i % 10 == 0) {
				i = 0;
			}
			String state = conn.getState().toString();
			if(BlankUtils.isBlank(state) || !state.equalsIgnoreCase("CONNECTED")) {
				StringUtil.log(this, DateFormatUtils.getCurrentDate() + "\tAsterisk  挂机事件监控守护程序 正在运行,但 Asterisk 服务器连接状态异常：状态为" + state + ",系统将重新连接!");
				try {
					if(state.equalsIgnoreCase("RECONNECTING")) {
						conn.logoff();
					}
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
			}else {
				if(i%2 == 0) {
					StringUtil.log(this, DateFormatUtils.getCurrentDate() + "\tAsterisk 挂机事件监控守护程序 正在运行 ,运行状态良好....!当前 uniqueIdAndTelIdMap 的数量为：" + uniqueIdAndTelIdMapCount);
				}
			}
			
			try {
				i++;
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	@Override
	public void onManagerEvent(ManagerEvent event) {
		/**
		 DialEvent [
			subEvent=Begin, 
			channel=SIP/Trunk-1431-0005fde8, 
			destination=SIP/IMSTrunk-3056-0005fde9, 
			callerIdNum=008651988130008, 
			callerIdName=hwz123, 
			uniqueId=1545758259.589015, 
			destUniqueId=1545758259.589016, 
			dialString=IMSTrunk-3056/013512771995, 
			dialStatus=null, 
			connectedLineNum=null, connectedLineName=null
		]
		 */
		if(event instanceof DialEvent) {
			DialEvent de = (DialEvent)event;
			
			String destination = de.getDestination();
			String dialString = de.getDialString();
			String callerIdName = de.getCallerIdName();      //通过我们系统自动外呼时，这个参数值我们将会强制设置成 telId-值，如  telId-123
			String uniqueId = de.getUniqueId();
			
			//四个变量均不为空时,才有判断的基础
			if(!BlankUtils.isBlank(destination) && !BlankUtils.isBlank(dialString) && !BlankUtils.isBlank(callerIdName) && !BlankUtils.isBlank(uniqueId)) {
				StringUtil.log(this, DateFormatUtils.getCurrentDate() + "\t系统获取到一个 DialEvent,destination:" + destination + ",dialString:" + dialString + ",callerIdName:" + callerIdName + ",uniqueId:" + uniqueId);
				if(destination.contains("IMSTrunk") && dialString.contains("IMSTrunk")) {     //当目标和目标字符串都包含 IMSTrunk时
					if(callerIdName.startsWith("telId-")) {
						//这样我们可以直接拆分，以 - 分隔，进行拆分
						String[] strs = callerIdName.split("-");
						String telId = null;
						if(strs.length>1) {
							telId = strs[1];
						}
						if(!BlankUtils.isBlank(telId)) {
							uniqueIdAndTelIdMap.put(uniqueId, telId);
							uniqueIdAndTelIdMapCount++;
						}
					}
				}
			}
		}else if(event instanceof HangupEvent) {
			
			HangupEvent he = (HangupEvent)event;
			/**
			这样的挂机事件，才是我们需要的
			（1）channel 要包括 IMSTrunk 表示这个是通过 IMS 服务器返回的
			（2）linkedId 指向我们在 DialEvent 中得到的 uniqueid 的值
			
			监测到挂机事件：channel:SIP/IMSTrunk-3056-0005fde9,callerIdNum:86626735,callerIdName:null,uniqueId:1545758259.589016,hangupCause:16,hangupCauseTxt:Normal Clearing
			挂机字符串：HangupEvent [cause=16, causeTxt=Normal Clearing, language=en, linkedId=1545758259.589015, accountCode=]
			*/
			String channel = he.getChannel();
			String linkeId = he.getLinkedId();
			Integer hangupCause = he.getCause();
			String hangupCauseTxt = he.getCauseTxt();
			
			//均不为空时，才有判断的必要
			if(!BlankUtils.isBlank(channel) && !BlankUtils.isBlank(linkeId) && !BlankUtils.isBlank(hangupCause) && !BlankUtils.isBlank(hangupCauseTxt)) {
				if(channel.contains("IMSTrunk")) {    //只有当 channel 包括 IMSTrunk 才有效，表示这是从 IMS 服务器返回的事件
					boolean isContain = uniqueIdAndTelIdMap.containsKey(linkeId);    //判断之前
					if(isContain) {
						String telId = uniqueIdAndTelIdMap.get(linkeId);
						uniqueIdAndTelIdMap.remove(linkeId);
						if(uniqueIdAndTelIdMapCount>0) {
							uniqueIdAndTelIdMapCount--;
						}
						if(!BlankUtils.isBlank(telId)) {    //如果取出来的 telId 不为空时,系统将保存挂机状态
							
							StringUtil.log(this, DateFormatUtils.getCurrentDate() + "\t系统获取到挂机事件,对应的telId为:" + telId + ",channel:" + channel + ",hangupCause:" + hangupCause + ",hangupCauseTxt:" + hangupCauseTxt);
							
							AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneHangupCause(Integer.valueOf(telId), String.valueOf(hangupCause));
						}
					}
				}
			}
			
		}
	}

}
