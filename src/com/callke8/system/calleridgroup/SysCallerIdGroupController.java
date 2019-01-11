package com.callke8.system.calleridgroup;

import java.util.*;
import com.callke8.common.IController;
import com.callke8.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONArray;

public class SysCallerIdGroupController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));	//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量

		String groupName = getPara("groupName");

		Map map = SysCallerIdGroup.dao.getSysCallerIdGroupByPaginateToMap(pageNumber,pageSize,groupName);
		renderJson(map);
	}

	@Override
	public void add() {
		SysCallerIdGroup formData = getModel(SysCallerIdGroup.class,"sys_callerid_group");

		formData.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));
		
		boolean b = SysCallerIdGroup.dao.add(formData);
		if(b) {
			render(RenderJson.success("新增记录成功!"));
		}else {
			render(RenderJson.error("新增记录失败!"));
		}
	}

	@Override
	public void update() {
		SysCallerIdGroup formData = getModel(SysCallerIdGroup.class,"sys_callerid_group");

		int groupId = formData.get("GROUP_ID");
		String groupName = formData.get("GROUP_NAME");

		boolean b = SysCallerIdGroup.dao.update(groupName,groupId);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		String id = getPara("id");

		boolean b = SysCallerIdGroup.dao.deleteById(id);
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}
}
