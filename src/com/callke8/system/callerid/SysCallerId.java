package com.callke8.system.callerid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.callke8.system.operator.Operator;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 主叫号码表
 * 
 * @author 黄文周
 *
 */
public class SysCallerId extends Model<SysCallerId> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 增加一个静态的  Map,用于存储轮循主叫号码时，任务ID对应的上次取主叫号码的 index ，
	 * 通过该Map,就可以看到上次这个任务获取主叫号码使用的index是什么，要取下一个，直接加一个即可，如果超过下标，则从0重新开始
	 */
	private static Map<String,Integer> taskIdAndIndexMap = new HashMap<String,Integer>();
	
	public static SysCallerId dao = new SysCallerId();
	
	public Page getSysCallerIdByPaginate(int pageNumber,int pageSize,String callerId,String purpose,String ids) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("from sys_callerid where 1=1");
		
		if(!BlankUtils.isBlank(callerId)) {
			sb.append(" and CALLERID like ?");
			pars[index] = "%" + callerId + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(purpose)) {
			sb.append(" and PURPOSE like ?");
			pars[index] = "%" + purpose + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(ids) && ids.length() > 0) {
			sb.append(" and ID in(" + ids + ")");
		}
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY ID DESC",ArrayUtils.copyArray(index, pars));
		
		return p;
		
	}
	
	public Map getSysCallerIdByPaginateToMap(int pageNumber,int pageSize,String callerId,String purpose,String ids) {
		
		Page<Record> p =  getSysCallerIdByPaginate(pageNumber,pageSize,callerId,purpose,ids);
		
		int total = p.getTotalRow();     //取出总数量
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:p.getList()) {
			
			//设置操作员名字（工号）
			String uc = r.getStr("CREATE_USERCODE");   //取出创建人工号
			Operator oper = Operator.dao.getOperatorByOperId(uc);
			if(!BlankUtils.isBlank(oper)) {
				r.set("CREATE_USERCODE_DESC", oper.get("OPER_NAME") + "(" + uc + ")");
			}
			
			newList.add(r);
			
		}
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", newList);
		
		return map;
		
	}
	
	
	//取出所有主叫号码的列表
	public List<Record> getAllSysCallerId() {
		
		String sql = "select * from sys_callerid order by ID asc";
		
		List<Record> list  = Db.find(sql);
		
		return list;
	}
	
	
	public boolean add(SysCallerId sysCallerId) {
		
		Record r = new Record();
		r.set("CALLERID", sysCallerId.get("CALLERID"));
		r.set("PURPOSE", sysCallerId.get("PURPOSE"));
		r.set("CREATE_USERCODE", sysCallerId.get("CREATE_USERCODE"));
		r.set("ORG_CODE", sysCallerId.get("ORG_CODE"));
		r.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
		
		return add(r);
	}
	
	public boolean add(Record sysCallerId) {
		boolean b = Db.save("sys_callerid", "ID", sysCallerId);
		if(b) {
			loadSysCallerIdToMemory();
		}
		return b;
	}
	
	public boolean update(String callerId,String purpose,int id) {
		
		boolean b = false;
		
		String sql = "update sys_callerid set CALLERID=?,PURPOSE=? where ID=?";
		
		int count = Db.update(sql,callerId,purpose,id);
		
		if(count > 0) {
			b = true;
			loadSysCallerIdToMemory();
		}
		
		return b;
	}
	
	/**
	 * 根据主叫号码，从数据表中取出数据
	 * 
	 * @param callerId
	 * @return
	 */
	public SysCallerId getSysCallerIdByCallerId(String callerId) {
		
		String sql = "select * from sys_callerid where CALLERID=?";
		
		SysCallerId sysCallerId = findFirst(sql, callerId);
		
		return sysCallerId;
	}
	
	/**
	 * 根据 id 取出主叫号码信息
	 * 
	 * @param id
	 * @return
	 */
	public SysCallerId getSysCallerIdById(int id) {
		
		String sql = "select * from sys_callerid where ID=?";
		
		SysCallerId sysCallerId = findFirst(sql, id);
		
		return sysCallerId;
	}
	
	/**
	 * 根据ID，删除主叫号码
	 * 
	 * @return
	 */
	public boolean deleteById(int id) {
		
		boolean b = false;
		
		int count = 0;
		
		count = Db.update("delete from sys_callerid where ID=?",id);
		
		if(count > 0) {
			b = true;
			loadSysCallerIdToMemory();
		}
		
		return b;
	}
	
	/**
	 * 加载系统的主叫号码的参数到内存
	 */
	public void loadSysCallerIdToMemory() {
		
		List<Record> sysCallerIdList = getAllSysCallerId();
		
		if(BlankUtils.isBlank(sysCallerIdList)) {
			System.out.println("错误：=======-加载系统主叫号码数据到内存失败,sys_callerid 表数据为空,请添加数据后,再重新启动进行加载!");
			return;
		}
		
		for(Record sysCallerId:sysCallerIdList) {
			String id = String.valueOf(sysCallerId.getInt("ID"));     //ID值
			String callerId = sysCallerId.getStr("CALLERID");         //主叫号码
			
			SysCallerIdConfig.sysCallerIdMap.put(id, callerId);
		}
		
		//遍历配置参数的Map
		Iterator<Map.Entry<String,String>> it = SysCallerIdConfig.sysCallerIdMap.entrySet().iterator();
		System.out.println("系统主叫号码加载至内存的情况(键值对):");
		while(it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		
	}
	
	/**
	 * 选择主叫号码，轮循主叫号码时，通过该方法，传入任务ID和任务管理时选择的主叫号码
	 * 
	 * @param taskId
	 * 			任务的ID
	 * @param taskName	
	 * 			任务的名称
	 * @param id
	 * 			任务的主叫的ID列表以逗号分隔，如  22,33,66
	 */
	public String selectCallerId(String taskId,String id) {
		
		String[] ids = id.split(",");
		if(ids.length==1) {    //如果传入的id只有一个时，直接根据传入的 id 取主叫即可
			return SysCallerIdConfig.sysCallerIdMap.get(id);
		}
		
		if(taskIdAndIndexMap.containsKey(taskId)) {      //如果当前的Map中已经存在当前任务ID的记录
			//查看上一次，该任务对应的 index 值 是多少
			int prevIndex = taskIdAndIndexMap.get(taskId);    //上一个index的值
			int nextIndex = prevIndex + 1;                    //下一个index的值
			if(nextIndex>=ids.length) {      //如果上一个index+1大于了 ids的个数，表示已经超过下标了
				taskIdAndIndexMap.put(taskId, 0);
				String idRs = ids[0];
				return SysCallerIdConfig.sysCallerIdMap.get(idRs);
			}else {
				taskIdAndIndexMap.put(taskId,nextIndex);
				String idRs = ids[nextIndex];
				return SysCallerIdConfig.sysCallerIdMap.get(idRs);
			}
		}else {                                //如果当前 Map 中不存在当前的任务的记录
			taskIdAndIndexMap.put(taskId, 0);
			String idRs = ids[0];
			return SysCallerIdConfig.sysCallerIdMap.get(idRs);
		}
	}
	
	/**
	 * 批量将主叫号码插入到数据库
	 * @param callerIdList
	 * @return
	 */
	public int[] batchSave(ArrayList<Record> sysCallerIdList) {
		if(BlankUtils.isBlank(sysCallerIdList) || sysCallerIdList.size()==0) {
			return null;
		}
		
		String sql = "insert into sys_callerid(CALLERID,PURPOSE,CREATE_USERCODE,CREATE_TIME)values(?,?,?,?)";
		
		int[] insertData = Db.batch(sql,"callerIdNumber,callerIdPurpose,CREATE_USERCODE,CREATE_TIME", sysCallerIdList, 1000);
		
		if(insertData.length > 0) {
			loadSysCallerIdToMemory();
		}
		
		return insertData;
	}
		
}
