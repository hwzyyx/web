package com.callke8.autocall.common;

import com.callke8.autocall.autoblacklist.AutoBlackListController;
import com.callke8.autocall.autoblacklist.AutoBlackListTelephoneController;
import com.callke8.autocall.autocalltask.AutoCallTaskController;
import com.callke8.autocall.autocalltask.AutoCallTaskRealTimeDataController;
import com.callke8.autocall.autocalltask.AutoCallTaskResultController;
import com.callke8.autocall.autocalltask.AutoCallTaskReviewController;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephoneController;
import com.callke8.autocall.autocalltask.history.AutoCallTaskHistoryController;
import com.callke8.autocall.autocalltask.history.AutoCallTaskTelephoneHistoryController;
import com.callke8.autocall.autocalltaskreport.AutoCallTaskReportController;
import com.callke8.autocall.autocalltaskreport.AutoCallTaskReportGroupByOperIdController;
import com.callke8.autocall.autonumber.AutoNumberController;
import com.callke8.autocall.autonumber.AutoNumberTelephoneController;
import com.callke8.autocall.flow.AutoFlowController;
import com.callke8.autocall.questionnaire.QuestionController;
import com.callke8.autocall.questionnaire.QuestionItemController;
import com.callke8.autocall.questionnaire.QuestionnaireController;
import com.callke8.autocall.questionnaire.QuestionnaireRespondController;
import com.callke8.autocall.schedule.ScheduleController;
import com.callke8.autocall.voice.VoiceController;
import com.jfinal.config.Routes;

public class AutoCallRoute extends Routes {

	@Override
	public void config() {
		add("/schedule",ScheduleController.class,"/autocall/schedule");
		add("/autoCallTask",AutoCallTaskController.class,"/autocall/autocalltask");
		add("/autoCallTaskResult",AutoCallTaskResultController.class,"/autocall/autocalltask");
		add("/autoCallTaskRealTimeData",AutoCallTaskRealTimeDataController.class,"/autocall/autocalltaskrealtimedata");
		add("/autoCallTaskHistory",AutoCallTaskHistoryController.class,"/autocall/autocalltaskhistory");
		add("/autoCallTaskTelephone",AutoCallTaskTelephoneController.class,"/autocall/autocalltask");
		add("/autoCallTaskTelephoneHistory",AutoCallTaskTelephoneHistoryController.class,"/autocall/autocalltaskhistory");
		add("/autoCallTaskReview",AutoCallTaskReviewController.class,"/autocall/autocalltask");
		add("/voice",VoiceController.class,"/autocall/voice");
		add("/questionnaire",QuestionnaireController.class,"/autocall/questionnaire");
		add("/questionnaireRespond",QuestionnaireRespondController.class,"/autocall/questionnaire");
		add("/question",QuestionController.class,"/autocall/questionnaire");
		add("/questionItem",QuestionItemController.class,"/autocall/questionnaire");
		add("/autoBlackList",AutoBlackListController.class,"/autocall/blacklist");
		add("/autoBlackListTelephone",AutoBlackListTelephoneController.class,"/autocall/blacklist");
		add("/autoNumber",AutoNumberController.class,"/autocall/number");
		add("/autoNumberTelephone",AutoNumberTelephoneController.class,"/autocall/number");
		add("/autoCallTaskReport",AutoCallTaskReportController.class,"/autocall/autocalltaskreport");
		add("/autoCallTaskReportGroupByOperId",AutoCallTaskReportGroupByOperIdController.class,"/autocall/autocalltaskreport");
		add("/autoFlow",AutoFlowController.class,"/autocall/flow");
	}

}
