package com.callke8.system.remindertype;

import java.util.*;

import com.callke8.system.operator.Operator;
import com.callke8.system.tasktype.SysTaskType;
import com.callke8.utils.*;
import com.jfinal.plugin.activerecord.*;

public class SysReminderType extends Model<SysReminderType>  {

	private static final long serialVersionUID = 1L;
	public static SysReminderType dao = new SysReminderType();

	public Page getSysReminderTypeByPaginate(int pageNumber,int pageSize,String reminderType,int numberOrder) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from sys_reminder_type where 1=1");

		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(reminderType)) {
			sb.append(" and REMINDER_TYPE like ?");
			pars[index] = "%" + reminderType + "%";
			index++;
		}
		
		if(numberOrder != 0) {
			sb.append(" and NUMBER_ORDER=?");
			pars[index] = numberOrder;
			index++;
		}
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY ID ASC",ArrayUtils.copyArray(index, pars));
		return p;
	}

	public Map getSysReminderTypeByPaginateToMap(int pageNumber,int pageSize,String reminderType,int numberOrder) {

		Page<Record> p =  getSysReminderTypeByPaginate(pageNumber,pageSize,reminderType,numberOrder);

		int total = p.getTotalRow();     //取出总数量

		ArrayList<Record> newList = new ArrayList<Record>();
		
		for(Record r:p.getList()) {
			
			//设置操作员名字（工号）
			String uc = r.getStr("CREATE_USERCODE");   //取出创建人工号
			Operator oper = Operator.dao.getOperatorByOperId(uc);
			if(!BlankUtils.isBlank(oper)) {
				r.set("CREATE_USERCODE_DESC", oper.get("OPER_NAME") + "(" + uc + ")");
			}
			
			newList.add(r);
		}
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", newList);

		return map;
	}
	public boolean add(SysReminderType formData) {

		Record r = new Record();
		r.set("REMINDER_TYPE", formData.get("REMINDER_TYPE"));
		r.set("NUMBER_ORDER", formData.get("NUMBER_ORDER"));
		r.set("CREATE_USERCODE", formData.get("CREATE_USERCODE"));
		r.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
		return add(r);
	}

	public boolean add(Record record) {

		boolean b = Db.save("sys_reminder_type", "ID", record);
		return b;

	}

	public boolean update(String reminderType,int numberOrder,int id) {

		boolean b = false;
		String sql = "update sys_reminder_type set REMINDER_TYPE=?,NUMBER_ORDER=? where ID=?";

		int count = Db.update(sql,reminderType,numberOrder,id);
		if(count > 0) {
			b = true;
		}
		return b;

	}
	
	//取出所有催缴类型的列表
	public List<Record> getAllSysReminderType() {
		
		String sql = "select * from sys_reminder_type";
		
		List<Record> list  = Db.find(sql);
		
		return list;
	}

	public SysReminderType getSysReminderTypeById(int id){

		String sql = "select * from sys_reminder_type where ID=?";
		SysReminderType entity = findFirst(sql, id);
		return entity;

	}
	
	/**
	 * 根据催缴类型的序号取得催缴类型
	 * 
	 * @param reminderTypeNumberOrder
	 * @return
	 */
	public SysReminderType getSysReminderTypeByNumberOrder(String reminderTypeNumberOrder) {
		
		String sql = "select * from sys_reminder_type where NUMBER_ORDER=?";
		SysReminderType entity = findFirst(sql,reminderTypeNumberOrder);
		return entity;
	}
	
	/**
	 * 根据催缴类型的序号，取得该催缴类型的描述（即名称）
	 * 
	 * @param reminderTypeNumberOrder
	 * @return
	 */
	public String getReminderTypeDescByNumberOrder(String reminderTypeNumberOrder) {
		
		SysReminderType sysReminderType = getSysReminderTypeByNumberOrder(reminderTypeNumberOrder);
		
		if(BlankUtils.isBlank(sysReminderType)) {
			return null;
		}else {
			return sysReminderType.getStr("REMINDER_TYPE");
		}
		
	}
	
	/**
	 * 根据任务类型，查询
	 * 
	 * @param taskType
	 * @return
	 */
	public SysReminderType getSysReminderTypeByReminderType(String reminderType) {
		String sql = "select * from sys_reminder_type where REMINDER_TYPE=?";
		SysReminderType entity = findFirst(sql,reminderType);
		return entity;
	}
	
	/**
	 * 根据任务序号，查询
	 * 
	 * @param taskType
	 * @return
	 */
	public List<SysReminderType> getSysReminderTypeByNumberOrder(int numberOrder) {
		String sql = "select * from sys_reminder_type where NUMBER_ORDER=?";
		List<SysReminderType> list = find(sql, numberOrder);
		return list;
	}

	public boolean deleteById(String id) {

		boolean b = false;
		int count = 0;
		count = Db.update("delete from sys_reminder_type where ID=?",id);
		if(count > 0) {
			b = true;
		}
		return b;

	}
}
