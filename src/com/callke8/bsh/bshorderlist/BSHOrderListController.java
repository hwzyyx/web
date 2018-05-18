package com.callke8.bsh.bshorderlist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.common.CommonController;
import com.callke8.common.IController;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.ExcelExportUtil;
import com.callke8.utils.NumberUtils;
import com.callke8.utils.TelephoneLocationUtils;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

import net.sf.json.JSONObject;

public class BSHOrderListController extends Controller implements IController {

	@Override
	public void index() {
		
		//日期类型combobox数据返回,有两一个，一个是带请选择，一个不带选择
		setAttr("channelSourceComboboxDataFor1",CommonController.getComboboxToString("BSH_CHANNEL_SOURCE","1"));      		//购物平台带请选择的combobox
		setAttr("brandComboboxDataFor1",CommonController.getComboboxToString("BSH_BRAND","1"));        			//品牌信息带请选择的combobox
		setAttr("productNameComboboxDataFor1",CommonController.getComboboxToString("BSH_PRODUCT_NAME","1"));        			//货物信息带请选择的combobox
		setAttr("stateComboboxDataFor1",CommonController.getComboboxToString("BSH_CALL_STATE","1"));        				//外呼状态带请选择的combobox
		setAttr("respondComboboxDataFor1",CommonController.getComboboxToString("BSH_CLIENT_RESPOND","1"));       			//客户回复带请选择的combobox
		
		render("list.jsp");
	}

	@Override
	public void datagrid() {
		
		String orderId = getPara("orderId");
		String channelSource = getPara("channelSource");
		String customerName = getPara("customerName");
		String customerTel = getPara("customerTel");
		String brand = getPara("brand");
		String productName = getPara("productName");
		String state = getPara("state");
		String respond = getPara("respond");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		String dateTimeType = getPara("dateTimeType");     //取得查询时间类型，0表示时间区段为以创建时间为查询区间，1表示以外呼时间为查询区间
		
		String createTimeStartTime = null;
		String createTimeEndTime = null;
		String loadTimeStartTime = null;
		String loadTimeEndTime = null;
		
		if(dateTimeType.equalsIgnoreCase("1")) {           //表示是以外呼时间为查询区间
			loadTimeStartTime = startTime;
			loadTimeEndTime = endTime;
		}else {                                            //表示是以创建时间为查询区间
			createTimeStartTime = startTime;
			createTimeEndTime = endTime;
		}
		
		Integer pageSize = BlankUtils.isBlank(getPara("rows"))?1:Integer.valueOf(getPara("rows"));
		Integer pageNumber = BlankUtils.isBlank(getPara("page"))?1:Integer.valueOf(getPara("page"));
		
		Map map = BSHOrderList.dao.getBSHOrderListByPaginateToMap(pageNumber, pageSize, orderId,channelSource,customerName, customerTel,brand,productName,state,respond,createTimeStartTime,createTimeEndTime,loadTimeStartTime,loadTimeEndTime);
		//System.out.println("map:" + map);
		renderJson(map);
		
	}
	
