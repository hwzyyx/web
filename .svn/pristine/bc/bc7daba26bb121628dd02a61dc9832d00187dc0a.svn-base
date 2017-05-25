package com.callke8.call.calltask;

import java.util.ArrayList;
import java.util.List;

import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 表结构如下：
 * mysql> desc call_task_auth;
+---------+--------------+------+-----+---------+-------+
| Field   | Type         | Null | Key | Default | Extra |
+---------+--------------+------+-----+---------+-------+
| OPER_ID | varchar(255) | NO   |     | NULL    |       |
| CT_ID   | int(32)      | NO   |     | NULL    |       |
| VAR1    | varchar(255) | YES  |     | NULL    |       |
| VAR2    | varchar(255) | YES  |     | NULL    |       |
| VAR3    | varchar(255) | YES  |     | NULL    |       |
| VAR4    | varchar(255) | YES  |     | NULL    |       |
+---------+--------------+------+-----+---------+-------+
6 rows in set (0.05 sec)
 * 
 * @author <a href="mailto:120077407@qq.com">hwz</a>
 */
@SuppressWarnings("serial")
public class CallTaskAuth extends Model<CallTaskAuth> {
	
	public static CallTaskAuth dao = new CallTaskAuth();
	
	/**
	 * 根据taskId查询记录
	 * 
	 * @param taskId
	 * @return
	 */
	public List<Record> getCallTaskAuthByTaskId(String taskId) {
		
		String sql = "select * from call_task_auth where CT_ID=?";
		
		List<Record> list = Db.find(sql, taskId);
		
		return list;
	}
	
	/**
	 * 根据taskId查询记录
	 * 
	 * @param taskId
	 * @return
	 */
	public List<Record> getCallTaskAuthByOperId(String operId) {
		
		String sql = "select * from call_task_auth where OPER_ID=?";
		
		List<Record> list = Db.find(sql, operId);
		
		return list;
	}
	
	/**
	 * 授权
	 * @param list
	 * @return
	 */
	public int batchSave(List<Record> list) {
		String sql = "insert into call_task_auth(OPER_ID,CT_ID) values(?,?)";
		
		int[] successData = Db.batch(sql, "OPER_ID,CT_ID", list, 20);
		
		return successData.length;
	}
	
	/**
	 * 批量添加授权，根据操作员ID字串，及任务ID
	 * @param operIds
	 * @param taskId
	 * @return
	 */
	public int batchSave(String operIds,String taskId) {
		
		if(BlankUtils.isBlank(operIds) || BlankUtils.isBlank(taskId)) {
			return 0;
		}
		
		List<Record> list = new ArrayList<Record> ();
		String[] idList = operIds.split(",");
		
		for(String id:idList) {
			if(id.equalsIgnoreCase("-1")) {
				continue;
			}
			
			Record r = new Record();
			r.set("OPER_ID", id);
			r.set("CT_ID", taskId);
			list.add(r);
		}
		
		return batchSave(list);
	}
	
	/**
	 * 取消全部授权
	 * @param taskId
	 * @return
	 */
	public int cancelAuth(String taskId) {
		
		String sql = "delete from call_task_auth where CT_ID=?";
		
		int count = Db.update(sql, taskId);
		
		return count;
	}
	
	
}
