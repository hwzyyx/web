package com.callke8.autocall.autocalltask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		System.out.println("取AutoCallTaskTelephoneController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String taskId = getPara("taskId");
		String telephone = getPara("telephone");
		String clientName = getPara("clientName");
		String state = getPara("state");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneByPaginateToMap(pageNumber, pageSize, taskId, telephone, clientName,state);
		
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
		String telephone = actt.get("TELEPHONE");
		
		//根据taskId,先取出任务信息
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		boolean checkRs = checkBlackList(telephone,autoCallTask);   //黑名单检查
		if(checkRs) {
			render(RenderJson.error("新增号码失败,号码" + telephone + "在黑名单中!"));
			return;
		}
		
		//（2）再判断任务的号码中是否存在相同的号码
		boolean repetitionRs = AutoCallTaskTelephone.dao.checkTelephoneRepetition(telephone, taskId);
		if(repetitionRs) {
			render(RenderJson.error("新增号码失败,号码" + telephone + "已重复!"));
			return;
		}
		
		autoCallTaskTelephone.set("TASK_ID", taskId);
		autoCallTaskTelephone.set("TELEPHONE",actt.get("TELEPHONE"));
		autoCallTaskTelephone.set("CLIENT_NAME",actt.get("CLIENT_NAME"));
		autoCallTaskTelephone.set("COMPANY", actt.get("COMPANY"));
		autoCallTaskTelephone.set("PERIOD", actt.get("PERIOD"));
		autoCallTaskTelephone.set("CHARGE", actt.get("CHARGE"));
		autoCallTaskTelephone.set("VIOLATION_CITY", actt.get("VIOLATION_CITY"));
		autoCallTaskTelephone.set("PUNISHMENT_UNIT",actt.get("PUNISHMENT_UNIT"));
		autoCallTaskTelephone.set("VIOLATION_REASON", actt.get("VIOLATION_REASON"));
		autoCallTaskTelephone.set("RETRIED",0);
		autoCallTaskTelephone.set("STATE",0);
		autoCallTaskTelephone.set("RESPOND",null);
		autoCallTaskTelephone.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = AutoCallTaskTelephone.dao.add(autoCallTaskTelephone);
		
		if(b) {
			render(RenderJson.success("新增号码成功!"));
		}else {
			render(RenderJson.error("新增号码失败!"));
		}
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
 			
 			String telephone = r.get("TELEPHONE");
 			
 			autoCallTaskTelephone.set("TASK_ID",taskId);
 			autoCallTaskTelephone.set("TELEPHONE",telephone);
 			autoCallTaskTelephone.set("CLIENT_NAME",r.get("CLIENT_NAME"));
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
			if(!BlankUtils.isBlank(telephone) && StringUtil.isNumber(telephone) && telephone.length() >= 7) {
				
				//做黑名单过滤
				if(blackListTelephones.contains(telephone)) {   //如果存在于黑名单
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
			
			String telephone = act.get("TELEPHONE");
			
			//重复性检查
			if(autoCallTaskTelephones.contains(telephone)) {
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
		
		String telephone = autoCallTaskTelephone.get("TELEPHONE");       //号码
		String clientName = autoCallTaskTelephone.get("CLIENT_NAME");    //客户姓名
		Integer telId = Integer.valueOf(autoCallTaskTelephone.get("TEL_ID").toString());
		
		String period = autoCallTaskTelephone.get("PERIOD");
		String violationCity = autoCallTaskTelephone.get("VIOLATION_CITY");
		String punishmentUnit = autoCallTaskTelephone.get("PUNISHMENT_UNIT");
		String violationReason = autoCallTaskTelephone.get("VIOLATION_REASON");
		String charge = autoCallTaskTelephone.get("CHARGE");
		String company = autoCallTaskTelephone.get("COMPANY");
 		
		//(1)在修改之前,该号码是否在黑名单中
		String taskId = getPara("taskId");    //得到任务ID
		//根据任务ID，取出任务信息
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);   
		boolean checkRs = checkBlackList(telephone,autoCallTask);
		if(checkRs) {
			render(RenderJson.error("修改后的号码在黑名单中,修改失败!"));
			return;
		}
		
		//（2）再判断任务的号码中是否存在相同的号码
		boolean repetitionRs = AutoCallTaskTelephone.dao.checkTelephoneRepetition(telephone, taskId);
		if(repetitionRs) {
			render(RenderJson.error("修改号码失败,号码" + telephone + "已重复!"));
			return;
		}
		
		
		boolean b = AutoCallTaskTelephone.dao.update(telephone,clientName,period,violationCity,punishmentUnit,violationReason,charge,company,telId);
		
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
		
	}
	
	/**
	 * 根据任务ID,和电话号码，查看当前号码，是否黑名单
	 * 
	 * @param telephone
	 * @param taskId
	 * @return
	 * 		  true:表示在黑名单中
	 * 		  false: 表示不在黑名单中
	 */
	public boolean checkBlackList(String telephone,AutoCallTask autoCallTask) {
		
		boolean b = false;
		
		if(!BlankUtils.isBlank(autoCallTask)) {
			
			String blackListId = autoCallTask.get("BLACKLIST_ID");    //取出黑名单ID
			
			if(!BlankUtils.isBlank(blackListId)) {                    //如果该任务选择了黑名单
				
				int count = AutoBlackListTelephone.dao.getAutoBlackListTelephoneCountByCondition(telephone, blackListId);
				
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
		
		//取出黑名单中的号码,并以List返回,用于黑名单过滤
		List<String> blackListTelephones = AutoBlackListTelephone.dao.getAutoBlackListTelephoneByBlackListId(blackListId);
		
		
		//（1）第一步做黑名单过滤判断
		int inBlackListCount = 0;
		for(int i=0;i<list.size();i++) {
			
			Record autoCallTaskTelephone = new Record();   //新建一个Record用于储存数据
			
			Record r = list.get(i);
			
			String telephone = r.get("0");         //所有的上传号码文件，第1列是号码
			autoCallTaskTelephone.set("TASK_ID",taskId);
			autoCallTaskTelephone.set("TELEPHONE", telephone);
			autoCallTaskTelephone.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
			autoCallTaskTelephone.set("RESPOND",null);
			autoCallTaskTelephone.set("STATE","0");
			autoCallTaskTelephone.set("RETRIED",0);
			
			if(taskType.equalsIgnoreCase("3")) {   //如果是催缴类时
				
				//如果催缴类型为1/2/3/4/5,即是电话费、电费、水费、燃气费和物业费时；
				//模板是：电话号码|日期|费用
				//       13811110000|201605|25.6
				if(reminderType.equalsIgnoreCase("1")||reminderType.equalsIgnoreCase("2")||reminderType.equalsIgnoreCase("3")||reminderType.equalsIgnoreCase("4")||reminderType.equalsIgnoreCase("5")) {
					String period = r.get("1");   //日期
					String charge = r.get("2");   //费用
					autoCallTaskTelephone.set("CLIENT_NAME", telephone);
					autoCallTaskTelephone.set("PERIOD", period);
					autoCallTaskTelephone.set("CHARGE", charge);
				}else if(reminderType.equalsIgnoreCase("6")) {   //交通违章催缴
					
					//物业催缴模板：号码|日期|城市|处罚单位|违法事由
					//             13811110000|20170101|广州市|广州市第一交警大队|违章停车
					String period = r.get("1");    //日期
					String violationCity = r.get("2");    //违法城市
					String punishmentUnit = r.get("3");   //处罚单位
					String violationReason = r.get("4");  //违章理由
					
					autoCallTaskTelephone.set("CLIENT_NAME", telephone);
					autoCallTaskTelephone.set("PERIOD", period);
					autoCallTaskTelephone.set("VIOLATION_CITY", violationCity);
					autoCallTaskTelephone.set("PUNISHMENT_UNIT", punishmentUnit);
					autoCallTaskTelephone.set("VIOLATION_REASON", violationReason);
				}else if(reminderType.equalsIgnoreCase("7")) {    //社保催缴
					//社保催缴模板:号码|姓名|日期|单位
					//            13811110000|张三|201701|某公司
					String clientName = r.get("1");     //姓名
					String period = r.get("2");         //日期
					String company = r.get("3");        //代缴单位
					
					autoCallTaskTelephone.set("CLIENT_NAME",clientName);
					autoCallTaskTelephone.set("PERIOD", period);
					autoCallTaskTelephone.set("COMPANY", company);
				}
				
			}else {                                //非催缴类
				String clientName = r.get("1");     //得到客户姓名
				autoCallTaskTelephone.set("CLIENT_NAME",clientName);
			}
			
			/**
			 * 接下来，对号码进行判断
			 * 1 第一列，即号码是否为正常的号码
			 * 2号码是否为黑名单数据
			 */
			//取出两列，然后判断第一列是否是号码,且号码的长度是否大于等于7位数，否则将不储存
			if(!BlankUtils.isBlank(telephone) && StringUtil.isNumber(telephone) && telephone.length() >= 7) {
				
				//做黑名单过滤
				if(blackListTelephones.contains(telephone)) {   //如果存在于黑名单
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
			
			String telephone = act.get("TELEPHONE");
			
			//重复性检查
			if(autoCallTaskTelephones.contains(telephone)) {
				repetitionCount++;
				continue;
			}
			
			afterRepetitionResult.add(act);
		}
		
		sb.append("<br/>重复的号码数量为:" + repetitionCount);
		
		int count  = AutoCallTaskTelephone.dao.batchSave(afterRepetitionResult);
	
		sb.append("<br/>成功插入号码数量为 :" + count + "!");
		
		return sb.toString();
		
	}
	
	public void exportExcel() {
		
		String taskId = getPara("taskId");
		String state = getPara("state");
		String telephone = getPara("telephone");
		String clientName = getPara("clientName");
		
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
		String taskType = autoCallTask.get("TASK_TYPE");          //任务类型
		String reminderType = autoCallTask.get("REMINDER_TYPE");  //催缴类型
		
		//如果传入的状态为5或是为空时
		if(BlankUtils.isBlank(state) || state.equalsIgnoreCase("5")) {
			state = null;
		}
		
		List<Record> list = AutoCallTaskTelephone.dao.getAutoCallTaskTelephonesByTaskIdAndState(taskId, state, telephone,clientName);
		
		String fileName = "export.xls";
		String sheetName = "号码列表";
		
		ExcelExportUtil export = new ExcelExportUtil(list,getResponse());
		if(taskType.equalsIgnoreCase("1") || taskType.equalsIgnoreCase("2")) {                //如果为普通外呼
			String[] headers = {"电话号码","客户姓名"};            
			String[] columns = {"TELEPHONE","CLIENT_NAME"};
			
			export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
		}else if(taskType.equalsIgnoreCase("3")) {          //如果为催缴外呼
			
			if(reminderType.equalsIgnoreCase("6")) {        //催缴类型为车辆违章
				
				String[] headers = {"电话号码","客户姓名","违章城市","处罚单位","违章事由","日期"};            
				String[] columns = {"TELEPHONE","CLIENT_NAME","VIOLATION_CITY","PUNISHMENT_UNIT","VIOLATION_REASON","PERIOD"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
			}else if(reminderType.equalsIgnoreCase("7")) {  //催缴类型为社保催缴
				String[] headers = {"电话号码","客户姓名","日期","代缴单位"};            
				String[] columns = {"TELEPHONE","CLIENT_NAME","PERIOD","COMPANY"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
			}else{        //如果为电话、水、电、气及物业催缴
				
				String[] headers = {"电话号码","客户姓名","日期","费用"};            
				String[] columns = {"TELEPHONE","CLIENT_NAME","PERIOD","CHARGE"};
				export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
				
			}
			
		}
		
		export.fileName(fileName).execExport();
		
	}

}
