package com.callke8.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.plugin.activerecord.Record;


/**
 * 导出excel工具
 * 
 * 使用方法：
 * 	(1) 第一步 new 工具,并将List<Record>数据传入,同时将 response 传入 
 * 	 ExcelExportUtil export = new ExcelExportUtil(new ArrayList<Record>(),getResponse());
 * 
 *  (2) 第二步,指定字段标题(headers[])、列指针(columns[])、文件名（fileName-可以为空）、表名(sheetName-可以为空)、字段宽度（cellWidth-可为空）
 *  		   export.headers(headers[]).columns(columns[]).fileName(String fileName).sheetName(String sheetName).cellWidth(int callWidth).export();
 * 			   export.headers(headers[]).columns(columns[]).export();
 * @author hwz
 *
 */
public class ExcelExportUtil {

	private List<String> lines  = new ArrayList<String>();
	private List<Record> data = new ArrayList<Record>();
	private String columns[];
	private String headers[];
	private String fileName = "exportExcel";	//默认导出的文件名字
	private String sheetName = "Sheet";   		//默认表的名字
	private int cellWidth = 54;           		//字段的默认宽度54
	private int sheetSize = Integer.valueOf(MemoryVariableUtil.autoCallTaskMap.get("sheet_size"));                  //每页的数据数量
	
	private ServletOutputStream sos = null;
	private HttpServletResponse res = null;
	private BufferedOutputStream bos = null;
	
	public ExcelExportUtil(List<Record> data,HttpServletResponse res) {
		this.data = data;
		this.res = res;
	}
	
