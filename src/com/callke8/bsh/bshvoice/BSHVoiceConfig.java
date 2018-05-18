package com.callke8.bsh.bshvoice;

import java.util.HashMap;
import java.util.Map;

public class BSHVoiceConfig {
	
	/**
	 * BSH语音列表中语音的配置，Map<voiceId,FILE_NAME>  即是语音ID，语音文件名
	 */
	private static Map<String,String> voiceMap = new HashMap<String,String>();

	public static Map<String, String> getVoiceMap() {
		return voiceMap;
	}

	public static void setVoiceMap(Map<String, String> voiceMap) {
		BSHVoiceConfig.voiceMap = voiceMap;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("博世家电语音配置详情:\r\n");
		
		for (Map.Entry<String, String> entry : voiceMap.entrySet()) { 
			//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 
			sb.append(entry.getKey() + "=" + entry.getValue() + "\r\n");     //
		}
		
		return sb.toString();
		
	}
	
}
