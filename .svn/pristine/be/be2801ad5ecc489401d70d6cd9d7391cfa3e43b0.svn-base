package com.callke8.fastagi.blacklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 黑名单拦截记录
 * 
 * @author Administrator
 *
 */
public class BlackListInterceptRecord extends Model<BlackListInterceptRecord> {

	public static BlackListInterceptRecord dao = new BlackListInterceptRecord();
	
	public Page getBlackListInterceptRecordByPaginate(int currentPage,int numPerPage,String clientTelephone,String clientName,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[4];   //先定义一个容量为4的参数数组
		int index = 0;
		
		sb.append("from sys_blacklist_intercept_record where 1=1");
		
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
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CALLDATE>=?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CALLDATE<=?");
			pars[index] = endTime + " 23:59:59";
			index++;
		}
		
		return Db.paginate(currentPage, numPerPage, "select * ", sb.toString() + " order by CALLDATE desc", ArrayUtils.copyArray(index, pars));
	}
	
	public Map getBlackListInterceptRecordByPaginateToMap(int currentPage,int numPerPage,String clientTelephone,String clientName,String startTime,String endTime) {
		
		Page page = getBlackListInterceptRecordByPaginate(currentPage, numPerPage, clientTelephone, clientName, startTime, endTime);
		
		int total = page.getTotalRow();
		List<Record> list = page.getList();
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:list) {
			
			//先设置拦截的原因
			Record blacklist = BlackList.dao.getBlacklistById(r.get("BLACKLIST_ID").toString());
			
			if(!BlankUtils.isBlank(blacklist)) {
				r.set("REASON", blacklist.get("REASON"));
			}
			newList.add(r);
		}
		
		Map m = new HashMap();
		m.put("total", total);
		m.put("rows", newList);
		
		return m;
	}
	
	public boolean add(Record record) {
		boolean b = Db.save("sys_blacklist_intercept_record","RECORD_ID", record);
		
		return b;
	}
	
	public boolean add(BlackListInterceptRecord record) {
		
		Record newRecord = new Record();
		
		newRecord.set("BLACKLIST_ID", record.get("BLACKLIST_ID"));
		newRecord.set("CLIENT_NAME", record.get("CLIENT_NAME"));
		newRecord.set("CLIENT_TELEPHONE", record.get("CLIENT_TELEPHONE"));
		newRecord.set("CALLDATE", record.get("CALLDATE"));
		
		return add(newRecord);
	}
	
}
