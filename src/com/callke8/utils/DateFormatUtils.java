package com.callke8.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期时间格式处理、转换工具类
 * 
 * @since 1.0
 */
public class DateFormatUtils {

	/**
	 * 长日期型
	 */
	public static final int LONG = DateFormat.LONG;

	/**
	 * 中日期型
	 */
	public static final int MEDIUM = DateFormat.MEDIUM;

	/**
	 * 短日期型
	 */
	public static final int SHORT = DateFormat.SHORT;

	/**
	 * 注意格里历和儒略历交接时的日期差别
	 */
	private static final int GREGORIAN_CUTOVER_YEAR = 1582;

	/**
	 * 根据指定的日期格式和语言将日期转换成字符串，默认当前区域的语言
	 * 
	 * <pre>
	 * Locale.CHINESE:
	 * 		SHORT:  09-12-4
	 * 		MEDIUM: 2009-12-4
	 * 		LONG:   2009年12月4日
	 * 
	 * Locale.ENGLISH:
	 * 		SHORT:  12/4/09
	 * 		MEDIUM: Dec 4, 2009
	 * 		LONG:   December 4, 2009
	 * </pre>
	 * 
	 * @param date
	 *            日期对象
	 * @param style
	 *            日期格式 （DateFormatUtil.LONG,MEDIUM,SHORT）
	 * @param locale
	 *            语言,不填默认为当前区域的语言
	 * @return 返回日期字符串
	 */
	public static String formatDate(Date date, int style, Locale locale) {

		if (BlankUtils.isBlank(date)) {
			throw new IllegalArgumentException("date parameter is null.");
		}

		if (style < LONG || style > SHORT) {
			throw new IllegalArgumentException("date style is illegal.");
		}

		locale = (locale == null ? Locale.getDefault() : locale);

		DateFormat dateformat = DateFormat.getDateInstance(style, locale);

		return dateformat.format(date);
	}

	/**
	 * 根据指定的日期格式将日期转换成字符串,默认语言Locale.CHINESE
	 * 
	 * <pre>
	 * Locale.CHINESE:
	 * 		SHORT:  09-12-4
	 * 		MEDIUM: 2009-12-4
	 * 		LONG:   2009年12月4日
	 * </pre>
	 * 
	 * @param date
	 *            日期对象
	 * @param style
	 *            日期格式 （DateFormatUtil.LONG,MEDIUM,SHORT）
	 * @return 返回日期字符串
	 */
	public static String formatDate(Date date, int style) {
		return formatDate(date, style, Locale.CHINESE);
	}

	/**
	 * 根据指定的日期格式，时间格式和语言将日期转换成字符串，默认当前区域的语言
	 * 
	 * <pre>
	 * Locale.CHINESE:
	 * 		SHORT SHORT:   09-12-4 下午5:44
	 * 		MEDIUM MEDIUM: 2009-12-4 17:44:06
	 * 		LONG LONG:     2009年12月4日 下午05时44分06秒
	 * 
	 * Locale.ENGLISH:
	 * 		SHORT SHORT:   12/4/09 5:41 PM
	 * 		MEDIUM MEDIUM: Dec 4, 2009 5:41:39 PM
	 * 		LONG LONG:     December 4, 2009 5:41:39 PM CST
	 * </pre>
	 * 
	 * @param date
	 *            日期对象
	 * @param dateStyle
	 *            日期格式 （DateFormatUtil.LONG,MEDIUM,SHORT）
	 * @param timeStyle
	 *            时间格式（DateFormatUtil.LONG,MEDIUM,SHORT）
	 * @param locale
	 *            语言,不填默认为当前区域的语言
	 * @return 返回日期字符串
	 */
	public static String formatDateTime(Date date, int dateStyle, int timeStyle, Locale locale) {

		if (BlankUtils.isBlank(date)) {
			throw new IllegalArgumentException("date parameter is null.");
		}

		if (dateStyle < LONG || dateStyle > SHORT) {
			throw new IllegalArgumentException("date style is illegal.");
		}

		if (timeStyle < LONG || timeStyle > SHORT) {
			throw new IllegalArgumentException("time style is illegal.");
		}

		locale = (locale == null ? Locale.getDefault() : locale);

		DateFormat dateformat = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);

