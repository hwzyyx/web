package com.callke8.system.ipaddress;

import java.util.ArrayList;

import com.callke8.utils.StringUtil;

/**
 * IP地址访问控制的配置，用于将IP地址配置加载到内存，在系统判断时，可以直接在内存中判断，提高系统的效率
 * 
 * @author 黄文周
 *
 */
public class SysIpAddressConfig {

	public static ArrayList<String> ipAddressList = new ArrayList<String>();
	
	/**
	 * 系统做IP地址访问控制权限判断
	 * 
	 * 如果该客户的IP地址有访问权限，返回 true
	 * 
	 * 否则返回 false
	 * 
	 * @param clientIpAddress
	 * @return
	 */
	public static boolean handleIpAddressAccessControl(String clientIpAddress) {
		
		if(StringUtil.isIP(clientIpAddress)) {
			//如果客户的IP地址在配置的 IpAddress 表中，则执行通过的操作,否则，将不允许执行登录操作
			if(SysIpAddressConfig.ipAddressList.contains(clientIpAddress)) {
				return true;
			}else {
				System.out.println("客户访问系统，客户 IP 地址为：" + clientIpAddress + ",由于该IP地址没有访问系统权限,系统将拒绝访问!");
				return false;
			}
		}else {
			System.out.println("客户的IP地址为非正常IP地址:" + clientIpAddress);
			return true;
		}
	}
	
}
