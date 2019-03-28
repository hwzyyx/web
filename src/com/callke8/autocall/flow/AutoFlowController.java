package com.callke8.autocall.flow;

import java.util.*;
import com.callke8.common.IController;
import com.callke8.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONArray;

public class AutoFlowController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));	//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量

		String flowName = getPara("flowName");

		Map map = AutoFlow.dao.getAutoFlowByPaginateToMap(pageNumber,pageSize,flowName);
		renderJson(map);
	}

	@Override
	public void add() {
		AutoFlow formData = getModel(AutoFlow.class,"ac_flow");

		boolean b = AutoFlow.dao.add(formData);
		if(b) {
			render(RenderJson.success("新增记录成功!"));
		}else {
			render(RenderJson.error("新增记录失败!"));
		}
	}

	@Override
	public void update() {
		AutoFlow formData = getModel(AutoFlow.class,"ac_flow");

		String flowId = formData.get("FLOW_ID");
		String flowName = formData.get("FLOW_NAME");
		String flowRule = formData.get("FLOW_RULE");

		boolean b = AutoFlow.dao.update(flowName,flowRule,flowId);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		String id = getPara("id");

		boolean b = AutoFlow.dao.deleteById(id);
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}
}
