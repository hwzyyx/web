package com.callke8.bsh.common;

import com.callke8.bsh.bshcallflow.BSHCallFlowController;
import com.callke8.bsh.bshcallparam.BSHCallParamController;
import com.callke8.bsh.bshorderlist.BSHDataStatisticsController;
import com.callke8.bsh.bshorderlist.BSHOrderListController;
import com.callke8.bsh.bshorderlist.BSHRealTimeDataController;
import com.callke8.bsh.bshvoice.BSHVoiceController;
import com.jfinal.config.Routes;

public class BSHRoute extends Routes {

	@Override
	public void config() {
		
		add("/bshOrderList",BSHOrderListController.class,"/bsh/bshorderlist");
		add("/bshCallFlow",BSHCallFlowController.class,"/bsh/bshcallflow");
		add("/bshVoice",BSHVoiceController.class,"/bsh/bshcallflow");
		add("/bshCallParam",BSHCallParamController.class,"/bsh/bshcallparam");
		add("/bshRealTimeData",BSHRealTimeDataController.class,"/bsh/bshrealtimedata");
		add("/bshDataStatistics",BSHDataStatisticsController.class,"/bsh/bshdatastatistics");
		
	}

}
