package com.callke8.cnn.cnndata;

import java.util.*;

import com.callke8.system.operator.Operator;
import com.callke8.utils.*;
import com.jfinal.plugin.activerecord.*;

public class CnnData extends Model<CnnData>  {

	private static final long serialVersionUID = 1L;
	public static CnnData dao = new CnnData();

	public Page getCnnDataByPaginate(int pageNumber,int pageSize,String customerName,String customerTel,String customerNewTel,String flag,String startTime,String endTime) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from cnn_data where 1=1");

		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(customerName)) {
			sb.append(" and CUSTOMER_NAME like ?");
			pars[index] = "%" + customerName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(customerTel)) {
			sb.append(" and CUSTOMER_TEL like ?");
			pars[index] = "%" + customerTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(customerNewTel)) {
			sb.append(" and CUSTOMER_NEW_TEL like ?");
			pars[index] = "%" + customerNewTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(flag) && !flag.equals("empty")) {
			sb.append(" and FLAG=?");
			pars[index] = flag;
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CREATE_TIME>?");
			pars[index] = startTime;
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CREATE_TIME<?");
			pars[index] = endTime;
			index++;
		}

		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY ID DESC",ArrayUtils.copyArray(index, pars));
		return p;
	}

	public Map getCnnDataByPaginateToMap(int pageNumber,int pageSize,String customerName,String customerTel,String customerNewTel,String flag,String startTime,String endTime) {

		Page<Record> p =  getCnnDataByPaginate(pageNumber,pageSize,customerName,customerTel,customerNewTel,flag,startTime,endTime);

		int total = p.getTotalRow();     //取出总数量
		
		ArrayList<Record> newList = new ArrayList<Record>();
		
		for(Record r:p.getList()) {
			
			String uc = r.getStr("CREATE_USERCODE");   //取出创建人工号
			Operator oper = Operator.dao.getOperatorByOperId(uc);
			if(!BlankUtils.isBlank(oper)) {
				r.set("CREATE_USERCODE_DESC", oper.get("OPER_NAME") + "(" + uc + ")");
			};
			
			newList.add(r);
		}

		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", newList);

		return map;
	}
	
	/**
	 * 根据条件查询改号数据(主要用于导出 Excel)
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param customerName
	 * @param customerTel
	 * @param customerNewTel
	 * @param flag
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<Record> getCnnDataByCondition(String customerName,String customerTel,String customerNewTel,String flag,String startTime,String endTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("select * from cnn_data where 1=1");
		
		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(customerName)) {
			sb.append(" and CUSTOMER_NAME like ?");
			pars[index] = "%" + customerName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(customerTel)) {
			sb.append(" and CUSTOMER_TEL like ?");
			pars[index] = "%" + customerTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(customerNewTel)) {
			sb.append(" and CUSTOMER_NEW_TEL like ?");
			pars[index] = "%" + customerNewTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(flag) && !flag.equals("empty")) {
			sb.append(" and FLAG=?");
			pars[index] = flag;
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CREATE_TIME>?");
			pars[index] = startTime;
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CREATE_TIME<?");
			pars[index] = endTime;
			index++;
		}
		
		List<Record> list = Db.find(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		//定义一个新的列表变量，用于存储
		ArrayList<Record> newList = new ArrayList<Record>();
		
		for(Record r:list) {
			String uc = r.getStr("CREATE_USERCODE");   //取出创建人工号
			Operator oper = Operator.dao.getOperatorByOperId(uc);
			if(!BlankUtils.isBlank(oper)) {
				r.set("CREATE_USERCODE_DESC", oper.get("OPER_NAME") + "(" + uc + ")");
			}
			
			String flagValue = r.get("FLAG");
			if(flagValue.equals("1")) {
				r.set("FLAG_DESC", "中文");
			}else {
				r.set("FLAG_DESC", "英文");
			}
			
			newList.add(r);
		}
		
		return newList;
		
	}
	
	public boolean add(CnnData formData) {

		Record r = new Record();
		r.set("CUSTOMER_NAME", formData.get("CUSTOMER_NAME"));
		r.set("CUSTOMER_TEL", formData.get("CUSTOMER_TEL"));
		r.set("CUSTOMER_NEW_TEL", formData.get("CUSTOMER_NEW_TEL"));
		r.set("CREATE_USERCODE", formData.get("CREATE_USERCODE"));
		r.set("CREATE_TIME", formData.get("CREATE_TIME"));
		r.set("FLAG", formData.get("FLAG"));

		return add(r);
	}

	public boolean add(Record record) {

		boolean b = Db.save("cnn_data", "ID", record);
		return b;

	}

	public boolean update(String customerName,String customerTel,String customerNewTel,String flag,int id) {

		boolean b = false;
		String sql = "update cnn_data set CUSTOMER_NAME=?,CUSTOMER_TEL=?,CUSTOMER_NEW_TEL=?,FLAG=? where ID=?";

		int count = Db.update(sql,customerName,customerTel,customerNewTel,flag,id);
		if(count > 0) {
			b = true;
		}
		return b;

	}

	public CnnData getCnnDataById(String id){

		String sql = "select * from cnn_data where ID=?";
		CnnData entity = findFirst(sql, id);
		return entity;

	}

	public boolean deleteById(String id) {

		boolean b = false;
		int count = 0;
		count = Db.update("delete from cnn_data where ID=?",id);
		if(count > 0) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 删除数据根据ID列表
	 * 
	 * @param idList
	 * @return
	 */
	public int deleteByIdList(String idList) {
		
		String sql = "delete from cnn_data where ID in(" + idList + ")";
		int count = Db.update(sql);
		return count;
	}
	
	/**
	 * 根据客户号码查询记录
	 * 
	 * @return
	 */
	public Record getCnnDataByCustomerTel(String customerTel) {
		
		String sql = "select * from cnn_data where CUSTOMER_TEL=?";
		
		Record r = Db.findFirst(sql, customerTel);
		
		return r;
	}
	
	/**
	 * 取出所有的改号数据
	 * @return
	 */
	public List<Record> getAllCnnData() {
		
		String sql = "select * from cnn_data order by ID DESC";
		
		List<Record> list = Db.find(sql);
		
		return list;
		
	}
	
	/**
	 * 批量将主叫号码插入到数据库
	 * @param callerIdList
	 * @return
	 */
	public int[] batchSave(ArrayList<Record> cnnDataList) {
		if(BlankUtils.isBlank(cnnDataList) || cnnDataList.size()==0) {
			return null;
		}
		
		String sql = "insert into cnn_data(CUSTOMER_NAME,CUSTOMER_TEL,CUSTOMER_NEW_TEL,FLAG,CHANGE_TIME,CREATE_USERCODE,CREATE_TIME)values(?,?,?,?,?,?,?)";
		
		int[] insertData = Db.batch(sql,"CUSTOMER_NAME,CUSTOMER_TEL,CUSTOMER_NEW_TEL,FLAG,CHANGE_TIME,CREATE_USERCODE,CREATE_TIME", cnnDataList, 1000);
		
		return insertData;
	}
	
}
