package com.callke8.system.tasktypeassign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.system.remindertype.SysReminderType;
import com.callke8.system.remindertypeassign.SysReminderTypeAssign;
import com.callke8.system.tasktype.SysTaskType;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class SysTaskTypeAssignController extends Controller implements IController {

	@Override
	public void index() {
		render("tasktype_assign.jsp");
	}

	@Override
	public void datagrid() {
		String operId = String.valueOf(getSession().getAttribute("currOperId"));        //当前登录操作员
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = SysTaskType.dao.getSysTaskTypeByPaginateToMap(pageNumber, pageSize, null, 0);      //取出所有的任务类型列表,显示所有的记录
		
		renderJson(map);
	}
	
	/**
	 * 取得目标操作员的任务类型分配情况
	 * 
	 * 返回任务类型的ID,并以逗号连接
	 * 
	 */
	public void getSysTaskTypeAssignResult() {
		
		String targetOperId = getPara("targetOperId");          //目标操作员
		
		List<Record> list = SysTaskTypeAssign.dao.getSysTaskTypeAssignByOperId(targetOperId);
		
		StringBuilder sb = new StringBuilder();
		for(Record r:list) {
			int taskType_Id = r.getInt("TASK_TYPE_ID");
			sb.append(taskType_Id + ",");
		}
		
		String message = sb.toString();
		System.out.println("操作员" + targetOperId + "的任务类型分配结果message:" + message);
		if(BlankUtils.isBlank(message)) {   //如果返回为空时，返回 error
			render(RenderJson.error(message));
		}else {                             //如果返回不为空时，返回成功
			render(RenderJson.success(message.substring(0,message.length()-1)));  //返回时，去除最后一个逗号
		}
		
	}
	
	/**
	 * 保存催缴类型分配
	 */
	public void saveSysTaskTypeAssign() {
		
		String targetOperId = getPara("targetOperId");     //目标操作员
		String ids = getPara("ids");                       //ID列表
		
		if(BlankUtils.isBlank(targetOperId)) {             //如果目标用户不为空时
			render(RenderJson.error("传入的目标操作员为空!无法分配任务类型!"));
			return;
		}
		
		//在保存任务类型分配之前，先删除之前的分配
		SysTaskTypeAssign.dao.deleteSysTaskTypeAssign(targetOperId);
		
		//再进行分配
		if(!BlankUtils.isBlank(ids)) {
			ArrayList<Record> list = new ArrayList<Record>();
			String[] idsStr = ids.split(",");
			for(String id:idsStr) {    //
				Record r = new Record();
				r.set("OPER_ID", targetOperId);
				r.set("TASK_TYPE_ID", Integer.valueOf(id));
				list.add(r);
			}
			
			//然后保存
			int count = SysTaskTypeAssign.dao.add(list);
			
			render(RenderJson.success("任务类型分配成功,系统为操作员：" + targetOperId + "，成功分配 " + count + " 种任务类型!"));
		}else {
			render(RenderJson.error("温馨提示:未选择任何的任务类型进行分配，现已删除了之前所有分配的任务类型!"));
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
