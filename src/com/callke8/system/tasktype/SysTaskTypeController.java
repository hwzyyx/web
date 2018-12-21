package com.callke8.system.tasktype;

import java.util.*;
import com.callke8.common.IController;
import com.callke8.system.callerid.SysCallerId;
import com.callke8.system.calleridassign.SysCallerIdAssign;
import com.callke8.system.tasktypeassign.SysTaskTypeAssign;
import com.callke8.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONArray;

public class SysTaskTypeController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));	//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量

		String taskType = getPara("taskType");
		int numberOrder = BlankUtils.isBlank(getPara("numberOrder"))?0:Integer.valueOf(getPara("numberOrder"));

		Map map = SysTaskType.dao.getSysTaskTypeByPaginateToMap(pageNumber,pageSize,taskType,numberOrder);
		renderJson(map);
	}

	@Override
	public void add() {
		SysTaskType formData = getModel(SysTaskType.class,"sys_task_type");
		
		String taskType = formData.get("TASK_TYPE");
		int numberOrder = formData.get("NUMBER_ORDER");
		
		//(1)检查是否存在相同的任务类型
		SysTaskType stt2 = SysTaskType.dao.getSysTaskTypeByTaskType(taskType);
		if(!BlankUtils.isBlank(stt2)) {
			render(RenderJson.error("修改失败,系统已经存在相同的任务类型!"));
			return;
		}
		
		//（2）检查是否已经存在相同的序号
		List<SysTaskType> list = SysTaskType.dao.getSysTaskTypeByNumberOrder(numberOrder);
		if(!BlankUtils.isBlank(list)) {
			for(SysTaskType stt4:list) {
				if(numberOrder==stt4.getInt("NUMBER_ORDER")) {    //如果有相同的序号记录，就表示已经存在相同的序号
					render(RenderJson.error("修改失败,系统已经存在相同序号的任务类型!"));
					return;
				}
			}
		}
		
		formData.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));
		
		boolean b = SysTaskType.dao.add(formData);
		if(b) {
			render(RenderJson.success("新增记录成功!"));
		}else {
			render(RenderJson.error("新增记录失败!"));
		}
	}

	@Override
	public void update() {
		SysTaskType formData = getModel(SysTaskType.class,"sys_task_type");

		int id = formData.get("ID");
		String taskType = formData.get("TASK_TYPE");
		int numberOrder = formData.get("NUMBER_ORDER");
		
		//(1)检查记录是否已经被删除
		SysTaskType stt = SysTaskType.dao.getSysTaskTypeById(id);
		if(BlankUtils.isBlank(stt)) {
			render(RenderJson.error("修改失败,该任务类型已经被删除!"));
			return;
		}
		
		//（2）检查是否已经存在相同的任务类型内容
		SysTaskType stt2 = SysTaskType.dao.getSysTaskTypeByTaskType(taskType);
		if(!BlankUtils.isBlank(stt2)) {
			if(id!=stt2.getInt("ID")) {
				render(RenderJson.error("修改失败,系统已经存在相同的任务类型!"));
				return;
			}
		}
		
		//(3)检查是否已经存在相同的序号
		List<SysTaskType> list = SysTaskType.dao.getSysTaskTypeByNumberOrder(numberOrder);
		if(!BlankUtils.isBlank(list)) {
			for(SysTaskType stt4:list) {
				if(id != stt4.getInt("ID")) {    //跟非当前记录对比
					if(numberOrder==stt4.getInt("NUMBER_ORDER")) {    //如果有相同的序号记录，就表示已经存在相同的序号
						render(RenderJson.error("修改失败,系统已经存在相同序号的任务类型!"));
						return;
					}
				}
			}
		}
		
		boolean b = SysTaskType.dao.update(taskType,numberOrder,id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		String id = getPara("id");

		boolean b = SysTaskType.dao.deleteById(id);
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}
	
	/**
	 * 根据当前登录的操作员ID（OPER_ID），将该操作员分配到的任务类型，以 combobox 数据返回
	 * 
	 * @param operId
	 * 			操作员ID
	 * @param flag
	 * 			0：没有请选择; 1： 指定请选择
	 */
	public static String getSysTaskTypeToComboboxByOperId(String operId,String flag) {
		
		String comboboxString = null;
		
		List<Record> sysTaskTypeList = SysTaskType.dao.getAllSysTaskType();    						//取出所有的任务类型
		List<Record> assignList = SysTaskTypeAssign.dao.getSysTaskTypeAssignByOperId(operId);       //取出操作员被分配到的情况
		
		List<Record> newList = new ArrayList<Record>();     //定义一个新的 list
		
		if(!BlankUtils.isBlank(sysTaskTypeList) && sysTaskTypeList.size()>0) {                       //任务类型列表大于0时
			if(!BlankUtils.isBlank(assignList) && assignList.size()>0) {
				for(Record sysTaskType:sysTaskTypeList) {      //遍历任务类型
					int id = sysTaskType.getInt("ID");              //任务类型序号
					//String callerId = sysCallerId.getStr("CALLERID");       //主叫号码
					for(Record sysTaskTypeAssign:assignList) {      //再遍历分配的结果
						int taskType_Id = sysTaskTypeAssign.getInt("TASK_TYPE_ID");    //取出分配到的 ID
						if(id == taskType_Id) {
							newList.add(sysTaskType);
						}
					}
				}
			}
		}
		
		
		List<ComboboxJson> cbjs = new ArrayList<ComboboxJson>();   //定义一个TreeJson 的 list
		ComboboxJson defalutCbj = new ComboboxJson();
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			defalutCbj = new ComboboxJson();
			defalutCbj.setId("empty");
			defalutCbj.setText("请选择");
			
			cbjs.add(defalutCbj);
			
		}
		
		if(!BlankUtils.isBlank(newList) && newList.size()>0) {
			for(Record record:newList) {
				ComboboxJson cbj = new ComboboxJson();
				cbj.setId(record.get("NUMBER_ORDER").toString());
				cbj.setText(record.get("TASK_TYPE").toString());
				
				cbjs.add(cbj);
			}
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cbjs);
		
		return jsonArray.toString();
		
	}
	
	/**
	 * 取得所有的任务类型，以 combobox 数据返回(主要是用于在查询中使用)
	 * 
	 * @param flag
	 * 			0：没有请选择; 1： 指定请选择
	 */
	public static String getAllSysTaskTypeToCombobox(String flag) {
		
		List<Record> sysTaskTypeList = SysTaskType.dao.getAllSysTaskType();    						//取出所有的任务类型
		
		List<ComboboxJson> cbjs = new ArrayList<ComboboxJson>();   //定义一个TreeJson 的 list
		ComboboxJson defalutCbj = new ComboboxJson();
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			defalutCbj = new ComboboxJson();
			defalutCbj.setId("empty");
			defalutCbj.setText("请选择");
			
			cbjs.add(defalutCbj);
			
		}
		
		if(!BlankUtils.isBlank(sysTaskTypeList) && sysTaskTypeList.size()>0) {
			for(Record record:sysTaskTypeList) {
				ComboboxJson cbj = new ComboboxJson();
				cbj.setId(record.get("NUMBER_ORDER").toString());
				cbj.setText(record.get("TASK_TYPE").toString());
				
				cbjs.add(cbj);
			}
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cbjs);
		
		return jsonArray.toString();
		
	}
	
}
