package com.callke8.bsh.bshcallparam;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class BSHCallParamController extends Controller implements IController {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		
		String paramCode = getPara("paramCode");
		String paramName = getPara("paramName");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		renderJson(BSHCallParam.dao.getCallParamByPaginateToMap(pageNumber, pageSize, paramCode,paramName));
		
	}

	@Override
	public void add() {
		
		BSHCallParam bshCallParam = getModel(BSHCallParam.class, "callParam");
		
		//判断是否已经存在相同的参数编码
		String paramCode = bshCallParam.get("PARAM_CODE");
		Record r = BSHCallParam.dao.getBSHCallParamByParamCode(paramCode);
		if(!BlankUtils.isBlank(r)) {
			render(RenderJson.error("新增成功,已经存在相同的参数编码!"));
			return;
		}
		
		bshCallParam.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = BSHCallParam.dao.add(bshCallParam);
		
		if(b) {
			render(RenderJson.success("新增呼叫参数成功!"));
		}else {
			render(RenderJson.error("新增呼叫参数失败!"));
		}
		
		
		
		
	}

	@Override
	public void update() {
		
		BSHCallParam bshCallParam = getModel(BSHCallParam.class,"callParam");
		
		boolean b = bshCallParam.dao.update(bshCallParam);
		
		if(b) {
			render(RenderJson.success("修改呼叫参数成功!"));
		}else {
			render(RenderJson.error("修改呼叫参数失败!"));
		}
		
	}

	@Override
	public void delete() {
		
		String paramCode = getPara("paramCode");
		
		boolean b = BSHCallParam.dao.deleteByParamCode(paramCode);
		
		if(b) {
			render(RenderJson.success("删除呼叫参数成功!"));
		}else {
			render(RenderJson.error("删除呼叫参数失败!"));
		}
		
	}

}
