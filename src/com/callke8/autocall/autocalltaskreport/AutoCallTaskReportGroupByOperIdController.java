package com.callke8.autocall.autocalltaskreport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.callke8.autocall.autocalltask.AutoCallTask;
import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.system.org.Org;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.NumberUtils;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动外呼任务报表-以操作员工号作为查询依据
 * 
 * @author 
 *
 */
public class AutoCallTaskReportGroupByOperIdController extends Controller implements IController {

	@Override
	public void index() {
		
		//获取并返回组织代码
		setAttr("orgComboTreeData", CommonController.getOrgComboTreeToString("1",getSession().getAttribute("currOrgCode").toString()));
		
		render("list_groupbyoperid.jsp");
	}

	@Override
	public void datagrid() {
		
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		String orgCodes = getPara("orgCodes");    //传入组织代码
		String isSearchHistoryCallTask = getPara("isSearchHistoryCallTask");
		
		String operIdList = null;    //创建的用户ID
		if(!BlankUtils.isBlank(orgCodes)) {
			operIdList = CommonController.getOperIdStringByOrgCode(orgCodes,getSession());
		}
		
		//Map m = getReportData(startTime,endTime,operIdList);
		//Map m = new HashMap();
		List<Record> dataList = getReportData(startTime,endTime,operIdList,isSearchHistoryCallTask);
		
		Map m = new HashMap();
		m.put("total",m.size());
		m.put("rows", dataList);
		
		renderJson(m);
	}
	
	/**
	 * 取得报表数据
	 * 
	 * 取得的数据列表：Record信息如下：
	 * 
	 * taskCount:   自动外呼任务的数量
	 * 
	 * state0Data   状态为0的数据量
	 * state1Data   状态为1的数据量
	 * state2Data   状态为2的数据量
	 * state3Data	状态为3的数据量
	 * state4Data   状态为4的数据量
	 * 
	 * totalCount         数据总量
	 * successCount       成功数量
	 * successRate        成功率
	 * failureCount       失败数量
	 * failureRate        失败率
	 * calledCount        已呼数量
	 * notCalledCount     未呼数量
	 * 
	 * OPER_ID_DESC    操作员
	 * ORG_NAME        组织名称
	 * 
	 * 
	 * @return
	 */
	public List<Record> getReportData(String startTime,String endTime,String operIdList,String isSearchHistoryCallTask) {
		
		//创建一个 List,根据操作员ID 对应的 任务ID列表
		List<Record> dataList = new ArrayList<Record>();
		
		//(1)根据取出任务列表
		long s = System.currentTimeMillis();
		List<AutoCallTask> taskList = AutoCallTask.dao.getAutoCallTaskListByOperIdListAndCreateTime(startTime, endTime, operIdList,isSearchHistoryCallTask);
		long e = System.currentTimeMillis();
		//System.out.println("查任务列表开始时间：" + s + ",结束时间：" + e + "，查询时长:" + (e - s));
		
		
		//如果查询回来的任务列表为空，则直接返回一个空的 dataList
		if(BlankUtils.isBlank(taskList) || taskList.size()==0) {
			return dataList;
		}
		
		
		/**
		 * 定义一个map<String,String> 用于储存操作员工号及任务ID列表
		 * 遍历后，格式如下：
		 * map.put("super","1123123,123123,123123");
		 * map.put("admin","3432234,234234");
		 */
		Map<String,String> operIdAndTaskIdListMap = new HashMap<String,String>();
		Map<String,Integer> operIdCreateTaskCountMap = new HashMap<String,Integer>();     //操作员创建外呼任务的数量
		for(AutoCallTask act:taskList) {
			
			String operId = act.getStr("CREATE_USERCODE");     //创建人工号
			String taskId = act.getStr("TASK_ID");             //取出任务ID
			
			boolean b = operIdAndTaskIdListMap.containsKey(operId);
			if(b) {     //如果前面已经存在了该工号的任务列表,则直接将新的 taskId 加入到后面
				operIdAndTaskIdListMap.put(operId, operIdAndTaskIdListMap.get(operId) + "," + taskId);
				operIdCreateTaskCountMap.put(operId, operIdCreateTaskCountMap.get(operId) + 1);
			}else {
				operIdAndTaskIdListMap.put(operId, taskId);
				operIdCreateTaskCountMap.put(operId, 1);
			}
		}
		
		//遍历operIdAndTaskIdListMap
		Iterator<Map.Entry<String,String>> entries = operIdAndTaskIdListMap.entrySet().iterator();
		while(entries.hasNext()) {
			Map.Entry<String,String> entry = entries.next();
			String operId = entry.getKey();            //得到操作员ID
			String taskIdList = entry.getValue();      //得到任务ID的列表值
			
			//创建一个数据 Record
			Record dataRecord = new Record();
			dataRecord.set("state0Data", 0);
			dataRecord.set("state1Data", 0);
			dataRecord.set("state2Data", 0);
			dataRecord.set("state3Data", 0);
			dataRecord.set("state4Data", 0);
			
			//主要返回: 已载入（state1Data）、已成功(state2Data)、待重呼(state3Data)、已失败(state4Data)、未处理(state0Data)  五种状态的数量
			AutoCallTaskTelephone.dao.getStatisticsDataForStateMultiTask(dataRecord,taskIdList,isSearchHistoryCallTask);
			
			//（1）处理外呼量的信息
			int totalCount = dataRecord.getInt("state0Data") + dataRecord.getInt("state1Data") + dataRecord.getInt("state2Data") + dataRecord.getInt("state3Data") + dataRecord.getInt("state4Data");
			int successCount = dataRecord.getInt("state2Data");      //成功数量
			int failureCount = dataRecord.getInt("state4Data");      //失败数量
			int calledCount = successCount + failureCount;      	 //已呼数量
			int notCalledCount = totalCount - calledCount;           //未外数量
			
			String successRate = NumberUtils.calculatePercent(successCount,calledCount) + "%";    //成功率
			String failureRate = NumberUtils.calculatePercent(failureCount,calledCount) + "%";    //失败率
			
			dataRecord.set("totalCount",totalCount);
			dataRecord.set("successCount", successCount);
			dataRecord.set("successRate",successRate);
			dataRecord.set("failureCount",failureCount);
			dataRecord.set("failureRate",failureRate);
			dataRecord.set("calledCount",calledCount);
			dataRecord.set("notCalledCount", notCalledCount);
			
			//（2）设置操作员信息
			Operator operator = Operator.dao.getOperatorByOperId(operId);
			dataRecord.set("OPER_ID_DESC",operator.getStr("OPER_NAME") + "(" + operId + ")"); 
			
			//（3）设置组织信息
			String orgCode = operator.getStr("ORG_CODE");                        //组织代码
			Record org = Org.dao.getOrgByOrgCode(orgCode);
			dataRecord.set("ORG_NAME",org.get("ORG_NAME"));
			
			//(4) 操作员创建外呼任务的数量
			dataRecord.set("taskCount", operIdCreateTaskCountMap.get(operId));
			
			dataList.add(dataRecord);
		}
		
		return dataList;
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
