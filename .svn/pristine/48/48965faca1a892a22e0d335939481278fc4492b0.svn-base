package com.callke8.system.dict;

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

/**
 * 数据字典组
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
public class DictGroup extends Model<DictGroup> {

	public static DictGroup dao = new DictGroup();
	
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
	public Page<Record> getDictGroupByPaginate(int currentPage,int numPerPage,String groupCode,String groupName,String state) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[3];
		int index = 0;
		
		sb.append("from sys_dict_group where 1=1");
		
		if(!BlankUtils.isBlank(groupCode)) {
			sb.append(" and GROUP_CODE like ?");
			pars[index] = "%" + groupCode + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(groupName)) {
			sb.append(" and GROUP_NAME like ?");
			pars[index] = "%" + groupName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(state) && !state.equalsIgnoreCase("2")) {
			sb.append(" and STATE=?");
			pars[index] = state;
			index++;
		}
		
		Page<Record> p = Db.paginate(currentPage, numPerPage,"select *",sb.toString(),ArrayUtils.copyArray(index, pars));
		
		return p;
		
	}
	
	@SuppressWarnings("unchecked")
	public Map getDictGroupByPaginateToMap(int currentPage,int numPerPage,String groupCode,String groupName,String state) {
		Map m = new HashMap();
		
		Page p = getDictGroupByPaginate(currentPage, numPerPage, groupCode, groupName, state);
		
		m.put("total",p.getTotalRow());
		m.put("rows", p.getList());
		
		return m;
	}
	
	/**
	 * 批量删除数据字典
	 * 
	 * @param ids
	 * @return
	 */
	public int batchDelete(String ids) {
		
		if(BlankUtils.isBlank(ids)) {
			return 0;
		}
		
		ArrayList<Record> list = new ArrayList<Record>();   //先创建一个record列表，主要是为了批量删除数据做准备
		
		String[] idList = ids.split(",");    //以逗号分隔ID
		
		for(String id:idList) {
			//先根据数据字典项，删除所有的数据字典项
			DictItem.dao.delete(id, null);
			
			Record dictGroup = new Record();
			dictGroup.set("GROUP_CODE", id);
			list.add(dictGroup);
		}
		
		String sql = "delete from sys_dict_group where GROUP_CODE=?";
		
		int[] delData = Db.batch(sql, "GROUP_CODE", list, 100);
		
		return delData.length;
	}
	
	/**
	 * 更新数据字典
	 * 
	 * @param dictGroup
	 * @return
	 */
	public int update(DictGroup dictGroup) {
		
		String groupName = dictGroup.get("GROUP_NAME");
		String groupCode = dictGroup.get("GROUP_CODE");
		String groupDesc = dictGroup.get("GROUP_DESC");
		String state = dictGroup.get("STATE");
		
		
		String sql = "update sys_dict_group set GROUP_NAME=?,GROUP_DESC=?,STATE=? where GROUP_CODE=?";
		
		int count = Db.update(sql,groupName,groupDesc,state,groupCode);
		return count;
	}
	
	/**
	 * 添加数据字典组
	 * 
	 * @param dictGroup
	 * @return
	 */
	public boolean add(DictGroup dictGroup) {
		
		boolean b = false;
		
		if(dictGroup.save()) {
			b = true;
		}
		return b;
	}
	
	/**
	 * 根据groupCode检查记录的数量
	 * @param groupCode
	 * @return
	 */
	public int checkDictGroup(String groupCode) {
		
		String sql = "select count(*) as count from sys_dict_group where GROUP_CODE=?";
		
		Record record = Db.findFirst(sql, groupCode);
		
		int count = Integer.valueOf(record.get("count").toString());
		
		return count;
	}
	
	/**
	 * 加载所有的数据字典数据，并写入内存
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map loadDictInfo() {
		Map<String,List<Record>> map = new HashMap<String,List<Record>>();
		String sql = "select * from sys_dict_group";
		
		List<Record> list = Db.find(sql);    //查询所有的数据字典组的记录
		
		//遍历记录集
		for(Record record:list) {
			//取得groupCode
			String groupCode = record.get("GROUP_CODE");
			
			List<Record> dictItemList = DictItem.dao.getDictItemByGroupCode(groupCode);
			
			map.put(groupCode, dictItemList);
		}
		
		return map;
	}
	
}
