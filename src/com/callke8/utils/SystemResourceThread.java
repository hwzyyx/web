package com.callke8.utils;

import java.text.DecimalFormat;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

import com.jfinal.plugin.activerecord.Record;

public class SystemResourceThread extends Thread {

	public static Record systemResourceDataRecord = new Record();
	
	public SystemResourceThread() {
		
	}
	
	public void run() {
		
		String osName = System.getProperty("os.name");     //系统信息
		String systemType = null;                          //系统类型
		if(osName.toLowerCase().startsWith("win")) {       //如果系统以 win 开头，则表示是 window 系统
			systemType = "windows";
		}else {
			systemType = "linux";
		}
		
		DecimalFormat df = new DecimalFormat("0.0");   //定义精度
		
		while(true) {
			
			Sigar sigar = new Sigar();
			
			
			try {
				
				//CPU信息
				CpuInfo[] infos = sigar.getCpuInfoList();
				systemResourceDataRecord.set("cpuInfo", infos[0].getVendor() + " " + infos[0].getModel());
				
				//CPU 处理速度
				systemResourceDataRecord.set("cpuSpeed",infos[0].getMhz() + "M");
				
				//CPU 百分比
				CpuPerc cpu = sigar.getCpuPerc();     //取CPU信息
				//String cpuValue = df.format(cpu.getSys() * 100);
				String cpuValue = df.format(cpu.getUser() * 100);
				
				//MEM 内存信息
				Mem mem = sigar.getMem();
				
				String ramValue = df.format(mem.getUsedPercent());
				
				systemResourceDataRecord.set("cpuValue",cpuValue);
				systemResourceDataRecord.set("ramValue",ramValue);
				
				//开机时间
				systemResourceDataRecord.set("upTime",DateFormatUtils.getDayHourMinuteBySeconds((int)sigar.getUptime().getUptime()));
				
				//RAM 及 SWAP
				Swap swap = sigar.getSwap();
				String ramTotal = mem.getTotal()/(1024L * 1024L) + " Mb";
				String swapTotal = swap.getTotal()/(1024L * 1024L) + " Mb";
				systemResourceDataRecord.set("memoryUsage", "RAM:" + ramTotal + "   Swap:" + swapTotal);
				
			} catch (SigarException se) {
				se.printStackTrace();
			}
			
			try {
				Thread.sleep(1 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}
	
}
