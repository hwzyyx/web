package com.callke8.system.tasktypeassign;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class SysTaskTypeAssign extends Model<SysTaskTypeAssign> {
	
	private static final long serialVersionUID = 1L;
	public static SysTaskTypeAssign dao = new SysTaskTypeAssign();
	
	/**
	 * 根据操作员，得到分配到的所有的任务类型
	 * 
	 * @param operId
	 * @return
	 */
	public List<Record> getSysTaskTypeAssignByOperId(String operId) {
		
		String sql = "select * from sys_task_type_assign where OPER_ID=?";
		
		List<Record> list = Db.find(sql, operId);
		
		return list;
	}
	
	/**
	 * 批量添加记录
	 * 
	 * @param sysTaskTypeAssign
	 * @return
	 */
	public int add(ArrayList<Record> sysTaskTypeAssignList) {
		
		int successCount = 0;
		
		for(Record sysTaskTypeAssign:sysTaskTypeAssignList) {
			
			boolean b = add(sysTaskTypeAssign);
			
			if(b) {
				successCount++;
			}
		}
		
		return successCount;
	}
	
	/**
	 * 新增任务类型分配结果
	 * 
	 * @param sysTaskTypeAssign
	 * @return
	 */
	public boolean add(Record sysTaskTypeAssign) {
		
		boolean b = Db.save("sys_task_type_assign", "ID", sysTaskTypeAssign);
		
		return b;
	}
	
	/**
	 * 删除目标操作员的任务类型分配
	 * 
	 * @param operId
	 * @return
	 */
	public int deleteSysTaskTypeAssign(String operId) {
		
		String sql = "delete from sys_task_type_assign where OPER_ID=?";
		
		int count = Db.update(sql, operId);
		
		return count;
		
	}
	
	/**
	 * 删除某个操作员的任务类型分配记录，以目标号码的ID为条件
	 * 
	 * @param taskType_Id
	 * @return
	 */
	public int deleteSysTaskTypeAssignByTaskType_Id(int taskType_Id) {
		
		String sql = "delete from sys_task_type_assign where TASK_TYPE_ID=?";
		
		int count = Db.update(sql, taskType_Id);
		
		return count;
	}
	
}
