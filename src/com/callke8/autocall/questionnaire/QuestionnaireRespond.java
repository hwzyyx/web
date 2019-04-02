package com.callke8.autocall.questionnaire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class QuestionnaireRespond extends Model<QuestionnaireRespond> {
	
	private static final long serialVersionId = 1L;
	public static QuestionnaireRespond dao = new QuestionnaireRespond();
	
	/*
	 * 添加回复
	 */
	public boolean add(QuestionnaireRespond questionnaireRespond) {
		
		boolean b = false;
		
		if(!BlankUtils.isBlank(questionnaireRespond)) {
			questionnaireRespond.save();
		}
		
		return b;
	}
	
	/**
	 * 根据任务ID及问题ID，取得回复按键的数量，并以组的方式返回 
	 * 
	 * @param taskId
	 * @param questionId
	 */
	public List<Record> getQuestionnnaireResppondCountForGroup(String taskId,String questionId) {
		
		List<Record> list = new ArrayList<Record>();
		
		if(BlankUtils.isBlank(taskId) || BlankUtils.isBlank(questionId)) {
			return list;
		}
		
		String sql = "SELECT RESPOND,count(RESPOND) as count from ac_questionnaire_respond where TASK_ID=? and QUESTION_ID=? group by RESPOND;";
		
		list = Db.find(sql,taskId,questionId);
		
		return list;
		
	}
	
	/**
	 * 根据客户回复结果，取出号码ID及回复组成的 Record 的list,  主要是用于客户回复结果导出 excel
	 * 
	 * 如果回复值为空时，返回全部回复状态的值, 但是，还需要去除无效的回复值的记录
	 * 所谓的无效回复值：比如一条调查移动套餐的情况？   1 全球通     2 动感地带  3 神州行   如果客户回复的结果为 4， 这条记录即视为无效回复
	 * 
	 * @param taskId
	 * @param questionId
	 * @param respond
	 * @return
	 */
	public List<Record> getTelephoneIdByRespondInfo(String taskId,String questionId,String respond) {
		
		List<Record> list = new ArrayList<Record>();
		
		if(BlankUtils.isBlank(taskId) || BlankUtils.isBlank(questionId)) {    //如果任务ID或是问题ID为空时,返回空值 
			return list;
		}
		
		String sql = null;
		
		if(!BlankUtils.isBlank(respond)) {
			
			sql = "select TEL_ID,RESPOND from ac_questionnaire_respond where TASK_ID=? and QUESTION_ID=? and RESPOND=?";
			
			list  = Db.find(sql, taskId,questionId,respond);
			
			return list;
			
		}else {
			
			//如果回复值为空时，返回全部回复状态的值, 但是，还需要去除无效的回复值的记录
			//所谓的无效回复值：比如一条调查移动套餐的情况？   1 全球通     2 动感地带  3 神州行   如果客户回复的结果为 4， 这条记录即视为无效回复
			
			sql = "select TEL_ID,RESPOND from ac_questionnaire_respond where TASK_ID=? and QUESTION_ID=?";
			
			list  = Db.find(sql, taskId,questionId);
			
			//接下来，去除无效回复记录
			//先取出所有的问题项列表
			List<QuestionItem> questionItemList = QuestionItem.dao.getQuestionItemByQuestionId(questionId);
			
			List<String> activeRespond = new ArrayList<String>();    //定义一个有效回复列表
			
			for(QuestionItem questionItem:questionItemList) {
				
				activeRespond.add(questionItem.get("ITEM_CODE").toString());      //将全部的有效回复按键加入到列表
				
			}
			
			List<Record> newList = new ArrayList<Record>();     //定义一个新的TEL_ID 及回复 列表，用于保存仅有效回复的结果
			
			for(Record r:list) {            //遍历原返回的结果，只有当回复结果为有效回复，才加入新的列表
				
				String respondRs = r.get("RESPOND").toString();
				
				if(activeRespond.contains(respondRs)) {
					
					newList.add(r);    
					
				}
				
			}
			
			return newList;
			
		}
		
	}
	
	/**
	 * 根据任务ID、号码ID、问题ID,检查是否已经有回复
	 * 如果有回复,返回正确
	 * 
	 * @param taskId
	 * @param telId
	 * @param questionId
	 * @return
	 */
	public boolean isExist(String taskId,int telId,String questionId) {
		
		boolean b = false;
		
		String sql = "select count(RESPOND_ID) as count from ac_questionnaire_respond where TASK_ID=? and TEL_ID=? and QUESTION_ID=?";
		
		Record record = Db.findFirst(sql,taskId,telId,questionId);
		
		int count = 0;
		
		if(!BlankUtils.isBlank(record)) {
			count = Integer.valueOf(record.get("count").toString());
		}
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 更改按键回应
	 * 
	 * @param taskId
	 * @param telId
	 * @param questionId
	 * @param respond
	 * @return
	 */
	public boolean update(String taskId,int telId,String questionId,String respond) {
		
		boolean b = false;
		
		//如果传入的参数为空时
		if(BlankUtils.isBlank(taskId) || telId <= 0 || BlankUtils.isBlank(questionId)) {
			return b;
		}
		
		String sql = "update ac_questionnaire_respond set RESPOND=? where TASK_ID=? and TEL_ID=? and QUESTION_ID=?";
		
		int count = Db.update(sql,respond,taskId,telId,questionId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 取得有效回复的数量
	 * 
	 * @param taskId
	 * 				任务ID
	 * @param questionId
	 * 				问题ID
	 * @param itemCodeList
	 * 				传入的问题对应的选项的对应按键，如果回复的结果与选择的 ItemCode 相同，就表示是有效的回复
	 * @return
	 */
	public int getValidRespondCount(String taskId, String questionId, String itemCodeList) {
		
		String sql = "select count(*) as count from ac_questionnaire_respond where TASK_ID=? and QUESTION_ID=? and RESPOND in(" + itemCodeList + ")";
		
		Record r = Db.findFirst(sql, taskId,questionId);
		
		int count = 0;
		
		if(!BlankUtils.isBlank(r)) {
			count = Integer.valueOf(r.get("count").toString());
		}
		
		return count;
	}

	/**
	 * 根据条件，取出该任务的对应题目的对应回复按键的号码ID
	 * 
	 * @param taskId
	 * @param questionId
	 * @param respond
	 * @return
	 */
	public String getTelIdListByCondition(String taskId,String questionId,String respond) {
		
		String sql = "select TEL_ID from ac_questionnaire_respond where TASK_ID=? and QUESTION_ID=? and RESPOND=?";
		
		List<Record> list = Db.find(sql,taskId,questionId,respond);
		
		String telIdList = "";
		
		for(Record r:list) {
			int telId = r.getInt("TEL_ID");
			telIdList += telId + ",";
		}
		
		if(!BlankUtils.isBlank(telIdList)) {     //去掉最后一个逗号
			telIdList = telIdList.substring(0,telIdList.length()-1);
		}
		
		return telIdList;
	}
	
}
