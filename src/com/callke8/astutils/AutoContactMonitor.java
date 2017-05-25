package com.callke8.astutils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.live.LiveException;
import org.asteriskjava.live.OriginateCallback;

import com.callke8.fastagi.autocontact.AutoContactRecord;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Record;

/**
 * 自动接触的守护程序，用于定时扫描 auto_contact_record表，当有记录状态为 0（即新建未外呼）的记录时
 * 
 * 执行外呼，将座席号码与服务号码连接
 * 
 * @author hwz
 *
 */
public class AutoContactMonitor implements Runnable {

	private static Log log = LogFactory.getLog(AstMonitor.class);
	
	public AutoContactMonitor() {
		
	}

	@Override
	public void run() {
		int i = 1;
		
		try {
			Thread.sleep(3 * 1000);   //先停止3秒再扫描
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		while(true) {
			
			//取出所有的未外呼的自动接触的记录
			List<Record> noCallAcrs = AutoContactRecord.dao.scanNoCallAutoContactRecord();
			//List<Record> noCallAcrs = new ArrayList<Record>();
			
			if(!BlankUtils.isBlank(noCallAcrs)&&noCallAcrs.size()>0) {

				//在执行外呼之前，先将已经创建超过三分钟的记录删除，并将其状态修改为4，即超时
				Iterator iter = noCallAcrs.iterator();
				while(iter.hasNext()) {
					
					Record autoContactRecord = (Record)iter.next();
					
					long createTime = autoContactRecord.getDate("CREATE_TIME").getTime();   //取出创建记录的时间戳
					long currentTime = new Date().getTime();                                //创建当前时间的时间戳
					
					long interval = currentTime - createTime;                                //间隔
					
					//System.out.println("当前时间戳：" + currentTime + ",创建时的时间戳:" + createTime + ",时间间隔为：" + interval);
					if(interval > 3 * 60 * 1000) {    //如果时间间隔大于3分钟时，则需要将其去掉，并将状态个性为4，即是超时,同时指定执行时间
						System.out.println("发现已超时的自动接触记录,系统将记录状态修改为4,记录的信息为:" + autoContactRecord);
						AutoContactRecord.dao.updateStatus("4", autoContactRecord.getInt("ID"),false);  
						iter.remove();
					}
				}
				
				log.info("第 " + i + " 次扫描自动接触记录表,有" + noCallAcrs.size() + "条记录需要自动接触");
				
				for(Record autoContactRecord:noCallAcrs) {    //
					
					AutoContactCallOutService service = new AutoContactCallOutService(autoContactRecord);
					service.doCallOut();
					
				}
				
			}else {
				log.info("第 " + i + " 次扫描自动接触记录表,暂无需自动接触的记录!");
			}
			
			if(i == 10){i = 1;} else { i++; };
			try {
				Thread.sleep(1000 * 3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
