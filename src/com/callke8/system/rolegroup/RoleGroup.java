package com.callke8.system.rolegroup;

import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 角色组Model
 * @author <a href="120077407@qq.com">hwz</a>
 * 
 * 表结构：
 * mysql> desc sys_role_group;
+------------+--------------+------+-----+---------+-------+
| Field      | Type         | Null | Key | Default | Extra |
+------------+--------------+------+-----+---------+-------+
| GROUP_CODE | varchar(255) | NO   | PRI | NULL    |       |
| GROUP_NAME | varchar(64)  | NO   |     | NULL    |       |
| ORG_CODE   | varchar(32)  | YES  |     | NULL    |       |
| GROUP_DESC | varchar(128) | YES  |     | NULL    |       |
| STATE      | varchar(1)   | YES  |     | NULL    |       |
| CREATETIME | datetime     | YES  |     | NULL    |       |
+------------+--------------+------+-----+---------+-------+
 * 
 */
@SuppressWarnings("serial")
public class RoleGroup extends Model<RoleGroup> {

	public static final RoleGroup dao = new RoleGroup();
	
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
	public Page<Record> getRoleGroupsByPaginate(int currentPage,int numPerPage,String groupCode,String groupName,String state) {
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[3]; 
		int index = 0;
		
		sb.append("from sys_role_group where 1=1");
		
		if(!BlankUtils.isBlank(groupCode)) {
			sb.append(" and GROUP_CODE like ?");
			pars[index] = "%" + groupCode + "%";
			index ++;
		}
		
		if(!BlankUtils.isBlank(groupName)) {
			sb.append(" and GROUP_NAME like ?");
			pars[index] = "%" + groupName + "%";
			index ++;
		}
		
		if(!BlankUtils.isBlank(state) && !state.equalsIgnoreCase("2")) {
			sb.append(" and STATE=?");
			pars[index] = state;
			index ++;
		}
		
		System.out.println("SQL:" + sb.toString());
		System.out.println("pars size: " + ArrayUtils.copyArray(index, pars).length);
		
		Page<Record> p = Db.paginate(currentPage, numPerPage,"select *",sb.toString(),ArrayUtils.copyArray(index, pars));
		
		return p;
	}
	
	/**
	 * 根据传入的条件查询
	 * @param groupCode
	 * @param groupName
	 * @param state
	 * @return
	 */
	public List<Record> getRoleGroupsByCondition(String groupCode,String groupName,String state) {
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[3]; 
		int index = 0;
		
		sb.append("select * from sys_role_group where 1=1");
		
		if(!BlankUtils.isBlank(groupCode)) {
			sb.append(" and GROUP_CODE like ?");
			pars[index] = "%" + groupCode + "%";
			index ++;
		}
		
		if(!BlankUtils.isBlank(groupName)) {
			sb.append(" and GROUP_NAME like ?");
			pars[index] = "%" + groupName + "%";
			index ++;
		}
		
		if(!BlankUtils.isBlank(state) && !state.equalsIgnoreCase("2")) {
			sb.append(" and STATE=?");
			pars[index] = state;
			index ++;
		}
		
		System.out.println("SQL:" + sb.toString());
		System.out.println("pars size: " + ArrayUtils.copyArray(index, pars).length);
		
		List<Record> list = Db.find(sb.toString(),ArrayUtils.copyArray(index, pars));
		
		return list;
		
	}
	
	/**
	 * 根据 groupCode 查询角色组，主要是用于在添加时检查是否已经存在相同的 groupCode
	 * @param groupCode
	 * @return
	 */
	public RoleGroup getRoleGroupByRoleGroupCode(String groupCode) {
		
		return findFirst("select * from sys_role_group where GROUP_CODE=?",groupCode);
		
	}
	
	/**
	 * 新增角色组
	 * @param roleGroup
	 * @return
	 */
	public boolean add(RoleGroup roleGroup) {
		
		boolean b = false; 
		b = roleGroup.save();
		
		if(b) {
			b = true;
		}
		
		return b;
	}
	
	
	/**
	 * 修改角色组
	 * @param rg
	 * @return
	 */
	public boolean update(RoleGroup rg) {
		
		boolean b = false;
		int count = 0;
		
		String groupCode = rg.get("GROUP_CODE");  //先得到角色组
		
		count = Db.update("update sys_role_group set GROUP_NAME=?,GROUP_DESC=?,STATE=? where GROUP_CODE=?",rg.get("GROUP_NAME"),rg.get("GROUP_DESC"),rg.get("STATE"),rg.get("GROUP_CODE"));
		
		if(count == 1) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 删除角色组
	 * @param groupCode
	 * @return
	 */
	public boolean deleteByGroupCode(String groupCode) {
		boolean b = false;
		int count = Db.update("delete from sys_role_group where GROUP_CODE=?",groupCode);
		
		if(count==1) {
			b = true;
		}
		
		return b;
	}
	
	public boolean delete(RoleGroup rg) {
		return deleteByGroupCode(rg.getStr("GROUP_CODE"));
	}
	
	
}
