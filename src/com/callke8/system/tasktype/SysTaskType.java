package com.callke8.system.tasktype;

import java.util.*;

import com.callke8.system.operator.Operator;
import com.callke8.utils.*;
import com.jfinal.plugin.activerecord.*;

public class SysTaskType extends Model<SysTaskType>  {

	private static final long serialVersionUID = 1L;
	public static SysTaskType dao = new SysTaskType();

	public Page getSysTaskTypeByPaginate(int pageNumber,int pageSize,String taskType,int numberOrder) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from sys_task_type where 1=1");

		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(taskType)) {
			sb.append(" and TASK_TYPE like ?");
			pars[index] = "%" + taskType + "%";
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

	public Map getSysTaskTypeByPaginateToMap(int pageNumber,int pageSize,String taskType,int numberOrder) {

		Page<Record> p =  getSysTaskTypeByPaginate(pageNumber,pageSize,taskType,numberOrder);

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
	
	public boolean add(SysTaskType formData) {

		Record r = new Record();
		r.set("TASK_TYPE", formData.get("TASK_TYPE"));
		r.set("NUMBER_ORDER", formData.get("NUMBER_ORDER"));
		r.set("CREATE_USERCODE", formData.get("CREATE_USERCODE"));
		r.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
		return add(r);
	}

	public boolean add(Record record) {

		boolean b = Db.save("sys_task_type", "ID", record);
		return b;

	}

	public boolean update(String taskType,int numberOrder,int id) {

		boolean b = false;
		String sql = "update sys_task_type set TASK_TYPE=?,NUMBER_ORDER=? where ID=?";

		int count = Db.update(sql,taskType,numberOrder,id);
		if(count > 0) {
			b = true;
		}
		return b;

	}
	
	//取出所有任务类型的列表
	public List<Record> getAllSysTaskType() {
		
		String sql = "select * from sys_task_type";
		
		List<Record> list  = Db.find(sql);
		
		return list;
	}
	
	public SysTaskType getSysTaskTypeById(int id){

		String sql = "select * from sys_task_type where ID=?";
		SysTaskType entity = findFirst(sql, id);
		return entity;

	}
	
	/**
	 * 根据任务类型的序号取得任务类型
	 * 
	 * @param taskTypeNumberOrder
	 * @return
	 */
	public SysTaskType getSysTaskTypeByNumberOrder(String taskTypeNumberOrder) {
		
		String sql = "select * from sys_task_type where NUMBER_ORDER=?";
		SysTaskType entity = findFirst(sql,taskTypeNumberOrder);
		return entity;
	}
	
	/**
	 * 根据任务类型的序号，取得该任务类型的描述（即名称）
	 * 
	 * @param taskTypeNumberOrder
	 * @return
	 */
	public String getTaskTypeDescByNumberOrder(String taskTypeNumberOrder) {
		
		SysTaskType sysTaskType = getSysTaskTypeByNumberOrder(taskTypeNumberOrder);
		
		if(BlankUtils.isBlank(sysTaskType)) {
			return null;
		}else {
			return sysTaskType.getStr("TASK_TYPE");
		}
		
	}
	
	/**
	 * 根据任务类型，查询
	 * 
	 * @param taskType
	 * @return
	 */
	public SysTaskType getSysTaskTypeByTaskType(String taskType) {
		String sql = "select * from sys_task_type where TASK_TYPE=?";
		SysTaskType entity = findFirst(sql,taskType);
		return entity;
	}
	
	/**
	 * 根据任务序号，查询
	 * 
	 * @param taskType
	 * @return
	 */
	public List<SysTaskType> getSysTaskTypeByNumberOrder(int numberOrder) {
		String sql = "select * from sys_task_type where NUMBER_ORDER=?";
		List<SysTaskType> list = find(sql, numberOrder);
		return list;
	}
	
	
	public boolean deleteById(String id) {

		boolean b = false;
		int count = 0;
		count = Db.update("delete from sys_task_type where ID=?",id);
		if(count > 0) {
			b = true;
		}
		return b;

	}
}
