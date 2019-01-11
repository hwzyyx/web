package com.callke8.system.calleridgroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.system.callerid.SysCallerId;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.FormUtils;
import com.callke8.utils.RenderJson;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TxtUtils;
import com.callke8.utils.XLSUtils;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

public class SysCallerIdGroupAssignController extends Controller implements IController {

	@Override
	public void index() {
		
	}

	@Override
	public void datagrid() {
		
		int groupId = BlankUtils.isBlank(getPara("groupId"))?0:Integer.valueOf(getPara("groupId"));
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));		//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量
		
		Map map = SysCallerIdGroupAssign.dao.getSysCallerIdGroupAssignByPaginateToMap(pageNumber,pageSize,groupId);
		renderJson(map);
		
	}
	
	/**
	 * 通过上传文件的方式上传主叫号码
	 */
	public void uploadFile() {
		
		Record getUploadFileRs = FormUtils.getUploadFile(getRequest(), this, "callerIdFile", "txt|xls|xlsx");  //获取上传的号码文件
		
		String statusCode = getUploadFileRs.getStr("statusCode");
		//如果获取上传的文件失败
		if(statusCode.equalsIgnoreCase("error")) {
			render(RenderJson.error(getUploadFileRs.getStr("message")));
			return;
		}
		
		String mimeType = getUploadFileRs.getStr("mimeType");          //取得上传的文件类型
		File file = getUploadFileRs.get("file");                       //取得上传的文件的对象
		
		
		int groupId = BlankUtils.isBlank(getPara("groupId"))?0:Integer.valueOf(getPara("groupId"));
		
		Record resultRecord = assignCallerIdToCallerIdGroupFromFile(file,mimeType,groupId);
		
		if(resultRecord.getStr("statusCode").equalsIgnoreCase("error")) {
			render(RenderJson.error(resultRecord.getStr("message")));
		}else {
			render(RenderJson.success(resultRecord.getStr("message")));
		}
		
	}
	
	/**
	 * 通过上传文件向号码组分配主叫号码
	 * 
	 * @param file
	 * @param groupId
	 * @return
	 */
	public Record assignCallerIdToCallerIdGroupFromFile(File file,String mimeType,int groupId) {
		
		//创建一个 Record，返回时，返回两个参数： statusCode:error|success, message:结果信息
		Record resultRecord = new Record();    
		
		//把文件中的信息取出并放置到一个  List 中， Record 以数字顺序存储数据
		List<Record> list = null;
		
		if(mimeType.equalsIgnoreCase("txt")) {     //如果文件类型为 txt
			list = TxtUtils.readTxt(file);
		}else {
			list = XLSUtils.readXls(file);
		}
		
		//建立两个 List， 一个用于检查数据格式，一个用于检查重复性
		ArrayList<Record> afterCheckFormatList = new ArrayList<Record>();      //结束格式检查后,同时兼备检查自身的重复性
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
		
		if(validCount==0) {                     //如果上传的有效数据为空，则直接返回，并提示
			resultRecord.set("statusCode","error");
			resultRecord.set("message","文件中无有效号码，无法分配!");
			return resultRecord;
		}
		
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
		
		//已经插入到数据库了，我们需要分配给该号码组的号码，应该是指向afterCheckFormatList
		//然后我们根据这些号码从数据库中查出这些号码对应的ID值
		//并将这些ID值存入 号码组的分配表
		int assignCount = saveCallerIdGroupAssign(afterCheckFormatList,groupId);
		
		resultRecord.set("statusCode","success");
		resultRecord.set("message","成功向主叫号码组成功分配:" + assignCount + " 个主叫号码");
		
		return resultRecord;
		
	}
	
	/**
	 * 保存分配主叫号码给主叫号码组
	 * 
	 * @param afterCheckFormatList
	 * @param groupId
	 * @return
	 */
	public int saveCallerIdGroupAssign(List<Record> afterCheckFormatList,int groupId) {
		
		List<Record> callerIdGroupAssignList = new ArrayList<Record>();
		
		//（1）先取出所有的主叫号码列表
		List<Record> allSysCallerId = SysCallerId.dao.getAllSysCallerId();    
		
		//（2）遍历需要分配的主叫号码，同时对比一下，这些记录是否在数据库中，如果在数据表中，才进行分配
		for(Record afterCheckFormatRecord:afterCheckFormatList) {
			
			String callerIdNumber = afterCheckFormatRecord.get("callerIdNumber");
			
			int callerId_Id = callerIdNumberIsInDatabaseReturnId(allSysCallerId,callerIdNumber);
			
			if(callerId_Id!=0) {     //如果查询出来的ID不为0，即是可以作为分配的值
				Record sysCallerIdGroupAssign = new Record();
				sysCallerIdGroupAssign.set("GROUP_ID", groupId);
				sysCallerIdGroupAssign.set("CALLERID_ID", callerId_Id);
				callerIdGroupAssignList.add(sysCallerIdGroupAssign);
			}
		}
		
		//如果需要分配的结果为空,则不再分配
		if(callerIdGroupAssignList.size() == 0) {
			return 0;
		}
		
		int insertCount =  SysCallerIdGroupAssign.dao.saveCallerIdGroupAssign(callerIdGroupAssignList, groupId);
		return insertCount;
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
	 * 判断传入的号码，是否存在于数据库中，如果存在，返回 ID值
	 * 
	 * @param allSysCallerIdList
	 * @param callerIdNumber
	 * @return
	 */
	public int callerIdNumberIsInDatabaseReturnId(List<Record> allSysCallerIdList,String callerIdNumber) {
		
		for(Record sysCallerId:allSysCallerIdList) {
			int id = sysCallerId.getInt("ID");
			String callerId = sysCallerId.get("CALLERID");
			if(callerId.equals(callerIdNumber)) {    //如果有相同时，设置为 true,直接返回
				return id;
			}
		}
		
		return 0;
	}
	
	/**
	 * 删除主叫号码组的分配
	 * 
	 * @return
	 */
	public void removeAssign() {
		
		int groupId = BlankUtils.isBlank(getPara("groupId"))?0:Integer.valueOf(getPara("groupId"));
		String ids = getPara("ids");
		
		if(BlankUtils.isBlank(ids) || ids.length() == 0) {
			render(RenderJson.error("没有选择记录，无法执行移除"));
			return;
		}
		
		int removeCount = SysCallerIdGroupAssign.dao.deleteCallerIdGroupAssignByIds(ids);
		
		render(RenderJson.success("成功移除：" + removeCount + " 条记录"));
	}

	@Override
	public void add() {
		
	}

	@Override
	public void update() {
		
	}

	@Override
	public void delete() {
		
	}

}
