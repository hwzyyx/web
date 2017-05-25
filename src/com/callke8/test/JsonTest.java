package com.callke8.test;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;

public class JsonTest {

	public static void main(String[] args) {
		
		Map m = new HashMap();
		
		JSONArray jsonArray = JSONArray.fromObject("aaa");
		
		System.out.println(jsonArray.toString());
		
	}

}
