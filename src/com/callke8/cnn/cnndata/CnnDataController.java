package com.callke8.cnn.cnndata;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import com.callke8.common.IController;
import com.callke8.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import net.sf.json.JSONArray;

public class CnnDataController extends Controller implements IController  {

	@Override
	public void index() {
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));	//页数
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));	//每页的数量

		String customerName = getPara("customerName");
		String customerTel = getPara("customerTel");
		String customerNewTel = getPara("customerNewTel");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		String flag = getPara("flag");

		Map map = CnnData.dao.getCnnDataByPaginateToMap(pageNumber,pageSize,customerName,customerTel,customerNewTel,flag,startTime,endTime);
		renderJson(map);
	}

	@Override
	public void add() {
		
		CnnData formData = getModel(CnnData.class,"cnn_data");
		
		String customerTel = formData.getStr("CUSTOMER_TEL");            //取出号码
		String customerNewTel = formData.getStr("CUSTOMER_NEW_TEL");     //取出新号码
		
		//第一步判断，两个号码是否为数字，且大于5位数
		boolean chk1 = StringUtil.isNumber(customerTel);     	//是否为号码类型
		boolean chk2 = StringUtil.isNumber(customerNewTel);     //新号码是否为号码类型
		if(!chk1) {
			render(RenderJson.error("新增失败,客户号码为非号码格式!"));
			return;
		}
		
		
		if(!chk2) {
			render(RenderJson.error("新增失败,客户新号码为非号码格式!"));
			return;
		}
		
		if(customerTel.length()<5 || customerNewTel.length()<5) {
			render(RenderJson.error("新增失败,号码的长度必须等于或大于5位!"));
			return;
		}
		
		//第二步判断传入的号码（主要是客户原本号码是否在数据库中已存在相关记录）
		Record r = CnnData.dao.getCnnDataByCustomerTel(customerTel);
		if(!BlankUtils.isBlank(r)) {
			render(RenderJson.error("新增失败,数据库中已经存在该客户号码的记录!"));
			return;
		}
		
		String flag = formData.getStr("FLAG");
		if(BlankUtils.isBlank(flag)) {    //表示没有勾选,需要设置为1，即是播放中文，2为播放英文
			formData.set("FLAG","1");
		}
		
		formData.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		formData.set("CREATE_USERCODE",getSession().getAttribute("currOperId"));   //设置创建人

		boolean b = CnnData.dao.add(formData);
		if(b) {
			render(RenderJson.success("新增记录成功!"));
		}else {
			render(RenderJson.error("新增记录失败!"));
		}
	}

	@Override
	public void update() {
		CnnData formData = getModel(CnnData.class,"cnn_data");

		int id = Integer.valueOf(formData.get("ID").toString());
		String customerName = formData.get("CUSTOMER_NAME");
		String customerTel = formData.get("CUSTOMER_TEL");
		String customerNewTel = formData.get("CUSTOMER_NEW_TEL");
		
		//第一步判断，两个号码是否为数字，且大于5位数
		boolean chk1 = StringUtil.isNumber(customerTel);     	//是否为号码类型
		boolean chk2 = StringUtil.isNumber(customerNewTel);     //新号码是否为号码类型
		if(!chk1) {
			render(RenderJson.error("修改失败,客户号码为非号码格式!"));
			return;
		}
		
		
		if(!chk2) {
			render(RenderJson.error("修改失败,客户新号码为非号码格式!"));
			return;
		}
		
		if(customerTel.length()<5 || customerNewTel.length()<5) {
			render(RenderJson.error("修改失败,号码的长度必须等于或大于5位!"));
			return;
		}
		
		//第二步判断传入的号码（主要是客户原本号码是否在数据库中已存在相关记录）
		Record r = CnnData.dao.getCnnDataByCustomerTel(customerTel);
		if(!BlankUtils.isBlank(r)) {
			
			int idValue = r.getInt("ID");   //取出查询出来的ID，看看是否与当前修改的是同一条
			if(idValue!=id) {
				render(RenderJson.error("修改失败,数据库中已经存在该客户号码的记录!"));
				return;
			}
		}
		
		String flag = formData.getStr("FLAG");
		if(BlankUtils.isBlank(flag)) {    //表示没有勾选,需要设置为1，即是播放中文，2为播放英文
			formData.set("FLAG","1");
			flag = "1";
		}

		boolean b = CnnData.dao.update(customerName,customerTel,customerNewTel,flag,id);
		if(b) {
			render(RenderJson.success("修改成功!"));
		}else {
			render(RenderJson.error("修改失败!"));
		}
	}

	@Override
	public void delete() {
		String id = getPara("id");

		boolean b = CnnData.dao.deleteById(id);
		if(b) {
			render(RenderJson.success("删除成功!"));
		}else {
			render(RenderJson.error("删除失败!"));
		}
	}
	
	/**
	 * 批量删除
	 */
	public void batchDelete() {
		
		String ids = getPara("ids");
		
		if(BlankUtils.isBlank(ids)) {
			render(RenderJson.error("删除失败,没有选中任何记录!"));
			return;
		}
		
		int count = CnnData.dao.deleteByIdList(ids);
		
		render(RenderJson.success("成功删除  " + count + " 条改号数据!"));
	}
	
	/**
	 * 通过上传数据文件上传数据
	 * 
	 * 上传时，还会传入一个参数： uploadFlag，用于定义该文件上传是用于批量增加还是批量删除数据， 1：批量增加数据；2：批量删除数据
	 */
	public void uploadFile() {
		
		//先判断文件大小限制
		int fileSize = getRequest().getContentLength();     //得到上传文件的大小，由于jfinal默认最大的上传的大小为 10 * 1024 * 1024 即是 10M
		if(fileSize > (10 * 1024 * 1024)) {    //如果大于 10M 时，返回错误
			render(RenderJson.error("上传失败：上传文件过大，已经超过 10M"));
			return;
		}
		
		//获取上传的文件
		//（1）定义上传路径，临时文件将上传到 upload 文件夹
		String uploadDir = PathKit.getWebRootPath() + File.separator + "upload" + File.separator;
		
		//（2）执行上传操作
		UploadFile uf = getFile("cnnDataFile",uploadDir);
		
		//（3）判断上传文件的类型
		String mimeType = StringUtil.getExtensionName(uf.getFileName());
		
		//判断文件类型，是否为 txt、xls 或是 xlsx
		if(BlankUtils.isBlank(mimeType) || (!mimeType.equalsIgnoreCase("xls") && !mimeType.equalsIgnoreCase("xlsx") && !mimeType.equalsIgnoreCase("txt"))) {
			//提示错误之前，先将上传的文件删除
			if(!BlankUtils.isBlank(uf.getFile()) && uf.getFile().exists())  {
				uf.getFile().delete();
			}
			
			render(RenderJson.error("上传失败,上传的号码文件类型不正确,只支持 txt、xls 或是 xlsx"));
			return;
		}
		
		//上传的文件名可能是中文名，不利于读取，需先重命名为数字格式文件名
		String renameFileName = DateFormatUtils.getTimeMillis() + "." + mimeType;
		File newFile = new File(uploadDir + renameFileName);
		
		uf.getFile().renameTo(newFile);
		
		//获取上传文件的操作动作 uploadFlag 的值, 1:批量添加; 2:批量删除
		String uploadFlag = getPara("uploadFlag"); 
		System.out.println("上传的动作类型为：" + uploadFlag);
		if(BlankUtils.isBlank(uploadFlag)) {
			uploadFlag = "1";
		}
		
		
		String execResult = "";
		if(uploadFlag.equals("1")) {    //如果上传类型为批量导入数据时
			execResult = insertCnnDataFromFile(newFile,mimeType);    //成功插入的数量
		}else {
			execResult = deleteCnnDataFromFile(newFile,mimeType);    //批量删除的数据
		}
		
		render(RenderJson.success(execResult));
		
	}
	
	/**
	 * 据上传的文件数据批量的从数据库中删除数据
	 * 
	 * @param file
	 * @param mimeType
	 * @return
	 */
	public String deleteCnnDataFromFile(File file,String mimeType) {
		StringBuilder sb = new StringBuilder();
		
		//第一步，将数据从文件中读出，并放置到列表
		//把文件中的信息取出并放置到一个  List 中， Record 以数字顺序存储数据
		List<Record> list = null;
		
		if(mimeType.equalsIgnoreCase("txt")) {     //如果文件类型为 txt
			list = TxtUtils.readTxt(file);
		}else {
			list = XLSUtils.readXls(file);
		}
		
		//定义一个存储上传的客户号码的列表，需要过滤掉非数字和小于5位的号码
		List<String> uploadTelList = new ArrayList<String>();
		
		//第二步：遍历数据，并将号码内容加入到一个列表
		/**
		 * 遍历上传的数据，找出客户号码
		 */
		for(Record r:list) {
			String customerTel = r.get("0");       //第一个数据是客户号码
			if(!BlankUtils.isBlank(customerTel)) {
				customerTel = customerTel.trim();
			}
			
			boolean chkIsNumber = StringUtil.isNumber(customerTel);
			if(chkIsNumber) {   				  //如果号码为纯数字时
				if(customerTel.length()<5) {      //如果小于5位，跳过循环
					continue;
				}
				
				//在将号码数据加入列表之前，先判断是否有重复的号码数据
				boolean isContain = uploadTelList.contains(customerTel);
				if(isContain) {    //如果已经存在该号码了，就跳过循环
					continue;
				}
				uploadTelList.add(customerTel);
			}
		}
		
		//如果上传的号码数据列表为空时，就表示无需删除数据
		if(uploadTelList.size()==0) {
			return "提交的文件中没有号码数据，无需删除数据!";
		}
		
		//第三步，遍历数据，返回对应的数据库的ID值的字符串值，以逗号分隔
		//如果有需要删除的数据，则先取出数据库的数据,再遍历这些数据
		List<Record> allCnnDataList = CnnData.dao.getAllCnnData();      //从数据库中取出所有的改号数据
		
		System.out.println("uploadTelList-=======:" + uploadTelList);
		StringBuilder idSb = new StringBuilder();   //id值的SB，以逗号分隔
		for(String uploadCustomerTel:uploadTelList) {
			String idValue = checkCnnDataAndGetIdValue(allCnnDataList, uploadCustomerTel);
			if(!BlankUtils.isBlank(idValue)) {
				idSb.append(idValue);
				idSb.append(",");
			}
		}
		
		//第四步，根据 idSb 的值，直接从数据表中删除数据
		if(BlankUtils.isBlank(idSb.toString())) {
			return "数据库的改号数据，不包括上传中的文件的号码数据，无需删除数据!";
		}
		
		//如果id的字符串不为空时，删除最后一个逗号
		String idList = idSb.toString();
		idList = idList.substring(0, idList.length()-1);
		
		System.out.println("idList======:" + idList);
		
		int count = CnnData.dao.deleteByIdList(idList);
		
		return "成功删除数据 " + count + " 条!";
	}
	
	/**
	 * 从 xls、 txt 读取主叫号码，并插入主叫号码到数据库
	 * 
	 * @param file
	 * @param mimeType
	 * @return
	 */
	public String insertCnnDataFromFile(File file,String mimeType) {
		
		StringBuilder sb = new StringBuilder();
		
		//把文件中的信息取出并放置到一个  List 中， Record 以数字顺序存储数据
		List<Record> list = null;
		
		if(mimeType.equalsIgnoreCase("txt")) {     //如果文件类型为 txt
			list = TxtUtils.readTxt(file);
		}else {
			list = XLSUtils.readXls(file);
		}
		
		//建立两个 List， 一个用于检查数据格式，一个用于检查重复性
		ArrayList<Record> afterCheckFormatList = new ArrayList<Record>();      //结束格式检查后
		ArrayList<Record> afterRepetitionList = new ArrayList<Record>();       //结束数据重复性后
		ArrayList<String> telList = new ArrayList<String>();                //创建一个号码的列表，用于过滤上传的文件号码的整批号码的重复性，因为不但需要对比已存入数据库的主叫号码的重复性，还需要对比自身的号码的重复性
		
		//创建两个属性
		int validCount = 0;     //有效号码数据
		int repeatCount = 0;    //重复的数据量
		
		//第一步：判断数据的格式
		for(Record r:list) {   //遍历数据，并格式格式
			String customerTel = r.get("0");       //第一个数据是客户号码
			String customerNewTel = r.get("1");    //第二个数据是客户的新号码
			String flagContent = r.get("2");       //第三个数据是标识符，如果为空时，播放中文；如果不为空时，播放英文语音
			String changeTime = r.get("3");        //第四个数据是修改的时间
			
			if(!BlankUtils.isBlank(customerTel)) {
				customerTel = customerTel.trim();
			}
			if(!BlankUtils.isBlank(customerNewTel)) {
				customerNewTel = customerNewTel.trim();
			}
			if(!BlankUtils.isBlank(flagContent)) {
				flagContent = flagContent.trim();
			}
			if(!BlankUtils.isBlank(changeTime)) {
				changeTime = changeTime.trim();
			}
			
			
			boolean isNumber = StringUtil.isNumber(customerTel);    //判断第一列是否为纯数字
			boolean newTelIsNumber = StringUtil.isNumber(customerNewTel);
			if(isNumber && newTelIsNumber) {    //第一列和第二列都为数字时，才加入格式正确的数据
				//（1）如果第一列为号码时，并等于这些号码就可以直接存入数据库，还需要判断号码的长度，只有号码长度大于5位，即是 95151，10000号之类，或是座机号码
				if(customerTel.length() < 5 || customerNewTel.length()<5) {     //小于5时，跳过循环
					continue;
				}
				
				//（2）再过滤自身的重复性
				boolean isContain = telList.contains(customerTel);
				if(isContain) {    //如果已经存在该号码了，就跳过循环
					continue;
				}
				
				r.set("CUSTOMER_TEL", customerTel);
				r.set("CUSTOMER_NEW_TEL",customerNewTel);
				
				String customerName = customerTel;    //客户名字上传的数据时不包括的，需要设置为客户号码即可
				r.set("CUSTOMER_NAME", customerName);
				
				//设置 flag 的值，如果 flagContent 的内容为空，表示这个值需要设置为1，即播放中文；如果内容不为空，就需要设置为2，即播放英文语音。
				if(!BlankUtils.isBlank(flagContent)) {
					r.set("FLAG", "2");
				}else {
					r.set("FLAG", "1");
				}
				
				r.set("CHANGE_TIME", changeTime);
				r.set("CREATE_USERCODE", getSession().getAttribute("currOperId"));           //创建人
				r.set("CREATE_TIME",DateFormatUtils.getCurrentDate());                       //创建时间
				
				telList.add(customerTel);
				afterCheckFormatList.add(r);    //加入检查数据格式后的List
			}
		}
		
		validCount = afterCheckFormatList.size();        //有效的数据
		telList = new ArrayList<String>();               //清空这个List，释放资源
		
		//第二步：检查重复性
		List<Record> allCnnDataList = CnnData.dao.getAllCnnData();      //从数据库中取出所有的改号数据
		
		//遍历上传的号码
		for(Record uploadCnnData:afterCheckFormatList) {
			String customerTel = uploadCnnData.getStr("CUSTOMER_TEL");
			boolean isInDb = chkCnnDataIsInDb(allCnnDataList,customerTel);
			
			if(isInDb) {     //如果上传的主叫号码已经在数据库中存在，则增加重复的数量
				repeatCount++;
			}else {          //如果在数据库中没有数据时
				afterRepetitionList.add(uploadCnnData);
			}
		}
		
		//通过上面的各过处理后，得到的 afterRepetitionList,就是需要上传到数据表的主叫号码列表，调用 SysCallerId 进行批量插入数据表
		int[] insertData = CnnData.dao.batchSave(afterRepetitionList);
		
		int successCount = 0;
		if(!BlankUtils.isBlank(insertData) && insertData.length > 0) {
			successCount = insertData.length;
		}
		
		sb.append("上传总行数为：" + list.size() + "行。");
		sb.append("有效号码：" + validCount);
		sb.append(",重复号码：" + repeatCount + "，成功插入：" + successCount + "!");
		
		return sb.toString();
		
	}
	
	/**
	 * 判断传入的号码，是否存在取出的列表中
	 * 
	 * @param allSysCallerIdList
	 * @param callerIdNumber
	 * @return
	 */
	public boolean chkCnnDataIsInDb(List<Record> allCnnDataList,String customerTel) {
		
		boolean b = false;
		
		for(Record cnnDataRecord:allCnnDataList) {
			String ct = cnnDataRecord.get("CUSTOMER_TEL");
			if(ct.equals(customerTel)) {    //如果有相同时，设置为 true,直接返回
				b = true;
				return b;
			}
		}
		
		return b;
	}
	
	/**
	 * 根据传入的号码，检查该号码是否存在于列表中，并将ID值以字符串的形式返回
	 * 
	 * @param allCnnDataList
	 * @param customerTel
	 * @return
	 */
	public String checkCnnDataAndGetIdValue(List<Record> allCnnDataList,String customerTel) {
		
		String idValue = null;
		
		for(Record cnnDataRecord:allCnnDataList) {
			String ct = cnnDataRecord.get("CUSTOMER_TEL");
			if(ct.equals(customerTel)) {    //如果有相同时，取出ID值,直接返回
				return String.valueOf(cnnDataRecord.getInt("ID"));
			}
		}
		
		return idValue;
	}
	
	/**
	 * 导出改号通知数据的模板
	 */
	public void template() {
		//文件类型: txt | excel
		String type = getPara("type");                 //得到文件的类型
		//模板的标识
		String identify = getPara("identify");         //callerId
		
		String fileName = "";
		String mimeType = "";
		
		String templateDir = File.separator + "template" + File.separator;    //模板所在的路径
		
		String path_tmp = PathKit.getWebRootPath() + templateDir;
		
		if(type.equalsIgnoreCase("txt")) {
			mimeType = "txt";
		}else {
			mimeType = "xlsx";
		}
		
		fileName = identify + "_template" + "." + mimeType;
		
		File file = new File(path_tmp + fileName);
		
		if(file.exists()) {
			renderFile(file);
		}else {
			render(RenderJson.error("下载模板失败,文件不存在!"));
		}
	}
	
	/**
	 * 导出为excel
	 */
	public void exportExcel() {
		
		String customerName = getPara("customerName");
		String customerTel = getPara("customerTel");
		String customerNewTel = getPara("customerNewTel");
		String flag = getPara("flag");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		
		List<Record> list = CnnData.dao.getCnnDataByCondition(customerName, customerTel, customerNewTel, flag, startTime, endTime);
		
		String fileName = "export.xls";
		String sheetName = "改号数据";
		
		ExcelExportUtil export = new ExcelExportUtil(list, getResponse());
		
		String[] headers = {"客户号码","客户新号码","客户姓名","(中/英)标识","创建人","创建时间"};
		String[] columns = {"CUSTOMER_TEL","CUSTOMER_NEW_TEL","CUSTOMER_NAME","FLAG_DESC","CREATE_USERCODE_DESC","CREATE_TIME"};
		
		export.headers(headers).columns(columns).cellWidth(100).sheetName(sheetName);
		export.fileName(fileName).execExport();
	}
	
	
	
	
	
	
	
	
}
