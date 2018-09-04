package com.callke8.bsh.bshorderlist;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 博世家电订单列表实体类，用于储存由博世电子传送过来的订单信息
 * 
 * @author 黄文周
 *
 */
public class BSHOrderList extends Model<BSHOrderList> {
	
	private static final long serialVersionUID = 1L;
	
	public static BSHOrderList dao = new BSHOrderList();
	
	BSHOrderList prev;
	BSHOrderList next;
	
	/**
	 * 根据传入信息，以分页的方式查询数据
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param orderId
	 * @param channelSource
	 * @param customerName
	 * @param customerTel
	 * @param brand
	 * @param productName
	 * @param state
	 * @param respond
	 * @param timeType
	 * @param createTimeStartTime
	 * 					创建时间的开始时间
	 * @param createTimeEndTime
	 * 					创建时间的结束时间
	 * @param loadTimeStartTime
	 * 					外呼时间的开始时间
	 * @param loadTimeEndTime
	 * 					外呼时间的结束时间
	 * @return
	 */
	public Page getBSHOrderListByPaginate(int pageNumber,int pageSize,String orderId,String channelSource,String customerName,String customerTel,String brand,String productName,String state,String respond,String timeType,String createTimeStartTime,String createTimeEndTime,String loadTimeStartTime,String loadTimeEndTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[20];
		int index = 0;
		
		sb.append("from bsh_orderlist where 1=1");
		
		//订单ID查询
		if(!BlankUtils.isBlank(orderId)) {
			sb.append(" and ORDER_ID like ?");
			pars[index] = "%" + orderId + "%";
			index++;
		}
		
		//购物平台
		if(!BlankUtils.isBlank(channelSource) && !channelSource.equalsIgnoreCase("empty")) {
			sb.append(" and CHANNEL_SOURCE=?");
			pars[index] = channelSource;
			index++;
		}
		
		//客户姓名查询
		if(!BlankUtils.isBlank(customerName)) {
			sb.append(" and CUSTOMER_NAME like ?");
			pars[index] = "%" + customerName + "%";
			index++;
		}
		
		//客户手机号码查询
		if(!BlankUtils.isBlank(customerTel)) {
			sb.append(" and CUSTOMER_TEL like ?");
			pars[index] = "%" + customerTel + "%";
			index++;
		}
		
		//品牌
		if(!BlankUtils.isBlank(brand) && !brand.equalsIgnoreCase("empty")) {
			sb.append(" and BRAND=?");
			pars[index] = brand;
			index++;
		}
		
		//产品名称查询
		if(!BlankUtils.isBlank(productName) && !productName.equalsIgnoreCase("empty")) {
			sb.append(" and PRODUCT_NAME=?");
			pars[index] = productName;
			index++;
		}
		
		//外呼结果查询
		if(!BlankUtils.isBlank(state) && !state.equalsIgnoreCase("empty")) {
			sb.append(" and STATE=?");
			pars[index] = state;
			index++;
		}
		
		//客户回复
		if(!BlankUtils.isBlank(respond) && !respond.equalsIgnoreCase("empty")) {
			sb.append(" and RESPOND=?");
			pars[index] = respond;
			index++;
		}
		
		//日期类型
		if(!BlankUtils.isBlank(timeType) && !timeType.equalsIgnoreCase("empty")) {
			sb.append(" and TIME_TYPE=?");
			pars[index] = timeType;
			index++;
		}
		
		//创建的开始时间查询
		if(!BlankUtils.isBlank(createTimeStartTime)) {
			sb.append(" and CREATE_TIME>?");
			pars[index] = createTimeStartTime;
			index++;
		}
		
		//创建的结束时间查询
		if(!BlankUtils.isBlank(createTimeEndTime)) {
			sb.append(" and CREATE_TIME<?");
			pars[index] = createTimeEndTime;
			index++;
		}
		
		//外呼时间的开始时间查询
		if(!BlankUtils.isBlank(loadTimeStartTime)) {
			sb.append(" and LOAD_TIME>?");
			pars[index] = loadTimeStartTime;
			index++;
		}
		
		//外呼时间的结束时间查询
		if(!BlankUtils.isBlank(loadTimeEndTime)) {
			sb.append(" and LOAD_TIME<?");
			pars[index] = loadTimeEndTime;
			index++;
		}
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString() + " ORDER BY ID DESC",ArrayUtils.copyArray(index, pars));
		
