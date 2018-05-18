package com.callke8.test;

import com.callke8.utils.BlankUtils;
import com.callke8.utils.StringUtil;

public class StringTest {

	public static void main(String[] args) {
		
		String result = "{resultCode:aaa}";
		
		if(BlankUtils.isBlank(result) || !StringUtil.containsAny(result, "resultCode")) {   
			System.out.println("重发一次");
		}else {
			System.out.println("请求成功了，不需要重发!");
		}
		
	}

}
