package com.callke8.autocall.autonumber;

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

public class AutoNumberController extends Controller implements IController {

	@Override
	public void index() {
		//setAttr("aaaa","bbbb");
		
		//获取并返回组织代码
		setAttr("orgComboTreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		render("list.jsp");
	}
	
	@Override
	public void datagrid() {
		
		String numberName = getPara("numberName");  
		String orgCode = getPara("orgCode");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = AutoNumber.dao.getAutoNumberByPaginateToMap(pageNumber, pageSize, numberName, orgCode, startTime, endTime);
		
		renderJson(map);
	}
	
	@Override
	public void add() {
		
		AutoNumber autoNumber = getModel(AutoNumber.class,"autoNumber");
		
		//自动生成ID，主要是以时间：年月日 + 随机四位数
		String numberId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
		autoNumber.set("NUMBER_ID", numberId);
		
		//设置操作工号
		String operId = String.valueOf(getSession().getAttribute("currOperId"));
		autoNumber.set("CREATE_USERCODE", operId);
		
		//设置操作工号
		autoNumber.set("ORG_CODE",Operator.dao.getOrgCodeByOperId(operId));
		
		//设置创建时间
		autoNumber.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = AutoNumber.dao.add(autoNumber);
		
		if(b) {
			render(RenderJson.success("添加号码组成功!",numberId));
		}else {
			render(RenderJson.error("添加号码组失败!"));
		}
	}


	@Override
	public void delete() {
		
		String numberId = getPara("numberId");
		
		boolean b = AutoNumber.dao.deleteByNumberId(numberId);
		
		if(b) {
			//如果删除成功时，需要将号码一并删除，以免堆积垃圾数据
			AutoNumberTelephone.dao.deleteByNumberId(numberId);
			render(RenderJson.success("删除号码组成功!"));
		}else {
			render(RenderJson.error("删除号码组失败!"));
		}
		
	}

	@Override
	public void update() {
		
		AutoNumber autoNumber = getModel(AutoNumber.class,"autoNumber");
		
		if(BlankUtils.isBlank(autoNumber)) {
			render(RenderJson.error("上传的号码组为空,无法修改!"));
			return;
		}
		
		String numberId = autoNumber.get("NUMBER_ID").toString();
		String numberName = autoNumber.get("NUMBER_NAME");
		
		boolean b = AutoNumber.dao.update(numberName, numberId);
		
		if(b) {
			render(RenderJson.success("修改号码组成功!"));
		}else {
			render(RenderJson.error("修改号码组失败!"));
		}
		
	}
	
	/*
	 * 导出模板：上传号码的模板
	 */
	public void template() {
		
		String type = getPara("type");     //可能是: txt 或是 excel 
		String fileName = "number_template";
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
