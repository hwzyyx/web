package com.callke8.utils;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 用于组织架构的树处理工具，主要是根据传入的节点编码，取得所有子节点（当然也包括自己）
 * 
 * @author 黄文周
 *
 */
public class OrgTreeUtils {
	
	List<Record> newList = new ArrayList<Record>();   //定义一个返回的 list
	
	/**
	 * 传入组织列表，及要查询的组织编码
	 * 
	 * @param list
	 * @param orgCode
	 * @return
	 */
	public List<Record> getChildNode(List<Record> list,String orgCode) {
		
		if(BlankUtils.isBlank(list) || list.size() ==0){return null;};
		
		for(Record r:list) {
			
			if(r.getStr("ORG_CODE").equals(orgCode)) {
				analyzeTree(list,r);
			}
			
		}
		
		return newList;
		
	}
	
	private void analyzeTree(List<Record> list,Record node) {
		
		List<Record> childList = getChildList(list, node);    //得到子节点
		if(hasChild(list, node)) {     //是否有子节点
			
			newList.add(node);
			for(Record clr:childList) {
				analyzeTree(list,clr);
			}
			
		}else {
			newList.add(node);         //如果没有就返回自己
		}
	}
	
	// 得到子节点列表
    private List<Record> getChildList(List<Record> list, Record node) {
        List<Record> nodeList = new ArrayList<Record>();
        
        for(Record r:list) {
        	if(r.getStr("PARENT_ORG_CODE").equals(node.getStr("ORG_CODE"))) {
        		nodeList.add(r);
        	}
        }
        
        return nodeList;
    }

	
	 // 判断是否有子节点
    private boolean hasChild(List<Record> list, Record node) {
        return getChildList(list, node).size() > 0 ? true : false;
    }

    public static void main(String[] args) {
    	
    	/*List<Record> list = new ArrayList<Record>();
    	
    	Record r1 = new Record();
    	r1.set("ORG_CODE", "1540873746200");
    	r1.set("ORG_NAME", "常州电信部门");
    	r1.set("PARENT_ORG_CODE", "1540873745100");
    	
    	Record r2 = new Record();
    	r2.set("ORG_CODE", "1540873747300");
    	r2.set("ORG_NAME", "农电项目");
    	r2.set("PARENT_ORG_CODE", "1540873745100");
    	
    	Record r3 = new Record();
    	r3.set("ORG_CODE", "1540873750600");
    	r3.set("ORG_NAME", "天宁区电信");
    	r3.set("PARENT_ORG_CODE", "1540873745100");
    	
    	Record r4 = new Record();
    	r4.set("ORG_CODE", "1540873751700");
    	r4.set("ORG_NAME", "武进区电信");
    	r4.set("PARENT_ORG_CODE", "1540873745100");
    	
    	Record r5 = new Record();
    	r5.set("ORG_CODE", "1540873748400");
    	r5.set("ORG_NAME", "电力局");
    	r5.set("PARENT_ORG_CODE", "1540873747300");
    	
    	Record r6 = new Record();
    	r6.set("ORG_CODE", "1540873749500");
    	r6.set("ORG_NAME", "服务部");
    	r6.set("PARENT_ORG_CODE", "1540873747300");
    	
    	Record r7 = new Record();
    	r7.set("ORG_CODE", "1540957175983");
    	r7.set("ORG_NAME", "测试部");
    	r7.set("PARENT_ORG_CODE", "1540873747300");
    	
    	Record r8 = new Record();
    	r8.set("ORG_CODE", "1540873745100");
    	r8.set("ORG_NAME", "常州电信");
    	r8.set("PARENT_ORG_CODE", "-1");
    	
    	Record r9 = new Record();
    	r9.set("ORG_CODE", "1540873745122");
    	r9.set("ORG_NAME", "电力局AAA部门");
    	r9.set("PARENT_ORG_CODE", "1540873748400");
    	
    	Record r10 = new Record();
    	r10.set("ORG_CODE", "1540873745123");
    	r10.set("ORG_NAME", "电力局BBB部门");
    	r10.set("PARENT_ORG_CODE", "1540873748400");
    	
    	
    	list.add(r1);
    	list.add(r2);
    	list.add(r3);
    	list.add(r4);
    	list.add(r5);
    	list.add(r6);
    	list.add(r7);
    	list.add(r8);
    	list.add(r9);
    	list.add(r10);
    	
    	TreeUtils tu = new TreeUtils();
    	List<Record> chileList = tu.getChildNode(list, "1540873748400");
    	
    	System.out.println("chileList:" + chileList);*/
    	
    }
    
	
}
