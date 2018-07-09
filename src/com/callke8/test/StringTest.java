package com.callke8.test;

import java.util.Date;

import com.callke8.utils.DateFormatUtils;

public class StringTest {

	public static void main(String[] args) {
		
		/*String result = "{resultCode:aaa}";
		
		if(BlankUtils.isBlank(result) || !StringUtil.containsAny(result, "resultCode")) {   
			System.out.println("重发一次");
		}else {
			System.out.println("请求成功了，不需要重发!");
		}*/
		long sec = DateFormatUtils.getTimeMillis();
		
		long sec2 = new Date().getTime();
		
		System.out.println("当前时间：" + DateFormatUtils.getCurrentDate());
		
		System.out.println("当前的毫秒: " + sec);
		System.out.println("当前的秒钟: " + sec2);
		
		long fiveMinuteSec = sec - 5*60*1000;
		
		Date d = new Date(fiveMinuteSec);
		
		String fiveMinuteDate = DateFormatUtils.formatDateTime(d, "yyyy-MM-dd HH:mm:ss");
		
		System.out.println("5分钟前的时间为:" + fiveMinuteDate);
		
		
		
	}

}
