package com.callke8.call.calltask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.callke8.astutils.CallTaskCounterUtils;
import com.callke8.call.calltelephone.CallTelephone;
import com.callke8.common.IController;
import com.callke8.system.operator.Operator;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.callke8.utils.RenderJson;
import com.callke8.utils.StringUtil;
import com.callke8.utils.TreeJson;
import com.callke8.utils.XLSUtils;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

public class CallTaskController extends Controller implements IController {

	public void index() {
		render("list.jsp");
	}
	
	public void datagrid() {
		
		String taskName = getPara("taskName");
		String taskType = "1";
		String taskState = getPara("taskState");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		int rows = Integer.valueOf(getPara("rows"));
		int page = Integer.valueOf(getPara("page"));
		if(page==0){page=1;}
		
		renderJson(CallTask.dao.getCallTaskByPaginateToMap(page, rows, taskName, taskType, taskState, startTime, endTime));
	}
	
	/**
	 * 新增外呼任务
	 */
	public void add(){
		Record callTask = new Record();
		CallTask ct = getModel(CallTask.class, "calltask");
		
		callTask.set("TASK_NAME",ct.get("TASK_NAME"));                             //设置项目名称
		callTask.set("CALLERID", ct.get("CALLERID"));                              //设置主叫号码
		callTask.set("TASK_TYPE","1");                             //设置任务类型
		
		callTask.set("CREATE_TIME", DateFormatUtils.getCurrentDate());             //设置创建时间
		callTask.set("CREATE_USERCODE",getSession().getAttribute("currOperId"));   //设置创建人
		callTask.set("TASK_STATE", "0");                                           //设置状态：  0 为新建
		
		int id = CallTask.dao.add(callTask);    //返回ID
		
		if(id>0) {    //表示添加成功
			System.out.println("添加外呼任务成功，返回的记录ID为:" + id);
			render(RenderJson.success("添加任务成功!"));
		}else {
			render(RenderJson.error("添加任务失败!"));
		}
		
	}
	
	public void update() {
		CallTask ct = getModel(CallTask.class,"setuptask");
		
		boolean b = CallTask.dao.update(ct);
		
		if(b) {
			render(RenderJson.success("修改任务成功!"));
		}else {
			render(RenderJson.error("修改任务失败!"));
		}
	}
	
	/**
	 * 单个添加号码
	 */
	public void addTelephone() {
		Record phone = new Record();
		CallTelephone cp = getModel(CallTelephone.class, "telephone");
		int taskId = Integer.valueOf(getPara("taskId"));   //取得 taskId
		
		String phoneNumber = cp.get("TELEPHONE");
		//先判断号码是否符合规则，即是否全部由数字组成,且号码长度为 7 及以上，12位以下
		if(BlankUtils.isBlank(phoneNumber) || phoneNumber.length() < 7 || phoneNumber.length() > 12) {
			render(RenderJson.error("添加失败:号码长度不对,号码长度须在7-12位数字!"));
			return;
		}
		
		if(!StringUtil.isNumber(phoneNumber)) {   //如果号码非数字时，也跳出判断
			render(RenderJson.error("添加失败:号码非数字!"));
			return;
		}
		
		phone.set("CT_ID",taskId);
		phone.set("TELEPHONE", cp.get("TELEPHONE"));
		phone.set("CLIENT_NAME", cp.get("CLIENT_NAME"));
		phone.set("CLIENT_SEX", BlankUtils.isBlank(cp.get("CLIENT_SEX"))?1:cp.get("CLIENT_SEX"));
		phone.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		phone.set("CREATE_USERCODE",getSession().getAttribute("currOperId"));
		phone.set("STATE","0");  //号码状态:  0新添加号码  1被分配  2已经呼叫且成功,3已经呼叫，但失败
		
		boolean b = CallTelephone.dao.add(phone);
		if(b) {
			//在号码添加成功后，需要修改该任务的数量情况，总号码的状态值 total, 新号码的状态值为 0
			CallTaskCounterUtils.increaseCounter(taskId, "total", 1);    //总号码增加1个
			CallTaskCounterUtils.increaseCounter(taskId, "0", 1);        //新号码增加1个
			
			render(RenderJson.success("添加号码成功!"));
		}else {
			render(RenderJson.error("添加失败!"));
		}
	}
	
