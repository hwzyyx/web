package com.callke8.autocall.autoblacklist;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.history.AutoCallTaskHistory;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.callke8.utils.TreeJson;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;

public class AutoBlackListController extends Controller implements IController {

	@Override
	public void index() {
		//setAttr("aaaa","bbbb");
		
		//获取并返回组织代码
		setAttr("orgComboTreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		render("list.jsp");
	}
	
	@Override
	public void datagrid() {
		System.out.println("取AutoBlackListController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String blackListName = getPara("blackListName");  
		String orgCode = getPara("orgCode");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		String createUserCode = null;    //创建的用户ID
		if(!BlankUtils.isBlank(orgCode)) {
			createUserCode = CommonController.getOperIdStringByOrgCode(orgCode,getSession());
		}
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = AutoBlackList.dao.getAutoBlackListByPaginateToMap(pageNumber, pageSize, blackListName, createUserCode, startTime, endTime);
		
		System.out.println("取AutoBlackListController datagrid的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
	}
	
	@Override
	public void add() {
		
		AutoBlackList autoBlackList = getModel(AutoBlackList.class,"autoBlackList");
		
		//自动生成ID，主要是以时间：年月日 + 随机四位数
		String blackListId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
		autoBlackList.set("BLACKLIST_ID", blackListId);
		
		//设置操作工号
		String operId = String.valueOf(getSession().getAttribute("currOperId"));
		autoBlackList.set("CREATE_USERCODE", operId);
		
		//设置操作工号
		autoBlackList.set("ORG_CODE",Operator.dao.getOrgCodeByOperId(operId));
		
		//设置创建时间
		autoBlackList.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = AutoBlackList.dao.add(autoBlackList);
		
		if(b) {
			render(RenderJson.success("添加黑名单成功!",blackListId));
		}else {
			render(RenderJson.error("添加黑名单失败!"));
		}
	}


	@Override
	public void delete() {
		
		String blackListId = getPara("blackListId");
		
		//在删除黑名单之前，先查看黑名单是否已经被外呼任务引用，如果被引用，则不允许删除黑名单
		boolean isUsed = AutoCallTask.dao.checkBlackListBeUsed(blackListId);
		if(isUsed) {
			render(RenderJson.error("执行删除操作失败,黑名单已被外呼任务引用,不允许删除!"));
			return;
		}
		
		//在删除黑名单之前,先查看黑名单是否已被历史任务引用，如果被引用，则不允许删除黑名单
		isUsed = AutoCallTaskHistory.dao.checkBlackListBeUsed(blackListId);
		if(isUsed) {
			render(RenderJson.error("执行删除操作失败,黑名单已被历史外呼任务引用,不允许删除!"));
			return;
		}
		
		
		
		boolean b = AutoBlackList.dao.deleteByBlackListId(blackListId);
		
		if(b) {
			//如果删除成功时，需要将号码一并删除，以免堆积垃圾数据
			AutoBlackListTelephone.dao.deleteByBlackListId(blackListId);
			render(RenderJson.success("删除黑名单成功!"));
		}else {
			render(RenderJson.error("删除黑名单失败!"));
		}
		
	}

	@Override
	public void update() {
		
		AutoBlackList autoBlackList = getModel(AutoBlackList.class,"autoBlackList");
		
		if(BlankUtils.isBlank(autoBlackList)) {
			render(RenderJson.error("上传的黑名单为空,无法修改!"));
			return;
		}
		
		String blackListId = autoBlackList.get("BLACKLIST_ID").toString();
		String blackListName = autoBlackList.get("BLACKLIST_NAME");
		
		boolean b = AutoBlackList.dao.update(blackListName, blackListId);
		
		if(b) {
			render(RenderJson.success("修改黑名单成功!"));
		}else {
			render(RenderJson.error("修改黑名单失败!"));
		}
		
	}
	
	/*
	 * 导出模板：上传号码的模板
	 */
	public void template() {
		
		String type = getPara("type");     //可能是: txt 或是 excel 
		String fileName = "standard_template";
		String mimeType = null;
		
		if(BlankUtils.isBlank(type)) {
			type = "excel";
		}
		
		
		
		String templateDir = File.separator + "template" + File.separator;     //模板所在路径
		String path_tmp = PathKit.getWebRootPath() + templateDir;              //绝对路径
		
		if(type.equalsIgnoreCase("txt")) {
			mimeType = "txt";
		}else {
			mimeType = "xlsx";
		}
		
		String fullFileName = fileName + "." + mimeType;
		
		File file = new File(path_tmp + fullFileName);
		
		if(file.exists()) {
			renderFile(file);
		}else {
			render(RenderJson.error("下载模板失败,文件不存在"));
		}
		
	}
	
}
