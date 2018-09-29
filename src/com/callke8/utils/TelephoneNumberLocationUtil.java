package com.callke8.utils;

import java.util.Map;

import com.callke8.system.param.ParamConfig;

/**
 * 客户号码定位工具类
 * 
 * 
 * 
 * 
 * @author 黄文周
 */
public class TelephoneNumberLocationUtil {
	
	/**
	 * 检查该号码是否为本地号码
	 * 
	 * @param tel
	 * @param localCity
	 * @param areaCode 本地区号
	 * @return
	 */
	public static boolean isLocalNumber(String tel,String localCity,String areaCode) {
		
		//取出号码，进行相关字段的设定（呼出号码、省份、城市）
				//判断号码是否以0开头
				boolean is0Prex = NumberUtils.checkNumberByRegex(tel, "[0]{1}[0-9]{1,13}");       //判断是否以0开头
				boolean is1Prex = NumberUtils.checkNumberByRegex(tel, "[1]{1}[0-9]{1,13}");       //判断是否以1开头
				boolean is01Prex = NumberUtils.checkNumberByRegex(tel, "01{1}[0-9]{1,13}");       //判断是否以01开头
				boolean is10Prex = NumberUtils.checkNumberByRegex(tel, "10{1}[0-9]{1,13}");       //判断是否以10开头
				boolean is010Prex = NumberUtils.checkNumberByRegex(tel, "010[0-9]{1,13}");        //判断是否以010开头
				boolean isLocalAreaCode = NumberUtils.checkNumberByRegex(tel,areaCode + "[0-9]{1,13}");        //判断是否以025开头  ,即是否为南京座机号码
				int customerTelLen = tel.length();                                                //取得客户号码的长度
				
				if(is0Prex) {         			//以0开头时，有三种可能：（1）带区号的座机   （2）带0开头的手机号码(01开头并不代表就是手机，也有可能是010（即北京座机）) （3）号码有问题（即是号码长度以0开头，但是长度小于）
								
					if(customerTelLen < 11) {        	//如果是小于11位长度时,则可以表示该号码格式有错误，因为只要带区号的座机，必然长度会等于或是大于11位
						System.out.println("客户号码 " + tel + " 格式错误，号码以0开头，但长度小于11位,为非正常座机或手机号码!");
						return false;
					}
					
					//(1)判断是否是01开头
					if(is01Prex && !is010Prex) {       //但是不为010开头，即是非北京区号，表示这个号码为手机号码，这里就要将0去掉，然后API查询归属地
						
						String searchNumber = tel.substring(1, tel.length());
						
						Map<String,String> locationMap = TelephoneLocationUtils.getTelephoneLocation(searchNumber,ParamConfig.paramConfigMap.get("paramType_1_juHeUrl"), ParamConfig.paramConfigMap.get("paramType_1_juHeAppKey"));
						
						if(!BlankUtils.isBlank(locationMap)) {
							String province = locationMap.get("province");
							String city = locationMap.get("city");
							
							if(city.equalsIgnoreCase(localCity)) {     //如果城市为江苏南京时，外呼号码为加一个0
								return true;
							}else {
								return false;
							}
						}else {                 //即是无法定位号码归属地，有可能是一个假的号码
							System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
							return false;
						}
					}else {       //如果为非01开头手机号码，则表示应该是座机
						
						//判断是否为南京座机
						if(isLocalAreaCode) {    //如果是本地座机时
							return true;
						}
					}
					
				}else if(is1Prex) {				//以1开头时，以1开头，看长度是否为10
					
					if(is10Prex) {              //如果以10开头，可能会是简化的以非0开头的北京座机
						return false;
					}else {                     //如果不是以10开头，那么就可能是手机号码
						if(customerTelLen < 11) {
							System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
							return false;
						}
						
						Map<String,String> locationMap = TelephoneLocationUtils.getTelephoneLocation(tel,ParamConfig.paramConfigMap.get("paramType_1_juHeUrl"),ParamConfig.paramConfigMap.get("paramType_1_juHeAppKey"));
						if(!BlankUtils.isBlank(locationMap)) {
							String province = locationMap.get("province");
							String city = locationMap.get("city");
							
							if(city.equalsIgnoreCase(localCity)) {     //如果为当地号码时
								return true;
							}else {
								return false;
							}
						}else {                 //即是无法定位号码归属地，有可能是一个假的号码
							System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
							return false;
						}
						
					}
					
					
				}else {       //如果非0，又非1开头，表示很有可能是直接给的南京本地号码，就要看长度，如果长度为8位，表示南京本地号码
					
					if(customerTelLen == 8 || customerTelLen == 7) {       //表示这个是本地号码
						return true;
					}else {  //如果长度不为8位，且长度大于8位时，该号码是带区号的没有给0的座机号
						System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
						return false;
					}
					
				}
				
				return false;
		
		
	}
	
}