		return p;
		
	}
	
	/**
	 * 根据传入信息，以分页的方式查询数据
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param orderId
	 * @param channelSource
	 * @param customerName
	 * @param customerTel
	 * @param brand
	 * @param productName
	 * @param state
	 * @param respond
	 * @param createTimeStartTime
	 * 					创建时间的开始时间
	 * @param createTimeEndTime
	 * 					创建时间的结束时间
	 * @param loadTimeStartTime
	 * 					外呼时间的开始时间
	 * @param loadTimeEndTime
	 * 					外呼时间的结束时间
	 * @return
	 */
	public Map getBSHOrderListByPaginateToMap(int pageNumber,int pageSize,String orderId,String channelSource,String customerName,String customerTel,String brand,String productName,String state,String respond,String timeType,String createTimeStartTime,String createTimeEndTime,String loadTimeStartTime,String loadTimeEndTime) {
		
		Page<Record> p = getBSHOrderListByPaginate(pageNumber, pageSize, orderId,channelSource,customerName, customerTel,brand,productName,state,respond,timeType,createTimeStartTime, createTimeEndTime,loadTimeStartTime,loadTimeEndTime);
		
		int total = p.getTotalRow();    //获取查询出来的总数量
		
		List<Record> newList = new ArrayList<Record>();
		
		for(Record r:p.getList()) {
			
			//客户回复结果描述
			int respondResult = r.getInt("RESPOND");
			r.set("RESPOND_DESC",MemoryVariableUtil.getDictName("BSH_CLIENT_RESPOND", String.valueOf(respondResult)));
			
			//外呼状态描述
			int stateResult = r.getInt("STATE");
			r.set("STATE_DESC", MemoryVariableUtil.getDictName("BSH_CALL_STATE", String.valueOf(stateResult)));
			
			//购物平台
			int channelSourceResult = r.getInt("CHANNEL_SOURCE");
			r.set("CHANNEL_SOURCE_DESC", MemoryVariableUtil.getDictName("BSH_CHANNEL_SOURCE", String.valueOf(channelSourceResult)));
			
			//品牌描述
			int brandResult = r.getInt("BRAND");
			r.set("BRAND_DESC", MemoryVariableUtil.getDictName("BSH_BRAND", String.valueOf(brandResult)));
			
			//时间类型描述
			int timeTypeResult = r.getInt("TIME_TYPE");
			r.set("TIME_TYPE_DESC", MemoryVariableUtil.getDictName("BSH_TIME_TYPE", String.valueOf(timeTypeResult)));
			
			//货物描述
			int productNameResult = r.getInt("PRODUCT_NAME");
			r.set("PRODUCT_NAME_DESC", MemoryVariableUtil.getDictName("BSH_PRODUCT_NAME", String.valueOf(productNameResult)));
			
			r.set("RETRIED", r.getInt("RETRIED") + "/" + BSHCallParamConfig.getRetryTimes());
			
			newList.add(r);
			
		}
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", newList);
		
		return map;
		
	}
	
	/**
	 * 新增一个订单信息
	 * 
	 * @param bshOrderList
	 * @return
	 */
	public boolean add(BSHOrderList bshOrderList) {
		
		Record nr = new Record();
		nr.set("ORDER_ID", bshOrderList.get("ORDER_ID"));
		nr.set("CHANNEL_SOURCE", bshOrderList.getInt("CHANNEL_SOURCE"));
		nr.set("CUSTOMER_NAME", bshOrderList.get("CUSTOMER_NAME"));
		nr.set("CUSTOMER_TEL", bshOrderList.get("CUSTOMER_TEL"));
		nr.set("EXPECT_INSTALL_DATE", bshOrderList.get("EXPECT_INSTALL_DATE"));
		nr.set("BRAND", bshOrderList.getInt("BRAND"));
		nr.set("PRODUCT_NAME", bshOrderList.getInt("PRODUCT_NAME"));
		nr.set("TIME_TYPE", bshOrderList.getInt("TIME_TYPE"));
		nr.set("CREATE_TIME", bshOrderList.get("CREATE_TIME"));
		nr.set("PROVINCE", bshOrderList.get("PROVINCE"));
		nr.set("CITY", bshOrderList.get("CITY"));
		nr.set("CALLOUT_TEL", bshOrderList.get("CALLOUT_TEL"));
		nr.set("STATE", bshOrderList.get("STATE"));
		
		return add(nr);
	}
	
	public boolean add(Record bshOrderList) {
		return Db.save("bsh_orderlist", "ID", bshOrderList);
	}
	
	/**
	 * 修改
	 */
	public boolean update() {
		return false;
	}
	
	/**
	 * 根据传入的订单信息删除记录
	 * 
	 * @param bshOrderList
	 * @return
	 */
	public boolean delete(BSHOrderList bshOrderList) {
		
		boolean b = false;
		
		if(BlankUtils.isBlank(bshOrderList)) {
			return b;
		}
		
		int id = bshOrderList.getInt("ID");
		
		b = deleteById(id);
		
		return b;
	}
	
	
	/**
	 * 当通道状态出现错误时，修改呼结果状态
	 * 
	 * 通道出现错误的表现是：有时通道已经接通，但 avaya 返回的结果仍为 NOANSWER 
	 * 
	 * 所以要通过该方法，强制将呼叫状态修改一下
	 * 
	 * @return
	 */
	public boolean modifyStateWhenChannelStateWrong() {
		
		boolean b = false;
		return b;
	}
	
	
	/**
	 * 修改订单记录的呼叫状态
	 * 
	 * @param id
	 * 			ID
	 * @param oldState
	 * 			旧状态
	 * @param newState
	 * 			新状态
	 * @param lastCallResult
	 * 			最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * 			可以为空,为空时，将不更改其最后一次外呼结果
	 * @return
	 */
	public int updateBSHOrderListState(int id,String oldState,String newState,String lastCallResult) {
		
		int count = 0;
		
		if(BlankUtils.isBlank(newState)) {
			return count;
		}
		
		if(id > 0 && !BlankUtils.isBlank(oldState)) {     //两者都不为空时
			
			if(BlankUtils.isBlank(lastCallResult)) {
				String sql = "update bsh_orderlist set STATE=? where ID=? and STATE=?";
				count = Db.update(sql, newState,id,oldState);
			}else {
				String sql = "update bsh_orderlist set STATE=?,LAST_CALL_RESULT where ID=? and STATE=?";
				count = Db.update(sql, newState,lastCallResult,id,oldState);
			}
			return count;
		}else if(id > 0 && BlankUtils.isBlank(oldState)) {           //如果号码ID不为空，但是旧状态为空时
			if(BlankUtils.isBlank(lastCallResult)) {
				String sql = "update bsh_orderlist set STATE=? where ID=?";
				count = Db.update(sql,newState,id);
			}else {
				String sql = "update bsh_orderlist set STATE=?,LAST_CALL_RESULT=? where ID=?";
				
				count = Db.update(sql,newState,lastCallResult,id);
			}
			return count;
		}else if(id<=0 && !BlankUtils.isBlank(oldState)) {  //号码为空,但旧状态不为空时
			
			if(BlankUtils.isBlank(lastCallResult)) {
				String sql = "update bsh_orderlist set STATE=? where STATE=?";
			
				count = Db.update(sql,newState,oldState);
			}else {
				String sql = "update bsh_orderlist set STATE=?,LAST_CALL_RESULT=? where STATE=?";
				
				count = Db.update(sql,newState,lastCallResult,oldState);
			}
		}
		return count;
	}
	
	/**
	 * 更换号码的状态为重试
	 * 
	 * @param id
	 * 			ID
	 * @param newState
	 * 			新状态
	 * @param retryInterval
	 * 			重试间隔
	 * @param lastCallResult
	 * 				最后一次外呼的结果：NOANSWER(未接);FAILURE(失败);BUSY(线忙);SUCCESS(成功)
	 * @return
	 */
	public boolean updateBSHOrderListStateToRetry(int id,String newState,int retryInterval,String lastCallResult) {
		
		boolean b = false;
		
		if(id <=0 && BlankUtils.isBlank(newState) && retryInterval<=0) {
			return false;
		}
		
		long currTimeMillis = DateFormatUtils.getTimeMillis();   			 //当前时间的毫秒数
		long retryTimeMillis = currTimeMillis + retryInterval * 60 * 1000;   //重试时的毫秒数
		
		//还有一种情况要考虑，如果当前时间与系统的生效时间小于半个时间差时，将下次呼叫修改为一分钟后
		int compareResult = BSHCallParamConfig.compareCurrTime2ActiveEndTime();
		if(compareResult == 2) {     //如果距离结束小于30分钟时，下次呼叫时间为一分钟后
			retryTimeMillis = currTimeMillis + 1 * 60 * 1000;
		}
		
		String nextCallOutTime = DateFormatUtils.formatDateTime(new Date(retryTimeMillis),"yyyy-MM-dd HH:mm:ss");
		
		String sql = "update bsh_orderlist set STATE=?,NEXT_CALLOUT_TIME=?,LAST_CALL_RESULT=? where ID=?";
		
		int count = Db.update(sql, newState,nextCallOutTime,lastCallResult,id);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 更新回复的值,同时强制将呼叫状态，更改为2，即呼叫成功
	 * 
	 * 当有客户回复时，必然是执行到了AGI流程，外呼必定是成功的
	 * 
	 * @param id
	 * @param respond
	 * @return
	 */
	public boolean updateBSHOrderListRespond(String id,String respond) {
		
		boolean  b = false;
		
		String sql = "update bsh_orderlist set RESPOND=?,STATE=2 where ID=?";
		
		int count = Db.update(sql, respond,id);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
		
	}
	
	/**
	 * 更新回复的值,同时强制将呼叫状态，更改为2，即呼叫成功,     同时还需要将通话时长加入
	 * 
	 * 当有客户回复时，必然是执行到了AGI流程，外呼必定是成功的
	 * 
	 * @param id
	 * @param respond
	 * @param billsec
	 * @return
	 */
	public boolean updateBSHOrderListRespondAndBillsec(String id,String respond,int billsec) {
		
		boolean  b = false;
		
		String sql = "update bsh_orderlist set RESPOND=?,STATE=2,BILLSEC=? where ID=?";
		
		int count = Db.update(sql, respond,billsec,id);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
		
	}
	
	
	/**
	 * 根据ID，删除订单信息
	 * 
	 * @return
	 */
	public boolean deleteById(int id) {
		
		boolean b = false;
		
		int count = 0;
		
		count = Db.update("delete from bsh_orderlist where ID=?",id);
		
		if(count > 0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 根据ID,取出一条订单记录
	 * 
	 * @param id
	 * @return
	 */
	public BSHOrderList getBSHOrderListById(String id) {
		
		BSHOrderList bol = findFirst("select * from bsh_orderlist where ID=?", Integer.valueOf(id));
		
		return bol;
		
	}
	
	/**
	 * 载入一定数量的订单数据
	 * 
	 * 查询外呼数据时，需要遵循以下的条件：
	 * (1) 状态为0,即是”未处理“状态
	 * (2) CALLOUT_TEL 不能为空
	 * (3) EXPECT_INSTALL_DATE 不能小于或是等于当天
	 * (4) 只能查询当天的记录(这个条件去掉)，因为前天晚上添加的数据，还是有可能需要第二天呼叫的
	 * 
	 * @param loadCount
	 * 			数量
	 * @return
	 */
	public List<BSHOrderList> loadOrderListToQueue(int loadCount) {
		
		//为了查询的效率,只能分两步实现，先查询出来ID，然后再根据ID查询记录
		String sql = "select ID from bsh_orderlist where STATE=0 and CALLOUT_TEL is not NULL and EXPECT_INSTALL_DATE>? ORDER BY ID ASC limit ?";
		
		List<Record> list = Db.find(sql,DateFormatUtils.formatDateTime(new Date(),"yyyy-MM-dd"),loadCount);
		
		String ids = "";     //定义一个ids
		for(Record r:list) {        
			ids += r.get("ID") + ",";
		}
		
		if(!BlankUtils.isBlank(list) && list.size() > 0) {      //去掉最后的逗号
			ids = ids.substring(0, ids.length()-1);
		}
		
		//如果没有时，返回空
		if(BlankUtils.isBlank(ids)) {
			return null;
		}
		
		//根据ids取出数据
		String sql2 = "select * from bsh_orderlist where ID in(" + ids + ")";
		
		//修改数据呼叫状态为 1，状态是：已载入（即已经加入排队机），同时将已试次数加1
		String sql3 = "update bsh_orderlist set STATE=1,LOAD_TIME=?,RETRIED=RETRIED+1,LAST_CALL_RESULT='',RESPOND=0,CALLRESULT_JSON='',FEEDBACK_CALLRESULT_RESPOND='' where ID in(" + ids + ")";
		
		int count = Db.update(sql3, DateFormatUtils.getCurrentDate());
		List<BSHOrderList> bshOrderList = find(sql2);
		
		
		return bshOrderList;
	}
	
	/**
	 * 载入一定数量的待重呼数据订单数据
	 * 
	 * 查询待重呼外呼数据时，需要遵循以下的条件：
	 * (1) 状态为3,即是”待重呼“状态
	 * (2) CALLOUT_TEL 不能为空
	 * (3) EXPECT_INSTALL_DATE 不能小于或是等于当天
	 * (4) 只能查询当天的记录(去除)
	 * (5) 当前时间要大于下次外呼时间
	 * 
	 * @param loadCount
	 * 			数量
	 * @return
	 */
	public List<BSHOrderList> loadOrderListRetryToQueue(int loadCount) {
		
		//为了查询的效率,只能分两步实现，先查询出来ID，然后再根据ID查询记录
		String sql = "select ID from bsh_orderlist where STATE=3 and CALLOUT_TEL is not NULL and EXPECT_INSTALL_DATE>? and NEXT_CALLOUT_TIME<? ORDER BY ID ASC limit ?";
		
		String currDateTimeStr = DateFormatUtils.formatDateTime(new Date(),"yyyy-MM-dd HH:mm:ss");
		
		List<Record> list = Db.find(sql,DateFormatUtils.formatDateTime(new Date(),"yyyy-MM-dd"),currDateTimeStr,loadCount);
		
		String ids = "";     //定义一个ids
		for(Record r:list) {        
			ids += r.get("ID") + ",";
		}
		
		if(!BlankUtils.isBlank(list) && list.size() > 0) {      //去掉最后的逗号
			ids = ids.substring(0, ids.length()-1);
		}
		
		//如果没有时，返回空
		if(BlankUtils.isBlank(ids)) {
			return null;
		}
		
		//根据ids取出数据
		String sql2 = "select * from bsh_orderlist where ID in(" + ids + ")";
		
		
		//修改数据呼叫状态为 1，状态是：已载入（即已经加入排队机）
		String sql3 = "update bsh_orderlist set STATE=1,LOAD_TIME=?,RETRIED=RETRIED+1,RESPOND=0,LAST_CALL_RESULT='',CALLRESULT_JSON='',FEEDBACK_CALLRESULT_RESPOND='' where ID in(" + ids + ")";
		
		int count = Db.update(sql3, DateFormatUtils.getCurrentDate());
		
		List<BSHOrderList> bshOrderList = find(sql2);
		
		return bshOrderList;
	}
	
	/**
	 * 更新呼叫结果及服务器收到呼叫结果的响应
	 * 
	 * @param id
	 * 			订单ID
	 * @param callResultJson
	 * 			呼叫结果的Json
	 * @param feedBackCallResultRespond
	 * 			服务器收到呼叫结果的响应
	 */
	public boolean updateCallResultAndFeedBackCallResultRespond(String id,String callResultJson,String feedBackCallResultRespond) {
		
		String sql = "update bsh_orderlist set CALLRESULT_JSON=?,FEEDBACK_CALLRESULT_RESPOND=? where ID=?";
		
		int count = Db.update(sql, callResultJson,feedBackCallResultRespond,id);
		
		if(count > 0) {
			
			return true;
			
		}else {
			return false;
		}
	}
	
	/**
	 * 取得已经超时的订单记录(所谓超时，是指安装日期小于或是等于今天)
	 * 
	 * 所谓超时，即是状态为：0（新建）,3（待重呼）
	 * 当超过20:00 - 23：59：59 之间时,如果状态仍为：0（新建）、3（待重呼）时，而安装日期是小于或是等于当前日期时，将状态修改为放弃外呼
	 * 
	 * 且创建日期小于当前时间
	 * 
	 * @return
	 */
	public List<BSHOrderList> handleTimeOutOrderList() {
		
		//String sql = "select ID from bsh_orderlist where STATE in(0,3) and EXPECT_INSTALL_DATE<=?";
		//理论上只需要按上面注释的条件即可处理超时数据,但是为了效率起见,需要加一个创建时间条件,只处理数据提交五天内的数据
		String sql = "select ID from bsh_orderlist where STATE in(0,3) and EXPECT_INSTALL_DATE<=? and CREATE_TIME>?";
		
		String currDateTimeStr = DateFormatUtils.formatDateTime(new Date(),"yyyy-MM-dd HH:mm:ss");
		
		long fiveDayTimes = 5 * 24 * 3600L;   //5天的秒数
		String fiveDayDateTimeStr = DateFormatUtils.getBeforeSecondDateTime(fiveDayTimes);   //5天前的日期时间
		
		List<Record> list = Db.find(sql,currDateTimeStr,fiveDayDateTimeStr);                 //多加一个条件
		
		String ids = "";     //定义一个ids
		for(Record r:list) {        
			ids += r.get("ID") + ",";
		}
		
		if(!BlankUtils.isBlank(list) && list.size() > 0) {      //去掉最后的逗号
			ids = ids.substring(0, ids.length()-1);
		}
		
		//如果没有时，返回空
		if(BlankUtils.isBlank(ids)) {
			return null;
		}
		
		String sql2 = "select * from bsh_orderlist where ID in(" + ids + ")";
		
		List<BSHOrderList> bshOrderList = find(sql2);
		
		//修改数据呼叫状态为 1，状态是：已载入（即已经加入排队机）
		String sql3 = "update bsh_orderlist set STATE=6 where ID in(" + ids + ")";
		
		int count = Db.update(sql3);     //查出来并将其修改为6，即是放弃呼叫
		
		return bshOrderList;
	}
	
	/**
	 * 根据传入条件信息，查询出所有的数据（无需分页）
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param orderId
	 * @param channelSource
	 * @param customerName
	 * @param customerTel
	 * @param brand
	 * @param productName
	 * @param state
	 * @param respond
	 * @param timeType
	 * @param createTimeStartTime
	 * 					创建时间的开始时间
	 * @param createTimeEndTime
	 * 					创建时间的结束时间
	 * @param loadTimeStartTime
	 * 					外呼时间的开始时间
	 * @param loadTimeEndTime
	 * 					外呼时间的结束时间
	 * @return
	 */
	public List<Record> getBSHOrderListByCondition(String orderId,String channelSource,String customerName,String customerTel,String brand,String productName,String state,String respond,String timeType,String createTimeStartTime,String createTimeEndTime,String loadTimeStartTime,String loadTimeEndTime) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[20];
		int index = 0;
		
		sb.append("select * from bsh_orderlist where 1=1");
		
		//订单ID查询
		if(!BlankUtils.isBlank(orderId)) {
			sb.append(" and ORDER_ID like ?");
			pars[index] = "%" + orderId + "%";
			index++;
		}
		
		//购物平台
		if(!BlankUtils.isBlank(channelSource) && !channelSource.equalsIgnoreCase("empty")) {
			sb.append(" and CHANNEL_SOURCE=?");
			pars[index] = channelSource;
			index++;
		}
		
		//客户姓名查询
		if(!BlankUtils.isBlank(customerName)) {
			sb.append(" and CUSTOMER_NAME like ?");
			pars[index] = "%" + customerName + "%";
			index++;
		}
		
		//客户手机号码查询
		if(!BlankUtils.isBlank(customerTel)) {
			sb.append(" and CUSTOMER_TEL like ?");
			pars[index] = "%" + customerTel + "%";
			index++;
		}
		
		//品牌
		if(!BlankUtils.isBlank(brand) && !brand.equalsIgnoreCase("empty")) {
			sb.append(" and BRAND=?");
			pars[index] = brand;
			index++;
		}
		
		//产品名称查询
		if(!BlankUtils.isBlank(productName) && !productName.equalsIgnoreCase("empty")) {
			sb.append(" and PRODUCT_NAME=?");
			pars[index] = productName;
			index++;
		}
		
		//外呼结果查询
		if(!BlankUtils.isBlank(state) && !state.equalsIgnoreCase("empty")) {
			sb.append(" and STATE=?");
			pars[index] = state;
			index++;
		}
		
		//客户回复
		if(!BlankUtils.isBlank(respond) && !respond.equalsIgnoreCase("empty")) {
			sb.append(" and RESPOND=?");
			pars[index] = respond;
			index++;
		}
		
		//时间类型
		if(!BlankUtils.isBlank(timeType) && !timeType.equalsIgnoreCase("empty")) {
			sb.append(" and TIME_TYPE=?");
			pars[index] = timeType;
			index++;
		}
		
		//创建的开始时间查询
		if(!BlankUtils.isBlank(createTimeStartTime)) {
			sb.append(" and CREATE_TIME>?");
			pars[index] = createTimeStartTime;
			index++;
		}
		
		//创建的结束时间查询
		if(!BlankUtils.isBlank(createTimeEndTime)) {
			sb.append(" and CREATE_TIME<?");
			pars[index] = createTimeEndTime;
			index++;
		}
		
		//外呼时间的开始时间查询
		if(!BlankUtils.isBlank(loadTimeStartTime)) {
			sb.append(" and LOAD_TIME>?");
			pars[index] = loadTimeStartTime;
			index++;
		}
		
		//外呼时间的结束时间查询
		if(!BlankUtils.isBlank(loadTimeEndTime)) {
			sb.append(" and LOAD_TIME<?");
			pars[index] = loadTimeEndTime;
			index++;
		}
		
		sb.append(" ORDER BY ID DESC");
		
		System.out.println("SQL语句是：" + sb.toString() + " createTimeStartTime:" + createTimeStartTime + "  createTimeEndTime:" + createTimeEndTime);
		List<Record> list = Db.find(sb.toString(),ArrayUtils.copyArray(index, pars));
		List<Record> newList = new ArrayList<Record>();    //定义该新的列表，主要是用于将某些列的值，转换为文字表达
		
		
		for(Record r:list) {    //遍历，然后将状态值转为文字表达
			
			//客户回复结果描述
			int respondResult = r.getInt("RESPOND");
			r.set("RESPOND_DESC",MemoryVariableUtil.getDictName("BSH_CLIENT_RESPOND", String.valueOf(respondResult)));
			
			//外呼状态描述
			int stateResult = r.getInt("STATE");
			r.set("STATE_DESC", MemoryVariableUtil.getDictName("BSH_CALL_STATE", String.valueOf(stateResult)));
			
			//购物平台
			int channelSourceResult = r.getInt("CHANNEL_SOURCE");
			r.set("CHANNEL_SOURCE_DESC", MemoryVariableUtil.getDictName("BSH_CHANNEL_SOURCE", String.valueOf(channelSourceResult)));
			
			//品牌描述
			int brandResult = r.getInt("BRAND");
			r.set("BRAND_DESC", MemoryVariableUtil.getDictName("BSH_BRAND", String.valueOf(brandResult)));
			
			//货物描述
			int productNameResult = r.getInt("PRODUCT_NAME");
			r.set("PRODUCT_NAME_DESC", MemoryVariableUtil.getDictName("BSH_PRODUCT_NAME", String.valueOf(productNameResult)));
			
			//时间类型描述
			int timeTypeResult = r.getInt("TIME_TYPE");
			r.set("TIME_TYPE_DESC", MemoryVariableUtil.getDictName("BSH_TIME_TYPE", String.valueOf(timeTypeResult)));
			
			r.set("RETRIED_VALUE", r.getInt("RETRIED"));
			r.set("RETRIED", r.getInt("RETRIED") + "/" + BSHCallParamConfig.getRetryTimes());
			
			
			newList.add(r);
			
		}
		
		return newList;
		
	}
	
	/**
	 * 获得统计数据,统计时间段内的统计数据
	 * 
	 * @param startTime
	 * 			外呼时间：开始时间
	 * @param endTime
	 * 			外呼时间：结束时间
	 * @param channelSource
	 * 			购物平台
	 * @return
	 */
	public Record getStatisticsData(String startTime,String endTime,String channelSource) {
		
		Record data = new Record();
		data.set("state1Data",0);
		data.set("state2Data",0);
		data.set("state3Data",0);
		data.set("state4Data",0);
		data.set("state5Data",0);
		data.set("state6Data",0);
		
		data.set("respond1Data", 0);
		data.set("respond2Data", 0);
		data.set("respond3Data", 0);
		data.set("respond4Data", 0);
		data.set("respond5Data", 0);
		
		//(1)取得统计数据（呼叫状态）
		getStatisticsDataForState(data,startTime,endTime,channelSource);
		
		//(2)取得统计数据（客户回复）：客户回复总和=呼叫状态为2（即已成功）的数量
		getStatisticsDataForRespond(data,startTime,endTime,channelSource);
		
		return data;
	}
	
	/**
	 * 取得统计数据（呼叫状态）
	 * 
	 * 取得除了状态0（未处理）的其他所有呼叫状态值
	 * 
	 * @param data
	 * @param startTime
	 * @param endTime
	 * @param channelSource
	 */
	public void getStatisticsDataForState(Record data,String startTime,String endTime,String channelSource) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("select STATE,COUNT(*) as count from bsh_orderlist where STATE!=0");
		
		//外呼时间的开始时间查询
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and LOAD_TIME>?");
			pars[index] = startTime;
			index++;
		}
		
		//外呼时间的结束时间查询
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and LOAD_TIME<?");
			pars[index] = endTime;
			index++;
		}
		
		//购物平台
		if(!BlankUtils.isBlank(channelSource) && !channelSource.equalsIgnoreCase("empty")) {
			sb.append(" and CHANNEL_SOURCE=?");
			pars[index] = channelSource;
			index++;
		}
		
		sb.append(" GROUP BY STATE");
		
		List<Record> stateList = Db.find(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		if(!BlankUtils.isBlank(stateList) && stateList.size() > 0) {
			for(Record stateR:stateList) {
				int stateValue = stateR.getInt("STATE");
				int stateCount = Integer.valueOf(stateR.get("count").toString());
				if(stateValue == 1) {
					data.set("state1Data", stateCount);
				}else if(stateValue == 2) {
					data.set("state2Data", stateCount);
				}else if(stateValue == 3) {
					data.set("state3Data", stateCount);
				}else if(stateValue == 4) {
					data.set("state4Data", stateCount);
				}else if(stateValue == 5) {
					data.set("state5Data", stateCount);
				}else if(stateValue == 6) {
					data.set("state6Data", stateCount);
				}
			}
			
		}
		
	}
	
	/**
	 * 取得统计数据（客户回复）
	 * 
	 * 客户回复总和=呼叫状态为2（即已成功）的数量
	 * 
	 * @param data
	 * @param startTime
	 * @param endTime
	 */
	public void getStatisticsDataForRespond(Record data,String startTime,String endTime,String channelSource) {
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];
		int index = 0;
		
		sb.append("select RESPOND,COUNT(*) as count from bsh_orderlist where STATE=2 and RESPOND!=0");
		
		//外呼时间的开始时间查询
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and LOAD_TIME>?");
			pars[index] = startTime;
			index++;
		}
		
		//外呼时间的结束时间查询
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and LOAD_TIME<?");
			pars[index] = endTime;
			index++;
		}
		
		//购物平台
		if(!BlankUtils.isBlank(channelSource) && !channelSource.equalsIgnoreCase("empty")) {
			sb.append(" and CHANNEL_SOURCE=?");
			pars[index] = channelSource;
			index++;
		}
		
		sb.append(" GROUP BY RESPOND");
		
		List<Record> respondList = Db.find(sb.toString(), ArrayUtils.copyArray(index, pars));
		
		if(!BlankUtils.isBlank(respondList) && respondList.size() > 0) {
			for(Record respondR:respondList) {
				int respondValue = respondR.getInt("RESPOND");
				int respondCount = Integer.valueOf(respondR.get("count").toString());
				if(respondValue == 1) {
					data.set("respond1Data", respondCount);
				}else if(respondValue == 2) {
					data.set("respond2Data", respondCount);
				}else if(respondValue == 3) {
					data.set("respond3Data", respondCount);
				}else if(respondValue == 4) {
					data.set("respond4Data", respondCount);
				}else if(respondValue == 5) {
					data.set("respond5Data", respondCount);
				}
			}
		}
	}
	
	public BSHOrderList getPrev() {
		return prev;
	}

	public void setPrev(BSHOrderList prev) {
		this.prev = prev;
	}

	public BSHOrderList getNext() {
		return next;
	}

	public void setNext(BSHOrderList next) {
		this.next = next;
	}

	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("ID:" + get("ID"));
		sb.append("订单号:" + getStr("ORDER_ID"));
		sb.append("客户姓名:" + getStr("CUSTOMER_NAME"));
		sb.append("客户电话:" + getStr("CUSTOMER_TEL"));
		sb.append("省份:" + getStr("PROVINCE"));
		sb.append("城市:" + getStr("CITY"));
		sb.append("呼出号码:" + getStr("CALLOUT_TEL"));
		
		return sb.toString();
	}

}
