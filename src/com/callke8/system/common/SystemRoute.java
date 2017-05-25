package com.callke8.system.common;

import com.callke8.system.dict.DictGroupController;
import com.callke8.system.dict.DictItemController;
import com.callke8.system.loginlog.LoginLogController;
import com.callke8.system.module.ModuleController;
import com.callke8.system.operationlog.OperationLogController;
import com.callke8.system.operator.OperatorController;
import com.callke8.system.org.OrgController;
import com.callke8.system.role.RoleController;
import com.callke8.system.rolegroup.RoleGroupController;
import com.callke8.system.rolemodule.RoleModuleController;
import com.jfinal.config.Routes;

public class SystemRoute extends Routes {

	@Override
	public void config() {
		add("/role",RoleController.class,"/system/role");
		add("/rolegroup",RoleGroupController.class,"/system/rolegroup");
		add("/org",OrgController.class,"/system/org");
		add("/operator",OperatorController.class,"/system/operator");
		add("/module",ModuleController.class,"/system/module");
		add("/roleModule",RoleModuleController.class,"/system/rolemodule");
		add("/loginLog",LoginLogController.class,"system/loginlog");
		add("/dictGroup",DictGroupController.class,"system/dictgroup");
		add("/dictItem",DictItemController.class,"system/dictgroup");
		add("/operationLog",OperationLogController.class,"system/operationlog");
		
		
	}

}
