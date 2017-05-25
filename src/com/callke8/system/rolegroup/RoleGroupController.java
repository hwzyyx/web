package com.callke8.system.rolegroup;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.DwzRenderJson;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class RoleGroupController extends Controller {
	
	public void index() {
		/** 收集查询参数 **/
		String ser_groupCode = getPara("ser_groupCode");
		String ser_groupName = getPara("ser_groupName");
		String ser_state = getPara("ser_state");
		String npp = getPara("numPerPage");      //得到每页显示的数量
		String pageNum = getPara("pageNum");     //得到提交的页码
		
		//System.out.println("npp:--" + npp);
		
		int currentPage = BlankUtils.isBlank(pageNum)?1:Integer.valueOf(pageNum);  //得到参数，当前页
		int numPerPage = BlankUtils.isBlank(npp)?10:Integer.valueOf(npp);          //得到参数，每页显示的数量
		
		/** 收集结束 **/
		//System.out.println("查询的条件为：groupCode=" + ser_groupCode + ",groupName=" + ser_groupName + ",state=" + ser_state);
		
		Page<Record> page = RoleGroup.dao.getRoleGroupsByPaginate(currentPage, numPerPage,ser_groupCode,ser_groupName,ser_state);
		
		setAttr("ser_groupCode",ser_groupCode);       
		setAttr("ser_groupName",ser_groupName);
		setAttr("ser_state",ser_state);
		setAttr("numPerPage",page.getPageSize());    //设置每页数量
		setAttr("currentPage",page.getPageNumber()); //设置当前页
		setAttr("totalCount", page.getTotalRow());   //设置总记录数
		setAttr("roleGroups", page.getList());       //设置数据列表
		
		render("list.jsp");
		
	}
	
	//@ActionKey("rolegroupsave")
	public void save() {
		RoleGroup rg = getModel(RoleGroup.class,"roleGroup");
		
		//判断是否有相同的角色组代码
		String groupCode = rg.get("GROUP_CODE");
		RoleGroup chkRg = RoleGroup.dao.getRoleGroupByRoleGroupCode(groupCode);
		
		if(!BlankUtils.isBlank(chkRg)) {    /** 如果根据角色代码查询结果不为空，返回错误 */
			render(DwzRenderJson.error("添加失败:已经存在相同的角色组代码！"));
			return;
		}
		
		rg.set("CREATETIME", DateFormatUtils.getCurrentDate());
		
		RoleGroup.dao.add(rg);
		
		render(DwzRenderJson.closeCurrentAndRefresh("showRoleGroup", "保存成功"));
		
	}
	
	public void add() {
		
	}
	
	public void edit() {
		String groupCode = getPara("groupCode");  //得到角色组代码
		
		//System.out.println("得到参数代码：" + groupCode);
		
		RoleGroup rg = RoleGroup.dao.getRoleGroupByRoleGroupCode(groupCode);
		
		//System.out.println("查询到 RG=" + rg);
		
		setAttr("roleGroup", rg);
		
	}
	
	public void update() {
		RoleGroup rg = getModel(RoleGroup.class,"roleGroup");
		
		//System.out.println("修改时提交的groupCode = " + rg.get("GROUP_CODE"));
		//System.out.println("rg:" + rg);
		
		boolean b = RoleGroup.dao.update(rg);
		
		if(b) {
			render(DwzRenderJson.success("修改成功！","showRoleGroup"));
			return;
		}else {
			render(DwzRenderJson.error("修改失败"));
			return;
		}
		
	}
	
	public void delete() {
		//得到参数 groupCode
		String groupCode = getPara("groupCode");
		
		//System.out.println("得到删除参数：" + groupCode);
		
		boolean b = RoleGroup.dao.deleteByGroupCode(groupCode);
		
		if(b) {
			render(DwzRenderJson.success("删除成功！","showRoleGroup"));
			return;
		}else {
			render(DwzRenderJson.error("删除失败！"));
			return;
		}
		
	}
	
}
