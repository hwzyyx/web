package com.callke8.predialqueue;

import com.callke8.autocall.autocalltask.AutoCallTaskTelephone;
import com.callke8.utils.BlankUtils;

/**
 * 排队机管理,主要是用于管理进入排队及从排队机中取出
 * 
 * @author hwz
 */
public class QueueMachineManager {
	
	//开始定义一个空排队机,且是静态排队机
	public static AutoCallTaskTelephone queue = null;
	public static Integer queueCount = 0;

	/**
	 * 将号码加入排队机,采取的是先进先出的原则
	 * 
	 * @param q
	 * @return
	 */
	public static AutoCallTaskTelephone enQueue(AutoCallTaskTelephone q) {
		
		if(BlankUtils.isBlank(q)) {   //如果传入的为空,则返回空
			return null;
		}
		
		AutoCallTaskTelephone node = queue;   //定义一个节点,为前面定义排队机
		
		if(node == null) {   //如果排除机为空
			queue = q;
			queueCount++;    //插入排队机时,排队机中的数量加1
			return queue;
		}
		
		//如果不为空时,限于先进先出的原则,所以新加入的号码,要插入到最底下
		while(node != null) {
			
			if(node.getNext() == null) {   //如果node的下一个为空时,node的下一个,就可以设置为新加入的号码了
				node.setNext(q);
				break;   //跳出循环
			}else {
				node = node.getNext();
			}
			
		}
		queueCount++;   //插入排队机时,排队机中的数量加1
		return queue;
	}
	
	/**
	 * 从排队机中取出一个号码
	 * 		排队机取出号码时，是根据先进先出的原则
	 * 
	 * @return
	 */
	public static AutoCallTaskTelephone deQueue() {   
		
		AutoCallTaskTelephone p;
		
		if(queue == null) {   //如果定义排队机为空时,返回空
			return null;
		}
		
		//由于采取先进先出的原则,新增的号码是处于底部的，所以只要取出最顶部的即是先存进排队机的
		p = queue;    
		
		queue = queue.getNext();   //取出了之后,最顶部将指向下一个
		
		if(!BlankUtils.isBlank(p)) {    //如果取出时不为空,则要将排队机减1
			if(queueCount>0) {
				queueCount--;
			}else {
				queueCount=0;
			}
		}
		
		return p;
	}
	
}
