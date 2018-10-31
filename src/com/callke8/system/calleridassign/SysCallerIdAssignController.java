package com.callke8.system.calleridassign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.callke8.common.IController;
import com.callke8.system.callerid.SysCallerId;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class SysCallerIdAssignController extends Controller implements IController {

	@Override
	public void index() {
		render("callerid_assign.jsp");
	}

	@Override
	public void datagrid() {
		
		String operId = String.valueOf(getSession().getAttribute("currOperId"));        //当前登录操作员
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = SysCallerId.dao.getSysCallerIdByPaginateToMap(pageNumber, pageSize, null, null);      //取出所有的主叫号码列表,显示所有的记录
		
		renderJson(map);
	}
	
	/**
	 * 取得目标号码的主叫号码分配情况
	 * 
	 * 返回主叫号码的ID,并以逗号连接
	 * 
	 */
	public void getSysCallerIdAssignResult() {
		
		String targetOperId = getPara("targetOperId");          //目标操作员
		
		List<Record> list = SysCallerIdAssign.dao.getSysCallerIdAssignByOperId(targetOperId);
		
		StringBuilder sb = new StringBuilder();
		for(Record r:list) {
			int callerId_Id = r.getInt("CALLERID_ID");
			sb.append(callerId_Id + ",");
		}
		
		String message = sb.toString();
		System.out.println("操作员" + targetOperId + "的主叫分配结果message:" + message);
		if(BlankUtils.isBlank(message)) {   //如果返回为空时，返回 error
			render(RenderJson.error(message));
		}else {                             //如果返回不为空时，返回成功
			render(RenderJson.success(message.substring(0,message.length()-1)));  //返回时，去除最后一个逗号
		}
		
	}
	
	/**
	 * 保存主叫号码分配
	 */
	public void saveSysCallerIdAssign() {
		
		String targetOperId = getPara("targetOperId");     //目标操作员
		String ids = getPara("ids");                       //ID列表
		
		if(BlankUtils.isBlank(targetOperId)) {             //如果目标用户不为空时
			render(RenderJson.error("传入的目标操作员为空!无法分配主叫号码"));
		}
		
		//在保存主叫号码分配之前，先删除之前的分配
		SysCallerIdAssign.dao.deleteSysCallerIdAssign(targetOperId);
		
		//再进行分配
		if(!BlankUtils.isBlank(ids)) {
			ArrayList<Record> list = new ArrayList<Record>();
			String[] idsStr = ids.split(",");
			for(String id:idsStr) {    //
				Record r = new Record();
				r.set("OPER_ID", targetOperId);
				r.set("CALLERID_ID", Integer.valueOf(id));
				list.add(r);
			}
			
			//然后保存
			int count = SysCallerIdAssign.dao.add(list);
			
			render(RenderJson.success("主叫号码分配成功,系统为操作员：" + targetOperId + "，成功分配 " + count + " 个主叫号码!"));
		}else {
			render(RenderJson.error("温馨提示:未选择任何的号码进行分配，现已删除了之前所有分配的主叫!"));
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
