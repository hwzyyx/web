package com.callke8.system.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.text.DateFormatter;

import net.sf.json.JSONArray;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.Md5Utils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.RenderJson;
import com.callke8.utils.TreeJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class OperatorController extends Controller implements IController {
	
	public void index() {
		render("list.jsp");
	}
	
	@SuppressWarnings("unchecked")
	public void datagrid() {
		
		String orgCode = getPara("orgCode");
		String operId = getPara("operId");
		String operName = getPara("operName");
		String operState = getPara("operState");
		
		//获取参数，每页记录数量及当前页数
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"))==0?1:Integer.valueOf(getPara("page"));
		
		
		Map getOperatorByPaginateToMap = Operator.dao.getOperatorByPaginateToMap(page, rows, operId, operName, operState,orgCode);
		
		renderJson(getOperatorByPaginateToMap);
	}
	
	/**
	 * 根据 operId 取得角色列表，用于前台选中对应的角色
	 * 
	 * @param operId
	 */
	public void getRoleByOperId() {
		String operId = getPara("operId");
		
		StringBuilder sb = new StringBuilder();
		List<String> roleList = OperRole.dao.getRoleCodeByOperId(operId);
		
		for(String roleCode:roleList) {
			sb.append(roleCode + ",");
		}
		
		String message = sb.toString();
		if(BlankUtils.isBlank(message)) {   //如果返回为空时，返回 error
			render(RenderJson.error(message));
		}else {                             //如果返回不为空时，返回成功
			render(RenderJson.success(message.substring(0,message.length()-1)));  //返回时，去除最后一个逗号
		}
		
	}
	
	public void chkOperId() {
		String operId = getPara("operId");
		
		Operator operator = Operator.dao.getOperatorByOperId(operId);
		
		if(!BlankUtils.isBlank(operator)) {   //如果不为空时，表示已经存在相同工号
			render(RenderJson.error("已经存在相同的工号"));
		}
		
	}
	
	public void changePassword() {
		
		String oldPassword = getPara("operator.OLD_PASSWORD");
		String newPassword = getPara("operator.NEW_PASSWORD");
		
		String operId = getSessionAttr("currOperId");
		
		//检查当前的登录的账户是否过期，如果为空时，则返回错误
		if(BlankUtils.isBlank(operId)) {
			render(RenderJson.error("修改失败，登录账号的Session已经过期!"));
			return;
		}
		
		//先根据操作工号，从数据库取出操作员信息
		Operator operator = Operator.dao.getOperatorByOperId(operId);
		
		String password = operator.get("PASSWORD");    //取出密码
		
		String oldPasswordMd5 = Md5Utils.Md5(oldPassword);  //将上传的老密码先Md5 加密
		
		//如果两个密码不相同时，则返回提供原密码不对
		if(!password.equalsIgnoreCase(oldPasswordMd5)) {    
			render(RenderJson.error("修改失败，原密码不正确!"));
			return;
		}
		
		boolean b = Operator.dao.changePassword(operId, Md5Utils.Md5(newPassword));    
		
		if(b) {
			render(RenderJson.success("密码修改成功"));
		}else {
			render(RenderJson.error("密码修改失败!"));
		}
		
	}
	
	public void update() {
		Operator  oper = getModel(Operator.class,"operator");
		String ids = getPara("ids");
		String operId = oper.get("OPER_ID");
		String sex = oper.get("SEX");
		String orgCode = getPara("orgCode");
		
		
		oper.set("ORG_CODE", orgCode);
		if(BlankUtils.isBlank(sex)){oper.set("SEX", "1");};   //如果性别为空时，默认为男
		boolean b = Operator.dao.update(oper);   //更新操作员信息
		
		if(b) {
			//更新操作员角色列表
			//先删除所有的角色，然后再添加
			boolean b2 = OperRole.dao.delete(operId);   //先删除所有的角色记录
			
			String[] idLists = ids.split(",");
			for(String id:idLists) {
				OperRole or = new OperRole();
				or.set("OPER_ID", operId);
				or.set("ROLE_CODE",id);
				OperRole.dao.add(or);
			}
			
			//System.out.println("Ids:===" + ids);
			//修改成功时，重新加载操作员的数据到内存中
			MemoryVariableUtil.operatorMap = Operator.dao.loadOperatorInfo();
			
			render(RenderJson.success("修改成功！"));
		}else {
			render(RenderJson.success("修改失败！"));
		}
	}
	
	public void add() {
		Operator  oper = getModel(Operator.class,"operator");
		String ids = getPara("ids");
		String orgCode = getPara("orgCode");
		String operId = oper.get("OPER_ID");
		String sex = oper.get("SEX");
		
		//先检查一下唯一性
		if(!BlankUtils.isBlank(Operator.dao.getOperatorByOperId(operId))) {
			render(RenderJson.warn("存在相同的工号!"));
			return;
		}
		
		oper.set("CREATETIME", DateFormatUtils.getCurrentDate());
		oper.set("ORG_CODE",orgCode);
		oper.set("PASSWORD",Md5Utils.Md5(oper.get("PASSWORD").toString()));
		if(BlankUtils.isBlank(sex)){oper.set("SEX", "1");};   //如果性别为空时，默认为男
		Operator.dao.add(oper);
		
		String[] idLists = ids.split(",");
		for(String id:idLists) {
			OperRole or = new OperRole();
			or.set("OPER_ID", operId);
			or.set("ROLE_CODE",id);
			OperRole.dao.add(or);
		}
		
		//添加成功时，重新加载数据到内存中
		MemoryVariableUtil.operatorMap = Operator.dao.loadOperatorInfo();
		
		render(RenderJson.success("添加成功！"));
	}
	
	public void delete() {
		
		String operId = getPara("operId");
		
		boolean b = Operator.dao.delete(operId);
		
		if(b) {
			
			OperRole.dao.delete(operId);
			
			//删除成功时，重新加载操作员数据到内存中
			MemoryVariableUtil.operatorMap = Operator.dao.loadOperatorInfo();
			
			render(RenderJson.success("删除成功！"));
		}else {
			render(RenderJson.error("删除失败！"));
		}
	}
	
	public void getCombobox() {
		
		List<Record> list = Operator.dao.getAllActiveOperator();
		List<TreeJson> tjs = new ArrayList<TreeJson>();
		
		TreeJson tjRoot = new TreeJson();
		tjRoot.setId("-1");
		tjRoot.setText("请选择");
		tjRoot.setPid("root");
		tjs.add(tjRoot);
		
		for(Record r:list) {
			
			TreeJson tj = new TreeJson();
			
			tj.setId(r.get("OPER_ID").toString());
			tj.setText(r.get("OPER_ID").toString() + " (" + r.get("OPER_NAME").toString() + ")");
			tj.setPid("-1");
			
			tjs.add(tj);
		}
		
		JSONArray jsonArray = JSONArray.fromObject(tjs);
		
		renderJson(jsonArray.toString());
	}
	
}
