package com.callke8.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Record;

public class CreateFileUtils {
	
	//list.jsp 和 _form.jsp 页面使用的 class 类型，即是 class="easyui-XXXX" 在这里定义,根据传入的mysql数据库类型（如 int,varchar,date,datetime,text） 获取相应的 easyui 类型
	public static Map<String,String> easyuiType = new HashMap<String,String>();    
	//在 controller 和 dao 中，获取参数时，定义数据类型，根据传入的mysql数据库类型（如 int,varchar,date,datetime,text）,定义相应的数据类型，如 String,   int 等。
	public static Map<String,String> dataType = new HashMap<String,String>();
	
	static {
		easyuiType.put("int","numberbox");
		easyuiType.put("varchar","textbox");
		easyuiType.put("date","datebox");
		easyuiType.put("datetime","datetimebox");
		easyuiType.put("text","textbox");
		
		dataType.put("int", "int");
		dataType.put("varchar", "String");
		dataType.put("date", "String");
		dataType.put("datetime", "String");
		dataType.put("text", "String");
		
	}
	
	public static void createListJsp(String title,String tableName,List<Record> paramList,List<Record> columnList,String idColumn,String urlInfo,String path,String formFullPath) {
		
		String br = "\r\n";
		File f = null;
		FileWriter fw = null;
		
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>" + br);
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" + br);
		sb.append("<html>" + br);
		sb.append(t(1) + "<head>" + br);
		sb.append(t(2) + "<title>" + title + "</title>" + br);
		sb.append(t(2) + "<style>" + br);
		sb.append(t(3) + ".font17{" + br);
		sb.append(t(4) + "font-size: 17px;" + br);
		sb.append(t(3) + "}" + br);
		sb.append(t(2) + "</style>" + br);
		sb.append(t(2) + "<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/default/easyui.css\">" + br);
		sb.append(t(2) + "<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/color.css\">" + br);
		sb.append(t(2) + "<link rel=\"stylesheet\" type=\"text/css\" href=\"themes/icon.css\">" + br);
		sb.append(t(2) + "<link rel=\"stylesheet\" type=\"text/css\" href=\"demo.css\">" + br);
		sb.append(t(2) + "<link rel=\"stylesheet\" type=\"text/css\" href=\"jplayer/dist/skin/blue.monday/css/jplayer.blue.monday.hwzcustom.css\">" + br);
		sb.append(t(2) + "<link rel=\"stylesheet\" type=\"text/css\" href=\"iconfont/iconfont.css\">" + br);
		sb.append(t(2) + "<script src=\"echarts/echarts.min.js\"></script>" + br);
		sb.append(t(2) + "<script src=\"iconfont/iconfont.js\"></script>" + br);
		sb.append(t(2) + "<script type=\"text/javascript\" src=\"jquery.min.js\"></script>" + br);
		sb.append(t(2) + "<script type=\"text/javascript\" src=\"jquery.easyui.min.js\"></script>" + br);
		sb.append(t(2) + "<script type=\"text/javascript\" src=\"js.date.utils.js\"></script>" + br);
		sb.append(t(2) + "<script type=\"text/javascript\" src=\"jplayer/dist/jplayer/jquery.jplayer.hwzcustom.js\"></script>" + br);
		sb.append(t(2) + "<script type=\"text/javascript\" src=\"locale/easyui-lang-zh_CN.js\"></script>" + br);
		sb.append(t(2) + "<script type=\"text/javascript\">" + br);
			//一、$(function(){}) 入口函数
			sb.append(t(3) + "$(function(){" + br);               //jquery的函数入口
				//datagrid 定义
				sb.append(t(4) + "$(\"#" + tableName + "_Dg\").datagrid({" + br);
				sb.append(t(5) + "pageSize:30," + br);
				sb.append(t(5) + "pagination:true," + br);
				sb.append(t(5) + "fit:true," + br);
				sb.append(t(5) + "toolbar:\"#datagridTool\"," + br);
				sb.append(t(5) + "singleSelect:true," + br);
				sb.append(t(5) + "rownumbers:true," + br);
				sb.append(t(5) + "rowrap:true," + br);
				sb.append(t(5) + "striped:true," + br);
				sb.append(t(5) + "pageList:[20,30,50]," + br);
				sb.append(t(5) + "url:'" + urlInfo + "/datagrid'," + br);
				sb.append(t(5) + "queryParams:{" + br);
				if(!BlankUtils.isBlank(paramList) && paramList.size()>0) {
					int pSize = paramList.size();
					int i = 0;
					for(Record paramRecord:paramList) {
						if(i == pSize-1) {
							sb.append(t(6) + paramRecord.getStr("paramName") + ":$(\"#" + paramRecord.getStr("paramName") + "\")." + easyuiType.get(paramRecord.getStr("type")) + "('getValue')" + br);
						}else {
							sb.append(t(6) + paramRecord.getStr("paramName") + ":$(\"#" + paramRecord.getStr("paramName") + "\")." + easyuiType.get(paramRecord.getStr("type")) + "('getValue')," + br);
						}
						i++;
					}
				}
				sb.append(t(5) + "}" + br);
				sb.append(t(4) + "})" + br);
				
				//关闭表单弹出框时操作
				sb.append(t(4) + "$(\"#" + tableName + "_Dlg\").dialog({" + br);
				sb.append(t(5) + "onClose:function() {" + br);
				sb.append(t(6) + "$(\"#" + tableName + "_Form\").form('clear');" + br);
				sb.append(t(5) + "}" + br);
				sb.append(t(4) + "});" + br);
				
			
			sb.append(t(3) + "});" + br);                         //jquery的结束函数入口
			
			sb.append(br);
			//二、查询数据
			sb.append(t(3) + "//查询数据" + br);
			sb.append(t(3) + "function findData() {" + br);
			sb.append(t(4) + "$(\"#" + tableName + "_Dg\").datagrid('load',{" + br);
			if(!BlankUtils.isBlank(paramList) && paramList.size()>0) {
				int pSize = paramList.size();
				int i = 0;
				for(Record paramRecord:paramList) {
					if(i == pSize-1) {
						sb.append(t(5) + paramRecord.getStr("paramName") + ":$(\"#" + paramRecord.getStr("paramName") + "\")." + easyuiType.get(paramRecord.getStr("type")) + "('getValue')" + br);
					}else {
						sb.append(t(5) + paramRecord.getStr("paramName") + ":$(\"#" + paramRecord.getStr("paramName") + "\")." + easyuiType.get(paramRecord.getStr("type")) + "('getValue')," + br);
					}
					i++;
				}
			}
			sb.append(t(4) + "});" + br);
			sb.append(t(3) + "}" + br);
			
			//三、编辑的超连接拼接
			sb.append(t(3) + "//编辑的超连接拼接" + br);
			sb.append(t(3) + "function rowformatter(value,data,index) {" + br);
			sb.append(t(4) + "return ");
			sb.append("\"<a href='#' onclick='javascript:doEdit(");
			if(!BlankUtils.isBlank(columnList) && columnList.size()>0) {
				int cSize = columnList.size();
				int i = 0;
				for(Record columnRecord:columnList) {
					if(i == cSize-1) {
						sb.append("\\\"\" + data." + columnRecord.getStr("columnName") + " + \"\\\"");
					}else {
						sb.append("\\\"\" + data." + columnRecord.getStr("columnName") + " + \"\\\",");
					}
				    i++;			
				}
			}
			sb.append(")'><img src='themes/icons/pencil.png' border='0'>编辑</a>");
			sb.append(t(1) + "<a href='#' onclick='javascript:doDel(\\\"\" + data." + idColumn + " +\"\\\")'><img src='themes/icons/cancel.png' border='0'>删除</a>\";" + br);
			sb.append(t(3) + "}" + br);
			
			sb.append(br);
			//四、删除操作
			sb.append(t(3) + "//删除操作" + br);
			sb.append(t(3) + "function doDel(id) {" + br);
			sb.append(t(4) + "$.messager.confirm('提示','你确定要删除选中的记录吗?',function(r){" + br);
				sb.append(t(5) + "if(r) {" + br);
					sb.append(t(6) + "$(\"#" + tableName + "_Form\").form('submit',{" + br);
						sb.append(t(7) + "url:\"" + urlInfo + "/delete?id=\" + id," + br);
						sb.append(t(7) + "onSubmit:function(){" + br);
						sb.append(t(7) + "}," + br);
						sb.append(t(7) + "success:function(data) {" + br);
							sb.append(t(8) + "var result = JSON.parse(data);    //解析Json 数据" + br);
							sb.append(t(8) + "var statusCode = result.statusCode; //返回的结果类型" + br);
							sb.append(t(8) + "var message = result.message;       //返回执行的信息" + br);
							sb.append(br);
							sb.append(t(8) + "window.parent.showMessage(message,statusCode);" + br);
							sb.append(t(8) + "if(statusCode == 'success') {         //保存成功时" + br);
								sb.append(t(9) + "findData();" + br);
							sb.append(t(8) + "}" + br);
						sb.append(t(7) + "}" + br);
					sb.append(t(6) + "});" + br);
				sb.append(t(5) + "}" + br);
			sb.append(t(4) + "});" + br);
			sb.append(t(3) + "}" + br);
			
			sb.append(br);
			//五、编辑操作
			sb.append(t(3) + "//编辑操作" + br);
			sb.append(t(3) + "function doEdit(");
			if(!BlankUtils.isBlank(columnList) && columnList.size()>0) {
				int cSize = columnList.size();
				int i = 0;
				for(Record columnRecord:columnList) {
					if(i == cSize-1) {
						sb.append(columnRecord.getStr("columnNameCamel"));
					}else {
						sb.append(columnRecord.getStr("columnNameCamel") + ",");
					}
				    i++;			
				}
			}
			sb.append("){" + br);
			
			sb.append(t(4) + "$(\"#saveBtn\").attr(\"onclick\",\"saveEdit()\");" + br);
			sb.append(t(4) + "$(\"#" + tableName + "_Dlg\").dialog(\"open\").dialog(\"setTitle\",\"编辑\");" + br);
			
			sb.append(t(4) + "$(\"#" + tableName + "_Form\").form('load',{" + br);
				if(!BlankUtils.isBlank(columnList) && columnList.size()>0) {
					int cSize = columnList.size();
					int i = 0;
					for(Record columnRecord:columnList) {
						if(i == cSize-1) {
							sb.append(t(5) + "'" + tableName + "." + columnRecord.getStr("columnName") + "':" + columnRecord.getStr("columnNameCamel") + br);
						}else {
							sb.append(t(5) + "'" + tableName + "." + columnRecord.getStr("columnName") + "':" + columnRecord.getStr("columnNameCamel") + "," + br);
						}
					    i++;			
					}
				}
			sb.append(t(4) + "});" + br);
			
			sb.append(t(3) + "}" + br);
			
			//六、保存编辑
			sb.append(t(3) + "//编辑操作" + br);
			sb.append(t(3) + "function saveEdit() {" + br);
				sb.append(t(4) + "$(\"#" + tableName + "_Form\").form('submit',{" + br);
					sb.append(t(5) + "url:\"" + urlInfo + "/update\"," + br);
					sb.append(t(5) + "onSubmit:function(){" + br);
						sb.append(t(6) + "var v = $(this).form('validate');" + br);
						sb.append(t(6) + "if(v) {" + br);
							sb.append(t(7) + "$.messager.progress({" + br);
								sb.append(t(8) + "msg:'系统正在处理，请稍候...'," + br);
								sb.append(t(8) + "interval:3000" + br);
							sb.append(t(7) + "});" + br);
						sb.append(t(6) + "}" + br);
						sb.append(t(6) + "return $(this).form('validate');" + br);
					sb.append(t(5) + "}," + br);
					sb.append(t(5) + "success:function(data) {" + br);
						sb.append(t(6) + "$.messager.progress(\"close\");" + br);
						sb.append(t(6) + "var result = JSON.parse(data);    //解析Json 数据" + br);
						sb.append(t(6) + "var statusCode = result.statusCode; //返回的结果类型" + br);
						sb.append(t(6) + "var message = result.message;       //返回执行的信息" + br);
						sb.append(t(6) + "window.parent.showMessage(message,statusCode);" + br);
						sb.append(t(6) + "if(statusCode == 'success') {         //保存成功时" + br);
							sb.append(t(7) + "findData();" + br);
							sb.append(t(7) + "$('#" + tableName + "_Dlg').dialog('close');//关闭对话框" + br);
						sb.append(t(6) + "}" + br);
					sb.append(t(5) + "}" + br);
				sb.append(t(4) + "});" + br);
			sb.append(t(3) + "}" + br);
			
			sb.append(br);
			//七、添加操作
			sb.append(t(3) + "function doAdd() {" + br);
				sb.append(t(4) + "$(\"#saveBtn\").attr(\"onclick\",\"saveAdd()\");" + br);
				
				sb.append(t(4) + "$(\"#" + tableName + "_Dlg\").dialog(\"setTitle\",\"添加\").dialog(\"open\");" + br);
				
			sb.append(t(3) + "}" + br);
			
			sb.append(br);
			//八、保存添加
			sb.append(t(3) + "function saveAdd() {" + br);
				sb.append(t(4) + "$(\"#" + tableName + "_Form\").form(\"submit\",{" + br);
					sb.append(t(5) + "url:\"" + urlInfo + "/add\"," + br);
					sb.append(t(5) + "onSubmit:function() {" + br);
						sb.append(t(6) + "var v = $(this).form('validate');" + br);
						sb.append(t(6) + "if(v) {" + br);
							sb.append(t(7) + "$.messager.progress({" + br);
								sb.append(t(8) + "msg:'系统正在处理，请稍候...'," + br);
								sb.append(t(8) + "interval:3000" + br);
							sb.append(t(7) + "});" + br);
						sb.append(t(6) + "}" + br);
						sb.append(t(6) + "return $(this).form('validate');" + br);
					sb.append(t(5) + "}," + br);
						sb.append(t(5) + "success:function(data) {" + br);
						sb.append(t(6) + "$.messager.progress(\"close\");" + br);
						sb.append(t(6) + "var result = JSON.parse(data);    //解析Json 数据" + br);
						sb.append(t(6) + "var statusCode = result.statusCode; //返回的结果类型" + br);
						sb.append(t(6) + "var message = result.message;       //返回执行的信息" + br);
						sb.append(t(6) + "window.parent.showMessage(message,statusCode);" + br);
						sb.append(t(6) + "if(statusCode == 'success') {         //保存成功时" + br);
							sb.append(t(7) + "findData();" + br);
							sb.append(t(7) + "$('#" + tableName + "_Dlg').dialog('close');//关闭对话框" + br);
						sb.append(t(6) + "}" + br);
					sb.append(t(5) + "}" + br);
				sb.append(t(4) + "});" + br);
			sb.append(t(3) + "}" + br);
			
			sb.append(br);
			//九：取消添加或是编辑
			sb.append(t(3) + "function doCancel(){" + br);
				sb.append(t(4) + "$('#" + tableName + "_Dlg').dialog('close');//关闭对话框" + br);
			sb.append(t(3) + "}" + br);
			
		//===========================================
		sb.append(t(2) + "</script>" + br);
		
		sb.append(t(1) + "</head>" + br);
		sb.append("<body>" + br);
		
		sb.append(br);
		//添加BODY的内容
			sb.append(t(1) + "<%@ include file=\"/base_loading.jsp\" %>" + br);
			sb.append(t(1) + "<!-- 定义一个 layout -->" + br);
			sb.append(t(1) + "<div data-options=\"fit:true\" class=\"easyui-layout\">" + br);
			
				sb.append(t(2) + "<!-- 顶部查询区 -->" + br);
				sb.append(t(2) + "<div data-options=\"region:'north',split:true,border:true\" style=\"height:50px;padding-top:5px;padding-left:5px;\">" + br);
					sb.append(t(3) + "<table>" + br);
						sb.append(t(4) + "<tr style=\"vertical-align: top;\">" + br);
							sb.append(t(5) + "<td>" + br);
								//搜索字段的列表
								if(!BlankUtils.isBlank(paramList) && paramList.size()>0) {
									int i = 0;
									for(Record paramRecord:paramList) {
										if(i==0) {
											sb.append(t(6) + paramRecord.getStr("paramLabel") + "：" + "<input id=\"" + paramRecord.getStr("paramName") + "\" class=\"easyui-" + easyuiType.get(paramRecord.getStr("type"))  + "\" style=\"width:200px;\"/>" + br);
										}else {
											sb.append(t(6) + "<span style=\"padding-left:20px;\">" + br);
											sb.append(t(7) + paramRecord.getStr("paramLabel") + "：" + "<input id=\"" + paramRecord.getStr("paramName") + "\" class=\"easyui-" + easyuiType.get(paramRecord.getStr("type")) + "\" style=\"width:200px;\"/>" + br);
											sb.append(t(6) + "</span>" + br);
										}
										i++;
									}
									sb.append(t(6) + "<span style=\"padding-left:20px;\">" + br);
									sb.append(t(7) + "<a href=\"javascript:findData()\" style=\"width:120px;\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-search'\">查询</a>" + br);
									sb.append(t(6) + "</span>" + br);
								}
							sb.append(t(5) + "</td>" + br);
						sb.append(t(4) + "</tr>" + br);
					sb.append(t(3) + "</table>" + br);
				sb.append(t(2) + "</div>" + br);
				
				sb.append(t(2) + "<!-- 数据显示区 -->" + br);
				sb.append(t(2) + "<div data-options=\"region:'center',split:true,border:false\">" + br);
					sb.append(t(3) + "<table id=\"" + tableName + "_Dg\">" + br);
						sb.append(t(4) + "<thead>" + br);
							sb.append(t(5) + "<tr style=\"height:12px;\">" + br);
								for(Record columnRecord:columnList) {
									if(!columnRecord.getStr("columnName").equals(idColumn)) {
										sb.append(t(6) + "<th data-options=\"field:'" + columnRecord.getStr("columnName") + "',width:200,align:'center'\">" + columnRecord.getStr("columnLabel") + "</th>" + br);
									}
								}
								sb.append(t(6) + "<th data-options=\"field:'rowColumn',width:150,align:'center',formatter:rowformatter\">操作</th>" + br);
							sb.append(t(5) + "</tr>" + br);
						sb.append(t(4) + "</thead>" + br);
					sb.append(t(3) + "</table>" + br);
				sb.append(t(2) + "</div>" + br);
				
			sb.append(t(1) + "</div>" + br);
			
			//datagrid的工具，主要是添加
			sb.append(t(1) + "<div id=\"datagridTool\" style=\"padding:5px;\">" + br);
				sb.append(t(2) + "<a href=\"#\" id=\"easyui-add\" onclick=\"doAdd()\" class=\"easyui-linkbutton\" iconCls='icon-add' plain=\"true\">新增</a>" + br);
			sb.append(t(1) + "</div>" + br);
			
			sb.append(br);
			//表单 dialog定义
			sb.append(t(1) + "<div id=\"" + tableName + "_Dlg\" class=\"easyui-dialog\" style=\"width:80%;height:80%;padding:10px 20px;\" modal=\"true\" closed=\"true\" buttons=\"#formBtn\">" + br);
				sb.append(t(2) + "<form id=\"" + tableName + "_Form\" method=\"post\">" + br);
					sb.append(t(3) + "<!-- 包含表单 -->" + br);
					sb.append(t(3) + "<%@ include file=\"" + formFullPath + "\"%>" + br);
				sb.append(t(2) + "</form>" + br);
			sb.append(t(1) + "</div>" + br);
			
		//添加BODY的内容结束
		sb.append(br);
		sb.append("</body>" + br);
		sb.append("</html>" + br);
		
		try {
			
			f = new File(path);
			fw = new FileWriter(f,false);
			
			fw.write(sb.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fw!=null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
	}
	
	public static void createFormJsp(String tableName,List<Record> columnList,String idColumn,String path) {
		
		String br = "\r\n";
		boolean b = false;     //是否已经将id字段的内容插入
		String idColumnInputString = "<input type=\"hidden\" name=\"" + tableName + ".ID\" id=\"" + idColumn + "\"/>";
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>" + br);
		sb.append("<table>" + br);
			if(!BlankUtils.isBlank(columnList) && columnList.size()>0) {
				
				for(Record cr:columnList) {
					String columnName = cr.getStr("columnName");
					String columnLabel = cr.getStr("columnLabel");
					String inputType = easyuiType.get(cr.getStr("type"));
					boolean required = cr.getBoolean("required");
					if(!idColumn.equals(columnName)) {   //只有当前字段名与ID名不同时，才以行的方式循环出来，然后再将ID字段的内容插入到第一个里
						sb.append(t(1) + "<tr>" + br);
						sb.append(t(2) + "<td>" + br);
							sb.append(t(3) + "<div style=\"padding-top:10px;\">" + br);
								if(!b) {
									sb.append(t(4) + idColumnInputString + br);
									b=true;
								}
								if(required) {       //不能为空时
									sb.append(t(4) + columnLabel + "：" + "<input name=\"" + tableName +"." + columnName + "\" id=\"" + columnName + "\" style=\"width:150px;\" class=\"easyui-" + inputType + "\"  required=\"true\" missingMessage=\"" + columnLabel + "不能为空!\"></input>" + br);
								}else {
									sb.append(t(4) + columnLabel + "：" + "<input name=\"" + tableName +"." + columnName + "\" id=\"" + columnName + "\" style=\"width:150px;\" class=\"easyui-" + inputType + "\"></input>" + br);
								}
							sb.append(t(3) + "</div>" + br);
						sb.append(t(2) + "</td>" + br);
						sb.append(t(1) + "</tr>" + br);
					}
				}
				
			}
		sb.append("</table>" + br);
		
		sb.append(br);
		//保存和取消保存按钮
		sb.append("<div id=\"formBtn\">" + br);
			sb.append(t(1) + "<a href=\"#\" id=\"saveBtn\" class=\"easyui-linkbutton\" iconCls=\"icon-ok\" onclick=\"saveAdd()\">保存</a>" + br);
			sb.append(t(1) + "<a href=\"#\" id=\"\" class=\"easyui-linkbutton\" iconCls=\"icon-cancel\" onclick=\"doCancel()\">取消</a>" + br);
		sb.append("</div>" + br);
		
		File f = null;
		FileWriter fw = null;
		try {
			f = new File(path);
			fw = new FileWriter(f);
			
			fw.write(sb.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(!BlankUtils.isBlank(fw)) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
	}
	
	/**
	 * 创建控制类
	 * 
	 * @param packageInfo
	 * 			包信息，需要完成的，包括：package 关键字和结尾的分号，如：package com.callke8.system.callerid;
	 * @param controllerName
	 */
	public static void createController(String packageInfo,String controllerName,String daoName,String tableName,List<Record> paramList,List<Record> columnList,String idColumn,String path) {
		
		String br = "\r\n";
		
		StringBuilder sb = new StringBuilder();
		//包名：package com.callke8.system.callerid;
		sb.append(packageInfo + br);
		
		sb.append(br);
		//引入相关包
		sb.append("import java.util.*;" + br);
		sb.append("import com.callke8.common.IController;" + br);
		sb.append("import com.callke8.utils.*;" + br);
		sb.append("import com.jfinal.core.Controller;" + br);
		sb.append("import com.jfinal.plugin.activerecord.Record;" + br);
		sb.append("import net.sf.json.JSONArray;" + br);
		
		sb.append(br);
		//创建类名
		sb.append("public class " + controllerName + " extends Controller implements IController  {" + br);
		
			sb.append(br);
			//一、创建index 方法
			sb.append(t(1) + "@Override" + br);
			sb.append(t(1) + "public void index() {" + br);
				sb.append(t(2) + "render(\"list.jsp\");" + br);
			sb.append(t(1) + "}" + br);
		
			sb.append(br);
			//二、创建datagrid 方法
			sb.append(t(1) + "@Override" + br);
			sb.append(t(1) + "public void datagrid() {" + br);
			
				sb.append(t(2) + "Integer pageSize = BlankUtils.isBlank(getPara(\"rows\"))?1:Integer.valueOf(getPara(\"rows\"));" + t(1) + "//页数" +  br);    //页数
				sb.append(t(2) + "Integer pageNumber = BlankUtils.isBlank(getPara(\"page\"))?1:Integer.valueOf(getPara(\"page\"));" + t(1) + "//每页的数量" + br);  //每页的数量
				
				sb.append(br);
				StringBuilder paramSb = new StringBuilder();   //定义一个参数列表，向DAO请求
				paramSb.append("pageNumber,pageSize");
			
				//遍历参数，获取传入的参数
				if(!BlankUtils.isBlank(paramList) && paramList.size()>0) {
					for(Record pr:paramList) {
						if(pr.getStr("type").equals("int")) {    //如果是int类型，需要做些转换
							sb.append(t(2) + "int " + pr.getStr("paramName") + " = BlankUtils.isBlank(getPara(\"" + pr.getStr("paramName") + "\"))?0:Integer.valueOf(getPara(\"" + pr.getStr("paramName") + "\"));" + br);  //每页的数量
						}else {
							sb.append(t(2) + "String " + pr.getStr("paramName") + " = getPara(\"" + pr.getStr("paramName") + "\");" + br);
						}
						paramSb.append("," + pr.getStr("paramName"));
					}
				}
				
				sb.append(br);
				
				
				sb.append(t(2) + "Map map = " + daoName + ".dao.get" + daoName + "ByPaginateToMap(" + paramSb.toString() + ");" + br);
				sb.append(t(2) + "renderJson(map);" + br);
				
			sb.append(t(1) + "}" + br);
			
			sb.append(br);
			//三、创建add方法
			sb.append(t(1) + "@Override" + br);
			sb.append(t(1) + "public void add() {" + br);
			
				sb.append(t(2) + daoName + " formData = getModel(" + daoName + ".class,\"" + tableName + "\");" + br);
				
				sb.append(br);
				
				sb.append(t(2) + "boolean b = " + daoName + ".dao.add(formData);" + br);
				
				sb.append(t(2) + "if(b) {" + br);
					sb.append(t(3) + "render(RenderJson.success(\"新增记录成功!\"));" + br);
				sb.append(t(2) + "}else {" + br);
					sb.append(t(3) + "render(RenderJson.error(\"新增记录失败!\"));" + br);
				sb.append(t(2) + "}" + br);
			
			sb.append(t(1) + "}" + br);
			
			sb.append(br);
			//三、创建update方法
			sb.append(t(1) + "@Override" + br);
			sb.append(t(1) + "public void update() {" + br);
			
				sb.append(t(2) + daoName + " formData = getModel(" + daoName + ".class,\"" + tableName + "\");" + br);
				
				sb.append(br);
				StringBuilder sbParam = new StringBuilder();    //定义一个要传入 update 方法的参数列表，比如 customerTel,customerName,customer 等。
				String idColumnCamel = null;  //定义一个ID那一列的驼峰列名
				for(Record cr:columnList) {
					sb.append(t(2) + dataType.get(cr.getStr("type")) + " " + cr.getStr("columnNameCamel") + " = formData.get(\"" + cr.getStr("columnName") + "\");" + br);
					if(!cr.getStr("columnName").equals(idColumn)) {    //非 ID的字段放在前面，ID字段放在最后
						sbParam.append(cr.getStr("columnNameCamel") + ",");
					}else {
						idColumnCamel = cr.getStr("columnNameCamel");
					}
				}
				
				sbParam.append(idColumnCamel);    //最后，将ID驼峰列名添加上去
				
				
				sb.append(br);
				sb.append(t(2) + "boolean b = " + daoName + ".dao.update(" + sbParam.toString() + ");" + br);
				sb.append(t(2) + "if(b) {" + br);
					sb.append(t(3) + "render(RenderJson.success(\"修改成功!\"));" + br);
				sb.append(t(2) + "}else {" + br);
					sb.append(t(3) + "render(RenderJson.error(\"修改失败!\"));" + br);
				sb.append(t(2) + "}" + br);
				
				
			sb.append(t(1) + "}" + br);
			
			sb.append(br);
			//四、创建delete方法
			sb.append(t(1) + "@Override" + br);
			sb.append(t(1) + "public void delete() {" + br);
				sb.append(t(2) + "String id = getPara(\"id\");" + br);
				
				sb.append(br);
				sb.append(t(2) + "boolean b = " + daoName + ".dao.deleteById(id);" + br);
				sb.append(t(2) + "if(b) {" + br);
					sb.append(t(3) + "render(RenderJson.success(\"删除成功!\"));" + br);
				sb.append(t(2) + "}else {" + br);
					sb.append(t(3) + "render(RenderJson.error(\"删除失败!\"));" + br);
				sb.append(t(2) + "}" + br);
			sb.append(t(1) + "}" + br);
			
			//--------结束类---------
		sb.append("}" + br);
		
		File f = null;
		FileWriter fw = null;
		try {
			f = new File(path);
			fw = new FileWriter(f);
			
			fw.write(sb.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(!BlankUtils.isBlank(fw)) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 创建DAO类
	 * 
	 * @param packageInfo
	 * 			包信息，需要完成的，包括：package 关键字和结尾的分号，如：package com.callke8.system.callerid;
	 * @param controllerName
	 */
	public static void createDao(String packageInfo,String controllerName,String daoName,String tableName,List<Record> paramList,List<Record> columnList,String idColumn,String path) {
		
		String br = "\r\n";
		
		StringBuilder sb = new StringBuilder();
		//包名：package com.callke8.system.callerid;
		sb.append(packageInfo + br);
		
		sb.append(br);
		//引入相关包
		sb.append("import java.util.*;" + br);
		sb.append("import com.callke8.utils.*;" + br);
		sb.append("import com.jfinal.plugin.activerecord.*;" + br);
		sb.append(br);
		//创建类名
		sb.append("public class " + daoName + " extends Model<" + daoName + ">  {" + br);
		
			sb.append(br);
			
			sb.append(t(1) + "private static final long serialVersionUID = 1L;" + br);
			sb.append(t(1) + "public static " + daoName + " dao = new " + daoName + "();" + br);
			
			sb.append(br);
			
			StringBuilder paramSb = new StringBuilder();           //参数列表，不带类型
			paramSb.append("pageNumber,pageSize");
			StringBuilder paramSbAddType = new StringBuilder();    //参数列表，带类型
			paramSbAddType.append("int pageNumber,int pageSize");
			for(Record pr:paramList) {
				paramSb.append("," + pr.getStr("paramName"));
				paramSbAddType.append("," + dataType.get(pr.getStr("type")) + " " + pr.getStr("paramName"));
			}
			//一、创建查询分页的类
			sb.append(t(1) + "public Page get" + daoName + "ByPaginate(" + paramSbAddType.toString() + ") {" + br);
				sb.append(br);
				sb.append(t(2) + "StringBuilder sb = new StringBuilder();" + br);
				sb.append(t(2) + "Object[] pars = new Object[10];" + br);
				sb.append(t(2) + "int index = 0;" + br);
				sb.append(br);
				sb.append(t(2) + "sb.append(\"from " + tableName + " where 1=1\");" + br);
				sb.append(br);
				sb.append(t(2) + "//条件判断暂时不自动添加" + br);
				sb.append(br);
				sb.append(t(2) + "Page<Record> p = Db.paginate(pageNumber, pageSize, \"select *\", sb.toString()+\" ORDER BY " + idColumn + " DESC\",ArrayUtils.copyArray(index, pars));" + br);
				sb.append(t(2) + "return p;" + br);
			sb.append(t(1) + "}" + br);
			
			sb.append(br);
			//二、创建查询分页，并将结果以 Map 返回
			sb.append(t(1) + "public Map get" + daoName + "ByPaginateToMap(" + paramSbAddType.toString() + ") {" + br);
				sb.append(br);
				sb.append(t(2) + "Page<Record> p =  get" + daoName + "ByPaginate(" + paramSb.toString() + ");" + br);
				sb.append(br);
				sb.append(t(2) + "int total = p.getTotalRow();     //取出总数量" + br);
				sb.append(br);
				sb.append(t(2) + "Map map = new HashMap();" + br);
				sb.append(t(2) + "map.put(\"total\", total);" + br);
				sb.append(t(2) + "map.put(\"rows\", p.getList());" + br);
				sb.append(br);
				sb.append(t(2) + "return map;" + br);
			sb.append(t(1) + "}" + br);
			
			//三、创建新增方法,传入 daoName
			sb.append(t(1) + "public boolean add(" + daoName + " formData) {" + br);
				sb.append(br);
				sb.append(t(2) + "Record r = new Record();" + br);
				for(Record cr:columnList) {
					if(!idColumn.equals(cr.getStr("columnName"))) {
						sb.append(t(2) + "r.set(\"" + cr.getStr("columnName") + "\", formData.get(\"" + cr.getStr("columnName") + "\"));" + br);
					}
				}
				sb.append(br);
				sb.append(t(2) + "return add(r);" + br);
			sb.append(t(1) + "}" + br);
			
			sb.append(br);
			//四、创建新增方法,传入 Record
			sb.append(t(1) + "public boolean add(Record record) {" + br);
				sb.append(br);
				sb.append(t(2) + "boolean b = Db.save(\"" + tableName + "\", \"" + idColumn + "\", record);" + br);
				sb.append(t(2) + "return b;" + br);
				sb.append(br);
			sb.append(t(1) + "}" + br);
			
			sb.append(br);
			//五、创建更新方法
			StringBuilder sqlSb = new StringBuilder();    //定义 sql 语句
			sqlSb.append("update " + tableName + " set ");
			StringBuilder sbParamAddDataType = new StringBuilder();    //定义一个要传入 update 方法的参数列表，比如 String customerTel,String customerName,int id 等。注意 ID 的那一列放在最后
			StringBuilder sbParam = new StringBuilder();               //定 义一个不加数据类型的，即是 customerTel,customerName,id 这样的。
			String idColumnCamelAddDataType = null;  //定义一个ID那一列的驼峰列名
			String idColumnCamel = null;             //定义一个不加数据类型的
			for(Record cr:columnList) {
				if(!cr.getStr("columnName").equals(idColumn)) {    //非 ID的字段放在前面，ID字段放在最后
					sbParamAddDataType.append(dataType.get(cr.getStr("type")) + " " + cr.getStr("columnNameCamel") + ",");
					sbParam.append(cr.getStr("columnNameCamel") + ",");
					sqlSb.append(cr.getStr("columnName") + "=?,");
				}else {
					idColumnCamelAddDataType = dataType.get(cr.getStr("type")) + " " + cr.getStr("columnNameCamel");
					idColumnCamel = cr.getStr("columnNameCamel");
				}
			}
			
			//sqlSb 的语句，至此只写到  update table_name set XXX=?,YYY=?, 需要将最后一个逗号去掉，然后再加 where 
			String sqlContent = sqlSb.toString();
			sqlContent = sqlContent.substring(0, sqlContent.length()-1);   //去掉最后一个逗号
			//然后再加上 where 信息
			sqlContent += " where " + idColumn + "=?";
			//System.out.println("sqlContent的内容为:" + sqlContent);   
			
			sbParamAddDataType.append(idColumnCamelAddDataType);    //最后，将ID驼峰列名添加上去
			sbParam.append(idColumnCamel);
			
			sb.append(t(1) + "public boolean update(" + sbParamAddDataType.toString() + ") {" + br);
				sb.append(br);
				sb.append(t(2) + "boolean b = false;" + br);
				sb.append(t(2) + "String sql = \"" + sqlContent + "\";" + br);
				sb.append(br);
				sb.append(t(2) + "int count = Db.update(sql," + sbParam.toString() + ");" + br);
				sb.append(t(2) + "if(count > 0) {" + br);
				sb.append(t(3) + "b = true;" + br);
				sb.append(t(2) + "}" + br);
				sb.append(t(2) + "return b;" + br);
				sb.append(br);
			sb.append(t(1) + "}" + br);
			
			sb.append(br);
			//六、根据ID，即出记录
			sb.append(t(1) + "public " + daoName + " get" + daoName + "ById(" + idColumnCamelAddDataType + "){" + br);
				sb.append(br);
				sb.append(t(2) + "String sql = \"select * from " + tableName + " where " + idColumn + "=?\";" + br);
				sb.append(t(2) + daoName + " entity = findFirst(sql, " + idColumnCamel + ");" + br);
				sb.append(t(2) + "return entity;" + br);
				sb.append(br);
			sb.append(t(1) + "}" + br);
			
			sb.append(br);
			//七、根据ID，删除记录
			sb.append(t(1) + "public boolean deleteById(String id) {" + br);
				sb.append(br);
				sb.append(t(2) + "boolean b = false;" + br);
				sb.append(t(2) + "int count = 0;" + br);
				sb.append(t(2) + "count = Db.update(\"delete from " + tableName + " where " + idColumn + "=?\",id);" + br);
				
				sb.append(t(2) + "if(count > 0) {" + br);
					sb.append(t(3) + "b = true;" + br);
				sb.append(t(2) + "}" + br);
				sb.append(t(2) + "return b;" + br);
				sb.append(br);
			sb.append(t(1) + "}" + br);
			
			//--------结束类---------
		sb.append("}" + br);
		
		File f = null;
		FileWriter fw = null;
		try {
			f = new File(path);
			fw = new FileWriter(f);
			
			fw.write(sb.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(!BlankUtils.isBlank(fw)) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 水平制表符，i表示是几个\t
	 * 	
	 * @param i
	 * @return
	 */
	public static String t(int i) {
		
		String rs = "";
		
		if(i<=0) {
			return null;
		}else {
			for(int j=0;j<i;j++) {
				rs += "\t";
			}
			return rs;
		}
	}

	public static void main(String[] args) {
		
		List<Record> paramList = new ArrayList<Record>();
		Record p1 = new Record();
		p1.set("paramName", "reminderType");
		p1.set("paramLabel", "催缴类型");
		p1.set("type", "varchar");
		p1.set("required", false);
		Record p2 = new Record();
		p2.set("paramName", "numberOrder");
		p2.set("paramLabel", "催缴序号");
		p2.set("type", "int");
		p2.set("required", false);
		paramList.add(p1);
		paramList.add(p2);
		
		List<Record> columnList = new ArrayList<Record>();
		Record c1 = new Record();
		c1.set("columnName","ID");
		c1.set("columnNameCamel","id");     //驼峰式字段名
		c1.set("columnLabel","ID");
		c1.set("type", "int");
		c1.set("required", false);
		Record c2 = new Record();
		c2.set("columnName","REMINDER_TYPE");
		c2.set("columnNameCamel","reminderType");     //驼峰式字段名
		c2.set("columnLabel","催缴类型");
		c2.set("type", "varchar");
		c2.set("required", true);
		Record c3 = new Record();
		c3.set("columnName","NUMBER_ORDER");
		c3.set("columnNameCamel","numberOrder");     //驼峰式字段名
		c3.set("columnLabel","催缴序号");
		c3.set("type", "int");
		c3.set("required", true);
		
		columnList.add(c1);
		columnList.add(c2);
		columnList.add(c3);
		
		String title = "催缴类型管理";
		String urlInfo = "sysReminderType";
		String idColumn = "ID";
		
		String tableName = "sys_reminder_type";     //表名
		
		String listJspPath = "D:/iotest/list.jsp";                       //list.jsp 文件存放路径
		String formJspPath = "D:/iotest/_form.jsp";                      //_form.jsp 文件存放路径
		String formFullPath = "/system/remindertype/_form.jsp";              //表单的全路径，用于包含表单
		
		
		String packageInfo = "package com.callke8.system.remindertype;";     //控制类和DAO的包信息
		String controllerPath = "D:/iotest/SysReminderTypeController.java";  //控制类文件存放路径
		String controllerName = "SysReminderTypeController";                 //控制类类名
		
		String daoPath = "D:/iotest/SysReminderType.java";					 //DAO类文件存放路径
		String daoName = "SysReminderType";                                  //DAO类类名
		
		//创建 list.jsp 文件
		createListJsp(title,tableName,paramList,columnList,idColumn,urlInfo,listJspPath,formFullPath);
		//创建表单文件
		createFormJsp(tableName,columnList,idColumn,formJspPath);
		
		//创建控制类
		createController(packageInfo,controllerName,daoName,tableName,paramList,columnList,idColumn,controllerPath);
		//创建DAO类
		createDao(packageInfo,controllerName,daoName,tableName,paramList,columnList,idColumn,daoPath);
		
	}

}
