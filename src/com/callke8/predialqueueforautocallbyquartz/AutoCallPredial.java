package com.callke8.predialqueueforautocallbyquartz;

import java.io.File;
import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.autocall.flow.AutoFlowTTSJob;
import com.callke8.bsh.bshorderlist.BSHHttpRequestThread;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.pridialqueueforbshbyquartz.BSHLaunchDialJob;
import com.callke8.pridialqueueforbshbyquartz.BSHLoadOrderListJob;
import com.callke8.pridialqueueforbshbyquartz.BSHLoadRetryJob;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.QuartzUtils;
import com.callke8.utils.StringUtil;
import com.jfinal.kit.PathKit;

/**
 * 自动外呼任务的调整主守护程序
 * 
 * 定时扫描外呼任务及加载数据到排队机并执行外呼的守护过程
 * 
 * @author 黄文周
 *
 */
public class AutoCallPredial {
	
	public static int activeChannelCount = 0;     //当前的活跃通道
	
	//构造函数
	public AutoCallPredial() {
		
	}
	
	//执行区
	public void exec() throws SchedulerException {
		
		System.out.println(DateFormatUtils.getCurrentDate() + "\t" + this.getClass().getSimpleName() + "，准备开始执行!");
		
		int scanInterval = Integer.valueOf(ParamConfig.paramConfigMap.get("paramType_4_scanInterval"));
		long startSeconds = System.currentTimeMillis() + 5 * 1000;       //在当前的毫秒基础上加5秒
		Date startTime = new Date(startSeconds);                         //定义 5 秒后启动守护程序
		
		//执行到这里，表示服务器（tomcat）被重启过，需要将状态为 "已载入"记录，重置为0，执行一次数据回滚
		int rollBackCount = AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(0, "1", "0", null);
		StringUtil.log(this, "服务器(Tomcat)被重启，系统回滚'已载入'号码数据,此次回滚数据量为:" + rollBackCount);
		
		//线程额外：在扫描任务之前，需要进行一次自动外呼呼叫流程规则的语音转换
		Scheduler schedulerForAutoFlowTTS = QuartzUtils.createScheduler("AutoFlowTTSJob" + System.currentTimeMillis(),1);
		JobDetail jobDetail = QuartzUtils.createJobDetail(AutoFlowTTSJob.class);
		schedulerForAutoFlowTTS.scheduleJob(jobDetail, QuartzUtils.createSimpleTrigger(new Date((System.currentTimeMillis() + 1000)), 0, 1));   //执行一次
		schedulerForAutoFlowTTS.start();
		
		//线程一:扫描任务到排队机线程
		Scheduler scheduler1 = QuartzUtils.createScheduler("AutoCallLoadTaskJob" + System.currentTimeMillis(),1);
		scheduler1.scheduleJob(QuartzUtils.createJobDetail(AutoCallLoadTaskJob.class),QuartzUtils.createTrigger(startTime,scanInterval));
		scheduler1.start();
		
		//线程二:扫描排队机,若排队机中有数据,则执行外呼
		Scheduler scheduler2 = QuartzUtils.createScheduler("AutoCallLaunchDialJob" + System.currentTimeMillis(),1);
		scheduler2.scheduleJob(QuartzUtils.createJobDetail(AutoCallLaunchDialJob.class),QuartzUtils.createSimpleTrigger(startTime,-1,400));    //每500毫秒发起一次呼叫
		scheduler2.start();
		
		//线程三:扫描待重呼记录到排队机线程，因为重呼的优先及应该高于非重呼，所以将扫描频率设置为2秒。
		Scheduler scheduler3 = QuartzUtils.createScheduler("AutoCallLoadRetryJob" + System.currentTimeMillis(),1);
		scheduler3.scheduleJob(QuartzUtils.createJobDetail(AutoCallLoadRetryJob.class),QuartzUtils.createTrigger(startTime,2));   //每两秒扫描一次
		scheduler3.start();
		
		//线程四：超时记录检查，如果某条外呼记录的状态为1（已载入），载入8分钟后，仍没有得到执行，系统将强制处理掉，将其转为待重呼或已失败。
		Scheduler scheduler4 = QuartzUtils.createScheduler("AutoCallHandleTimeOutRecordJob" + System.currentTimeMillis(), 1);
		scheduler4.scheduleJob(QuartzUtils.createJobDetail(AutoCallHandleTimeOutRecordJob.class),QuartzUtils.createTrigger(startTime, 30));     //由于该流程的要处理的数据不大，每30秒扫描一次即可
		scheduler4.start();
	}
	
