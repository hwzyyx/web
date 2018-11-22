package com.callke8.utils;

import java.util.Map;

import com.callke8.system.param.ParamConfig;
import com.jfinal.plugin.activerecord.Record;

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
	 * 获取客户号码 的归属地
	 * 
	 * 返回的 Record 主要是包括了五个信息：
	 * province:省份
	 * city:城市
	 * callOutTel:呼出号码，如果是本地号码，这个外呼号码就是传号码的本身，如果是外地号码，那么呼出号码，就需要在前面加一个0
	 * isLocalCity:是否为本地号码
	 * isLandlineNumber:是否为固话号码（座机号码）
	 * 
	 * @param tel
	 * @return
	 */
	public static Record getLocation(String tel) {
		/**
		 * province:省份
		 * city:城市
		 * callOutTel:呼出号码，如果是本地号码，这个外呼号码就是传号码的本身，如果是外地号码，那么呼出号码，就需要在前面加一个0
		 * isLocalCity:是否为本地号码
		 * isLandlineNumber:是否为固话号码（座机号码）
		 */
		Record rs = new Record();
		
		String localCity = ParamConfig.paramConfigMap.get("paramType_1_localCity");                       //当地城市
		String localCityAreaCode = ParamConfig.paramConfigMap.get("paramType_1_localCityAreaCode");       //当地城市区号
		
		//判断号码是否以0开头
		boolean is0Prex = NumberUtils.checkNumberByRegex(tel, "[0]{1}[0-9]{1,13}");       //判断是否以0开头
		boolean is1Prex = NumberUtils.checkNumberByRegex(tel, "[1]{1}[0-9]{1,13}");       //判断是否以1开头
		boolean is01Prex = NumberUtils.checkNumberByRegex(tel, "01{1}[0-9]{1,13}");       //判断是否以01开头
		boolean is10Prex = NumberUtils.checkNumberByRegex(tel, "10{1}[0-9]{1,13}");       //判断是否以10开头
		boolean is010Prex = NumberUtils.checkNumberByRegex(tel, "010[0-9]{1,13}");        //判断是否以010开头
		boolean isLocalAreaCode = NumberUtils.checkNumberByRegex(tel,localCityAreaCode + "[0-9]{1,13}");        //判断是否以系统设定的城市区号开始
		int customerTelLen = tel.length();                                                //取得客户号码的长度
		
		if(is0Prex) {         			//以0开头时，有三种可能：（1）带区号的座机   （2）带0开头的手机号码(01开头并不代表就是手机，也有可能是010（即北京座机）) （3）号码有问题（即是号码长度以0开头，但是长度小于）
						
			if(customerTelLen < 11) {        	//如果是小于11位长度时,则可以表示该号码格式有错误，因为只要带区号的座机，必然长度会等于或是大于11位
				System.out.println("客户号码 " + tel + " 格式错误，号码以0开头，但长度小于11位,为非正常座机或手机号码!");
				return null;
			}
			
			//(1)判断是否是01开头
			if(is01Prex && !is010Prex) {       //但是不为010开头，即是非北京区号，表示这个号码为手机号码，这里就要将0去掉，然后API查询归属地
				
				String searchNumber = tel.substring(1, tel.length());
				
				Map<String,String> locationMap = TelephoneLocationUtils.getTelephoneLocation(searchNumber,ParamConfig.paramConfigMap.get("paramType_1_juHeUrl"), ParamConfig.paramConfigMap.get("paramType_1_juHeAppKey"));
				
				if(!BlankUtils.isBlank(locationMap)) {
					String province = locationMap.get("province");
					String city = locationMap.get("city");
					
					rs.set("province",province);
					rs.set("city",city);
					rs.set("isLandlineNumber", false);            //非固定号码
					if(localCity.equalsIgnoreCase(province) || localCity.equalsIgnoreCase(city)) {    //只要系统设置的当地城市等于省份或是等于城市，则表示这个是本地号码
						rs.set("isLocalCity", true);
						rs.set("callOutTel",searchNumber);
					}else {                //否则表示这个是外地号码
						rs.set("isLocalCity", false);
						rs.set("callOutTel","0" + searchNumber);
					}
					
					return rs;
					
				}else {                 //即是无法定位号码归属地，有可能是一个假的号码
					System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
					return null;     
				}
			}else {       //如果为非01开头手机号码，则表示应该是座机,然后调用API,查询座机的归属地
				
				rs.set("province","座机");
				rs.set("city","座机");
				rs.set("isLandlineNumber", true);        //为固定号码
				if(tel.startsWith(localCityAreaCode)) {            //如果以配置的当地区号开头
					rs.set("isLocalCity", true);
					rs.set("callOutTel", tel.substring(localCityAreaCode.length(),tel.length()));    //本地号码，需要将区号去掉。
				}else {		//如果是其他的区号开头
					rs.set("isLocalCity", false);
					rs.set("callOutTel", tel);
				}
				return rs;
				
				/*Map<String,String> locationMap = TelephoneLocationUtils.getTelephoneLocation(tel,ParamConfig.paramConfigMap.get("paramType_1_juHeUrl"), ParamConfig.paramConfigMap.get("paramType_1_juHeAppKey"));
				
				if(!BlankUtils.isBlank(locationMap)) {
					String province = locationMap.get("province");
					String city = locationMap.get("city");
					
					rs.set("province",province);
					rs.set("city",city);
					rs.set("isLandlineNumber", true);        //为固定号码
					rs.set("callOutTel",tel);                //由于是固定号码直接设置传入的号码即可
					if(localCity.equalsIgnoreCase(province) || localCity.equalsIgnoreCase(city)) {    //只要系统设置的当地城市等于省份或是等于城市，则表示这个是本地号码
						rs.set("isLocalCity", true);
					}else {                					//否则表示这个是外地号码
						rs.set("isLocalCity", false);
					}
					
					return rs;
					
				}else {                 //即是无法定位号码归属地，有可能是一个假的号码
					System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
					return null;     
				}*/
			}
			
		}else if(is1Prex) {				//以1开头时，以1开头，看长度是否为10
			
			if(is10Prex) {              //如果以10开头，可能会是简化的以非0开头的北京座机,如果长度又刚好为10位，则暂定认为这是北京的固定电话
				if(customerTelLen == 10) {  
					rs.set("province","北京");
					rs.set("city","");
					rs.set("isLandlineNumber",true);     //为固定号码
					rs.set("callOutTel", "0" + tel);     //将外呼号码补全为北京的固话
					rs.set("isLocalCity", false);        //默认设定为非本地号码
					if(localCity.equalsIgnoreCase("北京")) {
						rs.set("isLocalCity", true);        //是本地号码
					}
					return rs;
				}else {
					System.out.println("客户号码 " + tel + " 格式以10开头，但无法定位归属地，号码异常!");
					return null;
				}
				
			}else {                     //如果不是以10开头，那么就可能是手机号码
				if(customerTelLen < 11) {
					System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
					return null;
				}
				
				Map<String,String> locationMap = TelephoneLocationUtils.getTelephoneLocation(tel,ParamConfig.paramConfigMap.get("paramType_1_juHeUrl"),ParamConfig.paramConfigMap.get("paramType_1_juHeAppKey"));
				if(!BlankUtils.isBlank(locationMap)) {
					String province = locationMap.get("province");
					String city = locationMap.get("city");
					
					rs.set("province",province);
					rs.set("city",city);
					rs.set("isLandlineNumber", false);        //非固定号码
					if(localCity.equalsIgnoreCase(province) || localCity.equalsIgnoreCase(city)) {    //只要系统设置的当地城市等于省份或是等于城市，则表示这个是本地号码
						rs.set("isLocalCity", true);
						rs.set("callOutTel",tel);                //本地手机号码，直接设置为传入的号码即可
					}else {                					//否则表示这个是外地号码
						rs.set("isLocalCity", false);
						rs.set("callOutTel","0" + tel);                //非本地手机号码，要在前面加一个0
					}
					
					return rs;
					
				}else {                 //即是无法定位号码归属地，有可能是一个假的手机号码
					System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
					return null;
				}
				
			}
			
		}else {       //如果非0，又非1开头，表示很有可能是直接给的本地号码，就要看长度，如果长度为7或8位，表示本地号码
			
			if(customerTelLen == 8 || customerTelLen == 7) {       //表示这个是本地号码，这时，加上系统设置的本地号码区号，调用 API 进行查询
				rs.set("province","座机");
				rs.set("city","座机");
				rs.set("isLandlineNumber", true);        //为固定号码
				rs.set("isLocalCity", true);             //为本地号码
				rs.set("callOutTel", tel);               //一般不带区号的本地号码，外呼时，无须加上区号，所以只需要将传入的号码即是呼出号码
				return rs;
				/*System.out.println("localCityAreaCode 本地区号－－－－－＝＝＝＝＝＝＝＝＝:" + localCityAreaCode);
				Map<String,String> locationMap = TelephoneLocationUtils.getTelephoneLocation(localCityAreaCode + tel,ParamConfig.paramConfigMap.get("paramType_1_juHeUrl"),ParamConfig.paramConfigMap.get("paramType_1_juHeAppKey"));
				if(!BlankUtils.isBlank(locationMap)) {
					String province = locationMap.get("province");
					String city = locationMap.get("city");
					
					rs.set("province",province);
					rs.set("city",city);
					rs.set("isLandlineNumber", true);        //为固定号码
					rs.set("isLocalCity", true);             //为本地号码
					rs.set("callOutTel", tel);               //一般不带区号的本地号码，外呼时，无须加上区号，所以只需要将传入的号码即是呼出号码
					return rs;
				}else {                						 //即是无法定位号码归属地
					System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
					return null;
				}*/
			}else {  //如果长度不为7或8位，且长度大于8位时，该号码是带区号的没有给0的座机号
				System.out.println("客户号码 " + tel + " 格式 正确，但无法定位归属地，号码异常!");
				return null;
			}
			
		}
	}
	
}
