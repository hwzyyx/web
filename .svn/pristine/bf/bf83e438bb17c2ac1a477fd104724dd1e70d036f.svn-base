import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.callke8.fastagi.blacklist.BlackList;
import com.callke8.fastagi.blacklist.BlackListInterceptRecord;
import com.callke8.utils.BlankUtils;
import com.jfinal.plugin.activerecord.Record;


/**
 * 黑名单Agi
 * 
 * @author Administrator
 */
public class BlackListAgi extends BaseAgiScript {

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		String callerId = request.getCallerIdNumber();
		String callDate = channel.getVariable("CDR(start)");
		
		Record blacklist = BlackList.dao.getBlackListByClientTelephone(callerId);
		
		System.out.println("来电号码:" + callerId + ",来电时间:" + callDate + ",黑名单判断处理开始...");
		exec("Noop", "来电号码:" + callerId + ",来电时间:" + callDate + ",黑名单判断处理开始...");
		if(!BlankUtils.isBlank(blacklist)) {    //如果黑名单中有记录，就执行黑名单拦截操作，将来电挂机即可
			
			Record bir = new Record();
			bir.set("BLACKLIST_ID", blacklist.get("BLACKLIST_ID"));
			bir.set("CLIENT_NAME", blacklist.get("CLIENT_NAME"));
			bir.set("CLIENT_TELEPHONE", callerId);
			bir.set("CALLDATE", callDate);
			
			boolean b = BlackListInterceptRecord.dao.add(bir);
			
			System.out.println("来电号码:" + callerId + ",来电时间:" + callDate + ",在黑名单中有记录，系统将准备执行黑名单操作.");
			exec("Noop", "来电号码:" + callerId + ",来电时间:" + callDate + ",在黑名单中有记录，系统将准备执行黑名单操作.");
			
			exec("hangup");
		} else {
			System.out.println("来电号码:" + callerId + ",来电时间:" + callDate + ",在黑名单中没有记录，无需执行黑名单操作.");
			exec("Noop", "来电号码:" + callerId + ",来电时间:" + callDate + ",在黑名单中没有记录，无需执行黑名单操作.");
		}
		
	}

}
