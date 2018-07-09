package com.callke8.common;

import java.util.HashMap;
import java.util.Map;

import org.quartz.SchedulerException;

import com.callke8.astutils.AgentStateMonitor;
import com.callke8.astutils.AsteriskConfig;
import com.callke8.astutils.AsteriskConnectionPool;
import com.callke8.astutils.AsteriskUtils;
import com.callke8.autocall.autoblacklist.AutoBlackList;
import com.callke8.autocall.autoblacklist.AutoBlackListTelephone;
import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.autocall.autocalltask.history.AutoCallTaskHistory;
import com.callke8.autocall.autocalltask.history.AutoCallTaskTelephoneHistory;
import com.callke8.autocall.autonumber.AutoNumber;
import com.callke8.autocall.autonumber.AutoNumberTelephone;
import com.callke8.autocall.common.AutoCallRoute;
import com.callke8.autocall.questionnaire.Question;
import com.callke8.autocall.questionnaire.QuestionItem;
import com.callke8.autocall.questionnaire.Questionnaire;
import com.callke8.autocall.questionnaire.QuestionnaireRespond;
import com.callke8.autocall.schedule.Schedule;
import com.callke8.autocall.voice.Voice;
import com.callke8.bsh.bshcallparam.BSHCallParam;
import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.bsh.bshvoice.BSHVoice;
import com.callke8.bsh.bshvoice.BSHVoiceConfig;
import com.callke8.bsh.common.BSHRoute;
import com.callke8.call.calltask.CallTask;
import com.callke8.call.calltelephone.CallTelephone;
import com.callke8.call.calltelephone.CallerLocation;
import com.callke8.call.common.CallRoute;
import com.callke8.call.incoming.InComing;
import com.callke8.fastagi.autocontact.AutoContact;
import com.callke8.fastagi.autocontact.AutoContactRecord;
import com.callke8.fastagi.blacklist.BlackList;
import com.callke8.fastagi.blacklist.BlackListInterceptRecord;
import com.callke8.fastagi.common.FastagiRoute;
import com.callke8.fastagi.transfer.Transfer;
import com.callke8.fastagi.transfer.TransferRecord;
import com.callke8.predialqueue.Predial;
import com.callke8.pridialqueueforbshbyquartz.BSHPredial;
import com.callke8.report.cdr.Cdr;
import com.callke8.report.clientinfo.ClientInfo;
import com.callke8.report.clienttouch.ClientTouchRecord;
import com.callke8.report.common.ReportRoute;
import com.callke8.system.common.SystemRoute;
import com.callke8.system.dict.DictGroup;
import com.callke8.system.dict.DictItem;
import com.callke8.system.loginlog.LoginLog;
import com.callke8.system.module.Module;
import com.callke8.system.operationlog.OperationLog;
import com.callke8.system.operator.OperRole;
import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.system.role.Role;
import com.callke8.system.rolegroup.RoleGroup;
import com.callke8.system.rolemodule.RoleModule;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.render.ViewType;

public class CommonConfig extends JFinalConfig {

