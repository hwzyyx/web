package com.callke8.autocall.questionnaire;

import java.util.Date;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;

/**
 * 问卷的问题控制器
 * 
 * @author hwz
 *
 */
public class QuestionController extends Controller implements IController {

	@Override
	public void datagrid() {
		System.out.println("取QuestionController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String questionnaireId = getPara("questionnaireId");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = Question.dao.getQuestionByPaginateToMap(pageNumber, pageSize, "", questionnaireId);
		System.out.println("数据结果：" + map);
		System.out.println("取QuestionController datagrid的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
	}
	
	@Override
	public void add() {
		
		Question question = getModel(Question.class,"question");
		
		String questionnaireId = getPara("questionnaireId");
		question.set("QUESTIONNAIRE_ID",questionnaireId);
		
		//自动生成ID,主要是以时间：年月日+随机四位数
		String questionId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
		question.set("QUESTION_ID", questionId);
		
		boolean b = Question.dao.add(question);
		
		if(b) {
			render(RenderJson.success("添加问题成功!",questionId));
		}else {
			render(RenderJson.error("添加问题失败"));
		}
		
	}


	@Override
	public void delete() {
		
		String questionId = getPara("questionId");
		
		Question question = Question.dao.getQuestionById(questionId);
		
		if(BlankUtils.isBlank(question)) {   //如果没有该记录时，返回错误
			render(RenderJson.error("删除失败,删除的记录不存在!"));
			return;
		}
		
		boolean b = Question.dao.deleteQuestionById(questionId);
		
		if(b) {
			QuestionItem.dao.deleteQuestionItemByQuestionId(questionId);   //如果删除问题成功时，同时删除问题选项
			render(RenderJson.success("删除问题成功!"));
		}else {
			render(RenderJson.error("删除问题失败!"));
		}
		
	}

	@Override
	public void index() {
		
	}

	@Override
	public void update() {
		
		Question question = getModel(Question.class,"question");
		
		if(BlankUtils.isBlank(question)) {
			render(RenderJson.error("修改失败,传入的参数为空!"));
			return;
		}
		
		String questionId = question.getStr("QUESTION_ID");
		String questionDesc = question.get("QUESTION_DESC");
		String voiceId = question.get("VOICE_ID");
		
		boolean b = Question.dao.update(questionDesc, questionId,voiceId);
		
		if(b) {
			render(RenderJson.success("修改问题成功!"));
		}else {
			render(RenderJson.error("修改问题失败!"));
		}
		
	}
	
	

}
