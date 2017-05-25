package com.callke8.fastagi.blacklist;

import com.callke8.common.IController;
import com.jfinal.core.Controller;

public class BlackListInterceptRecordController extends Controller implements
		IController {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		String clientName = getPara("clientName");
		String clientTelephone = getPara("clientTelephone");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;};
		
		renderJson(BlackListInterceptRecord.dao.getBlackListInterceptRecordByPaginateToMap(page, rows, clientTelephone, clientName, startTime, endTime));
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
