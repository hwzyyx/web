package com.callke8.autocall.autocalltask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
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
	 * @param telephone
	 * @param clientName
	 * @param state
	 * @return
	 */
	public Page<Record> getAutoCallTaskTelephoneByPaginate(int pageNumber,int pageSize,String taskId,String telephone,String clientName,String state) {
		
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
		
		if(!BlankUtils.isBlank(state) && !state.equals("5")) {
			sb.append(" and STATE=?");
			pars[index] = state;
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
	 * @param state
	 * @return
	 */
	public Map<String,Object> getAutoCallTaskTelephoneByPaginateToMap(int pageNumber,int pageSize,String taskId,String telephone,String clientName,String state) {
		
		Map<String,Object> m = new HashMap<String,Object>();
		
		if(BlankUtils.isBlank(taskId)) {
			m.put("total", 0);
			m.put("rows", new ArrayList<Record>());
			
			return m;
		}
		
		Page<Record> page = getAutoCallTaskTelephoneByPaginate(pageNumber, pageSize, taskId, telephone, clientName,state);
		
		int total = page.getTotalRow();
		m.put("total", total);
		m.put("rows", page.getList());
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
		
		String sql = "insert into ac_call_task_telephone(TASK_ID,TELEPHONE,CLIENT_NAME,COMPANY,PERIOD,CHARGE,VIOLATION_CITY,PUNISHMENT_UNIT,VIOLATION_REASON,CREATE_TIME,RETRIED,STATE)value(?,?,?,?,?,?,?,?,?,?,?,?)";
		
		int[] insertData = Db.batch(sql, "TASK_ID,TELEPHONE,CLIENT_NAME,COMPANY,PERIOD,CHARGE,VIOLATION_CITY,PUNISHMENT_UNIT,VIOLATION_REASON,CREATE_TIME,RETRIED,STATE",telephones,5000);
		
		return insertData.length;
	}
	
	public boolean update(String telephone,String clientName,String period,String violationCity,String punishmentUnit,String violationReason,String charge,String company,int telId) {
		
		boolean b = false;
		
		String sql = "update ac_call_task_telephone set TELEPHONE=?,CLIENT_NAME=?,PERIOD=?,VIOLATION_CITY=?,PUNISHMENT_UNIT=?,VIOLATION_REASON=?,CHARGE=?,COMPANY=? where TEL_ID=?";
		
		int count = Db.update(sql,telephone,clientName,period,violationCity,punishmentUnit,violationReason,charge,company,telId);
		
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
	 * @return
	 */
	public List<Record> getAutoCallTaskTelephonesByTaskIdAndState(String taskId,String state,String telephone,String clientName) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0; 
		
		sb.append("select * from ac_call_task_telephone where 1=1 ");
		
		if(!BlankUtils.isBlank(taskId)) {
			sb.append(" and TASK_ID=?");
			pars[index] = taskId;
			index++;
		}
		
		if(!BlankUtils.isBlank(state)) {
			sb.append(" and STATE=?");
			pars[index] = state;
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
		
		sb.append(" ORDER BY TEL_ID DESC");
		
		List<Record> list = Db.find(sb.toString(),ArrayUtils.copyArray(index, pars));
		List<Record> newList = new ArrayList<Record>();
		
		//遍历数据，并将相关的信息
		for(Record r:list) {
			
			String callState = r.get("STATE");   //取出状态
			
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
	 * 更改外呼号码为重试
	 * 
	 * @param telId
	 * 			号码ID
	 * @param newState
	 * 			新的状态（重试状态为3）
	 * @param retryInterval
	 * @param lastCallResult
	 * 			最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * @return
	 */
	public boolean updateAutoCallTaskTelephoneStateToRetry(int telId,String newState,int retryInterval,String lastCallResult) {
		
		boolean b = false;
		
		if(telId <= 0 && BlankUtils.isBlank(newState) && retryInterval<=0) {
			return false;
		}
		
		long currTimeMillis = DateFormatUtils.getTimeMillis();   			 //当前时间的毫秒数
		long retryTimeMillis = currTimeMillis + retryInterval * 60 * 1000;   //重试时的毫秒数
		
		String nextCallOutTime = DateFormatUtils.formatDateTime(new Date(retryTimeMillis),"yyyy-MM-dd HH:mm:ss");
		
		String sql = "update ac_call_task_telephone set STATE=?,NEXT_CALLOUT_TIME=?,RETRIED=RETRIED+1,LAST_CALL_RESULT=? where TEL_ID=?";
		
		int count = Db.update(sql,newState,nextCallOutTime,lastCallResult,telId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 根据号码状态及外呼任务ID为空时,取出数量
	 * 如果外呼任务ID为空时,取出所有任务的状态的数量
	 * 
	 * @param state
	 * 			0:新建;1:已载入(即载入号码到未外呼);2:呼叫成功;3:呼叫失败;
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
		String sql3 = "update ac_call_task_telephone set STATE=1,LOAD_TIME=? where TEL_ID in(" + ids + ")";
		
		int count = Db.update(sql3,DateFormatUtils.getCurrentDate());
		
		return autoCallTaskTelephones;
	}
	
	/**
	 * 得到重试的数据
	 * 
	 * @param loadCount
	 * @return
	 */
	public List<AutoCallTaskTelephone> loadRetryData(int loadCount) {
		
		String sql = "select TEL_ID from ac_call_task_telephone where STATE='3' and NEXT_CALLOUT_TIME<? limit ?";
		
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
		String sql3 = "update ac_call_task_telephone set STATE=1,LOAD_TIME=? where TEL_ID in(" + ids + ")";
		
		int count = Db.update(sql3,DateFormatUtils.getCurrentDate());
		
		return autoCallTaskTelephones;
		
	}
	
	/**
	 * 检查号码的重复性
	 * 添加号码时作检查,主要是为了防止上传了重复的号码
	 * 
	 * @param telephone
	 * @param taskId
	 * @return
	 */
	public boolean checkTelephoneRepetition(String telephone,String taskId) {
		
		boolean b = false;
		
		String sql = "select count(TEL_ID) as count from ac_call_task_telephone where TELEPHONE=? and TASK_ID=?";
		
		Record r = Db.findFirst(sql,telephone,taskId);
		
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
		
		String sql = "select TELEPHONE from ac_call_task_telephone where TASK_ID=?";
		
		List<Record> autoCallTaskTelephones = Db.find(sql, taskId);
		
		for(Record r:autoCallTaskTelephones) {
			String telephone = r.get("TELEPHONE");
			list.add(telephone);
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
	

}
