package com.callke8.call.calltask;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.astutils.CallTaskCounterUtils;
import com.callke8.astutils.PhoneNumberHandlerUtils;
import com.callke8.call.calltelephone.CallTelephone;
import com.callke8.common.IController;
import com.callke8.report.clientinfo.ClientInfo;
import com.callke8.report.clienttouch.ClientTouchRecord;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class TaskExecuteController extends Controller implements IController{
	
	public void index() {
		render("list.jsp");
	}
	
	public void datagrid() {
		
		String operId = getSession().getAttribute("currOperId").toString();
		
		String taskName = getPara("taskName");
		String taskType = getPara("taskType");
		String taskState = "1";              //状态一定是为已经启动状态
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		
		renderJson(CallTask.dao.getCallTaskByPaginateToMap4Auth(page, rows, taskName, taskType, taskState, startTime, endTime,operId));
	}
	
	/**
	 * 弹屏或是点外呼时，在弹屏的左侧显示的客户属性在这里处理
	 */
	public void propertygrid() {
		
		String telephone = getPara("telephone");    //得到参数号码
		String telId = getPara("telId");            //得到当前号码所在任务的id, 对于来电弹屏时，telId 为空
		
		Record clientInfo = ClientInfo.dao.getClientInfoByTelephone(telephone);    //先根据号码，将客户的信息查询出来
		
		if(BlankUtils.isBlank(clientInfo)) {        //如果得到的客户为空时，则需要先将当前客户信息插入客户信息表
			//先组织一个clientInfo,组织之前，先判断telId是否为空，如果不为空时，则组织clientInfo就以 call_telephone表为主
			Record clientInfo4Save = new Record();
			clientInfo4Save.set("LOCATION", PhoneNumberHandlerUtils.getLocation(telephone));    //添加归属地
			if(!BlankUtils.isBlank(telId)) {
				Record callTelephone = CallTelephone.dao.getCallTelephoneById(telId);
			
				clientInfo4Save.set("CLIENT_TELEPHONE", telephone);
				if(!BlankUtils.isBlank(callTelephone)) {       //如果号码信息为空时
					clientInfo4Save.set("CLIENT_NAME", callTelephone.get("CLIENT_NAME"));
					clientInfo4Save.set("CLIENT_SEX", callTelephone.get("CLIENT_SEX")==null?"1":callTelephone.get("CLIENT_SEX").toString());
				}
			}else {
				
				clientInfo4Save.set("CLIENT_TELEPHONE", telephone);
				
				clientInfo4Save.set("CLIENT_NAME","先生");     //先默认为先生
				clientInfo4Save.set("CLIENT_SEX","1");         //先默认为男
				clientInfo4Save.set("CLIENT_LEVEL","1");       //默认级别为A级用户
			}
			
			boolean b = ClientInfo.dao.add(clientInfo4Save);    //先保存客户信息
			
			if(b) {
				clientInfo = clientInfo4Save;
			}
		}
		
		if(BlankUtils.isBlank(clientInfo.get("LOCATION"))) {    //在组织 propertyGrid 之前，先判断归属地是否为空，如果为空时，先加入归属地
			clientInfo.set("LOCATION", PhoneNumberHandlerUtils.getLocation(telephone));    //添加归属地
		}
		
		Map m = assemblePropertyGrid(clientInfo);
		
		//System.out.println("-------------------------" + m);
		
		renderJson(m);
	}
	
	/**
	 * 根据客户的信息，组织一个 propertyGrid，并以 Map 返回
	 * @param clientInfo
	 * @return
	 */
	public Map assemblePropertyGrid(Record record) {
		Map m = new HashMap();
		if(BlankUtils.isBlank(record)) {
			return null;
		}
		
		int clientNo = Integer.valueOf(record.get("CLIENT_NO").toString());    //客户编号
		String clientTelephone = record.get("CLIENT_TELEPHONE");
		String clientTelephone2 = record.get("CLIENT_TELEPHONE2");
		String clientName = record.get("CLIENT_NAME");
		String clientLevel = record.get("CLIENT_LEVEL");
		String clientSex = record.get("CLIENT_SEX");
		String clientEmail = record.get("CLIENT_EMAIL");
		String clientCompany = record.get("CLIENT_COMPANY");
		String clientQQ = record.get("CLIENT_QQ");
		String clientAddress = record.get("CLIENT_ADDRESS");
		String location = record.get("LOCATION");
		String createTime = record.get("CREATE_TIME")==null?"":record.get("CREATE_TIME").toString();
		
		m.put("total", "12");
		
		List<Record> list = new ArrayList<Record>();
		
		list.add(createRecord4PropertyGrid("客户编号",clientNo,"clientInfo","disabled:true"));
		list.add(createRecord4PropertyGrid("客户号码",clientTelephone,"clientInfo","disabled:true"));
		list.add(createRecord4PropertyGrid("备用号码",clientTelephone2,"clientInfo","numberbox"));
		list.add(createRecord4PropertyGrid("客户名称",clientName,"clientInfo","text"));
		list.add(createLevelRecord(clientLevel,"clientInfo"));
		list.add(createSexRecord(clientSex,"clientInfo"));
		list.add(createRecord4PropertyGrid("归属地",location,"clientInfo","text"));
		list.add(createRecord4PropertyGrid("客户QQ",clientQQ,"clientInfo","numberbox"));
		list.add(createRecord4PropertyGrid("电子邮箱",clientEmail,"clientInfo","text"));
		list.add(createRecord4PropertyGrid("公司信息",clientCompany,"clientInfo","text"));
		list.add(createRecord4PropertyGrid("地址信息",clientAddress,"clientInfo","text"));
		list.add(createRecord4PropertyGrid("添加时间",createTime,"clientInfo","disabled:true"));
		
		
		m.put("rows",list);
		
		return m;
	}
	
	/**
	 * 创建record 
	 * 
	 * @param name
	 * @param value
	 * @param group
	 * @param edit
	 * @return
	 */
	public Record createRecord4PropertyGrid(String name,Object value,String group,String edit) {
		Record record = new Record();
		
		record.set("name", name);
		if(BlankUtils.isBlank(value)) {
			record.set("value", "");
		}else {
			record.set("value", value);
		}
		record.set("group", group);
		record.set("editor",edit);
		return record;
	}
	
	public Record createSexRecord(String clientSex,String group) {
		
		Record sexRecord = new Record();
		sexRecord.set("name", "客户性别");
		sexRecord.set("value",clientSex);
		sexRecord.set("group", group);
		
		List<Record> list = new ArrayList<Record>();
		
		Record r1 = new Record();
		r1.set("value", "1");
		r1.set("text", "男");
		list.add(r1);
		Record r2 = new Record();
		r2.set("value", "0");
		r2.set("text", "女");
		list.add(r2);
		
		
		Record r3 = new Record();
		r3.set("data", list);
		
		Record r4 = new Record();
		r4.set("type", "combobox");
		r4.set("options",r3);
		
		
		sexRecord.set("editor",r4);
		
		return sexRecord;
	}
	
	
	public Record createLevelRecord(String clientLevel,String group) {
		Record levelRecord = new Record();
		levelRecord.set("name", "客户级别");
		levelRecord.set("value",clientLevel);
		levelRecord.set("group", group);
		
		List<Record> list = new ArrayList<Record>();
		
		//先从内存中取出数据字典数据，groupCode为 CLIENT_LEVEL的数据
		List<Record> clientLevelList = MemoryVariableUtil.dictMap.get("CLIENT_LEVEL");
		
		for(Record record:clientLevelList) {
			
			Record r = new Record();
			
			r.set("value",record.get("DICT_CODE").toString());
			r.set("text",record.get("DICT_NAME").toString());
			
			list.add(r);
		}
		
		Record record2 = new Record();
		record2.set("data", list);
		
		Record record3 = new Record();
		record3.set("type", "combobox");
		record3.set("options",record2);
		
		levelRecord.set("editor",record3);
		
		return levelRecord;
	}
	
	/**
	 * 
	 * 显示被当前工号请求的数据
	 * 执行外呼号码列表
	 */
	public void telephoneDatagrid() {
		
		String operId = getSession().getAttribute("currOperId").toString();
		
		String taskId = getPara("taskId");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		
		if(page<=0) {page=1;}
		
		//查询已经分配的号码
		renderJson(CallTelephone.dao.getCallTelephoneByPaginateToMap4Auth(page, rows, taskId, null, null, "1", null, null,operId));
	}
	
	/**
	 * 请求外呼数据
	 */
	public void reqCallDatas() {
		
		String operId = getSession().getAttribute("currOperId").toString();
		int taskId = Integer.valueOf(getPara("taskId"));    
		int reqCount = Integer.valueOf(getPara("reqCount"));    //当次请求数量
		
		//先判断当前是否已经登录，如果未登录时
		if(BlankUtils.isBlank(operId)) {          
			render(RenderJson.error("登录工号已失效，无法请求数据，请重新登录工号!"));
			return;
		}
		
		//再判断当前任务的状态是否处于开启状态
		String taskState = CallTask.dao.getTaskStateByTaskId(taskId);
		if(BlankUtils.isBlank(taskState)){
			render(RenderJson.error("当前任务的状态不正确!"));
			return;
		}
		if(!taskState.equalsIgnoreCase("1")) {   //如果状态不为1,即非启动状态时
			if(taskState.equalsIgnoreCase("2")) {
				render(RenderJson.error("请求数据失败，当前任务已经暂停!"));
				return;
			}
			if(taskState.equalsIgnoreCase("3")) {
				render(RenderJson.error("请求数据失败，当前任务已经停止!"));
				return;
			}
		}
		
		
		int activeCount = CallTelephone.dao.getCountByTaskIdState(taskId, "0");    //先查看当前任务，是否还有未分配的号码，如果没有则返回错误
		
		if(activeCount <= 0) {
			render(RenderJson.error("任务暂无可请求的数据，请联系管理员添加数据后再请求!"));
			return;
		}
		
		int count = CallTelephone.dao.reqCallData(taskId, operId, reqCount);
		
		if(count>0) {      //如果请求成功的数据量大于0时，要更改任务的已经分配的量
			CallTaskCounterUtils.reduceCounter(taskId, "0", count);      //减少新号码的数量
			CallTaskCounterUtils.increaseCounter(taskId, "1", count);    //增加已经分配的数量
		}
		
		render(RenderJson.success("成功请求数据 <span style='color:red;font-weight:bold;'>" + count + "</span> 条"));
	}
	
	public void touchRecordDatagrid() {
		
		String telephone = getPara("telephone");   //先得到参数：号码
		if(BlankUtils.isBlank(telephone)) {        //如果号码为空时，返回空的列表
			Map m = new HashMap();
			m.put("total",0);
			m.put("rows",new ArrayList<Record>());
			renderJson(m);
			return;
		}
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0) {
			page=1;
		}
		
		renderJson(ClientTouchRecord.dao.getClientTouchRecordByPaginateToMap(page, rows, telephone,null,null,null,null,null));
	}
	
	public void add() {
		
	}

	
	/**
	 * 保存呼入、呼出的接触结果
	 */
	public void addTouchRecord() {
		
		String telId = null;
		int taskId = 0;
		String callType = null;
		ClientInfo clientInfo = new ClientInfo();    //定义一个空的客户资料
		Record ctr = new Record();                   //定义一个空的客户接触对象
		
		
		try {
			
			//获取提交上来的客户信息
			String clientNo = getPara("clientNo");
			String clientTelephone = getPara("clientTelephone");
			String clientTelephone2 = getPara("clientTelephone2");
			String clientName = URLDecoder.decode(getPara("clientName").toString(),"UTF-8");
			String clientLevel = getPara("clientLevel");
			if(BlankUtils.isBlank(clientLevel)) { clientLevel="2";};
			String clientSex = getPara("clientSex");
			String location = URLDecoder.decode(getPara("location"),"UTF-8");
			String clientQq = getPara("clientQq");
			String clientEmail = URLDecoder.decode(getPara("clientEmail"),"UTF-8");
			String clientCompany = URLDecoder.decode(getPara("clientCompany"),"UTF-8");
			String clientAddress = URLDecoder.decode(getPara("clientAddress"),"UTF-8");
			String touchNote = URLDecoder.decode(getPara("touchNote"),"UTF-8");     //接触备注
			
			clientInfo.set("CLIENT_NO", clientNo);
			clientInfo.set("CLIENT_NAME",clientName);
			clientInfo.set("CLIENT_TELEPHONE",clientTelephone);
			clientInfo.set("CLIENT_TELEPHONE2", clientTelephone2);
			clientInfo.set("CLIENT_LEVEL", clientLevel);
			clientInfo.set("CLIENT_SEX",clientSex);
			clientInfo.set("LOCATION", location);
			clientInfo.set("CLIENT_QQ", clientQq);
			clientInfo.set("CLIENT_EMAIL",clientEmail);
			clientInfo.set("CLIENT_COMPANY", clientCompany);
			clientInfo.set("CLIENT_ADDRESS", clientAddress);
			
			ctr.set("CLIENT_NO", clientNo);                 //客户编号
			ctr.set("AGENT", getSession().getAttribute("currAgentNumber"));      //座席号
			ctr.set("CLIENT_TELEPHONE", clientTelephone);   //客户号码
			ctr.set("TOUCH_OPERATOR", getSession().getAttribute("currOperId"));  //当前操作ID
			ctr.set("TOUCH_TIME", DateFormatUtils.getCurrentDate());             //接触时间
			ctr.set("TOUCH_CHANNEL","");                    //通道名称
			ctr.set("VOICES_FILE", "");                     //录音文件
			ctr.set("TOUCH_NOTE", touchNote);
			
			//获取接触信息
			callType = getPara("callType");         //通话类型,1:呼入   2：呼出
			ctr.set("TOUCH_TYPE",callType);     //接触类型（1：呼入；2：呼出）
			if(!BlankUtils.isBlank(callType) && callType.equals("1")) {    //组织呼入的接触记录
				
				String callReason = getPara("callReason");   //来电原因，1：来电咨询; 2：来电投诉; 3：异常来电
				ctr.set("CALL_RESULT",callReason);           //来电原因，1：来电咨询; 2：来电投诉; 3：异常来电
				ctr.set("FINAL_RESULT", 0);                  //这个默认为0
				
				if(callReason.equals("1")) {         //来电咨询
					String attentionFocus = getPara("attentionFocus");      //关注热点
					String coginitionWay = getPara("coginitionWay");        //认知渠道
					String visitTime = getPara("visitTime");                //确认到访时间
					String sendMessage = getPara("sendMessage");            //是否下发短信
					
					String requireType = getPara("requireType");            //需求类型
					String requireHouseType = getPara("requireHouseType");  //需求户型
					String requireArea = getPara("requireArea");            //需求面积
					String intendPrice = getPara("intendPrice");            //意向价格
					String zyIntend = getPara("zyIntend");                  //置业意向
					String propertiesPurpose = getPara("propertiesPurpose");  //置业目的
					String requireLocation = getPara("requireLocation");    //需求区位
					String makingRoomTime = getPara("makingRoomTime");      //交房时间
					
					
					ctr.set("ATTENTION_FOCUS", attentionFocus);
					ctr.set("COGINITION_WAY", coginitionWay);
					ctr.set("VISIT_TIME", visitTime);
					ctr.set("SEND_MESSAGE",sendMessage);
					ctr.set("TOUCH_NOTE", touchNote);
					
					ctr.set("REQUIRE_TYPE", requireType);
					ctr.set("REQUIRE_HOUSETYPE", requireHouseType);
					ctr.set("REQUIRE_AREA", requireArea);
					ctr.set("INTEND_PRICE", intendPrice);
					ctr.set("ZYINTEND", zyIntend);
					ctr.set("PROPERTIES_PURPOSE", propertiesPurpose);
					ctr.set("REQUIRE_LOCATION", requireLocation);
					ctr.set("MAKINGROOM_TIME", makingRoomTime);
					
				}else if(callReason.equals("2")) {   //来电投诉
					
					String complaintItem = getPara("complaintItem");   //来电投诉项目
					
					ctr.set("COMPLAINT_ITEM", complaintItem);
					
				}else if(callReason.equals("3")) {   //异常来电
					String abnormalReason = getPara("abnormalReason");   //异常来电原因
					ctr.set("ABNORMAL_REASON", abnormalReason);
				}
				
				
			}else {														   //组织呼出的接触记录
				
				String touchResult = getPara("touchResult");     //外呼结果，1：接触失败; 2：接触成功-感兴趣; 3：接触成功-不感兴趣
				String recallTime = getPara("recallTime");
				telId = getPara("telId");
				taskId = Integer.valueOf(getPara("taskId"));
				
				ctr.set("CALL_RESULT", touchResult);
				ctr.set("RECALL_TIME", BlankUtils.isBlank(recallTime)?null:recallTime);    //对于时间的字段，一定要处理
				ctr.set("TASK_ID",taskId);
				//为了便于统计，需要对接触记录标记是否为最后的结果，主要是针对外呼的，只要重呼时间不为空，即只要不需要重呼的接触记录，都表示其是最后的结果
				if(BlankUtils.isBlank(recallTime)) {   //重呼时间为空，即不需要重呼
					ctr.set("FINAL_RESULT", 1);
				}else {
					ctr.set("FINAL_RESULT",0);
				}
				
				if(touchResult.equals("1")) {        		//接触失败
					
					String touchFailureReason = getPara("touchFailureReason");    //接触失败的原因
					
					ctr.set("TOUCH_FAILURE_REASON", touchFailureReason);
					
				}else if(touchResult.equals("2") || touchResult.equals("3")) {         //接触成功-感兴趣
					
					if(touchResult.equals("2")) {          //感兴趣                                     
						String sendMessage = getPara("sendMessage");         //是否发送信息
						String attentionFocus = getPara("attentionFocus");   //关注热点
						String visitTime = getPara("visitTime");             //确认到访时间
						
						ctr.set("SEND_MESSAGE", sendMessage);                
						ctr.set("ATTENTION_FOCUS", attentionFocus);
						ctr.set("VISIT_TIME", BlankUtils.isBlank(visitTime)?null:visitTime);    //对于时间的字段，一定要处理
						
					}else if(touchResult.equals("3")) {    //不感兴趣
						String uninsterestingReason = getPara("uninsterestingReason");    //不感兴趣的原因
						
						ctr.set("UNINSTERESTING_REASON", uninsterestingReason);
					}
					
					String requireType = getPara("requireType");            //需求类型
					String requireHouseType = getPara("requireHouseType");  //需求户型
					String requireArea = getPara("requireArea");            //需求面积
					String intendPrice = getPara("intendPrice");            //意向价格
					String zyIntend = getPara("zyIntend");                  //置业意向
					String propertiesPurpose = getPara("propertiesPurpose");  //置业目的
					String requireLocation = getPara("requireLocation");    //需求区位
					String makingRoomTime = getPara("makingRoomTime");      //交房时间
					
					ctr.set("REQUIRE_TYPE", requireType);
					ctr.set("REQUIRE_HOUSETYPE", requireHouseType);
					ctr.set("REQUIRE_AREA", requireArea);
					ctr.set("INTEND_PRICE", intendPrice);
					ctr.set("ZYINTEND", zyIntend);
					ctr.set("PROPERTIES_PURPOSE", propertiesPurpose);
					ctr.set("REQUIRE_LOCATION", requireLocation);
					ctr.set("MAKINGROOM_TIME", makingRoomTime);
				}
				
			}
			
		
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		//保存客户资料、客户接触信息
		boolean b1 = ClientTouchRecord.dao.add(ctr);
		if(!b1) {
			render(RenderJson.error("客户接触信息插入失败"));
			return ;
		}
		
		boolean b2 = ClientInfo.dao.update(clientInfo);  //修改客户信息
		
		if(!b2) {
			render(RenderJson.error("客户信息修改失败!"));
			return;
		}
		
		//如果 telId 不为空时，即是呼出的记录，需要将当前任务下的这个号码记录的状态更改及增加接触时间
		//同时还需要根据保存接触记录的同时，将调整当前任务各状态的数量
		if(!BlankUtils.isBlank(telId) && callType.equals("2")) {     
			
			//在更改其状态前，先根据 telId 取出号码对象，然后根据原号码状态进行调整任务各状态的数量
			Record tel = CallTelephone.dao.getCallTelephoneById(telId);
			String oldCallState = tel.get("STATE");   //取出当前号码的原状态是
			
			CallTelephone telephone = new CallTelephone();
			telephone.set("TEL_ID",telId);
			telephone.set("CLIENT_NAME",clientInfo.get("CLIENT_NAME"));         //客户姓名
			telephone.set("CLIENT_SEX",clientInfo.get("CLIENT_SEX"));           //客户性别
			telephone.set("OP_TIME",DateFormatUtils.getCurrentDate());          //上一次的操作时间
			telephone.set("OPER_ID",getSession().getAttribute("currOperId"));   //操作员
			
			String callResult = ctr.get("CALL_RESULT");      //先取出外呼结果
			String recallTime = ctr.get("RECALL_TIME");       //重呼时间
			
			telephone.set("NEXT_CALLOUT_TIME", BlankUtils.isBlank(recallTime)?null:recallTime);
			if(callResult.equals("1")) {         //呼叫失败
				if(BlankUtils.isBlank(recallTime)) {   //如果重呼时间为空，需要将外呼状态修改为2, 即为接触失败
					telephone.set("STATE","2");
				}else {                               //如果重呼时间不为空时，需要将外呼状态修改为5,即为接触失败再外呼
					telephone.set("STATE","5");       
				}
			}else if(callResult.equals("2")) {   //呼叫成功-感兴趣
				if(BlankUtils.isBlank(recallTime)) {   //如果重呼时间为空，需要将外呼状态修改为3, 即为感兴趣
					telephone.set("STATE","3");
				}else {                               //如果重呼时间不为空时，需要将外呼状态修改为6,即为感兴趣再外呼
					telephone.set("STATE","6");       
				}
				
			}else if(callResult.equals("3")) {   //呼叫成功-不感兴趣
				if(BlankUtils.isBlank(recallTime)) {   //如果重呼时间为空，需要将外呼状态修改为 4, 即为不感兴趣
					telephone.set("STATE","4");
				}else {                               //如果重呼时间不为空时，需要将外呼状态修改为7,即为不感兴趣再外呼
					telephone.set("STATE","7");       
				}
			}
			
			
			boolean b3 = CallTelephone.dao.updateCallTelephone(telephone);
			if(!b3) {
				render(RenderJson.error("提交失败!"));
				return ;
			}else {
				//更改任务的各状态情况
				updateCallTaskCounter(taskId, oldCallState, callResult, recallTime);
			}
			
		}
		
		render(RenderJson.success("提交成功!"));
	}
	
	/**
	 * 更改任务的计数情况
	 * 
	 * @param taskId
	 * 			任务ID
	 * @param oldCallState
	 * 			telId 的记录原状态, 0：新建；1：已分配;2:呼叫失败；3：感兴趣; 4: 不感兴趣; 5:接触失败再外呼； 6：感兴趣再外呼; 7：不感兴趣再外呼
	 * @param callResult
	 * 			外呼结果：1, 接触失败；2接触成功-感兴趣；3接触成功-不感兴趣
	 * @param recallTime
	 */
	public void updateCallTaskCounter(int taskId,String oldCallState,String callResult,String recallTime) {
		
		int oldCallState2Int = Integer.valueOf(oldCallState);   //先将原状态转为 int 类型
		int callResult2Int = Integer.valueOf(callResult);           //外呼结果，转为 int 类型
		
		if(oldCallState2Int>=5) {     //如果该号码原来的状态大于5，即是再外呼类型 
			
			//如果原来该号码就是处于再次外呼的状态的
			if(!BlankUtils.isBlank(recallTime)) {               //如果重呼时间不为空，表示需要再次外呼   
				
				if(callResult2Int==1) {    //外呼结果为1，即外呼结果为失败再外呼
					
					if(oldCallState2Int==5) {    //如果原来的状态即为5，即失败再外呼的，就无须增加或是减少计数
						
					}else if(oldCallState2Int==6) {      //如果原来的状态为 6, 即是感兴趣再次外呼的，就需要先将状态为6的数减少1，然后再在状态为 5 的增加1
						CallTaskCounterUtils.reduceCounter(taskId, "6", 1);
						CallTaskCounterUtils.increaseCounter(taskId, "5", 1);
					}else if(oldCallState2Int==7) {      //如果原来的状态为7，即不感兴趣再外呼的，需要先将7的数减1，然后再在状态为 5 的增加1
						CallTaskCounterUtils.reduceCounter(taskId, "7", 1);
						CallTaskCounterUtils.increaseCounter(taskId, "5", 1);
					}
				}else if(callResult2Int==2) {   //外呼结果为2, 即是感兴趣再次外呼
					
					if(oldCallState2Int==5) {   //如果原状态为 5,
						CallTaskCounterUtils.reduceCounter(taskId, "5", 1);
						CallTaskCounterUtils.increaseCounter(taskId, "6", 1);
					}else if(oldCallState2Int==6) {   //如果原状态为 6，不做任何的计数更改
						
					}else if(oldCallState2Int==7) {   //如果原状态为 7
						CallTaskCounterUtils.reduceCounter(taskId, "7", 1);
						CallTaskCounterUtils.increaseCounter(taskId, "6", 1);
					}
					
				}else if(callResult2Int==3) {   //外呼结果为 3, 即是不感兴趣再次外呼的
					
					if(oldCallState2Int==5) {   //如果原状态为 5,
						CallTaskCounterUtils.reduceCounter(taskId, "5", 1);
						CallTaskCounterUtils.increaseCounter(taskId, "7", 1);
					}else if(oldCallState2Int==6) {   //如果原状态为 6
						CallTaskCounterUtils.reduceCounter(taskId, "6", 1);
						CallTaskCounterUtils.increaseCounter(taskId, "7", 1);
					}else if(oldCallState2Int==7) {   //如果原状态为 7, 不做任何的计数更改
					}
					
				}
				
			} else {                                            //如果重呼时间为空，则无须再次外呼的
				
				if(oldCallState2Int==5) {   //如果原状态为 5,
					CallTaskCounterUtils.reduceCounter(taskId, "5", 1);
				}else if(oldCallState2Int==6) {   //如果原状态为 6
					CallTaskCounterUtils.reduceCounter(taskId, "6", 1);
				}else if(oldCallState2Int==7) {   //如果原状态为 7, 
					CallTaskCounterUtils.reduceCounter(taskId, "7", 1);
				}
				
				if(callResult2Int==1) {       //如果外呼结果1，即是接触失败时
					CallTaskCounterUtils.increaseCounter(taskId, "2", 1);    //增加接触失败的记录
				}else if(callResult2Int==2) {    //如果外呼结果为感兴趣时
					CallTaskCounterUtils.increaseCounter(taskId, "3", 1);    //增加感兴趣的数量
				}else if(callResult2Int==3) {    //如果外呼结果为不感兴趣时
					CallTaskCounterUtils.increaseCounter(taskId, "3", 1);    //增加感兴趣的数量
				}
			}
			
		}else if(oldCallState2Int==1) {    //如果该号码原来的状态为 1, 即是新分配的
			
			if(!BlankUtils.isBlank(recallTime)) {    //如果重呼时间不为空时，表示是需要重呼的
				
				if(callResult2Int==1) {    //如果为失败重外呼时，需要增加一个状态为5的状态数，即接触失败再次外呼
					CallTaskCounterUtils.increaseCounter(taskId, "5", 1);
				}else if(callResult2Int==2) {   //如果为感兴趣再外呼，需要增加一个状态为6状态数，即是接触成功-感兴趣再次外呼
					CallTaskCounterUtils.increaseCounter(taskId, "6", 1);
				}else if(callResult2Int==3) {   //如果为不感兴趣再外呼，需要增加一个状态为7状态数，即是接触成功-不感兴趣再次外呼
					CallTaskCounterUtils.increaseCounter(taskId, "7", 1);
				}
				
			}else {                                 //如果重呼时间为空时，则表示无须再次外呼，只有两种结果，一种是接触失败，一种是接触成功，无论其是否感兴趣
				if(callResult2Int==1) {   //如果接触失败，增加一个接触失败的计数
					CallTaskCounterUtils.increaseCounter(taskId, "2", 1);
				}else if(callResult2Int==2) {   //如果为感兴趣时
					CallTaskCounterUtils.increaseCounter(taskId, "3", 1);
				}else if(callResult2Int==3) {   //如果为不感兴趣时
					CallTaskCounterUtils.increaseCounter(taskId, "4", 1);
				}
			}
			
		}
		
	}
	
	public void updateClientInfo() {
		try {
			//客户信息
			String clientNo = getPara("clientNo");
			String clientTelephone = getPara("clientTelephone");
			String clientTelephone2 = getPara("clientTelephone2");
			String clientName = URLDecoder.decode(getPara("clientName").toString(),"UTF-8");
			String clientLevel = getPara("clientLevel");
			if(BlankUtils.isBlank(clientLevel)) { clientLevel="2";};
			String clientSex = getPara("clientSex");
			String location = URLDecoder.decode(getPara("location"),"UTF-8");
			String clientQq = getPara("clientQq");
			String clientEmail = URLDecoder.decode(getPara("clientEmail"),"UTF-8");
			String clientCompany = URLDecoder.decode(getPara("clientCompany"),"UTF-8");
			String clientAddress = URLDecoder.decode(getPara("clientAddress"),"UTF-8");
			
			ClientInfo clientInfo = new ClientInfo();
			clientInfo.set("CLIENT_NO", clientNo);
			clientInfo.set("CLIENT_NAME",clientName);
			clientInfo.set("CLIENT_TELEPHONE",clientTelephone);
			clientInfo.set("CLIENT_TELEPHONE2", clientTelephone2);
			clientInfo.set("CLIENT_LEVEL", clientLevel);
			clientInfo.set("CLIENT_SEX",clientSex);
			clientInfo.set("LOCATION",location);
			clientInfo.set("CLIENT_QQ", clientQq);
			clientInfo.set("CLIENT_EMAIL",clientEmail);
			clientInfo.set("CLIENT_COMPANY", clientCompany);
			clientInfo.set("CLIENT_ADDRESS", clientAddress);
			
			boolean b = ClientInfo.dao.update(clientInfo);  //修改客户信息
			
			if(!b) {
				render(RenderJson.error("客户信息修改失败!"));
				return;
			}
			
			render(RenderJson.success("提交成功!"));
		
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}
	
}





















