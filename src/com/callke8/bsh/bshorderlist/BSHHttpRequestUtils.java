package com.callke8.bsh.bshorderlist;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * 博世电器的呼叫结果反馈信息
 * 
 * @author 黄文周
 *
 */
public class BSHHttpRequestUtils {
	
	/**
	 * 通过 http 请求向BSH服务器反馈呼叫结果
	 * 
	 * 参数	说明
		orderId	订单号id
		callType	外呼类型0.二次未接通1.一次接通/二次接通2放弃呼叫3已过期
		time	时间（yyyyMMddHHmmss）
		sign	签名（全小写）= md5(time + orderId+ key)key为约定好的密钥
		callResult	外呼结果 1：确认建单   2 暂不安装  3 短信确认   4 错误或无回复  5 放弃呼叫 6已过期

	 * 
	 * @param url
	 * 			URL
	 * @param key
	 * 			KEY
	 * @param orderId
	 * 			订单ID
	 * @param callType
	 * 			呼叫类型
	 * @param time
	 * 			时间
	 * @param sign
	 * 			签名
	 * @param callResult
	 * 			呼叫结果
	 */
	public static void httpRequestForCallResult(String url,String orderId,String callType,String time,String sign,String callResult) {
		
		HttpClient httpClient = new HttpClient();
		
		HttpMethod method = new GetMethod(url + "?orderId=" + orderId + "&callType=" + callType + "&time=" + time + "&sign=" + sign + "&callResult=" + callResult);
		
		try {
			
			httpClient.executeMethod(method);
			
			//打印服务器返回的信息
			String resToString = method.getResponseBodyAsString();
			
			System.out.println("向BSH服务器反馈呼叫结果，反馈URL信息为：" + method.getURI() + ",由BSH服务器返回的信息: " + resToString);
			
		}catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}

















