package com.callke8.autocall.autonumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;


public class AutoNumber extends Model<AutoNumber> {
	
	private static final long serialVersionUID = 1L;
	
	public static AutoNumber dao = new AutoNumber();
	
	public Page getAutoNumberByPaginate(int pageNumber,int pageSize,String numberName,String createUserCode,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("from ac_number where 1=1");
		
		if(!BlankUtils.isBlank(numberName)) {
			sb.append(" and NUMBER_NAME like ?");
			pars[index] = "%" + numberName + "%";
			index++;
		}
		
		//根据创建ID
		if(!BlankUtils.isBlank(createUserCode)) {
			sb.append(" and CREATE_USERCODE in(" + createUserCode + ")");
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CREATE_TIME>?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CREATE_TIME<?");
			pars[index] = endTime + "23:59:59";
			index++;
		}
		
		System.out.println("sql语句：" + sb.toString() + "startTime:" + startTime + " 00:00:00" + "; endTime:" + endTime + " 23:59:59");
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY NUMBER_ID DESC",ArrayUtils.copyArray(index, pars));
		
		return p;
	}
	
	
	public Map getAutoNumberByPaginateToMap(int pageNumber,int pageSize,String numberName,String createUserCode,String startTime,String endTime) {
		
		Page<Record> p = getAutoNumberByPaginate(pageNumber, pageSize, numberName,createUserCode,startTime,endTime);
		
		int total = p.getTotalRow();     //取出总数量
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:p.getList()) {
			
			String oc = r.get("ORG_CODE");   //得到组织编码
			
			Record o = Org.dao.getOrgByOrgCode(oc);  //取出组织（部门）
			if(!BlankUtils.isBlank(o)) {
				r.set("ORG_CODE_DESC", o.get("ORG_NAME"));
			}
			
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
	
	public boolean add(AutoNumber autoNumber) {
		
		boolean b = autoNumber.save();
		
		return b;
	}
	
	
	public boolean update(String numberName,String numberId) {
		
		boolean b = false;
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[2];
		int index = 0;
		
		sb.append("update ac_number set ");
		
		if(!BlankUtils.isBlank(numberName)) {
			
			sb.append("NUMBER_NAME=?");
			pars[index] = numberName;
			index ++;
		}
		
		if(!BlankUtils.isBlank(numberId)) {
			sb.append(" where NUMBER_ID=?");
			pars[index] = numberId;
			index++;
		}
		
		int count = Db.update(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	
	public boolean delete(AutoNumber autoNumber) {
		
		boolean b = false;
		
		if(BlankUtils.isBlank(autoNumber)) {
			return b;
		}
		
		String numberId = autoNumber.get("NUMBER_ID");
		
		b = deleteByNumberId(numberId);
		
		return b;
	}
	
	public boolean deleteByNumberId(String numberId) {
		
		boolean b = false;
		
		int count = 0;
		
		count = Db.update("delete from ac_number where NUMBER_ID=?", numberId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	public AutoNumber getAutoNumberByNumberId(String numberId) {
		
		AutoNumber autoNumber = findFirst("select * from ac_number where NUMBER_ID=?", numberId);
		
		return autoNumber;
	}

}








































