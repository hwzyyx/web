package com.callke8.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;

import com.callke8.astutils.AstMonitor;
import com.callke8.astutils.CtiUtils;
import com.callke8.call.incoming.InComing;
import com.callke8.system.loginlog.LoginLog;
import com.callke8.system.module.ModuleController;
import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.ComboboxJson;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.HttpRequestUtils;
import com.callke8.utils.Md5Utils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.RenderJson;
import com.callke8.utils.TreeJson;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

@ClearInterceptor
public class CommonController extends Controller {
	
	public void index() {
		if(BlankUtils.isBlank(getSession().getAttribute("currOperId"))) {
			//render("login.jsp");
			redirect("/login");
		}else {
			
			setAttr("menuAccordionData",ModuleController.getMenuToString(getSession().getAttribute("currOperId").toString()));
			render("index.jsp");
		}
	}
	
	
	/**
	 * 扫描当前座席的来电信息，用于弹屏
	 */
	public void scan() {
		String agentNumber = null;    //定义一个座席号码
		
		
		if(!BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))){
			agentNumber = getSession().getAttribute("currAgentNumber").toString();
		}
		//如果当前登录账号未关联座席号码时，则返回空值
		if(BlankUtils.isBlank(agentNumber)) {    
			render(RenderJson.error(""));
			return;
		}
		
		System.out.println("座席号码:" + getSession().getAttribute("currAgentNumber") + " 准备弹屏扫描...");
		//通过 RabbitmqUtils 获取消息，并进行弹屏
		InComing inComing = AstMonitor.getInComingByAgent(agentNumber);  
		if(BlankUtils.isBlank(inComing)) {     //如果返回的弹屏结果为空，则返回错误
			System.out.println("返回弹屏扫描的消息为空...");
			render(RenderJson.error(""));
		} else {
			String client = inComing.getStr("CLIENT");
			String channel = inComing.getStr("CHANNEL");
			
			System.out.println("返回可用弹屏消息，客户号码：" + client + ",座席号码:" + agentNumber + ",通道名称：" + channel);
			
			render(RenderJson.success(client));
			
		}
		
	}
	
	public void login() {
		render("login.jsp");
	}
	
	public void doLogin() {
		
		String operId = getPara("operId");
		String password = getPara("password");
		String callNumber = getPara("callNumber");
		
		//
		if(!BlankUtils.isBlank(operId)&& !BlankUtils.isBlank(password)) {
			//先根据传入的 operId，将操作员信息查询出来
			Operator operator = Operator.dao.getOperatorByOperId(operId);
			
			if(BlankUtils.isBlank(operator)) {    //先判断查询结果是否为空
				render(RenderJson.error("工号不存在!"));
				return;
			}
			
			//先将 password 先进行 md5 加密
			String password2Md5 = Md5Utils.Md5(password);
			
			//判断密码是否相等
			if(!password2Md5.equalsIgnoreCase(operator.get("PASSWORD").toString())) {
				render(RenderJson.error("用户名或密码不正确!"));
			}else {
				//登录正确
				getSession().setAttribute("currOperId",operId);
				getSession().setAttribute("currAgentNumber",callNumber);
				getSession().setAttribute("currOrgCode", operator.get("ORG_CODE").toString());   //当前登录用户所在的组织
				getSession().setAttribute("currOperName",operator.get("OPER_NAME").toString());
				
				//登录成功时，写入登录日志
				Record loginLog = new Record();
				loginLog.set("OPER_ID", operId);
				loginLog.set("ORG_CODE",operator.get("ORG_CODE").toString());
				loginLog.set("LOGIN_TIME",DateFormatUtils.getCurrentDate());
				loginLog.set("IP_ADDRESS", getRequest().getRemoteAddr());
				
				int logId = LoginLog.dao.add(loginLog);   //添加日志，并返回Id
				System.out.print("登录结果：" + operId + " 登录成功，");
				System.out.println("登录日志ID为:" + logId);
				if(logId>0) {   
					getSession().setAttribute("logId", logId);
				}
				
				render(RenderJson.success("登录成功!"));
			}
		}else {
			render(RenderJson.error("用户名或密码为空!"));
		}
	}
	
	/**
	 * 注销用户
	 */
	public void logout() {
		System.out.println("准备注销用户:" + getSession().getAttribute("currOperId"));
		//System.out.println(getSession().getAttribute("currOperId"));
		//System.out.println(getSession().getAttribute("currOperName"));
		//System.out.println(getSession().getAttribute("currAgentNumber"));
		//System.out.println(getSession().getAttribute("logId"));
		
		//同时，要修改退出登录的日志，修改其退出时间为
		if(!BlankUtils.isBlank(getSession().getAttribute("logId"))) {
			int logId = Integer.valueOf(getSession().getAttribute("logId").toString());
			LoginLog.dao.update(logId);
		}
		
		//将 session 删除即可
		getSession().removeAttribute("currOperId");
		getSession().removeAttribute("currOperName");
		getSession().removeAttribute("currAgentNumber");
		getSession().removeAttribute("logId");
		
		//render(RenderJson.success("注销成功"));
		//forwardAction("/index");
		render(RenderJson.success("注销成功"));
	}
	
	/**
	 * 根据传递上来的数据字典组编码及数据字典项编码，取得数据字典项名称
	 * 
	 * 主要是用于在 combobox 及显示数据时，显示正确的文字结果
	 * 
	 */
	public void getDictName() {
		
		String dictNameRs = null;   //定义返回的结果
		
		String groupCodeP = getPara("groupCode");
		String dictCodeP = getPara("dictCode");
		
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCodeP);
		
		for(Record record:list) {
			String dictCode = record.get("DICT_CODE");
			String dictName = record.get("DICT_NAME");
			
			if(dictCode.equalsIgnoreCase(dictCodeP)) {    //如果两者相同时，则返回数据字典项名称
				dictNameRs = dictName;
				break;
			}
		}
		render(RenderJson.success(dictNameRs));
	}
	
	/**
	 * 公共工具类：根据数据字典的 groupCode 取得radio并返回
	 */
	@SuppressWarnings("unchecked")
	public void getRadio() {
		StringBuilder sb = new StringBuilder();
		String groupCode = getPara("groupCode");
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCode);
		int i = 0;
		for(Record record:list) {
			String idInfo = groupCode + i;
			String value = record.get("DICT_CODE");
			String name = record.get("DICT_NAME");
			
			sb.append("<input type='radio' name='" + groupCode + "' id='" + idInfo + "' value='" + value + "'/><label for='" + idInfo +"'>" + name + "</label>&nbsp;&nbsp;&nbsp;");
			i++;
		}
		
		System.out.println("radio信息：" + sb.toString());
		render(RenderJson.success(sb.toString()));
	}
	
	public static String getRadioToString(String groupCode) {
		
		StringBuilder sb = new StringBuilder();
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCode);
		int i = 0;
		for(Record record:list) {
			String idInfo = groupCode + i;
			String value = record.get("DICT_CODE");
			String name = record.get("DICT_NAME");
			
			sb.append("<input type=\"radio\" name=\"" + groupCode + "\" id=\"" + idInfo + "\" value=\"" + value + "\"/><label for=\"" + idInfo +"\">" + name + "</label>&nbsp;&nbsp;&nbsp;");
			i++;
		}
		
		return sb.toString();
		
	}
	
	/**
	 * 公共工具类：根据数据字典的 groupCode 取得checkbox并返回
	 */
	public void getCheckBox() {
		
		StringBuilder sb = new StringBuilder();
		String groupCode = getPara("groupCode");
		
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCode);
		int i = 0;
		
		for(Record record:list) {
			String idInfo = groupCode + i;
			String value = record.get("DICT_CODE");
			String name = record.get("DICT_NAME");
			
			sb.append("<input type='checkbox' name='" + groupCode + "' id='" + idInfo + "' value='" + value + "'/><label for='" + idInfo + "'>" + name + "</label>&nbsp;&nbsp;");
			if(i>0 && i%7==0){
				sb.append("<br/>");
			}
			i++;
		}
		
		System.out.println("checkbox信息：" + sb.toString());
		render(RenderJson.success(sb.toString()));
	}
	
	
	/**
	 * 公共工具类：根据数据字典的 group　取得 combobox并返回
	 */
	public void getCombobox() {
		System.out.println("取 combobox的开始时间:" + DateFormatUtils.getTimeMillis());
		String groupCode = getPara("groupCode");
		String flag = getPara("flag");             //如果 flag为1时，则需要加入　"请选择"
		
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCode);
		
		//for(Record r:list) {
		//	System.out.println("得到了记录。。。。。。。。。。。：" + r.getStr("DICT_CODE") + ":" + r.get("DICT_NAME"));
		//}
		
		List<ComboboxJson> cbjs = new ArrayList<ComboboxJson>();   //定义一个TreeJson 的 list
		ComboboxJson defalutCbj = new ComboboxJson();
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			defalutCbj = new ComboboxJson();
			defalutCbj.setId("");
			defalutCbj.setText("请选择");
			
			cbjs.add(defalutCbj);
			
		}
		
		for(Record record:list) {
			ComboboxJson cbj = new ComboboxJson();
			cbj.setId(record.get("DICT_CODE").toString());
			cbj.setText(record.get("DICT_NAME").toString());
			
			cbjs.add(cbj);
			
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cbjs);
		System.out.println("取 combobox的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(jsonArray.toString());
	}
	
	/**
	 * 公共工具类：根据数据字典的 group　取得 combobox并返回
	 * 
	 * @param groupCode
	 * 			数据字典组代码
	 * @param flag
	 * 			0：没有请选择; 1： 指定请选择
	 */
	public static String getComboboxToString(String groupCode,String flag) {
		
		
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCode);
		
		List<ComboboxJson> cbjs = new ArrayList<ComboboxJson>();   //定义一个TreeJson 的 list
		ComboboxJson defalutCbj = new ComboboxJson();
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			defalutCbj = new ComboboxJson();
			defalutCbj.setId("empty");
			defalutCbj.setText("请选择");
			
			cbjs.add(defalutCbj);
			
		}
		
		if(!BlankUtils.isBlank(list)) {
			for(Record record:list) {
				ComboboxJson cbj = new ComboboxJson();
				cbj.setId(record.get("DICT_CODE").toString());
				cbj.setText(record.get("DICT_NAME").toString());
				
				cbjs.add(cbj);
			}
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cbjs);
		
		return jsonArray.toString();
	}
	
	/**
	 * 获取组织代码的选择树
	 * 
	 * 注：
	 * 		flag=0   表示得到的是所有的组织代码选择树
	 * 		flag=1   表示仅得到自己所在组织以下的选择树
	 * 
	 */
	public void getOrgComboTree() {
		System.out.println("取 getOrgComboTree的开始时间:" + DateFormatUtils.getTimeMillis());
		String flag = getPara("flag");
		String parentOrgCode = "-1";
		
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {   //如果为1时，表示返回自己所在组织以下的选择树
			String currOrgCode = getSession().getAttribute("currOrgCode").toString();    //当前登录的用户所在的组织
			Record org = Org.dao.getOrgByOrgCode(currOrgCode);          //根据当前登录用户所在的组织，得到组织信息
			parentOrgCode = org.get("PARENT_ORG_CODE").toString();
		}
		
		//查询所有的组织
		List<Record> orgList = Org.dao.getAllOrg(); 
		List<TreeJson> tjs = new ArrayList<TreeJson>();    //定义一个 treeJson 的List
		
		int rootIndex = 0;
		int i = 0;
		
		for(Record r:orgList) {
			
			TreeJson tj = new TreeJson();
			tj.setId(r.get("ORG_CODE").toString());
			tj.setText(r.get("ORG_NAME").toString());
			tj.setPid(r.get("PARENT_ORG_CODE").toString());
			tj.setDesc(r.get("ORG_DESC").toString());
			
			if(r.get("PARENT_ORG_CODE").toString().equalsIgnoreCase(parentOrgCode)) {
				rootIndex = i;
			}
			
			tjs.add(tj);
			
			i++;
		}
		
		if(rootIndex !=0) {
			TreeJson firstNode = tjs.get(0);
			TreeJson rootNode = tjs.get(rootIndex);
			
			tjs.set(0, rootNode);
			tjs.set(rootIndex, firstNode);
		}
		
		List<TreeJson> results = TreeJson.formatTree(tjs);
		
		JSONArray jsonArray = JSONArray.fromObject(results);
		
		System.out.println("取 getOrgComboTree的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(jsonArray.toString());
		
	}
	
	/**
	 * 获取组织代码的选择树
	 * 
	 * @param flag
	 * 		注：
	 * 		flag=0   表示得到的是所有的组织代码选择树
	 * 		flag=1   表示仅得到自己所在组织以下的选择树
	 * @param currOrgCode
	 * 		当前的登录用户所在用户组织代码
	 * @return
	 */
	public static String getOrgComboTreeToString(String flag,String currOrgCode) {
		System.out.println("取 getOrgComboTree的开始时间:" + DateFormatUtils.getTimeMillis());
		
		String parentOrgCode = "-1";
		
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {   //如果为1时，表示返回自己所在组织以下的选择树
			Record org = Org.dao.getOrgByOrgCode(currOrgCode);          //根据当前登录用户所在的组织，得到组织信息
			parentOrgCode = org.get("PARENT_ORG_CODE").toString();
		}
		
		//查询所有的组织
		List<Record> orgList = Org.dao.getAllOrg(); 
		List<TreeJson> tjs = new ArrayList<TreeJson>();    //定义一个 treeJson 的List
		
		int rootIndex = 0;
		int i = 0;
		
		for(Record r:orgList) {
			
			TreeJson tj = new TreeJson();
			tj.setId(r.get("ORG_CODE").toString());
			tj.setText(r.get("ORG_NAME").toString());
			tj.setPid(r.get("PARENT_ORG_CODE").toString());
			tj.setDesc(r.get("ORG_DESC").toString());
			
			if(r.get("PARENT_ORG_CODE").toString().equalsIgnoreCase(parentOrgCode)) {
				rootIndex = i;
			}
			
			tjs.add(tj);
			
			i++;
		}
		
		if(rootIndex !=0) {
			TreeJson firstNode = tjs.get(0);
			TreeJson rootNode = tjs.get(rootIndex);
			
			tjs.set(0, rootNode);
			tjs.set(rootIndex, firstNode);
		}
		
		List<TreeJson> results = TreeJson.formatTree(tjs);
		
		JSONArray jsonArray = JSONArray.fromObject(results);
		
		return jsonArray.toString();
		
	}
	
	/**
	 * 公共工具类：根据数据字典的 group　取得 combobox并返回
	 */
	public void getCombobox_bak() {
		
		String groupCode = getPara("groupCode");
		String flag = getPara("flag");             //如果 flag为1时，则需要加入　"请选择"
		
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCode);
		
		for(Record r:list) {
			System.out.println("得到了记录。。。。。。。。。。。：" + r.getStr("DICT_CODE") + ":" + r.get("DICT_NAME"));
		}
		
		List<TreeJson> tjs = new ArrayList<TreeJson>();   //定义一个TreeJson 的 list
		TreeJson defalutTj = new TreeJson();
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			defalutTj = new TreeJson();
			defalutTj.setId("");
			defalutTj.setText("请选择");
			
			tjs.add(defalutTj);
			
		}
		
		for(Record record:list) {
			TreeJson tj = new TreeJson();
			tj.setId(record.get("DICT_CODE").toString());
			tj.setText(record.get("DICT_NAME").toString());
			
			tjs.add(tj);
			
		}
		
		JSONArray jsonArray = JSONArray.fromObject(tjs);
		
		System.out.println("JsonArray----:" + jsonArray.toString());
		
		renderJson(jsonArray.toString());
	}
	
	/**
	 * 根据字典组码和字典项码，从内存中取出字典项
	 * 
	 * @param groupCode
	 * @param dictCode
	 * @return
	 */
	public static Record getDictItemFormMemoryVariable(String groupCode,String dictCode) {
		
		if(BlankUtils.isBlank(groupCode)||BlankUtils.isBlank(dictCode)) {
			return null;
		}
		
		List<Record> list = MemoryVariableUtil.dictMap.get(groupCode);   //根据字典组码，取出所有的字典项
		
		for(Record r:list) {
			String dictCodeV = r.getStr("DICT_CODE");
			
			if(dictCodeV.equals(dictCode)) {
				return r;
			}
			
		}
		
		return null;
		
	}
	
	/**
	 * 获取TTS的 AccessToken(tok)
	 * 1 如果内存中 tok 为空,重新获取一次,并将本次获取的时间存入内存中
	 * 2 如果内存中 tok 不为空,则查看上次获取的 accesstoken 的时间是否已经超过25天,如果超过，则重新获取一次
	 * 
	 * @return
	 */
	public static String getTTSTok() {
		
		String tok = MemoryVariableUtil.ttsParamMap.get("tok");
		String getTokDate = MemoryVariableUtil.ttsParamMap.get("getTokDate");   //最后一次获取 tok 的时间
		
		if(BlankUtils.isBlank(tok)) {       //如果 tok 为空时,则重新获取 tok 一次，并设置获取 tok 的时间
			
			String requestTok = HttpRequestUtils.httpRequestForTok();
			
			if(!BlankUtils.isBlank(requestTok)) {   
				MemoryVariableUtil.ttsParamMap.put("tok", requestTok);
				MemoryVariableUtil.ttsParamMap.put("getTokDate", DateFormatUtils.getFormatDate());
				
				return requestTok;
			}else {
				return null;    //否则返回空值
			}
			
		}else {                            //如果 tok 不为空时,判断最后一次获取 tok 的时间与现在的时间差
			
			try {
				
				//上次取得TOK的日期与当前日期的差值
				int i = DateFormatUtils.daysBetween(getTokDate, DateFormatUtils.getFormatDate());
				
				if(i <= 25) {               //如果相差小于25天时，直接将 tok 返回即可
					return tok;
				}else {                     //如果相差大于 25天时，重新获取一次 tok，并设置获取 tok 的时间
					
					String requestTok = HttpRequestUtils.httpRequestForTok();
					
					if(!BlankUtils.isBlank(requestTok)) {   
						MemoryVariableUtil.ttsParamMap.put("tok", requestTok);
						MemoryVariableUtil.ttsParamMap.put("getTokDate", DateFormatUtils.getFormatDate());
						
						return requestTok;
					}else {                 //如果请求不到时，将内存中的 tok 清空
						MemoryVariableUtil.ttsParamMap.put("tok", null);
						MemoryVariableUtil.ttsParamMap.put("getTokDate", null);
						
						return null;
					}
					
				}
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			
		}
		
		return null;
		
	}
	
	/**
	 * 挂机
	 */
	public void doHangup() {
		
		String agentNumber = (String)getSession().getAttribute("currAgentNumber");      //得到座席号
		
		System.out.println("准备执行挂机，座席号码为:" + agentNumber);
		
		Map rs = CtiUtils.doHangup(agentNumber);
		
		String result = (String) rs.get("result");
		String str = (String) rs.get("str");
		
		if(!BlankUtils.isBlank(result)&&result.equalsIgnoreCase("1")) {    //表示执行成功
			render(RenderJson.success(str));
		}else {
			render(RenderJson.error(str));
		}
		
	}
	
	/**
	 * 执行外呼的方法
	 */
	public void doDial() {
		
		String agentNumber = (String)getSession().getAttribute("currAgentNumber");    //得到座席号
		String telephone = getPara("telephone");
		
		System.out.println("准备执行外呼，外呼座席号为： " + agentNumber + ",客户号码为: " + telephone);
		
		String channel = "SIP/" +agentNumber;
		String context = "from-exten-sip";
		String exten = telephone;
		int priority = 1;
		long timeout = 30 * 1000;
		CallerId cid = new CallerId("0762883322","0762883322");
		Map m = new HashMap();
		
		try {
			Map rs = CtiUtils.doCallOutByAgent(agentNumber,channel, context, exten, priority, timeout, cid, m, new OriginateCallback() {
				
				/**
				 * 客户正忙
				 */
				@Override
				public void onBusy(AsteriskChannel channel) {
					System.out.println("线路繁忙...");
				}

				/**
				 * 开始外呼...
				 */
				@Override
				public void onDialing(AsteriskChannel channel) {
					System.out.println("开始呼叫...");
				}
				
				/**
				 * 呼叫失败
				 */
				@Override
				public void onFailure(LiveException live) {
					System.out.println("呼叫失败...");
					
				}

				/**
				 * 客户无应答
				 */
				@Override
				public void onNoAnswer(AsteriskChannel arg0) {
					System.out.println(" 无人接听...");
					
				}
				
				/**
				 * 呼叫成功
				 */
				@Override
				public void onSuccess(AsteriskChannel arg0) {
					System.out.println("呼叫成功...");
					
				}
				
			});
			
			String result = (String) rs.get("result");
			String str = (String) rs.get("str");
			
			if(!BlankUtils.isBlank(result)&&result.equalsIgnoreCase("1")) {    //表示执行成功
				render(RenderJson.success(str));
			}else {
				render(RenderJson.error(str));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 话务集成操作：外呼，通道保持，呼叫转移，示忙，示闲，挂机等等操作
	 * 
	 */
	public void doCti() {
		
		//标识：1 外呼; 2 通话保持; 3 呼叫转移; 4 示忙; 5 挂机
		String flg = getPara("flg");
		
		String msg = null;
		
		if(flg.equals("1")) {
			msg = "外呼执行成功";
		}else if(flg.equals("2")) {
			msg = "通话保持执行成功";
		}else if(flg.equals("3")) {
			msg = "呼叫转移执行成功";
		}else if(flg.equals("4")) {
			msg = "示忙执行成功";
		}else if(flg.equals("5")) {
			msg = "挂机执行成功";
		}
		
		render(RenderJson.success(msg));
		
	}
	
}
