package com.callke8.system.operator;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 数据表 sys_oper_role
 * 
mysql> desc sys_oper_role;
+------------+-------------+------+-----+---------+-------+
| Field      | Type        | Null | Key | Default | Extra |
+------------+-------------+------+-----+---------+-------+
| OPER_ID    | varchar(16) | NO   | PRI | NULL    |       |
| ROLE_CODE  | varchar(16) | NO   | PRI | NULL    |       |
| CREATETIME | datetime    | YES  |     | NULL    |       |
| STAR_TTIME | datetime    | YES  |     | NULL    |       |
| STOP_TIME  | datetime    | YES  |     | NULL    |       |
+------------+-------------+------+-----+---------+-------+
5 rows in set (0.01 sec)
 * @author Administrator
 *
 */
public class OperRole extends Model<OperRole> {

	public static OperRole dao = new OperRole();
	
	/**
	 * 根据操作员 ID，取得角色编码
	 * @param operId
	 * @return
	 */
	public List<String> getRoleCodeByOperId(String operId) {
		List<String> roleCodes = new ArrayList<String>();
		String sql = "select * from sys_oper_role where OPER_ID=?";
		
		List<Record> operRoles = Db.find(sql, operId);
		
		for(Record operRole:operRoles) {
			roleCodes.add(operRole.get("ROLE_CODE").toString());
		}
		
		return roleCodes;
	}
	
	/**
	 * 添加操作员角色表
	 * 
	 * @param operRole
	 * @return
	 */
	public boolean add(OperRole operRole) {
		boolean b = false;
		
		if(operRole.save()) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 删除 operRole
	 * @param operRole
	 * @return
	 */
	public boolean delete(OperRole operRole) {
		boolean b = false;
		
		if(operRole.delete()) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据操作员ID，删除全部的操作员角色记录
	 * @param operId
	 * @return
	 */
	public boolean delete(String operId) {
		boolean b = false;
		String sql = "delete from sys_oper_role where OPER_ID=?";
		
		int count = Db.update(sql, operId);
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据角色代码，查询使用的数量
	 * @param roleCode
	 * @return
	 */
	public int getCountByRoleCode(String roleCode) {
		
		String sql = "select count(*) as count from sys_oper_role where ROLE_CODE=?";
		
		Record record = Db.findFirst(sql,roleCode);
		
		System.out.println(record);
		
		int count = Integer.valueOf(record.get("count").toString());
		
		return count;
	}
	
}
