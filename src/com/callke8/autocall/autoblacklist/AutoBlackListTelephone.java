package com.callke8.autocall.autoblacklist;

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

public class AutoBlackListTelephone extends Model<AutoBlackListTelephone> {
	
	private static final long serialVersionUID = 1L;
	
	public static AutoBlackListTelephone dao = new AutoBlackListTelephone();
	
	
	public Page<Record> getAutoBlackListTelephoneByPaginate(int pageNumber,int pageSize,String blackListId,String telephone,String clientName)
	{
	
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[6];
		int index = 0;
		
		sb.append("from ac_blacklist_telephone where 1=1");
		
		if(!BlankUtils.isBlank(blackListId)){  //即黑名单ID不为空
			sb.append(" and BLACKLIST_ID=?");
			pars[index] = blackListId;
			index++;
		}
		
		if(!BlankUtils.isBlank(telephone)) {
			sb.append(" and TELEPHONE like ?");
			pars[index] = "%" + telephone + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(clientName)) {
			sb.append(" and CLIENT_NAME like ?");
			pars[index] = "%" + clientName + "%";
			index++;
		}
		
		
		Page<Record> page = Db.paginate(pageNumber, pageSize, "select *", sb.toString() + " ORDER BY TEL_ID DESC", ArrayUtils.copyArray(index, pars));
		return page;
		
	}
	
	public Map<String,Object> getAutoBlackListTelephoneByPaginateToMap(int pageNumber,int pageSize,String blackListId,String telephone,String clientName) {
		
		Map<String,Object> m = new HashMap<String,Object>();
		
		if(BlankUtils.isBlank(blackListId)) {   //即是没有传入黑名单ID
			m.put("total", 0);
			m.put("rows", new ArrayList<Record>());
			return m;
		}
		
		Page<Record> page = getAutoBlackListTelephoneByPaginate(pageNumber,pageSize,blackListId,telephone,clientName);
		
		int total = page.getTotalRow();   //得到总数量
		m.put("total", total);
		m.put("rows", page.getList());
		
		return m;
	}
	
	
	/**
	 * 新增黑名单号码
	 * 
	 * @param autoBlackListTelephone
	 * @return
	 */
	public boolean add(Record autoBlackListTelephone) {
		
		boolean b = Db.save("ac_blacklist_telephone", "TEL_ID", autoBlackListTelephone);
		
		return b;
	}
	
	
	
	/**
	 * 批量添加记录
	 * 
	 * @param autoBlackListTelephones
	 * @return
	 */
	public int add(ArrayList<Record> autoBlackListTelephones) {
		
		int successCount = 0;
		
		for(Record tel:autoBlackListTelephones) {
			
			boolean b = add(tel);
			
			if(b) {
				successCount++;
			}
		}
		
		return successCount;
	}
	
	
	/**
	 * 根据黑名单Id，删除号码
	 * 
	 * @param blackListId
	 * @return
	 */
	public int deleteByBlackListId(String blackListId) {
		
		String sql = "delete from ac_blacklist_telephone where BLACKLIST_ID=?";
		
		int count = Db.update(sql, blackListId);
		
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
		
		String sql = "delete from ac_blacklist_telephone where TEL_ID=?";
		
		int[] delData = Db.batch(sql,"TEL_ID",list,200);
		
		return delData.length;
		
	}
	
	/**
	 * 批量添加数据
	 * 
	 * @param telephones
	 * @return
	 */
	public int batchSave(ArrayList<Record> telephones) {
		
		if(BlankUtils.isBlank(telephones) || telephones.size()==0) {
			return 0;
		}
		
		String sql = "insert into ac_blacklist_telephone(BLACKLIST_ID,TELEPHONE,CLIENT_NAME) values(?,?,?)";
		
		int[] insertData = Db.batch(sql,"BLACKLIST_ID,TELEPHONE,CLIENT_NAME",telephones,5000);
		
		return insertData.length;
		
	}
	
	public boolean update(String telephone,String clientName,int telId) {
		
		boolean b = false;
		
		String sql = "update ac_blacklist_telephone set TELEPHONE=?,CLIENT_NAME=? where TEL_ID=?";
		
		int count = Db.update(sql,telephone,clientName,telId);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	
	/**
	 * 根据条件，返回黑名单号码的数量
	 * 
	 * @param telephone
	 * @param blackListId
	 * @return
	 */
	public int getAutoBlackListTelephoneCountByCondition(String telephone,String blackListId) {
		
		String sql = "select count(*) as count from ac_blacklist_telephone where TELEPHONE=? and BLACKLIST_ID=?";
		
		Record record = Db.findFirst(sql,telephone,blackListId);
		
		Integer count = Integer.valueOf(record.get("count").toString());
		
		return count ;
	}
	
	
	/**
	 * 根据黑名单ID,取出所有黑名单号码信息,并单独将号码加入List<String>,并返回
	 * 用于黑名单检查
	 * 
	 * @param blackListId
	 * @return
	 */
	public List<String> getAutoBlackListTelephoneByBlackListId(String blackListId) {
		
		
		List<String> list = new ArrayList<String>();
		
		//如果传入的黑名单ID为空时,直接返回空 List
		if(BlankUtils.isBlank(blackListId)) {
			return list;
		}
		
		String sql = "select TELEPHONE from ac_blacklist_telephone where BLACKLIST_ID=?";
		
		List<Record> blackListTelephoneList = Db.find(sql,blackListId);
		
		for(Record blackListTelephone:blackListTelephoneList) {
			list.add(blackListTelephone.get("TELEPHONE").toString());
		}
		
		return list;
	}
	

}