	@Override
	public void add() {
		
		String orderId = null;
		String customerName = null;
		String customerTel = null;
		String productName = null;
		String expectInstallDate = null;
		String brand = null;
		String channelSource = null;
		
		String type = getRequest().getMethod();    //请求方式（GET|POST）
		System.out.println("提交数据的请求方式为：" + type);
		
		if(type.equalsIgnoreCase("POST")) {      //如果请求方式是以 POST 请求时，一般是以 json 字符串为上传数据，需要处理一下
			String jsonStr = null;
			try {      //第一种方式是当客户以 header:    application/json 上传 json数据时
				StringBuilder sb = new StringBuilder();
				BufferedReader reader = this.getRequest().getReader();
				String line = null;
				while((line=reader.readLine()) != null) {
					sb.append(line);
				}
				jsonStr = sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//(参数列表
			/*if(BlankUtils.isBlank(jsonStr)) {    //如果第一种方式为空后，第二种方式是以  application/application/x-www-form-urlencoded 上传 json 数据时
				Enumeration<String> pns = this.getParaNames();
				if(pns.hasMoreElements()) {
					jsonStr = pns.nextElement();
				}
			}*/
			
			System.out.println("客户提交上来的 JSON 字符串为 :" + jsonStr);
			
			if(!BlankUtils.isBlank(jsonStr) && jsonStr.toString().length() > 40) {
				
				JSONObject paramJson = JSONObject.fromObject(jsonStr);
				
				orderId = String.valueOf(paramJson.get("orderId"));
				customerName = String.valueOf(paramJson.get("customerName"));
				customerTel = String.valueOf(paramJson.get("customerTel"));
				productName = String.valueOf(paramJson.get("productName"));
				expectInstallDate = String.valueOf(paramJson.get("expectInstallDate"));
				brand = String.valueOf(paramJson.get("brand"));
				channelSource = String.valueOf(paramJson.get("channelSource"));
				
			}
			
			
		}else {									//以GET方式请求数据时,一般是以 URI 请求数据，直接通过 getPara()获取数据即可
			orderId = getPara("orderId");
			customerName = getPara("customerName");
			customerTel = getPara("customerTel");
			productName = getPara("productName");
			expectInstallDate = getPara("expectInstallDate");
			brand = getPara("brand");
			channelSource = getPara("channelSource");
		}
		
		if(BlankUtils.isBlank(orderId)) {
			renderJson(resultMap("ERROR","订单编号为空!"));
			return;
		}else if(BlankUtils.isBlank(customerName)) {
			renderJson(resultMap("ERROR","客户姓名为空!"));
			return;
		}else if(BlankUtils.isBlank(customerTel)) {
			renderJson(resultMap("ERROR","客户号码为空!"));
			return;
		}else if(BlankUtils.isBlank(expectInstallDate)) {
			renderJson(resultMap("ERROR","计划安装日期为空!"));
			return;
		}else if(BlankUtils.isBlank(brand)) {
			renderJson(resultMap("ERROR","品牌信息为空!"));
			return;
		}else if(BlankUtils.isBlank(productName)) {
			renderJson(resultMap("ERROR","产品信息为空!"));
			return;
		}else if(BlankUtils.isBlank(channelSource)) {
			renderJson(resultMap("ERROR","购物平台信息为空!"));
			return;
		}
		
		//判断客户号码是否为纯数字，则长度为 7 ~ 12 位
		boolean checkTelephoneFormat = NumberUtils.checkNumberByRegex(customerTel,"[0-9]{7,12}");    
		if(!checkTelephoneFormat) {
			renderJson(resultMap("ERROR","客户号码 " + customerTel + " 格式错误，格式必须是 纯数字且号码长度为7-12位!"));
			return;
		}
		
		//为了确保客户号码的更新一步的正确性，可以允许号码最前面有一个0，但是不允许前面有两个0或是两个0以上
		boolean checkTelephoneZero = NumberUtils.checkNumberByRegex(customerTel,"[0]{2,9}[0-9]{0,13}");
		if(checkTelephoneZero) {
			renderJson(resultMap("ERROR","客户号码 " + customerTel + " 格式错误，格式必须是 纯数字且号码长度为7-13位!且不允许前面有两个零以上!"));
			return;
		}
		
		//再判断计划安装日期的格式是否正确：判断号码格式是否是正确的日期格式 yyyyMMdd,不过现在是2018年，所以必须是20开头才是正确的号码格式
		boolean checkDateFormat = NumberUtils.checkNumberByRegex(expectInstallDate,"20[0-9]{2}[0-1]{1}[0-9]{1}[0-3]{1}[0-9]{1}");
		if(!checkDateFormat) {
			renderJson(resultMap("ERROR","计划安装日期 " + expectInstallDate + " 格式错误，必须是yyyyMMdd 格式!"));
			return;
		}
		
		//组织订单实体
		BSHOrderList orderList = new BSHOrderList();
		
		//同时再判断期望安装日期与当前日期对比,如果期望安装日期与当前日期同一日，或是小于当前日期，则需要将呼叫状态修改为 5,即是（已过期）
		boolean compareInstallDate2CurrentDate = compareInstallDate2CurrentDate(expectInstallDate);    //检查当前日期与安装日期是否已经过期
		if(!compareInstallDate2CurrentDate) {    //如果已经过期，则直接将状态修改为5，即是安装日期已过期
			orderList.set("STATE","5");
			renderJson(resultMap("ERROR","计划安装日期 是" + expectInstallDate + ",已过期!"));
			return;
		}else {
			orderList.set("STATE","0");
		}
		
		//由于传入的安装日期为yyyyMMdd,数据表中的安装日期格式为 yyyy-MM-dd,需要转化一下
		Date installDate = DateFormatUtils.parseDateTime(expectInstallDate, "yyyyMMdd");
		String expectInstallDateFormat = DateFormatUtils.formatDateTime(installDate, "yyyy-MM-dd");
		orderList.set("EXPECT_INSTALL_DATE",expectInstallDateFormat);
		
		//取出号码，进行相关字段的设定（呼出号码、省份、城市）
		//判断号码是否以0开头
		boolean is0Prex = NumberUtils.checkNumberByRegex(customerTel, "[0]{1}[0-9]{1,13}");       //判断是否以0开头
		boolean is1Prex = NumberUtils.checkNumberByRegex(customerTel, "[1]{1}[0-9]{1,13}");       //判断是否以1开头
		boolean is01Prex = NumberUtils.checkNumberByRegex(customerTel, "01{1}[0-9]{1,13}");       //判断是否以01开头
		boolean is10Prex = NumberUtils.checkNumberByRegex(customerTel, "10{1}[0-9]{1,13}");       //判断是否以10开头
		boolean is010Prex = NumberUtils.checkNumberByRegex(customerTel, "010[0-9]{1,13}");        //判断是否以010开头
		boolean is025Prex = NumberUtils.checkNumberByRegex(customerTel, "025[0-9]{1,13}");        //判断是否以025开头  ,即是否为南京座机号码
		int customerTelLen = customerTel.length();                                                //取得客户号码的长度
		
		if(is0Prex) {         			//以0开头时，有三种可能：（1）带区号的座机   （2）带0开头的手机号码(01开头并不代表就是手机，也有可能是010（即北京座机）) （3）号码有问题（即是号码长度以0开头，但是长度小于）
						
			if(customerTelLen < 11) {        	//如果是小于11位长度时,则可以表示该号码格式有错误，因为只要带区号的座机，必然长度会等于或是大于11位
				renderJson(resultMap("ERROR","客户号码 " + customerTel + " 格式错误，号码以0开头，但长度小于11位,为非正常座机或手机号码!"));
				return;
			}
			
			//(1)判断是否是01开头
			if(is01Prex && !is010Prex) {       //但是不为010开头，即是非北京区号，表示这个号码为手机号码，这里就要将0去掉，然后API查询归属地
				
				String searchNumber = customerTel.substring(1, customerTel.length());
				
				Map<String,String> locationMap = TelephoneLocationUtils.getTelephoneLocation(searchNumber,BSHCallParamConfig.getJuHeUrl(), BSHCallParamConfig.getJuHeAppKey());
				
				if(!BlankUtils.isBlank(locationMap)) {
					String province = locationMap.get("province");
					String city = locationMap.get("city");
					
					orderList.set("PROVINCE",province);
					orderList.set("CITY", city);
					if(city.equalsIgnoreCase("南京")) {     //如果城市为江苏南京时，外呼号码为加一个0
						orderList.set("CALLOUT_TEL", "0" + searchNumber);
					}else {
						orderList.set("CALLOUT_TEL","00" + searchNumber);
					}
				}else {                 //即是无法定位号码归属地，有可能是一个假的号码
					renderJson(resultMap("ERROR","客户号码 " + customerTel + " 格式 正确，但无法定位归属地，号码异常!"));
					return;
				}
			}else {       //如果为非01开头手机号码，则表示应该是座机
				
				//判断是否为南京座机
				if(is025Prex) {    //如果以025开头
					orderList.set("PROVINCE","江苏");
					orderList.set("CITY","南京");
					orderList.set("CALLOUT_TEL", "0" + customerTel.substring(3,customerTel.length()));    //外呼号码，需要将南京号码的区号去除，且在原号码前加0
				}else {
					orderList.set("PROVINCE","外省");
					orderList.set("CITY","外市");
					orderList.set("CALLOUT_TEL", "0" + customerTel);                                      //需要在外地号码前加0，如02087878686 呼出号码为 002087878686
				}
				
			}
			
		}else if(is1Prex) {				//以1开头时，以1开头，看长度是否为10
			
			if(is10Prex) {              //如果以10开头，可能会是简化的以非0开头的北京座机
				orderList.set("PROVINCE","北京");
				orderList.set("CITY","北京");
				orderList.set("CALLOUT_TEL","00" + customerTel);
			}else {                     //如果不是以10开头，那么就可能是手机号码
				if(customerTelLen < 11) {
					renderJson(resultMap("ERROR","客户号码 " + customerTel + " 格式 正确，但无法定位归属地，号码异常!"));
					return;
				}
				
				Map<String,String> locationMap = TelephoneLocationUtils.getTelephoneLocation(customerTel,BSHCallParamConfig.getJuHeUrl(), BSHCallParamConfig.getJuHeAppKey());
				if(!BlankUtils.isBlank(locationMap)) {
					String province = locationMap.get("province");
					String city = locationMap.get("city");
					
					orderList.set("PROVINCE",province);
					orderList.set("CITY", city);
					if(city.equalsIgnoreCase("南京")) {     //如果城市为江苏南京时，外呼号码为加一个0
						orderList.set("CALLOUT_TEL", "0" + customerTel);
					}else {
						orderList.set("CALLOUT_TEL","00" + customerTel);
					}
				}else {                 //即是无法定位号码归属地，有可能是一个假的号码
					renderJson(resultMap("ERROR","客户号码 " + customerTel + " 格式 正确，但无法定位归属地，号码异常!"));
					return;
				}
				
			}
			
			
		}else {       //如果非0，又非1开头，表示很有可能是直接给的南京本地号码，就要看长度，如果长度为8位，表示南京本地号码
			
			if(customerTelLen == 8) {       //表示这个是南京本地的号码
				orderList.set("PROVINCE","江苏");
				orderList.set("CITY", "南京");
				orderList.set("CALLOUT_TEL", "0" + customerTel);
			}else {  //如果长度不为8位，且长度大于8位时，该号码是带区号的没有给0的座机号
				renderJson(resultMap("ERROR","客户号码 " + customerTel + " 格式 正确，但无法定位归属地，号码异常!"));
				return;
			}
			
		}
		
		orderList.set("ORDER_ID", orderId);
		orderList.set("CUSTOMER_NAME", customerName);
		orderList.set("CUSTOMER_TEL", customerTel);
		orderList.set("BRAND", Integer.valueOf(brand));
		orderList.set("PRODUCT_NAME", Integer.valueOf(productName));
		orderList.set("CHANNEL_SOURCE", Integer.valueOf(channelSource));
		orderList.set("CREATE_TIME", DateFormatUtils.getCurrentDate());
		
		boolean b = BSHOrderList.dao.add(orderList);
		
		if(b) {
			renderJson(resultMap("SUCCESS","提交订单数据成功!"));
		}else {
			renderJson(resultMap("ERROR","提交订单数据失败!"));
		}
		
	}
	
	/**
	 * 对比安装日期与当前日期
	 * 
	 * （1）如果安装日期大于当前日期，如果安装日期大于当前日期，再判断当前时间与系统的生效时间对比情况，
	 *      如果是 晚上20：00至 23：59:59 之间添加的数据，需要保证安装日期为第三天以上的日期
	 * 
	 * （2）如果安装日期小于或是等于当前日期，返回false
	 * 
	 * 安装日期格式为：MMdd,基于这种格式，我们可以强制将字符串转为数字，然后直接比较即可。
	 * 
	 * @param expectInstallDate
	 * 					格式可能是：20180501
	 * 
	 * @return
	 * 		true:安装日期大于当前日期
	 * 		false:安装日期小于或是等于当前日期
	 */
	public boolean compareInstallDate2CurrentDate(String expectInstallDate) {
		
		String currDateStr = DateFormatUtils.formatDateTime(new Date(),"yyyyMMdd");    				//当前日期先取出来，是为了将时、分、秒去掉
		
		Date expectInstallDate2Date = DateFormatUtils.parseDateTime(expectInstallDate,"yyyyMMdd");   	//安装日期对象
		Date currDate = DateFormatUtils.parseDateTime(currDateStr, "yyyyMMdd");                       //当前日期对象                   
		
		//比较两个日期的豪秒
		if(expectInstallDate2Date.getTime() > currDate.getTime()) {           //如果安装日期大于当前月份时，认为是安装期内
			
			//即使安装日期已经处于安装期内，但是还有一种极端情况，即是当前的添加日期为系统限定的晚上 20点至 24点提交的订单
			//因为这个时间段内添加的号码，不可能当天呼，只等等第二天才呼，所以安装日期应该是第三天才是正确的
			//(1)对比当前时间与系统的生效开始时间、系统生效结束时间对比，看对比的结果进行判断
			int compareResult = BSHCallParamConfig.compareCurrTime2ActiveTime();
			if(compareResult == 3) {     //即是 晚上 20：00至 23：59:59 添加的数据，要保证当前日期与安装日期的差值 大于两天
				
				//查看当前日期的毫秒差
				long timesDifference = expectInstallDate2Date.getTime() - currDate.getTime();
				long twoDayDifference = 2 * 24 * 60 * 60 * 1000;     //两天的时间毫秒差为
				
				if(timesDifference < twoDayDifference) {    //小于两天时，返回 false
					return false;
				}
				
			}
			
			return true;
		}else {
			return false;
		}
	}
	
	public Map<String,String> resultMap(String resultCode,String resultMsg) {
		
		Map<String,String> rsM = new HashMap<String,String>();
		rsM.put("resultCode", resultCode);
		rsM.put("resultMsg", resultMsg);
		
		return rsM;
	}
	
	/**
	 * 导出数据到 excel
	 */
	public void exportExcel() {
		
		String orderId = getPara("orderId");
		String channelSource = getPara("channelSource");
		String customerName = getPara("customerName");
		String customerTel = getPara("customerTel");
		String brand = getPara("brand");
		String productName = getPara("productName");
		String state = getPara("state");
		String respond = getPara("respond");
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");
		String dateTimeType = getPara("dateTimeType");     //取得查询时间类型，0表示时间区段为以创建时间为查询区间，1表示以外呼时间为查询区间
		
		String createTimeStartTime = null;
		String createTimeEndTime = null;
		String loadTimeStartTime = null;
		String loadTimeEndTime = null;
		
		if(dateTimeType.equalsIgnoreCase("1")) {           //表示是以外呼时间为查询区间
			loadTimeStartTime = startTime;
			loadTimeEndTime = endTime;
		}else {                                            //表示是以创建时间为查询区间
			createTimeStartTime = startTime;
			createTimeEndTime = endTime;
		}
		
		//根据传入的条件，从数据库中查询出列表
		List<Record> list = BSHOrderList.dao.getBSHOrderListByCondition(orderId, channelSource, customerName, customerTel, brand, productName, state, respond, createTimeStartTime, createTimeEndTime, loadTimeStartTime, loadTimeEndTime);
		
		//得到数据列表，准备以 Excel 方式导出
		String[] headers = {"订单编号","购物平台","客户姓名","客户号码","省份","城市","外呼号码","品牌","产品名称","计划安装日期","客户回复","创建时间","外呼结果","失败原因","已重试","外呼时间","下次外呼时间","外呼结果JSON","接口响应"};
		String[] columns = {"ORDER_ID","CHANNEL_SOURCE_DESC","CUSTOMER_NAME","CUSTOMER_TEL","PROVINCE","CITY","CALLOUT_TEL","BRAND_DESC","PRODUCT_NAME_DESC","EXPECT_INSTALL_DATE","RESPOND_DESC","CREATE_TIME","STATE_DESC","LAST_CALL_RESULT","RETRIED","LOAD_TIME","NEXT_CALLOUT_TIME","CALLRESULT_JSON","FEEDBACK_CALLRESULT_RESPOND"};
		String fileName = "时间区间:" + startTime + " 至 " + endTime + " .xls";
		String sheetName = "订单信息列表";
		int cellWidth = 200;
		
		ExcelExportUtil export = new ExcelExportUtil(list,getResponse());
		export.headers(headers).columns(columns).sheetName(sheetName).cellWidth(cellWidth);
		
		export.fileName(fileName).execExport();
	}
	
	/**
	 * 导出汇总数据
	 */
	public void exportExcelForSummaryData() {
		
		List<Record> list = new ArrayList<Record>();
		Record rc = new Record();
		rc.set("category","数量");
		Record rr = new Record();
		rr.set("category", "占比");
		
		String totalCount = getPara("totalCount");      rc.set("totalData", totalCount);
		String state1Count = getPara("state1Count");    rc.set("state1Data", state1Count);
		String state2Count = getPara("state2Count");	rc.set("state2Data", state2Count);
		String state3Count = getPara("state3Count");    rc.set("state3Data", state3Count);
		String state4Count = getPara("state4Count");	rc.set("state4Data", state4Count);	
		String state5Count = getPara("state5Count");	rc.set("state5Data", state5Count);
		String state6Count = getPara("state6Count");	rc.set("state6Data", state6Count);
		String respond1Count = getPara("respond1Count"); 	rc.set("respond1Data", respond1Count);
		String respond2Count = getPara("respond2Count");	rc.set("respond2Data", respond2Count);
		String respond3Count = getPara("respond3Count");	rc.set("respond3Data", respond3Count);
		String respond4Count = getPara("respond4Count");	rc.set("respond4Data", respond4Count);
		list.add(rc);
		
		String totalRate = getPara("totalRate");		rr.set("totalData", totalRate + "%");
		String state1Rate = getPara("state1Rate");		rr.set("state1Data", state1Rate + "%");
		String state2Rate = getPara("state2Rate");		rr.set("state2Data", state2Rate + "%");
		String state3Rate = getPara("state3Rate");		rr.set("state3Data", state3Rate + "%");
		String state4Rate = getPara("state4Rate");		rr.set("state4Data", state4Rate + "%");
		String state5Rate = getPara("state5Rate");		rr.set("state5Data", state5Rate + "%");
		String state6Rate = getPara("state6Rate");		rr.set("state6Data", state6Rate + "%");
		String respond1Rate = getPara("respond1Rate");		rr.set("respond1Data", respond1Rate + "%");
		String respond2Rate = getPara("respond2Rate");		rr.set("respond2Data", respond2Rate + "%");
		String respond3Rate = getPara("respond3Rate");		rr.set("respond3Data", respond3Rate + "%");
		String respond4Rate = getPara("respond4Rate");		rr.set("respond4Data", respond4Rate + "%");
		list.add(rr);
		
		String startTime = getPara("startTime");
		String endTime = getPara("endTime");

		//得到数据列表，准备以 Excel 方式导出
		String[] headers = {"","数据总量","已载入","已成功","待重呼","已失败","已过期","放弃呼叫","确认安装","暂不安装","延后安装","无/错回复"};
		String[] columns = {"category","totalData","state1Data","state2Data","state3Data","state4Data","state5Data","state6Data","respond1Data","respond2Data","respond3Data","respond4Data"};
		String fileName = "时间区间:" + startTime + " 至 " + endTime + " 的统计汇总情况.xls";
		String sheetName = "数据汇总信息";
		
		int cellWidth = 80;
		
		ExcelExportUtil export = new ExcelExportUtil(list,getResponse());
		export.headers(headers).columns(columns).sheetName(sheetName).cellWidth(cellWidth);
		
		export.fileName(fileName).execExport();
	}

	@Override
	public void update() {
		
	}

	@Override
	public void delete() {
		
	}

}
