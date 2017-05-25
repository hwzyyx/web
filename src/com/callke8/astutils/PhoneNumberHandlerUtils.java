package com.callke8.astutils;

import com.callke8.call.calltelephone.CallerLocation;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.NumberUtils;

/**
 * 电话号码处理工具类
 * 
 * 如获取归属地；判断号码是否正常并进行处理
 * 
 * @author Administrator
 *
 */
public class PhoneNumberHandlerUtils {

	/**
	 * 根据传入的号码，获取归属地
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public static String getLocation(String phoneNumber) {
		
		String location = null;
		
		//如果号码为空、号码小于7位，或是大于  13 位，直接返回空值
		if(BlankUtils.isBlank(phoneNumber) || phoneNumber.length()<7 || phoneNumber.length() > 13) {   
			return location;
		}
		
		//根据号码，得到前缀，如果是手机，则返回前 7 位，如果是座机，则返回区号
		String perfixNumber = NumberUtils.getPrefixByNumber(phoneNumber);
		
		location = CallerLocation.dao.getLocationByPrefix(perfixNumber);
		
		return location;
		
	}
	
	
	
}
