package com.callke8.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jfinal.plugin.activerecord.Record;

/**
 * 内存变量，主要是用于储存数据字典的信息
 * 
 * @author Administrator
 */
public class MemoryVariableUtil {
	
	@SuppressWarnings("unchecked")
	public static Map<String,List<Record>> dictMap;    //储存于内存中的数据字典，前面的String,是指数据字典组编码
	public static Map<String,Record> moduleMap;        //储存于内存中的菜单数据, 前面的String,是指菜单编码
	public static Map<String,Record> operatorMap;      //储存于内存中的操作员的数据,前面的String,是指操作员工号
	public static Map<String,String> autoContactMap;   //储存于内存中的自动接触配置，数据来源于 commonconfig.properties 文件
	public static Map<String,String> voicePathMap;     //储存于内存中的语音路径，主要是用于试听语音和上传语音（及转换格式）文件时用
	public static Map<String,String> autoCallTaskMap;  //储存于内存中的自动外呼任务的配置
	public static Map<String,String> ttsParamMap;         //储存于内存中 TTS 的参数情况
	
	/**
	 * 
	 * 根据传递上来的数据字典组编码及数据字典项编码，取得数据字典项名称
	 * 
	 * 主要是用于在 combobox 及显示数据时，显示正确的文字结果
	 * 
	 * @param groupCode
	 * @param dictCode
	 * @return
	 */
	public static String getDictName(String groupCode,String dictCode) {
		
		String dictNameRs = null;   //定义返回的结果
		
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCode);
		
		for(Record record:list) {
			String dc = record.get("DICT_CODE");
			String dn = record.get("DICT_NAME");
			
			if(dc.equalsIgnoreCase(dictCode)) {    //如果两者相同时，则返回数据字典项名称
				dictNameRs = dn;
				break;
			}
		}
		
		return dictNameRs;
	}
	
	/**
	 * 根据传递的参数：菜单编码，从内存数据中，取得菜单名称
	 * 
	 * @param moduleCode
	 * @return
	 */
	public static String getModuleName(String moduleCode) {
		String moduleName = null;
		Record module = MemoryVariableUtil.moduleMap.get(moduleCode);   //根据 moduleCode从内存中取出当前菜单记录
		
		if(!BlankUtils.isBlank(module)) {
			moduleName = module.get("MODULE_NAME");
		}
		
		return moduleName;
	}
	
	/**
	 * 根据传递的参数：uri(即是控制器的uri),从内存数据中，取得菜单编码，主要是用于储存操作日志
	 * 
	 * @param uri
	 * @return
	 */
	public static String getModuleCode(String uri) {
		
		//先遍历内存变量 moduleMap,　然后根据遍历，将所有module的对象取出，如果该对象的MODULE_URI的值不为空，且与传入的uri相同时，则表示该菜单编码为正确编码
		Set set = MemoryVariableUtil.moduleMap.keySet();
		Iterator i = set.iterator();
		while(i.hasNext()) {
			String key = i.next().toString();    
			Record module = MemoryVariableUtil.moduleMap.get(key);
			
			String uriRs = module.get("MODULE_URI");   
			
			if(!BlankUtils.isBlank(uriRs) && uriRs.equalsIgnoreCase(uri)) {
				return key;
			}
			
		}
		return null;
	}
	
	/**
	 * 根据传递的参数：操作员工号，从内存数据中，取得操作员名称
	 * @param operId
	 * @return
	 */
	public static String getOperName(String operId) {
		String operName = null;
		Record operator = MemoryVariableUtil.operatorMap.get(operId);   //根据 moduleCode从内存中取出当前菜单记录
		
		if(!BlankUtils.isBlank(operator)) {
			operName = operator.get("OPER_NAME");
		}
		
		return operName;
	}
	
	
	
}
