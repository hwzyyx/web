package com.callke8.utils;

import java.io.Serializable;

/**
 * 用于自动生成 combobox 的实体类
 * @author hwz
 *
 */
public class ComboboxJson implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String id;     //Id
	private String text;   //菜单名称
	private String desc;   //

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
		
}
