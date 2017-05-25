package com.callke8.call.incoming;

import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class InComing extends Model<InComing> {
	
	public static InComing dao = new InComing();
	
	public int add(Record inComing) {
		
		int id = 0;
		boolean b = Db.save("incoming", "ID", inComing);
		
		if(b) {
			id = Integer.valueOf(inComing.get("ID").toString());
		}
		
		return id;
	}
	
	/**
	 * 当通话被接听时，修改通道的状态，并修改接通时间 
	 * 
	 * @param status
	 * @param unqueId
	 * @return
	 */
	public boolean updateStatusByUnqueIdWhereChannelBeAnswer(int status, String uniqueId) {
		boolean b = false;
		String sql = "update incoming set STATUS=?,ANSWERDATE=now() where UNIQUEID=? and STATUS=0";
		
		int count = Db.update(sql, status, uniqueId);
		
		if(count>0) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 当通话被挂机时，修改通道的状态 
	 * 
	 * @param status
	 * @param unqueId
	 * @return
	 */
	public boolean updateStatusByUnqueIdWhereChannelBeHangup(String status, String uniqueId) {
		boolean b = false;
		String sql = "update incoming set STATUS=? where UNIQUEID=?";
		
		int count = Db.update(sql, status, uniqueId);
		
		if(count>0) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 根据来电通道标识符取得状态为 0 或是 1 的记录， 0表示新来电，1表示被接听的新来电
	 * 
	 * @param uniqueId
	 * @return
	 */
	public InComing getIncomingByUniqueId(String uniqueId) {
		
		InComing ic = null;
		
		String sql = "select * from incoming where UNIQUEID=? and STATUS in('0','1') order by ID desc limit 1";
		
		ic = findFirst(sql, uniqueId);
		
		return ic;
	}
	
	
	
}
