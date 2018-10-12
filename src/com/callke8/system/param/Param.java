package com.callke8.system.param;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 博世家电呼叫参数配置DAO类
 * 
 * @author 黄文周
 *
 */
public class Param extends Model<Param> {
	
	private static final long serialVersionUID = 1L;
	
	public static Param dao = new Param();
	
	/**
	 * 以分页形式查找数据
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param paramType
	 * @param paramCode
	 * @param paramName
	 * @return
	 */
	public Page<Record> getParamByPaginate(int pageNumber,int pageSize,int paramType,String paramCode,String paramName) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append(" from sys_param where 1=1");
		
		if(paramType>=1) {
			sb.append(" and PARAM_TYPE=?");
			pars[index] = paramType;
			index++;
		}
		
		if(!BlankUtils.isBlank(paramCode)) {
			sb.append(" and PARAM_CODE like ?");
			pars[index] = "%" + paramCode + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(paramName)) {
			sb.append(" and PARAM_NAME like ?");
			pars[index] = "%" + paramName + "%";
			index++;
		}
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString(),ArrayUtils.copyArray(index, pars));
		return p;
		
	}
	
	/**
	 * 以分页的形式查询数据并按一定格式放至一个 Map 中
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param paramType
	 * @param paramCode
	 * @param paramName
	 * @return
	 */
	public Map<String,Object> getParamByPaginateToMap(int pageNumber,int pageSize,int paramType,String paramCode,String paramName) {
		
		Map<String,Object> m = new HashMap<String,Object>();
		
		Page<Record> p = getParamByPaginate(pageNumber,pageSize,paramType,paramCode,paramName);
		
		int total = p.getTotalRow();    //得到查询总量
		
		m.put("total", total);
		m.put("rows", p.getList());
		
		return m;
	}
	
	/**
	 * 新增呼叫参数
	 * 
	 * @param bshCallParam
	 * @return
	 */
	public boolean add(Param param) {
		boolean b = param.save();
		if(b) {
			//如果新增成功，就需要重新加载数据到内存中
			loadParamDataToMemory();
		}
		return b;
	}
	
	/**
	 * 根据ParamType,ParamCode 取出一个系统参数
	 * 
	 * @param paramType
	 * @param paramCode
	 * @return
	 */
	public Record getParamByParamTypeAndParamCode(int paramType,String paramCode) {
		
		String sql = "select * from sys_param where PARAM_TYPE=? and PARAM_CODE=?";
		
		Record param = Db.findFirst(sql,paramType,paramCode);
		
		return param;
	}
	
	/**
	 * 取出所有的配置参数
	 * 
	 * @return
	 */
	public List<Record> getAllParam() {
		
		String sql = "select * from sys_param order by PARAM_TYPE ASC";
		
		List<Record> list = Db.find(sql);
		
		return list;
	}
	
	/**
	 * 修改呼叫参数
	 * 
	 * @param bshCallParam
	 * @return
	 */
	public boolean update(Param param) {
		
		boolean b = false;
		int count = 0;
		
		String sql = "update sys_param set PARAM_NAME=?,PARAM_VALUE=?,PARAM_DESC=? where PARAM_CODE=? and PARAM_TYPE=?";
		
		count = Db.update(sql,param.get("PARAM_NAME"),param.get("PARAM_VALUE"),param.get("PARAM_DESC"),param.get("PARAM_CODE"),param.getInt("PARAM_TYPE"));
		
		if(count == 1) {
			b = true;
			//如果修改成功，则需要重新将数据加载到内存
			loadParamDataToMemory();
		}
		
		return b;
	}
	
	/**
	 * 根据参数类型和参数编码删除数据
	 * 
	 * @param paramType
	 * @param paramCode
	 * @return
	 */
	public boolean deleteByParamTypeAndParamCode(int paramType,String paramCode) {
		
		boolean b = false;
		int count = 0;
		
		String sql = "delete from sys_param where PARAM_TYPE=? and PARAM_CODE=?";
		
		count = Db.update(sql, paramType,paramCode);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 删除系统参数
	 * 
	 * @param param
	 * @return
	 */
	public boolean delete(Param param) {
		
		if(BlankUtils.isBlank(param) || BlankUtils.isBlank(param.get("PARAM_CODE")) || BlankUtils.isBlank(param.getInt("PARAM_TYPE"))) {
			return false;
		}
		
		int paramType = param.getInt("PARAM_TYPE");
		String paramCode = param.get("PARAM_CODE");
		
		return deleteByParamTypeAndParamCode(paramType,paramCode);
	}
	
	/**
	 * 加载博世电器的呼叫参数到内存
	 */
	public void loadParamDataToMemory() {
		
		List<Record> cps = getAllParam();
		
		if(BlankUtils.isBlank(cps)) {
			System.out.println("错误：=======-加载系统参数数据到内存失败,sys_param 表数据为空,请添加数据后,再重新启动进行加载!");
			return;
		}
		
		for(Record cp:cps) {
			String paramCode = cp.getStr("PARAM_CODE");     //配置参数编码
			String paramName = cp.getStr("PARAM_NAME");     //配置参数名称
			String paramValue = cp.getStr("PARAM_VALUE");   //配置参数赋值
			int paramType = cp.getInt("PARAM_TYPE");     //参数类型: 1:全局参数；2：TTS参数；3：博世家电参数；4：自动外呼参数
			//任意一个值为空时,进入下一个循环
			if(BlankUtils.isBlank(paramCode) || BlankUtils.isBlank(paramName) || BlankUtils.isBlank(paramValue)) {  
				continue;
			}
			
			ParamConfig.paramConfigMap.put("paramType_" + paramType + "_" + paramCode, paramValue);
		}
		
		//遍历配置参数的Map
		Iterator<Map.Entry<String,String>> it = ParamConfig.paramConfigMap.entrySet().iterator();
		System.out.println("系统参数加载至内存的情况(键值对):");
		while(it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		
	}

}
