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
public class AtxferAgi extends BaseAgiScript {
	
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		
		exec("Read", "TEXTEN,en/pbx-transfer,0,s,,3");
		
		exec("Noop","hwz----" + channel.getVariable("TEXTEN"));
		
	}

}
