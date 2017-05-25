package com.callke8.report.clientinfo;

import com.callke8.common.IController;
import com.callke8.report.cdr.Cdr;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class ClientInfoController extends Controller implements IController {
	
	public void index() {
		render("list.jsp");
	}
	
	public void datagrid() {
		
		String clientName = getPara("clientName");
		String clientTelephone = getPara("clientTelephone");
		String clientLevel = getPara("clientLevel");
		String clientSex = getPara("clientSex");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;}
		
		renderJson(ClientInfo.dao.getClientInfoByPaginateToMap(page, rows, clientName, clientTelephone, clientLevel, clientSex,startTime,endTime));
	}
	
	/**
	 * 修改客户资料
	 */
	public void update() {
		ClientInfo client = getModel(ClientInfo.class,"clientInfo");
		
		//System.out.println(client.toString());
		boolean b = ClientInfo.dao.update(client);
		
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.success("修改失败!"));
		}
	}
	
	/**
	 * 增加客户信息
	 */
	public void add() {
		ClientInfo client = getModel(ClientInfo.class,"clientInfo");
		
		//在添加之前，需要判断是否已经有相同号码的记录
		Record chkClient = ClientInfo.dao.getClientInfoByTelephone(client.get("CLIENT_TELEPHONE").toString());
		if(!BlankUtils.isBlank(chkClient)) {    //已经存在相同号码的记录时，不允许添加
			render(RenderJson.error("添加失败，已经存在相同号码的信息"));
			return;
		}
		
		boolean b = ClientInfo.dao.add(modelToRecord(client));
		if(b) {
			render(RenderJson.success("添加成功"));
		}else{
			render(RenderJson.error("添加失败"));
		}
	}
	
	/**
	 * 删除
	 */
	public void delete() {
		String clientNo = getPara("clientNo");
		
		boolean b = ClientInfo.dao.del(clientNo);
		
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
		
	}
	
	public Record modelToRecord(ClientInfo client) {
		Record record = new Record();
		record.set("CLIENT_NO", client.get("CLIENT_NO"));
		record.set("CLIENT_NAME", client.get("CLIENT_NAME"));
		record.set("CLIENT_TELEPHONE", client.get("CLIENT_TELEPHONE"));
		record.set("CLIENT_TELEPHONE2", client.get("CLIENT_TELEPHONE2"));
		record.set("CLIENT_LEVEL", client.get("CLIENT_LEVEL"));
		record.set("CLIENT_SEX", client.get("CLIENT_SEX"));
		record.set("CLIENT_QQ", client.get("CLIENT_QQ"));
		record.set("CLIENT_EMAIL", client.get("CLIENT_EMAIL"));
		record.set("CLIENT_COMPANY", client.get("CLIENT_COMPANY"));
		record.set("CLIENT_ADDRESS", client.get("CLIENT_ADDRESS"));
		record.set("CREATE_TIME", DateFormatUtils.getCurrentDate());    //设置为当前的时间
		return record;
	}

}
