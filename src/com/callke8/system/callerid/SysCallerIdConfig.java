package com.callke8.system.callerid;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 定义一个主叫号码的Config类，用于将系统配置的所有主叫号码加入内存
 * 
 * 
 * @author 黄文周
 */
public class SysCallerIdConfig {

	/**
	 * 
	 * sysCallerIdMap 格式：键为sys_callerid 表中的 ID 值， 值为sys_callerid表中的 CALLERID
	 * 
	 * sys_callerid 的数据格式如下：
	 * 
	 *  ID   CALLERID        PURPOSE    CREATE_CODE   CREATE_TIME
	 *  38	008651983272222	交警移车转用	super		2018-10-30 11:39:26
		39	008651988193471	电话费专用	super		2018-10-30 11:39:44
		40	008651988130008	电费催缴专用	super		2018-10-30 11:39:57
		41	008651910000	自来水专用	super		2018-10-30 11:40:09
		41	008651910000	自来水专用	super		2018-10-30 11:40:09
	 * 
	 */
	public static Map<String,String> sysCallerIdMap = new LinkedHashMap<String,String>();
	
}