	@Override
	public void configConstant(Constants me) {
		me.setEncoding("UTF-8");
		me.setDevMode(true);
		me.setViewType(ViewType.JSP);
		
		//国际化配置
		//me.setI18n("app", Locale.SIMPLIFIED_CHINESE, Integer.MAX_VALUE);
		//me.setI18n("com.callke8.system.role.role", Locale.SIMPLIFIED_CHINESE, Integer.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configHandler(Handlers me) {
		//先判断数据字典的内容是否为空，如果为空时，则需要加载一下数据字典到内存
		if(BlankUtils.isBlank(MemoryVariableUtil.dictMap)) {    //如果为空时，则将数据字典的内容写入内存变量
			MemoryVariableUtil.dictMap = DictGroup.dao.loadDictInfo();
			System.out.println("执行数据字典数据初始化到内存变量!");
		}
		
		//先判断内存数据菜单数据是否为空，如果为空，则需要加载菜单数据到内存
		if(BlankUtils.isBlank(MemoryVariableUtil.moduleMap)) {
			MemoryVariableUtil.moduleMap = Module.dao.loadModuleInfo();
			System.out.println("执行菜单数据初始化到内存变量!");
		}
		
		//先判断内存数据操作员数据是否为空，如果为空，则需要加载操作员数据到内存
		if(BlankUtils.isBlank(MemoryVariableUtil.operatorMap)) {
			MemoryVariableUtil.operatorMap = Operator.dao.loadOperatorInfo();
			System.out.println("执行操作员数据初始化到内存变量!");
		}
		
		//加载博世电器的呼叫参数到内存中，加载成功后，打印配置详情
		System.out.println("----------=============--------------");
		System.out.println("准备加载博世电器的呼叫参数到内存!");
		BSHCallParam.dao.loadCallParamDataToMemory();
		System.out.println(new BSHCallParamConfig());
		
		System.out.println("-----------============-------------");
		System.out.println("准备加载博世电器的语音数据到内存!");
		BSHVoice.dao.loadBSHVoiceDataToMemory();
		System.out.println(new BSHVoiceConfig());
		
	}

	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new CommonInterceptor());
	}

	@Override
	public void configPlugin(Plugins me) {
		loadPropertyFile("commonconfig.properties");
		
		//将配置中的自动接触中的context和channel的内容加载到内存
		Map<String,String> map = new HashMap<String,String>();
		map.put("autoContactContext",getProperty("autoContactContext"));
		map.put("autoContactChannel",getProperty("autoContactChannel"));
		map.put("autoContactRecordDir",getProperty("autoContactRecordDir"));
		
		MemoryVariableUtil.autoContactMap = map;
		
		//将配置中的语音保存路径加载到内存
		Map<String,String> voiceMap = new HashMap<String,String>();
		voiceMap.put("cdrVoicePath", getProperty("cdrVoicePath"));
		voiceMap.put("autocallVoicePath", getProperty("autocallVoicePath"));
		voiceMap.put("autocallVoiceVoxPath", getProperty("autocallVoiceVoxPath"));
		
		MemoryVariableUtil.voicePathMap = voiceMap;
		
		//将配置中的TTS参数加载到内存
		Map<String,String> ttsParamMap = new HashMap<String,String>();
		ttsParamMap.put("grant_type", getProperty("tts_grant_type"));
		ttsParamMap.put("client_id", getProperty("tts_client_id"));
		ttsParamMap.put("client_secret", getProperty("tts_client_secret"));
		ttsParamMap.put("access_token_url", getProperty("tts_access_token_url"));
		ttsParamMap.put("exec_tts_url", getProperty("tts_exec_tts_url"));
		
		MemoryVariableUtil.ttsParamMap = ttsParamMap;
		
		
		//将配置中的自动外呼的变量加载到内存
		Map<String,String> autoCallTaskMap = new HashMap<String,String>();
		autoCallTaskMap.put("ac_scanInterval", getProperty("ac_scanInterval"));
		autoCallTaskMap.put("ac_scanCount", getProperty("ac_scanCount"));
		autoCallTaskMap.put("ac_maxLoadCount", getProperty("ac_maxLoadCount"));
		autoCallTaskMap.put("ac_maxConcurrentCount", getProperty("ac_maxConcurrentCount"));
		autoCallTaskMap.put("ac_timeout", getProperty("ac_timeout"));
		autoCallTaskMap.put("ac_channelPrefix", getProperty("ac_channelPrefix"));
		autoCallTaskMap.put("ac_agiUrl", getProperty("ac_agiUrl"));
		autoCallTaskMap.put("sheet_size", getProperty("sheet_size"));
		
		MemoryVariableUtil.autoCallTaskMap = autoCallTaskMap;
		
		//顺便将 Asterisk配置信息设置了一下
		AsteriskConfig.setAstHost(getProperty("asthost"));
		AsteriskConfig.setAstPort(Integer.valueOf(getProperty("astport")));
		AsteriskConfig.setAstUser(getProperty("astuser"));
		AsteriskConfig.setAstPassword(getProperty("astpass"));
		AsteriskConfig.setAstCallOutContext(getProperty("from-internal"));
		AsteriskConfig.setAstCallerId(getProperty("astcallerid"));
		AsteriskConfig.setAstHoldOnContext(getProperty("astholdoncontext"));
		AsteriskConfig.setAstPoolMinSize(Integer.valueOf(getProperty("astpoolminsize")));
		AsteriskConfig.setAstPoolMaxSize(Integer.valueOf(getProperty("astpoolmaxsize")));
		
		C3p0Plugin c3p0Plugin = new C3p0Plugin(getProperty("dburl"),getProperty("dbuser"),getProperty("dbpassword"));
		me.add(c3p0Plugin);
		
		ActiveRecordPlugin arp = new ActiveRecordPlugin(c3p0Plugin);
		me.add(arp);
		
		//表映射
		//系统管理表映射
		arp.addMapping("sys_role",Role.class);
		arp.addMapping("sys_role_group", RoleGroup.class);
		arp.addMapping("sys_org", Org.class);
		arp.addMapping("sys_operator",Operator.class);
		arp.addMapping("sys_oper_role",OperRole.class);
		arp.addMapping("sys_module",Module.class);
		arp.addMapping("sys_role_module",RoleModule.class);
		arp.addMapping("sys_login_log", LoginLog.class);
		arp.addMapping("sys_dict_item", DictItem.class);
		arp.addMapping("sys_dict_group", DictGroup.class);
		arp.addMapping("sys_operation_log", OperationLog.class);
		
		//外呼管理表映射
		arp.addMapping("call_task", CallTask.class);
		arp.addMapping("call_telephone", CallTelephone.class);
		arp.addMapping("callerloc", CallerLocation.class);
		
		//自动外呼管理数据表映射
		arp.addMapping("ac_call_task", AutoCallTask.class);
		arp.addMapping("ac_call_task_history", AutoCallTaskHistory.class);
		arp.addMapping("ac_call_task_telephone", AutoCallTaskTelephone.class);
		arp.addMapping("ac_call_task_telephone_history", AutoCallTaskTelephoneHistory.class);
		arp.addMapping("ac_schedule", Schedule.class);
		arp.addMapping("ac_voice",Voice.class);
		arp.addMapping("ac_questionnaire",Questionnaire.class);
		arp.addMapping("ac_questionnaire_respond",QuestionnaireRespond.class);
		arp.addMapping("ac_question",Question.class);
		arp.addMapping("ac_question_item",QuestionItem.class);
		arp.addMapping("ac_blacklist",AutoBlackList.class);
		arp.addMapping("ac_blacklist_telephone",AutoBlackListTelephone.class);
		arp.addMapping("ac_number",AutoNumber.class);
		arp.addMapping("ac_number_telephone",AutoNumberTelephone.class);
		
		//报表管理表映射
		arp.addMapping("cdr",Cdr.class);
		arp.addMapping("client_info", ClientInfo.class);
		arp.addMapping("client_touch_record", ClientTouchRecord.class);
		
		//话务功能
		arp.addMapping("sys_transfer", Transfer.class);
		arp.addMapping("sys_transfer_record", TransferRecord.class);
		arp.addMapping("sys_blacklist", BlackList.class);
		arp.addMapping("sys_blacklist_intercept_record", BlackListInterceptRecord.class);
		arp.addMapping("incoming", InComing.class);
		arp.addMapping("auto_contact", AutoContact.class);
		arp.addMapping("auto_contact_record",AutoContactRecord.class);
		
		//博世家电数据表
		arp.addMapping("bsh_orderlist", BSHOrderList.class);
		arp.addMapping("bsh_voice", BSHVoice.class);
		arp.addMapping("bsh_call_param", BSHCallParam.class);
		
		//在启动守护进程之前，先执行 Asterisk连接池初始化，以便在后期可以随时获取连接
		System.out.println("初始化Asterisk之前。。。");
		AsteriskUtils.connPool = AsteriskConnectionPool.newInstance() ;
		System.out.println("初始化Asterisk之后。。。");
		//--------以下为自动执行的守护进程------------
		
		//一、用于启动事件监控线程，用于监控来电信息，用于前端弹屏
		/*AstMonitor amt = new AstMonitor();
		Thread monitorThread = new Thread(amt); 
		monitorThread.start();*/

		
		//三、用于启动时,定时获取座席的状态,主要是根据 core show hints 命令返回的信息
		/*Thread agentStateMonitor = new Thread(new AgentStateMonitor());
		agentStateMonitor.start();*/
		
		//四、用于启动自动外呼任务扫描,并执行自动外呼操作
		/*Predial predial = new Predial();
		predial.execDial();*/
		
		//五、启动博世电器的自动外呼扫描，并执行自动外呼操作
		/*BSHPredial bshPredial = new BSHPredial();
		bshPredial.exec();*/
		BSHPredial bshPredial = new BSHPredial();
		try {
			bshPredial.exec();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void configRoute(Routes me) {
		me.add("/",CommonController.class);
		me.add("/systemResource",SystemResourceController.class);
		//添加路由组，即是 controller组
		me.add(new SystemRoute());
		me.add(new CallRoute());
		me.add(new ReportRoute());
		me.add(new FastagiRoute());
		me.add(new AutoCallRoute());
		me.add(new BSHRoute());
	}

}
























