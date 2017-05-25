package com.callke8.fastagi.autocontact;

import java.io.File;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;

public class AutoContactRecordController extends Controller implements
		IController {

	@Override
	public void index() {
		render("list.jsp");
	}
	
	@Override
	public void add() {
		
	}

	@Override
	public void datagrid() {
		
		String agentNumber = getPara("agentNumber");
		String clientNumber = getPara("clientNumber");
		String identifier = getPara("identifier");
		String callerId = getPara("callerId");
		String status = getPara("status");
		if(BlankUtils.isBlank(status)) { status="2";};   //刚开始的时候，只是显示已经外呼成功的记录
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		
		renderJson(AutoContactRecord.dao.getAutoContactRecordByPaginateToMap(page,rows,agentNumber,clientNumber,identifier,callerId,status,startTime,endTime));
		
	}

	@Override
	public void delete() {

	}


	@Override
	public void update() {

	}
	
	public void download() {
		
		String path = getPara("path");
		String file = getPara("file");
		String fn = PathKit.getWebRootPath() + "/" + path + "/" + file;
		File f = new File(fn);
		
		System.out.println("path:" + path);
		System.out.println("file:" + file);
		System.out.println("文件：" + f);
		//String aaa = p
		
		//File f = new File();
		
		renderFile(f);
	}

}
