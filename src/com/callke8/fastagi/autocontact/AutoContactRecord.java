package com.callke8.fastagi.autocontact;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AutoContactRecord extends Model<AutoContactRecord> {

	private static final long serialVersionUID = 1L;
	
	public static final AutoContactRecord dao = new AutoContactRecord();
	
	public boolean add(Record record) {
		
		boolean b = false;
		
		b = Db.save("auto_contact_record", "ID", record);
		
		return b;
		
	}
	
	/**
	 * 修改自动接触记录状态
	 * 
	 * @param id
	 * 			自动接触记录的ID
	 * @param status
	 * 			状态值,0:新建(未外呼);1:已加载;2:成功;3:未接;4:
	 * @param isUpdateExecuteTime
	 * 			是否更新执行时间,如果要更新时间，则用当前时间
	 * @return
	 */
	public boolean updateStatus(String status,int id,boolean isUpdateExecuteTime) {
		boolean b = false;
		
		String sql = null;
		int count = 0;
		if(isUpdateExecuteTime) {
			sql = "update auto_contact_record set STATUS=?,EXECUTE_TIME=? where ID=?";
			count = Db.update(sql, status,DateFormatUtils.getCurrentDate(),id);
		}else {
			sql = "update auto_contact_record set STATUS=? where ID=?";
			count = Db.update(sql,status,id);
		}
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 修改传入的ID的录音文件名字
	 * 
	 * @param fileName
	 * @param id
	 * @return
	 */
	public boolean updateVoiceFile(String fileName,int id) {
		
		boolean b = false;
		
		String sql = "update auto_contact_record set FILE_NAME=? where ID=?";
		
		int count = Db.update(sql,fileName,id);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	public Page<Record> getAutoContactRecordByPaginate(int currentPage,int numPerPage,String agentNumber,String clientNumber,String identifier,String callerId,String status,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[7];
		int index = 0;
		
		sb.append("from auto_contact_record where 1=1");
		
		if(!BlankUtils.isBlank(agentNumber)) {
			sb.append(" and AGENT_NUMBER like ?");
			pars[index] = "%" + agentNumber + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(clientNumber)) {
			sb.append(" and CLIENT_NUMBER like ?");
			pars[index] = "%" + clientNumber + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(identifier)) {
			sb.append(" and IDENTIFIER like ?");
			pars[index] = "%" + identifier + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(callerId)) {
			sb.append(" and CALLERID like ?");
			pars[index] = "%" + callerId + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(status) && !status.equalsIgnoreCase("5")) {
			sb.append(" and STATUS=?");
			pars[index] = status;
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and EXECUTE_TIME>=?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and EXECUTE_TIME<=?");
			pars[index] = endTime + " 23:59:59";
			index++;
		}
		
		Page<Record> p = Db.paginate(currentPage, numPerPage, "select *", sb.toString() + " ORDER BY ID DESC",ArrayUtils.copyArray(index,pars));
		
		return p;
		
	}
	
	public Map getAutoContactRecordByPaginateToMap(int currentPage,int numPerPage,String agentNumber,String clientNumber,String identifier,String callerId,String status,String startTime,String endTime) {
		
		Page<Record> page = getAutoContactRecordByPaginate(currentPage,numPerPage,agentNumber,clientNumber,identifier,callerId,status,startTime,endTime);
		
		int total = page.getTotalRow();
		
		Map m = new HashMap();
		
		//定义一个新的 ArrayList 用于将查询的数据设置路径和录音名字
		List<Record> list = page.getList();
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:list) {
			
			r.set("path",MemoryVariableUtil.autoContactMap.get("autoContactRecordDir"));  //设置路径
			//先判断录音文件是否存在
			if(!BlankUtils.isBlank(r.getStr("FILE_NAME"))) {
				
				File file = new File(PathKit.getWebRootPath() + "/" + r.getStr("path") + "/" + r.getStr("FILE_NAME")); 
				
				if(file.exists()) {
					r.set("recordingfile",r.getStr("FILE_NAME"));
				}
			}else {
				r.set("recordingfile", "");
			}
			
			newList.add(r);
			
		}
		
		m.put("total", total);
		m.put("rows", newList);
		
		return m;
	}
	
	/**
	 * 扫描并取出所有的未外呼的自动接触记录，取出后，然后修改状态为已扫描
	 * 
	 * @return
	 */
	public List<Record> scanNoCallAutoContactRecord() {
		
		String sql = "select * from auto_contact_record where STATUS=0";
		
		//System.out.println("Db.find(sql)-----" + Db.queryStr(sql));
		//System.out.println("-------------------");
		List<Record> list = Db.find(sql);
		if(!BlankUtils.isBlank(list)&&list.size()>0){  //如果查询结果不为空时
			for(Record r:list) {
				updateStatus("1", r.getInt("ID"),true);     //修改状态，0：新建；1：已扫描；2：成功；3：失败；
			}
		}
		
		return list;
	}
	
}
