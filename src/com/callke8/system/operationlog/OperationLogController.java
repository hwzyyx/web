package com.callke8.system.operationlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.callke8.common.IController;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

/**
 * 操作记录日志
 * 
 * @author <a href="mailto:120077407@qq.com">hwz</a>
 */
public class OperationLogController extends Controller implements IController {

	public void index() {
		render("list.jsp");
	}
	
	public void datagrid() {
		
		String moduleCode = getPara("moduleCode");
		String operation = getPara("operation");
		String operId = getPara("operId");
		
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;}
		
		renderJson(OperationLog.dao.getOperationLogByPaginateToMap(page, rows, moduleCode, operation, operId, startTime, endTime));
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
