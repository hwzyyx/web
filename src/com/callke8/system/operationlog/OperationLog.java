package com.callke8.system.operationlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.ptg.MemErrPtg;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 系统操作日志
 * mysql> desc sys_operation_log;
 * 
CREATE TABLE `sys_operation_log` (
  `OPERATION_ID` bigint(32) NOT NULL AUTO_INCREMENT,
  `OPER_ID` varchar(64) DEFAULT NULL,
  `OGR_CODE` varchar(255) DEFAULT NULL,
  `MODULE_CODE` varchar(255) DEFAULT NULL,
  `OPERATION` varchar(255) DEFAULT NULL,
  `IP_ADDRESS` varchar(255) DEFAULT NULL,
  `OPREATION_TIME` datetime DEFAULT NULL,
  `PARAMS` text,
  PRIMARY KEY (`OPERATION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
12 rows in set (0.00 sec)
 * @author <a href="mailto:120077407@qq.com">hwz</a>
 */
public class OperationLog extends Model<OperationLog> {
	
	public static OperationLog dao = new OperationLog();

	@SuppressWarnings("unchecked")
	public Page getOperationLogByPaginate(int currentPage,int numPerPage,String moduleCode,String operation,String operId,String startTime,String endTime) {
		
		//先拼接SQL语句
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[6];   //先定义一个容量为4的参数数组
		int index = 0;
		
		sb.append("from sys_operation_log where 1=1 and MODULE_CODE<>'' ");
		
		if(!BlankUtils.isBlank(moduleCode)) {
			sb.append(" and MODULE_CODE=?");
			pars[index] = moduleCode;
			index++;
		}
		
		if(!BlankUtils.isBlank(operation) && !operation.equalsIgnoreCase("0")) {   //为0时，即是为请选择，需要排除
			sb.append(" and OPERATION=?");
			pars[index] = operation;
			index++;
		}
		
		if(!BlankUtils.isBlank(operId) && !operId.equalsIgnoreCase("-1")) {       //当等于 -1　时，表示这个是请选择，需要排除
			sb.append(" and OPER_ID=?");
			pars[index] = operId;
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and OPERATION_TIME>=?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and OPERATION_TIME<=");
			pars[index] = endTime + "23:59:59";
			index++;
		}
		
		Page page = Db.paginate(currentPage, numPerPage, "select * ", sb.toString() + " ORDER BY OPERATION_ID DESC", ArrayUtils.copyArray(index, pars));
		
		return page;
	}
	
	public Map getOperationLogByPaginateToMap(int currentPage,int numPerPage,String moduleCode,String operation,String operId,String startTime,String endTime) {
		
		Map m = new HashMap();
		
		Page page = getOperationLogByPaginate(currentPage, numPerPage, moduleCode, operation, operId, startTime, endTime);
		
		int total = page.getTotalRow();
		List<Record> list = page.getList();
		
		List<Record> newList = new ArrayList<Record>();  //创建一个新的list,用于增加：菜单名字、操作员名字、操作动作名称
		
		for(Record r:list) {
			String operIdRs = r.get("OPER_ID");         //操作员工号
			String moduleCodeRs = r.get("MODULE_CODE"); //菜单编码
			String operationRs = r.get("OPERATION");    //操作动作类型
			
			r.set("OPER_NAME", operIdRs + "(" + MemoryVariableUtil.getOperName(operIdRs) + ")");   	  //设置操作员名称
			r.set("MODULE_NAME",MemoryVariableUtil.getModuleName(moduleCodeRs));    				  //设置菜单的名称
			r.set("OPERATION_DESC", MemoryVariableUtil.getDictName("OPERATION_TYPE", operationRs));   //设置操作动作的描述
			
			newList.add(r);
		}
		
		m.put("total", total);
		m.put("rows", newList);
		
		return m;
	}
	
	public boolean add(Record operationLog) {
		
		boolean b = Db.save("sys_operation_log", "OPERATION_ID", operationLog);
		
		return b;
	}
	
	
	
}
