package com.callke8.utils;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

/**
 * 地址内容的替换工具类
 *  
 * @author 黄文周
 */
public class AddressReplaceUtils {

	private static List<Record> ruleList = new ArrayList<Record>();
	
	static {
		Record r0 = new Record();
		r0.set("oldStr","0");
		r0.set("newStr","零");
		
		Record r1 = new Record();
		r1.set("oldStr","1");
		r1.set("newStr","一");
		
		Record r2 = new Record();
		r2.set("oldStr","2");
		r2.set("newStr","二");
		
		Record r3 = new Record();
		r3.set("oldStr","3");
		r3.set("newStr","三");
		
		Record r4 = new Record();
		r4.set("oldStr","4");
		r4.set("newStr","四");
		
		Record r5 = new Record();
		r5.set("oldStr","5");
		r5.set("newStr","五");
		
		Record r6 = new Record();
		r6.set("oldStr","6");
		r6.set("newStr","六");
		
		Record r7 = new Record();
		r7.set("oldStr","7");
		r7.set("newStr","七");
		
		Record r8 = new Record();
		r8.set("oldStr","8");
		r8.set("newStr","八");
		
		Record r9 = new Record();
		r9.set("oldStr","9");
		r9.set("newStr","九");
		
		Record r10 = new Record();
		r10.set("oldStr","-");
		r10.set("newStr","杠,");
		
		Record r11 = new Record();
		r11.set("oldStr","/");
		r11.set("newStr","斜杠,");
		
		Record r12 = new Record();
		r12.set("oldStr","#");
		r12.set("newStr",",井,");
		
		ruleList.add(r0);
		ruleList.add(r1);
		ruleList.add(r2);
		ruleList.add(r3);
		ruleList.add(r4);
		ruleList.add(r5);
		ruleList.add(r6);
		ruleList.add(r7);
		ruleList.add(r8);
		ruleList.add(r9);
		ruleList.add(r10);
		ruleList.add(r11);
		ruleList.add(r12);
		
	}
	
	/**
	 * 替换地址信息的内容，将地址中的相关信息
	 * 
	 * 比如：1替换成一
	 * 		0替换成零
	 *      -替换成杠
	 * 
	 * @param address
	 * @return
	 */
	public static String replaceAddressContent(String address) {
		
		if(BlankUtils.isBlank(address)) {
			return null;
		}
		
		if(BlankUtils.isBlank(ruleList) || ruleList.size()==0) {
			return address;
		}
		
		for(Record rr:ruleList) {
			address = address.replace(rr.getStr("oldStr"), rr.getStr("newStr"));
		}
		
		return address;
	}
	
}
