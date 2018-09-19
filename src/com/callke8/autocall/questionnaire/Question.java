package com.callke8.autocall.questionnaire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.autocall.voice.Voice;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.JplayerUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.Render;

public class Question extends Model<Question> {

	public static final long serialVersionId = 1L;
	
	public static Question dao = new Question();
	
	/**
	 * 以分页的方式查询问题
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param questionDesc
	 * @param questionnaireId
	 * @return
	 */
	public Page<Record> getQuestionByPaginate(int pageNumber,int pageSize,String questionDesc,String questionnaireId) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[3];
		int index = 0;
		
		sb.append("from ac_question where 1=1");
		
		if(!BlankUtils.isBlank(questionDesc)) {
			sb.append(" and QUESTION_DESC like ?");
			pars[index] = "%" + questionDesc + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(questionnaireId)) {
			sb.append(" and QUESTIONNAIRE_ID=?");
			pars[index] = questionnaireId;
			index++;
		}
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *",sb.toString() + " ORDER BY QUESTION_ID ASC",ArrayUtils.copyArray(index,pars));
		
		return p;
	}
	
	/**
	 * 以分页的方式查询问题，并以 Map 方式返回
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param questionDesc
	 * @param questionnaireId
	 * @return
	 */
	public Map getQuestionByPaginateToMap(int pageNumber,int pageSize,String questionDesc,String questionnaireId) {
		
		Map map = new HashMap();
		
		if(BlankUtils.isBlank(questionnaireId)) {   //如果传入的问卷Id，为空时，不查数据库
			map.put("total", 0);
			map.put("rows", null);
			return map;
		}
		
		Page<Record> p = getQuestionByPaginate(pageNumber, pageSize, questionDesc, questionnaireId);
		
		int total = p.getTotalRow();
		
		List<Record> newList = new ArrayList<Record>();
		
		int idIndex = 1;
		for(Record r:p.getList()) {
			
			String voiceId = r.get("VOICE_ID");   //先取出语音ID
			if(!BlankUtils.isBlank(voiceId)) {
				
				Voice voice = Voice.dao.getVoiceByVoiceId(voiceId);   //从语音表中根据ID，取出语音记录信息
				r.set("VOICE_DESC", voice.get("VOICE_DESC"));
				
				//设置试听的路径
				String path =  ParamConfig.paramConfigMap.get("paramType_4_voicePath") + "/" + voice.get("FILE_NAME") + "." + voice.get("MIME_TYPE");
				
				r.set("path", path);  //下载地址
				
				//设置播放器外观
				String playerSkin = JplayerUtils.getPlayerSkin(idIndex,"question");
				r.set("playerSkin", playerSkin);
				
				//设置播放器函数
				String playerFunction = JplayerUtils.getPlayerFunction(idIndex, path,"question");
				r.set("playerFunction", playerFunction);
				
				idIndex++;
				
			}
			
			newList.add(r);
			
			
		}
		
		map.put("total",total);
		map.put("rows", newList);
		
		return map;
	}
	
	/**
	 * 添加问题
	 * 
	 * @param question
	 * @return
	 */
	public boolean add(Question question) {
		
		boolean b = false;
		
		if(question.save()) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 更新问题信息
	 * 
	 * @param questionDesc
	 * @param questionId
	 * @return
	 */
	public boolean update(String questionDesc,String questionId,String voiceId) {
		
		boolean b = false;
		
		if(BlankUtils.isBlank(questionDesc) || BlankUtils.isBlank(questionId) || BlankUtils.isBlank(voiceId)) {
			return false;
		}
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[3];
		int index = 0;
		
		sb.append("update ac_question set ");
		
		if(!BlankUtils.isBlank(questionDesc)) {
			sb.append("QUESTION_DESC=?");
			pars[index] = questionDesc;
			index++;
		}
		
		if(!BlankUtils.isBlank(voiceId)) {
			sb.append(",VOICE_ID=?");
			pars[index] = voiceId;
			index++;
		}
		
		if(!BlankUtils.isBlank(questionId)) {
			sb.append(" where QUESTION_ID=?");
			pars[index] = questionId;
			index++;
		}
		
		int count = Db.update(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 删除问题
	 * 
	 * @param question
	 * @return
	 */
	public boolean delete(Question question) {
		
		boolean b = false;
		
		String questionId = question.get("QUESTION_ID");
		
		b = deleteQuestionById(questionId);
		
		return b;
	}
	
	/**
	 * 根据Id，删除问题
	 * 
	 * @param questionId
	 * @return
	 */
	public boolean deleteQuestionById(String questionId) {
		
		boolean b = false;
		int count = 0;
		
		count = Db.update("delete from ac_question where QUESTION_ID=?", questionId);
		
		if(count > 0){
			b = true;
		}
		
		return b;
	}
	
	public Question getQuestionById(String questionId) {
		
		Question question = findFirst("select * from ac_question where QUESTION_ID=?", questionId);
		
		return question;
	}
	
	/**
	 * 根据问卷ID，取出问题集
	 * 
	 * @param questionnaireId
	 * @return
	 */
	public List<Question> getQuestionByQuestionnaireId(String questionnaireId) {
		
		String sql = "select * from ac_question where QUESTIONNAIRE_ID=? ORDER BY QUESTION_ID ASC";
		
		List<Question> list = find(sql, questionnaireId);
		
		return list;
	}
	
	/**
	 * 根据语音ID,查看是否已经被一个或是多个问题选择
	 * 
	 * @param voiceId
	 * @return
	 */
	public boolean checkVoiceIsUsedByVoiceId(String voiceId) {
		
		boolean b = false;
		
		String sql = "select * from ac_question where VOICE_ID=?";
		
		Question question = findFirst(sql, voiceId);
		
		if(!BlankUtils.isBlank(question)) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据问卷ID,查看该问卷问题的个数
	 * 
	 * @param questionnaireId
	 * @return
	 */
	public int getQuestionCountByQuestionnaireId(String questionnaireId) {
		
		String sql = "select count(*) as count from ac_question where QUESTIONNAIRE_ID=?";
		
		Record record = Db.findFirst(sql, questionnaireId);
		
		int count = 0;
		
		if(!BlankUtils.isBlank(record)) {
			count = Integer.valueOf(record.get("count").toString());
		}
		
		return count;
	}
	
	
}
