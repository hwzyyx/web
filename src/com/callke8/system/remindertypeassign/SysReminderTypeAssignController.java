package com.callke8.system.remindertypeassign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.system.remindertype.SysReminderType;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class SysReminderTypeAssignController extends Controller implements IController {

	@Override
	public void index() {
		render("remindertype_assign.jsp");
	}

	@Override
	public void datagrid() {
		String operId = String.valueOf(getSession().getAttribute("currOperId"));        //当前登录操作员
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = SysReminderType.dao.getSysReminderTypeByPaginateToMap(pageNumber, pageSize, null, 0);      //取出所有的催缴类型列表,显示所有的记录
		
		renderJson(map);
	}
	
	/**
	 * 取得目标操作员的催缴类型分配情况
	 * 
	 * 返回催缴类型的ID,并以逗号连接
	 * 
	 */
	public void getSysReminderTypeAssignResult() {
		
		String targetOperId = getPara("targetOperId");          //目标操作员
		
		List<Record> list = SysReminderTypeAssign.dao.getSysReminderTypeAssignByOperId(targetOperId);
		
		StringBuilder sb = new StringBuilder();
		for(Record r:list) {
			int reminderType_Id = r.getInt("REMINDER_TYPE_ID");
			sb.append(reminderType_Id + ",");
		}
		
		String message = sb.toString();
		System.out.println("操作员" + targetOperId + "的催缴类型分配结果message:" + message);
		if(BlankUtils.isBlank(message)) {   //如果返回为空时，返回 error
			render(RenderJson.error(message));
		}else {                             //如果返回不为空时，返回成功
			render(RenderJson.success(message.substring(0,message.length()-1)));  //返回时，去除最后一个逗号
		}
		
	}
	
	/**
	 * 保存催缴类型分配
	 */
	public void saveSysReminderTypeAssign() {
		
		String targetOperId = getPara("targetOperId");     //目标操作员
		String ids = getPara("ids");                       //ID列表
		
		if(BlankUtils.isBlank(targetOperId)) {             //如果目标用户不为空时
			render(RenderJson.error("传入的目标操作员为空!无法分配催缴类型!"));
			return;
		}
		
		//在保存催缴类型分配之前，先删除之前的分配
		SysReminderTypeAssign.dao.deleteSysReminderTypeAssign(targetOperId);
		
		//再进行分配
		if(!BlankUtils.isBlank(ids)) {
			ArrayList<Record> list = new ArrayList<Record>();
			String[] idsStr = ids.split(",");
			for(String id:idsStr) {    //
				Record r = new Record();
				r.set("OPER_ID", targetOperId);
				r.set("REMINDER_TYPE_ID", Integer.valueOf(id));
				list.add(r);
			}
			
			//然后保存
			int count = SysReminderTypeAssign.dao.add(list);
			
			render(RenderJson.success("催缴类型分配成功,系统为操作员：" + targetOperId + "，成功分配 " + count + " 种催缴类型!"));
		}else {
			render(RenderJson.error("温馨提示:未选择任何的催缴类型进行分配，现已删除了之前所有分配的催缴类型!"));
		}
		
	}
	

	@Override
	public void add() {
		
	}

	@Override
	public void update() {
		
	}

	@Override
	public void delete() {
		
	}

}
