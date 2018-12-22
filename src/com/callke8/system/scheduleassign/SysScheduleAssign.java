package com.callke8.system.scheduleassign;

import java.util.ArrayList;
import java.util.List;

import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class SysScheduleAssign extends Model<SysScheduleAssign> {
	
	private static final long serialVersionUID = 1L;
	public static SysScheduleAssign dao = new SysScheduleAssign();
	
	/**
	 * 根据操作员，得到分配到的所有任务类型
	 * 
	 * @param operId
	 * @return
	 */
	public List<Record> getSysScheduleAssignByOperId(String operId) {
		String sql = "select * from sys_schedule_assign where OPER_ID=?";
		
		List<Record> list = Db.find(sql, operId);
		
		return list;
	}
	
	/**
	 * 批量添加记录
	 * 
	 * @param sysScheduleAssignList
	 * @return
	 */
	public int add(ArrayList<Record> sysScheduleAssignList) {
		
		int successCount = 0;
		
		for(Record sysScheduleAssign:sysScheduleAssignList) {
			boolean b = add(sysScheduleAssign);
			
			if(b) {
				successCount++;
			}
		}
		
		return successCount;
	}
	
	/**
	 * 新增调度任务分配结果
	 * 
	 * @param sysScheduleAssign
	 * @return
	 */
	public boolean add(Record sysScheduleAssign) {
		boolean b =  Db.save("sys_schedule_assign","SCHEDULE_ID", sysScheduleAssign);
		
		return b;
	}
	
	/**
	 * 删除目标操作员的调度任务分配
	 * 
	 * @param operId
	 * @return
	 */
	public int deleteSysScheduleAssign(String operId) {
		
		String sql = "delete from sys_schedule_assign where OPER_ID=?";
		
		int count = Db.update(sql, operId);
		
		return count;
		
	}
	
	/**
	 * 删除某个操作员的调度任务分配记录，以目标的ID为条件
	 * 
	 * @param SCHEDULE_Id
	 * @return
	 */
	public int deleteSysScheduleAssignBySchedule_Id(String scheduleId) {
		
		String sql = "delete from sys_schedule_assign where SCHEDULE_ID=?";
		
		int count = Db.update(sql, scheduleId);
		
		return count;
	}
	
	/**
	 * 根据传入的操作员的工号，得到该操作员已经被分配到的调度任务，并取出其中的一个
	 * 
	 * @param operId
	 * @return
	 */
	public String getScheduleForOperIdByAssigned(String operId) {
		
		String scheduleId = null;   //定义一个调度任务的ID，用于返回
		
		List<Record> assignList = SysScheduleAssign.dao.getSysScheduleAssignByOperId(operId);
		if(!BlankUtils.isBlank(assignList) && assignList.size() >0) {
			Record scheduleAssign = assignList.get(0);     		   //取出第一个
			scheduleId = scheduleAssign.getStr("SCHEDULE_ID");     //得到该调度任务的ID
		}
		
		return scheduleId;
		
	}
	
}
