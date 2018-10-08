package com.callke8.autocall.autocalltask;

import java.io.File;
import java.util.Date;
import java.util.Map;

import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;

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
		
		//任务类型和任务状态
		setAttr("taskTypeComboboxDataFor0", CommonController.getComboboxToString("TASK_TYPE","0"));
		setAttr("taskTypeComboboxDataFor1", CommonController.getComboboxToString("TASK_TYPE","1"));
		
		setAttr("taskStateComboboxDataFor0", CommonController.getComboboxToString("AC_TASK_STATE","0"));
		setAttr("taskStateComboboxDataFor1", CommonController.getComboboxToString("AC_TASK_STATE","1"));
		
		//主叫号码
		setAttr("callerIdComboboxDataFor0", CommonController.getComboboxToString("CALLERID","0"));
		
		render("list.jsp");
	}
	

	@Override
	public void datagrid() {
		System.out.println("取AutoCallTaskController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String taskName = getPara("taskName");
		String taskType = getPara("taskType");
		String taskState = getPara("taskState");
		String orgCode = getPara("orgCode");
		
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = AutoCallTask.dao.getAutoCallTaskByPaginateToMap(pageNumber, pageSize, taskName,taskType,taskState,orgCode, startTime, endTime);
		
		System.out.println("取AutoCallTaskController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
		
	}
	
	@Override
	public void add() {
		
		AutoCallTask autoCallTask = getModel(AutoCallTask.class,"autoCallTask");
		
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
		
		boolean b = AutoCallTask.dao.update(taskId,taskName,scheduleId,planStartTime,planEndTime,taskType,retryTimes,retryInterval,commonVoiceId,questionniareId,reminderType,startVoiceId,endVoiceId,blackListId,callerId,priority);
		
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
		String identify = getPara("identify");         //得到标识
		
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
				fileName = "standard_telephone_template" + "." + mimeType;
			}else if(identify.equalsIgnoreCase("telephone")) {
				fileName = "telephone_bill" + "." + mimeType;
			}else if(identify.equalsIgnoreCase("social")) {
				fileName = "social_security" + "." + mimeType;
			}else if(identify.equalsIgnoreCase("illegal")) {
				fileName = "vehicle_illegal" + "." + mimeType;
			}else if(identify.equalsIgnoreCase("property")) {
				fileName = "property_bill" + "." + mimeType;
			}
			
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

}
