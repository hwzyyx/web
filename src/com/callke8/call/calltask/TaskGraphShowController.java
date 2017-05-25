package com.callke8.call.calltask;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import com.callke8.utils.TreeJson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

/**
 * 外呼任务的图表展示类，主要是展示外呼任务的相关情况
 * 如：（1）任务的分配情况
 * 
 */
public class TaskGraphShowController extends Controller {

	public void index() {
		render("list.jsp");
	}
	
	public void getTaskCombobox() {
		
		List<Record> taskList = CallTask.dao.getAllActiveTask();
		List<TreeJson> tjs = new ArrayList<TreeJson>();
		
		TreeJson rootJ = new TreeJson();
		rootJ.setId("");
		rootJ.setText("请选择");
		//rootJ.setPid("root");
		
		tjs.add(rootJ);
		
		for(Record r:taskList) {
			TreeJson tj = new TreeJson();
			tj.setId(String.valueOf(r.get("CT_ID")));
			tj.setText(String.valueOf(r.get("TASK_NAME")));
			
			tjs.add(tj);
		}
		
		JSONArray jsonArray = JSONArray.fromObject(tjs);
		
		System.out.println("JsonArray----:" + jsonArray.toString());
		
		renderJson(jsonArray.toString());
		
	}

}
