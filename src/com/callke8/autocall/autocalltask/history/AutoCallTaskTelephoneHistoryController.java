package com.callke8.autocall.autocalltask.history;

import java.util.HashMap;
import java.util.Map;

import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.core.Controller;

public class AutoCallTaskTelephoneHistoryController extends Controller  {
	
	public void datagrid() {
		System.out.println("取AutoCallTaskTelephoneController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String taskId = getPara("taskId");
		String telephone = getPara("telephone");
		String clientName = getPara("clientName");
		String state = getPara("state");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		//Map map = AutoCallTaskTelephone.dao.getAutoCallTaskTelephoneByPaginateToMap(pageNumber, pageSize, taskId, telephone, clientName,state);
		Map map = new HashMap();
		
		System.out.println("取AutoCallTaskTelephoneController datagrid的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
		
	}

	public void index() {
		
	}

}
