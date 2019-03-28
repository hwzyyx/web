package com.callke8.cnn.common;

import com.callke8.cnn.cnncallindata.CnnCallinDataController;
import com.callke8.cnn.cnndata.CnnDataController;
import com.callke8.cnn.cnnvoice.CnnVoiceController;
import com.jfinal.config.Routes;

public class CnnRoute extends Routes {

	@Override
	public void config() {
		
		add("/cnnData",CnnDataController.class,"/cnn/cnndata");
		add("/cnnCallinData",CnnCallinDataController.class,"/cnn/cnncallindata");
		add("/cnnVoice",CnnVoiceController.class,"/cnn/cnnvoice");
	}

}
