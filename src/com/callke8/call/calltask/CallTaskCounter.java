package com.callke8.call.calltask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 外呼任务的计数器，主要是用于计算各种状态的数量进行计算
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
public class CallTaskCounter extends Model<CallTaskCounter> {
	
	public static CallTaskCounter dao = new CallTaskCounter();
	
	
	/**
	 * 根据 taskId 和状态值，根据偏移量增加数量
	 * 
	 * @param taskId
	 * @param callState
	 * @param offset
	 * @return
	 */
	public boolean increaseCounter(int taskId,String callState,int offset) {
		
		boolean b = false;
		
		String sql = "update call_task_counter set count=(count + ?) where CT_ID=? and CALL_STATE=?";
		
		int count = Db.update(sql,offset,taskId,callState);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据 taskId 和状态值，根据偏移量减少数量
	 * 
	 * @param taskId
	 * @param callState
	 * @param offset
	 * @return
	 */
	public boolean reduceCounter(int taskId,String callState,int offset) {
		
		boolean b = false;
		
		String sql = "update call_task_counter set count=(count - ?) where CT_ID=? and CALL_STATE=?";
		
		int count = Db.update(sql,offset,taskId,callState);
		if(count > 0) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 根据任务ID和状态，返回计数的数量
	 * 
	 * @param taskId
	 * 			任务ID
	 * @param callState
	 * 			状态
	 * @return
	 */
	public int getCount(int taskId,String callState) {
		
		String sql = "select COUNT as c from call_task_counter where CT_ID=? and CALL_STATE=?";
		
		Record record = Db.findFirst(sql, taskId, callState);
		
		int count = Integer.valueOf(record.get("c").toString());
		
		return count;
	}
	
	/**
	 * 根据任务ID，删除所有的记数，主要用于在删除任务时，清除该任务的记数器
	 * 
	 * @param taskId
	 * @return
	 */
	public boolean deleteByTaskId(int taskId) {
		
		boolean b = false;
		
		String sql = "delete from call_task_counter where CT_ID=?";
		
		int count = Db.update(sql,taskId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据传入的taskId、callState、offset 创建记数记录
	 * 
	 * @param taskId
	 * 			任务ID
	 * @param callState
	 * 			外呼状态
	 * @param offset
	 * 			偏移量
	 * @return
	 */
	public boolean createCounter(int taskId,String callState,int offset) {
		
		boolean b = false;
		
		Record ctc = new Record();
		ctc.set("CT_ID", taskId);
		ctc.set("CALL_STATE", callState);
		ctc.set("COUNT",offset);
		
		b = Db.save("call_task_counter", ctc);
		
		return b;
	}
	
	/**
	 * 根据taskId和callState检查是否已经存在该任务的状态记录
	 * 
	 * @param taskId
	 * @param callState
	 * @return
	 */
	public boolean isExistCounter(int taskId,String callState) {
		
		boolean b = false;
		
		String sql = "select count(*) count from call_task_counter where CT_ID=? and CALL_STATE=?"; 
		
		Record record = Db.findFirst(sql,taskId,callState);
		
		int count = Integer.valueOf(record.get("count").toString());
		
		if(count>0) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 根据任务的ID，取得记数情况，并以Map<String,Integer>形式返回，前面的Integer是指状态值，后面的为当前状态值的数量
	 * @param taskId
	 * @return
	 */
	public Map<String,Integer> getCounterByTaskId(int taskId) {
		
		Map<String,Integer> map  = new HashMap<String,Integer>();
		
		String sql = "select * from call_task_counter where CT_ID=?";
		
		List<Record> list = Db.find(sql, taskId);
		
		for(Record r:list) {
			
			String state = r.getStr("CALL_STATE");
			int count = r.getInt("COUNT");
			map.put(state, count);
		}
		
		return map;
	}
	
	
}
