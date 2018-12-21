package com.callke8.system.ipaddress;

import java.util.*;
import com.callke8.common.IController;
import com.callke8.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONArray;

public class SysIpAddressController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));	//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量

		String ipAddress = getPara("ipAddress");
		String memo = getPara("memo");

		Map map = SysIpAddress.dao.getSysIpAddressByPaginateToMap(pageNumber,pageSize,ipAddress,memo);
		renderJson(map);
	}

	@Override
	public void add() {
		SysIpAddress formData = getModel(SysIpAddress.class,"sys_ip_address");
		
		String ipAddress = formData.getStr("IP_ADDRESS");
		String memo = formData.getStr("MEMO");
		
		if(!StringUtil.isIP(ipAddress)) {
			render(RenderJson.error("新增失败,IP地址的格式不正确!"));
			return;
		}
		
		//（1）检查是否已经存在相同的 IP 地址
		SysIpAddress sia = SysIpAddress.dao.getSysIpAddressByIpAddress(ipAddress);
		if(!BlankUtils.isBlank(sia)) {
			render(RenderJson.error("修改失败,系统已经存在相同的IP地址!"));
			return;
		}
		
		//（2）检查是否已经存在相同的备注
		SysIpAddress sia2 = SysIpAddress.dao.getSysIpAddressByMemo(memo);
		if(!BlankUtils.isBlank(sia2)) {
			render(RenderJson.error("修改失败,系统已经存在相同的备注!"));
			return;
		}
		
		formData.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));

		boolean b = SysIpAddress.dao.add(formData);
		if(b) {
			render(RenderJson.success("新增记录成功!"));
		}else {
			render(RenderJson.error("新增记录失败!"));
		}
	}

	@Override
	public void update() {
		SysIpAddress formData = getModel(SysIpAddress.class,"sys_ip_address");

		int id = formData.get("ID");
		String ipAddress = formData.get("IP_ADDRESS");
		String memo = formData.get("MEMO");
		
		if(!StringUtil.isIP(ipAddress)) {
			render(RenderJson.error("修改失败,IP地址的格式不正确!"));
			return;
		}
		
		//(1)判断是否已经删除
		SysIpAddress sia = SysIpAddress.dao.getSysIpAddressById(id);
		if(BlankUtils.isBlank(sia)) {
			render(RenderJson.error("修改失败,当前的记录已经删除了!"));
			return;
		}
		
		
		//(2)判断是否已经存在相同的IP地址
		SysIpAddress sia2 = SysIpAddress.dao.getSysIpAddressByIpAddress(ipAddress);
		if(!BlankUtils.isBlank(sia2)) {
			if(id!=sia2.getInt("ID")) {
				render(RenderJson.error("修改失败,系统已经存在相同的IP地址!"));
				return;
			}
		}
		
		//(3)判断是否已经存在相同中的备注
		SysIpAddress sia3 = SysIpAddress.dao.getSysIpAddressByMemo(memo);
		if(!BlankUtils.isBlank(sia3)) {
			if(id!=sia3.getInt("ID")) {
				render(RenderJson.error("修改失败,系统已经存在相同的备注!"));
				return;
			}
		}

		boolean b = SysIpAddress.dao.update(ipAddress,memo,id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		String id = getPara("id");

		boolean b = SysIpAddress.dao.deleteById(id);
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}
	
	/**
	 * IP地址拦截
	 */
	public void ipAddressIntercept() {
		
		setAttr("clientIpAddress",getRequest().getRemoteAddr());
		System.out.println("执行到了 ipAddressIntercept()......");
		render("_ipaddress_intercept.jsp");
	}
	
}
