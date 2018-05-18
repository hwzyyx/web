package com.callke8.system.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 表结构
 * mysql> desc sys_role;
 *
+------------+--------------+------+-----+---------+-------+
| Field      | Type         | Null | Key | Default | Extra |
+------------+--------------+------+-----+---------+-------+
| ROLE_CODE  | varchar(255) | NO   | PRI | NULL    |       |
| ROLE_NAME  | varchar(64)  | NO   |     | NULL    |       |
| ORG_CODE   | varchar(32)  | YES  |     | NULL    |       |
| ROLE_DESC  | varchar(128) | YES  |     | NULL    |       |
| ROLE_STATE | varchar(1)   | YES  |     | NULL    |       |
| TYPE_CODE  | varchar(1)   | YES  |     | NULL    |       |
| CREATETIME | datetime     | YES  |     | NULL    |       |
+------------+--------------+------+-----+---------+-------+
7 rows in set (0.00 sec)
*/

public class Role extends Model<Role> {
	
	public static final Role dao = new Role();

	/**
	 * 按页查询
	 * 
	 * 查询回来的结果已经附带
	 * list   即是查询当前页内容
	 * pageNumber 返回当前页码
	 * pageSize   返回每页显示数量
	 * totalPage  总页数
	 * totalRow   总的数量
	 */
	public Page<Record> getRoleByPaginate(int currentPage,int numPerPage,String roleCode,String roleName,String roleState,boolean currOperIdIsSuperRole) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[8];
		int index = 0;
		
		sb.append("from sys_role where 1=1");
		
		if(!BlankUtils.isBlank(roleCode)) {
			sb.append(" and ROLE_CODE like ?");
			pars[index] = "%" + roleCode + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(roleName)) {
			sb.append(" and ROLE_NAME like ?");
			pars[index] = "%" + roleName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(roleState) && !roleState.equalsIgnoreCase("2")) {
			sb.append(" and ROLE_STATE=?");
			pars[index] = roleState;
			index++;
		}
		
		if(!currOperIdIsSuperRole) {   //如果非超级角色时
			sb.append(" and ROLE_CODE!=?");
			pars[index] = "super";
			index++;
		}
		
		Page<Record> p = Db.paginate(currentPage, numPerPage,"select *",sb.toString(),ArrayUtils.copyArray(index, pars));
		
		return p;
		
	}
	
	public void getRoleByPaginateToJson(int currentPage,int numPerPage,String roleCode,String roleName,String roleState,HttpServletResponse response,boolean currOperIdIsSuperRole) {
		
		/**
		 * 先查询出当前 page 的数量
		 */
		Page p = getRoleByPaginate(currentPage, numPerPage, roleCode, roleName, roleState,currOperIdIsSuperRole);
		
		int total = p.getTotalRow();
		List list = p.getList();
		
		JSONObject jo = new JSONObject();
		
		jo.accumulate("total", total);
		jo.accumulate("rows", list);
		
		//System.out.println("JsonToString:" + jo.toString());
		try {
			response.getWriter().write(jo.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//return jo;
		
	}
	
	public JSONObject getRoleByPaginateToJson(int currentPage,int numPerPage,String roleCode,String roleName,String roleState,boolean currOperIdIsSuperRole) {
		
		/**
		 * 先查询出当前 page 的数量
		 */
		Page p = getRoleByPaginate(currentPage, numPerPage, roleCode, roleName, roleState,currOperIdIsSuperRole);
		int total = p.getTotalRow();
		//List list = p.getList();
		
		JSONObject jo = new JSONObject();
		
		List<Record> list = new ArrayList<Record>();
		
		Record r1 = new Record();
		r1.set("ROLE_CODE", "zs");
		r1.set("ROLE_NAME", "张三");
		r1.set("ROLE_DESC", "张三的信息");
		
		Record r2 = new Record();
		r2.set("ROLE_CODE", "ls");
		r2.set("ROLE_NAME", "李四");
		r2.set("ROLE_DESC", "李四的信息");
		
		
		list.add(r1);
		list.add(r2);
		
		JSONArray ja = JSONArray.fromObject(list);
		
		System.out.println("JSONArray:" + ja.toString());
		
		List<String> l = new ArrayList<String>();
		l.add("aaa");
		l.add("bbbb");
		
		JSONArray ja2 = JSONArray.fromObject(l);
		System.out.println("j2:" + ja2.toString());
		
		jo.accumulate("total", total);
		jo.accumulate("rows", list);
		
		System.out.println("JsonToString:" + jo.toString());
		
		return jo;
		
	}
	public Map getRoleByPaginateToMap(int currentPage,int numPerPage,String roleCode,String roleName,String roleState,boolean currOperIdIsSuperRole) {
		/**
		 * 先查询出当前 page 的数量
		 */
		Page<Record> p = getRoleByPaginate(currentPage, numPerPage, roleCode, roleName, roleState,currOperIdIsSuperRole);
		int total = p.getTotalRow();
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", p.getList());
		
		return map;
	}
	
	public List<Record> getRoleByCondition(String roleCode,String roleName,String roleState) {
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[3]; 
		int index = 0;
		
		sb.append("select * from sys_role_group where 1=1");
		
		if(!BlankUtils.isBlank(roleCode)) {
			sb.append(" and ROLE_CODE like ?");
			pars[index] = "%" + roleCode + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(roleName)) {
			sb.append(" and ROLE_NAME like ?");
			pars[index] = "%" + roleName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(roleState) && !roleState.equalsIgnoreCase("2")) {
			sb.append(" and ROLE_STATE=?");
			pars[index] = roleState;
			index++;
		}
		
		List<Record> list = Db.find(sb.toString(),ArrayUtils.copyArray(index, pars));
		
		return list;
		
	}
	
	/**
	 * 根据 roleCode 查询角色，主要是用于在添加时检查是否已经存在相同的 角色
	 * @param groupCode
	 * @return
	 */
	public Role getRoleByRoleCode(String roleCode) {
		
		return findFirst("select * from sys_role where ROLE_CODE=?",roleCode);
		
	}
	
	public boolean add(Role r) {
		
		boolean b = false;
		
		if(r.save()) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 修改角色
	 * @param r
	 * @return
	 */
	public boolean update(Role r) {
		boolean b = false;
		int count = 0;
		
		//得到角色代码
		String roleCode = r.get("ROLE_CODE");
		
		count = Db.update("update sys_role set ROLE_NAME=?,ROLE_DESC=?,ROLE_STATE=? where ROLE_CODE=?", r.get("ROLE_NAME"),r.get("ROLE_DESC"),r.get("ROLE_STATE"),roleCode);
		
		if(count == 1) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据roleCode 删除角色
	 * @param roleCode
	 * @return
	 */
	public boolean deleteByRoleCode(String roleCode) {
		
		boolean b = false;
		int count = 0;
		
		count = Db.update("delete from sys_role where ROLE_CODE=?", roleCode);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	public boolean delete(Role r) {
		
		if(BlankUtils.isBlank(r)) {
			return false;
		}
		
		String roleCode = r.get("ROLE_CODE");
		
		return deleteByRoleCode(roleCode);
		
	}
	

	
}
