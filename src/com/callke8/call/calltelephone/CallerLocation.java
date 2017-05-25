package com.callke8.call.calltelephone;

import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
/**
 * CREATE TABLE `callerloc` (
  `ID` int(10) NOT NULL AUTO_INCREMENT COMMENT 'id,自动增长',
  `NUM` varchar(7) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '号码的前几位数',
  `PROVINCE` varchar(50) DEFAULT NULL COMMENT '省份',
  `CITY` varchar(50) DEFAULT NULL COMMENT '城市',
  `CARDTYPE` varchar(50) DEFAULT NULL COMMENT '卡的类型',
  PRIMARY KEY (`ID`),
  KEY `NUM_KEY` (`NUM`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=258421 DEFAULT CHARSET=utf8;
 * @author Administrator
 *
 */
public class CallerLocation extends Model<CallerLocation> {
	
	public static CallerLocation dao = new CallerLocation();
	
	
	/**
	 * 根据号码的前缀，获取归属地
	 * 
	 * @param prefix
	 * @return
	 */
	public String getLocationByPrefix(String prefix) {
		
		if(BlankUtils.isBlank(prefix)) {
			return null;
		}
		
		String sql = "select PROVINCE from callerloc where NUM=? limit 1";
		
		Record record = Db.findFirst(sql, prefix);
		
		return record.get("PROVINCE");
	}
	
}
