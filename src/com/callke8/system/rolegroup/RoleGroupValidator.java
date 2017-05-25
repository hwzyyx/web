package com.callke8.system.rolegroup;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class RoleGroupValidator extends Validator {


	@Override
	protected void validate(Controller c) {
		validateRequiredString("groupCode", "groupCodeMsg", "角色组不能为空");
		validateRequiredString("groupName", "groupNameMsg", "角色组名称不能为空");
	}

	@Override
	protected void handleError(Controller c) {
		c.keepPara("groupCode");
		//c.keepPara("groupName");
		
		String actionKey = getActionKey();
		
		c.render("content.jsp");
		
		/*if (actionKey.equals("/blog/save"))
			c.render("add.jsp");
		else if (actionKey.equals("/blog/update"))
			c.render("edit.jsp");*/
		
	}
}
