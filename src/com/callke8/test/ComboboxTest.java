package com.callke8.test;
import java.util.ArrayList;
import java.util.List;

import com.callke8.utils.TreeJson;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ComboboxTest {

	public static void main(String[] args) {
		
		//List<JsonNode> list = new ArrayList<JsonNode>();
		List<TreeJson> list = new ArrayList<TreeJson>();
		
		JsonNode jn1 = new JsonNode();
		jn1.setText("A");
		jn1.setValue("1");
		jn1.setId("A");
		JsonNode jn2 = new JsonNode();
		jn2.setText("B");
		jn2.setValue("2");
		jn2.setId("B");
		
		TreeJson tj1 = new TreeJson();
		tj1.setId("aa");
		tj1.setText("11");
		TreeJson tj2 = new TreeJson();
		tj2.setId("bb");
		tj2.setText("22");
		
		//JSONObject j1 = JSONObject.fromObject(tj1);
		//JSONObject j2 = JSONObject.fromObject(tj2);
		
		list.add(tj1);
		list.add(tj2);
		
		JSONArray jsonArray = JSONArray.fromObject(list);
		
		System.out.println("aaaaaaa");
		
	}

}
