package com.callke8.astutils;

import java.util.Map;

import com.callke8.call.calltask.CallTaskCounter;
import com.jfinal.plugin.activerecord.Db;

/**
 * 外呼任务的计数工具，主要是用于根据任务ID、状态标识对任务的数量进行增加与减少
 * 
 * @author Administrator
 *
 */
public class CallTaskCounterUtils {
	
	/**
	 * 根据 taskId 和状态值，根据偏移量增加数量
	 * 
	 * @param taskId
	 * 			任务ID
	 * @param callState
	 * 			呼叫状态
	 * @param offset
	 * 			偏移量
	 * @return
	 */
	public static boolean increaseCounter(int taskId,String callState,int offset) {
		
		boolean b = false;
		
		//在增加数量之前，先检查是否已经存在该任务的状态记录
		boolean b1 = CallTaskCounter.dao.isExistCounter(taskId, callState);
		
		if(b1) {      //如果存在相关状态记录时，直接在原有的记录上增加数量即可
			
			b = CallTaskCounter.dao.increaseCounter(taskId, callState, offset);   //在原记录的基础上直接增加记录
			
		}else {       //如果不存在关状态记录时，需要先增加新记录
			
			b = CallTaskCounter.dao.createCounter(taskId, callState, offset);     //直接创建记录即可
			
		}
		
		
		return b;
	}
	
	/**
	 * 根据 taskId 和状态值，根据偏移量减少数量
	 * 
	 * @param taskId
	 * 			任务ID
	 * @param callState
	 * 			呼叫状态
	 * @param offset
	 * 			偏移量
	 * @return
	 */
	public static boolean reduceCounter(int taskId,String callState,int offset) {
		
		boolean b = false;
		
		//在增加数量之前，先检查是否已经存在该任务的状态记录
		boolean b1 = CallTaskCounter.dao.isExistCounter(taskId, callState);
		
		if(b1) {     //存在时，在原记录直接减去数量即可
			
			//在减去数量之前，需要先取出原来的数量，与 offset对比，只有大于或是等于 offset 时，才允许相减，否则会出现记数为负数的情况
			int count = CallTaskCounter.dao.getCount(taskId, callState);
			
			if(count < offset) {     //如果取出来的数量小于时，不允许相减
				b = false;
				return b;
			}
			
			b = CallTaskCounter.dao.reduceCounter(taskId, callState, offset);   //直接减去数量
			
		}
		return b;
	}
	
	/**
	 * 根据任务ID,及状态，取得数量
	 * 
	 * @param taskId
	 * 			任务ID
	 * @param callState
	 * 			任务状态
	 * @return
	 */
	public static int getCount(int taskId,String callState) {
		
		int count = CallTaskCounter.dao.getCount(taskId, callState);
		
		return count;
	}
	
	/**
	 * 根据任务ID，删除所有的记数，主要用于在删除任务时，清除该任务的记数器
	 * 
	 * @param taskId
	 * @return
	 */
	public static boolean deleteByTaskId(int taskId) {
		
		boolean b = false;
		
		b = CallTaskCounter.dao.deleteByTaskId(taskId);
		
		return b;
	}
	
	/**
	 * 根据任务ID，取出任务的计数情况，并以 Map<String,Integer> 的方式返回，其他 String 为状态值， Integer 为状态值的数量
	 * 
	 * @param taskId
	 * 			任务的ID
	 * @return
	 */
	public static Map<String,Integer> getCounterByTaskId(int taskId) {
		return CallTaskCounter.dao.getCounterByTaskId(taskId);
	}
	
}
