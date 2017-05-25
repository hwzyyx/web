package com.callke8.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;
/**
 * TreeHelper类
 * @author Administrator
 *
 */
public class TreeHelper {
	
	List<Record> list;
	String moduleCode;
	String moduleName;
	String parentCode;
	String defaultParentCode;
	String url;
	
	int rootCount = 0;
	String rootModuleCode = null;    
	String rootModuleName = null;
	
	public TreeHelper(List<Record> list,String moduleCode,String moduleName,String parentCode,String defaultParentCode,String url) {
		
		this.list = list;
		this.moduleCode = moduleCode;
		this.moduleName = moduleName;
		this.parentCode = parentCode;
		this.defaultParentCode = defaultParentCode;
		this.url = url;
	}
	
	/**
	 * 根据 List<Record> 取得Tree
	 * @param list 
	 * 			List<Record> 记录
	 * @param moduleCode
	 * 			模块代码
	 * @param moduleName
	 * 			模块名字
	 * @param parentCode
	 * 			parentCode
	 * @param url
	 * 			URL
	 * @return
	 */
	public String getTree() {
		
		//检查是否为单根
		boolean singleRoot = singleRootCheck();
		
		//当为单根时
		if(singleRoot) {   
			
			return getTree4SingleRoot();
			
		}
		
		return null;
	}
	
	/**
	 * 单根取树
	 */
	public String getTree4SingleRoot() {
		
		StringBuilder sb = new StringBuilder(); 
		String searchPC = this.rootModuleCode;
		
		sb.append("<li><a id='clickRoot' href='" + this.url + "?moduleCode=" + rootModuleCode +"' target='navTab4Org' rel='showOrgDetail'>" + rootModuleName + "</a> ");
//		sb.append("<li><a id='clickRoot' href='" + this.url + "?moduleCode=" + rootModuleCode +"' target='ajax' rel='jbsxBox'>" + rootModuleName + "</a> ");
		
		getTreeByLevel(rootModuleCode,sb);
		
		sb.append("</li>");
		
		return sb.toString();
		
	}
	
	
	public void getTreeByLevel(String searchPC,StringBuilder sb) {
		
		
			List<Record> list = getListByParentCode(searchPC);
			for(Record re:list) {
				sb.append("<ul>");
				sb.append("<li>");
//				sb.append("<a href='" + this.url + "?moduleCode=" + re.get(moduleCode)+ "' target='ajax' rel='jbsxBox'>" + re.get(moduleName) + "</a>");
				sb.append("<a href='" + this.url + "?moduleCode=" + re.get(moduleCode)+ "' target='navTab4Org' rel='showOrgDetail'>" + re.get(moduleName) + "</a>");
				String sp = re.get(moduleCode);
				getTreeByLevel(sp, sb);
				sb.append("</li>");
				sb.append("</ul>");
			}
		
	}
	
	
	public List<Record> getListByParentCode(String p) {
		
		List<Record> ls = new ArrayList<Record>();
		
		Iterator<Record> iter = list.iterator();
		while(iter.hasNext()) {
			Record re = iter.next();
			
			String mc = re.get(moduleCode);
			String mn = re.get(moduleName);
			String pmc = re.get(parentCode);
			
			if(pmc.equalsIgnoreCase(p)) {
				ls.add(re);
			}
			
		}
		
		return ls;
	}
	
	/**
	 * 判定是否只有一个根，主要是根据传入的 parentCode 是否只有一个
	 * 
	 * @return
	 */
	public boolean singleRootCheck() {
		
		boolean b = true;
		
		for(Record rcd:list) {
			String mc = rcd.get(moduleCode);
			String mn = rcd.get(moduleName);
			String pmc = rcd.get(parentCode);
			
			if(pmc.equalsIgnoreCase(defaultParentCode)) {   //判断是否与默认 parentCode相同
				rootModuleCode = mc;      //设置
				rootModuleName = mn;
				rootCount++ ;
			}
		}
		
		if(rootCount>1) {   //如果大于一个根时设置为false
			b = false;
		}
		
		return b;
	}

	public String getRootModuleCode() {
		return rootModuleCode;
	}

	public void setRootModuleCode(String rootModuleCode) {
		this.rootModuleCode = rootModuleCode;
	}

	public String getRootModuleName() {
		return rootModuleName;
	}

	public void setRootModuleName(String rootModuleName) {
		this.rootModuleName = rootModuleName;
	}
	
	
}
