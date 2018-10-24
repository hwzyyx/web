package com.callke8.utils;

import java.util.HashMap;
import java.util.Map;

import com.callke8.system.param.ParamConfig;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 发送信息工具类
 * 
 * @author 黄文周
 *
 */
public class SendMessageUtils {
	
	/**
	 * 发送信息
	 * 
	 * 成功返回的字符串如下：
	 * <?xml version="1.0" encoding="utf-8" ?>
	 * <returnsms>
	 * 		<returnstatus>Success</returnstatus>
	 * 		<message>OK</message>
	 * 		<remainpoint>99520</remainpoint>
	 * 		<taskID>AC41498382730D13</taskID>
	 * 		<resplist>
	 * 			<resp>AC41498382730D13#@#13512771995#@#0#@#</resp>
	 * 		</resplist>
	 * 		<successCounts>1</successCounts>
	 *</returnsms>
	 *
	 * 失败返回的字符串如下：
	 * <?xml version="1.0" encoding="utf-8" ?>
	 * <returnsms>
	 * 		<returnstatus>Failed</returnstatus>
	 * 		<message>3</message>
	 * 		<remainpoint>0</remainpoint>
	 * </returnsms>
	 * 
	 * 我们只需要两个属性值即可：  returnstatus 和 message
	 * 
	 * 返回的 Record 包括了几个信息：
	 * 
	 * returnstatus: 成功返回 success, 失败返回: Failed
	 * message:  成功 为 OK，失败则是一些错误码
	 * 
	 * @param content
	 * @param phoneNumber
	 * 			全部被叫号码	发信发送的目的号码.多个号码之间用半角逗号隔开 
	 * @return
	 * 		返回一个 Record,包含两个信息: (1)returnstatus,成功(Success)、失败(Failed) （2）message, 成功(OK),失败(数字错误代码)
	 */
	public static Record sendMessage(String content,String phoneNumber) {
		
		String messageUrl = ParamConfig.paramConfigMap.get("paramType_5_messageUrl");             //发送信息 URL
		String messageAccount = ParamConfig.paramConfigMap.get("paramType_5_messageAccount");	  //发送用户帐号	用户帐号，由系统管理员提供
		String messagePassword = ParamConfig.paramConfigMap.get("paramType_5_messagePassword");	  //发送帐号密码	用户账号对应的密码
		String messageExtno = ParamConfig.paramConfigMap.get("paramType_5_messageExtno");		  //接入号	接入号，即 10690XXXXXX类似的号码
		
		//组织发送信息的参数
		Map<String,String> params = new HashMap<String,String>();
		params.put("extno",messageExtno);								//接入号	接入号，即 10690XXXXXX类似的号码
		params.put("account",messageAccount);							//发送用户帐号	用户帐号，由系统管理员
		params.put("password",messagePassword);							//发送帐号密码	用户账号对应的密码
		params.put("action","send");                    				//发送任务命令	设置为固定的:send
		params.put("mobile",phoneNumber);								//全部被叫号码	发信发送的目的号码.多个号码之间用半角逗号隔开 
		params.put("content",content);									//发送内容	短信的内容，内容需要UTF-8编码
		
		//请求发送，并返回信息服务器的响应
		//响应的字符串格式：
		//成功:<?xml version="1.0" encoding="utf-8" ?><returnsms><returnstatus>Failed</returnstatus><message>3</message><remainpoint>0</remainpoint></returnsms>
		//失败:<?xml version="1.0" encoding="utf-8" ?><returnsms><returnstatus>Failed</returnstatus><message>3</message><remainpoint>0</remainpoint></returnsms>
		String response = HttpClientUtil.doPost(messageUrl, params, "UTF-8");      
		if(BlankUtils.isBlank(response)) {
			Record r = new Record();
			r.set("returnstatus","Failed");
			r.set("message","102");
			return r;
		}
		
		//System.out.println("号码 :" + phoneNumber + "发送短信返回的xml字符串是:" + response);
		
		//调用 xmlUtils ，将xml字符串转为 Record, 然后返回
		Record record = XmlUtils.xml2Record(response);
		
		return record;
		
	}
	
}
