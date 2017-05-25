import java.util.Date;

import org.apache.poi.ss.formula.ptg.MemErrPtg;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.callke8.fastagi.transfer.Transfer;
import com.callke8.fastagi.transfer.TransferRecord;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Record;


/**
 * 呼叫转移
 * 
 * @author <a href="mailto:120077407@qq.com">hwz</a>
 *
 */
public class TransferAgi extends BaseAgiScript {
	
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		
		String callerId = request.getCallerIdNumber();
		String did = channel.getVariable("EXTEN");
		String callDate = channel.getVariable("CDR(start)");
		
		System.out.println("来电号码:" + callerId + ",DID:" + did + ",来电时间:" + callDate + ",来电呼叫转移处理开始...");
		exec("Noop", "来电号码:" + callerId + ",DID:" + did + ",来电时间:" + callDate + ",来电呼叫转移处理开始...");
		
		Record transfer = Transfer.dao.getActiveTransfer(did);
		//如果在呼叫转移功能表时，有相关的呼叫转移的定义时，需要进行呼叫转移操作
		if(!BlankUtils.isBlank(transfer)) {   
			
			//组织录音文件名,并根据数据字典，取出录音文件的路径加入
			String monitorFileName = "transfer-" + callerId + "-" + did + "-" + transfer.get("DESTINATION") + "-" + DateFormatUtils.formatDateTime(DateFormatUtils.parseDateTime(callDate, "yyyy-MM-dd HH:mm:ss"), "yyyyMMddHHmmss") + ".wav";
			String monitorFileInfo = MemoryVariableUtil.getDictName("TRANSFER_MONITOR_DIR", "DIR") + "/" + monitorFileName;
			
			//创建一个呼叫转移记录
			Record transferRecord = new Record();
			transferRecord.set("CALLERID", callerId);
			transferRecord.set("DID",did);
			transferRecord.set("DESTINATION", transfer.get("DESTINATION"));
			transferRecord.set("CALLDATE", callDate);
			transferRecord.set("MEMO", transfer.get("MEMO"));
			transferRecord.set("TRUNK", transfer.get("TRUNK"));
			transferRecord.set("RECORDING_FILE", monitorFileName);
			
			boolean b = TransferRecord.dao.add(transferRecord);
			
			//需要呼叫转出的目标号码, 需要组织一个外呼的目标号码，如：  SIP/MTG-GW/135XXXX  这样的形式
			String trunk = transfer.get("TRUNK");
			String trunkInfo = MemoryVariableUtil.getDictName("TRUNK_INFO", trunk);
			String destination = transfer.get("DESTINATION");
			
			String dstInfo = trunkInfo + "/" + destination;
			
			System.out.println("来电号码:" + callerId + ",DID:" + did + ",来电时间:" + callDate + ",在呼叫转移定义中有记录，系统将准备执行呼叫转移功能,呼转至:" + dstInfo);
			exec("Noop","来电号码:" + callerId + ",DID:" + did + ",来电时间:" + callDate + ",在呼叫转移定义中有记录，系统将准备执行呼叫转移功能,呼转至:" + dstInfo);
			
			channel.exec("MixMonitor",monitorFileInfo);
			channel.setCallerId(did);
			exec("dial",dstInfo);
		}else {				//如果在呼叫转移功能表中，没有相关的定义时，则不需要执行呼叫转移，并返回到原流程中
			System.out.println("来电号码:" + callerId + ",DID:" + did + ",来电时间:" + callDate + ",在呼叫转移表中无定义,无需做呼叫转移");
			exec("Noop","来电号码:" + callerId + ",DID:" + did + ",来电时间:" + callDate + ",在呼叫转移表中无定义,无需做呼叫转移");
		}
		
	}

}
