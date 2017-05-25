package com.callke8.system.loginlog;

import java.util.HashMap;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 表 sys_login_log 表结构：
 * mysql> desc sys_login_log;
+-------------+-------------+------+-----+---------+----------------+
| Field       | Type        | Null | Key | Default | Extra          |
+-------------+-------------+------+-----+---------+----------------+
| LOG_ID      | bigint(20)  | NO   | PRI | NULL    | auto_increment |
| OPER_ID     | varchar(16) | NO   |     | NULL    |                |
| ORG_CODE    | varchar(32) | YES  |     | NULL    |                |
| LOGIN_TIME  | datetime    | NO   |     | NULL    |                |
| LOGOUT_TIME | datetime    | YES  |     | NULL    |                |
| IP_ADDRESS  | varchar(16) | NO   |     | NULL    |                |
| MAC_ID      | varchar(32) | YES  |     | NULL    |                |
+-------------+-------------+------+-----+---------+----------------+
7 rows in set (0.06 sec)
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
public class LoginLog extends Model<LoginLog> {
	
	public static LoginLog dao = new LoginLog();
	
	/**
	 * 增加登录日志，并返回ID
	 * 
	 * @param loginLog
	 * @return
	 */
	public int add(Record loginLog) {
		
		boolean b = Db.save("sys_login_log", "LOG_ID", loginLog);
		
		if(b) {
			if(!BlankUtils.isBlank(loginLog.get("LOG_ID"))) {
				return Integer.valueOf(loginLog.get("LOG_ID").toString());
			}
		}
		
		return 0;
	}
	
	public boolean update(int logId) {
		boolean b = false;
		String sql = "update sys_login_log set LOGOUT_TIME=? where LOG_ID=?";
		
		int count = Db.update(sql, DateFormatUtils.getCurrentDate(),logId);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据条件查询分页数据
	 * @param currentPage
	 * @param numPerPage
	 * @param operId
	 * @param orgCode
	 * @param loginStartTime
	 * @param loginEndTime
	 * @return
	 */
	public Page<Record> getLoginLogByPaginate(int currentPage,int numPerPage,String operId,String orgCode,String loginStartTime,String loginEndTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[4];
		int index = 0;
		
		sb.append("from sys_login_log where 1=1");
		
		if(!BlankUtils.isBlank(operId)) {
			sb.append(" and OPER_ID like ?");
			pars[index] = "%" + operId + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(orgCode)) {
			sb.append(" and ORG_CODE like ?");
			pars[index] = "%" + orgCode + "%"; 
			index++;
		}
		
		if(!BlankUtils.isBlank(loginStartTime)) {
			loginStartTime += " 00:00:00";
			sb.append(" and LOGIN_TIME>?");
			pars[index] = loginStartTime;
			index++;
		}
		
		if(!BlankUtils.isBlank(loginEndTime)) {
			loginEndTime += " 23:59:59";
			sb.append(" and LOGIN_TIME<?");
			pars[index] = loginEndTime;
			index++;
		}
		
		Page<Record> page = Db.paginate(currentPage, numPerPage, "select *", sb.toString() + " ORDER BY LOGIN_TIME DESC", ArrayUtils.copyArray(index, pars));
		
		return page;
	}
	
	/**
	 * 根据条件，查询分页数据，并返回 map
	 * @param currentPage
	 * @param numPerPage
	 * @param operId
	 * @param orgCode
	 * @param loginStartTime
	 * @param loginEndTime
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map getLoginLogByPaginateToMap(int currentPage,int numPerPage,String operId,String orgCode,String loginStartTime,String loginEndTime) {
		
		Page<Record> page = getLoginLogByPaginate(currentPage, numPerPage, operId, orgCode, loginStartTime, loginEndTime);
		
		int total = page.getTotalRow();
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", page.getList());
		
		return map;
	}
	
	
	
	
}