	@SuppressWarnings("static-access")
	public void uploadFile() {
		
		//先判断文件大小限制
		int fileSize = getRequest().getContentLength();   //得到上传文件的大小, 由于jfinal默认最大的上传的大小为 10 * 1024 * 1024 即是 10M
		if(fileSize > (10 * 1024 * 1024)) {    //如果大于 10M 时，返回错误
			render(RenderJson.error("上传失败：上传文件过大，已经超过 10M"));
			return;
		}
		
		//获取上传的文件
		String path_tmp = "";
		String uploadDir = File.separator + "upload" + File.separator;    //保存路径
		
		path_tmp = PathKit.getWebRootPath() + uploadDir;
		UploadFile uf = getFile("phoneFile",path_tmp);
		
		
		//得到上传文件的类型
		String mimeType = null;
		
		mimeType = StringUtil.getExtensionName(uf.getFileName());   //先通过分割文件名，得到到文件的扩展名，并进行判断
		
		if(BlankUtils.isBlank(mimeType) || (!mimeType.equalsIgnoreCase("xls") && !mimeType.equalsIgnoreCase("xlsx"))) {    ////如果扩展名为空或是不等于 xls，不等于 xlsx 或是不等于 txt 时，则不进行上传
			if(!BlankUtils.isBlank(uf.getFile()) && uf.getFile().exists()) {   //如果存在时，删除先删除
				uf.getFile().delete();
			}
			render(RenderJson.error("上传失败：文件的类型不正确"));
			return;
		}
		
		//前面通过了扩展名判断文件的类型后，还需要进一步判断，目的是过滤故意通过修改扩展名进行上传
		//暂时不作判断，需要后期再实现
		
		//重命名文件名，因为文件名可能是中文的文件名，不利于读取，需要先重命名为数字名
		String renameFileName = DateFormatUtils.getTimeMillis() + "." + mimeType;
		File newFile = new File((path_tmp + renameFileName));
		uf.getFile().renameTo(newFile);
		
		int taskId = Integer.valueOf(getPara("taskId"));   //获取参数 taskId
		
		//读取文件的内容
		System.out.println(path_tmp + renameFileName);
		List<Record> list = XLSUtils.readXls(newFile);   //读取文件内容
		ArrayList<Record> listResult = new ArrayList<Record>();                   //新建一个list,用于储存数据到数据库
		
		//遍历数据，并重新组合 record，存入数据库
		for(int i=1;i<list.size();i++) {               //由于第0行为表头，固以1为开始
			Record r = list.get(i);
			
			String telephone = r.get("0");             //得到号码
			String clientName = r.get("1");            //得到姓名
			String sex = r.get("2");                   //得到性别
			
			//先判断号码是否符合规则，即是否全部由数字组成,且号码长度为 7 及以上，12位以下
			if(BlankUtils.isBlank(telephone) || telephone.length() < 7 || telephone.length() > 12) {
				continue;
			}
			
			if(!StringUtil.isNumber(telephone)) {   //如果号码非数字时，也跳出判断
				continue;
			}
			
			//当号码为正常的号码时，还需要注意，号码的前缀 0 可能已经被删除,需要重新处理
			telephone = StringUtil.getPhoneNumber4XlsFormat(telephone);
			
			//根据上面的值，重新组合record，并插入数据库
			Record nr = new Record();
			nr.set("CT_ID",taskId);
			nr.set("TELEPHONE", telephone);
			nr.set("CLIENT_NAME", clientName);
			if(!BlankUtils.isBlank(sex)) {
				nr.set("CLIENT_SEX", !sex.equalsIgnoreCase("男")?"0":"1");
			}else {
				nr.set("CLIENT_SEX", "1");
			}
			nr.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
			nr.set("CREATE_USERCODE",getSession().getAttribute("currOperId"));
			nr.set("STATE","0");  //号码状态:  0新添加号码  1被分配  2已经呼叫且成功,3已经呼叫，但失败
			
			listResult.add(nr);
		}
		System.out.println("总的数量为:" + list.size() + ",处理后的数量为:" + listResult.size());
		int successCount = CallTelephone.dao.batchSave(listResult);
		
		newFile.delete();  //同时，将数据文件删除
		int totalCount = (list.size()-1)>0?(list.size()-1):0;
		CallTaskCounterUtils.increaseCounter(taskId, "total", successCount);    //更新号码总量及新号码码总量    
		CallTaskCounterUtils.increaseCounter(taskId, "0", successCount);        //更新号码总量及新号码码总量
		render(RenderJson.success("上传号码总数为:" + totalCount + ",本次成功上传号码数量为:" + successCount));
		//同时，需要更改，任务的号码总量及未呼号码总量
	}
	