		return dateformat.format(date);
	}

	/**
	 * 根据指定的日期格式，时间格式将日期转换成字符串，默认语言Locale.CHINESE
	 * 
	 * <pre>
	 * Locale.CHINESE:
	 * 		SHORT SHORT:   09-12-4 下午5:44
	 * 		MEDIUM MEDIUM: 2009-12-4 17:44:06
	 * 		LONG LONG:     2009年12月4日 下午05时44分06秒
	 * </pre>
	 * 
	 * @param date
	 *            日期对象
	 * @param dateStyle
	 *            日期格式 （DateFormatUtil.LONG,MEDIUM,SHORT）
	 * @param timeStyle
	 *            时间格式（DateFormatUtil.LONG,MEDIUM,SHORT）
	 * @return 返回日期字符串
	 */
	public static String formatDateTime(Date date, int dateStyle, int timeStyle) {
		return formatDateTime(date, dateStyle, timeStyle, Locale.CHINESE);
	}

	/**
	 * 把日期格式转换成yyyy-MM-dd格式的字符串
	 * 
	 * <pre>
	 * yyyy-MM-dd 2009-12-04
	 * </pre>
	 * 
	 * @param date
	 *            日期对象
	 * @return 返回日期字符串
	 */
	public static String formatDate(Date date) {
		return formatDateTime(date, "yyyy-MM-dd");
	}

	/**
	 * 将yyyy-MM-dd格式的字符串的字符串解析成日期对象
	 * 
	 * @param date
	 *            日期字符串
	 * @return 返回日期对象
	 */
	public static Date parseDate(String date) {
		return parseDateTime(date, "yyyy-MM-dd");
	}
	
	
	/**
	 * 将yyyy-MM-dd格式的字符串的字符串解析成日期对象
	 * 
	 * @param date
	 *            日期字符串
	 * @return 返回日期对象
	 */
	public static Date parseDate2(String date) {
		return parseDateTime(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 将日期时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
	 * 
	 * <pre>
	 * yyyy-MM-dd HH:mm:ss 2009-12-04 18:20:32
	 * </pre>
	 * 
	 * @param date
	 *            日期对象
	 * @return 返回日期字符串
	 */
	public static String formatDateTime(Date date) {
		return formatDateTime(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 将yyyy-MM-dd HH:mm:ss格式的字符串的字符串解析成日期对象
	 * 
	 * @param date
	 *            日期字符串
	 * @return 返回日期对象
	 */
	public static Date parseDateTime(String date) {
		return parseDateTime(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 根据指定的日期格式和语言将日期转换成字符串，默认当前区域的语言
	 * 
	 * <pre>
	 * yy-MM-dd HH:mm:ss    09-12-04 18:20:32
	 * yy/MM/dd hh:mm:ss:SS 09/12/04 06:20:32:140
	 * yyyy/MM/dd hh:mm:ss  2009/12/04 06:20:32
	 * </pre>
	 * 
	 * @param date
	 *            日期对象
	 * @param pattern
	 *            日期格式
	 * @return 返回日期字符串
	 */
	public static String formatDateTime(Date date, String pattern) {

		if (BlankUtils.isBlank(date)) {
			throw new IllegalArgumentException("date parameter is null.");
		}

		if (BlankUtils.isBlank(pattern)) {
			throw new IllegalArgumentException("date style is illegal.");
		}

		DateFormat dateformart = new SimpleDateFormat(pattern);

		return dateformart.format(date);
	}

	/**
	 * 根据指定个日期格式将日期字符串转换成日期对象
	 * 
	 * @param date
	 *            日期时间字符串
	 * @param pattern
	 *            日期时间格式
	 * @return 返回日期时间对象
	 */
	public static Date parseDateTime(String date, String pattern) {
		if (BlankUtils.isBlank(date)) {
			throw new IllegalArgumentException("date parameter is null.");
		}

		if (BlankUtils.isBlank(pattern)) {
			throw new IllegalArgumentException("date style is illegal.");
		}

		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			return getDate();
		}
	}

	/**
	 * 获取当前时间的日期对象
	 * 
	 * @return 返回当前时间日期对象
	 */
	public static Date getDate() {
		return new Date(getTimeMillis());
	}

	/**
	 * 获取当前时间的日期字符串,格式为yyyy-MM-dd
	 * 
	 * @return 返回日期字符串
	 */
	public static String getFormatDate() {
		return formatDate(getDate());
	}

	/**
	 * 获取当前时间的日期时间字符串,格式为yyyy-MM-dd HH:mm:ss
	 * 
	 * @return 返回日期时间字符串
	 */
	public static String getFormatDateTime() {
		return formatDateTime(getDate());
	}

	/**
	 * 获取当前日期开始时间的日期对象
	 * 
	 * @return 返回日期对象
	 */
	public static Date getStartOfDate() {
		return parseDate(formatDate(getDate()));
	}

	/**
	 * 获取当前日期开始时间的日期字符串,格式为yyyy-MM-dd HH:mm:ss
	 * 
	 * @return 返回日期字符串
	 */
	public static String getFormatStartOfDate() {
		return formatDateTime(getStartOfDate());
	}

	/**
	 * 获取当前日期中最后一秒的日期对象
	 * 
	 * @return 返回日期对象
	 */
	public static Date getEndOfDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getStartOfDate());
		calendar.add(Calendar.DATE, 1);
		calendar.add(Calendar.MILLISECOND, -1);
		return calendar.getTime();
	}

	/**
	 * 获取当前日期中最后一秒的日期字符串,格式为yyyy-MM-dd HH:mm:ss
	 * 
	 * @return 返回日期字符串
	 */
	public static String getFormatEndOfDate() {
		return formatDateTime(getEndOfDate());
	}

	/**
	 * 获取当前时间距1970年1月1日的毫秒数
	 * 
	 * @return 返回当前时间距1970年1月1日的毫秒数
	 */
	public static long getTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * 获取指定日期之前或之后n年的日期
	 * 
	 * @param date
	 *            日期对象
	 * @param amount
	 *            为正负数,表示日期年数的偏移量
	 * @return 返回日期对象
	 */
	public static Date addYear(Date date, int amount) {
		return add(date, Calendar.YEAR, amount);
	}

	/**
	 * 获取指定日期之前或之后n月的日期
	 * 
	 * @param date
	 *            日期对象
	 * @param amount
	 *            为正负数,表示日期月数的偏移量
	 * @return 返回日期对象
	 */
	public static Date addMonth(Date date, int amount) {
		return add(date, Calendar.MONTH, amount);
	}

	/**
	 * 获取指定日期之前或之后n天的日期
	 * 
	 * @param date
	 *            日期对象
	 * @param amount
	 *            为正负数,表示日期天数的偏移量
	 * @return 返回日期对象
	 */
	public static Date addDay(Date date, int amount) {
		return add(date, Calendar.DATE, amount);
	}

	/**
	 * 获取当前日期之前或之后n天的日期
	 * 
	 * @param amount
	 *            为正负数,表示日期天数的偏移量
	 * @return 返回日期对象
	 */
	public static Date addDay(int amount) {
		return add(getDate(), Calendar.DATE, amount);
	}

	/**
	 * 根据指定的日期和日期字段,获取字段偏移量的日期
	 * 
	 * @param date
	 *            指定日期对象
	 * @param field
	 *            日期字段
	 *            (Calendar.YEAR,Calendar.YEAR.MONTH,Calendar.DAY_OF_MONTH等等)
	 * @param amount
	 *            日期偏移量
	 * @return 返回日期对象
	 */
	public static Date add(Date date, int field, int amount) {

		if (BlankUtils.isBlank(date)) {
			throw new IllegalArgumentException("date parameter is null.");
		}

		if (field < 0) {
			throw new IllegalArgumentException("date field is illegal.");
		}

		if (amount == 0) {
			return date;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(field, amount);
		return calendar.getTime();
	}

	/**
	 * 判断是否是闰年
	 * 
	 * @param year
	 *            年份
	 * @return 返回true或false ,true表示是闰年
	 */
	public static boolean isLeapYear(int year) {
		if (year >= GREGORIAN_CUTOVER_YEAR) {
			return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
		} else {
			return year % 4 == 0;
		}
	}
	
	/**
	 * 取得当前时间,格式: yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getCurrentDate() {
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		.format(new java.util.Date());
		return date;
	}
	
	/**
	 * 根据当前日期，格式: yyyy-MM-dd 取得星期几，返回 周一(1)、周二(2)... 周六(6)、周日(7)
	 * 当 date 为空时，返回当天是周几
	 *
	 * @param date
	 * @return
	 */
	public static int getDayOfWeek(String date) {
		
		Calendar cal = Calendar.getInstance();
		
		if(BlankUtils.isBlank(date)) {
			cal.setTime(parseDate(getFormatDate()));
		}else {
			cal.setTime(parseDate(date));
		}
		
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		
		if(dayOfWeek == 1) {
			dayOfWeek = 7;
		}else {
			dayOfWeek = dayOfWeek - 1;
		}
		return dayOfWeek;
	}
	
	/**
	 * 计算两个日期间相关的天数
	 * 注：当天减当天，也算是1天
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws ParseException
	 */
	public static int daysBetween(String startDate,String endDate) throws ParseException{  
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
        Calendar cal = Calendar.getInstance();    
        cal.setTime(sdf.parse(startDate));    
        long time1 = cal.getTimeInMillis();                 
        cal.setTime(sdf.parse(endDate));    
        long time2 = cal.getTimeInMillis();         
        long between_days=(time2-time1)/(1000*3600*24);  
            
       return Integer.parseInt(String.valueOf(between_days)) + 1;     
    }  
	
	public static String getDayHourMinuteBySeconds(int seconds) {
		
		StringBuilder sb = new StringBuilder();
		
		int day = seconds/(60 * 60 * 24);
		int hour = (seconds - 60 * 60 * 24 * day)/3600;
		int minute = (seconds - 60 * 60 * 24 * day - hour * 3600)/60;   
		int second = seconds - 60 * 60 * 24 * day - hour * 3600 - minute * 60; 
		
		if(day > 0) {
			sb.append(day + "天,");
		}
		
		if(hour > 0) {
			sb.append(hour + "小时,");
		}
		
		if(minute > 0) {
			sb.append(minute + "分,");
		}
		
		sb.append(second + "秒");
		
		
		return sb.toString();
		
	}
	                                               
	
	
}

