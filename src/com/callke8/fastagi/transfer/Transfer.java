package com.callke8.fastagi.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class Transfer extends Model<Transfer> {
	
	public static Transfer dao = new Transfer();
	
	public Page getTransferByPaginate(int currentPage,int numPerPage,String did,String destination) {
		
		//先拼接SQL语句
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[6];   //先定义一个容量为4的参数数组
		int index = 0;
		
		sb.append("from sys_transfer where 1=1");
		
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
		
		Page page = Db.paginate(currentPage, numPerPage, "select * ", sb.toString() + " order by CREATE_TIME desc", ArrayUtils.copyArray(index, pars));
		
		return page;
	}
	
	public Map getTransferByPaginateToMap(int currentPage,int numPerPage,String did,String destination) {
		
		Map m = new HashMap();
		
		Page page = getTransferByPaginate(currentPage, numPerPage, did, destination);
		List<Record> newList = new ArrayList<Record>();
		List<Record> list = page.getList();
		
		for(Record r:list) {
			
			String trunk = r.get("TRUNK");
			String trunkDesc = MemoryVariableUtil.getDictName("TRUNK_INFO", trunk);
			r.set("TRUNK_DESC",trunkDesc);
			newList.add(r);
		}
		
		m.put("total", page.getTotalRow());
		m.put("rows", newList);
		
		return m;
	}
	
	public boolean add(Record transfer) {
		return Db.save("sys_transfer", "TRANSFER_ID", transfer);
	}
	
	public boolean add(Transfer transfer) {
		Record newTransfer = new Record();
		newTransfer.set("DID", transfer.get("DID"));
		newTransfer.set("DESTINATION", transfer.get("DESTINATION"));
		newTransfer.set("CREATE_TIME", transfer.get("CREATE_TIME"));
		newTransfer.set("START_TIME", transfer.get("START_TIME"));
		newTransfer.set("END_TIME", transfer.get("END_TIME"));
		newTransfer.set("MEMO", transfer.get("MEMO"));
		newTransfer.set("TRUNK", transfer.get("TRUNK"));
		newTransfer.set("OPER_ID", transfer.get("OPER_ID"));
		
		return add(newTransfer);
	}
	
	public boolean update(Transfer transfer) {
		boolean b = false;
		
		int transferId = Integer.valueOf(transfer.get("TRANSFER_ID").toString());
		String did = transfer.get("DID");
		String destination = transfer.get("DESTINATION");
		String trunk = transfer.get("TRUNK");
		String memo = transfer.get("MEMO");
		
		String sql = "update sys_transfer set DID=?,DESTINATION=?,TRUNK=?,START_TIME=?,END_TIME=?,MEMO=? where TRANSFER_ID=?";
		
		int count = Db.update(sql, did,destination,trunk,transfer.get("START_TIME"),transfer.get("END_TIME"),memo,transferId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	public boolean delete(String transferId) {
		boolean b = false;
		String sql = "delete from sys_transfer where TRANSFER_ID=?";
		
		int count = Db.update(sql, transferId);
		
		if(count>0) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 得到活跃的呼叫转移的记录: 当前时间处于开始生效时间及结束生效时间之间，且物服号号码为正确
	 * 
	 * @param did
	 * 			特服号
	 * @return
	 */
	public Record getActiveTransfer(String did) {
		
		String sql = "select * from sys_transfer where DID=? and START_TIME<=? and END_TIME>=? ORDER BY TRANSFER_ID DESC LIMIT 1";
		
		Record transfer = Db.findFirst(sql, did,DateFormatUtils.getCurrentDate(),DateFormatUtils.getCurrentDate());
		
		return transfer;
	}

}
