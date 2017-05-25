package com.callke8.fastagi.autocontact;

import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class AutoContactController extends Controller implements IController {

	@Override
	public void index() {
		render("list.jsp");
	}
	
	@Override
	public void add() {
		
		AutoContact ac = getModel(AutoContact.class, "autoContact");
		
		//先看看是否有相同的识别符
		boolean checkRs = AutoContact.dao.checkIdentifier(ac,"add");
		if(checkRs) {
			render(RenderJson.error("添加失败，原因：已经存在相同的识别符!"));
			return;
		}
		
		Record record = model2Record(ac);
		
		boolean b = AutoContact.dao.add(record);
		if(b) {
			render(RenderJson.success("添加自动接触记录成功!"));
		}else {
			render(RenderJson.error("添加自动接触记录失败!"));
		}
		
	}
	
	/**
	 * 执行自动接触
	 * 
	 */
	public void exec() {
		
		String identifier = getPara("identifier");    //识别符
		
		Record record = AutoContact.dao.getAutoContactByIdentifier(identifier);
		
		if(BlankUtils.isBlank(record)) {
			render(RenderJson.error("插入自动接触记录失败!不存在识别符" + identifier + ",请查证后再试!"));
			return;
		}
		
		Record autoContactRecord = new Record();      //创建一个自动接触记录
		
		autoContactRecord.set("AGENT_NUMBER", record.get("AGENT_NUMBER"));
		autoContactRecord.set("CLIENT_NUMBER", record.get("CLIENT_NUMBER"));
		autoContactRecord.set("IDENTIFIER", record.get("IDENTIFIER"));
		autoContactRecord.set("CALLERID", record.get("CALLERID"));
		autoContactRecord.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		autoContactRecord.set("STATUS","0");

		boolean b = AutoContactRecord.dao.add(autoContactRecord);
		
		if(b) {
			//render("插入自动接触记录成功!");
			render(RenderJson.success("插入自动接触记录成功!"));
		}else {
			render(RenderJson.error("插入自动接触记录失败!"));
			//render("插入自动接触记录失败!");
		}
		
	}

	@Override
	public void datagrid() {
		
		String contactName = getPara("contactName");
		String agentNumber = getPara("agentNumber");
		String clientNumber = getPara("clientNumber");
		String identifier = getPara("identifier");
		String callerId = getPara("callerId");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		
		renderJson(AutoContact.dao.getAutoContactByPaginateToMap(page,rows,contactName,agentNumber,clientNumber,identifier,callerId));
	}

	@Override
	public void delete() {
		String id = getPara("id");
		
		if(BlankUtils.isBlank(id)) {
			render(RenderJson.error("删除时间自动接触记录失败!原因：没有选择要删除的项,请仔细检查!"));
		}
		
		boolean b = AutoContact.dao.deleteById(id);
		
		if(b) {
			render(RenderJson.success("删除自动接触记录成功!"));
		}else {
			render(RenderJson.error("删除自动接触记录失败!"));
		}
		
	}

	@Override
	public void update() {
		
		AutoContact ac = getModel(AutoContact.class,"autoContact");
		
		//先看看是否有相同的识别符
		boolean checkRs = AutoContact.dao.checkIdentifier(ac,"update");
		if(checkRs) {
			render(RenderJson.error("修改失败，原因：已经存在相同的识别符!"));
			return;
		}
		
		boolean b = AutoContact.dao.update(ac);
		
		if(b) {
			render(RenderJson.success("修改自动接触记录成功!"));
		}else {
			render(RenderJson.error("修改自动接触记录失败!"));
		}
		
	}
	
	/**
	 * 将Model转化为Record
	 * @param ac
	 * @return
	 */
	public Record model2Record(AutoContact ac) {
		
		Record r = new Record();
		
		r.set("CONTACT_NAME",ac.get("CONTACT_NAME"));
		r.set("AGENT_NUMBER",ac.get("AGENT_NUMBER"));
		r.set("CLIENT_NUMBER",ac.get("CLIENT_NUMBER"));
		r.set("IDENTIFIER",ac.get("IDENTIFIER"));
		r.set("CALLERID",ac.get("CALLERID"));
		r.set("URL_INFO",ac.get("URL_INFO"));
		r.set("MEMO",ac.get("MEMO"));
		
		return r;
	}

}
