package com.callke8.system.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 数据表 sys_operator
 * 
 * 表结构如下：
mysql> desc sys_operator;
+-------------+--------------+------+-----+---------+-------+
| Field       | Type         | Null | Key | Default | Extra |
+-------------+--------------+------+-----+---------+-------+
| OPER_ID     | varchar(255) | NO   | PRI | NULL    |       |
| ORG_CODE    | varchar(32)  | YES  |     | NULL    |       |
| OPER_NAME   | varchar(32)  | NO   |     | NULL    |       |
| OPER_LEVEL  | varchar(1)   | YES  |     | NULL    |       |
| STATE       | varchar(1)   | YES  |     | NULL    |       |
| DUTY_CODE   | varchar(64)  | YES  |     | NULL    |       |
| SEX         | varchar(1)   | YES  |     | NULL    |       |
| EMAIL       | varchar(255) | YES  |     | NULL    |       |
| TELNO       | varchar(64)  | YES  |     | NULL    |       |
| ADDRESS     | varchar(255) | YES  |     | NULL    |       |
| POSTCODE    | varchar(8)   | YES  |     | NULL    |       |
| PASSWORD    | varchar(64)  | NO   |     | NULL    |       |
| OPER_TYPE   | varchar(1)   | YES  |     | NULL    |       |
| OPER_FLAG   | varchar(1)   | YES  |     | NULL    |       |
| CREATETIME  | datetime     | YES  |     | NULL    |       |
| AREA_CODE   | varchar(32)  | YES  |     | NULL    |       |
| CALL_NUMBER | varchar(32)  | YES  |     | NULL    |       |
+-------------+--------------+------+-----+---------+-------+
17 rows in set (0.01 sec)
 * 
 * @author Administrator
 *
 */
public class Operator extends Model<Operator> {
	
	public static Operator dao = new Operator();
	
	/**
	 * 根据页数据查询
	 * @param currentPage
	 * @param numPerPage
	 * @param operId
	 * @param operName
	 * @param operState
	 * @return
	 */
	public Page<Record> getOperatorByPaginate(int currentPage,int numPerPage,String operId,String operName,String operState,String orgCode) {
		
		//先拼接SQL语句
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[4];   //先定义一个容量为3的参数数组
		int index = 0;
		
		sb.append("from sys_operator where 1=1");
		
		if(!BlankUtils.isBlank(operId)) {
			sb.append(" and OPER_ID like ?");
			pars[index] = "%" + operId + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(operName)) {
			sb.append(" and OPER_NAME like ?");
			pars[index] = "%" + operName + "%";
			index++;
		}
		
		//如果状态不为空，且状态不为2时
		if(!BlankUtils.isBlank(operState) && !operState.equalsIgnoreCase("2")) {
			sb.append(" and STATE=?");
			pars[index] = operState;
			index++;
		}
		
		if(!BlankUtils.isBlank(orgCode)) {
			sb.append(" and ORG_CODE=?");
			pars[index] = orgCode;
			index++;
		}
		
		
		Page<Record> p = Db.paginate(currentPage, numPerPage, "select *", sb.toString(), ArrayUtils.copyArray(index, pars));
		
		return p;
	}
	
	/**
	 * 查询页数据并转为 Map 用于前端显示数据
	 * @param currentPage
	 * @param numPerPage
	 * @param operId
	 * @param operName
	 * @param operState
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map getOperatorByPaginateToMap(int currentPage,int numPerPage,String operId,String operName,String operState,String orgCode) {
		
		Page p = getOperatorByPaginate(currentPage, numPerPage, operId, operName, operState,orgCode);
		
		int total = p.getTotalRow();
		
		Map m = new HashMap();
		m.put("total", total);
		m.put("rows", p.getList());
		
		return m;
	}
	
	/**
	 * 将所有状态为有效的操作员显示，主要是用于外呼任务授权使用
	 * @return
	 */
	public List<Record> getAllActiveOperator() {
		
		String sql = "select * from sys_operator where STATE=?";
		
		List<Record> list = Db.find(sql, "1");   //只有当状态为 1 时，即有效时
		
		return list;
	}
	
	/**
	 * 根据组织代码，查询当前组织下，是否有操作员，主要是用于删除组织时做判断用
	 * @param orgCode
	 * @return
	 */
	public boolean isHasOperatorByOrgCode(String orgCode) {
		
		boolean b = false;
		
		String sql = "select count(*) as count from sys_operator where ORG_CODE=?";
		
		Record r = Db.findFirst(sql, orgCode);
		
		int count = Integer.valueOf(r.get("count").toString());
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	public boolean add(Operator oper) {
		
		boolean b = false;
		
		if(oper.save()) {
			b = true;
		}
		
		return b;
	}
	
	public boolean update(Operator oper) {
		boolean b = false;
		int count;
		
		//得到角色代码
		String operId = oper.get("OPER_ID");
		
		count = Db.update("update sys_operator set ORG_CODE=?,OPER_NAME=?,STATE=?,SEX=?,TELNO=?,CALL_NUMBER=? where OPER_ID=?", oper.get("ORG_CODE"),oper.get("OPER_NAME"),oper.get("STATE"),oper.get("SEX"),oper.get("TELNO"),oper.get("CALL_NUMBER"),operId);
		
		if(count == 1) {
			b = true;
		}
		
		return b;
	}
	
	public boolean changePassword(String operId,String newPassword) {
		
		boolean b = false;
		int count;
		
		String sql = "update sys_operator set PASSWORD=? where OPER_ID=?";
		count = Db.update(sql, newPassword,operId);
		
		if(count==1) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据 operId 查询操作员
	 * @param operId
	 * @return
	 */
	public Operator getOperatorByOperId(String operId) {
		
		String sql = "select * from sys_operator where OPER_ID=?";
		
		Operator oper = findFirst(sql, operId);
		
		return oper;
	}
	
	/**
	 * 根据操作工号，取得其组织编码
	 * 
	 * @param operId
	 * @return
	 */
	public String getOrgCodeByOperId(String operId) {
		Operator oper = getOperatorByOperId(operId);
		
		if(!BlankUtils.isBlank(oper)) {
			return oper.getStr("ORG_CODE");
		}else {
			return null;
		}
	}
	
	public boolean delete(String operId) {
		
		boolean b = false;
		int count = 0;
		
		count = Db.update("delete from sys_operator where OPER_ID=?", operId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 用于将操作员数据加载到内存
	 * 
	 * @return
	 */
	public Map loadOperatorInfo() {
		
		Map<String,Record> map = new HashMap<String,Record>();
		
		//取出所有的活动的操作员
		List<Record> list = getAllActiveOperator();
		
		for(Record r:list) {
			map.put(r.get("OPER_ID").toString(), r);
		}
		
		return map;
	}
	
}








































































