package com.callke8.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	/**
	 * 定义一个方法：用于判断该方法内是否包含子字符串
	 * 
	 * @param str
	 *            字符串
	 * @param searchChars
	 *            要查找的字符串
	 * @return boolean true: 包含; false: 不包含
	 */
	public static boolean containsAny(String str, String searchChars) {

		if (str.length() != str.replace(searchChars, "").length()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否为数字
	 * 
	 * @return
	 */
	public static boolean isNumber(String str) {

		boolean b = false;

		if (!BlankUtils.isBlank(str)) {

			b = Pattern.compile("[0-9]+").matcher(str).matches();

		}

		return b;

	}

	/**
	 * 号码处理函数，当传入的号码加了前缀9、90 或是0 时，要将号码处理后返回
	 * 
	 * @param number
	 * @return
	 */
	public static String doPhoneNumberHandle(String phoneNumber) {

		if (!isNumber(phoneNumber)) { // 先判断号码是否是以数字组成,如果号码非以数字组成
			return null;
		}

		if (!BlankUtils.isBlank(phoneNumber)) {

			if (phoneNumber.startsWith("9")) { // 如果号码以 9 开头时，需要将前缀9 去除
				phoneNumber = phoneNumber.substring(1, phoneNumber.length()); // 要截至最后
			}

			if (phoneNumber.startsWith("01") && !phoneNumber.startsWith("010")) { // 如果号码是以
																					// 01
																					// 开头的
																					// ，
																					// 有可能是手机号码
																					// ，
																					// 或是北京的座机
																					// ，
																					// 需要将手机的前缀0去掉
				phoneNumber = phoneNumber.substring(1, phoneNumber.length()); // 要截至最后
			}

		}

		return phoneNumber;

	}
	
	/**
	 * 主要是用于处理xls读取出来的号码，进行二次处理，由于xls时，如果是带区号的座机时，前缀0会自动被删除掉，这里，就需要将0补回去
	 * @param phoneNumber
	 * @return
	 */
	public static String getPhoneNumber4XlsFormat(String phoneNumber) {
		
		phoneNumber = doPhoneNumberHandle(phoneNumber);   //先调用方法，将号码预处理
		
		if(!BlankUtils.isBlank(phoneNumber)) {
			
			if(phoneNumber.startsWith("1") ) {       //如果号码以1为开头时，有可能是手机号码，或是北京座机
				if(phoneNumber.startsWith("10")) {   //如果号码为 10 时，表示很有可能是 北京的座机号码，需要添加前缀 0
					phoneNumber = "0" + phoneNumber;
				}
			}else if(!phoneNumber.startsWith("0")){                                  //如果号码为非 0 开头时，就可能是普通座机，普通座机如果带区号，就需要添加0                                
				if(phoneNumber.length()>=10) {       //号码大于 10, 就表示这是已经添加了区号的号码，需要添加前缀0
					phoneNumber = "0" + phoneNumber;
				}
			}
			
		}
		
		return phoneNumber;
	}

	/*
	 * Java文件操作 获取文件扩展名
	 * 
	 * Created on: 2011-8-2 Author: blueeagle
	 */
	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}
	
	/**
	 * 根据calldate得到录音的路径
	 * 
	 * @param calldate
	 * @return
	 */
	public static String getPathByCallDate(String calldate) {
		String dir = "voices" + "/";
		if(BlankUtils.isBlank(calldate)){
			return dir;
		}
		Date date = DateFormatUtils.parseDate(calldate);    //将字符转为Date对象
		
		String fd = DateFormatUtils.formatDate(date);       //从 Date对象中取出日期，即得到 2014-12-01 的字符   
		
		String[] strs = fd.split("-");
		
		for(String str:strs) {
			dir += str + "/";
		}
		return dir;
	}
	
	/**
	 * 手机号验证
	 * 
	 * @param str
	 * @return 验证通过返回true
	 */
	public static boolean isMobile(String str) {
		Pattern p = null;
		Matcher m = null;
		boolean b = false;
		p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
		m = p.matcher(str);
		b = m.matches();
		return b;
	} 
	
	/**
	 * 判断一个host是否为正常的 IP
	 * 
	 * @param host
	 * @return
	 */
	public static boolean isIP (String host) {
		boolean b = false;
		
		Pattern p = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		
		Matcher m = p.matcher(host);
		if(m.matches()) {			//当host 部分是以IP的形式,表示座席确实是注册成功的
			b = true;
		}
	
		return b;
	}
	
	/**
	 * 判断传入的数字是否为钱的表示
	 * 即是否为整数或是浮点数
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isMoney(String number) {
		
		if(BlankUtils.isBlank(number)) {
			return false;
		}
		
		String intReg = "^\\d+";
		String floatReg = "^\\d+\\.\\d+";
		
		boolean isInt = number.matches(intReg);  //是否为整数
		boolean isFloat = number.matches(floatReg);   //是否为浮点数
		
		if(isInt || isFloat) {    //只要有一个正确,即返回正确
			return true;
		}else {
			return false;
		}
		
	}
	
	/**
	 * 将数字转换成字符串读法
	 * 
	 * 比如：2690.05 转换到得到 2q6b9sd05y;    104.36 转换得到 1b04d36y
	 * 
	 * @param number
	 * @return
	 */
	public static String numberExchangeToMoney(String number) {
		
		String moneyStr = null; 
		
		//先判断是否为
		boolean isMoney = isMoney(number); 
		
		if(!isMoney) {   //如果不是Money的表达方式
			return null;
		}
		
		//查看整形还是浮点型
		boolean b = StringUtil.containsAny(number,".");   
		
		if(b) {    //表示浮点型
			
			String[] ns = number.split("\\.");   //以点分隔
			moneyStr = NumberTransfrom.transfrom(ns[0]);
			moneyStr += "d";   //点
			moneyStr += ns[1];
			moneyStr += "y";    //元
			
		}else {    //表示整形,返回转换后的字串 + ”元“
			moneyStr = NumberTransfrom.transfrom(number) + "y";
		}
		
		return moneyStr;
		
	}
	
	/**
	 * 向指定的路径写入字符串
	 * 
	 * @param path
	 * 			文件路径
	 * @param str
	 * 			字符串
	 * @param append
	 * 			是否追加写入
	 */
	public static void writeString(String path,String str,boolean append) {
		
		FileWriter fw = null;
		
		try {
			File f = new File(path);     //如果文件不存在，就创建文件，如果文件存在，直接添加内容
			fw = new FileWriter(f, append);
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		PrintWriter pw = new PrintWriter(fw);
		pw.println(str);
		
		try {
			fw.flush();
			pw.close();
			fw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 模拟输出日志
	 * 
	 * @param o
	 * @param str
	 */
	public static void log(Object o,String str) {
		System.out.println(DateFormatUtils.getCurrentDate() + "\t" + o.getClass().getSimpleName() + "\t" + str);
	}
	

}