	/**
	 * 用于对任务进行授权用的操作员树形列表
	 */
	public void opertorTree() {
		
		List<Record> list = Operator.dao.getAllActiveOperator();
		List<TreeJson> tjs = new ArrayList<TreeJson>();   //定义一个TreeJson 的 list
		
		//由于菜单的树形是多根节点，所以需要先构建一个虚拟的根节点
		TreeJson tjRoot = new TreeJson();
		tjRoot.setId("-1");
		tjRoot.setText("请选择工号进行授权");
		tjRoot.setPid("root");
		tjs.add(tjRoot);
		
		for(Record r:list) {
			
			TreeJson tj = new TreeJson();
			
			tj.setId(r.get("OPER_ID").toString());
			tj.setText(r.get("OPER_ID").toString() + " (" + r.get("OPER_NAME").toString() + ")");
			tj.setPid("-1");
			
			tjs.add(tj);
		}
		
		List<TreeJson> results = TreeJson.formatTree(tjs);
		JSONArray jsonArray = JSONArray.fromObject(results);
		
		renderJson(jsonArray.toString());
	}
	
	/**
	 * 根据 taskId 取得已经授权的数据
	 */
	public void getAuthData() {
		
		String taskId = getPara("taskId");
		
		List<Record> list = CallTaskAuth.dao.getCallTaskAuthByTaskId(taskId);
		
		String operIds = "";
		
		for(Record r:list) {
			operIds += r.get("OPER_ID") + ",";
		}
		
		render(RenderJson.success(operIds));
	}
	
	/**
	 * 任务授权
	 */
	public void auth() {
		String operIds = getPara("ids");
		String taskId = getPara("taskId");
		
		
		CallTaskAuth.dao.cancelAuth(taskId);
		int count = CallTaskAuth.dao.batchSave(operIds,taskId);
		
		render(RenderJson.success("授权成功"));
	}
	
	/**
	 * 更改任务的状态
	 */
	public void changeState() {
		int taskId = Integer.valueOf(getPara("taskId"));   //任务ID
		String state = getPara("state");     //任务的当前状态：  state: 0 新任务;1已经启动;3:暂停;4:停止;5 已经完成
		int type = Integer.valueOf(getPara("type"));       //更改状态类型：  type：1启动; 2暂停; 3停止; 4删除任务; 5标注为历史
		
		switch(type) {
		
			case 1:                         //启动操作
			{
				//启动的操作，只需要将状态更改即可
				//任务启动后，已经授权的工号，就可以在任务执行模块显示并执行该任务
				CallTask.dao.updateState(taskId, "1");   
			}
			break;
			case 2:	                        //暂停操作
			{
				//暂停任务的操作，只需要更改任务的状态即可
				//任务暂停后，已经授权的工号，在任务执行模块是无法显示或是执行该任务的，即使是已经打开后，也无法执行该任务
				CallTask.dao.updateState(taskId, "2");
			}
			break;
			case 3:                         //停止操作
			{
				//停止操作时
				//需要将已经请求的数据收回
				CallTask.dao.updateState(taskId, "3");
				int count = CallTelephone.dao.reuse(taskId);   //回收已经分配，但是未外呼的数据
				
				if(count>0) {   //如果将已经分配，但是未外呼的数据收回后，需要调整当前任务的各种状态
					CallTaskCounterUtils.reduceCounter(taskId, "1", count);    //将已经分配的数量减少
					CallTaskCounterUtils.increaseCounter(taskId, "0", count);    //将新号码的数量增加
				}
				
			}
			break;
			case 4:                         //删除操作 
			{
				//CallTask.dao.updateState(taskId, "3");
				//删除操作时
				//直接将任务及数据全部删除，无论是否已经分配。
				CallTask.dao.delete(taskId);    //删除任务，在删除任务的方法里，已经自动将当前任务的号码一并删除
			}
			break;
			case 5:                         //标注为历史
			{
				CallTask.dao.updateState(taskId, "5");
			}
			break;
			case 6:                         //回收已经分配的号码
			{
				//回收号码已经，不需要更改用任务的状态，只需要将号码的状态更改即可
				int count = CallTelephone.dao.reuse(taskId);   //回收已经分配，但是未外呼的数据
				
				if(count>0) {   //如果将已经分配，但是未外呼的数据收回后，需要调整当前任务的各种状态
					CallTaskCounterUtils.reduceCounter(taskId, "1", count);    //将已经分配的数量减少
					CallTaskCounterUtils.increaseCounter(taskId, "0", count);    //将新号码的数量增加
				}
			}
			break;
			default:
			{
				
			}
		}
		
		render(RenderJson.success("操作成功!"));
	}

