package com.callke8.autocall.autonumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AutoNumberTelephone extends Model<AutoNumberTelephone> {
	
	private static final long serialVersionUID = 1L;
	
	public static AutoNumberTelephone dao = new AutoNumberTelephone();
	
	
	public Page<Record> getAutoNumberTelephoneByPaginate(int pageNumber,int pageSize,String numberId,String customerTel,String customerName)
	{
	
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[6];
		int index = 0;
		
		sb.append("from ac_number_telephone where 1=1");
		
		if(!BlankUtils.isBlank(numberId)){  //即黑名单ID不为空
			sb.append(" and NUMBER_ID=?");
			pars[index] = numberId;
			index++;
		}
		
		if(!BlankUtils.isBlank(customerTel)) {
			sb.append(" and CUSTOMER_TEL like ?");
			pars[index] = "%" + customerTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(customerName)) {
			sb.append(" and CUSTOMER_NAME like ?");
			pars[index] = "%" + customerName + "%";
			index++;
		}
		
		
		Page<Record> page = Db.paginate(pageNumber, pageSize, "select *", sb.toString() + " ORDER BY TEL_ID DESC", ArrayUtils.copyArray(index, pars));
		return page;
		
	}
	
	public Map<String,Object> getAutoNumberTelephoneByPaginateToMap(int pageNumber,int pageSize,String numberId,String customerTel,String customerName) {
		
		Map<String,Object> m = new HashMap<String,Object>();
		
		if(BlankUtils.isBlank(numberId)) {   //即是没有传入黑名单ID
			m.put("total", 0);
			m.put("rows", new ArrayList<Record>());
			return m;
		}
		
		Page<Record> page = getAutoNumberTelephoneByPaginate(pageNumber,pageSize,numberId,customerTel,customerName);
		
		int total = page.getTotalRow();   //得到总数量
		m.put("total", total);
		m.put("rows", page.getList());
		
		return m;
	}
	
	/**
	 * 根据传入的 numberId,customerTel,customerName 取出所有符合条件的记录
	 * 
	 * 主要是用于导出号码数据到 Excel 表
	 * 
	 * @param numberId
	 * @param customerTel
	 * @param customerName
	 * @return
	 */
	public List<Record> getAutoNumberTelephoneByCondition(String numberId,String customerTel,String customerName)
	{
	
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[6];
		int index = 0;
		
		sb.append("select * from ac_number_telephone where 1=1");
		
		if(!BlankUtils.isBlank(numberId)){  //即黑名单ID不为空
			sb.append(" and NUMBER_ID=?");
			pars[index] = numberId;
			index++;
		}
		
		if(!BlankUtils.isBlank(customerTel)) {
			sb.append(" and CUSTOMER_TEL like ?");
			pars[index] = "%" + customerTel + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(customerName)) {
			sb.append(" and CUSTOMER_NAME like ?");
			pars[index] = "%" + customerName + "%";
			index++;
		}
		
		sb.append(" ORDER BY TEL_ID DESC");
		
		List<Record> list = Db.find(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		return list;
		
	}
	
	
	/**
	 * 新增号码组号码
	 * 
	 * @param autoBlackListTelephone
	 * @return
	 */
	public boolean add(Record autoNumberTelephone) {
		
		boolean b = Db.save("ac_number_telephone", "TEL_ID", autoNumberTelephone);
		
		return b;
	}
	
	
	
	/**
	 * 批量添加记录
	 * 
	 * @param autoNumberTelephones
	 * @return
	 */
	public int add(ArrayList<Record> autoNumberTelephones) {
		
		int successCount = 0;
		
		for(Record tel:autoNumberTelephones) {
			
			boolean b = add(tel);
			
			if(b) {
				successCount++;
			}
		}
		
		return successCount;
	}
	
	
	/**
	 * 根据号码组Id，删除号码
	 * 
	 * @param numberId
	 * @return
	 */
	public int deleteByNumberId(String numberId) {
		
		String sql = "delete from ac_number_telephone where NUMBER_ID=?";
		
		int count = Db.update(sql, numberId);
		
		return count;
	}
	
	
	/**
	 * 批量删除号码记录
	 * 
	 * @param ids
	 * @return
	 */
	public int batchDelete(String ids) {
		
		if(BlankUtils.isBlank(ids)) {   //如果传入为空，不作处理
			return 0;
		}
		
		ArrayList<Record> list = new ArrayList<Record>();
		
		String[] idList = ids.split(",");   //以逗号分隔
		
		for(String id:idList) {
			
			Record tel = new Record();
			
			tel.set("TEL_ID", id);
			
			list.add(tel);
		}
		
		String sql = "delete from ac_number_telephone where TEL_ID=?";
		
		int[] delData = Db.batch(sql,"TEL_ID",list,200);
		
		return delData.length;
		
	}
	
	/**
	 * 批量添加数据
	 * 
	 * @param customerTel
	 * @return
	 */
	public int batchSave(ArrayList<Record> telephones) {
		
		if(BlankUtils.isBlank(telephones) || telephones.size()==0) {
			return 0;
		}
		
		String sql = "insert into ac_number_telephone(NUMBER_ID,CUSTOMER_TEL,CUSTOMER_NAME) values(?,?,?)";
		
		int[] insertData = Db.batch(sql,"NUMBER_ID,CUSTOMER_TEL,CUSTOMER_NAME",telephones,5000);
		
		return insertData.length;
		
	}
	
	public boolean update(String customerTel,String customerName,int telId) {
		
		boolean b = false;
		
		String sql = "update ac_number_telephone set CUSTOMER_TEL=?,CUSTOMER_NAME=? where TEL_ID=?";
		
		int count = Db.update(sql,customerTel,customerName,telId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	
	/**
	 * 根据条件，返回号码组号码的数量
	 * 
	 * @param customerTel
	 * @param numberId
	 * @return
	 */
	public int getAutoNumberTelephoneCountByCondition(String customerTel,String numberId) {
		
		String sql = "select count(*) as count from ac_number_telephone where CUSTOMER_TEL=? and NUMBER_ID=?";
		
		Record record = Db.findFirst(sql,customerTel,numberId);
		
		Integer count = Integer.valueOf(record.get("count").toString());
		
		return count ;
	}
	
	
	/**
	 * 根据号码组ID,取出所有号码组号码信息,并以 Record 列表方式返回
	 * 用于号码组检查
	 * 
	 * @param numberId
	 * @return
	 */
	public List<Record> getAutoNumberTelephoneByNumberId(String numberId) {
		
		
		List<Record> list = new ArrayList<Record>();
		
		//如果传入的号码组ID为空时,直接返回空 List
		if(BlankUtils.isBlank(numberId)) {
			return list;
		}
		
		String sql = "select CUSTOMER_TEL,CUSTOMER_NAME from ac_number_telephone where NUMBER_ID=?";
		
		list = Db.find(sql,numberId);
		
		return list;
	}
	

}
