package com.callke8.autocall.autocalltask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.autocall.questionnaire.Question;
import com.callke8.autocall.questionnaire.QuestionItem;
import com.callke8.autocall.questionnaire.QuestionnaireRespond;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.ExcelExportUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动外呼结果查询 
 * 
 * @author hwz
 */
public class AutoCallTaskResultController extends Controller implements IController {

	@Override
	public void index() {
		
		//获取并返回组织代码
		setAttr("orgCombotreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		//获取并返回任务类型
		setAttr("taskTypeComboboxData", CommonController.getComboboxToString("TASK_TYPE","1"));
		setAttr("taskStateComboboxData", CommonController.getComboboxToString("AC_TASK_STATE","1"));
		
		
		render("listresult.jsp");
	}
	
	/*
	 * 获取饼图数据
	 */
	public void getPieData() {
		
		
		String taskId = getPara("taskId");    //任务ID
		
		int noCallCount = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("0",taskId);
		int loadCount = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("1",taskId);
		int successCount = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("2",taskId);
		int retryCount = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("3",taskId);
		int failureCount = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneCountByState("4",taskId);
		
		//未处理数据
		Record noCallData = new Record();   
		noCallData.set("name", "未处理");
		noCallData.set("value", noCallCount);

		//已载入数据
		Record loadedData = new Record();   
		loadedData.set("name", "已载入");
		loadedData.set("value", loadCount);
		
		//已成功
		Record successData = new Record();   
		successData.set("name", "已成功");
		successData.set("value", successCount);
		
		//待重呼
		Record retryData = new Record();   
		retryData.set("name", "待重呼");
		retryData.set("value", retryCount);
		
		Record failureData = new Record();   
		failureData.set("name", "已失败");
		failureData.set("value", failureCount);
		
		List<Record> list = new ArrayList<Record>();
		
		list.add(noCallData);
		list.add(loadedData);
		list.add(successData);
		list.add(retryData);
		list.add(failureData);
		
		renderJson(list);
	
	}
	
	//得到客户回复的统计饼图
	public void getRespondPieData() {
		
		
		String questionId = getPara("questionId");     //问题ID
		String taskId = getPara("taskId");             //任务ID
		
		List<Record> list = new ArrayList<Record>();
		
		if(!BlankUtils.isBlank(questionId)) {
			
			//先取出所有的问题项列表
			List<QuestionItem> questionItemList = QuestionItem.dao.getQuestionItemByQuestionId(questionId);
			
			//取出所有回复的结果，并以组的方式返回，每条记录表示当前任务、当前问题的回复情况
			List<Record> questionnaireRespondCountList = QuestionnaireRespond.dao.getQuestionnnaireResppondCountForGroup(taskId, questionId);
			
			//遍历问题的选项
			for(QuestionItem questionItem:questionItemList) {
				
				Record r = new Record();
				
				String itemCode = String.valueOf(questionItem.get("ITEM_CODE"));     //取出问题选项的响应按键
				
				r.set("name", questionItem.get("ITEM_DESC"));
				r.set("itemCode", questionItem.get("ITEM_CODE"));
				r.set("value", 0);      //默认数量为0
				
				//遍历返回的客户回复数量结果,重新设置数量
				for(Record questionnaireRespondCount:questionnaireRespondCountList) {
					
					String respond  = questionnaireRespondCount.get("RESPOND");
					Integer count = Integer.valueOf(questionnaireRespondCount.get("count").toString());
					
					if(itemCode.equalsIgnoreCase(respond)) {
						r.set("value",count);
					}
					
				}
				list.add(r);
			}
			
		}
		
		renderJson(list);
		
	}
	
	public void getQuestionList() {
		
		List<Record> list = new ArrayList<Record>();     
		
		String taskId = getPara("taskId");    //取出任务ID
		
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		
		String questionnaireId = autoCallTask.get("QUESTIONNAIRE_ID");       //取出问卷ID
		
		if(!BlankUtils.isBlank(questionnaireId)) {                           //如果问卷ID不为空时，根据问卷ID，取出问题列表
			
			List<Question> questionList = Question.dao.getQuestionByQuestionnaireId(questionnaireId);    //取出问题列表
			
			int i = 1;       //问题顺序
			
			for(Question question:questionList) {
				
				Record r = new Record();
				String questionId = question.get("QUESTION_ID");
				
				r.set("title", "question" + i);
				r.set("titleDetail","问题 " + i + ":" + question.get("QUESTION_DESC"));
				r.set("onClickTarget","showRespondChart(" + questionId + ")");
				r.set("questionId", questionId);
				
				list.add(r);
				
				i++;
			}
			
		}
		
		
		renderJson(list);
		
	}
	
	
	public void exportExcel() {
		
		String taskId = getPara("taskId");
		String state = getPara("state");
		String telephone = getPara("telephone");
		String clientName = getPara("clientName");
		
		//如果传入的状态为5或是为空时
		if(BlankUtils.isBlank(state) || state.equalsIgnoreCase("5")) {
			state = null;
		}
		
		List<Record> list = AutoCallTaskTelephone.dao.getAutoCallTaskTelephonesByTaskIdAndState(taskId, state, telephone,clientName);
		
		String[] headers = {"电话号码","客户姓名","外呼时间","外呼结果","失败原因","已重试","再次外呼时间"};
		String[] columns = {"TELEPHONE","CLIENT_NAME","OP_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED","NEXT_CALLOUT_TIME"};
		String fileName = "export.xls";
		String sheetName = "外呼结果表";
		int cellWidth = 100;
		
		
		ExcelExportUtil export = new ExcelExportUtil(list,getResponse());
		export.headers(headers).columns(columns).sheetName(sheetName).cellWidth(cellWidth);
		
		export.fileName(fileName).execExport();
		
	}
	
	
	/**
	 * 导出客户回复结果数据
	 */
	public void exportRespondExcel() {
		
		String taskId = getPara("taskId");
		String questionId = getPara("questionId");
		String questionItemValue = getPara("questionItemValue");    //如果该结果为空时，取出所有的回复
		
		
		List<Record> exportRecordlist = new ArrayList<Record>();          //定义一个用于导出数据表List
		
		
		//根据任务ID、问题ID及回复结果，返回回复表中的数据，包括TEL_ID及回复结果
		List<Record> telIdList = QuestionnaireRespond.dao.getTelephoneIdByRespondInfo(taskId, questionId, questionItemValue);
		
		//接下来，就需要从任务号码中取出号码
		//第一步，根据任务ID及状态，取出所有的号码
		List<Record> telephoneList = AutoCallTaskTelephone.dao.getAutoCallTaskTelephonesByTaskIdAndState(taskId, "2", null, null);     //客户有回复时,必须是已经呼通的号码，状态为2
		
		//第二步, 取两组列表的交集，即是两者 TEL_ID 相同的记录，才是真正有效的记录
		//(1) 为了效率考虑,先定义三个变量：    List<String> 用于储存已经呼通的号码ID;   Map<String,String> 用于存放 号码ID对应的号码 及对应的客户姓名
		List<String> successTelIdList = new ArrayList<String>();
		Map<String,String> successTelMap = new HashMap<String,String>();
		Map<String,String> successClientNameMap = new HashMap<String,String>();
		
		for(Record telephone:telephoneList) {
			
			String telId = telephone.get("TEL_ID").toString();
			String tel = telephone.get("TELEPHONE");
			String clientName = telephone.get("CLIENT_NAME").toString();
			
			successTelIdList.add(telId);
			successTelMap.put(telId, tel);
			successClientNameMap.put(telId, clientName);
		}
		
		//(2) 取交集，并生成可导出的数据
		
		Map<String,String> respondInfo = getRespondInfoMapByQuestionId(questionId);       //先取出回复按键对应的问题项
		
		for(Record telIdRecord:telIdList) {     //遍历返回的已经回复的号码ID
			
			String telIdRs = telIdRecord.get("TEL_ID").toString();
			String respondRs = telIdRecord.get("RESPOND").toString();
			
			if(successTelIdList.contains(telIdRs)) {         //如果有交集，即可以生成可用于导出的 Record
				
				Record exportRecord = new Record();         //新建一个导出 Record
				
				exportRecord.set("TELEPHONE", successTelMap.get(telIdRs));       			 //号码
				exportRecord.set("CLIENT_NAME", successClientNameMap.get(telIdRs));          //客户姓名
				exportRecord.set("RESPOND", respondRs);                                      //回复按键
				exportRecord.set("RESPOND_DESC", respondInfo.get(respondRs));
				
				exportRecordlist.add(exportRecord);
				
			}
			
		}
		
		
		//第三步，执行导出操作
		
		String[] headers = {"电话号码","客户姓名","回复按键","回复结果"};
		String[] columns = {"TELEPHONE","CLIENT_NAME","RESPOND","RESPOND_DESC"};
		String fileName = "export.xls";
		String sheetName = "客户回复结果表";
		int cellWidth = 100;
		
		ExcelExportUtil export = new ExcelExportUtil(exportRecordlist,getResponse());
		export.headers(headers).columns(columns).sheetName(sheetName).cellWidth(cellWidth);
		
		export.fileName(fileName).execExport();
		
	}
	
	/**
	 * 根据问题ID，取出回复按键Map , 主要是用于知道，按键对应的描述
	 * 
	 * @param questionId
	 * @return
	 */
	public Map<String,String> getRespondInfoMapByQuestionId(String questionId) {
		
		Map<String,String> map = new HashMap<String,String>();
		
		//先取出所有的问题项列表
		List<QuestionItem> questionItemList = QuestionItem.dao.getQuestionItemByQuestionId(questionId);
		
		for(QuestionItem questionItem:questionItemList) {
			
			String itemCode = questionItem.get("ITEM_CODE").toString();
			String itemDesc = questionItem.get("ITEM_DESC").toString();
			
			map.put(itemCode, itemDesc);
			
		}
		
		return map;
		
	}
	
	
	@Override
	public void delete() {
	}
	
	@Override
	public void add() {
		
	}

	@Override
	public void datagrid() {
		
		
	}



	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	
	
}
