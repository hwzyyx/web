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
import com.callke8.autocall.questionnaire.Question;
import com.callke8.autocall.questionnaire.QuestionnaireRespond;
import com.callke8.autocall.voice.Voice;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;
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
		
		String taskId = channel.getVariable("taskId");
		String telId = channel.getVariable("telId");
		List<Record> playList = new ArrayList<Record>();    //定义一个List,用于存储播放列表
		
		exec("Noop","任务ID:" + taskId);
		exec("Noop","号码ID:" + telId);
		
		//根据通道变量（任务ID）取出任务信息
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		AutoCallTaskTelephone autoCallTaskTelephone = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneById(telId);
		//取出任务类型
		String taskType = autoCallTask.get("TASK_TYPE");
	
		playList = getPlayList(autoCallTask,autoCallTaskTelephone);    //取出插入列表
		
		//如果播放列表不为空
		if(!BlankUtils.isBlank(playList) && playList.size() > 0) {
			exec("Noop","播放列表中语音文件的数量为:" + playList.size());
			//第一次必定先播放一次
			execPlay(playList, taskId, Integer.valueOf(telId), autoCallTaskTelephone.get("TELEPHONE").toString(), channel);
			
			
			if(!taskType.equals("2")) {    //非问卷调查任务时,需要提示重复播放
				while(repeat(channel)) {      //如果客户需要重复时,重复播放
					execPlay(playList, taskId, Integer.valueOf(telId), autoCallTaskTelephone.get("TELEPHONE").toString(), channel);
				}
			}
			
		}else {   //如果播放列表为空时,提醒播放无语音文件播放
			exec("Noop","播放列表为空");
			exec("PlayBack",voicePathSingle + "/emptyPlayList");
		}
		
		exec("hangup");
	}
	
	
	/**
	 * 执行播放
	 * 
	 * @param playList
	 */
	public void execPlay(List<Record> playList,String taskId,int telId,String telephone,AgiChannel channel) {
		
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
	public List<Record> getPlayList(AutoCallTask autoCallTask,AutoCallTaskTelephone autoCallTaskTelephone) {
		
		List<Record> list = new ArrayList<Record>();                    //新建一个List,用于储存播放语音
		
		String taskType = autoCallTask.get("TASK_TYPE");   				//任务类型
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
							
							list.add(setRecord("Read","question," + questionVoicePath + ",1,,3,10",questionId));
						}
						
						i++;
					}
					
				}
				
			}
			
		}else if(taskType.equals("3")) {    //如果是催缴类任务
			
			String period = autoCallTaskTelephone.get("PERIOD");    //日期
			String charge = autoCallTaskTelephone.get("CHARGE");    //费用
			String year = null;
			String month = null;
			String day = null;
			
			//分解日期
			if(!BlankUtils.isBlank(period) && period.length() >=6) {
				
				year = period.substring(0,4);    //取年
 				month = period.substring(4,6);   //取月
				
			}
			
			//问候语：尊敬的客户您好
			list.add(setRecord("PlayBack",voicePathSingle + "/greeting",null));
			
			//判断催缴类型
			if(reminderType.equals("7")) {    //社保催缴
				
				//催缴提醒:请及时汇缴
				list.add(setRecord("PlayBack",voicePathSingle + "/reminderalert",null));
				
				//2017年
				if(!BlankUtils.isBlank(year)) {
					list.add(setRecord("PlayBack",voicePathSingle + "/" + year,null));
				}
				
				//5月
				if(!BlankUtils.isBlank(month)) {
					list.add(setRecord("PlayBack",voicePathSingle + "/" + month,null));
				}
				
				//的社保费
				list.add(setRecord("Play",voicePathSingle + "/socialfees",null));
				
			}else if(reminderType.equals("6")) {     //交通违章催缴
				
				//交通违章催缴的模板：尊敬的客户您好,您2016年03月01日，有交通违章行为,请到交警部门接受处理
				
				//您
				list.add(setRecord("PlayBack",voicePathSingle + "/nin",null));
				
				//要重新分解日期
				if(!BlankUtils.isBlank(period) && period.length()==8) {
					
					year = period.substring(0,4);
					month = period.substring(4,6);
					day = period.substring(6,8);
					
					//2017年
					list.add(setRecord("PlayBack",voicePathSingle + "/" + year,null));
					//5月
					list.add(setRecord("PlayBack",voicePathSingle + "/" + month,null));
					//1日
					list.add(setRecord("PlayBack",voicePathSingle + "/" + day + "d",null));
				}
				
				//有交通违章行为,请到交警部门接受处理：trafficnotice.wav
				list.add(setRecord("PlayBack",voicePathSingle + "/trafficnotice",null));
				
			}else if(reminderType.equals("5")) {       //物业管理费
				
				String year1 = null;
				String month1 = null;
				String year2 = null;
				String month2 = null;
				
				//物业催缴模板：
				//模板：尊敬的客户您好,您2016年05月至2016年7月的 物业管理费，是：99.6
				//日期格式：应该是 201605-201606
				
				//您
				list.add(setRecord("PlayBack",voicePathSingle + "/nin",null));
				
				if(!BlankUtils.isBlank(period) && period.length()==13) {
					
					year1 = period.substring(0,4);
					month1 = period.substring(4,6);
					
					//2017年5月
					list.add(setRecord("PlayBack",voicePathSingle + "/" + year1,null));
					list.add(setRecord("PlayBack",voicePathSingle + "/" + month1,null));
					
					//至 
					list.add(setRecord("PlayBack",voicePathSingle + "/zhi",null));
					
					year2 = period.substring(7,11);
					month2 = period.substring(11,13);
					
					//2017年6月
					list.add(setRecord("PlayBack",voicePathSingle + "/" + year2,null));
					list.add(setRecord("PlayBack",voicePathSingle + "/" + month2,null));
				}
				
				//的物业管理费是
				list.add(setRecord("PlayBack",voicePathSingle + "/propertyfees",null));
				
				//读费用
				//先转换,将费用转换成金钱读数: 如 103.24 返回  1b03d24y
				String moneyStr = StringUtil.numberExchangeToMoney(charge);
				if(!BlankUtils.isBlank(moneyStr)) {   //如果不为空时,开始分解并读取
					char[] chars = moneyStr.toCharArray();
					
					for(char c:chars) {      
						//组织文件
						list.add(setRecord("PlayBack",voicePathSingle + "/" + c,null));
					}
					
				}
				
			}else {
				
				//您
				list.add(setRecord("PlayBack",voicePathSingle + "/nin",null));
				
				//2017年6月
				list.add(setRecord("PlayBack",voicePathSingle + "/" + year,null));
				list.add(setRecord("PlayBack",voicePathSingle + "/" + month,null));
				
				if(reminderType.equals("1")) {          //电话费
					//的电话费是
					list.add(setRecord("PlayBack",voicePathSingle + "/telephonefees",null));
				}else if(reminderType.equals("2")) {    //电费
					//的电费是
					list.add(setRecord("PlayBack",voicePathSingle + "/electricfees",null));
				}else if(reminderType.equals("3")) {    //水费
					//的水费是
					list.add(setRecord("PlayBack",voicePathSingle + "/waterfees",null));
				}else if(reminderType.equals("4")) {    //燃气费
					//的燃气费是
					list.add(setRecord("PlayBack",voicePathSingle + "/gasfees",null));
				}
				
				//读费用
				//先转换,将费用转换成金钱读数: 如 103.24 返回  1b03d24y
				String moneyStr = StringUtil.numberExchangeToMoney(charge);
				if(!BlankUtils.isBlank(moneyStr)) {   //如果不为空时,开始分解并读取
					char[] chars = moneyStr.toCharArray();
					for(char c:chars) {      
						//组织文件
						list.add(setRecord("PlayBack",voicePathSingle + "/" + c,null));
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
	

}
