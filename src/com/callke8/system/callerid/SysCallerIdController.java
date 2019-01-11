package com.callke8.system.callerid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.common.IController;
import com.callke8.system.calleridassign.SysCallerIdAssign;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.ComboboxJson;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TxtUtils;
import com.callke8.utils.XLSUtils;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import net.sf.json.JSONArray;

public class SysCallerIdController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		
		String callerId = getPara("callerId");
		String purpose = getPara("purpose");
		String ids = getPara("ids");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = SysCallerId.dao.getSysCallerIdByPaginateToMap(pageNumber,pageSize,callerId,purpose,ids);
		
		renderJson(map);
		
	}
	
	/**
	 * datagrid 主要是用于创建外呼任务时，选择主叫号码，或是显示自动外呼任务已经选择的主叫号码
	 * 
	 * (1)如果传入的自动外呼任务ID为空，则表示查询当前登录操作员分配到的主叫号码情况
	 * （2）如果传入的自动外呼任务不为空，则表示显示传入的自动外呼任务已经选择的主叫号码
	 * 
	 */
	public void datagridForOperIdOrAutoCallTask() {
		
		String taskId = getPara("targetTaskId");
		String callerId = getPara("callerId");
		String purpose = getPara("purpose");
		
		String ids = "";
		if(!BlankUtils.isBlank(taskId)) {    //表示需要查询传入的自动外呼任务已经选择的主叫号码
			AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);
			ids = autoCallTask.getStr("CALLERID");
		}else {
			String operId = String.valueOf(getSession().getAttribute("currOperId"));    //当前登录的用户
			//查询当前客户被分配的主叫号码的 ID列表
			List<Record> beAssignlist = SysCallerIdAssign.dao.getSysCallerIdAssignByOperId(operId);
			
			//遍历查询出来的已经分配的列表，并组织成 ids
			for(Record sysCallerIdAssign:beAssignlist) {
				ids += sysCallerIdAssign.getInt("CALLERID_ID") + ",";
			}
			
			if(!BlankUtils.isBlank(ids) && ids.length() > 0) {
				//如果不为空，删除最后一个 逗号
				ids = ids.substring(0, ids.length()-1);
			}
		}
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = SysCallerId.dao.getSysCallerIdByPaginateToMap(pageNumber,pageSize,callerId,purpose,ids);
		
		renderJson(map);
		
	}
	
	/**
	 * 查询主叫号码，传入自动外呼任务ID
	 */
	public void datagridForAutoCallTask() {
		
		String autoCallTaskId = getPara("taskId");
		String callerId = getPara("callerId");
		String purpose = getPara("purpose");
		
		AutoCallTask autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(autoCallTaskId);
		
		String ids = autoCallTask.getStr("CALLERID");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = SysCallerId.dao.getSysCallerIdByPaginateToMap(pageNumber,pageSize,callerId,purpose,ids);
		
		renderJson(map);
	}

	@Override
	public void add() {
		SysCallerId sysCallerId = getModel(SysCallerId.class,"sysCallerId");
		
		//(1)检查号码的格式
		String callerId = sysCallerId.getStr("CALLERID");
		boolean isNumber = StringUtil.isNumber(callerId);    //检查是否为纯数字
		if(!isNumber) {
			render(RenderJson.error("主叫号码非纯数字，添加主叫号码失败!"));
			return;
		}
		
		//(2)检查是否已经存在相同的主叫号码了
		SysCallerId sci = SysCallerId.dao.getSysCallerIdByCallerId(callerId);
		if(!BlankUtils.isBlank(sci)) {
			render(RenderJson.error("系统已存在相同主叫号码，添加主叫号码失败!"));
			return;
		}
		
		sysCallerId.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));
		
		SysCallerId.dao.add(sysCallerId);
		System.out.println(sysCallerId);
		
		render(RenderJson.success("添加主叫号码成功!"));
		
	}

	@Override
	public void update() {
		SysCallerId sysCallerId = getModel(SysCallerId.class,"sysCallerId");
		
		int id = sysCallerId.getInt("ID");
		String callerId = sysCallerId.getStr("CALLERID");
		String purpose = sysCallerId.getStr("PURPOSE");
		
		//检查新修改上去的号码，是否被别的记录占用
		SysCallerId sci = SysCallerId.dao.getSysCallerIdByCallerId(callerId);
		if(!BlankUtils.isBlank(sci)) {
			
			int callerId_id = sci.getInt("ID");
			
			if(id!=callerId_id) {
				render(RenderJson.error("已存在相同的主叫号码，修改号码失败!"));
				return;
			}
		}
		
		boolean b = SysCallerId.dao.update(callerId,purpose,id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		
		int id = Integer.valueOf(getPara("id"));
		
		boolean b = SysCallerId.dao.deleteById(id);
		if(b) {
			//如果删除成功，还需要删除这个号码分配给相关操作员的记录
			int count = SysCallerIdAssign.dao.deleteSysCallerIdAssignByCallerId_Id(id);
			
			render(RenderJson.success("删除成功!"));
			
		}else {
			render(RenderJson.error("删除失败!"));
		}
		
	}
	
	/**
	 * 根据当前登录的操作员ID（OPER_ID），将该操作员分配到的主叫号码，以 combobox 数据返回
	 * 
	 * @param operId
	 * 			操作员ID
	 * @param flag
	 * 			0：没有请选择; 1： 指定请选择
	 */
	public static String getSysCallerIdToComboboxByOperId(String operId,String flag) {
		
		String comboboxString = null;
		
		List<Record> sysCallerIdList = SysCallerId.dao.getAllSysCallerId();    						//取出所有的主叫号码
		List<Record> assignList = SysCallerIdAssign.dao.getSysCallerIdAssignByOperId(operId);       //取出操作员被分配到的情况
		
		List<Record> newList = new ArrayList<Record>();     //定义一个新的 list
		
		if(!BlankUtils.isBlank(sysCallerIdList) && sysCallerIdList.size()>0) {                       //主叫号码列表大于0时
			if(!BlankUtils.isBlank(assignList) && assignList.size()>0) {
				for(Record sysCallerId:sysCallerIdList) {      //遍历主叫号码
					int id = sysCallerId.getInt("ID");              		//ID
					//String callerId = sysCallerId.getStr("CALLERID");       //主叫号码
					for(Record sysCallerIdAssign:assignList) {      //再遍历分配的结果
						int callerId_Id = sysCallerIdAssign.getInt("CALLERID_ID");    //取出分配到的 ID
						if(id == callerId_Id) {
							newList.add(sysCallerId);
						}
					}
				}
			}
		}
		
		
		List<ComboboxJson> cbjs = new ArrayList<ComboboxJson>();   //定义一个TreeJson 的 list
		ComboboxJson defalutCbj = new ComboboxJson();
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			defalutCbj = new ComboboxJson();
			defalutCbj.setId("empty");
			defalutCbj.setText("请选择");
			
			cbjs.add(defalutCbj);
			
		}
		
		if(!BlankUtils.isBlank(newList) && newList.size()>0) {
			for(Record record:newList) {
				ComboboxJson cbj = new ComboboxJson();
				cbj.setId(record.get("ID").toString());
				cbj.setText(record.get("CALLERID").toString() + "(" + record.getStr("PURPOSE") + ")");
				
				cbjs.add(cbj);
			}
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cbjs);
		
		return jsonArray.toString();
		
	}
	
	/**
	 * 通过上传文件的方式上传主叫号码
	 */
	public void uploadFile() {
		
		//先判断文件大小限制
		int fileSize = getRequest().getContentLength();     //得到上传文件的大小，由于jfinal默认最大的上传的大小为 10 * 1024 * 1024 即是 10M
		if(fileSize > (10 * 1024 * 1024)) {    //如果大于 10M 时，返回错误
			render(RenderJson.error("上传失败：上传文件过大，已经超过 10M"));
			return;
		}
		
		//获取上传的文件
		//（1）定义上传路径，临时文件将上传到 upload 文件夹
		String uploadDir = PathKit.getWebRootPath() + File.separator + "upload" + File.separator;
		//（2）执行上传操作
		UploadFile uf = getFile("callerIdFile",uploadDir);
		
		//（3）判断上传文件的类型
		String mimeType = StringUtil.getExtensionName(uf.getFileName());
		
		//判断文件类型，是否为 txt、xls 或是 xlsx
		if(BlankUtils.isBlank(mimeType) || (!mimeType.equalsIgnoreCase("xls") && !mimeType.equalsIgnoreCase("xlsx") && !mimeType.equalsIgnoreCase("txt"))) {
			//提示错误之前，先将上传的文件删除
			if(!BlankUtils.isBlank(uf.getFile()) && uf.getFile().exists())  {
				uf.getFile().delete();
			}
			
			render(RenderJson.error("上传失败,上传的号码文件类型不正确,只支持 txt、xls 或是 xlsx"));
			return;
		}
		
		//上传的文件名可能是中文名，不利于读取，需先重命名为数字格式文件名
		String renameFileName = DateFormatUtils.getTimeMillis() + "." + mimeType;
		File newFile = new File(uploadDir + renameFileName);
		
		uf.getFile().renameTo(newFile);
		
		String insertResult = insertCallerIdFromFile(newFile,mimeType);    //成功插入的数量
		
		render(RenderJson.success(insertResult));
	}
	
	/**
	 * 从 xls、 txt 读取主叫号码，并插入主叫号码到数据库
	 * 
	 * @param file
	 * @param mimeType
	 * @return
	 */
	public String insertCallerIdFromFile(File file,String mimeType) {
		
		StringBuilder sb = new StringBuilder();
		
		//把文件中的信息取出并放置到一个  List 中， Record 以数字顺序存储数据
		List<Record> list = null;
		
		if(mimeType.equalsIgnoreCase("txt")) {     //如果文件类型为 txt
			list = TxtUtils.readTxt(file);
		}else {
			list = XLSUtils.readXls(file);
		}
		
		//建立两个 List， 一个用于检查数据格式，一个用于检查重复性
		ArrayList<Record> afterCheckFormatList = new ArrayList<Record>();      //结束格式检查后
		ArrayList<Record> afterRepetitionList = new ArrayList<Record>();       //结束数据重复性后
		ArrayList<String> numberList = new ArrayList<String>();                //创建一个号码的列表，用于过滤上传的文件号码的整批号码的重复性，因为不但需要对比已存入数据库的主叫号码的重复性，还需要对比自身的号码的重复性
		
		//创建两个属性
		int validCount = 0;     //有效号码数据
		int repeatCount = 0;    //重复的数据量
		
		//第一步：判断数据的格式
		for(Record r:list) {   //遍历数据，并格式格式
			String callerIdNumber = r.get("0");     //第一个数据是号码
			String callerIdPurpose = r.get("1");    //第二个数据是号码用途
			
			boolean isNumber = StringUtil.isNumber(callerIdNumber);    //判断第一列是否为纯数字
			if(isNumber) {    //第一列为数字时，才加入格式正确的数据
				//（1）如果第一列为号码时，并等于这些号码就可以直接存入数据库，还需要判断号码的长度，只有号码长度大于5位，即是 95151，10000号之类，或是座机号码
				if(callerIdNumber.length() < 5) {     //小于5时，跳过循环
					continue;
				}
				
				//（2）再过滤自身的重复性
				boolean isContain = numberList.contains(callerIdNumber);
				if(isContain) {    //如果已经存在该号码了，就跳过循环
					continue;
				}
				
				r.set("callerIdNumber", callerIdNumber);
				if(BlankUtils.isBlank(callerIdPurpose)) {
					callerIdPurpose = callerIdNumber;
				}
				r.set("callerIdPurpose", callerIdPurpose);
				r.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));           //创建人
				r.set("CREATE_TIME",DateFormatUtils.getCurrentDate());                       //创建时间
				
				numberList.add(callerIdNumber);
				afterCheckFormatList.add(r);    //加入检查数据格式后的List
			}
		}
		
		validCount = afterCheckFormatList.size();           //有效的号码数量
		numberList = new ArrayList<String>();         		//清空这个用于过滤的list
		
		//第二步：检查重复性
		List<Record> allSysCallerIdList = SysCallerId.dao.getAllSysCallerId();    //先从数据库中取出所有的主叫号码
		
		//遍历上传的主叫号码
		for(Record uploadCallerId:afterCheckFormatList) {
			
			String callerIdNumber = uploadCallerId.getStr("callerIdNumber");
			boolean isInDatabase = callerIdNumberIsInDatabase(allSysCallerIdList,callerIdNumber);
			if(isInDatabase) {       //如果上传的主叫号码已经在数据库中存在，则增加重复的数量
				repeatCount++;
			}else {                  //如果在数据库中没有数据时
				afterRepetitionList.add(uploadCallerId);
			}
			
		}
		
		//通过上面的各过处理后，得到的 afterRepetitionList,就是需要上传到数据表的主叫号码列表，调用 SysCallerId 进行批量插入数据表
		int[] insertData = SysCallerId.dao.batchSave(afterRepetitionList);
		
		int successCount = 0;
		if(!BlankUtils.isBlank(insertData) && insertData.length > 0) {
			successCount = insertData.length;
		}
		
		sb.append("上传总行数为：" + list.size() + "行。");
		sb.append("有效号码：" + validCount);
		sb.append(",重复号码：" + repeatCount + "，成功插入：" + successCount + "!");
		
		return sb.toString();
		 
	}
	
	
	
	
	/**
	 * 判断传入的号码，是否
	 * 
	 * @param allSysCallerIdList
	 * @param callerIdNumber
	 * @return
	 */
	public boolean callerIdNumberIsInDatabase(List<Record> allSysCallerIdList,String callerIdNumber) {
		
		boolean b = false;
		
		for(Record sysCallerId:allSysCallerIdList) {
			String callerId = sysCallerId.get("CALLERID");
			if(callerId.equals(callerIdNumber)) {    //如果有相同时，设置为 true,直接返回
				b = true;
				return b;
			}
		}
		
		return b;
	}
	
	
	/**
	 * 导出主叫号码的模板
	 */
	public void template() {
		//文件类型: txt | excel
		String type = getPara("type");                 //得到文件的类型
		//模板的标识
		String identify = getPara("identify");         //callerId
		
		String fileName = "";
		String mimeType = "";
		
		String templateDir = File.separator + "template" + File.separator;    //模板所在的路径
		
		String path_tmp = PathKit.getWebRootPath() + templateDir;
		
		if(type.equalsIgnoreCase("txt")) {
			mimeType = "txt";
		}else {
			mimeType = "xlsx";
		}
		
		fileName = identify + "_template" + "." + mimeType;
		
		File file = new File(path_tmp + fileName);
		
		if(file.exists()) {
			renderFile(file);
		}else {
			render(RenderJson.error("下载模板失败,文件不存在!"));
		}
	}

}
