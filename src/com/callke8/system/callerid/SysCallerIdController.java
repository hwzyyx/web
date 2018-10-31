package com.callke8.system.callerid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.callke8.autocall.autonumber.AutoNumber;
import com.callke8.common.IController;
import com.callke8.fastagi.blacklist.BlackList;
import com.callke8.system.calleridassign.SysCallerIdAssign;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.ComboboxJson;
import com.callke8.utils.NumberUtils;
import com.callke8.utils.RenderJson;
import com.callke8.utils.StringUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

import net.sf.json.JSONArray;

public class SysCallerIdController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		
		String callerId = getPara("callerId");
		String purpose = getPara("purpose");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = SysCallerId.dao.getSysCallerIdByPaginateToMap(pageNumber,pageSize,callerId,purpose);
		
		renderJson(map);
		
	}

	@Override
	public void add() {
		SysCallerId sysCallerId = getModel(SysCallerId.class,"sysCallerId");
		
		//(1)检查号码的格式
		String callerId = sysCallerId.getStr("CALLERID");
		boolean isNumber = StringUtil.isNumber(callerId);    //检查是否为纯数字
		if(!isNumber) {
			render(RenderJson.error("主叫号码非纯数字，添加主叫号码失败!"));
			return;
		}
		
		//(2)检查是否已经存在相同的主叫号码了
		SysCallerId sci = SysCallerId.dao.getSysCallerIdByCallerId(callerId);
		if(!BlankUtils.isBlank(sci)) {
			render(RenderJson.error("系统已存在相同主叫号码，添加主叫号码失败!"));
			return;
		}
		
		sysCallerId.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));
		
		SysCallerId.dao.add(sysCallerId);
		System.out.println(sysCallerId);
		
		render(RenderJson.success("添加主叫号码成功!"));
		
	}

	@Override
	public void update() {
		SysCallerId sysCallerId = getModel(SysCallerId.class,"sysCallerId");
		
		int id = sysCallerId.getInt("ID");
		String callerId = sysCallerId.getStr("CALLERID");
		String purpose = sysCallerId.getStr("PURPOSE");
		
		//检查新修改上去的号码，是否被别的记录占用
		SysCallerId sci = SysCallerId.dao.getSysCallerIdByCallerId(callerId);
		if(!BlankUtils.isBlank(sci)) {
			
			int callerId_id = sci.getInt("ID");
			
			if(id!=callerId_id) {
				render(RenderJson.error("已存在相同的主叫号码，修改号码失败!"));
				return;
			}
		}
		
		boolean b = SysCallerId.dao.update(callerId,purpose,id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		
		int id = Integer.valueOf(getPara("id"));
		
		boolean b = SysCallerId.dao.deleteById(id);
		if(b) {
			//如果删除成功，还需要删除这个号码分配给相关操作员的记录
			int count = SysCallerIdAssign.dao.deleteSysCallerIdAssignByCallerId_Id(id);
			
			render(RenderJson.success("删除成功!"));
			
		}else {
			render(RenderJson.error("删除失败!"));
		}
		
	}
	
	/**
	 * 根据当前登录的操作员ID（OPER_ID），将该操作员分配到的主叫号码，以 combobox 数据返回
	 * 
	 * @param operId
	 * 			操作员ID
	 * @param flag
	 * 			0：没有请选择; 1： 指定请选择
	 */
	public static String getSysCallerIdToComboboxByOperId(String operId,String flag) {
		
		String comboboxString = null;
		
		List<Record> sysCallerIdList = SysCallerId.dao.getAllSysCallerId();    						//取出所有的主叫号码
		List<Record> assignList = SysCallerIdAssign.dao.getSysCallerIdAssignByOperId(operId);       //取出操作员被分配到的情况
		
		List<Record> newList = new ArrayList<Record>();     //定义一个新的 list
		
		if(!BlankUtils.isBlank(sysCallerIdList) && sysCallerIdList.size()>0) {                       //主叫号码列表大于0时
			if(!BlankUtils.isBlank(assignList) && assignList.size()>0) {
				for(Record sysCallerId:sysCallerIdList) {      //遍历主叫号码
					int id = sysCallerId.getInt("ID");              		//ID
					//String callerId = sysCallerId.getStr("CALLERID");       //主叫号码
					for(Record sysCallerIdAssign:assignList) {      //再遍历分配的结果
						int callerId_Id = sysCallerIdAssign.getInt("CALLERID_ID");    //取出分配到的 ID
						if(id == callerId_Id) {
							newList.add(sysCallerId);
						}
					}
				}
			}
		}
		
		
		List<ComboboxJson> cbjs = new ArrayList<ComboboxJson>();   //定义一个TreeJson 的 list
		ComboboxJson defalutCbj = new ComboboxJson();
		if(!BlankUtils.isBlank(flag) && flag.equalsIgnoreCase("1")) {
			defalutCbj = new ComboboxJson();
			defalutCbj.setId("empty");
			defalutCbj.setText("请选择");
			
			cbjs.add(defalutCbj);
			
		}
		
		if(!BlankUtils.isBlank(newList) && newList.size()>0) {
			for(Record record:newList) {
				ComboboxJson cbj = new ComboboxJson();
				cbj.setId(record.get("ID").toString());
				cbj.setText(record.get("CALLERID").toString());
				
				cbjs.add(cbj);
			}
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cbjs);
		
		return jsonArray.toString();
		
	}

}
