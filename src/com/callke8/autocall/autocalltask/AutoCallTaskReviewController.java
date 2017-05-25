package com.callke8.autocall.autocalltask;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.callke8.autocall.questionnaire.Question;
import com.callke8.autocall.schedule.Schedule;
import com.callke8.autocall.voice.Voice;
import com.callke8.common.CommonController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.JplayerUtils;
import com.callke8.utils.RenderJson;
import com.callke8.utils.StringUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class AutoCallTaskReviewController extends Controller {
	
	public void index() {
		
		//获取并返回组织代码
		setAttr("orgCombotreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		//任务类型和任务状态
		setAttr("taskTypeComboboxDataFor0", CommonController.getComboboxToString("TASK_TYPE","0"));
		setAttr("taskTypeComboboxDataFor1", CommonController.getComboboxToString("TASK_TYPE","1"));
		
		setAttr("taskStateComboboxDataFor0", CommonController.getComboboxToString("AC_TASK_STATE","0"));
		setAttr("taskStateComboboxDataFor1", CommonController.getComboboxToString("AC_TASK_STATE","1"));
		
		//主叫号码
		setAttr("callerIdComboboxDataFor0", CommonController.getComboboxToString("CALLERID","0"));
		
		//审核结果 radio
		setAttr("reviewResultRadioData", CommonController.getRadioToString("REVIEW_RESULT"));
		System.out.println("taskTypeComboboxDataFor0:" + getAttr("taskTypeComboboxDataFor0"));
		System.out.println("reviewResultRadioData:" + getAttr("reviewResultRadioData"));
		
		render("reviewlist.jsp");
	}
	
	//保存审核结果
	public void saveReview() {
		
		String taskId = getPara("taskId");
		String reviewResult = getPara("reviewResult");
		String reviewAdvice = getPara("review.reviewAdvice");
		
		//System.out.println("taskId:" + taskId + ",reviewResult:" + reviewResult + ",reviewAdvice:" + reviewAdvice);
		
		
		AutoCallTask act = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		if(BlankUtils.isBlank(act)) {   //如果任务为空
			render(RenderJson.error("审核失败,外呼任务不存在,已被删除或是被归档!"));
			return;
		}
		
		String taskState = act.get("TASK_STATE");   //取出任务状态
		if(!taskState.equals("1")) {    //如果当前任务的状态不为1,即是待审核状态时
			render(RenderJson.error("审核失败,该任务状态为非待审核状态,任务状态已被更改,请查证后再重新操作!"));
			return;
		}
		
		boolean b = AutoCallTask.dao.saveReviewResult(taskId, reviewResult, reviewAdvice);
		if(b) {
			render(RenderJson.success("保存审核结果操作成功!"));
		}else {
			render(RenderJson.error("保存审核结果操作失败!"));
		}
	}
	
	//查询审核提示
	public void reviewNote() {
		
		StringBuilder msg = new StringBuilder();
		
		String taskId = getPara("taskId");
		
		//取出任务信息
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		Schedule schedule = Schedule.dao.getScheduleById(autoCallTask.get("SCHEDULE_ID").toString());   //取出调度任务
		int dateType = schedule.getInt("DATETYPE");     //日期类型 1:每天; 2：星期
		String dateTypeDetail = schedule.get("DATETYPE_DETAIL");   //得到生效的星期天数,数据以逗分隔,如 1,2,3,4,7(即周一，周二，周3，周四，周日)
		
		int count = AutoCallTaskTelephone.dao.getTelephoneCountByTaskId(taskId);
		
		//创建一个<table> 用于显示问卷标题信息
		msg.append("<table border='0' cellspacing='0' cellpadding='0' style='width:100%'>");
		msg.append("<tr><td style='padding-top:5px;' align='center'>");
		msg.append("&nbsp;&nbsp;<span style='font-weight:bolder;font-size:14px'>审核提示</span>");
		msg.append("<HR style='FILTER:alpha(opacity=100,finishopacity=0,style=3);margin-left:3px;' width='95%' color=#cccccc SIZE=1>");
		msg.append("</td></tr>");
		
		//1 显示号码数量
		String countColor = count>0?"#009900":"#FF0000";
		msg.append("<tr><td style='padding-top:5px;padding-left:5px;' align='left'>");
		msg.append("<span style='font-weight:bold;color:" + countColor + "'>1. 该外呼任务的号码数量为: " + count + "</span>");
		msg.append("</td></tr>");
		
		//2 任务的开始日期及结束日期
		String planStartTime = autoCallTask.get("PLAN_START_TIME").toString();
		String planEndTime = autoCallTask.get("PLAN_END_TIME").toString();
		String currTime = DateFormatUtils.getFormatDate();
		
		long planStartTimeSeconds = DateFormatUtils.parseDate(planStartTime).getTime();//开始时间的秒数
		long planEndTimeSeconds = DateFormatUtils.parseDate(planEndTime).getTime();//结束时间的秒数
		long currentTimeSeconds = DateFormatUtils.parseDate(currTime).getTime();//当前时间的秒数
		
		String planTimeColor = "";
		if(currentTimeSeconds>planEndTimeSeconds) {
			planTimeColor = "#FF0000";     //红色
		}else if(currentTimeSeconds < planStartTimeSeconds) {
			planTimeColor = "#009900";     //绿色
		}else {
			planTimeColor = "#FF7F00";     //橙色
		}
		
		msg.append("<tr><td style='padding-top:5px;padding-left:5px;' align='left'>");
		msg.append("<span style='font-weight:bold;color:" + planTimeColor + "'>2.任务期限为" + planStartTime + "至" + planEndTime + ",当前日期为:" + currTime + "</span>");
		msg.append("</td></tr>");
		
		//3 提示外呼任务的限期天数，除去调度计划，剩余天数
		int amountDays = 0;             //期限总天数
		int validAmountDays = 0;         //有效总天数(在设置的总天数上,除去调度计划中失效的星期数)
		
		try {
			//计算任务设置的总天数
			amountDays = DateFormatUtils.daysBetween(planStartTime, planEndTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if(dateType == 1) {   //如果调度任务的日期类型为1，即是每天时,有效总期限与总期限天数一样
			validAmountDays = amountDays;
		}else {
		
			String startCheckTime = "";      //判断的开始日期 
			
			if(currentTimeSeconds>planEndTimeSeconds) {   //如果当时间已经超过了结束日期
				validAmountDays = 0;
			}else {
				
				if(currentTimeSeconds < planStartTimeSeconds) {
					startCheckTime = planStartTime;
				}else {
					startCheckTime = currTime;
				}
				
				int dayOfWeek = DateFormatUtils.getDayOfWeek(startCheckTime);    //返回检查的日期的星期数
				if(StringUtil.containsAny(dateTypeDetail,String.valueOf(dayOfWeek))) {
					validAmountDays++;      //如果任务设置的星期日期包含当前日期时，有效期限天数默认为1
				}
				
				for(int i=0;i<amountDays;i++) {
					
					Date startCheckTimeAfter1Day = DateFormatUtils.addDay(DateFormatUtils.parseDate(startCheckTime),1);
					String startCheckTimeAfter1DayToString = DateFormatUtils.formatDate(startCheckTimeAfter1Day);
					
					long startCheckTimeAfter1DaySeconds = startCheckTimeAfter1Day.getTime();
					
					//System.out.println("检查开始时间startCheckTime:" + startCheckTime + ",往后一天日期startCheckTimeAfter1Day:" + startCheckTimeAfter1Day);
					
					if(startCheckTimeAfter1DaySeconds>planEndTimeSeconds) {
						break;
					}
					
					startCheckTime = startCheckTimeAfter1DayToString;
					
					dayOfWeek = DateFormatUtils.getDayOfWeek(startCheckTime);    //返回检查的日期的星期数
					if(StringUtil.containsAny(dateTypeDetail,String.valueOf(dayOfWeek))) {
						validAmountDays++;      //如果任务设置的星期日期包含当前日期时，有效期限天数默认为1
					}
					
				}
				
				
			}
			
		}
		
		
		String validAmountDaysColor = "#009900";   //有效限期天数
		
		if(validAmountDays <= 0) {
			validAmountDaysColor = "#FF0000";   //有效期限天数为0时，红色
		}else if(validAmountDays < 3) {         
			validAmountDaysColor = "#FF7F00";   //有效期限天数小于3天时,橙色
		}
		
		msg.append("<tr><td style='padding-top:5px;padding-bottom:5px;padding-left:5px;' align='left'>");
		msg.append("<span style='font-weight:bold;color:" + validAmountDaysColor + "'>3. 该任务设置总期限为: " + amountDays + " 天,去除调度计划设置星期数,剩余有效天数为: " + validAmountDays + " 天 </span>");
		msg.append("</td></tr>");
		
		msg.append("</table>");
		
		List<Record> playList = getPlayList(autoCallTask);   //取得播放列表
		String playerListFunction = JplayerUtils.getPlayerListFunction(playList);  //得到播放列表函数
		
		System.out.println("播放列表信息：" + playerListFunction);
		
		render(RenderJson.success(msg.toString(),playerListFunction));
		
	}
	
	/**
	 * 根据外呼任务信息，取得播放列表
	 * 
	 * @param autoCallTask
	 * @return
	 * 		Map<"播放名字","播放路径">
	 */
	public List<Record> getPlayList(AutoCallTask autoCallTask) {
		
		List<Record> list = new ArrayList<Record>();
		
		//查看开始语音是否为空
		String startVoiceId = autoCallTask.get("START_VOICE_ID");
		if(!BlankUtils.isBlank(startVoiceId)) {   //如果开始欢迎语音不为空
			Record startVoiceRecord = Voice.dao.getVoiceForPlayListByVoiceId(startVoiceId,"开始语音");
			if(!BlankUtils.isBlank(startVoiceRecord)) {
				list.add(startVoiceRecord);
			}
		}
		
		//查看任务类型
		String taskType = autoCallTask.get("TASK_TYPE");
		if(taskType.equals("1")) {            //普通外呼
			
			String commonVoiceId = autoCallTask.get("COMMON_VOICE_ID");
			if(!BlankUtils.isBlank(commonVoiceId)) {   //如果开始欢迎语音不为空
				Record commonVoiceRecord = Voice.dao.getVoiceForPlayListByVoiceId(commonVoiceId,"普通语音");
				if(!BlankUtils.isBlank(commonVoiceRecord)) {
					list.add(commonVoiceRecord);
				}
			}
			
		}else if(taskType.equals("2")) {      //调查外呼
			
			String questionnaireId = autoCallTask.get("QUESTIONNAIRE_ID");   //取出问卷ID
			
			if(!BlankUtils.isBlank(questionnaireId)) {   //问卷不为空时
				
				//根据问卷ID，取出所有的问题项
				List<Question> questionList = Question.dao.getQuestionByQuestionnaireId(questionnaireId);
				int index = 1;
				for(Question question:questionList) {     //遍历问题
					
					String questionVoiceId = question.get("VOICE_ID");    //取出问题的语音ID
					if(!BlankUtils.isBlank(questionVoiceId)) {            //语音ID不为空时
						Record questionVoiceRecord = Voice.dao.getVoiceForPlayListByVoiceId(questionVoiceId,"问题" + index);
						
						if(!BlankUtils.isBlank(questionVoiceRecord)) {
							list.add(questionVoiceRecord);
						}
					}
					index++;
				}
			}
			
		}else if(taskType.equals("3")) {      //催缴外呼
			
			String reminderType = autoCallTask.get("REMINDER_TYPE");     //催缴类型
			
			list.add(Voice.dao.getVoiceByFileName("greeting", "wav"));
			
			if(reminderType.equals("1") || reminderType.equals("2") || reminderType.equals("3") || reminderType.equals("4")) {              //电话费
				//话费催缴模板：尊敬的客户您好,您2016年05月的 电话费 是：25.60元
				//您
				list.add(Voice.dao.getVoiceByFileName("nin", "wav"));
				//2017年01月
				list.add(Voice.dao.getVoiceByFileName("2017", "wav"));
				list.add(Voice.dao.getVoiceByFileName("01", "wav"));
				
				if(reminderType.equals("1")) {
					//的电话费是
					list.add(Voice.dao.getVoiceByFileName("telephonefees", "wav"));
				}else if(reminderType.equals("2")) {
					//的电费是
					list.add(Voice.dao.getVoiceByFileName("electricfees", "wav"));
				}else if(reminderType.equals("3")) {
					//的水费是
					list.add(Voice.dao.getVoiceByFileName("waterfees", "wav"));
				}else if(reminderType.equals("4")) {
					//的燃气费是
					list.add(Voice.dao.getVoiceByFileName("gasfees", "wav"));
				}
				//25.60元
				list.add(Voice.dao.getVoiceByFileName("2", "wav"));
				list.add(Voice.dao.getVoiceByFileName("s", "wav"));
				list.add(Voice.dao.getVoiceByFileName("5", "wav"));
				list.add(Voice.dao.getVoiceByFileName("d", "wav"));
				list.add(Voice.dao.getVoiceByFileName("6", "wav"));
				list.add(Voice.dao.getVoiceByFileName("0", "wav"));
				list.add(Voice.dao.getVoiceByFileName("y", "wav"));
				
			}else if(reminderType.equals("5")) {        //物业管理费
				//您
				list.add(Voice.dao.getVoiceByFileName("nin", "wav"));
				//2017年01月至2017年02月
				list.add(Voice.dao.getVoiceByFileName("2017", "wav"));
				list.add(Voice.dao.getVoiceByFileName("01", "wav"));
				list.add(Voice.dao.getVoiceByFileName("zhi", "wav"));
				list.add(Voice.dao.getVoiceByFileName("2017", "wav"));
				list.add(Voice.dao.getVoiceByFileName("02", "wav"));
				
				//的物业管理费是
				list.add(Voice.dao.getVoiceByFileName("propertyfees", "wav"));
				
				//99.60元
				list.add(Voice.dao.getVoiceByFileName("9", "wav"));
				list.add(Voice.dao.getVoiceByFileName("s", "wav"));
				list.add(Voice.dao.getVoiceByFileName("9", "wav"));
				list.add(Voice.dao.getVoiceByFileName("d", "wav"));
				list.add(Voice.dao.getVoiceByFileName("6", "wav"));
				list.add(Voice.dao.getVoiceByFileName("0", "wav"));
				list.add(Voice.dao.getVoiceByFileName("y", "wav"));
				
			}else if(reminderType.equals("6")) {        //车辆违章
				
				//您
				list.add(Voice.dao.getVoiceByFileName("nin", "wav"));
				
				//2017年01月01日
				list.add(Voice.dao.getVoiceByFileName("2017", "wav"));
				list.add(Voice.dao.getVoiceByFileName("01", "wav"));
				list.add(Voice.dao.getVoiceByFileName("01d", "wav"));
				
				//有交通违章行为,请到交警部门接受处理
				list.add(Voice.dao.getVoiceByFileName("trafficnotice", "wav"));
				
			}else if(reminderType.equals("7")) {        //社保催缴
				
				//请及时汇缴
				list.add(Voice.dao.getVoiceByFileName("reminderalert", "wav"));
				
				//2017年01月
				list.add(Voice.dao.getVoiceByFileName("2017", "wav"));
				list.add(Voice.dao.getVoiceByFileName("01", "wav"));
				
				//的社保费
				list.add(Voice.dao.getVoiceByFileName("socialfees", "wav"));
			}
			
		}
		
		
		//查看结束语音是否为空
		String endVoiceId = autoCallTask.get("END_VOICE_ID");
		if(!BlankUtils.isBlank(endVoiceId)) {   //如果开始欢迎语音不为空
			Record endVoiceRecord = Voice.dao.getVoiceForPlayListByVoiceId(endVoiceId,"结束语音");
			if(!BlankUtils.isBlank(endVoiceRecord)) {
				list.add(endVoiceRecord);
			}
		}
		
		return list;
	}
	
	
	
	
	
}