	/**
	 * 外呼失败时,更改号码的状态
	 * 失败的原因有三：1 pbx的连接不成功;2呼叫失败;3 无人接听
	 * 
	 * @param lastCallResult
	 * 			最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * 
	 */
	public static void updateTelehponeStateForFailure(String lastCallResult,AutoCallTaskTelephone actt,AutoCallTask autoCallTask) {
		
		int retried  = actt.getInt("RETRIED");                       //已外呼次数
		int retryTimes = autoCallTask.getInt("RETRY_TIMES");		 //任务设置的最大外呼次数
		int retryInterval = autoCallTask.getInt("RETRY_INTERVAL");   //重试间隔
		int intervalType = autoCallTask.getInt("INTERVAL_TYPE");     //间隔类型：1分钟；2小时；3天
		int telId = actt.getInt("TEL_ID");                           //记录的号码ID
		
		if(retried < retryTimes) {      //如果已经外呼次数小于允许的最大外呼次数，则将记录状态修改为待重呼
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneStateToRetry(telId, "3", retryInterval,intervalType,lastCallResult);
		}else {                         //否则,修改为已失败
			AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(telId, null, "4", lastCallResult);
			deleteVoiceFileByTelId(telId);    //删除该记录的录音文件
		}
		
	}
	
	/**
	 * 通话成功时保存
	 * 通话成功时,通话并没有结束,所以活动通道不能减除,而是需要将通道加入内存
	 * 加一个事件监控线程,在挂机事件时，再将其解除
	 * 
	 * @param lastCallResult
	 */
	public static void updateTelehponeStateForSuccess(String lastCallResult, AutoCallTaskTelephone actt) {
		
		AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneState(actt.getInt("TEL_ID"),null,"2",lastCallResult);
		
	}
	
	/**
	 * 根据传入的号码ID(telId),取出号码记录中，三个通过 TTS 转换为语音文件的字段
	 * 
	 * 这三个字段分别是：地址（ADDRESS）、车牌号码(PLATE_NUMBER)、车辆类型（VEHICLE_TYPE） 
	 * 
	 * 通过TTS后转换的语音文件名，将存在(ADDRESS_VOICE_NAME、PLATE_NUMBER_VOICE_NAME、VEHICLE_TYPE_VOICE_NAME) 三个字段里
	 * 
	 * 如果这两个文件号码字段不为空，则取出文件名（后缀默认为 wav）的文件是否存在，如果存在，则需要将文件删除，避免太多的临时文件占用系统空间
	 * 
	 * @param telId
	 */
	public static void deleteVoiceFileByTelId(int telId) {
		
		//从数据表中，取出号码的记录
		AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(String.valueOf(telId));    
		
		if(!BlankUtils.isBlank(actt)) {   //只有不为空时，才执行语音文件删除的操作
			
			String addressVoiceName = actt.getStr("ADDRESS_VOICE_NAME");            	//地址通过TTS转换后的语音文件名
			String plateNumberVoiceName = actt.getStr("PLATE_NUMBER_VOICE_NAME");		//车牌号码通过TTS转换后的语音文件名
			String vehicleTypeVoiceName = actt.getStr("VEHICLE_TYPE_VOICE_NAME");		//车辆类型通过TTS转换后的语音文件名
			
			String voicePath = ParamConfig.paramConfigMap.get("paramType_4_voicePath");					//语音文件路径（立体声）
			String voicePathSingle = ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle");		//语音文件路径（单声道）
			
			if(!BlankUtils.isBlank(addressVoiceName)) {               //地址语音文件名不为空时，删除语音文件
				String voicePathForAddress = PathKit.getWebRootPath() + File.separator + voicePath + File.separator + addressVoiceName + ".wav";
				String voicePathForAddressSingle = PathKit.getWebRootPath() + File.separator + voicePathSingle + File.separator + addressVoiceName + ".wav";
				
				deleteVoiceFile(voicePathForAddress);    			//删除立体声语音文件
				deleteVoiceFile(voicePathForAddressSingle);			//删除单声道语音文件
				
			}
			
			if(!BlankUtils.isBlank(plateNumberVoiceName)) {			  //车牌号码语音文件名不为空时，删除语音文件
				String voicePathForPlateNumber = PathKit.getWebRootPath() + File.separator + voicePath + File.separator + plateNumberVoiceName + ".wav";
				String voicePathForPlateNumberSingle = PathKit.getWebRootPath() + File.separator + voicePathSingle + File.separator + plateNumberVoiceName + ".wav";
				
				deleteVoiceFile(voicePathForPlateNumber);    			//删除立体声语音文件
				deleteVoiceFile(voicePathForPlateNumberSingle);			//删除单声道语音文件
			}
			
			if(!BlankUtils.isBlank(vehicleTypeVoiceName)) {           //车辆类型语音文件不粉空时，删除语音文件
				String voicePathForVehicleType = PathKit.getWebRootPath() + File.separator + voicePath + File.separator + vehicleTypeVoiceName + ".wav";
				String voicePathForVehicleTypeSingle = PathKit.getWebRootPath() + File.separator + voicePathSingle + File.separator + vehicleTypeVoiceName + ".wav";
				
				deleteVoiceFile(voicePathForVehicleType);    			//删除立体声语音文件
				deleteVoiceFile(voicePathForVehicleTypeSingle);			//删除单声道语音文件
			}
			
		}
		
	}
	
	/**
	 * 删除语音文件
	 * 
	 * 如果文件存在，就执行删除操作，如果文件不存在，则不执行删除
	 * 
	 * @param path
	 */
	public static void deleteVoiceFile(String path) {
		
		if(BlankUtils.isBlank(path)) {
			return;
		}
		
		File f = new File(path);
		
		if(f.exists()) {     //如果存在，就删除
			f.delete();
		}
		
	}

}