	@Override
	public void delete() {
		
	}
	
	/**
	 * 根据任务的ID和类型得到饼图，
	 * 数型分为两种：1：已经分配和未分配的饼图； 2：已经分配的号码总体情况的饼图
	 */
	public void getPieChartData() {
		Integer taskId = Integer.valueOf(getPara("taskId"));
		String type = getPara("type");
		if(BlankUtils.isBlank(type)) {type="1";};
		
		if(type.equalsIgnoreCase("1")) {    //表示是需要返回已经分配和未分配的饼图

			ArrayList<Record> dataList = new ArrayList<Record>();   //创建一个 ArrayList
			Map<String,Integer> counterM = CallTaskCounterUtils.getCounterByTaskId(taskId);  //根据任务的ID, 从记数器中取出计数
			
			
			Record totalR = new Record();          			//创建号码总量记录
			Record undistributionR = new Record();          //创建未分配
			Record distributionR = new Record();          	//创建已分配
			
			Iterator<Map.Entry<String, Integer>> it = counterM.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, Integer> entry = it.next();
				String k = entry.getKey();             //取出key
				Integer v = entry.getValue();          //取出值value
				
				if(k.equalsIgnoreCase("total")) {      //取出总数量
					totalR.set("value",v);
				}else if(k.equalsIgnoreCase("0")) {    //取出未分配数量
					undistributionR.set("value", v);
				}else if(k.equalsIgnoreCase("1")) {    //取出已经分配的数量
					distributionR.set("value", v);
				}
			}
			
			if(!counterM.containsKey("total")) { totalR.set("value", 0); }    		//总数判断
			if(!counterM.containsKey("0")) { undistributionR.set("value", 0); }    	//未分配判断
			if(!counterM.containsKey("1")) { distributionR.set("value", 0); }    	//已分配判断
			totalR.set("name", "号码总数(" + totalR.getInt("value") + ")");
			undistributionR.set("name", "号码总量为(" + totalR.getInt("value") + "),未分配为(" + undistributionR.getInt("value") + ")");
			distributionR.set("name", "号码总量为(" + totalR.getInt("value") + "),已分配为(" + distributionR.getInt("value") + ")");
			
			//下面创建其他状态的饼图
			dataList.add(distributionR);
			dataList.add(undistributionR);
			
			//下面再次遍历计数器，用于显示已经分配的总体情况
			int calledCount = 0;   //已经外呼的数量
			Iterator<Map.Entry<String, Integer>> it2 = counterM.entrySet().iterator();
			while(it2.hasNext()) {
				Map.Entry<String, Integer> entry = it2.next();
				String k = entry.getKey();             //取出key
				Integer v = entry.getValue();          //取出值value
				
				if(!k.equalsIgnoreCase("0") && !k.equalsIgnoreCase("1") && !k.equalsIgnoreCase("total")) {    //排除新号码，已经分配和总数量，将其他的状态的取出来
					calledCount += v;
					Record statusRecord = new Record();
					String itemName = MemoryVariableUtil.getDictName("CALL_STATE",k);          //根据数据字典型 groupCode 和 Item 的值，取得 Item 的名
					statusRecord.set("name","已经分配的数量为(" + distributionR.getInt("value") + ")," + itemName + "的数量为(" + v + ")");   
					statusRecord.set("value",v);
					dataList.add(statusRecord);
				}
			}
			
			Record notCalledR = new Record();    //创建一个record, 用于记录未外呼的数量统计
			notCalledR.set("name", "已经分配的数量为(" + distributionR.getInt("value") + "),未外呼的数量为(" + (distributionR.getInt("value")-calledCount) + ")");
			notCalledR.set("value", (distributionR.getInt("value")-calledCount));
			dataList.add(notCalledR);
			
			renderJson(dataList);
			
		}else {
			
		}
		
	}
	
}






