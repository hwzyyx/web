package com.callke8.fastagi.transfer;

import java.io.File;

import com.callke8.common.IController;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;

public class TransferRecordController extends Controller implements IController {

	@Override
	public void index() {
		render("list.jsp");
	}
	
	@Override
	public void datagrid() {
		
		String did = getPara("did");
		String destination = getPara("destination");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;};
		
		renderJson(TransferRecord.dao.getTransferRecordByPaginateToMap(page, rows, did, destination, startTime, endTime));
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

	public void download() {
		String path = getPara("path");
		String file = getPara("file");
		String fn = PathKit.getWebRootPath() + "/" + path + file;
		File f = new File(fn);
		
		System.out.println("path:" + path);
		System.out.println("file:" + file);
		System.out.println("文件：" + f);
		//String aaa = p
		
		//File f = new File();
		
		renderFile(f);
	}
}
