package com.callke8.system.param;

import java.util.ArrayList;
import java.util.List;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class ParamController extends Controller implements IController {

	@Override
	public void index() {
		
		render("list.jsp");
	}
	
	public void reloadTab() {
		//返回的 Record存储的是数据字典项的内容，格式：Record(DICT_CODE:1,GROUP_CODE:PARAM_TYPE,DICT_NAME:'全局参数')
		List<Record> list = MemoryVariableUtil.dictMap.get("PARAM_TYPE");
		
		List<Record> newList = new ArrayList<Record>();   //定义一个新的 list<Record> 用于放置新增的内容
		for(Record r:list) {      //遍历并定义 tab 的内容
			String dictCode = r.getStr("DICT_CODE");
			String paramName = r.getStr("DICT_NAME");
			
			r.set("paramType", dictCode);
			r.set("paramTypeDesc", paramName);
			r.set("paramTypeId", "paramTypeId" + dictCode);
			
			newList.add(r);
		}
		
		renderJson(newList);
	}

	@Override
	public void datagrid() {
		
		int paramType = Integer.valueOf(getPara("paramType"));
		String paramCode = getPara("paramCode");
		String paramName = getPara("paramName");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		renderJson(Param.dao.getParamByPaginateToMap(pageNumber, pageSize,paramType,paramCode,paramName));
		
	}

	@Override
	public void add() {
		int paramType = Integer.valueOf(getPara("paramType"));
		Param param = getModel(Param.class, "param");
		param.set("PARAM_TYPE", paramType);
		
		//判断是否已经存在相同的参数类型与参数代码
		String paramCode = param.get("PARAM_CODE");
		Record r = Param.dao.getParamByParamTypeAndParamCode(paramType,paramCode);
		if(!BlankUtils.isBlank(r)) {
			render(RenderJson.error("新增失败,已经存在相同的参数类型的参数编码!"));
			return;
		}
		
		param.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = Param.dao.add(param);
		
		if(b) {
			render(RenderJson.success("新增系统参数成功!"));
		}else {
			render(RenderJson.error("新增系统参数失败!"));
		}
		
		
		
		
	}

	@Override
	public void update() {
		
		int paramType = Integer.valueOf(getPara("paramType"));
		Param param = getModel(Param.class,"param");
		param.set("PARAM_TYPE", paramType);
		
		boolean b = Param.dao.update(param);
		
		if(b) {
			render(RenderJson.success("修改呼叫参数成功!"));
		}else {
			render(RenderJson.error("修改呼叫参数失败!"));
		}
		
	}

	@Override
	public void delete() {
		
		int paramType = Integer.valueOf(getPara("paramType"));
		String paramCode = getPara("paramCode");
		
		boolean b = Param.dao.deleteByParamTypeAndParamCode(paramType,paramCode);
		
		if(b) {
			render(RenderJson.success("删除呼叫参数成功!"));
		}else {
			render(RenderJson.error("删除呼叫参数失败!"));
		}
		
	}

}
