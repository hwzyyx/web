package com.callke8.autocall.questionnaire;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;

public class QuestionItemController extends Controller implements IController{

	@Override
	public void datagrid() {
		
		String questionId = getPara("questionId");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		renderJson(QuestionItem.dao.getQuestionItemByPaginateToMap(pageNumber, pageSize, questionId));
	}
	
	
	
	@Override
	public void add() {
		
		QuestionItem questionItem = getModel(QuestionItem.class,"questionItem");
		
		String questionId = getPara("questionId");
		questionItem.set("QUESTION_ID",questionId);
		
		Integer itemCode = Integer.valueOf(questionItem.get("ITEM_CODE").toString());   //取出响应按键,用于测试是否有相同的响应按键
		
		QuestionItem searchQuestionItem = QuestionItem.dao.getQuestionItemById(itemCode, questionId);
		
		if(!BlankUtils.isBlank(searchQuestionItem)) {  //如果查询出来的不为空
			
			render(RenderJson.error("新增选项失败,已存在相同的响应按键!"));
			return;
		}
		
		
		boolean b = QuestionItem.dao.add(questionItem);
		
		if(b) {
			render(RenderJson.success("新增选项成功!"));
		}else {
			render(RenderJson.error("新增选项失败!"));
		}
		
		
	}


	@Override
	public void delete() {
		
		String questionId = getPara("questionId");
		String ids = getPara("ids");
		
		if(BlankUtils.isBlank(ids) || BlankUtils.isBlank(questionId)) {
			render(RenderJson.error("删除失败,请选项问题选项后再执行删除操作!"));
			return;
		}
		
		boolean b = QuestionItem.dao.deleteQuestionItem(ids,questionId);
		
		if(b) {
			render(RenderJson.success("删除选项成功!"));
		}else {
			render(RenderJson.error("删除选项失败!"));
		}
		
	}


	@Override
	public void update() {
		
		String questionId = getPara("questionId");   //获取问题ID
		QuestionItem questionItem = getModel(QuestionItem.class,"questionItem");
		
		if(BlankUtils.isBlank(questionItem)) {
			render(RenderJson.error("修改失败,传入的参数为空!"));
			return;
		}
		
		Integer itemCode = Integer.valueOf(questionItem.get("ITEM_CODE").toString());
		String itemDesc = questionItem.get("ITEM_DESC");
		
		boolean b = QuestionItem.dao.update(itemCode, itemDesc,questionId);
		
		if(b) {
			render(RenderJson.success("修改选项成功!"));
		}else {
			render(RenderJson.error("修改选项失败!"));
		}
		
	}

	@Override
	public void index() {
		
	}
	
	
	
}
