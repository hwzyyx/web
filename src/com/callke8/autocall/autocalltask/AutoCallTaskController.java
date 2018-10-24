package com.callke8.autocall.autocalltask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.eval.BlankEval;

import com.callke8.autocall.flow.AutoFlow;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.ExcelExportUtil;
import com.callke8.utils.Md5Utils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.NumberUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;

import net.sf.json.JSONObject;

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
	
	/**
	 * 供交警移车通过网络提交数据，创建任务
	 * 
	 * 交警移车通过网络提交的数据格式如下：
	 * 
	       用户号码|报警人电话|车辆类型|车牌号码
	   18951082343|13512771995|小型车辆|DF168
	 * 
	 * 处理策略如下：
	 * （1）提交数据时，如果当天的外呼任务中没有创建交警移车任务，则系统自动创建一个外呼任务，如果当天已经创建了交警移车任务，则以该任务作为主任务接收数据
	 * （2）接收4个参数
	 * 
	 */
	public void createSelfTask() {
		
		String customerTel = null;         //用户号码
		String callPoliceTel = null;	   //报警人电话号码
		String vehicleType = null;  	   //车辆类型
		String plateNumber = null;		   //车牌号码
		String userCode = null;            //账号
		String pwd = null;                 //密码
		
		String type = getRequest().getMethod();     //请求方式(GET|POST)
		
		System.out.println("交警移车提交数据的请求方式为:" + type);
		
		if(type.equalsIgnoreCase("GET")) {          //如果是 GET 的方式请求，就更简单一些，只需要直接 getPara("XXXX")   即可以获取到客户提交上来的数据 
			
			customerTel = getPara("customerTel");
			callPoliceTel = getPara("callPoliceTel");
			vehicleType = getPara("vehicleType");
			plateNumber = getPara("plateNumber");
			userCode = getPara("userCode");
			pwd = getPara("pwd");
			
		}else {             //如果是通过 POST 提交时，接收 JSON 数据上传
			
			String jsonStr = null;
			
			try {           //客户以 header :application/json 上传 json数据
				
				StringBuilder sb = new StringBuilder();
				BufferedReader reader = this.getRequest().getReader();
				String line = null;
				
				while((line = reader.readLine()) !=null) {
					sb.append(line);
				}
				jsonStr = sb.toString();
			}catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
			System.out.println("客户提交上来的 JSON 字符串为 :" + jsonStr);
			
			if(!BlankUtils.isBlank(jsonStr) && jsonStr.toString().length() > 20) {
				
				JSONObject paramJson = JSONObject.fromObject(jsonStr);
				
				customerTel  = String.valueOf(paramJson.get("customerTel"));
				callPoliceTel = String.valueOf(paramJson.get("callPoliceTel"));
				vehicleType = String.valueOf(paramJson.get("vehicleType"));
				plateNumber = String.valueOf(paramJson.get("plateNumber"));
				userCode = String.valueOf(paramJson.get("userCode"));
				pwd = String.valueOf(paramJson.get("pwd"));
			}
			
		}
		
		//获取到上述参数后，做进一步判断和处理
		//(1) 判断上传的数据是否为空
		if(BlankUtils.isBlank(customerTel)) {
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:客户号码为空!",""));
			return;
		}else if(BlankUtils.isBlank(vehicleType)) {
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:车辆类型为空!",""));
			return;
		}else if(BlankUtils.isBlank(plateNumber)) {
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:车牌号码为空!",""));
			return;
		}else if(BlankUtils.isBlank(callPoliceTel)) {
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:报警人号码为空!",""));
			return;
		}else if(BlankUtils.isBlank(userCode)) {  
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:账号为空!",""));
			return;
		}else if(BlankUtils.isBlank(pwd)) {  
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:密码为空!",""));
			return;
		}
		
		//（2）判断用户名和密码
		Operator operator = Operator.dao.getOperatorByOperId(userCode);
		if(BlankUtils.isBlank(operator)) {         //找不到用户时
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:用户账号或密码错误!",""));
			return;
		}else {									   //帐户存在时，再检查密码
			String password = operator.getStr("PASSWORD");     //取出密码
			
			if(!password.equals(Md5Utils.Md5(pwd))) {           //加密传上来的密码后再比较
				renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:账号或密码错误!",""));
				return;
			}
		}
		
		//（3）判断客户号码和报警人电话，是否为正常的手机号码或是座机号码
		if(!(NumberUtils.isCellPhone(customerTel) || NumberUtils.isFixedPhone(customerTel))) {
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:客户号码非正常的手机或座机号码!",""));
			return;
		}
		
		if(!(NumberUtils.isCellPhone(callPoliceTel) || NumberUtils.isFixedPhone(callPoliceTel))) {
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:报警人电话号码非正常的手机或座机号码!",""));
			return;
		}
		
		//（4）处理车牌号码，车牌的格式为： 苏DA1179 , 即是长度为 7 位, 有可能提交的数据长度不是7位，而将 苏D 去掉了。
		if(plateNumber.length()<5 || plateNumber.length() > 7) {    //如果车牌的长度有问题，返回错误
			renderJson(returnCreateSelfTaskMap("FAILURE","失败,失败原因:车牌的长度小于5位或是大于7位!",""));
			return;
		}
		
		if(plateNumber.length() == 5) {    //如果是5位时，需要将前缀苏D强加上去，形成完整的车牌号码
			plateNumber = ParamConfig.paramConfigMap.get("paramType_1_defaultPlateNumberPrefix") + plateNumber;
		}
		
		//至此，表示上传的数据可以使用了
		//(1)先从已经有任务中，取出相应的类型的当天的任务
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByCondition("3", "7", DateFormatUtils.formatDateTime(new Date(), "yyyy-MM-dd"));
		
		if(BlankUtils.isBlank(autoCallTask)) {    //如果任务为空时，直接创建一个新的外呼任务
			autoCallTask = AutoCallTask.dao.createAutoCallTask("3","7",userCode,operator.getStr("ORG_CODE"));    //创建交警移车的外呼任务
		}
		
		if(!BlankUtils.isBlank(autoCallTask)) {    //如果创建了任务之后不为空，则可以直接储存这些数据了
			String taskId = autoCallTask.get("TASK_ID");
			String serialNumber = String.valueOf(System.currentTimeMillis() + Math.round(Math.random()*9000 + 1000));   //创建一个唯一序列号，用于通过网络取外呼结果用
			
			Record actt = new Record();
			actt.set("TASK_ID", taskId);
			actt.set("CUSTOMER_TEL",customerTel);
			actt.set("CUSTOMER_NAME",customerTel);
			
			actt.set("CALL_POLICE_TEL", callPoliceTel);     //报警人电话号码
			actt.set("VEHICLE_TYPE", vehicleType);          //车辆类型
			actt.set("PLATE_NUMBER", plateNumber);			//车牌号
			
			actt.set("RETRIED",0);
			actt.set("STATE",0);
			actt.set("RESPOND",null);
			actt.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
			actt.set("SERIAL_NUMBER", serialNumber);                 //序列号
			
			boolean b = AutoCallTaskTelephone.dao.add(actt);
			
			if(b) {
				renderJson(returnCreateSelfTaskMap("SUCCESS","提交数据成功", serialNumber));
			}else {
				renderJson(returnCreateSelfTaskMap("FAILUER","失败,失败原因:储存数据库失败!",""));
			}
			
		}else {
			renderJson(returnCreateSelfTaskMap("FAILUER","失败,失败原因:任务不存在,创建交警移车任务失败!",""));
		}
		
	}
	
	public void getResult() {
		
		String serialNumber = null;		   //流水号，唯一标识符
		String userCode = null;            //账号
		String pwd = null;                 //密码
		
		String type = getRequest().getMethod();     //请求方式(GET|POST)
		
		System.out.println("交警移车提交数据的请求方式为:" + type);
		
		if(type.equalsIgnoreCase("GET")) {          //如果是 GET 的方式请求，就更简单一些，只需要直接 getPara("XXXX")   即可以获取到客户提交上来的数据 
			
			serialNumber = getPara("serialNumber");
			userCode = getPara("userCode");
			pwd = getPara("pwd");
			
		}else {             //如果是通过 POST 提交时，接收 JSON 数据上传
			String jsonStr = null;
			
			try {           //客户以 header :application/json 上传 json数据
				
				StringBuilder sb = new StringBuilder();
				BufferedReader reader = this.getRequest().getReader();
				String line = null;
				
				while((line = reader.readLine()) !=null) {
					sb.append(line);
				}
				jsonStr = sb.toString();
			}catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
			System.out.println("客户提交上来的 JSON 字符串为 :" + jsonStr);
			
			if(!BlankUtils.isBlank(jsonStr) && jsonStr.toString().length() > 20) {
				
				JSONObject paramJson = JSONObject.fromObject(jsonStr);
				
				serialNumber = String.valueOf(paramJson.get("serialNumber"));
				userCode = String.valueOf(paramJson.get("userCode"));
				pwd = String.valueOf(paramJson.get("pwd"));
			}
		}
		
		//获取到上述参数后，做进一步判断和处理
		//(1) 判断上传的数据是否为空
		if(BlankUtils.isBlank(serialNumber)) {
			renderJson(returnGetResultMap("FAILURE","失败,失败原因：为空!",null));
			return;
		}else if(BlankUtils.isBlank(userCode)) {  
			renderJson(returnGetResultMap("FAILURE","失败,失败原因:账号为空!",null));
			return;
		}else if(BlankUtils.isBlank(pwd)) {  
			renderJson(returnGetResultMap("FAILURE","失败,失败原因:密码为空!",null));
			return;
		}
		
		
		//（2）判断用户名和密码
		Operator operator = Operator.dao.getOperatorByOperId(userCode);
		if(BlankUtils.isBlank(operator)) {         //找不到用户时
			renderJson(returnGetResultMap("FAILURE","失败,失败原因:用户账号或密码错误!",null));
			return;
		}else {									   //帐户存在时，再检查密码
			String password = operator.getStr("PASSWORD");     //取出密码
			
			if(!password.equals(Md5Utils.Md5(pwd))) {           //加密传上来的密码后再比较
				renderJson(returnGetResultMap("FAILURE","失败,失败原因:账号或密码错误!",null));
				return;
			}
		}
		
		//（3）取得结果，并返回结果
		AutoCallTaskTelephone actt = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneBySerialNumber(serialNumber);
		if(BlankUtils.isBlank(actt)) {    //如果取回的记录为空，表示数据已经被删除或是其他的原因无法查询到记录
			renderJson(returnGetResultMap("FAILURE","失败,失败原因：该流水号对应的外呼任务不存在",null));
			return;
		}else {                           //如果取回的记录不为空时，则返回外呼的结果
			
			int state = actt.getInt("STATE");                          //外呼状态
			String lastCallResult = actt.getStr("LAST_CALL_RESULT");   //外呼结果
			
			
			if(state==0) {                //未外呼
				renderJson(returnGetResultMap("FAILURE", "呼叫不成功，状态：呼叫暂未外呼", null));
				return;
			}else if(state==1) {          //执行外呼过程中
				renderJson(returnGetResultMap("FAILURE", "呼叫不成功，状态：正在外呼", null));
			}else if(state==2) {          //外呼成功
				
				String taskId = actt.getStr("TASK_ID");
				AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);    //取出任务
				String callerId = autoCallTask.getStr("CALLERID");                               //主叫对应的ID
				String displayNumber = MemoryVariableUtil.getDictName("CALLERID", callerId);     //主叫号码
				
				Map<String,String> m = new HashMap<String,String>();
				
				m.put("serialNumber",serialNumber);         		           //流水号
				m.put("displayNumber",displayNumber);                          //外呼时显示的号码
				m.put("calleeNumber", actt.getStr("CALLOUT_TEL"));             //被叫号码
				m.put("time", actt.get("LOAD_TIME").toString());               //外呼时间
				m.put("duration",actt.getInt("BILLSEC").toString());           //通话时长
				
				renderJson(returnGetResultMap("SUCCESS","呼叫成功",m));
			}else if(state==3) {
				renderJson(returnGetResultMap("FAILURE", "呼叫不成功，状态：待重外呼，失败原因：" + lastCallResult, null));
			}else if(state==4) {
				renderJson(returnGetResultMap("FAILURE", "呼叫失败，失败原因：" +lastCallResult, null));
			}else {
				renderJson(returnGetResultMap("FAILURE", "呼叫失败，失败原因：未知状态", null));
			}
			
		}
		
	}

	/**
	 * 返回创建结果
	 * 
	 * @param resultCode
	 * @param resultMsg
	 * @param serialNumber
	 * @return
	 */
	public Map<String,String> returnCreateSelfTaskMap(String resultCode,String resultMsg,String serialNumber) {
		
		Map<String,String> rsM = new HashMap<String,String>();
		rsM.put("resultCode", resultCode);
		rsM.put("resultMsg", resultMsg);
		rsM.put("serialNumber", serialNumber);
		
		return rsM;
	}
	
	/**
	 * 返回外呼结果
	 * 
	 * @param resultCode
	 * @param resultMsg
	 * @param result
	 * @return
	 */
	public Map<String,Object> returnGetResultMap(String resultCode,String resultMsg,Map<String,String> result) {
		
		Map<String,Object> rsM = new HashMap<String,Object>();
		
		rsM.put("resultCode", resultCode);
		rsM.put("resultMsg", resultMsg);
		rsM.put("result", result);
		
		return rsM;
	}
	
	public void getMssageContentTemplate() {
		
		String reminderType = getPara("reminderType");   //催缴类型
		
		if(BlankUtils.isBlank(reminderType)) {
			reminderType = "1";
		}
		
		AutoFlow autoFlow = AutoFlow.dao.getAutoFlowByReminderType(reminderType);
		
		if(!BlankUtils.isBlank(autoFlow)) {
			
			String flowRule = autoFlow.getStr("FLOW_RULE");    //取出规则
			
			String content = null;
			
			if(reminderType.equals("1")) {     //电费催缴时
				//常州供电公司友情提醒：您户地址%s，总户号%s于%s发生电费%s元，请按时缴纳，逾期缴纳将产生滞纳金。详情可关注“国网江苏电力”公众微信号或下载掌上电力app。如您本次收到的用电地址有误，可在工作时间致电83272222。若已缴费请忽略本次提醒。
				content = String.format(flowRule, "南京市幸福小区5栋204室","107343433","2018年10月","224.22");
			}else if(reminderType.equals("2")) {    //水费催缴
				//尊敬的自来水用户您好，下面为您播报本期水费对账单。您水表所在地址%s于%s抄见数为%s，月用水量为%s吨，水费为%s元。特此提醒。详情可凭用户号%s登录常州通用自来水公司网站或致电常水热线：88130008查询。
				content = String.format(flowRule, "南京市幸福小区5栋204室","2018年10月","5432","122","231.43","107343433");
			}else if(reminderType.equals("3")) {    //电话费催缴
				//尊敬的客户您好，你%s的电话费为%s元。
				content = String.format(flowRule,"2018年10月","123.32");
			}else if(reminderType.equals("4")) {    //燃气费催缴
				//尊敬的客户您好，你%s的燃气费为%s元。
				content = String.format(flowRule,"2018年10月","123.32");
			}else if(reminderType.equals("5")) {    //物业费催缴
				//尊敬的客户您好，你%s的物业费为%s元。
				content = String.format(flowRule,"2018年10月","123.32");
			}else if(reminderType.equals("6")) {    //交通违章
				//您的%s汽车于%s违反了相关的交通条例，请收到本告知之日起30日内接受处理。
				content = String.format(flowRule,"苏DR1179","2018年10月");
			}else if(reminderType.equals("7")) {    //交警移车
				//您好，这是常州公安微警务051981990110挪车服务专线，您是%s车主吗？你的%s占用他人车位，请按任意键接听车位业主电话。
				content = String.format(flowRule,"苏DR1179","小型汽车");
			}else if(reminderType.equals("8")) {    //交警移车
				//尊敬的客户您好，你%s的社保费为%s元。
				content = String.format(flowRule,"2018年10月","123.32");
			}
			
			render(RenderJson.success(content));
		}
		
	}
	
	
}