	public void execExport() {
		
		getWorkbookHead();
		
		arrangeData();
		
		getWorkbookTail();
		
		try {
			fileName = new String(fileName.getBytes(),"ISO-8859-1");
			res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			res.setContentType("application/octet-stream");
			res.setContentType("application/OCTET-STREAM;charset=UTF-8");
			
			sos = res.getOutputStream();
		
			bos = new BufferedOutputStream(sos);
			
			for(String line:lines) {
				bos.write(line.getBytes("UTF-8"));
			}
			
			bos.flush();
			
			bos.close();
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(sos!=null) {
					sos.close();
				}
				if(bos!=null) {
					bos.close();
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public ExcelExportUtil headers(String headers[]) {
		this.headers = headers;
		return this;
	}
	
	public ExcelExportUtil columns(String columns[]) {
		this.columns = columns;
		return this;
	}
	
	public ExcelExportUtil fileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
	
	public ExcelExportUtil cellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
		return this;
	}
	
	public ExcelExportUtil sheetName(String sheetName) {
		this.sheetName = sheetName;
		return this;
	}
	
	
	
	/**
	 * 整理数据部分,需要根据数量来决定是一张表，还是两张表
	 */
	public void arrangeData() {
		
		int dataCount = data.size();     //取出数据的数量
		
		//计算 sheet 表的数量
		int sheetCount = (dataCount + sheetSize -1)/sheetSize;
		
		//列数量
		int columnCount = headers.length;
		
		//如果表的数量为空时,第一页必须输出，不然无法打开 xls 文件
		if(sheetCount == 0) {
			
			//生成 sheet 表的数据
			lines.add("<Worksheet ss:Name=\"" + sheetName + "1" + "\">");
			lines.add("<Table ss:ExpandedColumnCount=\"" + columnCount + "\" x:FullColumns=\"1\" x:FullRows=\"1\" ss:DefaultColumnWidth=\"54\" ss:DefaultRowHeight=\"14.25\">");
			
			//设置每列的宽度
			for(int j = 1; j<= columnCount; j++) {   
				lines.add("<Column ss:AutoFitWidth=\"0\" ss:Width=\"" + cellWidth + "\"/>");
			}
			
			//输出表头
			lines.add("<Row>");
			for(int k = 0; k< columnCount;k++) {
				lines.add("<Cell><Data ss:Type=\"String\">" + headers[k] + "</Data></Cell>");
			}
			lines.add("</Row>");
			
			//table结束
			lines.add("</Table>");
			
			lines.add("<WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">");
			lines.add("<PageSetup>");
			lines.add("<Header x:Margin=\"0.3\"/>");
			lines.add("<Footer x:Margin=\"0.3\"/>");
			lines.add("<PageMargins x:Bottom=\"0.75\" x:Left=\"0.7\" x:Right=\"0.7\" x:Top=\"0.75\"/>");
			lines.add("</PageSetup>");
			lines.add("<ProtectObjects>False</ProtectObjects>");
			lines.add("<ProtectScenarios>False</ProtectScenarios>");
			lines.add("</WorksheetOptions>");
			lines.add("</Worksheet>");
			
		}else {
		
			for(int i = 1; i <= sheetCount; i++) {
				
				//当前页面的开始找数据的下标
				int startIndex =  (i - 1) * sheetSize;
				int endIndex = startIndex + sheetSize - 1;
				
				//循环生成每个 sheet 表的数据
				lines.add("<Worksheet ss:Name=\"" + sheetName + i + "\">");
				lines.add("<Table ss:ExpandedColumnCount=\"" + columnCount + "\" x:FullColumns=\"1\" x:FullRows=\"1\" ss:DefaultColumnWidth=\"54\" ss:DefaultRowHeight=\"14.25\">");
				
				//设置每列的宽度
				for(int j = 1; j<= columnCount; j++) {   
					lines.add("<Column ss:AutoFitWidth=\"0\" ss:Width=\"" + cellWidth + "\"/>");
				}
				
				//输出表头
				lines.add("<Row>");
				for(int k = 0; k< columnCount;k++) {
					lines.add("<Cell><Data ss:Type=\"String\">" + headers[k] + "</Data></Cell>");
				}
				lines.add("</Row>");
				
				int index = startIndex;
				while(!BlankUtils.isBlank(data.get(index))) {    // 如果取出来的对象为空时,输出数据
					
					Record dataRecord = data.get(index);
					
					lines.add("<Row>");
					for(int x = 0; x < columns.length; x++) {
						lines.add("<Cell><Data ss:Type=\"String\">" + dataRecord.get(columns[x]) + "</Data></Cell>");
					}
					lines.add("</Row>");
					
					
					if(index < endIndex && index < (dataCount -1)) {
						index ++;
					}else {
						break;
					}
					
				}
				
				//table结束
				lines.add("</Table>");
				
				lines.add("<WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">");
				lines.add("<PageSetup>");
				lines.add("<Header x:Margin=\"0.3\"/>");
				lines.add("<Footer x:Margin=\"0.3\"/>");
				lines.add("<PageMargins x:Bottom=\"0.75\" x:Left=\"0.7\" x:Right=\"0.7\" x:Top=\"0.75\"/>");
				lines.add("</PageSetup>");
				lines.add("<ProtectObjects>False</ProtectObjects>");
				lines.add("<ProtectScenarios>False</ProtectScenarios>");
				lines.add("</WorksheetOptions>");
				lines.add("</Worksheet>");
			}
		}
	}
	
	/**
	 * 整理数据部分,需要根据数量来决定是一张表，还是两张表
	 */
	public void arrangeData_bak() {
		
		lines.add("<Worksheet ss:Name=\"Sheet1\">");
		lines.add("<Table ss:ExpandedColumnCount=\"3\" ss:ExpandedRowCount=\"3\" x:FullColumns=\"1\" x:FullRows=\"1\" ss:DefaultColumnWidth=\"54\" ss:DefaultRowHeight=\"14.25\">");
		lines.add("<Column ss:AutoFitWidth=\"0\" ss:Width=\"101.25\"/>");
		lines.add("<Column ss:AutoFitWidth=\"0\" ss:Width=\"87.75\"/>");
		lines.add("<Column ss:AutoFitWidth=\"0\" ss:Width=\"134.25\"/>");
		lines.add("<Row>");
		lines.add("<Cell><Data ss:Type=\"String\">号码</Data></Cell>");
		lines.add("<Cell><Data ss:Type=\"String\">姓名</Data></Cell>");
		lines.add("</Row>");
		lines.add("<Row>");
		lines.add("<Cell><Data ss:Type=\"String\">13512771995</Data></Cell>");
		lines.add("<Cell><Data ss:Type=\"String\">李工</Data></Cell>");
		lines.add("</Row>");
		lines.add("<Row>");
		lines.add("<Cell><Data ss:Type=\"String\">13512771995</Data></Cell>");
		lines.add("<Cell><Data ss:Type=\"String\">张工</Data></Cell>");
		lines.add("</Row>");
		lines.add("</Table>");
		lines.add("<WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">");
		lines.add("<PageSetup>");
		lines.add("<Header x:Margin=\"0.3\"/>");
		lines.add("<Footer x:Margin=\"0.3\"/>");
		lines.add("<PageMargins x:Bottom=\"0.75\" x:Left=\"0.7\" x:Right=\"0.7\" x:Top=\"0.75\"/>");
		lines.add("</PageSetup>");
		lines.add("<Print>");
		lines.add("<ValidPrinterInfo/>");
		lines.add("<PaperSizeIndex>9</PaperSizeIndex>");
		lines.add("<HorizontalResolution>-1</HorizontalResolution>");
		lines.add("<VerticalResolution>-1</VerticalResolution>");
		lines.add("</Print>");
		lines.add("<Panes>");
		lines.add("<Pane>");
		lines.add("<Number>3</Number>");
		lines.add("<ActiveRow>2</ActiveRow>");
		lines.add("<ActiveCol>1</ActiveCol>");
		lines.add("</Pane>");
		lines.add("</Panes>");
		lines.add("<ProtectObjects>False</ProtectObjects>");
		lines.add("<ProtectScenarios>False</ProtectScenarios>");
		lines.add("</WorksheetOptions>");
		lines.add("</Worksheet>");
		
	}
	
	/**
	 * 得到Workbook的头部
	 * 
	 */
	public void getWorkbookHead() {
		
		lines.add("<?xml version=\"1.0\"?>");
		lines.add("<?mso-application progid=\"Excel.Sheet\"?>");
		lines.add("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
		//一、DocumentProperties
		lines.add("<DocumentProperties xmlns=\"urn:schemas-microsoft-com:office:office\">");
		lines.add("<Created>2015-06-05T18:19:34Z</Created>");
		lines.add("<LastSaved>2017-03-10T06:17:34Z</LastSaved>");
		lines.add("<Version>16.00</Version>");
		lines.add("</DocumentProperties>");
		
		//二、OfficeDocumentSettings
		lines.add("<OfficeDocumentSettings xmlns=\"urn:schemas-microsoft-com:office:office\">");
		lines.add("<AllowPNG/>");
		lines.add("<RemovePersonalInformation/>");
		lines.add("</OfficeDocumentSettings>");
		
		//三、ExcelWorkbook
		lines.add("<ExcelWorkbook xmlns=\"urn:schemas-microsoft-com:office:excel\">");
		lines.add("<WindowHeight>12645</WindowHeight>");
		lines.add("<WindowWidth>22260</WindowWidth>");
		lines.add("<WindowTopX>0</WindowTopX>");
		lines.add("<WindowTopY>0</WindowTopY>");
		lines.add("<ActiveSheet>1</ActiveSheet>");
		lines.add("<ProtectStructure>False</ProtectStructure>");
		lines.add("<ProtectWindows>False</ProtectWindows>");
		lines.add("</ExcelWorkbook>");
		
		//四、Styles
		lines.add("<Styles>");
		lines.add("<Style ss:ID=\"Default\" ss:Name=\"Normal\">");
		lines.add("<Alignment ss:Vertical=\"Bottom\"/>");
		lines.add("<Borders/>");
		lines.add("<Font ss:FontName=\"等线\" x:CharSet=\"134\" ss:Size=\"11\" ss:Color=\"#000000\"/>");
		lines.add("<Interior/>");
		lines.add("<NumberFormat/>");
		lines.add("<Protection/>");
		lines.add("</Style>");
		lines.add("</Styles>");
		
	}
	
	/**
	 * 得到Workbook的尾部
	 * 
	 * @return
	 */
	public void getWorkbookTail() {
		
		lines.add("</Workbook>");
		
	}
	
}
