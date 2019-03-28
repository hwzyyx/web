package com.callke8.cnn.cnncallindata;

import java.util.*;

import com.callke8.cnn.cnndata.CnnData;
import com.callke8.common.IController;
import com.callke8.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONArray;

public class CnnCallinDataController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));	//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量

		String callerId = getPara("callerId");
		String callee = getPara("callee");
		String state = getPara("state");
		String customerNewTel = getPara("customerNewTel");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");

		Map map = CnnCallinData.dao.getCnnCallinDataByPaginateToMap(pageNumber,pageSize,callerId,callee,state,customerNewTel,startTime,endTime);
		renderJson(map);
	}

	@Override
	public void add() {
		CnnCallinData formData = getModel(CnnCallinData.class,"cnn_callin_data");

		boolean b = CnnCallinData.dao.add(formData);
		if(b) {
			render(RenderJson.success("新增记录成功!"));
		}else {
			render(RenderJson.error("新增记录失败!"));
		}
	}

	@Override
	public void update() {
		CnnCallinData formData = getModel(CnnCallinData.class,"cnn_callin_data");

		int id = formData.get("ID");
		String callerId = formData.get("CALLERID");
		String callee = formData.get("CALLEE");
		String state = formData.get("STATE");
		String callDate = formData.get("CALL_DATE");
		String pkCnnDataId = formData.get("PK_CNN_DATA_ID");

		boolean b = CnnCallinData.dao.update(callerId,callee,state,callDate,pkCnnDataId,id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		String id = getPara("id");

		boolean b = CnnCallinData.dao.deleteById(id);
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}
	
	/**
	 * 导出为excel
	 */
	public void exportExcel() {
		
		String callerId = getPara("callerId");
		String callee = getPara("callee");
		String state = getPara("state");
		String customerNewTel = getPara("customerNewTel");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		List<Record> list = CnnCallinData.dao.getCnnCallinDataByCondition(callerId, callee, state, customerNewTel, startTime, endTime);
		
		String fileName = "export.xls";
		String sheetName = "来电数据";
		
		ExcelExportUtil export = new ExcelExportUtil(list, getResponse());
		
		String[] headers = {"主叫号码","被叫号码","被叫是否已改号","被叫新号码","来电时间"};
		String[] columns = {"CALLERID","CALLEE","STATE_DESC","CUSTOMER_NEW_TEL","CALL_DATE"};
		
		export.headers(headers).columns(columns).cellWidth(150).sheetName(sheetName);
		export.fileName(fileName).execExport();
	}
	
}
