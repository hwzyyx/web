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

public class Questionnaire extends Model<Questionnaire> {
	
	private static final long serialVersionId = 1L;
	public static Questionnaire dao = new Questionnaire();
	
	/**
	 * 以分页的方式查询问卷
	 * 
	 * @param pageNumber
	 * 				当前页面
	 * @param pageSize
	 * 				每页显示的数据量
	 * @param questionnaireDesc
	 * 				问卷标题
	 * @param orgCode
	 * 				组织编码
	 * @return
	 */
	public Page<Record> getQuestionnaireByPaginate(int pageNumber,int pageSize,String questionnaireDesc,String createUserCode,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("from ac_questionnaire where 1=1");
		
		if(!BlankUtils.isBlank(questionnaireDesc)) {
			sb.append(" and QUESTIONNAIRE_DESC like ?");
			pars[index] = "%" + questionnaireDesc + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(createUserCode)) {
			sb.append(" and CREATE_USERCODE in(" + createUserCode + ")");
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
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString() + " ORDER BY QUESTIONNAIRE_ID DESC",ArrayUtils.copyArray(index,pars));
		                                                                                         
		return p;
	}
	
	/**
	 * 以分页的方式查询问卷,并以 datagrid 的方式以 Map 返回
	 * 
	 * @param pageNumber
	 * 				当前页面
	 * @param pageSize
	 * 				每页显示的数据量
	 * @param questionnaireDesc
	 * 				问卷标题
	 * @param orgCode
	 * 				组织编码
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map getQuestionnaireByPaginateToMap(int pageNumber,int pageSize,String questionnaireDesc,String createUserCode,String startTime,String endTime) {
		
		Page<Record> p = getQuestionnaireByPaginate(pageNumber,pageSize,questionnaireDesc,createUserCode,startTime,endTime);
		
		int total = p.getTotalRow();    //取出总数据量
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:p.getList()) {
			
			String questionnaireId = r.getStr("QUESTIONNAIRE_ID");   //先取出问卷ID
			int questionCount = Question.dao.getQuestionCountByQuestionnaireId(questionnaireId);   //得到该问卷的问题数量
			r.set("QUESTION_COUNT", questionCount);                  //设置问题的数量
			
			
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
			
			newList.add(r);
			
		}
		
		Map map =  new HashMap();
		
		map.put("total", total);
		map.put("rows", newList);
		return map;
	}
	
	/**
	 * 添加问卷信息
	 * 
	 * @param questionnaire
	 * @return
	 */
	public boolean add(Questionnaire questionnaire) {
		
		boolean b = false;
		
		if(questionnaire.save()) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 更新问卷信息
	 * 
	 * @param questionnaireDesc
	 * 				问卷标题
	 * @param questionnaireId
	 * 				问卷ID
	 * @return
	 */
	public boolean update(String questionnaireDesc,String questionnaireId) {
		
		boolean b = false;
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[3];
		int index = 0;
		
		sb.append("update ac_questionnaire set ");
		
		if(!BlankUtils.isBlank(questionnaireDesc)) {
			sb.append("QUESTIONNAIRE_DESC=?");
			pars[index] = questionnaireDesc;
			index++;
		}
		
		if(!BlankUtils.isBlank(questionnaireId)) {
			sb.append(" where QUESTIONNAIRE_ID=?");
			pars[index] = questionnaireId;
			index++;
		}
		
		int count = Db.update(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	
	/**
	 * 根据问卷标题取出问卷
	 * 
	 * @param questionnaireDesc
	 * @return
	 */
	public Questionnaire getQuestionnaire(String questionnaireDesc) {
		
		String sql = "select * from ac_questionnaire where QUESTIONNAIRE_DESC=?";
		
		Questionnaire questionnaire = findFirst(sql, questionnaireDesc);
		
		return questionnaire;
	}
	
	/**
	 * 根据传入的对象，删除问卷信息
	 * 
	 * @param questionnaire
	 * @return
	 */
	public boolean delete(Questionnaire questionnaire) {
		
		boolean b = false;
		
		String questionnaireId = questionnaire.get("QUESTIONNAIRE_ID");
		
		b = deleteByQuestionnaireId(questionnaireId);
		
		return b;
	}
	
	/**
	 * 根据问卷Id,删除问卷信息
	 * 
	 * @param questionnaireId
	 * @return
	 */
	public boolean deleteByQuestionnaireId(String questionnaireId) {
		
		boolean b = false;
		int count = 0;
		
		String sql = "delete from ac_questionnaire where QUESTIONNAIRE_ID=?";
		
		count = Db.update(sql, questionnaireId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	public Questionnaire getQuestionnaireById(String questionnaireId) {
		
		if(BlankUtils.isBlank(questionnaireId)) {
			return null;
		}
		
		Questionnaire questionnaire = findFirst("select * from ac_questionnaire where QUESTIONNAIRE_ID=?",questionnaireId);
		
		return questionnaire;
		
	}
	

}
