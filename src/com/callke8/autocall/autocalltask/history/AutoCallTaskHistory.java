package com.callke8.autocall.autocalltask.history;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class AutoCallTaskHistory extends Model<AutoCallTaskHistory> {
	
	private static final long serialVersionId = 1L;
	
	public static AutoCallTaskHistory dao = new AutoCallTaskHistory();
	
	
	/**
	 * 检查黑名单是否已经被历史任务引用
	 * 
	 * @param blackListId
	 * @return
	 */
	public boolean checkBlackListBeUsed(String blackListId) {
		
		boolean b = false;
		
		String sql = "select * from ac_call_task_history where BLACKLIST_ID=?";
		
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
		
		String sql = "select * from ac_call_task_history where COMMON_VOICE_ID=? OR START_VOICE_ID=? OR END_VOICE_ID=?";
		
		List<Record> list = Db.find(sql,voiceId,voiceId,voiceId);
		
		if(list.size()>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 检查调度方案是否已经被引用 
	 * 
	 * @param voiceId
	 * @return
	 */
	public boolean checkScheduleBeUsed(String scheduleId) {
		
		boolean b = false;
		
		String sql = "select * from ac_call_task_history where SCHEDULE_ID=?";
		
		List<Record> list = Db.find(sql,scheduleId);
		
		if(list.size()>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 检查调查问卷是否已经被引用 
	 * 
	 * @param voiceId
	 * @return
	 */
	public boolean checkQuestionnaireBeUsed(String questionnaireId) {
		
		boolean b = false;
		
		String sql = "select * from ac_call_task_history where QUESTIONNAIRE_ID=?";
		
		List<Record> list = Db.find(sql,questionnaireId);
		
		if(list.size()>0) {
			b = true;
		}
		
		return b;
	}
	

}
