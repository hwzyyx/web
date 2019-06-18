package com.callke8.autocall.questionnaire;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.autocall.autocalltask.history.AutoCallTaskHistory;
import com.callke8.autocall.voice.Voice;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.ExcelExportUtil;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.NumberUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class QuestionnaireController extends Controller implements IController {

	@Override
	public void index() {
		
		//获取并返回组织代码
		setAttr("orgComboTreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		//语音类型combobox数据返回,有两一个，一个是带请选择，一个不带选择
		setAttr("voiceTypeComboboxDataFor0", CommonController.getComboboxToString("VOICE_TYPE","0"));
		setAttr("voiceTypeComboboxDataFor1", CommonController.getComboboxToString("VOICE_TYPE","1"));
		
		render("list.jsp");
	}
	
	@Override
	public void datagrid() {
		System.out.println("取QuestionnaireController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String questionnaireDesc = getPara("questionnaireDesc");
		String orgCode = getPara("orgCode");
		String startTime = getPara("startTime");   //这是一个dateBox,需要加入时间
		String endTime = getPara("endTime");       //这是一个dateBox,需要加入时间
		
		String createUserCode = null;    //创建的用户ID
		if(!BlankUtils.isBlank(orgCode)) {
			createUserCode = CommonController.getOperIdStringByOrgCode(orgCode,getSession());
		}
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = Questionnaire.dao.getQuestionnaireByPaginateToMap(pageNumber, pageSize, questionnaireDesc, createUserCode,startTime,endTime);
		
		System.out.println("取QuestionnaireController datagrid的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
	}
	
	@Override
	public void add() {
		
		Questionnaire questionnaire = getModel(Questionnaire.class, "questionnaire");
		
		//先检查是否存在相同标题的问卷
		String questionnaireDesc = questionnaire.get("QUESTIONNAIRE_DESC");
		if(!BlankUtils.isBlank(Questionnaire.dao.getQuestionnaire(questionnaireDesc))) {
			render(RenderJson.error("新增问卷失败,已经存在相同的问卷标题!"));
			return;
		}
		
		//自动生成ID，主要是以时间：年月日 + 随机四位数
		String questionnaireId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
		questionnaire.set("QUESTIONNAIRE_ID", questionnaireId);
		
		//设置操作工号
		String operId = String.valueOf(getSession().getAttribute("currOperId"));
		questionnaire.set("CREATE_USERCODE", operId);
		
		//设置操作工号所在的组织编码
		questionnaire.set("ORG_CODE",Operator.dao.getOrgCodeByOperId(operId));
		
		//设置创建时间
		questionnaire.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		
		
		boolean b = Questionnaire.dao.add(questionnaire);
		
		if(b) {
			render(RenderJson.success("添加问卷成功!",questionnaireId));
		}else {
			render(RenderJson.error("添加问题失败!"));
		}
		
	}
	

	@Override
	public void delete() {
		
		String questionnaireId = getPara("questionnaireId");
		
		Questionnaire questionnaire = Questionnaire.dao.getQuestionnaireById(questionnaireId);
		
		if(BlankUtils.isBlank(questionnaire)) {    //如果没有该记录时，返回错误
			
			render(RenderJson.error("删除失败,删除的记录不存在"));
			return;
		}
		
		//删除问卷之前,先查看有没有问题，如果有问题时，不允许删除问卷
		List<Question> questionList = Question.dao.getQuestionByQuestionnaireId(questionnaireId);
		if(!BlankUtils.isBlank(questionList)) {
			render(RenderJson.error("删除失败,删除问卷前请先删除问题!"));
			return;
		}
		
		//删除问卷之前，先查看问卷是否已经被外呼任务引用
		boolean isBeUsed = AutoCallTask.dao.checkQuestionnaireBeUsed(questionnaireId);
		if(isBeUsed) {
			render(RenderJson.error("删除失败,调查问卷已经被自动外呼任务引用,不允许删除!"));
			return;
		}
		
		//删除问卷之前，先查看问卷是否已经被历史外呼任务引用
		isBeUsed = AutoCallTaskHistory.dao.checkQuestionnaireBeUsed(questionnaireId);
		if(isBeUsed) {
			render(RenderJson.error("删除失败,调查问卷已经被历史外呼任务引用,不允许删除!"));
			return;
		}
		
		boolean b = Questionnaire.dao.delete(questionnaire);
		
		if(b) {
			render(RenderJson.success("删除问卷成功!"));
		}else {
			render(RenderJson.error("删除问卷失败!"));
		}
	}

	@Override
	public void update() {
		
		Questionnaire questionnaire = getModel(Questionnaire.class, "questionnaire");
		
		String questionnaireId = questionnaire.get("QUESTIONNAIRE_ID");
		String questionnaireDesc = questionnaire.get("QUESTIONNAIRE_DESC");
		
		//在修改之前,先检查是否已经存在了相同的问卷标题
		//先根据上传的问卷标题，从数据取出问卷，如果存在时，先看ID，是否与上传的相同
		Questionnaire q = Questionnaire.dao.getQuestionnaire(questionnaireDesc);
		if(!BlankUtils.isBlank(q)) {
			
			String qId = q.get("QUESTIONNAIRE_ID");
			if(!BlankUtils.isBlank(qId) && !qId.equals(questionnaireId)) {
				render(RenderJson.error("修改问卷失败!已存在相同的问卷标题!"));
				return;
			}
		}
		
		boolean b = Questionnaire.dao.update(questionnaireDesc, questionnaireId);
		
		if(b) {
			render(RenderJson.success("修改问卷信息成功!"));
		}else {
			render(RenderJson.error("修改问卷信息失败!"));
		}
		
	}
	
	/**
	 * 问卷预览
	 */
	public void preview() {
		
		StringBuilder msg = new StringBuilder();   //新建信息变量
		
		String questionnaireId = getPara("questionnaireId");    
		
		//根据上传的问卷ID,查询出问卷信息
		Questionnaire questionnaire = Questionnaire.dao.getQuestionnaireById(questionnaireId);
		
		if(BlankUtils.isBlank(questionnaire)) {
			render(RenderJson.success("暂无法预览问卷!"));
			return;
		}
		
		//从问卷信息中,取出问卷标题
		String questionnaireDesc = questionnaire.get("QUESTIONNAIRE_DESC");
		
		//根据卷ID,取出问题集
		List<Question> questionList = Question.dao.getQuestionByQuestionnaireId(questionnaireId); 
		
		//创建一个<table> 用于显示问卷标题信息
		msg.append("<table border='0' cellspacing='0' cellpadding='0' style='width:100%'>");
		msg.append("<tr><td style='padding-top:10px;' align='center'>");
		msg.append("&nbsp;&nbsp;<span style='font-weight:bolder;font-size:14px'>" + questionnaireDesc + "</span>");
		msg.append("<HR style='FILTER:alpha(opacity=100,finishopacity=0,style=3);margin-left:3px;' width='95%' color=#cccccc SIZE=1>");
		msg.append("</td></tr>");
		
		int qId = 1;   //问题顺序
		for(Question question:questionList)  {
			System.out.println(question);
			
			String questionId = question.get("QUESTION_ID");       //取出问题ID
			String questionDesc = question.get("QUESTION_DESC");   //取出问题内容
			String voiceId = question.get("VOICE_ID");             //取出语音ID
			
			Voice voice = Voice.dao.getVoiceByVoiceId(voiceId);    //查询语音信息
			
			//设置试听的路径
			String path =  ParamConfig.paramConfigMap.get("paramType_4_voicePath") + "/" + voice.get("FILE_NAME") + "." + voice.get("MIME_TYPE");
			
			List<QuestionItem> questionItemList = QuestionItem.dao.getQuestionItemByQuestionId(questionId); //根据问题ID,取出问题的所有的选项
			
			//显示问题(开始)
			//-------------------------
			msg.append("<tr><td style='padding-top:5px;'>");
			msg.append("&nbsp;&nbsp;" + qId + "." + questionDesc);
			//显示音乐播放器(结束)
			msg.append("</td></tr>");
			//显示问题(结束)
			
			msg.append("<tr><td style='padding-top:5px;'>");
			for(QuestionItem questionItem:questionItemList) {
				
				String itemCode = String.valueOf(questionItem.get("ITEM_CODE"));
				String itemDesc = questionItem.get("ITEM_DESC");
				
				msg.append("&nbsp;&nbsp;&nbsp;&nbsp;<input name='question" + qId + "' id='question" + qId + itemCode + "' type='radio'/>&nbsp;<label for='question" +qId + itemCode + "'>" + itemDesc + "</label><br/>" );
			}
			msg.append("</td></tr>");
			qId++;
		}
		
		msg.append("</table>");
		System.out.println(msg.toString());
		render(RenderJson.success(msg.toString()));
		
	}
	
	
	/**
	 * 问卷调查结果情况
	 */
	public void surveyResultPreview() {
		
		StringBuilder msg = new StringBuilder();   //新建信息变量
		
		String questionnaireId = getPara("questionnaireId");
		String taskId = getPara("taskId");                        //取得任务的ID
		
		//根据上传的问卷ID,查询出问卷信息
		Questionnaire questionnaire = Questionnaire.dao.getQuestionnaireById(questionnaireId);
		
		if(BlankUtils.isBlank(questionnaire)) {
			render(RenderJson.success("暂无法预览问卷!"));
			return;
		}
		
		//从问卷信息中,取出问卷标题
		String questionnaireDesc = questionnaire.get("QUESTIONNAIRE_DESC");
		
		//根据卷ID,取出问题集
		List<Question> questionList = Question.dao.getQuestionByQuestionnaireId(questionnaireId); 
		
		String validUrlInfo = "<a href='#' onclick='javascript:exportTelDataToExcel(" + taskId + "," + questionnaireId + ",1)'>导出客户回复明细</a>";
		//定义一个导出无效回复的链接
		String invalidUrlInfourlInfo = "<a href='#' onclick='javascript:exportTelDataToExcel(" + taskId + "," + questionnaireId + ",2)'>导出无效回复</a>";
		
		//创建一个<table> 用于显示问卷标题信息
		msg.append("<table border='0' cellspacing='0' cellpadding='0' style='width:100%'>");
		msg.append("<tr><td style='padding-top:10px;' align='center'>");
		msg.append("&nbsp;&nbsp;<span style='font-weight:bolder;font-size:14px'>" + questionnaireDesc + "</span><br>");
		msg.append("&nbsp;&nbsp;" + validUrlInfo + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + invalidUrlInfourlInfo + "<br>");
		msg.append("<HR style='FILTER:alpha(opacity=100,finishopacity=0,style=3);margin-left:3px;' width='95%' color=#cccccc SIZE=1>");
		msg.append("</td></tr>");
		
		int qId = 1;   //问题顺序
		for(Question question:questionList)  {
			System.out.println(question);
			
			String questionId = question.get("QUESTION_ID");       //取出问题ID
			String questionDesc = question.get("QUESTION_DESC");   //取出问题内容
			String voiceId = question.get("VOICE_ID");             //取出语音ID
			
			Voice voice = Voice.dao.getVoiceByVoiceId(voiceId);    //查询语音信息
			
			//设置试听的路径
			String path =  ParamConfig.paramConfigMap.get("paramType_4_voicePath") + "/" + voice.get("FILE_NAME") + "." + voice.get("MIME_TYPE");
			
			List<QuestionItem> questionItemList = QuestionItem.dao.getQuestionItemByQuestionId(questionId); //根据问题ID,取出问题的所有的选项
			
			//在显示问题之前,先计算这个问题的所有回复和有效回复的数量（所谓有效回复，是指在回复结果表中，属于该任务，且回复的结果与问题选项对应，如果回复的不是问题选项就不算有效回复）
			int validRespondCount = 0;   //默认设置有效回复数量为0
			String itemCodeList = "";
			for(QuestionItem questionItem:questionItemList) {
				itemCodeList += String.valueOf(questionItem.get("ITEM_CODE")) + ",";
			}
			
			if(!BlankUtils.isBlank(itemCodeList)) {    //如果itemCode 不为空时，去掉最后一个 逗号
				itemCodeList = itemCodeList.substring(0, itemCodeList.length()-1);
			}
			
			if(!BlankUtils.isBlank(itemCodeList)) {    //如果不为空时，我们去数据库查询有效的回复数量
				System.out.println("itemCodeList:=====" + itemCodeList);
				validRespondCount = QuestionnaireRespond.dao.getRespondCount(taskId, questionId, itemCodeList);    //取得有效回复数量
			}
			
			//再取出这个问题的所有回复量，无论是否是有效回复
			int allRespondCount = QuestionnaireRespond.dao.getRespondCount(taskId,questionId,null);
			String respondCountNotice = "";
			if(allRespondCount == validRespondCount) {    //如果总回复数量和有效回复量是一样时，则不区分所有回复，有效回复11
				respondCountNotice = "<span style='color:green;'>(回复量：" + allRespondCount + ")</span>";
			}else {
				int differenceValue = allRespondCount - validRespondCount;   //计算两者的差值
				respondCountNotice = "<span style='color:red;'>(回复量：" + allRespondCount + "，有效：" + validRespondCount + "，无效：" + differenceValue + ")</span>";
			}
			
			//显示问题(开始)
			//-------------------------
			msg.append("<tr><td style='padding-top:5px;'>");
			msg.append("&nbsp;&nbsp;" + qId + "." + questionDesc + respondCountNotice);
			//显示音乐播放器(结束)
			msg.append("</td></tr>");
			//显示问题(结束)
			
			msg.append("<tr><td style='padding-top:5px;'>");
			//显示问题的选项
			for(QuestionItem questionItem:questionItemList) {
				
				String itemCode = String.valueOf(questionItem.get("ITEM_CODE"));
				String itemDesc = questionItem.get("ITEM_DESC");
				
				//查询和返回当前问题选项的回复数量，并计算与有效回复数量的占比情况
				String itemCountAndPercent = "";     //定义一个变量，存储当前选择的数量和百分比 
				if(validRespondCount==0) {   //如果有效数量为0，就没有必要去查询
					itemCountAndPercent = "<span style='margin-left:100px;'>(数量：0,占比：0%)</span>";
				}else {
					int itemRespondCount = QuestionnaireRespond.dao.getRespondCount(taskId, questionId,itemCode);
					//计算百分比
					String percent = NumberUtils.calculatePercent(itemRespondCount, validRespondCount);
					itemCountAndPercent = "<span style='margin-left:100px;'>(数量：" + itemRespondCount + ",占比：" + percent + "%)</span>";
				}
				
				msg.append("&nbsp;&nbsp;&nbsp;&nbsp;<input name='question" + qId + "' id='question" + qId + itemCode + "' type='radio'/>&nbsp;<label for='question" +qId + itemCode + "'>" + itemDesc + itemCountAndPercent + "</label><br/>" );
			}
			msg.append("</td></tr>");
			qId++;
		}
		
		msg.append("</table>");
		
		render(RenderJson.success(msg.toString()));
		
	}
	
	/**
	 * 导出号码数据到Excel
	 */
	public void exportTelData() {
		
		String taskId = getPara("taskId");                  		// 任务Id
		String questionnaireId = getPara("questionnaireId");        // 问卷ID
		String isSearchHistoryCallTask = getPara("isSearchHistoryCallTask");       //是否查询历史任务
		
		//根据上传的问卷ID,查询出问卷信息
		Questionnaire questionnaire = Questionnaire.dao.getQuestionnaireById(questionnaireId);
		
		if(BlankUtils.isBlank(questionnaire)) {
			render(RenderJson.success("暂无法预览问卷!"));
			return;
		}
		
		//从问卷信息中,取出问卷标题
		String questionnaireDesc = questionnaire.get("QUESTIONNAIRE_DESC");
		
		//根据卷ID,取出问题集
		List<Question> questionList = Question.dao.getQuestionByQuestionnaireId(questionnaireId);
		List<Record> exportExcelDataList = new ArrayList<Record>();
		
		int qId = 1;   //问题顺序
		for(Question question:questionList) {    //遍历问题
			
			String questionId = question.get("QUESTION_ID");       //取出问题ID
			String questionDesc = qId + "." + question.get("QUESTION_DESC");   //取出问题内容
			
			List<QuestionItem> questionItemList = QuestionItem.dao.getQuestionItemByQuestionId(questionId); //根据问题ID,取出问题的所有的选项
			
			for(QuestionItem questionItem:questionItemList) {      //遍历问题的选项
				
				String itemCode = String.valueOf(questionItem.get("ITEM_CODE"));
				String itemDesc = questionItem.get("ITEM_DESC");
				
				//取得选择该选项的用户的telId列表, 以逗号分隔： 123,1223,343
				String telIdList =  QuestionnaireRespond.dao.getTelIdListByCondition(taskId, questionId, itemCode);
				
				if(!BlankUtils.isBlank(telIdList)) {     //只有不为空时，才查询任务的号码列表
					List<Record> autoCallTaskTelephoneList = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneByTelIdList(telIdList, isSearchHistoryCallTask,questionDesc,itemDesc);
					if(!BlankUtils.isBlank(autoCallTaskTelephoneList)) {
						exportExcelDataList.addAll(autoCallTaskTelephoneList);
					}
				}
				
			}
			
			qId++;
		}
		
		String fileName = "export.xls";
		String sheetName = questionnaireDesc;
		
		ExcelExportUtil export = new ExcelExportUtil(exportExcelDataList, getResponse());
		
		String[] headers = {"问题内容","问题选项","客户姓名","电话号码","省份","城市","外呼时间","通话时长"};
		String[] columns = {"QUESTION_DESC","ITEM_DESC","CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","LOAD_TIME","BILLSEC"};
		
		export.headers(headers).columns(columns).cellWidth(200).sheetName(sheetName);
		
		export.fileName(fileName).execExport();
		
	}
	
	/**
	 * 导出号码数据到Excel, 无效回复导出
	 */
	public void exportTelDataForInvalid() {
		
		String taskId = getPara("taskId");                  		// 任务Id
		String questionnaireId = getPara("questionnaireId");        // 问卷ID
		String isSearchHistoryCallTask = getPara("isSearchHistoryCallTask");       //是否查询历史任务
		
		//根据上传的问卷ID,查询出问卷信息
		Questionnaire questionnaire = Questionnaire.dao.getQuestionnaireById(questionnaireId);
		
		if(BlankUtils.isBlank(questionnaire)) {
			render(RenderJson.success("暂无法预览问卷!"));
			return;
		}
		
		//从问卷信息中,取出问卷标题
		String questionnaireDesc = questionnaire.get("QUESTIONNAIRE_DESC");
		
		//根据卷ID,取出问题集
		List<Question> questionList = Question.dao.getQuestionByQuestionnaireId(questionnaireId);
		List<Record> exportExcelDataList = new ArrayList<Record>();
		
		int qId = 1;   //问题顺序
		for(Question question:questionList) {    //遍历问题
			
			String questionId = question.get("QUESTION_ID");       //取出问题ID
			String questionDesc = qId + "." + question.get("QUESTION_DESC");   //取出问题内容
			
			List<QuestionItem> questionItemList = QuestionItem.dao.getQuestionItemByQuestionId(questionId); //根据问题ID,取出问题的所有的选项
			
			
			String itemCodeList = "";    //定义 一个变量，用于存储该问题的所有选项的ItemCode 列表，用逗号分隔。如：1,2,3
			for(QuestionItem questionItem:questionItemList) {      //遍历问题的选项
				String itemCode = String.valueOf(questionItem.get("ITEM_CODE"));
				itemCodeList += itemCode + ",";
			}
			
			if(!BlankUtils.isBlank(itemCodeList)) {    //删除最后一个逗号
				itemCodeList = itemCodeList.substring(0, itemCodeList.length()-1);
			}
			
			if(!BlankUtils.isBlank(itemCodeList)) {    //如果不为空时，取出当前问题的所有选项外的选择的客户号码ID列表
				
				String telIdList = QuestionnaireRespond.dao.getTelIdListByConditionForNotIntRespond(taskId, questionId, itemCodeList);
				
				if(!BlankUtils.isBlank(telIdList)) {
					List<Record> autoCallTaskTelephoneList = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneByTelIdList(telIdList, isSearchHistoryCallTask,questionDesc,"");
					if(!BlankUtils.isBlank(autoCallTaskTelephoneList)) {
						exportExcelDataList.addAll(autoCallTaskTelephoneList);
					}
				}
				
				
			}
			
			qId++;
		}
		
		String fileName = "export.xls";
		String sheetName = questionnaireDesc + "-无效回复数据";
		
		ExcelExportUtil export = new ExcelExportUtil(exportExcelDataList, getResponse());
		
		String[] headers = {"问题内容","客户姓名","电话号码","省份","城市","外呼时间","通话时长"};
		String[] columns = {"QUESTION_DESC","CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","LOAD_TIME","BILLSEC"};
		
		export.headers(headers).columns(columns).cellWidth(200).sheetName(sheetName);
		
		export.fileName(fileName).execExport();
		
	}

}
