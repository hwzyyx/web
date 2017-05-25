package com.callke8.autocall.autoblacklist;

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


public class AutoBlackList extends Model<AutoBlackList> {
	
	private static final long serialVersionUID = 1L;
	
	public static AutoBlackList dao = new AutoBlackList();
	
	public Page getAutoBlackListByPaginate(int pageNumber,int pageSize,String blackListName,String orgCode,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("from ac_blacklist where 1=1");
		
		if(!BlankUtils.isBlank(blackListName)) {
			sb.append(" and BLACKLIST_NAME like ?");
			pars[index] = "%" + blackListName + "%";
			index++;
		}
		
		//根据组织编码，取得所有的下属的组织编码创建的语音
		if(!BlankUtils.isBlank(orgCode)) {
			//先取出下属所有的编码
			String ocs = "";   //组织 in 的内容，即是 select * from voice where ORG_CODE in ('a','b')    
			String[] orgCodes = orgCode.split(",");   //分割组织代码
			for(String oc:orgCodes) {
				ocs += "\'" + oc + "\',";
			}
			
			ocs = ocs.substring(0,ocs.length()-1);           //去掉最后一个逗号
			System.out.println("OCS:" + ocs);
			sb.append(" and ORG_CODE in(" + ocs + ")");
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
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY BLACKLIST_ID DESC",ArrayUtils.copyArray(index, pars));
		
		return p;
	}
	
	
	public Map getAutoBlackListByPaginateToMap(int pageNumber,int pageSize,String blackListName,String orgCode,String startTime,String endTime) {
		
		Page<Record> p = getAutoBlackListByPaginate(pageNumber, pageSize, blackListName,orgCode,startTime,endTime);
		
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
	
	public boolean add(AutoBlackList autoBlackList) {
		
		boolean b = autoBlackList.save();
		
		return b;
	}
	
	
	public boolean update(String blackListName,String blackListId) {
		
		boolean b = false;
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[2];
		int index = 0;
		
		sb.append("update ac_blacklist set ");
		
		if(!BlankUtils.isBlank(blackListName)) {
			
			sb.append("BLACKLIST_NAME=?");
			pars[index] = blackListName;
			index ++;
		}
		
		if(!BlankUtils.isBlank(blackListId)) {
			sb.append(" where BLACKLIST_ID=?");
			pars[index] = blackListId;
			index++;
		}
		
		int count = Db.update(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	
	public boolean delete(AutoBlackList autoBlackList) {
		
		boolean b = false;
		
		if(BlankUtils.isBlank(autoBlackList)) {
			return b;
		}
		
		String blackListId = autoBlackList.get("BLACKLIST_ID");
		
		b = deleteByBlackListId(blackListId);
		
		return b;
	}
	
	public boolean deleteByBlackListId(String blackListId) {
		
		boolean b = false;
		
		int count = 0;
		
		count = Db.update("delete from ac_blacklist where BLACKLIST_ID=?", blackListId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	public AutoBlackList getAutoBlackListByBlackListId(String blackListId) {
		
		AutoBlackList autoBlackList = findFirst("select * from ac_blacklist where BLACKLIST_ID=?", blackListId);
		
		return autoBlackList;
	}

}








































