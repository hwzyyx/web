package com.callke8.system.remindertypeassign;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class SysReminderTypeAssign extends Model<SysReminderTypeAssign> {
	
	private static final long serialVersionUID = 1L;
	public static SysReminderTypeAssign dao = new SysReminderTypeAssign();
	
	/**
	 * 根据操作员，得到分配到的所有的催缴类型
	 * 
	 * @param operId
	 * @return
	 */
	public List<Record> getSysReminderTypeAssignByOperId(String operId) {
		
		String sql = "select * from sys_reminder_type_assign where OPER_ID=?";
		
		List<Record> list = Db.find(sql, operId);
		
		return list;
	}
	
	/**
	 * 批量添加记录
	 * 
	 * @param sysReminderTypeAssign
	 * @return
	 */
	public int add(ArrayList<Record> sysReminderTypeAssignList) {
		
		int successCount = 0;
		
		for(Record sysReminderTypeAssign:sysReminderTypeAssignList) {
			
			boolean b = add(sysReminderTypeAssign);
			
			if(b) {
				successCount++;
			}
		}
		
		return successCount;
	}
	
	/**
	 * 新增催缴类型分配结果
	 * 
	 * @param sysReminderTypeAssign
	 * @return
	 */
	public boolean add(Record sysReminderTypeAssign) {
		
		boolean b = Db.save("sys_reminder_type_assign", "ID", sysReminderTypeAssign);
		
		return b;
	}
	
	/**
	 * 删除目标操作员的催缴类型分配
	 * 
	 * @param operId
	 * @return
	 */
	public int deleteSysReminderTypeAssign(String operId) {
		
		String sql = "delete from sys_reminder_type_assign where OPER_ID=?";
		
		int count = Db.update(sql, operId);
		
		return count;
		
	}
	
	/**
	 * 删除某个操作员的催缴类型分配记录，以目标号码的ID为条件
	 * 
	 * @param reminderType_Id
	 * @return
	 */
	public int deleteSysReminderTypeAssignByReminderType_Id(int reminderType_Id) {
		
		String sql = "delete from sys_reminder_type_assign where REMINDER_TYPE_ID=?";
		
		int count = Db.update(sql, reminderType_Id);
		
		return count;
	}
	
}
