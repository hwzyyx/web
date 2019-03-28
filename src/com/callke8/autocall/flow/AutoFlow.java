package com.callke8.autocall.flow;

import java.util.*;
import com.callke8.utils.*;
import com.jfinal.plugin.activerecord.*;

public class AutoFlow extends Model<AutoFlow>  {

	private static final long serialVersionUID = 1L;
	public static AutoFlow dao = new AutoFlow();

	public Page getAutoFlowByPaginate(int pageNumber,int pageSize,String flowName) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from ac_flow where 1=1");

		//条件判断暂时不自动添加

		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY REMINDER_TYPE ASC",ArrayUtils.copyArray(index, pars));
		return p;
	}

	public Map getAutoFlowByPaginateToMap(int pageNumber,int pageSize,String flowName) {

		Page<Record> p =  getAutoFlowByPaginate(pageNumber,pageSize,flowName);

		int total = p.getTotalRow();     //取出总数量

		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", p.getList());

		return map;
	}
	public boolean add(AutoFlow formData) {

		Record r = new Record();
		r.set("FLOW_NAME", formData.get("FLOW_NAME"));
		r.set("FLOW_RULE", formData.get("FLOW_RULE"));

		return add(r);
	}

	public boolean add(Record record) {

		boolean b = Db.save("ac_flow", "FLOW_ID", record);
		return b;

	}

	public boolean update(String flowName,String flowRule,String flowId) {

		boolean b = false;
		String sql = "update ac_flow set FLOW_NAME=?,FLOW_RULE=? where FLOW_ID=?";

		int count = Db.update(sql,flowName,flowRule,flowId);
		if(count > 0) {
			b = true;
		}
		return b;

	}

	public AutoFlow getAutoFlowById(String flowId){

		String sql = "select * from ac_flow where FLOW_ID=?";
		AutoFlow entity = findFirst(sql, flowId);
		return entity;

	}

	public boolean deleteById(String id) {

		boolean b = false;
		int count = 0;
		count = Db.update("delete from ac_flow where FLOW_ID=?",id);
		if(count > 0) {
			b = true;
		}
		return b;

	}
	
	/**
	 * 取得所有的 流程
	 * 
	 * @return
	 */
	public List<Record> getAllAutoFlow() {
		
		String sql = "select * from ac_flow order by REMINDER_TYPE asc";
		
		List<Record> list = Db.find(sql);
		
		return list;
	}
	
	/**
	 * 根据催缴类型，取得流程规则模块
	 * 
	 * @param reminderType
	 * @return
	 */
	public AutoFlow getAutoFlowByReminderType(String reminderType) {
		
		//System.out.println("reminderType-===:" + reminderType);
		
		String sql = "select * from ac_flow where REMINDER_TYPE=?";
		
		AutoFlow autoFlow = findFirst(sql, reminderType);
		
		return autoFlow;
		
	}
	
}
