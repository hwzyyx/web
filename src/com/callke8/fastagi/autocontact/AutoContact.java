package com.callke8.fastagi.autocontact;

import java.util.HashMap;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动接触管理
 * 
 * @author hwz
 */
public class AutoContact extends Model<AutoContact> {

	private static final long serialVersionUID = 1L;
	
	public static final AutoContact dao = new AutoContact();
	/**
	 * 分页查询
	 * 
	 * @param currentPage
	 * 			当前页码
	 * @param numPerPage
	 * 			每页显示数量
	 * @return
	 */
	public Page<Record> getAutoContactByPaginate(int currentPage,int numPerPage,String contactName,String agentNumber,String clientNumber,String identifier,String callerId) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("from auto_contact where 1=1");
		
		if(!BlankUtils.isBlank(contactName)) {
			sb.append(" and CONTACT_NAME like ?");
			pars[index] = "%" + contactName + "%";
			index++;
		}
		
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
		
		Page<Record> p = Db.paginate(currentPage, numPerPage, "select *", sb.toString(),ArrayUtils.copyArray(index,pars));
		
		return p;
	}
	
	/**
	 * 分页查询以Map返回
	 * 
	 * @param currentPage
	 * @param numPerPage
	 * @return
	 */
	public Map getAutoContactByPaginateToMap(int currentPage,int numPerPage,String contactName,String agentNumber,String clientNumber,String identifier,String callerId) {
		
		Page<Record> p = getAutoContactByPaginate(currentPage,numPerPage,contactName,agentNumber,clientNumber,identifier,callerId);
		
		int total = p.getTotalRow();
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", p.getList());
		
		return map;
		
	}
	
	public Record getAutoContactByIdentifier(String identifier) {
		
		if(BlankUtils.isBlank(identifier)) {
			return null;
		}
		
		String sql = "select * from auto_contact where IDENTIFIER=?";
		
		Record record = Db.findFirst(sql, identifier);
		
		return record;
	}
	
	public boolean add(Record record) {
		
		boolean b = false;
		
		b = Db.save("auto_contact", "ID", record);
		
		return b;
	}
	
	public boolean deleteById(String id) {
		
		String sql = "delete from auto_contact where ID=?";
		
		boolean b = false;
		int count = 0;
		
		count = Db.update(sql, id);
		
		if(count>0) {
			b = true;
		}
		return b;
	}
	
	public boolean delete(AutoContact ac) {
		
		if(BlankUtils.isBlank(ac)) {
			return false;
		}
		
		String id = ac.get("ID");
		
		return deleteById(id);
	}
	
	public boolean update(AutoContact ac) {
		
		boolean b = false;
		
		String sql = "update auto_contact set CONTACT_NAME=?,AGENT_NUMBER=?,CLIENT_NUMBER=?,IDENTIFIER=?";
		
		sql += ",CALLERID=?,URL_INFO=?,MEMO=? where ID=?";
		
		
		int count = Db.update(sql, ac.get("CONTACT_NAME"),ac.get("AGENT_NUMBER"),ac.get("CLIENT_NUMBER"),ac.get("IDENTIFIER"),ac.get("CALLERID"),ac.get("URL_INFO"),ac.get("MEMO"),ac.get("ID"));
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据传入的实体和类型（add OR update）来判断是否已经有相同的识别符
	 * 
	 * @param ac 
	 * 			传入的
	 * @param type
	 * 			add OR update 即添加和修改
	 * @return
	 */
	public boolean checkIdentifier(AutoContact ac,String type) {
		
		boolean b = false;
		
		String identifier = ac.get("IDENTIFIER");   //识别符
		String sql = null;
		Record r = null;
		
		if(type.equals("add")) {                    //如果类型为添加时
			sql = "select count(*) as count from auto_contact where IDENTIFIER=?";
			r = Db.findFirst(sql, identifier);
		}else {                                     //如果类型为修改时
			int id = ac.getInt("ID");
			sql = "select count(*) as count from auto_contact where IDENTIFIER=? and ID!=?";
			r = Db.findFirst(sql, identifier,id);
		}
		
		int count = Integer.valueOf(r.get("count").toString());
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	
}
