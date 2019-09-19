package com.callke8.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 电话号码的判断及处理工具
 * 
 * @author 黄文周 
 */
public class TelephoneNumberUtils {
	
	/**
	 * 手机号码的判断规则
	 */
	private final static String REGEX_MOBILEPHONE = "^(0?)(1[3456789]\\d{9})$";
	
	/**
	 * 座机号码的判断规则-带区号
	 */
	private final static String REGEX_FIXEDPHONE_WITH_AREACODE = "^(010|02\\d|0[3-9]\\d{2})(\\d{7,8})$";
	
	/**
	 * 座机号码的判断规则-不带区号
	 */
	private final static String REGEX_FIXEDPHONE_NO_AREACODE = "^\\d{7,8}$";
	
	/**
	 * 特殊号码的判断规则-主要是 3 位或是5位的特殊号码 或是 400 号码
	 */
	private final static String REGEX_SPECIALPHONE = "^\\d{3,5}$";
	
	/**
	 * 判断传入的号码，是否为 手机号码 或是 座机号码
	 * 
	 * 无论手机号码是否带前缀0，也无论座机号码是否带区号
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isMobilePhoneNumberOrFixedPhoneNumber(String number) {
		
		if(isFixedPhoneNumber(number) || isMobilePhoneNumber(number)) {
			return true;
		}else {
			return false;
		}
		
	}
	
	/**
	 * 判断一个号码是否为手机号码
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isMobilePhoneNumber(String number) {
		
		if(BlankUtils.isBlank(number)) {     return false;   }
		
		return Pattern.matches(REGEX_MOBILEPHONE, number);
		
	}
	
	/**
	 * 根据传入的手机号码，返回不带0的手机号码
	 * 如果原手机号码带前缀0，则去掉前缀0，返回真实的手机号码
	 * 如果不带前缀0的手机号码，则直接返回原号码即可
	 * 
	 * @param number
	 * @return
	 */
	public static String getMobilePhoneNumberNoPrefix0(String number) {
		
		String mobilePhoneNumber = null;
		
		//先判断传入的号码是否为手机号码, 如果本身不为手机号码，则不再继续判断
		boolean isMobilePhoneNumber = isMobilePhoneNumber(number);
		if(!isMobilePhoneNumber) { 
			return mobilePhoneNumber;
		}
		
		//如果为手机号码，则返回手机号码
		Pattern p = Pattern.compile(REGEX_MOBILEPHONE);
		Matcher m = p.matcher(number);
		
		if(m.matches()) {
			mobilePhoneNumber = m.group(2);
		}
		
		return mobilePhoneNumber;
		
	}
	
	/**
	 * 判断一个号码是否为带区号的座机号码
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isFixedPhoneNumberWithAreaCode(String number) {
		
		if(BlankUtils.isBlank(number)) {     return false;   }
		
		return Pattern.matches(REGEX_FIXEDPHONE_WITH_AREACODE, number);
	}
	
	/**
	 * 判断一个号码是否为不带区号的座机号码
	 * @param number
	 * @return
	 */
	public static boolean isFixedPhoneNumberNoAreaCode(String number) {
		
		if(BlankUtils.isBlank(number)) {     return false;   }
		
		return Pattern.matches(REGEX_FIXEDPHONE_NO_AREACODE, number);
	}
	
	/**
	 * 判断一个号码是否为固定号码，无论是否带区号
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isFixedPhoneNumber(String number) {
		
		if(isFixedPhoneNumberWithAreaCode(number)) {
			return true;
		}
		
		if(isFixedPhoneNumberNoAreaCode(number)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 根据号码获取区号
	 * 
	 * 座机号码才有可能取到区号
	 * 
	 * @param number
	 * @return
	 */
	public static String getAreaCode(String number) {
		
		String areaCode = null;
		
		if(BlankUtils.isBlank(number)) {
			return areaCode;
		}
		
		boolean isFixedPhoneWidthAreaCode = isFixedPhoneNumberWithAreaCode(number);
		if(isFixedPhoneWidthAreaCode) {        //只有带区号的座机
			
			Pattern p = Pattern.compile(REGEX_FIXEDPHONE_WITH_AREACODE);
			Matcher m = p.matcher(number);
			
			if(m.matches()) {
				areaCode = m.group(1);
			}
		}
		
		return areaCode;
	}
	
	/**
	 * 获取不带区号的座机号码
	 * 座机号码才有可能取得不带区号的座机号码
	 * 
	 * 如果传入的电话号码为座机，且不带区号时，直接返回
	 * 如果传入的电话号码为带区号座机时，去掉区号，返回真实的座机号码
	 * 
	 * @param number
	 * @return
	 */
	public static String getFixedPhoneNumberNoAreaCode(String number) {
		
		String realFixedPhoneNumber = null;    //真实座机号码，不带区号
		
		if(BlankUtils.isBlank(number)) {
			return realFixedPhoneNumber;
		}
		
		//(1) 先判断是否为座机号码，无论是否带区号
		boolean isFixedPhoneNumber = isFixedPhoneNumber(number);
		if(!isFixedPhoneNumber) {     //非座机号码时，返回空
			return realFixedPhoneNumber;
		}
		
		//(2) 判断是否为不带区号的座机号码，直接返回该座机号码即可
		boolean isFixedPhoneNumberNoAreaCode = isFixedPhoneNumberNoAreaCode(number);
		if(isFixedPhoneNumberNoAreaCode) {    //如果为不带区号的座机，直接返回原号码即为真实的固话号码
			realFixedPhoneNumber = number;
			return realFixedPhoneNumber;
		}
		
		//(3) 判断是否为带区号的座机号码，去掉区号，将号码返回
		boolean isFixedPhoneNumberWithAreaCode = isFixedPhoneNumberWithAreaCode(number);
		if(isFixedPhoneNumberWithAreaCode) {        //只有带区号的座机
			
			Pattern p = Pattern.compile(REGEX_FIXEDPHONE_WITH_AREACODE);
			Matcher m = p.matcher(number);
			if(m.matches()) {
				realFixedPhoneNumber = m.group(2);
				return realFixedPhoneNumber;
			}
		}
		
		return realFixedPhoneNumber;
	}

}
