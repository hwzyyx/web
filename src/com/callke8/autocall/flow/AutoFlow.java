package com.callke8.autocall.flow;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 *  自动外呼流程规则模块
 *  
 *  主要是用于制定外呼的流程语音
 *  
 * @author 黄文周
 *
 */
public class AutoFlow extends Model<AutoFlow> {
	
	private static final long serialVersionUID = 1L;

	public static final AutoFlow dao = new AutoFlow();
	
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
