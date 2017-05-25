package com.callke8.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TreeJson implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String id;     //Id
	private String text;   //菜单名称
	private String desc;   //
	private String pid;    //父 Id
	private String iconCls;//图标
	private String uri;    //URL
	private String state;
	private boolean checked;
	private List<TreeJson> children = new ArrayList<TreeJson>();

	public static List<TreeJson> formatTree(List<TreeJson> list) {
		
		TreeJson root = new TreeJson();
		TreeJson node = new TreeJson();
		
		List<TreeJson> treelist = new ArrayList<TreeJson>();    //拼凑好的 json 格式的数据
		List<TreeJson> parentnodes = new ArrayList<TreeJson>(); //parentnodes 存放所有的父节点
		
		if(list != null && list.size() > 0) {
			root = list.get(0);
			
			//循环遍历查询的所有节点
			for(int i=1;i<list.size(); i++) {
				
				node = list.get(i);
				if(node.getPid().equals(root.getId())) {
					//为tree root增加子节点
					getChildrenNodes(parentnodes, node);
					parentnodes.add(node);
					root.getChildren().add(node);
				}else {
					//获取root子节点的子节点
					getChildrenNodes(parentnodes, node);
                    parentnodes.add(node);
				}
			}
		}
		treelist.add(root);
		
		return treelist;
		
	}
	
	
	
	private static void getChildrenNodes(List<TreeJson> parentnodes,TreeJson node) {
		
		//循环遍历所有的父节点和node进行匹配，确定父子关系
		for(int i = 0; i < parentnodes.size(); i++) {
			TreeJson pnode = parentnodes.get(i);
			
			//如果
			if(pnode.getPid().equals(node.getId())) {
				node.getChildren().add(pnode);
			}else if(node.getPid().equals(pnode.getId())) {
				pnode.getChildren().add(node);
			}
			
		}
		
	}


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

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getIconCls() {
		return iconCls;
	}

	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}

	public boolean getChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public List<TreeJson> getChildren() {
		return children;
	}

	public void setChildren(List<TreeJson> children) {
		this.children = children;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}



	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}



	public String getUri() {
		return uri;
	}



	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}
