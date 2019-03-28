package com.callke8.common;

import org.quartz.SchedulerException;

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
import com.callke8.autocall.flow.AutoFlow;
import com.callke8.autocall.flow.AutoFlowDetail;
import com.callke8.autocall.questionnaire.Question;
import com.callke8.autocall.questionnaire.QuestionItem;
import com.callke8.autocall.questionnaire.Questionnaire;
import com.callke8.autocall.questionnaire.QuestionnaireRespond;
import com.callke8.autocall.schedule.Schedule;
import com.callke8.autocall.voice.Voice;
import com.callke8.bsh.bshorderlist.BSHOrderList;
import com.callke8.bsh.bshvoice.BSHVoice;
import com.callke8.bsh.common.BSHRoute;
import com.callke8.call.calltask.CallTask;
import com.callke8.call.calltelephone.CallTelephone;
import com.callke8.call.calltelephone.CallerLocation;
import com.callke8.call.common.CallRoute;
import com.callke8.call.incoming.InComing;
import com.callke8.cnn.cnncallindata.CnnCallinData;
import com.callke8.cnn.cnndata.CnnData;
import com.callke8.cnn.cnnvoice.CnnVoice;
import com.callke8.cnn.common.CnnRoute;
import com.callke8.fastagi.blacklist.BlackList;
import com.callke8.fastagi.blacklist.BlackListInterceptRecord;
import com.callke8.fastagi.common.FastagiRoute;
import com.callke8.fastagi.transfer.Transfer;
import com.callke8.fastagi.transfer.TransferRecord;
import com.callke8.predialqueueforautocallbyquartz.AutoCallPredial;
import com.callke8.report.cdr.Cdr;
import com.callke8.report.clientinfo.ClientInfo;
import com.callke8.report.clienttouch.ClientTouchRecord;
import com.callke8.report.common.ReportRoute;
import com.callke8.system.callerid.SysCallerId;
import com.callke8.system.callerid.SysCallerIdController;
import com.callke8.system.calleridassign.SysCallerIdAssign;
import com.callke8.system.calleridgroup.SysCallerIdGroup;
import com.callke8.system.calleridgroup.SysCallerIdGroupAssign;
import com.callke8.system.common.SystemRoute;
import com.callke8.system.dict.DictGroup;
import com.callke8.system.dict.DictItem;
import com.callke8.system.ipaddress.SysIpAddress;
import com.callke8.system.loginlog.LoginLog;
import com.callke8.system.module.Module;
import com.callke8.system.operationlog.OperationLog;
import com.callke8.system.operator.OperRole;
import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.system.param.Param;
import com.callke8.system.remindertype.SysReminderType;
import com.callke8.system.remindertypeassign.SysReminderTypeAssign;
import com.callke8.system.role.Role;
import com.callke8.system.rolegroup.RoleGroup;
import com.callke8.system.rolemodule.RoleModule;
import com.callke8.system.schedule.SysSchedule;
import com.callke8.system.scheduleassign.SysScheduleAssign;
import com.callke8.system.tasktype.SysTaskType;
import com.callke8.system.tasktypeassign.SysTaskTypeAssign;
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
		
		
	}

	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new CommonInterceptor());
		
		System.out.println("CommonConfig.configInterceptor()");
		//从数据表加载数据到内存
		loadDataIntoMemory();
		
		//初始化连接池
		initAstConnectionPool();
		
		//启动应用(守护程序)
		startApplications();
		
	}

	@Override
	public void configPlugin(Plugins me) {
		loadPropertyFile("commonconfig.properties");
		
		//顺便将 Asterisk配置信息设置了一下
		AsteriskConfig.setAstHost(getProperty("asthost"));
		AsteriskConfig.setAstPort(Integer.valueOf(getProperty("astport")));
		AsteriskConfig.setAstUser(getProperty("astuser"));
		AsteriskConfig.setAstPassword(getProperty("astpass"));
		
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
		arp.addMapping("sys_param", Param.class);
		arp.addMapping("sys_callerid", SysCallerId.class);
		arp.addMapping("sys_callerid_assign", SysCallerIdAssign.class);
		arp.addMapping("sys_callerid_group", SysCallerIdGroup.class);
		arp.addMapping("sys_callerid_group_assign", SysCallerIdGroupAssign.class);
		arp.addMapping("sys_task_type", SysTaskType.class);
		arp.addMapping("sys_task_type_assign", SysTaskTypeAssign.class);
		arp.addMapping("sys_reminder_type", SysReminderType.class);
		arp.addMapping("sys_reminder_type_assign", SysReminderTypeAssign.class);
		arp.addMapping("sys_schedule", SysSchedule.class);
		arp.addMapping("sys_schedule_assign", SysScheduleAssign.class);
		arp.addMapping("sys_ip_address", SysIpAddress.class);
		
		
		
		//外呼管理表映射
		arp.addMapping("call_task", CallTask.class);
		arp.addMapping("call_telephone", CallTelephone.class);
		arp.addMapping("callerloc", CallerLocation.class);
		
		//自动外呼管理数据表映射
		arp.addMapping("ac_call_task", AutoCallTask.class);
		arp.addMapping("ac_call_task_history", AutoCallTaskHistory.class);
		arp.addMapping("ac_call_task_telephone", AutoCallTaskTelephone.class);
		arp.addMapping("ac_call_task_telephone_history", AutoCallTaskTelephoneHistory.class);
		//arp.addMapping("ac_schedule", Schedule.class);
		arp.addMapping("ac_voice",Voice.class);
		arp.addMapping("ac_questionnaire",Questionnaire.class);
		arp.addMapping("ac_questionnaire_respond",QuestionnaireRespond.class);
		arp.addMapping("ac_question",Question.class);
		arp.addMapping("ac_question_item",QuestionItem.class);
		arp.addMapping("ac_blacklist",AutoBlackList.class);
		arp.addMapping("ac_blacklist_telephone",AutoBlackListTelephone.class);
		arp.addMapping("ac_number",AutoNumber.class);
		arp.addMapping("ac_number_telephone",AutoNumberTelephone.class);
		arp.addMapping("ac_flow", AutoFlow.class);
		arp.addMapping("ac_flow_detail", AutoFlowDetail.class);
		
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
		
		//博世家电数据表
		arp.addMapping("bsh_orderlist", BSHOrderList.class);
		arp.addMapping("bsh_voice", BSHVoice.class);
		
		//改号通知数据表
		arp.addMapping("cnn_data", CnnData.class);
		arp.addMapping("cnn_callin_data", CnnCallinData.class);
		arp.addMapping("cnn_voice", CnnVoice.class);
		
	}
	
	/**
	 * 从数据表中加载数据到内存
	 * 
	 * 在后期系统需要使用相关表数据时，可以直接取出，无需再从数据表中查询，增加系统的效率。
	 * 
	 */
	public void loadDataIntoMemory() {
		
		
		System.out.println("-----------从数据表加载数据到内存!------------");
		//(1)执行数据字典数据初始化到内存变量!
		if(BlankUtils.isBlank(MemoryVariableUtil.dictMap)) {    //如果为空时，则将数据字典的内容写入内存变量
			MemoryVariableUtil.dictMap = DictGroup.dao.loadDictInfo();
			System.out.println("(1)执行数据字典数据初始化到内存变量!");
		}
		
		//(2)执行菜单数据初始化到内存变量!
		if(BlankUtils.isBlank(MemoryVariableUtil.moduleMap)) {
			MemoryVariableUtil.moduleMap = Module.dao.loadModuleInfo();
			System.out.println("(2)执行菜单数据初始化到内存变量!");
		}
		
		//(3)执行操作员数据初始化到内存变量!
		if(BlankUtils.isBlank(MemoryVariableUtil.operatorMap)) {
			MemoryVariableUtil.operatorMap = Operator.dao.loadOperatorInfo();
			System.out.println("(3)执行操作员数据初始化到内存变量!");
		}
		
		//(4)准备加载系统配置参数到内存!
		System.out.println("(4)准备加载系统配置参数到内存!");
		Param.dao.loadParamDataToMemory();
		
		//(5)准备加载系统的主叫号码到内存
		System.out.println("(5)准备加载系统主叫号码参数到内存!");
		SysCallerId.dao.loadSysCallerIdToMemory();
		
		//(6)准备加载系统配置的IP地址到内存
		System.out.println("(5)准备加载IP地址访问控制参数到内存!");
		SysIpAddress.dao.loadSysIpAddressToMemory();
	}
	
	/**
	 * 初始化 Asterisk 连接池
	 */
	public void initAstConnectionPool() {
		System.out.println("初始化Asterisk之前。。。");
		AsteriskUtils.connPool = AsteriskConnectionPool.newInstance() ;
		System.out.println("初始化Asterisk之后。。。");
	}
	
	/**
	 * 启动相关应用
	 */
	public void startApplications() {
		//一、用于启动事件监控线程，用于监控来电信息，用于前端弹屏
		/*AstMonitor amt = new AstMonitor();
		Thread monitorThread = new Thread(amt); 
		monitorThread.start();*/

		
		//三、用于启动时,定时获取座席的状态,主要是根据 core show hints 命令返回的信息
		/*Thread agentStateMonitor = new Thread(new AgentStateMonitor());
		agentStateMonitor.start();*/
		
		//四、用于启动自动外呼任务扫描,并执行自动外呼操作
		
		//AutoCallPredial autoCallPredial = new AutoCallPredial();
		//try {  autoCallPredial.exec();   } catch (SchedulerException e) { e.printStackTrace();}
		
		//五、启动博世电器的自动外呼扫描，并执行自动外呼操作
		/*System.out.println("启动博世电器守护程序前，先加载博世电器的语音数据到内存!");
		BSHVoice.dao.loadBSHVoiceDataToMemory();      //执行加载数据到内存
		System.out.println(new BSHVoiceConfig());
		
		BSHPredial bshPredial = new BSHPredial();     //创建博世家电守护程序并准备启动守护程序
		try {
			bshPredial.exec();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}*/
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
		me.add(new CnnRoute());
	}

}
























