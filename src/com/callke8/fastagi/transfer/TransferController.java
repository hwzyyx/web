package com.callke8.fastagi.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class TransferController extends Controller implements IController {

	
	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		String did = getPara("did");                   //特服号
		String destination = getPara("destination");   //目标号码

		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;};
		
		Map m = Transfer.dao.getTransferByPaginateToMap(page, rows, did, destination);
		renderJson(m);
	}

	@Override
	public void add() {
		
		Transfer transfer = getModel(Transfer.class,"transfer");
		
		String currOperId = getSessionAttr("currOperId");
		transfer.set("OPER_ID",currOperId);
		transfer.set("CREATE_TIME",DateFormatUtils.getCurrentDate());

		boolean b = Transfer.dao.add(transfer);
		
		if(b) {
			render(RenderJson.success("添加成功!"));
		}else {
			render(RenderJson.error("添加成功!"));
		}
		
	}
	
	@Override
	public void delete() {
		
		String transferId = getPara("transferId");
		
		boolean b = Transfer.dao.delete(transferId);
		
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
		
	}


	@Override
	public void update() {
		
		Transfer transfer = getModel(Transfer.class, "transfer");
		
		boolean b = Transfer.dao.update(transfer);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

}
