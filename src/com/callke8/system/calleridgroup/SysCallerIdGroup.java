package com.callke8.system.calleridgroup;

import java.util.*;

import com.callke8.system.operator.Operator;
import com.callke8.utils.*;
import com.jfinal.plugin.activerecord.*;

public class SysCallerIdGroup extends Model<SysCallerIdGroup>  {

	private static final long serialVersionUID = 1L;
	public static SysCallerIdGroup dao = new SysCallerIdGroup();

	public Page getSysCallerIdGroupByPaginate(int pageNumber,int pageSize,String groupName) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from sys_callerid_group where 1=1");

		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(groupName)) {
			sb.append(" and GROUP_NAME like ?");
			pars[index] = "%" + groupName + "%";
			index++;
		}

		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY GROUP_ID DESC",ArrayUtils.copyArray(index, pars));
		return p;
	}

	public Map getSysCallerIdGroupByPaginateToMap(int pageNumber,int pageSize,String groupName) {

		Page<Record> p =  getSysCallerIdGroupByPaginate(pageNumber,pageSize,groupName);

		int total = p.getTotalRow();     //取出总数量

		List<Record> newList = new ArrayList<Record>();
		
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
	public boolean add(SysCallerIdGroup formData) {

		Record r = new Record();
		r.set("GROUP_NAME", formData.get("GROUP_NAME"));
		r.set("CREATE_USERCODE", formData.get("CREATE_USERCODE"));
		r.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
		
		return add(r);
	}

	public boolean add(Record record) {

		boolean b = Db.save("sys_callerid_group", "GROUP_ID", record);
		return b;

	}

	public boolean update(String groupName,int groupId) {

		boolean b = false;
		String sql = "update sys_callerid_group set GROUP_NAME=? where GROUP_ID=?";

		int count = Db.update(sql,groupName,groupId);
		if(count > 0) {
			b = true;
		}
		return b;

	}

	public SysCallerIdGroup getSysCallerIdGroupById(int groupId){

		String sql = "select * from sys_callerid_group where GROUP_ID=?";
		SysCallerIdGroup entity = findFirst(sql, groupId);
		return entity;

	}

	public boolean deleteById(String id) {

		boolean b = false;
		int count = 0;
		count = Db.update("delete from sys_callerid_group where GROUP_ID=?",id);
		if(count > 0) {
			b = true;
		}
		return b;

	}
}
