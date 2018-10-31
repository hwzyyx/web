package com.callke8.system.callerid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.system.operator.Operator;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 主叫号码表
 * 
 * @author 黄文周
 *
 */
public class SysCallerId extends Model<SysCallerId> {
	
	private static final long serialVersionUID = 1L;
	
	public static SysCallerId dao = new SysCallerId();
	
	public Page getSysCallerIdByPaginate(int pageNumber,int pageSize,String callerId,String purpose) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("from sys_callerid where 1=1");
		
		if(!BlankUtils.isBlank(callerId)) {
			sb.append(" and CALLERID like ?");
			pars[index] = "%" + callerId + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(purpose)) {
			sb.append(" and PURPOSE like ?");
			pars[index] = "%" + purpose + "%";
			index++;
		}
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY ID DESC",ArrayUtils.copyArray(index, pars));
		
		return p;
		
	}
	
	public Map getSysCallerIdByPaginateToMap(int pageNumber,int pageSize,String callerId,String purpose) {
		
		Page<Record> p =  getSysCallerIdByPaginate(pageNumber,pageSize,callerId,purpose);
		
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
	
	//取出所有主叫号码的列表
	public List<Record> getAllSysCallerId() {
		
		String sql = "select * from sys_callerid";
		
		List<Record> list  = Db.find(sql);
		
		return list;
	}
	
	
	public boolean add(SysCallerId sysCallerId) {
		
		Record r = new Record();
		r.set("CALLERID", sysCallerId.get("CALLERID"));
		r.set("PURPOSE", sysCallerId.get("PURPOSE"));
		r.set("CREATE_USERCODE", sysCallerId.get("CREATE_USERCODE"));
		r.set("ORG_CODE", sysCallerId.get("ORG_CODE"));
		r.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
		
		return add(r);
	}
	
	public boolean add(Record sysCallerId) {
		boolean b = Db.save("sys_callerid", "ID", sysCallerId);
		
		return b;
	}
	
	public boolean update(String callerId,String purpose,int id) {
		
		boolean b = false;
		
		String sql = "update sys_callerid set CALLERID=?,PURPOSE=? where ID=?";
		
		int count = Db.update(sql,callerId,purpose,id);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据主叫号码，从数据表中取出数据
	 * 
	 * @param callerId
	 * @return
	 */
	public SysCallerId getSysCallerIdByCallerId(String callerId) {
		
		String sql = "select * from sys_callerid where CALLERID=?";
		
		SysCallerId sysCallerId = findFirst(sql, callerId);
		
		return sysCallerId;
	}
	
	/**
	 * 根据 id 取出主叫号码信息
	 * 
	 * @param id
	 * @return
	 */
	public SysCallerId getSysCallerIdById(int id) {
		
		String sql = "select * from sys_callerid where ID=?";
		
		SysCallerId sysCallerId = findFirst(sql, id);
		
		return sysCallerId;
	}
	
	/**
	 * 根据ID，删除主叫号码
	 * 
	 * @return
	 */
	public boolean deleteById(int id) {
		
		boolean b = false;
		
		int count = 0;
		
		count = Db.update("delete from sys_callerid where ID=?",id);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
}
