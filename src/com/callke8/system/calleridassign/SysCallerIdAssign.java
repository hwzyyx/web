package com.callke8.system.calleridassign;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 主叫号码分配表
 * 
 * @author 黄文周
 *
 */
public class SysCallerIdAssign extends Model<SysCallerIdAssign> {
	
	public static SysCallerIdAssign dao = new SysCallerIdAssign();

	/**
	 * 根据操作员，得到分配到的所有的主叫
	 * 
	 * @param operId
	 * @return
	 */
	public List<Record> getSysCallerIdAssignByOperId(String operId) {
		
		String sql = "select * from sys_callerid_assign where OPER_ID=?";
		
		List<Record> list = Db.find(sql, operId);
		
		return list;
	}
	
	/**
	 * 批量添加记录
	 * 
	 * @param sysCallerIdAssign
	 * @return
	 */
	public int add(ArrayList<Record> sysCallerIdAssignList) {
		
		int successCount = 0;
		
		for(Record sysCallerIdAssign:sysCallerIdAssignList) {
			
			boolean b = add(sysCallerIdAssign);
			
			if(b) {
				successCount++;
			}
		}
		
		return successCount;
	}
	
	/**
	 * 新增号码分配结果
	 * 
	 * @param sysCallerIdAssign
	 * @return
	 */
	public boolean add(Record sysCallerIdAssign) {
		
		boolean b = Db.save("sys_callerid_assign", "ID", sysCallerIdAssign);
		
		return b;
	}
	
	/**
	 * 删除目标操作员的主叫号码分配
	 * 
	 * @param operId
	 * @return
	 */
	public int deleteSysCallerIdAssign(String operId) {
		
		String sql = "delete from sys_callerid_assign where OPER_ID=?";
		
		int count = Db.update(sql, operId);
		
		return count;
		
	}
	
	/**
	 * 删除某个号码的主叫分配记录，以目标号码的ID为条件
	 * 
	 * @param callerId_Id
	 * @return
	 */
	public int deleteSysCallerIdAssignByCallerId_Id(int callerId_Id) {
		
		String sql = "delete from sys_callerId_assign where CALLERID_ID=?";
		
		int count = Db.update(sql, callerId_Id);
		
		return count;
	}
	
}
