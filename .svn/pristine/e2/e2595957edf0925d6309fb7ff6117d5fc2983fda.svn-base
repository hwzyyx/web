package com.callke8.system.loginlog;

import com.callke8.common.IController;
import com.jfinal.core.Controller;

public class LoginLogController extends Controller implements IController {
	
	public void index() {
		render("list.jsp");
	}
	
	public void datagrid() {
		
		String operId = getPara("operId");
		String orgCode = getPara("orgCode");
		String loginStartTime =getPara("loginStartTime");
		String loginEndTime = getPara("loginEndTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		
		renderJson(LoginLog.dao.getLoginLogByPaginateToMap(page, rows, operId, orgCode, loginStartTime, loginEndTime));
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
