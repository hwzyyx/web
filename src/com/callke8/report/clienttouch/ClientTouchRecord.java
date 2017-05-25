package com.callke8.report.clienttouch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.system.operator.Operator;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 接触记录表，即在外呼或是客户来电弹屏后，登记的客户本次通话记录
 * @author Administrator
 *
 *CREATE TABLE `client_touch_record` (
  `TOUCH_NO` bigint(11) NOT NULL AUTO_INCREMENT,
  `CLIENT_NO` bigint(32) DEFAULT NULL,
  `AGENT` varchar(32) DEFAULT NULL,
  `CLIENT_TELEPHONE` varchar(32) DEFAULT NULL,
  `TOUCH_CHANNEL` varchar(2) DEFAULT NULL,
  `TOUCH_TYPE` varchar(1) DEFAULT NULL,
  `TOUCH_TIME` datetime DEFAULT NULL,
  `TOUCH_OPERATOR` varchar(32) DEFAULT NULL,
  `TOUCH_NOTE` varchar(64) DEFAULT NULL,
  `VOICES_FILE` varchar(64) DEFAULT NULL,
  `VCHAR1` varchar(32) DEFAULT NULL,
  `VCHAR2` varchar(32) DEFAULT NULL,
  `VCHAR3` varchar(32) DEFAULT NULL,
  `VCHAR5` varchar(32) DEFAULT NULL,
  `VCHAR4` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`TOUCH_NO`),
  KEY `FK_Reference_2` (`CLIENT_NO`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=759 DEFAULT CHARSET=utf8;
 *
 */
public class ClientTouchRecord extends Model<ClientTouchRecord> {
	
	public static ClientTouchRecord dao = new ClientTouchRecord();
	
	public int getClientTouchRecordCountByCondition(int currentPage,int numPerPage,String telephone,String agent,String touchType,List<String> touchOperator,String startTime,String endTime) {
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[200];
		int index = 0;
		
		sb.append("select count(*) as count from client_touch_record where 1=1");
		
		if(!BlankUtils.isBlank(telephone)){
			sb.append(" and CLIENT_TELEPHONE=?");
			pars[index] = telephone;
			index++;
		}
		
		if(!BlankUtils.isBlank(agent)) {
			sb.append(" and AGENT=?");
			pars[index] = agent;
			index++;
		}
		
		if(!BlankUtils.isBlank(touchType)) {
			sb.append(" and TOUCH_TYPE=?");
			pars[index] = touchType;
			index++;
		}
		
		if(!BlankUtils.isBlank(touchOperator) && touchOperator.size()>0) {
			String condition = "";
			for(int i=0;i<touchOperator.size();i++) {
				condition += "?,";
			}
			condition = condition.substring(0,condition.length()-1);
			sb.append(" and TOUCH_OPERATOR in (" + condition + ")");
			for(int j=0;j<touchOperator.size();j++) {
				pars[index] = touchOperator.get(j);
				index++;
			}
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and TOUCH_TIME>=?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and TOUCH_TIME<=?");
			pars[index] = endTime + " 23:59:59";
			index++;
		}
		
		Record record = Db.findFirst(sb.toString(), ArrayUtils.copyArray(index, pars));
		return Integer.valueOf(record.get("count").toString());
	}
	
	public List<Record> getCLientTouchRecordByPaginate(int currentPage,int numPerPage,String telephone,String agent,String touchType,List<String> touchOperator,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[200];
		int index = 0;
		
		sb.append("select TOUCH_NO from client_touch_record where 1=1");
		
		if(!BlankUtils.isBlank(telephone)){
			sb.append(" and CLIENT_TELEPHONE=?");
			pars[index] = telephone;
			index++;
		}
		
		if(!BlankUtils.isBlank(agent)) {
			sb.append(" and AGENT=?");
			pars[index] = agent;
			index++;
		}
		
		if(!BlankUtils.isBlank(touchType) && !touchType.equalsIgnoreCase("2")) {
			sb.append(" and TOUCH_TYPE=?");
			pars[index] = touchType;
			index++;
		}
		
		/*if(!BlankUtils.isBlank(touchOperator)) {
			String[] opers = {"super","admin","root"};
			sb.append(" and TOUCH_OPERATOR in (?)");
			pars[index] = opers;
			index++;
		}*/
		if(!BlankUtils.isBlank(touchOperator) && touchOperator.size()>0) {
			String condition = "";
			for(int i=0;i<touchOperator.size();i++) {
				condition += "?,";
			}
			condition = condition.substring(0,condition.length()-1);
			sb.append(" and TOUCH_OPERATOR in (" + condition + ")");
			for(int j=0;j<touchOperator.size();j++) {
				pars[index] = touchOperator.get(j);
				index++;
			}
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and TOUCH_TIME>=?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and TOUCH_TIME<=?");
			pars[index] = endTime + " 23:59:59";
			index++;
		}
		
		sb.append(" order by TOUCH_NO desc");
		
		int s = (currentPage-1) * numPerPage;
		int e = s + numPerPage;
		
		sb.append(" limit " + s + "," + numPerPage);
		
		//这里查询出来的，只是TOUCH_NO的值，需要将这里的值取出
		List<Record> list = Db.find(sb.toString(), ArrayUtils.copyArray(index, pars));
		StringBuilder sbIds = new StringBuilder();  //拼凑 id
		for(Record r:list) {
			sbIds.append(r.get("TOUCH_NO") + ",");
		}
		
		String ids = sbIds.toString();
		if(!BlankUtils.isBlank(ids)) {    //如果ids 的结果不为空时，去掉最后一个逗号，并查询以 ids 为查询条件的 id 列表
			ids = ids.substring(0, ids.length()-1);
			
			String sql2 = "select * from client_touch_record where TOUCH_NO in(" + ids + ") order by TOUCH_NO desc";
			
			List<Record> list2 = Db.find(sql2);
			return list2;
		}else {                         //如果结果为空时，直接返回 null
			return new ArrayList<Record>();
		}
	}
	
	public Map getClientTouchRecordByPaginateToMap(int currentPage,int numPerPage,String telephone,String agent,String touchType,String touchOperator,String startTime,String endTime) {
		List<String> operators = new ArrayList<String>();
		if(!BlankUtils.isBlank(touchOperator)) {
			String[] opers = touchOperator.split(",");
			for(String oper:opers) {
				operators.add(oper);
			}
		}
		int total = getClientTouchRecordCountByCondition(currentPage, numPerPage, telephone,agent,touchType,operators,startTime,endTime);
		
		
		List<Record> list = getCLientTouchRecordByPaginate(currentPage, numPerPage, telephone,agent,touchType,operators,startTime,endTime);
		
		Map m = new HashMap();
		m.put("total", total);
		m.put("rows", list);
		
		return m;
	}
	
	public boolean add(Record touch) {
		
		boolean b = false;
		
		b = Db.save("client_touch_record", "TOUCH_NO", touch);
		
		return b;
	}
	
	
}
