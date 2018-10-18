package com.callke8.autocall.autocalltask;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.autocall.autoblacklist.AutoBlackList;
import com.callke8.autocall.questionnaire.Questionnaire;
import com.callke8.autocall.schedule.Schedule;
import com.callke8.autocall.voice.Voice;
import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.system.param.Param;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.NumberUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AutoCallTask extends Model<AutoCallTask> {
	
	private static final long serialVersionId = 1L;
	
	public static AutoCallTask dao = new AutoCallTask();
	/**
	 * 以分页的方式查询外呼任务
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param taskName
	 * @param orgCode
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Page<Record> getAutoCallTaskByPaginate(int pageNumber,int pageSize,String taskName,String taskType,String reminderType,String taskState,String orgCode,String sendMessage,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[8];
		int index = 0;
		
		sb.append("from ac_call_task where 1=1");
		
		if(!BlankUtils.isBlank(taskName)) {
			sb.append(" and TASK_NAME like ?");
			pars[index] = "%" + taskName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(taskType) && !taskType.equalsIgnoreCase("empty")) {
			sb.append(" and TASK_TYPE=?");
			pars[index] = taskType;
			index++;
		}
		
		if(!BlankUtils.isBlank(reminderType) && !reminderType.equalsIgnoreCase("empty")) {
			sb.append(" and REMINDER_TYPE=?");
			pars[index] = reminderType;
			index++;
		}
		
		if(!BlankUtils.isBlank(taskState) && !taskState.equalsIgnoreCase("empty")) {
			sb.append(" and TASK_STATE=?");
			pars[index] = taskState;
			index++;
		}
		
		if(!BlankUtils.isBlank(orgCode)) {
			
			String ocs = "";   //组织 in 的内容，即是 select * from questionnaire where ORG_CODE in ('a','b');
			String[] orgCodes = orgCode.split(",");    //分隔组织代码
			
			for(String oc:orgCodes) {
				ocs += "\'" + oc + "\',"; 
			}
			
			ocs = ocs.substring(0,ocs.length()-1);     //删除最后一个逗号
			
			sb.append(" and ORG_CODE in(" + ocs + ")");
		}
		
		if(!BlankUtils.isBlank(sendMessage) && !sendMessage.equalsIgnoreCase("empty")) {
			sb.append(" and SEND_MESSAGE=?");
			pars[index] = sendMessage;
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CREATE_TIME>?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CREATE_TIME<?");
			pars[index] = endTime + " 23:59:59";
			index++;
		}
		
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString() + " ORDER BY TASK_ID DESC",ArrayUtils.copyArray(index,pars));
        
		return p;
	}
	
	public Map getAutoCallTaskByPaginateToMap(int pageNumber,int pageSize,String taskName,String taskType,String reminderType,String taskState,String orgCode,String sendMessage,String startTime,String endTime) {
		
		Page<Record> p = getAutoCallTaskByPaginate(pageNumber, pageSize, taskName,taskType,reminderType,taskState,orgCode,sendMessage,startTime, endTime);
		
		int total = p.getTotalRow();   //取出总数据量
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:p.getList()) {
			
			//设置组织名字
			String oc = r.getStr("ORG_CODE");   //得到组织编码
			Record o = Org.dao.getOrgByOrgCode(oc);
			if(!BlankUtils.isBlank(o)) {
				r.set("ORG_CODE_DESC", o.get("ORG_NAME"));
			}
			
			//设置操作员名字（工号）
			String uc = r.getStr("CREATE_USERCODE");   //取出创建人工号
			Operator oper = Operator.dao.getOperatorByOperId(uc);
			if(!BlankUtils.isBlank(oper)) {
				r.set("CREATE_USERCODE_DESC", oper.get("OPER_NAME") + "(" + uc + ")");
			}
			
			//设置主叫号码（由于是从数据字典选择的，要从字典中取出真实号码）
			String callerIdDesc = MemoryVariableUtil.getDictName("CALLERID", r.getStr("CALLERID"));
			r.set("CALLERID_DESC", callerIdDesc);
			
			//设置调度计划名称
			String scheduleId = r.get("SCHEDULE_ID");   //取出调度计划ID
			Schedule schedule = Schedule.dao.getScheduleById(scheduleId);   //从数据库中根据ID，取出调度计划信息
			if(!BlankUtils.isBlank(schedule)) {
				r.set("SCHEDULE_NAME", schedule.get("SCHEDULE_NAME"));
			}
			
			r.set("schedule", schedule);
			
			//设置普通外呼的语音文件的描述
			String commonVoiceId = r.get("COMMON_VOICE_ID");
			if(!BlankUtils.isBlank(commonVoiceId)) {
				Voice voice = Voice.dao.getVoiceByVoiceId(commonVoiceId);
				if(!BlankUtils.isBlank(voice)) {
					r.set("COMMON_VOICE_DESC", voice.get("VOICE_DESC"));
				}
			}
			
			//设置调查问卷的描述
			String questionnaireId = r.get("QUESTIONNAIRE_ID");
			if(!BlankUtils.isBlank(questionnaireId)) {
				Questionnaire questionnaire = Questionnaire.dao.getQuestionnaireById(questionnaireId);
				if(!BlankUtils.isBlank(questionnaire)) {
					r.set("QUESTIONNAIRE_DESC",questionnaire.get("QUESTIONNAIRE_DESC"));
				}
			}
			
			//如果任务处于已激活时，再查看该任务是否处已经开始执行中
			String tState = r.get("TASK_STATE");    //取得当前任务的状态
			if(tState.equals("2")) {        //如果任务的状态为2,即是已激活时
				
				String planStartTime = r.get("PLAN_START_TIME").toString();   //计划开始时间
				String planEndTime = r.get("PLAN_END_TIME").toString();       //计划结束时间
				String currTime = DateFormatUtils.getFormatDate(); //当前时间
				
				long planStartTimeSeconds = DateFormatUtils.parseDate(planStartTime).getTime();//开始时间的秒数
				long planEndTimeSeconds = DateFormatUtils.parseDate(planEndTime).getTime();//结束时间的秒数
				long currentTimeSeconds = DateFormatUtils.parseDate(currTime).getTime();//当前时间的秒数
				
				if(currentTimeSeconds>planEndTimeSeconds) {   //当前时间超过结束时间（即过期）
					r.set("runningNotice", "<span style='color:#FF0000'>已过期</span>");
				}else if(currentTimeSeconds < planStartTimeSeconds) {  //当前时间小于开始时间时(未开始)
					r.set("runningNotice", "<span style='color:#FF7F00'>未开始</span>");
				}else {            //否则，则表示已经到了开始期限
					
					boolean isScheduleActive = Schedule.dao.checkScheduleIsActive(scheduleId);
					if(isScheduleActive) {      //如果当前任务的调度方案处于活跃中时
						//即使当前时间处理活跃中
						r.set("runningNotice","<span style='color:#009900'>执行中</span>");
					}else {
						r.set("isRunning","0");
						r.set("runningNotice","<span style='color:#FF7F00'>未开始</span>");
					}
					
				}
				
			}
			
			//设置开始语音及结束语音的文件描述
			String startVoiceId = r.get("START_VOICE_ID");
			if(!BlankUtils.isBlank(startVoiceId)) {
				Voice startVoice = Voice.dao.getVoiceByVoiceId(startVoiceId);
				if(!BlankUtils.isBlank(startVoice)) {
					r.set("START_VOICE_DESC", startVoice.get("VOICE_DESC"));
				}
			}
			String endVoiceId = r.get("END_VOICE_ID");
			if(!BlankUtils.isBlank(endVoiceId)) {
				Voice endVoice = Voice.dao.getVoiceByVoiceId(endVoiceId);
				if(!BlankUtils.isBlank(endVoice)) {
					r.set("END_VOICE_DESC", endVoice.get("VOICE_DESC"));
				}
			}
			
			//设置黑名单的标题描述
			String blackListId = r.get("BLACKLIST_ID");
			if(!BlankUtils.isBlank(blackListId)) {
				AutoBlackList autoBlackList = AutoBlackList.dao.getAutoBlackListByBlackListId(blackListId);
				if(!BlankUtils.isBlank(autoBlackList)) {
					r.set("BLACKLIST_NAME",autoBlackList.get("BLACKLIST_NAME"));
				}
			}
			
			String taskTypeRs = r.get("TASK_TYPE");             //任务类型
			String reminderTypeRs = r.get("REMINDER_TYPE");     //催缴类型
			if(taskTypeRs.equalsIgnoreCase("3")) {              //如果是催缴型任务
				r.set("TASK_TYPE_DESC", MemoryVariableUtil.getDictName("TASK_TYPE",taskTypeRs) + "(" + MemoryVariableUtil.getDictName("REMINDER_TYPE", reminderTypeRs) + ")");
			}else {
				r.set("TASK_TYPE_DESC", MemoryVariableUtil.getDictName("TASK_TYPE",taskTypeRs));
			}
			
			
			//设置呼叫完成率（已呼数量/全部数量）,其中的已呼数量：是指状态为 不为 0(新建)、1(已载入),3(待重呼)
			String taskId = r.get("TASK_ID");
			int totalCount = AutoCallTaskTelephone.dao.getTelephoneCountByTaskId(taskId);     			//取得任务的号码数量
			int finishCount = AutoCallTaskTelephone.dao.getTelephoneCountByNotInState(taskId, "0,1,3");	//取得不为: 0(新建)、1(已载入),3(待重呼)的数量,即表示已经完成的数量
			r.set("FINISH_RATE", NumberUtils.calculatePercent(finishCount, totalCount));                //设置呼叫完成情况
			
			//是否下发短信
			int sendMessageRs = r.getInt("SEND_MESSAGE");
			if(sendMessageRs==1) {
				r.set("IS_SEND_MESSAGE","是");
			}else {
				r.set("IS_SEND_MESSAGE","否");
			}
			
			newList.add(r);
			
		}
		
		Map map = new HashMap();
		
		map.put("total", total);
		map.put("rows", newList);
		
		return map;
	}
	
	/**
	 * 添加外呼任务
	 * 
	 * @param autoCallTask
	 * @return
	 */
	public boolean add(AutoCallTask autoCallTask) {
		
		boolean b = false;
		
		if(autoCallTask.save()) {
			b = true;
		}
		
		return b;
	}
	
	
	/**
	 * 更新外呼任务
	 * 
	 * @param taskId
	 * @param taskName
	 * @param scheduleId
	 * @param planStartTime
	 * @param planEndTime
	 * @param taskType
	 * @param retryTimes
	 * @param retryInterval
	 * @param commonVoiceId
	 * @param questionnaireId
	 * @param reminderType
	 * @param startVoiceId
	 * @param endVoiceId
	 * @param blackListId
	 * @param callerId
	 * @param sendMessage
	 * @param messageContent
	 * @return
	 */
	public boolean update(String taskId,String taskName,String scheduleId,String planStartTime,String planEndTime,String taskType,Integer retryTimes,Integer retryInterval,String commonVoiceId,String questionnaireId,String reminderType,String startVoiceId,String endVoiceId,String blackListId,String callerId,Integer priority,Integer sendMessage,String messageContent) {
	
		boolean b = false;
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[25];
		int index = 0;
		
		sb.append("update ac_call_task set ");
		
		if(!BlankUtils.isBlank(taskName)) {
			sb.append("TASK_NAME=?");
			pars[index] = taskName;
			index++;
		}
		
		if(!BlankUtils.isBlank(scheduleId)) {
			sb.append(",SCHEDULE_ID=?");
			pars[index] = scheduleId;
			index++;
		}
		
		if(!BlankUtils.isBlank(planStartTime)) {
			sb.append(",PLAN_START_TIME=?");
			pars[index] = planStartTime;
			index++;
		}
		
		if(!BlankUtils.isBlank(planEndTime)) {
			sb.append(",PLAN_END_TIME=?");
			pars[index] = planEndTime;
			index++;
		}
		
		if(!BlankUtils.isBlank(taskType)) {
			sb.append(",TASK_TYPE=?");
			pars[index] = taskType;
			index++;
		}
		
		if(retryTimes>0) {
			sb.append(",RETRY_TIMES=?");
			pars[index] = retryTimes;
			index++;
		}
		
		if(retryInterval>0) {
			sb.append(",RETRY_INTERVAL=?");
			pars[index] = retryInterval;
			index++;
		}
		
		if(!BlankUtils.isBlank(commonVoiceId)) {
			sb.append(",COMMON_VOICE_ID=?");
			pars[index] = commonVoiceId;
			index++;
		}
		
		if(!BlankUtils.isBlank(questionnaireId)) {
			sb.append(",QUESTIONNAIRE_ID=?");
			pars[index] = questionnaireId;
			index++;
		}
		
		if(!BlankUtils.isBlank(reminderType)) {
			sb.append(",REMINDER_TYPE=?");
			pars[index] = reminderType;
			index++;
		}
		
		//开始语音无论是否为空，都要重新设置
		sb.append(",START_VOICE_ID=?");
		pars[index] = startVoiceId;
		index++;
		
		//结束语音无论是否为空，都要重新设置
		sb.append(",END_VOICE_ID=?");
		pars[index] = endVoiceId;
		index++;
		
		//黑名单无论是否为空，都要重新设置
		sb.append(",BLACKLIST_ID=?");
		pars[index] = blackListId;
		index++;
		
		if(!BlankUtils.isBlank(callerId)) {
			sb.append(",CALLERID=?");
			pars[index] = callerId;
			index++;
		}
		
		if(priority > 0) {
			sb.append(",PRIORITY=?");
			pars[index] = priority;
			index++;
		}
		
		if(!BlankUtils.isBlank(sendMessage)) {
			sb.append(",SEND_MESSAGE=?");
			pars[index] = sendMessage;
			index++;
		}
		
		if(!BlankUtils.isBlank(messageContent)) {
			sb.append(",MESSAGE_CONTENT=?");
			pars[index] = messageContent;
			index++;
		}
		
		if(!BlankUtils.isBlank(taskId)) {
			sb.append(" where TASK_ID=?");
			pars[index] = taskId;
			index++;
		}
		
		
		int count = Db.update(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		if(count > 0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 根据外呼任务Id删除任务
	 * 
	 * @param taskId
	 * @return
	 */
	public boolean deleteByTaskId(String taskId) {
		
		boolean b = false;
		int count = 0;
		
		String sql = "delete from ac_call_task where TASK_ID=?";
		
		count = Db.update(sql, taskId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据传入的外呼任务对象删除任务
	 * 
	 * @param autoCallTask
	 * @return
	 */
	public boolean delete(AutoCallTask autoCallTask) {
		
		boolean b = false;
		
		String taskId = autoCallTask.get("TASK_ID");
		
		b = deleteByTaskId(taskId);
		
		return b;
	}
	
	/**
	 * 根据任务名称查找外呼任务
	 * 
	 * @param taskName
	 * @return
	 */
	public AutoCallTask getAutoCallTaskByTaskName(String taskName) {
		
		AutoCallTask autoCallTask = findFirst("select * from ac_call_task where TASK_NAME=?", taskName);
		
		return autoCallTask;
	}
	
	
	/**
	 * 根据任务ID查找外呼任务
	 * 
	 * @param taskId
	 * @return
	 */
	public AutoCallTask getAutoCallTaskByTaskId(String taskId) {
		
		
		AutoCallTask autoCallTask = findFirst("select * from ac_call_task where TASK_ID=?", taskId);
		
		return autoCallTask;
		
	}
	
	/**
	 * 检查黑名单是否已经被引用
	 * 
	 * @param blackListId
	 * @return
	 */
	public boolean checkBlackListBeUsed(String blackListId) {
		
		boolean b = false;
		
		String sql = "select * from ac_call_task where BLACKLIST_ID=?";
		
		List<Record> list = Db.find(sql, blackListId);
		
		if(list.size()>0) {   //当返回一个任务不为空时，则返回true;
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 检查语音是否已经被引用 
	 * 
	 * @param voiceId
	 * @return
	 */
	public boolean checkVoiceBeUsed(String voiceId) {
		
		boolean b = false;
		
		String sql = "select * from ac_call_task where COMMON_VOICE_ID=? OR START_VOICE_ID=? OR END_VOICE_ID=?";
		
		List<Record> list = Db.find(sql,voiceId,voiceId,voiceId);
		
		if(list.size()>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 检查调度方案是否已经被引用 
	 * 
	 * @param scheduleId
	 * @return
	 */
	public boolean checkScheduleBeUsed(String scheduleId) {
		
		boolean b = false;
		
		String sql = "select * from ac_call_task where SCHEDULE_ID=?";
		
		List<Record> list = Db.find(sql,scheduleId);
		
		if(list.size()>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 检查调查问卷是否已经被引用 
	 * 
	 * @param questionnaireId
	 * @return
	 */
	public boolean checkQuestionnaireBeUsed(String questionnaireId) {
		
		boolean b = false;
		
		String sql = "select * from ac_call_task where QUESTIONNAIRE_ID=?";
		
		List<Record> list = Db.find(sql,questionnaireId);
		
		if(list.size()>0) {
			b = true;
		}
		
		return b;
	}
	
	
	/**
	 * 修改任务状态
	 * 
	 * @param taskId
	 * @param newTaskState
	 * @return
	 */
	public boolean changeState(String taskId,String newTaskState) {
		
		boolean b = false;
		
		String sql = "update ac_call_task set TASK_STATE=? where TASK_ID=?";
		
		int count = Db.update(sql,newTaskState,taskId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
		
	}
	
	
	/**
	 * 归档外呼任务
	 * 
	 * @param taskId
	 * @return
	 */
	public boolean archiveCallTask(String taskId) {
		
		boolean b = false;
		
		String sql = "insert into ac_call_task_history select * from ac_call_task where TASK_ID=?";
		
		int count = Db.update(sql,taskId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 保存审核结果
	 * 
	 * @param taskId
	 * 			 任务ID
	 * @param reviewResult
	 * 			 审核结果：1审核通过;2审核不通过
	 * @param reviewAdvice
	 * 			 审核意见
	 * @return
	 */
	public boolean saveReviewResult(String taskId,String reviewResult,String reviewAdvice) {
		
		boolean b = false;
		String newTaskState = reviewResult.equals("1")?"2":"3";   //如果审核结果时，修改任务状态为2(即审核通过),否则修改任务状态为3(即审核不通过)
		
		String sql = "update ac_call_task set TASK_STATE=?,REVIEW_ADVICE=? where TASK_ID=?";
		
		int count = Db.update(sql,newTaskState,reviewAdvice,taskId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	
	/**
	 * 根据任务的状态取出任务
	 * 
	 * @param taskState
	 * 			任务的状态:
	 * 				0:未激活
	 * 				1:激活审核中
	 * 				2:已激活
	 * 				3:审核不通过
	 * 				4:暂停中
	 * 				5:任务停止
	 * @return
	 */
	public List<AutoCallTask> getAutoCallTaskByState(String taskState) {
		
		List<AutoCallTask> list = new ArrayList<AutoCallTask>();
		
		String sql = "select * from ac_call_task where TASK_STATE=?";

		list = find(sql,taskState);
		
		return list;
	}
	
	/**
	 * 根据任务的状态取出任务,而且当前日期是在任务期限内的任务
	 * 
	 * @param taskState
	 * 			任务的状态:
	 * 				0:未激活
	 * 				1:激活审核中
	 * 				2:已激活
	 * 				3:审核不通过
	 * 				4:暂停中
	 * 				5:任务停止
	 * @return
	 */
	public List<AutoCallTask> getAutoCallTaskByStateInPlanTime(String taskState) {
		
		List<AutoCallTask> list = new ArrayList<AutoCallTask>();
		
		String sql = "select * from ac_call_task where TASK_STATE=? and PLAN_START_TIME<=? and PLAN_END_TIME>=?";

		String currDate = DateFormatUtils.getFormatDate();   //当前日期 2017-01-01
		
		list = find(sql,taskState,currDate,currDate);
		
		return list;
		
	}
	
	/**
	 * 取得已经激活、且任务的生效时间处于当时间内的任务
	 * 
	 * @return
	 */
	public List<AutoCallTask> getActiveCallTasks(){
		
		//先根据任务状态：2(即已激活) 取出传的状态且当前日期处于任务期限内的任务列表
		List<AutoCallTask> list = getAutoCallTaskByStateInPlanTime("2");
		List<AutoCallTask> activeList = new ArrayList<AutoCallTask>();   //定义一个活跃的任务
		
		//当已激活任务列表不为空时
		if(!BlankUtils.isBlank(list) && list.size()>0) {   
			
			//循环遍历已激活的外呼任务,主要是过滤调度方案
			for(AutoCallTask autoCallTask:list) {    
				
				//取出调度任务ID
				String scheduleId = autoCallTask.get("SCHEDULE_ID");
				
				//查看当前的调度方案是否处理活跃期
				boolean isScheduleActive = Schedule.dao.checkScheduleIsActive(scheduleId); 
				
				if(isScheduleActive) {    //如果当前任务的调度方案处于激活期
					activeList.add(autoCallTask);   //将任务加入激活列表
				}
				
			}
			
		}
		
		return activeList;
		
	}
	
	/**
	 * 根据传入的任务，取得其统计数据：
	 * 
	 * 主要返回: 已载入、已成功、待重呼、已失败  四种状态的数量
	 * 
	 * @return
	 */
	public Record getStatisticsData(String taskId) {
		
		Record data = new Record();
		data.set("state0Data",0);
		data.set("state1Data",0);
		data.set("state2Data",0);
		data.set("state3Data",0);
		data.set("state4Data",0);
		
		AutoCallTaskTelephone.dao.getStatisticsDataForState(data,taskId);
		
		return data;
	}
	
	/**
	 * 根据条件取出一个任务，主要是用于通过 网络接口提交数据上来创建任务，如果已经存在，直接通过这个方法取出
	 * 
	 * 条件有三：(1)任务类型   (2)催缴类型  （3）当前日期
	 * 
	 * @param taskType
	 * 			任务类型：1普通外呼; 2调查问卷外呼   3：催缴外呼
	 * @param reminderType
	 * 			催缴类型： 1电费   2水费  3电话费  4燃气费 5物业费 6车辆违章  7交警移车  8社保催缴
	 * @param currentDate
	 * 			当前的日期，2018-10-18
	 * @return
	 */
	public AutoCallTask getAutoCallTaskByCondition(String taskType,String reminderType,String currentDate) {
		
		AutoCallTask autoCallTask = null;
		
		String startTime = currentDate + " 00:00:00";
		String endTime = currentDate + " 23:59:59";
		
		String sql = "select * from ac_call_task where TASK_TYPE=? and REMINDER_TYPE=? and CREATE_TIME>? and CREATE_TIME<? order by CREATE_TIME desc";
		
		autoCallTask = findFirst(sql, taskType,reminderType,startTime,endTime);
		
		return autoCallTask;
	}
	
	/**
	 * 创建外呼任务
	 * 
	 * @param taskType
	 * 			任务类型：1普通外呼; 2调查问卷外呼   3：催缴外呼
	 * @param reminderType
	 * 			催缴类型： 1电费   2水费  3电话费  4燃气费 5物业费 6车辆违章  7交警移车  8社保催缴
	 * @param userCode
	 * 			创建者帐号
	 * @param orgCode
	 * 			组织机构代码
	 * 			
	 * @return
	 */
	public AutoCallTask createAutoCallTask(String taskType,String reminderType,String userCode,String orgCode) {
		AutoCallTask autoCallTask = new AutoCallTask();
		
		long times = System.currentTimeMillis();                    //当前的毫秒数
		long afterThreeDayTimes = times + 3 * 24 * 60 * 60 * 1000;      //三天后的毫秒数
	
		//自定义
		autoCallTask.set("TASK_ID",DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000));         									//TASK_ID
		autoCallTask.set("TASK_NAME","外呼任务(" + MemoryVariableUtil.getDictName("REMINDER_TYPE",reminderType) + ")" + DateFormatUtils.formatDateTime(new Date(), "yyyyMMdd"));     //外呼任务名字
		autoCallTask.set("CREATE_TIME",DateFormatUtils.getCurrentDate());       //创建时间
		autoCallTask.set("PLAN_START_TIME",DateFormatUtils.formatDateTime(new Date(times), "yyyy-MM-dd"));
		autoCallTask.set("PLAN_END_TIME",DateFormatUtils.formatDateTime(new Date(afterThreeDayTimes), "yyyy-MM-dd"));
		autoCallTask.set("TASK_STATE","2");       //任务状态直接设置为2，即已激活状态
		autoCallTask.set("TASK_TYPE",taskType);
		autoCallTask.set("REMINDER_TYPE",reminderType);
		autoCallTask.set("RETRY_TIMES","3");
		autoCallTask.set("RETRY_INTERVAL","10");
		autoCallTask.set("PRIORITY","2");                       //优先级
		autoCallTask.set("SEND_MESSAGE","0");
		
		//需要根据实际情况设计
		autoCallTask.set("CALLERID",ParamConfig.paramConfigMap.get("paramType_4_defaultCallerId"));                       //主叫号码
		autoCallTask.set("SCHEDULE_ID",ParamConfig.paramConfigMap.get("paramType_4_defaultScheduleId"));                  //调度
		autoCallTask.set("CREATE_USERCODE",userCode);            //创建者账号
		autoCallTask.set("ORG_CODE",orgCode);              		 //组织机构
		
		boolean b = add(autoCallTask);
		if(!b) {
			autoCallTask = null;
		}
		
		return autoCallTask;
	}
	
}









