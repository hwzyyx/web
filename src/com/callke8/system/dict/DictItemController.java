package com.callke8.system.dict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class DictItemController extends Controller implements IController {
	
	@SuppressWarnings("unchecked")
	public void datagrid() {
		
		String groupCode = getPara("groupCode");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		
		if(page==0){page=1;}
		if(BlankUtils.isBlank(groupCode)){
			Map m = new HashMap();
			m.put("total", 0);
			m.put("rows",new ArrayList<Record>());
			renderJson(m);
			return;
		}
		renderJson(DictItem.dao.getDictItemByPaginateToMap(page, rows, groupCode));
	}
	
	public void delete() {
		
		String ids = getPara("ids");
		String groupCode = getPara("groupCode");

		if(BlankUtils.isBlank(ids)) {   //如果选中的项的ＩＤ为空时，不进行删除
			render(RenderJson.error("删除失败,请选择数据字典项后再执行删除！"));
			return;
		}
		
		for(String id:ids.split(",")) {
			DictItem.dao.delete(groupCode,id);
		}
		//如果删除成功时，需要重新加载数据字典的数据到内存
		MemoryVariableUtil.dictMap = DictGroup.dao.loadDictInfo();
		
		render(RenderJson.success("删除成功！"));
	}
	
	public void update() {
		DictItem dictItem = getModel(DictItem.class, "dictitem");
		String groupCode = getPara("groupCode");
		
		int count = DictItem.dao.update(dictItem, groupCode);
		
		if(count>0) {
			//如果修改成功时，需要重新加载数据字典的数据到内存
			MemoryVariableUtil.dictMap = DictGroup.dao.loadDictInfo();
			render(RenderJson.success("修改成功"));
		}else {
			render(RenderJson.error("修改失败"));
		}
		
	}
	

	@Override
	public void add() {
		DictItem dictItem = getModel(DictItem.class, "dictitem");
		String groupCode = getPara("groupCode");
		
		if(BlankUtils.isBlank(groupCode)) {
			render(RenderJson.error("当前选择的GroupCode为空，添加失败!"));
			return;
		}
		
		//添加之前，先检测是否有相同的dictCode及groupCode的记录，如果有，则不允许添加
		String dictCode = dictItem.get("DICT_CODE");
		int count = DictItem.dao.checkDictItem(dictCode, groupCode);
		
		if(count>0) {
			render(RenderJson.error("添加失败,已经存在相同的字典编码!"));
			return;
		}
		
		dictItem.set("GROUP_CODE", groupCode);
		
		boolean b = DictItem.dao.add(dictItem);
		
		if(b) {
			//如果添加成功时，需要重新加载数据字典的数据到内存
			MemoryVariableUtil.dictMap = DictGroup.dao.loadDictInfo();
			
			render(RenderJson.success("添加成功!"));
		}else {
			render(RenderJson.error("添加失败"));
		}
		
	}

	@Override
	public void index() {
		
	}
	
}
