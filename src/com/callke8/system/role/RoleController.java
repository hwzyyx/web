package com.callke8.system.role;

import java.util.Enumeration;

import com.callke8.common.IController;
import com.callke8.system.operator.OperRole;
import com.callke8.system.rolemodule.RoleModule;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;

public class RoleController extends Controller implements IController {
	
	public void index() {
		render("list.jsp");
	}
	
	
	public void datagrid(){
		String roleCode = getPara("roleCode");
		String roleName = getPara("roleName");
		String roleState = getPara("roleState");
		
		Integer rows = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer page = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		//判断当前登录操作员ID，是否属于超级角色
		//(1)如果为超级角色时，显示所有的角色列表
		//(2)如果非超级角色时，只返回除了超级角色的角色列表
		String currOperId = !BlankUtils.isBlank(getSession().getAttribute("currOperId"))?getSession().getAttribute("currOperId").toString():null;
		boolean currOperIdIsSuperRole = OperRole.dao.checkOperIdIsSuperRole(currOperId);
		
		renderJson(Role.dao.getRoleByPaginateToMap(page,  rows, roleCode, roleName, roleState,currOperIdIsSuperRole));
	}
	
	public void add() {
		Role r = getModel(Role.class,"role");
		
		//判断是否有相同的角色组代码
		String roleCode = r.get("ROLE_CODE");
		Role chkRole = Role.dao.getRoleByRoleCode(roleCode);
		
		if(!BlankUtils.isBlank(chkRole)) {    /** 如果根据角色代码查询结果不为空，返回错误 */
			render(RenderJson.warn("已经存在相同的角色代码！"));
			return;
		}
		
		r.set("CREATETIME", DateFormatUtils.getCurrentDate());
		
		boolean b = Role.dao.add(r);
		
		if(b) {
			render(RenderJson.success("插入角色成功！"));
		}else {
			render(RenderJson.error("插入角色失败！"));
		}
		
	}
	
	public void edit() {
		String roleCode = getPara("roleCode");  //得到角色代码
		
		//System.out.println("得到参数代码：" + roleCode);
		
		Role r = Role.dao.getRoleByRoleCode(roleCode);
		
		//System.out.println("查询到 RG=" + r);
		
		setAttr("role", r);
	}
	
	public void update() {
		Role r = getModel(Role.class,"role");
		
		//System.out.println("修改时提交的roleCode = " + r.get("ROLE_CODE"));
		//System.out.println("r:" + r);
		
		boolean b = Role.dao.update(r);
		
		if(b) {
			render(RenderJson.success("角色修改成功！"));
			return;
		}else {
			render(RenderJson.error("角色修改失败！"));
			return;
		}
	}
	
	public void delete() {
		//得到参数 roleCode
		String roleCode = getPara("roleCode");
		
		//System.out.println("得到删除参数：" + roleCode);
		
		//在删除之前，先判断当前角色是否已经成功某操作员的角色，若有时，则不允许删除
		int count = OperRole.dao.getCountByRoleCode(roleCode);
		
		if(count>0) {
			render(RenderJson.error("角色删除失败,该角色已经被操作员关联，不允许删除!"));
			return;
		}
		
		boolean b = Role.dao.deleteByRoleCode(roleCode);
		
		if(b) {
			//如果角色被删除时，需要将角色菜单授权一变删除
			RoleModule.dao.deleteByRoleCode(roleCode);
			
			render(RenderJson.success("角色删除成功!"));
			return;
		}else {
			render(RenderJson.error("角色删除失败!"));
			return;
		}
	}
	
}
