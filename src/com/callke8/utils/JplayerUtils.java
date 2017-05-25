package com.callke8.utils;

import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Record;

public class JplayerUtils {
	
	/**
	 * 生成播放器字符串
	 * 
	 * @param idIndex
	 * 			 id的顺序(1至N),语音列表时，生成多个播放器
	 * @param playerName 
	 * 			 playerName 主要是用于限定播放器的ID的名字，因为要在别的模块中引用别一个模块
	 * 			 
	 * @return
	 */
	public static String getPlayerSkin(int idIndex,String playerName) {
		
		StringBuilder sb = new StringBuilder();
		
		String idInfo = "jp_" + playerName + "_" + idIndex;
		String containerInfo = "jp_container_" + playerName + "_" + idIndex;

		sb.append("<div style='float:left' id='" + idInfo + "' class='jp-jplayer'></div>");
		sb.append("<div style='float:left' id='" + containerInfo + "'>");
		sb.append("<div class='jp-type-single'>");
		sb.append("<div class='jp-controls'>");
		sb.append("<button class='jp-play' role='button' tabindex='0'>play</button>");
		sb.append("</div>");
		sb.append("</div>");
		sb.append("</div>");
		
		return sb.toString();
	}
	
	/**
	 * 生成播放器函数
	 * 
	 * @param idIndex
	 * @param path
	 * @param playerName
	 * 			playerName 主要是用于限定播放器的ID的名字，因为要在别的模块中引用别一个模块
	 * @return
	 */
	public static String getPlayerFunction(int idIndex,String path,String playerName) {
		
		StringBuilder sb = new StringBuilder();
		
		String idInfo = "jp_" + playerName + "_" + idIndex;
		String containerInfo = "jp_container_" + playerName + "_" + idIndex;

		sb.append("$('#" + idInfo + "').jPlayer({");
		sb.append("ready:function(event){");
		sb.append("$(this).jPlayer('setMedia',{");
		sb.append("title:'Bubble',");
		sb.append("wav:'" + path + "'");
		sb.append("});");
		sb.append("},");
		
		sb.append("swfPath:'../../dist/jplayer',");
		sb.append("supplied:'m4a,oga,wav',");
		sb.append("cssSelectorAncestor:'#" + containerInfo + "',");
		sb.append("wmode:'window',");
		sb.append("useStateClassSkin:true,");
		sb.append("autoBlur:false,");
		sb.append("smoothPlayBar:true,");
		sb.append("keyEnabled: true,");
		sb.append("remainingDuration: true,");
		sb.append("toggleDuration: true");
		sb.append("});");

		return sb.toString();
	}
	
	
	/**
	 * 生成播放列表播放器
	 * 
	 * @param playList
	 * @return
	 */
	public static String getPlayerListFunction(List<Record> playList) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("new jPlayerPlaylist({");
		sb.append("jPlayer: '#jquery_jplayer_1',");
		sb.append("cssSelectorAncestor: '#jp_container_1'");
		sb.append("},[");
		//遍历播放列表
		
		for(int i=0;i<playList.size();i++) {
			Record voiceRecord = playList.get(i);
			sb.append("{");
				sb.append("title:'" + voiceRecord.get("title") + "',");
				sb.append("wav:'" + voiceRecord.get("path") + "'");
			if(i == (playList.size()-1)) {
				sb.append("}");
			}else {
				sb.append("},");
			}
		}
		
		//遍历播放列表结束
		sb.append("],{");
		sb.append("swfPath:'../../dist/jplayer',");
		sb.append("supplied:'m4a,oga,wav',");
		sb.append("wmode:'window',");
		sb.append("useStateClassSkin:true,");
		sb.append("autoBlur:false,");
		sb.append("smoothPlayBar:true,");
		sb.append("keyEnabled: true");
		sb.append("});");
		
		return sb.toString();
	}
	
	
	
}
