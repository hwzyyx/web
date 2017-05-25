package com.callke8.system.rolemodule;

import java.util.ArrayList;
import java.util.List;

import com.callke8.common.IController;
import com.callke8.system.module.Module;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class RoleModuleController extends Controller implements IController {
	
	public void index() {
		render("list.jsp");
	}
	
	@SuppressWarnings("unchecked")
	public void auth() {
		
		String moduleCode = getPara("moduleCode");
		String roleCode = getPara("roleCode");
		
		if(!BlankUtils.isBlank(roleCode)) {     //如果roleCode不为空时，才进行授权
			
			//在进行授权之前，先将之前的 roleCode 的授权全部删除
			RoleModule.dao.deleteByRoleCode(roleCode);
			
			String[] moduleCodes = moduleCode.split(",");   //根据
			List<Model> list = new ArrayList<Model>();
			
			for(String mc:moduleCodes) {         //生成对象组，并进行保存授权
				//同时还需要将当前菜单编码的父代码查询出来，父代码为 -1 时，表示这是根目录，内不需要加入菜单权限表
				Record module = Module.dao.getModuleByModuleCode(mc);
				
				if(BlankUtils.isBlank(module)) {
					continue;
				}
				
				String parentCode = module.get("PARENT_CODE");
				if(!BlankUtils.isBlank(mc) && !parentCode.equalsIgnoreCase("-1") && !mc.equalsIgnoreCase("-1")) {    //不为空时，或是-1时
					Model<RoleModule> rm = new RoleModule();
					
					rm.set("MODULE_CODE",mc);
					rm.set("ROLE_CODE", roleCode);
					
					list.add(rm);
				}
			}
			
			RoleModule.dao.add(list);
			
			render(RenderJson.success("授权成功!"));
		}else {
			render(RenderJson.error("授权失败，没有选择角色!"));
		}
	}
	
	/**
	 * 根据传入的角色代码，取得授权列表，主要是用于授权时默认选中值
	 */
	public void getRoleModuleByRoleCode() {
		
		String roleCode = getPara("roleCode");
		StringBuilder sb = new StringBuilder();
		
		if(!BlankUtils.isBlank(roleCode)) {
			
			List<Record> list = RoleModule.dao.getRoleModuleByRoleCode(roleCode);
			
			for(Record r:list) {
				sb.append(r.get("MODULE_CODE") + ",");
			}
			
			String message = sb.toString();
			
			if(BlankUtils.isBlank(message)) {
				render(RenderJson.success(message));
			}else {
				//System.out.println("aaaaa:" + message.substring(0,message.length()-1));
				render(RenderJson.success(message.substring(0,message.length()-1)));   //去掉最后一个逗号
			}
			
			
		}else {
			render(RenderJson.error("没有选择角色，所以无法获取原来的授权信息!"));
		}
		
	}

	@Override
	public void add() {
		
	}

	@Override
	public void datagrid() {
		
	}

	@Override
	public void delete() {
		
	}

	@Override
	public void update() {
		
	}
	
}
