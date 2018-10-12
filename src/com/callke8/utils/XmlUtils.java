package com.callke8.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * XML 工具，注意本工具只对二级 xml字符串有效，多级的无效，只取两级
 * 
 * 比如:
 * <?xml version="1.0" encoding="utf-8" ?>
 * <returnsms>
 * 		<returnstatus>Failed</returnstatus>
 * 		<message>3</message>
 * 		<remainpoint>0</remainpoint>
 * </returnsms>
 * 
 * 就可以通过这个方法以 Record 去取值即可
 * 
 * 
 * 包括将 xml字符串，转为 Record 对象，转换后，可以以 ActiveRecord 的方式方便的读取对象的属性
 * 
 * @author 黄文周
 *
 */
public class XmlUtils {
	
	/**
	 * 将二级的 XML 字符串转到 Record 中存储
	 * 
	 * 注意：此方法只处理二级XML字符串，高于二级将无法处理
	 * 
	 * 比如：
	 * 
		 <?xml version="1.0" encoding="utf-8" ?>
		 <returnsms>
		  		<returnstatus>Failed</returnstatus>
		  		<message>3</message>
		  		<remainpoint>0</remainpoint>
		 </returnsms>
	 	
	 	 经此方法处理后，在 Record 中，可以直接通过取属性，得到属性值
	 	String xmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><returnsms><returnstatus>Failed</returnstatus><message>3</message><remainpoint>0</remainpoint></returnsms>";
	 	 如   Record r = xml2Record(xmlStr);
	 	 
	 	 String returnStatus = r.getStr("returnstatus");
	 	 String message = r.getStr("message");
	 	 
	 * @param xmlStr
	 * @return
	 */
	public static Record xml2Record(String xmlStr) {
		
		Record r = new Record();
		
		try {
			Document dm = DocumentHelper.parseText(xmlStr);
			
			Element rootElement = dm.getRootElement();
			
			//System.out.println(rootElement);
			for(Iterator<?> iterator = rootElement.elementIterator();iterator.hasNext();) {
				
				Element element = (Element)iterator.next();
				
				String name = element.getName();
				Object data = element.getData();
				
				if(!BlankUtils.isBlank(name) && !BlankUtils.isBlank(data)) {
					r.set(name, data.toString());
				}
				
			}
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return r;
	}
	
	public static void main(String[] args) {
		String tel = "013512001995";
		String tel2 = "137903432334";
		
		boolean b = NumberUtils.isCellPhone(tel);
		boolean b2 = NumberUtils.isCellPhone(tel2);
		
		System.out.println("号码：" + tel + " 是手机号码吗？ 结果是：" + b);
		System.out.println("号码：" + tel2 + " 是手机号码吗？ 结果是：" + b2);
		
		System.out.println(tel.startsWith("0"));
		System.out.println(tel2.startsWith("0"));
	}
	
}
