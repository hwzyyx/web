package com.callke8.system.param;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * 定义一个系统参数的配置类，用于将系统参数加入内存
 * 
 * @author 黄文周
 *
 */
public class ParamConfig {
	
	/**
	 * paramConfigMap 的格式,  KEY:前缀(paramType)_参数类型_参数编码;VALUE:参数值，如：paramConfigMap.put(paramType_1_voicePath,'bsh_voice_single');
	 * 
	 * paramType: 1(全局参数);2(TTS参数);3(博世家电参数);4(自动外呼参数)
	 * 
	 */
	public static Map<String,String> paramConfigMap = new LinkedHashMap<String,String>();
	
}
