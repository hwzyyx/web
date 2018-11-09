import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.autocall.flow.AutoFlow;
import com.callke8.autocall.flow.AutoFlowDetail;
import com.callke8.autocall.questionnaire.Question;
import com.callke8.autocall.questionnaire.QuestionnaireRespond;
import com.callke8.autocall.voice.Voice;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.predialqueueforautocallbyquartz.AutoCallPredial;
import com.callke8.system.callerid.SysCallerId;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TelephoneNumberLocationUtil;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动外呼的AGI脚本
 * 		用于将自动外呼任务中呼出的电话转到当前的脚本执行
 * 
 * @author hwz
 *
 */
public class AutoCallTaskAgi extends BaseAgiScript {

	private Log log = LogFactory.getLog(AutoCallTaskAgi.class);
	private String voicePathSingle = ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle"); 
	
	@Override
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		//从通道变量中，获得 telId
		String telId =  channel.getVariable("autoCallTaskTelephoneId");
		AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(telId);     //取出号码信息
		
		String taskId = actt.get("TASK_ID");    //获得任务的 ID
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);                  //取得任务信息
		
		List<Record> playList = new ArrayList<Record>();    //定义一个List,用于存储播放列表
		
		exec("Noop","自动外呼流程AGI流程,任务名称:" + autoCallTask.getStr("TASK_NAME") + ",客户号码：" + actt.getStr("CUSTOMER_TEL") + " 开始执行 AGI 流程!");
		StringUtil.log(this, "自动外呼流程AGI流程,任务名称:" + autoCallTask.getStr("TASK_NAME") + ",客户号码：" + actt.getStr("CUSTOMER_TEL") + " 开始执行 AGI 流程!");
		
		//执行到这里，表示呼叫已经成功，需要修改状态为2，即是成功
		AutoCallPredial.updateTelehponeStateForSuccess("SUCCESS", actt);
		
		String taskType = autoCallTask.get("TASK_TYPE");            		//取出任务类型
		String reminderType = autoCallTask.get("REMINDER_TYPE");        	//催缴类型
	
		playList = getPlayList(autoCallTask,actt);    				//取出播放列表
		
		//如果播放列表不为空
		if(!BlankUtils.isBlank(playList) && playList.size() > 0) {
			exec("Noop","播放列表中语音文件的数量为:" + playList.size());
			StringUtil.log(this, "[====GASYAGI===]播放列表中语音文件的数量为:" + playList.size());
			if(taskType.equalsIgnoreCase("3") && reminderType.equalsIgnoreCase("7")) {    //如果任务类型为3即是催缴类型，且催缴类型为 7 即是交警移车时
				
				execPlayForTaskType3AndReminderType7(playList,actt,autoCallTask,channel);
				
			}else {
				//第一次必定先播放一次
				execPlay(playList, taskId, Integer.valueOf(telId), actt.get("CUSTOMER_TEL").toString(), channel);
				
				if(!taskType.equals("2")) {    	  //非问卷调查任务时,需要提示重复播放
					while(repeat(channel)) {      //如果客户需要重复时,重复播放
						execPlay(playList, taskId, Integer.valueOf(telId), actt.get("CUSTOMER_TEL").toString(), channel);
					}
				}
			}
			
		}else {   //如果播放列表为空时,提醒播放无语音文件播放
			exec("Noop","播放列表为空");
			exec("PlayBack",voicePathSingle + "/emptyPlayList");
		}
		
		//更新通话时长
		AutoCallTaskTelephone.dao.updateAutoCallTaskTelephoneBillsec(Integer.valueOf(telId), Integer.valueOf(channel.getVariable("CDR(billsec)")));
		
		//退出之后，需要清理一下，当前的活跃通道，释放资源
		AutoCallPredial.activeChannelCount--;
		
		exec("hangup");
	}
	
	/**
	 * 执行播放,为催缴类型中交警移车的任务
	 * 
	 * @param playList
	 */
	public void execPlayForTaskType3AndReminderType7(List<Record> playList,AutoCallTaskTelephone actt,AutoCallTask autoCallTask,AgiChannel channel) {
		
		//如果插入列表不为空时
		if(!BlankUtils.isBlank(playList) && playList.size()>0) {
			
			for(Record record:playList) {
				
				String action = record.get("action");
				String path = record.get("path");
				if(action.equalsIgnoreCase("Read")) {     //如果为调查问卷类型时
				
					try {
						exec(action,path);
						
						//获取客户回复的按键
						String respond = channel.getVariable("respond");
						System.out.println("客户回复的按键是：" + respond);
						if(!BlankUtils.isBlank(respond)) {    //如果按键不为空时,将来电再呼叫报警人电话上
							
							String callPoliceTel = actt.getStr("CALL_POLICE_TEL");   //报警人电话号码
							
							if(!BlankUtils.isBlank(callPoliceTel)) {                //只有当报警人电话号码不为空时，才执行呼转到报警人电话号码上
								exec("Noop","系统将通话呼转到报警人电话号码:" + callPoliceTel);
								System.out.println("系统将通话呼转到报警人电话号码：" + callPoliceTel + " 上");
								
								String callOutTel = callPoliceTel;
								
								//取归属地
								Record customerTelLocation = TelephoneNumberLocationUtil.getLocation(callPoliceTel);    //取得号码归属地
								System.out.println("customerTelLocation: " + customerTelLocation);
								if(!BlankUtils.isBlank(customerTelLocation)) {
									callOutTel = customerTelLocation.getStr("callOutTel");                     //得到外呼号码
								}
								
								String numberPrefix = ParamConfig.paramConfigMap.get("paramType_4_numberPrefix");   //增加前缀
								callOutTel = numberPrefix + callOutTel;
								
								String channelInfo = ParamConfig.paramConfigMap.get("paramType_4_trunkInfo") + "/" + callOutTel;
								
								//获取主叫号码
								String callerIdInfo = autoCallTask.get("CALLERID");   									//主叫的ID信息
								SysCallerId sci = SysCallerId.dao.getSysCallerIdById(Integer.valueOf(callerIdInfo));    //得到主叫的记录
								String callerIdNumber = null;
								if(!BlankUtils.isBlank(sci)) {
									callerIdNumber = sci.getStr("CALLERID");
								}
								
								channel.setCallerId(callerIdNumber);
								exec("Noop","准备执行外呼到报警人的电话上，主叫号码为:" + callerIdNumber + ",报警人通道为:" + channelInfo);
								
								exec("Dial",channelInfo);
								
							}else {
								exec("noop","呼转到报警人电话无效，原因：报警人电话为空");
								System.out.println("呼转到报警人电话无效，原因：报警人电话为空");
							}
							
							
						}
						
					} catch (AgiException e) {
						e.printStackTrace();
					}
				
				}else {   //如果PlayBack 就执行播放操作
					try {
						exec(action,path);
					} catch (AgiException e) {
						e.printStackTrace();
					}
					
				}
				
				
			}
			
		}
		
	}
	
	/**
	 * 执行播放
	 * 
	 * @param playList
	 */
	public void execPlay(List<Record> playList,String taskId,int telId,String customerTel,AgiChannel channel) {
		
		//如果插入列表不为空时
		if(!BlankUtils.isBlank(playList) && playList.size()>0) {
			
			for(Record record:playList) {
				
				String action = record.get("action");
				String path = record.get("path");
				String questionId = null;
				if(action.equalsIgnoreCase("Read")) {     //如果为调查问卷类型时
				
					questionId = record.get("questionId");
				
					try {
						exec(action,path);
						
						//获取客户回复的按键
						String respond = channel.getVariable("question");
						
						if(!BlankUtils.isBlank(respond)) {    //如果按键不为空时
							
							boolean isExist = QuestionnaireRespond.dao.isExist(taskId, Integer.valueOf(telId), questionId);
							
							if(isExist) {    //如果已经存在该回复,则直接修改即可
								QuestionnaireRespond.dao.update(taskId, telId, questionId, respond);
							}else {          //如果不存在该回复,直接存储
								
								QuestionnaireRespond res = new QuestionnaireRespond();
								//生成自定义ID
								String respondId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
								res.set("RESPOND_ID",respondId);
								res.set("TASK_ID", taskId);
								res.set("TEL_ID",telId);
								res.set("QUESTION_ID",questionId);
								res.set("RESPOND",respond);
								
								QuestionnaireRespond.dao.add(res);   //保存回复结果
							}
							
						}
						
					} catch (AgiException e) {
						e.printStackTrace();
					}
				
				}else {   //如果PlayBack 就执行播放操作
					try {
						exec(action,path);
					} catch (AgiException e) {
						e.printStackTrace();
					}
					
				}
				
				
			}
			
		}
		
	}
	
	
	
	/**
	 * 是否重复
	 * 
	 * @return
	 */
	public boolean repeat(AgiChannel channel) {
		
		boolean b = false;
		
		try {
			
			exec("wait","1");     //休息一秒
			
			exec("Read","isRepeat," + voicePathSingle + "/repeat,1,s,,10");
			
			String isRepeat = channel.getVariable("isRepeat");
			
			System.out.println("客户回复按键：" + isRepeat);
			
			if(isRepeat.equals("1")) {
				b = true;
			}
			
		} catch (AgiException e) {
			e.printStackTrace();
		}
		
		return b;
	}
	
	/**
	 * 根据传入的外呼任务和号码信息,生成播放列表
	 * 
	 * 用Record 存储播放信息：action 即执行的动作：PlayBack、Read等
	 * 						 path   播放路径
	 * 						 questionId 如果是调查问卷外呼,需要传入问题ID
	 * 
	 * @param autoCallTask
	 * @param autoCallTaskTelephone
	 * @return
	 */
	public List<Record> getPlayList(AutoCallTask autoCallTask,AutoCallTaskTelephone actt) {
		
		List<Record> list = new ArrayList<Record>();                    //新建一个List,用于储存播放语音
		list.add(setRecord("wait","0.5",null));         				//先停顿0.5秒
		
		String taskType = autoCallTask.get("TASK_TYPE");   					//任务类型
		String reminderType = autoCallTask.get("REMINDER_TYPE");        	//催缴类型
		
		String startVoiceId = autoCallTask.get("START_VOICE_ID");       //开始语音ID
		String endVoiceId = autoCallTask.get("END_VOICE_ID");           //结束语音ID
		String commonVoiceId = autoCallTask.get("COMMON_VOICE_ID");     //普通外呼语音ID
		String questionnaireId = autoCallTask.get("QUESTIONNAIRE_ID");  //问卷ID
		
		//(1)判断并配置开始语音
		if(!BlankUtils.isBlank(startVoiceId)) {
			//根据开始语音ID,取出语音对象
			Voice startVoice = Voice.dao.getVoiceByVoiceId(startVoiceId);
			
			String fileName = startVoice.get("FILE_NAME");    //语音文件
			String startVoicePath = voicePathSingle + "/" + fileName;
			
			list.add(setRecord("PlayBack",startVoicePath,null));    // 设置record并加入list
		}
		
		//(2)配置中部语音,主要是根据任务类型配置播放语音
		if(taskType.equals("1")) {             //即是普通外呼,只要取出普通语音即可
			
			if(!BlankUtils.isBlank(commonVoiceId)) {     //先判断普通语音是否为空
				
				Voice commonVoice = Voice.dao.getVoiceByVoiceId(commonVoiceId);
				String fileName = commonVoice.get("FILE_NAME");
				
				String commonVoicePath = voicePathSingle + "/" + fileName;
				list.add(setRecord("wait","0.5",null));         //先停顿0.5秒
				list.add(setRecord("PlayBack",commonVoicePath,null));
				
			}
		}else if(taskType.equals("2")) {       //即是调查问卷外呼
			
			if(!BlankUtils.isBlank(questionnaireId)) {      //判断调查问卷ID不为空
				
				//根据问卷Id，取出问题列表
				List<Question> questionList = Question.dao.getQuestionByQuestionnaireId(questionnaireId);
				
				//判断问题的数量
				if(!BlankUtils.isBlank(questionList) && questionList.size() > 0) {
					int i = 1;
					//遍历问题
					for(Question question:questionList) {
						
						String questionId = question.get("QUESTION_ID");   //问题ID
						String voiceId = question.get("VOICE_ID");         //语音ID
						
						if(!BlankUtils.isBlank(voiceId)) {     //语音ID不为空时
							
							Voice questionVoice = Voice.dao.getVoiceByVoiceId(voiceId);
							
							String fileName = questionVoice.get("FILE_NAME");
							String questionVoicePath = voicePathSingle + "/" + fileName;
							list.add(setRecord("wait","0.5",null));         //先停顿0.5秒
							list.add(setRecord("Read","question," + questionVoicePath + ",1,,3,10",questionId));
						}
						
						i++;
					}
					
				}
				
			}
			
		}else if(taskType.equals("3")) {    //如果是催缴类任务
			
			AutoFlow autoFlow = AutoFlow.dao.getAutoFlowByReminderType(reminderType);    //根据催缴类型，取出流程规则
			
			System.out.println("流程规则===== ：" + autoFlow);
			
			if(!BlankUtils.isBlank(autoFlow)) {
				//System.out.println("执行流程规则分析和判断.........");
				String flowId = autoFlow.getStr("FLOW_ID");    
				//System.out.println("flowID：===：" + flowId);
				List<Record> autoFlowDetailList = AutoFlowDetail.dao.getAutoFlowDetailByFlowId(flowId);     //根据流程规则ID,取得流程详情列表
				//System.out.println("autoFlowDetailList============:" + autoFlowDetailList + ",autoFlowDetailList.size():" + autoFlowDetailList.size());
				if(!BlankUtils.isBlank(autoFlowDetailList)) {
					//System.out.println("我要开始判断催缴类型了:" + reminderType);
					if(reminderType.equals("1")) {     //如果是电费催缴
						/*
						用户号码|户号|地址|电费
						18951082343|1009988777|南京市玄武区XXX号XXX小区|220.14
						
						规则：
						常州供电公司友情提醒：您户地址%s，总户号%s于%s发生电费%s元，请按时缴纳，逾期缴纳将产生滞纳金。详情可关注“国网江苏电力”公众微信号或下载掌上电力app。如您本次收到的用电地址有误，可在工作时间致电83272222。若已缴费请忽略本次提醒。
						*/
						String addressVoiceName = actt.getStr("ADDRESS_VOICE_NAME");    //取得地址的语音文件名
						String accountNumber = actt.getStr("ACCOUNT_NUMBER");           //户号
						String charge = actt.getStr("CHARGE");                          //费用
						String period = actt.getStr("PERIOD");                          //日期
						
						//组织语音列表
						//(1)常州供电公司友情提醒：您户地址
						Record autoFlowDetail0 = autoFlowDetailList.get(0);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail0.getStr("VOICE_NAME")));    //常州供电公司友情提醒：您户地址
						
						//(2)地址
						list.add(setRecord("playback",addressVoiceName));
						
						//(3)，总户号
						list.add(setRecord("wait","0.5"));    //停0.5秒
						Record autoFlowDetail1 = autoFlowDetailList.get(1);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail1.getStr("VOICE_NAME")));   //，总户号
						
						//(4)户号结果，是一串数字
						setAccountNumberPlayList(list,accountNumber);
						
						//（5）于
						list.add(setRecord("wait","0.5"));    //停0.5秒
						Record autoFlowDetail2 = autoFlowDetailList.get(2);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail2.getStr("VOICE_NAME")));   //于
						
						//（6）年月
						setPeriodPlayList(list,period);
						
						//（7）发生电费
						Record autoFlowDetail3 = autoFlowDetailList.get(3);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail3.getStr("VOICE_NAME")));   //电生电费
						
						//（8）费用
						setChargePlayList(list,charge);
						
						//(9)元，请按时缴纳，逾期缴纳将产生滞纳金。详情可关注“国网江苏电力”公众微信号或下载掌上电力app。如您本次收到的用电地址有误，可在工作时间致电83272222。若已缴费请忽略本次提醒。
						list.add(setRecord("wait","0.3"));    //停0.5秒
						Record autoFlowDetail4 = autoFlowDetailList.get(4);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail4.getStr("VOICE_NAME")));   //元，请按时缴纳，逾期缴纳将产生滞纳金。详情可关注“国网江苏电力”公众微信号或下载掌上电力app。如您本次收到的用电地址有误，可在工作时间致电83272222。若已缴费请忽略本次提醒。
						
					}else if(reminderType.equals("2")) {                              //自来水费催缴
						/*
						用户|地址|本月抄见数|本月用量|本期金额|户号
						18951082343|南京市玄武区XXX号XXX小区|5523|321|222.19|1001692206
						
						规则：
						尊敬的自来水用户您好，下面为您播报本期水费对账单。您水表所在地址%s于%s抄见数为%s，月用水量为%s吨，水费为%s元。特此提醒。详情可凭用户号%s登录常州通用自来水公司网站或致电常水热线：88130008查询。
						*/
						String addressVoiceName = actt.getStr("ADDRESS_VOICE_NAME");     //
						String period = actt.getStr("PERIOD"); 
						String displayNumber = actt.getStr("DISPLAY_NUMBER");
						String dosage = actt.getStr("DOSAGE");
						String charge = actt.getStr("CHARGE");
						String accountNumber = actt.getStr("ACCOUNT_NUMBER");
						
						//组织语音列表
						//(1)尊敬的自来水用户您好，下面为您播报本期水费对账单。您水表所在地址
						Record autoFlowDetail0 = autoFlowDetailList.get(0);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail0.getStr("VOICE_NAME")));    //尊敬的自来水用户您好，下面为您播报本期水费对账单。您水表所在地址
						
						//（2）地址
						list.add(setRecord("playback",addressVoiceName));
						
						//（3）于
						list.add(setRecord("wait","0.5"));    //停0.5秒
						Record autoFlowDetail1 = autoFlowDetailList.get(1);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail1.getStr("VOICE_NAME")));   //于
						
						//(4)日期
						setPeriodPlayList(list,period);
						
						//（5）抄见数为
						Record autoFlowDetail2 = autoFlowDetailList.get(2);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail2.getStr("VOICE_NAME")));   //抄见数为
						
						//(6)表显数量
						setDisplayNumberPlayList(list,displayNumber);
						
						//(7)月用水量为
						Record autoFlowDetail3 = autoFlowDetailList.get(3);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail3.getStr("VOICE_NAME")));   //月用水量为
						
						//（8）用水量
						setDosagePlayList(list,dosage);       //用水量数量
						
						//（9）吨，水费为
						Record autoFlowDetail4 = autoFlowDetailList.get(4);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail4.getStr("VOICE_NAME")));   //吨，水费为
						
						//(10)费用
						setChargePlayList(list, charge);
						
						//(11)元。特此提醒。详情可凭用户号
						Record autoFlowDetail5 = autoFlowDetailList.get(5);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail5.getStr("VOICE_NAME")));   //元。特此提醒。详情可凭用户号
						
						//(12)用户号
						setAccountNumberPlayList(list, accountNumber);
						
						//(13)登录常州通用自来水公司网站或致电常水热线：88130008查询。
						Record autoFlowDetail6 = autoFlowDetailList.get(6);        				//取出流程规则详情
						list.add(setRecord("playback",autoFlowDetail6.getStr("VOICE_NAME")));   //登录常州通用自来水公司网站或致电常水热线：88130008查询。
						
					}else if(reminderType.equals("3")) {       //电话费催缴
						/*
						用户号码|户号|地址|电话费
						18951082343|100138341|南京市玄武区XXX号XXX小区|220.14
						
						规则：
						尊敬的客户您好，你%s的电话费为%s元。
						
						*/
						String period = actt.getStr("PERIOD");     //日期
						String charge = actt.getStr("CHARGE");	   //费用
						
						//设计语音列表
						//(1)尊敬的客户您好，你
						Record autoFlowDetail0 = autoFlowDetailList.get(0);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail0.getStr("VOICE_NAME")));    //尊敬的客户您好，你
						
						//(2)日期
						setPeriodPlayList(list, period);
						
						//(3)的电话费为
						Record autoFlowDetail1 = autoFlowDetailList.get(1);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail1.getStr("VOICE_NAME")));    //的电话费为
						
						//（4）费用
						setChargePlayList(list, charge);
						
						//(5)元
						Record autoFlowDetail2 = autoFlowDetailList.get(2);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail2.getStr("VOICE_NAME")));    //元
					}else if(reminderType.equals("4")) {       //燃气费催缴
						/*
						用户号码|户号|地址|燃气费
					    18951082343|100138341|南京市玄武区XXX号XXX小区|220.14
						
						规则：
						尊敬的客户您好，你%s的燃气费为%s元。
						
						*/
						String period = actt.getStr("PERIOD");     //日期
						String charge = actt.getStr("CHARGE");	   //费用
						
						//设计语音列表
						//(1)尊敬的客户您好，你
						Record autoFlowDetail0 = autoFlowDetailList.get(0);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail0.getStr("VOICE_NAME")));    //尊敬的客户您好，你
						
						//(2)日期
						setPeriodPlayList(list, period);
						
						//(3)的燃气费为
						Record autoFlowDetail1 = autoFlowDetailList.get(1);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail1.getStr("VOICE_NAME")));    //燃气费
						
						//（4）费用
						setChargePlayList(list, charge);
						
						//(5)元
						Record autoFlowDetail2 = autoFlowDetailList.get(2);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail2.getStr("VOICE_NAME")));    //元
					}else if(reminderType.equals("5")) {       //物业费催缴
						/*
						用户号码|地址|物业费
						18951082343|南京市玄武区XXX号XXX小区|220.14
						
						规则：
						尊敬的客户您好，你%s的物业费为%s元。
						
						*/
						String period = actt.getStr("PERIOD");     //日期
						String charge = actt.getStr("CHARGE");	   //费用
						
						//设计语音列表
						//(1)尊敬的客户您好，你
						Record autoFlowDetail0 = autoFlowDetailList.get(0);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail0.getStr("VOICE_NAME")));    //尊敬的客户您好，你
						
						//(2)日期
						setPeriodPlayList(list, period);
						
						//(3)的物业费为
						Record autoFlowDetail1 = autoFlowDetailList.get(1);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail1.getStr("VOICE_NAME")));    //物业费
						
						//（4）费用
						setChargePlayList(list, charge);
						
						//(5)元
						Record autoFlowDetail2 = autoFlowDetailList.get(2);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail2.getStr("VOICE_NAME")));    //元
					}else if(reminderType.equals("6")) {       //交通违章
						/*
						用户号码|车牌|违章日期|违章城市|违章事由|处罚单位
						18951082343|苏DR1179|20181001|南京市|高速连续变道|南京市交警大队
						
						您的（车牌）汽车于（违章日期）在违反了相关的交通条例，请收到本告知之日起30日内接受处理。
						
						规则：
						您的%s汽车于%s在违反了相关的交通条例，请收到本告知之日起30日内接受处理。
						
						*/
						
						
						String period = actt.getStr("PERIOD");     //日期
						String plateNumberVoiceName = actt.getStr("PLATE_NUMBER_VOICE_NAME");	   //车牌语音列表
						
						//设计语音列表
						//(1)您的
						Record autoFlowDetail0 = autoFlowDetailList.get(0);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail0.getStr("VOICE_NAME")));    //您的
						
						//(2)车牌
						list.add(setRecord("playback",plateNumberVoiceName));
						
						//(3)汽车于
						Record autoFlowDetail1 = autoFlowDetailList.get(1);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail1.getStr("VOICE_NAME")));    //汽车于
						
						//（4）日期
						setPeriodPlayList(list, period);
						
						//(5)违反了相关的交通条例，请收到本告知之日起30日内接受处理。
						Record autoFlowDetail2 = autoFlowDetailList.get(2);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail2.getStr("VOICE_NAME")));    //违反了相关的交通条例，请收到本告知之日起30日内接受处理。
					}else if(reminderType.equals("7")) {       //交警移车
						/*
						用户号码|报警人电话|车辆类型|车牌号码
						18951082343|13512771995|小型车辆|DF168
						
						您好，这是常州公安微警务051981990110挪车服务专线，您是(车牌)车主吗？你的（车辆类型）占用他人车位，请按任意键接听车位业主电话。

						规则：
						您好，这是常州公安微警务051981990110挪车服务专线，您是%s车主吗？你的%s占用他人车位，请按任意键接听车位业主电话。
						
						*/
						//System.out.println("执行到交警移车的流程判断----------");
						String plateNumberVoiceName = actt.getStr("PLATE_NUMBER_VOICE_NAME");	   //车牌语音列表
						String vehicleTypeVoiceName = actt.getStr("VEHICLE_TYPE_VOICE_NAME");      //车辆类型语音名字
						
						//组织语音文件
						//由于交警移车需要通过  read 响应客户回复的按键，如果客户回复了，则将通话转到报警人电话中
						StringBuilder sb = new StringBuilder();
						
						//（1）您好，这是常州公安微警务051981990110挪车服务专线，您是
						sb.append(voicePathSingle + "/" + autoFlowDetailList.get(0).getStr("VOICE_NAME"));    //您好，这是常州公安微警务051981990110挪车服务专线，您是
						
						//（2）车牌
						sb.append("&" + voicePathSingle + "/" + plateNumberVoiceName);
						
						//(3)车主吗？你的
						sb.append("&" + voicePathSingle + "/" + autoFlowDetailList.get(1).getStr("VOICE_NAME"));   //车主吗？你的
						
						//(4)车辆类型
						sb.append("&" + voicePathSingle + "/" + vehicleTypeVoiceName);
						
						//(5)占用他人车位，请按任意键接听车位业主电话。
						sb.append("&" + voicePathSingle + "/" + autoFlowDetailList.get(2).getStr("VOICE_NAME"));   //占用他人车位，请按任意键接听车位业主电话。
						
						Record readRecord = new Record();
						readRecord.set("action", "Read");
						readRecord.set("path","respond," + sb.toString() + ",1,,3,8");
						
						list.add(readRecord);
					}else if(reminderType.equals("8")) {       //社保费催缴
						/*
						用户号码|社保费
						18951082343|880.20
						
						规则：
						尊敬的客户您好，你%s的社保费为%s元。
						
						*/
						String period = actt.getStr("PERIOD");     //日期
						String charge = actt.getStr("CHARGE");	   //费用
						
						//设计语音列表
						//(1)尊敬的客户您好，你
						Record autoFlowDetail0 = autoFlowDetailList.get(0);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail0.getStr("VOICE_NAME")));    //尊敬的客户您好，你
						
						//(2)日期
						setPeriodPlayList(list, period);
						
						//(3)的社保费为
						Record autoFlowDetail1 = autoFlowDetailList.get(1);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail1.getStr("VOICE_NAME")));    //社保费
						
						//（4）费用
						setChargePlayList(list, charge);
						
						//(5)元
						Record autoFlowDetail2 = autoFlowDetailList.get(2);         //取出流程规则详情
						list.add(setRecord("playback", autoFlowDetail2.getStr("VOICE_NAME")));    //元
					}else {
						System.out.println("无法判断----------");
					}
					
				}
				
			}
			
		}
		
		
		
		//(3)判断并配置结束语音
		if(!BlankUtils.isBlank(endVoiceId)) {
			//根据开始语音ID,取出语音对象
			Voice endVoice = Voice.dao.getVoiceByVoiceId(endVoiceId);
			
			String fileName = endVoice.get("FILE_NAME");    //语音文件
			String endVoicePath = voicePathSingle + "/" + fileName;
			
			Record endVoiceRecord = new Record();
			
			endVoiceRecord.set("action","PlayBack");
			endVoiceRecord.set("path",endVoicePath);
			
			list.add(endVoiceRecord);
		}
		
		return list;
		
	}
	
	/**
	 * 设置金额的列表
	 * 
	 * @param list
	 * @param charge
	 */
	public void setChargePlayList(List<Record> list,String charge) {
		String countStr = StringUtil.numberExchangeToCount(charge);
		if(!BlankUtils.isBlank(countStr)) {   //如果不为空时,开始分解并读取
			char[] chars = countStr.toCharArray();
			for(char c:chars) {      
				//组织文件
				list.add(setRecord("wait","0.2",null));         //先停顿0.5秒
				list.add(setRecord("PlayBack",voicePathSingle + "/" + c,null));
			}
			
		}
	}
	
	
	/**
	 * 设置使用量的列表
	 * 
	 * @param list
	 * @param charge
	 */
	public void setDosagePlayList(List<Record> list,String dosage) {
		String countStr = StringUtil.numberExchangeToCount(dosage);
		if(!BlankUtils.isBlank(countStr)) {   //如果不为空时,开始分解并读取
			char[] chars = countStr.toCharArray();
			for(char c:chars) {      
				//组织文件
				list.add(setRecord("wait","0.2",null));         //先停顿0.5秒
				list.add(setRecord("PlayBack",voicePathSingle + "/" + c,null));
			}
			
		}
	}
	
	/**
	 * 获取日期对应的语音列表，
	 * 
	 * 是以年月为主
	 * 
	 * @param list
	 * @param period
	 */
	public void setPeriodPlayList(List<Record> list,String period) {
		
		String year = period.substring(0,4);          //取得年
		String month = period.substring(4, 6);        //取得月
		
		list.add(setRecord("playback", year));
		list.add(setRecord("playback", month));
		
	}
	
	/**
	 * 获取户号对应的语音列表
	 * 
	 * @param list
	 * @param accountNumber
	 */
	public void setAccountNumberPlayList(List<Record> list,String accountNumber) {
		
		char[] cs = accountNumber.toCharArray();
		for(char c:cs) {
			list.add(setRecord("playback",String.valueOf(c)));
		}
		
	}
	
	
	/**
	 * 获取户号对应的语音列表
	 * 
	 * @param list
	 * @param accountNumber
	 */
	public void setDisplayNumberPlayList(List<Record> list,String displayNumber) {
		
		char[] cs = displayNumber.toCharArray();
		for(char c:cs) {
			list.add(setRecord("playback",String.valueOf(c)));
		}
		
	}
	
	
	/**
	 * 设置一个Record 
	 * 
	 * @param action
	 * @param path
	 * @param questionId
	 * @return
	 */
	public Record setRecord(String action,String path,String questionId) {
		
		Record record = new Record();
		
		record.set("action", action);
		record.set("path",path);
		
		if(!BlankUtils.isBlank(questionId)) {
			record.set("questionId", questionId);
		}
		
		return record;
	}
	
	public Record setRecord(String action,String voiceName) {
		
		Record record = new Record();
		record.set("action", action);
		record.set("path",voicePathSingle + "/" + voiceName);
		
		return record;
		
	}
	

}
