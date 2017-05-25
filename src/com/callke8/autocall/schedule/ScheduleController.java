package com.callke8.autocall.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.history.AutoCallTaskHistory;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.ComboboxJson;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class ScheduleController extends Controller implements IController {

	@Override
	public void index() {
		
		//日期类型combobox数据返回,有两一个，一个是带请选择，一个不带选择
		setAttr("dateTypeComboboxDataFor0", CommonController.getComboboxToString("DATETYPE","0"));
		setAttr("dateTypeComboboxDataFor1", CommonController.getComboboxToString("DATETYPE","1"));
		
		render("list.jsp");
	}
	
	/**
	 * 保存添加时间调度方案
	 */
	@Override
	public void add() {
		Schedule schedule = getModel(Schedule.class, "schedule");
		
		
		//自动生成ID，主要是以时间：年月日 + 随机四位数
		String scheduleId = DateFormatUtils.formatDateTime(new Date(), "yyyyMMddHHmm") + Math.round(Math.random()*9000 + 1000);
		schedule.set("SCHEDULE_ID", scheduleId);
		
		//设置最大时间项
		int maxTimeItem = getParaToInt("maxTimeItem");
		schedule.set("MAXTIMEITEM", maxTimeItem);
		
		int dateType = schedule.getInt("DATETYPE");  //日期类型：1表示每天；2表示星期
		String[] weeks = getParaValues("week");
		String dateTypeDetail = null;                //日期类型详情，如果日期类型为星期时才有
		
		if(dateType == 2) {   //如果时间类型为星期
			dateTypeDetail = "";
			for(String weekDay:weeks) {
				dateTypeDetail += weekDay + ",";
			}
			
			dateTypeDetail = dateTypeDetail.substring(0, dateTypeDetail.length()-1);
			schedule.set("DATETYPE_DETAIL", dateTypeDetail);
			
		}
		
		//设置操作工号
		String operId = String.valueOf(getSession().getAttribute("currOperId"));
		schedule.set("CREATE_USERCODE", operId);
		//设置操作工号所在的组织编码
		schedule.set("ORG_CODE",Operator.dao.getOrgCodeByOperId(operId));
		
		//设置创建时间
		schedule.set("CREATETIME", DateFormatUtils.getCurrentDate());
		
		boolean b = Schedule.dao.add(schedule);
		
		if(b) {
			render(RenderJson.success("插入时间调度计划成功！"));
		}else {
			render(RenderJson.error("插入时间调度计划失败！"));
		}
		
	}

	@Override
	public void datagrid() {
		System.out.println("取ScheduleController datagrid的开始时间:" + DateFormatUtils.getTimeMillis());
		String orgCode = getPara("orgCode");
		String scheduleName = getPara("scheduleName");
		String dateType = getPara("dateType");
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = Schedule.dao.getScheduleByPaginateToMap(pageNumber,pageSize,scheduleName,dateType,orgCode);
		
		System.out.println("取ScheduleController datagrid的结束时间:" + DateFormatUtils.getTimeMillis());
		renderJson(map);
	}

	/**
	 * 以combobox 的形式取出调度计划，用于创建任务时被选择
	 */
	public void getCombobox() {
		
		String flag = getPara("flag");   //获取参数 flag,为1 时，表示需要返回请选择，如果非1时，则不需要返回请选择
		
		List<Record> list = Schedule.dao.getAllSchedules();
		
		List<ComboboxJson> cList = new ArrayList<ComboboxJson>();
		
		if(!BlankUtils.isBlank(flag)&&flag.equalsIgnoreCase("1")) {
			ComboboxJson cj = new ComboboxJson();
			cj.setId("");
			cj.setText("请选择");
			
			cList.add(cj);
		}
		
		for(Record r:list) {
			
			ComboboxJson cj = new ComboboxJson();
			
			cj.setId(r.getStr("SCHEDULE_ID"));
			cj.setText(r.getStr("SCHEDULE_NAME"));
			cList.add(cj);
		}
		
		JSONArray jsonArray = JSONArray.fromObject(cList);
		
		renderJson(jsonArray.toString());
	}
	
	@Override
	public void delete() {
		
		String scheduleId = getPara("scheduleId");
		
		if(BlankUtils.isBlank(scheduleId)) {
			render(RenderJson.error("删除时间调度计划失败!原因：没有选择要删除的项,请仔细检查!"));
		}
		
		//删除之前，先判断当前调度是否已被外呼任务使用
		boolean isBeUsed = AutoCallTask.dao.checkScheduleBeUsed(scheduleId);
		if(isBeUsed) {
			render(RenderJson.error("删除失败,要删除的记录已经被自动外呼任务引用,不允许删除!"));
			return;
		}
		
		isBeUsed = AutoCallTaskHistory.dao.checkScheduleBeUsed(scheduleId);
		if(isBeUsed) {
			render(RenderJson.error("删除失败,要删除的记录已经被历史外呼任务引用,不允许删除!"));
			return;
		}
		
		boolean b = Schedule.dao.deleteByScheduleId(scheduleId);
		
		if(b) {
			render(RenderJson.success("删除时间调度计划成功!"));
		}else {
			render(RenderJson.error("删除时间调度计划失败!"));
		}
		
	}

	public void getSchedule() {
		
		String scheduleId = getPara("scheduleId");
		
		Schedule schedule = Schedule.dao.getScheduleById(scheduleId);
		
		renderJson(schedule);
	}
	
	@Override
	public void update() {
		
		Schedule schedule = getModel(Schedule.class,"schedule");
		
		//设置最大时间项
		int maxTimeItem = getParaToInt("maxTimeItem");
		schedule.set("MAXTIMEITEM", maxTimeItem);
		
		int dateType = schedule.getInt("DATETYPE");  //日期类型：1表示每天；2表示星期
		String[] weeks = getParaValues("week");
		String dateTypeDetail = null;                //日期类型详情，如果日期类型为星期时才有
		
		if(dateType == 2) {   //如果时间类型为星期
			dateTypeDetail = "";
			for(String weekDay:weeks) {
				dateTypeDetail += weekDay + ",";
			}
			
			dateTypeDetail = dateTypeDetail.substring(0, dateTypeDetail.length()-1);
			schedule.set("DATETYPE_DETAIL", dateTypeDetail);
		}else if(dateType == 1) {
			schedule.set("DATETYPE_DETAIL", null);
		}
		
		boolean b = Schedule.dao.update(schedule);
		
		if(b) {
			render(RenderJson.success("修改调度计划成功!"));
		}else {
			render(RenderJson.error("修改调度计划失败!"));
		}

	}

}
