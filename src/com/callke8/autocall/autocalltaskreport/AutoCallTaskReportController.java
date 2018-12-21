package com.callke8.autocall.autocalltaskreport;

import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.remindertype.SysReminderTypeController;
import com.callke8.system.tasktype.SysTaskTypeController;
import com.jfinal.core.Controller;

public class AutoCallTaskReportController extends Controller implements IController {

	@Override
	public void index() {
		String currOrgCode = getSession().getAttribute("currOrgCode").toString();
		String currOperId = String.valueOf(getSession().getAttribute("currOperId"));     //取出当前登录的用户
		
		//获取并返回组织代码
		setAttr("orgComboTreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		setAttr("allTaskTypeComboboxDataFor0",SysTaskTypeController.getAllSysTaskTypeToCombobox("0"));
		setAttr("allTaskTypeComboboxDataFor1",SysTaskTypeController.getAllSysTaskTypeToCombobox("1"));
		
		setAttr("allReminderTypeComboboxDataFor0",SysReminderTypeController.getAllSysReminderTypeToCombobox("0"));
		setAttr("allReminderTypeComboboxDataFor1",SysReminderTypeController.getAllSysReminderTypeToCombobox("1"));
		
		setAttr("taskStateComboboxDataFor0", CommonController.getComboboxToString("AC_TASK_STATE","0"));
		setAttr("taskStateComboboxDataFor1", CommonController.getComboboxToString("AC_TASK_STATE","1"));
		
		//短信状态 combobox , 用于客户号码的页面搜索用
		setAttr("messageStateComboboxDataFor1", CommonController.getComboboxToString("COMMON_MESSAGE_STATE","1"));
		
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		
	}

	@Override
	public void add() {
		
	}

	@Override
	public void update() {
		
	}

	@Override
	public void delete() {
		
	}

}
