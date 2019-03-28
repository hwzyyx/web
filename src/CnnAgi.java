import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.callke8.cnn.cnncallindata.CnnCallinData;
import com.callke8.cnn.cnndata.CnnData;
import com.callke8.cnn.cnnvoice.CnnVoice;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 改号通知的AGI流程
 * 
 * @author 黄文周
 *
 */
public class CnnAgi extends BaseAgiScript {

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		
		//从通道变量中,获取两个参数：主叫号码，被叫号码
		String callerId = channel.getVariable("cid");          //主叫号码
		String callee = channel.getVariable("callee");		   //被叫号码
		if(BlankUtils.isBlank(callerId) || BlankUtils.isBlank(callee)) {
			return;
		}
		//通过拨号方案传入的cidName信息，信息的格式：cidName=13512771995@172
		//通过该格式，即可获取主叫号码：13512771995
		String cidName = channel.getVariable("cidName");       
		if(!BlankUtils.isBlank(cidName)) {
			
			String getCallerIdValue = null;
			
			if(cidName.contains("@")) {
				String[] cidNameArray = cidName.split("@");     //以 @ 分隔
				//取出第一个字符串： <sip:13512771995
				getCallerIdValue = cidNameArray[0];   
			}else {
				getCallerIdValue = cidName;  
			}
			
			if(!callerId.equals(getCallerIdValue)) {
				callerId = getCallerIdValue;
			}
		}
		
		
		System.out.println("收到一个呼入来电：callerId=" + callerId + ";callee=" + callee);
		
		//判断被叫号码，是否已经改号，即查询 cnn_data 数据表，是否包含该被叫的数据
		Record cnnData = CnnData.dao.getCnnDataByCustomerTel(callee);                   //从数据表中将查询的数据取出
		
		
		//定义一个变量
		Record cnnCallinData = new Record();
		cnnCallinData.set("CALLERID",callerId);
		cnnCallinData.set("CALLEE", callee);
		cnnCallinData.set("CALL_DATE", DateFormatUtils.getCurrentDate());
		
		if(!BlankUtils.isBlank(cnnData)) {            //如果查询出来的数据不为空，则表示已经改号，需要播放已改号的语音提示
			
			String customerNewTel = cnnData.getStr("CUSTOMER_NEW_TEL");         //取出改号后的新号码
			String flag = cnnData.getStr("FLAG");                               //（中/英）语音标识符，1：中文; 2：英文
			
			cnnCallinData.set("STATE", "1");            				//因为有改号数据，这里将状态设置为1，即是已改号
			cnnCallinData.set("CUSTOMER_NEW_TEL",customerNewTel);		//设置客户新号码
			cnnCallinData.set("PK_CNN_DATA_ID", cnnData.getInt("ID"));	//
			
			if(!BlankUtils.isBlank(customerNewTel) && !BlankUtils.isBlank(flag)) {     //都不为空时
				
				String playBackFileList = getPlayBackFileList(customerNewTel, flag);
				System.out.println("播放列表=============：" + playBackFileList);
				exec("wait","0.5");
				exec("PlayBack",playBackFileList);   //执行播放列表
				//再播放一次
				exec("wait","0.5");
				exec("PlayBack",playBackFileList);   //执行播放列表
			}
			
			
		}else {
			cnnCallinData.set("STATE", "2");            //因为没有改号数据，这里将状态设置为2，即是未改号
		}
		
		boolean b = CnnCallinData.dao.add(cnnCallinData);
		
	}
	
	/**
	 * 获取执行 PlayBack 的文件播放列表
	 * 
	 * @param customerNewTel
	 * 			客户新号码
	 * @param flag
	 * @return
	 */
	public String getPlayBackFileList(String customerNewTel,String flag) {
		
		if(BlankUtils.isBlank(flag)) {    flag = "1";  }   //如果 flag 为空，则默认设置为 1，即是中文语音
		
		//定义一个返回的结果字符串，用于返回语音的列表
		StringBuilder sb = new StringBuilder();
		String path = ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle");    //取出语音路径
		
		//第一步：根据 flag 返回提示语音
		CnnVoice cnnVoice = CnnVoice.dao.getCnnVoiceByFlag(flag);
		
		if(BlankUtils.isBlank(cnnVoice)) {   //如果查询的提示音为空，直接返回空值
			return null; 
		}else {
			String voiceName = cnnVoice.getStr("VOICE_NAME");
			sb.append(path + "/" + voiceName);
		}
		
		//第二步：根据 客户新号码,返回号码的语音
		if(!flag.equals("1")) {    path = "en/digits"; }       //如果 flag 不为1，则语音切换到英文语音

		char[] telChars = customerNewTel.toCharArray();
		
		for(char tc:telChars) {
			sb.append("&" + path + "/" + tc);
		}
		
		return sb.toString();
	}
	
}
























