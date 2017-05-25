package com.callke8.autocall.autocalltask.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AutoCallTaskTelephoneHistory extends Model<AutoCallTaskTelephoneHistory> {
	
	private static final long serialVersionUID = 1L;
	
	public static AutoCallTaskTelephoneHistory dao = new AutoCallTaskTelephoneHistory();
	
	/**
	 * 以分页的方式获取数据
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param taskId
	 * @param telephone
	 * @param clientName
	 * @return
	 */
	public Page<Record> getAutoCallTaskTelephoneByPaginate(int pageNumber,int pageSize,String taskId,String telephone,String clientName) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[6];
		int index = 0;
		
		sb.append("from ac_call_task_telephone where 1=1");
		
		if(!BlankUtils.isBlank(taskId)) {   //任务ID不为空
			
			sb.append(" and TASK_ID=?");
			pars[index] = taskId;
			index++;
		}
		
		if(!BlankUtils.isBlank(telephone)) {
			sb.append(" and TELEPHONE like ?");
			pars[index] = "%" + telephone + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(clientName)) {
			sb.append(" and CLIENT_NAME like ?");
			pars[index] = "%" + clientName + "%";
			index++;
		}
		
		Page<Record> page = Db.paginate(pageNumber,pageSize,"select *",sb.toString() + " ORDER BY TEL_ID DESC",ArrayUtils.copyArray(index, pars));
		
		return page;
	}
	
	
	/**
	 * 得到分页数据并以Map 返回
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param taskId
	 * @param telephone
	 * @param clientName
	 * @return
	 */
	public Map<String,Object> getAutoCallTaskTelephoneByPaginateToMap(int pageNumber,int pageSize,String taskId,String telephone,String clientName) {
		
		Map<String,Object> m = new HashMap<String,Object>();
		
		if(BlankUtils.isBlank(taskId)) {
			m.put("total", 0);
			m.put("rows", new ArrayList<Record>());
		}
		
		Page<Record> page = getAutoCallTaskTelephoneByPaginate(pageNumber, pageSize, taskId, telephone, clientName);
		
		int total = page.getTotalRow();
		m.put("total", total);
		m.put("rows", page.getList());
		
		return m;
	}
	
	/**
	 * 根据外呼任务ID,得到号码的数量
	 * 
	 * @param taskId
	 * @return
	 */
	public int getTelephoneCountByTaskId(String taskId) {
		
		String sql = "select count(*) as count from ac_call_task_telephone where TASK_ID=?";
		
		Record r = Db.findFirst(sql, taskId);
		
		return Integer.valueOf(r.get("count").toString());
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
