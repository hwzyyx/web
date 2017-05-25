package com.callke8.common;

import com.jfinal.core.Controller;

/**
 * 先创建一个控制器接口，主要是制定控制器的默认的方法
 * 
 * @author Administrator
 *
 */
public interface IController {
	/**
	 * index
	 */
	public void index();
	
	/**
	 * 查询数据
	 */
	public void datagrid();
	
	/**
	 * 添加数据
	 */
	public void add();
	
	/**
	 * 修改数据
	 */
	public void update();
	
	/**
	 * 删除数据
	 */
	public void delete();
	
}
