package com.callke8.bsh.bshcallparam;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.impl.jdbcjobstore.DB2v6Delegate;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.StringUtil;
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
public class BSHCallParam extends Model<BSHCallParam> {
	
	private static final long serialVersionUID = 1L;
	
	public static BSHCallParam dao = new BSHCallParam();
	
	/**
	 * 以分页形式查找数据
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param paramName
	 * @return
	 */
	public Page<Record> getCallParamByPaginate(int pageNumber,int pageSize,String paramCode,String paramName) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append(" from bsh_call_param where 1=1");
		
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
	 * @param paramName
	 * @return
	 */
	public Map<String,Object> getCallParamByPaginateToMap(int pageNumber,int pageSize,String paramCode,String paramName) {
		
		Map<String,Object> m = new HashMap<String,Object>();
		
		Page<Record> p = getCallParamByPaginate(pageNumber,pageSize,paramCode,paramName);
		
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
	public boolean add(BSHCallParam bshCallParam) {
		boolean b = bshCallParam.save();
		if(b) {
			//如果新增成功，就需要重新加载数据到内存中
			loadCallParamDataToMemory();
		}
		return b;
	}
	
	/**
	 * 根据ParamCode 取出一个呼叫参数
	 * 
	 * @param paramCode
	 * @return
	 */
	public Record getBSHCallParamByParamCode(String paramCode) {
		
		String sql = "select * from bsh_call_param where PARAM_CODE=?";
		
		Record bshCallParam = Db.findFirst(sql,paramCode);
		
		return bshCallParam;
	}
	
	/**
	 * 取出所有的呼出数据
	 * 
	 * @return
	 */
	public List<Record> getAllCallParam() {
		
		String sql = "select * from bsh_call_param";
		
		List<Record> list = Db.find(sql);
		
		return list;
	}
	
	/**
	 * 修改呼叫参数
	 * 
	 * @param bshCallParam
	 * @return
	 */
	public boolean update(BSHCallParam bshCallParam) {
		
		boolean b = false;
		int count = 0;
		
		String sql = "update bsh_call_param set PARAM_NAME=?,PARAM_VALUE=?,PARAM_DESC=? where PARAM_CODE=?";
		
		count = Db.update(sql, bshCallParam.get("PARAM_NAME"),bshCallParam.get("PARAM_VALUE"),bshCallParam.get("PARAM_DESC"),bshCallParam.get("PARAM_CODE"));
		
		if(count == 1) {
			b = true;
			//如果修改成功，则需要重新将数据加载到内存
			loadCallParamDataToMemory();
		}
		
		return b;
	}
	
	/**
	 * 根据呼叫编码删除数据
	 * 
	 * @param paramCode
	 * @return
	 */
	public boolean deleteByParamCode(String paramCode) {
		
		boolean b = false;
		int count = 0;
		
		String sql = "delete from bsh_call_param where PARAM_CODE=?";
		
		count = Db.update(sql, paramCode);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 删除呼叫参数
	 * 
	 * @param bshCallParam
	 * @return
	 */
	public boolean delete(BSHCallParam bshCallParam) {
		
		if(BlankUtils.isBlank(bshCallParam) || BlankUtils.isBlank(bshCallParam.get("PARAM_CODE"))) {
			return false;
		}
		
		String paramCode = bshCallParam.get("PARAM_CODE");
		
		return deleteByParamCode(paramCode);
	}
	
	/**
	 * 加载博世电器的呼叫参数到内存
	 */
	public void loadCallParamDataToMemory() {
		
		List<Record> cps = getAllCallParam();
		
		if(BlankUtils.isBlank(cps)) {
			System.out.println("错误：=======-加载博世电器呼叫数据到内存失败,bsh_call_param 表数据为空,请添加数据后,再重新启动进行加载!");
			return;
		}
		
		for(Record cp:cps) {
			
			String paramCode = cp.getStr("PARAM_CODE");     //配置参数编码
			String paramName = cp.getStr("PARAM_NAME");     //配置参数名称
			String paramValue = cp.getStr("PARAM_VALUE");   //配置参数赋值
			//任意一个值为空时,进入下一个循环
			if(BlankUtils.isBlank(paramCode) || BlankUtils.isBlank(paramName) || BlankUtils.isBlank(paramValue)) {  
				continue;
			}
			
			if(paramCode.equalsIgnoreCase("callerNumber")) {
				BSHCallParamConfig.setCallerNumber(paramValue);
			}else if(paramCode.equalsIgnoreCase("trunkInfo")) {
				BSHCallParamConfig.setTrunkInfo(paramValue);
			}else if(paramCode.equalsIgnoreCase("scanInterval")) {
				BSHCallParamConfig.setScanInterval(Long.valueOf(paramValue));
			}else if(paramCode.equalsIgnoreCase("scanCount")) {
				BSHCallParamConfig.setScanCount(Integer.valueOf(paramValue));
			}else if(paramCode.equalsIgnoreCase("queueMaxCount")) {
				BSHCallParamConfig.setQueueMaxCount(Integer.valueOf(paramValue));
			}else if(paramCode.equalsIgnoreCase("trunkMaxCapacity")) {
				BSHCallParamConfig.setTrunkMaxCapacity(Integer.valueOf(paramValue));
			}else if(paramCode.equalsIgnoreCase("retryTimes")) {
				BSHCallParamConfig.setRetryTimes(Integer.valueOf(paramValue));
			}else if(paramCode.equalsIgnoreCase("retryInterval")) {
				BSHCallParamConfig.setRetryInterval(Integer.valueOf(paramValue));
			}else if(paramCode.equalsIgnoreCase("voicePath")) {
				BSHCallParamConfig.setVoicePath(paramValue);
			}else if(paramCode.equalsIgnoreCase("voicePathSingle")) {
				BSHCallParamConfig.setVoicePathSingle(paramValue);
			}else if(paramCode.equalsIgnoreCase("agiUrl")) {
				BSHCallParamConfig.setAgiUrl(paramValue);
			}else if(paramCode.equalsIgnoreCase("activeTime")) {    
				//如果配置参数为生效时间，一般格式为：      08:30-20:00  这种格式
				//所以我们需要将其分解，并将其设置到 activeStartTime 和  activeEndTime 这两个时间属性内
				if(!StringUtil.containsAny(paramValue, "-")) {    		//先查看是否包含 "-" 这个标识先,如果不包含，跳过此设置
					continue;
				}
				String[] spRs = paramValue.split("-");
				if(spRs.length <2) {
					continue;
				}
				
				String startTime = spRs[0];
				String endTime = spRs[1];
				
				BSHCallParamConfig.setActiveStartTime(startTime);
				BSHCallParamConfig.setActiveEndTime(endTime);
			}else if(paramCode.equalsIgnoreCase("juHeUrl")) {
				BSHCallParamConfig.setJuHeUrl(paramValue);
			}else if(paramCode.equalsIgnoreCase("juHeAppKey")) {
				BSHCallParamConfig.setJuHeAppKey(paramValue);
			}else if(paramCode.equalsIgnoreCase("mimeTypeForSingle")) {
				BSHCallParamConfig.setMimeTypeForSingle(paramValue);
			}else if(paramCode.equalsIgnoreCase("soxBinPath")) {
				BSHCallParamConfig.setSoxBinPath(paramValue);
			}else if(paramCode.equalsIgnoreCase("bshCallResultUrl")) {
				BSHCallParamConfig.setBshCallResultUrl(paramValue);
			}else if(paramCode.equalsIgnoreCase("bshCallResultKey")) {
				BSHCallParamConfig.setBshCallResultKey(paramValue);
			}
			
		}
		
		
	}

}
