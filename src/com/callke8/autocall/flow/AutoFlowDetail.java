package com.callke8.autocall.flow;

import java.util.ArrayList;
import java.util.List;

import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动外呼流程规则详情
 * 
 * @author 黄文周
 *
 */
public class AutoFlowDetail extends Model<AutoFlowDetail> {
	
	private static final long serialVersionUID = 1L;

	public static final AutoFlowDetail dao = new AutoFlowDetail();

	/**
	 * 根据 流程规则 ID，取得流程规则列表
	 * 
	 * @param flowId
	 * @return
	 */
	public List<Record> getAutoFlowDetailByFlowId(String flowId) {
		
		String sql = "select * from ac_flow_detail where FLOW_ID=? order by ORDER_INDEX asc";
		
		List<Record> list = Db.find(sql, flowId);
		
		return list;
	}
	
	/**
	 * 根据流程规则 ID，删除所有的详情记录
	 * 
	 * @param flowId
	 */
	public int deleteAutoFlowDetailByFlowId(String flowId) {
		
		String sql = "delete from ac_flow_detail where FLOW_ID=?";
		
		int count = Db.update(sql, flowId);
		
		return count;
		
	}
	
	/**
	 * 根据详情ID, 设置语音文件名字
	 * 
	 * @param voiceName
	 * @param detailId
	 * @return
	 */
	public int updateAutoFlowDetailVoiceNameById(String voiceName,int detailId) {
		
		String sql = "update ac_flow_detail set VOICE_NAME=? where DETAIL_ID=?";
		
		int count = Db.update(sql, voiceName,detailId);
		
		return count;
	}
	
	/**
	 * 新增流程规则详情
	 * 
	 * @param au
	 * @return
	 */
	public boolean add(Record autoFlowDetail) {
		
		boolean b = Db.save("ac_flow_detail", "DETAIL_ID", autoFlowDetail);
		
		return b;
	}
	
	/**
	 * 批量添加数据
	 * 
	 * @param autoFlowDetails
	 * @return
	 */
	public int batchSave(ArrayList<Record> autoFlowDetails) {
		
		if(BlankUtils.isBlank(autoFlowDetails) || autoFlowDetails.size()==0) {
			return 0;
		}
		
		String sql = "insert into ac_flow_detail(FLOW_ID,ORDER_INDEX,CONTENT,VOICE_NAME) values(?,?,?,?)";
		
		int[] insertData = Db.batch(sql,"FLOW_ID,ORDER_INDEX,CONTENT,VOICE_NAME",autoFlowDetails,100);
		
		return insertData.length;
		
	}
	
}
