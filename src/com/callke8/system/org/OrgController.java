package com.callke8.system.org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.system.role.Role;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.DwzRenderJson;
import com.callke8.utils.RenderJson;
import com.callke8.utils.TreeHelper;
import com.callke8.utils.TreeJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class OrgController extends Controller implements IController {
	
	TreeHelper th = null;
	
	public void index() {
		render("list.jsp");
	}
	
	public void tree() {
		List<Record> orgList = Org.dao.getAllOrg();   //查询得到所有的组织
		List<TreeJson> tjs = new ArrayList<TreeJson>();   //定义一个TreeJson 的 list
		
		int rootIndex = 0;
		int i = 0;
		
		for(Record r:orgList) {
			
			TreeJson tj = new TreeJson();
			tj.setId(r.get("ORG_CODE").toString());
			tj.setText(r.get("ORG_NAME").toString());
			tj.setPid(r.get("PARENT_ORG_CODE").toString());
			tj.setDesc(r.get("ORG_DESC").toString());
			
			//tj.setState("closed");
			if(r.get("PARENT_ORG_CODE").toString().equalsIgnoreCase("-1")) {
				rootIndex = i;
				//tj.setChecked(true);
			}
			tjs.add(tj);
			
			i++;
		}
		
		if(rootIndex != 0) {
			
			TreeJson firstNode = tjs.get(0);
			TreeJson rootNode = tjs.get(rootIndex);
			
			tjs.set(0, rootNode);
			tjs.set(rootIndex, firstNode);
		}
		
		List<TreeJson> results = TreeJson.formatTree(tjs);
		
		JSONArray jsonArray = JSONArray.fromObject(results);
		//System.out.println("----" + jsonArray.toString());
		renderJson(jsonArray.toString());
	}
	
	/**
	 * 根据操作员的ID，取得操作员所在ID以下的所有的部门的树形
	 */
	public void treeFromOperId() {
		
		String operId = getPara("operId");    //先取得
		String orgCode = Operator.dao.getOrgCodeByOperId(operId);   //再根据传入的 operId 取得操作员所在的组织编码
		Record org = Org.dao.getOrgByOrgCode(orgCode);              //取出操作员所在的组织的记录
		String parentOrgCode = org.get("PARENT_ORG_CODE");          //取出当前组织的父代码
		
		List<Record> orgList = Org.dao.getAllOrg();          //查询得到所有的组织
		List<Record> newOrgList = new ArrayList<Record>();   //组织一个新的list,用于储存以当前操作员所在的组织往下的组织
		
		String nodePoc = parentOrgCode;   //当前操作员的子节点的父节点,默认的就是当前组织的父组织码
		String nodeOc = orgCode;          //当前操作员的子节点的组织码,默认的就是当前的组织码
		
		while (!BlankUtils.isBlank(nodeOc)){    //当不为空时
			for(Record r:orgList) {
				String oc = r.get("ORG_CODE");               //取出当前记录的组织码
				String poc = r.get("PARENT_ORG_CODE");       //取出当前记录的父组织码
				
				//第一次循环，是取出当前操作员所在的组织的记录
				//第二次循环，是取出当前操作员组织的子节点的记录
				if(oc.equals(orgCode)) {                     
					
					newOrgList.add(r);
					boolean b = false;
					for(Record r2:orgList) {   //再次循环时，主要是查询当前的这条记录，是否有子节点
						
						String oc2 = r.get("ORG_CODE");               //取出当前记录的组织码
						String poc2 = r.get("PARENT_ORG_CODE");       //取出当前记录的父组织码
						
						if(poc2.equals(oc)) {         //如果有记录的父组织为当前的组织码，表示还有子结点
							b = true;
							continue;
						}
					}
					
					if(b) {    //如果没有子节点时，就要将
						
					}
					
				}
				
			}
		}
		
	}
	
	/**
	 * 根据传入的当前组织码，然后根据当前的组织码，找到所有的父组织代码及更高层的父组织码
	 * 
	 * @param org
	 * 			传入当前记录
	 * @param orgList
	 * 			所有的组织记录
	 * @return
	 */
	public List<String> getAllParentOrgCodes(Org org,List<Record> orgList) {
		
		String orgCode = org.get("ORG_CODE");                  //取出当前的组织码
		String parentCode = org.get("PARENT_ORG_CODE");        //取出当前的父组织码
		List<Record> newOrgList = orgList;                     //先复制一个列表出来，在循环之前，要先去除本记录先
		boolean b = newOrgList.remove(org);
		
		List<String> parentOrgCodes = new ArrayList<String>();
		String nextOrgCode = parentCode;                      //先设定下次要循环时要找到组织码，初始为当前父组织码
		
		while(!BlankUtils.isBlank(nextOrgCode)) {             //直到没有下一个要查找组织码才停止
			
			for(Record r:orgList) {           //循环
				String oc = r.get("ORG_CODE");
				String poc = r.get("PARENT_ORG_CODE");
				
				if(!(orgCode.equals(oc)&&parentCode.equals(parentCode))) {
					
				}
				
			}
			
		}
		
		
		return parentOrgCodes;
	}
	
	public void show(){
		String moduleCode = getPara("moduleCode");  //得到组织代码
		//System.out.println("得到参数代码：" + moduleCode);
		
		if(BlankUtils.isBlank(moduleCode)) {        //如果为空时,将查询以 parentOrgCode 为 -1 的 orgCode 
			
			moduleCode = th.getRootModuleCode();
			
		}
		//System.out.println("得到参数代码：" + moduleCode);
		
		//查询子组织
		List<Record> orgs = Org.dao.getOrgByParentOrgCode(moduleCode);
		Map map = new HashMap();
		map.put("total", orgs.size());
		map.put("rows", orgs);
		
		renderJson(map);
	}
	
	public void update() {
		Org org = getModel(Org.class,"org");
		System.out.println("orgCode:" + org.get("ORG_CODE"));
		System.out.println("orgName:" + org.get("ORG_NAME"));
		System.out.println("orgDesc:" + org.get("ORG_DESC"));
		//System.out.println("修改时提交的orgCode = " + org.get("ORG_CODE"));
		
		boolean b = Org.dao.update(org);
		
		
		if(b) {
			render(RenderJson.success("组织修改成功!"));
			return;
		}else {
			render(RenderJson.error("组织修改失败"));
			return;
		}
	}
	
	public void add() {
		Org org = getModel(Org.class,"org");
		org.set("ORG_TYPE_CODE", "1");
		
		//判断是否有相同的组织代码
		String orgCode = org.get("ORG_CODE");
		Record chkOrg = Org.dao.getOrgByOrgCode(orgCode);
		
		if(!BlankUtils.isBlank(chkOrg)) {    /** 如果根据角色代码查询结果不为空，返回错误 */
			render(RenderJson.warn("添加失败:已经存在相同的组织代码！"));
			return;
		}
		
		Org.dao.add(org);
		
		render(RenderJson.success("组织保存成功!"));
		
	}
	
	public void delete() {
		
		String orgCode = getPara("orgCode");
		
		//得到需要删除的 OrgCode 
		//System.out.println("得到需要删除的OrgCode:" + orgCode);
		
		//先判断当前 orgCode 下，是否有其他的子组织，如果有子组织，则禁止删除
		List<Record> list = Org.dao.getOrgByParentOrgCode(orgCode);
		if(list.size()>0) {   //即是如果有子组织，则禁止删除
			render(RenderJson.warn("删除失败，该组织下还有子组织，不允许删除!"));
			return;
		}
		
		//再判断该组织下，是否还有操作员，如果有操作员，也不允许删除
		boolean b = Operator.dao.isHasOperatorByOrgCode(orgCode);
		if(b) {
			render(RenderJson.warn("删除失败，该组织下有操作员，不允许删除!"));
			return;
		}
		
		Org.dao.deleteByOrgCode(orgCode);
		
		render(RenderJson.success("删除组织成功!"));
		
	}

	@Override
	public void datagrid() {
		
	}
	
}
