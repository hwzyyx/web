package com.callke8.fastagi.common;

import com.callke8.fastagi.blacklist.BlackListController;
import com.callke8.fastagi.blacklist.BlackListInterceptRecordController;
import com.callke8.fastagi.transfer.TransferController;
import com.callke8.fastagi.transfer.TransferRecordController;
import com.callke8.fastagi.tts.TTSController;
import com.jfinal.config.Routes;

public class FastagiRoute extends Routes {

	@Override
	public void config() {
		add("blacklist", BlackListController.class, "/fastagi/blacklist");
		add("blacklistInterceptRecord", BlackListInterceptRecordController.class, "/fastagi/blacklistinterceptrecord");
		add("transfer", TransferController.class, "/fastagi/transfer");
		add("transferRecord", TransferRecordController.class, "/fastagi/transferrecord");
		add("tts",TTSController.class,"/fastagi/tts");
		
		
	}

}
