package com.callke8.bsh.bshorderlist;

import com.callke8.bsh.bshcallparam.BSHCallParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.HttpClientUtil;
import com.callke8.utils.StringUtil;

import net.sf.json.JSONObject;

/**
 * 反馈呼叫结果线程，由于请求会占用一定的时间，所以需要以线程的方式返回呼叫结果
 * 
 * @author 黄文周
 *
 */
public class BSHHttpRequestThread implements Runnable {
	
	private BSHCallResultVO bshCallResultVO;
	private String id;
	
	/**
	 * 反馈呼叫结果
	 * 
	 *  * 参数	说明
		orderId	订单号id
		callType	外呼类型0.二次未接通1.一次接通/二次接通2放弃呼叫3已过期
		time	时间（yyyyMMddHHmmss）
		sign	签名（全小写）= md5(time + orderId+ key)key为约定好的密钥
		callResult	外呼结果 1：确认建单   2 暂不安装  3 短信确认   4 错误或无回复  5 放弃呼叫 6已过期
	 * 
	 * @param id
	 * 				订单对应的ID，用于储存反馈提交反馈json及由服务器返回的处理结果
	 * 
	 * @param orderId
	 * 				订单编号
	 * @param callType
	 * 				外呼类型
	 * @param callResult
	 * 				外呼结果
	 */
	public BSHHttpRequestThread(String id,String orderId,String callType,String callResult) {

		this.id = id;
		this.bshCallResultVO = new BSHCallResultVO(orderId, callType, callResult);

	}
	
	@Override
	public void run() {
		
		
		//发送呼叫结果至BSH 平台数据接口
		JSONObject obj = JSONObject.fromObject(this.bshCallResultVO);
		
		String result = HttpClientUtil.doPost(BSHCallParamConfig.getBshCallResultUrl(),obj.toString());
		
		//在提交外呼结果时，可能会出现通讯异常的情况，出现通讯异常时，DOB平台会返回 null, 
		//不管是字符串的 null 还是 NULL,判定标准是，只要不包括 resultCode 或是 resultMsg 两个关键字，都表示通讯异常，需要再提交一次
		
		//如果第一次提交的结果为空,或是不包括 resultCode 时，再提交一次
		if(BlankUtils.isBlank(result) || !StringUtil.containsAny(result, "resultCode")) {     
			result = HttpClientUtil.doPost(BSHCallParamConfig.getBshCallResultUrl(),obj.toString());
		}
		
		String callResultJson = this.bshCallResultVO.toString();
		String feedBackCallResultRespond = result;
		
		boolean b = BSHOrderList.dao.updateCallResultAndFeedBackCallResultRespond(id, callResultJson, feedBackCallResultRespond);
		
		System.out.println("将呼叫结果json数据:" + this.bshCallResultVO.toString() + " 反馈给BSH数据平台,BSH数据平台返回的结果是:" + result);
		
	}

}
