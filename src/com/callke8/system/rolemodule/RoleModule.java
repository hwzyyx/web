package com.callke8.system.rolemodule;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class RoleModule extends Model<RoleModule> {

	public static RoleModule dao = new RoleModule();
	
	public boolean add(List<Model> roleModules) {
		
		boolean b = true;
		
		for(Model<RoleModule> rm:roleModules) {
			
			if(!rm.save()) {
				b = false;
			}
			
		}
		return b = true;
	}
	
	/**
	 * 删除当前角色对应的授权
	 *     授权的方法是：先删除当前 roleCode 的记录，然后再将已经选中的菜单添加进去
	 * @param roleCode
	 * @return
	 */
	public boolean deleteByRoleCode(String roleCode) {
		boolean b = false;
		
		String sql = "delete from sys_role_module where ROLE_CODE=?";
		
		int count = Db.update(sql, roleCode);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据菜单编码，删除角色菜单授权数据，主要是用于删除菜单时做同步
	 * @param moduleCode
	 * @return
	 */
	public boolean deleteByModuleCode(String moduleCode) {
		
		boolean b = false;
		
		String sql = "delete from sys_role_module where MODULE_CODE=?";
		
		int count = Db.update(sql, moduleCode);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据 roleCode 取得所有的菜单，主要是用于授权时已经选中的记录
	 * 
	 * @param roleCode
	 * @return
	 */
	public List<Record> getRoleModuleByRoleCode(String roleCode) {
		
		String sql = "select * from sys_role_module where ROLE_CODE=?";
		
		List<Record> list = Db.find(sql, roleCode);
		
		return list;
	}
	
	/**
	 * 根据角色编码，取得菜单编码，并以 list 返回
	 * 
	 * @param roleCode
	 * @return
	 */
	public List<String> getModuleCodeByRoleCode(String roleCode) {
		List<String> moduleCodes = new ArrayList<String>();
		
		//根据角色代码查询所有的角色权限记录
		List<Record> list = getRoleModuleByRoleCode(roleCode);
		
		for(Record roleModule:list) {
			
			String moduleCode = roleModule.get("MODULE_CODE");
			
			moduleCodes.add(moduleCode);
			
		}
		
		return moduleCodes;
		
	}
	
}
