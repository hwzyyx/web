package com.callke8.predialqueueforautocallbyquartz;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.system.callerid.SysCallerId;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.AddressReplaceUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.QuartzUtils;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TTSUtils;
import com.callke8.utils.TelephoneNumberLocationUtil;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 自动外呼，执行  TTS转换和归属地定位 的 JOB
 * 
 * 主要是用于 AutoCallLaunchDialJob 守护程序，如果从排队机取得外呼数据后，先转到这个  JOB 
 * 
 * 在这个 JOB 中，执行（1）地址、车牌、车辆类型三个字段的TTS转换   （2）号码归属地查询
 * 
 * @author 黄文周
 *
 */
public class AutoCallDoTtsAndLocationJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			
			JobDataMap map = context.getJobDetail().getJobDataMap();
			String autoCallTaskTelephoneId = map.getString("autoCallTaskTelephoneId");     //取出号码的 id
			
			StringUtil.log(this, "线程 AutoCallDoTtsAndLocationJob[" + context.getScheduler().getSchedulerName() + "]:自动外呼TTS和归属地定位守护线程收到执行请求，传入的autoCallTaskTelephoneId：" + autoCallTaskTelephoneId);
			
			//(1)检查传入的号码ID,是否为空。如果传入的自动外呼任务的号码的ID为空，则关闭该守护，并返回
			if(BlankUtils.isBlank(autoCallTaskTelephoneId)) {
				StringUtil.log(this, "线程 AutoCallDoTtsAndLocationJob[" + context.getScheduler().getSchedulerName() + "]:执行 TTS 和 Location 错误。传入的 autoCallTaskTelephoneId 为空!");
				if(AutoCallPredial.activeChannelCount > 0) {
					AutoCallPredial.activeChannelCount--;
				}
				context.getScheduler().shutdown();
				return;
			}
			
			AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(autoCallTaskTelephoneId);
			//(2)检查任务的号码信息是否为空。如果从数据库中取出的任务号码信息为空，也同样关闭该守护，并返回
			if(BlankUtils.isBlank(actt)) {
				StringUtil.log(this, "线程 AutoCallDoTtsAndLocationJob[" + context.getScheduler().getSchedulerName() + "]:执行 TTS 和 Location 错误。传入的 autoCallTaskTelephoneId：" + autoCallTaskTelephoneId + " 对应的号码信息为空!");
				if(AutoCallPredial.activeChannelCount > 0) {
					AutoCallPredial.activeChannelCount--;
				}
				context.getScheduler().shutdown();
				return;
			}
			
			//（3）检查号码定位情况
			boolean b = checkLocation(actt);     //检查外呼记录的号码归属地情况，如果没有定位，则重新定位，并将定位结果存储到数据库中，如果定位失败，返回 false
			if(!b) {
				StringUtil.log(this, "线程 AutoCallDoTtsAndLocationJob[" + context.getScheduler().getSchedulerName() + "]:执行 Location 错误。系统将返回!");
				if(AutoCallPredial.activeChannelCount > 0) {
					AutoCallPredial.activeChannelCount--;
				}
				context.getScheduler().shutdown();
				return;   
			}
			
			//（4）对于某些催缴类型，需要通过TTS转换某些字段语音
			// 				（A）电费催缴：需要转换地址    （B）水费催缴：需要转换地址   （C)车辆违章：需要转换车牌    （D）交警移车：车牌、车辆类型
			doTts(actt);   //无论是否执行 TTS 成功，都将往下执行。
			
			
			//（5）最后，调用执行外呼的 JOB ，准备执行
			String scheduleNameForCallOut = "AutoCallDoCallOutJob" + String.valueOf(System.currentTimeMillis()) + String.valueOf(Math.round(Math.random()*900000 + 100000));
			Scheduler schedulerForCallOut = QuartzUtils.createScheduler(scheduleNameForCallOut,1);
			JobDetail jobDetail = QuartzUtils.createJobDetail(AutoCallDoCallOutJob.class);
			jobDetail.getJobDataMap().put("autoCallTaskTelephoneId", String.valueOf(actt.getInt("TEL_ID")));    //将ID以参数传入到quartz的执行区
			schedulerForCallOut.scheduleJob(jobDetail, QuartzUtils.createSimpleTrigger(new Date((System.currentTimeMillis() + 1000)), 0, 1));   //执行一次
			schedulerForCallOut.start();
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}finally {
			try {
				context.getScheduler().shutdown();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	/**
	 * TTS语音转换
	 * 
	 * 对于某些催缴类型，需要通过TTS转换某些字段语音
	     电费催缴(1)：需要转换地址   
	      水费催缴(2)：需要转换地址   
	      车辆违章(6)：需要转换车牌    
	      交警移车(7)：车牌、车辆类型
	 * 
	 * @param autoCallTaskTelephone
	 * @return
	 */
	public void doTts(AutoCallTaskTelephone autoCallTaskTelephone) {
		
		int telId = autoCallTaskTelephone.getInt("TEL_ID");				   //取出号码的ID
		
		//先从数据库中取出 telId 对应的最新的记录，因为在将该待呼记录加入排队机后，系统还更改了该记录的几个值：STATE=1（记录状态）,LOAD_TIME=?（外呼时间）,RETRIED=RETRIED+1（已呼次数）,LAST_CALL_RESULT=''
		AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(String.valueOf(telId));
		if(BlankUtils.isBlank(actt)) {    return; }    //如果记录无法查询出来，很可以该记录已经被删除，直接返回 false 即可
		
		String taskId = actt.getStr("TASK_ID");      //取出 taskId
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);    //取出任务
		
		String taskType = autoCallTask.getStr("TASK_TYPE");              //任务类型：1：普通外呼  2：调查问卷外呼   3：催缴外呼
		String reminderType = autoCallTask.getStr("REMINDER_TYPE");      //催缴类型的催缴: 1:电费模板   2：水费模板  3：电话费模板 4：燃气费模板  5：物业费模板  6：车辆违章  7：交警移车  8：社保催缴
		if(BlankUtils.isBlank(taskType) || BlankUtils.isBlank(reminderType)) {    //如果其中一个为空时，直接返回
			return;
		}
		
		if(!taskType.equalsIgnoreCase("3")) {        //如果任务类型不是3，即非催缴外呼时，直接返回 true,不需要转换
			return;
		}else {                                      //如果为催缴类型
			
			List<Record> list = new ArrayList<Record>();    //需要转换的语音列表
			
			if(reminderType.equalsIgnoreCase("1") || reminderType.equalsIgnoreCase("2")) {       //电费或是水费催缴，需要转换地址的 TTS
				String address = actt.getStr("ADDRESS");   //取出地址
				
				if(!BlankUtils.isBlank(address)) {    
					
					//并不是地址不为空时，就一定要转换，在转换前要查看是否已经转换好了，即是先取出对应的语音文件名是否为空，同时要查看语音文件是否存在
					boolean voiceFileExist = checkVoiceExist(actt,"ADDRESS_VOICE_NAME",ParamConfig.paramConfigMap.get("paramType_4_voicePath"));
					if(!voiceFileExist) {
						Record r = new Record();
						r.set("fileName", String.valueOf(DateFormatUtils.getTimeMillis()) + String.valueOf(Math.round(Math.random()*90000 + 10000)));    //定义一个文件名);
						r.set("columnName","ADDRESS_VOICE_NAME");
						r.set("ttsContent", AddressReplaceUtils.replaceAddressContent(address));    //调用地址替换工具类，将请求TTS的地址内容，去替换一些特殊字符，如数字、-
						
						list.add(r);
					}
				}
				
			}else if(reminderType.equalsIgnoreCase("6") || reminderType.equalsIgnoreCase("7")) {      //交通违章或是交警移车
				
				String plateNumber = actt.getStr("PLATE_NUMBER");    //取出车牌信息
				String vehicleType = actt.getStr("VEHICLE_TYPE");    //取出车辆类型
				System.out.println("车牌号码：" + plateNumber + "，车辆类型:" + vehicleType);
				if(!BlankUtils.isBlank(plateNumber)) {                //
					//检查车牌文件是否已经存在，不存在时，才执行转换
					boolean voiceFileExist = checkVoiceExist(actt,"PLATE_NUMBER_VOICE_NAME",ParamConfig.paramConfigMap.get("paramType_4_voicePath"));
					if(!voiceFileExist) {
						Record r = new Record();
						r.set("fileName", String.valueOf(DateFormatUtils.getTimeMillis() + Math.round(Math.random()*9000 + 1000)));    //定义一个文件名);
						r.set("columnName","PLATE_NUMBER_VOICE_NAME");
						r.set("ttsContent", AddressReplaceUtils.replaceAddressContent(plateNumber));   //车牌号码，也需要将数字转换，避免读成数值
						list.add(r);
					}
				}
				
				if(!BlankUtils.isBlank(vehicleType)) {
					boolean voiceFileExist = checkVoiceExist(actt,"VEHICLE_TYPE_VOICE_NAME",ParamConfig.paramConfigMap.get("paramType_4_voicePath"));
					if(!voiceFileExist) {    
						Record r2 = new Record();
						r2.set("fileName", String.valueOf(DateFormatUtils.getTimeMillis() + Math.round(Math.random()*9000 + 1000)));    //定义一个文件名);
						r2.set("columnName","VEHICLE_TYPE_VOICE_NAME");
						r2.set("ttsContent", vehicleType);
						list.add(r2);
					}
				}
				
			}
			
			if(!BlankUtils.isBlank(list) && list.size() > 0) {    //当list 不为空时，才执行 TTS 转换
				
				for(Record r:list) {
					String fileName = r.getStr("fileName");
					String columnName = r.getStr("columnName");
					String ttsContent = r.getStr("ttsContent");
					System.out.println("需要转换的语音：fileName:" + fileName + ",columnName:" + columnName + ",ttsContent:" + ttsContent);
					TTSUtils.doTTS(fileName, ttsContent, ParamConfig.paramConfigMap.get("paramType_4_voicePath"), ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle"));
					
					AutoCallTaskTelephone.dao.setVoiceName(columnName, fileName, telId);
				}
				
			}
			
		}
		
	}
	
	/**
	 * 检查文件是否存在
	 * 
	 * 首先取出语音文件名，看看是否存在，如果不存在，直接返回 false 
	 * 
	 * 如果存在，则需要查看该文件是否存在，如果文件不存在返回 false, 如果文件存在，则返回 true
	 * 
	 * @param actt
	 * @param columnName
	 * @return
	 */
	public static boolean checkVoiceExist(AutoCallTaskTelephone actt,String columnName,String voicePath) {
		
		String voiceName = actt.getStr(columnName);   
		
		if(BlankUtils.isBlank(voiceName)) {
			return false;
		}
		
		String voicePathFullDir = PathKit.getWebRootPath() + File.separator + voicePath + File.separator + voiceName + ".wav";
		
		File f = new File(voicePathFullDir);
		
		if(f.exists()) {
			return true;
		}else {
			return false;
		}
		
	}
	
	
	/**
	 * 检查归属地情况，如果归属地已定位，直接返回 true
	 * 
	 * 如果归属地没有定位，则调用相关接口，将归属地和外呼号码赋值，并返回 true
	 * 
	 * 如果定位失败，或是定位到了，但是更改时失败，则返回
	 * 
	 * @param actt
	 * @return
	 */
	public boolean checkLocation(AutoCallTaskTelephone autoCallTaskTelephone) {
		
		int telId = autoCallTaskTelephone.getInt("TEL_ID");				   //取出号码的ID
		
		//先从数据库中取出 telId 对应的最新的记录，因为在将该待呼记录加入排队机后，系统还更改了该记录的几个值：STATE=1（记录状态）,LOAD_TIME=?（外呼时间）,RETRIED=RETRIED+1（已呼次数）,LAST_CALL_RESULT=''
		AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(String.valueOf(telId));
		if(BlankUtils.isBlank(actt)) {    return false; }    //如果记录无法查询出来，很可以该记录已经被删除，直接返回 false 即可
		
		String callOutTel = actt.getStr("CALLOUT_TEL");      //取出记录的外呼号码，如果外呼号码不为空，则表示该记录肯定已经定位了归属地，直接返回 true
		if(!BlankUtils.isBlank(callOutTel)) {                //如果外呼号码不为空，则直接返回 true
			return true; 
		}else {                                              //如果外呼号码为空，则表示该记录没有定位归属地
			
			String taskId = actt.getStr("TASK_ID");                  //取出任务 ID
			String customerTel = actt.getStr("CUSTOMER_TEL");		 //取出客户号码（上传时的号码）
			int retried = actt.getInt("RETRIED");                    //已外呼次数
			
			AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);     //取出任务的信息
			int retryTimes = autoCallTask.getInt("RETRY_TIMES");                              //任务设置的最大外呼次数
			
			Record customerTelLocation = TelephoneNumberLocationUtil.getLocation(customerTel);    //取得号码归属地
			
			if(!BlankUtils.isBlank(customerTelLocation)) {        //如果客户号码归属地不为空,则将归属地信息插入数据库
				String provinceRs = customerTelLocation.getStr("province");                     //省份
				String cityRs = customerTelLocation.getStr("city");								//城市
				String callOutTelRs = customerTelLocation.getStr("callOutTel");					//外呼号码
				boolean isLocalCity = customerTelLocation.getBoolean("isLocalCity");			//是否为本地号码
				boolean isLandlineNumber = customerTelLocation.getBoolean("isLandlineNumber");	//是否为固定电话号码
				
				StringUtil.log(this, "客户号码：" + customerTel + " 的归属地定位信息为:" + customerTelLocation);
				//再根据任务的信息，取一个主叫号码出来
				String callOutCallerIdRs = SysCallerId.dao.selectCallerId(taskId, autoCallTask.getStr("CALLERID"));    //取出一个主叫号码,对于轮循主叫号码，或是单个号码，都适用
				
				int count = AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneLocationAndCallOutTel(telId, provinceRs, cityRs, callOutTelRs,callOutCallerIdRs);
				//int count = AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneLocationAndCallOutTel(telId, provinceRs, cityRs, callOutTelRs);
				if(count < 0) {    //如果更改号码归属地和外呼号码不成功，则将外呼记录的状态修改为 已失败（或待重呼），原因为：更改归属地异常
					if(retried < retryTimes) {      //如果已经外呼次数小于允许的最大外呼次数，则将记录状态修改为待重呼
						AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneStateToRetry(telId, "3", autoCallTask.getInt("RETRY_INTERVAL"),autoCallTask.getInt("INTERVAL_TYPE"),"4","更改归属地异常");
					}else {                         //否则,修改为已失败
						AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(telId, null, "4", "更改归属地异常");
					}
					return false;
				}
				
				return true;
				
			}else {			//如果客户的归属地无法定位，那么就直接将这条外呼号码的状态，修改为失败（或待重呼），原因定为定位归属地异常
				
				if(retried < retryTimes) {      //如果已经外呼次数小于允许的最大外呼次数，则将记录状态修改为待重呼
					AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneStateToRetry(telId, "3", autoCallTask.getInt("RETRY_INTERVAL"),autoCallTask.getInt("INTERVAL_TYPE"),"4","定位归属地异常");
				}else {                         //否则,修改为已失败
					AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(telId, null, "4", "定位归属地异常");
				}
				return false;
			}
			
		}
		
	}

}
