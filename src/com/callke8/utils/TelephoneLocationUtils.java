package com.callke8.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

public class TelephoneLocationUtils {
	
	public static final String DEF_CHATSET = "UTF-8";
	public static final int DEF_CONN_TIMEOUT = 30 * 1000;
	public static final int DEF_READ_TIMEOUT = 30 * 1000;
	
	public static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";
	
	/**
	 * 取得号码的归属地
	 * 
	 * @param telephone
	 * 				手机号码前7位或是完整的号码
	 * 
	 * @return 
	 * 			结果：{"resultcode":"200","reason":"Return Successd!","result":{"province":"广东","city":"广州","areacode":"020","zip":"510000","company":"移动","card":""},"error_code":0}
	 * 			返回的Map<String,String> 结果是：zip：邮编;province:省份;city:城市;company:(移动|联通);areacode:区号
	 * 
	 */
	public static Map<String,String> getTelephoneLocation(String telephone,String url,String appKey) {

		Map<String,String> rsMap = new HashMap<String,String>();
		String result = null;
		
		Map params = new HashMap();     //请求参数Map
		params.put("phone", telephone);
		params.put("key", appKey);
		params.put("dtype", "");
		
		try {
			result = getRequest(url,params,"GET");
			JSONObject object = JSONObject.fromObject(result);
			if(object.getInt("error_code")==0) {
				
				JSONObject rsObject = JSONObject.fromObject(object.get("result"));
				String zip = rsObject.getString("zip");
				String province = rsObject.getString("province");
				String city = rsObject.getString("city");
				String company = rsObject.getString("company");
				String areacode = rsObject.getString("areacode");
				
				rsMap.put("zip",zip);
				rsMap.put("province",province);
				rsMap.put("city",city);
				rsMap.put("company",company);
				rsMap.put("areaCode",areacode);
			}else {
				System.out.println(object.get("error_code") + ":" + object.get("reason"));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return rsMap;
	}
	
	/**
	 * @param strUrl    
	 * 				请求地址
	 * @param params
	 * 				请求参数
	 * @param method
	 * 				请求方式
	 * @return
	 * 				网络请求字符串
	 * @throws Exception
	 */
	public static String getRequest(String strUrl,Map<String,String> params,String method) throws Exception {
		
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		String rs = null;
		
		try {
			StringBuffer sb = new StringBuffer();
			
			if(method == null || method.equals("GET")) {
				strUrl = strUrl + "?" + urlencode(params);
			}
			System.out.println("strUrl---===:" + strUrl);
			URL url = new URL(strUrl);
			conn = (HttpURLConnection)url.openConnection();
			if(method == null || method.equals("GET")) {
				conn.setRequestMethod("POST");
			}else {
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
			}
			
			conn.setRequestProperty("User-agent", userAgent);
			conn.setUseCaches(false);
			conn.setConnectTimeout(DEF_CONN_TIMEOUT);
			conn.setReadTimeout(DEF_READ_TIMEOUT);
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			if(params != null && method.equals("POST")) {
				try {
					DataOutputStream out = new DataOutputStream(conn.getOutputStream());
					out.writeBytes(urlencode(params));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			InputStream is = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is,DEF_CHATSET));
			String strRead = null;
			while((strRead = reader.readLine()) != null) {
				sb.append(strRead);
			}
			rs = sb.toString();
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			if(reader != null) {
				reader.close();
			}
			if(conn != null) {
				conn.disconnect();
			}
		}
		System.out.println("结果：" + rs.toString());
		return rs;
	}
	
	public static String urlencode(Map<String,String> data) {
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry i:data.entrySet()) {
			try {
				sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
			}catch(UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
}
