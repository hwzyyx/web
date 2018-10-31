package com.callke8.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;

import com.callke8.astutils.AstMonitor;
import com.callke8.astutils.CtiUtils;
import com.callke8.astutils.LaunchCtiService;
import com.callke8.call.incoming.InComing;
import com.callke8.system.loginlog.LoginLog;
import com.callke8.system.module.ModuleController;
import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.ComboboxJson;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.HttpRequestUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.OrgTreeUtils;
import com.callke8.utils.RenderJson;
import com.callke8.utils.TreeJson;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Record;

import net.sf.json.JSONArray;

@ClearInterceptor
public class CommonController extends Controller {
	
	Logger log = Logger.getLogger(CommonController.class);
	
	public void index() {
		if(BlankUtils.isBlank(getSession().getAttribute("currOperId"))) {
			//render("login.jsp");
			setAttr("webSiteName",ParamConfig.paramConfigMap.get("paramType_1_webSiteName"));
			setAttr("copyrightInfo",ParamConfig.paramConfigMap.get("paramType_1_copyrightInfo"));
			//System.out.println("网站名称:" + ParamConfig.paramConfigMap.get("paramType_1_webSiteName") + "=====版本信息:" + ParamConfig.paramConfigMap.get("paramType_1_copyrightInfo") );
			//redirect("/login");
			render("login.jsp");
		}else {
			//当前座席号码
			String currAgentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?null:getSession().getAttribute("currAgentNumber").toString();
			
			setAttr("menuAccordionData",ModuleController.getMenuToString(getSession().getAttribute("currOperId").toString()));
			
			setAttr("loginInfo",getLoginInfo());      //显示登录信息
			
			setAttr("currAgentNumber",currAgentNumber);
			
			setAttr("webSiteName",ParamConfig.paramConfigMap.get("paramType_1_webSiteName"));
			
			render("index-simple.jsp");
		}
	}
	
	public String getLoginInfo() {
		
		String currOperId = BlankUtils.isBlank(getSession().getAttribute("currOperId"))?"":getSession().getAttribute("currOperId").toString();
		String currOperName = BlankUtils.isBlank(getSession().getAttribute("currOperName"))?"":getSession().getAttribute("currOperName").toString();
		String currAgentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("工号：");
		sb.append(currOperId);
		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		
		sb.append("操作员：");
		sb.append(currOperName);
		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		
		sb.append("座席号：");
		sb.append(currAgentNumber);
		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		
		return sb.toString();
		
	}
	
	/**
	 * 如果传入一组组织代码(代码格式: "a,b,c,d")，返回创建记录的操作员 ID 的条件
	 * 
	 * @param orgCode
	 * 			orgCode 的格式是这样的："a,b,c,d"
	 * @return
	 * 			返回的字符串格式：'a','b','c'
	 */
	public static String getOperIdStringByOrgCode(String orgCode,HttpSession httpSession) {
		
		String rs = null;
		
		String currOperId = String.valueOf(httpSession.getAttribute("currOperId"));      //当前登录的用户ID
		String currOrgCode = String.valueOf(httpSession.getAttribute("currOrgCode"));    //当前登录的组织码
		//System.out.println("-------=======查询任务，登录的用户ID ：" + currOperId + ", 登录用户的组织:" + currOrgCode);
		if(!(BlankUtils.isBlank(currOperId) || BlankUtils.isBlank(currOrgCode))) {
			//由于查询出来的记录，只能是自己及自己下属的记录，所以不能以组织码为条件去查询，只能是根据组织码条件，查询出相应的组织码下有什么用户
			List<Record> opertorList = Operator.dao.getOperatorByOrgCode(orgCode);    //根据传入组织代码字符串，查出这些组织的所有的操作员
			if(!BlankUtils.isBlank(opertorList)) {    //如果操作员不为空时
				String operIdString = "";
				for(Record r:opertorList) {
					String operIdRs = r.getStr("OPER_ID");              //操作员ID
					String orgCodeRs = r.getStr("ORG_CODE");            //组织代码
					
					//查出来的操作员，与登录的用户的组织一样时，就表示这个与登录的人是同级的人，需要去除掉
					if(!currOperId.equals("super")) {
						if(!operIdRs.equals(currOperId) && orgCodeRs.equals(currOrgCode)) {    //如果当前客户ID不是当前登录的人，但是组织码与当前登录用户相同的组织代码时，表示这个客户应该与登录用户是同级关系，要去掉
							continue;
						}
					}
					operIdString += "\'" + operIdRs + "\',";
				}
				
				System.out.println("查询的组织代码的字串: " + operIdString);
				if(!BlankUtils.isBlank(operIdString)) {
					rs = operIdString.substring(0,operIdString.length()-1);     //删除最后一个逗号
				}
			}
		}
		
		return rs;
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
		setAttr("webSiteName",ParamConfig.paramConfigMap.get("paramType_1_webSiteName"));
		setAttr("copyrightInfo",ParamConfig.paramConfigMap.get("paramType_1_copyrightInfo"));
		render("login.jsp");
	}
	
