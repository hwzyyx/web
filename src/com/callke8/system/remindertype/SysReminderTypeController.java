package com.callke8.system.remindertype;

import java.util.*;
import com.callke8.common.IController;
import com.callke8.system.remindertypeassign.SysReminderTypeAssign;
import com.callke8.system.tasktype.SysTaskType;
import com.callke8.system.tasktypeassign.SysTaskTypeAssign;
import com.callke8.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONArray;

public class SysReminderTypeController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));	//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量

		String reminderType = getPara("reminderType");
		int numberOrder = BlankUtils.isBlank(getPara("numberOrder"))?0:Integer.valueOf(getPara("numberOrder"));

		Map map = SysReminderType.dao.getSysReminderTypeByPaginateToMap(pageNumber,pageSize,reminderType,numberOrder);
		renderJson(map);
	}

	@Override
	public void add() {
		SysReminderType formData = getModel(SysReminderType.class,"sys_reminder_type");

		String reminderType = formData.get("REMINDER_TYPE");
		int numberOrder = formData.get("NUMBER_ORDER");
		
		//(1)检查是否存在相同的催缴类型
		SysReminderType srt = SysReminderType.dao.getSysReminderTypeByReminderType(reminderType);
		if(!BlankUtils.isBlank(srt)) {
			render(RenderJson.error("修改失败,系统已经存在相同的催缴类型!"));
			return;
		}
		
		//（2）检查是否已经存在相同的序号
		List<SysReminderType> list = SysReminderType.dao.getSysReminderTypeByNumberOrder(numberOrder);
		if(!BlankUtils.isBlank(list)) {
			for(SysReminderType srt2:list) {
				if(numberOrder==srt2.getInt("NUMBER_ORDER")) {    //如果有相同的序号记录，就表示已经存在相同的序号
					render(RenderJson.error("修改失败,系统已经存在相同序号的催缴类型!"));
					return;
				}
			}
		}
		
		formData.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));
		
		boolean b = SysReminderType.dao.add(formData);
		if(b) {
			render(RenderJson.success("新增记录成功!"));
		}else {
			render(RenderJson.error("新增记录失败!"));
		}
	}

	@Override
	public void update() {
		SysReminderType formData = getModel(SysReminderType.class,"sys_reminder_type");

		int id = formData.get("ID");
		String reminderType = formData.get("REMINDER_TYPE");
		int numberOrder = formData.get("NUMBER_ORDER");
		
		//(1)检查记录是否已经被删除
		SysReminderType srt = SysReminderType.dao.getSysReminderTypeById(id);
		if(BlankUtils.isBlank(srt)) {
			render(RenderJson.error("修改失败,该催缴类型已经被删除!"));
			return;
		}
		
		//（2）检查是否已经存在相同的任务类型内容
		SysReminderType srt2 = SysReminderType.dao.getSysReminderTypeByReminderType(reminderType);
		if(!BlankUtils.isBlank(srt2)) {
			if(id!=srt2.getInt("ID")) {
				render(RenderJson.error("修改失败,系统已经存在相同的催缴类型!"));
				return;
			}
		}
		
		//(3)检查是否已经存在相同的序号
		List<SysReminderType> list = SysReminderType.dao.getSysReminderTypeByNumberOrder(numberOrder);
		if(!BlankUtils.isBlank(list)) {
			for(SysReminderType srt3:list) {
				if(id != srt3.getInt("ID")) {    //跟非当前记录对比
					if(numberOrder==srt3.getInt("NUMBER_ORDER")) {    //如果有相同的序号记录，就表示已经存在相同的序号
						render(RenderJson.error("修改失败,系统已经存在相同序号的催缴类型!"));
						return;
					}
				}
			}
		}

		boolean b = SysReminderType.dao.update(reminderType,numberOrder,id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		String id = getPara("id");

		boolean b = SysReminderType.dao.deleteById(id);
		if(b) {
			render(RenderJson.success("删除成功!"));
			//删除成功后，还需要删除该催缴类型已经分配的记录
			SysReminderTypeAssign.dao.deleteSysReminderTypeAssignByReminderType_Id(Integer.valueOf(id));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}
	
	/**
	 * 根据当前登录的操作员ID（OPER_ID），将该操作员分配到的催缴类型，以 combobox 数据返回
	 * 
	 * @param operId
	 * 			操作员ID
	 * @param flag
	 * 			0：没有请选择; 1： 指定请选择
	 */
	public static String getSysReminderTypeToComboboxByOperId(String operId,String flag) {
		
		String comboboxString = null;
		
		List<Record> sysReminderTypeList = SysReminderType.dao.getAllSysReminderType();    				  //取出所有的催缴类型
		List<Record> assignList = SysReminderTypeAssign.dao.getSysReminderTypeAssignByOperId(operId);     //取出操作员被分配到的情况
		
		List<Record> newList = new ArrayList<Record>();     //定义一个新的 list
		
		if(!BlankUtils.isBlank(sysReminderTypeList) && sysReminderTypeList.size()>0) {                       //催缴类型列表大于0时
			if(!BlankUtils.isBlank(assignList) && assignList.size()>0) {
				for(Record sysReminderType:sysReminderTypeList) {         	           //遍历催缴类型
					int id = sysReminderType.getInt("ID");          //催缴类型序号
					//String callerId = sysCallerId.getStr("CALLERID");       //主叫号码
					for(Record sysReminderTypeAssign:assignList) {      //再遍历分配的结果
						int reminderType_Id = sysReminderTypeAssign.getInt("REMINDER_TYPE_ID");    //取出分配到的 ID
						if(id == reminderType_Id) {
							newList.add(sysReminderType);
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
				cbj.setText(record.get("REMINDER_TYPE").toString());
				
				cbjs.add(cbj);
			}
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cbjs);
		
		return jsonArray.toString();
		
	}
	
	
	/**
	 * 取得所有的催缴类型类型，以 combobox 数据返回(主要是用于在查询中使用)
	 * 
	 * @param flag
	 * 			0：没有请选择; 1： 指定请选择
	 */
	public static String getAllSysReminderTypeToCombobox(String flag) {
		
		List<Record> sysReminderTypeList = SysReminderType.dao.getAllSysReminderType();    						//取出所有的催缴类型
		
		List<ComboboxJson> cbjs = new ArrayList<ComboboxJson>();   //定义一个TreeJson 的 list
		ComboboxJson defalutCbj = new ComboboxJson();
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			defalutCbj = new ComboboxJson();
			defalutCbj.setId("empty");
			defalutCbj.setText("请选择");
			
			cbjs.add(defalutCbj);
			
		}
		
		if(!BlankUtils.isBlank(sysReminderTypeList) && sysReminderTypeList.size()>0) {
			for(Record record:sysReminderTypeList) {
				ComboboxJson cbj = new ComboboxJson();
				cbj.setId(record.get("NUMBER_ORDER").toString());
				cbj.setText(record.get("REMINDER_TYPE").toString());
				
				cbjs.add(cbj);
			}
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cbjs);
		
		return jsonArray.toString();
		
	}
	
}
