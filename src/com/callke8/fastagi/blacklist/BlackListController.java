package com.callke8.fastagi.blacklist;

import com.callke8.common.IController;
import com.callke8.fastagi.transfer.Transfer;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;

public class BlackListController extends Controller implements IController {

	@Override
	public void index() {
		render("list.jsp");
	}
	
	@Override
	public void datagrid() {
		
		String clientTelephone = getPara("clientTelephone");     //客户号码
		String clientName = getPara("clientName");  	 		 //客户姓名
		String state = getPara("state");  	 		 			 //客户姓名

		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;};
		
		renderJson(BlackList.dao.getBlackListByPaginateToMap(page,rows,clientTelephone,clientName,state));
	}
	
	@Override
	public void add() {
		
		BlackList blackList = getModel(BlackList.class,"blacklist");
		
		blackList.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
		blackList.set("OPER_ID",getSessionAttr("currOperId"));
		
		boolean b = BlackList.dao.add(blackList);
		
		if(b) {
			render(RenderJson.success("添加成功!"));
		}else {
			render(RenderJson.error("添加失败!"));
		}
	}


	@Override
	public void delete() {
		String blacklistId = getPara("blacklistId");
		
		boolean b = BlackList.dao.delete(blacklistId);
		
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}


	@Override
	public void update() {
		
		BlackList blackList = getModel(BlackList.class,"blacklist");
		
		boolean b = BlackList.dao.update(blackList);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

}