	public void doLogin() {
		
		String operId = getPara("operId");
		String password = getPara("password");    //上传的密码，在前端已经过 Md5 加密,故收到密码后无须再加密
		//String callNumber = getPara("callNumber");
		
		//
		if(!BlankUtils.isBlank(operId)&& !BlankUtils.isBlank(password)) {
			//先根据传入的 operId，将操作员信息查询出来
			Operator operator = Operator.dao.getOperatorByOperId(operId);
			
			if(BlankUtils.isBlank(operator)) {    //先判断查询结果是否为空
				render(RenderJson.error("工号不存在!"));
				return;
			}
			
			//判断密码是否相等
			if(!password.equalsIgnoreCase(operator.get("PASSWORD").toString())) {
				render(RenderJson.error("用户名或密码不正确!"));
			}else {
				//登录正确
				getSession().setAttribute("currOperId",operId);
				//getSession().setAttribute("currAgentNumber",callNumber);
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
		//index();   //转到 index() 中去执行
		//forward
		//forwardAction("/index");
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
		
		//查询所有的组织
		List<Record> orgList = Org.dao.getAllOrg(); 
		
		OrgTreeUtils otu = new OrgTreeUtils();
		List<Record> childList = otu.getChildNode(orgList, currOrgCode);     //得到当前组织的所有下属组织部门
		
		List<TreeJson> tjs = new ArrayList<TreeJson>();    //定义一个 treeJson 的List
		
		for(Record r:childList) {
			
			TreeJson tj = new TreeJson();
			tj.setId(r.get("ORG_CODE").toString());
			tj.setText(r.get("ORG_NAME").toString());
			tj.setPid(r.get("PARENT_ORG_CODE").toString());
			tj.setDesc(r.get("ORG_DESC").toString());
			
			tjs.add(tj);
		}
		
		List<TreeJson> results = TreeJson.formatTree(tjs);
		
		JSONArray jsonArray = JSONArray.fromObject(results);
		
		return jsonArray.toString();
		
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
	public static String getOrgComboTreeToString_bak(String flag,String currOrgCode) {
		System.out.println("取 getOrgComboTree的开始时间:" + DateFormatUtils.getTimeMillis());
		
		String parentOrgCode = "-1";
		
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {   //如果为1时，表示返回自己所在组织以下的选择树
			Record org = Org.dao.getOrgByOrgCode(currOrgCode);          //根据当前登录用户所在的组织，得到组织信息
			//parentOrgCode = org.get("PARENT_ORG_CODE").toString();
			parentOrgCode = org.get("ORG_CODE").toString();
		}
		
		System.out.println("parentOrgCode:-----" + parentOrgCode);
		
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
		
		//String tok = MemoryVariableUtil.ttsParamMap.get("tok");
		String tok = ParamConfig.paramConfigMap.get("paramType_2_tok");
		String getTokDate = ParamConfig.paramConfigMap.get("paramType_2_getTokDate");    //最后一次获取 tok 的时间
		
		if(BlankUtils.isBlank(tok)) {       //如果 tok 为空时,则重新获取 tok 一次，并设置获取 tok 的时间
			
			String requestTok = HttpRequestUtils.httpRequestForTok();
			
			if(!BlankUtils.isBlank(requestTok)) {
				ParamConfig.paramConfigMap.put("paramType_2_tok", requestTok);
				ParamConfig.paramConfigMap.put("paramType_2_getTokDate",DateFormatUtils.getFormatDate());
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
						ParamConfig.paramConfigMap.put("paramType_2_tok", requestTok);
						ParamConfig.paramConfigMap.put("paramType_2_getTokDate",DateFormatUtils.getFormatDate());
						
						return requestTok;
					}else {                 //如果请求不到时，将内存中的 tok 清空
						ParamConfig.paramConfigMap.put("paramType_2_tok", null);
						ParamConfig.paramConfigMap.put("paramType_2_getTokDate",null);
						
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
	 * 
	 * 传入坐席号码
	 */
	public void doHangup() {
		
		String agentNumber = (String)getSession().getAttribute("currAgentNumber");      //得到座席号
		
		System.out.println("准备执行挂机，座席号码为:" + agentNumber);
		
		CtiUtils.doHangUpByAgentNumber(agentNumber);
		
	}
	
	/**
	 * 执行外呼的方法
	 */
	public void doDial() {
		
		String agentNumber = (String)getSession().getAttribute("currAgentNumber");    //得到座席号
		String telephone = getPara("telephone");
		
		System.out.println("准备执行外呼，外呼座席号为： " + agentNumber + ",客户号码为: " + telephone);
		
		String channel = "SIP/" +agentNumber;
		String context = ParamConfig.paramConfigMap.get("paramType_1_defaultCallOutContext");
		String exten = telephone;
		int priority = 1;
		long timeout = 30 * 1000;
		CallerId cid = new CallerId(ParamConfig.paramConfigMap.get("paramType_1_defaultCallerId"),ParamConfig.paramConfigMap.get("paramType_1_defaultCallerId"));
		Map m = new HashMap();
		
		String rs = CtiUtils.doCallOutByAgent(agentNumber, channel, context, exten, priority, timeout, cid, m, new OriginateCallback(){

			/**
			 * 开始外呼
			 */
			@Override
			public void onDialing(AsteriskChannel channel) {
				
				System.out.println("开始外呼");
			}
			
			/**
			 * 客户无应答
			 */
			@Override
			public void onNoAnswer(AsteriskChannel channel) {
				
				System.out.println("客户无应答");
			}
			
			/**
			 * 客户繁忙
			 */
			@Override
			public void onBusy(AsteriskChannel channel) {
				
				System.out.println("客户繁忙");
			}

			/**
			 * 外呼失败
			 */
			@Override
			public void onFailure(LiveException le) {
				
				System.out.println("外呼失败");
			}

			/**
			 * 外呼成功
			 */
			@Override
			public void onSuccess(AsteriskChannel channel) {
				System.out.println("外呼成功");
			}
			
		});
		
		if(rs.equalsIgnoreCase("success")) {
			render(RenderJson.success("发送外呼操作指令成功!"));
		}else {
			render(RenderJson.error(rs));
		}
		
	}
	

	/**
	 * 话务集成操作：外呼，通道保持，呼叫转移，示忙，示闲，挂机等等操作
	 * 
	 * 动作描述：
	 * 
	 * 	     signIn: 签入
	 *      signOut: 签出
	 *      callOut: 呼出
	 * 		 holdOn: 保持
	 * cancelHoldOn: 取消保持
	 *  callForward: 呼叫转移
	 *         busy: 示忙
	 *         free: 示闲
	 *       hangUp: 挂机
	 * 
	 */
	public void doCti() {
		
		String content_type = getRequest().getContentType();
		
		System.out.println("我得到的 content_type" + content_type);
		
		String msg = "";              //处理结果
		
		//获取cti 动作
		/*
		* 	     signIn: 签入
		 *      signOut: 签出
		 *      callOut: 呼出
		 * 		 holdOn: 保持
		 * cancelHoldOn: 取消保持
		 *  callForward: 呼叫转移
		 *         busy: 示忙
		 *         free: 示闲
		 *       hangUp: 挂机
		 */
		
		
		String actionName = getPara("actionName");
		
		
		if(actionName.equalsIgnoreCase("signIn")) {    				//签入
			
			String agentNumber = getPara("agentNumber");   //获取座席号码
			
			System.out.println("actionName:" + actionName + ";agentNumber：" + agentNumber);
			
			//一、先检查填写签入座席号码是否为空
			if(BlankUtils.isBlank(agentNumber)) {
				 render(RenderJson.error("签入失败,签入座席号码为空!"));
				 return;
			}
			
			//二、检查 PBX 连接状态
			boolean connState = CtiUtils.checkConnectionState();
			
			if(!connState) {
				render(RenderJson.error("签入失败,话务服务器连接状态异常!"));
				return;
			}
			
			//三、检查座席号码是否在线
			//从内存数据中取出座席的状态
			boolean isOnLine = false;

			String agentState = MemoryVariableUtil.agentStateMap.get(agentNumber);   //取出状态
			
			if(!BlankUtils.isBlank(agentState)) {
				System.out.println("要签入的坐席号码 " + agentNumber + " 当前的状态为:" + agentState);
				if(!(agentState.equalsIgnoreCase("Unknown") || agentState.equalsIgnoreCase("Invalid") || agentState.equalsIgnoreCase("Unavailable"))) {
					isOnLine = true;
				}
			}
			
			if(!isOnLine) {   
				render(RenderJson.error("签入失败,座席号码 " + agentNumber + " 掉线,请检查话机状态后再进行签入!"));
				return;
			}
			
			//四、执行绑定
			getSession().setAttribute("currAgentNumber", agentNumber);
			
			//更新登录信息,用于前端显示
			String loginInfo = getLoginInfo();
			
			render(RenderJson.success("座席签入成功!",loginInfo,agentNumber));
			return;
		}else if(actionName.equalsIgnoreCase("signOff")) {			//签出
			
			String agentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
			
			if(BlankUtils.isBlank(agentNumber)) {                   //先查看当前账号是否已经签入了座席，如果无签入座席，返回提示信息
				
				render(RenderJson.error("当前登录账号并未签入座席,忽略签出操作!",getLoginInfo()));
				return;
			}
			
			getSession().removeAttribute("currAgentNumber");    //从 session 去除 currAgentNumber
			render(RenderJson.success("签出操作成功!",getLoginInfo()));
			return;
			
		}else if(actionName.equalsIgnoreCase("callOut")) {          //呼出
			
			//取得输入客户外呼号码
			String clientNumber = getPara("clientNumber"); 
			final String agentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
			
			//一 检查客户号码
			if(BlankUtils.isBlank(clientNumber)) {
				render(RenderJson.error("执行外呼失败,输入的客户号码为空!"));
				return;
			}
			
			//二 检查座席号码是否为空
			if(BlankUtils.isBlank(agentNumber)) {
				render(RenderJson.error("执行外呼失败,当前账号未关联座席!"));
				return;
			}
			
			//三、检查 PBX 连接状态
			boolean connState = CtiUtils.checkConnectionState();
			
			if(!connState) {
				render(RenderJson.error("执行外呼失败,话务服务器连接状态异常!"));
				return;
			}
			
			//四 检查座席号码的状态
			String agentState = MemoryVariableUtil.agentStateMap.get(agentNumber);
			
			boolean isFree = false; 
			
			if(!BlankUtils.isBlank(agentState) && (agentState.equalsIgnoreCase("idle") || agentState.equalsIgnoreCase("NOT_INUSE"))) {     //如果号码的状态为 idle或是 not_inuse 即是空闲状态
				isFree = true;
			}
			
			if(!isFree) {   
				render(RenderJson.error("执行呼叫失败,座席号码 " + agentNumber + " 非空闲状态,请检查话机状态后再进行签入!"));
				return;
			}
			
			//五 执行外呼
			//(组织相关参数)
			
			LaunchCtiService ctiService = new LaunchCtiService("doCallOutByAgent", agentNumber, clientNumber);
			
			Thread ctiThread = new Thread(ctiService);
			
			ctiThread.start();
			
			render(RenderJson.success("系统正在执行呼叫..."));
			return;
			
		}else if(actionName.equalsIgnoreCase("park")) {           //保持
			
			String agentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
			
			//一、检查是否已签入 座席号码
			if(BlankUtils.isBlank(agentNumber)) {
				render(RenderJson.error("执行外呼失败,当前账号未关联座席!"));
				return;
			}
			
			//二、检查 PBX 连接状态
			boolean connState = CtiUtils.checkConnectionState();
			
			if(!connState) {
				render(RenderJson.error("执行通话保持失败,话务服务器连接状态异常!"));
				return;
			}
			
			//三、检查当前座席是否在通话中
			boolean isInUse = false;
			
			String agentState = MemoryVariableUtil.agentStateMap.get(agentNumber);
			
			
			if(!BlankUtils.isBlank(agentState) && agentState.equalsIgnoreCase("InUse")) {
				isInUse = true;
			}
			
			if(!isInUse) {     //如果话机没有在通话中时，返回错误
				render(RenderJson.error("座席号码：" + agentNumber + " 未在通话中, 无法执行通话保持!"));
				return;
			}
			
			//四、执行通话保持
			Map<String,String> channelMap = CtiUtils.getSrcChannelAndDstChannelByAgentNumber(agentNumber);     //在执行通话保持之前,先取出通话中的座席对应的源通道和目标通道
			System.out.println("得到的 channelMap: " + channelMap);
			if(!BlankUtils.isBlank(channelMap)) {
				
				String srcChannel = channelMap.get("srcChannel");
				String dstChannel = channelMap.get("dstChannel");
				
				log.info("准备执行通话保持,获取到的源通道为: " + srcChannel + ",目标通道为:" + dstChannel);
				
				if(!BlankUtils.isBlank(srcChannel) && !BlankUtils.isBlank(dstChannel)) {
					
					//在执行通话保持之前,将保持的记录进行储存
					CtiUtils.parkMap.put(agentNumber, dstChannel);
					
					CtiUtils.doPark(srcChannel,dstChannel);
					render(RenderJson.success("执行通话保持成功!"));
					return;
				}else {
					
					render(RenderJson.error("执行通话保持失败,无法获取座席的源通道和目标通道!估计座席不在通话中."));
					return;
				}
				
				
				
			}else {
				render(RenderJson.error("执行通话保持失败,无法获取座席的源通道和目标通道!估计座席不在通话中."));
				return;
			}
			
			
		}else if(actionName.equalsIgnoreCase("backPark")) {     //取消保持
			
			String agentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
			
			//一、检查是否已签入 座席号码
			if(BlankUtils.isBlank(agentNumber)) {
				CtiUtils.parkMap.remove(agentNumber);
				render(RenderJson.error("执行恢复通话失败,当前账号未关联座席!"));
				return;
			}
			
			//二、检查 PBX 连接状态
			boolean connState = CtiUtils.checkConnectionState();
			
			if(!connState) {
				CtiUtils.parkMap.remove(agentNumber);
				render(RenderJson.error("执行通话保持失败,话务服务器连接状态异常!"));
				return;
			}
			
			//三、检查当前座席是否处于通话保持中
			boolean isParked = false;
			
			//取出保持的目标通道
			String dstChannel = CtiUtils.parkMap.get(agentNumber);
			
			if(!BlankUtils.isBlank(dstChannel)) {
				isParked = true;
			}
			
			if(!isParked) {
				CtiUtils.parkMap.remove(agentNumber);
				render(RenderJson.error("当前座席并非处于通话保持中,取消通话保持失败!"));
				return;
			}
			
			//四、查看该目标通道是否还存在
			boolean b = CtiUtils.isExistChannel(dstChannel);
			if(!b) {
				CtiUtils.parkMap.remove(agentNumber);
				render(RenderJson.error("恢复通话失败,目标通道已经不存在,可能对方已挂机!"));
				return;
			}
			
			
			//五、恢复通话
			CtiUtils.doBackPark(agentNumber, dstChannel);
			CtiUtils.parkMap.remove(agentNumber);
			
			render(RenderJson.success("座席恢复通话中..."));
			return;
			
		}else if(actionName.equalsIgnoreCase("callForward")) {      //呼叫转移
			
			//取得转移的目标号码
			String forwardNumber = getPara("forwardNumber");
			String agentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
			
			//一、检查客户号码
			if(BlankUtils.isBlank(forwardNumber)) {
				render(RenderJson.error("执行呼叫转移失败,输入的目标号码为空!"));
				return;
			}
			
			//二、检查座席号码是否为空
			if(BlankUtils.isBlank(agentNumber)) {
				render(RenderJson.error("执行呼叫转移失败,当前账号未关联座席!"));
				return;
			}
			
			//三、检查 PBX 连接状态
			boolean connState = CtiUtils.checkConnectionState();
			
			if(!connState) {
				render(RenderJson.error("执行外呼转移失败,话务服务器连接状态异常!"));
				return;
			}
			
			//四 检查座席号码的状态
			boolean isInUse = false;
			
			String agentState = MemoryVariableUtil.agentStateMap.get(agentNumber);
			
			if(!BlankUtils.isBlank(agentState) && agentState.equalsIgnoreCase("InUse")) {
				isInUse = true;
			}
			
			if(!isInUse) {   
				render(RenderJson.error("执行呼叫转移失败,座席号码 " + agentNumber + " 未在通话中!"));
				return;
			}
			
			//五 执行呼叫转移
			
			//先取出目标通道
			String dstChannel = CtiUtils.getDstChannelByAgentNumber(agentNumber);
			
			log.info("执行呼叫转移的目标通道为 : " + dstChannel);
			if(BlankUtils.isBlank(dstChannel)) {
				render(RenderJson.error("执行呼叫转移失败,系统无法取得目标通道!请检查后再执行呼叫转移."));
				return;
			}
			
			//执行呼叫转移操作
			CtiUtils.doTransfer(dstChannel, forwardNumber);
			
			render(RenderJson.success("呼叫转移已经执行!"));
			return;
			
		}else if(actionName.equalsIgnoreCase("busy")) {             //示忙
			
			//取出座席号码
			String agentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
			
			//一、检查座席号码是否为空
			if(BlankUtils.isBlank(agentNumber)) {
				render(RenderJson.error("执行示忙失败,当前账号未关联座席!"));
				return;
			}
			
			//二、检查 PBX 连接状态
			boolean connState = CtiUtils.checkConnectionState();
			
			if(!connState) {
				render(RenderJson.error("执行示忙失败,话务服务器连接状态异常!"));
				return;
			}
			
			//三、检查座席状态是否已经登录 
			boolean isOnLine = true;
			
			if(!isOnLine) {
				render(RenderJson.error("执行示忙失败,当前座席号码 " + agentNumber + " 已经掉线!"));
				return;
			}
			
			//四、暂时没有必要检查当前座席是否已经示忙，即使现在已经处于示忙中时,再次示忙时，也不会导致执行命令错误。
			
			CtiUtils.doDNDOn(agentNumber);
			
			render(RenderJson.success("示忙成功!"));
			return;
			
		}else if(actionName.equalsIgnoreCase("free")) {             //示闲
			
			//取出座席号码
			String agentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
			
			//一、检查座席号码是否为空
			if(BlankUtils.isBlank(agentNumber)) {
				render(RenderJson.error("执行示闲失败,当前账号未关联座席!"));
				return;
			}
			
			//二、检查 PBX 连接状态
			boolean connState = CtiUtils.checkConnectionState();
			
			if(!connState) {
				render(RenderJson.error("执行示闲失败,话务服务器连接状态异常!"));
				return;
			}
			
			//三、检查座席状态是否已经登录 
			String agentState = MemoryVariableUtil.agentStateMap.get(agentNumber);
			//
			if(agentState.equalsIgnoreCase("Unavailable")) {
				render(RenderJson.error("执行示闲失败,当前座席号码 " + agentNumber + " 已经掉线!"));
				return;
			}
			
			//四、检查座席状态二，当前座席是否处于示忙中(暂时可以忽略)
			
			
			//五、 执行示闲操作
			CtiUtils.doDNDOff(agentNumber);
			
			render(RenderJson.success("示闲成功!"));
			return;
			
		}else if(actionName.equalsIgnoreCase("hangup")) {           //挂机
			
			//取出座席号码
			String agentNumber = BlankUtils.isBlank(getSession().getAttribute("currAgentNumber"))?"":getSession().getAttribute("currAgentNumber").toString();
			
			//一、检查座席号码是否为空
			if(BlankUtils.isBlank(agentNumber)) {
				render(RenderJson.error("执行挂机失败,当前账号未关联座席!"));
				return;
			}
			
			//二、检查 PBX 连接状态
			boolean connState = CtiUtils.checkConnectionState();
			if(!connState) {
				render(RenderJson.error("执行挂机失败,话务服务器连接状态异常!"));
				return;
			}
			
			//三、检查当前座席是否在通话中
			boolean isInUse = false;
			String agentState = MemoryVariableUtil.agentStateMap.get(agentNumber);
			
			if(!BlankUtils.isBlank(agentState) && agentState.equalsIgnoreCase("InUse")) {
				isInUse = true;
			}
			
			if(!isInUse) {
				render(RenderJson.error("执行挂机失败,当前座席 " + agentNumber + " 未在通话中!"));
				return;
			}
			
			//四、执行挂机操作
			CtiUtils.doHangUpByAgentNumber(agentNumber);
			
			render(RenderJson.success("系统正在执行挂机操作!"));
			return;
		}
		
	}
	
	
	/**
	 * 根据传入的座席号码,取得座席的状态
	 * 
	 * 状态值主要从 AgentStateMonitor 中定义扫描的结果取得
	 * 
	 * 状态值可能如下：
	 * 
	 * 空值 ：表示未获取到座席状态
	 * Unavailable：未注册（已掉线）
	 * Idle: 空闲
	 * Ringing: 响铃中
	 * InUse: 通话中
	 * DND: 示忙
	 * 
	 * 不过为了前端使用的方便, 都转化为小写
	 * 
	 */
	public void getAgentState() {
		
		String agentNumber = getPara("agentNumber");
		
		if(BlankUtils.isBlank(agentNumber)) {
			//System.out.println("当前账户未关联座席,暂时无法取得座席状态!");
			render(RenderJson.success("nosignin"));
			return;
		}
		
		String state = MemoryVariableUtil.agentStateMap.get(agentNumber);
		//System.out.println("座席: " + agentNumber + " 的当前状态为: " + state);
		render(RenderJson.success(state));
		
	}
	
	
	
}
