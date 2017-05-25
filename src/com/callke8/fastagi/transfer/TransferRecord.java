package com.callke8.fastagi.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class TransferRecord extends Model<TransferRecord> {

	public static TransferRecord dao = new TransferRecord();
	
	public Page getTransferRecordByPaginate(int currentPage,int numPerPage,String did,String destination,String startTime,String endTime) {
		
		//先拼接SQL语句
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[6];   //先定义一个容量为4的参数数组
		int index = 0;
		
		sb.append("from sys_transfer_record where 1=1");
		
		if(!BlankUtils.isBlank(did)) {
			sb.append(" and DID like ?");
			pars[index] = "%" + did + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(destination)) {
			sb.append(" and DESTINATION like ?");
			pars[index] = "%" + destination + "%";
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
	
	public Map getTransferRecordByPaginateToMap(int currentPage,int numPerPage,String did,String destination,String startTime,String endTime) {
		
		Page page = getTransferRecordByPaginate(currentPage, numPerPage, did, destination, startTime, endTime);
		
		int total = page.getTotalRow();
		List<Record> list = page.getList();
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:list) {
			
			String trunk = r.get("TRUNK");
			String trunkDesc = MemoryVariableUtil.getDictName("TRUNK_INFO", trunk);
			r.set("TRUNK_DESC",trunkDesc);
			
			r.set("path","voices/");
			
			newList.add(r);
		}
		
		Map m = new HashMap();
		m.put("total", total);
		m.put("rows", newList);
		
		return m;
	}
	
	/**
	 * 添加呼叫转移记录
	 * 
	 * @param transferRecord
	 * @return
	 */
	public boolean add(Record transferRecord) {
		
		boolean b = Db.save("sys_transfer_record", "RECORD_ID", transferRecord);
		
		return b;
	}
	
}
