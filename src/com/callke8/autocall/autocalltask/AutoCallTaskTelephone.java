package com.callke8.autocall.autocalltask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.common.CommonController;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AutoCallTaskTelephone extends Model<AutoCallTaskTelephone> {
	
	private static final long serialVersionUID = 1L;
	
	public static AutoCallTaskTelephone dao = new AutoCallTaskTelephone();
	
	AutoCallTaskTelephone prev;
	AutoCallTaskTelephone next;
	
	/**
	 * 以分页的方式获取数据
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param taskId
	 * @param customerTel
	 * @param customerName
	 * @param state
	 * @return
	 */
	public Page<Record> getAutoCallTaskTelephoneByPaginate(int pageNumber,int pageSize,String taskId,String customerTel,String customerName,String state,String lastCallResult,String messageState,String createTimeStartTime,String createTimeEndTime,String loadTimeStartTime,String loadTimeEndTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[12];
		int index = 0;
		
		sb.append("from ac_call_task_telephone where 1=1");
		
		if(!BlankUtils.isBlank(taskId)) {   //任务ID不为空
			
			sb.append(" and TASK_ID=?");
			pars[index] = taskId;
			index++;
		}
		
		if(!BlankUtils.isBlank(customerTel)) {
			sb.append(" and CUSTOMER_TEL like ?");
			pars[index] = "%" + customerTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(customerName)) {
			sb.append(" and CUSTOMER_NAME like ?");
			pars[index] = "%" + customerName + "%";
			index++;
		}
		
		/**
		 * 传入的 state 有可能是包括种状态，以逗号分隔，
		 */
		if(!BlankUtils.isBlank(state) && !state.equals("5")) {
			if(state.contains(",")) {   //如果状态有逗号分隔
				sb.append(" and STATE in(" + state + ")");
			}else {
				sb.append(" and STATE=?");
				pars[index] = state;
				index++;
			}
		}
		
		if(!BlankUtils.isBlank(lastCallResult) && !lastCallResult.equals("empty")) {
			sb.append(" and LAST_CALL_RESULT=?");
			pars[index] = lastCallResult;
			index++;
		}
		
		if(!BlankUtils.isBlank(messageState) && !messageState.equalsIgnoreCase("empty")) {
			sb.append(" and MESSAGE_STATE=?");
			pars[index] = messageState;
			index++;
		}
		
		//创建的开始时间查询
		if(!BlankUtils.isBlank(createTimeStartTime)) {
			sb.append(" and CREATE_TIME>?");
			pars[index] = createTimeStartTime;
			index++;
		}
		
		//创建的结束时间查询
		if(!BlankUtils.isBlank(createTimeEndTime)) {
			sb.append(" and CREATE_TIME<?");
			pars[index] = createTimeEndTime;
			index++;
		}
		
		//外呼时间的开始时间查询
		if(!BlankUtils.isBlank(loadTimeStartTime)) {
			sb.append(" and LOAD_TIME>?");
			pars[index] = loadTimeStartTime;
			index++;
		}
		
		//外呼时间的结束时间查询
		if(!BlankUtils.isBlank(loadTimeEndTime)) {
			sb.append(" and LOAD_TIME<?");
			pars[index] = loadTimeEndTime;
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
	 * @param customerTel
	 * @param customerName
	 * @param state
	 * @return
	 */
	public Map<String,Object> getAutoCallTaskTelephoneByPaginateToMap(int pageNumber,int pageSize,String taskId,String customerTel,String customerName,String state,String lastCallResult,String messageState,String createTimeStartTime,String createTimeEndTime,String loadTimeStartTime,String loadTimeEndTime) {
		
		Map<String,Object> m = new HashMap<String,Object>();
		AutoCallTask autoCallTask = null;
		int retryTimes = 0;
		
		if(BlankUtils.isBlank(taskId)) {
			m.put("total", 0);
			m.put("rows", new ArrayList<Record>());
			
			return m;
		}else {
			autoCallTask = AutoCallTask.dao.getAutoCallTaskByTaskId(taskId);    //取得任务的信息
			retryTimes = autoCallTask.getInt("RETRY_TIMES");
		}
		
		Page<Record> page = getAutoCallTaskTelephoneByPaginate(pageNumber, pageSize, taskId, customerTel, customerName,state,lastCallResult,messageState,createTimeStartTime,createTimeEndTime,loadTimeStartTime,loadTimeEndTime);
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:page.getList()) {
			int retried = r.getInt("RETRIED");
			r.set("RETRIED_DESC",retried + "/" + retryTimes);
			
			String lastCallResultRs = r.getStr("LAST_CALL_RESULT");
			String lastCallResultDesc = MemoryVariableUtil.getDictName("LAST_CALL_RESULT",lastCallResultRs);
			r.set("LAST_CALL_RESULT_DESC", lastCallResultDesc);
			
			String messageStateRs = String.valueOf(r.getInt("MESSAGE_STATE"));
			String messageStateDesc = MemoryVariableUtil.getDictName("COMMON_MESSAGE_STATE", messageStateRs);
			r.set("MESSAGE_STATE_DESC", messageStateDesc);
			
			newList.add(r);
		}
		
		int total = page.getTotalRow();
		m.put("total", total);
		m.put("rows",newList);
		return m;
	}
	
	/**
	 * 单个添加记录
	 * 
	 * @param autoCallTaskTelephone
	 * @return
	 */
	public boolean add(Record autoCallTaskTelephone) {
		
		boolean b = Db.save("ac_call_task_telephone", autoCallTaskTelephone);
		
		return b;
	}
	
	/**
	 * 批量添加记录
	 * 
	 * @param autoCallTaskTelephones
	 * @return
	 */
	public int add(ArrayList<Record> autoCallTaskTelephones) {
		
		int successCount = 0;
		
		for(Record tel:autoCallTaskTelephones) {
			boolean b = add(tel);
			if(b) {
				successCount++;
			}
		}
		
		return successCount;
	}
	
	
	/**
	 * 根据外呼任务ID，删除所有号码
	 * 
	 * @param taskId
	 * @return
	 */
	public int deleteByTaskId(String taskId) {
		
		String sql = "delete from ac_call_task_telephone where TASK_ID=?";
		
		int count = Db.update(sql,taskId);
		
		return count ;
		
	}
	
	
	/**
	 * 批量删除号码
	 * 
	 * @param ids
	 * @return
	 */
	public int batchDelete(String ids) {
		
		if(BlankUtils.isBlank(ids)) {
			return 0;
		}
		
		ArrayList<Record> list = new ArrayList<Record>();
		
		String[] idList = ids.split(",");   //以逗号分隔
		
		for(String id:idList) {
			Record tel = new Record();
			tel.set("TEL_ID", id);
			list.add(tel);
		}
		
		String sql = "delete from ac_call_task_telephone where TEL_ID=?";
		
		int[] delData = Db.batch(sql,"TEL_ID",list,200);
		
		return delData.length;
		
	}
	
	
	/**
	 * 批量添加数据
	 * 
	 * @param telephones
	 * @return
	 */
	public int batchSave(ArrayList<Record> telephones) {
		
		if(BlankUtils.isBlank(telephones) || telephones.size()==0) {
			return 0;
		}
		
		String sql = "insert into ac_call_task_telephone(TASK_ID,CUSTOMER_TEL,CUSTOMER_NAME,CREATE_TIME,RETRIED,STATE,PERIOD,DISPLAY_NUMBER,DOSAGE,CHARGE,ACCOUNT_NUMBER,ADDRESS,CALL_POLICE_TEL,VEHICLE_TYPE,PLATE_NUMBER,ILLEGAL_CITY,PUNISHMENT_UNIT,ILLEGAL_REASON,COMPANY)value(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		int[] insertData = Db.batch(sql, "TASK_ID,CUSTOMER_TEL,CUSTOMER_NAME,CREATE_TIME,RETRIED,STATE,PERIOD,DISPLAY_NUMBER,DOSAGE,CHARGE,ACCOUNT_NUMBER,ADDRESS,CALL_POLICE_TEL,VEHICLE_TYPE,PLATE_NUMBER,ILLEGAL_CITY,PUNISHMENT_UNIT,ILLEGAL_REASON,COMPANY",telephones,5000);
		
		return insertData.length;
	}
	
	public boolean update(String customerTel,String customerName,String period,String displayNumber,String dosage,String charge,String accountNumber,String address,String callPoliceTel,String vehicleType,String plateNumber,String illegalCity,String punishmentUnit,String illegalReason,String company,int telId) {
		
		boolean b = false;
		
		String sql = "update ac_call_task_telephone set CUSTOMER_TEL=?,CUSTOMER_NAME=?,PERIOD=?,DISPLAY_NUMBER=?,DOSAGE=?,CHARGE=?,ACCOUNT_NUMBER=?,ADDRESS=?,CALL_POLICE_TEL=?,VEHICLE_TYPE=?,PLATE_NUMBER=?,ILLEGAL_CITY=?,PUNISHMENT_UNIT=?,ILLEGAL_REASON=?,COMPANY=? where TEL_ID=?";
		
		int count = Db.update(sql,customerTel,customerName,period,displayNumber,dosage,charge,accountNumber,address,callPoliceTel,vehicleType,plateNumber,illegalCity,punishmentUnit,illegalReason,company,telId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
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
	
	/**
	 * 根据任务和状态集，查找在状态集内该任务的数量
	 * 
	 * 即是传入任务ID,及多个状态集（用逗号分隔），查找数量
	 * 
	 * 传入的 states 如： 1,2,3 字符串
	 * 
	 * @param taskId
	 * 			任务ID
	 * @param states
	 * 			状态集，用逗号分隔
	 * @return
	 */
	public int getTelephoneCountByInStates(String taskId,String states) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("select count(*) as count from ac_call_task_telephone where 1=1");
		
		if(!BlankUtils.isBlank(taskId)) {
			sb.append(" and TASK_ID=?");
			pars[index] = taskId;
			index++;
		}
		
		if(!BlankUtils.isBlank(states)) {
			String[] stateArr = states.split(",");
			String markN = "";    //问号的数量
			for(String s:stateArr) {
				markN += "?,";
				pars[index] = Integer.valueOf(s);
				index++;
			}
			
			//将 markN的最后一个逗号去掉
			markN = markN.substring(0, markN.length() - 1);
			
			//拼接sql语句
			sb.append(" and STATE in(" + markN + ")");
		}
		
		Record r = Db.findFirst(sb.toString(),ArrayUtils.copyArray(index, pars));
		//System.out.println("查询的SQL语句:" + sb.toString());
		
		return Integer.valueOf(r.get("count").toString());
		
	}
	
	/**
	 * 根据任务和状态集，查找不在状态集内该任务的数量
	 * 
	 * 即是传入任务ID,及多个状态集（用逗号分隔），查找非这些状态的数量。
	 * 
	 * 传入的 states 如： 1,2,3 字符串
	 * 
	 * @param taskId
	 * 			任务ID
	 * @param states
	 * 			状态集，用逗号分隔
	 * @return
	 */
	public int getTelephoneCountByNotInState(String taskId,String states) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("select count(*) as count from ac_call_task_telephone where 1=1");
		
		if(!BlankUtils.isBlank(taskId)) {
			sb.append(" and TASK_ID=?");
			pars[index] = taskId;
			index++;
		}
		
		if(!BlankUtils.isBlank(states)) {
			String[] stateArr = states.split(",");
			String markN = "";    //问号的数量
			for(String s:stateArr) {
				markN += "?,";
				pars[index] = Integer.valueOf(s);
				index++;
			}
			
			//将 markN的最后一个逗号去掉
			markN = markN.substring(0, markN.length() - 1);
			
			//拼接sql语句
			sb.append(" and STATE not in(" + markN + ")");
		}
		
		Record r = Db.findFirst(sb.toString(),ArrayUtils.copyArray(index, pars));
		
		//System.out.println("查询的SQL语句:" + sb.toString());
		
		return Integer.valueOf(r.get("count").toString());
	}
	
	/**
	 * 根据外呼任务ID及状态,取得数据列表
	 * 
	 * state:状态    0：未处理; 1：已载入; 2：已成功; 3：待重呼; 4:已失败    （可以为空）
	 * telephone: 查看某些号码的情况
	 * 
	 * @param taskId
	 * @param state
	 * @param customerTel
	 * @param customerName
	 * @param createTimeStartTime
	 * @param createTimeEndTime
	 * @param loadTimeStartTime
	 * @param loadTimeEndTime
	 * @param retryTimes
	 * 				外呼任务设定的重试次数
	 * @return
	 */
	public List<Record> getAutoCallTaskTelephonesByTaskIdAndState(String taskId,String state,String lastCallResult,String messageState,String customerTel,String customerName,String createTimeStartTime,String createTimeEndTime,String loadTimeStartTime,String loadTimeEndTime,int retryTimes) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0; 
		
		sb.append("select * from ac_call_task_telephone where 1=1 ");
		
		if(!BlankUtils.isBlank(taskId)) {
			sb.append(" and TASK_ID=?");
			pars[index] = taskId;
			index++;
		}
		
		/**
		 * 传入的 state 有可能是包括种状态，以逗号分隔，
		 */
		if(!BlankUtils.isBlank(state)) {
			if(state.contains(",")) {   //如果状态有逗号分隔
				sb.append(" and STATE in(" + state + ")");
			}else {
				sb.append(" and STATE=?");
				pars[index] = state;
				index++;
			}
		}
		
		if(!BlankUtils.isBlank(lastCallResult)) {
			sb.append(" and LAST_CALL_RESULT=?");
			pars[index] = lastCallResult;
			index++;
		}
		
		if(!BlankUtils.isBlank(messageState) && !messageState.equalsIgnoreCase("empty")) {
			sb.append(" and MESSAGE_STATE=?");
			pars[index] = messageState;
			index++;
		}
		
		if(!BlankUtils.isBlank(customerTel)) {
			sb.append(" and CUSTOMER_TEL like ?");
			pars[index] = "%" + customerTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(customerName)) {
			sb.append(" and CUSTOMER_NAME like ?");
			pars[index] = "%" + customerName + "%";
			index++;
		}
		
		//创建的开始时间查询
		if(!BlankUtils.isBlank(createTimeStartTime)) {
			sb.append(" and CREATE_TIME>?");
			pars[index] = createTimeStartTime;
			index++;
		}
		
		//创建的结束时间查询
		if(!BlankUtils.isBlank(createTimeEndTime)) {
			sb.append(" and CREATE_TIME<?");
			pars[index] = createTimeEndTime;
			index++;
		}
		
		//外呼时间的开始时间查询
		if(!BlankUtils.isBlank(loadTimeStartTime)) {
			sb.append(" and LOAD_TIME>?");
			pars[index] = loadTimeStartTime;
			index++;
		}
		
		//外呼时间的结束时间查询
		if(!BlankUtils.isBlank(loadTimeEndTime)) {
			sb.append(" and LOAD_TIME<?");
			pars[index] = loadTimeEndTime;
			index++;
		}
		
		sb.append(" ORDER BY TEL_ID DESC");
		System.out.println("getAutoCallTaskTelephonesByTaskIdAndState的 sql 语句++++++++++++：" + sb.toString() + "------------" +  ArrayUtils.copyArray(index, pars).toString());
		List<Record> list = Db.find(sb.toString(),ArrayUtils.copyArray(index, pars));
		List<Record> newList = new ArrayList<Record>();
		
		//遍历数据，并将相关的信息
		for(Record r:list) {
			
			//呼叫总数
			String callState = String.valueOf(r.getInt("STATE"));   //取出状态
			int retried = r.getInt("RETRIED");
			r.set("RETRIED_DESC",retried + "/" + retryTimes);
			
			String lastCallResultRs = r.getStr("LAST_CALL_RESULT");
			String lastCallResultDesc = MemoryVariableUtil.getDictName("LAST_CALL_RESULT", lastCallResultRs);
			r.set("LAST_CALL_RESULT_DESC", lastCallResultDesc);
			
			if(!BlankUtils.isBlank(callState)) {
				
				if(callState.equals("0")) {
					r.set("STATE_DESC","未处理");
					r.set("OP_TIME","");
					r.set("LAST_CALL_RESULT","");
					r.set("NEXT_CALLOUT_TIME","");
				}else if(callState.equals("1")) {
					r.set("STATE_DESC","已载入");
					r.set("OP_TIME",r.get("LOAD_TIME"));
					r.set("LAST_CALL_RESULT","");
					r.set("NEXT_CALLOUT_TIME","");
				}else if(callState.equals("2")) {
					r.set("STATE_DESC","已成功");
					r.set("LAST_CALL_RESULT","");
					r.set("NEXT_CALLOUT_TIME","");
				}else if(callState.equals("3")) {
					r.set("STATE_DESC","待重呼");
				}else if(callState.equals("4")) {
					r.set("STATE_DESC","已失败");
					r.set("NEXT_CALLOUT_TIME","");
				}
				
				String messageStateRs = String.valueOf(r.getInt("MESSAGE_STATE"));
				String messageStateDesc = MemoryVariableUtil.getDictName("COMMON_MESSAGE_STATE", messageStateRs);
				r.set("MESSAGE_STATE_DESC", messageStateDesc);
				
				newList.add(r);
			}
			
		}
		
		return newList;
		
	}
	
	
	/**
	 * 归档外呼任务的号码到历史任务
	 * 
	 * @return
	 */
	public boolean archiveAutoCallTaskTelephone(String taskId) {
		
		boolean b = false;
		
		String sql = "insert into ac_call_task_telephone_history select * from ac_call_task_telephone where TASK_ID=?";
		
		int count = Db.update(sql, taskId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 修改号码的状态
	 * 
	 * @param telId
	 * 			telId 号码ID，如果为空时修改所有的号码状态
	 * @param oldState
	 * 			oldState 旧状态,如果为空时,即不用判断原状态
	 * @param newState
	 * 			newState 新状态,不能为空
	 * @param lastCallResult
	 * 			最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * 			可以为空,为空时，将不更改其最后一次外呼结果
	 * @return
	 */
	public int updateAutoCallTaskTelephoneState(int telId,String oldState,String newState,String lastCallResult) {
		
		int count = 0;
		
		if(BlankUtils.isBlank(newState)) {
			return count;
		}
		
		if(telId > 0 && !BlankUtils.isBlank(oldState)) {   //两者都不为空时
			if(BlankUtils.isBlank(lastCallResult)) {
				String sql = "update ac_call_task_telephone set STATE=? where TEL_ID=? and STATE=?";
			
				count = Db.update(sql,newState,telId,oldState);
			}else {
				String sql = "update ac_call_task_telephone set STATE=?,LAST_CALL_RESULT=? where TEL_ID=? and STATE=?";
				count = Db.update(sql,newState,lastCallResult,telId,oldState);
			}
			
			return count;
		}else if(telId>0 && BlankUtils.isBlank(oldState)) {   //如果号码ID不为空,但是旧状态为空时
			
			if(BlankUtils.isBlank(lastCallResult)) {
				String sql = "update ac_call_task_telephone set STATE=? where TEL_ID=?";
			
				count = Db.update(sql,newState,telId);
			}else {
				String sql = "update ac_call_task_telephone set STATE=?,LAST_CALL_RESULT=? where TEL_ID=?";
				
				count = Db.update(sql,newState,lastCallResult,telId);
			}
			
			return count;
		}else if(telId<=0 && !BlankUtils.isBlank(oldState)) {  //号码为空,但旧状态不为空时
			
			if(BlankUtils.isBlank(lastCallResult)) {
				String sql = "update ac_call_task_telephone set STATE=? where STATE=?";
			
				count = Db.update(sql,newState,oldState);
			}else {
				String sql = "update ac_call_task_telephone set STATE=?,LAST_CALL_RESULT=? where STATE=?";
				
				count = Db.update(sql,newState,lastCallResult,oldState);
			}
		}
		
		return count;
		
	}
	
	/**
	 * telId
	 * 			telId 号码ID，如果为空时修改所有的号码状态
	 * @param oldState
	 * 			oldState 旧状态,如果为空时,即不用判断原状态
	 * @param newState
	 * 			newState 新状态,不能为空
	 * @param lastCallResult
	 * 			外呼状态：1（onSuccess）;2(onNoAnswer);3(onBusy);4(onFailure)
	 * @param hangupCause
	 * 			挂机原因：根据通道返回chnanel.getHangupCause 或是其他的错误原因
	 * @return
	 */
	public int updateAutoCallTaskTelephoneState(int telId,String oldState,String newState,String lastCallResult,String hangupCause) {
		
		int count = 0;
		if(BlankUtils.isBlank(newState)) {
			return count;
		}
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;
		
		sb.append("update ac_call_task_telephone set ");
		
		if(!BlankUtils.isBlank(newState)) {
			sb.append("STATE=?");
			pars[index] = newState;
			index++;
		}
		
		if(!BlankUtils.isBlank(lastCallResult)) {
			sb.append(",LAST_CALL_RESULT=?");
			pars[index] = lastCallResult;
			index++;
		}
		
		if(!BlankUtils.isBlank(hangupCause)) {
			sb.append(",HANGUP_CAUSE=?");
			pars[index] = lastCallResult;
			index++;
		}
		
		sb.append(" where ");
		
		if(telId>0) {
			sb.append("TEL_ID=?");
			pars[index] = telId;
			index++;
		}
		
		if(!BlankUtils.isBlank(oldState)) {
			sb.append(" and STATE=?");
			pars[index] = oldState;
			index++;
		}
		
		count = Db.update(sb.toString(),ArrayUtils.copyArray(index, pars));
		
		return count;
		
	}
	
	/**
	 * 更改外呼号码为重试
	 * 
	 * @param telId
	 * 			号码ID
	 * @param newState
	 * 			新的状态（重试状态为3）
	 * @param retryInterval   
	 * 			重试间隔，单位分钟
	 * @param lastCallResult
	 * 			最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * @return
	 */
	public boolean updateAutoCallTaskTelephoneStateToRetry(int telId,String newState,int retryInterval,int intervalType,String lastCallResult) {
		
		boolean b = false;
		
		if(telId <= 0 && BlankUtils.isBlank(newState) && retryInterval<=0) {
			return false;
		}
		
		long currTimeMillis = DateFormatUtils.getTimeMillis();   			 //当前时间的毫秒数
		long retryTimeMillis = 0;
		if(intervalType==1) {   		//分钟
			retryTimeMillis = currTimeMillis + retryInterval * 60 * 1000;   			//重试时的毫秒数
		}else if(intervalType==2) {		//小时
			retryTimeMillis = currTimeMillis + retryInterval * 60 * 60 * 1000;   		//重试时的毫秒数
		}else if(intervalType==3) {     //天
			retryTimeMillis = currTimeMillis + retryInterval * 24 * 60 * 60 * 1000;   	//重试时的毫秒数
		}else {                         //否则，默认为分钟
			retryTimeMillis = currTimeMillis + retryInterval * 60 * 1000;   			//重试时的毫秒数
		}
		
		String nextCallOutTime = DateFormatUtils.formatDateTime(new Date(retryTimeMillis),"yyyy-MM-dd HH:mm:ss");
		
		String sql = "update ac_call_task_telephone set STATE=?,NEXT_CALLOUT_TIME=?,LAST_CALL_RESULT=? where TEL_ID=?";
		
		int count = Db.update(sql,newState,nextCallOutTime,lastCallResult,telId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 更改外呼号码为重试
	 * 
	 * @param telId
	 * 			号码ID
	 * @param newState
	 * 			新的状态（重试状态为3）
	 * @param retryInterval   
	 * 			重试间隔，单位分钟
	* @param lastCallResult
	 * 			外呼状态：1（onSuccess）;2(onNoAnswer);3(onBusy);4(onFailure)
	 * @param hangupCause
	 * 			挂机原因：根据通道返回chnanel.getHangupCause 或是其他的错误原因
	 * @return
	 */
	public boolean updateAutoCallTaskTelephoneStateToRetry(int telId,String newState,int retryInterval,int intervalType,String lastCallResult,String hangupCause) {
		
		boolean b = false;
		
		if(telId <= 0 && BlankUtils.isBlank(newState) && retryInterval<=0) {
			return false;
		}
		
		long currTimeMillis = DateFormatUtils.getTimeMillis();   			 //当前时间的毫秒数
		long retryTimeMillis = 0;
		if(intervalType==1) {   		//分钟
			retryTimeMillis = currTimeMillis + retryInterval * 60 * 1000;   			//重试时的毫秒数
		}else if(intervalType==2) {		//小时
			retryTimeMillis = currTimeMillis + retryInterval * 60 * 60 * 1000;   		//重试时的毫秒数
		}else if(intervalType==3) {     //天
			retryTimeMillis = currTimeMillis + retryInterval * 24 * 60 * 60 * 1000;   	//重试时的毫秒数
		}else {                         //否则，默认为分钟
			retryTimeMillis = currTimeMillis + retryInterval * 60 * 1000;   			//重试时的毫秒数
		}
		
		String nextCallOutTime = DateFormatUtils.formatDateTime(new Date(retryTimeMillis),"yyyy-MM-dd HH:mm:ss");
		
		String sql = "update ac_call_task_telephone set STATE=?,NEXT_CALLOUT_TIME=?,LAST_CALL_RESULT=?,HANGUP_CAUSE=? where TEL_ID=?";
		
		int count = Db.update(sql,newState,nextCallOutTime,lastCallResult,hangupCause,telId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 更改客户号码的归属地和外呼号码
	 * 
	 * @param telId
	 * @param province
	 * @param city
	 * @param callOutTel
	 * @return
	 */
	public int updateAutoCallTaskTelephoneLocationAndCallOutTel(int telId,String province,String city,String callOutTel,String callerId) {
		
		int count = 0;
		
		String sql = "update ac_call_task_telephone set PROVINCE=?,CITY=?,CALLOUT_TEL=?,CALLERID=? where TEL_ID=?";
		
		count = Db.update(sql,province,city,callOutTel,callerId,telId);     
		
		return count;
	}
	
	/**
	 * 更改客户号码的归属地和外呼号码
	 * 
	 * @param telId
	 * @param province
	 * @param city
	 * @param callOutTel
	 * @return
	 */
	public int updateAutoCallTaskTelephoneLocationAndCallOutTel(int telId,String province,String city,String callOutTel) {
		
		int count = 0;
		
		String sql = "update ac_call_task_telephone set PROVINCE=?,CITY=?,CALLOUT_TEL=?,where TEL_ID=?";
		
		count = Db.update(sql,province,city,callOutTel,telId);     
		
		return count;
	}
	
	/**
	 * 更新通话时长
	 * 
	 * @param telId
	 * @param billsec
	 * @return
	 */
	public boolean updateAutoCallTaskTelephoneBillsec(int telId,int billsec) {
		
		boolean  b = false;
		
		String sql = "update ac_call_task_telephone set BILLSEC=? where TEL_ID=?";
		
		int count = Db.update(sql,billsec,telId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 更改号码记录的短信状态和状态码
	 * 
	 * @param messageState
	 * 			0:暂未下发；1：下发成功；2：下发失败；3：放弃下发
	 * @param messageFailureCode
	 * 			0：提交成功
	 * 			6：错误的消息
	 * 			10：错误的原发号码，接入码错误
	 * 			12：错误的目的号码
	 * 			15：余额不足
	 * 			16：该时间段内禁止下发
	 * 			17：签名无效
	 * 			101:非手机号码
	 * @param telId
	 * @return
	 */
	public int updateAutoCallTaskTelephoneMessageState(int messageState,String messageFailureCode,int telId) {
		
		int count = 0;
		
		String sql = "update ac_call_task_telephone set MESSAGE_STATE=?,MESSAGE_FAILURE_CODE=? where TEL_ID=?";
		
		count = Db.update(sql, messageState,messageFailureCode,telId);
		
		return count;
	}
	
	/**
	 * 当任务处理非激活状态时,系统将回滚该记录，将外呼次数减1，且状态修改为0
	 * 
	 * @param telId
	 * @return
	 */
	public int rollBackAutoCallTaskTelephoneWhenTaskNoActive(int telId) {
		
		int count = 0;
		
		String sql = "update ac_call_task_telephone set RETRIED=RETRIED-1,STATE=0 where TEL_ID=?";
		
		count = Db.update(sql,telId);
		
		return count;
	}
	
	
	/**
	 * 根据号码状态及外呼任务ID为空时,取出数量
	 * 如果外呼任务ID为空时,取出所有任务的状态的数量
	 * 
	 * @param state
	 * 			0:新建;1:已载入(即载入号码到未外呼);2:已成功;3:待重呼;4已失败
	 * @param taskId
	 * 
	 * @return
	 */
	public int getAutoCallTaskTelephoneCountByState(String state,String taskId) {
		
		if(BlankUtils.isBlank(state)) {  //如果传入的状态为空时,直接返回0
			return 0;
		}
		
		String sql = "";
		int count = 0;
		if(!BlankUtils.isBlank(taskId)) {
			sql = "select count(*) as count from ac_call_task_telephone where STATE=? and TASK_ID=?";
			
			Record r = Db.findFirst(sql, state,taskId);
			
			count = Integer.valueOf(r.get("count").toString());
			
		}else {
			sql = "select count(*) as count from ac_call_task_telephone where STATE=?";
			
			Record r = Db.findFirst(sql, state);
			
			count = Integer.valueOf(r.get("count").toString());
		}
		
		return count;
		
	}
	
	/**
	 * 根据ID，取出号码
	 * 
	 * @param telId
	 * @return
	 */
	public AutoCallTaskTelephone getAutoCallTaskTelephoneById(String telId) {
		
		String sql = "select * from ac_call_task_telephone where TEL_ID=?";
		
		AutoCallTaskTelephone autoCallTaskTelephone = findFirst(sql, telId);
		
		return autoCallTaskTelephone;
		
	}
	
	/**
	 * 根据流水号，取得外呼号码
	 * 
	 * @param serialNumber
	 * @return
	 */
	public AutoCallTaskTelephone getAutoCallTaskTelephoneBySerialNumber(String serialNumber) {
		
		String sql = "select * from ac_call_task_telephone where SERIAL_NUMBER=?";
		
		AutoCallTaskTelephone actt = findFirst(sql,serialNumber);
		
		return actt;
	}
	
	/**
	 * 根据号码的状态及外呼任务ID,取出外呼任务号码列表
	 * 
	 * @param state
	 * 			0:新建;1:已载入(即载入号码到未外呼);2:呼叫成功;3:呼叫失败;
	 * @param taskId
	 * 			如果外呼任务ID为空,则不分任务，混合取号码列表
	 * @param loadCount 
	 * 			加载的数量
	 * @param orderField
	 * 			排序字段
	 * @param orderBy
	 * 			排序方式：asc 顺序; DESC 倒序
	 * 
	 * @return
	 */
	public List<AutoCallTaskTelephone> getAutoCallTaskTelephoneByState(String state,String taskId,int loadCount,String orderField,String orderBy) {
		
		if(BlankUtils.isBlank(state)) {
			return null;
		}
		
		Object[] pars = new Object[4];
		int index = 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select TEL_ID from ac_call_task_telephone where STATE=?");
		pars[index] = state;
		index++;
		
		if(!BlankUtils.isBlank(taskId)) {
			sb.append(" and TASK_ID=?");
			pars[index] = taskId;
			index++;
		}
		
		
		if(!BlankUtils.isBlank(orderField)) {
			if(BlankUtils.isBlank(orderBy)) {
				orderBy = "asc";
			}
			
			sb.append(" order by " + orderField + " " + orderBy);
		}
		
		
		sb.append(" limit " + loadCount);
		
		System.out.println("查询语句：" + sb.toString() + ";index:" + index + "  字段:" + ArrayUtils.copyArray(index,pars) + "    pars=" + pars);
		
		List<Record> list = Db.find(sb.toString(),ArrayUtils.copyArray(index,pars));
		
		String ids = "";    //定义一个ids
		
		for(Record r:list) {
			ids += r.get("TEL_ID") + ",";
		}
		
		if(!BlankUtils.isBlank(list) && list.size()>0) {
			ids = ids.substring(0, ids.length()-1);  //去掉最后的逗号
		}
		
		//如果没有时,返回空
		if(BlankUtils.isBlank(ids)) {
			return null;
		}
		
		String sql2 = "select * from ac_call_task_telephone where TEL_ID in(" + ids + ")";
		
		List<AutoCallTaskTelephone> tList = find(sql2);
		
		return tList;
	}
	
	
	/**
	 * 根据外呼任务ID,载入外呼数据,即将状态为0的号码,修改状态为1
	 * 
	 * @param taskId
	 * @param loadCount
	 * 			载入的数量
	 * @return
	 */
	public List<AutoCallTaskTelephone> loadAutoCallTask(String taskId,int loadCount) {
		
		String sql = "select TEL_ID from ac_call_task_telephone where STATE='0' and TASK_ID=? limit ?";
		
		List<Record> list = Db.find(sql,taskId,loadCount);
		String ids = "";   //定义一个 ids
		for(Record r:list) {
			ids += r.get("TEL_ID") + ",";
		}
		
		if(!BlankUtils.isBlank(list) && list.size()>0) {
			ids = ids.substring(0, ids.length()-1);  //去掉最后的逗号
		}
		
		//如果没有时,返回空
		if(BlankUtils.isBlank(ids)) {
			return null;
		}
		
		//取出数据
		String sql2 = "select * from ac_call_task_telephone where TEL_ID in(" + ids + ")";
		
		List<AutoCallTaskTelephone> autoCallTaskTelephones = find(sql2);
		
		
		//修改数据
		String sql3 = "update ac_call_task_telephone set STATE=1,LOAD_TIME=?,RETRIED=RETRIED+1,LAST_CALL_RESULT='' where TEL_ID in(" + ids + ")";
		
		int count = Db.update(sql3,DateFormatUtils.getCurrentDate());
		
		return autoCallTaskTelephones;
	}
	
	/**
	 * 得到重试的数据
	 * 
	 * @param loadCount
	 * @param taskIds
	 * 			已激活的任务的 ID字符，格式：   id1,id2,id3
	 * @return
	 */
	public List<AutoCallTaskTelephone> loadRetryData(int loadCount,String taskIds) {
		
		String sql = "select TEL_ID from ac_call_task_telephone where STATE='3' and NEXT_CALLOUT_TIME<? and TASK_ID in(" + taskIds + ") limit ?";
		
		List<Record> list = Db.find(sql,DateFormatUtils.getCurrentDate(),loadCount);
		
		String ids = "";   //定义一个 ids
		for(Record r:list) {
			ids += r.get("TEL_ID") + ",";
		}
		
		if(!BlankUtils.isBlank(list) && list.size()>0) {
			ids = ids.substring(0, ids.length()-1);  //去掉最后的逗号
		}
		
		//如果没有时,返回空
		if(BlankUtils.isBlank(ids)) {
			return null;
		}
		
		//取出数据
		String sql2 = "select * from ac_call_task_telephone where TEL_ID in(" + ids + ")";
		
		List<AutoCallTaskTelephone> autoCallTaskTelephones = find(sql2);
		
		//修改数据
		String sql3 = "update ac_call_task_telephone set STATE=1,LOAD_TIME=?,RETRIED=RETRIED+1,LAST_CALL_RESULT='' where TEL_ID in(" + ids + ")";
		
		int count = Db.update(sql3,DateFormatUtils.getCurrentDate());
		
		return autoCallTaskTelephones;
		
	}
	
	/**
	 * 检查号码的重复性--对于增加号码时检查
	 * 添加号码时作检查,主要是为了防止上传了重复的号码
	 * 
	 * @param telephone
	 * @param taskId
	 * @return
	 */
	public boolean checkTelephoneRepetitionForAdd(String customerTel,String taskId) {
		
		boolean b = false;
		
		String sql = "select count(TEL_ID) as count from ac_call_task_telephone where CUSTOMER_TEL=? and TASK_ID=?";
		
		Record r = Db.findFirst(sql,customerTel,taskId);
		
		if(!BlankUtils.isBlank(r)) {
			int count = Integer.valueOf(r.get("count").toString());
			if(count > 0) {
				b = true;
			}
		}
		
		return b;
	}
	
	/**
	 * 检查号码的重复性--对于修改号码时检查
	 * 修改号码时作检查,主要是为了防止修改成别的已经存在的号码
	 * 
	 * @param customerTel
	 * @param taskId
	 * @param telId
	 * @return
	 */
	public boolean checkTelephoneRepetitionForUpdate(String customerTel,String taskId,int telId) {
		
		boolean b = false;
		
		String sql = "select count(TEL_ID) as count from ac_call_task_telephone where CUSTOMER_TEL=? and TASK_ID=? and TEL_ID!=?";
		
		Record r = Db.findFirst(sql,customerTel,taskId,telId);
		
		if(!BlankUtils.isBlank(r)) {
			int count = Integer.valueOf(r.get("count").toString());
			if(count > 0) {
				b = true;
			}
		}
		
		return b;
		
	}
	
	/**
	 * 设置外呼的执行时间
	 * 
	 * @param telId
	 * @return
	 */
	public boolean setCallOutOperatorTime(int telId) {
		
		String sql = "update ac_call_task_telephone set OP_TIME=? where TEL_ID=?";
		
		int count = Db.update(sql,DateFormatUtils.getCurrentDate(),telId);
		
		if(count>0) {
			return true;
		}else {
			return false;
		}
		
	}
	
	/**
	 * 存入语音文件名，根据列名存储
	 * 
	 * @param column
	 * @param voiceName
	 * @param telId
	 * @return
	 */
	public boolean setVoiceName(String columnName,String voiceName,int telId) {
		
		if(BlankUtils.isBlank(columnName) || BlankUtils.isBlank(voiceName) || BlankUtils.isBlank(telId)) {
			return false;
		}
		
		String sql = "update ac_call_task_telephone set " + columnName + "=? where TEL_ID=?";
		
		int count = Db.update(sql, voiceName,telId);
		
		if(count > 0) {
			return true;
		}else {
			return false;
		}
		
	}
	
	/**
	 * 根据任务ID，取出所有的任务号码,并单独将号码加入List并返回
	 * 
	 * 用于通过文件批量上传号码时，做重复性判断
	 * 
	 * @param taskId
	 * @return
	 */
	public List<String> getAutoCallTaskTelephoneByTaskId(String taskId) {
		
		List<String> list = new ArrayList<String>();
		
		//如果taskId为空,直接返回list
		if(BlankUtils.isBlank(taskId)) {
			return list;
		}
		
		String sql = "select CUSTOMER_TEL from ac_call_task_telephone where TASK_ID=?";
		
		List<Record> autoCallTaskTelephones = Db.find(sql, taskId);
		
		for(Record r:autoCallTaskTelephones) {
			String customerTel = r.get("CUSTOMER_TEL");
			list.add(customerTel);
		}
		
		return list;
	}


	public AutoCallTaskTelephone getPrev() {
		return prev;
	}


	public void setPrev(AutoCallTaskTelephone prev) {
		this.prev = prev;
	}


	public AutoCallTaskTelephone getNext() {
		return next;
	}


	public void setNext(AutoCallTaskTelephone next) {
		this.next = next;
	}
	
	/**
	 * 取得统计数据（呼叫结果）
	 * 
	 * 主要返回: 已载入、已成功、待重呼、已失败、未处理  五种状态的数量
	 * 
	 * @param data
	 * @param startTime
	 * @param endTime
	 * @param channelSource
	 */
	public void getStatisticsDataForState(Record data,String taskId) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("select STATE,COUNT(*) as count from ac_call_task_telephone where TASK_ID=?");
		
		sb.append(" GROUP BY STATE");
		
		List<Record> stateList = Db.find(sb.toString(),taskId);
		
		if(!BlankUtils.isBlank(stateList) && stateList.size() > 0) {
			for(Record stateR:stateList) {
				int stateValue = stateR.getInt("STATE");
				int stateCount = Integer.valueOf(stateR.get("count").toString());
				if(stateValue == 0) {
					data.set("state0Data", stateCount);
				}else if(stateValue == 1) {
					data.set("state1Data", stateCount);
				}else if(stateValue == 2) {
					data.set("state2Data", stateCount);
				}else if(stateValue == 3) {
					data.set("state3Data", stateCount);
				}else if(stateValue == 4) {
					data.set("state4Data", stateCount);
				}
			}
			
		}
		
	}
	
	/**
	 * 取得统计数据（呼叫状态）
	 * 
	 * 主要返回: 
		1：呼叫成功; 2：无应答; 3：客户忙; 4：请求错误
	 * 
	 * @param data
	 * @param startTime
	 * @param endTime
	 * @param channelSource
	 */
	public void getStatisticsDataForLastCallResult(Record data,String taskId) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("select LAST_CALL_RESULT,COUNT(*) as count from ac_call_task_telephone where TASK_ID=? and STATE in(3,4)");
		
		sb.append(" GROUP BY LAST_CALL_RESULT");
		
		List<Record> lastCallResultList = Db.find(sb.toString(),taskId);
		
		if(!BlankUtils.isBlank(lastCallResultList) && lastCallResultList.size() > 0) {
			for(Record lastCallResultR:lastCallResultList) {
				String lastCallResultValue = lastCallResultR.getStr("LAST_CALL_RESULT");
				int lastCallResultCount = Integer.valueOf(lastCallResultR.get("count").toString());
				//如果该记录为空，则跳过循环
				if(BlankUtils.isBlank(lastCallResultValue)) { 
					continue;
				}
				
				if(lastCallResultValue.equalsIgnoreCase("1")) {
					data.set("lastCallResult1Data", lastCallResultCount);
				}else if(lastCallResultValue.equalsIgnoreCase("2")) {
					data.set("lastCallResult2Data", lastCallResultCount);
				}else if(lastCallResultValue.equalsIgnoreCase("3")) {
					data.set("lastCallResult3Data", lastCallResultCount);
				}else if(lastCallResultValue.equalsIgnoreCase("4")) {
					data.set("lastCallResult4Data", lastCallResultCount);
				}
			}
			
		}
		
	}
	
	
	/**
	 * 取得统计数据（呼叫状态）
	 * 
	 * 主要返回: 已载入、已成功、待重呼、已失败、未处理  五种状态的数量
	 * 
	 * @param data
	 * @param startTime
	 * @param endTime
	 * @param channelSource
	 */
	public void getStatisticsDataForState_bak(Record data,String taskId) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("select STATE,COUNT(*) as count from ac_call_task_telephone where TASK_ID=?");
		
		sb.append(" GROUP BY STATE");
		
		List<Record> stateList = Db.find(sb.toString(),taskId);
		
		if(!BlankUtils.isBlank(stateList) && stateList.size() > 0) {
			for(Record stateR:stateList) {
				int stateValue = stateR.getInt("STATE");
				int stateCount = Integer.valueOf(stateR.get("count").toString());
				if(stateValue == 0) {
					data.set("state0Data", stateCount);
				}else if(stateValue == 1) {
					data.set("state1Data", stateCount);
				}else if(stateValue == 2) {
					data.set("state2Data", stateCount);
				}else if(stateValue == 3) {
					data.set("state3Data", stateCount);
				}else if(stateValue == 4) {
					data.set("state4Data", stateCount);
				}
			}
			
		}
		
	}
	
	
	/**
	 * 取得统计数据（呼叫状态）
	 * 
	 * 主要返回: 已载入、已成功、待重呼、已失败、未处理  五种状态的数量
	 * 
	 * @param data
	 */
	public void getStatisticsDataForStateMultiTask(Record data,String ids) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("select STATE,COUNT(*) as count from ac_call_task_telephone where TASK_ID in(" + ids + ")");
		
		sb.append(" GROUP BY STATE");
		
		List<Record> stateList = Db.find(sb.toString());
		
		if(!BlankUtils.isBlank(stateList) && stateList.size() > 0) {
			for(Record stateR:stateList) {
				int stateValue = stateR.getInt("STATE");
				int stateCount = Integer.valueOf(stateR.get("count").toString());
				if(stateValue == 0) {
					data.set("state0Data", stateCount);
				}else if(stateValue == 1) {
					data.set("state1Data", stateCount);
				}else if(stateValue == 2) {
					data.set("state2Data", stateCount);
				}else if(stateValue == 3) {
					data.set("state3Data", stateCount);
				}else if(stateValue == 4) {
					data.set("state4Data", stateCount);
				}
			}
			
		}
		
	}
	

}
