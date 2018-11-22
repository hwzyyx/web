package com.callke8.autocall.autocalltask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NumberUtils;

import com.callke8.autocall.autoblacklist.AutoBlackListTelephone;
import com.callke8.autocall.autonumber.AutoNumberTelephone;
import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.ExcelExportUtil;
import com.callke8.utils.RenderJson;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TxtUtils;
import com.callke8.utils.XLSUtils;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

public class AutoCallTaskTelephoneController extends Controller implements
		IController {
	
	@Override
	public void datagrid() {
		//System.out.println("取AutoCallTaskTelephoneController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String taskId = getPara("taskId");
		String customerTel = getPara("customerTel");
		String customerName = getPara("customerName");
		String state = getPara("state");
		String messageState = getPara("messageState");
		String startTimeForTelephone = getPara("startTimeForTelephone");
		String endTimeForTelephone = getPara("endTimeForTelephone");
		String dateTimeType = getPara("dateTimeType");     //取得查询时间类型，0表示时间区段为以创建时间为查询区间，1表示以外呼时间为查询区间
		
		if(BlankUtils.isBlank(dateTimeType)) {
			dateTimeType = "0";
		}
		
		String createTimeStartTime = null;
		String createTimeEndTime = null;
		String loadTimeStartTime = null;
		String loadTimeEndTime = null;
		
		if(dateTimeType.equalsIgnoreCase("1")) {           //表示是以外呼时间为查询区间
			loadTimeStartTime = startTimeForTelephone;
			loadTimeEndTime = endTimeForTelephone;
		}else {                                            //表示是以创建时间为查询区间
			createTimeStartTime = startTimeForTelephone;
			createTimeEndTime = endTimeForTelephone;
		}
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneByPaginateToMap(pageNumber, pageSize, taskId, customerTel, customerName,state,messageState,createTimeStartTime,createTimeEndTime,loadTimeStartTime,loadTimeEndTime);
		
		System.out.println("取AutoCallTaskTelephoneController datagrid的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
		
	}
	

	@Override
	public void add() {

		String taskId = getPara("taskId");       //得到任务的Id
		//String taskType = getPara("taskType");   //得到任务类型
		AutoCallTaskTelephone actt = getModel(AutoCallTaskTelephone.class,"autoCallTaskTelephone");
		
		Record autoCallTaskTelephone = new Record();
		
		//（1）在添加之前，先判断号码是否在黑名单之中
		String customerTel = actt.get("CUSTOMER_TEL");
		
		//根据taskId,先取出任务信息
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		if(BlankUtils.isBlank(autoCallTask)) {
			render(RenderJson.error("新增号码失败,外呼任务已经不存在，可能已被删除!"));
			return;
		}
		boolean checkRs = checkBlackList(customerTel,autoCallTask);   //黑名单检查
		if(checkRs) {
			render(RenderJson.error("新增号码失败,号码" + customerTel + "在黑名单中!"));
			return;
		}
		
		//（2）再判断任务的号码中是否存在相同的号码
		boolean repetitionRs = AutoCallTaskTelephone.dao.checkTelephoneRepetitionForAdd(customerTel, taskId);
		if(repetitionRs) {
			render(RenderJson.error("新增号码失败,号码" + customerTel + "已重复!"));
			return;
		}
		
		autoCallTaskTelephone.set("TASK_ID", taskId);
		autoCallTaskTelephone.set("CUSTOMER_TEL",actt.get("CUSTOMER_TEL"));
		autoCallTaskTelephone.set("CUSTOMER_NAME",actt.get("CUSTOMER_NAME"));
		
		autoCallTaskTelephone.set("PERIOD", actt.get("PERIOD"));                        //日期
		autoCallTaskTelephone.set("DISPLAY_NUMBER", actt.get("DISPLAY_NUMBER"));        //表显数量
		autoCallTaskTelephone.set("DOSAGE", actt.get("DOSAGE"));						//使用量
		autoCallTaskTelephone.set("CHARGE", actt.get("CHARGE"));						//费用
		autoCallTaskTelephone.set("ACCOUNT_NUMBER", actt.get("ACCOUNT_NUMBER"));		//户号
		autoCallTaskTelephone.set("ADDRESS", actt.get("ADDRESS"));                      //地址
		autoCallTaskTelephone.set("CALL_POLICE_TEL", actt.get("CALL_POLICE_TEL"));      //报警人电话号码
		autoCallTaskTelephone.set("VEHICLE_TYPE", actt.get("VEHICLE_TYPE"));            //车辆类型
		autoCallTaskTelephone.set("PLATE_NUMBER", actt.get("PLATE_NUMBER"));			//车牌号
		autoCallTaskTelephone.set("ILLEGAL_CITY", actt.get("ILLEGAL_CITY"));			//违法城市
		autoCallTaskTelephone.set("PUNISHMENT_UNIT",actt.get("PUNISHMENT_UNIT"));		//处罚单位
		autoCallTaskTelephone.set("ILLEGAL_REASON", actt.get("ILLEGAL_REASON"));		//违法理由
		autoCallTaskTelephone.set("COMPANY", actt.get("COMPANY"));						//公司
		
		autoCallTaskTelephone.set("RETRIED",0);
		autoCallTaskTelephone.set("STATE",0);
		autoCallTaskTelephone.set("RESPOND",null);
		autoCallTaskTelephone.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		//(3)检查号码的格式
		String checkResult = checkDataFormat(autoCallTask,autoCallTaskTelephone);    //检查号码格式
		if(!BlankUtils.isBlank(checkResult)) {
			render(RenderJson.error(checkResult));
			return;
		}
		
		boolean b = AutoCallTaskTelephone.dao.add(autoCallTaskTelephone);
		
		if(b) {
			render(RenderJson.success("新增号码成功!"));
		}else {
			render(RenderJson.error("新增号码失败!"));
		}
	}
	
	/**
	 * 检查号码格式
	 * 
	 * @param act
	 * @param actt
	 * @return
	 * 		如果返回的信息中，为空，表示检查正常；如果不为空，返回的结果就是错误的原因。
	 */
	public String checkDataFormat(AutoCallTask act,Record actt) {
		String msg = null;
		
		String taskType = act.getStr("TASK_TYPE");               //任务类型
		String reminderType = act.getStr("REMINDER_TYPE");       //催缴类型
		if(taskType.equals("3")) {     //如果为催缴任务时，才执行号码类型检查
			String period = actt.getStr("PERIOD");						//日期
			String accountNumber = actt.getStr("ACCOUNT_NUMBER");        //户号
			String address = actt.getStr("ADDRESS");               	 	//地址
			String charge = actt.getStr("CHARGE");						//费用
			String displayNumber = actt.getStr("DISPLAY_NUMBER"); 		//表显数量
			String dosage = actt.getStr("DOSAGE");						//使用量
			String illegalCity = actt.getStr("ILLEGAL_CITY");			//违章城市	
			String illegalReason = actt.getStr("ILLEGAL_REASON");		//违章事由
			String punishmentUnit = actt.getStr("PUNISHMENT_UNIT");		//处罚单位	
			String callPoliceTel = actt.getStr("CALL_POLICE_TEL");		//报警人电话
			String vehicleType = actt.getStr("VEHICLE_TYPE");			//车辆类型
			String plateNumber = actt.getStr("PLATE_NUMBER");			//车牌号码
			//催缴类型编号： 1电费   2水费  3电话费  4燃气费 5物业费 6车辆违章  7交警移车  8社保催缴
			if(reminderType.equalsIgnoreCase("1")) {              	//电费催缴
				//需要检查：日期、户号、地址、费用
				//(1)检查日期
				if(!checkPeriodFormat(period)) {
					msg = "新增号码失败,日期为空或格式不对!";
					return msg;
				}
				//(2)检查户号
				if(!checkAccountNumber(accountNumber)) {
					msg = "新增号码失败,户号为空或格式不对!";
					return msg;
				}
				//（3）检查地址
				if(BlankUtils.isBlank(address)) {
					msg = "新增号码失败,地址为空!";
					return msg;
				}
				//(4) 检查费用
				if(!checkCharge(charge)) {
					msg = "新增号码失败,费用为空或格式不对!";
					return msg;
				}
				
			}else if(reminderType.equalsIgnoreCase("2")) {			//水费催缴
				//需要检查：日期、地址、表显数据、使用量、费用、户号
				//（1）检查日期
				if(!checkPeriodFormat(period)) {
					msg = "新增号码失败,日期为空或格式不对!";
					return msg;
				}
				//(2)检查地址
				if(BlankUtils.isBlank(address)) {
					msg = "新增号码失败,地址为空!";
					return msg;
				}
				//(3)检查表显数据
				if(!checkDisplayNumber(displayNumber)) {
					msg = "新增号码失败,表显数据为空或格式不对!";
					return msg;
				}
				//（4）使用量
				if(!checkDosage(dosage)) {
					msg = "新增号码失败,使用量为空或格式不对!";
					return msg;
				}
				//（5）检查费用
				if(!checkCharge(charge)) {
					msg = "新增号码失败,费用为空或格式不对!";
					return msg;
				}
				//(6)检查户号
				if(!checkAccountNumber(accountNumber)) {
					msg = "新增号码失败,户号为空或格式不对!";
					return msg;
				}
				
			}else if(reminderType.equalsIgnoreCase("3")) {			//电话费催缴
				//需要检查：日期、户号、地址、电话费
				//（1）检查日期
				if(!checkPeriodFormat(period)) {
					msg = "新增号码失败,日期为空或格式不对!";
					return msg;
				}
				//(2)检查户号
				if(!checkAccountNumber(accountNumber)) {
					msg = "新增号码失败,户号为空或格式不对!";
					return msg;
				}
				//(3)检查地址
				if(BlankUtils.isBlank(address)) {
					msg = "新增号码失败,地址为空!";
					return msg;
				}
				//（4）检查费用
				if(!checkCharge(charge)) {
					msg = "新增号码失败,费用为空或格式不对!";
					return msg;
				}
			}else if(reminderType.equalsIgnoreCase("4")) {			//燃气费催缴
				//需要检查：日期、户号、地址、电话费
				//（1）检查日期
				if(!checkPeriodFormat(period)) {
					msg = "新增号码失败,日期为空或格式不对!";
					return msg;
				}
				//(2)检查户号
				if(!checkAccountNumber(accountNumber)) {
					msg = "新增号码失败,户号为空或格式不对!";
					return msg;
				}
				//(3)检查地址
				if(BlankUtils.isBlank(address)) {
					msg = "新增号码失败,地址为空!";
					return msg;
				}
				//（4）检查费用
				if(!checkCharge(charge)) {
					msg = "新增号码失败,费用为空或格式不对!";
					return msg;
				}
			}else if(reminderType.equalsIgnoreCase("5")) {			//物业费催缴
				//需要检查：日期、地址、物业费
				//（1）检查日期
				if(!checkPeriodFormat(period)) {
					msg = "新增号码失败,日期为空或格式不对!";
					return msg;
				}
				//(2)检查地址
				if(BlankUtils.isBlank(address)) {
					msg = "新增号码失败,地址为空!";
					return msg;
				}
				//（3）检查费用
				if(!checkCharge(charge)) {
					msg = "新增号码失败,费用为空或格式不对!";
					return msg;
				}
				
			}else if(reminderType.equalsIgnoreCase("6")) {			//车辆违章
				//需要检查：车牌号码、日期、违法城市、违法事由、处罚单位
				//（1）检查车牌号码
				if(BlankUtils.isBlank(plateNumber)) {
					msg = "新增号码失败,车牌号码为空!";
					return msg;
				}
				//（2）检查日期
				if(!checkPeriodFormat(period)) {
					msg = "新增号码失败,日期为空或格式不对!";
					return msg;
				}
				//(3)检查违法城市
				if(BlankUtils.isBlank(illegalCity)) {
					msg = "新增号码失败,违法城市为空!";
					return msg;
				}
				//（4)检查事由
				if(BlankUtils.isBlank(illegalReason)) {
					msg = "新增号码失败,违法事由为空!";
					return msg;
				}
				//(5)自罚单位
				if(BlankUtils.isBlank(punishmentUnit)) {
					msg = "新增号码失败,处罚单位为空!";
					return msg;
				}
				
			}else if(reminderType.equalsIgnoreCase("7")) {          //7交警移车
				//需要检查：报警人电话、车辆类型、车牌号码
				//（1）检查报警人电话，
				if(!checkCallPoliceTel(callPoliceTel)) {
					msg = "新增号码失败,报警人电话为空或格式不对!";
					return msg;
				}
				//(2)检查车辆类型
				if(BlankUtils.isBlank(vehicleType)) {
					msg = "新增号码失败,车辆类型为空!";
					return msg;
				}
				//(3)检查车牌号码
				if(BlankUtils.isBlank(plateNumber)) {
					msg = "新增号码失败,车牌号码为空!";
					return msg;
				}
			}else if(reminderType.equalsIgnoreCase("8")) {          //社保催缴
				//需要检查：日期、费用
				//（1）检查日期
				if(!checkPeriodFormat(period)) {
					msg = "新增号码失败,日期为空或格式不对!";
					return msg;
				}
				//(2)检查费用
				if(!checkCharge(charge)) {
					msg = "新增号码失败,费用为空或格式不对!";
					return msg;
				}
			}
		}
		
		return null;
		
	}
	
	
	/**
	 * 通过号码组的方式添加号码
	 */
	public void addByAutoNumber() {
		
		StringBuilder sb = new StringBuilder();
		
		String taskId = getPara("taskId");
		String numberId = getPara("numberId");
		
		//如果任务Id或号码组Id为空时，反回错误
		if(BlankUtils.isBlank(taskId) || BlankUtils.isBlank(numberId)) {
			render(RenderJson.error("任务Id为空或号码组为空,请查证后再操作!"));
			return;
		}
		
		//根据号码组ID,从号码组中取出号码
		List<Record> list = AutoNumberTelephone.dao.getAutoNumberTelephoneByNumberId(numberId);
		
		//如果号码组中无号码时，返回错误并提示
		if(BlankUtils.isBlank(list) || list.size() <= 0) {
			render(RenderJson.error("号码组中号码为空,本次上传成功号码为0!"));
			return;
		}
		
		//根据任务ID,取出任务信息，然后取出黑名单判断用
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		if(BlankUtils.isBlank(autoCallTask)) {
			render("操作失败,任务不存在,请查证后再操作!");
			return;
		}
		
		String blackListId = autoCallTask.get("BLACKLIST_ID");      //取出黑名单ID
		//根据黑名单,取出黑名单中的号码
		List<String> blackListTelephones = AutoBlackListTelephone.dao.getAutoBlackListTelephoneByBlackListId(blackListId); 
		
		//创建两个 List，用于格式化可以存储到数据的Record
		ArrayList<Record> afterBlackListResult = new ArrayList<Record>();    //经过黑名单过滤后的列表 
 		ArrayList<Record> afterRepetitionResult = new ArrayList<Record>();   //经过重复性过滤后的列表
 		
 		//(1) 第一步做黑名单过滤判断
 		int inBlackListCount = 0;
 		
 		for(int i=0;i<list.size();i++) {
 			
 			Record autoCallTaskTelephone = new Record();    //创建一个 Record 用于储存数据
 			
 			Record r = list.get(i);
 			
 			String customerTel = r.get("CUSTOMER_TEL");
 			
 			autoCallTaskTelephone.set("TASK_ID",taskId);
 			autoCallTaskTelephone.set("CUSTOMER_TEL",customerTel);
 			autoCallTaskTelephone.set("CUSTOMER_NAME",r.get("CUSTOMER_NAME"));
 			autoCallTaskTelephone.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
 			autoCallTaskTelephone.set("RESPOND",null);
 			autoCallTaskTelephone.set("STATE","0");
 			autoCallTaskTelephone.set("RETRIED",0);
 			
 			/**
			 * 接下来，对号码进行判断
			 * 1 第一列，即号码是否为正常的号码
			 * 2号码是否为黑名单数据
			 */
			//取出两列，然后判断第一列是否是号码,且号码的长度是否大于等于7位数，否则将不储存
			if(!BlankUtils.isBlank(customerTel) && StringUtil.isNumber(customerTel) && customerTel.length() >= 7) {
				
				//做黑名单过滤
				if(blackListTelephones.contains(customerTel)) {   //如果存在于黑名单
					inBlackListCount++;
					continue;
				}
				
				afterBlackListResult.add(autoCallTaskTelephone);
			}else {
				continue;
			}
 			
 		}
 		
 		
 		sb.append("黑名单过滤的号码数量为：" + inBlackListCount);
 		
 		//第二步：进行重复性检查
 		List<String> autoCallTaskTelephones = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneByTaskId(taskId); 
 		
 		int repetitionCount = 0;
 		
 		for(Record act:afterBlackListResult) {
			
			String customerTel = act.get("CUSTOMER_TEL");
			
			//重复性检查
			if(autoCallTaskTelephones.contains(customerTel)) {
				repetitionCount++;
				continue;
			}
			
			afterRepetitionResult.add(act);
		}
		
 		sb.append("<br/>重复的号码数量为:" + repetitionCount);
		
		int count  = AutoCallTaskTelephone.dao.batchSave(afterRepetitionResult);
	
		sb.append("<br/>成功插入号码数量为 :" + count + "!");
		
		render(RenderJson.success(sb.toString()));
		
	}


	@Override
	public void delete() {
		String ids = getPara("ids");    //要删除的号码ID
		int count = AutoCallTaskTelephone.dao.batchDelete(ids);
		
		render(RenderJson.success("成功删除的数据量为:" + count));
	}

	@Override
	public void index() {

	}

	@Override
	public void update() {
		
		AutoCallTaskTelephone autoCallTaskTelephone = getModel(AutoCallTaskTelephone.class,"autoCallTaskTelephone");
		
		String customerTel = autoCallTaskTelephone.get("CUSTOMER_TEL");       //号码
		String customerName = autoCallTaskTelephone.get("CUSTOMER_NAME");    //客户姓名
		Integer telId = Integer.valueOf(autoCallTaskTelephone.get("TEL_ID").toString());
		
		String period = autoCallTaskTelephone.get("PERIOD");
		String displayNumber = autoCallTaskTelephone.get("DISPLAY_NUMBER");
		String dosage = autoCallTaskTelephone.get("DOSAGE");
		String charge = autoCallTaskTelephone.get("CHARGE");
		String accountNumber = autoCallTaskTelephone.get("ACCOUNT_NUMBER");
		String address = autoCallTaskTelephone.get("ADDRESS");
		String callPoliceTel = autoCallTaskTelephone.get("CALL_POLICE_TEL");
		String vehicleType = autoCallTaskTelephone.get("VEHICLE_TYPE");
		String plateNumber = autoCallTaskTelephone.get("PLATE_NUMBER");
		String illegalCity = autoCallTaskTelephone.get("ILLEGAL_CITY");
		String punishmentUnit = autoCallTaskTelephone.get("PUNISHMENT_UNIT");
		String illegalReason = autoCallTaskTelephone.get("ILLEGAL_REASON");
		String company = autoCallTaskTelephone.get("COMPANY");
 		
		//(1)在修改之前,该号码是否在黑名单中
		String taskId = getPara("taskId");    //得到任务ID
		//根据任务ID，取出任务信息
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);   
		boolean checkRs = checkBlackList(customerTel,autoCallTask);
		if(checkRs) {
			render(RenderJson.error("修改后的号码在黑名单中,修改失败!"));
			return;
		}
		
		//（2）再判断任务的号码中是否存在相同的号码
		boolean repetitionRs = AutoCallTaskTelephone.dao.checkTelephoneRepetitionForUpdate(customerTel, taskId,telId);
		if(repetitionRs) {
			render(RenderJson.error("修改号码失败,号码" + customerTel + "已重复!"));
			return;
		}
		
		Record actt = new Record();
		actt.set("PERIOD", period);
		actt.set("DISPLAY_NUMBER", displayNumber);
		actt.set("DOSAGE", dosage);
		actt.set("CHARGE", charge);
		actt.set("ACCOUNT_NUMBER", accountNumber);
		actt.set("ADDRESS", address);
		actt.set("CALL_POLICE_TEL", callPoliceTel);
		actt.set("VEHICLE_TYPE", vehicleType);
		actt.set("PLATE_NUMBER", plateNumber);
		actt.set("ILLEGAL_CITY", illegalCity);
		actt.set("PUNISHMENT_UNIT", punishmentUnit);
		actt.set("ILLEGAL_REASON", illegalReason);
		actt.set("COMPANY", company);
		//(3)检查号码的格式
		String checkResult = checkDataFormat(autoCallTask,actt);    //检查号码格式
		if(!BlankUtils.isBlank(checkResult)) {
			render(RenderJson.error(checkResult));
			return;
		}
		
		boolean b = AutoCallTaskTelephone.dao.update(customerTel,customerName,period,displayNumber,dosage,charge,accountNumber,address,callPoliceTel,vehicleType,plateNumber,illegalCity,punishmentUnit,illegalReason,company,telId);
		
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
		
	}
	
	/**
	 * 根据任务ID,和电话号码，查看当前号码，是否黑名单
	 * 
	 * @param customerTel
	 * @param autoCallTask
	 * @return
	 * 	true:表示在黑名单中
	 *  false: 表示不在黑名单中
	 */
	public boolean checkBlackList(String customerTel,AutoCallTask autoCallTask) {
		
		boolean b = false;
		
		if(!BlankUtils.isBlank(autoCallTask)) {
			
			String blackListId = autoCallTask.get("BLACKLIST_ID");    //取出黑名单ID
			
			if(!BlankUtils.isBlank(blackListId)) {                    //如果该任务选择了黑名单
				
				int count = AutoBlackListTelephone.dao.getAutoBlackListTelephoneCountByCondition(customerTel, blackListId);
				
				if(count>0) {
					b = true;
				}
			}
		}
		
		return b;
	}
	
	
	/**
	 * 上传号码文件添加号码
	 */
	public void uploadFile() {
		
		//先判断文件大小限制
		int fileSize = getRequest().getContentLength();   //得到上传文件的大小, 由于jfinal默认最大的上传的大小为 10 * 1024 * 1024 即是 10M
		if(fileSize > (10 * 1024 * 1024)) {    //如果大于 10M 时，返回错误
			render(RenderJson.error("上传失败：上传文件过大，已经超过 10M"));
			return;
		}
		
		//获取上传的文件
		//(1)定义上传路径,这种上传号码,都上传到 upload 这个文件夹中
		String uploadDir = PathKit.getWebRootPath() + File.separator + "upload" + File.separator;
		//(2)执行上传操作
		UploadFile uf = getFile("telephoneFile",uploadDir);
		
		//判断上传的文件的类型
		String mimeType = StringUtil.getExtensionName(uf.getFileName());
		
		//判断文件的类型,是否为 txt、xls 或是 xlsx
		if(BlankUtils.isBlank(mimeType) || (!mimeType.equalsIgnoreCase("xls") && !mimeType.equalsIgnoreCase("xlsx") && !mimeType.equalsIgnoreCase("txt"))) {
			//提示错误之前，先将上传的文件删除
			if(!BlankUtils.isBlank(uf.getFile()) && uf.getFile().exists())  {
				uf.getFile().delete();
			}
			
			render(RenderJson.error("上传失败,上传的号码文件类型不正确,只支持 txt、xls 或是 xlsx"));
			return;
		}
		
		//上传的文件名有可能是中文名,不利于读取,需先重命名为数字名
		String renameFileName = DateFormatUtils.getTimeMillis() + "." + mimeType;
		File newFile = new File(uploadDir + renameFileName);
		
		uf.getFile().renameTo(newFile);
		
		String taskId = getPara("taskId");    //获取参数任务ID
		
		String insertResult = insertTelephoneFromFile(newFile,mimeType,taskId);    //成功插入的数量
		
		render(RenderJson.success(insertResult));
		
	}
	
	
	/**
	 * 从XLS、TXT读取号号码，并插入号码到数据库
	 * 
	 * @param file
	 * @param mimeType
	 * @param taskId
	 * @return
	 * 		  插入后,返回插入数据的信息，并以字符串返回
	 */
	public String insertTelephoneFromFile(File file,String mimeType,String taskId) {
		
		StringBuilder sb = new StringBuilder();
		
		//先根据任务ID，取出任务的信息，主要是用于做黑名单判断用
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		if(BlankUtils.isBlank(autoCallTask)) {
			sb.append("操作失败,任务不存在,请查证后再操作!");
			return sb.toString();
		}
		
		String blackListId = autoCallTask.get("BLACKLIST_ID");    //取出黑名单ID
		
		String taskType = autoCallTask.get("TASK_TYPE");          //得到任务类型
		String reminderType = autoCallTask.get("REMINDER_TYPE");  //如果是催缴类，则取出催缴类型
		
		//把文件中的信息取出并放置到一个List中，Record以数字顺序存储数据
		List<Record> list = null;
		
		if(mimeType.equalsIgnoreCase("txt")) {  //如果文件类型为txt
			list = TxtUtils.readTxt(file);
		}else {
			list = XLSUtils.readXls(file);    
		}
		
		//新建两个 list，用于格式化成可以存储到数据库的 Record
		ArrayList<Record> afterBlackListResult = new ArrayList<Record>();
		ArrayList<Record> afterRepetitionResult = new ArrayList<Record>();
		ArrayList<Record> afterFormatCheckResult = new ArrayList<Record>();           //数据格式检查
		
		//取出黑名单中的号码,并以List返回,用于黑名单过滤
		List<String> blackListTelephones = AutoBlackListTelephone.dao.getAutoBlackListTelephoneByBlackListId(blackListId);
		
		
		//（1）第一步做黑名单过滤判断
		int inBlackListCount = 0;
		for(int i=0;i<list.size();i++) {
			
			Record autoCallTaskTelephone = new Record();   //新建一个Record用于储存数据
			
			Record r = list.get(i);
			
			String customerTel = r.get("0");         //所有的上传号码文件，第1列是号码
			autoCallTaskTelephone.set("TASK_ID",taskId);
			autoCallTaskTelephone.set("CUSTOMER_TEL", customerTel);
			autoCallTaskTelephone.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
			autoCallTaskTelephone.set("RESPOND",null);
			autoCallTaskTelephone.set("STATE","0");
			autoCallTaskTelephone.set("RETRIED",0);
			
			if(taskType.equalsIgnoreCase("3")) {   //如果是催缴类时
				
				//催缴类型编号： 1电费   2水费  3电话费  4燃气费 5物业费 6车辆违章  7交警移车  8社保催缴
				if(reminderType.equalsIgnoreCase("1")) {              //电费催缴
				    //用户号码|户号|地址|电费
				    //18951082343|1009988777|南京市玄武区XXX号XXX小区|220.14
				   	String accountNumber = r.get("1");      //户号
				   	String address = r.get("2");            //地址
				   	String charge = r.get("3");             //费用
				   	
				   	String period = DateFormatUtils.formatDateTime(new Date(), "yyyyMM");    //以当前的年月为日期
				   	
				   	autoCallTaskTelephone.set("CUSTOMER_NAME", customerTel);
				   	autoCallTaskTelephone.set("PERIOD", period);
				   	autoCallTaskTelephone.set("ACCOUNT_NUMBER", accountNumber);
				   	autoCallTaskTelephone.set("ADDRESS",address);
				   	autoCallTaskTelephone.set("CHARGE",charge);
					
				}else if(reminderType.equalsIgnoreCase("2")) {		  //水费催缴
					//用户|地址|本月抄见数|本月用量|本期金额|户号
					//18951082343|南京市玄武区XXX号XXX小区|5523|321|222.19|1001692206
					String address = r.get("1");                //地址
					String displayNumber = r.get("2");			//表显数量
					String dosage = r.get("3");                 //使用量
					String charge = r.get("4");                 //费用
					String accountNumber = r.get("5");          //户号
					String period = DateFormatUtils.formatDateTime(new Date(), "yyyyMM");    //以当前的年月为日期
					
					autoCallTaskTelephone.set("CUSTOMER_NAME", customerTel);
					autoCallTaskTelephone.set("ADDRESS",address);
					autoCallTaskTelephone.set("DISPLAY_NUMBER", displayNumber);
					autoCallTaskTelephone.set("DOSAGE",dosage);
					autoCallTaskTelephone.set("CHARGE",charge);
					autoCallTaskTelephone.set("ACCOUNT_NUMBER",accountNumber);
					autoCallTaskTelephone.set("PERIOD",period);
					
				}else if(reminderType.equalsIgnoreCase("3")) {		  //电话费催缴
					//用户号码|户号|地址|电话费
					//18951082343|100138341|南京市玄武区XXX号XXX小区|220.14
					String accountNumber = r.get("1");            //户号
					String address = r.get("2");                  //地址
					String charge = r.get("3");                   //费用
					
					String period = DateFormatUtils.formatDateTime(new Date(), "yyyyMM");    //以当前的年月为日期
					
					autoCallTaskTelephone.set("CUSTOMER_NAME", customerTel);
					autoCallTaskTelephone.set("ACCOUNT_NUMBER", accountNumber);
					autoCallTaskTelephone.set("ADDRESS", address);
					autoCallTaskTelephone.set("CHARGE", charge);
					autoCallTaskTelephone.set("PERIOD", period);
					
				}else if(reminderType.equalsIgnoreCase("4")) {		  //燃气费催缴
					//用户号码|户号|地址|燃气费
					//18951082343|100138341|南京市玄武区XXX号XXX小区|220.14
					String accountNumber = r.get("1");            //户号
					String address = r.get("2");                  //地址
					String charge = r.get("3");                   //费用
					
					String period = DateFormatUtils.formatDateTime(new Date(), "yyyyMM");    //以当前的年月为日期
					
					autoCallTaskTelephone.set("CUSTOMER_NAME", customerTel);
					autoCallTaskTelephone.set("ACCOUNT_NUMBER", accountNumber);
					autoCallTaskTelephone.set("ADDRESS", address);
					autoCallTaskTelephone.set("CHARGE", charge);
					autoCallTaskTelephone.set("PERIOD", period);
					
				}else if(reminderType.equalsIgnoreCase("5")) {		  //物业费催缴
					//用户号码|地址|物业费
					//18951082343|南京市玄武区XXX号XXX小区|220.14
					String address = r.get("1");                  //地址
					String charge = r.get("2");                   //费用
					
					String period = DateFormatUtils.formatDateTime(new Date(), "yyyyMM");    //以当前的年月为日期
					
					autoCallTaskTelephone.set("CUSTOMER_NAME", customerTel);
					autoCallTaskTelephone.set("ADDRESS", address);
					autoCallTaskTelephone.set("CHARGE", charge);
					autoCallTaskTelephone.set("PERIOD", period);
				}else if(reminderType.equalsIgnoreCase("6")) {		  //车辆违章
					//用户号码|车牌|违章日期|违章城市|违章事由|处罚单位
					//18951082343|苏DR1179|20181001|南京市|高速连续变道|南京市交警大队
					String plateNumber = r.get("1");                //车牌号
					String period = r.get("2");                     //违章日期
					String illegalCity = r.get("3");                //违章城市
					String illegalReason = r.get("4");              //违章事由
					String punishmentUnit = r.get("5");             //处罚单位
					
					autoCallTaskTelephone.set("CUSTOMER_NAME", customerTel);
					autoCallTaskTelephone.set("PLATE_NUMBER",plateNumber);
					autoCallTaskTelephone.set("PERIOD", period);
					autoCallTaskTelephone.set("ILLEGAL_CITY", illegalCity);
					autoCallTaskTelephone.set("ILLEGAL_REASON",illegalReason);
					autoCallTaskTelephone.set("PUNISHMENT_UNIT", punishmentUnit);
					
				}else if(reminderType.equalsIgnoreCase("7")) {		  //交警移车
					//用户号码|报警人电话|车辆类型|车牌号码
					//18951082343|13512771995|小型车辆|苏DA1179
					String callPoliceTel = r.get("1");                //报警人电话
					String vehicleType = r.get("2");                  //车辆类型
					String plateNumber = r.get("3");                  //车牌号码
					
					autoCallTaskTelephone.set("CUSTOMER_NAME", customerTel);
					autoCallTaskTelephone.set("CALL_POLICE_TEL",callPoliceTel);
					autoCallTaskTelephone.set("VEHICLE_TYPE", vehicleType);
					autoCallTaskTelephone.set("PLATE_NUMBER", plateNumber);
					
				}else if(reminderType.equalsIgnoreCase("8")) {		  //社保催缴
					//用户号码|社保费
					//18951082343|880.20
					String charge = r.get("1");                       //社保费
					
					String period = DateFormatUtils.formatDateTime(new Date(), "yyyyMM");    //以当前的年月为日期
					
					autoCallTaskTelephone.set("CUSTOMER_NAME", customerTel);
					autoCallTaskTelephone.set("PERIOD", period);
					autoCallTaskTelephone.set("CHARGE", charge);
				}
				
			}else {                                //非催缴类
				String customerName = r.get("1");     //得到客户姓名
				autoCallTaskTelephone.set("CUSTOMER_NAME",customerName);
			}
			
			/**
			 * 接下来，对号码进行判断
			 * 1 第一列，即号码是否为正常的号码
			 * 2号码是否为黑名单数据
			 */
			//取出两列，然后判断第一列是否是号码,且号码的长度是否大于等于7位数，否则将不储存
			if(!BlankUtils.isBlank(customerTel) && StringUtil.isNumber(customerTel) && customerTel.length() >= 7) {
				
				//做黑名单过滤
				if(blackListTelephones.contains(customerTel)) {   //如果存在于黑名单
					inBlackListCount++;
					continue;
				}
				
				afterBlackListResult.add(autoCallTaskTelephone);
			}else {
				continue;
			}
			
		}
		
		sb.append("黑名单过滤的号码数量为：" + inBlackListCount);
		
		//第二步：进行重复性检查
		List<String> autoCallTaskTelephones = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneByTaskId(taskId); 
		
		int repetitionCount = 0;
		for(Record act:afterBlackListResult) {
			
			String customerTel = act.get("CUSTOMER_TEL");
			
			//重复性检查
			if(autoCallTaskTelephones.contains(customerTel)) {
				repetitionCount++;
				continue;
			}
			
			afterRepetitionResult.add(act);
		}
		
		sb.append("<br/>重复的号码数量为:" + repetitionCount);
		
		//第三步，对数据的格式检查，比如日期格式：201810，户号格式：纯数字，金额、等的检查
		int formatWrongCount = 0;
		if(taskType.equalsIgnoreCase("3")) {   //如果是催缴类时,进行数据格式检查
			for(Record act:afterRepetitionResult) {  //进行数据格式检查
				String period = act.getStr("PERIOD");						//日期
				String accountNumber = act.getStr("ACCOUNT_NUMBER");        //户号
				String address = act.getStr("ADDRESS");               	 	//地址
				String charge = act.getStr("CHARGE");						//费用
				String displayNumber = act.getStr("DISPLAY_NUMBER"); 		//表显数量
				String dosage = act.getStr("DOSAGE");						//使用量
				String illegalCity = act.getStr("ILLEGAL_CITY");			//违章城市	
				String illegalReason = act.getStr("ILLEGAL_REASON");		//违章事由
				String punishmentUnit = act.getStr("PUNISHMENT_UNIT");		//处罚单位	
				String callPoliceTel = act.getStr("CALL_POLICE_TEL");		//报警人电话
				String vehicleType = act.getStr("VEHICLE_TYPE");			//车辆类型
				String plateNumber = act.getStr("PLATE_NUMBER");			//车牌号码
				//催缴类型编号： 1电费   2水费  3电话费  4燃气费 5物业费 6车辆违章  7交警移车  8社保催缴
				if(reminderType.equalsIgnoreCase("1")) {              	//电费催缴
					//需要检查：日期、户号、地址、费用
					//(1)检查日期
					if(!checkPeriodFormat(period)) {    
						formatWrongCount++;
						continue;
					}
					//(2)检查户号
					if(!checkAccountNumber(accountNumber)) {
						formatWrongCount++;
						continue;
					}
					//（3）检查地址
					if(BlankUtils.isBlank(address)) {
						formatWrongCount++;
						continue;
					}
					//(4) 检查费用
					if(!checkCharge(charge)) {
						formatWrongCount++;
						continue;
					}
					
				}else if(reminderType.equalsIgnoreCase("2")) {			//水费催缴
					//需要检查：日期、地址、表显数据、使用量、费用、户号
					//（1）检查日期
					if(!checkPeriodFormat(period)) {
						formatWrongCount++;
						continue;
					}
					//(2)检查地址
					if(BlankUtils.isBlank(address)) {
						formatWrongCount++;
						continue;
					}
					//(3)检查表显数据
					if(!checkDisplayNumber(displayNumber)) {
						formatWrongCount++;
						continue;
					}
					//（4）使用量
					if(!checkDosage(dosage)) {
						formatWrongCount++;
						continue;
					}
					//（5）检查费用
					if(!checkCharge(charge)) {
						formatWrongCount++;
						continue;
					}
					//(6)检查户号
					if(!checkAccountNumber(accountNumber)) {
						formatWrongCount++;
						continue;
					}
					
				}else if(reminderType.equalsIgnoreCase("3")) {			//电话费催缴
					//需要检查：日期、户号、地址、电话费
					//（1）检查日期
					if(!checkPeriodFormat(period)) {
						formatWrongCount++;
						continue;
					}
					//(2)检查户号
					if(!checkAccountNumber(accountNumber)) {
						formatWrongCount++;
						continue;
					}
					//(3)检查地址
					if(BlankUtils.isBlank(address)) {
						formatWrongCount++;
						continue;
					}
					//（4）检查费用
					if(!checkCharge(charge)) {
						formatWrongCount++;
						continue;
					}
				}else if(reminderType.equalsIgnoreCase("4")) {			//燃气费催缴
					//需要检查：日期、户号、地址、电话费
					//（1）检查日期
					if(!checkPeriodFormat(period)) {
						formatWrongCount++;
						continue;
					}
					//(2)检查户号
					if(!checkAccountNumber(accountNumber)) {
						formatWrongCount++;
						continue;
					}
					//(3)检查地址
					if(BlankUtils.isBlank(address)) {
						formatWrongCount++;
						continue;
					}
					//（4）检查费用
					if(!checkCharge(charge)) {
						formatWrongCount++;
						continue;
					}
				}else if(reminderType.equalsIgnoreCase("5")) {			//物业费催缴
					//需要检查：日期、地址、物业费
					//（1）检查日期
					if(!checkPeriodFormat(period)) {
						formatWrongCount++;
						continue;
					}
					//(2)检查地址
					if(BlankUtils.isBlank(address)) {
						formatWrongCount++;
						continue;
					}
					//（3）检查费用
					if(!checkCharge(charge)) {
						formatWrongCount++;
						continue;
					}
					
				}else if(reminderType.equalsIgnoreCase("6")) {			//车辆违章
					//需要检查：车牌号码、日期、违法城市、违法事由、处罚单位
					//（1）检查车牌号码
					if(BlankUtils.isBlank(plateNumber)) {
						formatWrongCount++;
						continue;
					}
					//（2）检查日期
					if(!checkPeriodFormat(period)) {
						formatWrongCount++;
						continue;
					}
					//(3)检查违法城市
					if(BlankUtils.isBlank(illegalCity)) {
						formatWrongCount++;
						continue;
					}
					//（4)检查事由
					if(BlankUtils.isBlank(illegalReason)) {
						formatWrongCount++;
						continue;
					}
					//(5)自罚单位
					if(BlankUtils.isBlank(punishmentUnit)) {
						formatWrongCount++;
						continue;
					}
					
				}else if(reminderType.equalsIgnoreCase("7")) {          //7交警移车
					//需要检查：报警人电话、车辆类型、车牌号码
					//（1）检查报警人电话，
					if(!checkCallPoliceTel(callPoliceTel)) {
						formatWrongCount++;
						continue;
					}
					//(2)检查车辆类型
					if(BlankUtils.isBlank(vehicleType)) {
						formatWrongCount++;
						continue;
					}
					//(3)检查车牌号码
					if(BlankUtils.isBlank(plateNumber)) {
						formatWrongCount++;
						continue;
					}
				}else if(reminderType.equalsIgnoreCase("8")) {          //社保催缴
					//需要检查：日期、费用
					//（1）检查日期
					if(!checkPeriodFormat(period)) {
						formatWrongCount++;
						continue;
					}
					//(2)检查费用
					if(!checkCharge(charge)) {
						formatWrongCount++;
						continue;
					}
				}
				afterFormatCheckResult.add(act);
			}
			
			sb.append("<br/>格式错误号码数量为:" + formatWrongCount);
			
		}else {
			afterFormatCheckResult = afterRepetitionResult;
		}
		int count  = AutoCallTaskTelephone.dao.batchSave(afterFormatCheckResult);
	
		sb.append("<br/>成功插入号码数量为 :" + count + "!");
		
		return sb.toString();
		
	}
	
	//检查日期，一般日期为 201810或是20181020 
	public static boolean checkPeriodFormat(String period) {
		if(BlankUtils.isBlank(period)) {    //为空时，返回 false
			return false;
		}
		
		boolean b = DateFormatUtils.checkDateFormat(period,"yyyyMM");
		boolean b1 = DateFormatUtils.checkDateFormat(period,"yyyyMMdd");   
		
		if(b || b1) {     //只要有一种格式匹配，即表示正确
			return true;
		}else {
			return false;
		}
	}
	
	//检查户号,是否不为空，且为纯数字
	public static boolean checkAccountNumber(String accountNumber) {
		if(BlankUtils.isBlank(accountNumber)) {
			return false;
		}
		//是否为纯数字
		boolean b =  StringUtil.isNumber(accountNumber);
		return b;
	}
	
	//检查费用
	public static boolean checkCharge(String charge) {
		if(BlankUtils.isBlank(charge)) {
			return false;
		}
		
		boolean isMoney = StringUtil.isMoney(charge);
		return isMoney;
	}
	
	//检查表显数量
	public static boolean checkDisplayNumber(String displayNumber) {
		if(BlankUtils.isBlank(displayNumber)) {
			return false;
		}
		
		//是否为纯数字
		boolean b =  StringUtil.isNumber(displayNumber);
		return b;
	}
	
	//检查用量
	public static boolean checkDosage(String dosage) {
		if(BlankUtils.isBlank(dosage)) {
			return false;
		}
		
		//是否为纯数字
		boolean b =  StringUtil.isNumber(dosage);
		return b;
	}
	
	//检查报警人电话号码
	public static boolean checkCallPoliceTel(String callPoliceTel) {
		
		if(BlankUtils.isBlank(callPoliceTel)) {
			return false;
		}
		
		//是否为数字
		boolean b = StringUtil.isNumber(callPoliceTel);
		if(!b) {
			return false;
		}
		
		//长度是否大于7位以上
		if(callPoliceTel.length()>=7) {
			return true;
		}else {
			return false;
		}
		
	}
	
	
	
	public void exportExcel() {
		
		String taskId = getPara("taskId");
		String state = getPara("state");
		String messageState = getPara("messageState");
		String customerTel = getPara("customerTel");
		String customerName = getPara("customerName");
		String startTimeForTelephone = getPara("startTimeForTelephone");
		String endTimeForTelephone = getPara("endTimeForTelephone");
		String dateTimeType = getPara("dateTimeType");     //取得查询时间类型，0表示时间区段为以创建时间为查询区间，1表示以外呼时间为查询区间 
		
		String createTimeStartTime = null;
		String createTimeEndTime = null;
		String loadTimeStartTime = null;
		String loadTimeEndTime = null;
		
		if(BlankUtils.isBlank(dateTimeType)) {
			dateTimeType = "0";
		}
		
		if(dateTimeType.equalsIgnoreCase("1")) {           //表示是以外呼时间为查询区间
			loadTimeStartTime = startTimeForTelephone;
			loadTimeEndTime = endTimeForTelephone;
		}else {                                            //表示是以创建时间为查询区间
			createTimeStartTime = startTimeForTelephone;
			createTimeEndTime = endTimeForTelephone;
		}
		
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		String taskType = autoCallTask.get("TASK_TYPE");          //任务类型
		String reminderType = autoCallTask.get("REMINDER_TYPE");  //催缴类型
		int retryTimes = autoCallTask.getInt("RETRY_TIMES");
		
		//如果传入的状态为5或是为空时
		if(BlankUtils.isBlank(state) || state.equalsIgnoreCase("5")) {
			state = null;
		}
		
		List<Record> list = AutoCallTaskTelephone.dao.getAutoCallTaskTelephonesByTaskIdAndState(taskId, state,messageState,customerTel,customerName,createTimeStartTime,createTimeEndTime,loadTimeStartTime,loadTimeEndTime,retryTimes);
		
		String fileName = "export.xls";
		String sheetName = "号码列表";
		
		ExcelExportUtil export = new ExcelExportUtil(list,getResponse());
		if(taskType.equalsIgnoreCase("1") || taskType.equalsIgnoreCase("2")) {                //如果为普通外呼
			String[] headers = {"客户姓名","电话号码","省份","城市","外呼号码","创建时间","外呼结果","失败原因","呼叫次数","外呼时间","通话时长","下次外呼时间","短信状态","短信错误代码"};            
			String[] columns = {"CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","CALLOUT_TEL","CREATE_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED_DESC","LOAD_TIME","BILLSEC","NEXT_CALLOUT_TIME","MESSAGE_STATE_DESC","MESSAGE_FAILURE_CODE"};
			
			export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
		}else if(taskType.equalsIgnoreCase("3")) {          //如果为催缴外呼
			
			if(reminderType.equalsIgnoreCase("6")) {        //催缴类型为车辆违章
				String[] headers = {"客户姓名","电话号码","省份","城市","外呼号码","创建时间","外呼结果","失败原因","呼叫次数","外呼时间","通话时长","下次外呼时间","短信状态","短信错误代码","日期","车牌号码","违法城市","处罚单位","违法理由"};            
				String[] columns = {"CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","CALLOUT_TEL","CREATE_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED_DESC","LOAD_TIME","BILLSEC","NEXT_CALLOUT_TIME","MESSAGE_STATE_DESC","MESSAGE_FAILURE_CODE","PERIOD","PLATE_NUMBER","ILLEGAL_CITY","PUNISHMENT_UNIT","ILLEGAL_REASON"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
			}else if(reminderType.equalsIgnoreCase("7")) {  //交警移车
				String[] headers = {"客户姓名","电话号码","省份","城市","外呼号码","创建时间","外呼结果","失败原因","呼叫次数","外呼时间","通话时长","下次外呼时间","短信状态","短信错误代码","报警人电话","车辆类型","车牌号码"};            
				String[] columns = {"CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","CALLOUT_TEL","CREATE_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED_DESC","LOAD_TIME","BILLSEC","NEXT_CALLOUT_TIME","MESSAGE_STATE_DESC","MESSAGE_FAILURE_CODE","CALL_POLICE_TEL","VEHICLE_TYPE","PLATE_NUMBER"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
			}else if(reminderType.equalsIgnoreCase("8")) {  //社保催缴
				String[] headers = {"客户姓名","电话号码","省份","城市","外呼号码","创建时间","外呼结果","失败原因","呼叫次数","外呼时间","通话时长","下次外呼时间","短信状态","短信错误代码","日期","费用"};            
				String[] columns = {"CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","CALLOUT_TEL","CREATE_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED_DESC","LOAD_TIME","BILLSEC","NEXT_CALLOUT_TIME","MESSAGE_STATE_DESC","MESSAGE_FAILURE_CODE","PERIOD","CHARGE"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
			}else if(reminderType.equalsIgnoreCase("1")){   //电费催缴
				String[] headers = {"客户姓名","电话号码","省份","城市","外呼号码","创建时间","外呼结果","失败原因","呼叫次数","外呼时间","通话时长","下次外呼时间","短信状态","短信错误代码","日期","户号","地址","费用"};            
				String[] columns = {"CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","CALLOUT_TEL","CREATE_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED_DESC","LOAD_TIME","BILLSEC","NEXT_CALLOUT_TIME","MESSAGE_STATE_DESC","MESSAGE_FAILURE_CODE","PERIOD","ACCOUNT_NUMBER","ADDRESS","CHARGE"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
			}else if(reminderType.equalsIgnoreCase("2")){   //水费催缴
				String[] headers = {"客户姓名","电话号码","省份","城市","外呼号码","创建时间","外呼结果","失败原因","呼叫次数","外呼时间","通话时长","下次外呼时间","短信状态","短信错误代码","日期","地址","表显数量","使用量","费用","户号"};            
				String[] columns = {"CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","CALLOUT_TEL","CREATE_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED_DESC","LOAD_TIME","BILLSEC","NEXT_CALLOUT_TIME","MESSAGE_STATE_DESC","MESSAGE_FAILURE_CODE","PERIOD","ADDRESS","DISPLAY_NUMBER","DOSAGE","CHARGE","ACCOUNT_NUMBER"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
			}else{        				//如果为电话、气及物业催缴
				String[] headers = {"客户姓名","电话号码","省份","城市","外呼号码","创建时间","外呼结果","失败原因","呼叫次数","外呼时间","通话时长","下次外呼时间","短信状态","短信错误代码","日期","费用"};            
				String[] columns = {"CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","CALLOUT_TEL","CREATE_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED_DESC","LOAD_TIME","BILLSEC","NEXT_CALLOUT_TIME","MESSAGE_STATE_DESC","MESSAGE_FAILURE_CODE","PERIOD","CHARGE"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
			}
			
		}
		
		export.fileName(fileName).execExport();
		
	}

}
