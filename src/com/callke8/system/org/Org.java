package com.callke8.system.org;

import java.util.List;

import com.callke8.system.role.Role;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 表结构
 * mysql> desc sys_org;
+-----------------+--------------+------+-----+---------+-------+
| Field           | Type         | Null | Key | Default | Extra |
+-----------------+--------------+------+-----+---------+-------+
| ORG_CODE        | varchar(255) | NO   | PRI | NULL    |       |
| ORG_NAME        | varchar(64)  | NO   |     | NULL    |       |
| ORG_TYPE_CODE   | varchar(1)   | NO   |     | NULL    |       |
| PARENT_ORG_CODE | varchar(32)  | YES  |     | NULL    |       |
| ORG_DESC        | varchar(128) | YES  |     | NULL    |       |
+-----------------+--------------+------+-----+---------+-------+
 *
 */
public class Org extends Model<Org> {

	public static final Org dao = new Org();
	
	/**
	 * 取得所有的组织
	 * @return
	 */
	public List<Record> getAllOrg() {
		List<Record> list = null;
		
		String sql = "select * from sys_org";
		
		list = Db.find(sql);
		
		return list;
	}
	
	public Record getOrgByOrgCode(String orgCode) {
		
		String sql = "select * from sys_org where ORG_CODE=?";
		
		Record record = Db.findFirst(sql, orgCode);
		
		return record;
	}
	
	/**
	 * 根据 parentOrgCode 查询子集
	 * @param parentCode
	 * @return
	 */
	public List<Record> getOrgByParentOrgCode(String parentOrgCode) {
		
		String sql = "select * from sys_org where PARENT_ORG_CODE=?";
		
		List<Record> list = Db.find(sql, parentOrgCode);
		
		return list;
	}
	
	/**
	 * 得到默认Pid,一般是指 pid = -1 的记录
	 * @param defaultPid
	 * @return
	 */
	public Record getRootOrgByDefaultPid(String defaultPid) {
		String sql = "select * from sys_org where PARENT_ORG_CODE=?";
		Record record = Db.findFirst(sql,defaultPid);
		return record;
	}
	
	/**
	 * 修改组织结构
	 * @param r
	 * @return
	 */
	public boolean update(Org org) {
		boolean b = false;
		int count = 0;
		
		//得到组织代码
		String orgCode = org.get("ORG_CODE");
		
		count = Db.update("update sys_org set ORG_NAME=?,ORG_DESC=? where ORG_CODE=?", org.get("ORG_NAME"),org.get("ORG_DESC"),orgCode);
		
		if(count == 1) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 添加组织
	 * @param org
	 * @return
	 */
	public boolean add(Org org) {
		
		boolean b = false;
		
		if(org.save()) {
			b = true;
		}
		
		return b;
	} 
	
	/**
	 * 根据orgCode 删除组织
	 * @param orgCode
	 * @return
	 */
	public boolean deleteByOrgCode(String orgCode) {
		
		boolean b = false;
		int count = 0;
		
		count = Db.update("delete from sys_org where ORG_CODE=?", orgCode);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	public boolean delete(Org org) {
		
		if(BlankUtils.isBlank(org)) {
			return false;
		}
		
		String orgCode = org.get("ORG_CODE");
		
		return deleteByOrgCode(orgCode);
		
	}
	
	
}
