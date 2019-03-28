package com.callke8.cnn.cnncallindata;

import java.util.*;
import com.callke8.utils.*;
import com.jfinal.plugin.activerecord.*;

public class CnnCallinData extends Model<CnnCallinData>  {

	private static final long serialVersionUID = 1L;
	public static CnnCallinData dao = new CnnCallinData();

	public Page getCnnCallinDataByPaginate(int pageNumber,int pageSize,String callerId,String callee,String state,String customerNewTel,String startTime,String endTime) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from cnn_callin_data where 1=1");

		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(callerId)) {
			sb.append(" and CALLERID like ?");
			pars[index] = "%" + callerId + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(callee)) {
			sb.append(" and CALLEE like ?");
			pars[index] = "%" + callee + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(state) && !state.equals("empty")) {
			sb.append(" and STATE=?");
			pars[index] = state;
			index++;
		}
		
		if(!BlankUtils.isBlank(customerNewTel)) {
			sb.append(" and CUSTOMER_NEW_TEL like ?");
			pars[index] = "%" + customerNewTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CALL_DATE>=?");
			pars[index] = startTime;
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CALL_DATE<?");
			pars[index] = endTime;
			index++;
		}

		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY ID DESC",ArrayUtils.copyArray(index, pars));
		return p;
	}

	public Map getCnnCallinDataByPaginateToMap(int pageNumber,int pageSize,String callerId,String callee,String state,String customerNewTel,String startTime,String endTime) {

		Page<Record> p =  getCnnCallinDataByPaginate(pageNumber,pageSize,callerId,callee,state,customerNewTel,startTime,endTime);

		int total = p.getTotalRow();     //取出总数量
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", p.getList());

		return map;
	}
	
	public List<Record> getCnnCallinDataByCondition(String callerId,String callee,String state,String customerNewTel,String startTime,String endTime) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("select * from cnn_callin_data where 1=1");
		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(callerId)) {
			sb.append(" and CALLERID like ?");
			pars[index] = "%" + callerId + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(callee)) {
			sb.append(" and CALLEE like ?");
			pars[index] = "%" + callee + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(state) && !state.equals("empty")) {
			sb.append(" and STATE=?");
			pars[index] = state;
			index++;
		}
		
		if(!BlankUtils.isBlank(customerNewTel)) {
			sb.append(" and CUSTOMER_NEW_TEL like ?");
			pars[index] = "%" + customerNewTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CALL_DATE>=?");
			pars[index] = startTime;
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CALL_DATE<?");
			pars[index] = endTime;
			index++;
		}
		
		List<Record> list = Db.find(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		//定义一个新的List
		ArrayList<Record> newList = new ArrayList<Record>();
		
		for(Record r:list) {
			int stateValue = r.getInt("STATE");
			
			if(stateValue==1) {
				r.set("STATE_DESC","已改号");
			}else {
				r.set("STATE_DESC","未改号");
			}
			newList.add(r);
		}
		
		return newList;
	}
	
	public boolean add(CnnCallinData formData) {

		Record r = new Record();
		r.set("CALLERID", formData.get("CALLERID"));
		r.set("CALLEE", formData.get("CALLEE"));
		r.set("STATE", formData.get("STATE"));
		r.set("CALL_DATE", formData.get("CALL_DATE"));
		r.set("PK_CNN_DATA_ID", formData.get("PK_CNN_DATA_ID"));

		return add(r);
	}

	public boolean add(Record record) {

		boolean b = Db.save("cnn_callin_data", "ID", record);
		return b;

	}

	public boolean update(String callerId,String callee,String state,String callDate,String pkCnnDataId,int id) {

		boolean b = false;
		String sql = "update cnn_callin_data set CALLERID=?,CALLEE=?,STATE=?,CALL_DATE=?,PK_CNN_DATA_ID=? where ID=?";

		int count = Db.update(sql,callerId,callee,state,callDate,pkCnnDataId,id);
		if(count > 0) {
			b = true;
		}
		return b;

	}

	public CnnCallinData getCnnCallinDataById(int id){

		String sql = "select * from cnn_callin_data where ID=?";
		CnnCallinData entity = findFirst(sql, id);
		return entity;

	}

	public boolean deleteById(String id) {

		boolean b = false;
		int count = 0;
		count = Db.update("delete from cnn_callin_data where ID=?",id);
		if(count > 0) {
			b = true;
		}
		return b;

	}
}
