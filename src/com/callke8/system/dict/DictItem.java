package com.callke8.system.dict;

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
 * 数据字典项
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
public class DictItem extends Model<DictItem> {
	
	public static DictItem dao = new DictItem();
	
	/**
	 * 根据数据字典组及数据字典项，删除数据字典项，如果数据字典项为空时，则是根据数据字典组删除所有的项
	 * 
	 * @param groupCode
	 * 			  数据字典组编码
	 * @param dictCode
	 * 			　数据字典项编码
	 * @return
	 */
	public int delete(String groupCode,String dictCode) {
		
		int count = 0;
		
		String sql = "delete from sys_dict_item where GROUP_CODE=?";
		if(!BlankUtils.isBlank(dictCode)) {
			sql += " and DICT_CODE=?";
		}
		
		if(!BlankUtils.isBlank(dictCode)) {
			count = Db.update(sql,groupCode,dictCode);
		}else {
			count = Db.update(sql,groupCode);
		}
		
		return count;
	}
	
	/**
	 * 添加数据字典项
	 * 
	 * @param dictItem
	 * @return
	 */
	public boolean add(DictItem dictItem) {
		
		boolean b = false;
		
		if(dictItem.save()) {
			b = true;
		}
		
		return b;
	}
	
	public int update(DictItem dictItem,String groupCode) {
		
		String dictCode = dictItem.get("DICT_CODE");
		String dictName = dictItem.get("DICT_NAME");
		String dictDesc = dictItem.get("DICT_DESC");
		
		String sql = "update sys_dict_item set DICT_NAME=?,DICT_DESC=? where DICT_CODE=? and GROUP_CODE=?";
		
		int count = Db.update(sql, dictName,dictDesc,dictCode,groupCode);
		
		return count; 
	}
	
	/**
	 * 根据dictCode及groupCode，查看已有记录数
	 * 
	 * @param dictCode
	 * @param groupCode
	 * @return
	 */
	public int checkDictItem(String dictCode,String groupCode) {
		
		String sql = "select count(*) as count from sys_dict_item where DICT_CODE=? and GROUP_CODE=?";
		
		Record record = Db.findFirst(sql,dictCode,groupCode);
		
		int count = Integer.valueOf(record.get("count").toString());
		
		return count;
	}
	
	/**
	 * 按页查询
	 * 
	 * 查询回来的结果已经附带
	 * list   即是查询当前页内容
	 * pageNumber 返回当前页码
	 * pageSize   返回每页显示数量
	 * totalPage  总页数
	 * totalRow   总的数量
	 */
	public Page<Record> getDictItemByPaginate(int currentPage,int numPerPage,String groupCode) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[1];
		int index = 0;
		
		sb.append("from sys_dict_item where 1=1");
		
		if(!BlankUtils.isBlank(groupCode)) {
			sb.append(" and GROUP_CODE=?");
			pars[index] = groupCode;
			index++;
		}
		
		Page<Record> p = Db.paginate(currentPage, numPerPage,"select *",sb.toString() + " ORDER BY -DICT_CODE DESC",ArrayUtils.copyArray(index, pars));
		
		return p;
		
	}
	
	@SuppressWarnings("unchecked")
	public Map getDictItemByPaginateToMap(int currentPage,int numPerPage,String groupCode) {
		Map m = new HashMap();
		
		Page p = getDictItemByPaginate(currentPage, numPerPage, groupCode);
		
		m.put("total",p.getTotalRow());
		m.put("rows", p.getList());
		
		return m;
	}
	
	/**
	 * 根据groupCode取得所有的数据字典项
	 * 
	 * @param groupCode
	 * @return
	 */
	public List<Record> getDictItemByGroupCode(String groupCode) {
		
		String sql = "select * from sys_dict_item where GROUP_CODE=? ORDER BY -DICT_CODE DESC";
		
		List<Record> list = Db.find(sql,groupCode);
		
		return list;
	}
	
}
