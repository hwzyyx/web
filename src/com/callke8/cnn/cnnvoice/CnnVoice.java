package com.callke8.cnn.cnnvoice;

import java.util.*;

import com.callke8.system.operator.Operator;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.*;
import com.jfinal.plugin.activerecord.*;

public class CnnVoice extends Model<CnnVoice>  {

	private static final long serialVersionUID = 1L;
	public static CnnVoice dao = new CnnVoice();

	public Page getCnnVoiceByPaginate(int pageNumber,int pageSize,String voiceDesc,String flag) {

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;

		sb.append("from cnn_voice where 1=1");

		//条件判断暂时不自动添加
		if(!BlankUtils.isBlank(voiceDesc)) {
			sb.append(" and VOICE_DESC like ?");
			pars[index] = "%" + voiceDesc + "%";
			index++;
		}
		
		//flag
		if(!BlankUtils.isBlank(flag) && !flag.equalsIgnoreCase("empty")) {
			sb.append(" and FLAG=?");
			pars[index] = flag;
			index++;
		}

		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString()+" ORDER BY ID DESC",ArrayUtils.copyArray(index, pars));
		return p;
	}

	public Map getCnnVoiceByPaginateToMap(int pageNumber,int pageSize,String voiceDesc,String flag) {

		Page<Record> p =  getCnnVoiceByPaginate(pageNumber,pageSize,voiceDesc,flag);

		int total = p.getTotalRow();     //取出总数量

		List<Record> newList = new ArrayList<Record>();
		int idIndex = 1;              //定义一个变量，用于生成播放器
		for(Record r:p.getList()) {
			
			//设置操作员名字（工号）
			String uc = r.getStr("CREATE_USERCODE");   //取出创建人工号
			Operator oper = Operator.dao.getOperatorByOperId(uc);
			if(!BlankUtils.isBlank(oper)) {
				r.set("CREATE_USERCODE_DESC", oper.get("OPER_NAME") + "(" + uc + ")");
			}
			
			//设置试听的路径
			String path =  ParamConfig.paramConfigMap.get("paramType_4_voicePath") + "/" + r.get("VOICE_NAME") + ".wav";
			//System.out.println("path:=======:" + path);
			r.set("path", path);
			r.set("listenFileName", r.get("VOICE_NAME") + ".wav");
			
			//设置播放器外观
			String playerSkin = JplayerUtils.getPlayerSkin(idIndex,"voice");
			r.set("playerSkin", playerSkin);
			
			//设置播放器函数
			String playerFunction = JplayerUtils.getPlayerFunction(idIndex, path,"voice");
			r.set("playerFunction", playerFunction);
			
			newList.add(r);
			
			idIndex++;
			
		}
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", newList);

		return map;
	}
	public boolean add(CnnVoice formData) {

		Record r = new Record();
		r.set("VOICE_DESC", formData.get("VOICE_DESC"));
		r.set("FLAG", formData.get("FLAG"));
		r.set("VOICE_NAME",formData.get("VOICE_NAME"));
		r.set("CREATE_USERCODE", formData.get("CREATE_USERCODE"));
		r.set("CREATE_TIME",formData.get("CREATE_TIME"));

		return add(r);
	}

	public boolean add(Record record) {

		boolean b = Db.save("cnn_voice", "ID", record);
		return b;

	}

	public boolean update(String voiceDesc,int flag,String voiceName,int id) {

		boolean b = false;

		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[10];
		int index = 0;
		
		sb.append("update cnn_voice set ");
		
		if(!BlankUtils.isBlank(voiceDesc)) {
			sb.append("VOICE_DESC=?");
			pars[index] = voiceDesc;
			index++;
		}
		
		if(flag!=0) {
			sb.append(",FLAG=?");
			pars[index] = flag;
			index++;
		}
		
		if(!BlankUtils.isBlank(voiceName)) {
			sb.append(",VOICE_NAME=?");
			pars[index]=voiceName;
			index++;
		}
		
		if(id!=0) {
			sb.append(" where ID=?");
			pars[index] = id;
			index++;
		}
		
		int count = Db.update(sb.toString(),ArrayUtils.copyArray(index, pars));
		
		if(count > 0) {
			b = true;
		}
		return b;

	}

	public CnnVoice getCnnVoiceById(int id){

		String sql = "select * from cnn_voice where ID=?";
		CnnVoice entity = findFirst(sql, id);
		return entity;

	}
	
	/**
	 * 根据语音内容,查询记录
	 * 
	 * @param voiceDesc
	 * @return
	 */
	public Record getCnnVoiceByVoiceDesc(String voiceDesc) {
		
		String sql = "select * from cnn_voice where VOICE_DESC=?";
		
		Record r = Db.findFirst(sql,voiceDesc);
		
		return r;
	}

	public boolean deleteById(String id) {

		boolean b = false;
		int count = 0;
		count = Db.update("delete from cnn_voice where ID=?",id);
		if(count > 0) {
			b = true;
		}
		return b;

	}
	
	/**
	 * 根据 Flag 查询最新的那条语音记录
	 * 
	 * @return
	 */
	public CnnVoice getCnnVoiceByFlag(String flag) {
	
		String sql = "select * from cnn_voice where FLAG=? order by ID desc";
		
		CnnVoice cnnVoice = findFirst(sql, flag);
		
		return cnnVoice;
	}
	
}
