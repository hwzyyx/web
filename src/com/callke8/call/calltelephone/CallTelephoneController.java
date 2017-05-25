package com.callke8.call.calltelephone;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.callke8.astutils.CallTaskCounterUtils;
import com.callke8.call.calltask.CallTask;
import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.RenderJson;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;

public class CallTelephoneController extends Controller implements IController {

	public void datagrid() {
		
		String taskId = getPara("taskId");   //得到 taskId
		String telephone = getPara("telephone");   //得到 taskId
		String clientName = getPara("clientName");  
		String state = getPara("state");   //得到 taskId
		String startTime = getPara("telephoneStartTime");
		String endTime = getPara("telephoneEndTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0) {
			page=1;
		}
		
		renderJson(CallTelephone.dao.getCallTelephoneByPaginateToMap(page, rows, taskId,telephone,clientName,state,startTime,endTime));
	}
	
	/**
	 * 下载模板
	 */
	public void template() {
		
		String uploadDir = File.separator + "upload" + File.separator;    //保存路径
		
		String path_tmp = PathKit.getWebRootPath() + uploadDir;
		
		File file = new File(path_tmp + "demo.xls");
		
		renderFile(file);
		
	}
	
	public void delete() {
		Integer taskId = Integer.valueOf(getPara("taskId"));    //收集传入的任务ID
		String ids = getPara("ids");          //要删除的号码的ID
		
		int idCount = 0;    //定义传入的ID的数量为0,如果传入的id 数量大于0时，才执行操作处理
		
		if(!BlankUtils.isBlank(ids)) {
			String[] idList = ids.split(",");
			idCount = idList.length;
		}
		
		System.out.println("传入的号码ID的数量为:" + idCount);
		
		if(idCount>0) {   //即当有删除成功时，那肯定任务的号码的数量是有变化的，包括号码总数，已分配数量，呼叫成功，呼叫失败  这些都需要重新统计
			updateCallTaskCounter(taskId,ids);    
		}
		int count = CallTelephone.dao.batchDelete(ids);
		
		render(RenderJson.success("成功删除数据量为:" + count));
	}
	
	/**
	 * 根据传入的 taskId, 及要删除的 ids,更新当前任务的计数情况 
	 * 
	 * @param taskId
	 * @param ids
	 */
	public void updateCallTaskCounter(int taskId,String ids) {
		
		Map<Integer,Integer> stateMap = new HashMap<Integer,Integer>();
		
		if(!BlankUtils.isBlank(ids)) {
			
			String[] idList = ids.split(",");    //以逗号分隔ID
			
			for(String id:idList) {
				Record ct = CallTelephone.dao.getCallTelephoneById(id);    //根据传入的 id, 从数据库取出记录   
				
				if(BlankUtils.isBlank(ct)) {    //如果查出来的记录为空时,继续下一轮
					continue;
				}
				
				Integer callState = Integer.valueOf(ct.getStr("STATE"));    
				
				if(stateMap.containsKey(callState)) {     //如果已经包含了当前状态的记录，则在这个基础上增加一个
					stateMap.put(callState,stateMap.get(callState) + 1);
				}else {
					stateMap.put(callState, 1);
				}
			}
			
			//遍历 map, 并根据情况修改任务的计数
			Iterator<Map.Entry<Integer, Integer>> entries = stateMap.entrySet().iterator();
			while(entries.hasNext()) {
				
				Map.Entry<Integer, Integer> entry = entries.next(); 
				
				Integer state = entry.getKey();        //得到状态
				Integer count = entry.getValue();      //得到当关状态的数量
				
				if(count >0 ) {     //只有数量大于0，才有更改计数的必要
					
					if(state == 0) {    //如果状态为0，即是状态为新号码时
						CallTaskCounterUtils.reduceCounter(taskId, "0", count);       //将状态为新号码的减掉偏移量
						CallTaskCounterUtils.reduceCounter(taskId, "total", count);   //将号码总数的号码减掉偏移量
					}else if(state == 1) {   //如果状态为1, 即是状态为已分配的
						CallTaskCounterUtils.reduceCounter(taskId, "1", count);       //将状态为已分配的减掉偏移量
						CallTaskCounterUtils.reduceCounter(taskId, "total", count);   //将号码总数的号码减掉偏移量
					}else if(state > 1) {   //如果状态为2,即是呼叫成功的被删除
						CallTaskCounterUtils.reduceCounter(taskId, "1", count);       //将状态为已分配的减掉偏移量
						CallTaskCounterUtils.reduceCounter(taskId, "'" + state + "'", count);       //将状态为已经成功的的减掉偏移量
						CallTaskCounterUtils.reduceCounter(taskId, "total", count);   //将号码总数的号码减掉偏移量
					}
					
				}
				
				
			}
			
		}
		
		
	}

	@Override
	public void add() {
		
	}

	@Override
	public void index() {
		
	}

	@Override
	public void update() {
		
	}
	
}	
