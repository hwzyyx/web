package com.callke8.autocall.flow;

import java.io.File;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callke8.system.param.ParamConfig;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.TTSUtils;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动外呼的流程规则的TTS转换操作
 * 
 * 启动一次，如果已经转换过了，就不再转换，
 * 
 * 如果没有转换过，则转换一次
 * 
 * 规则示例：
      尊敬的自来水用户您好，下面为您播报本期水费对账单。您水表所在地址%s于%s抄见数为%s，月用水量为%s吨，水费为%s元。特此提醒。详情可凭用户号%s登录常州通用自来水公司网站或致电常水热线：88130008查询。
 * 
 * 
 * @author 黄文周
 *
 */
public class AutoFlowTTSJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		/**
		 * 取出所有的流程规则 
		 */
		List<Record> autoFlowList = AutoFlow.dao.getAllAutoFlow();
		
		for(Record autoFlow:autoFlowList) {
			
			String flowIdRs = autoFlow.getStr("FLOW_ID");      					 //流程 ID
			String flowRuleRs = autoFlow.getStr("FLOW_RULE"); 				 	 //流程规则
			//String reminderType = autoFlow.getStr("REMINDER_TYPE");           //催缴类型
			//String flowName = autoFlow.getStr("FLOW_NAME");                   //流程名称
			if(BlankUtils.isBlank(flowRuleRs)) {
				return;
			}
			
			List<Record> list = AutoFlowDetail.dao.getAutoFlowDetailByFlowId(flowIdRs);    //取得流程规则详情列表
			
			if(BlankUtils.isBlank(list) || list.size() == 0) {                  //如果流程详情为空时，则表示没有经过 TTS 处理过的流程规则 
				
				String[] strs = flowRuleRs.split("\\%s");                         //以 %s 分隔
				
				int i = 0;
				
				for(String content:strs) {                                            //将分隔出来的内容存取到详情表
					
					String voiceName = String.valueOf(DateFormatUtils.getTimeMillis() + Math.round(Math.random()*9000 + 1000));   //自定义一个语音文件名
					
					Record autoFlowDetail = new Record();
					
					autoFlowDetail.set("FLOW_ID", flowIdRs);
					autoFlowDetail.set("ORDER_INDEX", i);
					autoFlowDetail.set("CONTENT",content);
					autoFlowDetail.set("VOICE_NAME",voiceName);
					
					AutoFlowDetail.dao.add(autoFlowDetail);    //添加到数据库
					
					i++;
					
				}
				
				list = AutoFlowDetail.dao.getAutoFlowDetailByFlowId(flowIdRs);    //取得流程规则详情列表
				
			}
			
			//检查TTS是否转换成功，如果已经转换成功。
			//如果没有转换，然后再通过 TTS 转换一下
			if(!BlankUtils.isBlank(list) && list.size()>0) {
				
				for(Record autoFlowDetail:list) {    //
					
					int detailId = autoFlowDetail.getInt("DETAIL_ID");         //详情ID
					String flowId = autoFlowDetail.getStr("FLOW_ID");          //流程规则 ID
					int orderIndex = autoFlowDetail.getInt("ORDER_INDEX");     //序号
					String content = autoFlowDetail.getStr("CONTENT");         //内容
					String voiceName = autoFlowDetail.getStr("VOICE_NAME");    //语音文件名字
					
					//查看语音是否存在
					String voicePathFullDir = PathKit.getWebRootPath() + File.separator + ParamConfig.paramConfigMap.get("paramType_4_voicePath") + File.separator + voiceName + ".wav";
					File f = new File(voicePathFullDir);
					
					if(!f.exists()) {     //如果文件不存在，就进行 TTS 转换
						TTSUtils.doTTS(voiceName, content, ParamConfig.paramConfigMap.get("paramType_4_voicePath"), ParamConfig.paramConfigMap.get("paramType_4_voicePathSingle"));
					}
					
				}
				
			}
			
		}
		
	}

}














