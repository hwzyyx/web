package com.callke8.autocall.autocalltask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.ExcelExportUtil;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动外呼任务 
 * 
 * @author hwz
 */
public class AutoCallTaskController extends Controller implements IController {

	@Override
	public void index() {
		
		//获取并返回组织代码
		setAttr("orgComboTreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		//语音类型combobox数据返回,有两一个，一个是带请选择，一个不带选择
		setAttr("voiceTypeComboboxDataFor0", CommonController.getComboboxToString("VOICE_TYPE","0"));
		setAttr("voiceTypeComboboxDataFor1", CommonController.getComboboxToString("VOICE_TYPE","1"));
		
		//调度选择搜索日期类型
		setAttr("dateTypeComboboxDataFor0", CommonController.getComboboxToString("DATETYPE","0"));
		setAttr("dateTypeComboboxDataFor1", CommonController.getComboboxToString("DATETYPE","1"));
		
		//任务类型、催缴类型任务状态
		setAttr("taskTypeComboboxDataFor0", CommonController.getComboboxToString("TASK_TYPE","0"));
		setAttr("taskTypeComboboxDataFor1", CommonController.getComboboxToString("TASK_TYPE","1"));
		
		setAttr("reminderTypeComboboxDataFor0", CommonController.getComboboxToString("REMINDER_TYPE","0"));
		setAttr("reminderTypeComboboxDataFor1", CommonController.getComboboxToString("REMINDER_TYPE","1"));
		
		setAttr("taskStateComboboxDataFor0", CommonController.getComboboxToString("AC_TASK_STATE","0"));
		setAttr("taskStateComboboxDataFor1", CommonController.getComboboxToString("AC_TASK_STATE","1"));
		
		//短信状态 combobox , 用于客户号码的页面搜索用
		setAttr("messageStateComboboxDataFor1", CommonController.getComboboxToString("COMMON_MESSAGE_STATE","1"));
		
		//主叫号码
		setAttr("callerIdComboboxDataFor0", CommonController.getComboboxToString("CALLERID","0"));
		
		render("list.jsp");
	}
	

	@Override
	public void datagrid() {
		System.out.println("取AutoCallTaskController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String taskName = getPara("taskName");
		String taskType = getPara("taskType");
		String reminderType = getPara("reminderType");
		String taskState = getPara("taskState");
		String orgCode = getPara("orgCode");
		String sendMessage = getPara("sendMessage");
		
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = AutoCallTask.dao.getAutoCallTaskByPaginateToMap(pageNumber, pageSize, taskName,taskType,reminderType,taskState,orgCode,sendMessage, startTime, endTime);
		
		System.out.println("取AutoCallTaskController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
		
	}
	
	@Override
	public void add() {
		
		AutoCallTask autoCallTask = getModel(AutoCallTask.class,"autoCallTask");
		String messageContentRs = autoCallTask.get("MESSAGE_CONTENT");    //短信内容
		if(!BlankUtils.isBlank(messageContentRs)) {     //如果短信内容不为空，那么表示需要下发短信
			autoCallTask.set("SEND_MESSAGE",1);
		}else {
			autoCallTask.set("SEND_MESSAGE",0);
		}
		
		//设置任务ID
		//自动生成ID，主要是以时间：年月日 + 随机四位数
		String taskId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
		autoCallTask.set("TASK_ID", taskId);
		
		//先检查是否存在相同名字的外呼任务
		String taskName = autoCallTask.get("TASK_NAME");
		if(!BlankUtils.isBlank(AutoCallTask.dao.getAutoCallTaskByTaskName(taskName))) {
			render(RenderJson.error("新增外呼任务失败,已经存在相同的任务名称!"));
			return;
		}
		
		//设置操作工号
		String operId = String.valueOf(getSession().getAttribute("currOperId"));
		autoCallTask.set("CREATE_USERCODE",operId);
		
		//设置组织给编码
		autoCallTask.set("ORG_CODE",Operator.dao.getOrgCodeByOperId(operId));
		
		//设置创建时间
		autoCallTask.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
		
		//设置任务状态
		autoCallTask.set("TASK_STATE", "0");
		
		//判断任务状态,如果为普通任务时，只保存普通语音文件；如果为调查问卷任务时，只保存调查问卷。
		int taskType = Integer.valueOf(autoCallTask.get("TASK_TYPE").toString());
		if(taskType==1) {   //普通任务时，将调查问卷置空
			autoCallTask.set("QUESTIONNAIRE_ID", null);
			autoCallTask.set("REMINDER_TYPE",null);
		}else if(taskType==2) {   //调查问卷任务时，将普通语音文件置空
			autoCallTask.set("COMMON_VOICE_ID", null);
			autoCallTask.set("REMINDER_TYPE",null);
			
			//如果是调查问卷时，即使上传的任务信息有需要下发短信的内容，也要强制不下发内容
			autoCallTask.set("SEND_MESSAGE", 0);
			autoCallTask.set("MESSAGE_CONTENT",null);
			
		}else if(taskType==3) {   //如果为催缴任务时，都置空
			autoCallTask.set("QUESTIONNAIRE_ID", null);
			autoCallTask.set("COMMON_VOICE_ID", null);
		}
		
		boolean b = AutoCallTask.dao.add(autoCallTask);
		
		if(b) {
			render(RenderJson.success("添加任务成功!",taskId));
		}else {
			render(RenderJson.error("添加问题失败!"));
		}
		
	}
	

	@Override
	public void delete() {

		String taskId = getPara("taskId");
		
		AutoCallTask act = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		
		//删除之前，先判断外呼任务是否存在
		if(BlankUtils.isBlank(act)) {
			
			render(RenderJson.error("删除失败,删除的记录不存在"));
			return;
		}
		
		//同时还要判断当前的任务的状态
		
		boolean b = AutoCallTask.dao.deleteByTaskId(taskId);
		
		if(b) {
			//然后删除任务的号码
			AutoCallTaskTelephone.dao.deleteByTaskId(taskId);
			
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
		
	}
	
	/**
	 * 修改任务状态
	 */
	public void changeState() {
		
		String taskId = getPara("taskId");          //任务ID
		String taskState = getPara("taskState");    //原任务状态
		String action = getPara("action");          //动作指令
		String newTaskState = null;                 //创建新的状态变量，用于修改后的状态值
		String actionDesc = "";                     //动作描述
		
		if(BlankUtils.isBlank(taskId) || BlankUtils.isBlank(action)) {
			render(RenderJson.error("操作失败,传入的任务ID或动作指令为空!"));
			return;
		}
		
		AutoCallTask act = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);   //根据任务ID，从数据库中取出任务信息
		if(BlankUtils.isBlank(act)) {    //如果取出的任务为空
			render(RenderJson.error("操作失败,外呼任务已被删除或被归档为历史任务!"));
			return;
		}
		
		String tState = act.get("TASK_STATE");          //先取出任务状态
		
		if(!tState.equalsIgnoreCase(taskState)) {       //如果原状态与取出的状态不一致时，不允许操作，以免造成系统错误!
			render(RenderJson.error("操作失败,任务状态已经被修改或是发生了变化,系统已经重新加载,请重新操作!"));
			return;
		}
		
		if(action.equalsIgnoreCase("archive")) {          //如果是归档操作时
			actionDesc = "任务归档";
			
			boolean b = execArchive(taskId);
			
			if(b) {
				render(RenderJson.success(actionDesc + " 操作成功!"));
			}else {
				render(RenderJson.error(actionDesc + " 操作失败!"));	
			}
			
		}else {                                           //如果是其他的操作时
			
			if(action.equalsIgnoreCase("applyActive")) {               //如果动作指令为 applyActive(即申请激活)
				newTaskState = "1"; 
				actionDesc = "申请激活";
			}else if(action.equalsIgnoreCase("cancelApplyActive")) {   //如果动作为 cancelApplyActive(即取消激活申请) 
				newTaskState = "0";     
				actionDesc = "取消激活";
			}else if(action.equalsIgnoreCase("pause")) {              //如果动作为 pause(即暂停) 
				newTaskState = "4";     
				actionDesc = "暂停任务";
			}else if(action.equalsIgnoreCase("cancelPause")) {        //如果动作为 cancelPause(即重新开始)
				newTaskState = "2";
				actionDesc = "取消暂停";
			}else if(action.equalsIgnoreCase("stop")) {        		  //如果动作为 stop(即结束任务)
				newTaskState = "5";
				actionDesc = "结束任务";
			}
			
			boolean b = AutoCallTask.dao.changeState(taskId, newTaskState);
			
			if(b) {
				render(RenderJson.success(actionDesc + " 操作成功"));
			}else {
				render(RenderJson.error(actionDesc + " 操作失败"));
			}
			
		}
		
	}

	@Override
	public void update() {
		
		AutoCallTask autoCallTask = getModel(AutoCallTask.class,"autoCallTask");
		
		String messageContentRs = autoCallTask.get("MESSAGE_CONTENT");    //短信内容
		if(!BlankUtils.isBlank(messageContentRs)) {     //如果短信内容不为空，那么表示需要下发短信
			autoCallTask.set("SEND_MESSAGE",1);
		}else {
			autoCallTask.set("SEND_MESSAGE",0);
		}
		
		String taskId = autoCallTask.get("TASK_ID");
		String taskName = autoCallTask.get("TASK_NAME");
		
		//修改之前，先检查是否已经存在相同的任务名字
		AutoCallTask checkAct = AutoCallTask.dao.getAutoCallTaskByTaskName(taskName);
		if(!BlankUtils.isBlank(checkAct)) {
			String tId = checkAct.get("TASK_ID");
			if(!BlankUtils.isBlank(tId)&&!tId.equalsIgnoreCase(taskId)) {    //如果查询出来的ID与更新的Id不一样时，表示存在相同的任务名
				render(RenderJson.error("修改任务失败!已存在相同的任务名字!"));
				return;
			}
		}
		
		String taskType = autoCallTask.get("TASK_TYPE");
		if(taskType.equalsIgnoreCase("1")) {   //普通任务，将调查问卷置空
			autoCallTask.set("QUESTIONNAIRE_ID",null);
			autoCallTask.set("REMINDER_TYPE",null);
		}else if(taskType.equalsIgnoreCase("2")) {  //调查问卷任务时，将普通外呼文件置空
			autoCallTask.set("COMMON_VOICE_ID", null);
			autoCallTask.set("REMINDER_TYPE",null);
			
			//如果是调查问卷时，即使上传的任务信息有需要下发短信的内容，也要强制不下发内容
			autoCallTask.set("SEND_MESSAGE", 0);
			autoCallTask.set("MESSAGE_CONTENT",null);
			
		}else if(taskType.equalsIgnoreCase("3")) {  //催缴费任务时
			autoCallTask.set("COMMON_VOICE_ID", null);
			autoCallTask.set("QUESTIONNAIRE_ID",null);
		}
		
		String scheduleId = autoCallTask.get("SCHEDULE_ID");
		String planStartTime = autoCallTask.get("PLAN_START_TIME").toString();
		String planEndTime = autoCallTask.get("PLAN_END_TIME").toString();
		Integer retryTimes = Integer.valueOf(autoCallTask.get("RETRY_TIMES").toString());
		Integer retryInterval = Integer.valueOf(autoCallTask.get("RETRY_INTERVAL").toString());
		String commonVoiceId = autoCallTask.get("COMMON_VOICE_ID");
		String questionniareId = autoCallTask.get("QUESTIONNAIRE_ID");
		String reminderType = autoCallTask.get("REMINDER_TYPE");
		String startVoiceId = autoCallTask.get("START_VOICE_ID");
		String endVoiceId = autoCallTask.get("END_VOICE_ID");
		String blackListId = autoCallTask.get("BLACKLIST_ID");
		String callerId = autoCallTask.get("CALLERID");
		Integer priority = Integer.valueOf(autoCallTask.get("PRIORITY").toString());
		int sendMessage = autoCallTask.getInt("SEND_MESSAGE");
		String messageContent = autoCallTask.getStr("MESSAGE_CONTENT");
		
		boolean b = AutoCallTask.dao.update(taskId,taskName,scheduleId,planStartTime,planEndTime,taskType,retryTimes,retryInterval,commonVoiceId,questionniareId,reminderType,startVoiceId,endVoiceId,blackListId,callerId,priority,sendMessage,messageContent);
		
		if(b) {
			render(RenderJson.success("修改外呼任务成功!"));
		}else {
			render(RenderJson.error("修改外呼任务失败!"));
		}
	}
	
	/**
	 * 号码模板下载
	 */
	public void template() {
		
		String type = getPara("type");                 //得到文件的类型
		//得到标识, standard：标准模板（普通外呼、调查外呼）,reminderTypeN(N:1,2,3,4,5,6,7,8);
		//催缴类型的催缴: 1:电费模板   2：水费模板  3：电话费模板 4：燃气费模板  5：物业费模板  6：车辆违章  7：交警移车  8：社保催缴
		String identify = getPara("identify"); 
		
		if(!BlankUtils.isBlank(type) && !BlankUtils.isBlank(identify)) {   //只有两样都不为空时，才返回模板文件
			
			String fileName = "";
			String mimeType = "";
			
			String templateDir = File.separator + "template" + File.separator;    //模板所在的路径
			
			String path_tmp = PathKit.getWebRootPath() + templateDir;
			
			if(type.equalsIgnoreCase("txt")) {
				mimeType = "txt";
			}else {
				mimeType = "xlsx";
			}
			
			if(identify.equalsIgnoreCase("standard")) {   //标准号码模板， 号码|姓名
				fileName = "standard_template" + "." + mimeType;
			}else if(identify.contains("reminderType")) {
				fileName = identify + "_template" + "." + mimeType;
			}
			
			System.out.println("文件名:" + fileName);
			
			File file = new File(path_tmp + fileName);
			
			if(file.exists()) {
				renderFile(file);
			}else {
				render(RenderJson.error("下载模板失败,文件不存在!"));
			}
			
		}
		
	}
	
	/**
	 * 任务归档（即置任务为历史任务）
	 * 
	 */
	public void archive() {
		
		String taskId = getPara("taskId");   //得到任务ID
		
		AutoCallTask act = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		
		if(BlankUtils.isBlank(act)) {
			render(RenderJson.error("操作失败,外呼任务已被删除或被归档为历史任务!"));
			return;
		}
		
		boolean b = execArchive(taskId);
		
		if(b) {
			render(RenderJson.success("归档成功!"));
		}else {
			render(RenderJson.error("归档失败!"));
		}
		
	}
	
	
	/**
	 * 执行归档
	 * 
	 * @param taskId
	 * @return
	 */
	public boolean execArchive(String taskId) {
		
		//将外呼任务转存到历史任务表
		boolean archiveCallTask = AutoCallTask.dao.archiveCallTask(taskId);     
		
		if(archiveCallTask) {   //如果转存成功,再转存号码
			AutoCallTaskTelephone.dao.archiveAutoCallTaskTelephone(taskId);
			
			//然后删除原外呼任务及外呼号码
			AutoCallTask.dao.deleteByTaskId(taskId);
			AutoCallTaskTelephone.dao.deleteByTaskId(taskId);
		}
		
		
		return archiveCallTask;
	}
	
	
	/**
	 * 重新加载统计数据
	 */
	public void reloadStatistics() {
		
		String taskId = getPara("taskId");
		
		Record data = AutoCallTask.dao.getStatisticsData(taskId);
		
		List<Record> list = new ArrayList<Record>();
		
		//已载入
		Record state1Data = new Record();
		state1Data.set("name", "已载入");
		state1Data.set("value", data.get("state1Data"));
		list.add(state1Data);
		
		//已成功
		Record state2Data = new Record();
		state2Data.set("name", "已成功");
		state2Data.set("value", data.get("state2Data"));
		list.add(state2Data);
		
		//待重呼
		Record state3Data = new Record();
		state3Data.set("name", "待重呼");
		state3Data.set("value", data.get("state3Data"));
		list.add(state3Data);
		
		//已失败
		Record state4Data = new Record();
		state4Data.set("name", "已失败");
		state4Data.set("value", data.get("state4Data"));
		list.add(state4Data);
		
		//未处理
		Record state0Data = new Record();
		state0Data.set("name","未处理");
		state0Data.set("value",data.get("state0Data"));
		list.add(state0Data);
		
		renderJson(list);
		
	}
	
	/**
	 * 导出汇总数据
	 */
	public void exportExcelForSummaryData() {
		
		List<Record> list = new ArrayList<Record>();
		Record rc = new Record();
		rc.set("category","数量");
		Record rr = new Record();
		rr.set("category", "占比");
		
		String totalCount = getPara("totalCount");      rc.set("totalData", totalCount);
		String state1Count = getPara("state1Count");    rc.set("state1Data", state1Count);
		String state2Count = getPara("state2Count");	rc.set("state2Data", state2Count);
		String state3Count = getPara("state3Count");    rc.set("state3Data", state3Count);
		String state4Count = getPara("state4Count");	rc.set("state4Data", state4Count);	
		list.add(rc);
		
		String totalRate = getPara("totalRate");		rr.set("totalData", totalRate + "%");
		String state1Rate = getPara("state1Rate");		rr.set("state1Data", state1Rate + "%");
		String state2Rate = getPara("state2Rate");		rr.set("state2Data", state2Rate + "%");
		String state3Rate = getPara("state3Rate");		rr.set("state3Data", state3Rate + "%");
		String state4Rate = getPara("state4Rate");		rr.set("state4Data", state4Rate + "%");
		
		list.add(rr);
		
		String taskName = getPara("taskName");

		//得到数据列表，准备以 Excel 方式导出
		String[] headers = {"","已呼数量","已载入","已成功","待重呼","已失败",};
		String[] columns = {"category","totalData","state1Data","state2Data","state3Data","state4Data"};
		String fileName = "任务：" + taskName + "的统计汇总情况.xls";
		String sheetName = "数据汇总信息";
		
		int cellWidth = 80;
		
		ExcelExportUtil export = new ExcelExportUtil(list,getResponse());
		export.headers(headers).columns(columns).sheetName(sheetName).cellWidth(cellWidth);
		
		export.fileName(fileName).execExport();
	}
	
	public void createSelfTask() {
		
	}

}
