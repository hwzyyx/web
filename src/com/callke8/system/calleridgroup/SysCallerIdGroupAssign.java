package com.callke8.system.calleridgroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.system.callerid.SysCallerId;
import com.callke8.system.operator.Operator;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 主叫号码组号码分配
 * 
 * @author 黄文周
 *
 */
public class SysCallerIdGroupAssign extends Model<SysCallerIdGroupAssign> {

	private static final long serialVersionUID = 1L;
	public static SysCallerIdGroupAssign dao = new SysCallerIdGroupAssign();
	
	public Page getSysCallerIdGroupAssignByPaginate(int pageNumber,int pageSize,int groupId) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from sys_callerid_group_assign where 1=1");

		//条件判断暂时不自动添加
		if(groupId != 0) {
			sb.append(" and GROUP_ID=?");
			pars[index] = groupId;
			index++;
		}

		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY ID DESC",ArrayUtils.copyArray(index, pars));
		return p;
		
	}
	
	public Map getSysCallerIdGroupAssignByPaginateToMap(int pageNumber,int pageSize,int groupId) {
		
		if(groupId == 0) {
			Map map = new HashMap();
			map.put("total",0);
			map.put("rows",null);
			return map;
		}
		
		Page<Record> p = getSysCallerIdGroupAssignByPaginate(pageNumber,pageSize,groupId);
		
		int total = p.getTotalRow();       //取出总数量
		
		List<Record> newList = new ArrayList<Record>();
		
		//取出主叫号码组信息
		SysCallerIdGroup sysCallerIdGroup  = SysCallerIdGroup.dao.getSysCallerIdGroupById(groupId);
		
		for(Record r:p.getList()) {
			int callerId_IdRs = r.getInt("CALLERID_ID");    //取出主叫号码的ID
			SysCallerId sysCallerId = SysCallerId.dao.getSysCallerIdById(callerId_IdRs);    //取出主叫号码
			
			if(BlankUtils.isBlank(sysCallerId)) {    //如果取出的主叫号码为空，可能是被删除，则跳过循环
				continue;
			}
			
			r.set("CALLERID", sysCallerId.get("CALLERID"));    //主叫号码
			r.set("PURPOSE", sysCallerId.get("PURPOSE"));      //号码用途
			
			//设置操作员名字（工号）
			String uc = sysCallerId.getStr("CREATE_USERCODE");   //取出创建人工号
			Operator oper = Operator.dao.getOperatorByOperId(uc);
			if(!BlankUtils.isBlank(oper)) {
				r.set("CREATE_USERCODE_DESC", oper.get("OPER_NAME") + "(" + uc + ")");
			}
			
			r.set("CREATE_TIME", sysCallerId.get("CREATE_TIME"));    //主叫号码的添加时间
			
			r.set("GROUP_NAME", sysCallerIdGroup.get("GROUP_NAME"));
			newList.add(r);
		}
		
		Map map = new HashMap();
		map.put("total", newList.size());
		map.put("rows", newList);

		return map;
		
	}
	
	/**
	 * 保存主叫号码分配
	 * 
	 * @param callerIdGroupAssign
	 * @param groupId
	 * @return
	 */
	public int saveCallerIdGroupAssign(List<Record> callerIdGroupAssignList,int groupId) {
		
		if(groupId==0 || BlankUtils.isBlank(callerIdGroupAssignList)) {
			return 0;
		}
		
		//(1)删除前面已经分配的结果
		int deleteCount = deleteCallerIdGroupAssignByGroupId(groupId);
		
		//(2)批量存入主叫号码结果
		int insertCount = batchSave(callerIdGroupAssignList);
		
		return insertCount;
	}
	
	/**
	 * 批量存入分配结果
	 * 
	 * @param callerIdGroupAssignList
	 * @return
	 */
	public int batchSave(List<Record> callerIdGroupAssignList) {
		
		if(BlankUtils.isBlank(callerIdGroupAssignList) || callerIdGroupAssignList.size() == 0) {
			return 0;
		}
		
		String sql = "insert into sys_callerid_group_assign(GROUP_ID,CALLERID_ID)values(?,?)";
		
		int[] insertData = Db.batch(sql,"GROUP_ID,CALLERID_ID", callerIdGroupAssignList, 1000);
		
		return insertData.length;
	}
	
	/**
	 * 根据主叫号码组，取得该组分配的情况
	 * 
	 * @return
	 */
	public List<Record> getCallerIdGroupAssignByGroupId(int groupId) {
		
		String sql = "select * from sys_callerid_group_assign where GROUP_ID=?";
		
		List<Record> list = Db.find(sql,groupId);
		
		return list;
	}
	
	/**
	 * 根据号码组ID,删除号码组的分配结果
	 * 
	 * @param groupId
	 * @return
	 */
	public int deleteCallerIdGroupAssignByGroupId(int groupId) {
		
		String sql = "delete from sys_callerid_group_assign where GROUP_ID=?";
		
		int count = Db.update(sql, groupId);
		
		return count;
		
	}
	
	/**
	 * 根据分配ID,删除号码组的分配结果
	 * 
	 * @param id
	 * @return
	 */
	public int deleteCallerIdGroupAssignById(int id) {
		
		String sql = "delete from sys_callerid_group_assign where ID=?";
		
		int count = Db.update(sql,id);
		
		return count;
	}
	
	/**
	 * 删除分配
	 * 
	 * @param ids
	 * 			ids 的格式为： 11，22，33 格式
	 * @return
	 */
	public int deleteCallerIdGroupAssignByIds(String ids) {
		String sql = "delete from sys_callerid_group_assign where ID in(" + ids  + ")";
		
		int count = Db.update(sql);
		
		return count;
	}
	
}
