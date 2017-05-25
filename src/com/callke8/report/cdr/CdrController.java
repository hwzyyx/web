package com.callke8.report.cdr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.callke8.utils.BlankUtils;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;

public class CdrController extends Controller {
	
	public void index() {
		render("list.jsp");
	}
	
	public void datagrid() {
		
		String src = getPara("src");
		String dst = getPara("dst");
		String seq = getPara("seq");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;}
		
		renderJson(Cdr.dao.getCdrByPaginateToMap(page, rows, src, dst,seq,startTime, endTime));
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
