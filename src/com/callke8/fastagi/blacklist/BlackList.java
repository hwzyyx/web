package com.callke8.fastagi.blacklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 黑名单
 * @author Administrator
 *
 */
public class BlackList extends Model<BlackList> {
	
	public static BlackList dao = new BlackList();
	
	public Page getBlackListByPaginate(int currentPage,int numPerPage,String clientTelephone,String clientName,String state) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[4];   //先定义一个容量为4的参数数组
		int index = 0;
		
		sb.append("from sys_blacklist where 1=1");
		
		if(!BlankUtils.isBlank(clientTelephone)) {
			sb.append(" and CLIENT_TELEPHONE like ?");
			pars[index] = "%" + clientTelephone + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(clientName)) {
			sb.append(" and CLIENT_NAME like ?");
			pars[index] = "%" + clientName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(state) && !state.equalsIgnoreCase("2")) {
			sb.append(" and STATE=?");
			pars[index] = state;
			index++;
		}
		
		return Db.paginate(currentPage, numPerPage, "select * ", sb.toString() + " order by CREATE_TIME desc", ArrayUtils.copyArray(index, pars));
	}
	
	
	public Map getBlackListByPaginateToMap(int currentPage,int numPerPage,String clientTelephone,String clientName,String state) {
		
		Page page = getBlackListByPaginate(currentPage, numPerPage, clientTelephone, clientName, state);
		
		int total = page.getTotalRow();
		List<Record> list = page.getList();
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:list) {
			newList.add(r);
		}
		
		Map m = new HashMap();
		m.put("total", total);
		m.put("rows", newList);
		
		return m;
	}
	
	public boolean add(Record blackList) {
		
		boolean b = Db.save("sys_blacklist", "BLACKLIST_ID", blackList);
		
		return b;
	}
	
	public boolean add(BlackList blackList) {
	
		Record newBlackList = new Record();
		newBlackList.set("CLIENT_NAME", blackList.get("CLIENT_NAME"));
		newBlackList.set("CLIENT_TELEPHONE", blackList.get("CLIENT_TELEPHONE"));
		newBlackList.set("REASON", blackList.get("REASON"));
		newBlackList.set("CREATE_TIME", blackList.get("CREATE_TIME"));
		newBlackList.set("STATE", blackList.get("STATE"));
		newBlackList.set("OPER_ID", blackList.get("OPER_ID"));
		
		return add(newBlackList);
	}
	
	
	public boolean update(BlackList blackList) {
		boolean b = false;
		
		String sql = "update sys_blacklist set CLIENT_TELEPHONE=?,CLIENT_NAME=?,REASON=?,STATE=? where BLACKLIST_ID=?";
		
		int count = Db.update(sql, blackList.get("CLIENT_TELEPHONE"),blackList.get("CLIENT_NAME"),blackList.get("REASON"),blackList.get("STATE"),blackList.get("BLACKLIST_ID"));
		
		if(count>0) {
			b = true;
		}
		return b;
	}
	
	public boolean delete(String blackListId) {
		
		boolean b = false;
		
		String sql = "delete from sys_blacklist where BLACKLIST_ID=?";
		
		int count = Db.update(sql, blackListId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	public Record getBlacklistById(String blacklistId) {
		
		String sql = "select * from sys_blacklist where BLACKLIST_ID=?";
		
		Record blacklist = Db.findFirst(sql, blacklistId);
		
		return blacklist;
	}
	
	public Record getBlackListByClientTelephone(String telephone) {
		
		String sql = "select * from sys_blacklist where CLIENT_TELEPHONE=? and STATE=1";
		
		Record blacklist = Db.findFirst(sql,telephone);
		
		return blacklist;
	}
	
}
