package com.callke8.autocall.questionnaire;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

@SuppressWarnings("serial")
public class QuestionItem extends Model<QuestionItem> {
	
	public static final long serialVersionId = 1L;
	
	public static QuestionItem dao = new QuestionItem();
	
	/**
	 * 以分页的方式查询问题选项
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param questionId
	 * @return
	 */
	public Page<Record> getQuestionItemByPaginate(int pageNumber,int pageSize,String questionId) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[2];
		int index = 0;
		
		sb.append("from ac_question_item where 1=1");
		
		if(!BlankUtils.isBlank(questionId)) {
			sb.append(" and QUESTION_ID=?");
			pars[index] = questionId;
			index++;
		}
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select * ",sb.toString() + " ORDER BY ITEM_CODE ASC", ArrayUtils.copyArray(index,pars));
		return p;
	}
	
	/**
	 * 以分页方式查询问题选项
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param questionId
	 * @return
	 */
	public Map getQuestionItemByPaginateToMap(int pageNumber,int pageSize,String questionId) {
		Map map = new HashMap();

		if(BlankUtils.isBlank(questionId)) {
			
			map.put("total", 0);
			map.put("rows", null);
			
			return map;
		}
		
		Page<Record> p = getQuestionItemByPaginate(pageNumber,pageSize,questionId);
		
		int total = p.getTotalRow();
		
		map.put("total", total);
		map.put("rows",p.getList());
		
		return map;
	}
	
	/**
	 * 添加问题选项
	 * 
	 * @param questionItem
	 * @return
	 */
	public boolean add(QuestionItem questionItem) {
		
		boolean b = false;
		
		if(questionItem.save()) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 更新问题选项
	 * 
	 * @param itemCode
	 * @param itemDesc
	 * @return
	 */
	public boolean update(Integer itemCode,String itemDesc,String questionId) {
		
		boolean b = false;
		
		
		String sql = "update ac_question_item set ITEM_DESC=? where ITEM_CODE=? and QUESTION_ID=?";
		
		if(BlankUtils.isBlank(itemCode) || BlankUtils.isBlank(itemDesc) || BlankUtils.isBlank(questionId)) {   //如果任意一个为空时，返回错误
			return false;
		}
		
		int count = Db.update(sql,itemDesc,itemCode,questionId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 删除问题选项
	 * 
	 * @param ids
	 * 				响应按键集
	 * @param questionId
	 * 		 		当前问题ID
	 * @return
	 */
	public boolean deleteQuestionItem(String ids,String questionId) {
		
		if(BlankUtils.isBlank(ids) || BlankUtils.isBlank(questionId)) {   //如果问题ID，或是响应按键集为空
			return false;
		}
		
		String sql = "delete from ac_question_item where QUESTION_ID=? and ITEM_CODE in(" + ids + ")";
		
		System.out.println("删除的SQL语句为:" + sql);
		
		int count = Db.update(sql, questionId);
		
		if(count > 0) {
			return true;
		}else {
			return false;
		}
		
	}
	
	/**
	 * 根据ID删除问题选项
	 * 
	 * @param questionItemId
	 * @return
	 */
	public boolean deleteQuestionItemById(String questionItemId) {
		
		boolean b = false;
		
		String sql = "delete from ac_question_item where QUESTION_ITEM_ID=?";
		
		int count = Db.update(sql, questionItemId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据问题ID,删除所有的问题选项
	 * 
	 * @param questionId
	 * @return
	 */
	public void deleteQuestionItemByQuestionId(String questionId) {
		
		String sql = "delete from ac_question_item where QUESTION_ID=?";
		Db.update(sql, questionId);
	}
	
	/**
	 * 删除问题选项
	 * 
	 * @param questionItem
	 * @return
	 */
	public boolean delete(QuestionItem questionItem) {
		
		boolean b = false;
		
		if(BlankUtils.isBlank(questionItem)) {
			return false;
		}
		
		String questionItemId = questionItem.get("QUESTION_ITEM_ID");
		
		b = deleteQuestionItemById(questionItemId);
		
		return b;
	}
	
	/**
	 * 根据响应的按键和问题ID,取出选项信息
	 * 
	 * @param itemCode
	 * @param questionId
	 * @return
	 */
	public QuestionItem getQuestionItemById(Integer itemCode,String questionId) {
		
		QuestionItem questionItem = findFirst("select * from ac_question_item where ITEM_CODE=? and QUESTION_ID=?", itemCode,questionId);
		
		return questionItem;
	}
	
	/**
	 * 根据问题ID,取出所有的问题项
	 * 
	 * @param questionId
	 * @return
	 */
	public List<QuestionItem> getQuestionItemByQuestionId(String questionId) {
		
		if(BlankUtils.isBlank(questionId)) {
			return null;
		}
		
		String sql = "select * from ac_question_item where QUESTION_ID=? order by ITEM_CODE ASC";
		
		List<QuestionItem> questionItemList = find(sql, questionId);
		
		return questionItemList;
	}
	
	
	
	
	
	
	
	
	
	
}
