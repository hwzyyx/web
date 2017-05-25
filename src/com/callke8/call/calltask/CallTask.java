package com.callke8.call.calltask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.callke8.astutils.CallTaskCounterUtils;
import com.callke8.call.calltelephone.CallTelephone;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

@SuppressWarnings("serial")
public class CallTask extends Model<CallTask> {
	
	public static CallTask dao = new CallTask();
	
	/**
	 * 新增外呼任务
	 * @param callTask
	 * @return
	 */
	public int add(Record callTask) {
		
		int id = 0;
		boolean b = Db.save("call_task", "CT_ID", callTask);
		
		if(b) {
			id = Integer.valueOf(callTask.get("CT_ID").toString());
		}
		
		return id;
	}
	
	
	/**
	 * 修改
	 * 
	 * @param ct
	 * @return
	 */
	public boolean update(CallTask ct) {
		boolean b = false;
		int ctId = Integer.valueOf(ct.get("CT_ID").toString());
		String taskName = ct.get("TASK_NAME").toString();
		String callerId = ct.get("CALLERID").toString();
		
		String sql = "update call_task set TASK_NAME=?,CALLERID=? where CT_ID=?";
		
		int count = Db.update(sql, taskName,callerId,ctId);
		
		if(count>0){   //修改数量大于0时
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据传入的任务id, 及状态值，修改任务的状态
	 * 
	 * @param taskId
	 * @param state
	 * @return
	 */
	public boolean updateState(int taskId,String state) {
		boolean b = false;
		String sql = "update call_task set TASK_STATE=? where CT_ID=?";
		
		int count = Db.update(sql, state,taskId);
		
		if(count>0) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 根据任务ID,返回任务的状态
	 * 
	 * @param taskId
	 * @return
	 */
	public String getTaskStateByTaskId(int taskId) {
		
		String sql = "select TASK_STATE from call_task where CT_ID=?";
		
		Record record = Db.findFirst(sql, taskId);
		
		if(BlankUtils.isBlank(record)) {
			return null;
		}
		
		return record.get("TASK_STATE");
		
	}
	
	/**
	 * 根据 taskId 删除外呼任务
	 * @param taskId
	 * @return
	 */
	public boolean delete(int taskId) {
		boolean b = false;
		//先删除当前任务的号码
		int count = CallTelephone.dao.deleteByTaskId(taskId);
		
		System.out.println("删除任务 " + taskId + " 共删除任务号码数量：" + count);
		
		String sql = "delete from call_task where CT_ID=?";
		int count2 = Db.update(sql,taskId);
		
		if(count2>0) {
			//删除任务成功后，需要将该任务的计数器删除
			CallTaskCounterUtils.deleteByTaskId(taskId);      //删除计数数据
			
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据条件查询分页
	 * @param currentPage
	 * @param numPerPage
	 * @param taskName
	 * @param taskType
	 * @param taskState
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Page<Record> getCallTaskByPaginate(int currentPage,int numPerPage,String taskName,String taskType,String taskState,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("from call_task where 1=1");
		
		if(!BlankUtils.isBlank(taskName)) {
			sb.append(" and TASK_NAME like ?");
			pars[index] = "%" + taskName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(taskType)) {
			sb.append(" and TASK_TYPE=?");
			pars[index] = taskType;
			index++;
		}
		
		if(!BlankUtils.isBlank(taskState) && !taskState.equalsIgnoreCase("5")) {
			sb.append(" and TASK_STATE=?");
			pars[index] = taskState;
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CREATE_TIME>=?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CREATE_TIME<=?");
			pars[index] = endTime + " 23:59:59";
			index++;
		}
		
		Page<Record> page = Db.paginate(currentPage, numPerPage, "select *", sb.toString() + " ORDER BY CREATE_TIME DESC", ArrayUtils.copyArray(index, pars));
		
		return page;
	}
	
	/**
	 * 根据条件查询分页，并转为 Map 
	 * @param currentPage
	 * @param numPerPage
	 * @param taskName
	 * @param taskType
	 * @param taskState
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map getCallTaskByPaginateToMap(int currentPage,int numPerPage,String taskName,String taskType,String taskState,String startTime,String endTime){
		
		Page<Record> page = getCallTaskByPaginate(currentPage,numPerPage,taskName,taskType,taskState,startTime,endTime);
		
		int total = page.getTotalRow();
		
		Map m = new HashMap();
		m.put("total", total);
		//m.put("rows", page.getList());
		List<Record> list = page.getList();    //需要将每条任务的记数情况加入
		List<Record> newList = new ArrayList<Record>();    //新建一个 list
		
		for(Record r:list) { 
			
			int taskId = r.getInt("CT_ID");   
			
			Map<String,Integer> stateMap = CallTaskCounter.dao.getCounterByTaskId(taskId);
			
			//遍历 map, 并根据情况修改任务的计数
			Iterator<Map.Entry<String, Integer>> entries = stateMap.entrySet().iterator();
			while(entries.hasNext()) {
				
				Map.Entry<String, Integer> entry = entries.next(); 
				
				String state = entry.getKey();        //得到状态
				Integer count = entry.getValue();      //得到当关状态的数量
			
				if(state.equalsIgnoreCase("total")) {
					r.set("TOTAL", count);
				}else if(state.equalsIgnoreCase("1")) {
					r.set("DISTRIBUTION",count);
				}else if(state.equalsIgnoreCase("0")) {
					r.set("UNDISTRIBUTION",count);
				}
			}
			
			if(BlankUtils.isBlank(r.get("TOTAL"))) { r.set("TOTAL", 0);};
			if(BlankUtils.isBlank(r.get("DISTRIBUTION"))) { r.set("DISTRIBUTION", 0);};
			if(BlankUtils.isBlank(r.get("UNDISTRIBUTION"))) { r.set("UNDISTRIBUTION", 0);};
			
			newList.add(r);
			
		}
		m.put("rows", newList);                      //将重新组织的号码加入
		
		return m;
	}
	
	/**
	 * 根据当前登录的工号的授权情况显示外呼任务
	 * 
	 * @param currentPage
	 * @param numPerPage
	 * @param taskName
	 * @param taskType
	 * @param taskState
	 * @param startTime
	 * @param endTime
	 * @param operId
	 * 			当前登录的工号
	 * @return
	 */
	public Map getCallTaskByPaginateToMap4Auth(int currentPage,int numPerPage,String taskName,String taskType,String taskState,String startTime,String endTime,String operId) {
		Page<Record> page = getCallTaskByPaginate4Auth(currentPage,numPerPage,taskName,taskType,taskState,startTime,endTime,operId);
		
		int total = page.getTotalRow();
		
		Map m = new HashMap();
		m.put("total", total);
		m.put("rows", page.getList());
		
		return m;
	}
	
	/**
	 * 根据条件查询分页
	 * @param currentPage
	 * @param numPerPage
	 * @param taskName
	 * @param taskType
	 * @param taskState
	 * @param startTime
	 * @param endTime
	 * @param operId
	 * 			当前登录的工号
	 * @return
	 */
	public Page<Record> getCallTaskByPaginate4Auth(int currentPage,int numPerPage,String taskName,String taskType,String taskState,String startTime,String endTime,String operId) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[6];
		int index = 0;
		sb.append("from call_task where 1=1");
		
		sb.append(" and CT_ID in(" + getTaskIdByOperId(operId) + ")");
		
		if(!BlankUtils.isBlank(taskName)) {
			sb.append(" and TASK_NAME like ?");
			pars[index] = "%" + taskName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(taskType)) {
			sb.append(" and TASK_TYPE=?");
			pars[index] = taskType;
			index++;
		}
		
		if(!BlankUtils.isBlank(taskState) && !taskState.equalsIgnoreCase("5")) {
			sb.append(" and TASK_STATE=?");
			pars[index] = taskState;
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CREATE_TIME>=?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CREATE_TIME<=?");
			pars[index] = endTime + " 23:59:59";
			index++;
		}
		
		Page<Record> page = Db.paginate(currentPage, numPerPage, "select *", sb.toString() + " ORDER BY CREATE_TIME DESC", ArrayUtils.copyArray(index, pars));
		
		return page;
	}
	
	/**
	 * 根据当前登录的工号，查询已经授权的任务ID，并拼接成字符串返回
	 * 
	 * @param operId
	 * @return
	 */
	public String getTaskIdByOperId(String operId) {
		
		String ids = "";
		
		List<Record> list = CallTaskAuth.dao.getCallTaskAuthByOperId(operId);
		
		for(Record r:list) {
			ids += r.get("CT_ID") + ",";
		}
		
		//最后，要将最后一个逗号去掉
		if(!BlankUtils.isBlank(ids)) {
			ids = ids.substring(0,ids.length()-1);
		}
		
		return ids;
	}
	
	/**
	 * 得到所有已经活跃的任务
	 * 
	 * 状态为：新建(状态为0)、已启动(状态为1)、暂停(状态为2)、已停止(状态为3)的任务
	 * 
	 * @return
	 */
	public List<Record> getAllActiveTask() {
		
		String sql = "select * from call_task where TASK_STATE<=3";    //仅显示新建(状态为0)、已启动(状态为1)、暂停(状态为2)、已停止(状态为3)的任务
		
		List<Record> list = Db.find(sql);
		
		return list;
	}
	
	
}
