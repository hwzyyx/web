package com.callke8.system.scheduleassign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.system.schedule.SysSchedule;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class SysScheduleAssignController extends Controller implements IController{

	@Override
	public void index() {
		render("schedule_assign.jsp");
	}

	@Override
	public void datagrid() {
		String operId = String.valueOf(getSession().getAttribute("currOperId"));        //当前登录操作员
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = SysSchedule.dao.getScheduleByPaginateToMap(pageNumber, pageSize, null, null,null);      //取出所有的调度任务列表,显示所有的记录
		
		renderJson(map);
	}
	
	/**
	 * 取得目标操作员的调度任务的分配情况
	 * 
	 * 返回调度任务的ID,并以逗号连接
	 * 
	 */
	public void getSysScheduleAssignResult() {
		
		String targetOperId = getPara("targetOperId");     //目标操作员
		
		List<Record> list = SysScheduleAssign.dao.getSysScheduleAssignByOperId(targetOperId);
		
		StringBuilder sb  = new StringBuilder();
		for(Record r:list) {
			String scheduleId = r.getStr("SCHEDULE_ID");
			sb.append(scheduleId + ",");
		}
		
		String message = sb.toString();
		System.out.println("操作员" + targetOperId + "的调度任务分配结果message:" + message);
		if(BlankUtils.isBlank(message)) {
			render(RenderJson.error(message));
		}else {
			render(RenderJson.success(message.substring(0, message.length()-1)));   //返回时，去掉最后一个逗号
		}
		
	}
	
	/**
	 * 保存调度任务的分配结果
	 */
	public void saveSysScheduleAssign() {
		
		String targetOperId = getPara("targetOperId");     //目标操作员
		String ids = getPara("ids");                       //ID列表
		
		if(BlankUtils.isBlank(targetOperId)) {             //如果目标用户为空时
			render(RenderJson.error("传入的目标操作员为空!无法分配调度任务!"));
			return;
		}
		
		//在保存调度任务分配之前，先删除之前的分配
		SysScheduleAssign.dao.deleteSysScheduleAssign(targetOperId);
		
		//再进行分配
		if(!BlankUtils.isBlank(ids)) {
			
			ArrayList<Record> list = new ArrayList<Record>();
			String[] idsStr = ids.split(",");
			for(String id:idsStr) {
				Record r = new Record();
				r.set("OPER_ID",targetOperId);
				r.set("SCHEDULE_ID", id);
				list.add(r);
			}
			
			//然后保存
			int count = SysScheduleAssign.dao.add(list);
			render(RenderJson.success("调度任务分配成功,系统为操作员：" + targetOperId + "，成功分配 " + count + " 种调度任务!"));
		}else {
			render(RenderJson.error("温馨提示:未选择任何的调度任务进行分配，现已删除了之前所有分配的调度任务!"));
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
