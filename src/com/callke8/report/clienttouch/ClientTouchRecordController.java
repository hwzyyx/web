package com.callke8.report.clienttouch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class ClientTouchRecordController extends Controller implements IController {
	
	public void index() {
		render("list.jsp");
	}
	
	public void datagrid() {
		
		String agent = getPara("agent");
		String clientTelephone = getPara("clientTelephone");
		String touchType = getPara("touchType");
		String touchOperator = getPara("operator");
		
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;}
		
		renderJson(ClientTouchRecord.dao.getClientTouchRecordByPaginateToMap(page, rows,clientTelephone,agent,touchType,touchOperator,startTime,endTime));
	}
	
	public void getAllOperator() {
		Map m = new HashMap();
		List<Record> list = Operator.dao.getAllActiveOperator();
		renderJson(list);
	}

	@Override
	public void add() {
		
	}

	@Override
	public void delete() {
		
	}

	@Override
	public void update() {
		
	}
	
}
